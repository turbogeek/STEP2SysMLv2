package geometry

import groovy.transform.CompileStatic
import step.StepShape

/**
 * Computes the inertia tensor of a triangulated solid body about its
 * centre of mass, using the signed-tetrahedron covariance method.
 *
 * <h3>Algorithm</h3>
 * <p>For each triangle (A, B, C) a signed tetrahedron (O, A, B, C) is formed.
 * The volume-weighted covariance about the origin is accumulated, then the
 * parallel-axis theorem shifts the tensor to the supplied CoM.</p>
 *
 * <p>Normalization: the raw accumulated sums are scaled by
 * {@code density / 20 = (mass/volume) / 20} to yield the inertia tensor
 * components about the origin, then the CoM shift is applied.</p>
 *
 * <h3>Limitations</h3>
 * <ul>
 *   <li>Requires a closed, consistently-oriented triangulation.</li>
 *   <li>Falls back to a solid-cuboid approximation when no triangles are present.</li>
 * </ul>
 */
@CompileStatic
class InertiaCalculator {

    /**
     * Compute the inertia tensor for a shape, translated to the given CoM.
     *
     * @param shape  shape with vertex + triangle data
     * @param comX   centre-of-mass x (mm)
     * @param comY   centre-of-mass y (mm)
     * @param comZ   centre-of-mass z (mm)
     * @param mass   total mass (g)
     * @return inertia tensor (g·mm²)
     */
    static InertiaTensor compute(StepShape shape,
                                  double comX, double comY, double comZ,
                                  double mass) {
        if (!shape.hulls) {
            return cuboidFallback(shape, mass)
        }

        // ── One-pass accumulation ─────────────────────────────────────────────
        double vol6 = 0.0                        // = 6 * signedVolume
        double cxx = 0, cyy = 0, czz = 0
        double cxy = 0, cxz = 0, cyz = 0

        for (def hull : shape.hulls) {
            double[] v = hull.vertices
            int[]    t = hull.triangles

            if (t == null || t.length < 3) {
                return cuboidFallback(shape, mass)
            }

            for (int i = 0; i < t.length; i += 3) {
                int i0 = t[i] * 3, i1 = t[i+1] * 3, i2 = t[i+2] * 3
                double ax = v[i0],   ay = v[i0+1], az = v[i0+2]
                double bx = v[i1],   by = v[i1+1], bz = v[i1+2]
                double cx = v[i2],   cy = v[i2+1], cz = v[i2+2]

                // Scalar triple product = 6 × signed tet volume
                double d = ax * (by*cz - bz*cy)
                         - ay * (bx*cz - bz*cx)
                         + az * (bx*cy - by*cx)
                vol6 += d

                // Second-moment accumulation (each term = d × symmetric polynomial)
                cxx += d * (ax*ax + ax*bx + bx*bx + ax*cx + bx*cx + cx*cx)
                cyy += d * (ay*ay + ay*by + by*by + ay*cy + by*cy + cy*cy)
                czz += d * (az*az + az*bz + bz*bz + az*cz + bz*cz + cz*cz)
                cxy += d * (2*ax*ay + bx*ay + cx*ay + ax*by + 2*bx*by + cx*by + ax*cy + bx*cy + 2*cx*cy)
                cxz += d * (2*ax*az + bx*az + cx*az + ax*bz + 2*bx*bz + cx*bz + ax*cz + bx*cz + 2*cx*cz)
                cyz += d * (2*ay*az + by*az + cy*az + ay*bz + 2*by*bz + cy*bz + ay*cz + by*cz + 2*cy*cz)
            }
        }

        double volume = vol6 / 6.0
        if (Math.abs(volume) < 1e-14) {
            return cuboidFallback(shape, mass)
        }

        // ── Scale to physical inertia about origin ───────────────────────────
        // density = mass / |volume|; factor = density / 20
        double density = mass / Math.abs(volume)
        double f       = density / 20.0

        // Inertia about origin from covariance, then parallel-axis to CoM
        double cx2 = comX*comX, cy2 = comY*comY, cz2 = comZ*comZ

        double Ixx = f*(cyy + czz) - mass*(cy2 + cz2)
        double Iyy = f*(cxx + czz) - mass*(cx2 + cz2)
        double Izz = f*(cxx + cyy) - mass*(cx2 + cy2)
        double Ixy = -(f*cxy)      + mass*comX*comY
        double Ixz = -(f*cxz)      + mass*comX*comZ
        double Iyz = -(f*cyz)      + mass*comY*comZ

        return new InertiaTensor(Ixx, Iyy, Izz, Ixy, Ixz, Iyz, mass)
    }

    // ── Fallback: cuboid formula (uses bounding box) ──────────────────────────

    private static InertiaTensor cuboidFallback(StepShape shape, double mass) {
        BoundingBox bb = BoundingBoxCalculator.compute(shape)
        double lx = bb.sizeX, ly = bb.sizeY, lz = bb.sizeZ
        double Ixx = mass * (ly*ly + lz*lz) / 12.0
        double Iyy = mass * (lx*lx + lz*lz) / 12.0
        double Izz = mass * (lx*lx + ly*ly) / 12.0
        return new InertiaTensor(Ixx, Iyy, Izz, 0.0, 0.0, 0.0, mass)
    }
}
