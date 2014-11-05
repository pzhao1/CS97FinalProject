package edu.swarthmore.cs.moodtracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import edu.swarthmore.cs.moodtracker.util.SurveyArrayAdapter;

/**
 * Created by Peng on 10/19/2014.
 * The fragment that shows Movement tracking information.
 */
public class SurveyQuestionFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    private SurveyArrayAdapter mAdapter;
    private ListView mListView;
    private TextView mHeader;
    private TextView mQuestionSubheader;
    private String mQuestion;
    private int mPageNumber;

    private static final String[] QUESTION_TEXT = {
            "1    (Very Slightly or Not at All)" ,
            "2    (A Little)",
            "3    (Moderately)",
            "4    (Quite a Bit)",
            "5    (Extremely)"

    };
    private SurveyArrayAdapter.RowSelectionListener mSelectionListener = new SurveyArrayAdapter.RowSelectionListener() {
        @Override
        public void onRowSelection(final int row) {
            if (mAdapter != null) {
                mAdapter.setSelection(row);
                mAdapter.notifyDataSetChanged();
                notifyParentOfSelection();
            }
        }
    };

    public SurveyQuestionFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQuestion = getArguments().getString(TakeSurveyActivity.MOOD_QUESTION);
        mPageNumber = getArguments().getInt(TakeSurveyActivity.QUESTION_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_survey_question, container, false);

        mHeader = (TextView) rootView.findViewById(R.id.section_header);
        mQuestionSubheader = (TextView) rootView.findViewById(R.id.section_question);

        mListView = (ListView) rootView.findViewById(android.R.id.list);

        setHasOptionsMenu(false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHeader.setText("Question " + new Integer(mPageNumber + 1));

        StringBuilder fullQuestion = new StringBuilder("To what extent are you feeling right now?");
        fullQuestion.insert(fullQuestion.indexOf("g") + 1, " " + mQuestion);
        mQuestionSubheader.setText(fullQuestion);

        if (mAdapter == null) {
            List<String> options = Arrays.asList(QUESTION_TEXT);
            mAdapter = new SurveyArrayAdapter(getActivity(), R.layout.list_item_survey, android.R.id.text1, options);
            mAdapter.setRowSelectionListener(mSelectionListener);
        }

        mListView.setAdapter(mAdapter);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //Placeholder
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                float originOffset = mHeader.getY();

                int pos = mListView.getFirstVisiblePosition();
                View firstItem = mListView.getChildAt(0);
                if (firstItem != null) {

                    float scrollY = firstItem.getY();
                    float alpha = -1 * (originOffset - scrollY) / (mListView.getPaddingTop() - originOffset);
                    mHeader.setAlpha(alpha);
                    mQuestionSubheader.setAlpha(alpha);
                }
            }
        });
    }

    public void notifyParentOfSelection() {
        ((TakeSurveyActivity) getActivity()).selectedRating();
    }
}