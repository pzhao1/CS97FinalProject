package edu.swarthmore.cs.moodtracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import edu.swarthmore.cs.moodtracker.db.AppUsageEntry;
import edu.swarthmore.cs.moodtracker.db.QuerySentimentTask;
import edu.swarthmore.cs.moodtracker.db.TextMsgEntry;
import edu.swarthmore.cs.moodtracker.db.TrackDatabase;

/**
 * Created by cwang3 on 11/9/14.
 * Collect one day of text messages
 */
public class CollectTextMsgReceiver extends BroadcastReceiver {
    public static final String TAG = "CollectTextMsgReceiver";
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
                    TextMsgEntry entry = new TextMsgEntry(id,date,sender,receiver,type,message,-1,-1,-1);
                    mDatabase.writeTextMsgRecord(entry);
                } while (cur.moveToNext());
            }
            cur.close();
        }

        ConnectivityManager mConManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = mConManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if (isConnected) {
            new QuerySentimentTask(context) {
                @Override
                public void onFinish(boolean success, String error) {

                }
            }.execute();
        }


        //tryRegression();

    }


    private void tryRegression() {
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        double[] y = {11.0, 12.0, 13.0, 14.0, 15.0, 16.0};
        double[][] x = new double[6] [];
        x[0] = new double[]{0, 0, 0, 0, 0};
        x[1] = new double[]{2.0, 0, 0, 0, 0};
        x[2] = new double[]{0, 3.0, 0, 0, 0};
        x[3] = new double[]{0, 0, 4.0, 0, 0};
        x[4] = new double[]{0, 0, 0, 5.0, 0};
        x[5] = new double[]{0, 0, 0, 0, 6.0};
        regression.newSampleData(y, x);

        double[] beta = regression.estimateRegressionParameters();
        System.out.println("beta: " + Arrays.toString(beta));
        double rSquared = regression.calculateRSquared();
        System.out.printf("RSquare: %f\n", rSquared);
    }

    private void trainModel(Context context) {
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        TrackDatabase mDatabase = TrackDatabase.getInstance(context);
        // Read in dependent variable -- scores for mood each day
        double[] moodScore = null;
        // Read in app usage info

    }

}
