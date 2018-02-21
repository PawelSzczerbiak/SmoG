package com.pawelszczerbiak.smog;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StationAdapter extends ArrayAdapter<Station> {

    public StationAdapter(@NonNull Context context, int resource, @NonNull List<Station> stations) {
        super(context, resource, stations);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.station_list_item, parent, false);
        }

        final Station currentStation = getItem(position);

        // We join all dates' lists together
        // The first value, if exists, will be displayed on the screen
        List<String> dates = new ArrayList<>();
        for (List<String> list : currentStation.getDates().values()) {
            dates.addAll(list);
        }
        Map<PollutionType, List<Double>> pollutions = currentStation.getPollutions();
        String locationType = currentStation.getLocationType();

        // Views to be changed
        TextView locationView = listItemView.findViewById(R.id.location);
        TextView dateView = listItemView.findViewById(R.id.date);
        // OLD IDEA: Changes layout color depending on the station's type
        // LinearLayout stationInfoLayout = (LinearLayout) listItemView.findViewById(R.id.stationInfo);
        // stationInfoLayout.setBackgroundColor(ContextCompat.getColor(getContext(), getLayoutColor(type)));
        // NEW IDEA: Changes text and text color of the station's type label
        TextView typeView = listItemView.findViewById(R.id.locationType);
        typeView.setText(getLocationTypeText(locationType));
        typeView.setTextColor(ContextCompat.getColor(getContext(), getLocationTypeColor(locationType)));

        /**
         * Format data from current station: date
         * First date is for PM2.5 which is more important than PM10 etc.
         * Note: dates are ordered according to IDs @stationsData
         * but we can change ordering by initialization the data map
         */
        String formattedDate = (dates.size() == 0) ? "" : formatDate(dates.get(0));
        // Insert formatted data into the views
        locationView.setText(currentStation.getLocation());
        dateView.setText(formattedDate);

        /**
         *  Format data from current station: pollutions
         */
        for (PollutionType key : pollutions.keySet()) {
            switch (key) {
                case PM25:
                    changePollutionView(listItemView, pollutions.get(key).get(0),
                            R.id.val_PM25, R.id.label_PM25,
                            PollutionNorms.TABLE_REF_PM25, PollutionNorms.NORM_PM25);
                    break;
                case PM10:
                    changePollutionView(listItemView, pollutions.get(key).get(0),
                            R.id.val_PM10, R.id.label_PM10,
                            PollutionNorms.TABLE_REF_PM10, PollutionNorms.NORM_PM10);
                    break;
                case C6H6:
                    changePollutionView(listItemView, pollutions.get(key).get(0),
                            R.id.val_C6H6, R.id.label_C6H6,
                            PollutionNorms.TABLE_REF_C6H6, PollutionNorms.NORM_C6H6);
                    break;
                case SO2:
                    changePollutionView(listItemView, pollutions.get(key).get(0),
                            R.id.val_SO2, R.id.label_SO2,
                            PollutionNorms.TABLE_REF_SO2, PollutionNorms.NORM_SO2);
                    break;
                case NO2:
                    changePollutionView(listItemView, pollutions.get(key).get(0),
                            R.id.val_NO2, R.id.label_NO2,
                            PollutionNorms.TABLE_REF_NO2, PollutionNorms.NORM_NO2);
                    break;
            }
        }

        /**
         * Action performed after clicking
         */
        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent plotIntent = new Intent(getContext(), PlotActivity.class);
                plotIntent.putExtra("station", currentStation);
                getContext().startActivities(new Intent[]{plotIntent});
            }
        });

        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }

    /**
     * Changes views for specific pollution
     */
    private void changePollutionView(View listItemView, double pollutionValue, int idValue, int idLabel, int[] tableRef, int norm) {
        // Views to be changed
        TextView valueView = listItemView.findViewById(idValue);
        TextView labelView = listItemView.findViewById(idLabel);
        // Rectangle for specific pollutionValue
        GradientDrawable valueRectangle = (GradientDrawable) valueView.getBackground();
        // Get the appropriate background color based on the current pollutionValue
        int ValueColor = getValueColor(pollutionValue, tableRef);
        valueRectangle.setColor(ValueColor);
        // Set text and label color
        valueView.setTextColor(ContextCompat.getColor(getContext(), getValueTextColor(pollutionValue)));
        labelView.setTextColor(ContextCompat.getColor(getContext(), getLabelTextColor(pollutionValue)));
        // Format data from current station
        String formattedMag = formatValue(pollutionValue, norm);
        // Insert formatted data into the view
        valueView.setText(formattedMag);
    }

    /**
     * Formats value
     */
    private String formatValue(double value, int norm) {
        if (value >= 0) {
            return String.valueOf((int) Math.round(100.0 * value / norm)) + "%";
        } else {
            return String.valueOf(R.string.no_data);
        }
    }

    /**
     * Gives text color for specific value
     */
    private int getValueTextColor(double value) {
        if (value >= 0) {
            return R.color.textColorValue;
        } else {
//         return R.color.textColorValueDefault;
            return R.color.colorValueNone;
        }
    }

    /**
     * Gives text color for specific label
     */
    private int getLabelTextColor(double value) {
        if (value >= 0) {
            return R.color.textColorValueDefault;
        } else {
            return R.color.colorValueNone;
        }
    }

    /**
     * Formats date
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String formatDate(String oldDateString) {

        // Old date format
        DateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date oldDate = null;
        try {
            oldDate = oldFormat.parse(oldDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // New date format
        DateFormat newFormat = new SimpleDateFormat("HH:mm");
        String newDateString = newFormat.format(oldDate);

        return newDateString;
    }

    /**
     * Gives value color for specific pollution
     *
     * @param value    given value
     * @param tableRef reference table for colors
     */
    private int getValueColor(double value, int[] tableRef) {
        int valueRound = (int) Math.round(value);
        if (valueRound > tableRef[4]) // very bad
            return ContextCompat.getColor(getContext(), R.color.colorValueVeryBad);
        else if (valueRound > tableRef[3]) // bad
            return ContextCompat.getColor(getContext(), R.color.colorValueBad);
        else if (valueRound > tableRef[2]) // sufficient
            return ContextCompat.getColor(getContext(), R.color.colorValueSufficient);
        else if (valueRound > tableRef[1]) // moderate
            return ContextCompat.getColor(getContext(), R.color.colorValueModerate);
        else if (valueRound > tableRef[0]) // good
            return ContextCompat.getColor(getContext(), R.color.colorValueGood);
        else if (valueRound >= 0) // very good
            return ContextCompat.getColor(getContext(), R.color.colorValueVeryGood);
        else // if there is a problem e.g. negative value
            return ContextCompat.getColor(getContext(), R.color.colorValueNone);
    }

    /**
     * Gives color for specific location's type
     */
    private int getLocationTypeColor(String locationType) {
        switch (locationType) {
            case DataRepository.IMPORTANT_CITIES:
                return R.color.colorImportantCities;
            case DataRepository.PODHALE:
                return R.color.colorPodhale;
            case DataRepository.BESKIDY_ZACH:
                return R.color.colorBeskidyZachodnie;
            case DataRepository.BESKIDY_WSCH:
                return R.color.colorBeskidyWschodnie;
            case DataRepository.SUDETY:
                return R.color.colorSudety;
            case DataRepository.JURA:
                return R.color.colorJura;
            default:
                return R.color.colorValueNone;
        }
    }

    /**
     * Gives text for specific location's type
     */
    private int getLocationTypeText(String locationType) {
        switch (locationType) {
            case DataRepository.IMPORTANT_CITIES:
                return R.string.stringImportantCities;
            case DataRepository.PODHALE:
                return R.string.stringPodhale;
            case DataRepository.BESKIDY_ZACH:
                return R.string.stringBeskidyZachodnie;
            case DataRepository.BESKIDY_WSCH:
                return R.string.stringBeskidyWschodnie;
            case DataRepository.SUDETY:
                return R.string.stringSudety;
            case DataRepository.JURA:
                return R.string.stringJura;
            default:
                return R.string.no_data;
        }
    }
}