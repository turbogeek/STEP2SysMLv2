package step

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

/**
 * A STEP Library adapter that communicates with a separate, isolated local
 * API Server (e.g., StepParsingServer). This offloads the heavy native
 * processing (like OpenCASCADE or JSDAI) to an isolated memory context,
 * preventing crashes inside the Cameo/MagicDraw IDE.
 */
class RestStepAdapter implements StepLibraryAdapter {

    private final String serverUrl

    RestStepAdapter(String serverUrl = "http://localhost:8081") {
        this.serverUrl = serverUrl
    }

    @Override
    StepDocument readFile(File stepFile) {
        if (!stepFile.exists()) {
            throw new StepParseException("File not found: ${stepFile.absolutePath}")
        }

        try {
            // Encode the file path properly
            String encodedPath = URLEncoder.encode(stepFile.absolutePath, "UTF-8")
            URL url = new URL("${serverUrl}/api/parse?file=${encodedPath}")
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection()
            connection.setRequestMethod("GET")
            connection.setConnectTimeout(5000)
            connection.setReadTimeout(30000) // Parsing might take a bit

            int status = connection.getResponseCode()
            if (status != 200) {
                String errorText = connection.getErrorStream()?.getText("UTF-8") ?: "Unknown Error"
                throw new StepParseException("Server returned ${status}: ${errorText}")
            }

            String jsonResponse = connection.getInputStream().getText("UTF-8")
            def parsed = new JsonSlurper().parseText(jsonResponse) as Map

            List<StepShape> shapes = []
            int entityIdCounter = 1000
            
            if (parsed.shapes instanceof List) {
                parsed.shapes.each { shapeMap ->
                    def rawHulls = shapeMap.hulls as List
                    List<ConvexHull> hulls = []
                    
                    if (rawHulls) {
                        rawHulls.each { hMap ->
                            def verts = hMap.vertices as List<Number>
                            def indices = hMap.indices as List<Number>
                            
                            double[] vArray = new double[verts.size()]
                            verts.eachWithIndex { v, i -> vArray[i] = v.doubleValue() }
                            
                            int[] iArray = new int[indices.size()]
                            indices.eachWithIndex { idx, i -> iArray[i] = idx.intValue() }
                            double vol = hMap.volume != null ? (hMap.volume as Number).doubleValue() : 0.0
                            
                            hulls << new ConvexHull(vArray, iArray, vol)
                        }
                    }

                    shapes << new StepShape(
                        entityIdCounter++,
                        shapeMap.name as String,
                        hulls
                    )
                }
            }
            
            List<StepProduct> products = []
            if (parsed.root != null) {
                products << mapNodeToProduct(parsed.root as Map, entityIdCounter)
            }
            
            // Stub return for now to satisfy interface
            return new StepDocument(
                "NATIVE_PARSED", 
                parsed.file as String ?: stepFile.name, 
                "Now", 
                "hash", 
                shapes.size(), 
                products, 
                shapes
            )

        } catch (Exception e) {
            e.printStackTrace()
            throw new StepParseException("Failed to communicate with Native Parsing Server: ${e.message}", e)
        }
    }

    private StepProduct mapNodeToProduct(Map node, int idCounter) {
        String name = node.name as String ?: "Unnamed"
        List<StepProduct> children = []
        if (node.children instanceof List) {
            (node.children as List).each { child ->
                children << mapNodeToProduct(child as Map, idCounter + 1)
            }
        }
        return new StepProduct(idCounter, name.toUpperCase(), name, "", [], children)
    }
}
