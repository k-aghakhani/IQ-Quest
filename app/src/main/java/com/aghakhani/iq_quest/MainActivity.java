package com.aghakhani.iq_quest;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView questionText, statusBar;
    private RadioGroup optionsGroup;
    private RadioButton option1, option2, option3, option4;
    private Button nextButton;
    private ProgressBar progressBar;

    private String[][][] allQuestions = {
            {
                    {"What is 2 + 2?", "3", "4", "5", "6", "1"},
                    {"What is 3 + 5?", "5", "6", "8", "9", "2"},
                    {"What is 10 - 6?", "3", "4", "5", "6", "2"}
            },
            {
                    {"If 5x = 25, what is x?", "2", "3", "5", "6", "3"},
                    {"What comes next: 2, 4, 8, 16, ...", "20", "32", "24", "30", "2"},
                    {"Ali is twice as old as Reza. If Reza is 10, how old is Ali?", "15", "20", "25", "30", "2"}
            },
            {
                    {"If A > B and B > C, which is true?", "A > C", "A < C", "A = C", "Cannot determine", "1"},
                    {"Which shape has 4 equal sides?", "Triangle", "Rectangle", "Square", "Circle", "3"},
                    {"What is the next Fibonacci number: 1, 1, 2, 3, 5, ...?", "7", "8", "9", "10", "2"}
            }
    };

    private int currentLevel = 0;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int requiredScores[] = {2, 2, 2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        questionText = findViewById(R.id.questionText);
        statusBar = findViewById(R.id.statusBar);
        optionsGroup = findViewById(R.id.optionsGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        nextButton = findViewById(R.id.nextButton);
        progressBar = findViewById(R.id.progressBar);

        loadQuestion();

        nextButton.setOnClickListener(v -> {
            int selectedId = optionsGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedAnswer = optionsGroup.indexOfChild(findViewById(selectedId));
            int correctAnswer = Integer.parseInt(allQuestions[currentLevel][currentQuestionIndex][5]) - 1;

            if (selectedAnswer == correctAnswer) {
                score++;
            }

            currentQuestionIndex++;

            if (currentQuestionIndex < allQuestions[currentLevel].length) {
                loadQuestion();
            } else {
                checkProgress();
            }
        });
    }

    private void loadQuestion() {
        String[] questionData = allQuestions[currentLevel][currentQuestionIndex];
        questionText.setText(questionData[0]);
        option1.setText(questionData[1]);
        option2.setText(questionData[2]);
        option3.setText(questionData[3]);
        option4.setText(questionData[4]);
        optionsGroup.clearCheck();

        statusBar.setText("Question: " + (currentQuestionIndex + 1) + " / " + allQuestions[currentLevel].length + "  |  Score: " + score);
        progressBar.setProgress((currentQuestionIndex + 1) * 100 / allQuestions[currentLevel].length);
    }

    private void checkProgress() {
        if (score >= requiredScores[currentLevel]) {
            if (currentLevel == allQuestions.length - 1) {
                showResultDialog("Congratulations! You won the game!");
            } else {
                currentLevel++;
                currentQuestionIndex = 0;
                score = 0;
                loadQuestion();
            }
        } else {
            showResultDialog("You didn't pass. Restarting previous level.");
        }
    }

    private void showResultDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over");
        builder.setMessage(message);
        builder.setPositiveButton("Retry", (dialog, which) -> {
            currentQuestionIndex = 0;
            score = 0;
            loadQuestion();
        });
        builder.setNegativeButton("Exit", (dialog, which) -> finish());
        builder.setCancelable(false);
        builder.show();
    }
}