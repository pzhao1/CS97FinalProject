package edu.swarthmore.cs.moodtracker.db;

import android.provider.BaseColumns;

/**
 * Created by Peng on 10/19/2014.
 * Schema of the Track Database. Defines database name, table names, and column names.
 */
public class TrackContract {
    public static final String DATABASE_NAME = "TrackDatabase";
    public static final int DATABASE_VERSION = 1;

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public TrackContract() {}

    /* Inner class that defines the app usage table schema */
    public static abstract class AppUsageSchema implements BaseColumns {
        public static final String TABLE_NAME = "AppUsage";
        public static final String COLUMN_PACKAGE = "package";
        public static final String COLUMN_USAGE_SEC = "usage_sec";
        public static final String COLUMN_DATE = "date";
    }

    /* Inner class that defines the app info table schema */
    public static abstract class AppInfoSchema implements BaseColumns {
        public static final String TABLE_NAME = "AppInfo";
        public static final String COLUMN_PACKAGE = "package";
        public static final String COLUMN_APP_NAME = "app_name";
        public static final String COLUMN_APP_ICON = "app_icon";
    }

    /* Inner class that defines the survey answers table schema*/
    public static abstract class SurveyInfoSchema implements BaseColumns {
        public static final String TABLE_NAME = "SurveyInfo";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_QUESTIONS_ANSWERS = "questions_answers";
    }
}
