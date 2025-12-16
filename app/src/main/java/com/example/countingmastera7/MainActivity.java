package com.example.countingmastera7;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.start_button);
        Button statsButton = findViewById(R.id.stats_button);
        Button exitButton = findViewById(R.id.exit_button);

        startButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ModeSelectionActivity.class));
        });

        statsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
        });

        exitButton.setOnClickListener(v -> finish());
    }
}
