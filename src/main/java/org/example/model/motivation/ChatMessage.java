package org.example.model.motivation;

import java.time.LocalDateTime;

public class ChatMessage {
    private int id;
    private int challengeId;
    private String senderName;
    private String message;
    private LocalDateTime timestamp;

    public ChatMessage() {}

    public ChatMessage(int challengeId, String senderName, String message) {
        this.challengeId = challengeId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getChallengeId() { return challengeId; }
    public void setChallengeId(int challengeId) { this.challengeId = challengeId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
