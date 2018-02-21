package com.pawelszczerbiak.smog;

public enum PollutionType {
    PM25("PM2.5"),
    PM10("PM10"),
    C6H6("C6H6"),
    SO2("SO2"),
    NO2("NO2");

    private String type;

    PollutionType(String type) {
        this.type = type;
    }

    public static PollutionType fromString(String type) {
        for (PollutionType pollutionType : PollutionType.values()) {
            if (pollutionType.type.equalsIgnoreCase(type)) {
                return pollutionType;
            }
        }
        return null;
    }

}

