package com.pawelszczerbiak.smog;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pawelszczerbiak.smog.PollutionType.C6H6;
import static com.pawelszczerbiak.smog.PollutionType.PM10;
import static com.pawelszczerbiak.smog.PollutionType.PM25;
import static com.pawelszczerbiak.smog.QueryUtils.POLLUTION_DEFAULT_VALUE;

public class PlotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_activity);

        // Available graphs (plots)
        GraphView graph_today = findViewById(R.id.graph_today);
        GraphView graph_yesterday = findViewById(R.id.graph_yesterday);
        GraphView graph_before_yesterday = findViewById(R.id.graph_before_yesterday);

        // Days for which we will present plots
        final int TODAY = getDayOfMonth(0);
        final int YESTERDAY = getDayOfMonth(-1);
        final int BEFORE_YESTERDAY = getDayOfMonth(-2);

        // Data: pollutions and dates
        Station station = (Station) getIntent().getSerializableExtra("station");
        Map<PollutionType, List<Double>> pollutionsMap = station.getPollutions();
        Map<PollutionType, List<String>> datesMap = station.getDates();

        // Pollution's type to be added as separate lines
        final PollutionType[] POLLUTION_TYPES = {PM25, PM10, C6H6};
        final int[] COLORS = {Color.RED, Color.BLUE, Color.BLACK};

        // Maximum value of all pollutions [%]
        double maxValuePercent = 0;

        /**
         * Iterating over keys
         */
        for (int type_index = 0; type_index < POLLUTION_TYPES.length; type_index++) {

            final PollutionType POLLUTION_TYPE = POLLUTION_TYPES[type_index];
            final int COLOR = COLORS[type_index];

            // Dates for specific key
            // Using full map is not efficient
            List<String> datesForGivenKey = datesMap.get(POLLUTION_TYPE);

            // If data for specific key are not available - go to another key
            if (pollutionsMap.get(POLLUTION_TYPE).get(0) == POLLUTION_DEFAULT_VALUE) {
                continue;
            }

            // Data series (lines)
            LineGraphSeries<DataPoint> seriesToday = new LineGraphSeries<>();
            LineGraphSeries<DataPoint> seriesYesterday = new LineGraphSeries<>();
            LineGraphSeries<DataPoint> seriesBeforeYesterday = new LineGraphSeries<>();

            final int SIZE = pollutionsMap.get(POLLUTION_TYPE).size();

            // Maps that contains data for specific day
            // Order is crucial because we will later plot them
            Map<Integer, List<Integer>> hoursForGivenDay = new HashMap<>();
            Map<Integer, List<Double>> pollutionsForGivenDay = new HashMap<>();

            /**
             * Retrieving data in reverse (growing with time) order
             * The oldest data will be at the beginning
             */
            for (int i = SIZE - 1; i >= 0; i--) {
                String[] parts = separateDate(datesForGivenKey.get(i));
                int day = Integer.parseInt(parts[0]);
                int hour = Integer.parseInt(parts[1]);
                double value = pollutionsMap.get(POLLUTION_TYPE).get(i);
                try {
                    hoursForGivenDay.get(day).add(hour);
                    pollutionsForGivenDay.get(day).add(value);
                } catch (NullPointerException e) {
                    hoursForGivenDay.put(day, new ArrayList<>(Arrays.asList(hour)));
                    pollutionsForGivenDay.put(day, new ArrayList<>(Arrays.asList(value)));
                }
            }

            /**
             * Filling series with data
             */
            for (int day : hoursForGivenDay.keySet()) {
                List<Integer> hours = hoursForGivenDay.get(day);
                List<Double> values = pollutionsForGivenDay.get(day);
                final int SIZE_DAY = hours.size();
                // New data points
                DataPoint[] points = new DataPoint[SIZE_DAY];
                // Adding data to data points
                for (int i = 0; i < SIZE_DAY; i++) {
                    double percentValue = transformValueToPercent(values.get(i), POLLUTION_TYPE);
                    if (percentValue > maxValuePercent) {
                        maxValuePercent = percentValue;
                    }
                    points[i] = new DataPoint(hours.get(i), percentValue);
                }
                // TODO: enums
                if (day == TODAY) {
                    seriesToday = new LineGraphSeries<>(points);
                } else if (day == YESTERDAY) {

                    seriesYesterday = new LineGraphSeries<>(points);
                } else if (day == BEFORE_YESTERDAY) {
                    seriesBeforeYesterday = new LineGraphSeries<>(points);
                }
            }

            // Adding lines to the plots
            seriesToday.setColor(COLOR);
            graph_today.addSeries(seriesToday);
            seriesYesterday.setColor(COLOR);
            graph_yesterday.addSeries(seriesYesterday);
            seriesBeforeYesterday.setColor(COLOR);
            graph_before_yesterday.addSeries(seriesBeforeYesterday);
        }

        // Adding horizontal lines with 100% values
        LineGraphSeries<DataPoint> series100percent = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, 100),
                new DataPoint(23, 100)
        });
        series100percent.setDrawBackground(true);
        series100percent.setThickness(0);
        series100percent.setColor(Color.GRAY);
        series100percent.setBackgroundColor(Color.argb(50, 0, 200, 0));
        graph_today.addSeries(series100percent);
        graph_yesterday.addSeries(series100percent);
        graph_before_yesterday.addSeries(series100percent);

        // Resizing graph
        int maxSizeY = roundPollutionValue(maxValuePercent);
        resizeGraph(graph_today, maxSizeY);
        resizeGraph(graph_yesterday, maxSizeY);
        resizeGraph(graph_before_yesterday, maxSizeY);
    }

    /**
     * Rounds pollution's value
     */
    private int roundPollutionValue(double maxValue) {
        if (maxValue < 200) {
            return 200; // 100% line must be always visible
        } else if (maxValue >= 200 && maxValue < 1000) {
            return (int) Math.ceil(maxValue / 100.) * 100;
        } else {
            return (int) Math.ceil(maxValue / 1000.) * 1000;
        }
    }

    /**
     * Returns day of month
     */
    private int getDayOfMonth(int i) {
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

    /**
     * Day and hour separation
     */
    String[] separateDate(String oldDateString) {

        // Old date format
        DateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date oldDate = null;
        try {
            oldDate = oldFormat.parse(oldDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // New date format
        DateFormat newFormat = new SimpleDateFormat("dd:HH");
        String newDateString = newFormat.format(oldDate);
        // Day and hour separation
        String[] parts = newDateString.split(":");

        return parts;
    }

    private double transformValueToPercent(double value, PollutionType key) {
        switch (key) {
            case PM25:
                return 100. * value / PollutionNorms.NORM_PM25;
            case PM10:
                return 100. * value / PollutionNorms.NORM_PM10;
            case C6H6:
                return 100. * value / PollutionNorms.NORM_C6H6;
            case SO2:
                return 100. * value / PollutionNorms.NORM_SO2;
            case NO2:
                return 100. * value / PollutionNorms.NORM_NO2;
            default:
                return 0;
        }
    }

    /**
     * Changes size of graphs
     */
    private void resizeGraph(GraphView graph, int maxY) {
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(23);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(maxY);
        graph.getViewport().setYAxisBoundsManual(true);
    }
}
