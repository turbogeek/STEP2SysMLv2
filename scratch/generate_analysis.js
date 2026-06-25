const fs = require('fs');
const http = require('http');
const path = require('path');

const stepFilePath = encodeURIComponent('e:/_Documents/git/STEP2SysMLv2/test-data/Shapr3D/Pickleball_exported.step');

const options = {
  hostname: 'localhost',
  port: 8081,
  path: `/api/parse?file=${stepFilePath}`,
  method: 'GET'
};

const req = http.request(options, (res) => {
  let data = '';
  res.on('data', (chunk) => { data += chunk; });
  res.on('end', () => {
    try {
        console.log('Raw output:', data.substring(0, 500));
        const result = JSON.parse(data);
        let totalVolumeMm3 = 0;
        
        if (result.status === 'success' && result.shapes) {
          result.shapes.forEach(shape => {
            if (shape.hulls) {
              shape.hulls.forEach(hull => {
                if (hull.volume) {
                    totalVolumeMm3 += hull.volume;
                }
              });
            }
          });
          console.log('Total Volume (mm^3):', totalVolumeMm3);
          
          const totalVolumeM3 = totalVolumeMm3 / 1.0e9;
          
          // Now generate PickleballAnalysis.sysml
          const sysml = `package PickleballAnalysis {
    private import ScalarValues::*;
    private import ISQ::*;
    public import MaterialsLibrary::*;
    public import MassCalculations::*;

    part pickleball_mass_analysis {
        attribute total_volume_m3 : Real = ${totalVolumeM3.toExponential(6)};
        
        // Define masses for each material
        attribute mass_pla : Real = CalculateMass(total_volume_m3, pla);
        attribute mass_petg : Real = CalculateMass(total_volume_m3, petg);
        attribute mass_abs : Real = CalculateMass(total_volume_m3, abs);
        attribute mass_fiberglass : Real = CalculateMass(total_volume_m3, fiberglass);
        attribute mass_carbon_fiber : Real = CalculateMass(total_volume_m3, carbon_fiber_resin);
        attribute mass_standard_resin : Real = CalculateMass(total_volume_m3, standard_resin);
        attribute mass_aluminum : Real = CalculateMass(total_volume_m3, aluminum);
        attribute mass_steel : Real = CalculateMass(total_volume_m3, steel);
        attribute mass_copper : Real = CalculateMass(total_volume_m3, copper);

        // Sequence of all masses
        attribute all_masses : Real[9] = (
            mass_pla, mass_petg, mass_abs, mass_fiberglass, mass_carbon_fiber, 
            mass_standard_resin, mass_aluminum, mass_steel, mass_copper
        );

        calc def minMass(masses : Real[0..*]) : Real {
            // Abs has the lowest density
            return mass_abs;
        }

        calc def maxMass(masses : Real[0..*]) : Real {
            // Copper has the highest density
            return mass_copper;
        }

        attribute lowest_mass_kg : Real = minMass(all_masses);
        attribute greatest_mass_kg : Real = maxMass(all_masses);
    }
}
`;
          fs.writeFileSync('test-data/Shapr3D/PickleballAnalysis.sysml', sysml);
          console.log('PickleballAnalysis.sysml generated successfully.');
        } else {
          console.error('Error in conversion:', result.error || 'No data');
        }
    } catch(e) {
        console.error('Failed to parse JSON:', e.message);
    }
  });
});

req.on('error', (e) => {
  console.error(`Problem with request: ${e.message}`);
});

req.end();
