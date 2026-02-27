package org.example.ai;

import org.example.model.motivation.Challenge;
import org.example.model.motivation.Task;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SmartTaskGenerator {

    private static final Random RANDOM = new Random();

    private static final Map<String, List<String>> ACTION_VERBS = new HashMap<>();
    private static final Map<String, List<String>> DIFFICULTY_ADJECTIVES = new HashMap<>();
    private static final List<String> EMOJIS = Arrays.asList("✨", "🚀", "💡", "🔧", "🎨", "📊", "⚙️", "🔍", "💻", "📝");

    static {
        // Verbes par type de challenge
        ACTION_VERBS.put("programmation", Arrays.asList(
                "Développer", "Implémenter", "Coder", "Configurer", "Optimiser", "Tester", "Déboguer", "Refactoriser",
                "Architecturer", "Sécuriser", "Documenter", "Déployer", "Intégrer", "Profiler", "Auditer"
        ));
        ACTION_VERBS.put("design", Arrays.asList(
                "Créer", "Concevoir", "Illustrer", "Prototyper", "Esquisser", "Coloriser", "Animer", "Maquetter",
                "Rechercher", "Harmoniser", "Styliser", "Peaufiner", "Valider", "Itérer", "Décliner"
        ));
        ACTION_VERBS.put("marketing", Arrays.asList(
                "Analyser", "Planifier", "Lancer", "Promouvoir", "Segmenter", "Cibler", "Mesurer", "Optimiser",
                "Scénariser", "Diffuser", "Convertir", "Fidéliser", "Monitorer", "Ajuster", "Négocier"
        ));
        ACTION_VERBS.put("business", Arrays.asList(
                "Étudier", "Négocier", "Structurer", "Financer", "Prévoir", "Budgétiser", "Piloter", "Décider",
                "Explorer", "Recruter", "Informer", "Coordonner", "Signer", "Vendre", "Lever"
        ));
        ACTION_VERBS.put("lecture", Arrays.asList(
                "Lire", "Annoter", "Résumer", "Analyser", "Explorer", "Réfléchir", "Découvrir", "Étudier",
                "Approfondir", "Partager", "Synthétiser", "Comparer", "Critiquer", "Documenter", "Mémoriser"
        ));
        ACTION_VERBS.put("sport", Arrays.asList(
                "S'entraîner", "Courir", "Soulever", "Pratiquer", "S'étirer", "Échauffer", "Performer", "Récupérer",
                "Marcher", "Nager", "Sauter", "Transpirer", "Renforcer", "Tonifier", "Défier"
        ));
        ACTION_VERBS.put("meditation", Arrays.asList(
                "Respirer", "Méditer", "Visualiser", "Se relaxer", "Écouter", "Observer", "Lâcher prise", "Se concentrer",
                "Calmer", "Apaiser", "Ressentir", "S'ancrer", "Explorer", "Déconnecter", "Se retrouver"
        ));
        ACTION_VERBS.put("personnel", Arrays.asList(
                "Méditer", "Planifier", "Organiser", "Réfléchir", "Écrire", "Pratiquer", "Apprendre", "Évaluer",
                "Exercer", "Lire", "Visualiser", "Équilibrer", "Simplifier", "Se reposer", "Fêter"
        ));

        // Adjectifs par difficulté
        DIFFICULTY_ADJECTIVES.put("Facile", Arrays.asList(
                "simple", "basique", "fondamental", "essentiel", "rapide", "initial", "clair", "accessible"
        ));
        DIFFICULTY_ADJECTIVES.put("Moyen", Arrays.asList(
                "approfondi", "détaillé", "technique", "structuré", "élaboré", "efficace", "complet", "analytique"
        ));
        DIFFICULTY_ADJECTIVES.put("Difficile", Arrays.asList(
                "complexe", "avancé", "expert", "stratégique", "critique", "majeur", "déterminant", "poussé", "expert"
        ));
    }

    public static List<Task> generateTasksForChallenge(Challenge challenge) {
        List<Task> tasks = new ArrayList<>();
        String type = challenge.getTypeChallenge() != null ? challenge.getTypeChallenge().toLowerCase() : "personnel";
        int numberOfTasks = ThreadLocalRandom.current().nextInt(5, 8);

        List<String> verbs = ACTION_VERBS.getOrDefault(type, ACTION_VERBS.get("personnel"));

        for (int i = 0; i < numberOfTasks; i++) {
            String difficulty = getDifficultyForIndex(i, numberOfTasks);
            Task task = createTask(challenge, verbs, difficulty, i + 1);
            tasks.add(task);
        }

        tasks.sort(Comparator.comparingInt(t -> getDifficultyOrder(t.getDifficulty())));
        return tasks;
    }

    private static String getDifficultyForIndex(int index, int total) {
        if (index < total / 3) return "Facile";
        if (index < 2 * total / 3) return "Moyen";
        return "Difficile";
    }

    private static Task createTask(Challenge challenge, List<String> verbs, String difficulty, int order) {
        String verb = verbs.get(RANDOM.nextInt(verbs.size()));
        String adjective = getRandomAdjectiveForDifficulty(difficulty);
        String emoji = EMOJIS.get(RANDOM.nextInt(EMOJIS.size()));

        String title = generateSpecificTitle(challenge, verb, adjective, emoji);
        String description = generateDescription(challenge, verb, adjective, difficulty);

        Task task = new Task(
                challenge.getIdChallenge(),
                title,
                description,
                difficulty,
                order
        );

        task.setPoints(calculatePoints(difficulty));
        task.setDueDate(LocalDate.now().plusDays(order * 2));
        return task;
    }

    private static String generateSpecificTitle(Challenge challenge, String verb, String adjective, String emoji) {
        String challengeTitle = challenge.getTitre();
        String[] templates = {
                "%s %s les bases de : %s",
                "%s %s les points %s",
                "%s %s stratégique : %s",
                "%s Objectif %s : %s",
                "%s %s (Niveau %s)",
                "%s %s pour le challenge %s"
        };
        
        String template = templates[RANDOM.nextInt(templates.length)];
        return String.format(template, emoji, verb, adjective, challengeTitle)
                .replace("  ", " ")
                .replace("de :", "de")
                .trim();
    }

    private static String generateDescription(Challenge challenge, String verb, String adjective, String difficulty) {
        String challengeTitle = challenge.getTitre();
        
        String[] templates = {
                "Objectif : %s les aspects %s pour '%s'.\n\nPlan d'Action :\n• Évaluer les besoins initiaux\n• %s consciencieusement chaque point\n• Valider la conformité avec l'objectif global.\n\nBénéfice : Une base solide (Niveau %s).",
                "Mission : Progression sur '%s' (%s).\n\nÉtapes :\n• Analyser les éléments %s\n• %s les parties critiques\n• Documenter vos résultats.\n\nBénéfice : Gain de précision immédiat.",
                "Action concrète : %s stratégique de '%s'.\n\nMarche à suivre :\n• Préparer l'environnement de travail\n• %s de manière %s\n• Vérifier les critères de réussite.\n\nBénéfice : Efficacité maximale (Niveau %s).",
                "Optimisation : Allez %s les acquis liés à '%s'.\n\nInstructions :\n• Revoir les fondamentaux %s\n• %s les points de blocage\n• Confirmer la maîtrise du sujet.\n\nBénéfice : Transition fluide vers l'étape suivante."
        };

        String template = templates[RANDOM.nextInt(templates.length)];
        
        // Dynamic mapping of placeholders for consistency
        int placeholders = (template.length() - template.replace("%s", "").length()) / 2;
        
        if (template.contains("Plan d'Action")) {
            return String.format(template, verb, adjective, challengeTitle, verb, difficulty.toLowerCase());
        } else if (template.contains("Étapes")) {
            return String.format(template, challengeTitle, difficulty.toLowerCase(), adjective, verb);
        } else if (template.contains("Marche à suivre")) {
            return String.format(template, verb, challengeTitle, verb, adjective, difficulty.toLowerCase());
        } else if (template.contains("Instructions")) {
            return String.format(template, verb, challengeTitle, adjective, verb);
        }
        
        return String.format(template, verb, adjective, challengeTitle, difficulty.toLowerCase());
    }

    private static int calculatePoints(String difficulty) {
        return switch (difficulty) {
            case "Facile" -> 50 + RANDOM.nextInt(20);
            case "Moyen" -> 100 + RANDOM.nextInt(30);
            case "Difficile" -> 200 + RANDOM.nextInt(50);
            default -> 75;
        };
    }

    private static int getDifficultyOrder(String difficulty) {
        return switch (difficulty) {
            case "Facile" -> 1;
            case "Moyen" -> 2;
            case "Difficile" -> 3;
            default -> 2;
        };
    }

    private static String getRandomAdjectiveForDifficulty(String difficulty) {
        List<String> adjectives = DIFFICULTY_ADJECTIVES.get(difficulty);
        return adjectives.get(RANDOM.nextInt(adjectives.size()));
    }
}