package com.pawelszczerbiak.smog;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StationActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Station>> {

    /**
     * Constant value for the station loader ID. We can choose any integer
     * This really only comes into play if you're using multiple loaders
     */
    private static final int STATION_LOADER_ID = 1;

    /**
     * URL with JSON data
     * At the end of the URL id must be added (see @extractStations method)
     */
    private static final String URL_BASE =
            "http://api.gios.gov.pl/pjp-api/rest/data/getData/";

    /**
     * Adapter for the list of stations
     */
    private StationAdapter mAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_activity);

        // Check whether there is Internet connection
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // Create a list of stations
        ListView stationListView = findViewById(R.id.list);

        mEmptyStateTextView = findViewById(R.id.empty_view);
        stationListView.setEmptyView(mEmptyStateTextView);

        if (isConnected == false) {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            // Set empty state text to display "No Internet connection."
            mEmptyStateTextView.setText(R.string.no_internet);
        } else {

            // Create a new adapter that takes the list of stations as input
            mAdapter = new StationAdapter(this, 0, new ArrayList<Station>());

            // Set the adapter on the {@link ListView}
            // so the list can be populated in the user interface
            stationListView.setAdapter(mAdapter);

            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(STATION_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<List<Station>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        return new StationLoader(this, URL_BASE);
    }

    @Override
    public void onLoadFinished(Loader<List<Station>> loader, List<Station> stations) {

        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No stations found."
        mEmptyStateTextView.setText(R.string.no_stations);

        // Clear the adapter of previous station data
        mAdapter.clear();

        // If there is a valid list of {@link Station}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (stations != null && !stations.isEmpty()) {
            mAdapter.addAll(stations);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Station>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

}
