package edu.swarthmore.cs.moodtracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.swarthmore.cs.moodtracker.util.MoodQuestion;


/**
 * Created by rliang on 11/4/14.
 */
public class TakeSurveyActivity extends FragmentActivity {

    public static final String MOOD_QUESTION = "mood_question";
    public static final String QUESTION_PAGE = "question_page";
    public static final String RESULTS = "results";

    public static final int MAX_PAGES = 10;

    static final Map<Integer, String> PAGES_MAP;
    static {
        Map<Integer, String> tmp = new LinkedHashMap<Integer, String>();
        tmp.put(0, MoodQuestion.MOOD_ACTIVE);
        tmp.put(1, MoodQuestion.MOOD_DETERMINED);
        tmp.put(2, MoodQuestion.MOOD_ATTENTIVE);
        tmp.put(3, MoodQuestion.MOOD_INSPIRED);
        tmp.put(4, MoodQuestion.MOOD_ALERT);
        tmp.put(5, MoodQuestion.MOOD_AFRAID);
        tmp.put(6, MoodQuestion.MOOD_NERVOUS);
        tmp.put(7, MoodQuestion.MOOD_UPSET);
        tmp.put(8, MoodQuestion.MOOD_HOSTILE);
        tmp.put(9, MoodQuestion.MOOD_ASHAMED);

        PAGES_MAP = Collections.unmodifiableMap(tmp);
    }

    private static int mNumPages = 1;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private HashMap<Integer, Integer> mResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_survey);

        // Set title bar
        setTitle(R.string.survey_questionnaire);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mResults = new HashMap<Integer, Integer>();

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putString(MOOD_QUESTION, PAGES_MAP.get(position));
            args.putInt(QUESTION_PAGE, position);
            args.putSerializable(RESULTS, mResults);

            SurveyQuestionFragment sqf = new SurveyQuestionFragment();
            sqf.setArguments(args);
            sqf.setRetainInstance(true);
            return sqf;
        }

        @Override
        public int getCount() {
            return mNumPages;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pop_enter, R.anim.slide_exit);
    }

    public void selectedRating(HashMap<Integer, Integer> results) {
        mResults = results;

        int nextPage = mPager.getCurrentItem() + 1;
        if (nextPage == MAX_PAGES) {
          return;
        } else if (nextPage == mNumPages) {
            mNumPages++;
        }

        mPagerAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(nextPage, true);
    }
}
