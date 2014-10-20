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
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private TrackService mService = null;

    /**
     * Default constructor.
     */
    public AppUsageSectionFragment() {
        super();
    }

    /**
     * Set the service this fragment uses to retrieve tracking information.
     * @param service A TrackService instance
     */
    public void setService(TrackService service) {
        mService = service;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_app_usage, container, false);

        List<AppUsageEntry> appUsageEntries = mService.getCurrentAppUsageInfo();

        ListView appUsageList = (ListView) rootView.findViewById(R.id.list_app_usage);
        AppUsageListAdapter adapter = new AppUsageListAdapter(
                getActivity(), R.layout.list_item_app_usage, appUsageEntries);
        appUsageList.setAdapter(adapter);

        return rootView;
    }
}
