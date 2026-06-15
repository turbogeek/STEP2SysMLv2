package step

/**
 * Parses a STEP Part 21 (.stp/.step) file and returns a {@link StepDocument}
 * containing the header information and top-level product list.
 *
 * <p>This class is the sole entry point into the step-reader layer.
 * It has <strong>no dependency on Cameo / MagicDraw</strong> and can be
 * exercised in a headless JUnit/Spock test.</p>
 *
 * <p>Library wiring: the actual STEP I/O is delegated to a configurable
 * {@link StepLibraryAdapter}. The default adapter (set via
 * {@link #withAdapter}) is a no-op stub that must be replaced with a real
 * JSDAI or STEP Tools adapter once the jar is available in {@code lib/}.</p>
 *
 * <h3>Usage (Groovy)</h3>
 * <pre>{@code
 * def parser = new StepFileParser()
 * StepDocument doc = parser.parse(new File('path/to/model.stp'))
 * println "Schema: ${doc.schema}, Products: ${doc.products.size()}"
 * }</pre>
 */
class StepFileParser {

    /** Pluggable low-level STEP library adapter. */
    private StepLibraryAdapter adapter

    StepFileParser() {
        this.adapter = new RestStepAdapter()
    }

    /** Replace the adapter with a real JSDAI or STEP Tools implementation. */
    StepFileParser withAdapter(StepLibraryAdapter adapter) {
        this.adapter = adapter
        return this
    }

    /**
     * Parse the given STEP file and return a {@link StepDocument}.
     *
     * @param stepFile a readable STEP Part 21 file
     * @return parsed document model
     * @throws StepParseException if the file cannot be read or is not valid STEP
     */
    StepDocument parse(File stepFile) {
        if (!stepFile?.exists()) {
            throw new StepParseException("STEP file not found: ${stepFile?.absolutePath}")
        }
        try {
            return adapter.readFile(stepFile)
        } catch (Exception e) {
            throw new StepParseException("Failed to parse STEP file '${stepFile.name}': ${e.message}", e)
        }
    }
}
