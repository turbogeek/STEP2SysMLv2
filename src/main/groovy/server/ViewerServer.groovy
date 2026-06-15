package server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import groovy.json.JsonOutput
import step.StepDocument
import step.StepProduct

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors

class ViewerServer {

    static final int PORT = 8080
    static final String WEB_ROOT = "viewer"
    static final String CACHE_DIR = "cache"

    static void main(String[] args) {
        // Ensure cache directory exists
        Files.createDirectories(Paths.get(CACHE_DIR))

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0)

        // API Endpoints
        server.createContext("/api/files", new FilesHandler())
        server.createContext("/api/load_assembly", new LoadAssemblyHandler())
        server.createContext("/api/stream_part", new StreamPartHandler())
        
        // Static file serving (catch-all)
        server.createContext("/", new StaticFileHandler())

        server.setExecutor(Executors.newCachedThreadPool()) // Multi-threaded executor
        server.start()
        println "🚀 Groovy Viewer Server running at http://localhost:${PORT}/index.html"
    }

    static class FilesHandler implements HttpHandler {
        @Override
        void handle(HttpExchange exchange) {
            exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
            def query = exchange.requestURI.query
            String dirPath = "test-data"
            if (query && query.startsWith("dir=")) {
                dirPath = URLDecoder.decode(query.substring(4), "UTF-8")
            }

            def dir = new File(dirPath)
            if (!dir.exists() || !dir.isDirectory()) {
                sendJsonResponse(exchange, 404, [error: "Directory not found"])
                return
            }

            def list = []
            // Add parent dir option if not root
            if (dir.parentFile && new File(dirPath).name != "test-data") {
                list << [name: "..", path: dir.parentFile.path.replace('\\', '/'), isDir: true]
            }

            dir.listFiles()?.sort { it.name }?.each { file ->
                if (file.isDirectory() || file.name.toLowerCase().endsWith(".stp") || file.name.toLowerCase().endsWith(".step")) {
                    list << [
                            name : file.name,
                            path : file.path.replace('\\', '/'),
                            isDir: file.isDirectory()
                    ]
                }
            }
            sendJsonResponse(exchange, 200, list)
        }
    }

    static class LoadAssemblyHandler implements HttpHandler {
        @Override
        void handle(HttpExchange exchange) {
            exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
            def query = exchange.requestURI.query
            String filePath = ""
            if (query && query.startsWith("file=")) {
                filePath = URLDecoder.decode(query.substring(5), "UTF-8")
            }

            def file = new File(filePath)
            if (!file.exists()) {
                sendJsonResponse(exchange, 404, [error: "File not found"])
                return
            }

            try {
                // Parse using our REST adapter safely
                def parser = new step.StepFileParser()
                StepDocument doc = parser.parse(file)

                // Trigger background meshing pool task
                triggerBackgroundMeshing(file)
                
                // Export Graphify knowledge graph for faster hierarchy processing
                exportGraphifyFormat(doc, file)

                // Return hierarchy to UI instantly
                def jsonTree = doc.products.collect { mapProduct(it) }
                sendJsonResponse(exchange, 200, [
                        file: file.name,
                        schema: doc.schema,
                        products: jsonTree
                ])
            } catch (Exception e) {
                e.printStackTrace()
                sendJsonResponse(exchange, 500, [error: e.message])
            }
        }

        private void exportGraphifyFormat(StepDocument doc, File stepFile) {
            String hash = stepFile.name.replaceAll("[^a-zA-Z0-9.-]", "_")
            Path outDir = Paths.get(CACHE_DIR, hash, "graphify-out")
            Files.createDirectories(outDir)

            List nodes = []
            List links = []
            Map visited = [:]

            doc.products.each { root -> traverseProductGraphify(root, null, visited, nodes, links, stepFile.name) }

            String json = """{
                "directed": true,
                "multigraph": false,
                "graph": {},
                "nodes": [ ${nodes.collect { n -> """{"id":"${n.id}","label":"${n.label}","file_type":"${n.file_type}","source_file":"${n.source_file}","community":${n.community},"norm_label":"${n.norm_label}"}""" }.join(",")} ],
                "links": [ ${links.collect { l -> """{"source":"${l.source}","target":"${l.target}","relation":"${l.relation}","weight":${l.weight},"confidence":"${l.confidence}","confidence_score":${l.confidence_score}}""" }.join(",")} ]
            }"""

            File outFile = outDir.resolve("graph.json").toFile()
            outFile.write(json)
            println "[Graphify] Exported STEP hierarchy to ${outFile.absolutePath}"
            
            // Also serve this via a specific endpoint or just write it to cache
        }

        private void traverseProductGraphify(StepProduct product, StepProduct parent, Map nodeSet, List nodes, List links, String fileName) {
            if (!nodeSet.containsKey(product.entityId)) {
                nodes << [
                    id: product.entityId.toString(),
                    label: product.name ?: "Unnamed Part",
                    file_type: "code",
                    source_file: fileName,
                    community: product.isLeaf() ? 2 : 1,
                    norm_label: product.name?.toLowerCase() ?: "unnamed part"
                ]
                nodeSet[product.entityId] = true
            }

            if (parent) {
                links << [
                    source: parent.entityId.toString(),
                    target: product.entityId.toString(),
                    relation: "contains",
                    weight: 1.0,
                    confidence: "EXTRACTED",
                    confidence_score: 1.0
                ]
            }

            product.children?.each { child ->
                traverseProductGraphify(child, product, nodeSet, nodes, links, fileName)
            }
        }

        private Map mapProduct(StepProduct p) {
            return [
                    id: p.entityId,
                    partId: p.partId,
                    name: p.name,
                    children: p.children?.collect { mapProduct(it) } ?: []
            ]
        }

        private void triggerBackgroundMeshing(File stepFile) {
            // Check if caching directory for this file exists
            String hash = stepFile.name.replaceAll("[^a-zA-Z0-9.-]", "_")
            Path outDir = Paths.get(CACHE_DIR, hash)
            if (Files.exists(outDir)) {
                println "[Cache] Meshes already exist for ${stepFile.name}. Skipping background tessellation."
                return
            }

            Files.createDirectories(outDir)
            println "[Mesher] Starting background tessellation for ${stepFile.name}..."

            // We will launch a Node.js process to do the heavy lifting with high memory limits
            Thread.start {
                try {
                    meshingStatus.put(stepFile.name, true)
                    // Absolute path to the node mesher script
                    def mesherScript = new File(WEB_ROOT, "server_mesher.js").absolutePath
                    def process = new ProcessBuilder("node", "--max-old-space-size=8192", mesherScript, stepFile.absolutePath, outDir.toString())
                            .redirectErrorStream(true)
                            .start()
                            
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    String line
                    while ((line = reader.readLine()) != null) {
                        println "[NodeWorker] " + line
                    }
                    process.waitFor()
                    println "[Mesher] Completed tessellation for ${stepFile.name}."
                } catch (Exception e) {
                    println "[Mesher Error] " + e.message
                } finally {
                    meshingStatus.put(stepFile.name, false)
                }
            }
        }
    }

    static final java.util.concurrent.ConcurrentHashMap<String, Boolean> meshingStatus = new java.util.concurrent.ConcurrentHashMap<>()

    static class StreamPartHandler implements HttpHandler {
        @Override
        void handle(HttpExchange exchange) {
            exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
            def query = exchange.requestURI.query
            // Expected query: file=Assembly.stp&part=Part_1
            def params = query.split('&').collectEntries { it.split('=') as List }
            
            String fileName = params.file ? URLDecoder.decode(params.file, "UTF-8") : ""
            String partName = params.part ? URLDecoder.decode(params.part, "UTF-8") : ""
            
            // Clean names
            String hash = new File(fileName).name.replaceAll("[^a-zA-Z0-9.-]", "_")
            String safePartName = partName.replaceAll("[^a-zA-Z0-9.-]", "_")
            Path partFile = Paths.get(CACHE_DIR, hash, "${safePartName}.json")
            
            if (Files.exists(partFile)) {
                // Instantly serve cached part
                serveFile(exchange, partFile.toFile(), "application/json")
            } else if (meshingStatus.getOrDefault(new File(fileName).name, false)) {
                // Not ready yet (still meshing in background)
                sendJsonResponse(exchange, 202, [status: "processing"])
            } else {
                // Mesher is not running, and file does not exist -> No geometry for this part!
                sendJsonResponse(exchange, 404, [error: "No geometry found for this part"])
            }
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        void handle(HttpExchange exchange) {
            String path = exchange.requestURI.path
            if (path == "/" || path == "/viewer/") path = "/index.html"
            // remove /viewer prefix since WEB_ROOT is "viewer"
            if (path.startsWith("/viewer/")) path = path.substring(7)
            
            File file = new File(WEB_ROOT, path)
            if (file.exists() && !file.isDirectory()) {
                String mimeType = "text/plain"
                if (path.endsWith(".html")) mimeType = "text/html"
                else if (path.endsWith(".js")) mimeType = "application/javascript"
                else if (path.endsWith(".css")) mimeType = "text/css"
                else if (path.endsWith(".wasm")) mimeType = "application/wasm"
                
                serveFile(exchange, file, mimeType)
            } else {
                String response = "404 Not Found"
                exchange.sendResponseHeaders(404, response.length())
                exchange.responseBody.write(response.bytes)
                exchange.responseBody.close()
            }
        }
    }

    static void serveFile(HttpExchange exchange, File file, String contentType) {
        exchange.responseHeaders.add("Content-Type", contentType)
        exchange.sendResponseHeaders(200, file.length())
        file.withInputStream { is ->
            exchange.responseBody << is
        }
        exchange.responseBody.close()
    }

    static void sendJsonResponse(HttpExchange exchange, int statusCode, Object responseObj) {
        String json = stringifyJson(responseObj)
        byte[] bytes = json.getBytes("UTF-8")
        exchange.responseHeaders.set("Content-Type", "application/json")
        // CORS headers
        exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(statusCode, bytes.length)
        OutputStream os = exchange.getResponseBody()
        os.write(bytes)
        os.close()
    }

    static String stringifyJson(Object obj) {
        if (obj == null) return "null"
        if (obj instanceof String) return "\"" + obj.toString().replace("\"", "\\\"").replace("\n", "\\n") + "\""
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString()
        if (obj instanceof List) {
            return "[" + ((List)obj).collect { stringifyJson(it) }.join(",") + "]"
        }
        if (obj instanceof Map) {
            return "{" + ((Map)obj).collect { k, v -> "\"${k}\":" + stringifyJson(v) }.join(",") + "}"
        }
        return "\"" + obj.toString() + "\""
    }
}
