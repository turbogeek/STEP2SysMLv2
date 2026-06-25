package TestPackage {
    private import ScalarValues::*;
    part def Polyhedron {
        attribute vertices : Real[0..*];
        attribute faces : Integer[0..*];
    }
    part test_hull : Polyhedron {
        :>> vertices = (0.0, 1.0, 2.0);
        :>> faces = (0, 1, 2);
    }
}
