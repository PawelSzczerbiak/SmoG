package com.pawelszczerbiak.smog;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pawelszczerbiak.smog.Day.fromInt;
import static com.pawelszczerbiak.smog.PollutionNorms.DANGER_VALUE_ARBITRARY;
import static com.pawelszczerbiak.smog.PollutionType.C6H6;
import static com.pawelszczerbiak.smog.PollutionType.PM10;
import static com.pawelszczerbiak.smog.PollutionType.PM25;
import static com.pawelszczerbiak.smog.QueryUtils.POLLUTION_DEFAULT_VALUE;
import static com.pawelszczerbiak.smog.StationAdapter.*;

public class PlotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_activity);

        Station station = (Station) getIntent().getSerializableExtra("station");

        // Available graphs (plots)
        GraphView graph_today = findViewById(R.id.graph_today);
        GraphView graph_yesterday = findViewById(R.id.graph_yesterday);
        GraphView graph_before_yesterday = findViewById(R.id.graph_before_yesterday);

        // Data: pollutions and dates
        Map<PollutionType, List<Double>> pollutionsMap = station.getPollutions();
        Map<PollutionType, List<String>> datesMap = station.getDates();

        // Pollution type that are added as separate lines
        final PollutionType[] POLLUTION_TYPES = {PM25, PM10, C6H6};

        // Maximal value of all pollutions [%]
        double maxValuePercent = 0;

        // Location name and type views
        TextView locationView = findViewById(R.id.location_plot);
        TextView locationTypeView = findViewById(R.id.location_type_plot);
        String locationType = station.getLocationType();
        locationView.setText(station.getLocation());
        locationTypeView.setText(getLocationTypeText(locationType));
        locationTypeView.setTextColor(ContextCompat.getColor(getApplicationContext(), getLocationTypeColor(locationType)));

        /**
         * Iterating over keys
         */
        for (int type_index = 0; type_index < POLLUTION_TYPES.length; type_index++) {

            final PollutionType POLLUTION_TYPE = POLLUTION_TYPES[type_index];
            final int COLOR = getLineColor(POLLUTION_TYPE);

            // Dates for specific key
            // - use of full map is not efficient
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
             * Retrieve data in reverse (growing with time) order
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
             * Fill series with data for specific day
             */
            for (int day : hoursForGivenDay.keySet()) {
                List<Integer> hours = hoursForGivenDay.get(day);
                List<Double> values = pollutionsForGivenDay.get(day);
                final int SIZE_DAY = hours.size();
                // New data points
                DataPoint[] points = new DataPoint[SIZE_DAY];
                // Add data to data points
                for (int i = 0; i < SIZE_DAY; i++) {
                    double percentValue = transformValueToPercent(values.get(i), POLLUTION_TYPE);
                    if (percentValue > maxValuePercent) {
                        maxValuePercent = percentValue;
                    }
                    points[i] = new DataPoint(hours.get(i), percentValue);
                }
                switch (fromInt(day)) {
                    case TODAY:
                        seriesToday = new LineGraphSeries<>(points);
                        break;
                    case YESTERDAY:
                        seriesYesterday = new LineGraphSeries<>(points);
                        break;
                    case BEFORE_YESTERDAY:
                        seriesBeforeYesterday = new LineGraphSeries<>(points);
                }
            }

            // Add lines to the plots
            seriesToday.setColor(COLOR);
            graph_today.addSeries(seriesToday);
            seriesYesterday.setColor(COLOR);
            graph_yesterday.addSeries(seriesYesterday);
            seriesBeforeYesterday.setColor(COLOR);
            graph_before_yesterday.addSeries(seriesBeforeYesterday);
        }

        // Add horizontal lines with 100% values
        LineGraphSeries<DataPoint> series100percent = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, 100), new DataPoint(23, 100)});
        series100percent.setDrawBackground(true);
        series100percent.setThickness(0);
        series100percent.setColor(Color.GRAY);
        series100percent.setBackgroundColor(Color.argb(50, 0, 200, 0));
        graph_today.addSeries(series100percent);
        graph_yesterday.addSeries(series100percent);
        graph_before_yesterday.addSeries(series100percent);

        // Add danger line

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(6);
        paint.setColor(Color.RED);
        paint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));

        LineGraphSeries<DataPoint> seriesDangerPercent = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, DANGER_VALUE_ARBITRARY), new DataPoint(23, DANGER_VALUE_ARBITRARY)});
        seriesDangerPercent.setDrawAsPath(true);
        seriesDangerPercent.setCustomPaint(paint);

        graph_today.addSeries(seriesDangerPercent);
        graph_yesterday.addSeries(seriesDangerPercent);
        graph_before_yesterday.addSeries(seriesDangerPercent);

        // Resize graph
        int maxSizeY = roundPollutionValue(maxValuePercent);
        resizeGraph(graph_today, maxSizeY);
        resizeGraph(graph_yesterday, maxSizeY);
        resizeGraph(graph_before_yesterday, maxSizeY);
    }

    /**
     * Rounds pollution value
     */
    private int roundPollutionValue(double maxValue) {
        if (maxValue < 400) {
            return 400; // 100% line must be always visible
        } else {
            int res = (int) Math.ceil(maxValue / 100.) * 100;
            return (res % 200 == 0) ? res : res + 100;

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

    /**
     * Transforms pollution value to percent
     */
    private double transformValueToPercent(double value, PollutionType type) {
        switch (type) {
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
     * Changes graphs size
     */
    private void resizeGraph(GraphView graph, int maxY) {
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(23);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(maxY);
        graph.getViewport().setYAxisBoundsManual(true);
    }

    /**
     * Gives line color for a given pollution type
     */
    private int getLineColor(PollutionType type) {
        switch (type) {
            case PM25:
                return getResources().getColor(R.color.colorLinePM25);
            case PM10:
                return getResources().getColor(R.color.colorLinePM10);
            case C6H6:
                return getResources().getColor(R.color.colorLineC6H6);
        }
        return 0;
    }
}
