package com.example.countingmastera7;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private TextView scoreText, correctText, lastLevelText;
    private Button backToMenuButton, playAgainButton;
    private LinearLayout historyContainer;
    private StatsManager statsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        scoreText = findViewById(R.id.score_text);
        correctText = findViewById(R.id.correct_text);
        lastLevelText = findViewById(R.id.last_level_text);
        backToMenuButton = findViewById(R.id.back_to_menu_button);
        playAgainButton = findViewById(R.id.play_again_button);
        historyContainer = findViewById(R.id.history_container);

        statsManager = new StatsManager(this);

        // Получаем данные из Intent от GameActivity (если есть)
        Intent intent = getIntent();
        if (intent.hasExtra("score_percent")) {
            int scorePercent = intent.getIntExtra("score_percent", 0);
            int correctAnswers = intent.getIntExtra("correct_answers", 0);
            int totalQuestions = intent.getIntExtra("total_questions", 0);
            int base = intent.getIntExtra("base", 2);
            int difficulty = intent.getIntExtra("difficulty", 1);
            String baseName = getBaseName(base);
            String difficultyName = getDifficultyName(difficulty);

            scoreText.setText("Результат: " + scorePercent + "%");
            correctText.setText("Правильных ответов: " + correctAnswers + "/" + totalQuestions);
            lastLevelText.setText("Система: " + base + " (" + baseName + ") | Сложность: " + difficultyName);
        } else {
            // Если открыто из главного меню, показываем последнюю игру
            List<StatsManager.GameStats> history = statsManager.getGameHistory();
            if (!history.isEmpty()) {
                StatsManager.GameStats lastGame = history.get(0);
                scoreText.setText("Результат: " + lastGame.scorePercent + "%");
                correctText.setText("Правильных ответов: " + lastGame.correctAnswers + "/" + lastGame.totalQuestions);
                String baseName = getBaseName(lastGame.base);
                String difficultyName = getDifficultyName(lastGame.difficulty);
                lastLevelText.setText("Система: " + lastGame.base + " (" + baseName + ") | Сложность: " + difficultyName);
            } else {
                scoreText.setText("Результат: -");
                correctText.setText("Правильных ответов: -");
                lastLevelText.setText("Система счисления: - | Сложность: -");
            }
        }

        displayHistory();

        backToMenuButton.setOnClickListener(v -> {
            Intent mainIntent = new Intent(StatisticsActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });

        playAgainButton.setOnClickListener(v -> {
            Intent gameIntent = new Intent(StatisticsActivity.this, GameActivity.class);
            // Получаем систему счисления и сложность из Intent или из последней игры
            if (intent.hasExtra("base")) {
                int base = intent.getIntExtra("base", 2);
                int difficulty = intent.getIntExtra("difficulty", 1);
                String baseName = getBaseName(base);
                gameIntent.putExtra("base", base);
                gameIntent.putExtra("base_name", baseName);
                gameIntent.putExtra("difficulty", difficulty);
            } else {
                // Если открыто из главного меню, используем последнюю игру
                List<StatsManager.GameStats> history = statsManager.getGameHistory();
                if (!history.isEmpty()) {
                    StatsManager.GameStats lastGame = history.get(0);
                    String baseName = getBaseName(lastGame.base);
                    gameIntent.putExtra("base", lastGame.base);
                    gameIntent.putExtra("base_name", baseName);
                    gameIntent.putExtra("difficulty", lastGame.difficulty);
                }
            }
            startActivity(gameIntent);
            finish();
        });
    }

    private void displayHistory() {
        historyContainer.removeAllViews();

        List<StatsManager.GameStats> history = statsManager.getGameHistory();

        if (history.isEmpty()) {
            TextView noHistoryText = new TextView(this);
            noHistoryText.setText("История игр пуста");
            noHistoryText.setTextColor(0xFFFFFFFF);
            noHistoryText.setTextSize(16f);
            noHistoryText.setPadding(0, 16, 0, 16);
            historyContainer.addView(noHistoryText);
            return;
        }

        for (StatsManager.GameStats stats : history) {
            View historyItem = createHistoryItemView(stats);
            historyContainer.addView(historyItem);
        }
    }

    private View createHistoryItemView(StatsManager.GameStats stats) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(16, 12, 16, 12);
        itemLayout.setBackgroundResource(R.drawable.history_item_background);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 12);
        itemLayout.setLayoutParams(params);

        TextView gameNumberText = new TextView(this);
        gameNumberText.setText("Игра #" + stats.gameNumber + " - " + stats.date);
        gameNumberText.setTextColor(0xFFFFFFFF);
        gameNumberText.setTextSize(16f);
        gameNumberText.setTypeface(null, Typeface.BOLD);
        itemLayout.addView(gameNumberText);

        TextView scoreText = new TextView(this);
        String baseName = getBaseName(stats.base);
        String difficultyName = getDifficultyName(stats.difficulty);
        scoreText.setText("Результат: " + stats.scorePercent + "% | " +
                stats.correctAnswers + "/" + stats.totalQuestions + " правильных | " +
                "Система: " + stats.base + " (" + baseName + ") | " +
                "Сложность: " + difficultyName);
        scoreText.setTextColor(0xFFE0E0E0);
        scoreText.setTextSize(14f);
        scoreText.setPadding(0, 4, 0, 0);
        itemLayout.addView(scoreText);

        return itemLayout;
    }

    private String getBaseName(int base) {
        switch (base) {
            case 2:
                return "двоичная";
            case 8:
                return "восьмеричная";
            case 16:
                return "шестнадцатеричная";
            default:
                return "неизвестная";
        }
    }

    private String getDifficultyName(int difficulty) {
        switch (difficulty) {
            case 1:
                return "легкий";
            case 2:
                return "сложный";
            default:
                return "неизвестная";
        }
    }
}