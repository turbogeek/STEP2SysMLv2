package geometry

import groovy.transform.CompileStatic
import step.StepShape

/**
 * Computes approximate mass properties (volume, mass, centre of mass)
 * for a triangulated solid using the <em>divergence theorem method</em>.
 *
 * <h3>Algorithm</h3>
 * <p>For each triangle (A, B, C) of the tessellation, a signed tetrahedron
 * with apex at the origin is formed.  The signed volume of all tetrahedra
 * sums to the enclosed volume of the solid (positive when normals point
 * outward).  The centre of mass is the density-weighted centroid of all
 * tetrahedra.</p>
 *
 * <p>Reference: Zhang &amp; Chen, "Efficient Feature Extraction for 2D/3D
 * Objects in Mesh Representation", ICIP 2001.</p>
 *
 * <h3>Limitations</h3>
 * <ul>
 *   <li>Requires a closed, consistently-oriented triangulation.
 *       Open shells or self-intersecting meshes give incorrect results.</li>
 *   <li>When only a vertex cloud (no triangles) is available, the method
 *       falls back to the bounding-box envelope volume scaled by 0.5
 *       as a rough estimate, and the CoM is set to the bounding-box centre.</li>
 * </ul>
 *
 * <h3>No external dependencies</h3>
 * <p>This class depends only on {@link StepShape} and {@link BoundingBoxCalculator};
 * it can be exercised in a headless JUnit/Spock test.</p>
 */
@CompileStatic
class MassPropertiesCalculator {

    /** Steel density in g/mm³ — used as the default when no material is supplied. */
    static final double DEFAULT_DENSITY_STEEL = 7.85e-3

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Compute mass properties for a single shape using the given density.
     *
     * @param shape    shape with populated vertex (and optionally triangle) data
     * @param density  material density in g/mm³; pass {@code Double.NaN} to skip
     * @return computed mass properties
     */
    static MassProperties compute(StepShape shape, double density) {
        if (!shape.hulls) {
            return new MassProperties(0, 0, [0d, 0d, 0d] as double[], density)
        }

        double totalVolume = 0.0
        double[] totalComNumerator = [0d, 0d, 0d] as double[]

        for (def hull : shape.hulls) {
            double[] verts = hull.vertices
            int[]    tris  = hull.triangles

            double volume
            double[] com

            if (tris != null && tris.length >= 3) {
                // Triangulated path — accurate for closed manifold meshes
                volume = signedVolume(verts, tris)
                com    = centreOfMass(verts, tris, volume)
            } else {
                // Fallback: bounding-box estimation
                BoundingBox bb = BoundingBoxCalculator.computeFromVertices(verts)
                volume = bb.envelopeVolume * 0.5   // crude approximation
                com    = bb.centre
            }
            
            totalVolume += volume
            totalComNumerator[0] += com[0] * volume
            totalComNumerator[1] += com[1] * volume
            totalComNumerator[2] += com[2] * volume
        }

        double[] finalCom
        if (Math.abs(totalVolume) > 1e-12) {
            finalCom = [
                totalComNumerator[0] / totalVolume,
                totalComNumerator[1] / totalVolume,
                totalComNumerator[2] / totalVolume
            ] as double[]
        } else {
            // fallback if degenerate
            if (shape.hulls.size() > 0 && shape.hulls[0].vertices.length >= 3) {
                finalCom = vertexCentroid(shape.hulls[0].vertices)
            } else {
                finalCom = [0d, 0d, 0d] as double[]
            }
        }

        double mass = Double.isNaN(density) ? Double.NaN : Math.abs(totalVolume) * density
        return new MassProperties(totalVolume, mass, finalCom, density)
    }

    /**
     * Compute mass properties using the default steel density
     * ({@value #DEFAULT_DENSITY_STEEL} g/mm³).
     */
    static MassProperties compute(StepShape shape) {
        return compute(shape, DEFAULT_DENSITY_STEEL)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Internal computation
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Signed volume via the divergence theorem.
     * Result is positive when face normals point consistently outward.
     */
    private static double signedVolume(double[] v, int[] t) {
        double vol = 0.0
        for (int i = 0; i < t.length; i += 3) {
            int i0 = t[i] * 3, i1 = t[i + 1] * 3, i2 = t[i + 2] * 3
            double ax = v[i0], ay = v[i0+1], az = v[i0+2]
            double bx = v[i1], by = v[i1+1], bz = v[i1+2]
            double cx = v[i2], cy = v[i2+1], cz = v[i2+2]
            // Signed volume of tetrahedron (O, A, B, C) = (A · (B × C)) / 6
            vol += (ax * (by * cz - bz * cy)
                  - ay * (bx * cz - bz * cx)
                  + az * (bx * cy - by * cx)) / 6.0
        }
        return vol
    }

    /**
     * Centre of mass as the weighted centroid of all signed tetrahedra.
     *
     * @param totalVolume  the signed total volume (from {@link #signedVolume})
     */
    private static double[] centreOfMass(double[] v, int[] t, double totalVolume) {
        if (Math.abs(totalVolume) < 1e-12) {
            // Degenerate solid — return vertex centroid as fallback
            return vertexCentroid(v)
        }
        double cx = 0, cy = 0, cz = 0
        for (int i = 0; i < t.length; i += 3) {
            int i0 = t[i] * 3, i1 = t[i + 1] * 3, i2 = t[i + 2] * 3
            double ax = v[i0], ay = v[i0+1], az = v[i0+2]
            double bx = v[i1], by = v[i1+1], bz = v[i1+2]
            double cx2 = v[i2], cy2 = v[i2+1], cz2 = v[i2+2]
            double tetVol = (ax * (by * cz2 - bz * cy2)
                           - ay * (bx * cz2 - bz * cx2)
                           + az * (bx * cy2 - by * cx2)) / 6.0
            cx += tetVol * (ax + bx + cx2)
            cy += tetVol * (ay + by + cy2)
            cz += tetVol * (az + bz + cz2)
        }
        double factor = 1.0 / (totalVolume * 4.0)
        return [cx * factor, cy * factor, cz * factor] as double[]
    }

    private static double[] vertexCentroid(double[] v) {
        double sx = 0, sy = 0, sz = 0
        int n = v.length.intdiv(3)
        for (int i = 0; i < v.length; i += 3) { sx += v[i]; sy += v[i+1]; sz += v[i+2] }
        return [sx/n, sy/n, sz/n] as double[]
    }
}
