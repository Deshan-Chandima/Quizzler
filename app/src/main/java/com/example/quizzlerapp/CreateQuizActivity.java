package com.example.quizzlerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateQuizActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        dbHelper = DatabaseHelper.getInstance(this);

        Button createAndSaveButton = findViewById(R.id.btnCreateAndSave);
        createAndSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText quizNameEditText = findViewById(R.id.quizName);
                String quizName = quizNameEditText.getText().toString().trim();

                // Validate the quiz name
                if (TextUtils.isEmpty(quizName)) {
                    quizNameEditText.setError("Please enter Quiz Name");
                    return;
                }

                // Prepare quiz data
                List<Map<String, Object>> questions = new ArrayList<>();
                for (int i = 1; i <= 10; i++) {
                    EditText questionEditText = findViewById(getResources().getIdentifier("question" + i, "id", getPackageName()));
                    String questionText = questionEditText.getText().toString().trim();

                    // Skip empty questions
                    if (TextUtils.isEmpty(questionText)) {
                        continue;
                    }

                    Map<String, Object> options = new HashMap<>();
                    RadioGroup radioGroup = findViewById(getResources().getIdentifier("question" + i + "_options", "id", getPackageName()));
                    int selectedOptionId = radioGroup.getCheckedRadioButtonId();
                    String correctOption = "";

                    for (char option = 'A'; option <= 'D'; option++) {
                        EditText optionEditText = findViewById(getResources().getIdentifier("question" + i + "_option" + option + "_edittext", "id", getPackageName()));
                        String optionText = optionEditText.getText().toString().trim();
                        options.put("option" + option, optionText);

                        RadioButton optionRadioButton = findViewById(getResources().getIdentifier("question" + i + "_option" + option + "_radio", "id", getPackageName()));
                        if (optionRadioButton.isChecked()) {
                            correctOption = "option" + option;
                        }
                    }

                    Map<String, Object> questionData = new HashMap<>();
                    questionData.put("question", questionText);
                    questionData.put("options", options);
                    questionData.put("correctOption", correctOption);

                    questions.add(questionData);
                }

                // Save the quiz to SQLite
                boolean success = dbHelper.saveQuiz(quizName, questions);

                if (success) {
                    Toast.makeText(CreateQuizActivity.this, "Quiz Created Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateQuizActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateQuizActivity.this, "Error creating quiz. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}