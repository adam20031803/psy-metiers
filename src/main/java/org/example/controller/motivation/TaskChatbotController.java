package org.example.controller.motivation;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.ai.QuotaAwareGeminiGenerator;
import org.example.model.motivation.Challenge;
import org.example.model.motivation.Recompense;
import org.example.model.motivation.Task;

import org.example.dao.motivation.TaskCrud;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.io.File;

import javafx.stage.StageStyle;
import javafx.scene.Cursor;



public class TaskChatbotController {

    // ── State ────────────────────────────────────────────────────────────────
    private Stage stage;
    private Challenge challenge;
    private List<Recompense> recompenses;
    private List<Task> generatedTasks;
    private int totalPoints = 0;
    private int completedTasks = 0;

    // ── UI refs ───────────────────────────────────────────────────────────────
    private VBox tasksContainer;
    private Label pointsTotalLabel;
    private ProgressBar globalProgress;
    private Label progressPercent;
    private Label statusLabel;
    private Button generateBtn;
    private StackPane mainStack; // New overlay container

    private final QuotaAwareGeminiGenerator aiTaskGenerator = new QuotaAwareGeminiGenerator();
    private final TaskCrud taskCrud = new TaskCrud();

    // ─────────────────────────────────────────────────────────────────────────
    public void show(Challenge challenge, List<Recompense> recompenses) {
        this.challenge = challenge;
        this.recompenses = recompenses;

        stage = new Stage();
        stage.setTitle("🤖 AI Task Planner — " + challenge.getTitre());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setMinWidth(900);
        stage.setMinHeight(720);

        BorderPane root = buildRoot();
        Scene scene = new Scene(root, 1000, 760);
        stage.setScene(scene);

        // Check for existing tasks
        Platform.runLater(this::loadExistingTasks);

        stage.showAndWait();
    }

    private void loadExistingTasks() {
        List<Task> existing = taskCrud.getTasksByChallenge(challenge.getIdChallenge());
        if (!existing.isEmpty()) {
            displayTasks(existing);
            statusLabel.setText("✅ " + existing.size() + " tâches chargées depuis la base");
            generateBtn.setText("  🔄   Régénérer");
        }
    }

    private void displayTasks(List<Task> tasks) {
        generatedTasks = tasks;
        totalPoints = 0;
        completedTasks = 0;
        
        tasksContainer.getChildren().clear();

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.isCompleted()) {
                totalPoints += task.getPoints();
                completedTasks++;
            }
            VBox card = buildTaskCard(task, i + 1, tasks.size());
            
            // Initial state for animation
            card.setOpacity(0);
            card.setTranslateY(20);
            tasksContainer.getChildren().add(card);

