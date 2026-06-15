package geometry

import groovy.transform.CompileStatic
import step.StepShape

/**
 * Computes the axis-aligned bounding box of a {@link StepShape} by
 * scanning the raw vertex array.
 *
 * <p>Operates purely on the {@code double[]} vertex array — no STEP library
 * or Cameo dependency.</p>
 *
 * <h3>Complexity</h3>
 * <p>O(n) in the number of vertices — single-pass min/max scan.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * BoundingBox bb = BoundingBoxCalculator.compute(shape)
 * println "X range: ${bb.minX} → ${bb.maxX} (${bb.sizeX} mm)"
 * }</pre>
 */
@CompileStatic
class BoundingBoxCalculator {

    /**
     * Compute the bounding box for a single shape.
     *
     * @param shape a {@link StepShape} whose {@code vertices} array is populated
     * @return the axis-aligned bounding box
     * @throws IllegalArgumentException if the shape has no vertices
     */
    static BoundingBox compute(StepShape shape) {
        if (!shape.hulls) {
            throw new IllegalArgumentException(
                "StepShape #${shape.entityId} has no hulls — cannot compute bounding box")
        }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE
        
        for (def hull : shape.hulls) {
            BoundingBox bb = computeFromVertices(hull.vertices)
            if (bb.minX < minX) minX = bb.minX
            if (bb.minY < minY) minY = bb.minY
            if (bb.minZ < minZ) minZ = bb.minZ
            if (bb.maxX > maxX) maxX = bb.maxX
            if (bb.maxY > maxY) maxY = bb.maxY
            if (bb.maxZ > maxZ) maxZ = bb.maxZ
        }
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
    }

    /**
     * Compute the bounding box that encloses all shapes in the list.
     *
     * @param shapes non-empty list of shapes
     * @return combined axis-aligned bounding box
     */
    static BoundingBox computeAll(List<StepShape> shapes) {
        if (!shapes) throw new IllegalArgumentException('shapes list must not be empty')
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE
        for (StepShape s : shapes) {
            BoundingBox bb = compute(s)
            if (bb.minX < minX) minX = bb.minX
            if (bb.minY < minY) minY = bb.minY
            if (bb.minZ < minZ) minZ = bb.minZ
            if (bb.maxX > maxX) maxX = bb.maxX
            if (bb.maxY > maxY) maxY = bb.maxY
            if (bb.maxZ > maxZ) maxZ = bb.maxZ
        }
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
    }

    /**
     * Compute the bounding box from a raw flat vertex array
     * {@code [x0,y0,z0, x1,y1,z1, ...]}.
     *
     * @param verts array whose length is a positive multiple of 3
     * @return the axis-aligned bounding box
     */
    static BoundingBox computeFromVertices(double[] verts) {
        if (verts == null || verts.length == 0) {
            throw new IllegalArgumentException('Vertex array must not be empty')
        }
        if (verts.length % 3 != 0) {
            throw new IllegalArgumentException(
                "Vertex array length (${verts.length}) must be a multiple of 3")
        }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE
        for (int i = 0; i < verts.length; i += 3) {
            double x = verts[i], y = verts[i + 1], z = verts[i + 2]
            if (x < minX) minX = x
            if (y < minY) minY = y
            if (z < minZ) minZ = z
            if (x > maxX) maxX = x
            if (y > maxY) maxY = y
            if (z > maxZ) maxZ = z
        }
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
    }
}
