package org.example.ai;

import com.google.gson.*;
import org.example.config.GeminiConfig;
import org.example.model.motivation.Challenge;
import org.example.model.motivation.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiTaskGenerator {

    private final GeminiViaOpenAICompatibility geminiClient;
    private final Gson gson;

    public GeminiTaskGenerator() {
        this.geminiClient = new GeminiViaOpenAICompatibility();
        this.gson = new Gson();
    }

    /**
     * Génère des tâches intelligentes pour un challenge
     */
    public List<Task> generateTasksForChallenge(Challenge challenge) {
        // Vérifier que la clé API est configurée
        if (GeminiConfig.API_KEY == null || GeminiConfig.API_KEY.isBlank()
                || GeminiConfig.API_KEY.startsWith("VOTRE_")) {
            System.err.println("❌ Clé API Gemini non configurée !");
            System.err.println("   → Allez sur https://aistudio.google.com/app/apikey");
            System.err.println("   → Créez une clé gratuite et mettez-la dans GeminiConfig.API_KEY");
            System.out.println("⚠️ Utilisation du générateur de secours (clé API manquante)");
            return SmartTaskGenerator.generateTasksForChallenge(challenge);
        }

        try {
            System.out.println("📡 Appel API Gemini pour: " + challenge.getTitre());

            String prompt = buildPrompt(challenge);
            String response = geminiClient.generateTasks(prompt);

            System.out.println("📥 Réponse reçue: " + response.substring(0, Math.min(100, response.length())) + "...");

            List<Task> tasks = parseTasksFromResponse(response, challenge);

            if (!tasks.isEmpty()) {
                System.out.println("✅ " + tasks.size() + " tâches générées avec Gemini");
                return tasks;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur Gemini (Quota/API): " + e.getMessage());
        }

        // Fallback
        System.out.println("⚠️ Utilisation du générateur de secours intelligent");
        return SmartTaskGenerator.generateTasksForChallenge(challenge);
    }

    /**
     * Construit le prompt pour Gemini
     */
    private String buildPrompt(Challenge challenge) {
        return String.format("""
            Rôle : Tu es un coach professionnel de haut niveau spécialisé en productivité et motivation.
            Ton but est d'aider l'utilisateur à réussir son challenge : "%s".
            
            Détails du Challenge :
            - Titre : %s
            - Description : %s
            - Catégorie : %s
            - Difficulté : %s
            
            Objectifs de génération :
            1. Progression Logique : Les tâches doivent suivre un parcours fluide, de l'initiation à la maîtrise.
            2. Adaptabilité : Les tâches doivent être DIRECTEMENT liées au thème "%s".
            3. Richesse sémantique : Chaque description doit être structurée de manière PROFESSIONNELLE avec un plan d'action.
            
            Format de la description (Obligatoire) :
            La description doit obligatoirement inclure :
            - Un "Objectif" clair.
            - Une section "Étapes à suivre" (sous forme de liste à puces).
            - Un "Bénéfice" pour le challenge final.
            
            Contraintes de format (JSON strict) :
            - Chaque titre doit commencer par un emoji pertinent.
            - Difficultés acceptées : "Facile", "Moyen", "Difficile".
            - Ne pas inclure de texte d'introduction/conclusion.
            
            Format JSON :
            {
              "tasks": [
                {
                  "title": "Emoji Titre",
                  "description": "Objectif : [But]\\n\\nÉtapes :\\n• Étape 1\\n• Étape 2\\n• Étape 3\\n\\nBénéfice : [Impact]",
                  "difficulty": "Facile"
                }
              ]
            }
            """,
                challenge.getTitre(),
                challenge.getTitre(),
                challenge.getDescription(),
                challenge.getTypeChallenge(),
                challenge.getNiveauDifficulte(),
                challenge.getTypeChallenge()
        );
    }

    /**
     * Parse la réponse JSON en objets Task
     */
    private List<Task> parseTasksFromResponse(String response, Challenge challenge) {
        List<Task> tasks = new ArrayList<>();

        try {
            // Nettoyer la réponse
            String jsonStr = extractJson(response);

            JsonObject root = gson.fromJson(jsonStr, JsonObject.class);
            JsonArray tasksArray = root.getAsJsonArray("tasks");

            if (tasksArray == null) {
                System.err.println("❌ Pas de tableau 'tasks' dans la réponse");
                return tasks;
            }

            for (int i = 0; i < tasksArray.size(); i++) {
                JsonObject taskJson = tasksArray.get(i).getAsJsonObject();

                String title = getJsonString(taskJson, "title");
                String description = getJsonString(taskJson, "description");
                String difficulty = getJsonString(taskJson, "difficulty");

                if (title != null && description != null && difficulty != null) {
                    // Normaliser la difficulté
                    difficulty = normalizeDifficulty(difficulty);

                    Task task = new Task(
                            challenge.getIdChallenge(),
                            title,
                            description,
                            difficulty,
                            i + 1
                    );

                    task.setDueDate(LocalDate.now().plusDays((i + 1) * 2));
                    tasks.add(task);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing JSON: " + e.getMessage());
            System.err.println("Réponse reçue: " + response);
        }

        return tasks;
    }

    /**
     * Extrait le JSON de la réponse
     */
    private String extractJson(String text) {
        // Enlever les ```json et ``` si présents
        text = text.replaceAll("```json", "").replaceAll("```", "").trim();

        // Chercher le premier { et dernier }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }

        return text;
    }

    /**
     * Récupère une chaîne JSON en gérant les nulls
     */
    private String getJsonString(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return null;
    }

    /**
     * Normalise la difficulté
     */
    private String normalizeDifficulty(String difficulty) {
        String diff = difficulty.toLowerCase();
        if (diff.contains("facile")) return "Facile";
        if (diff.contains("moyen")) return "Moyen";
        if (diff.contains("difficile")) return "Difficile";
        return "Moyen";
    }
}