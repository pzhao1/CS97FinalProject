package edu.swarthmore.cs.moodtracker.db;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import edu.swarthmore.cs.moodtracker.services.TrackService;
import edu.swarthmore.cs.moodtracker.util.TrackDateUtil;


/**
 * Async task that retrieves app usage information from database.
 * Usage: new ReadAppusageTask(context [,service]) {
 *            override onFinish(result)
 *        }.execute(startDate, endDate[, displayLimit])
 * Pass in startDate = -1 to read from beginning of time.
 * Pass in endDate = -1 to read until end of time.
 */
public abstract class QuerySentimentTask extends AsyncTask<Long, Integer, List<AppUsageEntry> > {
    private TrackDatabase mDatabase;
    private TrackService mService;
    private long mCurrentDate = TrackDateUtil.getDaysSinceEpoch();

    /**
     * Construct a ReadAppUsageTask that read app usages from database.
     * @param context Used to get database instance.
     */
    public QuerySentimentTask(Context context) {
        mDatabase = TrackDatabase.getInstance(context);
        mService = null;
    }


    @Override
    protected List<AppUsageEntry> doInBackground(Long... params) {
        if (params.length < 2 || params.length > 3)
            return null;

        // Parse parameters
        long startDate = params[0], endDate = params[1];
        int displayLimit = (params.length > 2 && params[2] != null) ? params[2].intValue() : -1;

        // Query db and service to get app usage.
        ArrayList<AppUsageEntry> dbResult = mDatabase.readAppUsage(startDate, endDate);
        List<AppUsageEntry> serviceResult = (mService != null) ? mService.getTodayAppUsage() : null;

        // If service doesn't have any today's app usage yet, get today's app usage from DB.
        // This could happen when service itself reads from DB.
        boolean getTodayUsageFromDB = (serviceResult == null);

        // Sum usage time for every app.
        HashMap<String, AppUsageEntry> resultMap = new HashMap<String, AppUsageEntry>();

        for (AppUsageEntry entry:dbResult){
            // If we want to get today's usage from service, ignore today's entries.
            if (!getTodayUsageFromDB && entry.DaysSinceEpoch >= mCurrentDate)
                continue;

            if (resultMap.containsKey(entry.PackageName))
                resultMap.get(entry.PackageName).UsageTimeSec += entry.UsageTimeSec;
            else
                resultMap.put(entry.PackageName, entry);
        }

        if (serviceResult != null) {
            for (AppUsageEntry entry:serviceResult) {
                if (resultMap.containsKey(entry.PackageName))
                    resultMap.get(entry.PackageName).UsageTimeSec += entry.UsageTimeSec;
                else
                    resultMap.put(entry.PackageName, entry);
            }
        }

        // Get a list and return it.
        List<AppUsageEntry> resultList = new ArrayList<AppUsageEntry>(resultMap.values());
        Collections.sort(resultList, new AppUsageEntryComparator());
        if (displayLimit >= 0 && displayLimit < resultList.size()) {
            resultList = resultList.subList(0, displayLimit);
        }
        return resultList;
    }

    @Override
    protected void onPostExecute(List<AppUsageEntry> result) {
        onFinish(result);
    }

    /**
     * Override this method to get result of query.
     * @param result A list of sorted AppUsageEntries. DisplayLimit applied.
     */
    public abstract void onFinish(List<AppUsageEntry> result);

    /**
     * Comparator for two AppUsageEntries. Used in sorting the list.
     */
    private class AppUsageEntryComparator implements Comparator<AppUsageEntry> {
        @Override
        public int compare(AppUsageEntry entry1, AppUsageEntry entry2)
        {
            return  (entry2.UsageTimeSec - entry1.UsageTimeSec);
        }
    }

}
