package org.example.dao.motivation;

import org.example.model.motivation.ChatMessage;
import org.example.model.motivation.Team;
import org.example.model.motivation.TeamMember;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollaborativeCrud {
    private Connection cnx = DatabaseConnection.getConnection();

    public CollaborativeCrud() {
        createTablesIfNotExist();
    }

    private void createTablesIfNotExist() {
        try (Statement st = cnx.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS team (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "challenge_id INT, " +
                    "name VARCHAR(255), " +
                    "created_at DATETIME, " +
                    "FOREIGN KEY (challenge_id) REFERENCES challenge(id_challenge) ON DELETE CASCADE)");

            st.execute("CREATE TABLE IF NOT EXISTS team_member (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "team_id INT, " +
                    "user_id INT, " +
                    "user_name VARCHAR(255), " +
                    "points INT DEFAULT 0, " +
                    "FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE)");

            st.execute("CREATE TABLE IF NOT EXISTS challenge_chat (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "challenge_id INT, " +
                    "sender_name VARCHAR(255), " +
                    "message TEXT, " +
                    "timestamp DATETIME, " +
                    "FOREIGN KEY (challenge_id) REFERENCES challenge(id_challenge) ON DELETE CASCADE)");
            
            // S'assurer que la colonne user_id existe si la table a été créée précédemment
            try {
                st.execute("ALTER TABLE team_member ADD COLUMN user_id INT AFTER team_id");
            } catch (SQLException ignore) {
                // La colonne existe probablement déjà
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Team Methods ---
    public void createTeam(Team team) {
        String query = "INSERT INTO team (challenge_id, name, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, team.getChallengeId());
            ps.setString(2, team.getName());
            ps.setTimestamp(3, Timestamp.valueOf(team.getCreatedAt()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) team.setId(rs.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Team> getTeamsByChallenge(int challengeId) {
        List<Team> teams = new ArrayList<>();
        String query = "SELECT * FROM team WHERE challenge_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, challengeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Team t = new Team();
                t.setId(rs.getInt("id"));
                t.setChallengeId(rs.getInt("challenge_id"));
                t.setName(rs.getString("name"));
                t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                teams.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teams;
    }

    // --- Team Member Methods ---
    public void addMember(TeamMember member) {
        String query = "INSERT INTO team_member (team_id, user_id, user_name, points) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, member.getTeamId());
            ps.setInt(2, member.getUserId());
            ps.setString(3, member.getUserName());
            ps.setInt(4, member.getPoints());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<TeamMember> getMembers(int teamId) {
        List<TeamMember> members = new ArrayList<>();
        String query = "SELECT * FROM team_member WHERE team_id = ? ORDER BY points DESC";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TeamMember m = new TeamMember();
                m.setId(rs.getInt("id"));
                m.setTeamId(rs.getInt("team_id"));
                m.setUserId(rs.getInt("user_id"));
                m.setUserName(rs.getString("user_name"));
                m.setPoints(rs.getInt("points"));
                members.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public String findPseudoByUserId(int userId) {
        String query = "SELECT user_name FROM team_member WHERE user_id = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("user_name");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateMemberPoints(int memberId, int pointsToAdd) {
        String query = "UPDATE team_member SET points = points + ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, pointsToAdd);
            ps.setInt(2, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Chat Methods ---
    public void saveChatMessage(ChatMessage msg) {
        String query = "INSERT INTO challenge_chat (challenge_id, sender_name, message, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, msg.getChallengeId());
            ps.setString(2, msg.getSenderName());
            ps.setString(3, msg.getMessage());
            ps.setTimestamp(4, Timestamp.valueOf(msg.getTimestamp()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ChatMessage> getChatHistory(int challengeId) {
        List<ChatMessage> messages = new ArrayList<>();
        String query = "SELECT * FROM challenge_chat WHERE challenge_id = ? ORDER BY timestamp ASC";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, challengeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChatMessage m = new ChatMessage();
                m.setId(rs.getInt("id"));
                m.setChallengeId(rs.getInt("challenge_id"));
                m.setSenderName(rs.getString("sender_name"));
                m.setMessage(rs.getString("message"));
                m.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                messages.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    public boolean isUserInChallenge(int userId, int challengeId) {
        String query = "SELECT tm.id FROM team_member tm JOIN team t ON tm.team_id = t.id " +
                       "WHERE tm.user_id = ? AND t.challenge_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, challengeId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
