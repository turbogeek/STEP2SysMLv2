const fs = require('fs');
const occtimportjs = require('./viewer/occt-import-js.js');

const stepFile = 'test-data/cube_ap203.stp';

occtimportjs().then((occt) => {
    let fileContent = fs.readFileSync(stepFile);
    let result = occt.ReadStepFile(fileContent, null);
    console.log("Result success:", result.success);
    console.log("Result meshes:", result.meshes ? result.meshes.length : "undefined");
}).catch(err => console.error(err));
