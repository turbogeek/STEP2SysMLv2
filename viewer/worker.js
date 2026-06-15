importScripts('occt-import-js.js');

self.onmessage = function (e) {
    const data = e.data; // expects Uint8Array

    self.postMessage({ type: 'progress', message: 'Initializing OpenCASCADE Kernel...' });
    
    occtimportjs({
        locateFile: (path) => {
            if (path.endsWith('.wasm')) {
                return 'occt-import-js.wasm';
            }
            return path;
        }
    }).then((occt) => {
        self.postMessage({ type: 'progress', message: 'Tessellating solid geometry (this may take a moment)...' });
        
        try {
            const startTime = performance.now();
            console.log(`[Worker] Started reading STEP file (${data.byteLength} bytes)...`);
            
            self.postMessage({ type: 'progress', message: 'Parsing STEP structure (this may take a while for large files)...' });
            
            let params = {
                linearDeflectionType: 'bounding_box_ratio',
                linearDeflection: 0.02,  // 2% of bounding box (coarser mesh)
                angularDeflection: 0.8   // Allows rougher angles on curves
            };

            const parseStartTime = performance.now();
            let result = occt.ReadStepFile(data, params);
            const parseEndTime = performance.now();
            
            console.log(`[Worker] OCCT ReadStepFile completed in ${(parseEndTime - parseStartTime).toFixed(2)} ms.`);
            
            if (result.success) {
                const totalTime = performance.now() - startTime;
                console.log(`[Worker] Total processing time: ${totalTime.toFixed(2)} ms. Extracted ${result.meshes.length} meshes.`);
                self.postMessage({ type: 'done', meshes: result.meshes });
            } else {
                console.error(`[Worker] OCCT Parsing Failed.`);
                self.postMessage({ type: 'error', message: 'Failed to parse STEP file (Invalid format or unsupported entities).' });
            }
        } catch (err) {
            self.postMessage({ type: 'error', message: "OCCT Error: " + err.toString() });
        }
    }).catch(err => {
        self.postMessage({ type: 'error', message: "WASM Init Error: " + err.toString() });
    });
};
