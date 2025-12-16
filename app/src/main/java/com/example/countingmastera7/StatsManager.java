package com.example.countingmastera7;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatsManager {
    private static final String PREFS_NAME = "game_stats";
    private static final String KEY_GAME_HISTORY = "game_history";
    private static final String KEY_GAME_COUNTER = "game_counter";
    
    private SharedPreferences prefs;
    
    public StatsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static class GameStats {
        public int gameNumber;
        public String date;
        public int scorePercent;
        public int correctAnswers;
        public int totalQuestions;
        public int maxLevel;
        
        public GameStats(int gameNumber, String date, int scorePercent, int correctAnswers, 
                        int totalQuestions, int maxLevel) {
            this.gameNumber = gameNumber;
            this.date = date;
            this.scorePercent = scorePercent;
            this.correctAnswers = correctAnswers;
            this.totalQuestions = totalQuestions;
            this.maxLevel = maxLevel;
        }
    }
    
    public void saveGame(int scorePercent, int correctAnswers, int totalQuestions, int maxLevel) {
        int gameNumber = prefs.getInt(KEY_GAME_COUNTER, 0) + 1;
        prefs.edit().putInt(KEY_GAME_COUNTER, gameNumber).apply();
        
        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date());
        
        GameStats stats = new GameStats(gameNumber, date, scorePercent, correctAnswers, totalQuestions, maxLevel);
        
        List<GameStats> history = getGameHistory();
        history.add(0, stats); // Добавляем в начало
        
        // Сохраняем только последние 10 игр
        if (history.size() > 10) {
            history = history.subList(0, 10);
        }
        
        saveHistory(history);
    }
    
    public List<GameStats> getGameHistory() {
        String historyJson = prefs.getString(KEY_GAME_HISTORY, "[]");
        List<GameStats> history = new ArrayList<>();
        
        try {
            JSONArray jsonArray = new JSONArray(historyJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                GameStats stats = new GameStats(
                    obj.getInt("gameNumber"),
                    obj.getString("date"),
                    obj.getInt("scorePercent"),
                    obj.getInt("correctAnswers"),
                    obj.getInt("totalQuestions"),
                    obj.getInt("maxLevel")
                );
                history.add(stats);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return history;
    }
    
    private void saveHistory(List<GameStats> history) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (GameStats stats : history) {
                JSONObject obj = new JSONObject();
                obj.put("gameNumber", stats.gameNumber);
                obj.put("date", stats.date);
                obj.put("scorePercent", stats.scorePercent);
                obj.put("correctAnswers", stats.correctAnswers);
                obj.put("totalQuestions", stats.totalQuestions);
                obj.put("maxLevel", stats.maxLevel);
                jsonArray.put(obj);
            }
            prefs.edit().putString(KEY_GAME_HISTORY, jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public void clearHistory() {
        prefs.edit().remove(KEY_GAME_HISTORY).putInt(KEY_GAME_COUNTER, 0).apply();
    }
}

