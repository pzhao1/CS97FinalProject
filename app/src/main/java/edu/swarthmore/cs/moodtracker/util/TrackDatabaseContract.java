package edu.swarthmore.cs.moodtracker.util;

import android.provider.BaseColumns;

/**
 * Created by Peng on 10/19/2014.
 * Schema of the Track Database. Defines database name, table names, and column names.
 */
public class TrackDatabaseContract {
    public static final String DATABASE_NAME = "TrackDatabase";
    public static final int DATABASE_VERSION = 1;

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public TrackDatabaseContract() {}

    /* Inner class that defines the table contents */
    public static abstract class AppUsageSchema implements BaseColumns {
        public static final String TABLE_NAME = "AppUsage";
        public static final String COLUMN_NAME_PACKAGE = "package";
        public static final String COLUMN_NAME_APP = "app";
        public static final String COLUMN_NAME_TIME = "time";

        public static final String[] ALL_COLUMN_NAMES = new String[]{
                COLUMN_NAME_PACKAGE, COLUMN_NAME_APP, COLUMN_NAME_TIME};
    }
}
