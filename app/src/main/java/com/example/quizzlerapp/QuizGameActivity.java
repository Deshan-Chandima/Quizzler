package com.example.quizzlerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class QuizGameActivity extends AppCompatActivity {

    private static final String TAG = "QuizGameActivity";
    private TextView txtQuestion, txtTimer;
    private Button btnOptionA, btnOptionB, btnOptionC, btnOptionD;

    private String quizName;
    private int currentQuestionNumber = 1;
    private int userScore = 0;
    private CountDownTimer countDownTimer;
    private DatabaseHelper dbHelper;
    private Map<String, Object> quizData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_game);

        txtQuestion = findViewById(R.id.txtQuestion);
        txtTimer = findViewById(R.id.txtTimer);
        btnOptionA = findViewById(R.id.btnOptionA);
        btnOptionB = findViewById(R.id.btnOptionB);
        btnOptionC = findViewById(R.id.btnOptionC);
        btnOptionD = findViewById(R.id.btnOptionD);

        quizName = getIntent().getStringExtra("quizName");
        dbHelper = DatabaseHelper.getInstance(this); // Initialize DatabaseHelper

        // Load the entire quiz data once
        quizData = dbHelper.getQuiz(quizName);
        if (quizData == null || quizData.isEmpty()) {
            Toast.makeText(this, "Failed to load quiz data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadQuestion(currentQuestionNumber);

        btnOptionA.setOnClickListener(view -> checkAnswer("optionA"));
        btnOptionB.setOnClickListener(view -> checkAnswer("optionB"));
        btnOptionC.setOnClickListener(view -> checkAnswer("optionC"));
        btnOptionD.setOnClickListener(view -> checkAnswer("optionD"));
    }

    private void loadQuestion(int questionNumber) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        String questionKey = "question" + questionNumber;
        if (quizData.containsKey(questionKey)) {
            Map<String, Object> questionData = (Map<String, Object>) quizData.get(questionKey);

            if (questionData != null) {
                txtQuestion.setText((String) questionData.get("question"));

                Map<String, Object> options = (Map<String, Object>) questionData.get("options");
                if (options != null) {
                    btnOptionA.setText((String) options.get("optionA"));
                    btnOptionB.setText((String) options.get("optionB"));
                    btnOptionC.setText((String) options.get("optionC"));
                    btnOptionD.setText((String) options.get("optionD"));
                }

                clearSelectedAnswer();
                enableOptionButtons(true);
                startTimer();
            } else {
                navigateToViewScore();
            }
        } else {
            navigateToViewScore();
        }
    }

    private void clearSelectedAnswer() {
        btnOptionA.setSelected(false);
        btnOptionB.setSelected(false);
        btnOptionC.setSelected(false);
        btnOptionD.setSelected(false);
    }

    private void enableOptionButtons(boolean enable) {
        btnOptionA.setEnabled(enable);
        btnOptionB.setEnabled(enable);
        btnOptionC.setEnabled(enable);
        btnOptionD.setEnabled(enable);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                txtTimer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                txtTimer.setText("0");
                checkAnswer(""); // Auto-select incorrect answer
            }
        }.start();
    }

    private void checkAnswer(String selectedOption) {
        enableOptionButtons(false);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        String questionKey = "question" + currentQuestionNumber;
        if (quizData.containsKey(questionKey)) {
            Map<String, Object> questionData = (Map<String, Object>) quizData.get(questionKey);

            if (questionData != null) {
                String correctOption = (String) questionData.get("correctOption");
                Log.d(TAG, "Correct Answer: " + correctOption + ", Selected: " + selectedOption);

                boolean isCorrect = correctOption.equals(selectedOption);
                Log.d(TAG, "Is Correct: " + isCorrect);

                if (isCorrect) {
                    userScore++;
                    Toast.makeText(this, "Correct! Your Score: " + userScore, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Incorrect! The correct answer was option " +
                            correctOption.substring(correctOption.length() - 1), Toast.LENGTH_SHORT).show();
                }

                // Save the last quiz score
                SharedPreferences prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("last_quiz_name", quizName);
                editor.putInt("last_quiz_score", userScore);
                editor.apply();

                currentQuestionNumber++;
                if (currentQuestionNumber <= 10 && quizData.containsKey("question" + currentQuestionNumber)) {
                    loadQuestion(currentQuestionNumber);
                } else {
                    // Also save final score to database
                    dbHelper.addScore(quizName, userScore);
                    navigateToViewScore();
                }
            }
        } else {
            Toast.makeText(this, "Error checking answer", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToViewScore() {
        Intent intent = new Intent(QuizGameActivity.this, ViewScoreActivity.class);
        intent.putExtra("userScore", userScore);
        startActivity(intent);
        finish();
    }
}