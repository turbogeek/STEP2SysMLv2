const fs = require('fs');
const text = fs.readFileSync('test-data/Shapr3D/shapr3d_export_2026-06-11_11h15m.step', 'utf8');
const regex = /CARTESIAN_POINT\s*\([^,]*,\s*\(([^)]+)\)\s*\)/g;
let match;
let count = 0;
while ((match = regex.exec(text)) !== null) {
    count++;
}
console.log('Points found:', count);
