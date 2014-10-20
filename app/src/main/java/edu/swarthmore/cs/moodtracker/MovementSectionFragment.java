package edu.swarthmore.cs.moodtracker;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Peng on 10/19/2014.
 * The fragment that shows Movement tracking information.
 */
public class MovementSectionFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    public MovementSectionFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_movement, container, false);

        TextView labelTextView = (TextView) rootView.findViewById(R.id.section_label);
        labelTextView.setText("This is the Movement tracking section");

        return rootView;
    }
}