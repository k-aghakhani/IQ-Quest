package com.aghakhani.iq_quest;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView questionText, statusText, timerText;
    private RadioGroup optionsGroup;
    private RadioButton option1, option2, option3, option4;
    private Button nextButton;
    private ProgressBar progressBar;
    private CountDownTimer countDownTimer;
    private final int quizTimeLimit = 120000; // 120 seconds

    private String[][] questions = {
            {"What is 5 + 3?", "6", "7", "8", "9", "8"},
            {"Which is a fruit?", "Carrot", "Potato", "Apple", "Onion", "Apple"},
            {"How many legs does a cat have?", "2", "3", "4", "5", "4"},
            {"What is 12 / 4?", "2", "3", "4", "6", "3"},
            {"What color is an emerald?", "Red", "Green", "Blue", "Yellow", "Green"},
            {"Who wrote 'Hamlet'?", "Shakespeare", "Dickens", "Hemingway", "Tolkien", "Shakespeare"},
            {"Find the missing number: 2, 6, 12, 20, ?", "28", "30", "32", "36", "30"},
            {"What comes next: A, C, E, G, ?", "H", "I", "J", "K", "I"},
            {"If a train moves at 90 km/h, how far does it travel in 2.5 hours?", "180 km", "200 km", "225 km", "250 km", "225 km"}
    };

    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        questionText = findViewById(R.id.questionText);
        statusText = findViewById(R.id.statusText);
        timerText = findViewById(R.id.timerText);
        optionsGroup = findViewById(R.id.optionsGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        nextButton = findViewById(R.id.nextButton);
        progressBar = findViewById(R.id.progressBar);

        startTimer();
        loadQuestion();

        nextButton.setOnClickListener(v -> checkAnswer());
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(quizTimeLimit, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerText.setText("Time: " + secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                showResultDialog("Time's up!");
            }
        }.start();
    }

    private void loadQuestion() {
        if (currentQuestionIndex >= questions.length) {
            countDownTimer.cancel();
            showResultDialog("Quiz Completed!");
            return;
        }

        String[] currentQuestion = questions[currentQuestionIndex];
        questionText.setText(currentQuestion[0]);
        option1.setText(currentQuestion[1]);
        option2.setText(currentQuestion[2]);
        option3.setText(currentQuestion[3]);
        option4.setText(currentQuestion[4]);

        optionsGroup.clearCheck();
        updateStatus();
    }

    private void checkAnswer() {
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedOption = findViewById(selectedId);
        String selectedText = selectedOption.getText().toString();
        String correctAnswer = questions[currentQuestionIndex][5];

        if (selectedText.equals(correctAnswer)) {
            score += 10;
            correctAnswers++;
        } else {
            wrongAnswers++;
        }

        currentQuestionIndex++;
        progressBar.setProgress((currentQuestionIndex * 100) / questions.length);
        loadQuestion();
    }

    private void updateStatus() {
        int remaining = questions.length - currentQuestionIndex;
        statusText.setText("Question: " + (currentQuestionIndex + 1) + "/" + questions.length +
                " | Remaining: " + remaining + " | Score: " + score);
    }

    private void showResultDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz Completed")
                .setMessage(message + "\n\nScore: " + score +
                        "\nCorrect: " + correctAnswers +
                        "\nWrong: " + wrongAnswers)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }
}
