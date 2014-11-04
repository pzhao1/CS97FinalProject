package edu.swarthmore.cs.moodtracker.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import edu.swarthmore.cs.moodtracker.R;

/**
 * Created by rliang on 11/4/14.
 */
public class SurveyArrayAdapter extends ArrayAdapter<String>{

    public interface RowSelectionListener {
        public void onRowSelection(int row);
    }

    private int mSelectedIndex;
    private RowSelectionListener mListener;

    public SurveyArrayAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public void setRowSelectionListener(RowSelectionListener listener) {
        mListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        view.findViewById(R.id.checkmark).setVisibility(position == mSelectedIndex ? View.VISIBLE : View.GONE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mListener != null) {
                    mListener.onRowSelection(position);
                }
            }
        });

        return view;
    }

    public void setSelection(final int selectedIndex) {
        mSelectedIndex = selectedIndex;
    }

}
