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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_activity);

        GraphView graph_today = findViewById(R.id.graph_today);
        GraphView graph_yesterday = findViewById(R.id.graph_yesterday);
        GraphView graph_before_yesterday = findViewById(R.id.graph_before_yesterday);

        Station station = (Station) getIntent().getSerializableExtra("station");

        Map<String, List<Double>> pollutions = station.getPollutions();
        Map<String, List<String>> dates = station.getDates();

        final int SIZE = pollutions.get("PM2.5").size();
        Map<Double, List<Double>> hoursForGivenDay = new LinkedHashMap<>();
        Map<Double, List<Double>> pollutionsForGivenDay = new LinkedHashMap<>();

        int index = -1;
        double currentDay = -1;
        for (int i = 0; i < SIZE; i++) {
            String date = formatDate(dates.get("PM2.5").get(i));
            String[] parts = date.split(":");
            double day = Double.parseDouble(parts[0]);
            double hour = Double.parseDouble(parts[1]);
            if (currentDay != day) {
                currentDay = day;
                index++;
                hoursForGivenDay.put(day, new ArrayList<Double>());
                pollutionsForGivenDay.put(day, new ArrayList<Double>());
            }
            hoursForGivenDay.get(day).add(hour);
            pollutionsForGivenDay.get(day).add(pollutions.get("PM2.5").get(i));
        }

        LineGraphSeries<DataPoint> seriesToday = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> seriesYesterday = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> seriesBeforeYesterday = new LineGraphSeries<>();

        // for a given day:

        index = -1;
        currentDay = -1;
        for (Double day : hoursForGivenDay.keySet()) {
            // New data points
            DataPoint[] points = new DataPoint[hoursForGivenDay.get(day).size()];
            // Reverse order
            Collections.reverse(hoursForGivenDay.get(day));
            Collections.reverse(pollutionsForGivenDay.get(day));
            // Adding data
            for (int i = 0; i < hoursForGivenDay.get(day).size(); i++) {
                points[i] = new DataPoint(hoursForGivenDay.get(day).get(i),
                        pollutionsForGivenDay.get(day).get(i));
            }
            if (currentDay != day) {
                currentDay = day;
                index++;
            }
            switch(index){
                case 0:
                    seriesToday = new LineGraphSeries<>(points);
                    break;
                case 1:
                    seriesYesterday = new LineGraphSeries<>(points);
                    break;
                case 2:
                    seriesBeforeYesterday = new LineGraphSeries<>(points);
                    break;
            }
        }

        // styling series
        seriesToday.setColor(Color.BLUE);
        graph_today.addSeries(seriesToday);
        graph_today.getViewport().setMinX(0);
        graph_today.getViewport().setMaxX(23);
        graph_today.getViewport().setXAxisBoundsManual(true);

        seriesYesterday.setColor(Color.BLUE);
        graph_yesterday.addSeries(seriesYesterday);
        graph_yesterday.getViewport().setMinX(0);
        graph_yesterday.getViewport().setMaxX(23);
        graph_yesterday.getViewport().setXAxisBoundsManual(true);

        seriesBeforeYesterday.setColor(Color.BLUE);
        graph_before_yesterday.addSeries(seriesBeforeYesterday);
        graph_before_yesterday.getViewport().setMinX(0);
        graph_before_yesterday.getViewport().setMaxX(23);
        graph_before_yesterday.getViewport().setXAxisBoundsManual(true);
    }

    /**
     * Formats date
     */
    String formatDate(String oldDateString) {

        // Old date format
        DateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date oldDate = null;
        try {
            oldDate = oldFormat.parse(oldDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DateFormat newFormat = new SimpleDateFormat("dd:HH");
        String newDateString = newFormat.format(oldDate);
        return newDateString;
    }

}
