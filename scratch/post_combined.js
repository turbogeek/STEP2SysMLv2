const fs = require('fs');
const http = require('http');

const libText = fs.readFileSync('test-data/Shapr3D/MaterialsLibrary.sysml', 'utf8');
const calcText = fs.readFileSync('test-data/Shapr3D/MassCalculations.sysml', 'utf8');
const analysisText = fs.readFileSync('test-data/Shapr3D/PickleballAnalysis.sysml', 'utf8');

const combined = libText + '\n\n' + calcText + '\n\n' + analysisText;
fs.writeFileSync('scratch/combined.sysml', combined);

const postData = JSON.stringify({ filePath: 'e:/_Documents/git/STEP2SysMLv2/scratch/combined.sysml' });

const options = {
  hostname: 'localhost',
  port: 8770,
  path: '/load-sysml',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(postData)
  }
};

const req = http.request(options, (res) => {
  let data = '';
  res.on('data', (chunk) => { data += chunk; });
  res.on('end', () => {
    console.log('Response:', data);
  });
});

req.on('error', (e) => {
  console.error(`Problem with request: ${e.message}`);
});

req.write(postData);
req.end();
