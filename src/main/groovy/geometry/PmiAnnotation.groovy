package geometry

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * A PMI (Product Manufacturing Information) annotation extracted from
 * a STEP AP242 file.
 *
 * <p>Maps to AP242 entities such as {@code GEOMETRIC_TOLERANCE},
 * {@code DATUM_REFERENCE_FRAME}, {@code SURFACE_CONDITION}, and
 * {@code DIMENSION_CHARACTERISTIC_REPRESENTATION}.</p>
 *
 * <p>This POJO is consumed by {@link PmiExtractor} and later by
 * {@code sysml-writer.RequirementGenerator} to produce SysMLv2
 * {@code RequirementUsage} elements.</p>
 */
@CompileStatic
@ToString(includePackage = false, includeNames = true)
class PmiAnnotation {

    /**
     * High-level PMI category.
     * Known values: {@code "DIMENSION"}, {@code "GEOMETRIC_TOLERANCE"},
     * {@code "SURFACE_FINISH"}, {@code "DATUM"}, {@code "NOTE"}.
     */
    final String type

    /**
     * Tolerance or dimension type sub-classification.
     * Examples: {@code "FLATNESS"}, {@code "CYLINDRICITY"}, {@code "POSITION"},
     * {@code "PERPENDICULARITY"}, {@code "LINEAR_DIMENSION"}, {@code "SURFACE_ROUGHNESS"}.
     */
    final String subType

    /** Nominal value for dimension/tolerance (in file's length units). {@code NaN} if not applicable. */
    final double nominalValue

    /** Upper tolerance (positive offset from nominal). {@code NaN} if not applicable. */
    final double upperTolerance

    /** Lower tolerance (negative offset from nominal). {@code NaN} if not applicable. */
    final double lowerTolerance

    /** STEP entity id of the referencing annotation entity. */
    final int entityId

    /**
     * Entity id of the shape/face/edge this annotation applies to.
     * May be -1 if not determinable without a full B-Rep walker.
     */
    final int appliedToEntityId

    /** Raw label text from the annotation (e.g., the GD&T callout string). */
    final String label

    /** Datum label(s) referenced, if any (e.g., "A", "B|C"). Empty string if none. */
    final String datumReferences

    PmiAnnotation(
        String type, String subType,
        double nominalValue, double upperTolerance, double lowerTolerance,
        int entityId, int appliedToEntityId,
        String label, String datumReferences
    ) {
        this.type               = type
        this.subType            = subType
        this.nominalValue       = nominalValue
        this.upperTolerance     = upperTolerance
        this.lowerTolerance     = lowerTolerance
        this.entityId           = entityId
        this.appliedToEntityId  = appliedToEntityId
        this.label              = label ?: ''
        this.datumReferences    = datumReferences ?: ''
    }

    /** Convenience: does this annotation have a finite tolerance band? */
    boolean hasTolerance() {
        !Double.isNaN(upperTolerance) || !Double.isNaN(lowerTolerance)
    }

    /**
     * Format as a simple requirement text suitable for a SysMLv2
     * {@code RequirementUsage.text} attribute.
     */
    String toRequirementText() {
        StringBuilder sb = new StringBuilder()
        sb << "${type}"
        if (subType) sb << " (${subType})"
        if (!Double.isNaN(nominalValue)) sb << ": nominal=${nominalValue}"
        if (!Double.isNaN(upperTolerance)) sb << " +${upperTolerance}"
        if (!Double.isNaN(lowerTolerance)) sb << " ${lowerTolerance}"
        if (datumReferences) sb << " | datums: ${datumReferences}"
        if (label) sb << " [${label}]"
        return sb.toString()
    }
}
