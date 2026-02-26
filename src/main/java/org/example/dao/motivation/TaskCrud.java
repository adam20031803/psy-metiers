package org.example.dao.motivation;

import org.example.model.motivation.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskCrud {

    private Connection cnx = DatabaseConnection.getConnection();

    public void create(Task task) {
        String query = "INSERT INTO task (challenge_id, title, description, status, points, difficulty, icon, due_date, order_index, is_completed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, task.getChallengeId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getStatus());
            ps.setInt(5, task.getPoints());
            ps.setString(6, task.getDifficulty());
            ps.setString(7, task.getIcon());
            ps.setDate(8, Date.valueOf(task.getDueDate()));
            ps.setInt(9, task.getOrderIndex());
            ps.setBoolean(10, task.isCompleted());

            ps.executeUpdate();
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Task> getTasksByChallenge(int challengeId) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT * FROM task WHERE challenge_id = ? ORDER BY order_index ASC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, challengeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task(
                            rs.getInt("challenge_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("difficulty"),
                            rs.getInt("order_index")
                    );
                    task.setId(rs.getInt("id"));
                    task.setStatus(rs.getString("status"));
                    task.setPoints(rs.getInt("points"));
                    task.setIcon(rs.getString("icon"));
                    task.setDueDate(rs.getDate("due_date").toLocalDate());
                    task.setCompleted(rs.getBoolean("is_completed"));
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public void update(Task task) {
        String query = "UPDATE task SET status=?, points=?, is_completed=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, task.getStatus());
            ps.setInt(2, task.getPoints());
            ps.setBoolean(3, task.isCompleted());
            ps.setInt(4, task.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteByChallenge(int challengeId) {
        String query = "DELETE FROM task WHERE challenge_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, challengeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
