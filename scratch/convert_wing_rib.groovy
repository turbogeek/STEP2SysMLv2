import sysmlv2.GeometryGenerator
import step.StepFileParser
import java.io.File

File file = new File("test-data/DassaultExports/N101_Submodel LH Wing Rib 150 A.stp")
if (!file.exists()) {
    throw new RuntimeException("File not found: " + file.absolutePath)
}

def parser = new step.StepFileParser()
def doc = parser.parse(file)

def generator = new sysmlv2.GeometryGenerator()
String sysml = generator.generate("WingRib150A", doc.shapes)

File outFile = new File("test-data/DassaultExports/N101_Submodel LH Wing Rib 150 A.sysml")
outFile.text = sysml
println "Successfully converted to " + outFile.absolutePath
