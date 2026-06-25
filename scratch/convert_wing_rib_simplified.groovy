import geometry.BoundingBoxCalculator
import geometry.BoundingBox
import step.StepFileParser
import java.io.File

File file = new File("test-data/DassaultExports/N101_Submodel LH Wing Rib 150 A.stp")
if (!file.exists()) {
    throw new RuntimeException("File not found: " + file.absolutePath)
}

def parser = new step.StepFileParser()
def doc = parser.parse(file)

// Compute the overall bounding box across all shapes
BoundingBox overallBB = BoundingBoxCalculator.computeAll(doc.shapes)

// Calculate total true volume
double totalTrueVolumeMm3 = 0.0
for (def shape : doc.shapes) {
    if (shape.hulls) {
        for (def hull : shape.hulls) {
            totalTrueVolumeMm3 += hull.volume
        }
    }
}

// Convert volume to m^3 for standard calculation
double totalTrueVolumeM3 = totalTrueVolumeMm3 / 1.0e9

StringBuilder sb = new StringBuilder()
sb.append("package WingRibSimplified {\n")
sb.append("    private import ScalarValues::*;\n")
sb.append("    private import ISQ::*;\n")
sb.append("    public import MaterialsLibrary::*;\n")
sb.append("    public import MassCalculations::*;\n\n")

sb.append("    part def BoundingBoxShape {\n")
sb.append("        attribute minX : Real;\n")
sb.append("        attribute minY : Real;\n")
sb.append("        attribute minZ : Real;\n")
sb.append("        attribute maxX : Real;\n")
sb.append("        attribute maxY : Real;\n")
sb.append("        attribute maxZ : Real;\n")
sb.append("    }\n\n")

sb.append("    part root {\n")
sb.append("        part bounding_shape : BoundingBoxShape {\n")
sb.append(String.format(java.util.Locale.US, "            :>> minX = %.6f;\n", overallBB.minX))
sb.append(String.format(java.util.Locale.US, "            :>> minY = %.6f;\n", overallBB.minY))
sb.append(String.format(java.util.Locale.US, "            :>> minZ = %.6f;\n", overallBB.minZ))
sb.append(String.format(java.util.Locale.US, "            :>> maxX = %.6f;\n", overallBB.maxX))
sb.append(String.format(java.util.Locale.US, "            :>> maxY = %.6f;\n", overallBB.maxY))
sb.append(String.format(java.util.Locale.US, "            :>> maxZ = %.6f;\n", overallBB.maxZ))
sb.append("        }\n\n")

sb.append(String.format(java.util.Locale.US, "        attribute true_volume_m3 : Real = %.10f;\n\n", totalTrueVolumeM3))

sb.append("        // Standard material calculation mapping\n")
sb.append("        attribute material : Material = aluminum;\n")
sb.append("        attribute mass_kg : Real = CalculateMass(true_volume_m3, material);\n")
sb.append("    }\n")
sb.append("}\n")

File outFile = new File("test-data/DassaultExports/N101_Simplified.sysml")
outFile.text = sb.toString()
println "Successfully converted to " + outFile.absolutePath
