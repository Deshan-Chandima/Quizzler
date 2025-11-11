package com.example.quizzlerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button createQuiz = findViewById(R.id.btnCreateQuiz);
        createQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, CreateQuizActivity.class);
                startActivity(intent);
            }
        });

        Button quizMenu = findViewById(R.id.btnQuizMenu);
        quizMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, QuizMenuActivity.class);
                startActivity(intent);
            }
        });

        Button editQuiz = findViewById(R.id.btnEditQuiz);
        editQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, QuizMenu2Activity.class);
                startActivity(intent);
            }
        });

        Button btnLastQuizScore = findViewById(R.id.btnLastQuizScore);
        btnLastQuizScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, LastQuizScoreActivity.class);
                startActivity(intent);
            }
        });

        // Add the download quiz button
        Button btnDownloadQuiz = findViewById(R.id.btnDownloadQuiz);
        btnDownloadQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, DownloadQuizActivity.class);
                startActivity(intent);
            }
        });
    }
}