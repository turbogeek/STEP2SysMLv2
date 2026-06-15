package MassCalculations {
    private import ScalarValues::*;
    private import ISQ::*;
    private import SI::*;
    public import MaterialsLibrary::*;

    doc
    /*
     * Library for mass and physical property calculations.
     */

    calc def CalculateMass {
        doc /* Calculates the mass in kg given the volume in m^3 and a standard Material. */
        in vol_m3 : Real; // Volume in cubic meters
        in mat : Material;
        return mass_kg : Real = vol_m3 * mat.density;
    }
}
