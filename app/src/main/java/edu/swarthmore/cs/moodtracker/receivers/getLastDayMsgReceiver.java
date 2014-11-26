package edu.swarthmore.cs.moodtracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;

import edu.swarthmore.cs.moodtracker.db.TrackDatabase;

/**
 * Created by cwang3 on 11/9/14.
 * Collect one day of text messages
 */
public class getLastDayMsgReceiver extends BroadcastReceiver {
    public static final String TAG = "getLastDayMsgReceiver";
    private TrackDatabase mDatabase;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        mDatabase = TrackDatabase.getInstance(context);

        Calendar startTime = Calendar.getInstance();
        startTime.add(Calendar.DAY_OF_MONTH,-1);
        startTime.set(Calendar.HOUR_OF_DAY,0);
        startTime.set(Calendar.MINUTE,0);
        startTime.set(Calendar.SECOND,0);
        long start = (long) startTime.getTimeInMillis();

        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DAY_OF_MONTH,-1);
        endTime.set(Calendar.HOUR_OF_DAY,23);
        endTime.set(Calendar.MINUTE,59);
        endTime.set(Calendar.SECOND,59);
        long end = (long) endTime.getTimeInMillis();

        Cursor cur = context.getContentResolver().query(Uri.parse("content://sms"),
                new String[]{"_id", "address", "person", "date", "body", "type"},
                "date >= " + start + " AND date <=" + end,
                null,
                null);

        Log.d(TAG, "cursor created");
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    /*for (int i=0; i < cur.getColumnCount(); i++) {
                        String name = cur.getColumnName(i);
                        System.out.println(name + " : " + cur.getString(i));
                    }*/
                    Integer id = cur.getInt(cur.getColumnIndex("_id"));
                    Long date = cur.getLong(cur.getColumnIndex("date"));
                    String sender = cur.getString(cur.getColumnIndex("person"));
                    String receiver = cur.getString(cur.getColumnIndex("address"));
                    String message = cur.getString(cur.getColumnIndex("body"));
                    Integer type = cur.getInt(cur.getColumnIndex("type"));
                    Log.d(TAG, message);
                    mDatabase.writeTextMsgRecord(id, date, sender, receiver, type, message);
                } while (cur.moveToNext());
            }
        }
        cur.close();


    }

}
