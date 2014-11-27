package edu.swarthmore.cs.moodtracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import edu.swarthmore.cs.moodtracker.db.QuerySentimentTask;


/**
 * Created by cwang3 on 11/26/14.
 */
public class WifiReceiver extends BroadcastReceiver{
    public final String TAG = "WifiReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager mConManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = mConManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if (isConnected) {
            Log.d(TAG, "Start querying sentiment");
            new QuerySentimentTask(context) {
                @Override
                public void onFinish(boolean success, String error) {
                    Log.d(TAG, "query sentiment " + ((success) ? "success!" : "failed because of " + error));
                }
            }.execute();
        }
    }
}
