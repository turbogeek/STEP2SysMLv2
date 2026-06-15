package geometry

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Axis-aligned bounding box computed from a set of 3D vertices.
 *
 * <p>All coordinates are in the same unit as the source STEP file (typically mm).
 * The {@link #envelopeVolume} is the volume of the rectangular envelope
 * — <em>not</em> the volume of the solid.</p>
 */
@CompileStatic
@ToString(includePackage = false, includeNames = true)
class BoundingBox {
    final double minX, minY, minZ
    final double maxX, maxY, maxZ

    BoundingBox(double minX, double minY, double minZ,
                double maxX, double maxY, double maxZ) {
        this.minX = minX; this.minY = minY; this.minZ = minZ
        this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ
    }

    double getSizeX() { maxX - minX }
    double getSizeY() { maxY - minY }
    double getSizeZ() { maxZ - minZ }
    double getEnvelopeVolume() { sizeX * sizeY * sizeZ }

    /** Centre of the bounding box. */
    double[] getCentre() { [(minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2] as double[] }
}
