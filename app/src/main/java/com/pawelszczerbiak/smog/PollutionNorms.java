package com.pawelszczerbiak.smog;

/**
 * Contains norms and reference values for all pollutions
 */
public final class PollutionNorms {

    // Private constructor - no one should ever create an object of this class
    private PollutionNorms() {}

    // norms for different pollutions [um/m^3]
    public static final int NORM_PM25 = 20;
    public static final int NORM_PM10 = 50;
    public static final int NORM_C6H6 = 5;
    public static final int NORM_SO2 = 350;
    public static final int NORM_NO2 = 200;
    // reference values for pollutions for distinct colors [um/m^3]
    public static final int[] TABLE_REF_PM25 = new int[]{12, 36, 60, 84, 120};
    public static final int[] TABLE_REF_PM10 = new int[]{20, 60, 100, 140, 200};
    public static final int[] TABLE_REF_C6H6 = new int[]{5, 10, 15, 20, 50};
    public static final int[] TABLE_REF_SO2 = new int[]{50, 100, 200, 350, 500};
    public static final int[] TABLE_REF_NO2 = new int[]{40, 100, 150, 200, 400};
    // Danger values in % (used on plots)
    public static final double DANGER_PM25 = 100.*TABLE_REF_PM25[3]/NORM_PM25;
    public static final double DANGER_PM10 = 100.*TABLE_REF_PM10[3]/NORM_PM10;
    public static final double DANGER_C6H6 = 100.*TABLE_REF_C6H6[3]/NORM_C6H6;
    public static final double DANGER_SO2 = 100.*TABLE_REF_SO2[3]/NORM_SO2;
    public static final double DANGER_NO2 = 100.*TABLE_REF_NO2[3]/NORM_NO2;
    public static final double DANGER_VALUE_ARBITRARY = 400.;
}
