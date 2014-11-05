package edu.swarthmore.cs.moodtracker;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SubmitSurveyFragment extends Fragment {
    public static SubmitSurveyFragment newInstance(String param1, String param2) {
        SubmitSurveyFragment fragment = new SubmitSurveyFragment();
        return fragment;
    }

    public SubmitSurveyFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_submit_survey, container, false);
        return rootView;
    }


}
