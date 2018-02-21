package com.pawelszczerbiak.smog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains stations' data
 */
public class DataRepository {

    // List of stations' IDs to be added to the URL
    private static List<StationID> stationIDs = new ArrayList<>();

    // Location types
    public static final String IMPORTANT_CITIES = "Ważne miejscowości";
    public static final String PODHALE = "Podhale";
    public static final String BESKIDY_ZACH = "Beskidy Zachodnie";
    public static final String BESKIDY_WSCH = "Beskidy Wschodnie";
    public static final String SUDETY = "Sudety";
    public static final String JURA = "Jura";

    static {
        /**
         * Locations are displayed on the screen according to their insertions below
         */
        stationIDs.add(new StationID("Kraków ul. Bujaka", IMPORTANT_CITIES, Arrays.asList(2772, 2770, 17896, 2774, 2766)));
        stationIDs.add(new StationID("Kraków al. Krasińskiego", IMPORTANT_CITIES, Arrays.asList(2752, 2750, 16500, 2747)));
/*
        stationIDs.add(new StationID("Kraków ul. Bulwarowa", IMPORTANT_CITIES, Arrays.asList(2794, 2792, 2779, 2797, 2788)));
//        stationsData.put("Kraków ul. Dietla", IMPORTANT_CITIES, Arrays.asList(16377, 16516));
        stationIDs.add(new StationID("Tarnów", IMPORTANT_CITIES, Arrays.asList(3070, 3073, 3066)));
        stationIDs.add(new StationID("Zgierz", IMPORTANT_CITIES, Arrays.asList(2378, 2377, 2382, 2373)));

        stationIDs.add(new StationID("Nowy Targ", PODHALE, Arrays.asList(16905, 16904)));
        stationIDs.add(new StationID("Nowy Sącz", PODHALE, Arrays.asList(2941, 2944, 2938)));
        stationIDs.add(new StationID("Zakopane", PODHALE, Arrays.asList(3195, 16706, 3198, 3191)));
*/

        stationIDs.add(new StationID("Sucha Beskidzka", BESKIDY_ZACH, Arrays.asList(16498)));
 /*       stationIDs.add(new StationID("Żywiec", BESKIDY_ZACH, Arrays.asList(14916, 14915, 14917)));
        stationIDs.add(new StationID("Bielsko-Biała", BESKIDY_ZACH, Arrays.asList(5167, 5171, 5162)));
        stationIDs.add(new StationID("Ustroń", BESKIDY_ZACH, Arrays.asList(5515, 5516, 5512)));
        stationIDs.add(new StationID("Cieszyn", BESKIDY_ZACH, Arrays.asList(5212, 5216, 5208)));

        stationIDs.add(new StationID("Przemyśl", BESKIDY_WSCH, Arrays.asList(4338, 4336, 14771, 4339, 4333)));
        stationIDs.add(new StationID("Rymanów-Zdrój", BESKIDY_WSCH, Arrays.asList(17185, 17184)));
//        stationIDs.add(new StationID("Krempna",BESKIDY_WSCH, Arrays.asList(14751, 14753)));
//        stationIDs.add(new StationID("Szymbark",BESKIDY_WSCH, Arrays.asList(3058, 3055)));

//        stationIDs.add(new StationID("Czerniawa", SUDETY, Arrays.asList(56, 50)));
        stationIDs.add(new StationID("Wałbrzych", SUDETY, Arrays.asList(618, 605, 621, 614)));
        stationIDs.add(new StationID("Kłodzko", SUDETY, Arrays.asList(224, 225, 221)));
        stationIDs.add(new StationID("Nowa Ruda", SUDETY, Arrays.asList(14706)));
        stationIDs.add(new StationID("Jelenia Góra", SUDETY, Arrays.asList(14731, 14730, 14734, 14733, 14727)));
*///        stationIDs.add(new StationID("Złoty Potok", JURA, Arrays.asList(5619, 5618, 5600, 5623, 5612)));
//        stationIDs.add(new StationID("Częstochowa",JURA, Arrays.asList(5232	, 5233, 5230)));
//        stationIDs.add(new StationID("Trzebinia",JURA, Arrays.asList(3123, 3127, 3120)));
//        stationIDs.add(new StationID("Olkusz",JURA, Arrays.asList(2976)));
//        stationIDs.add(new StationID("Starachowice",JURA, Arrays.asList(17249)));
    }

    // Private constructor - no one should ever create an object of this class
    private DataRepository() {
    }

    public static List<StationID> getStationIDs() {
        return stationIDs;
    }
}
