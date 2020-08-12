package com.example.thefactorfactor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.thefactorfactor.databinding.ActivityGameBinding;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
    private static final String defaultBack = "#fafafa", greenBack = "#388e3c", redBack = "#d32f2f";
    private final long START_TIME_IN_MILLIS = 10000;

    private ActivityGameBinding binding;

    private int locationOfAnswer, correctAnswer, totalCorrect = 0, total = 0, winStreak = 0, longestWinStreak;
    private long timeLeftInMillis = START_TIME_IN_MILLIS;
    private boolean firstTime = true, editTextEnabled = true, okButtonEnabled = true, timerRunning = false;
    private String resultString, backgroundColor;
    private int[] options = new int[3];

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
        outState.putIntArray("options", options);

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
        options = savedInstanceState.getIntArray("options");

        if (!firstTime) {
            binding.setOptionsVisible(true);
            binding.setOptions(options);
            binding.setResult(resultString);
            binding.gameLayout.setBackgroundColor(Color.parseColor(backgroundColor));
        }

        binding.setTotalCorrect(totalCorrect);
        binding.setTotal(total);

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game);

        setUpLayout();

        random = new Random();

        sharedPreferences = GameActivity.this.getSharedPreferences("pref", MODE_PRIVATE);
        longestWinStreak = sharedPreferences.getInt("longestWinStreak", 0);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


    }

    public void setUpLayout() {
        binding.setTotalCorrect(totalCorrect);
        binding.setTotal(total);
        binding.setTimeLeft(10);
        binding.setResult("");
    }

    public void updateEditText(boolean enabled) {
        editTextEnabled = enabled;
        binding.etNumber.setEnabled(editTextEnabled);
        binding.etNumber.setFocusableInTouchMode(editTextEnabled);
    }

    public void updateOkButton(boolean enabled) {
        okButtonEnabled = enabled;
        float okButtonAlpha = 1;
        if (okButtonEnabled)
            okButtonAlpha = 1;
        else
            okButtonAlpha = (float) 0.5;
        binding.okBt.setEnabled(okButtonEnabled);
        binding.okBt.setAlpha(okButtonAlpha);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                binding.setTimeLeft((int) (timeLeftInMillis / 1000));
                Log.i(TAG, "onTick: Time Left:" + (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "Timer Finished");
                showResults(findViewById(R.id.endGameBt));
            }
        }.start();
        timerRunning = true;
    }

    public void submitNumber(View view) {
        String numberString = binding.etNumber.getText().toString();
        if (numberString.isEmpty()) {
            Toast.makeText(GameActivity.this, "Enter a number to Play", Toast.LENGTH_SHORT).show();
        } else if (numberString.equals("1") || numberString.equals("0")) {
            Toast.makeText(GameActivity.this, "That's Cheating", Toast.LENGTH_SHORT).show();
        } else {
            updateEditText(false);
            updateOkButton(false);
            startTimer();
            Log.i(TAG, "onClick: Countdown timer started");
            binding.tvTimer.setVisibility(View.VISIBLE);
            if (firstTime) {
                binding.setOptionsVisible(true);
                firstTime = false;
            }
            generateOptions(Integer.parseInt(numberString));
        }
    }

    public void checkAnswer(View view) {
        if (locationOfAnswer != -1) {
            updateEditText(true);
            updateOkButton(true);
            binding.etNumber.getText().clear();
            total++;
            if (Integer.parseInt(view.getTag().toString()) == locationOfAnswer) {
                totalCorrect++;
                backgroundColor = greenBack;
                binding.gameLayout.setBackgroundColor(Color.parseColor(backgroundColor));
                resultString = "Correct";
                binding.setResult(resultString);
                winStreak++;
                if (winStreak > longestWinStreak) {
                    longestWinStreak = winStreak;
                    sharedPreferences.edit().putInt("longestWinStreak", longestWinStreak).apply();
                }
            } else {
                winStreak = 0;
                backgroundColor = redBack;
                binding.gameLayout.setBackgroundColor(Color.parseColor(backgroundColor));
                resultString = "Wrong.Correct answer is " + correctAnswer;
                binding.setResult(resultString);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                else
                    vibrator.vibrate(500);
            }
            countDownTimer.cancel();
            timeLeftInMillis = START_TIME_IN_MILLIS;
            timerRunning = false;
            Log.i(TAG, "Timer Canceled");
            binding.setTimeLeft(10);
            binding.setTotalCorrect(totalCorrect);
            binding.setTotal(total);
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
                    options[i] = correctAnswer;
                } else {
                    wrongAnswer = random.nextInt(n + 10) - 10;
                    while (wrongAnswer == 0 || n % wrongAnswer == 0 || x == wrongAnswer) {
                        wrongAnswer = random.nextInt(n + 10) - 10;
                    }
                    x = wrongAnswer;
                    options[i] = wrongAnswer;
                }
            }
        } else {
            correctAnswer = generateRandomFactor(n);
            for (int i = 0; i < 3; i++) {
                if (i == locationOfAnswer) {
                    options[i] = correctAnswer;
                } else {
                    wrongAnswer = random.nextInt(n) + 1;
                    while (n % wrongAnswer == 0 || x == wrongAnswer) {
                        wrongAnswer = random.nextInt(n) + 1;
                    }
                    x = wrongAnswer;
                    options[i] = wrongAnswer;
                }
            }
        }
        binding.setOptions(options);
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
