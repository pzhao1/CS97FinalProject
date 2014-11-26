package edu.swarthmore.cs.moodtracker.db;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.swarthmore.cs.moodtracker.util.TrackDateUtil;

/**
 * Async task that exports database to JSON files in public directory
 * Usage: new WriteAppusageTask(context, entries) {
 *            override onFinish()
 *        }.execute()
 */
public abstract class ExportDataTask extends AsyncTask<Integer, Integer, Boolean > {
    public static final String TAG = "ExportDataTask";
    private Context mContext;
    private TrackDatabase mDatabase;

    /**
     * Construct a SaveAppUsage task that writes app usage to database.
     * @param context Used to get database instance.
     */
    public ExportDataTask(Context context) {
        mContext = context;
        mDatabase = TrackDatabase.getInstance(context);
    }

    @Override
    protected Boolean doInBackground(Integer... params) {

        if (!isExternalStorageWritable())
            return false;

        // Make the directory.
        String storageRoot = Environment.getExternalStorageDirectory().toString();
        File saveDir = new File(storageRoot + "/MoodTrackerData");
        if (! saveDir.isDirectory()) {
            if (! saveDir.mkdirs())
                return false;
        }

        // MTP has an indefinite delay in scanning new files. We keep track of new files
        // and send a broadcast requesting immediate scan to get files immediately.
        ArrayList<Uri> filesToScan = new ArrayList<Uri>();

        // Export app usage data.
        if (!exportAppUsageData(saveDir, filesToScan)) {
            return false;
        }

        // Export app usage data.
        if (!exportSurveyData(saveDir, filesToScan)) {
            return false;
        }

        for (Uri uri : filesToScan) {
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        onFinish(success);
    }

    /**
     * Override this method to get result of query.
     */
    public abstract void onFinish(boolean success);

    /**
     * Checks if external storage is available for write
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private boolean exportAppUsageData(File saveDir, ArrayList<Uri> filesToScan) {

        // Pull data from database.
        List<AppUsageEntry> allEntries = mDatabase.readAppUsage(-1, -1);
        HashMap<Long, List<AppUsageEntry> > entriesByDay = new HashMap<Long, List<AppUsageEntry>>();

        for (AppUsageEntry entry : allEntries) {
            if (!entriesByDay.containsKey(entry.DaysSinceEpoch)) {
                entriesByDay.put(entry.DaysSinceEpoch, new ArrayList<AppUsageEntry>());
            }
            entriesByDay.get(entry.DaysSinceEpoch).add(entry);
        }

        for (long date : entriesByDay.keySet()) {

            File saveFile = new File(saveDir, "AppUsage" + String.valueOf(date) + ".json");
            if (saveFile.exists()) {
                if (date == TrackDateUtil.getDaysSinceEpoch() )
                    Log.d(TAG, "deleting today (" + date + ") app usage file: " + (saveFile.delete() ? "success" : "fail"));
                else
                    continue;
            }

            List<AppUsageEntry> oneDayEntries = entriesByDay.get(date);
            JSONObject rootObject;
            try {
                rootObject = appUsageListToJSON(oneDayEntries);
            }
            catch (JSONException e) {
                Log.e(TAG, "Converting day " + date + " usage to JSON failed");
                return false;
            }

            try  {
                FileOutputStream fOut = new FileOutputStream(saveFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.write(rootObject.toString());
                myOutWriter.flush();
                myOutWriter.close();
                fOut.close();
                filesToScan.add(Uri.fromFile(saveFile));
            }
            catch (IOException e) {
                Log.e(TAG, "Writing day " + date + " app usage to file failed");
                return false;
            }
        }
        return true;
    }

    private JSONObject appUsageListToJSON(List<AppUsageEntry> entriesList) throws JSONException {
        JSONObject rootObject = new JSONObject();
        JSONArray usageArray = new JSONArray();
        for (AppUsageEntry entry : entriesList) {
            JSONObject usageObject = entry.toJSON();
            usageArray.put(usageObject);
        }

        rootObject.put("AppUsage", usageArray);
        return rootObject;
    }


    private boolean exportSurveyData(File saveDir, ArrayList<Uri> filesToScan) {

        // Pull data from database.
        List<SurveyEntry> allEntries = mDatabase.readSurveyInfo();
        HashMap<Long, List<SurveyEntry> > entriesByDay = new HashMap<Long, List<SurveyEntry>>();

        for (SurveyEntry entry : allEntries) {
            long daysSinceEpoch = TrackDateUtil.getDaysSinceEpoch(entry.getDate());
            if (!entriesByDay.containsKey(daysSinceEpoch)) {
                entriesByDay.put(daysSinceEpoch, new ArrayList<SurveyEntry>());
            }
            entriesByDay.get(daysSinceEpoch).add(entry);
        }

        for (long date : entriesByDay.keySet()) {

            File saveFile = new File(saveDir, "SurveyInfo" + String.valueOf(date) + ".json");
            if (saveFile.exists()) {
                if (date == TrackDateUtil.getDaysSinceEpoch() )
                    Log.d(TAG, "deleting today (" + date + ") survey info file: " + (saveFile.delete() ? "success" : "fail"));
                else
                    continue;
            }

            List<SurveyEntry> oneDayEntries = entriesByDay.get(date);
            JSONObject rootObject;
            try {
                rootObject = surveyListToJSON(oneDayEntries);
            }
            catch (JSONException e) {
                Log.e(TAG, "Converting day " + date + " survey to JSON failed");
                return false;
            }

            try  {
                FileOutputStream fOut = new FileOutputStream(saveFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.write(rootObject.toString());
                myOutWriter.flush();
                myOutWriter.close();
                fOut.close();
                filesToScan.add(Uri.fromFile(saveFile));
            }
            catch (IOException e) {
                Log.e(TAG, "Writing day " + date + " survey info to file failed");
                return false;
            }
        }
        return true;
    }


    private JSONObject surveyListToJSON(List<SurveyEntry> entriesList) throws JSONException {
        JSONObject rootObject = new JSONObject();
        JSONArray usageArray = new JSONArray();
        for (SurveyEntry entry : entriesList) {
            JSONObject usageObject = entry.toJSON();
            usageArray.put(usageObject);
        }

        rootObject.put("SurveyInfo", usageArray);
        return rootObject;
    }
}
