package com.pawelszczerbiak.smog;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * An {@link Station} object contains information
 * related to a single station
 */
public class Station implements Serializable {

    private String location;
    private String locationType;
    private Map<PollutionType, List<String>> dates;
    private Map<PollutionType, List<Double>> pollutions;

    public Station(String location, String locationType, Map<PollutionType, List<String>> dates, Map<PollutionType, List<Double>> pollutions) {
        this.location = location;
        this.locationType = locationType;
        this.dates = dates;
        this.pollutions = pollutions;
    }

    public String getLocationType() {
        return locationType;
    }

    public String getLocation() {
        return location;
    }

    public Map<PollutionType, List<String>> getDates() {
        return dates;
    }

    public Map<PollutionType, List<Double>> getPollutions() {
        return pollutions;
    }
}
