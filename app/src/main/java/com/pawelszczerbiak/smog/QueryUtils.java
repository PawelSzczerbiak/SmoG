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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.pawelszczerbiak.smog.PollutionType.C6H6;
import static com.pawelszczerbiak.smog.PollutionType.NO2;
import static com.pawelszczerbiak.smog.PollutionType.PM10;
import static com.pawelszczerbiak.smog.PollutionType.PM25;
import static com.pawelszczerbiak.smog.PollutionType.SO2;
import static com.pawelszczerbiak.smog.PollutionType.fromString;

/**
 * Performs JSON requests specified in {@link StationID} IDs
 * and enters obtained data to {@link Station} list
 */
public class QueryUtils {

    //Tag for the log messages
    public static final String LOG_TAG = StationActivity.class.getSimpleName();

    public static final double POLLUTION_DEFAULT_VALUE = -1.;

    // Private constructor - no one should ever create an object of this class
    private QueryUtils() {
    }

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
            stations.add(extractFeaturesFromJson(stationID.getLocation(), stationID.getLocationType(), jsonResponses));
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
     * Extracts data from JSON for a given station
     */
    private static Station extractFeaturesFromJson(String location, String locationType, List<String> jsonResponses) {

        // In dates elements are ordered according to their insertion
        Map<PollutionType, List<String>> dates = new LinkedHashMap<>();
        Map<PollutionType, List<Double>> pollutions = new HashMap<>();
        // Maps initialization
        initializeMaps(pollutions, dates);

        for (String jsonResponse : jsonResponses) {
            try {
                JSONObject root = new JSONObject(jsonResponse);
                // possibilities: PM2.5, PM10, SO2, NO2, C6H6
                PollutionType pollutionType = fromString(root.getString("key"));
                JSONArray values = root.getJSONArray("values");
                // values for specific dates (we retrieve up to 10)
                for (int i = 0; i < values.length(); i++) {
                    JSONObject value = values.getJSONObject(i);
                    if (!value.isNull("value")) {
                        String date = value.getString("date");
                        dates.get(pollutionType).add(date); // TODO: more efficient
                        double val = value.getDouble("value");
                        // Replacing the first element
                        if (pollutions.get(pollutionType).get(0) == POLLUTION_DEFAULT_VALUE) {
                            pollutions.get(pollutionType).set(0, val);
                        } else {
                            pollutions.get(pollutionType).add(val);
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("QueryUtils", "Problem parsing the JSON results", e);
            }
        }

        return new Station(location, locationType, dates, pollutions);
    }

    /**
     * Initialize pollutions for a given station with negative values
     */
    private static void initializeMaps(Map<PollutionType, List<Double>> pollutions, Map<PollutionType, List<String>> dates) {
        pollutions.put(PM25, new ArrayList<>(Arrays.asList(POLLUTION_DEFAULT_VALUE)));
        pollutions.put(PM10, new ArrayList<>(Arrays.asList(POLLUTION_DEFAULT_VALUE)));
        pollutions.put(C6H6, new ArrayList<>(Arrays.asList(POLLUTION_DEFAULT_VALUE)));
        pollutions.put(SO2, new ArrayList<>(Arrays.asList(POLLUTION_DEFAULT_VALUE)));
        pollutions.put(NO2, new ArrayList<>(Arrays.asList(POLLUTION_DEFAULT_VALUE)));
        dates.put(PM25, new ArrayList<String>());
        dates.put(PM10, new ArrayList<String>());
        dates.put(C6H6, new ArrayList<String>());
        dates.put(SO2, new ArrayList<String>());
        dates.put(NO2, new ArrayList<String>());
    }

}
