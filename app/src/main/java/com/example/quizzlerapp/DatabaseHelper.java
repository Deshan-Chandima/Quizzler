package com.example.quizzlerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "quizzlerDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_QUIZZES = "quizzes";
    private static final String TABLE_QUESTIONS = "questions";
    private static final String TABLE_OPTIONS = "options";
    private static final String TABLE_SCORES = "scores";

    // Quiz Table Columns
    private static final String KEY_QUIZ_ID = "id";
    private static final String KEY_QUIZ_NAME = "quiz_name";

    // Question Table Columns
    private static final String KEY_QUESTION_ID = "id";
    private static final String KEY_QUESTION_QUIZ_ID_FK = "quiz_id";
    private static final String KEY_QUESTION_TEXT = "question_text";
    private static final String KEY_QUESTION_NUMBER = "question_number";
    private static final String KEY_CORRECT_OPTION = "correct_option";

    // Option Table Columns
    private static final String KEY_OPTION_ID = "id";
    private static final String KEY_OPTION_QUESTION_ID_FK = "question_id";
    private static final String KEY_OPTION_LETTER = "option_letter"; // A, B, C, or D
    private static final String KEY_OPTION_TEXT = "option_text";

    // Score Table Columns
    private static final String KEY_SCORE_ID = "id";
    private static final String KEY_SCORE_QUIZ_ID_FK = "quiz_id";
    private static final String KEY_SCORE_VALUE = "score_value";
    private static final String KEY_SCORE_DATE = "score_date";

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_QUIZZES_TABLE = "CREATE TABLE " + TABLE_QUIZZES +
                "(" +
                KEY_QUIZ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_QUIZ_NAME + " TEXT UNIQUE" +
                ")";

        String CREATE_QUESTIONS_TABLE = "CREATE TABLE " + TABLE_QUESTIONS +
                "(" +
                KEY_QUESTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_QUESTION_QUIZ_ID_FK + " INTEGER REFERENCES " + TABLE_QUIZZES + "," +
                KEY_QUESTION_TEXT + " TEXT," +
                KEY_QUESTION_NUMBER + " INTEGER," +
                KEY_CORRECT_OPTION + " TEXT," +
                "UNIQUE (" + KEY_QUESTION_QUIZ_ID_FK + ", " + KEY_QUESTION_NUMBER + ")" +
                ")";

        String CREATE_OPTIONS_TABLE = "CREATE TABLE " + TABLE_OPTIONS +
                "(" +
                KEY_OPTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_OPTION_QUESTION_ID_FK + " INTEGER REFERENCES " + TABLE_QUESTIONS + "," +
                KEY_OPTION_LETTER + " TEXT," +
                KEY_OPTION_TEXT + " TEXT," +
                "UNIQUE (" + KEY_OPTION_QUESTION_ID_FK + ", " + KEY_OPTION_LETTER + ")" +
                ")";

        String CREATE_SCORES_TABLE = "CREATE TABLE " + TABLE_SCORES +
                "(" +
                KEY_SCORE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_SCORE_QUIZ_ID_FK + " INTEGER REFERENCES " + TABLE_QUIZZES + "," +
                KEY_SCORE_VALUE + " INTEGER," +
                KEY_SCORE_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";

        db.execSQL(CREATE_QUIZZES_TABLE);
        db.execSQL(CREATE_QUESTIONS_TABLE);
        db.execSQL(CREATE_OPTIONS_TABLE);
        db.execSQL(CREATE_SCORES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_OPTIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZZES);
            onCreate(db);
        }
    }

    // Adding a new quiz
    public long addQuiz(String quizName) {
        SQLiteDatabase db = getWritableDatabase();
        long quizId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_QUIZ_NAME, quizName);

            quizId = db.insertWithOnConflict(TABLE_QUIZZES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error while adding quiz", e);
        } finally {
            db.endTransaction();
        }

        return quizId;
    }

    // Adding a question to a quiz
    public long addQuestion(long quizId, String questionText, int questionNumber, String correctOption) {
        SQLiteDatabase db = getWritableDatabase();
        long questionId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_QUESTION_QUIZ_ID_FK, quizId);
            values.put(KEY_QUESTION_TEXT, questionText);
            values.put(KEY_QUESTION_NUMBER, questionNumber);
            values.put(KEY_CORRECT_OPTION, correctOption);

            questionId = db.insertWithOnConflict(TABLE_QUESTIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error while adding question", e);
        } finally {
            db.endTransaction();
        }

        return questionId;
    }

    // Adding an option to a question
    public boolean addOption(long questionId, String optionLetter, String optionText) {
        SQLiteDatabase db = getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_OPTION_QUESTION_ID_FK, questionId);
            values.put(KEY_OPTION_LETTER, optionLetter);
            values.put(KEY_OPTION_TEXT, optionText);

            db.insertWithOnConflict(TABLE_OPTIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "Error while adding option", e);
        } finally {
            db.endTransaction();
        }

        return success;
    }

    // Saving a complete quiz with questions and options
    public boolean saveQuiz(String quizName, List<Map<String, Object>> questions) {
        SQLiteDatabase db = getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            // First add the quiz
            long quizId = addQuiz(quizName);

            if (quizId != -1) {
                // Then add each question and its options
                for (int i = 0; i < questions.size(); i++) {
                    Map<String, Object> questionData = questions.get(i);
                    String questionText = (String) questionData.get("question");
                    String correctOption = (String) questionData.get("correctOption");

                    long questionId = addQuestion(quizId, questionText, i + 1, correctOption);

                    if (questionId != -1) {
                        Map<String, Object> options = (Map<String, Object>) questionData.get("options");
                        for (String optionKey : options.keySet()) {
                            String optionLetter = optionKey.replace("option", "");
                            String optionText = (String) options.get(optionKey);
                            addOption(questionId, optionLetter, optionText);
                        }
                    }
                }
            }

            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "Error while saving quiz", e);
        } finally {
            db.endTransaction();
        }

        return success;
    }

    // Getting all quiz names
    public List<String> getAllQuizNames() {
        List<String> quizNames = new ArrayList<>();

        String QUIZ_SELECT_QUERY = "SELECT * FROM " + TABLE_QUIZZES;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(QUIZ_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    quizNames.add(cursor.getString(cursor.getColumnIndexOrThrow(KEY_QUIZ_NAME)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting quiz names", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return quizNames;
    }

    // Getting a quiz with all questions and options
    public Map<String, Object> getQuiz(String quizName) {
        Map<String, Object> quiz = new HashMap<>();

        SQLiteDatabase db = getReadableDatabase();

        // First get the quiz ID
        String QUIZ_SELECT_QUERY = "SELECT * FROM " + TABLE_QUIZZES + " WHERE " + KEY_QUIZ_NAME + " = ?";
        Cursor quizCursor = db.rawQuery(QUIZ_SELECT_QUERY, new String[]{quizName});

        try {
            if (quizCursor.moveToFirst()) {
                long quizId = quizCursor.getLong(quizCursor.getColumnIndexOrThrow(KEY_QUIZ_ID));
                quiz.put("quizName", quizName);

                // Get all questions for this quiz
                String QUESTIONS_SELECT_QUERY = "SELECT * FROM " + TABLE_QUESTIONS +
                        " WHERE " + KEY_QUESTION_QUIZ_ID_FK + " = ?" +
                        " ORDER BY " + KEY_QUESTION_NUMBER;
                Cursor questionsCursor = db.rawQuery(QUESTIONS_SELECT_QUERY, new String[]{String.valueOf(quizId)});

                try {
                    if (questionsCursor.moveToFirst()) {
                        do {
                            long questionId = questionsCursor.getLong(questionsCursor.getColumnIndexOrThrow(KEY_QUESTION_ID));
                            int questionNumber = questionsCursor.getInt(questionsCursor.getColumnIndexOrThrow(KEY_QUESTION_NUMBER));
                            String questionText = questionsCursor.getString(questionsCursor.getColumnIndexOrThrow(KEY_QUESTION_TEXT));
                            String correctOption = questionsCursor.getString(questionsCursor.getColumnIndexOrThrow(KEY_CORRECT_OPTION));

                            Map<String, Object> questionData = new HashMap<>();
                            questionData.put("question", questionText);
                            questionData.put("correctOption", correctOption);

                            // Get all options for this question
                            String OPTIONS_SELECT_QUERY = "SELECT * FROM " + TABLE_OPTIONS +
                                    " WHERE " + KEY_OPTION_QUESTION_ID_FK + " = ?";
                            Cursor optionsCursor = db.rawQuery(OPTIONS_SELECT_QUERY, new String[]{String.valueOf(questionId)});

                            Map<String, Object> options = new HashMap<>();
                            try {
                                if (optionsCursor.moveToFirst()) {
                                    do {
                                        String optionLetter = optionsCursor.getString(optionsCursor.getColumnIndexOrThrow(KEY_OPTION_LETTER));
                                        String optionText = optionsCursor.getString(optionsCursor.getColumnIndexOrThrow(KEY_OPTION_TEXT));
                                        options.put("option" + optionLetter, optionText);
                                    } while (optionsCursor.moveToNext());
                                }
                            } finally {
                                if (optionsCursor != null && !optionsCursor.isClosed()) {
                                    optionsCursor.close();
                                }
                            }

                            questionData.put("options", options);
                            quiz.put("question" + questionNumber, questionData);

                        } while (questionsCursor.moveToNext());
                    }
                } finally {
                    if (questionsCursor != null && !questionsCursor.isClosed()) {
                        questionsCursor.close();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting quiz", e);
        } finally {
            if (quizCursor != null && !quizCursor.isClosed()) {
                quizCursor.close();
            }
        }

        return quiz;
    }

    // Deleting a quiz
    public boolean deleteQuiz(String quizName) {
        SQLiteDatabase db = getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            // First get the quiz ID
            String QUIZ_SELECT_QUERY = "SELECT * FROM " + TABLE_QUIZZES + " WHERE " + KEY_QUIZ_NAME + " = ?";
            Cursor quizCursor = db.rawQuery(QUIZ_SELECT_QUERY, new String[]{quizName});

            if (quizCursor.moveToFirst()) {
                long quizId = quizCursor.getLong(quizCursor.getColumnIndexOrThrow(KEY_QUIZ_ID));

                // Delete all options for questions in this quiz
                db.execSQL("DELETE FROM " + TABLE_OPTIONS +
                                " WHERE " + KEY_OPTION_QUESTION_ID_FK + " IN " +
                                "(SELECT " + KEY_QUESTION_ID + " FROM " + TABLE_QUESTIONS +
                                " WHERE " + KEY_QUESTION_QUIZ_ID_FK + " = ?)",
                        new String[]{String.valueOf(quizId)});

                // Delete all questions for this quiz
                db.delete(TABLE_QUESTIONS, KEY_QUESTION_QUIZ_ID_FK + " = ?", new String[]{String.valueOf(quizId)});

                // Delete all scores for this quiz
                db.delete(TABLE_SCORES, KEY_SCORE_QUIZ_ID_FK + " = ?", new String[]{String.valueOf(quizId)});

                // Finally delete the quiz itself
                db.delete(TABLE_QUIZZES, KEY_QUIZ_ID + " = ?", new String[]{String.valueOf(quizId)});
            }

            quizCursor.close();
            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "Error while deleting quiz", e);
        } finally {
            db.endTransaction();
        }

        return success;
    }

    // Recording a quiz score
    public boolean addScore(String quizName, int score) {
        SQLiteDatabase db = getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            // First get the quiz ID
            String QUIZ_SELECT_QUERY = "SELECT * FROM " + TABLE_QUIZZES + " WHERE " + KEY_QUIZ_NAME + " = ?";
            Cursor quizCursor = db.rawQuery(QUIZ_SELECT_QUERY, new String[]{quizName});

            if (quizCursor.moveToFirst()) {
                long quizId = quizCursor.getLong(quizCursor.getColumnIndexOrThrow(KEY_QUIZ_ID));

                // Insert the score
                ContentValues values = new ContentValues();
                values.put(KEY_SCORE_QUIZ_ID_FK, quizId);
                values.put(KEY_SCORE_VALUE, score);

                db.insert(TABLE_SCORES, null, values);
            }

            quizCursor.close();
            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "Error while adding score", e);
        } finally {
            db.endTransaction();
        }

        return success;
    }

    // Getting the last quiz score
    public Map<String, Object> getLastScore() {
        Map<String, Object> scoreData = new HashMap<>();

        SQLiteDatabase db = getReadableDatabase();

        String SCORE_SELECT_QUERY =
                "SELECT s." + KEY_SCORE_VALUE + ", q." + KEY_QUIZ_NAME + ", s." + KEY_SCORE_DATE +
                        " FROM " + TABLE_SCORES + " s" +
                        " JOIN " + TABLE_QUIZZES + " q ON s." + KEY_SCORE_QUIZ_ID_FK + " = q." + KEY_QUIZ_ID +
                        " ORDER BY s." + KEY_SCORE_ID + " DESC LIMIT 1";

        Cursor cursor = db.rawQuery(SCORE_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE_VALUE));
                String quizName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_QUIZ_NAME));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SCORE_DATE));

                scoreData.put("score", score);
                scoreData.put("quizName", quizName);
                scoreData.put("date", date);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting last score", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return scoreData;
    }
}