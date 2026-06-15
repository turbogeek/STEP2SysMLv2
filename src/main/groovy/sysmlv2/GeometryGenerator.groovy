package sysmlv2

import geometry.BoundingBox
import geometry.BoundingBoxCalculator
import step.StepShape

class GeometryGenerator {
    
    /**
     * Translates a list of StepShape objects into SysMLv2 text.
     */
    String generate(String packageName, List<StepShape> shapes) {
        StringBuilder sb = new StringBuilder()
        
        sb.append("package ${packageName} {\n")
        sb.append("    private import ScalarValues::*;\n")
        sb.append("    private import Geometry::*;\n")
        sb.append("    private import ISQ::*;\n")
        sb.append("    public import MaterialsLibrary::*;\n")
        sb.append("    public import MassCalculations::*;\n\n")
        
        sb.append("    part def Polyhedron {\n")
        sb.append("        attribute vertices : Real[0..*];\n")
        sb.append("        attribute faces : Integer[0..*];\n")
        sb.append("    }\n\n")
        
        sb.append("    part root {\n")
        
        shapes.eachWithIndex { shape, sIdx ->
            String baseType = shape.entityType?.replaceAll("[^a-zA-Z0-9_]", "_") ?: "shape"
            if (!baseType.matches("^[a-zA-Z_].*")) {
                baseType = "shape_" + baseType
            }
            String shapePartName = "${baseType}_${sIdx}"
            
            sb.append("        part ${shapePartName} {\n")
            
            shape.hulls.eachWithIndex { hull, hIdx ->
                sb.append("            part hull_${hIdx} : Polyhedron {\n")
                
                // Format vertices as a flat list
                sb.append("                :>> vertices = (\n")
                sb.append("                    ")
                for (int i = 0; i < hull.vertices.length; i++) {
                    sb.append(String.format(java.util.Locale.US, "%.6f [mm]", hull.vertices[i]))
                    if (i < hull.vertices.length - 1) {
                        sb.append(", ")
                        if ((i + 1) % 6 == 0) sb.append("\n                    ")
                    }
                }
                sb.append("\n                );\n")
                
                // Format faces (triangles) as tuples of 3
                sb.append("                :>> faces = (\n")
                sb.append("                    ")
                for (int i = 0; i < hull.triangles.length; i += 3) {
                    sb.append(String.format("(%d, %d, %d)", hull.triangles[i], hull.triangles[i+1], hull.triangles[i+2]))
                    if (i < hull.triangles.length - 3) {
                        sb.append(", ")
                        if (((i/3) + 1) % 6 == 0) sb.append("\n                    ")
                    }
                }
                sb.append("\n                );\n")
                sb.append("            }\n")
                
                if (hull.volume > 0.0) {
                    double vol_m3 = hull.volume / 1.0e9 // convert mm^3 to m^3
                    sb.append(String.format(java.util.Locale.US, "            attribute volume_m3_%d : Real = %.8f;\n", hIdx, vol_m3))
                    sb.append("            attribute material_${hIdx} : Material = pla;\n")
                    sb.append("            attribute mass_kg_${hIdx} : Real = CalculateMass(volume_m3_${hIdx}, material_${hIdx});\n")
                }
            }
            sb.append("        }\n")
        }
        
        sb.append("    }\n")
        sb.append("}\n")
        return sb.toString()
    }
}
