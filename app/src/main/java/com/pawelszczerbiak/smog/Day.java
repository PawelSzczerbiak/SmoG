package com.pawelszczerbiak.smog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public enum Day {
    TODAY(getDayOfMonth(0)),
    YESTERDAY(getDayOfMonth(-1)),
    BEFORE_YESTERDAY(getDayOfMonth(-2));

    private int dayOfMonth;

    Day(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    /**
     * Returns day of month
     */
    private static int getDayOfMonth(int i) {
        Calendar cal = Calendar.getInstance();
        if (i == 0) {
            return cal.get(Calendar.DAY_OF_MONTH);
        } else {
            cal.add(Calendar.DATE, i);
            Date date = cal.getTime();
            DateFormat dateFormat = new SimpleDateFormat("dd");
            return Integer.parseInt(dateFormat.format(date));
        }
    }

    public static Day fromInt(int value) {
        for (Day day : Day.values()) {
            if (day.dayOfMonth == value) {
                return day;
            }
        }
        return null;
    }

}
