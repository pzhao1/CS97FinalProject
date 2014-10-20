package edu.swarthmore.cs.moodtracker.util;

import android.database.Cursor;

/**
 * Created by Peng on 10/19/2014.
 * Stores the information of an AppUsage table entry in the Track database.
 */
public class AppUsageEntry {
    public String PackageName = "";
    public int UsageTimeSec = 0;
    public String AppName = "";

    /**
     * Empty Constructor.
     */
    public AppUsageEntry(){
    }

    /**
     * Construct an AppUsageEntry using given information.
     * @param packageName Package name of the app.
     * @param appName Name of the app.
     * @param usageTimeSec Usage time of the app in seconds.
     */
    public AppUsageEntry(String packageName, String appName,int usageTimeSec){
        this.PackageName = packageName;
        this.AppName = appName;
        this.UsageTimeSec = usageTimeSec;
    }

    /**
     * Construct an AppUsageEntry instance from a database cursor. Information is read from
     * the current rpw of the cursor, or the default values are kept if cursor is at the end.
     * Position of the cursor is incremented by 1 after construction.
     * @param cursor A Cursor returned from database query.
     */
    public AppUsageEntry(Cursor cursor){
        // Keep default values if cursor is already finished reading.
        if (cursor.isAfterLast()){
            return;
        }

        // Get the Package Name column
        int index = cursor.getColumnIndex(TrackDatabaseContract.AppUsageSchema.COLUMN_NAME_PACKAGE);
        if (index >= 0) {
            this.PackageName = cursor.getString(index);
        }

        // Get the App Name column
        index = cursor.getColumnIndex(TrackDatabaseContract.AppUsageSchema.COLUMN_NAME_APP);
        if (index >= 0) {
            this.AppName = cursor.getString(index);
        }

        // Get the Usage Time column
        index = cursor.getColumnIndex(TrackDatabaseContract.AppUsageSchema.COLUMN_NAME_TIME);
        if (index >= 0) {
            this.UsageTimeSec = cursor.getInt(index);
        }

        cursor.moveToNext();
    }
}
