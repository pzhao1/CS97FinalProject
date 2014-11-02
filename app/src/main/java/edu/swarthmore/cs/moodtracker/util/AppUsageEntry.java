package edu.swarthmore.cs.moodtracker.util;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by Peng on 10/19/2014.
 * Stores the information of an AppUsage table entry in the Track database.
 */
public class AppUsageEntry {
    public static final String TAG = "AppUsageEntry";

    public String PackageName = "";
    public int UsageTimeSec = 0;
    public String AppName = "";
    public Bitmap AppIcon = null;
    public long DaysSinceEpoch = 0;

    /**
     * Construct an AppUsageEntry using given information.
     * @param packageName Package name of the app.
     * @param appName Name of the app.
     * @param usageTimeSec Usage time of the app in seconds.
     */
    public AppUsageEntry(String packageName, String appName, Bitmap icon, int usageTimeSec, long daysSinceEpoch){
        this.PackageName = packageName;
        this.AppName = appName;
        this.UsageTimeSec = usageTimeSec;
        this.AppIcon = icon;
        this.DaysSinceEpoch = daysSinceEpoch;
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
        int index = cursor.getColumnIndex(TrackContract.AppUsageSchema.COLUMN_PACKAGE);
        if (index >= 0)
            this.PackageName = cursor.getString(index);
        else
            Log.e(TAG, "AppUsageEntry(Cursor): COLUMN_PACKAGE not found");

        // Get the App Name column
        index = cursor.getColumnIndex(TrackContract.AppInfoSchema.COLUMN_APP_NAME);
        if (index >= 0)
            this.AppName = cursor.getString(index);
        else
            Log.e(TAG, "AppUsageEntry(Cursor): COLUMN_APP_NAME not found");

        // Get the App Name column
        index = cursor.getColumnIndex(TrackContract.AppInfoSchema.COLUMN_APP_ICON);
        if (index >= 0)
            this.populateIconFromByteArray(cursor.getBlob(index));
        else
            Log.e(TAG, "AppUsageEntry(Cursor): COLUMN_APP_ICON not found");

        // Get the Usage Time column
        index = cursor.getColumnIndex(TrackContract.AppUsageSchema.COLUMN_USAGE_SEC);
        if (index >= 0)
            this.UsageTimeSec = cursor.getInt(index);
        else
            Log.e(TAG, "AppUsageEntry(Cursor): COLUMN_USAGE_SEC not found");

        // Get the Date column
        index = cursor.getColumnIndex(TrackContract.AppUsageSchema.COLUMN_DATE);
        if (index >= 0)
            this.DaysSinceEpoch = cursor.getLong(index);
        else
            Log.e(TAG, "AppUsageEntry(Cursor): COLUMN_DATE not found");

        cursor.moveToNext();
    }

    /**
     * Converts the icon of this app usage entry to a byte array. Used in database storage.
     * @return The encoded byte array, using PNG format (no information loss).
     */
    public byte[] getIconInByteArray() {
        if (this.AppIcon == null)
            return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.AppIcon.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Populate the app icon of this entry using an encoded byte array.
     * @param byteArray The encoded byte array to be converted to BitMap.
     */
    public void populateIconFromByteArray(byte[] byteArray) {
        if (byteArray == null)
            return;
        this.AppIcon = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}
