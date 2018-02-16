package com.pawelszczerbiak.smog;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * An {@link Station} object contains information
 * related to a single station
 */
public class Station {

    private String location;
    private String type;
    private Map<String, String> dates;
    private Map<String, Double> pollutions;

    public Station(String location, String type, Map<String, String> dates, Map<String, Double> pollutions) {
        this.location = location;
        this.type = type;
        this.dates = dates;
        this.pollutions = pollutions;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public Map<String, String> getDates() {
        return dates;
    }

    public Map<String, Double> getPollutions() {
        return pollutions;
    }
}
