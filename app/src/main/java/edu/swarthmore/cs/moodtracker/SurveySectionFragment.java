package edu.swarthmore.cs.moodtracker;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import edu.swarthmore.cs.moodtracker.util.SurveyArrayAdapter;

/**
 * Created by Peng on 10/19/2014.
 * The fragment that shows Movement tracking information.
 */
public class SurveySectionFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    private SurveyArrayAdapter mAdapter;
    private ListView mListView;
    private TextView mHeader;
    private TextView mQuestionSubheader;

    private static final String[] QUESTION_TEXT = {
            "1 (Very Slightly or Not at All)" ,
            "2 (A Little)",
            "3 (Moderately)",
            "4 (Quite a Bit)",
            "5 (Extremely)"

    };
    private SurveyArrayAdapter.RowSelectionListener mSelectionListener = new SurveyArrayAdapter.RowSelectionListener() {
        @Override
        public void onRowSelection(final int row) {
            if (mAdapter != null) {
                mAdapter.setSelection(row);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    public SurveySectionFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_survey, container, false);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mHeader = (TextView) rootView.findViewById(R.id.section_header);
        mQuestionSubheader = (TextView) rootView.findViewById(R.id.section_question);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHeader.setText("Example");
        mQuestionSubheader.setText("Hello?");

        if (mAdapter == null) {
            List<String> options = Arrays.asList(QUESTION_TEXT);
            mAdapter = new SurveyArrayAdapter(getActivity(), R.layout.list_item_survey, android.R.id.text1, options);
            mAdapter.setRowSelectionListener(mSelectionListener);
        }

        mListView.setAdapter(mAdapter);
    }


}