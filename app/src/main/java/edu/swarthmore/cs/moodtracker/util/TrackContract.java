package edu.swarthmore.cs.moodtracker.util;

import android.provider.BaseColumns;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        private static final Map<Integer, String> questionInfo;
        static {
            Map<Integer, String> map = new HashMap<Integer, String>();
            map.put(0, "Active");
            map.put(2, "Determined");
            map.put(3, "Attentive");
            map.put(4, "Inspired");
            map.put(5, "Alert");
            map.put(6, "Afraid");
            map.put(7, "Nervous");
            map.put(8, "Upset");
            map.put(9, "Hostile");
            map.put(10, "Ashamed");
            questionInfo = Collections.unmodifiableMap(map);
        }

        public static final String TABLE_NAME = "SurveyInfo";
        public static final String COLUMN_ANSWERS = "answers";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_SURVEY_NUMBER = "survey_numbers";
    }
}
