import step.Part21TextParserAdapter
import step.StepDocument

def parser = new Part21TextParserAdapter()
File file = new File("test-data/pmi_ap242.stp")
long start = System.currentTimeMillis()
StepDocument doc = parser.readFile(file)
long end = System.currentTimeMillis()

println "Parsed in ${end - start} ms"
println "Schema: ${doc.schema}"
println "Entities: ${doc.entityCount}"
println "Original Name: ${doc.originalFileName}"
println "Products: ${doc.products.size()}"
println "Shapes: ${doc.shapes.size()}"
if (doc.shapes) {
    println "Vertices in first shape: ${doc.shapes[0].vertices.length / 3}"
}
