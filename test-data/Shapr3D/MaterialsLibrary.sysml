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
