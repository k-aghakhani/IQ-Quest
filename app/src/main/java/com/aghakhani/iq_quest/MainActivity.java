package com.aghakhani.iq_quest;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView questionText, statusText, timerText;
    private RadioGroup optionsGroup;
    private RadioButton option1, option2, option3, option4;
    private Button nextButton, skipButton;
    private ProgressBar progressBar;
    private CountDownTimer countDownTimer;
    private MediaPlayer correctSound, wrongSound;
    private final int quizTimeLimit = 100000; // 120 seconds for the entire quiz
    private long timeLeft;

    private List<String[]> allQuestions = new ArrayList<>(); // Store all fetched questions
    private List<String[]> questions = new ArrayList<>(); // Store selected questions for the game
    private final int QUESTIONS_TO_USE = 15; // Set how many questions you want (change this)

    // Default questions in case fetching fails
    private String[][] defaultQuestions = {
            {"What is 2 + 2?", "3", "4", "5", "6", "4"},
            {"Which is a fruit?", "Carrot", "Potato", "Apple", "Onion", "Apple"}
    };

    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int lives = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        questionText = findViewById(R.id.questionText);
        statusText = findViewById(R.id.statusText);
        timerText = findViewById(R.id.timerText);
        optionsGroup = findViewById(R.id.optionsGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        nextButton = findViewById(R.id.nextButton);
        skipButton = findViewById(R.id.skipButton);
        progressBar = findViewById(R.id.progressBar);

        // Initialize sound effects
        correctSound = MediaPlayer.create(this, R.raw.correct_sound);
        wrongSound = MediaPlayer.create(this, R.raw.wrong_sound);

        // Set button click listeners
        nextButton.setOnClickListener(v -> checkAnswer());
        skipButton.setOnClickListener(v -> skipQuestion());

        // Fetch questions from Google Sheets
        fetchQuestionsFromCSV();
    }

    // Fetch questions from the Google Sheets CSV link using Volley
    private void fetchQuestionsFromCSV() {
        String url = "https://docs.google.com/spreadsheets/d/14aPuqvSzA3cCyuRHUZ06pm9fXghtfxuprDvsemtEyv8/export?format=csv";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("CSV_RESPONSE", "Raw CSV: " + response);
                        Toast.makeText(MainActivity.this, "CSV fetched successfully", Toast.LENGTH_SHORT).show();

                        // Parse the CSV response
                        parseCSV(response);

                        // Check if questions were parsed successfully
                        if (allQuestions.isEmpty()) {
                            Log.w("CSV_PARSE", "No questions parsed from CSV");
                            Toast.makeText(MainActivity.this, "No questions parsed, using default", Toast.LENGTH_LONG).show();
                            loadDefaultQuestions();
                        } else {
                            Log.d("CSV_PARSE", "Parsed " + allQuestions.size() + " questions");
                            Toast.makeText(MainActivity.this, "Fetched " + allQuestions.size() + " questions", Toast.LENGTH_SHORT).show();
                            selectRandomQuestions(); // Select random questions
                        }

                        // Start the timer and load the first question
                        startTimer();
                        loadQuestion();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CSV_ERROR", "Error fetching CSV: " + error.toString());
                Toast.makeText(MainActivity.this, "Failed to fetch questions: " + error.getMessage(), Toast.LENGTH_LONG).show();

                // Load default questions on failure
                loadDefaultQuestions();
                startTimer();
                loadQuestion();
            }
        });

        // Add the request to the queue
        queue.add(stringRequest);
    }

    // Load default questions into the list
    private void loadDefaultQuestions() {
        for (String[] question : defaultQuestions) {
            questions.add(question);
        }
    }

    // Parse CSV data into the allQuestions list (7 columns: Number,Question,Option1,Option2,Option3,Option4,CorrectAnswer)
    private void parseCSV(String csvData) {
        String[] lines = csvData.split("\n"); // Split by line
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || i == 0) continue; // Skip empty lines and header row

            // Simple split by comma
            String[] parts = line.split(",");
            Log.d("CSV_PARSE", "Line " + i + ": " + line + " | Parts: " + parts.length);

            // Ensure there are exactly 7 parts (Number + Question + 4 options + Correct Answer)
            if (parts.length >= 7) { // Use >= to handle extra commas gracefully
                // Create a new array with only Question, Options, and Correct Answer (skip Number)
                String[] questionData = new String[6];
                for (int j = 0; j < 6; j++) {
                    questionData[j] = parts[j + 1].replace("\"", "").trim(); // Skip Number (parts[0]), remove quotes
                }
                allQuestions.add(questionData);
                Log.d("CSV_PARSE", "Added question: " + questionData[0]);
            } else {
                Log.w("CSV_PARSE", "Invalid line format at line " + i + ": " + line);
            }
        }
    }

    // Select a random subset of questions
    private void selectRandomQuestions() {
        Collections.shuffle(allQuestions); // Shuffle all questions
        int questionsToUse = Math.min(QUESTIONS_TO_USE, allQuestions.size()); // Use the smaller of desired or available
        for (int i = 0; i < questionsToUse; i++) {
            questions.add(allQuestions.get(i)); // Add the first X questions
        }
        Log.d("RANDOM_QUESTIONS", "Selected " + questions.size() + " random questions");
        Toast.makeText(this, "Using " + questions.size() + " random questions", Toast.LENGTH_SHORT).show();
    }

    // Starts the countdown timer for the quiz
    private void startTimer() {
        countDownTimer = new CountDownTimer(quizTimeLimit, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerText.setText("Time: " + secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                showResultDialog("Time's up!");
            }
        }.start();
    }

    // Loads the current question and options into the UI
    private void loadQuestion() {
        if (questions.isEmpty()) {
            Log.e("LOAD_QUESTION", "No questions available!");
            Toast.makeText(this, "No questions available!", Toast.LENGTH_LONG).show();
            return;
        }
        if (currentQuestionIndex >= questions.size() || lives <= 0) {
            countDownTimer.cancel();
            showResultDialog(lives <= 0 ? "No lives left!" : "Quiz Completed!");
            return;
        }

        String[] currentQuestion = questions.get(currentQuestionIndex);
        questionText.setText(currentQuestion[0]);
        questionText.setAlpha(0f);
        questionText.animate().alpha(1f).setDuration(300).start();
        option1.setText(currentQuestion[1]);
        option2.setText(currentQuestion[2]);
        option3.setText(currentQuestion[3]);
        option4.setText(currentQuestion[4]);

        resetOptionColors();
        optionsGroup.clearCheck();
        updateStatus();
    }

    // Checks the selected answer and updates score/lives
    private void checkAnswer() {
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedOption = findViewById(selectedId);
        String selectedText = selectedOption.getText().toString();
        String correctAnswer = questions.get(currentQuestionIndex)[5];

        if (selectedText.equals(correctAnswer)) {
            selectedOption.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            score += 10;
            if (timeLeft > (quizTimeLimit - 5000)) score += 5;
            correctAnswers++;
            correctSound.start();
        } else {
            selectedOption.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            lives--;
            wrongAnswers++;
            wrongSound.start();
        }

        currentQuestionIndex++;
        animateProgressBar();
        loadQuestion();
    }

    // Skips the current question with a penalty
    private void skipQuestion() {
        score -= 5;
        currentQuestionIndex++;
        animateProgressBar();
        loadQuestion();
    }

    // Resets the text color of all options to default
    private void resetOptionColors() {
        option1.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        option2.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        option3.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        option4.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }

    // Animates the progress bar when moving to the next question
    private void animateProgressBar() {
        ObjectAnimator.ofInt(progressBar, "progress", (currentQuestionIndex * 100) / questions.size())
                .setDuration(500)
                .start();
    }

    // Updates the status bar with lives, question number, and score
    private void updateStatus() {
        int remaining = questions.size() - currentQuestionIndex;
        statusText.setText("Lives: " + lives + " | Question: " + (currentQuestionIndex + 1) + "/" + questions.size() +
                " | Remaining: " + remaining + " | Score: " + score);
    }

    // Shows the final result dialog with score and feedback
    private void showResultDialog(String message) {
        SharedPreferences prefs = getSharedPreferences("IQQuest", MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        if (score > highScore) {
            prefs.edit().putInt("highScore", score).apply();
            highScore = score;
        }

        String resultMessage;
        if (score > 70) {
            resultMessage = "Great job! You're a true genius!";
        } else if (score > 40) {
            resultMessage = "Good effort, but you can do better!";
        } else {
            resultMessage = "You need more practice!";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz Completed")
                .setMessage(message + "\n\n" + resultMessage + "\nScore: " + score +
                        "\nHigh Score: " + highScore + "\nCorrect: " + correctAnswers +
                        "\nWrong: " + wrongAnswers)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }
}