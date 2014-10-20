package edu.swarthmore.cs.moodtracker.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Peng on 10/19/2014.
 * Local SQLite Database that stores all tracking information.
 * Provides permanent storage, i.e. information is not lost when activity and service stop.
 */
public class TrackDatabase extends SQLiteOpenHelper {
    private static final String TAG = "TrackDatabase";

    // Private factory instance.
    private static TrackDatabase sInstance = null;

    /**
     * Static factory method to create a TrackDatabase instance or retrieve the existing instance
     * @param context The context of the activity creating the database.
     * @return The TrackDatabase instance
     */
    public static TrackDatabase getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new TrackDatabase(context.getApplicationContext());
        }

        return sInstance;
    }

    /**
     * Private constructor for TrackDatabase, used by static getInstance() method.
     * @param context The application context this database lives in
     */
    private TrackDatabase(Context context) {
        super(context, TrackDatabaseContract.DATABASE_NAME, null, TrackDatabaseContract.DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the AppUsage Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TrackDatabaseContract.AppUsageSchema.TABLE_NAME + "("
                + TrackDatabaseContract.AppUsageSchema.COLUMN_NAME_PACKAGE + " Text PRIMARY KEY, "
                + TrackDatabaseContract.AppUsageSchema.COLUMN_NAME_APP + " TEXT, "
                + TrackDatabaseContract.AppUsageSchema.COLUMN_NAME_TIME + " INTEGER"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TrackDatabaseContract.AppUsageSchema.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    /**
     * Write an AppUsage entry into the database, overwriting any existing entries.
     * @param entry The AppUsage entry that we write into the database.
     */
    public void writeAppUsage(AppUsageEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        String tableName = TrackDatabaseContract.AppUsageSchema.TABLE_NAME;
        String packageColumn = TrackDatabaseContract.AppUsageSchema.COLUMN_NAME_PACKAGE;
        String appColumn = TrackDatabaseContract.AppUsageSchema.COLUMN_NAME_APP;
        String timeColumn = TrackDatabaseContract.AppUsageSchema.COLUMN_NAME_TIME;

        String sqlCommand = "INSERT OR REPLACE INTO " + tableName;
        sqlCommand += " (" + packageColumn + ", " + appColumn + ", " + timeColumn + ") VALUES ";
        sqlCommand += "('" + entry.PackageName + "', '" + entry.AppName + "', " + entry.UsageTimeSec + ")";

        db.execSQL(sqlCommand);
        db.close();
    }

    /**
     * Retrieve all AppUsage information in the AppUsage table.
     * @return A list of all AppUsage Entries stored in database.
     */
    public ArrayList<AppUsageEntry> getAllAppUsage() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get all rows in the AppUsage table.
        Cursor cursor = db.query(TrackDatabaseContract.AppUsageSchema.TABLE_NAME,
                TrackDatabaseContract.AppUsageSchema.ALL_COLUMN_NAMES,
                null, null, null, null, null, null);

        ArrayList<AppUsageEntry> entries = new ArrayList<AppUsageEntry>();

        // Store the rows in a list of AppUsageEntry, and return it.
        if (cursor.moveToFirst()){
            int numApps = cursor.getCount();
            for (int i=0; i<numApps; i++){
                entries.add(new AppUsageEntry(cursor));
            }
        }

        db.close();
        return entries;
    }
}
