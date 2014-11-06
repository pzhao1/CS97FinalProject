package edu.swarthmore.cs.moodtracker.util;

/**
 * Created by rliang on 11/3/14.
 */
public class MoodRatingQuestion {
    public static final String MOOD_ACTIVE = "Active";
    public static final String MOOD_DETERMINED = "Determined";
    public static final String MOOD_ATTENTIVE = "Attentive";
    public static final String MOOD_INSPIRED = "Inspired";
    public static final String MOOD_ALERT = "Alert";
    public static final String MOOD_AFRAID = "Afraid";
    public static final String MOOD_NERVOUS = "Nervous";
    public static final String MOOD_UPSET = "Upset";
    public static final String MOOD_HOSTILE = "Hostile";
    public static final String MOOD_ASHAMED = "Ashamed";

    private String mQuestion;
    private Integer mAnswer;

    public static MoodRatingQuestion questionFromDBString(String encodedString) {
        String[] decodedInfo = StringEncodeUtil.decode(encodedString);
        return new MoodRatingQuestion(decodedInfo[1], new Integer(decodedInfo[2]));
    }

    public MoodRatingQuestion(String question, int answer) {
        mQuestion = question;
        mAnswer = answer;
    }


    public String getQuestion() {
        return mQuestion;
    }

    public int getAnswer() {
        return mAnswer;
    }


    @Override
    public String toString() {
        String[] toEncode = new String[] {mQuestion, mAnswer.toString()};
        return StringEncodeUtil.encode(toEncode);
    }
}
