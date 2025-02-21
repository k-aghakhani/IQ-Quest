package com.aghakhani.iq_quest;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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

    // UI components
    private TextView questionText; // TextView to display the current question
    private RadioGroup optionsGroup; // Group of RadioButtons for answer options
    private RadioButton option1, option2, option3, option4; // Individual RadioButtons for answer options
    private Button nextButton; // Button to move to the next question

    // Quiz data
    private String[] questions = { // Array of questions
            "What is the next number in the sequence: 2, 4, 8, 16, ...?",
            "If all Bloops are Razzies and all Razzies are Lazzies, are all Bloops definitely Lazzies?",
            "Which word is the odd one out?",
            "How many squares are there in the given figure? (Imagine a large square with smaller squares inside)",
            "Ali is twice as old as Reza. Five years later, Reza will be 15. How old is Ali now?",
            "If one clock is 3 minutes slow and another is 2 minutes fast, what is the time difference?"
    };

    private String[][] options = { // 2D array of answer options for each question
            {"24", "32", "20", "30"},
            {"Yes", "No", "Maybe", "Cannot be determined"},
            {"Dog", "Cat", "Table", "Bird"},
            {"9", "10", "11", "12"},
            {"30", "25", "35", "40"},
            {"1 minute", "5 minute", "3 minute", "6 minute"}
    };

    private int[] correctAnswers = {1, 0, 2,0,0,1}; // Array of correct answer indices corresponding to each question

    // Quiz state
    private int currentQuestionIndex = 0; // Index of the current question being displayed
    private int score = 0; // User's score

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        nextButton = findViewById(R.id.nextButton);

        // Load the first question
        loadQuestion();

        // Set the click listener for the Next button
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the ID of the selected RadioButton
                int selectedId = optionsGroup.getCheckedRadioButtonId();

                // Check if an option was selected
                if (selectedId == -1) {
                    // Show a message if no answer is selected
                    // Toast.makeText(MainActivity.this, "Please select an answer", Toast.LENGTH_SHORT).show();
                    custom_toast( "Please select an answer");
                    return;
                }

                // Get the index of the selected answer
                int selectedAnswer = optionsGroup.indexOfChild(findViewById(selectedId));

                // Check if the selected answer is correct and update the score
                if (selectedAnswer == correctAnswers[currentQuestionIndex]) {
                    score++;
                }

                // Move to the next question
                currentQuestionIndex++;
                if (currentQuestionIndex < questions.length) {
                    // Load the next question
                    loadQuestion();
                } else {
                    // Show the result dialog when all questions are answered
                    showResultDialog();
                }
            }
        });
    }

    // Load the current question and options into the UI
    private void loadQuestion() {
        questionText.setText(questions[currentQuestionIndex]); // Set the question text
        option1.setText(options[currentQuestionIndex][0]); // Set the first option
        option2.setText(options[currentQuestionIndex][1]); // Set the second option
        option3.setText(options[currentQuestionIndex][2]); // Set the third option
        option4.setText(options[currentQuestionIndex][3]); // Set the fourth option
        optionsGroup.clearCheck(); // Clear any previously selected options
    }

    // Display the result dialog when the quiz is finished
    private void showResultDialog() {
        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_result, null); // Inflate the custom dialog layout
        builder.setView(dialogView); // Set the custom view for the dialog

        AlertDialog dialog = builder.create(); // Create the dialog

        // Get references to dialog components
        TextView title = dialogView.findViewById(R.id.dialogTitle);
        TextView message = dialogView.findViewById(R.id.dialogMessage);
        Button retryButton = dialogView.findViewById(R.id.retryButton);
        Button exitButton = dialogView.findViewById(R.id.exitButton);

        // Set dialog title
        title.setText("Quiz Result");

        // Create a spannable string to highlight the score
        String text = "You scored " + score + " out of " + questions.length + ".";
        SpannableString spannableString = new SpannableString(text);

        // Highlight the score in green
        int start = text.indexOf(String.valueOf(score)); // Find the start position of the score
        int end = start + String.valueOf(score).length(); // Find the end position of the score
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.result_color)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the message text with the highlighted score
        message.setText(spannableString);

        // Set the Retry button action
        retryButton.setOnClickListener(v -> {
            resetQuiz(); // Reset the quiz state
            dialog.dismiss(); // Dismiss the dialog
        });

        // Set the Exit button action
        exitButton.setOnClickListener(v -> finish()); // Close the activity

        // Prevent the dialog from being canceled by tapping outside
        dialog.setCancelable(false);
        dialog.show(); // Show the dialog
    }

    // Reset the quiz to the initial state
    private void resetQuiz() {
        currentQuestionIndex = 0; // Reset the question index
        score = 0; // Reset the score
        loadQuestion(); // Load the first question
    }

    private void custom_toast(String s) {
        LayoutInflater inflater = getLayoutInflater();
        View customToastLayout = inflater.inflate(R.layout.custom_toast, null);
        TextView toastMessage = customToastLayout.findViewById(R.id.toast_message);
        toastMessage.setText(s);
        Toast customToast = new Toast(getApplicationContext());
        customToast.setDuration(Toast.LENGTH_LONG);
        customToast.setView(customToastLayout);
        customToast.show();

    }


}