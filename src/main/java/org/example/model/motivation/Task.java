package org.example.model.motivation;

import java.time.LocalDate;

public class Task {
    private int id;
    private int challengeId;
    private String title;
    private String description;
    private String status; // "À faire", "En cours", "Terminé"
    private int points;     // ← Ce champ existe déjà
    private String difficulty; // "Facile", "Moyen", "Difficile"
    private String icon;
    private LocalDate dueDate;
    private int orderIndex;
    private boolean isCompleted;

    public Task(int challengeId, String title, String description, String difficulty, int orderIndex) {
        this.challengeId = challengeId;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.orderIndex = orderIndex;
        this.status = "À faire";
        this.isCompleted = false;
        this.icon = getIconForDifficulty(difficulty);
        this.dueDate = LocalDate.now().plusDays(orderIndex * 2 + 3);

        // Assigner des points selon la difficulté
        switch(difficulty) {
            case "Facile":
                this.points = 50;
                break;
            case "Moyen":
                this.points = 100;
                break;
            case "Difficile":
                this.points = 200;
                break;
            default:
                this.points = 75;
        }
    }

    private String getIconForDifficulty(String difficulty) {
        switch(difficulty) {
            case "Facile": return "🌱";
            case "Moyen": return "🌿";
            case "Difficile": return "🌳";
            default: return "📋";
        }
    }

    // === AJOUTER CES GETTERS ET SETTERS MANQUANTS ===

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
        this.status = completed ? "Terminé" : "En cours";
    }

    // Getters et setters existants
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getChallengeId() { return challengeId; }
    public void setChallengeId(int challengeId) { this.challengeId = challengeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
        this.icon = getIconForDifficulty(difficulty);
    }
}