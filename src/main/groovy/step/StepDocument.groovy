package step

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Immutable data transfer object representing the top-level contents of a
 * parsed STEP Part 21 file.
 *
 * <p>All fields are populated by {@link StepLibraryAdapter} implementations
 * and consumed by the geometry-core layer — no STEP-library types leak out.</p>
 */
@CompileStatic
@ToString(includePackage = false, includeNames = true)
class StepDocument {

    /**
     * STEP application protocol schema, e.g. {@code "AP203"}, {@code "AP214"},
     * {@code "AP242"}.  Derived from the {@code FILE_SCHEMA} header section.
     */
    final String schema

    /**
     * Original file name from the {@code FILE_NAME} header entity.
     * May differ from the file path on disk if the file was renamed.
     */
    final String originalFileName

    /** Timestamp string from the STEP file header (ISO 8601 when present). */
    final String fileTimestamp

    /** SHA-256 hex digest of the raw file bytes (computed by the parser). */
    final String fileHash

    /** Total entity count in the DATA section (informational). */
    final int entityCount

    /**
     * Root-level products in the assembly tree.
     * Each {@link StepProduct} may have child products recursively.
     */
    final List<StepProduct> products

    /**
     * All shapes referenced by the products in this document.
     * Shapes are stored flat here; the product→shape association is held
     * within each {@link StepProduct}.
     */
    final List<StepShape> shapes

    StepDocument(
        String schema,
        String originalFileName,
        String fileTimestamp,
        String fileHash,
        int entityCount,
        List<StepProduct> products,
        List<StepShape> shapes
    ) {
        this.schema           = schema
        this.originalFileName = originalFileName
        this.fileTimestamp    = fileTimestamp
        this.fileHash         = fileHash
        this.entityCount      = entityCount
        this.products         = Collections.unmodifiableList(products ?: [])
        this.shapes           = Collections.unmodifiableList(shapes   ?: [])
    }
}
