package com.example.thefactorfactor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.thefactorfactor.databinding.ActivityResultsBinding;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_results);

        int totalCorrect = getIntent().getIntExtra("totalCorrect", -1);
        int total = getIntent().getIntExtra("total", -1);
        int longestWinStreak = getIntent().getIntExtra("longestWinStreak", -1);

        binding.setTotalCorrect(totalCorrect);
        binding.setTotal(total);
        binding.setLongestWinStreak(longestWinStreak);
    }

    public void restartGame(View view) {
        startActivity(new Intent(ResultsActivity.this, GameActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
