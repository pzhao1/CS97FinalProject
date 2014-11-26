package edu.swarthmore.cs.moodtracker.util;

import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Peng on 11/11/2014.
 */
public class TrackDateUtil {
    private static TimeZone mMyTimeZone = TimeZone.getDefault();
    /**
     * Get the number of days passed since Epoch (1970/1/1). This is the date formate stored in database.
     * @return a long representing the number of days since epoch.
     */
    public static long getDaysSinceEpoch() {
        long offset = mMyTimeZone.getOffset(System.currentTimeMillis());
        long millis = System.currentTimeMillis() + offset;
        return millis / (24*3600*1000);
    }

    public static long getDaysSinceEpoch(Date date) {
        long offset = mMyTimeZone.getOffset(System.currentTimeMillis());
        long millis = date.getTime() + offset;
        return millis / (24*3600*1000);
    }
}
