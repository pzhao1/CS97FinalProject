package edu.swarthmore.cs.moodtracker.db;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Collection;

/**
 * Async task that save app usage information to database.
 * Usage: new WriteAppusageTask(context, entries) {
 *            override onFinish()
 *        }.execute()
 */
public abstract class SaveAppUsageTask extends AsyncTask<Integer, Integer, Boolean > {
    private TrackDatabase mDatabase;
    private Collection<AppUsageEntry> mEntries;

    /**
     * Construct a SaveAppUsage task that writes app usage to database.
     * @param context Used to get database instance.
     * @param entries App usage entries to save.
     */
    public SaveAppUsageTask(Context context, Collection<AppUsageEntry> entries) {
        mDatabase = TrackDatabase.getInstance(context);
        mEntries = entries;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        for (AppUsageEntry entry : mEntries) {
            mDatabase.writeAppUsage(entry);
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected void onPostExecute(Boolean result) {
        onFinish();
    }

    /**
     * Override this method to get result of query.
     */
    public abstract void onFinish();
}
