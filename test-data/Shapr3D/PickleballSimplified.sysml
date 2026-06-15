package PickleballSimplified {
    private import Spatial::*;
    private import Geometry::*;
    private import ISQ::*;
    private import SI::*;
    
    public import MaterialsLibrary::*;
    public import MassCalculations::*;

    doc
    /*
     * This model manually defines the Pickleball using precise SysMLv2 Geometry primitives
     * (Sphere) rather than the faceted Polyhedron meshes generated from STEP imports.
     * It also demonstrates assigning materials and calculating mass properties.
     */

    part def Pickleball {
        // --- Geometry Definition ---
        // A standard pickleball is 73mm to 75.5mm in diameter. We will use a radius of 37mm.
        // The wall thickness is typically around 1.5mm to 2mm.
        
        part outerSurface :> Sphere {
            :>> radius = 37.0; // mm
        }
        
        part innerCutout :> Sphere {
            :>> radius = 35.0; // mm
        }

        // The true geometry is the boolean difference between outer and inner sphere (plus holes)
        // For mass calculation, we will use an explicit volume formula for a hollow sphere:
        // V = (4/3) * pi * (R_outer^3 - R_inner^3)
        // V = 4.18879 * (37^3 - 35^3) = 4.18879 * (50653 - 42875) = 4.18879 * 7778 = ~32,580 mm^3
        // 32,580 mm^3 = 0.00003258 m^3
        
        attribute volume_m3 : Real = 0.00003258; // Simplified calculated hollow volume
        
        // --- Material & Mass Properties ---
        
        // Define the material of this specific pickleball (e.g. standard PLA)
        attribute material : Material = pla;
        
        // Bind the calculation output to the mass attribute
        attribute mass_kg : Real = CalculateMass(vol_m3, material);
    }
}
