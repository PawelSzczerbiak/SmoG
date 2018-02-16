package com.pawelszczerbiak.smog;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs JSON requests specified in {@link StationID} IDs
 * and enters obtained data to {@link Station} list
 */
public class QueryUtils {

    //Tag for the log messages
    public static final String LOG_TAG = StationActivity.class.getSimpleName();

    // Private constructor - no one should ever create an object of this class
    private QueryUtils() {}

    public static List<Station> extractStations(String URL_BASE) {

        // List of stations' IDs
        final List<StationID> stationIDs = DataRepository.getStationIDs();
        // List of stations to be returned
        final List<Station> stations = new ArrayList<>();

        for (StationID stationID : stationIDs) {

            // List of JSON responses for URLs created from IDs
            List<String> jsonResponses = new ArrayList<>();

            for (Integer id : stationID.getIDs()) {
                // Create URL object
                URL url = createUrl(URL_BASE + id);

                // Perform HTTP request to the URL and receive a JSON response back
                String jsonResponse = null;
                try {
                    jsonResponse = makeHttpRequest(url);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Problem making the HTTP request.", e);
                }
                jsonResponses.add(jsonResponse);
            }

            // Extract relevant fields from the JSON responses and create a list of {@link Station}s
            stations.add(extractFeaturesFromJson(stationID.getLocation(), stationID.getType(), jsonResponses));
        }

        // Return the list of {@link Station}s
        return stations;
    }

    /**
     * Returns new URL object from the given string URL
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     *  Extracts data from JSON for a given station
     */
    private static Station extractFeaturesFromJson(String location, String type, List<String> jsonResponses) {

        // Elements ordered according to their insertion
        // see ordering in: @initializePollutions
        Map<String, String> dates = new LinkedHashMap<>();
        Map<String, Double> pollutions = new LinkedHashMap<>();
        // Pollutions' map initialization
        initializePollutions(pollutions);

        for (String jsonResponse : jsonResponses) {
            try {
                JSONObject root = new JSONObject(jsonResponse);
                // possibilities: PM2.5, PM10, SO2, NO2, C6H6
                String key = root.getString("key");
                JSONArray values = root.getJSONArray("values");
                // values for specific dates
                for (int i = 0; i < values.length(); i++) {
                    JSONObject value = values.getJSONObject(i);
                    if (!value.isNull("value")) {
                        String date = value.getString("date");
                        double val = value.getDouble("value");
                        pollutions.put(key, val);
                        dates.put(key, date);
                        // we took the first not-null value
                        break;
                    }
                }

            } catch (JSONException e) {
                Log.e("QueryUtils", "Problem parsing the JSON results", e);
            }
        }

        return new Station(location, type, dates, pollutions);
    }

    // Initialize pollutions for a given station with negative values
    private static void initializePollutions(Map<String, Double> pollutions) {
        pollutions.put("PM2.5", -1.);
        pollutions.put("PM10", -1.);
        pollutions.put("C6H6", -1.);
        pollutions.put("SO2", -1.);
        pollutions.put("NO2", -1.);
    }

}
