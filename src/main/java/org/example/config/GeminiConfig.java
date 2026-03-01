package org.example.config;

public class GeminiConfig {

    /**
     * Clé API Gemini (gratuite sur https://aistudio.google.com/app/apikey)
     */
    public static final String API_KEY = "";

    /**
     * Modèle principal — gemini-2.0-flash (modèle le plus rapide et moderne)
     */
    public static final String MODEL_NAME = "gemini-2.0-flash";

    /**
     * Modèles de secours essayés dans l'ordre si le quota est épuisé
     * (uniquement les modèles confirmés disponibles pour cette clé)
     */
    public static final String[] FALLBACK_MODELS = {
        "gemini-2.0-flash-lite",
        "gemini-1.5-flash",
        "gemini-1.5-flash-8b",
        "gemini-1.5-pro",
        "gemini-2.0-flash-exp"
    };
}