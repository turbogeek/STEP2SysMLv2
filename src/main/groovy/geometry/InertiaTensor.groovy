package geometry

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Inertia tensor of a solid body, expressed in the shape's local frame.
 *
 * <p>All quantities are in g·mm² when the input vertices are in mm and
 * density is in g/mm³.</p>
 *
 * <p>The tensor is symmetric; only the six independent components are stored.</p>
 */
@CompileStatic
@ToString(includePackage = false, includeNames = true)
class InertiaTensor {

    /** Principal diagonal moments of inertia (g·mm²). */
    final double Ixx, Iyy, Izz

    /** Off-diagonal products of inertia (g·mm²). */
    final double Ixy, Ixz, Iyz

    /** Total mass used in the calculation (g). */
    final double mass

    InertiaTensor(double Ixx, double Iyy, double Izz,
                  double Ixy, double Ixz, double Iyz,
                  double mass) {
        this.Ixx = Ixx; this.Iyy = Iyy; this.Izz = Izz
        this.Ixy = Ixy; this.Ixz = Ixz; this.Iyz = Iyz
        this.mass = mass
    }

    /**
     * Return the full 3×3 inertia tensor as a row-major flat array:
     * [Ixx, Ixy, Ixz,  Iyx, Iyy, Iyz,  Izx, Izy, Izz]
     */
    double[] toFlatMatrix() {
        [Ixx, Ixy, Ixz,
         Ixy, Iyy, Iyz,
         Ixz, Iyz, Izz] as double[]
    }
}
