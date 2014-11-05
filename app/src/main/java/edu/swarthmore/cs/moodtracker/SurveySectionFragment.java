package edu.swarthmore.cs.moodtracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by rliang on 11/4/14.
 */
public class SurveySectionFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    public SurveySectionFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_survey, container, false);

        return rootView;
    }
}
