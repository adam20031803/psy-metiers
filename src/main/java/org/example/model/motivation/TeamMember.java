package org.example.model.motivation;

public class TeamMember {
    private int id;
    private int teamId;
    private int userId;
    private String userName;
    private int points;

    public TeamMember() {}

    public TeamMember(int teamId, int userId, String userName) {
        this.teamId = teamId;
        this.userId = userId;
        this.userName = userName;
        this.points = 0;
    }

    public TeamMember(int teamId, String userName) {
        this(teamId, 0, userName);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