            // Staggered entrance animation
            int index = i;
            PauseTransition pt = new PauseTransition(Duration.millis(i * 100));
            pt.setOnFinished(ev -> {
                FadeTransition ft = new FadeTransition(Duration.millis(350), card);
                ft.setFromValue(0); ft.setToValue(1);
                TranslateTransition tt = new TranslateTransition(Duration.millis(350), card);
                tt.setFromY(20); tt.setToY(0);
                tt.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(ft, tt).play();
            });
            pt.play();
        }
        
        pointsTotalLabel.setText(String.valueOf(totalPoints));
        updateProgress(completedTasks, tasks.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ROOT
    // ─────────────────────────────────────────────────────────────────────────
    private BorderPane buildRoot() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0d0d1a;");
        root.setTop(buildHeader());
        
        // Use a StackPane as the center container to allow floating badges (Flash Challenge)
        mainStack = new StackPane();
        mainStack.getChildren().add(buildCenter());
        
        root.setCenter(mainStack);
        root.setBottom(buildFooter());
        return root;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────────────────────────────────
    private VBox buildHeader() {
        VBox header = new VBox(12);
        header.setPadding(new Insets(22, 30, 18, 30));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #1a1a2e, #16213e, #0f3460);" +
            "-fx-border-color: rgba(99,102,241,0.3);" +
            "-fx-border-width: 0 0 1 0;"
        );

        // ── Top row ──
        HBox topRow = new HBox(16);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Animated robot avatar
        StackPane avatar = buildGlowAvatar("🤖", "#6366f1", "#8b5cf6");

        // Title block
        VBox titleBlock = new VBox(4);
        Label title = new Label("AI TASK PLANNER");
        title.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: white;" +
            "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.8), 12, 0, 0, 0);"
        );
        Label subtitle = new Label("Powered by Gemini AI  ·  Parcours personnalisé pour votre challenge");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.55);");
        titleBlock.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Challenge info badge
        VBox challengeBadge = new VBox(3);
        challengeBadge.setAlignment(Pos.CENTER_RIGHT);
        challengeBadge.setPadding(new Insets(8, 16, 8, 16));
        challengeBadge.setStyle(
            "-fx-background-color: rgba(99,102,241,0.15);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: rgba(99,102,241,0.4);" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;"
        );
        Label challengeName = new Label("🏆  " + challenge.getTitre());
        challengeName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #a5b4fc;");
        Label challengeMeta = new Label(challenge.getNiveauDifficulte() + "  ·  " + challenge.getDureeJours() + " jours  ·  " + challenge.getTypeChallenge());
        challengeMeta.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(165,180,252,0.6);");
        challengeBadge.getChildren().addAll(challengeName, challengeMeta);

        topRow.getChildren().addAll(avatar, titleBlock, spacer, challengeBadge);

        // ── Progress bar row ──
        HBox progressRow = new HBox(12);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        Label progressTitle = new Label("PROGRESSION");
        progressTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.4);");

        globalProgress = new ProgressBar(0);
        globalProgress.setPrefWidth(Double.MAX_VALUE);
        globalProgress.setPrefHeight(8);
        globalProgress.setStyle(
            "-fx-accent: linear-gradient(to right, #6366f1, #8b5cf6, #a78bfa);" +
            "-fx-control-inner-background: rgba(255,255,255,0.08);" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 0;"
        );
        HBox.setHgrow(globalProgress, Priority.ALWAYS);

        progressPercent = new Label("0%");
        progressPercent.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #a78bfa;");
        progressPercent.setMinWidth(35);

        progressRow.getChildren().addAll(progressTitle, globalProgress, progressPercent);

        header.getChildren().addAll(topRow, progressRow);
        return header;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CENTER
    // ─────────────────────────────────────────────────────────────────────────
    private HBox buildCenter() {
        HBox center = new HBox(0);

        // LEFT — rewards sidebar
        VBox sidebar = buildSidebar();
        sidebar.setPrefWidth(260);
        sidebar.setMinWidth(220);
        sidebar.setMaxWidth(280);

        // Divider
        Rectangle div = new Rectangle(1, 0);
        div.setFill(Color.web("rgba(99,102,241,0.2)"));
        div.heightProperty().bind(center.heightProperty());

        // RIGHT — tasks area
        VBox tasksArea = buildTasksArea();
        HBox.setHgrow(tasksArea, Priority.ALWAYS);

        center.getChildren().addAll(sidebar, div, tasksArea);
        return center;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SIDEBAR — Rewards
    // ─────────────────────────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setStyle("-fx-background-color: #0a0a14;");

        // Sidebar header
        HBox sideHeader = new HBox(8);
        sideHeader.setPadding(new Insets(16, 16, 12, 16));
        sideHeader.setAlignment(Pos.CENTER_LEFT);
        sideHeader.setStyle("-fx-background-color: rgba(99,102,241,0.08);");
        Label sideTitle = new Label("🎁  RÉCOMPENSES");
        sideTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.45);");
        sideHeader.getChildren().add(sideTitle);

        // Rewards list
        VBox rewardsList = new VBox(10);
        rewardsList.setPadding(new Insets(14, 14, 14, 14));

        for (Recompense r : recompenses) {
            HBox rewardItem = buildRewardItem(r);
            rewardsList.getChildren().add(rewardItem);
        }

        ScrollPane rewardsScroll = new ScrollPane(rewardsList);
        rewardsScroll.setFitToWidth(true);
        rewardsScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rewardsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(rewardsScroll, Priority.ALWAYS);

        // Points counter at bottom
        VBox pointsBox = buildPointsBox();

        sidebar.getChildren().addAll(sideHeader, rewardsScroll, pointsBox);
        return sidebar;
    }

    private HBox buildRewardItem(Recompense r) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 14, 12, 14));
        item.setStyle(
            "-fx-background-color: rgba(255,215,0,0.06);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(255,215,0,0.18);" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );

        // Icon circle
        StackPane iconCircle = new StackPane();
        Circle bg = new Circle(20);
        bg.setFill(Color.web("rgba(255,215,0,0.12)"));
        Label icon = new Label(getIconForReward(r.getTypeRecompense()));
        icon.setStyle("-fx-font-size: 18px;");
        iconCircle.getChildren().addAll(bg, icon);

        VBox info = new VBox(3);
        Label name = new Label(r.getTitre());
        name.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #fbbf24;");
        name.setWrapText(true);
        Label type = new Label(r.getTypeRecompense().toUpperCase());
        type.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(251,191,36,0.5);");
        info.getChildren().addAll(name, type);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Lock icon
        Label lock = new Label("🔒");
        lock.setStyle("-fx-font-size: 12px; -fx-opacity: 0.5;");

        item.getChildren().addAll(iconCircle, info, lock);

        // Hover effect
        item.setOnMouseEntered(e -> item.setStyle(
            "-fx-background-color: rgba(255,215,0,0.12);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(255,215,0,0.35);" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.15), 8, 0, 0, 2);"
        ));
        item.setOnMouseExited(e -> item.setStyle(
            "-fx-background-color: rgba(255,215,0,0.06);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(255,215,0,0.18);" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        ));

        return item;
    }

    private VBox buildPointsBox() {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: rgba(99,102,241,0.1);" +
            "-fx-border-color: rgba(99,102,241,0.25);" +
            "-fx-border-width: 1 0 0 0;"
        );

        Label label = new Label("⭐  POINTS GAGNÉS");
        label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.4);");

        pointsTotalLabel = new Label("0");
        pointsTotalLabel.setStyle(
            "-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #a78bfa;" +
            "-fx-effect: dropshadow(gaussian, rgba(167,139,250,0.6), 10, 0, 0, 0);"
        );

        Label pts = new Label("points");
        pts.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(167,139,250,0.6);");

        box.getChildren().addAll(label, pointsTotalLabel, pts);
        return box;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TASKS AREA
    // ─────────────────────────────────────────────────────────────────────────
    private VBox buildTasksArea() {
        VBox area = new VBox(0);
        area.setStyle("-fx-background-color: #0d0d1a;");

        // Area header
        HBox areaHeader = new HBox(10);
        areaHeader.setPadding(new Insets(14, 20, 10, 20));
        areaHeader.setAlignment(Pos.CENTER_LEFT);
        areaHeader.setStyle("-fx-background-color: rgba(255,255,255,0.02);");

        Label areaTitle = new Label("📋  TÂCHES GÉNÉRÉES PAR L'IA");
        areaTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.4);");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        statusLabel = new Label("En attente de génération...");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.3);");

        areaHeader.getChildren().addAll(areaTitle, sp, statusLabel);

        // Tasks scroll
        tasksContainer = new VBox(14);
        tasksContainer.setPadding(new Insets(16, 20, 20, 20));

        // Placeholder
        tasksContainer.getChildren().add(buildPlaceholder());

        ScrollPane scroll = new ScrollPane(tasksContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        area.getChildren().addAll(areaHeader, scroll);
        return area;
    }

    private VBox buildPlaceholder() {
        VBox ph = new VBox(18);
        ph.setAlignment(Pos.CENTER);
        ph.setPadding(new Insets(80, 40, 80, 40));

        // Animated pulse circle
        StackPane pulse = new StackPane();
        Circle outer = new Circle(50);
        outer.setFill(Color.web("rgba(99,102,241,0.08)"));
        outer.setStroke(Color.web("rgba(99,102,241,0.2)"));
        outer.setStrokeWidth(1);
        Circle inner = new Circle(35);
        inner.setFill(Color.web("rgba(99,102,241,0.12)"));
        Label rocketLabel = new Label("🚀");
        rocketLabel.setStyle("-fx-font-size: 32px;");
        pulse.getChildren().addAll(outer, inner, rocketLabel);

        // Pulse animation
        ScaleTransition pulseSt = new ScaleTransition(Duration.millis(1800), outer);
        pulseSt.setFromX(1.0); pulseSt.setFromY(1.0);
        pulseSt.setToX(1.15); pulseSt.setToY(1.15);
        pulseSt.setAutoReverse(true);
        pulseSt.setCycleCount(Animation.INDEFINITE);
        pulseSt.play();

        Label msg1 = new Label("Prêt à générer votre parcours");
        msg1.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.7);");

        Label msg2 = new Label("Cliquez sur « Générer les Tâches » pour que l'IA\ncréé un parcours ordonné du plus facile au plus difficile");
        msg2.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.35);");
        msg2.setTextAlignment(TextAlignment.CENTER);
        msg2.setWrapText(true);

        ph.getChildren().addAll(pulse, msg1, msg2);
        return ph;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FOOTER
    // ─────────────────────────────────────────────────────────────────────────
    private HBox buildFooter() {
        HBox footer = new HBox(14);
        footer.setPadding(new Insets(16, 30, 20, 30));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle(
            "-fx-background-color: #0a0a14;" +
            "-fx-border-color: rgba(99,102,241,0.2);" +
            "-fx-border-width: 1 0 0 0;"
        );

        generateBtn = new Button("  🚀   GÉNÉRER LES TÂCHES AVEC L'IA  ");
        generateBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6, #a855f7);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 900;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 14 36;" +
            "-fx-background-radius: 30;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.55), 18, 0, 0, 4);"
        );
        generateBtn.setOnMouseEntered(e -> generateBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #4f46e5, #7c3aed, #9333ea);" +
            "-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px;" +
            "-fx-padding: 14 36; -fx-background-radius: 30; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.8), 24, 0, 0, 6);"
        ));
        generateBtn.setOnMouseExited(e -> generateBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6, #a855f7);" +
            "-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px;" +
            "-fx-padding: 14 36; -fx-background-radius: 30; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.55), 18, 0, 0, 4);"
        ));
        generateBtn.setOnAction(e -> onGenerate());

        Button closeBtn = new Button("  ✕   Fermer  ");
        closeBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-text-fill: rgba(255,255,255,0.6);" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 14 24;" +
            "-fx-background-radius: 30;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: rgba(255,255,255,0.12);" +
            "-fx-border-radius: 30;" +
            "-fx-border-width: 1;"
        );
        closeBtn.setOnAction(e -> stage.close());

        footer.getChildren().addAll(generateBtn, closeBtn);
        return footer;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GENERATE LOGIC
    // ─────────────────────────────────────────────────────────────────────────
    private void onGenerate() {
        generateBtn.setDisable(true);
        generateBtn.setText("  ⏳   Génération en cours...");
        statusLabel.setText("🤖 L'IA analyse votre challenge...");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a78bfa;");

        // Show loading skeleton
        tasksContainer.getChildren().clear();
        for (int i = 0; i < 4; i++) {
            tasksContainer.getChildren().add(buildSkeletonCard(i));
        }

        CompletableFuture.supplyAsync(() ->
            aiTaskGenerator.generateTasksForChallenge(challenge)
        ).thenAcceptAsync(tasks -> {
            Platform.runLater(() -> {
                // Save tasks to database
                taskCrud.deleteByChallenge(challenge.getIdChallenge());
                for (Task t : tasks) {
                    taskCrud.create(t);
                }
                
                displayTasks(tasks);

                statusLabel.setText("✅ " + tasks.size() + " tâches générées");
                statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #34d399;");
                generateBtn.setText("  🔄   Régénérer");
                generateBtn.setDisable(false);
                
                // ⚡ Trigger Flash Challenge bonus
                showFlashChallengeBadge();
            });
        });
    }

    private void showFlashChallengeBadge() {
        Button flashBtn = new Button("⚡ DÉFI FLASH +200 PTS");
        flashBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #f59e0b, #ef4444);" +
            "-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 10px;" +
            "-fx-padding: 8 16; -fx-background-radius: 20; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.5), 10, 0, 0, 0);"
        );
        
        StackPane.setAlignment(flashBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(flashBtn, new Insets(20));
        
        mainStack.getChildren().add(flashBtn);
        
        // Transitions... (rest of logic same)
        TranslateTransition floatAnim = new TranslateTransition(Duration.millis(1000), flashBtn);
        floatAnim.setByY(10);
        floatAnim.setAutoReverse(true);
        floatAnim.setCycleCount(Animation.INDEFINITE);
        floatAnim.play();
        
        flashBtn.setOnAction(e -> showFlashChallengePopup(flashBtn));
    }

    private void showFlashChallengePopup(Button badge) {
        Stage flashStage = new Stage();
        flashStage.initModality(Modality.APPLICATION_MODAL);
        flashStage.initOwner(stage);
        flashStage.initStyle(StageStyle.TRANSPARENT);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0.85);");

        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 45, 40, 45));
        card.setMaxWidth(550);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1e293b, #0f172a);" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: linear-gradient(to bottom, #f97316, #dc2626);" +
                        "-fx-border-radius: 30;" +
                        "-fx-border-width: 3;" +
                        "-fx-effect: dropshadow(gaussian, rgba(249,115,22,0.5), 30, 0, 0, 5);"
        );

        card.setScaleX(0.3);
        card.setScaleY(0.3);
        card.setOpacity(0);

        // ── En-tête avec icône (identique à votre code) ──
        StackPane iconContainer = new StackPane();
        Circle pulseRing = new Circle(45);
        pulseRing.setFill(Color.TRANSPARENT);
        pulseRing.setStroke(Color.web("#f97316"));
        pulseRing.setStrokeWidth(4);
        pulseRing.setOpacity(0.4);

        Timeline ringPulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(pulseRing.scaleXProperty(), 1),
                        new KeyValue(pulseRing.scaleYProperty(), 1),
                        new KeyValue(pulseRing.opacityProperty(), 0.4)
                ),
                new KeyFrame(Duration.millis(1200),
                        new KeyValue(pulseRing.scaleXProperty(), 1.6),
                        new KeyValue(pulseRing.scaleYProperty(), 1.6),
                        new KeyValue(pulseRing.opacityProperty(), 0)
                )
        );
        ringPulse.setCycleCount(Animation.INDEFINITE);
        ringPulse.play();

        Circle bgCircle = new Circle(40);
        bgCircle.setFill(Color.web("rgba(249,115,22,0.2)"));

        Label iconLabel = new Label("⚡");
        iconLabel.setStyle("-fx-font-size: 56px; -fx-text-fill: #f97316;");

        RotateTransition rotate = new RotateTransition(Duration.millis(3000), iconLabel);
        rotate.setByAngle(15);
        rotate.setAutoReverse(true);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.play();

        iconContainer.getChildren().addAll(pulseRing, bgCircle, iconLabel);

        // ── Titre ──
        Label titleLabel = new Label("⚡ DÉFI FLASH ⚡");
        titleLabel.setStyle(
                "-fx-font-size: 28px; -fx-font-weight: 900;" +
                        "-fx-text-fill: linear-gradient(to right, #f97316, #dc2626);"
        );

        // ── Points bonus ──
        HBox pointsBox = new HBox(15);
        pointsBox.setAlignment(Pos.CENTER);
        pointsBox.setPadding(new Insets(20, 30, 20, 30));
        pointsBox.setStyle(
                "-fx-background-color: rgba(249,115,22,0.15);" +
                        "-fx-background-radius: 60;" +
                        "-fx-border-color: rgba(249,115,22,0.5);" +
                        "-fx-border-radius: 60;" +
                        "-fx-border-width: 2;"
        );

        Label starIcon = new Label("⭐");
        starIcon.setStyle("-fx-font-size: 32px;");

        VBox pointsGroup = new VBox(-8);
        pointsGroup.setAlignment(Pos.CENTER);

        Label pointsValue = new Label("+200");
        pointsValue.setStyle("-fx-font-size: 44px; -fx-font-weight: 900; -fx-text-fill: #fbbf24;");

        Label pointsText = new Label("POINTS BONUS");
        pointsText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.8);");

        pointsGroup.getChildren().addAll(pointsValue, pointsText);
        pointsBox.getChildren().addAll(starIcon, pointsGroup);

        // ── Description du défi (Améliorée pour s'adapter à la taille) ──
        // ── Description du défi (Améliorée pour s'adapter à la taille) ──
        // ── Description du défi (CORRIGÉE avec TextArea) ──
        VBox challengeBox = new VBox(15);
        challengeBox.setAlignment(Pos.CENTER);
        challengeBox.setPadding(new Insets(20));
        challengeBox.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: rgba(255,255,255,0.15);" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-width: 1;"
        );

        Label challengeLabel = new Label("🎯 VOTRE DÉFI EXPRESS :");
        challengeLabel.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: 900; -fx-text-fill: #f97316;"
        );

        Label challengeText = new Label(
                "Écrivez 3 objectifs concrets pour demain\nen lien avec ce challenge.\n\n" +
                        "Exemples :\n" +
                        "✨ Finaliser la première étape\n" +
                        "✨ Faire une recherche approfondie\n" +
                        "✨ Contacter un expert"
        );
        challengeText.setWrapText(true);
        challengeText.setTextAlignment(TextAlignment.CENTER);
        challengeText.setMaxWidth(450);
        challengeText.setMinHeight(Region.USE_PREF_SIZE); // Force à afficher tout le texte
        challengeText.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-style: italic;" +
                        "-fx-line-spacing: 5;"
        );

        challengeBox.getChildren().addAll(challengeLabel, challengeText);

        // ── Timer Dynamique (05:00 -> 00:00) ──
        HBox timerBox = new HBox(15);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.setPadding(new Insets(10, 25, 10, 25));
        timerBox.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 40;");

        Label timerIcon = new Label("⏱️");
        timerIcon.setStyle("-fx-font-size: 24px;");

        Label timerLabel = new Label("05:00");
        timerLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #f97316; -fx-font-family: 'Monospaced';");

        timerBox.getChildren().addAll(timerIcon, timerLabel);

        // LOGIQUE DU TIMER
        int[] timeSeconds = {300}; // 5 minutes
        Timeline timerTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    timeSeconds[0]--;
                    int mins = timeSeconds[0] / 60;
                    int secs = timeSeconds[0] % 60;
                    timerLabel.setText(String.format("%02d:%02d", mins, secs));

                    if (timeSeconds[0] <= 10) {
                        timerLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #ef4444; -fx-font-family: 'Monospaced';");
                    }

                    if (timeSeconds[0] <= 0) {
                        flashStage.close();
                    }
                })
        );
        timerTimeline.setCycleCount(300);
        timerTimeline.play();

        // ── Boutons ──
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button acceptBtn = new Button("⚡  RELEVER LE DÉFI  ⚡");
        acceptBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #f97316, #dc2626);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 16 35;" +
                        "-fx-background-radius: 40;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.5), 20, 0, 0, 4);"
        );

        acceptBtn.setOnMouseEntered(e -> acceptBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #ea580c, #b91c1c);" +
                        "-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 16px;" +
                        "-fx-padding: 16 35; -fx-background-radius: 40; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(185,28,28,0.7), 25, 0, 0, 6);"
        ));
        acceptBtn.setOnMouseExited(e -> acceptBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #f97316, #dc2626);" +
                        "-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 16px;" +
                        "-fx-padding: 16 35; -fx-background-radius: 40; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.5), 20, 0, 0, 4);"
        ));

        Button declineBtn = new Button("✕  PASSER");
        declineBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1);" +
                        "-fx-text-fill: rgba(255,255,255,0.7);" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 25;" +
                        "-fx-background-radius: 40;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: rgba(255,255,255,0.2);" +
                        "-fx-border-radius: 40;"
        );

        buttonBox.getChildren().addAll(acceptBtn, declineBtn);

        // ── Assemblage ──
        card.getChildren().addAll(
                iconContainer,
                titleLabel,
                pointsBox,
                challengeBox,
                timerBox,
                buttonBox
        );

        root.getChildren().add(card);

        // ── Actions ──
        acceptBtn.setOnAction(e -> {
            timerTimeline.stop(); // Arrêter le timer
            totalPoints += 200;
            animatePointsUpdate();

            Label successLabel = new Label("✅ DÉFI RELEVÉ ! +200 PTS");
            successLabel.setStyle(
                    "-fx-background-color: #059669;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: 900;" +
                            "-fx-font-size: 16px;" +
                            "-fx-padding: 15 25;" +
                            "-fx-background-radius: 40;" +
                            "-fx-effect: dropshadow(gaussian, #059669, 15, 0, 0, 2);"
            );

            mainStack.getChildren().add(successLabel);
            StackPane.setAlignment(successLabel, Pos.TOP_CENTER);
            StackPane.setMargin(successLabel, new Insets(80, 0, 0, 0));

            FadeTransition ft = new FadeTransition(Duration.millis(2000), successLabel);
            ft.setFromValue(1); ft.setToValue(0);
            ft.setOnFinished(ev -> mainStack.getChildren().remove(successLabel));
            ft.play();

            statusLabel.setText("🔥 DÉFI FLASH RÉUSSI ! +200 PTS");
            badge.setVisible(false);
            flashStage.close();
        });

        declineBtn.setOnAction(e -> {
            timerTimeline.stop();
            flashStage.close();
        });

        // ── Scène et animation ──
        Scene scene = new Scene(root, 600, 750);
        scene.setFill(Color.TRANSPARENT);
        flashStage.setScene(scene);
        flashStage.show();

        // Animation d'entrée
        ScaleTransition st = new ScaleTransition(Duration.millis(400), card);
        st.setFromX(0.3); st.setFromY(0.3);
        st.setToX(1.0); st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setFromValue(0); ft.setToValue(1);

        ParallelTransition pt = new ParallelTransition(st, ft);
        pt.play();

        // Démarrer le timer
        timerTimeline.play();
    }
    // ─────────────────────────────────────────────────────────────────────────
    // TASK CARD
    // ─────────────────────────────────────────────────────────────────────────
    private VBox buildTaskCard(Task task, int index, int total) {
        String accent = getDifficultyAccent(task.getDifficulty());
        String bgGrad = getDifficultyGradient(task.getDifficulty());
        String glowColor = getDifficultyGlow(task.getDifficulty());

        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: " + bgGrad + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + accent + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 0 0 0 4;" +
            "-fx-effect: dropshadow(gaussian, " + glowColor + ", 12, 0, 0, 3);"
        );

        // ── Top accent bar ──
        HBox topBar = new HBox(0);
        topBar.setPrefHeight(3);
        topBar.setStyle(
            "-fx-background-color: linear-gradient(to right, " + accent + ", transparent);" +
            "-fx-background-radius: 16 16 0 0;"
        );

        // ── Card body ──
        VBox body = new VBox(12);
        body.setPadding(new Insets(16, 18, 16, 18));

        // Header row
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Step number
        StackPane stepBadge = new StackPane();
        Circle stepCircle = new Circle(16);
        stepCircle.setFill(Color.web(accent + "33"));
        stepCircle.setStroke(Color.web(accent));
        stepCircle.setStrokeWidth(1.5);
        Label stepNum = new Label(String.valueOf(index));
        stepNum.setStyle("-fx-font-size: 11px; -fx-font-weight: 900; -fx-text-fill: " + accent + ";");
        stepBadge.getChildren().addAll(stepCircle, stepNum);

        // Icon
        Label iconLabel = new Label(task.getIcon());
        iconLabel.setStyle("-fx-font-size: 22px;");

        // Title
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 900; -fx-text-fill: white;");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Difficulty pill
        Label diffPill = new Label(task.getDifficulty());
        diffPill.setStyle(
            "-fx-background-color: " + accent + "22;" +
            "-fx-text-fill: " + accent + ";" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 4 10;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + accent + "55;" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 1;"
        );

        // Points badge
        HBox pointsBadge = new HBox(4);
        pointsBadge.setAlignment(Pos.CENTER);
        pointsBadge.setPadding(new Insets(4, 10, 4, 10));
        pointsBadge.setStyle(
            "-fx-background-color: rgba(251,191,36,0.12);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: rgba(251,191,36,0.3);" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 1;"
        );
        Label starIcon = new Label("⭐");
        starIcon.setStyle("-fx-font-size: 11px;");
        Label ptsLabel = new Label(task.getPoints() + " pts");
        ptsLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #fbbf24;");
        pointsBadge.getChildren().addAll(starIcon, ptsLabel);

        headerRow.getChildren().addAll(stepBadge, iconLabel, titleLabel, diffPill, pointsBadge);

        // Description
        Label descLabel = new Label(task.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.6); -fx-line-spacing: 2;");
        descLabel.setWrapText(true);

        // Separator
        Rectangle sep = new Rectangle();
        sep.setHeight(1);
        sep.setFill(Color.web("rgba(255,255,255,0.06)"));
        sep.widthProperty().bind(body.widthProperty().subtract(36));

        // Footer row
        HBox footerRow = new HBox(12);
        footerRow.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("📅  " + task.getDueDate().toString());
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.3);");

        // Progress indicator (step X of N)
        Label stepOf = new Label("Étape " + index + " sur " + total);
        stepOf.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.25);");

        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);

        // Complete checkbox — styled as a toggle button
        CheckBox check = new CheckBox();
        check.setStyle("-fx-font-size: 0px;");

        HBox completeBtn = new HBox(8);
        completeBtn.setAlignment(Pos.CENTER);
        completeBtn.setPadding(new Insets(7, 16, 7, 16));
        completeBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: rgba(255,255,255,0.15);" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        Label checkIcon = new Label("○");
        checkIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.4);");
        Label checkText = new Label("Marquer terminé");
        checkText.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.5);");
        completeBtn.getChildren().addAll(checkIcon, checkText);

        // Click handler on the whole button
        completeBtn.setOnMouseClicked(e -> {
            if (!task.isCompleted()) {
                task.setCompleted(true);
                totalPoints += task.getPoints();
                completedTasks++;

                // Update button to "done" state
                completeBtn.setStyle(
                    "-fx-background-color: rgba(52,211,153,0.15);" +
                    "-fx-background-radius: 20;" +
                    "-fx-border-color: rgba(52,211,153,0.4);" +
                    "-fx-border-radius: 20;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: default;"
                );
                checkIcon.setText("✓");
                checkIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #34d399;");
                checkText.setText("Terminé !");
                checkText.setStyle("-fx-font-size: 11px; -fx-text-fill: #34d399;");
                completeBtn.setOnMouseClicked(null);

                // Dim the card slightly
                card.setOpacity(0.72);

                // Update points with animation
                animatePointsUpdate();

                // Update progress
                updateProgress(completedTasks, generatedTasks.size());
                
                // Sync with DB
                taskCrud.update(task);

                // 🎉 Show cartoon kid congrats popup for this task
                boolean isLastTask = (completedTasks == generatedTasks.size());
                showTaskCongratsPopup(task, isLastTask);
            }
        });

        footerRow.getChildren().addAll(dateLabel, stepOf, fSpacer, completeBtn);
        body.getChildren().addAll(headerRow, descLabel, sep, footerRow);
        card.getChildren().addAll(topBar, body);

        // Apply completed state if loaded from DB
        if (task.isCompleted()) {
            completeBtn.setStyle(
                "-fx-background-color: rgba(52,211,153,0.15);" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: rgba(52,211,153,0.4);" +
                "-fx-border-radius: 20;" +
                "-fx-border-width: 1;" +
                "-fx-cursor: default;"
            );
            checkIcon.setText("✓");
            checkIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #34d399;");
            checkText.setText("Terminé !");
            checkText.setStyle("-fx-font-size: 11px; -fx-text-fill: #34d399;");
            completeBtn.setOnMouseClicked(null);
            card.setOpacity(0.72);
        }

        // Hover glow
        card.setOnMouseEntered(ev -> {
            if (!task.isCompleted()) {
                card.setStyle(
                    "-fx-background-color: " + bgGrad + ";" +
                    "-fx-background-radius: 16;" +
                    "-fx-border-color: " + accent + ";" +
                    "-fx-border-radius: 16;" +
                    "-fx-border-width: 0 0 0 4;" +
                    "-fx-effect: dropshadow(gaussian, " + glowColor + ", 20, 0, 0, 5);"
                );
            }
        });
        card.setOnMouseExited(ev -> {
            if (!task.isCompleted()) {
                card.setStyle(
                    "-fx-background-color: " + bgGrad + ";" +
                    "-fx-background-radius: 16;" +
                    "-fx-border-color: " + accent + ";" +
                    "-fx-border-radius: 16;" +
                    "-fx-border-width: 0 0 0 4;" +
                    "-fx-effect: dropshadow(gaussian, " + glowColor + ", 12, 0, 0, 3);"
                );
            }
        });

        // ✅ Double-clic pour voir la description complète
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                showTaskDescriptionPopup(task);
            }
        });
        card.setCursor(Cursor.HAND);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SKELETON LOADING CARD
    // ─────────────────────────────────────────────────────────────────────────
    private VBox buildSkeletonCard(int index) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255,255,255,0.06);" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;"
        );

        // Shimmer animation
        HBox shimmerRow = new HBox(12);
        shimmerRow.setAlignment(Pos.CENTER_LEFT);

        Rectangle circle = new Rectangle(32, 32);
        circle.setArcWidth(32); circle.setArcHeight(32);
        circle.setFill(Color.web("rgba(255,255,255,0.07)"));

        Rectangle titleBar = new Rectangle(0, 14);
        titleBar.setFill(Color.web("rgba(255,255,255,0.07)"));
        titleBar.setArcWidth(7); titleBar.setArcHeight(7);
        titleBar.widthProperty().bind(card.widthProperty().multiply(0.5));

        shimmerRow.getChildren().addAll(circle, titleBar);

        Rectangle descBar1 = new Rectangle(0, 10);
        descBar1.setFill(Color.web("rgba(255,255,255,0.05)"));
        descBar1.setArcWidth(5); descBar1.setArcHeight(5);
        descBar1.widthProperty().bind(card.widthProperty().multiply(0.85));

        Rectangle descBar2 = new Rectangle(0, 10);
        descBar2.setFill(Color.web("rgba(255,255,255,0.04)"));
        descBar2.setArcWidth(5); descBar2.setArcHeight(5);
        descBar2.widthProperty().bind(card.widthProperty().multiply(0.65));

        card.getChildren().addAll(shimmerRow, descBar1, descBar2);

        // Shimmer fade animation
        FadeTransition ft = new FadeTransition(Duration.millis(800), card);
        ft.setFromValue(0.3); ft.setToValue(0.8);
        ft.setAutoReverse(true);
        ft.setCycleCount(Animation.INDEFINITE);
        ft.setDelay(Duration.millis(index * 150));
        ft.play();

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REWARD POPUP
    // ─────────────────────────────────────────────────────────────────────────
    private void showRewardPopup() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(stage);
        popup.setTitle("🎉 Challenge Complété !");

        // Root
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #080814;");

        // Background glow orbs
        StackPane bgOrbs = new StackPane();
        Circle orb1 = new Circle(200);
        orb1.setFill(Color.web("rgba(99,102,241,0.08)"));
        orb1.setTranslateX(-150); orb1.setTranslateY(-100);
        Circle orb2 = new Circle(150);
        orb2.setFill(Color.web("rgba(251,191,36,0.06)"));
        orb2.setTranslateX(150); orb2.setTranslateY(120);
        bgOrbs.getChildren().addAll(orb1, orb2);

        // Main card
        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(50, 55, 45, 55));
        card.setMaxWidth(560);
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #12122a, #0d0d1a);" +
            "-fx-background-radius: 28;" +
            "-fx-border-color: rgba(251,191,36,0.35);" +
            "-fx-border-radius: 28;" +
            "-fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(gaussian, rgba(251,191,36,0.2), 40, 0, 0, 0);"
        );

        // Confetti emojis with bounce
        Label confetti = new Label("🎉  🏆  🎊  ✨  🎁");
        confetti.setStyle("-fx-font-size: 32px;");
        ScaleTransition confettiBounce = new ScaleTransition(Duration.millis(500), confetti);
        confettiBounce.setFromX(0.5); confettiBounce.setFromY(0.5);
        confettiBounce.setToX(1.0); confettiBounce.setToY(1.0);
        confettiBounce.setInterpolator(Interpolator.EASE_OUT);
        confettiBounce.play();

        // Title
        Label titleLabel = new Label("CHALLENGE COMPLÉTÉ !");
        titleLabel.setStyle(
            "-fx-font-size: 30px; -fx-font-weight: 900; -fx-text-fill: #fbbf24;" +
            "-fx-effect: dropshadow(gaussian, rgba(251,191,36,0.6), 15, 0, 0, 0);"
        );

        // Points earned
        HBox pointsRow = new HBox(10);
        pointsRow.setAlignment(Pos.CENTER);
        pointsRow.setPadding(new Insets(10, 24, 10, 24));
        pointsRow.setStyle(
            "-fx-background-color: rgba(167,139,250,0.1);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: rgba(167,139,250,0.3);" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 1;"
        );
        Label starLbl = new Label("⭐");
        starLbl.setStyle("-fx-font-size: 20px;");
        Label pointsEarned = new Label(totalPoints + " points gagnés !");
        pointsEarned.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #a78bfa;");
        pointsRow.getChildren().addAll(starLbl, pointsEarned);

        // Subtitle
        Label subtitle = new Label("Vous avez débloqué les récompenses suivantes :");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.5);");

        // Rewards grid
        FlowPane rewardsGrid = new FlowPane(14, 14);
        rewardsGrid.setAlignment(Pos.CENTER);

        for (int i = 0; i < recompenses.size(); i++) {
            Recompense r = recompenses.get(i);
            VBox rewardCard = buildPopupRewardCard(r, i);
            rewardsGrid.getChildren().add(rewardCard);
        }

        // Collect button
        Button collectBtn = new Button("  🎁   COLLECTER LES RÉCOMPENSES  ");
        collectBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #f59e0b, #d97706, #b45309);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 900;" +
            "-fx-font-size: 15px;" +
            "-fx-padding: 16 40;" +
            "-fx-background-radius: 30;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.6), 20, 0, 0, 4);"
        );
        collectBtn.setOnMouseEntered(e -> collectBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #d97706, #b45309, #92400e);" +
            "-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 15px;" +
            "-fx-padding: 16 40; -fx-background-radius: 30; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.8), 26, 0, 0, 6);"
        ));
        collectBtn.setOnMouseExited(e -> collectBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #f59e0b, #d97706, #b45309);" +
            "-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 15px;" +
            "-fx-padding: 16 40; -fx-background-radius: 30; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.6), 20, 0, 0, 4);"
        ));
        collectBtn.setOnAction(e -> { popup.close(); stage.close(); });

        // Share button
        Button shareBtn = new Button("  🔗   GÉNÉRER MA CARTE DE RÉUSSITE  ");
        shareBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;" +
            "-fx-padding: 12 30; -fx-background-radius: 30; -fx-cursor: hand;" +
            "-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 30;"
        );
        shareBtn.setOnAction(e -> generateSuccessCard(card));

        card.getChildren().addAll(confetti, titleLabel, pointsRow, subtitle, rewardsGrid, collectBtn, shareBtn);
        root.getChildren().addAll(bgOrbs, card);

        Scene scene = new Scene(root, 600, 650);
        popup.setScene(scene);

        // Entrance animation
        card.setScaleX(0.4); card.setScaleY(0.4); card.setOpacity(0);
        popup.show();

        ScaleTransition st = new ScaleTransition(Duration.millis(550), card);
        st.setFromX(0.4); st.setFromY(0.4);
        st.setToX(1.0); st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setFromValue(0); ft.setToValue(1);

        new ParallelTransition(st, ft).play();
    }

    private VBox buildPopupRewardCard(Recompense r, int index) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setMinWidth(140);
        card.setStyle(
            "-fx-background-color: rgba(251,191,36,0.08);" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: rgba(251,191,36,0.3);" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(251,191,36,0.15), 12, 0, 0, 3);"
        );

        Label iconLabel = new Label(getIconForReward(r.getTypeRecompense()));
        iconLabel.setStyle("-fx-font-size: 38px;");

        Label nameLabel = new Label(r.getTitre());
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #fbbf24;");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setAlignment(Pos.CENTER);

        Label typeLabel = new Label(r.getTypeRecompense().toUpperCase());
        typeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(251,191,36,0.5);");

        // Unlock animation with delay
        card.setOpacity(0);
        PauseTransition delay = new PauseTransition(Duration.millis(300 + index * 120));
        delay.setOnFinished(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(400), card);
            ft.setFromValue(0); ft.setToValue(1);
            ScaleTransition st = new ScaleTransition(Duration.millis(400), card);
            st.setFromX(0.7); st.setFromY(0.7);
            st.setToX(1.0); st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(ft, st).play();
        });
        delay.play();

        card.getChildren().addAll(iconLabel, nameLabel, typeLabel);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private void updateProgress(int done, int total) {
        double pct = total == 0 ? 0 : (double) done / total;
        globalProgress.setProgress(pct);
        int pctInt = (int) Math.round(pct * 100);
        progressPercent.setText(pctInt + "%");
        statusLabel.setText(done + " / " + total + " tâches complétées");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (done == total && total > 0 ? "#34d399" : "#a78bfa") + ";");
    }

    private void animatePointsUpdate() {
        // Bounce animation on the points label
        ScaleTransition st = new ScaleTransition(Duration.millis(200), pointsTotalLabel);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.3); st.setToY(1.3);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.setOnFinished(e -> pointsTotalLabel.setText(String.valueOf(totalPoints)));
        st.play();
    }

    private StackPane buildGlowAvatar(String emoji, String color1, String color2) {
        StackPane pane = new StackPane();
        pane.setMinSize(46, 46);
        pane.setMaxSize(46, 46);

        Circle bg = new Circle(23);
        bg.setFill(Color.web(color1 + "33"));
        bg.setStroke(Color.web(color1));
        bg.setStrokeWidth(1.5);

        DropShadow glow = new DropShadow(12, Color.web(color2));
        bg.setEffect(glow);

        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 20px;");

        pane.getChildren().addAll(bg, icon);

        // Pulse glow animation
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 8)),
            new KeyFrame(Duration.millis(1500), new KeyValue(glow.radiusProperty(), 18))
        );
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        return pane;
    }

    private String getDifficultyAccent(String d) {
        switch (d) {
            case "Facile":    return "#34d399";
            case "Moyen":     return "#fbbf24";
            case "Difficile": return "#f87171";
            default:          return "#818cf8";
        }
    }

    private String getDifficultyGradient(String d) {
        switch (d) {
            case "Facile":    return "linear-gradient(to right, rgba(52,211,153,0.10), rgba(52,211,153,0.04))";
            case "Moyen":     return "linear-gradient(to right, rgba(251,191,36,0.10), rgba(251,191,36,0.04))";
            case "Difficile": return "linear-gradient(to right, rgba(248,113,113,0.10), rgba(248,113,113,0.04))";
            default:          return "linear-gradient(to right, rgba(129,140,248,0.10), rgba(129,140,248,0.04))";
        }
    }

    private String getDifficultyGlow(String d) {
        switch (d) {
            case "Facile":    return "rgba(52,211,153,0.25)";
            case "Moyen":     return "rgba(251,191,36,0.25)";
            case "Difficile": return "rgba(248,113,113,0.25)";
            default:          return "rgba(129,140,248,0.25)";
        }
    }

    private String getIconForReward(String type) {
        if (type == null) return "🏆";
        switch (type.toLowerCase()) {
            case "badge":      return "🎖️";
            case "certificat": return "📜";
            case "titre":      return "👑";
            case "bonus":      return "🎁";
            case "médaille":   return "🥇";
            default:           return "🏆";
        }
    }

    private void showTaskCongratsPopup(Task task, boolean isLastTask) {
        Stage congratsPopup = new Stage();
        congratsPopup.initModality(Modality.APPLICATION_MODAL);
        congratsPopup.initOwner(stage);
        congratsPopup.setTitle("Bravo !");

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #1e293b, #0f172a);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: #38bdf8;" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 2;"
        );

        // Cartoon Kid (using Emoji and Shapes)
        StackPane kidContainer = new StackPane();
        Label kidLabel = new Label("👦");
        kidLabel.setStyle("-fx-font-size: 80px;");
        
        Label starsLabel = new Label("✨ ✨ ✨");
        starsLabel.setStyle("-fx-font-size: 40px;");
        starsLabel.setTranslateY(-40);

        kidContainer.getChildren().addAll(starsLabel, kidLabel);

        // Animation for the kid (Jump)
        TranslateTransition jump = new TranslateTransition(Duration.millis(400), kidContainer);
        jump.setByY(-30);
        jump.setAutoReverse(true);
        jump.setCycleCount(4);
        jump.play();

        Label congratsMsg = new Label("FÉLICITATIONS !");
        congratsMsg.setStyle(
            "-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #38bdf8;" +
            "-fx-effect: dropshadow(gaussian, rgba(56,189,248,0.6), 10, 0, 0, 0);"
        );

        Label taskLabel = new Label("Tâche terminée : " + task.getTitle());
        taskLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        taskLabel.setWrapText(true);
        taskLabel.setTextAlignment(TextAlignment.CENTER);

        HBox pointsGained = new HBox(5);
        pointsGained.setAlignment(Pos.CENTER);
        Label pIcon = new Label("⭐");
        Label pVal = new Label("+" + task.getPoints() + " points !");
        pVal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #fbbf24;");
        pointsGained.getChildren().addAll(pIcon, pVal);

        root.getChildren().addAll(kidContainer, congratsMsg, taskLabel, pointsGained);

        Scene scene = new Scene(root, 350, 400);
        scene.setFill(null); // Allows for rounded corners if stage is transparent
        congratsPopup.setScene(scene);

        // Auto-close after 2.5 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(2.5));
        delay.setOnFinished(e -> {
            congratsPopup.close();
            if (isLastTask) {
                showRewardPopup();
            }
        });

        // Show with animation
        root.setOpacity(0);
        root.setScaleX(0.5);
        root.setScaleY(0.5);

        congratsPopup.show();

        FadeTransition ft = new FadeTransition(Duration.millis(300), root);
        ft.setToValue(1);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), root);
        st.setToX(1);
        st.setToY(1);

        new ParallelTransition(ft, st).play();
        delay.play();
    }

    private void showTaskDescriptionPopup(Task task) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(stage);
        popup.initStyle(StageStyle.TRANSPARENT);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0.7);"); // Dim background
        root.setPadding(new Insets(50));

        VBox card = new VBox(25);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(35));
        card.setMaxWidth(600);
        card.setPrefWidth(600);
        
        String accent = getDifficultyAccent(task.getDifficulty());
        String bgGrad = "linear-gradient(to bottom right, #1e293b, #0f172a)";

        card.setStyle(
            "-fx-background-color: " + bgGrad + ";" +
            "-fx-background-radius: 30;" +
            "-fx-border-color: " + accent + "55;" +
            "-fx-border-radius: 30;" +
            "-fx-border-width: 2;" +
            "-fx-effect: dropshadow(gaussian, " + accent + "44, 40, 0, 0, 5);"
        );

        // Header with Icon and Title
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(task.getIcon());
        iconLabel.setStyle("-fx-font-size: 45px;");
        
        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: white;");
        titleLabel.setWrapText(true);
        Label categoryLabel = new Label("OBJECTIF DU CHALLENGE : " + challenge.getTitre().toUpperCase());
        categoryLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + accent + "; -fx-letter-spacing: 1px;");
        titleBox.getChildren().addAll(categoryLabel, titleLabel);
        
        header.getChildren().addAll(iconLabel, titleBox);

        // Stats Row
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
            createPopupStat("🎯 Difficulté", task.getDifficulty(), accent),
            createPopupStat("⭐ Récompense", task.getPoints() + " Points", "#fbbf24"),
            createPopupStat("📅 Échéance", task.getDueDate().toString(), "#38bdf8")
        );

        // Separator
        Separator sep = new Separator();
        sep.setOpacity(0.1);

        // Description content
        VBox descContainer = new VBox(15);
        Label descHeader = new Label("📝 DESCRIPTION DÉTAILLÉE");
        descHeader.setStyle("-fx-font-size: 12px; -fx-font-weight: 900; -fx-text-fill: rgba(255,255,255,0.4); -fx-letter-spacing: 1px;");
        
        Label fullDesc = new Label(task.getDescription());
        fullDesc.setWrapText(true);
        fullDesc.setStyle("-fx-font-size: 15px; -fx-text-fill: rgba(255,255,255,0.85); -fx-line-spacing: 6;");
        fullDesc.setLineSpacing(6);
        
        descContainer.getChildren().addAll(descHeader, fullDesc);
        
        // Scroll pane for long descriptions
        ScrollPane scroll = new ScrollPane(descContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefHeight(250);

        // Close button
        Button closeBtn = new Button("COMPRIS, JE M'EN OCCUPE !");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, " + accent + ", " + accent + "cc);" +
            "-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px;" +
            "-fx-padding: 15; -fx-background-radius: 15; -fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> popup.close());
        
        // Dismiss on clicking background
        root.setOnMouseClicked(e -> {
            if (e.getTarget() == root) popup.close();
        });

        card.getChildren().addAll(header, statsRow, sep, scroll, closeBtn);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 700, 600);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.show();

        // Animation
        card.setOpacity(0);
        card.setTranslateY(20);
        FadeTransition ft = new FadeTransition(Duration.millis(300), card);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), card);
        tt.setByY(-20);
        new ParallelTransition(ft, tt).play();
    }

    private VBox createPopupStat(String label, String value, String color) {
        VBox box = new VBox(4);
        Label lbl = new Label(label.toUpperCase());
        lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: 900; -fx-text-fill: rgba(255,255,255,0.3);");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        box.getChildren().addAll(lbl, val);
        box.setPadding(new Insets(10, 15, 10, 15));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 12;");
        return box;
    }

    private void generateSuccessCard(VBox rewardCard) {
        try {
            javafx.scene.image.WritableImage image = rewardCard.snapshot(new javafx.scene.SnapshotParameters(), null);
            File file = new File(System.getProperty("user.home") + "/Desktop/SuccessCard_" + challenge.getTitre().replaceAll(" ", "_") + ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Carte générée !");
            alert.setHeaderText("FÉLICITATIONS !");
            alert.setContentText("Votre carte de réussite a été enregistrée sur votre bureau :\n" + file.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
