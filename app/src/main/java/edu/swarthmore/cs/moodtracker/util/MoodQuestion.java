package edu.swarthmore.cs.moodtracker.util;

/**
 * Created by rliang on 11/3/14.
 */
public class MoodQuestion {
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

    public enum QuestionType {
        QUESTION_TYPE_RATING ("rating");
        private final String type;

        private QuestionType(String s) {
            type = s;
        }

        public boolean equalsType(String otherType){
            return otherType != null && type.equals(otherType);
        }

        public String toString(){
            return type;
        }
    }

    private QuestionType mQuestionType;
    private String mQuestion;
    private String mAnswer;

    public static MoodQuestion questionFromDBString(String encodedString) {
        String[] decodedInfo = StringEncodeUtil.decode(encodedString);
        return new MoodQuestion(QuestionType.valueOf(decodedInfo[0]), decodedInfo[1], decodedInfo[2]);
    }

    public MoodQuestion(QuestionType questionType, String question, String answer) {
        mQuestionType = questionType;
        mQuestion = question;
        mAnswer = answer;
    }

    public String getmQuestion() {
        return mQuestion;
    }

    public String getmAnswer() {
        return mAnswer;
    }

    public QuestionType getmQuestionType() {
        return mQuestionType;
    }

    @Override
    public String toString() {
        String[] toEncode = new String[] {mQuestionType.toString(), mQuestion, mAnswer};
        return StringEncodeUtil.encode(toEncode);
    }
}
