package step

/**
 * Service-provider interface for the low-level STEP I/O library.
 *
 * <p>Implementations wrap a specific STEP library (JSDAI, STEP Tools, etc.)
 * and translate its types into the neutral {@link StepDocument} object graph.
 * The interface is intentionally minimal so adapters are easy to swap.</p>
 *
 * <p>Two implementations are anticipated:</p>
 * <ul>
 *   <li>{@link StubStepLibraryAdapter} — no-op / fixture-driven; used in
 *       unit tests without a real STEP jar on the classpath.</li>
 *   <li>{@code JsdaiStepLibraryAdapter} (Phase 1) — wraps JSDAI when the
 *       {@code jsdai_core.jar} is available in {@code lib/}.</li>
 * </ul>
 */
interface StepLibraryAdapter {

    /**
     * Read a STEP Part 21 file and return the neutral document model.
     *
     * @param stepFile an existing, readable STEP file
     * @return populated {@link StepDocument}
     * @throws StepParseException on any I/O or schema error
     */
    StepDocument readFile(File stepFile)
}
