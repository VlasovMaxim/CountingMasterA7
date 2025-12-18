package com.example.countingmastera7;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private static final int TOTAL_QUESTIONS = 10;
    private static final int MAX_LIVES_EASY = 4; // Легкий: 4 жизни
    private static final int MAX_LIVES_HARD = 3; // Сложный: 3 жизни
    private static final long TIME_EASY = 45000; // 45 секунд для легкого уровня
    private static final long TIME_HARD = 20000; // 20 секунд для сложного уровня

    private TextView questionCounterText, timerText, baseText, questionText, feedbackText;
    private TextView heart1, heart2, heart3, heart4; // Добавили четвертое сердечко
    private Button answerButton1, answerButton2, answerButton3, answerButton4, checkButton;

    private int currentQuestionIndex = 1;
    private int maxLives; // Будет установлено в зависимости от сложности
    private int livesLeft;
    private int selectedAnswerIndex = -1; // 0..3
    private int correctAnswerIndex = -1;
    private int base; // Система счисления (2, 8, 16)
    private String baseName;
    private int difficulty; // 1 - легкий, 2 - сложный

    private CountDownTimer countDownTimer;
    private Random random = new Random();

    private int totalCorrectAnswers = 0;
    private int totalQuestionsAnswered = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initViews();

        // Получаем систему счисления и уровень сложности из Intent
        Intent intent = getIntent();
        if (intent.hasExtra("base") && intent.hasExtra("base_name")) {
            base = intent.getIntExtra("base", 2);
            baseName = intent.getStringExtra("base_name");
            difficulty = intent.getIntExtra("difficulty", 1); // По умолчанию легкий
        } else {
            // Если не передано, используем двоичную по умолчанию и легкий уровень
            base = 2;
            baseName = "Двоичная";
            difficulty = 1;
        }

        startNewGame();
    }

    private void initViews() {
        questionCounterText = findViewById(R.id.question_counter_text);
        timerText = findViewById(R.id.timer_text);
        baseText = findViewById(R.id.base_text);
        questionText = findViewById(R.id.question_text);
        feedbackText = findViewById(R.id.feedback_text);

        heart1 = findViewById(R.id.heart1);
        heart2 = findViewById(R.id.heart2);
        heart3 = findViewById(R.id.heart3);
        heart4 = findViewById(R.id.heart4); // Новое сердечко

        answerButton1 = findViewById(R.id.answer_button_1);
        answerButton2 = findViewById(R.id.answer_button_2);
        answerButton3 = findViewById(R.id.answer_button_3);
        answerButton4 = findViewById(R.id.answer_button_4);
        checkButton = findViewById(R.id.check_button);

        answerButton1.setOnClickListener(v -> selectAnswer(0));
        answerButton2.setOnClickListener(v -> selectAnswer(1));
        answerButton3.setOnClickListener(v -> selectAnswer(2));
        answerButton4.setOnClickListener(v -> selectAnswer(3));

        checkButton.setOnClickListener(v -> checkAnswer());
    }

    private void startNewGame() {
        currentQuestionIndex = 1;

        // Устанавливаем количество жизней в зависимости от сложности
        if (difficulty == 1) {
            maxLives = MAX_LIVES_EASY; // 4 жизни для легкого
        } else {
            maxLives = MAX_LIVES_HARD; // 3 жизни для сложного
        }
        livesLeft = maxLives;

        totalCorrectAnswers = 0;
        totalQuestionsAnswered = 0;

        String difficultyText = (difficulty == 1) ? "легкий" : "сложный";
        baseText.setText("Система: " + base + " (" + baseName.toLowerCase() + ") | Сложность: " + difficultyText);

        // Настраиваем отображение сердечек
        setupHeartsUI();
        updateLivesUI();
        startNewQuestion();
    }

    private void setupHeartsUI() {
        if (difficulty == 1) {
            // Для легкого уровня показываем 4 сердечка
            heart4.setVisibility(android.view.View.VISIBLE);
        } else {
            // Для сложного уровня скрываем 4-е сердечко
            heart4.setVisibility(android.view.View.GONE);
        }
    }

    private void startNewQuestion() {
        feedbackText.setText("");
        selectedAnswerIndex = -1;
        resetAnswerButtonsBackground();

        questionCounterText.setText("Вопрос " + currentQuestionIndex + "/" + TOTAL_QUESTIONS);

        startTimer();
        generateQuestion();
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();

        long timePerQuestion = (difficulty == 1) ? TIME_EASY : TIME_HARD;

        countDownTimer = new CountDownTimer(timePerQuestion, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("Время: " + (millisUntilFinished / 1000) + "с");
            }

            @Override
            public void onFinish() {
                timerText.setText("Время: 0с");
                handleWrongAnswer("Время вышло!");
            }
        };
        countDownTimer.start();
    }

    private void resetAnswerButtonsBackground() {
        Button[] buttons = {answerButton1, answerButton2, answerButton3, answerButton4};
        for (Button button : buttons) {
            button.setBackground(ContextCompat.getDrawable(this, R.drawable.button_gradient_blue));
            button.setTextColor(Color.WHITE);
        }
    }

    private void selectAnswer(int index) {
        selectedAnswerIndex = index;
        resetAnswerButtonsBackground();
        Button[] buttons = {answerButton1, answerButton2, answerButton3, answerButton4};
        buttons[index].setBackground(ContextCompat.getDrawable(this, R.drawable.button_answer_yellow_selected));
        buttons[index].setTextColor(Color.BLACK);
    }

    private void checkAnswer() {
        if (selectedAnswerIndex == -1) {
            feedbackText.setText("Выбери ответ!");
            return;
        }

        if (countDownTimer != null) countDownTimer.cancel();

        if (selectedAnswerIndex == correctAnswerIndex) {
            totalCorrectAnswers++;
            feedbackText.setText("Правильно!");
            goNextQuestion();
        } else {
            handleWrongAnswer("Неправильно!");
        }
    }

    private void handleWrongAnswer(String baseMessage) {
        livesLeft--;
        updateLivesUI();

        String correctText = "";
        Button[] buttons = {answerButton1, answerButton2, answerButton3, answerButton4};
        if (correctAnswerIndex >= 0 && correctAnswerIndex < 4) {
            correctText = " Правильный ответ: " + buttons[correctAnswerIndex].getText();
        }
        feedbackText.setText(baseMessage + correctText);

        if (livesLeft <= 0) {
            endGame();
        } else {
            goNextQuestion();
        }
    }

    private void updateLivesUI() {
        TextView[] hearts;
        if (difficulty == 1) {
            // Для легкого уровня используем 4 сердечка
            hearts = new TextView[]{heart1, heart2, heart3, heart4};
        } else {
            // Для сложного уровня используем 3 сердечка
            hearts = new TextView[]{heart1, heart2, heart3};
        }

        for (int i = 0; i < maxLives; i++) {
            if (i < livesLeft) {
                hearts[i].setText("❤️");
            } else {
                hearts[i].setText("♡");
            }
        }
    }

    private void goNextQuestion() {
        questionText.postDelayed(() -> {
            totalQuestionsAnswered++;
            currentQuestionIndex++;
            if (currentQuestionIndex > TOTAL_QUESTIONS) {
                // Все вопросы пройдены
                endGame();
                return;
            }
            startNewQuestion();
        }, 1200);
    }

    private void endGame() {
        if (countDownTimer != null) countDownTimer.cancel();

        int scorePercent = totalQuestionsAnswered > 0
                ? (int) ((totalCorrectAnswers * 100.0f) / totalQuestionsAnswered)
                : 0;

        // Сохраняем статистику
        StatsManager statsManager = new StatsManager(this);
        statsManager.saveGame(scorePercent, totalCorrectAnswers, totalQuestionsAnswered, base, difficulty);

        Intent intent = new Intent(GameActivity.this, StatisticsActivity.class);
        intent.putExtra("score_percent", scorePercent);
        intent.putExtra("correct_answers", totalCorrectAnswers);
        intent.putExtra("total_questions", totalQuestionsAnswered);
        intent.putExtra("base", base);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
        finish();
    }

    private void generateQuestion() {
        // Случайно выбираем тип операции: сложение, вычитание или сравнение
        int opType = random.nextInt(3); // 0 = +, 1 = -, 2 = сравнение

        if (opType == 2) {
            generateComparisonQuestion();
        } else {
            generateArithmeticQuestion(opType);
        }
    }

    private void generateArithmeticQuestion(int opType) {
        // Для обоих уровней одинаковые числа
        int max = 30;
        int a = random.nextInt(max) + 1;
        int b = random.nextInt(max) + 1;

        String op = (opType == 0) ? "+" : "-";
        int result10 = (opType == 0) ? (a + b) : (a - b);

        if (opType == 1 && result10 < 0) {
            // Если вычитание даёт отрицательный результат, меняем местами
            int temp = a;
            a = b;
            b = temp;
            result10 = a - b;
        }

        String aStr = toBaseString(a, base);
        String bStr = toBaseString(b, base);
        String resultStr = toBaseString(result10, base);

        questionText.setText(aStr + " " + op + " " + bStr + " = ?");

        correctAnswerIndex = random.nextInt(4);
        Button[] buttons = {answerButton1, answerButton2, answerButton3, answerButton4};
        String[] answers = new String[4];
        answers[correctAnswerIndex] = resultStr;

        // Генерируем уникальные неправильные ответы
        for (int i = 0; i < 4; i++) {
            if (i == correctAnswerIndex) {
                continue;
            }
            int fake;
            int attempts = 0;
            do {
                fake = result10 + random.nextInt(7) - 3;
                if (fake < 0) fake = result10 + 1;
                if (fake == result10) fake += 2;
                attempts++;
                if (attempts > 50) {
                    fake = result10 + 10 + i;
                    break;
                }
            } while (isDuplicateAnswer(answers, toBaseString(fake, base), i));
            answers[i] = toBaseString(fake, base);
        }

        // Устанавливаем тексты кнопок и делаем все кнопки видимыми
        for (int i = 0; i < 4; i++) {
            buttons[i].setText(answers[i]);
            buttons[i].setVisibility(android.view.View.VISIBLE);
        }
    }

    private void generateComparisonQuestion() {
        int a = random.nextInt(20) + 1;
        int b = random.nextInt(20) + 1;

        // Разные операторы сравнения для разных уровней сложности
        String[] ops;
        if (difficulty == 1) {
            // Легкий уровень: только базовые операторы
            ops = new String[]{"<", ">", "=", "!="};
        } else {
            // Сложный уровень: больше операторов
            ops = new String[]{"<", ">", "=", "<=", ">=", "!="};
        }

        String op = ops[random.nextInt(ops.length)];

        boolean shouldBeTrue = random.nextBoolean();
        boolean actual = compare(a, b, op);

        if (shouldBeTrue != actual) {
            // Корректируем b, чтобы выражение было истинным или ложным, как нужно
            if (shouldBeTrue) {
                // Нужно сделать выражение истинным
                switch (op) {
                    case "<": b = a + random.nextInt(5) + 1; break;
                    case ">": b = Math.max(1, a - random.nextInt(5) - 1); break;
                    case "=": b = a; break;
                    case "<=": b = a + random.nextInt(5); break;
                    case ">=": b = Math.max(1, a - random.nextInt(5)); break;
                    case "!=": b = a + random.nextInt(5) + 1; break;
                }
            } else {
                // Нужно сделать выражение ложным
                switch (op) {
                    case "<": b = Math.max(1, a - random.nextInt(5) - 1); break;
                    case ">": b = a + random.nextInt(5) + 1; break;
                    case "=": b = a + random.nextInt(5) + 1; break;
                    case "<=": b = Math.max(1, a - random.nextInt(5) - 1); break;
                    case ">=": b = a + random.nextInt(5) + 1; break;
                    case "!=": b = a; break;
                }
            }
        }

        String aStr = toBaseString(a, base);
        String bStr = toBaseString(b, base);
        questionText.setText(aStr + " " + op + " " + bStr + " ?");

        correctAnswerIndex = shouldBeTrue ? 0 : 1;
        answerButton1.setText("Да");
        answerButton2.setText("Нет");
        // Скрываем кнопки 3 и 4 для вопросов на сравнение
        answerButton3.setVisibility(android.view.View.GONE);
        answerButton4.setVisibility(android.view.View.GONE);

        resetAnswerButtonsBackground();
    }

    private boolean isDuplicateAnswer(String[] answers, String newAnswer, int currentIndex) {
        for (int i = 0; i < currentIndex; i++) {
            if (answers[i] != null && answers[i].equals(newAnswer)) {
                return true;
            }
        }
        return false;
    }

    private boolean compare(int a, int b, String op) {
        switch (op) {
            case "<":
                return a < b;
            case ">":
                return a > b;
            case "=":
                return a == b;
            case "<=":
                return a <= b;
            case ">=":
                return a >= b;
            case "!=":
                return a != b;
            default:
                return false;
        }
    }

    private String toBaseString(int value, int base) {
        switch (base) {
            case 2:
                return Integer.toBinaryString(value) + "₂";
            case 8:
                return Integer.toOctalString(value) + "₈";
            case 16:
                return Integer.toHexString(value).toUpperCase() + "₁₆";
            default:
                return String.valueOf(value);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}