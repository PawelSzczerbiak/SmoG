package com.pawelszczerbiak.smog;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Loads a list of stations by using an AsyncTask to perform the
 * network request to the given URL
 */
public class StationLoader extends AsyncTaskLoader<List<Station>> {

    /**
     * Query URL
     */
    private String mUrl;

    /**
     * Adapter for the list of stations
     */
    public StationLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Station> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of stations
        List<Station> stations = QueryUtils.extractStations(mUrl);
        return stations;
    }
}
