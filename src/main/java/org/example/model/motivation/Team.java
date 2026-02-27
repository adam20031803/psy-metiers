package org.example.model.motivation;

import java.time.LocalDateTime;

public class Team {
    private int id;
    private int challengeId;
    private String name;
    private LocalDateTime createdAt;

    public Team() {}

    public Team(int challengeId, String name) {
        this.challengeId = challengeId;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getChallengeId() { return challengeId; }
    public void setChallengeId(int challengeId) { this.challengeId = challengeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
