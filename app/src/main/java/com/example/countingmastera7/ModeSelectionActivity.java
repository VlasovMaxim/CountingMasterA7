package com.example.countingmastera7;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ModeSelectionActivity extends AppCompatActivity {

    private Button base2Button, base8Button, base16Button;
    private Button backButton;
    private RadioGroup difficultyRadioGroup;
    private RadioButton easyRadioButton, hardRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        TextView titleText = findViewById(R.id.mode_selection_title);
        base2Button = findViewById(R.id.base2_button);
        base8Button = findViewById(R.id.base8_button);
        base16Button = findViewById(R.id.base16_button);
        backButton = findViewById(R.id.back_button);

        // Инициализация RadioGroup и RadioButton для выбора сложности
        difficultyRadioGroup = findViewById(R.id.difficulty_radio_group);
        easyRadioButton = findViewById(R.id.easy_radio_button);
        hardRadioButton = findViewById(R.id.hard_radio_button);

        // Устанавливаем легкий уровень по умолчанию
        easyRadioButton.setChecked(true);

        base2Button.setOnClickListener(v -> startGame(2, "Двоичная"));
        base8Button.setOnClickListener(v -> startGame(8, "Восьмеричная"));
        base16Button.setOnClickListener(v -> startGame(16, "Шестнадцатеричная"));

        backButton.setOnClickListener(v -> finish());
    }

    private void startGame(int base, String baseName) {
        // Определяем выбранный уровень сложности
        int difficulty;
        if (easyRadioButton.isChecked()) {
            difficulty = 1; // Легкий
        } else {
            difficulty = 2; // Сложный
        }

        Intent intent = new Intent(ModeSelectionActivity.this, GameActivity.class);
        intent.putExtra("base", base);
        intent.putExtra("base_name", baseName);
        intent.putExtra("difficulty", difficulty); // Передаем уровень сложности
        startActivity(intent);
        finish();
    }
}