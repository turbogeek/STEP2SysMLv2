package geometry

import groovy.transform.CompileStatic

/**
 * Extracts Product Manufacturing Information (PMI) annotations from the
 * raw text of a STEP AP242 Part 21 file, returning a list of
 * {@link PmiAnnotation} objects.
 *
 * <h3>Design</h3>
 * <p>Like {@link step.StubStepLibraryAdapter}, this extractor operates purely
 * on the text of the Part 21 file using regex pattern matching — no STEP
 * library jar is required.  This is sufficient for unit testing and for
 * extracting coarse PMI from simple AP242 files.</p>
 *
 * <p>For production use against complex AP242 files (semantic PMI networks,
 * full datum reference frames) this class should be replaced by or
 * supplemented with a JSDAI- or STEP-Tools-based extractor.</p>
 *
 * <h3>AP242 entities handled (text-level)</h3>
 * <ul>
 *   <li>{@code FLATNESS_TOLERANCE} / {@code CYLINDRICITY_TOLERANCE} /
 *       {@code PERPENDICULARITY_TOLERANCE} / {@code POSITION_TOLERANCE} /
 *       {@code SURFACE_CONDITION}</li>
 *   <li>{@code MEASURE_WITH_UNIT} referenced by the above</li>
 *   <li>Generic {@code GEOMETRIC_TOLERANCE} catch-all</li>
 *   <li>{@code DATUM} — datums named inline</li>
 * </ul>
 */
@CompileStatic
class PmiExtractor {

    /**
     * Extract all PMI annotations from the raw text of a STEP file.
     *
     * @param raw  the full text of a STEP Part 21 file
     * @return list of {@link PmiAnnotation}; empty if no PMI is found
     */
    static List<PmiAnnotation> extract(String raw) {
        List<PmiAnnotation> result = []
        result.addAll(extractGeometricTolerances(raw))
        result.addAll(extractDatums(raw))
        result.addAll(extractSurfaceConditions(raw))
        return result
    }

    // ── Geometric tolerances ──────────────────────────────────────────────────

    private static final Map<String, String> TOLERANCE_TYPES = [
        'FLATNESS_TOLERANCE'        : 'FLATNESS',
        'CYLINDRICITY_TOLERANCE'    : 'CYLINDRICITY',
        'PERPENDICULARITY_TOLERANCE': 'PERPENDICULARITY',
        'POSITION_TOLERANCE'        : 'POSITION',
        'ROUNDNESS_TOLERANCE'       : 'ROUNDNESS',
        'STRAIGHTNESS_TOLERANCE'    : 'STRAIGHTNESS',
        'PARALLELISM_TOLERANCE'     : 'PARALLELISM',
        'ANGULARITY_TOLERANCE'      : 'ANGULARITY',
        'SYMMETRY_TOLERANCE'        : 'SYMMETRY',
        'CONCENTRICITY_TOLERANCE'   : 'CONCENTRICITY',
        'TOTAL_RUNOUT_TOLERANCE'    : 'TOTAL_RUNOUT',
        'CIRCULAR_RUNOUT_TOLERANCE' : 'CIRCULAR_RUNOUT',
        'LINE_PROFILE_TOLERANCE'    : 'PROFILE_OF_LINE',
        'SURFACE_PROFILE_TOLERANCE' : 'PROFILE_OF_SURFACE',
        'GEOMETRIC_TOLERANCE'       : 'GEOMETRIC',
    ]

    private static List<PmiAnnotation> extractGeometricTolerances(String raw) {
        List<PmiAnnotation> list = []
        String typePattern = TOLERANCE_TYPES.keySet().join('|')
        // #id = FLATNESS_TOLERANCE('label',#measure_ref,#datum_or_nil,...);
        def m = raw =~ /(?m)^#(\d+)\s*=\s*(${typePattern})\s*\(\s*'([^']*)'\s*,\s*#(\d+)/
        while (m.find()) {
            int    entityId = m.group(1) as int
            String rawType  = m.group(2)
            String label    = m.group(3)
            int    measureRef = m.group(4) as int
            String subType  = TOLERANCE_TYPES[rawType] ?: 'GEOMETRIC'

            double toleranceValue = resolveMeasure(raw, measureRef)

            list << new PmiAnnotation(
                'GEOMETRIC_TOLERANCE', subType,
                Double.NaN, toleranceValue, Double.NaN,
                entityId, -1, label, ''
            )
        }
        return list
    }

    // ── Datums ────────────────────────────────────────────────────────────────

    private static List<PmiAnnotation> extractDatums(String raw) {
        List<PmiAnnotation> list = []
        // #id = DATUM('label',...);
        def m = raw =~ /(?m)^#(\d+)\s*=\s*DATUM\s*\(\s*'([^']*)'/
        while (m.find()) {
            int    entityId = m.group(1) as int
            String label    = m.group(2)
            list << new PmiAnnotation(
                'DATUM', 'DATUM',
                Double.NaN, Double.NaN, Double.NaN,
                entityId, -1, label, ''
            )
        }
        return list
    }

    // ── Surface conditions (roughness / finish) ────────────────────────────────

    private static List<PmiAnnotation> extractSurfaceConditions(String raw) {
        List<PmiAnnotation> list = []
        // #id = SURFACE_CONDITION('label',...);
        def m = raw =~ /(?m)^#(\d+)\s*=\s*SURFACE_CONDITION\s*\(\s*'([^']*)'/
        while (m.find()) {
            int    entityId = m.group(1) as int
            String label    = m.group(2)
            list << new PmiAnnotation(
                'SURFACE_FINISH', 'SURFACE_ROUGHNESS',
                Double.NaN, Double.NaN, Double.NaN,
                entityId, -1, label, ''
            )
        }
        return list
    }

    // ── Measure resolution ─────────────────────────────────────────────────────

    /**
     * Resolve a {@code MEASURE_WITH_UNIT} or {@code LENGTH_MEASURE} entity
     * referenced by {@code #entityId} and return the numeric value.
     * Returns {@code Double.NaN} if the reference cannot be resolved.
     */
    private static double resolveMeasure(String raw, int entityId) {
        // #id = MEASURE_WITH_UNIT(LENGTH_MEASURE(0.05),#unit);
        def m = raw =~ /(?m)^#${entityId}\s*=\s*[A-Z_]*MEASURE[A-Z_]*\s*\(\s*[A-Z_]*MEASURE\s*\(\s*([0-9.Ee+-]+)/
        if (m.find()) {
            try { return m.group(1) as double } catch (ignored) {}
        }
        // #id = (MEASURE_WITH_UNIT(...) LENGTH_MEASURE(0.05) ...);  — combined instance
        def m2 = raw =~ /(?m)^#${entityId}\s*=\s*\(.*?([0-9]+\.[0-9]+(?:[Ee][+-]?[0-9]+)?)/
        if (m2.find()) {
            try { return m2.group(1) as double } catch (ignored) {}
        }
        return Double.NaN
    }
}
