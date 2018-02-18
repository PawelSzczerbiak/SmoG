package com.pawelszczerbiak.smog;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An {@link Station} object contains information
 * related to a single station
 */
public class Station implements Serializable {

    private String location;
    private String type;
    private Map<String, List<String>> dates;
    private Map<String, List<Double>> pollutions;

    public Station(String location, String type, Map<String, List<String>> dates, Map<String, List<Double>> pollutions) {
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

    public Map<String, List<String>> getDates() {
        return dates;
    }

    public Map<String, List<Double>> getPollutions() {
        return pollutions;
    }
}
