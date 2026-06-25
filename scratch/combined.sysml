package MaterialsLibrary {
    private import ScalarValues::*;
    private import ISQ::*;
    private import SI::*;

    doc
    /*
     * A reusable library of material definitions and their physical properties.
     * Sources for densities:
     * - Metals: MatWeb Material Property Data (http://www.matweb.com)
     * - 3D Printing Filaments: Prusa Material Guide & Simplify3D
     * - Resins: Formlabs Material Properties Data Sheets
     */

    attribute def Material {
        attribute name : String;
        attribute density : Real; // Density in kg/m^3
        attribute sourceReference : String;
    }

    // --- Standard Metals ---
    attribute steel : Material { 
        :>> name = "Steel"; 
        :>> density = 7850.0; 
        :>> sourceReference = "MatWeb (General Steel)"; 
    }
    attribute aluminum : Material { 
        :>> name = "Aluminum 6061"; 
        :>> density = 2700.0; 
        :>> sourceReference = "MatWeb (Al 6061-T6)"; 
    }
    attribute copper : Material { 
        :>> name = "Copper"; 
        :>> density = 8960.0; 
        :>> sourceReference = "EngineeringToolBox"; 
    }

    // --- 3D Printing Filaments & Plastics ---
    attribute pla : Material { 
        :>> name = "PLA"; 
        :>> density = 1240.0; 
        :>> sourceReference = "Prusa Material Guide"; 
    }
    attribute petg : Material { 
        :>> name = "PETG"; 
        :>> density = 1270.0; 
        :>> sourceReference = "Prusa Material Guide"; 
    }
    attribute abs : Material { 
        :>> name = "ABS"; 
        :>> density = 1040.0; 
        :>> sourceReference = "Simplify3D Density Table"; 
    }

    // --- Composites & Resins ---
    attribute fiberglass : Material { 
        :>> name = "Fiberglass"; 
        :>> density = 1520.0; 
        :>> sourceReference = "EngineeringToolBox"; 
    }
    attribute carbon_fiber_resin : Material { 
        :>> name = "Carbon Fiber Epoxy"; 
        :>> density = 1600.0; 
        :>> sourceReference = "MatWeb (CF Composite)"; 
    }
    attribute standard_resin : Material { 
        :>> name = "Standard UV Resin"; 
        :>> density = 1100.0; 
        :>> sourceReference = "Formlabs Data Sheet"; 
    }
}


package MassCalculations {
    private import ScalarValues::*;
    private import ISQ::*;
    private import SI::*;
    public import MaterialsLibrary::*;

    doc
    /*
     * Library for mass and physical property calculations.
     */

    calc def CalculateMass {
        doc /* Calculates the mass in kg given the volume in m^3 and a standard Material. */
        in vol_m3 : Real; // Volume in cubic meters
        in mat : Material;
        return mass_kg : Real = vol_m3 * mat.density;
    }
}


package PickleballAnalysis {
    private import ScalarValues::*;
    private import ISQ::*;
    public import MaterialsLibrary::*;
    public import MassCalculations::*;

    part pickleball_mass_analysis {
        attribute total_volume_m3 : Real = 2.882734e-4;
        
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

        // Lowest density is ABS, highest density is Copper
        attribute lowest_mass_kg : Real = mass_abs;
        attribute greatest_mass_kg : Real = mass_copper;
    }
}
