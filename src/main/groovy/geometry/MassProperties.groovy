package geometry

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Result of a mass properties calculation for one or more shapes.
 *
 * <p>All linear quantities are in the same unit as the source STEP file
 * (typically mm).  Mass is in grams when {@code density} is supplied in
 * g/mm³.  If no density is set, {@link #mass} will be {@code NaN}.</p>
 */
@CompileStatic
@ToString(includePackage = false, includeNames = true)
class MassProperties {

    /**
     * Signed volume of the solid computed via the divergence theorem
     * (sum of signed tetrahedra).  Positive for outward-pointing normals.
     * Units: mm³ (when STEP vertices are in mm).
     */
    final double volume

    /**
     * Mass = volume × density.  {@code NaN} if density was not provided.
     * Units: grams (when density is in g/mm³).
     */
    final double mass

    /**
     * Centre of mass [x, y, z] in the shape's local coordinate frame.
     * Units: mm.
     */
    final double[] centreOfMass

    /**
     * Material density used for the mass calculation (g/mm³).
     * {@code NaN} if not set.
     */
    final double density

    MassProperties(double volume, double mass, double[] centreOfMass, double density) {
        this.volume       = volume
        this.mass         = mass
        this.centreOfMass = centreOfMass?.clone() ?: new double[3]
        this.density      = density
    }

    boolean hasMass()    { !Double.isNaN(mass) }
    boolean hasDensity() { !Double.isNaN(density) }
}
