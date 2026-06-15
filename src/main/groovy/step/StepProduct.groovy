package step

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Represents one product (part or sub-assembly) from a STEP file.
 *
 * <p>Maps to the STEP {@code PRODUCT} / {@code PRODUCT_DEFINITION} entity
 * pair.  The {@link #children} list represents
 * {@code NEXT_ASSEMBLY_USAGE_OCCURRENCE} references.</p>
 */
@CompileStatic
@ToString(includePackage = false, includeNames = true)
class StepProduct {

    /** STEP entity instance id (the {@code #nnn} number in the Part 21 file). */
    final int entityId

    /** Value of the STEP {@code PRODUCT.id} attribute (often a part number). */
    final String partId

    /** Human-readable product name from {@code PRODUCT.name}. */
    final String name

    /** Description from {@code PRODUCT.description}. May be empty. */
    final String description

    /** Entity ids of {@link StepShape} objects that describe this product's geometry. */
    final List<Integer> shapeEntityIds

    /** Child products (sub-assemblies / components). */
    final List<StepProduct> children

    StepProduct(
        int entityId,
        String partId,
        String name,
        String description,
        List<Integer> shapeEntityIds,
        List<StepProduct> children
    ) {
        this.entityId       = entityId
        this.partId         = partId
        this.name           = name
        this.description    = description
        this.shapeEntityIds = shapeEntityIds ?: []
        this.children       = children       ?: []
    }

    /** Convenience: is this product a leaf (no children)? */
    boolean isLeaf() { children.isEmpty() }
}
