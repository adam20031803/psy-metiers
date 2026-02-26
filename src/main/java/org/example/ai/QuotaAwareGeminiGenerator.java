package org.example.ai;

import org.example.model.motivation.Challenge;
import org.example.model.motivation.Task;
import java.time.LocalDate;
import java.util.List;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;


public class QuotaAwareGeminiGenerator {

    private final GeminiTaskGenerator geminiGenerator;
    private final SmartTaskGenerator fallbackGenerator;
    private int requestCount = 0;
    private LocalDate lastRequestDate = LocalDate.now();
    private static final int DAILY_QUOTA = 20;

    public QuotaAwareGeminiGenerator() {
        this.geminiGenerator = new GeminiTaskGenerator();
        this.fallbackGenerator = new SmartTaskGenerator();
    }

    public List<Task> generateTasksForChallenge(Challenge challenge) {

        // Reset quota chaque jour
        if (!LocalDate.now().equals(lastRequestDate)) {
            requestCount = 0;
            lastRequestDate = LocalDate.now();
        }

        // Vérifier quota
        if (requestCount < DAILY_QUOTA) {
            try {
                System.out.println("📊 Tentative d'appel Gemini (" + (requestCount + 1) + "/" + DAILY_QUOTA + ")");

                List<Task> tasks = geminiGenerator.generateTasksForChallenge(challenge);

                requestCount++;
                System.out.println("✅ Appel réussi! Quota utilisé: " + requestCount + "/" + DAILY_QUOTA);

                return tasks;

            } catch (Exception e) {
                System.out.println("⚠️ Erreur Gemini, utilisation du fallback");
                return fallbackGenerator.generateTasksForChallenge(challenge);

            }
        }

        System.out.println("⚠️ Quota dépassé, utilisation du fallback");
        return fallbackGenerator.generateTasksForChallenge(challenge);

    }

}