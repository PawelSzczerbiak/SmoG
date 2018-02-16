package com.pawelszczerbiak.smog;

import java.util.List;

/**
 * An {@link StationID} object contains information
 * related to a single station type and ID data
 */
public class StationID {
    private String location;
    private String type;
    private List<Integer> IDs;

    public StationID(String location, String type, List<Integer> IDs) {
        this.location = location;
        this.type = type;
        this.IDs = IDs;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public List<Integer> getIDs() {
        return IDs;
    }
}
