package com.example.quizzlerapp;





import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadQuizActivity extends AppCompatActivity {

    private EditText quizNameEditText;
    private Spinner categorySpinner, difficultySpinner;
    private Button downloadButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_quiz);

        quizNameEditText = findViewById(R.id.quizNameEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        downloadButton = findViewById(R.id.downloadButton);
        progressBar = findViewById(R.id.progressBar);

        setupSpinners(); // populate spinner items

        downloadButton.setOnClickListener(v -> {
            String quizName = quizNameEditText.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String difficulty = difficultySpinner.getSelectedItem().toString();

            if (quizName.isEmpty()) {
                Toast.makeText(this, "Please enter a quiz name.", Toast.LENGTH_SHORT).show();
                return;
            }

            new DownloadQuizTask(quizName, category, difficulty).execute();
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Science", "Math", "History", "Geography"});
        categorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Easy", "Medium", "Hard"});
        difficultySpinner.setAdapter(difficultyAdapter);
    }

    private class DownloadQuizTask extends AsyncTask<Void, Void, String> {
        String quizName, category, difficulty;

        public DownloadQuizTask(String quizName, String category, String difficulty) {
            this.quizName = quizName;
            this.category = category;
            this.difficulty = difficulty;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            downloadButton.setEnabled(false);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Replace with your real API endpoint
                String apiUrl = "https://yourapi.com/api/quiz?category=" +
                        category + "&difficulty=" + difficulty;

                URL url = new URL(apiUrl.replace(" ", "%20"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                return result.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            downloadButton.setEnabled(true);

            if (result == null) {
                Toast.makeText(DownloadQuizActivity.this, "Failed to download quiz.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONArray jsonArray = new JSONArray(result);
                SQLiteDatabase db = openOrCreateDatabase("QuizDB", MODE_PRIVATE, null);
                db.execSQL("CREATE TABLE IF NOT EXISTS quiz (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "quiz_name TEXT, question TEXT, option1 TEXT, option2 TEXT, option3 TEXT, option4 TEXT, answer TEXT)");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject quiz = jsonArray.getJSONObject(i);

                    ContentValues values = new ContentValues();
                    values.put("quiz_name", quizName);
                    values.put("question", quiz.getString("question"));
                    values.put("option1", quiz.getString("option1"));
                    values.put("option2", quiz.getString("option2"));
                    values.put("option3", quiz.getString("option3"));
                    values.put("option4", quiz.getString("option4"));
                    values.put("answer", quiz.getString("answer"));

                    db.insert("quiz", null, values);
                }

                db.close();
                Toast.makeText(DownloadQuizActivity.this, "Quiz downloaded and saved!", Toast.LENGTH_LONG).show();

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(DownloadQuizActivity.this, "Error parsing quiz data.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
