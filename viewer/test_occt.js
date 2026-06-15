const fs = require('fs');
const occtimportjs = require('./occt-import-js.js');

occtimportjs().then((occt) => {
    const fileData = fs.readFileSync('../test-data/assembly_ap214.stp');
    let result = occt.ReadStepFile(new Uint8Array(fileData), null);
    console.log("Full result:", Object.keys(result));
    console.log("Success:", result.success);
    if (result.meshes) {
        console.log("Meshes count:", result.meshes.length);
        if (result.meshes.length > 0) {
            console.log("Mesh 0 keys:", Object.keys(result.meshes[0]));
            console.log("Mesh 0 name:", result.meshes[0].name);
        }
    }
}).catch(err => {
    console.error(err);
});
