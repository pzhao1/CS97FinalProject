package edu.swarthmore.cs.moodtracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import edu.swarthmore.cs.moodtracker.db.TrackContract.AppInfoSchema;
import edu.swarthmore.cs.moodtracker.db.TrackContract.AppUsageSchema;
import edu.swarthmore.cs.moodtracker.db.TrackContract.SurveyInfoSchema;

/**
 * Created by Peng on 10/19/2014.
 * Local SQLite Database that stores all tracking information.
 * Provides permanent storage, i.e. information is not lost when activity and service stop.
 */
public class TrackDatabase extends SQLiteOpenHelper {
    private static final String TAG = "TrackDatabase";

    // Private factory instance.
    private static TrackDatabase sInstance = null;

    // Application context of this database.
    private Context mContext = null;

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
        super(context, TrackContract.DATABASE_NAME, null, TrackContract.DATABASE_VERSION);
        mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the AppUsage Table
        String CREATE_APP_USAGE_TABLE = "CREATE TABLE " + AppUsageSchema.TABLE_NAME + "("
                + AppUsageSchema.COLUMN_PACKAGE + " Text, "
                + AppUsageSchema.COLUMN_USAGE_SEC + " INTEGER, "
                + AppUsageSchema.COLUMN_DATE + " INTEGER, "
                + "PRIMARY KEY (" + AppUsageSchema.COLUMN_PACKAGE + ", " + AppUsageSchema.COLUMN_DATE + ")"
                + ")";
        db.execSQL(CREATE_APP_USAGE_TABLE);

        // Create the AppInfo Table.
        // We don't store AppName and Icon in AppUsage Table, because there might be multiple rows
        // of the same app in AppUsage, and we don't want duplicate information.
        String CREATE_APP_INFO_TABLE = "CREATE TABLE " + AppInfoSchema.TABLE_NAME + "("
                + AppInfoSchema.COLUMN_PACKAGE + " Text PRIMARY KEY, "
                + AppInfoSchema.COLUMN_APP_NAME + " Text, "
                + AppInfoSchema.COLUMN_APP_ICON + " BLOB"
                + ")";
        db.execSQL(CREATE_APP_INFO_TABLE);

        String CREATE_SURVEY_INFO_TABLE = "CREATE TABLE " + SurveyInfoSchema.TABLE_NAME + "("
                + SurveyInfoSchema.COLUMN_DATE + " INTEGER PRIMARY KEY, "
                + SurveyInfoSchema.COLUMN_QUESTIONS_ANSWERS + " Text"
                + ")";
        db.execSQL(CREATE_SURVEY_INFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + AppUsageSchema.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AppInfoSchema.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SurveyInfoSchema.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    /**
     * Retrieve app usage entries from database satisfying certain conditions.
     * @param startDate Earliest date to include in query. Pass in -1 to start from beginning
     * @param endDate Latest date to include in query. Pass in -1 to end at today.
     * @return A list of app usage entries satisfying the given condition.
     */
    public ArrayList<AppUsageEntry> readAppUsage(long startDate, long endDate) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Use a raw query to query appInfoTable and appUsageTable at the same time.
        // Specify selections.
        String selections = " ";
        selections += AppInfoSchema.TABLE_NAME + "." + AppInfoSchema.COLUMN_PACKAGE + ", ";
        selections += AppInfoSchema.TABLE_NAME + "." + AppInfoSchema.COLUMN_APP_NAME + ", ";
        selections += AppInfoSchema.TABLE_NAME + "." + AppInfoSchema.COLUMN_APP_ICON + ", ";
        //selections += "SUM(" + AppUsageSchema.TABLE_NAME + "." + AppUsageSchema.COLUMN_USAGE_SEC + ")" +
        //        " AS " + AppUsageSchema.COLUMN_USAGE_SEC + ", ";
        selections += AppUsageSchema.TABLE_NAME + "." + AppUsageSchema.COLUMN_USAGE_SEC + ", ";
        selections += AppUsageSchema.TABLE_NAME + "." + AppUsageSchema.COLUMN_DATE + " ";

        // Specify Tables ("FROM" clause).
        String tables = " " + AppInfoSchema.TABLE_NAME + ", " + AppUsageSchema.TABLE_NAME + " ";

        // Specify conditions ("WHERE" clause)
        String conditions = " ";
        conditions += AppInfoSchema.TABLE_NAME + "." + AppInfoSchema.COLUMN_PACKAGE +
                " = " + AppUsageSchema.TABLE_NAME + "." + AppUsageSchema.COLUMN_PACKAGE;
        if (startDate > 0)
            conditions += " AND " + AppUsageSchema.TABLE_NAME + "." + AppUsageSchema.COLUMN_DATE + " >= " + startDate;
        if (endDate > 0)
            conditions += " AND " + AppUsageSchema.TABLE_NAME + "." + AppUsageSchema.COLUMN_DATE + " <= " + endDate + " ";

        // Specify "GROUP BY" clause
        //String groupBy =  " " + AppInfoSchema.TABLE_NAME + "." + AppInfoSchema.COLUMN_PACKAGE + " ";

        // Construct raw query.
        String rawQuery = "SELECT" + selections + "FROM" + tables + "WHERE" + conditions; // + "GROUP BY" + groupBy;

        // Query the database to get a cursor
        Cursor cursor = db.rawQuery(rawQuery, null);

        ArrayList<AppUsageEntry> result = new ArrayList<AppUsageEntry>();
        if (cursor.moveToFirst()){
            for (int i=0; i<cursor.getCount(); i++){
                result.add(new AppUsageEntry(cursor));
            }
        }

        db.close();
        return result;
    }

