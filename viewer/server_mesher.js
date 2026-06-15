const fs = require('fs');
const path = require('path');
const occtimportjs = require('./occt-import-js.js');

const stepFile = process.argv[2];
const outDir = process.argv[3];

if (!stepFile || !outDir) {
    console.error("Usage: node server_mesher.js <stepFile> <outDir>");
    process.exit(1);
}

console.log(`Starting mesher for ${stepFile}...`);
console.time("TotalTime");

try {
    const fileData = fs.readFileSync(stepFile);
    
    occtimportjs().then((occt) => {
        let params = {
            linearDeflectionType: 'bounding_box_ratio',
            linearDeflection: 0.02,
            angularDeflection: 0.8
        };
        
        console.log(`Parsing STEP file with OCCT...`);
        console.time("OCCT_Read");
        let result = occt.ReadStepFile(new Uint8Array(fileData), params);
        console.timeEnd("OCCT_Read");

        if (result.success && result.meshes) {
            console.log("Result object keys: ", Object.keys(result));
            console.log(`Extracted ${result.meshes.length} meshes. Writing to cache directory ${outDir}...`);
            let count = 0;
            result.meshes.forEach((m, index) => {
                // Ensure name matches the UI assumption or fallback
                let name = m.name || `Part_${index + 1}`;
                // Clean name for filesystem
                let safeName = name.replace(/[^a-zA-Z0-9.-]/g, '_');
                let outFile = path.join(outDir, `${safeName}.json`);
                
                // Write out lightweight JSON geometry
                let geometryData = {
                    name: name,
                    color: m.color,
                    position: Array.from(m.attributes.position.array),
                    index: Array.from(m.index.array)
                };
                if (m.attributes.normal) {
                    geometryData.normal = Array.from(m.attributes.normal.array);
                }

                fs.writeFileSync(outFile, JSON.stringify(geometryData));
                count++;
            });
            console.log(`Successfully cached ${count} parts.`);
        } else {
            console.error("Failed to parse STEP file or no meshes found.");
        }
        console.timeEnd("TotalTime");
    }).catch(err => {
        console.error("Failed to initialize occt-import-js:", err);
    });

} catch (err) {
    console.error("File Read Error:", err.message);
}
