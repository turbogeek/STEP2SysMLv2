package step

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Represents a single convex hull generated from a STEP part decomposition.
 */
@CompileStatic
@ToString(includePackage = false, includeNames = true)
class ConvexHull {
    final double[] vertices
    final int[] triangles
    final double volume

    ConvexHull(double[] vertices, int[] triangles, double volume = 0.0) {
        this.vertices = vertices ?: new double[0]
        this.triangles = triangles ?: new int[0]
        this.volume = volume
    }
}

/**
 * Represents a geometric shape extracted from a STEP file.
 *
 * <p>After V-HACD processing, a complex shape is represented as a collection
 * of ConvexHulls.</p>
 */
@CompileStatic
@ToString(includePackage = false, includeNames = true)
class StepShape {

    /** STEP entity instance id. */
    final int entityId

    /** STEP entity type name, e.g. {@code "MANIFOLD_SOLID_BREP"}. */
    final String entityType

    /** The collection of convex hulls comprising this shape. */
    final List<ConvexHull> hulls

    StepShape(
        int entityId,
        String entityType,
        List<ConvexHull> hulls
    ) {
        this.entityId   = entityId
        this.entityType = entityType
        this.hulls      = hulls ?: []
    }
}
