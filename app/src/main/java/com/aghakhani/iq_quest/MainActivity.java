package com.aghakhani.iq_quest;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    private final int quizTimeLimit = 120000; // 120 seconds for the entire quiz
    private long timeLeft; // Tracks remaining time for speed bonus

    private List<String[]> questions = new ArrayList<>(); // Dynamic list to store fetched questions
    private int currentQuestionIndex = 0; // Tracks the current question
    private int score = 0; // Player's total score
    private int correctAnswers = 0; // Count of correct answers
    private int wrongAnswers = 0; // Count of wrong answers
    private int lives = 3; // Player starts with 3 lives

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
                        // Parse the CSV response
                        parseCSV(response);
                        // Shuffle questions after fetching
                        Collections.shuffle(questions);
                        // Start the timer and load the first question
                        startTimer();
                        loadQuestion();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Failed to fetch questions: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Add the request to the queue
        queue.add(stringRequest);
    }

    // Parse CSV data into the questions list
    private void parseCSV(String csvData) {
        String[] lines = csvData.split("\n"); // Split by line
        for (String line : lines) {
            // Split each line by comma, assuming format: Question,Option1,Option2,Option3,Option4,CorrectAnswer
            String[] parts = line.split(",");
            if (parts.length == 6) { // Ensure there are exactly 6 parts (question + 4 options + answer)
                questions.add(parts);
            }
        }
    }

    // Starts the countdown timer for the quiz
    private void startTimer() {
        countDownTimer = new CountDownTimer(quizTimeLimit, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished; // Update remaining time
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerText.setText("Time: " + secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                showResultDialog("Time's up!"); // Show result when time runs out
            }
        }.start();
    }

    // Loads the current question and options into the UI
    private void loadQuestion() {
        if (currentQuestionIndex >= questions.size() || lives <= 0) {
            countDownTimer.cancel();
            showResultDialog(lives <= 0 ? "No lives left!" : "Quiz Completed!");
            return;
        }

        String[] currentQuestion = questions.get(currentQuestionIndex);
        questionText.setText(currentQuestion[0]);
        // Fade-in animation for question text
        questionText.setAlpha(0f);
        questionText.animate().alpha(1f).setDuration(300).start();
        option1.setText(currentQuestion[1]);
        option2.setText(currentQuestion[2]);
        option3.setText(currentQuestion[3]);
        option4.setText(currentQuestion[4]);

        resetOptionColors(); // Reset option colors to default
        optionsGroup.clearCheck(); // Clear selected option
        updateStatus(); // Update status bar
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
            if (timeLeft > (quizTimeLimit - 5000)) score += 5; // Speed bonus
            correctAnswers++;
            correctSound.start();
        } else {
            selectedOption.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            lives--;
            wrongAnswers++;
            wrongSound.start();
        }

        currentQuestionIndex++;
        animateProgressBar(); // Animate progress bar update
        loadQuestion(); // Load next question
    }

    // Skips the current question with a penalty
    private void skipQuestion() {
        score -= 5; // Penalty for skipping
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