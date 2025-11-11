package com.example.quizzlerapp;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizDownloader {
    private static final String TAG = "QuizDownloader";
    private final Context context;
    private final DatabaseHelper dbHelper;
    private final TriviaApiService apiService;

    public interface QuizDownloadListener {
        void onQuizDownloaded(String quizName);
        void onError(String errorMessage);
    }

    public QuizDownloader(Context context) {
        this.context = context;
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.apiService = ApiClient.getClient().create(TriviaApiService.class);
    }

    public void downloadQuiz(int amount, Integer category, String difficulty, String quizName, QuizDownloadListener listener) {
        Call<TriviaResponse> call = apiService.getQuestions(amount, category, difficulty, "multiple");

        call.enqueue(new Callback<TriviaResponse>() {
            @Override
            public void onResponse(Call<TriviaResponse> call, Response<TriviaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TriviaResponse triviaResponse = response.body();

                    if (triviaResponse.getResponseCode() == 0) {
                        List<TriviaResponse.Question> questions = triviaResponse.getResults();

                        if (questions.size() > 0) {
                            saveQuizToDatabase(quizName, questions, listener);
                        } else {
                            listener.onError("No questions found");
                        }
                    } else {
                        listener.onError("API Error: " + triviaResponse.getResponseCode());
                    }
                } else {
                    listener.onError("Failed to fetch questions");
                }
            }

            @Override
            public void onFailure(Call<TriviaResponse> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                listener.onError("Network error: " + t.getMessage());
            }
        });
    }

    private void saveQuizToDatabase(String quizName, List<TriviaResponse.Question> apiQuestions, QuizDownloadListener listener) {
        List<Map<String, Object>> formattedQuestions = new ArrayList<>();

        // Process each question (up to 10)
        for (int i = 0; i < Math.min(apiQuestions.size(), 10); i++) {
            TriviaResponse.Question apiQuestion = apiQuestions.get(i);

            Map<String, Object> questionData = new HashMap<>();
            questionData.put("question", apiQuestion.getQuestion());

            Map<String, Object> options = new HashMap<>();

            // Get all answers and shuffle them
            List<String> allOptions = new ArrayList<>(apiQuestion.getIncorrectAnswers());
            allOptions.add(apiQuestion.getCorrectAnswer());
            Collections.shuffle(allOptions);

            // Map the shuffled answers to option letters
            char[] optionLetters = {'A', 'B', 'C', 'D'};
            String correctOption = "";

            for (int j = 0; j < Math.min(allOptions.size(), 4); j++) {
                String optionKey = "option" + optionLetters[j];
                options.put(optionKey, allOptions.get(j));

                if (allOptions.get(j).equals(apiQuestion.getCorrectAnswer())) {
                    correctOption = optionKey;
                }
            }

            questionData.put("options", options);
            questionData.put("correctOption", correctOption);

            formattedQuestions.add(questionData);
        }

        // Save to database
        boolean success = dbHelper.saveQuiz(quizName, formattedQuestions);

        if (success) {
            listener.onQuizDownloaded(quizName);
        } else {
            listener.onError("Failed to save quiz to database");
        }
    }
}