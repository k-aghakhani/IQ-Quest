package com.aghakhani.iq_quest;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private TextView questionText;
    private RadioGroup optionsGroup;
    private RadioButton option1, option2, option3, option4;
    private Button nextButton;

    private String[][] questions = {
            {"2+2=?", "What is 5+3?", "What comes after A?", "3+4=?", "Which shape has 3 sides?",
                    "What is 1+6?", "If today is Monday, tomorrow is?", "What is 10/2?", "Which is bigger: 5 or 10?", "What color is the sun?"},
            {"12, 24, 48, ?", "What is the square root of 64?", "Which number is prime? 4, 6, 7, 9", "How many angles does a pentagon have?", "What is 15% of 200?",
                    "Find the missing letter: B, D, F, ?", "Which is heavier: 1kg iron or 1kg cotton?", "How many legs does a spider have?", "What is the cube of 3?", "Solve: 2x + 5 = 15"},
            {"What is the next Fibonacci number: 1,1,2,3,5,?", "If 6 people shake hands, how many handshakes?", "Solve: 4x - 7 = 9", "Which shape has the most symmetry?", "What is 7 factorial?",
                    "Which word is the odd one? (Apple, Banana, Carrot, Orange)", "What is the smallest prime number?", "How many prime numbers between 1-20?", "How many diagonals does an octagon have?", "A train travels 60 km/h. How far in 2.5 hours?"}
    };

    private String[][][] options = {
            {{"1", "2", "3", "4"}, {"6", "7", "8", "9"}, {"B", "C", "D", "E"}, {"5", "6", "7", "8"}, {"Square", "Circle", "Triangle", "Hexagon"},
                    {"5", "6", "7", "8"}, {"Sunday", "Tuesday", "Wednesday", "Tuesday"}, {"3", "4", "5", "6"}, {"5", "10", "15", "20"}, {"Blue", "Green", "Yellow", "Red"}},
            {{"96", "72", "64", "80"}, {"6", "7", "8", "9"}, {"4", "6", "7", "9"}, {"3", "4", "5", "6"}, {"20", "25", "30", "35"},
                    {"G", "H", "I", "J"}, {"Iron", "Cotton", "Both equal", "None"}, {"6", "8", "10", "12"}, {"9", "12", "15", "18"}, {"4", "5", "6", "7"}},
            {{"8", "9", "10", "11"}, {"12", "15", "21", "28"}, {"3", "4", "5", "6"}, {"Circle", "Triangle", "Square", "Pentagon"}, {"5040", "40320", "720", "120"},
                    {"Apple", "Banana", "Carrot", "Orange"}, {"1", "2", "3", "5"}, {"4", "5", "6", "7"}, {"10", "15", "20", "25"}, {"100", "120", "150", "180"}}
    };

    private int[][] correctAnswers = {{3, 2, 0, 2, 2, 2, 1, 1, 1, 2}, {2, 2, 2, 2, 0, 1, 2, 1, 2, 1}, {0, 1, 2, 2, 0, 2, 0, 2, 1, 2}};

    private int level = 0;
    private int currentQuestionIndex = 0;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = MediaPlayer.create(this, R.raw.lose_sound);
        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        nextButton = findViewById(R.id.nextButton);

        loadQuestion();

        nextButton.setOnClickListener(v -> {
            int selectedId = optionsGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(MainActivity.this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedAnswer = optionsGroup.indexOfChild(findViewById(selectedId));
            if (selectedAnswer == correctAnswers[level][currentQuestionIndex]) {
                score++;
            }

            currentQuestionIndex++;
            if (currentQuestionIndex < questions[level].length) {
                loadQuestion();
            } else {
                checkLevelProgress();
            }
        });
    }

    private void loadQuestion() {
        questionText.setText(questions[level][currentQuestionIndex]);
        option1.setText(options[level][currentQuestionIndex][0]);
        option2.setText(options[level][currentQuestionIndex][1]);
        option3.setText(options[level][currentQuestionIndex][2]);
        option4.setText(options[level][currentQuestionIndex][3]);
        optionsGroup.clearCheck();
    }

    private void checkLevelProgress() {
        int requiredScore = (level == 0) ? 5 : (level == 1) ? 10 : 11;

        if (score >= requiredScore) {
            if (level == 2) {
                showResultDialog("You won!");
            } else {
                level++;
                currentQuestionIndex = 0;
                score = 0;
                loadQuestion();
            }
        } else {
            level = Math.max(0, level - 1);
            currentQuestionIndex = 0;
            score = 0;
            showResultDialog("Try again from level " + (level + 1));
        }
    }

    private void showResultDialog(String message) {
        mediaPlayer.start();
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> loadQuestion())
                .setCancelable(false)
                .show();
    }
}
