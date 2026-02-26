package org.example.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.config.GeminiConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Appelle l'API REST native de Gemini.
 * Essaie automatiquement plusieurs modèles si le quota est épuisé (429).
 */
public class GeminiViaOpenAICompatibility {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String generateTasks(String prompt) throws Exception {
        // Essayer le modèle principal + les modèles de secours
        String[] modelsToTry = buildModelList();

        Exception lastException = null;

        for (String model : modelsToTry) {
            try {
                System.out.println("🔄 Essai avec le modèle: " + model);
                String result = callGemini(model, prompt);
                System.out.println("✅ Succès avec le modèle: " + model);
                return result;
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && (msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED")
                        || msg.contains("404") || msg.contains("NOT_FOUND"))) {
                    System.out.println("⚠️ Modèle " + model + " indisponible (Quota ou Erreur), essai du suivant...");
                    lastException = e;
                    // Attendre plus longtemps (1.5s) pour espérer un reset de quota/rate limit
                    Thread.sleep(1500); 
                } else {
                    // Erreur fatale (401 auth, 400 bad request...) → ne pas réessayer
                    throw e;
                }
            }
        }

        throw new RuntimeException("Tous les modèles Gemini ont atteint leur quota. Réessayez dans quelques minutes.", lastException);
    }

    private String callGemini(String model, String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + GeminiConfig.API_KEY;

        // Corps de la requête au format natif Gemini
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(part);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("maxOutputTokens", 2048);
        generationConfig.addProperty("topP", 0.95);

        JsonObject body = new JsonObject();
        body.add("contents", contents);
        body.add("generationConfig", generationConfig);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur API Gemini " + response.statusCode()
                    + ": " + response.body());
        }

        // Extraire le texte généré
        JsonObject root = gson.fromJson(response.body(), JsonObject.class);
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.size() == 0) {
            throw new RuntimeException("Aucun candidat dans la réponse Gemini");
        }
        JsonObject responseContent = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
        JsonArray responseParts = responseContent.getAsJsonArray("parts");
        return responseParts.get(0).getAsJsonObject().get("text").getAsString();
    }

    private String[] buildModelList() {
        // Commencer par le modèle principal, puis les fallbacks (sans doublons)
        String primary = GeminiConfig.MODEL_NAME;
        String[] fallbacks = GeminiConfig.FALLBACK_MODELS;

        // Construire la liste : primary en premier, puis fallbacks qui ne sont pas le primary
        java.util.List<String> models = new java.util.ArrayList<>();
        models.add(primary);
        for (String f : fallbacks) {
            if (!f.equals(primary)) {
                models.add(f);
            }
        }
        return models.toArray(new String[0]);
    }
}