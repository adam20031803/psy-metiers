package org.example.utils;

import org.example.dao.motivation.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SparksManager {
    private static SparksManager instance;
    private int streakCount;
    private String currentTheme = "default";
    private List<String> unlockedThemes = new ArrayList<>();

    private SparksManager() {
        loadData();
    }

    public static SparksManager getInstance() {
        if (instance == null) instance = new SparksManager();
        return instance;
    }

    private void loadData() {
        try (Connection cnx = DatabaseConnection.getConnection()) {
            String q = "SELECT * FROM user_sparks WHERE id = 1";
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(q);
            if (rs.next()) {
                streakCount = rs.getInt("streak_count");
                currentTheme = rs.getString("current_theme");
                String unlocked = rs.getString("unlocked_themes");
                if (unlocked != null) {
                    unlockedThemes = new ArrayList<>(Arrays.asList(unlocked.split(",")));
                } else {
                    unlockedThemes.add("default");
                }
                
                Date lastDate = rs.getDate("last_connection");
                updateStreak(lastDate != null ? lastDate.toLocalDate() : null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStreak(LocalDate lastConnection) {
        LocalDate today = LocalDate.now();
        if (lastConnection == null) {
            streakCount = 1;
        } else if (lastConnection.equals(today.minusDays(1))) {
            streakCount++;
        } else if (!lastConnection.equals(today)) {
            streakCount = 1;
        }
        
        // Check for new theme unlocks
        checkUnlocks();
        saveData(today);
    }

    private void checkUnlocks() {
        if (streakCount >= 3 && !unlockedThemes.contains("midnight")) unlockedThemes.add("midnight");
        if (streakCount >= 7 && !unlockedThemes.contains("neon")) unlockedThemes.add("neon");
        if (streakCount >= 15 && !unlockedThemes.contains("gold")) unlockedThemes.add("gold");
    }

    private void saveData(LocalDate date) {
        try (Connection cnx = DatabaseConnection.getConnection()) {
            String q = "UPDATE user_sparks SET streak_count = ?, last_connection = ?, unlocked_themes = ? WHERE id = 1";
            PreparedStatement ps = cnx.prepareStatement(q);
            ps.setInt(1, streakCount);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, String.join(",", unlockedThemes));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getStreakCount() { return streakCount; }
    public String getCurrentTheme() { return currentTheme; }
    public List<String> getUnlockedThemes() { return unlockedThemes; }

    public void setTheme(String theme) {
        if (unlockedThemes.contains(theme)) {
            this.currentTheme = theme;
            try (Connection cnx = DatabaseConnection.getConnection()) {
                String q = "UPDATE user_sparks SET current_theme = ? WHERE id = 1";
                PreparedStatement ps = cnx.prepareStatement(q);
                ps.setString(1, theme);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
