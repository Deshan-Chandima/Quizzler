package com.example.quizzlerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class QuizMenuActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_menu);

        dbHelper = DatabaseHelper.getInstance(this);
        layout = findViewById(R.id.layout);

        displayQuizButtons();
    }

    private void displayQuizButtons() {
        List<String> quizNames = dbHelper.getAllQuizNames();

        if (quizNames.isEmpty()) {
            //  Optionally, display a message to the user
            return;
        }

        for (String quizName : quizNames) {
            Button button = new Button(this);
            button.setText(quizName);
            button.setBackgroundResource(R.color.Pallete2);
            button.setTextColor(getResources().getColor(R.color.Pallete4));
            button.setTypeface(getResources().getFont(R.font.chewy));

            button.setOnClickListener(v -> {
                Intent intent = new Intent(QuizMenuActivity.this, QuizGameActivity.class);
                intent.putExtra("quizName", quizName);
                startActivity(intent);
            });
            layout.addView(button);
        }
    }
}