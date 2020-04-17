package com.example.thefactorfactor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        TextView tvScore = findViewById(R.id.tvScore2);
        TextView tvWinStreak = findViewById(R.id.tvWinStreak);

        int total = getIntent().getIntExtra("total", -1);
        int totalCorrect = getIntent().getIntExtra("totalCorrect", -1);
        int longestWinStreak = getIntent().getIntExtra("longestWinStreak", -1);

        tvScore.setText("Score : "+ totalCorrect +"/"+ total);
        tvWinStreak.setText("Longest Win Streak : "+ longestWinStreak);
    }

    public void restartGame(View view){
        startActivity(new Intent(ResultsActivity.this,GameActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(ResultsActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
