package edu.swarthmore.cs.moodtracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.swarthmore.cs.moodtracker.db.MoodRatingQuestion;
import edu.swarthmore.cs.moodtracker.db.SurveyEntry;
import edu.swarthmore.cs.moodtracker.db.TrackDatabase;


/**
 * Created by rliang on 11/4/14.
 */
public class TakeSurveyActivity extends FragmentActivity {

    public static final String MOOD_QUESTION = "mood_question";
    public static final String QUESTION_PAGE = "question_page";
    public static final String RESULTS = "results";

    public static final int MAX_PAGES = 10;

    private static final String CURRENT_PAGE = "current_page";
    private static final String NUM_PAGES = "num_pages";

    static final Map<Integer, String> PAGES_MAP;
    static {
        Map<Integer, String> tmp = new LinkedHashMap<Integer, String>();
        tmp.put(0, MoodRatingQuestion.MOOD_ACTIVE);
        tmp.put(1, MoodRatingQuestion.MOOD_DETERMINED);
        tmp.put(2, MoodRatingQuestion.MOOD_ATTENTIVE);
        tmp.put(3, MoodRatingQuestion.MOOD_INSPIRED);
        tmp.put(4, MoodRatingQuestion.MOOD_ALERT);
        tmp.put(5, MoodRatingQuestion.MOOD_AFRAID);
        tmp.put(6, MoodRatingQuestion.MOOD_NERVOUS);
        tmp.put(7, MoodRatingQuestion.MOOD_UPSET);
        tmp.put(8, MoodRatingQuestion.MOOD_HOSTILE);
        tmp.put(9, MoodRatingQuestion.MOOD_ASHAMED);

        PAGES_MAP = Collections.unmodifiableMap(tmp);
    }

    private int mNumPages = 1;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private HashMap<Integer, Integer> mResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_survey);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        //Bind the title indicator to the adapter
        CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.circles);
        indicator.setRadius(indicator.getRadius() * 1.5f);
        indicator.setAlpha(0.5f);
        indicator.setViewPager(mPager);

        if (savedInstanceState == null) {
            mResults = new HashMap<Integer, Integer>();
        } else {
            mResults = (HashMap<Integer, Integer> )savedInstanceState.getSerializable(RESULTS);
            mNumPages = savedInstanceState.getInt(NUM_PAGES);
            mPagerAdapter.notifyDataSetChanged();
            mPager.setCurrentItem(savedInstanceState.getInt(CURRENT_PAGE));
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(RESULTS, mResults);
        outState.putInt(CURRENT_PAGE, mPager.getCurrentItem());
        outState.putInt(NUM_PAGES, mNumPages);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_done) {
            Date date = new Date();

            ArrayList<MoodRatingQuestion> questions = new ArrayList<MoodRatingQuestion>(10);
            for (int pageNumber : mResults.keySet()) {
                MoodRatingQuestion question = new MoodRatingQuestion(TakeSurveyActivity.PAGES_MAP.get(pageNumber), mResults.get(pageNumber));
                questions.add(question);
            }

            SurveyEntry entry = new SurveyEntry(date, questions);
            TrackDatabase db = TrackDatabase.getInstance(this);
            db.writeSurveyEntry(entry);

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
        invalidateOptionsMenu();
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