    /**
     * Write an AppUsage entry into the database, overwriting any existing entries.
     * @param entry The AppUsage entry that we write into the database.
     */
    public void writeAppUsage(AppUsageEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Insert app info (name and icon) if AppInfo table doesn't have this app yet.
        ContentValues appInfoValues = new ContentValues();
        appInfoValues.put(AppInfoSchema.COLUMN_PACKAGE, entry.PackageName);
        appInfoValues.put(AppInfoSchema.COLUMN_APP_NAME, entry.AppName);
        appInfoValues.put(AppInfoSchema.COLUMN_APP_ICON, entry.getIconInByteArray());
        db.insertWithOnConflict(AppInfoSchema.TABLE_NAME, null, appInfoValues, SQLiteDatabase.CONFLICT_IGNORE);

        // Insert app usage info, overwriting any existing entries.
        ContentValues appUsageValues = new ContentValues();
        appUsageValues.put(AppUsageSchema.COLUMN_PACKAGE, entry.PackageName);
        appUsageValues.put(AppUsageSchema.COLUMN_USAGE_SEC, entry.UsageTimeSec);
        appUsageValues.put(AppUsageSchema.COLUMN_DATE, entry.DaysSinceEpoch);
        db.insertWithOnConflict(AppUsageSchema.TABLE_NAME, null, appUsageValues, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public List<SurveyEntry> readSurveyInfo() {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableName = SurveyInfoSchema.TABLE_NAME;
        String[] columns = null;
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = SurveyInfoSchema.COLUMN_DATE + " DESC";
        String limit = null;

        Cursor surveyCursor = db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        ArrayList<SurveyEntry> entries = new ArrayList<SurveyEntry>();
        if (surveyCursor.moveToFirst()) {
            int numSurveys = surveyCursor.getCount();
            for (int i = 0; i < numSurveys; i++) {
                entries.add(new SurveyEntry(surveyCursor));
            }
        }

        db.close();
        return entries;
    }

    public void writeSurveyEntry(SurveyEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues surveyValues = new ContentValues();
        surveyValues.put(SurveyInfoSchema.COLUMN_DATE, entry.getDate().getTime());

        StringBuilder builder = new StringBuilder();
        for (MoodRatingQuestion question : entry.getQuestions()) {
            String encodedQuestion = question.toString();
            builder.append(encodedQuestion + SurveyEntry.QUESTIONS_DELIM);
        }

        surveyValues.put(SurveyInfoSchema.COLUMN_QUESTIONS_ANSWERS, builder.toString());
        db.insertWithOnConflict(SurveyInfoSchema.TABLE_NAME, null, surveyValues, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }
}
