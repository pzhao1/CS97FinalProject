package edu.swarthmore.cs.moodtracker.util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import edu.swarthmore.cs.moodtracker.R;
import edu.swarthmore.cs.moodtracker.db.AppUsageEntry;

/**
 * Created by Peng on 10/19/2014.
 * Custom adapter that populates an app usage ListView from a list of app usage entries.
 */
public class AppUsageListAdapter extends ArrayAdapter<AppUsageEntry> {;

    // Context used to create BitmapDrawable from Bitmap.
    private Context mContext = null;

    public AppUsageListAdapter(Context context, int resource, List<AppUsageEntry> entries) {
        super(context, resource, entries);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Date d = new Date();
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
            ImageView iconImage = (ImageView) v.findViewById(R.id.list_field_app_icon);

            appNameText.setText(entry.AppName);
            timeText.setText(secToHourMinuteSecond(entry.UsageTimeSec));
            iconImage.setImageDrawable(new BitmapDrawable(mContext.getResources(), entry.AppIcon));
        }
        return v;
    }

    private String secToHourMinuteSecond(int numSeconds) {
        int seconds = (numSeconds % 60);
        int minutes = ((numSeconds / 60) % 60);
        int hours = (numSeconds / 3600);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


}
