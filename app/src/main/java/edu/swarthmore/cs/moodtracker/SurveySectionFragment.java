package edu.swarthmore.cs.moodtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;

import java.util.List;

import edu.swarthmore.cs.moodtracker.db.SurveyEntry;
import edu.swarthmore.cs.moodtracker.db.TrackDatabase;
import edu.swarthmore.cs.moodtracker.util.SurveyEntryAdapter;

/**
 * Created by rliang on 11/4/14.
 *
 */
public class SurveySectionFragment extends Fragment {
    private TextView mTextView;
    private TrackDatabase mDatabase;
    private SurveyEntryAdapter mAdapter;
    private ListView mListView;

    public SurveySectionFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_survey, container, false);
        mTextView = (TextView) rootView.findViewById(R.id.take_survey_button);
        mDatabase = TrackDatabase.getInstance(getActivity());
        mListView = (ListView) rootView.findViewById(android.R.id.list);

        int footerHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.take_survey_button);
        QuickReturnListViewOnScrollListener onScrollListener = new QuickReturnListViewOnScrollListener(QuickReturnType.FOOTER, null, 0, mTextView, footerHeight);
        mListView.setOnScrollListener(onScrollListener);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent surveyIntent = new Intent(getActivity(), TakeSurveyActivity.class);
                startActivity(surveyIntent);
                getActivity().overridePendingTransition(R.anim.slide_enter, R.anim.pop_exit);
            }
        });

        List<SurveyEntry> list = mDatabase.readSurveyInfo();
        mAdapter = new SurveyEntryAdapter(getActivity(), R.layout.list_item_survey_entry, android.R.id.text1, list);
        mListView.setAdapter(mAdapter);
    }
}
