package edu.swarthmore.cs.moodtracker.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import edu.swarthmore.cs.moodtracker.R;
import edu.swarthmore.cs.moodtracker.db.SurveyEntry;

/**
 * Created by rliang on 11/19/14.
 */
public class SurveyEntryAdapter extends ArrayAdapter<SurveyEntry> {

    private SimpleDateFormat mDateFormat;

    public SurveyEntryAdapter(Context context, int resource, int textViewResourceId, List<SurveyEntry> objects) {
        super(context, resource, textViewResourceId, objects);

        mDateFormat = new SimpleDateFormat("MMM. dd, yyyy hh:mm");
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.list_item_survey_entry, null);
        }

        SurveyEntry entry = getItem(position);
        if (entry != null) {
            TextView numberField = (TextView) view.findViewById(android.R.id.text1);
            numberField.setText("Survey Number " + (getCount() - position));

            TextView dateField = (TextView) view.findViewById(android.R.id.text2);
            dateField.setText(mDateFormat.format(entry.getDate()));
        }

        return view;
    }


}
