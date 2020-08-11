package com.example.thefactorfactor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
    private final long START_TIME_IN_MILLIS = 10000;
    private final String defaultBack = "#fafafa", greenBack = "#388e3c", redBack = "#d32f2f";
    private int locationOfAnswer, correctAnswer, totalCorrect, total, winStreak = 0, longestWinStreak;
    private long timeLeftInMillis = START_TIME_IN_MILLIS;
    private float okButtonAlpha = 1;
    private boolean firstTime = true, editTextEnabled = true, okButtonEnabled = true, timerRunning = false;
    private String resultString, backgroundColor;
    private ArrayList<Integer> options;
    private ConstraintLayout layout;
    private TextView tvScore, tvTimer, tvResult;
    private EditText etNumber;
    private Button btOk, btOp1, btOp2, btOp3;
    private Random random;
    private SharedPreferences sharedPreferences;
    private Vibrator vibrator;
    private CountDownTimer countDownTimer;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("locationOfAnswer", locationOfAnswer);
        outState.putInt("correctAnswer", correctAnswer);
        outState.putInt("totalCorrect", totalCorrect);
        outState.putInt("total", total);
        outState.putInt("winStreak", winStreak);
        outState.putLong("timeLeftInMillis", timeLeftInMillis);
        outState.putBoolean("firstTime", firstTime);
        outState.putBoolean("editTextEnabled", editTextEnabled);
        outState.putBoolean("okButtonEnabled", okButtonEnabled);
        outState.putBoolean("timerRunning", timerRunning);
        outState.putString("resultString", resultString);
        outState.putString("backgroundColor", backgroundColor);
        outState.putIntegerArrayList("options", options);

        if (timerRunning) {
            countDownTimer.cancel();
            Log.i(TAG, "onSaveInstanceState: countDownTimer canceled");
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        locationOfAnswer = savedInstanceState.getInt("locationOfAnswer", -1);
        correctAnswer = savedInstanceState.getInt("correctAnswer", 0);
        totalCorrect = savedInstanceState.getInt("totalCorrect", 0);
        total = savedInstanceState.getInt("total", 0);
        winStreak = savedInstanceState.getInt("winStreak", 0);
        timeLeftInMillis = savedInstanceState.getLong("timeLeftInMillis", timeLeftInMillis);
        firstTime = savedInstanceState.getBoolean("firstTime", true);
        editTextEnabled = savedInstanceState.getBoolean("editTextEnabled", true);
        okButtonEnabled = savedInstanceState.getBoolean("okButtonEnabled", true);
        timerRunning = savedInstanceState.getBoolean("timerRunning", false);
        resultString = savedInstanceState.getString("resultString");
        backgroundColor = savedInstanceState.getString("backgroundColor", defaultBack);
        options = savedInstanceState.getIntegerArrayList("options");

        if (!firstTime) {
            btOp1.setVisibility(View.VISIBLE);
            btOp2.setVisibility(View.VISIBLE);
            btOp3.setVisibility(View.VISIBLE);
            btOp1.setText(String.valueOf(options.get(0)));
            btOp2.setText(String.valueOf(options.get(1)));
            btOp3.setText(String.valueOf(options.get(2)));
            tvResult.setText(resultString);
            layout.setBackgroundColor(Color.parseColor(backgroundColor));
        }

        tvScore.setText("Score : " + totalCorrect + "/" + total);

        updateEditText(editTextEnabled);
        updateOkButton(okButtonEnabled);

        if (timerRunning) {
            startTimer();
            Log.i(TAG, "onRestoreInstanceState: Countdown timer started");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        setUpLayout();

        random = new Random();

        options = new ArrayList<>();
        options.add(0);
        options.add(0);
        options.add(0);

        sharedPreferences = GameActivity.this.getSharedPreferences("pref", MODE_PRIVATE);
        longestWinStreak = sharedPreferences.getInt("longestWinStreak", 0);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setUpLayout() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)//Device is in Portrait Mode
            layout = findViewById(R.id.gameLayoutPortrait);
        else                                                  //Device is in Landscape Mode
            layout = findViewById(R.id.gameLayoutLand);
        btOk = findViewById(R.id.btOk);
        btOp1 = findViewById(R.id.bt1);
        btOp2 = findViewById(R.id.bt2);
        btOp3 = findViewById(R.id.bt3);
        etNumber = findViewById(R.id.etNumber);
        tvResult = findViewById(R.id.tvResult);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
    }

    public void updateEditText(boolean enabled) {
        editTextEnabled = enabled;
        etNumber.setEnabled(editTextEnabled);
        etNumber.setFocusableInTouchMode(editTextEnabled);
    }

    public void updateOkButton(boolean enabled) {
        okButtonEnabled = enabled;
        if (okButtonEnabled)
            okButtonAlpha = 1;
        else
            okButtonAlpha = (float) 0.5;
        btOk.setEnabled(okButtonEnabled);
        btOk.setAlpha(okButtonAlpha);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                tvTimer.setText("Time Left : " + (timeLeftInMillis / 1000) + "s");
                Log.i(TAG, "onTick: Time Left:" + (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "Timer Finished");
                showResults(findViewById(R.id.btEndGame));
            }
        }.start();
        timerRunning = true;
    }

    public void submitNumber(View view) {
        String numberString = etNumber.getText().toString();
        if (numberString.isEmpty()) {
            Toast.makeText(GameActivity.this, "Enter a number to Play", Toast.LENGTH_SHORT).show();
        } else if (numberString.equals("1") || numberString.equals("0")) {
            Toast.makeText(GameActivity.this, "That's Cheating", Toast.LENGTH_SHORT).show();
        } else {
            updateEditText(false);
            updateOkButton(false);
            startTimer();
            Log.i(TAG, "onClick: Countdown timer started");
            tvTimer.setVisibility(View.VISIBLE);
            if (firstTime) {
                btOp1.setVisibility(View.VISIBLE);
                btOp2.setVisibility(View.VISIBLE);
                btOp3.setVisibility(View.VISIBLE);
                firstTime = false;
            }
            generateOptions(Integer.parseInt(numberString));
        }
    }

    public void checkAnswer(View view) {
        if (locationOfAnswer != -1) {
            updateEditText(true);
            updateOkButton(true);
            etNumber.getText().clear();
            total++;
            if (Integer.parseInt(view.getTag().toString()) == locationOfAnswer) {
                totalCorrect++;
                backgroundColor = greenBack;
                layout.setBackgroundColor(Color.parseColor(backgroundColor));
                resultString = "Correct";
                tvResult.setText(resultString);
                winStreak++;
                if (winStreak > longestWinStreak) {
                    longestWinStreak = winStreak;
                    sharedPreferences.edit().putInt("longestWinStreak", longestWinStreak).apply();
                }
            } else {
                winStreak = 0;
                backgroundColor = redBack;
                layout.setBackgroundColor(Color.parseColor(backgroundColor));
                resultString = "Wrong.Correct answer is " + correctAnswer;
                tvResult.setText(resultString);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                else
                    vibrator.vibrate(500);
            }
            countDownTimer.cancel();
            timeLeftInMillis = START_TIME_IN_MILLIS;
            timerRunning = false;
            Log.i(TAG, "Timer Canceled");
            tvTimer.setText("Time Left : 10s");
            tvScore.setText("Score : " + totalCorrect + "/" + total);
            locationOfAnswer = -1;
        }
    }

    public void generateOptions(int n) {
        int x = 0;
        int wrongAnswer;
        locationOfAnswer = random.nextInt(3);
        if (n <= 4) {
            correctAnswer = n;
            for (int i = 0; i < 3; i++) {
                if (i == locationOfAnswer) {
                    options.set(i, correctAnswer);
                } else {
                    wrongAnswer = random.nextInt(n + 10) - 10;
                    while (wrongAnswer == 0 || n % wrongAnswer == 0 || x == wrongAnswer) {
                        wrongAnswer = random.nextInt(n + 10) - 10;
                    }
                    x = wrongAnswer;
                    options.set(i, wrongAnswer);
                }
            }
        } else {
            correctAnswer = generateRandomFactor(n);
            for (int i = 0; i < 3; i++) {
                if (i == locationOfAnswer) {
                    options.set(i, correctAnswer);
                } else {
                    wrongAnswer = random.nextInt(n) + 1;
                    while (n % wrongAnswer == 0 || x == wrongAnswer) {
                        wrongAnswer = random.nextInt(n) + 1;
                    }
                    x = wrongAnswer;
                    options.set(i, wrongAnswer);
                }
            }
        }
        btOp1.setText(String.valueOf(options.get(0)));
        btOp2.setText(String.valueOf(options.get(1)));
        btOp3.setText(String.valueOf(options.get(2)));
    }

    public int generateRandomFactor(int n) {
        int randomFactor;
        ArrayList<Integer> factors = new ArrayList<>();
        for (int i = 2; i < n; i++) {
            if (n % i == 0) {
                factors.add(i);
            }
        }
        if (factors.isEmpty()) {
            return n;
        } else {
            randomFactor = factors.get(random.nextInt(factors.size()));
            while (randomFactor == n) {
                randomFactor = factors.get(random.nextInt(factors.size()));
            }
            return randomFactor;
        }
    }

    public void showResults(View view) {
        Intent intent = new Intent(GameActivity.this, ResultsActivity.class);
        intent.putExtra("total", total);
        intent.putExtra("totalCorrect", totalCorrect);
        intent.putExtra("longestWinStreak", longestWinStreak);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
