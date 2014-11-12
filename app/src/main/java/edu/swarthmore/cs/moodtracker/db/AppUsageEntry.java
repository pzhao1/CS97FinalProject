package edu.swarthmore.cs.moodtracker.db;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by Peng on 10/19/2014.
 * Stores the information of an AppUsage table entry in the Track database.
 */
public class AppUsageEntry {
    public static final String TAG = "AppUsageEntry";
    public static final String JSON_PACKAGE_NAME = "package_name";
    public static final String JSON_APP_NAME = "app_name";
    public static final String JSON_APP_ICON = "app_icon";
    public static final String JSON_USAGE = "usage_time_sec";
    public static final String JSON_DATE = "days_since_epoch";

    public String PackageName = null;
    public String AppName = null;
    public Bitmap AppIcon = null;
    public int UsageTimeSec = -1;
    public long DaysSinceEpoch = -1;


    /**
     * Construct an empty AppUsageEntry
     */
    public AppUsageEntry(String pkgName, String appName, Bitmap appIcon, int usage, long date) {
        this.PackageName = pkgName;
        this.AppName = appName;
        this.AppIcon = appIcon;
        this.UsageTimeSec = usage;
        this.DaysSinceEpoch = date;
    }

    /**
     * Construct an AppUsageEntry instance from a database cursor. Information is read from
     * the current rpw of the cursor, or the default values are kept if cursor is at the end.
     * Position of the cursor is incremented by 1 after construction.
     * @param cursor A Cursor returned from database query.
     */
    public AppUsageEntry(Cursor cursor){
        // Keep default values if cursor is already finished reading.
        if (cursor.isAfterLast()) {
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

        // Get the App Icon column
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
     * Converts class to a JSON object. Used for storage.
     * @return The JSON representation of this class.
     */
    public JSONObject toJSON() {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put(JSON_PACKAGE_NAME, PackageName);
            jsonObj.put(JSON_APP_NAME, AppName);

            byte[] bytes = getIconInByteArray();
            String iconEncodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
            jsonObj.put(JSON_APP_ICON, iconEncodedString);

            jsonObj.put(JSON_USAGE, UsageTimeSec);
            jsonObj.put(JSON_DATE, DaysSinceEpoch);
            return jsonObj;
        }
        catch (JSONException e) {
            return null;
        }
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
