package edu.swarthmore.cs.moodtracker;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SurveyResultsFragment extends Fragment {
    public static SurveyResultsFragment newInstance(String param1, String param2) {
        SurveyResultsFragment fragment = new SurveyResultsFragment();
        return fragment;
    }

    public SurveyResultsFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_submit_survey, container, false);
        return rootView;
    }


}
