package edu.swarthmore.cs.moodtracker;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.swarthmore.cs.moodtracker.R;

/**
 * Created by Peng on 10/19/2014.
 */
public class TextSectionFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    public TextSectionFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_text, container, false);

        TextView labelTextView = (TextView) rootView.findViewById(R.id.section_label);
        labelTextView.setText("This is the Text tracking section");

        return rootView;
    }
}
