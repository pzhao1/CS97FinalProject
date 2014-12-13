package edu.swarthmore.cs.moodtracker;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import com.aliasi.classify.JointClassifier;



/**
 * Fragment for text analysis section.
 */
public class TextSectionFragment extends Fragment {

    public static final String TAG = "TextSectionFragment";

    private View mWaitingView = null;
    private View mContentView = null;
    private EditText mClassifyInput = null;
    private TextView mClassifyBtn = null;
    private TextView mResultText = null;
    private HashMap<String, String> mResults;
    private HashMap<String, Integer> mResultsColors;
    //private JointClassifier<CharSequence> mPolarityClassifier;

    /**
     * Default constructor
     */
    public TextSectionFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_text, container, false);

        // Show waiting wheel when we load classifier
        mWaitingView = rootView.findViewById(R.id.text_analysis_waiting_view);
        mContentView = rootView.findViewById(R.id.text_analysis_content_view);
        mWaitingView.setVisibility(View.VISIBLE);
        mContentView.setVisibility(View.GONE);

        mResults = new HashMap<String, String>();
        mResults.put("pos", "Positive");
        mResults.put("neg", "Negative");
        mResults.put("neutral", "Neutral");


        mResultsColors = new HashMap<String, Integer>();
        mResultsColors.put("pos", 0xFF228b22);
        mResultsColors.put("neg", 0xFFFF4500);
        mResultsColors.put("neutral", 0xFFFFD700);

        //(new LoadClassifierTask()).execute();
        setupRemoteClassifyUI();

        return rootView;
    }

    /*
    private void setupLocalClassifyUI() {
        mWaitingView.setVisibility(View.GONE);
        mContentView.setVisibility(View.VISIBLE);

        mClassifyInput = (EditText) mContentView.findViewById(R.id.text_analysis_input_field);
        mClassifyBtn = (Button) mContentView.findViewById(R.id.text_analysis_classify_btn);
        mResultText = (TextView) mContentView.findViewById(R.id.text_analysis_result_text);

        mClassifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToClassify = mClassifyInput.getText().toString();

                if (textToClassify.length() < 1)
                    mResultText.setText("Input cannot be empty!");
                else
                    mResultText.setText(mPolarityClassifier.classify(textToClassify).bestCategory());

                // Clear focus from input field and hide keyboard.
                hideKeyboard();
            }
        });
    }
    */

    private void setupRemoteClassifyUI() {
        mWaitingView.setVisibility(View.GONE);
        mContentView.setVisibility(View.VISIBLE);

        mClassifyInput = (EditText) mContentView.findViewById(R.id.text_analysis_input_field);
        mClassifyBtn = (TextView) mContentView.findViewById(R.id.text_analysis_classify_btn);
        mResultText = (TextView) mContentView.findViewById(R.id.text_analysis_result_text);

        mClassifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToClassify = mClassifyInput.getText().toString();

                if (textToClassify.length() < 1)
                    mResultText.setText("Input cannot be empty!");
                else {
                    new QueryNLTKTask().execute(textToClassify);
                }

                hideKeyboard();
            }
        });
    }


    private void hideKeyboard() {
        if (mClassifyInput == null)
            return;

        // Clear focus from input field and hide keyboard.
        mClassifyInput.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (getActivity().getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow( getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    private class QueryNLTKTask extends AsyncTask<String, Integer, Boolean> {
        private String mResult = null;
        private String mError = null;
        @Override
        protected Boolean doInBackground(String... params) {

            if (params.length < 1) {
                mError = "Too few arguments. Usage: task.execute(string-to-classify)";
                return false;
            }

            String textToClassify = params[0];

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://text-processing.com/api/sentiment/");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("text", textToClassify));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String strResponse = "";
                for (String line = null; (line = reader.readLine()) != null;) {
                    strResponse += (line + "\n");
                }
                JSONObject responseObject = new JSONObject(strResponse);
                mResult = responseObject.getString("label");
                return true;

            } catch (Exception e) {
                mError = "Error! Are you connected to the network?";
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                String result = "Sentiment for \"" + mClassifyInput.getText() + "\": " + mResults.get(mResult);

                int index = result.lastIndexOf(":");
                SpannableString formattedString = new SpannableString(result);
                formattedString.setSpan(new ForegroundColorSpan(mResultsColors.get(mResult)), index + 1, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mResultText.setText(formattedString);
            }
            else {
                mResultText.setText(mError);
            }

            mClassifyInput.setText(null);
        }
    }

    /*
    private class LoadClassifierTask extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                AssetManager am = getActivity().getAssets();
                InputStream modelFile = am.open("polarity.model");
                ObjectInputStream objIn = new ObjectInputStream(modelFile);
                mPolarityClassifier = (JointClassifier<CharSequence>) objIn.readObject();
            }
            catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                setupLocalClassifyUI();
            }
            else{
                mWaitingView.findViewById(R.id.text_analysis_progress_bar).setVisibility(View.GONE);
                ((TextView) mWaitingView.findViewById(R.id.text_analysis_waiting_text)).setText("Loading failed!");
            }
        }
    }
    */
}
