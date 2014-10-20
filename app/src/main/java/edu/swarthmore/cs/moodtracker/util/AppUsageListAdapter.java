package edu.swarthmore.cs.moodtracker.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import edu.swarthmore.cs.moodtracker.R;

/**
 * Created by Peng on 10/19/2014.
 */
public class AppUsageListAdapter extends ArrayAdapter<AppUsageEntry> {;

    public AppUsageListAdapter(Context context, int resource, List<AppUsageEntry> entries) {
        super(context, resource, entries);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_app_usage, null);
        }

        AppUsageEntry entry = getItem(position);
        if (entry != null) {

            TextView appNameText = (TextView) v.findViewById(R.id.list_field_app_name);
            TextView timeText = (TextView) v.findViewById(R.id.list_field_time);

            if (appNameText != null) {
                appNameText.setText(entry.AppName);
            }

            if (timeText != null) {
                timeText.setText(String.valueOf(entry.UsageTimeSec));
            }
        }
        return v;
    }
}
