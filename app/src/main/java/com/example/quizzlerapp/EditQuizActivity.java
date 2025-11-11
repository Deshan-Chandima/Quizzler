package com.example.quizzlerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class EditQuizActivity extends AppCompatActivity {
    private static final String TAG = "EditQuizActivity";
    private String quizName;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quiz);

        dbHelper = DatabaseHelper.getInstance(this);

        // Get the quiz name from the intent
        quizName = getIntent().getStringExtra("quizName");
        if (quizName == null || quizName.isEmpty()) {
            Toast.makeText(this, "Error: Quiz name not provided!", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if no quiz name
            return;
        }

        // Initialize the form with existing data
        initializeForm();

        Button editAndSaveButton = findViewById(R.id.btnEditAndSave);
        editAndSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editAndSaveQuiz();
            }
        });

        Button deleteQuizButton = findViewById(R.id.btnDeleteQuiz);
        deleteQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    private void initializeForm() {
        // Get quiz data from SQLite
        Map<String, Object> quizData = dbHelper.getQuiz(quizName);

        if (quizData != null && !quizData.isEmpty()) {
            // Set quiz name
            EditText quizNameEditText = findViewById(R.id.quizName);
            quizNameEditText.setText(quizName);

            // Set questions and options
            for (int i = 1; i <= 10; i++) {
                String questionKey = "question" + i;
                if (quizData.containsKey(questionKey)) {
                    Map<String, Object> questionData = (Map<String, Object>) quizData.get(questionKey);
                    if (questionData != null) {
                        // Set question text
                        EditText questionEditText = findViewById(getResources().getIdentifier("question" + i, "id", getPackageName()));
                        questionEditText.setText((String) questionData.get("question"));

                        // Set options
                        Map<String, Object> options = (Map<String, Object>) questionData.get("options");
                        if (options != null) {
                            for (char optionChar = 'A'; optionChar <= 'D'; optionChar++) {
                                String optionKey = "option" + optionChar;
                                EditText optionEditText = findViewById(getResources().getIdentifier(
                                        "question" + i + "_" + optionKey + "_edittext", "id", getPackageName()));

                                if (options.containsKey(optionKey)) {
                                    optionEditText.setText((String) options.get(optionKey));
                                }

                                // Set correct option radio button
                                RadioButton optionRadioButton = findViewById(getResources().getIdentifier(
                                        "question" + i + "_" + optionKey + "_radio", "id", getPackageName()));

                                optionRadioButton.setChecked(optionKey.equals(questionData.get("correctOption")));
                            }
                        }
                    }
                }
            }
        } else {
            Log.e(TAG, "Quiz data is null or empty");
            Toast.makeText(this, "Failed to load quiz data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void editAndSaveQuiz() {
        EditText quizNameEditText = findViewById(R.id.quizName);
        String newQuizName = quizNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newQuizName)) {
            quizNameEditText.setError("Please enter Quiz Name");
            return;
        }

        // If quiz name changed, delete old quiz first
        if (!newQuizName.equals(quizName)) {
            dbHelper.deleteQuiz(quizName);
        } else {
            // If quiz name unchanged, update by deleting and recreating
            dbHelper.deleteQuiz(quizName);
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
            String correctOption = "";

            for (char optionChar = 'A'; optionChar <= 'D'; optionChar++) {
                EditText optionEditText = findViewById(getResources().getIdentifier(
                        "question" + i + "_option" + optionChar + "_edittext", "id", getPackageName()));
                String optionText = optionEditText.getText().toString().trim();
                options.put("option" + optionChar, optionText);

                RadioButton optionRadioButton = findViewById(getResources().getIdentifier(
                        "question" + i + "_option" + optionChar + "_radio", "id", getPackageName()));
                if (optionRadioButton.isChecked()) {
                    correctOption = "option" + optionChar;
                }
            }

            Map<String, Object> questionData = new HashMap<>();
            questionData.put("question", questionText);
            questionData.put("options", options);
            questionData.put("correctOption", correctOption);
            questions.add(questionData);
        }

        // Save updated quiz
        boolean success = dbHelper.saveQuiz(newQuizName, questions);

        if (success) {
            Toast.makeText(EditQuizActivity.this, "Quiz updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(EditQuizActivity.this, "Error updating quiz", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Quiz");
        builder.setMessage("Are you sure you want to delete this quiz?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleDeleteQuiz();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void handleDeleteQuiz() {
        boolean success = dbHelper.deleteQuiz(quizName);

        if (success) {
            Toast.makeText(EditQuizActivity.this, "Quiz deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(EditQuizActivity.this, "Error deleting quiz", Toast.LENGTH_SHORT).show();
        }
    }
}