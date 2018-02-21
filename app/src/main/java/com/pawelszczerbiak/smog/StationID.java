package com.pawelszczerbiak.smog;

import java.util.List;

/**
 * An {@link StationID} object contains information
 * related to a single station pollutionType and ID data
 */
public class StationID {
    private String location;
    private String locationType;
    private List<Integer> IDs;

    public StationID(String location, String locationType, List<Integer> IDs) {
        this.location = location;
        this.locationType = locationType;
        this.IDs = IDs;
    }

    public String getLocation() {
        return location;
    }

    public String getLocationType() {
        return locationType;
    }

    public List<Integer> getIDs() {
        return IDs;
    }
}
