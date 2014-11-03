package edu.swarthmore.cs.moodtracker;

/**
 * Created by Peng on 10/19/2014.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import edu.swarthmore.cs.moodtracker.util.AppUsageEntry;
import edu.swarthmore.cs.moodtracker.util.AppUsageListAdapter;

/**
 * Created by Peng on 10/19/2014.
 * The fragment that shows App Usage tracking information.
 */
public class AppUsageSectionFragment extends Fragment {

    public static final String TAG = "AppUsageSectionFragment";

    private ListView mAppUsageListView = null;
    private View mWaitingView = null;
    private TrackService mService = null;

    /**
     * Default constructor.
     */
    public AppUsageSectionFragment() {
        super();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_app_usage, container, false);

        mAppUsageListView = (ListView) rootView.findViewById(R.id.list_app_usage);
        mWaitingView = rootView.findViewById(R.id.waiting_view);
        syncLayoutWithService();

        // Before we get connected, just display a waiting spinner. See fragment_section_app_usage.xml
        return rootView;
    }

    /**
     * When MainActivity connects to the service, call this function to deliver service instance.
     */
    public void setService(TrackService service) {
        mService = service;
        syncLayoutWithService();
    }

    /**
     * When MainActivity disconnects to the service, call this function to update status.
     */
    public void unSetService() {
        mService = null;
        syncLayoutWithService();
    }

    /**
     * Update layout based on the current service status.
     * If we are connected to service, hide the waiting section and display usage list.
     * If we are disconnected to service, hide the usage list and display waiting section.
     */
    private void syncLayoutWithService() {
        if (mWaitingView == null || mAppUsageListView == null)
            return;

        if (mService != null) {
            mWaitingView.setVisibility(View.GONE);
            mAppUsageListView.setVisibility(View.VISIBLE);
            updateAppUsageList();
        }
        else {
            mWaitingView.setVisibility(View.VISIBLE);
            mAppUsageListView.setVisibility(View.GONE);
        }
    }

    /**
     * Update the usage times in app usage list. Requires mService != null.
     */
    private void updateAppUsageList() {
        List<AppUsageEntry> appUsageEntries = mService.getCurrentAppUsageInfo();
        AppUsageListAdapter adapter = (AppUsageListAdapter) mAppUsageListView.getAdapter();
        if (adapter != null) {
            adapter.clear();
            adapter.addAll(appUsageEntries);
        }
        else {
            adapter = new AppUsageListAdapter(
                    getActivity(), R.layout.list_item_app_usage, appUsageEntries);
            mAppUsageListView.setAdapter(adapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // If we are already connected to the service, update the list.
        if (mService != null)
            updateAppUsageList();
    }
}
