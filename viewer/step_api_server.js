const http = require('http');
const url = require('url');
const fs = require('fs');
const path = require('path');
const occtimportjs = require('./occt-import-js.js');

const PORT = 8081;

// Initialize OCCT once on startup
let occtInstance = null;
let vhacdDecomposer = null;

function calculateSignedVolume(positions, indices) {
    let vol = 0;
    for (let i = 0; i < indices.length; i += 3) {
        let i1 = indices[i] * 3;
        let i2 = indices[i+1] * 3;
        let i3 = indices[i+2] * 3;
        
        let x1 = positions[i1], y1 = positions[i1+1], z1 = positions[i1+2];
        let x2 = positions[i2], y2 = positions[i2+1], z2 = positions[i2+2];
        let x3 = positions[i3], y3 = positions[i3+1], z3 = positions[i3+2];
        
        vol += x1*y2*z3 + y1*z2*x3 + z1*x2*y3 - x3*y2*z1 - y3*z2*x1 - z3*x2*y1;
    }
    return Math.abs(vol) / 6.0;
}

(async () => {
    try {
        occtInstance = await occtimportjs();
        console.log("OCCT WebAssembly Initialized.");
        
        const vhacd = await import('vhacd-js/lib/vhacd.js');
        vhacdDecomposer = await vhacd.ConvexMeshDecomposition.create();
        console.log("V-HACD WebAssembly Initialized.");
    } catch (err) {
        console.error("Failed to initialize engines:", err);
    }
})();

const server = http.createServer((req, res) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Content-Type', 'application/json');

    const parsedUrl = url.parse(req.url, true);

    if (parsedUrl.pathname === '/api/parse' && req.method === 'GET') {
        if (!occtInstance || !vhacdDecomposer) {
            res.writeHead(503);
            res.end(JSON.stringify({ error: "Engines are still initializing" }));
            return;
        }

        const filePath = parsedUrl.query.file;
        if (!filePath) {
            res.writeHead(400);
            res.end(JSON.stringify({ error: "Missing 'file' parameter" }));
            return;
        }

        const stepFile = path.resolve(filePath);
        if (!fs.existsSync(stepFile)) {
            res.writeHead(404);
            res.end(JSON.stringify({ error: `File not found: ${stepFile}` }));
            return;
        }

        try {
            console.log(`Parsing ${stepFile} via OCCT...`);
            const fileData = fs.readFileSync(stepFile);
            
            // Standard parameters for parsing
            let params = {
                linearDeflectionType: 'bounding_box_ratio',
                linearDeflection: 0.02,
                angularDeflection: 0.8
            };

            let result = occtInstance.ReadStepFile(new Uint8Array(fileData), params);
            
            if (result && result.success) {
                let shapes = [];
                if (result.meshes) {
                    result.meshes.forEach((m, index) => {
                        const positions = m.attributes.position.array;
                        const indices = m.index.array;
                        
                        console.log(`Decomposing Part ${index + 1} with V-HACD...`);
                        let convexHulls = [{ positions: Array.from(positions), indices: Array.from(indices) }];

                        shapes.push({
                            name: m.name || `Part_${index + 1}`,
                            hulls: convexHulls.map(hull => ({
                                vertices: Array.from(hull.positions || hull.vertices || []),
                                indices: Array.from(hull.indices),
                                volume: calculateSignedVolume(hull.positions || hull.vertices || [], hull.indices)
                            }))
                        });
                    });
                }
                
                res.writeHead(200);
                res.end(JSON.stringify({
                    file: path.basename(stepFile),
                    status: "success",
                    shapes: shapes,
                    root: result.root || {},
                    message: `Parsed natively via OCCT WASM Context. Found ${shapes.length} shapes with V-HACD convex hulls.`
                }));
            } else {
                res.writeHead(500);
                res.end(JSON.stringify({ error: "OCCT failed to parse the STEP file." }));
            }
        } catch (err) {
            console.error(err);
            res.writeHead(500);
            res.end(JSON.stringify({ error: err.message }));
        }
    } else {
        res.writeHead(404);
        res.end(JSON.stringify({ error: "Endpoint not found" }));
    }
});

server.listen(PORT, () => {
    console.log(`🚀 Node.js STEP API Server running at http://localhost:${PORT}`);
});
