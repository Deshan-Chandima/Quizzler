package com.example.quizzlerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LastQuizScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_quiz_score);

        TextView quizNameTextView = findViewById(R.id.last_quiz_name_text_view);
        TextView scoreTextView = findViewById(R.id.last_quiz_score_text_view);

        SharedPreferences prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE);
        String lastQuizName = prefs.getString("last_quiz_name", "N/A");
        int lastQuizScore = prefs.getInt("last_quiz_score", 0);

        quizNameTextView.setText("Quiz Name: " + lastQuizName);
        scoreTextView.setText("Score: " + lastQuizScore);

        Button btnBackToMenu = findViewById(R.id.btnBackToMenu);
        btnBackToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LastQuizScoreActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            }
        });
    }
}