package com.example.quizzlerapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_score);

        // Initialize UI components
        TextView txtScore = findViewById(R.id.txtScore);
        Button btnDone = findViewById(R.id.btnDone);

        // Retrieve user's score from the Intent
        int userScore = getIntent().getIntExtra("userScore", 0);

        // Display the user's score
        txtScore.setText(String.valueOf(userScore));

        // Set up click listener for the "Done" button
        btnDone.setOnClickListener(view -> {
            // Navigate back to the WelcomeActivity
            finish();
        });
    }
}
