package edu.swarthmore.cs.moodtracker;

/**
 * Created by Peng on 10/19/2014.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.List;

import edu.swarthmore.cs.moodtracker.db.AppUsageEntry;
import edu.swarthmore.cs.moodtracker.db.ReadAppUsageTask;
import edu.swarthmore.cs.moodtracker.services.TrackService;
import edu.swarthmore.cs.moodtracker.util.AppUsageListAdapter;
import edu.swarthmore.cs.moodtracker.util.TrackDateUtil;

/**
 * Created by Peng on 10/19/2014.
 * The fragment that shows App Usage tracking information.
 */
public class AppUsageSectionFragment extends Fragment {
    public static final String TAG = "AppUsageSectionFragment";
    public static final String STATE_DATE_RANGE_SELECTION = "date_range_selection";
    public static final String STATE_DISPLAY_LIMIT_SELECTION = "display_limit_selection";

    // Initialization Variables. See allInitialized() for more details.
    private boolean mResumeInitialized = false;
    private boolean mDateRangeInitialized = false;
    private boolean mDisplayLimitInitialized = false;

    // Filter variables.
    private int mDateRange = 0;
    private int mDisplayLimit = -1;

    // Other saved variables.
    private View mWaitingView = null;
    private ListView mAppUsageListView = null;
    private Spinner mDateRangeSpinner = null;
    private Spinner mDisplayLimitSpinner = null;
    private TrackService mService = null;


    /*-----------------------*/
    /*        Methods        */
    /*-----------------------*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_section_app_usage, container, false);
        mWaitingView = rootView.findViewById(R.id.app_usage_waiting_view);
        mAppUsageListView = (ListView) rootView.findViewById(R.id.app_usage_list);
        mDateRangeSpinner = (Spinner) rootView.findViewById(R.id.app_usage_date_range_spinner);
        mDisplayLimitSpinner = (Spinner) rootView.findViewById(R.id.app_usage_display_limit_spinner);

        syncLayoutWithData(false);
        setupFilter();

        // Before we get connected, just display a waiting spinner. See fragment_section_app_usage.xml
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mResumeInitialized = true;
        if (allInitialized())
            tryUpdateAppUsageList();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_DATE_RANGE_SELECTION, mDateRangeSpinner.getSelectedItemPosition());
        outState.putInt(STATE_DISPLAY_LIMIT_SELECTION, mDisplayLimitSpinner.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }


    /**
     * When MainActivity connects to the service, call this function to deliver service instance.
     */
    public void setService(TrackService service) {
        mService = service;
        tryUpdateAppUsageList();
    }

    /**
     * When MainActivity disconnects to the service, call this function to update status.
     */
    public void unsetService() {
        mService = null;
        syncLayoutWithData(false);
    }

    /**
     * Update layout based on the current service status.
     * If we have data, hide the waiting section and display usage list.
     * If we don't have data, hide the usage list and display waiting section.
     */
    private void syncLayoutWithData(boolean hasData) {
        if (hasData) {
            mWaitingView.setVisibility(View.GONE);
            mAppUsageListView.setVisibility(View.VISIBLE);
        }
        else {
            mWaitingView.setVisibility(View.VISIBLE);
            mAppUsageListView.setVisibility(View.GONE);
        }
    }

    /**
     * Update the usage times in app usage list.
     */
    private void tryUpdateAppUsageList() {
        if (!allInitialized())
            return;

        syncLayoutWithData(false);
        // Read app usage from database (asynchronously) and use result to update usage list.
        long currentDate = TrackDateUtil.getDaysSinceEpoch();
        new ReadAppUsageTask(getActivity(), mService) {
            @Override
            public void onFinish(List<AppUsageEntry> result) {
                if (result != null) {
                    AppUsageListAdapter adapter = (AppUsageListAdapter) mAppUsageListView.getAdapter();
                    if (adapter != null) {
                        adapter.clear();
                        adapter.addAll(result);
                    } else {
                        adapter = new AppUsageListAdapter(
                                getActivity(), R.layout.list_item_app_usage, result);
                        mAppUsageListView.setAdapter(adapter);
                    }
                    syncLayoutWithData(true);
                }
            }
        }.execute(currentDate - mDateRange, currentDate, (long)mDisplayLimit);
    }

    /**
     * Setup the Spinners in filter section.
     */
    private void setupFilter() {
        // Initialize the date range spinner
        ArrayAdapter<CharSequence> dateRangeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.app_usage_date_range_array, R.layout.list_item_spinner_filter);

        dateRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mDateRangeSpinner.setAdapter(dateRangeAdapter);
        mDateRangeSpinner.setOnItemSelectedListener(new DateRangeSpinnerListener());

        // Initialize the display limit spinner
        ArrayAdapter<CharSequence> displayLimitAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.app_usage_display_limit_array,  R.layout.list_item_spinner_filter);

        displayLimitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mDisplayLimitSpinner.setAdapter(displayLimitAdapter);
        mDisplayLimitSpinner.setOnItemSelectedListener(new DisplayLimitSpinnerListener());

        // Select spinners to default values.
        mDateRangeSpinner.setSelection(0);
        mDisplayLimitSpinner.setSelection(4);
    }


    /**
     * We want to call updateUsageList() when the following things happen:
     *   - onResume(), onCreateView(), setService()
     *   - a new value is selected for mDateRangeSpinner and mDisplayLimitSpinner
     * However, some of them might throw errors if others are not initialized. Moreover, some of them
     * can overlap when the fragment is first created, and we call updateUsageList() 3-4 times, which
     * is really slow and inefficient. This function helps by checking everything has been initialized,
     * and we only update list in updateUsageList() if it returns true.
     * @return true if all necessary components have been initialized. false otherwise.
     */
    private boolean allInitialized() {
        return (mDateRangeInitialized && mDisplayLimitInitialized && mResumeInitialized &&
                mService != null && mAppUsageListView != null);
    }

    /*-----------------------*/
    /*     Inner Classes     */
    /*-----------------------*/

    /**
     * Selector for date range spinner.
     */
    private class DateRangeSpinnerListener implements AdapterView.OnItemSelectedListener {
        private final int[] mPositionToPrevDaysArray = {0, 1, 2, 7, 14, 30, 60, -1};

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mDateRangeInitialized = true;
            mDateRange = mPositionToPrevDaysArray[pos];
            tryUpdateAppUsageList();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    }

    /**
     * Selector for display limit spinner.
     */
    private class DisplayLimitSpinnerListener implements AdapterView.OnItemSelectedListener {
        private final int[] mPositionToLimitArray = {5, 10, 20, 30, -1};

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mDisplayLimitInitialized = true;
            mDisplayLimit = mPositionToLimitArray[pos];
            tryUpdateAppUsageList();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    }
}
