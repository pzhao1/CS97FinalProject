package edu.swarthmore.cs.moodtracker.db;

import android.content.Context;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


/**
 * Async task that retrieves app usage information from database.
 * Usage: new ReadAppusageTask(context [,service]) {
 *            override onFinish(result)
 *        }.execute(startDate, endDate[, displayLimit])
 * Pass in startDate = -1 to read from beginning of time.
 * Pass in endDate = -1 to read until end of time.
 */
public abstract class QuerySentimentTask extends AsyncTask<Integer, Integer, Boolean> {
    private final String NLTK_URL = "http://text-processing.com/api/sentiment/";
    private Context mContext = null;
    private TrackDatabase mDatabase = null;
    private String mError = "";

    /**
     * Construct a ReadAppUsageTask that read app usages from database.
     * @param context Used to get database instance.
     */
    public QuerySentimentTask(Context context) {
        mDatabase = TrackDatabase.getInstance(context);
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {

        List<TextMsgEntry> negativeEntries = mDatabase.readTextMsg(true);
        HttpClient httpclient = new DefaultHttpClient();

        for (TextMsgEntry entry:negativeEntries) {
            // Create new http post request.
            HttpPost httppost = new HttpPost(NLTK_URL);
            try {
                // Add your data.
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("text", entry.message));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request.
                HttpResponse response = httpclient.execute(httppost);

                // Check if we have a valid resposne.
                if (response.getStatusLine().getStatusCode() != 200) {
                    mError = "Got HTTP 400 Error: Bad request";
                    return false;
                }

                // Read response into a JSON object.
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String strResponse = "";
                for (String line = null; (line = reader.readLine()) != null; ) {
                    strResponse += (line + "\n");
                }
                JSONObject responseObject = new JSONObject(strResponse);

                // Populate the entry.
                JSONObject probability = responseObject.getJSONObject("probability");
                entry.positive = probability.getDouble("pos");
                entry.negative = probability.getDouble("neg");
                entry.neutral = probability.getDouble("neutral");

                if (entry.positive < 0 || entry.negative < 0 || entry.neutral < 0) {
                    mError = "Got negative value from NLTK";
                    return false;
                }

            } catch (JSONException e) {
                mError = "Caught JSON exception. Not handling format properly";
                return false;
            } catch (UnknownHostException e) {
                mError = "Host nltk cannot be resolved. Are you connected to network?";
                return false;
            } catch (Exception e) {
                mError = "Caught unknown exception: " + e.toString();
                return false;
            }
        }

        for (TextMsgEntry entry : negativeEntries) {
            mDatabase.writeTextMsgRecord(entry);
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        onFinish(success, mError);
    }

    /**
     * Override this method to get result of query.
     */
    public abstract void onFinish(boolean success, String error);


}
