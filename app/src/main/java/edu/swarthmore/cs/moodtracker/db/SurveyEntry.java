package edu.swarthmore.cs.moodtracker.db;

import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rliang on 11/12/14.
 */
public class SurveyEntry {
    public static final String QUESTIONS_DELIM = "@";

    private Date mDate;
    private List<MoodRatingQuestion> mQuestions;

    public SurveyEntry(Date date, List<MoodRatingQuestion> questions) {
        mDate = date;
        mQuestions = questions;
    }

    public SurveyEntry(Cursor cursor) {
        if (cursor.isAfterLast()){
            return;
        }
        int index = cursor.getColumnIndex(TrackContract.SurveyInfoSchema.COLUMN_DATE);
        if (index >= 0) {
            mDate = new Date(cursor.getLong(index));
        }

        index = cursor.getColumnIndex(TrackContract.SurveyInfoSchema.COLUMN_QUESTIONS_ANSWERS);
        if (index >= 0) {
            String encodedQuestions = cursor.getString(index);
            String[] encodedQuestionsList = encodedQuestions.split(QUESTIONS_DELIM);

            mQuestions = new ArrayList<MoodRatingQuestion>(10);
            for (String encodedQuestion : encodedQuestionsList) {
                MoodRatingQuestion question = MoodRatingQuestion.questionFromDBString(encodedQuestion);
                mQuestions.add(question);
            }
        }

        cursor.moveToNext();
    }

    public Date getDate() {
        return mDate;
    }

    public List<MoodRatingQuestion> getQuestions() {
        return mQuestions;
    }


    /**
     * Converts class to a JSON object. Used for exporting data.
     * @return The JSON representation of this class.
     */
    public JSONObject toJSON() throws JSONException{
        JSONObject jsonObj = new JSONObject();
        for (MoodRatingQuestion question : mQuestions) {
            jsonObj.put(question.getQuestion(), question.getAnswer());
        }
        return jsonObj;

    }

}
