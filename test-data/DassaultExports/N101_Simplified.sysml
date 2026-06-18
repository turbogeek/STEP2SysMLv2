package WingRibSimplified {
    private import ScalarValues::*;
    private import ISQ::*;
    public import MaterialsLibrary::*;
    public import MassCalculations::*;

    part def BoundingBoxShape {
        attribute minX : Real;
        attribute minY : Real;
        attribute minZ : Real;
        attribute maxX : Real;
        attribute maxY : Real;
        attribute maxZ : Real;
    }

    part root {
        part bounding_shape : BoundingBoxShape {
            :>> minX = -30133.118091;
            :>> minY = 8787.884000;
            :>> minZ = -184.107776;
            :>> maxX = -28604.874868;
            :>> maxY = 9433.463366;
            :>> maxZ = 208.523922;
        }

        attribute true_volume_m3 : Real = 2.2161040129;

        // Standard material calculation mapping
        attribute material : Material = aluminum;
        attribute mass_kg : Real = CalculateMass(true_volume_m3, material);
    }
}
