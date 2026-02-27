package org.example.controller.motivation;

import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.util.Session;
import org.example.dao.motivation.ChallengeCrud;
import org.example.dao.motivation.ChallengeCoachCrud;
import org.example.dao.motivation.CoachMotivationCrud;
import org.example.dao.motivation.RecompenseCrud;
import org.example.dao.motivation.CrudCoach;
import org.example.dao.motivation.CollaborativeCrud;
import org.example.dao.motivation.TaskCrud;
import org.example.model.motivation.Challenge;
import org.example.model.motivation.Recompense;
import org.example.model.motivation.CoachMotivation;
import org.example.model.motivation.Team;
import org.example.model.motivation.TeamMember;
import org.example.model.motivation.ChatMessage;
import org.example.model.motivation.Task;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;



// Nouveaux imports pour l'export PDF professionnel
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import javafx.scene.control.Tooltip;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.scene.shape.*;
import javafx.scene.paint.Color;
import javafx.scene.Cursor;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.Cursor;
import javafx.scene.shape.Circle;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.geometry.Orientation;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import java.util.ArrayList;
import java.util.Comparator;
import org.example.utils.EmailSender;

// Ajoutez ces imports avec les autres imports JavaFX
import javafx.animation.ScaleTransition;
import javafx.animation.Animation;
import javafx.util.Duration;

// Ajoutez ces imports avec les autres imports JavaFX
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.effect.DropShadow;
import java.util.Optional;


import javafx.scene.layout.BorderPane;


public class ChallengeController implements Initializable {

    /* ================= TABLE ================= */
    @FXML
    private ListView<Challenge> challengeListView;
    @FXML
    private TableColumn<Challenge, Integer> idColumn;
    @FXML
    private TableColumn<Challenge, String> titleColumn;
    @FXML
    private TableColumn<Challenge, String> descColumn;
    @FXML
    private TableColumn<Challenge, Integer> dureeColumn;
    @FXML
    private TableColumn<Challenge, String> difficulteColumn;
    @FXML
    private TableColumn<Challenge, String> typeColumn;
    @FXML
    private TableColumn<Challenge, Boolean> statusColumn;
    @FXML
    private TableColumn<Challenge, String> recompensesColumn;
    @FXML
    private TableColumn<Challenge, Void> actionsColumn;


    /* ================= FORM ================= */
    @FXML
    private TextField searchField;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descField;
    @FXML
    private TextField dureeField;
    @FXML
    private ComboBox<String> difficulteField;
    @FXML
    private ComboBox<String> typeField;
    @FXML
    private CheckBox actifField;
    @FXML
    private TableColumn<Challenge, String> coachesColumn;

    @FXML
    private Button searchBtn;
    @FXML
    private Button addBtn;
    @FXML
    private Button updateBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private Button coachBtn;
    @FXML
    private Button recBtn;
    @FXML
    private Button exportBtn;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button homeBtn;


    @FXML
    private Label totalLabel;
    @FXML
    private Label actifsLabel;
    @FXML
    private Label inactifsLabel;


    @FXML private HBox sortBox;
    @FXML private Label streakLabel;
    private ToggleGroup sortToggleGroup;
    private String currentSortType = "date_desc";

    /* ================= DATA ================= */
    //flexible au modification
    private final ObservableList<Challenge> challengeList = FXCollections.observableArrayList();
    private final ChallengeCrud challengeCrud = new ChallengeCrud();
    private final CrudCoach.ChallengeRecompenseCrud challengeRecompenseCrud = new CrudCoach.ChallengeRecompenseCrud();


    private final ChallengeCoachCrud challengeCoachCrud = new ChallengeCoachCrud();
    private final CollaborativeCrud collaborativeCrud = new CollaborativeCrud();
    private final TaskCrud taskCrud = new TaskCrud();

    /* ================= INIT ================= */

    //Méthode appelée automatiquement par JavaFX après le chargement du fichier FXML
    @Override

    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INITIALIZATION START ===");
        System.out.println("totalLabel is null? " + (totalLabel == null));
        System.out.println("actifsLabel is null? " + (actifsLabel == null));
        System.out.println("inactifsLabel is null? " + (inactifsLabel == null));

        setupListView();
        setupForm();
        setupButtons();
        setupInputValidation();
        setupSortControls(); // AJOUTER CETTE LIGNE
        loadData();
        updateStatistics();
        setupStreak();

        loadDataWithoutStats();

        System.out.println("=== INITIALIZATION END ===");
    }

    private void setupStreak() {
        if (streakLabel == null) return;
        org.example.utils.SparksManager sparks = org.example.utils.SparksManager.getInstance();
        streakLabel.setText(String.valueOf(sparks.getStreakCount()));

        // Pulse animation for the flame
        ScaleTransition pulse = new ScaleTransition(Duration.millis(800), streakLabel.getParent());
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.05); pulse.setToY(1.05);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }
    //charger les données des challenges dans l’interface graphique, sans calculer de statistiques.
    private void loadDataWithoutStats() {
        try {
            //recuper readAll de challengeCrud et le met en challengelist
            challengeList.setAll(challengeCrud.readAll());
            //le listview recupere les donné de challengelist
            challengeListView.setItems(challengeList);
            System.out.println("Chargé " + challengeList.size() + " challenges");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*configure un ListView JavaFX pour afficher des Challenge
     sous forme de cartes personnalisées avec du style, des boutons et des interactions.*/
    private void setupListView() {
        //setCellFactory permet de personnaliser l’apparence et le comportement de chaque cellule dans le ListView
        //on crée une classe anonyme qui hérite de ListCell<Challenge> pour définir le rendu des challenges.
        challengeListView.setCellFactory(param -> new ListCell<Challenge>() {
            //Hbox:conteneur pour organiser les element horizentallement
            //Vbox:conteneur pour organiser les element verticallement
            private final HBox card = new HBox(15);
            private final VBox content = new VBox(8);
            private final HBox header = new HBox(10);
            private final HBox infoRow = new HBox(15);
            private final HBox bottomRow = new HBox(10);
            private final VBox rightColumn = new VBox(5);

            private final Label titleLabel = new Label();
            private final Label statusLabel = new Label();
            private final Label descLabel = new Label();
            private final Label dureeLabel = new Label();
            private final Label diffLabel = new Label();
            private final Label typeLabel = new Label();
            private final Label coachesLabel = new Label();
            private final Label rewardsLabel = new Label();

            private final HBox actionBox = new HBox(5);
            private final Button coachesBtn = new Button("👨‍🏫");
            private final Button rewardsBtn = new Button("🎁");
            private final Button detailsBtn = new Button("👁️");
            private final Button tasksBtn = new Button("🤖 Tasks");
            private final Button qrCodeBtn = new Button("📱 QR");
            private final Button teamBtn = new Button("🤝 Team");

            {
                // Configuration initiale des composants

                // Style de la carte principale
                card.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 15; " +
                        "-fx-border-color: rgba(255,255,255,0.1); " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1; " +
                        "-fx-cursor: hand;");
                //contenu aligné à gauche.
                card.setAlignment(Pos.CENTER_LEFT);
                //la carte peut s’étendre pour remplir horizontalement.
                card.setMaxWidth(Double.MAX_VALUE);

                // Configuration du header
                header.setAlignment(Pos.CENTER_LEFT);

                titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: black;");
                //evite le debordement(text retoure a la ligne)
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(250);

                statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10;");

                // Description
                descLabel.setWrapText(true);
                descLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
                descLabel.setMaxWidth(350);
                descLabel.setMaxHeight(40);

                // Configuration de la ligne d'info
                infoRow.setAlignment(Pos.CENTER_LEFT);

                dureeLabel.setStyle("-fx-text-fill: #3498DB; -fx-font-size: 11px; -fx-font-weight: bold;");
                diffLabel.setStyle("-fx-text-fill: #9B59B6; -fx-font-size: 11px; -fx-font-weight: bold;");
                typeLabel.setStyle("-fx-text-fill: #E67E22; -fx-font-size: 11px; -fx-font-weight: bold;");

                // Configuration de la ligne du bas
                bottomRow.setAlignment(Pos.CENTER_LEFT);
                bottomRow.setSpacing(15);

                // Configuration des boutons
                detailsBtn.setStyle("-fx-background-color: linear-gradient(to right, #3498DB, #2980B9); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 5 10; " +
                        "-fx-font-size: 11px; -fx-cursor: hand;");
                detailsBtn.setTooltip(new Tooltip("Voir les détails du challenge"));

                coachesBtn.setStyle("-fx-background-color: linear-gradient(to right, #3498DB, #2980B9); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 5 10; " +
                        "-fx-font-size: 11px; -fx-cursor: hand;");
                coachesBtn.setTooltip(new Tooltip("Gérer les coaches"));

                rewardsBtn.setStyle("-fx-background-color: linear-gradient(to right, #9B59B6, #8E44AD); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 5 10; " +
                        "-fx-font-size: 11px; -fx-cursor: hand;");
                rewardsBtn.setTooltip(new Tooltip("Gérer les récompenses"));

                tasksBtn.setStyle("-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 5 10; " +
                        "-fx-font-size: 11px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(99,102,241,0.3), 5, 0, 0, 1);");
                tasksBtn.setTooltip(new Tooltip("Générer et voir les tâches avec l'IA"));



                qrCodeBtn.setStyle(
                        "-fx-background-color: linear-gradient(to right, #9b59b6, #8e44ad); " +
                                "-fx-text-fill: white; -fx-font-weight: bold; " +
                                "-fx-background-radius: 8; -fx-padding: 5 10; " +
                                "-fx-font-size: 11px; -fx-cursor: hand; " +
                                "-fx-effect: dropshadow(gaussian, rgba(155,89,182,0.3), 5, 0, 0, 1);"
                );
                qrCodeBtn.setTooltip(new Tooltip("Générer le QR Code du challenge"));

                // Effet au survol
                qrCodeBtn.setOnMouseEntered(e ->
                        qrCodeBtn.setStyle(
                                "-fx-background-color: linear-gradient(to right, #8e44ad, #6c3483); " +
                                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                                        "-fx-background-radius: 8; -fx-padding: 5 10; " +
                                        "-fx-font-size: 11px; -fx-cursor: hand; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(142,68,173,0.5), 8, 0, 0, 2);"
                        )
                );

                qrCodeBtn.setOnMouseExited(e ->
                        qrCodeBtn.setStyle(
                                "-fx-background-color: linear-gradient(to right, #9b59b6, #8e44ad); " +
                                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                                        "-fx-background-radius: 8; -fx-padding: 5 10; " +
                                        "-fx-font-size: 11px; -fx-cursor: hand;"
                        )
                );

                teamBtn.setStyle("-fx-background-color: linear-gradient(to right, #f59e0b, #d97706); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 5 10; " +
                        "-fx-font-size: 11px; -fx-cursor: hand;");
                teamBtn.setTooltip(new Tooltip("Espace Collaboratif : Équipe & Leaderboard"));


                // ✅ AJOUT UNIQUE des boutons à actionBox
                actionBox.getChildren().addAll(qrCodeBtn,detailsBtn, coachesBtn, rewardsBtn, tasksBtn);
                actionBox.setAlignment(Pos.CENTER_RIGHT);

                // Labels pour coaches et récompenses
                coachesLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 11px; -fx-font-weight: bold;");
                rewardsLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 11px; -fx-font-weight: bold;");

                // Configuration de la colonne de droite
                rightColumn.setAlignment(Pos.CENTER_RIGHT);
                rightColumn.setMinWidth(100);

                // Assemblage des composants
                VBox leftContent = new VBox(10);
                leftContent.getChildren().addAll(header, descLabel, infoRow, bottomRow);

                HBox mainContent = new HBox(15);
                mainContent.setAlignment(Pos.CENTER_LEFT);
                mainContent.getChildren().addAll(leftContent, rightColumn);
                //Elle retourne la liste des nœuds enfants de ce conteneur
                content.getChildren().addAll(mainContent, actionBox);

                VBox mainCard = new VBox(10);
                mainCard.getChildren().addAll(content);

                card.getChildren().add(mainCard);

                // Effet au survol
                card.setOnMouseEntered(e -> {
                    card.setStyle(card.getStyle() +
                            "-fx-background-color: rgba(255,255,255,0.12); " +
                            "-fx-border-color: rgba(255,255,255,0.2); " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 3);");
                });

                card.setOnMouseExited(e -> {
                    card.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                            "-fx-background-radius: 12; " +
                            "-fx-padding: 15; " +
                            "-fx-border-color: rgba(255,255,255,0.1); " +
                            "-fx-border-radius: 12; " +
                            "-fx-border-width: 1; " +
                            "-fx-effect: null;");
                });

                // Gestion des clics
                // Dans setupListView(), remplacez la gestion des clics
                card.setOnMouseClicked(e -> {
                    Challenge challenge = getItem();
                    if (challenge != null) {
                        if (e.getClickCount() == 2) {
                            // Double-clic : ouvrir le chatbot générateur de tâches
                            openTaskGeneratorChatbot(challenge);
                        } else {
                            // Simple clic : sélection normale
                            challengeListView.getSelectionModel().select(challenge);
                            loadChallengeData(challenge);
                        }
                    }
                });
            }

            //updateItem est appelée chaque fois qu’une cellule doit afficher un nouvel élément ou se vider.

            @Override
            protected void updateItem(Challenge challenge, boolean empty) {
                //on appelle la méthode de la classe parente pour préparer la cellule.
                super.updateItem(challenge, empty);

                if (empty || challenge == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Mise à jour des données
                    titleLabel.setText("🏆 " + challenge.getTitre());

                    // Statut avec badge coloré
                    if (challenge.isActif()) {
                        statusLabel.setText("✅ ACTIF");
                        statusLabel.setStyle(statusLabel.getStyle() +
                                "-fx-background-color: rgba(46,204,113,0.2); " +
                                "-fx-text-fill: #2ECC71;");
                    } else {
                        statusLabel.setText("❌ INACTIF");
                        statusLabel.setStyle(statusLabel.getStyle() +
                                "-fx-background-color: rgba(231,76,60,0.2); " +
                                "-fx-text-fill: #E74C3C;");
                    }

                    // Description tronquée si trop longue
                    String desc = challenge.getDescription();
                    if (desc.length() > 100) {
                        desc = desc.substring(0, 97) + "...";
                    }
                    descLabel.setText(desc);

                    // Informations principales
                    dureeLabel.setText("⏱️ " + challenge.getDureeJours() + " jours");
                    diffLabel.setText("🎯 " + challenge.getNiveauDifficulte());
                    typeLabel.setText("📂 " + challenge.getTypeChallenge());

                    // Mise à jour de la ligne d'info
                    infoRow.getChildren().clear();
                    infoRow.getChildren().addAll(dureeLabel, diffLabel, typeLabel);

                    // Mise à jour du header
                    header.getChildren().clear();
                    //separateur entre titre  et status(dans header)
                    Region spacer = new Region();
                    //le spacer prend tout l’espace disponible entre les éléments.
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    header.getChildren().addAll(titleLabel, spacer, statusLabel);
                    //=>Résultat : dans la ligne du header, le titre reste à gauche, le statut à droite, même si la largeur de la carte change.

                    // Coaches associés
                    List<CoachMotivation> coaches = challengeCoachCrud.getCoachesForChallenge(challenge.getIdChallenge());
                    if (coaches.isEmpty()) {
                        coachesLabel.setText("👤 Aucun coach");
                        coachesLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");
                        coachesLabel.setOnMouseClicked(null);
                    } else {

                        //construire dynamiquement le texte du label.
                        StringBuilder coachesText = new StringBuilder("👤 ");
                        for (int i = 0; i < Math.min(coaches.size(), 2); i++) {
                            coachesText.append(coaches.get(i).getNomCoach());
                            if (i < Math.min(coaches.size(), 2) - 1) {
                                coachesText.append(", ");
                            }
                        }

                        if (coaches.size() > 2) {
                            coachesText.append(" +").append(coaches.size() - 2);
                        }

                        coachesLabel.setText(coachesText.toString());
                        coachesLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 11px; -fx-font-weight: bold;");
                        //cursor cliquable
                        coachesLabel.setCursor(Cursor.HAND);

                        coachesLabel.setOnMouseClicked(e -> {
                            //empêche l’événement de remonter à la cellule entière
                            e.consume();
                            //ouvre un popup avec tous les coaches associés au challenge.
                            showCoachesPopup(challenge, coaches);
                        });
                    }

                    // Récompenses associées

                    /*challengeRecompenseCrud.getRecompensesByChallenge(...) → méthode qui interroge la base de données
                     pour récupérer toutes les récompenses liées à ce challenge.*/
                    List<Recompense> recompenses = challengeRecompenseCrud.getRecompensesByChallenge(challenge.getIdChallenge());
                    if (recompenses.isEmpty()) {
                        rewardsLabel.setText("🎁 Aucune récompense");
                        rewardsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");
                        rewardsLabel.setOnMouseClicked(null);
                    } else {
                        StringBuilder rewardsText = new StringBuilder("🎁 ");
                        for (int i = 0; i < Math.min(recompenses.size(), 2); i++) {
                            rewardsText.append(recompenses.get(i).getTitre());
                            if (i < Math.min(recompenses.size(), 2) - 1) {
                                rewardsText.append(", ");
                            }
                        }

                        if (recompenses.size() > 2) {
                            rewardsText.append(" +").append(recompenses.size() - 2);
                        }

                        rewardsLabel.setText(rewardsText.toString());
                        rewardsLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 11px; -fx-font-weight: bold;");
                        rewardsLabel.setCursor(Cursor.HAND);

                        rewardsLabel.setOnMouseClicked(e -> {
                            e.consume();
                            showRecompensesPopup(challenge, recompenses);
                        });
                    }

                    // Mise à jour de la ligne du bas
                    bottomRow.getChildren().clear();
                    bottomRow.getChildren().addAll(coachesLabel, rewardsLabel);

                    // Configuration de la colonne de droite (statistiques)
                    rightColumn.getChildren().clear();

                    VBox statsBox = new VBox(5);
                    statsBox.setAlignment(Pos.CENTER_RIGHT);

                    Label idLabel = new Label("#" + challenge.getIdChallenge());
                    idLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");

                    // Badge pour le nombre de coaches
                    HBox coachBadge = new HBox(5);
                    coachBadge.setAlignment(Pos.CENTER);
                    Label coachIcon = new Label("👤");
                    Label coachCount = new Label(String.valueOf(coaches.size()));
                    coachCount.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: black;");
                    coachBadge.setStyle("-fx-background-color: rgba(52,152,219,0.2); -fx-background-radius: 10; -fx-padding: 3 8;");
                    coachBadge.getChildren().addAll(coachIcon, coachCount);

                    // Badge pour le nombre de récompenses
                    HBox rewardBadge = new HBox(5);
                    rewardBadge.setAlignment(Pos.CENTER);
                    Label rewardIcon = new Label("🎁");
                    Label rewardCount = new Label(String.valueOf(recompenses.size()));
                    rewardCount.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: black;");
                    rewardBadge.setStyle("-fx-background-color: rgba(155,89,182,0.2); -fx-background-radius: 10; -fx-padding: 3 8;");
                    rewardBadge.getChildren().addAll(rewardIcon, rewardCount);

                    //La VBox statsBox contient maintenant :
                    //
                    //ID du challenge (#12)
                    //
                    //Badge coaches 👤 + nombre
                    //
                    //Badge récompenses 🎁 + nombre
                    statsBox.getChildren().addAll(idLabel, coachBadge, rewardBadge);
                    rightColumn.getChildren().add(statsBox);

                    // Mise à jour des actions des boutons
                    //action declenché lors d un clic
                    detailsBtn.setOnAction(e -> showChallengeDetail(challenge));
                    coachesBtn.setOnAction(e -> openCoachManager(challenge));
                    rewardsBtn.setOnAction(e -> openRecompenseManager(challenge));
                    tasksBtn.setOnAction(e -> openTaskGeneratorChatbot(challenge));


                    qrCodeBtn.setOnAction(e -> {
                        //Challenge challenge = getItem();
                        if (challenge != null) {
                            showQRCodeDialog(challenge);
                        }
                    });

                    if ("Collaboratif".equals(challenge.getTypeChallenge())) {
                        if (!actionBox.getChildren().contains(teamBtn)) {
                            actionBox.getChildren().add(1, teamBtn);
                        }
                        teamBtn.setOnAction(e -> openCollaborativeWorkspace(challenge));
                    } else {
                        actionBox.getChildren().remove(teamBtn);
                    }


                    //card est un VBox ou HBox qui contient toute ta structure :
                    //
                    //header
                    //
                    //infoRow
                    //
                    //bottomRow
                    //
                    //rightColumn
                    //
                    //boutons

                    setGraphic(card);
                }
            }
        });

        // Sélection d'un élément
        //newVal contient le challenge sélectionné
        //oldval:selection precedant
        //adlistner :s je clic fais quelque chose
        challengeListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        //load les donné de l element selectionné
                        loadChallengeData(newVal);
                    }
                }
        );

        // Style de la ListView
        challengeListView.setStyle("-fx-background-color: transparent; " +
                "-fx-background-insets: 0; " +
                "-fx-padding: 0;");
    }

    /* ================= FORM ================= */
    private void setupForm() {
        difficulteField.getItems().addAll("Facile", "Moyen", "Difficile", "Expert");
        typeField.getItems().addAll("Programmation", "Design", "Marketing", "Business", "Personnel", "Collaboratif");

        difficulteField.setValue("Moyen");
        typeField.setValue("Programmation");
    }

    /* ================= BUTTONS ================= */
    private void setupButtons() {
        addBtn.setOnAction(e -> addChallenge());
        updateBtn.setOnAction(e -> updateChallenge());
        deleteBtn.setOnAction(e -> deleteChallenge());
        clearBtn.setOnAction(e -> clearForm());
        searchBtn.setOnAction(e -> searchChallenges());
        coachBtn.setOnAction(e -> goToCoaches());
        recBtn.setOnAction(e -> goToRecompenses());
        exportBtn.setOnAction(e -> exportToPDF());

        dashboardBtn.setOnAction(e -> goToDashboard());
        if (homeBtn != null) homeBtn.setOnAction(e -> onGoHome());

         //getSelectionModel:’est l’objet qui gère :Quel élément est sélectionné
        //selectedItemProperty():L’élément actuellement sélectionné dans la ListView.
        //addListener(...):Quand la sélection change → on exécute la lambda.

        challengeListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) loadChallengeData(newVal);
                }
        );
    }

    private void onGoHome() {
        Stage stage = (Stage) dashboardBtn.getScene().getWindow();
        org.example.controller.SceneUtil.switchTo(stage, "/org/example/ui/Home.fxml", "Atomic You - Accueil");
    }

    // ==================== CONTRÔLE DE SAISIE ====================

    private void setupInputValidation() {
        // Validation pour le titre
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (Character.isDigit(newValue.charAt(0))) {
                    showFieldError(titleField, "Le titre ne doit pas commencer par un chiffre");
                    return;
                }
                if (newValue.matches("\\d+")) {
                    showFieldError(titleField, "Le titre ne doit pas être composé uniquement de chiffres");
                    return;
                }
                if (!newValue.matches(".*[a-zA-Z].*")) {
                    showFieldError(titleField, "Le titre doit contenir au moins une lettre");
                    return;
                }
                if (newValue.length() > 100) {
                    showFieldError(titleField, "Le titre ne doit pas dépasser 100 caractères");
                    return;
                }
                clearFieldError(titleField);
            }
        });

        // Validation pour la description
        descField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (newValue.length() > 0 && Character.isDigit(newValue.charAt(0))) {
                    showFieldError(descField, "La description ne doit pas commencer par un chiffre");
                    return;
                }
                if (newValue.matches("\\d+")) {
                    showFieldError(descField, "La description ne doit pas être composée uniquement de chiffres");
                    return;
                }
                if (!newValue.matches("(?s).*[\\p{L}\\p{M}].*")) {
                    showFieldError(descField, "La description doit contenir au moins une lettre");
                    return;
                }
                if (newValue.length() > 500) {
                    showFieldError(descField, "La description ne doit pas dépasser 500 caractères");
                    return;
                }
                clearFieldError(descField);
            }
        });

        // Validation pour la durée
        dureeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("\\d*")) {
                    showFieldError(dureeField, "La durée doit contenir uniquement des chiffres");
                    return;
                }
                try {
                    int value = Integer.parseInt(newValue);
                    if (value < 1 || value > 365) {
                        showFieldError(dureeField, "La durée doit être entre 1 et 365 jours");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showFieldError(dureeField, "Valeur invalide");
                    return;
                }
                clearFieldError(dureeField);
            }
        });

        // Validation pour les ComboBox
        difficulteField.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isEmpty()) {
                showFieldError(difficulteField, "La difficulté est obligatoire");
            } else {
                clearFieldError(difficulteField);
            }
        });

        typeField.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isEmpty()) {
                showFieldError(typeField, "Le type est obligatoire");
            } else {
                clearFieldError(typeField);
            }
        });
    }

    private void showFieldError(Control field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        //"Est-ce que le champ est un champ texte ?"
        if (field instanceof TextInputControl) {
            //On transforme le Control en TextInputControl
            ((TextInputControl) field).setTooltip(new Tooltip(message));
        } else if (field instanceof ComboBox) {
            field.setTooltip(new Tooltip(message));
        }
    }
    //Supprimer l’affichage d’erreur d’un champ.
    private void clearFieldError(Control field) {
        field.setStyle("");
        field.setTooltip(null);
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation du titre
        String titre = titleField.getText().trim();
        if (titre.isEmpty()) {
            showFieldError(titleField, "Le titre est obligatoire");
            showAlert("Validation", "Le titre est obligatoire", Alert.AlertType.WARNING);
            titleField.requestFocus();
            isValid = false;
        } else if (Character.isDigit(titre.charAt(0))) {
            showFieldError(titleField, "Le titre ne doit pas commencer par un chiffre");
            showAlert("Validation", "Le titre ne doit pas commencer par un chiffre", Alert.AlertType.WARNING);
            titleField.requestFocus();
            isValid = false;
        } else if (titre.matches("\\d+")) {
            showFieldError(titleField, "Le titre ne doit pas être composé uniquement de chiffres");
            showAlert("Validation", "Le titre ne doit pas être composé uniquement de chiffres", Alert.AlertType.WARNING);
            titleField.requestFocus();
            isValid = false;
        } else if (!titre.matches(".*[a-zA-Z].*")) {
            showFieldError(titleField, "Le titre doit contenir au moins une lettre");
            showAlert("Validation", "Le titre doit contenir au moins une lettre", Alert.AlertType.WARNING);
            titleField.requestFocus();
            isValid = false;
        } else if (titre.length() > 100) {
            showFieldError(titleField, "Le titre ne doit pas dépasser 100 caractères");
            showAlert("Validation", "Le titre ne doit pas dépasser 100 caractères", Alert.AlertType.WARNING);
            titleField.requestFocus();
            isValid = false;
        } else {
            clearFieldError(titleField);
        }

        // Validation de la description
        String description = descField.getText().trim();
        if (description.isEmpty()) {
            showFieldError(descField, "La description est obligatoire");
            if (isValid) {
                showAlert("Validation", "La description est obligatoire", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else if (description.length() > 0 && Character.isDigit(description.charAt(0))) {
            showFieldError(descField, "La description ne doit pas commencer par un chiffre");
            if (isValid) {
                showAlert("Validation", "La description ne doit pas commencer par un chiffre", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else if (description.matches("\\d+")) {
            showFieldError(descField, "La description ne doit pas être composée uniquement de chiffres");
            if (isValid) {
                showAlert("Validation", "La description ne doit pas être composée uniquement de chiffres", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else if (!description.matches(".*[a-zA-Z].*")) {
            showFieldError(descField, "La description doit contenir au moins une lettre");
            if (isValid) {
                showAlert("Validation", "La description doit contenir au moins une lettre", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else if (description.length() > 500) {
            showFieldError(descField, "La description ne doit pas dépasser 500 caractères");
            if (isValid) {
                showAlert("Validation", "La description ne doit pas dépasser 500 caractères", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else {
            clearFieldError(descField);
        }

        // Validation de la durée
        String dureeText = dureeField.getText().trim();
        if (dureeText.isEmpty()) {
            showFieldError(dureeField, "La durée est obligatoire");
            if (isValid) {
                showAlert("Validation", "La durée est obligatoire", Alert.AlertType.WARNING);
                dureeField.requestFocus();
            }
            isValid = false;
        } else if (!dureeText.matches("\\d+")) {
            showFieldError(dureeField, "La durée doit contenir uniquement des chiffres");
            if (isValid) {
                showAlert("Validation", "La durée doit contenir uniquement des chiffres", Alert.AlertType.WARNING);
                dureeField.requestFocus();
            }
            isValid = false;
        } else {
            try {
                int duree = Integer.parseInt(dureeText);
                if (duree < 1 || duree > 365) {
                    showFieldError(dureeField, "La durée doit être entre 1 et 365 jours");
                    if (isValid) {
                        showAlert("Validation", "La durée doit être entre 1 et 365 jours", Alert.AlertType.WARNING);
                        dureeField.requestFocus();
                    }
                    isValid = false;
                } else {
                    clearFieldError(dureeField);
                }
            } catch (NumberFormatException e) {
                showFieldError(dureeField, "Valeur invalide pour la durée");
                if (isValid) {
                    showAlert("Validation", "Valeur invalide pour la durée", Alert.AlertType.WARNING);
                    dureeField.requestFocus();
                }
                isValid = false;
            }
        }

        // Validation des ComboBox
        if (difficulteField.getValue() == null || difficulteField.getValue().isEmpty()) {
            showFieldError(difficulteField, "La difficulté est obligatoire");
            showAlert("Validation", "La difficulté est obligatoire", Alert.AlertType.WARNING);
            if (isValid) difficulteField.requestFocus();
            isValid = false;
        } else {
            clearFieldError(difficulteField);
        }

        if (typeField.getValue() == null || typeField.getValue().isEmpty()) {
            showFieldError(typeField, "Le type est obligatoire");
            showAlert("Validation", "Le type est obligatoire", Alert.AlertType.WARNING);
            if (isValid) typeField.requestFocus();
            isValid = false;
        } else {
            clearFieldError(typeField);
        }

        return isValid;
    }

    /* ================= DATA ================= */
    private void loadData() {
        try {
            List<Challenge> challenges = challengeCrud.readAll();
            ObservableList<Challenge> observableList = FXCollections.observableArrayList(challenges);
            challengeListView.setItems(observableList);
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchChallenges() {
        String keyword = searchField.getText().toLowerCase().trim();

        List<Challenge> filtered;

        if (keyword.isEmpty()) {
            filtered = challengeCrud.readAll();
        } else {
            filtered = challengeCrud.readAll().stream()
                    .filter(c ->
                            c.getTitre().toLowerCase().contains(keyword) ||
                                    c.getDescription().toLowerCase().contains(keyword) ||
                                    c.getTypeChallenge().toLowerCase().contains(keyword) ||
                                    c.getNiveauDifficulte().toLowerCase().contains(keyword)
                    )
                    .collect(Collectors.toList());
        }

        // Appliquer le tri actuel sur les résultats filtrés
        challengeList.setAll(filtered);
        sortChallenges(currentSortType); // Réappliquer le tri
    }

    /* ================= CRUD ================= */
    private void addChallenge() {
        if (!validateForm()) return;

        try {
            Challenge challenge = new Challenge(
                    titleField.getText().trim(),
                    descField.getText().trim(),
                    Integer.parseInt(dureeField.getText().trim()),
                    difficulteField.getValue(),
                    typeField.getValue()
            );

            challenge.setActif(actifField.isSelected());
            challengeCrud.create(challenge);

            showAlert("Succès", "Challenge ajouté avec succès", Alert.AlertType.INFORMATION);
            clearForm();
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateChallenge() {
        Challenge selected = challengeListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez un challenge", Alert.AlertType.WARNING);
            return;
        }

        if (!validateForm()) return;

        try {
            selected.setTitre(titleField.getText().trim());
            selected.setDescription(descField.getText().trim());
            selected.setDureeJours(Integer.parseInt(dureeField.getText().trim()));
            selected.setNiveauDifficulte(difficulteField.getValue());
            selected.setTypeChallenge(typeField.getValue());
            selected.setActif(actifField.isSelected());

            challengeCrud.update(selected);
            challengeListView.refresh();
            updateStatistics();

            showAlert("Succès", "Challenge modifié", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteChallenge() {
        Challenge selected = challengeListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez un challenge à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le challenge");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer : " + selected.getTitre() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    challengeCrud.delete_reel(selected.getIdChallenge());
                    showAlert("Succès", "Challenge supprimé!", Alert.AlertType.INFORMATION);
                    clearForm();
                    loadData();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    /* ================= GESTION DES RÉCOMPENSES ================= */
    private void openRecompenseManager(Challenge challenge) {
        try {
            Stage stage = new Stage();
            stage.setTitle("🎁 Gérer les récompenses - " + challenge.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);

            VBox root = new VBox(20);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #f8f9fa;");

            // En-tête
            Label titleLabel = new Label("🎁 Gérer les récompenses pour : " + challenge.getTitre());
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

            // Liste des récompenses actuelles
            ListView<Recompense> currentList = new ListView<>();
            ObservableList<Recompense> currentRecompenses =
                    FXCollections.observableArrayList(
                            challengeRecompenseCrud.getRecompensesByChallenge(challenge.getIdChallenge())
                    );
            currentList.setItems(currentRecompenses);
            currentList.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(Recompense r, boolean empty) {
                    super.updateItem(r, empty);
                    if (empty || r == null) {
                        setText(null);
                    } else {
                        setText("🏆 " + r.getTitre() + " (" + r.getTypeRecompense() + ")");
                    }
                }
            });

            VBox currentBox = new VBox(10);
            currentBox.getChildren().addAll(
                    new Label("✅ Récompenses assignées (" + currentRecompenses.size() + ") :"),
                    currentList
            );

            // Bouton pour ajouter une nouvelle récompense
            Button addNewButton = new Button("➕ Ajouter une récompense");
            addNewButton.setStyle(
                    "-fx-background-color: #2ECC71; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 10 20;"
            );
            addNewButton.setOnAction(e -> {
                openRecompenseSelector(challenge, currentRecompenses);
            });

            // Boutons de contrôle
            HBox buttonBox = new HBox(15);
            Button saveButton = new Button("💾 Enregistrer");
            saveButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
            saveButton.setOnAction(e -> {
                showAlert("Succès", "Modifications enregistrées", Alert.AlertType.INFORMATION);
                stage.close();
                challengeListView.refresh();
            });

            Button cancelButton = new Button("❌ Annuler");
            cancelButton.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white;");
            cancelButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(saveButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            root.getChildren().addAll(
                    titleLabel,
                    currentBox,
                    addNewButton,
                    buttonBox
            );

            Scene scene = new Scene(root, 500, 400);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le gestionnaire", Alert.AlertType.ERROR);
        }
    }

    private void openRecompenseSelector(Challenge challenge, ObservableList<Recompense> currentList) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Sélectionner des récompenses");
            stage.initModality(Modality.APPLICATION_MODAL);

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));

            Label titleLabel = new Label("📦 Sélectionnez les récompenses à ajouter :");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Récupérer toutes les récompenses disponibles
            RecompenseCrud recompenseCrud = new RecompenseCrud();
            List<Recompense> allRecompenses = recompenseCrud.readAll();

            // Filtrer celles déjà assignées
            List<Recompense> availableRecompenses = allRecompenses.stream()
                    .filter(r -> currentList.stream()
                            .noneMatch(cr -> cr.getIdRecompense() == r.getIdRecompense()))
                    .collect(Collectors.toList());

            ListView<Recompense> listView = new ListView<>();
            listView.setItems(FXCollections.observableArrayList(availableRecompenses));
            listView.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(Recompense r, boolean empty) {
                    super.updateItem(r, empty);
                    if (empty || r == null) {
                        setText(null);
                    } else {
                        setText("🎯 " + r.getTitre() + " - " + r.getTypeRecompense());
                    }
                }
            });

            // Boutons
            HBox buttonBox = new HBox(10);
            Button addButton = new Button("➕ Ajouter sélection");
            addButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white;");
            addButton.setOnAction(e -> {
                Recompense selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    challengeRecompenseCrud.associateRecompense(
                            challenge.getIdChallenge(),
                            selected.getIdRecompense()
                    );
                    currentList.add(selected);
                    stage.close();
                }
            });

            Button cancelButton = new Button("Annuler");
            cancelButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(addButton, cancelButton);

            root.getChildren().addAll(titleLabel, listView, buttonBox);

            Scene scene = new Scene(root, 400, 300);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= UTIL ================= */
    private void loadChallengeData(Challenge c) {
        titleField.setText(c.getTitre());
        descField.setText(c.getDescription());
        dureeField.setText(String.valueOf(c.getDureeJours()));
        difficulteField.setValue(c.getNiveauDifficulte());
        typeField.setValue(c.getTypeChallenge());
        actifField.setSelected(c.isActif());

        // Effacer les erreurs de validation lors du chargement
        clearFieldError(titleField);
        clearFieldError(descField);
        clearFieldError(dureeField);
        clearFieldError(difficulteField);
        clearFieldError(typeField);
    }

    private void clearForm() {
        titleField.clear();
        descField.clear();
        dureeField.clear();
        difficulteField.setValue("Moyen");
        typeField.setValue("Programmation");
        actifField.setSelected(false);
        challengeListView.getSelectionModel().clearSelection();

        // Effacer toutes les erreurs de validation
        clearFieldError(titleField);
        clearFieldError(descField);
        clearFieldError(dureeField);
        clearFieldError(difficulteField);
        clearFieldError(typeField);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }


    /* ================= NAVIGATION VERS DASHBOARD ================= */
    private void goToDashboard() {
        Stage stage = (Stage) dashboardBtn.getScene().getWindow();
        org.example.controller.SceneUtil.switchTo(stage, "/org/example/ui/motivation/dashboard_statistiques.fxml", "📊 Dashboard Statistiques");
    }

    private void goToCoaches() {
        Stage stage = (Stage) coachBtn.getScene().getWindow();
        org.example.controller.SceneUtil.switchTo(stage, "/org/example/ui/motivation/coach.fxml", "👨‍🏫 Gestion Coaches");
    }

    private void goToRecompenses() {
        Stage stage = (Stage) recBtn.getScene().getWindow();
        org.example.controller.SceneUtil.switchTo(stage, "/org/example/ui/motivation/RecompenseView.fxml", "🏆 Gestion des Récompenses");
    }


    /* ================= EXPORT PDF ================= */
    /* ================= EXPORT SIMPLE (SANS PDFBOX) ================= */
    /* ================= EXPORT PDF PROFESSIONNEL ================= */
    /* ================= EXPORT PDF PROFESSIONNEL ================= */
    private void exportToPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les challenges en PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );

            fileChooser.setInitialFileName("challenges_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");

            File file = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());

            if (file != null) {
                generateProfessionalPDF(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur d'exportation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void generateProfessionalPDF(File file) {
        try {
            PDDocument document = new PDDocument();

            // Page 1: Page de garde
            createCoverPage(document);

            // Page 2: Statistiques et graphiques
            createStatisticsPage(document);

            // Pages suivantes: Détails des challenges
            createChallengeDetailsPages(document);

            // Sauvegarder le document
            document.save(file);
            document.close();

            showAlert("Succès",
                    "✅ PDF généré avec succès!\n\n" +
                            "📄 Fichier: " + file.getName() + "\n" +
                            "📁 Chemin: " + file.getAbsolutePath() + "\n" +
                            "📊 Nombre de challenges: " + challengeList.size(),
                    Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la génération du PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void createCoverPage(PDDocument document) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // Fond coloré en haut
            contentStream.setNonStrokingColor(new java.awt.Color(41, 128, 185)); // Bleu professionnel
            contentStream.addRect(0, pageHeight - 200, pageWidth, 200);
            contentStream.fill();

            // Titre principal - CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(java.awt.Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 36);
            contentStream.newLineAtOffset(50, pageHeight - 100);
            contentStream.showText("RAPPORT DES CHALLENGES");
            contentStream.endText();

            // Sous-titre - CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(java.awt.Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 16);
            contentStream.newLineAtOffset(50, pageHeight - 140);
            contentStream.showText("Analyse complete et statistiques");
            contentStream.endText();

            // Date de génération - CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(java.awt.Color.BLACK);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.newLineAtOffset(50, pageHeight - 250);
            contentStream.showText("Date de generation: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            contentStream.endText();

            // Statistiques rapides (cartes colorées)
            float cardY = pageHeight - 500;
            float cardHeight = 100;
            float cardWidth = 150;
            float spacing = 20;

            long actifs = challengeList.stream().filter(Challenge::isActif).count();
            long inactifs = challengeList.size() - actifs;

            // Carte 1: Total
            drawStatCard(contentStream, 50, cardY, cardWidth, cardHeight,
                    new java.awt.Color(52, 152, 219), "TOTAL",
                    String.valueOf(challengeList.size()), "Challenges");

            // Carte 2: Actifs
            drawStatCard(contentStream, 50 + cardWidth + spacing, cardY, cardWidth, cardHeight,
                    new java.awt.Color(46, 204, 113), "ACTIFS",
                    String.valueOf(actifs), "Challenges");

            // Carte 3: Inactifs
            drawStatCard(contentStream, 50 + 2 * (cardWidth + spacing), cardY, cardWidth, cardHeight,
                    new java.awt.Color(231, 76, 60), "INACTIFS",
                    String.valueOf(inactifs), "Challenges");

            // Pied de page avec ligne décorative
            contentStream.setStrokingColor(new java.awt.Color(41, 128, 185));
            contentStream.setLineWidth(3);
            contentStream.moveTo(50, 100);
            contentStream.lineTo(pageWidth - 50, 100);
            contentStream.stroke();

            // Texte du pied de page - CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(new java.awt.Color(127, 140, 141));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 10);
            contentStream.newLineAtOffset(50, 70);
            contentStream.showText("Genere automatiquement par le systeme de gestion des challenges");
            contentStream.endText();


            //image logo

            String imageLeftPath = getClass().getResource("/org/example/image/logo_equipe-removebg-preview (1).png").toURI().getPath();
            String imageRightPath = getClass().getResource("/org/example/image/Logo_ESPRIT_-_Tunisie.png").toURI().getPath();

            PDImageXObject imageLeft = PDImageXObject.createFromFile(imageLeftPath, document);
            PDImageXObject imageRight = PDImageXObject.createFromFile(imageRightPath, document);

            // Dimensions et marges
            float imageWidth = 50;
            float imageHeight = 50;
            float margin = 50;

            // Récupération de la largeur de la page


            // Dessiner l'image gauche (en bas à gauche)
            contentStream.drawImage(imageLeft, margin, 20, imageWidth, imageHeight);

            // Dessiner l'image droite (en bas à droite)
            contentStream.drawImage(imageRight, pageWidth - margin - imageWidth, 40, imageWidth, imageHeight);
        }
    }

    private void drawStatCard(PDPageContentStream contentStream, float x, float y,
                              float width, float height, java.awt.Color color,
                              String label, String value, String unit) throws Exception {
        // Fond de la carte
        contentStream.setNonStrokingColor(color);
        contentStream.addRect(x, y, width, height);
        contentStream.fill();

        // Label - CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(java.awt.Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(x + 10, y + height - 25);
        contentStream.showText(label);
        contentStream.endText();

        // Valeur (grand) - CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(java.awt.Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 32);
        contentStream.newLineAtOffset(x + 10, y + height - 65);
        contentStream.showText(value);
        contentStream.endText();

        // Unité - CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(java.awt.Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.newLineAtOffset(x + 10, y + 15);
        contentStream.showText(unit);
        contentStream.endText();
    }

    private void createStatisticsPage(PDDocument document) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // En-tête de la page
            contentStream.setNonStrokingColor(new java.awt.Color(41, 128, 185));
            contentStream.addRect(0, pageHeight - 80, pageWidth, 80);
            contentStream.fill();

            // CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(java.awt.Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
            contentStream.newLineAtOffset(50, pageHeight - 50);
            contentStream.showText("STATISTIQUES DETAILLEES");
            contentStream.endText();

            float currentY = pageHeight - 120;

            // Section: Répartition par difficulté
            currentY = drawSectionTitle(contentStream, currentY, "Repartition par difficulte");
            currentY -= 20;

            Map<String, Long> difficultyStats = challengeList.stream()
                    .collect(Collectors.groupingBy(Challenge::getNiveauDifficulte, Collectors.counting()));

            for (Map.Entry<String, Long> entry : difficultyStats.entrySet()) {
                currentY = drawStatLine(contentStream, currentY, entry.getKey(), entry.getValue().toString());
            }

            currentY -= 30;

            // Section: Répartition par type
            currentY = drawSectionTitle(contentStream, currentY, "Repartition par type");
            currentY -= 20;

            Map<String, Long> typeStats = challengeList.stream()
                    .collect(Collectors.groupingBy(Challenge::getTypeChallenge, Collectors.counting()));

            for (Map.Entry<String, Long> entry : typeStats.entrySet()) {
                currentY = drawStatLine(contentStream, currentY, entry.getKey(), entry.getValue().toString());
            }

            currentY -= 30;

            // Section: Durée moyenne
            currentY = drawSectionTitle(contentStream, currentY, "Analyse de duree");
            currentY -= 20;

            double avgDuration = challengeList.stream()
                    .mapToInt(Challenge::getDureeJours)
                    .average()
                    .orElse(0);

            int minDuration = challengeList.stream()
                    .mapToInt(Challenge::getDureeJours)
                    .min()
                    .orElse(0);

            int maxDuration = challengeList.stream()
                    .mapToInt(Challenge::getDureeJours)
                    .max()
                    .orElse(0);

            currentY = drawStatLine(contentStream, currentY, "Duree moyenne",
                    String.format("%.1f jours", avgDuration));
            currentY = drawStatLine(contentStream, currentY, "Duree minimale",
                    minDuration + " jours");
            currentY = drawStatLine(contentStream, currentY, "Duree maximale",
                    maxDuration + " jours");

            // Numéro de page - CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(new java.awt.Color(127, 140, 141));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(pageWidth - 80, 30);
            contentStream.showText("Page 2");
            contentStream.endText();
        }
    }

    private float drawSectionTitle(PDPageContentStream contentStream, float y, String title) throws Exception {
        // CORRIGÉ
        contentStream.setNonStrokingColor(new java.awt.Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, y);
        contentStream.showText(title);
        contentStream.endText();

        // Ligne sous le titre
        contentStream.setStrokingColor(new java.awt.Color(41, 128, 185));
        contentStream.setLineWidth(2);
        contentStream.moveTo(50, y - 5);
        contentStream.lineTo(300, y - 5);
        contentStream.stroke();

        return y - 10;
    }

    private float drawStatLine(PDPageContentStream contentStream, float y, String label, String value) throws Exception {
        // CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(java.awt.Color.BLACK);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        contentStream.newLineAtOffset(70, y);
        contentStream.showText(label);
        contentStream.endText();

        // CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(new java.awt.Color(41, 128, 185));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(300, y);
        contentStream.showText(value);
        contentStream.endText();

        return y - 25;
    }

    private void createChallengeDetailsPages(PDDocument document) throws Exception {
        int pageNumber = 3;
        int challengesPerPage = 3;

        for (int i = 0; i < challengeList.size(); i += challengesPerPage) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                // En-tête de la page
                contentStream.setNonStrokingColor(new java.awt.Color(41, 128, 185));
                contentStream.addRect(0, pageHeight - 60, pageWidth, 60);
                contentStream.fill();

                // CORRIGÉ
                contentStream.beginText();
                contentStream.setNonStrokingColor(java.awt.Color.WHITE);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
                contentStream.newLineAtOffset(50, pageHeight - 40);
                contentStream.showText("DETAIL DES CHALLENGES");
                contentStream.endText();

                float currentY = pageHeight - 100;

                // Afficher jusqu'à 3 challenges par page
                for (int j = i; j < Math.min(i + challengesPerPage, challengeList.size()); j++) {
                    Challenge challenge = challengeList.get(j);
                    currentY = drawChallengeCard(contentStream, challenge, currentY, pageWidth);
                    currentY -= 30; // Espacement entre les cartes
                }

                // Numéro de page - CORRIGÉ
                contentStream.beginText();
                contentStream.setNonStrokingColor(new java.awt.Color(127, 140, 141));
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(pageWidth - 80, 30);
                contentStream.showText("Page " + pageNumber);
                contentStream.endText();
            }
            pageNumber++;
        }
    }

    private float drawChallengeCard(PDPageContentStream contentStream, Challenge challenge,
                                    float y, float pageWidth) throws Exception {
        float cardHeight = 180;
        float margin = 50;
        float cardWidth = pageWidth - 2 * margin;

        // Fond de la carte avec couleur selon le statut
        java.awt.Color cardColor = challenge.isActif() ?
                new java.awt.Color(236, 240, 241) : new java.awt.Color(250, 235, 235);

        contentStream.setNonStrokingColor(cardColor);
        contentStream.addRect(margin, y - cardHeight, cardWidth, cardHeight);
        contentStream.fill();

        // Bordure colorée à gauche
        java.awt.Color borderColor = challenge.isActif() ?
                new java.awt.Color(46, 204, 113) : new java.awt.Color(231, 76, 60);

        contentStream.setNonStrokingColor(borderColor);
        contentStream.addRect(margin, y - cardHeight, 10, cardHeight);
        contentStream.fill();

        float textX = margin + 20;
        float textY = y - 25;

        // Titre du challenge (en gras) - CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(new java.awt.Color(44, 62, 80));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
        contentStream.newLineAtOffset(textX, textY);
        String titre = challenge.getTitre();
        if (titre.length() > 60) {
            titre = titre.substring(0, 57) + "...";
        }
        contentStream.showText(titre);
        contentStream.endText();

        textY -= 25;

        // Description - CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(new java.awt.Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.newLineAtOffset(textX, textY);
        String desc = challenge.getDescription();
        if (desc.length() > 80) {
            desc = desc.substring(0, 77) + "...";
        }
        contentStream.showText(desc);
        contentStream.endText();

        textY -= 30;

        // Informations en colonnes - CORRIGÉ
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

        // Colonne 1
        drawInfoItem(contentStream, textX, textY, "Duree:",
                challenge.getDureeJours() + " jours");
        drawInfoItem(contentStream, textX, textY - 20, "Difficulte:",
                challenge.getNiveauDifficulte());

        // Colonne 2
        drawInfoItem(contentStream, textX + 200, textY, "Type:",
                challenge.getTypeChallenge());
        drawInfoItem(contentStream, textX + 200, textY - 20, "Statut:",
                challenge.isActif() ? "ACTIF" : "INACTIF");

        // Badge de statut - CORRIGÉ
        float badgeX = margin + cardWidth - 100;
        float badgeY = y - 30;

        contentStream.setNonStrokingColor(borderColor);
        contentStream.addRect(badgeX, badgeY, 80, 25);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(java.awt.Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        contentStream.newLineAtOffset(badgeX + 10, badgeY + 8);
        contentStream.showText(challenge.isActif() ? "ACTIF" : "INACTIF");
        contentStream.endText();

        // Récompenses (si disponibles)
        List<Recompense> recompenses = challengeRecompenseCrud
                .getRecompensesByChallenge(challenge.getIdChallenge());

        if (!recompenses.isEmpty()) {
            textY -= 50;
            // CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(new java.awt.Color(230, 126, 34));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
            contentStream.newLineAtOffset(textX, textY);
            contentStream.showText("Recompenses: ");
            contentStream.endText();

            String recompensesText = recompenses.stream()
                    .map(Recompense::getTitre)
                    .limit(3)
                    .collect(Collectors.joining(", "));

            if (recompenses.size() > 3) {
                recompensesText += " +" + (recompenses.size() - 3);
            }

            // CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(new java.awt.Color(127, 140, 141));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
            contentStream.newLineAtOffset(textX + 75, textY);
            contentStream.showText(recompensesText);
            contentStream.endText();
        }

        return y - cardHeight;
    }

    private void drawInfoItem(PDPageContentStream contentStream, float x, float y,
                              String label, String value) throws Exception {
        // CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(new java.awt.Color(127, 140, 141));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(label);
        contentStream.endText();

        // CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(new java.awt.Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        contentStream.newLineAtOffset(x + 60, y);
        contentStream.showText(value);
        contentStream.endText();
    }


    /* ================= MISE À JOUR DES STATISTIQUES ================= */
    /* ================= MISE À JOUR DES STATISTIQUES ================= */
    private void updateStatistics() {
        if (challengeList.isEmpty()) {
            if (totalLabel != null) {
                totalLabel.setText("0");
                totalLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #FFFFFF;"); // Blanc pur
            }
            if (actifsLabel != null) {
                actifsLabel.setText("0");
                actifsLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #00FF88;"); // Vert vif néon
            }
            if (inactifsLabel != null) {
                inactifsLabel.setText("0");
                inactifsLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #FF6B6B;"); // Rouge vif
            }
            return;
        }

        // Calcul des statistiques
        int total = challengeList.size();
        long actifs = challengeList.stream().filter(Challenge::isActif).count();
        long inactifs = total - actifs;

        // Mise à jour des labels avec vérification null et couleurs améliorées
        if (totalLabel != null) {
            totalLabel.setText(String.valueOf(total));
            totalLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #FFFFFF;"); // Blanc pur
        }
        if (actifsLabel != null) {
            actifsLabel.setText(String.valueOf(actifs));
            actifsLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #00FF88;"); // Vert vif néon
        }
        if (inactifsLabel != null) {
            inactifsLabel.setText(String.valueOf(inactifs));
            inactifsLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #FF6B6B;"); // Rouge vif
        }

        // Optionnel: animation
        if (totalLabel != null) animateLabel(totalLabel);
        if (actifsLabel != null) animateLabel(actifsLabel);
        if (inactifsLabel != null) animateLabel(inactifsLabel);
    }

    /* ================= ANIMATION DES LABELS ================= */
    private void animateLabel(Label label) {
        label.setScaleX(1.1);
        label.setScaleY(1.1);

        javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(200), label);
        scaleTransition.setFromX(1.1);
        scaleTransition.setFromY(1.1);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();
    }


    /* ================= GESTION DES COACHES ================= */
    private void openCoachManager(Challenge challenge) {
        try {
            Stage stage = new Stage();
            stage.setTitle("👨‍🏫 Gérer les coaches - " + challenge.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);

            VBox root = new VBox(20);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #f5f5f5;");

            // En-tête
            Label titleLabel = new Label("👨‍🏫 Gérer les coaches pour : " + challenge.getTitre());
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

            // Liste des coaches actuels
            ListView<CoachMotivation> currentList = new ListView<>();
            ObservableList<CoachMotivation> currentCoaches =
                    FXCollections.observableArrayList(
                            challengeCoachCrud.getCoachesForChallenge(challenge.getIdChallenge())
                    );
            currentList.setItems(currentCoaches);
            currentList.setCellFactory(list -> new ListCell<CoachMotivation>() {
                @Override
                protected void updateItem(CoachMotivation coach, boolean empty) {
                    super.updateItem(coach, empty);
                    if (empty || coach == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(10);

                        Label nameLabel = new Label("👤 " + coach.getNomCoach());
                        nameLabel.setStyle("-fx-font-weight: bold;");

                        Label styleLabel = new Label("🎭 " + coach.getStyle());
                        styleLabel.setStyle("-fx-text-fill: #7F8C8D;");

                        Button removeButton = new Button("❌");
                        removeButton.setStyle(
                                "-fx-background-color: #E74C3C; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-padding: 2 5;"
                        );
                        removeButton.setOnAction(e -> {
                            boolean success = challengeCoachCrud.dissociateCoachFromChallenge(
                                    challenge.getIdChallenge(), coach.getIdCoach()
                            );
                            if (success) {
                                currentCoaches.remove(coach);
                                showAlert("Succès", "Coach retiré avec succès", Alert.AlertType.INFORMATION);
                                // Rafraîchir la table
                                challengeListView.refresh();
                            }
                        });

                        hbox.getChildren().addAll(nameLabel, styleLabel, removeButton);
                        setGraphic(hbox);
                    }
                }
            });

            VBox currentBox = new VBox(10);
            currentBox.getChildren().addAll(
                    new Label("✅ Coaches assignés (" + currentCoaches.size() + ") :"),
                    currentList
            );

            // Bouton pour ajouter un nouveau coach
            Button addNewButton = new Button("➕ Ajouter un coach");
            addNewButton.setStyle(
                    "-fx-background-color: #2ECC71; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 10 20;"
            );
            addNewButton.setOnAction(e -> {
                openCoachSelector(challenge, currentCoaches);
            });

            // Boutons de contrôle
            HBox buttonBox = new HBox(15);
            Button saveButton = new Button("💾 Enregistrer");
            saveButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
            saveButton.setOnAction(e -> {
                showAlert("Succès", "Modifications enregistrées", Alert.AlertType.INFORMATION);
                stage.close();
                challengeListView.refresh();
            });

            Button cancelButton = new Button("❌ Fermer");
            cancelButton.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white;");
            cancelButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(saveButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            root.getChildren().addAll(
                    titleLabel,
                    currentBox,
                    addNewButton,
                    buttonBox
            );

            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le gestionnaire", Alert.AlertType.ERROR);
        }
    }

    private void openCoachSelector(Challenge challenge, ObservableList<CoachMotivation> currentList) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Sélectionner des coaches");
            stage.initModality(Modality.APPLICATION_MODAL);

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));

            Label titleLabel = new Label("👤 Sélectionnez les coaches à ajouter :");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Récupérer tous les coaches disponibles
            CoachMotivationCrud coachCrud = new CoachMotivationCrud(); // Assure-toi d'avoir cette instance
            List<CoachMotivation> allCoaches = coachCrud.readAll();

            // Filtrer ceux déjà assignés
            List<CoachMotivation> availableCoaches = allCoaches.stream()
                    .filter(c -> currentList.stream()
                            .noneMatch(cc -> cc.getIdCoach() == c.getIdCoach()))
                    .collect(Collectors.toList());

            ListView<CoachMotivation> listView = new ListView<>();
            listView.setItems(FXCollections.observableArrayList(availableCoaches));
            listView.setCellFactory(list -> new ListCell<CoachMotivation>() {
                @Override
                protected void updateItem(CoachMotivation coach, boolean empty) {
                    super.updateItem(coach, empty);
                    if (empty || coach == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(10);

                        Label nameLabel = new Label("👤 " + coach.getNomCoach());
                        nameLabel.setStyle("-fx-font-weight: bold;");

                        Label styleLabel = new Label("🎭 Style: " + coach.getStyle());
                        styleLabel.setStyle("-fx-text-fill: #7F8C8D;");

                        Label statusLabel = new Label(coach.isActif() ? "✅" : "❌");
                        statusLabel.setStyle(coach.isActif() ?
                                "-fx-text-fill: green;" : "-fx-text-fill: red;");

                        hbox.getChildren().addAll(nameLabel, styleLabel, statusLabel);
                        setGraphic(hbox);
                    }
                }
            });

            // Boutons
            HBox buttonBox = new HBox(10);
            Button addButton = new Button("➕ Ajouter sélection");
            addButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white;");
            addButton.setOnAction(e -> {
                CoachMotivation selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    boolean success = challengeCoachCrud.associateCoachToChallenge(
                            challenge.getIdChallenge(),
                            selected.getIdCoach()
                    );
                    if (success) {
                        currentList.add(selected);
                        stage.close();
                    }
                }
            });

            Button cancelButton = new Button("Annuler");
            cancelButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(addButton, cancelButton);

            root.getChildren().addAll(titleLabel, listView, buttonBox);

            Scene scene = new Scene(root, 500, 400);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= POPUP COACHES ================= */
    private void showCoachesPopup(Challenge challenge, List<CoachMotivation> coaches) {
        try {
            Stage popupStage = new Stage();
            popupStage.setTitle("👤 Coaches - " + challenge.getTitre());
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(challengeListView.getScene().getWindow());

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 10;");

            Label titleLabel = new Label("👤 COACHES ASSOCIÉS");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

            ListView<CoachMotivation> coachesList = new ListView<>();
            coachesList.setItems(FXCollections.observableArrayList(coaches));
            coachesList.setCellFactory(param -> new ListCell<CoachMotivation>() {
                @Override
                protected void updateItem(CoachMotivation coach, boolean empty) {
                    super.updateItem(coach, empty);
                    if (empty || coach == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(10);
                        hbox.setAlignment(Pos.CENTER_LEFT);
                        hbox.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 5;");

                        // Avatar/icône
                        Label icon = new Label("👤");
                        icon.setStyle("-fx-font-size: 18px;");

                        VBox infoBox = new VBox(5);
                        Label nameLabel = new Label(coach.getNomCoach());
                        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000; -fx-font-size: 14px;");

                        // === AJOUT DE L'EMAIL DANS L'INFOBULLE ===
                        String email = coach.getEmail();
                        if (email != null && !email.isEmpty() && !email.equals("null")) {
                            nameLabel.setTooltip(new Tooltip("Email: " + email));
                        } else {
                            nameLabel.setTooltip(new Tooltip("Email non renseigné"));
                        }

                        Label styleLabel = new Label("Style: " + coach.getStyle());
                        styleLabel.setStyle("-fx-text-fill: #BDC3C7; -fx-font-size: 12px;");

                        // Statut avec badge coloré
                        Label statusLabel = new Label(coach.isActif() ? "✅ ACTIF" : "❌ INACTIF");
                        statusLabel.setStyle(coach.isActif() ?
                                "-fx-text-fill: #2ECC71; -fx-font-size: 11px; -fx-font-weight: bold;" :
                                "-fx-text-fill: #E74C3C; -fx-font-size: 11px; -fx-font-weight: bold;");

                        infoBox.getChildren().addAll(nameLabel, styleLabel, statusLabel);

                        // === NOUVEAU BOUTON "CONSULTER COACH" (EMAIL) ===
                        Button emailBtn = new Button("📧 Consulter coach");
                        emailBtn.setStyle(
                                "-fx-background-color: #9B59B6; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-padding: 5 10; " +
                                        "-fx-background-radius: 5;"
                        );
                        emailBtn.setTooltip(new Tooltip("Envoyer un email au coach"));

                        emailBtn.setOnAction(e -> {
                            try {
                                // Recharger le coach complet pour avoir l'email
                                CoachMotivationCrud coachCrud = new CoachMotivationCrud();
                                List<CoachMotivation> allCoaches = coachCrud.readAll();
                                CoachMotivation fullCoach = allCoaches.stream()
                                        .filter(c -> c.getIdCoach() == coach.getIdCoach())
                                        .findFirst()
                                        .orElse(coach);

                                // Ouvrir la boîte de dialogue d'envoi d'email
                                showEmailDialog(fullCoach, challenge);

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showAlert("Erreur", "Impossible de charger les informations du coach", Alert.AlertType.ERROR);
                            }
                        });

                        Button detailBtn = new Button("📋 Détails");
                        detailBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
                        detailBtn.setOnAction(e -> {
                            try {
                                CoachMotivationCrud coachCrud = new CoachMotivationCrud();
                                List<CoachMotivation> allCoaches = coachCrud.readAll();
                                CoachMotivation fullCoach = allCoaches.stream()
                                        .filter(c -> c.getIdCoach() == coach.getIdCoach())
                                        .findFirst()
                                        .orElse(coach);
                                showCoachDetail(fullCoach);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showAlert("Erreur", "Impossible de charger les détails du coach", Alert.AlertType.ERROR);
                            }
                        });

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        hbox.getChildren().addAll(icon, infoBox, spacer, emailBtn, detailBtn);
                        setGraphic(hbox);
                        setCursor(Cursor.HAND);

                        // Effet au survol
                        setOnMouseEntered(e -> {
                            setStyle("-fx-background-color: rgba(52,152,219,0.2);");
                        });
                        setOnMouseExited(e -> {
                            setStyle("");
                        });
                    }
                }
            });

            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
            closeButton.setOnAction(e -> popupStage.close());

            root.getChildren().addAll(titleLabel, coachesList, closeButton);

            Scene scene = new Scene(root, 650, 400); // Agrandi pour accueillir le nouveau bouton
            popupStage.setScene(scene);
            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= POPUP RÉCOMPENSES ================= */
    private void showRecompensesPopup(Challenge challenge, List<Recompense> recompenses) {
        try {
            Stage popupStage = new Stage();
            popupStage.setTitle("🎁 Récompenses - " + challenge.getTitre());
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(challengeListView.getScene().getWindow());

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 10;");

            Label titleLabel = new Label("🎁 RÉCOMPENSES DISPONIBLES");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

            ListView<Recompense> recompensesList = new ListView<>();
            recompensesList.setItems(FXCollections.observableArrayList(recompenses));
            recompensesList.setCellFactory(param -> new ListCell<Recompense>() {
                @Override
                protected void updateItem(Recompense recompense, boolean empty) {
                    super.updateItem(recompense, empty);
                    if (empty || recompense == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(10);
                        hbox.setAlignment(Pos.CENTER_LEFT);
                        hbox.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 5;");

                        // Icône selon le type
                        Label icon = new Label(getIconForRecompenseType(recompense.getTypeRecompense()));
                        icon.setStyle("-fx-font-size: 18px;");

                        VBox infoBox = new VBox(5);
                        Label nameLabel = new Label(recompense.getTitre());
                        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");

                        Label typeLabel = new Label("Type: " + recompense.getTypeRecompense());
                        typeLabel.setStyle("-fx-text-fill: #BDC3C7; -fx-font-size: 12px;");

                        // Condition d'obtention
                        String condition = recompense.getConditionObtention();
                        if (condition != null && !condition.trim().isEmpty()) {
                            Label conditionLabel = new Label("Condition: " + condition);
                            conditionLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 11px;");
                            conditionLabel.setWrapText(true);
                            conditionLabel.setMaxWidth(350);
                            infoBox.getChildren().addAll(nameLabel, typeLabel, conditionLabel);
                        } else {
                            infoBox.getChildren().addAll(nameLabel, typeLabel);
                        }

                        // Statut
                        Label statusLabel = new Label(recompense.isActif() ? "✅ ACTIF" : "❌ INACTIF");
                        statusLabel.setStyle(recompense.isActif() ?
                                "-fx-text-fill: #2ECC71; -fx-font-size: 11px; -fx-font-weight: bold;" :
                                "-fx-text-fill: #E74C3C; -fx-font-size: 11px; -fx-font-weight: bold;");
                        infoBox.getChildren().add(statusLabel);

                        // Bouton pour voir les détails
                        Button detailBtn = new Button("👁️ Voir");
                        detailBtn.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8; -fx-font-size: 11px;");
                        detailBtn.setOnAction(e -> showRecompenseDetail(recompense));

                        HBox bottomBox = new HBox(10);
                        bottomBox.setAlignment(Pos.CENTER_LEFT);
                        bottomBox.getChildren().addAll(statusLabel, detailBtn);

                        hbox.getChildren().addAll(icon, infoBox);
                        setGraphic(hbox);

                        // Rendre toute la cellule cliquable
                        setCursor(Cursor.HAND);
                        setOnMouseClicked(e -> {
                            if (e.getClickCount() == 1) {
                                showRecompenseDetail(recompense);
                            }
                        });

                        // Effet au survol
                        setOnMouseEntered(e -> {
                            setStyle("-fx-background-color: rgba(241,196,15,0.2);");
                        });
                        setOnMouseExited(e -> {
                            setStyle("");
                        });
                    }
                }
            });

            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
            closeButton.setOnAction(e -> popupStage.close());

            root.getChildren().addAll(titleLabel, recompensesList, closeButton);

            Scene scene = new Scene(root, 550, 450);
            popupStage.setScene(scene);
            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= MÉTHODE POUR ICÔNES DE RÉCOMPENSES ================= */
    private String getIconForRecompenseType(String type) {
        if (type == null) return "🎁";

        switch (type.toLowerCase()) {
            case "médaille":
            case "medaille":
                return "🥇";
            case "badge":
                return "🎖️";
            case "certificat":
                return "📜";
            case "points":
                return "⭐";
            case "réduction":
            case "reduction":
                return "💸";
            case "cadeau":
                return "🎁";
            case "accès premium":
            case "acces premium":
                return "👑";
            case "autre":
                return "🏆";
            default:
                return "🎁";
        }
    }

    /* ================= CARTE DÉTAIL COACH ================= */
    private void showCoachDetail(CoachMotivation coach) {
        try {
            Stage detailStage = new Stage();
            detailStage.setTitle("👤 Détails du Coach");
            detailStage.initModality(Modality.WINDOW_MODAL);
            detailStage.initOwner(challengeListView.getScene().getWindow());

            // Création d'une carte similaire à une carte étudiant
            VBox card = new VBox(20);
            card.setPadding(new Insets(25));
            card.setStyle("-fx-background-color: linear-gradient(to bottom right, #3498DB, #2C3E50); " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: rgba(255,255,255,0.2); " +
                    "-fx-border-radius: 15; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 5);");

            // En-tête avec photo de profil (simulée)
            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);

            Circle profileCircle = new Circle(40);
            profileCircle.setFill(javafx.scene.paint.Color.web("#1ABC9C")); // Utiliser javafx.scene.paint.Color
            profileCircle.setStroke(javafx.scene.paint.Color.WHITE); // Utiliser javafx.scene.paint.Color
            profileCircle.setStrokeWidth(3);

            Label profileIcon = new Label("👨‍🏫");
            profileIcon.setStyle("-fx-font-size: 32px;");
            StackPane profilePane = new StackPane(profileCircle, profileIcon);

            VBox headerInfo = new VBox(5);
            Label nameLabel = new Label(coach.getNomCoach());
            nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

            Label roleLabel = new Label("Coach Motivation");
            roleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9); -fx-font-style: italic;");

            headerInfo.getChildren().addAll(nameLabel, roleLabel);
            header.getChildren().addAll(profilePane, headerInfo);

            // Section des informations
            // ========== SECTION DES INFORMATIONS ==========
            VBox infoSection = new VBox(15);
            infoSection.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10; -fx-padding: 15;");

// Style de coaching
            HBox styleBox = new HBox(10);
            styleBox.setAlignment(Pos.CENTER_LEFT);
            Label styleIcon = new Label("🎭");
            Label styleTitle = new Label("Style:");
            styleTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 100;");
            Label styleValue = new Label(coach.getStyle());
            styleValue.setStyle("-fx-text-fill: #F1C40F; -fx-font-weight: bold;");
            styleBox.getChildren().addAll(styleIcon, styleTitle, styleValue);

// EMAIL
            HBox emailBox = new HBox(10);
            emailBox.setAlignment(Pos.CENTER_LEFT);
            Label emailIcon = new Label("📧");
            Label emailTitle = new Label("Email:");
            emailTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 100;");

            String emailText = coach.getEmail();
            Label emailValue = new Label();

            if (emailText != null && !emailText.isEmpty() && !emailText.equals("null")) {
                emailValue.setText(emailText);
                emailValue.setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold; -fx-font-size: 13px;");
                emailValue.setWrapText(true);
                emailValue.setMaxWidth(350);
                emailValue.setTooltip(new Tooltip("Email: " + emailText));
            } else {
                emailValue.setText("Non renseigné");
                emailValue.setStyle("-fx-text-fill: #7F8C8D; -fx-font-style: italic; -fx-font-size: 12px;");
            }
            emailBox.getChildren().addAll(emailIcon, emailTitle, emailValue);

// Description
            HBox descBox = new HBox(10);
            descBox.setAlignment(Pos.CENTER_LEFT);
            Label descIcon = new Label("📝");
            Label descTitle = new Label("Description:");
            descTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 100;");

            String description = coach.getDescription();
            Label descValue = new Label(description != null && !description.isEmpty() ?
                    description : "Aucune description disponible");
            descValue.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 12px;");
            descValue.setWrapText(true);
            descValue.setMaxWidth(350);
            descBox.getChildren().addAll(descIcon, descTitle, descValue);

// Statut
            HBox statusBox = new HBox(10);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            Label statusIcon = new Label("📈");
            Label statusTitle = new Label("Statut:");
            statusTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 100;");
            Label statusValue = new Label(coach.isActif() ? "ACTIF" : "INACTIF");
            statusValue.setStyle(coach.isActif() ?
                    "-fx-text-fill: #2ECC71; -fx-font-weight: bold;" :
                    "-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
            statusBox.getChildren().addAll(statusIcon, statusTitle, statusValue);

// ID Coach
            HBox idBox = new HBox(10);
            idBox.setAlignment(Pos.CENTER_LEFT);
            Label idIcon = new Label("🆔");
            Label idTitle = new Label("ID Coach:");
            idTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 100;");
            Label idValue = new Label(String.valueOf(coach.getIdCoach()));
            idValue.setStyle("-fx-text-fill: #BDC3C7; -fx-font-size: 12px;");
            idBox.getChildren().addAll(idIcon, idTitle, idValue);

// Assembler toutes les informations
            infoSection.getChildren().addAll(styleBox, emailBox, descBox, statusBox, idBox);

            // Bouton fermer
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 8; " +
                    "-fx-padding: 10 30;");
            closeButton.setOnAction(e -> detailStage.close());

            // Assemblage
            card.getChildren().addAll(header, infoSection, closeButton);
            card.setAlignment(Pos.CENTER);

            Scene scene = new Scene(card, 500, 500); // Ajusté pour la description
            detailStage.setScene(scene);
            detailStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails du coach", Alert.AlertType.ERROR);
        }
    }

    /* ================= CARTE DÉTAIL RÉCOMPENSE ================= */
    private void showRecompenseDetail(Recompense recompense) {
        try {
            Stage detailStage = new Stage();
            detailStage.setTitle("🎁 Détails de la Récompense");
            detailStage.initModality(Modality.WINDOW_MODAL);
            detailStage.initOwner(challengeListView.getScene().getWindow());

            // Création d'une carte colorée selon le type
            VBox card = new VBox(20);
            card.setPadding(new Insets(25));

            // Couleur de fond selon le type
            String backgroundColor = getColorForRecompenseType(recompense.getTypeRecompense());
            card.setStyle("-fx-background-color: " + backgroundColor + "; " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: rgba(255,255,255,0.3); " +
                    "-fx-border-radius: 15; " +
                    "-fx-border-width: 2; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0, 0, 8);");

            // En-tête avec grande icône
            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);

            // Icône principale
            Label mainIcon = new Label(getIconForRecompenseType(recompense.getTypeRecompense()));
            mainIcon.setStyle("-fx-font-size: 48px;");

            VBox headerInfo = new VBox(5);
            Label nameLabel = new Label(recompense.getTitre());
            nameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: white;");

            Label typeLabel = new Label(recompense.getTypeRecompense().toUpperCase());
            typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.9); " +
                    "-fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.2); " +
                    "-fx-padding: 3 10; -fx-background-radius: 10;");

            headerInfo.getChildren().addAll(nameLabel, typeLabel);
            header.getChildren().addAll(mainIcon, headerInfo);

            // Section des informations détaillées
            VBox infoSection = new VBox(15);
            infoSection.setStyle("-fx-background-color: rgba(255,255,255,0.15); " +
                    "-fx-background-radius: 12; -fx-padding: 20;");

            // Description
            HBox descBox = new HBox(10);
            descBox.setAlignment(Pos.CENTER_LEFT);
            Label descIcon = new Label("📄");
            Label descTitle = new Label("Description:");
            descTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 120;");

            String description = recompense.getDescription();
            Label descValue = new Label(description != null && !description.isEmpty() ?
                    description : "Aucune description disponible");
            descValue.setStyle("-fx-text-fill: #ECF0F1; -fx-font-size: 13px;");
            descValue.setWrapText(true);
            descValue.setMaxWidth(400);

            VBox descContent = new VBox(5);
            descContent.getChildren().addAll(descTitle, descValue);
            descBox.getChildren().addAll(descIcon, descContent);

            // Condition d'obtention
            HBox conditionBox = new HBox(10);
            conditionBox.setAlignment(Pos.CENTER_LEFT);
            Label conditionIcon = new Label("🎯");
            Label conditionTitle = new Label("Condition:");
            conditionTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 120;");

            String condition = recompense.getConditionObtention();
            Label conditionValue = new Label(condition != null && !condition.trim().isEmpty() ?
                    condition : "Aucune condition spécifique");
            conditionValue.setStyle("-fx-text-fill: #F1C40F; -fx-font-weight: bold; -fx-font-size: 13px;");
            conditionValue.setWrapText(true);
            conditionValue.setMaxWidth(400);

            VBox conditionContent = new VBox(5);
            conditionContent.getChildren().addAll(conditionTitle, conditionValue);
            conditionBox.getChildren().addAll(conditionIcon, conditionContent);

            // Informations techniques
            HBox techBox = new HBox(20);
            techBox.setAlignment(Pos.CENTER_LEFT);

            // Statut
            VBox statusBox = new VBox(5);
            Label statusIcon = new Label("📊");
            statusIcon.setStyle("-fx-font-size: 16px;");
            Label statusLabel = new Label("Statut:");
            statusLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");
            Label statusValue = new Label(recompense.isActif() ? "ACTIVE" : "INACTIVE");
            statusValue.setStyle(recompense.isActif() ?
                    "-fx-text-fill: #2ECC71; -fx-font-weight: bold; -fx-font-size: 14px;" :
                    "-fx-text-fill: #E74C3C; -fx-font-weight: bold; -fx-font-size: 14px;");
            statusBox.getChildren().addAll(statusIcon, statusLabel, statusValue);

            // ID
            VBox idBox = new VBox(5);
            Label idIcon = new Label("🆔");
            idIcon.setStyle("-fx-font-size: 16px;");
            Label idLabel = new Label("ID:");
            idLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");
            Label idValue = new Label(String.valueOf(recompense.getIdRecompense()));
            idValue.setStyle("-fx-text-fill: #BDC3C7; -fx-font-weight: bold; -fx-font-size: 14px;");
            idBox.getChildren().addAll(idIcon, idLabel, idValue);

            techBox.getChildren().addAll(statusBox, idBox);

            infoSection.getChildren().addAll(descBox, conditionBox, techBox);

            // Bouton fermer
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: rgba(255,255,255,0.9); " +
                    "-fx-text-fill: #2C3E50; -fx-font-weight: bold; " +
                    "-fx-background-radius: 8; -fx-padding: 10 30; " +
                    "-fx-font-size: 14px; -fx-cursor: hand;");
            closeButton.setOnAction(e -> detailStage.close());

            // Effet au survol du bouton
            closeButton.setOnMouseEntered(e -> {
                closeButton.setStyle("-fx-background-color: white; " +
                        "-fx-text-fill: #2C3E50; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 10 30; " +
                        "-fx-font-size: 14px; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            });
            closeButton.setOnMouseExited(e -> {
                closeButton.setStyle("-fx-background-color: rgba(255,255,255,0.9); " +
                        "-fx-text-fill: #2C3E50; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 10 30; " +
                        "-fx-font-size: 14px; -fx-cursor: hand;");
            });

            // Assemblage
            card.getChildren().addAll(header, infoSection, closeButton);
            card.setAlignment(Pos.CENTER);

            Scene scene = new Scene(card, 500, 550);
            detailStage.setScene(scene);
            detailStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails de la récompense", Alert.AlertType.ERROR);
        }
    }

    /* ================= MÉTHODE POUR COULEURS DE RÉCOMPENSES ================= */
    private String getColorForRecompenseType(String type) {
        if (type == null) return "linear-gradient(to bottom right, #9B59B6, #8E44AD)";

        switch (type.toLowerCase()) {
            case "médaille":
            case "medaille":
                return "linear-gradient(to bottom right, #F1C40F, #F39C12)"; // Or
            case "badge":
                return "linear-gradient(to bottom right, #3498DB, #2980B9)"; // Bleu
            case "certificat":
                return "linear-gradient(to bottom right, #2ECC71, #27AE60)"; // Vert
            case "points":
                return "linear-gradient(to bottom right, #E74C3C, #C0392B)"; // Rouge
            case "réduction":
            case "reduction":
                return "linear-gradient(to bottom right, #9B59B6, #8E44AD)"; // Violet
            case "cadeau":
                return "linear-gradient(to bottom right, #E67E22, #D35400)"; // Orange
            case "accès premium":
            case "acces premium":
                return "linear-gradient(to bottom right, #1ABC9C, #16A085)"; // Turquoise
            case "autre":
                return "linear-gradient(to bottom right, #34495E, #2C3E50)"; // Gris foncé
            default:
                return "linear-gradient(to bottom right, #9B59B6, #8E44AD)";
        }


    }

    /* ================= CARTE DÉTAIL CHALLENGE ================= */
    private void showChallengeDetail(Challenge challenge) {
        try {
            Stage detailStage = new Stage();
            detailStage.setTitle("🏆 Détails du Challenge");
            detailStage.initModality(Modality.WINDOW_MODAL);
            detailStage.initOwner(challengeListView.getScene().getWindow());

            // ============= CRÉATION DE LA CARTE PROFESSIONNELLE =============
            VBox card = new VBox(25);
            card.setPadding(new Insets(30));

            // Dégradé de couleur selon le statut et la difficulté
            String gradientColor = getGradientForChallenge(challenge);
            card.setStyle(
                    "-fx-background-color: " + gradientColor + ";" +
                            "-fx-background-radius: 25;" +
                            "-fx-border-radius: 25;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-color: rgba(255,255,255,0.3);" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0, 0, 10);" +
                            "-fx-min-width: 600;" +
                            "-fx-max-width: 700;"
            );

            // ============= EN-TÊTE AVEC BADGE ET TITRE =============
            HBox headerBox = new HBox(20);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            // Icône principale avec cercle de fond
            StackPane iconContainer = new StackPane();
            iconContainer.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.2);" +
                            "-fx-background-radius: 30;" +
                            "-fx-padding: 20;"
            );

            Label mainIcon = new Label(getIconForChallenge(challenge));
            mainIcon.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");
            iconContainer.getChildren().add(mainIcon);

            // Titre et type
            VBox titleBox = new VBox(10);

            Label titleLabel = new Label(challenge.getTitre());
            titleLabel.setStyle(
                    "-fx-font-size: 28px;" +
                            "-fx-font-weight: 900;" +
                            "-fx-text-fill: white;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);"
            );
            titleLabel.setWrapText(true);

            HBox typeBadgeBox = new HBox(10);
            typeBadgeBox.setAlignment(Pos.CENTER_LEFT);

            Label typeBadge = new Label(challenge.getTypeChallenge());
            typeBadge.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.25);" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 8 20;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: white;"
            );

            Label difficultyBadge = new Label(challenge.getNiveauDifficulte());
            difficultyBadge.setStyle(getDifficultyStyle(challenge.getNiveauDifficulte()));

            typeBadgeBox.getChildren().addAll(typeBadge, difficultyBadge);
            titleBox.getChildren().addAll(titleLabel, typeBadgeBox);

            headerBox.getChildren().addAll(iconContainer, titleBox);

            // ============= BADGE DE STATUT =============
            HBox statusBox = new HBox();
            statusBox.setAlignment(Pos.CENTER_RIGHT);

            HBox.setHgrow(titleBox, Priority.ALWAYS);

            Label statusLabel = new Label();
            if (challenge.isActif()) {
                statusLabel.setText("● ACTIF");
                statusLabel.setStyle(
                        "-fx-background-color: rgba(46,204,113,0.3);" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 8 25;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-border-color: rgba(46,204,113,0.8);" +
                                "-fx-border-radius: 20;" +
                                "-fx-border-width: 1.5;"
                );
            } else {
                statusLabel.setText("● INACTIF");
                statusLabel.setStyle(
                        "-fx-background-color: rgba(231,76,60,0.3);" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 8 25;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-border-color: rgba(231,76,60,0.8);" +
                                "-fx-border-radius: 20;" +
                                "-fx-border-width: 1.5;"
                );
            }

            headerBox.getChildren().add(statusBox);

            // ============= SÉPARATEUR ÉLÉGANT =============
            Separator separator = new Separator();
            separator.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.3);" +
                            "-fx-background-insets: 0;" +
                            "-fx-background-radius: 2;" +
                            "-fx-pref-height: 2;"
            );

            // ============= SECTION DESCRIPTION =============
            VBox descriptionSection = new VBox(15);

            Label descTitle = new Label("📝 DESCRIPTION");
            descTitle.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-font-weight: 700;" +
                            "-fx-text-fill: rgba(255,255,255,0.9);"
            );

            Label descriptionContent = new Label(challenge.getDescription());
            descriptionContent.setWrapText(true);
            descriptionContent.setStyle(
                    "-fx-font-size: 15px;" +
                            "-fx-line-spacing: 5;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 20;" +
                            "-fx-background-color: rgba(255,255,255,0.1);" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-color: rgba(255,255,255,0.2);" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-width: 1;"
            );
            descriptionContent.setMaxWidth(620);

            descriptionSection.getChildren().addAll(descTitle, descriptionContent);

            // ============= GRILLE D'INFORMATIONS PRINCIPALES =============
            // ============= GRILLE D'INFORMATIONS PRINCIPALES - VERSION AMÉLIORÉE =============
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(20);
            infoGrid.setVgap(15);
            infoGrid.setStyle(
                    "-fx-padding: 20;" +
                            "-fx-background-color: white;" + // Fond blanc
                            "-fx-background-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);" // Ombre légère
            );

// Configuration des colonnes
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPrefWidth(200);
            col1.setHgrow(Priority.SOMETIMES);

            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPrefWidth(200);
            col2.setHgrow(Priority.SOMETIMES);

            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPrefWidth(220);
            col3.setHgrow(Priority.ALWAYS);

            infoGrid.getColumnConstraints().addAll(col1, col2, col3);

// ===== LIGNE 1 =====
// DURÉE
            VBox durationBox = createModernInfoBox("⏱️", "DURÉE",
                    challenge.getDureeJours() + " jours", "#2C3E50");
            infoGrid.add(durationBox, 0, 0);

// DIFFICULTÉ
            VBox difficultyBox = createModernInfoBox("🎯", "DIFFICULTÉ",
                    challenge.getNiveauDifficulte(), getDifficultyTextColor(challenge.getNiveauDifficulte()));
            infoGrid.add(difficultyBox, 1, 0);

// DATE DE CRÉATION
            VBox creationBox = createModernInfoBox("📅", "DATE DE CRÉATION",
                    getFormattedDate(challenge), "#16A085");
            infoGrid.add(creationBox, 2, 0);

// ===== LIGNE 2 =====
// TYPE
            VBox typeInfoBox = createModernInfoBox("📂", "TYPE",
                    challenge.getTypeChallenge(), "#E67E22");
            infoGrid.add(typeInfoBox, 0, 1);

// ID CHALLENGE
            VBox idBox = createModernInfoBox("🆔", "ID CHALLENGE",
                    "#" + challenge.getIdChallenge(), "#7F8C8D");
            infoGrid.add(idBox, 1, 1);

// DATE D'EXPIRATION
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(challenge.getDureeJours());
            String expirationDateStr = expirationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            VBox expirationBox = createModernInfoBox("⏰", "DATE D'EXPIRATION",
                    expirationDateStr, "#C0392B");
            infoGrid.add(expirationBox, 2, 1);

            // Récupérer les coaches et récompenses associés
            List<CoachMotivation> coaches = challengeCoachCrud.getCoachesForChallenge(challenge.getIdChallenge());
            List<Recompense> recompenses = challengeRecompenseCrud.getRecompensesByChallenge(challenge.getIdChallenge());



// ============= AJOUTER LE BLOCK statsBox ICI =============
            HBox statsBox = new HBox(20);
            statsBox.setAlignment(Pos.CENTER_LEFT);
            statsBox.setStyle(
                    "-fx-padding: 20;" +
                            "-fx-background-color: rgba(0,0,0,0.2);" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-color: rgba(255,255,255,0.1);" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-width: 1;"
            );
// =======================================================


            // Badge Coaches
            VBox coachStatsBox = new VBox(8);
            coachStatsBox.setAlignment(Pos.CENTER_LEFT);

            HBox coachHeader = new HBox(10);
            coachHeader.setAlignment(Pos.CENTER_LEFT);
            Label coachIcon = new Label("👨‍🏫");
            coachIcon.setStyle("-fx-font-size: 20px;");
            Label coachCount = new Label(String.valueOf(coaches.size()));
            coachCount.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #3498DB;");
            coachHeader.getChildren().addAll(coachIcon, coachCount);

            Label coachLabel = new Label("Coaches associés");
            coachLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
            coachStatsBox.getChildren().addAll(coachHeader, coachLabel);

            // Badge Récompenses
            VBox rewardStatsBox = new VBox(8);
            rewardStatsBox.setAlignment(Pos.CENTER_LEFT);

            HBox rewardHeader = new HBox(10);
            rewardHeader.setAlignment(Pos.CENTER_LEFT);
            Label rewardIcon = new Label("🎁");
            rewardIcon.setStyle("-fx-font-size: 20px;");
            Label rewardCount = new Label(String.valueOf(recompenses.size()));
            rewardCount.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #F1C40F;");
            rewardHeader.getChildren().addAll(rewardIcon, rewardCount);

            Label rewardLabel = new Label("Récompenses disponibles");
            rewardLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
            rewardStatsBox.getChildren().addAll(rewardHeader, rewardLabel);

            // Badge Statut
            VBox statusStatsBox = new VBox(8);
            statusStatsBox.setAlignment(Pos.CENTER_LEFT);

            HBox statusHeader = new HBox(10);
            statusHeader.setAlignment(Pos.CENTER_LEFT);
            Label statusIcon = new Label("⚡");
            statusIcon.setStyle("-fx-font-size: 20px;");
            Label statusValue = new Label(challenge.isActif() ? "ACTIF" : "INACTIF");
            statusValue.setStyle(challenge.isActif() ?
                    "-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #2ECC71;" :
                    "-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #E74C3C;");
            statusHeader.getChildren().addAll(statusIcon, statusValue);

            Label statusDesc = new Label("État actuel");
            statusDesc.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
            statusStatsBox.getChildren().addAll(statusHeader, statusDesc);

            statsBox.getChildren().addAll(coachStatsBox, rewardStatsBox, statusStatsBox);

            // ============= LISTE DES COACHES (si disponibles) =============
            VBox coachesSection = new VBox(15);
            if (!coaches.isEmpty()) {
                Label coachesTitle = new Label("👨‍🏫 COACHES ASSOCIÉS");
                coachesTitle.setStyle(
                        "-fx-font-size: 14px;" +
                                "-fx-font-weight: 700;" +
                                "-fx-text-fill: rgba(255,255,255,0.9);"
                );

                // CORRECTION: Utiliser FlowPane au lieu de HBox avec setWrapText
                FlowPane coachesList = new FlowPane(Orientation.HORIZONTAL, 15, 15);
                coachesList.setAlignment(Pos.CENTER_LEFT);

                for (int i = 0; i < Math.min(coaches.size(), 3); i++) {
                    CoachMotivation coach = coaches.get(i);
                    VBox coachCard = createMiniCoachCard(coach);
                    coachesList.getChildren().add(coachCard);
                }

                if (coaches.size() > 3) {
                    Label moreLabel = new Label("+" + (coaches.size() - 3) + " autres");
                    moreLabel.setStyle(
                            "-fx-background-color: rgba(255,255,255,0.2);" +
                                    "-fx-background-radius: 20;" +
                                    "-fx-padding: 8 15;" +
                                    "-fx-font-size: 12px;" +
                                    "-fx-text-fill: white;"
                    );
                    moreLabel.setAlignment(Pos.CENTER);

                    // Centrer verticalement
                    VBox moreBox = new VBox(moreLabel);
                    moreBox.setAlignment(Pos.CENTER);
                    moreBox.setPrefHeight(70);
                    coachesList.getChildren().add(moreBox);
                }

                coachesSection.getChildren().addAll(coachesTitle, coachesList);
            }

            // ============= LISTE DES RÉCOMPENSES (si disponibles) =============
            VBox rewardsSection = new VBox(15);
            if (!recompenses.isEmpty()) {
                Label rewardsTitle = new Label("🎁 RÉCOMPENSES ASSOCIÉES");
                rewardsTitle.setStyle(
                        "-fx-font-size: 14px;" +
                                "-fx-font-weight: 700;" +
                                "-fx-text-fill: rgba(255,255,255,0.9);"
                );

                // CORRECTION: Utiliser FlowPane au lieu de HBox avec setWrapText
                FlowPane rewardsList = new FlowPane(Orientation.HORIZONTAL, 15, 15);
                rewardsList.setAlignment(Pos.CENTER_LEFT);

                for (int i = 0; i < Math.min(recompenses.size(), 3); i++) {
                    Recompense reward = recompenses.get(i);
                    VBox rewardCard = createMiniRewardCard(reward);
                    rewardsList.getChildren().add(rewardCard);
                }

                if (recompenses.size() > 3) {
                    Label moreLabel = new Label("+" + (recompenses.size() - 3) + " autres");
                    moreLabel.setStyle(
                            "-fx-background-color: rgba(255,255,255,0.2);" +
                                    "-fx-background-radius: 20;" +
                                    "-fx-padding: 8 15;" +
                                    "-fx-font-size: 12px;" +
                                    "-fx-text-fill: white;"
                    );
                    moreLabel.setAlignment(Pos.CENTER);

                    // Centrer verticalement
                    VBox moreBox = new VBox(moreLabel);
                    moreBox.setAlignment(Pos.CENTER);
                    moreBox.setPrefHeight(70);
                    rewardsList.getChildren().add(moreBox);
                }

                rewardsSection.getChildren().addAll(rewardsTitle, rewardsList);
            }

            // ============= PIED DE CARTE =============
            HBox footerBox = new HBox();
            footerBox.setAlignment(Pos.CENTER_RIGHT);
            footerBox.setStyle("-fx-padding: 15 0 0 0;");

            Label footerLabel = new Label("✨ Challenge créé le " + getFormattedDate(challenge));
            footerLabel.setStyle(
                    "-fx-font-size: 11px;" +
                            "-fx-text-fill: rgba(255,255,255,0.6);" +
                            "-fx-font-style: italic;"
            );
            footerBox.getChildren().add(footerLabel);

            // ============= BOUTON FERMER =============
            Button closeButton = new Button("Fermer");
            closeButton.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.9);" +
                            "-fx-text-fill: #2C3E50;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 12 40;" +
                            "-fx-background-radius: 30;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
            );

            closeButton.setOnMouseEntered(e -> {
                closeButton.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-text-fill: #2C3E50;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 12 40;" +
                                "-fx-background-radius: 30;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);"
                );
            });

            closeButton.setOnMouseExited(e -> {
                closeButton.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.9);" +
                                "-fx-text-fill: #2C3E50;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 12 40;" +
                                "-fx-background-radius: 30;" +
                                "-fx-cursor: hand;"
                );
            });

            closeButton.setOnAction(e -> detailStage.close());

            // ============= ASSEMBLAGE FINAL =============
            VBox content = new VBox(20);
            content.getChildren().addAll(
                    headerBox,
                    separator,
                    descriptionSection,
                    infoGrid,
                    statsBox
            );

            if (!coaches.isEmpty()) {
                content.getChildren().add(coachesSection);
            }

            if (!recompenses.isEmpty()) {
                content.getChildren().add(rewardsSection);
            }

            content.getChildren().addAll(footerBox, closeButton);

            card.getChildren().add(content);

            // ScrollPane pour le contenu
            ScrollPane scrollPane = new ScrollPane(card);
            scrollPane.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background: transparent;" +
                            "-fx-border-color: transparent;"
            );
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            Scene scene = new Scene(scrollPane, 700, 800);
            detailStage.setScene(scene);
            detailStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails du challenge: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    /* ================= MISSING HELPER METHODS FOR CHALLENGE DETAIL CARD ================= */

    /**
     * Returns a gradient color based on challenge status and difficulty
     */
    private String getGradientForChallenge(Challenge challenge) {
        if (challenge == null) {
            return "linear-gradient(to bottom right, #34495E, #2C3E50)";
        }

        if (!challenge.isActif()) {
            return "linear-gradient(to bottom right, #7F8C8D, #2C3E50)";
        }

        switch (challenge.getNiveauDifficulte().toLowerCase()) {
            case "facile":
                return "linear-gradient(to bottom right, #27AE60, #229954)";
            case "moyen":
                return "linear-gradient(to bottom right, #F39C12, #E67E22)";
            case "difficile":
                return "linear-gradient(to bottom right, #E74C3C, #C0392B)";
            case "expert":
                return "linear-gradient(to bottom right, #8E44AD, #6C3483)";
            default:
                return "linear-gradient(to bottom right, #3498DB, #2980B9)";
        }
    }

    /**
     * Returns an appropriate icon based on challenge type
     */
    private String getIconForChallenge(Challenge challenge) {
        if (challenge == null) {
            return "🏆";
        }

        switch (challenge.getTypeChallenge().toLowerCase()) {
            case "programmation":
                return "💻";
            case "design":
                return "🎨";
            case "marketing":
                return "📈";
            case "business":
                return "💼";
            case "personnel":
                return "🧘";
            default:
                return "🏆";
        }
    }

    /**
     * Returns CSS style for difficulty badge
     */
    private String getDifficultyStyle(String difficulty) {
        if (difficulty == null) {
            difficulty = "Moyen";
        }

        String baseStyle = "-fx-background-radius: 20; -fx-padding: 8 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;";

        switch (difficulty.toLowerCase()) {
            case "facile":
                return baseStyle + " -fx-background-color: #27AE60;";
            case "moyen":
                return baseStyle + " -fx-background-color: #F39C12;";
            case "difficile":
                return baseStyle + " -fx-background-color: #E74C3C;";
            case "expert":
                return baseStyle + " -fx-background-color: #8E44AD;";
            default:
                return baseStyle + " -fx-background-color: #3498DB;";
        }
    }

    /**
     * Creates an information box for the challenge detail card
     */
    private VBox createInfoBox(String icon, String title, String value, String color) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(
                "-fx-padding: 12;" +
                        "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-background-radius: 12;"
        );

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: rgba(255,255,255,0.8);"
        );

        header.getChildren().addAll(iconLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: " + color + ";"
        );
        valueLabel.setWrapText(true);

        box.getChildren().addAll(header, valueLabel);

        return box;
    }

    /**
     * Returns a color based on difficulty level
     */
    private String getDifficultyColor(String difficulty) {
        if (difficulty == null) {
            return "#3498DB";
        }

        switch (difficulty.toLowerCase()) {
            case "facile":
                return "#2ECC71";
            case "moyen":
                return "#F39C12";
            case "difficile":
                return "#E74C3C";
            case "expert":
                return "#9B59B6";
            default:
                return "#3498DB";
        }
    }

    /**
     * Returns formatted date for challenge
     */
    private String getFormattedDate(Challenge challenge) {
        // Since Challenge class doesn't seem to have a creation date field,
        // you can either:
        // 1. Add a creationDate field to Challenge class
        // 2. Return a default value
        // 3. Use the current date

        // Option 2: Return default formatted date
        // You can modify this based on your actual Challenge class structure
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // If you add a creationDate field to Challenge class, use:
        // return challenge.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Creates a mini coach card for the challenge detail view
     */
    private VBox createMiniCoachCard(CoachMotivation coach) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 15;" +
                        "-fx-min-width: 140;" +
                        "-fx-max-width: 140;"
        );

        // Avatar
        Label avatarIcon = new Label("👤");
        avatarIcon.setStyle("-fx-font-size: 24px;");

        // Name
        Label nameLabel = new Label(coach.getNomCoach());
        nameLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        // Style
        Label styleLabel = new Label(coach.getStyle());
        styleLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-text-fill: rgba(255,255,255,0.8);"
        );

        card.getChildren().addAll(avatarIcon, nameLabel, styleLabel);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(52,152,219,0.3);" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 12 15;" +
                            "-fx-min-width: 140;" +
                            "-fx-max-width: 140;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.15);" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 12 15;" +
                            "-fx-min-width: 140;" +
                            "-fx-max-width: 140;"
            );
        });

        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> showCoachDetail(coach));

        return card;
    }

    /**
     * Creates a mini reward card for the challenge detail view
     */
    private VBox createMiniRewardCard(Recompense reward) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 15;" +
                        "-fx-min-width: 140;" +
                        "-fx-max-width: 140;"
        );

        // Icon based on reward type
        Label iconLabel = new Label(getIconForRecompenseType(reward.getTypeRecompense()));
        iconLabel.setStyle("-fx-font-size: 24px;");

        // Title
        Label titleLabel = new Label(reward.getTitre());
        titleLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);

        // Type
        Label typeLabel = new Label(reward.getTypeRecompense());
        typeLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-text-fill: rgba(255,255,255,0.8);"
        );

        card.getChildren().addAll(iconLabel, titleLabel, typeLabel);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(241,196,15,0.3);" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 12 15;" +
                            "-fx-min-width: 140;" +
                            "-fx-max-width: 140;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.15);" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 12 15;" +
                            "-fx-min-width: 140;" +
                            "-fx-max-width: 140;"
            );
        });

        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> showRecompenseDetail(reward));

        return card;
    }


    /**
     * Crée une carte d'information moderne avec texte noir sur fond blanc
     */
    private VBox createModernInfoBox(String icon, String title, String value, String accentColor) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(
                "-fx-padding: 15;" +
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #ECF0F1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );
        box.setPrefWidth(200);
        box.setMaxWidth(Double.MAX_VALUE);

        // Header avec icône et titre
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #7F8C8D;" + // Gris foncé
                        "-fx-uppercase: true;"
        );

        header.getChildren().addAll(iconLabel, titleLabel);

        // Valeur
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: " + accentColor + ";" // Couleur d'accent pour la valeur
        );
        valueLabel.setWrapText(true);

        box.getChildren().addAll(header, valueLabel);

        return box;
    }

    /**
     * Retourne une couleur de texte selon la difficulté
     */
    private String getDifficultyTextColor(String difficulty) {
        if (difficulty == null) return "#2C3E50";

        switch (difficulty.toLowerCase()) {
            case "facile":
                return "#27AE60"; // Vert
            case "moyen":
                return "#F39C12"; // Orange
            case "difficile":
                return "#E74C3C"; // Rouge
            case "expert":
                return "#8E44AD"; // Violet
            default:
                return "#2C3E50"; // Gris foncé
        }
    }



    /* ================= CONFIGURATION DU SYSTÈME DE TRI ================= */
    private void setupSortControls() {
        // Créer le conteneur pour les boutons de tri
        HBox sortContainer = new HBox(15);
        sortContainer.setAlignment(Pos.CENTER_LEFT);
        sortContainer.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 25; -fx-padding: 10 20;");

        // Label "TRIER PAR :"
        Label sortLabel = new Label("🔽 TRIER PAR :");
        sortLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #B4C6E7; -fx-letter-spacing: 1px;");

        // Groupe de bascules pour les radios
        sortToggleGroup = new ToggleGroup();

        // Style commun pour les boutons radio
        String radioStyle = "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 5 10;";

        // Bouton radio : Nom (A-Z)
        RadioButton nameAZBtn = createStyledRadio("👤 Nom (A-Z)", "name_asc", radioStyle);

        // Bouton radio : Nom (Z-A)
        RadioButton nameZABtn = createStyledRadio("👤 Nom (Z-A)", "name_desc", radioStyle);

        // Bouton radio : Durée (croissante)
        RadioButton durationAscBtn = createStyledRadio("⏱️ Durée ↑", "duration_asc", radioStyle);

        // Bouton radio : Durée (décroissante)
        RadioButton durationDescBtn = createStyledRadio("⏱️ Durée ↓", "duration_desc", radioStyle);

        // Bouton radio : Date (récent → ancien)
        RadioButton dateDescBtn = createStyledRadio("📅 Plus récent", "date_desc", radioStyle);

        // Bouton radio : Date (ancien → récent)
        RadioButton dateAscBtn = createStyledRadio("📅 Plus ancien", "date_asc", radioStyle);

        // Bouton radio : Statut (actifs en premier)
        RadioButton statusActiveBtn = createStyledRadio("✅ Actifs", "status_active", radioStyle);

        // Bouton radio : Statut (inactifs en premier)
        RadioButton statusInactiveBtn = createStyledRadio("❌ Inactifs", "status_inactive", radioStyle);

        // Définir le tri par défaut (plus récent)
        dateDescBtn.setSelected(true);

        // Ajouter les listeners pour chaque bouton
        nameAZBtn.setOnAction(e -> sortChallenges("name_asc"));
        nameZABtn.setOnAction(e -> sortChallenges("name_desc"));
        durationAscBtn.setOnAction(e -> sortChallenges("duration_asc"));
        durationDescBtn.setOnAction(e -> sortChallenges("duration_desc"));
        dateDescBtn.setOnAction(e -> sortChallenges("date_desc"));
        dateAscBtn.setOnAction(e -> sortChallenges("date_asc"));
        statusActiveBtn.setOnAction(e -> sortChallenges("status_active"));
        statusInactiveBtn.setOnAction(e -> sortChallenges("status_inactive"));

        // Ajouter les boutons au conteneur
        sortContainer.getChildren().addAll(
                sortLabel,
                nameAZBtn, nameZABtn,
                durationAscBtn, durationDescBtn,
                dateDescBtn, dateAscBtn,
                statusActiveBtn, statusInactiveBtn
        );

        // Ajouter le conteneur à l'interface (à côté de la recherche)
        // Vous devez avoir un conteneur dans votre FXML avec fx:id="sortBox"
        // Si vous n'en avez pas, vous pouvez l'ajouter dans le header
        if (sortBox != null) {
            sortBox.getChildren().add(sortContainer);
        } else {
            // Alternative : chercher le conteneur parent et l'ajouter
            HBox headerRight = (HBox) searchField.getParent();
            headerRight.getChildren().add(1, sortContainer); // Ajouter après la recherche
        }
    }


    private RadioButton createStyledRadio(String text, String userData, String style) {
        RadioButton radio = new RadioButton(text);
        radio.setToggleGroup(sortToggleGroup);
        radio.setUserData(userData);
        radio.setStyle(style);

        // Style au survol
        radio.setOnMouseEntered(e ->
                radio.setStyle("-fx-text-fill: #00f2fe; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 5 10;")
        );
        radio.setOnMouseExited(e ->
                radio.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 5 10;")
        );

        return radio;
    }

    private void sortChallenges(String sortType) {
        currentSortType = sortType;

        // Récupérer la liste actuelle (filtrée ou complète)
        ObservableList<Challenge> currentList = challengeListView.getItems();
        List<Challenge> sortedList = new ArrayList<>(currentList);

        // Appliquer le tri selon le type
        switch (sortType) {
            case "name_asc":
                sortedList.sort(Comparator.comparing(Challenge::getTitre, String.CASE_INSENSITIVE_ORDER));
                break;
            case "name_desc":
                sortedList.sort((c1, c2) -> c2.getTitre().compareToIgnoreCase(c1.getTitre()));
                break;
            case "duration_asc":
                sortedList.sort(Comparator.comparingInt(Challenge::getDureeJours));
                break;
            case "duration_desc":
                sortedList.sort((c1, c2) -> Integer.compare(c2.getDureeJours(), c1.getDureeJours()));
                break;
            case "date_desc":
                // Tri par ID (le plus récent = ID le plus grand)
                sortedList.sort((c1, c2) -> Integer.compare(c2.getIdChallenge(), c1.getIdChallenge()));
                break;
            case "date_asc":
                // Tri par ID (le plus ancien = ID le plus petit)
                sortedList.sort(Comparator.comparingInt(Challenge::getIdChallenge));
                break;
            case "status_active":
                // Actifs en premier, puis inactifs
                sortedList.sort((c1, c2) -> {
                    if (c1.isActif() && !c2.isActif()) return -1;
                    if (!c1.isActif() && c2.isActif()) return 1;
                    return 0;
                });
                break;
            case "status_inactive":
                // Inactifs en premier, puis actifs
                sortedList.sort((c1, c2) -> {
                    if (!c1.isActif() && c2.isActif()) return -1;
                    if (c1.isActif() && !c2.isActif()) return 1;
                    return 0;
                });
                break;
        }

        // Mettre à jour la ListView
        challengeListView.setItems(FXCollections.observableArrayList(sortedList));

        // Animation de confirmation
        animateSortChange();
    }

    private void animateSortChange() {
        // Animation de fondu pour la ListView
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(300), challengeListView
        );
        fade.setFromValue(0.5);
        fade.setToValue(1.0);
        fade.play();

        // Petit effet sonore visuel (changement de couleur du header)
        if (sortBox != null && !sortBox.getChildren().isEmpty()) {
            HBox sortContainer = (HBox) sortBox.getChildren().get(0);
            String originalStyle = sortContainer.getStyle();
            sortContainer.setStyle("-fx-background-color: rgba(0,242,254,0.3); -fx-background-radius: 25; -fx-padding: 10 20;");

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(200));
            pause.setOnFinished(e -> sortContainer.setStyle(originalStyle));
            pause.play();
        }
    }


    private void showEmailDialog(CoachMotivation coach, Challenge challenge) {
        try {
            Stage emailStage = new Stage();
            emailStage.setTitle("📧 Envoyer un email à " + coach.getNomCoach());
            emailStage.initModality(Modality.APPLICATION_MODAL);
            emailStage.initOwner(challengeListView.getScene().getWindow());

            VBox root = new VBox(20);
            root.setPadding(new Insets(25));
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #c3cfe2); -fx-background-radius: 15;");

            // En-tête
            Label titleLabel = new Label("📧 ENVOYER UN EMAIL");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #2C3E50;");

            // Informations du destinataire
            VBox recipientBox = new VBox(10);
            recipientBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");

            Label recipientLabel = new Label("Destinataire:");
            recipientLabel.setStyle("-fx-font-weight: bold;");

            HBox coachInfo = new HBox(15);
            coachInfo.setAlignment(Pos.CENTER_LEFT);

            Label coachIcon = new Label("👤");
            coachIcon.setStyle("-fx-font-size: 24px;");

            VBox coachDetails = new VBox(5);
            Label coachName = new Label(coach.getNomCoach());
            coachName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            String email = coach.getEmail();
            Label coachEmail = new Label();
            if (email != null && !email.isEmpty() && !email.equals("null")) {
                coachEmail.setText(email);
                coachEmail.setStyle("-fx-text-fill: #3498DB;");
            } else {
                coachEmail.setText("Email non renseigné");
                coachEmail.setStyle("-fx-text-fill: #E74C3C; -fx-font-style: italic;");
            }

            coachDetails.getChildren().addAll(coachName, coachEmail);
            coachInfo.getChildren().addAll(coachIcon, coachDetails);

            recipientBox.getChildren().addAll(recipientLabel, coachInfo);

            // Champs du formulaire d'email
            VBox formBox = new VBox(15);
            formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");

            // Sujet
            VBox subjectBox = new VBox(5);
            Label subjectLabel = new Label("Sujet:");
            subjectLabel.setStyle("-fx-font-weight: bold;");
            TextField subjectField = new TextField();
            subjectField.setPromptText("Sujet de l'email");
            subjectField.setText("Invitation au challenge: " + challenge.getTitre());
            subjectBox.getChildren().addAll(subjectLabel, subjectField);

            // Message
            VBox messageBox = new VBox(5);
            Label messageLabel = new Label("Message:");
            messageLabel.setStyle("-fx-font-weight: bold;");
            TextArea messageArea = new TextArea();
            messageArea.setPromptText("Votre message ici...");
            messageArea.setPrefRowCount(10);
            messageArea.setText(generateEmailTemplate(coach, challenge));
            messageBox.getChildren().addAll(messageLabel, messageArea);

            formBox.getChildren().addAll(subjectBox, messageBox);

            // Boutons
            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER);

            Button sendButton = new Button("📤 Envoyer");
            sendButton.setStyle(
                    "-fx-background-color: #2ECC71; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 12 30; " +
                            "-fx-background-radius: 25; " +
                            "-fx-font-size: 14px; " +
                            "-fx-cursor: hand;"
            );

            Button cancelButton = new Button("❌ Annuler");
            cancelButton.setStyle(
                    "-fx-background-color: #E74C3C; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 12 30; " +
                            "-fx-background-radius: 25; " +
                            "-fx-font-size: 14px; " +
                            "-fx-cursor: hand;"
            );

            buttonBox.getChildren().addAll(sendButton, cancelButton);

            // Action du bouton Envoyer
            sendButton.setOnAction(e -> {
                String to = coach.getEmail();
                if (to == null || to.isEmpty() || to.equals("null")) {
                    showAlert("Erreur", "L'adresse email du coach n'est pas renseignée.", Alert.AlertType.ERROR);
                    return;
                }

                String subject = subjectField.getText().trim();
                String message = messageArea.getText().trim();

                if (subject.isEmpty() || message.isEmpty()) {
                    showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
                    return;
                }

                // Ici vous appellerez votre service d'envoi d'email
                // Pour l'instant, on simule l'envoi
                boolean sent = sendEmail(to, subject, message);

                if (sent) {
                    showAlert("Succès", "Email envoyé avec succès à " + coach.getNomCoach(), Alert.AlertType.INFORMATION);
                    emailStage.close();
                } else {
                    showAlert("Erreur", "L'envoi de l'email a échoué. Vérifiez votre configuration.", Alert.AlertType.ERROR);
                }
            });

            cancelButton.setOnAction(e -> emailStage.close());

            // Assemblage final
            root.getChildren().addAll(titleLabel, recipientBox, formBox, buttonBox);

            Scene scene = new Scene(root, 500, 600);
            emailStage.setScene(scene);
            emailStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la boîte d'envoi d'email", Alert.AlertType.ERROR);
        }
    }

    // Méthode pour générer un template d'email
    private String generateEmailTemplate(CoachMotivation coach, Challenge challenge) {
        return String.format(
                "Bonjour %s,\n\n" +
                        "Nous sommes ravis de vous inviter à participer au challenge '%s' en tant que coach.\n\n" +
                        "Détails du challenge:\n" +
                        "- Titre: %s\n" +
                        "- Description: %s\n" +
                        "- Durée: %d jours\n" +
                        "- Difficulté: %s\n" +
                        "- Type: %s\n\n" +
                        "Veuillez confirmer votre participation en répondant à cet email.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Challenge Manager Pro",
                coach.getNomCoach(),
                challenge.getTitre(),
                challenge.getTitre(),
                challenge.getDescription(),
                challenge.getDureeJours(),
                challenge.getNiveauDifficulte(),
                challenge.getTypeChallenge()
        );
    }

    // Méthode temporaire pour simuler l'envoi d'email (à remplacer par EmailSender.sendEmail)
    private boolean sendEmail(String to, String subject, String content) {
        return EmailSender.sendEmail(to, subject, content);
    }




    private void openTaskGeneratorChatbot(Challenge challenge) {
        // Récupérer les récompenses associées au challenge
        List<Recompense> recompenses = challengeRecompenseCrud.getRecompensesByChallenge(challenge.getIdChallenge());

        if (recompenses.isEmpty()) {
            System.out.println("⚠️ Attention: Pas de récompenses pour ce challenge.");
        }

        TaskChatbotController chatbot = new TaskChatbotController();
        chatbot.show(challenge, recompenses);
    }



    /* ================= GÉNÉRATION QR CODE ================= */
    private void showQRCodeDialog(Challenge challenge) {
        try {
            // Créer la fenêtre modale
            Stage qrStage = new Stage();
            qrStage.setTitle("📱 QR Code - " + challenge.getTitre());
            qrStage.initModality(Modality.APPLICATION_MODAL);
            qrStage.initOwner(challengeListView.getScene().getWindow());

            // Récupérer les données liées (coaches et récompenses) pour le QR
            List<CoachMotivation> coaches = challengeCoachCrud.getCoachesForChallenge(challenge.getIdChallenge());
            List<Recompense> recompenses = challengeRecompenseCrud.getRecompensesByChallenge(challenge.getIdChallenge());

            // Conteneur principal avec fond dégradé
            VBox root = new VBox(25);
            root.setPadding(new Insets(30));
            root.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-color: rgba(255,255,255,0.3);"
            );
            root.setAlignment(Pos.CENTER);
            root.setMinWidth(450);
            root.setMaxWidth(450);

            // ========== EN-TÊTE AVEC EFFET BRILLANT ==========
            HBox headerBox = new HBox(15);
            headerBox.setAlignment(Pos.CENTER);

            Label iconLabel = new Label("📱");
            iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");

            Label titleLabel = new Label("QR CODE DU CHALLENGE");
            titleLabel.setStyle(
                    "-fx-font-size: 20px;" +
                            "-fx-font-weight: 900;" +
                            "-fx-text-fill: white;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);"
            );

            headerBox.getChildren().addAll(iconLabel, titleLabel);

            // ========== NOM DU CHALLENGE AVEC BADGE ==========
            HBox challengeNameBox = new HBox(10);
            challengeNameBox.setAlignment(Pos.CENTER);
            challengeNameBox.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.2);" +
                            "-fx-background-radius: 30;" +
                            "-fx-padding: 12 25;"
            );

            Label challengeIcon = new Label("🏆");
            challengeIcon.setStyle("-fx-font-size: 18px;");

            Label challengeName = new Label(challenge.getTitre());
            challengeName.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: white;"
            );
            challengeName.setWrapText(true);
            challengeName.setMaxWidth(300);
            challengeName.setAlignment(Pos.CENTER);

            challengeNameBox.getChildren().addAll(challengeIcon, challengeName);

            // ========== GÉNÉRATION DU QR CODE ==========
            // Construire les données du QR Code (Inclut toutes les infos)
            String qrData = buildQRData(challenge, coaches, recompenses);

            // Générer l'image QR Code
            javafx.scene.image.Image qrImage = generateQRCodeImage(qrData, 250, 250);

            // Conteneur pour le QR Code avec effet de brillance
            StackPane qrContainer = new StackPane();
            qrContainer.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0, 0, 8);"
            );

            ImageView qrImageView = new ImageView(qrImage);
            qrImageView.setFitWidth(250);
            qrImageView.setFitHeight(250);
            qrImageView.setPreserveRatio(true);

            // Effet de lueur autour du QR Code
            qrImageView.setEffect(new DropShadow(15, javafx.scene.paint.Color.WHITE));

            qrContainer.getChildren().add(qrImageView);

            // ========== INFORMATIONS SUPPLÉMENTAIRES ==========
            VBox infoBox = new VBox(12);
            infoBox.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.3);" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 20;"
            );
            infoBox.setAlignment(Pos.CENTER_LEFT);

            // Ligne 1: ID et Difficulté
            HBox infoRow1 = new HBox(20);
            infoRow1.setAlignment(Pos.CENTER);

            // Badge ID
            HBox idBadge = new HBox(8);
            idBadge.setAlignment(Pos.CENTER);
            idBadge.setStyle(
                    "-fx-background-color: #3498DB;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 5 15;"
            );
            Label idIcon = new Label("🆔");
            idIcon.setStyle("-fx-text-fill: white;");
            Label idValue = new Label("#" + challenge.getIdChallenge());
            idValue.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            idBadge.getChildren().addAll(idIcon, idValue);

            // Badge Difficulté
            HBox difficultyBadge = new HBox(8);
            difficultyBadge.setAlignment(Pos.CENTER);
            String diffColor = getDifficultyBadgeColor(challenge.getNiveauDifficulte());
            difficultyBadge.setStyle(
                    "-fx-background-color: " + diffColor + ";" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 5 15;"
            );
            Label diffIcon = new Label("🎯");
            diffIcon.setStyle("-fx-text-fill: white;");
            Label diffValue = new Label(challenge.getNiveauDifficulte());
            diffValue.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            difficultyBadge.getChildren().addAll(diffIcon, diffValue);

            infoRow1.getChildren().addAll(idBadge, difficultyBadge);

            // Ligne 2: Durée et Type
            HBox infoRow2 = new HBox(20);
            infoRow2.setAlignment(Pos.CENTER);

            // Badge Durée
            HBox durationBadge = new HBox(8);
            durationBadge.setAlignment(Pos.CENTER);
            durationBadge.setStyle(
                    "-fx-background-color: #F39C12;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 5 15;"
            );
            Label durationIcon = new Label("⏱️");
            durationIcon.setStyle("-fx-text-fill: white;");
            Label durationValue = new Label(challenge.getDureeJours() + " jours");
            durationValue.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            durationBadge.getChildren().addAll(durationIcon, durationValue);

            // Badge Type
            HBox typeBadge = new HBox(8);
            typeBadge.setAlignment(Pos.CENTER);
            typeBadge.setStyle(
                    "-fx-background-color: #E67E22;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 5 15;"
            );
            Label typeIcon = new Label("📂");
            typeIcon.setStyle("-fx-text-fill: white;");
            Label typeValue = new Label(challenge.getTypeChallenge());
            typeValue.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            typeBadge.getChildren().addAll(typeIcon, typeValue);

            infoRow2.getChildren().addAll(durationBadge, typeBadge);

            infoBox.getChildren().addAll(infoRow1, infoRow2);

            // ========== BOUTONS D'ACTION ==========
            HBox actionBox = new HBox(20);
            actionBox.setAlignment(Pos.CENTER);

            // Bouton Télécharger
            Button downloadBtn = new Button("💾 Télécharger QR");
            downloadBtn.setStyle(
                    "-fx-background-color: #2ECC71;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 13px;" +
                            "-fx-padding: 10 20;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;"
            );

            // Bouton Partager
            Button shareBtn = new Button("📤 Partager");
            shareBtn.setStyle(
                    "-fx-background-color: #3498DB;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 13px;" +
                            "-fx-padding: 10 20;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;"
            );

            // Bouton Imprimer
            Button printBtn = new Button("🖨️ Imprimer");
            printBtn.setStyle(
                    "-fx-background-color: #9B59B6;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 13px;" +
                            "-fx-padding: 10 20;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;"
            );

            actionBox.getChildren().addAll(downloadBtn, shareBtn, printBtn);

            // ========== BOUTON FERMER ==========
            Button closeButton = new Button("❌ Fermer");
            closeButton.setStyle(
                    "-fx-background-color: #E74C3C;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 12 40;" +
                            "-fx-background-radius: 30;" +
                            "-fx-cursor: hand;"
            );
            closeButton.setOnAction(e -> qrStage.close());

            // ========== PIED DE PAGE ==========
            Label footerLabel = new Label("✨ Scannez pour accéder rapidement au challenge");
            footerLabel.setStyle(
                    "-fx-font-size: 11px;" +
                            "-fx-text-fill: rgba(255,255,255,0.8);" +
                            "-fx-font-style: italic;"
            );

            // ========== ASSEMBLAGE FINAL ==========
            root.getChildren().addAll(
                    headerBox,
                    challengeNameBox,
                    qrContainer,
                    infoBox,
                    actionBox,
                    closeButton,
                    footerLabel
            );

            // Animation d'apparition
            root.setOpacity(0);
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), root
            );
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            // ScrollPane pour le contenu
            ScrollPane scrollPane = new ScrollPane(root);
            scrollPane.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background: transparent;" +
                            "-fx-border-color: transparent;"
            );
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            Scene scene = new Scene(scrollPane, 500, 700);
            qrStage.setScene(scene);
            qrStage.show();

            // ========== ACTIONS DES BOUTONS ==========
            downloadBtn.setOnAction(e -> downloadQRCode(qrImage, challenge));
            shareBtn.setOnAction(e -> shareQRCode(challenge));
            printBtn.setOnAction(e -> printQRCode(qrImage, challenge));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le QR Code: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private String buildQRData(Challenge challenge, List<CoachMotivation> coaches, List<Recompense> recompenses) {
        StringBuilder sb = new StringBuilder();
        // Format très compact pour accélérer le scan
        sb.append("🏆 ").append(challenge.getTitre().toUpperCase()).append("\n");
        sb.append("ID: #").append(challenge.getIdChallenge()).append("\n");
        
        // Description très courte
        String desc = challenge.getDescription();
        if (desc.length() > 80) desc = desc.substring(0, 77) + "...";
        sb.append("DESC: ").append(desc).append("\n");
        
        sb.append("⏱️ ").append(challenge.getDureeJours()).append("j | 🎯 ").append(challenge.getNiveauDifficulte()).append("\n");
        
        if (coaches != null && !coaches.isEmpty()) {
            sb.append("👨‍🏫 ").append(coaches.get(0).getNomCoach());
            if (coaches.size() > 1) sb.append(" +").append(coaches.size() - 1);
            sb.append("\n");
        }
        
        if (recompenses != null && !recompenses.isEmpty()) {
            sb.append("🎁 ").append(recompenses.get(0).getTitre());
            if (recompenses.size() > 1) sb.append(" +").append(recompenses.size() - 1);
            sb.append("\n");
        }
        
        sb.append("✨ MotivationManager Pro");
        return sb.toString();
    }


    private javafx.scene.image.Image generateQRCodeImage(String data, int width, int height) {
        try {
            // Configuration du QR Code
            Map<com.google.zxing.EncodeHintType, Object> hints = new java.util.HashMap<>();
            hints.put(com.google.zxing.EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);
            hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(com.google.zxing.EncodeHintType.MARGIN, 2);

            // Génération de la matrice du QR Code
            com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(
                    data,
                    com.google.zxing.BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            );

            // Convertir en BufferedImage avec couleurs personnalisées
            java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                    width, height, java.awt.image.BufferedImage.TYPE_INT_RGB
            );

            // Définition des couleurs Pro (Contraste Élevé pour scan facile)
            java.awt.Color darkColor = new java.awt.Color(20, 20, 40); // Presque noir pour un meilleur scan
            java.awt.Color lightColor = new java.awt.Color(60, 20, 80); // Pour le dégradé stylé
            java.awt.Color bgColor = java.awt.Color.WHITE;

            // Colorier le QR Code
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (bitMatrix.get(x, y)) {
                        // Dégradé avec un contraste fort
                        float ratio = (float) (x + y) / (width + height);
                        int red = (int) (darkColor.getRed() * (1 - ratio) + lightColor.getRed() * ratio);
                        int green = (int) (darkColor.getGreen() * (1 - ratio) + lightColor.getGreen() * ratio);
                        int blue = (int) (darkColor.getBlue() * (1 - ratio) + lightColor.getBlue() * ratio);
                        bufferedImage.setRGB(x, y, new java.awt.Color(red, green, blue).getRGB());
                    } else {
                        bufferedImage.setRGB(x, y, bgColor.getRGB());
                    }
                }
            }

            // Ajouter le logo au centre pour le côté "Creative/Pro"
            addLogoToQRCode(bufferedImage);

            // Convertir en JavaFX Image
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(bufferedImage, "png", out);
            byte[] imageBytes = out.toByteArray();

            return new javafx.scene.image.Image(new java.io.ByteArrayInputStream(imageBytes));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur de génération QR Code: " + e.getMessage(), Alert.AlertType.ERROR);
            return null;
        }
    }

    private void addLogoToQRCode(java.awt.image.BufferedImage qrImage) {
        try {
            int width = qrImage.getWidth();
            int height = qrImage.getHeight();
            
            // Taille du logo (environ 1/4 du QR Code pour ErrorCorrection level H)
            int logoSize = width / 5; // Logo un peu plus petit pour faciliter le scan

            java.awt.Graphics2D g2d = qrImage.createGraphics();
            
            // Anti-aliasing pour un rendu pro
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int centerX = (width - logoSize) / 2;
            int centerY = (height - logoSize) / 2;

            // 1. Fond blanc arrondi pour le logo
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRoundRect(centerX - 5, centerY - 5, logoSize + 10, logoSize + 10, 20, 20);
            
            // 2. Bordure dégradée
            java.awt.GradientPaint gp = new java.awt.GradientPaint(
                centerX, centerY, new java.awt.Color(52, 152, 219),
                centerX + logoSize, centerY + logoSize, new java.awt.Color(155, 89, 182)
            );
            g2d.setPaint(gp);
            g2d.setStroke(new java.awt.BasicStroke(3));
            g2d.drawRoundRect(centerX - 5, centerY - 5, logoSize + 10, logoSize + 10, 20, 20);

            // 3. Dessiner l'icône/logo (🏆)
            g2d.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.BOLD, logoSize * 2/3));
            
            // Calculer la position pour centrer l'émoji
            java.awt.FontMetrics metrics = g2d.getFontMetrics();
            int emojiX = centerX + (logoSize - metrics.stringWidth("🏆")) / 2;
            int emojiY = centerY + ((logoSize - metrics.getHeight()) / 2) + metrics.getAscent();
            
            g2d.setColor(java.awt.Color.BLACK);
            g2d.drawString("🏆", emojiX, emojiY);

            g2d.dispose();

        } catch (Exception e) {
            System.err.println("Erreur ajout logo QR: " + e.getMessage());
        }
    }

    private void downloadQRCode(javafx.scene.image.Image qrImage, Challenge challenge) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sauvegarder le QR Code");
            fileChooser.setInitialFileName("qrcode_challenge_" + challenge.getIdChallenge() + ".png");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images PNG", "*.png")
            );

            // Remplacer qrCodeBtn.getScene().getWindow() par une référence à la fenêtre actuelle
            // Option 1: Utiliser la fenêtre du challengeListView
            File file = fileChooser.showSaveDialog(challengeListView.getScene().getWindow());

            if (file != null) {
                // Convertir Image JavaFX en BufferedImage
                javafx.scene.image.PixelReader reader = qrImage.getPixelReader();
                int width = (int) qrImage.getWidth();
                int height = (int) qrImage.getHeight();

                javafx.scene.image.WritableImage writableImage = new javafx.scene.image.WritableImage(
                        reader, width, height
                );

                java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                        width, height, java.awt.image.BufferedImage.TYPE_INT_RGB
                );

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        javafx.scene.paint.Color color = writableImage.getPixelReader().getColor(x, y);
                        int rgb = new java.awt.Color(
                                (float) color.getRed(),
                                (float) color.getGreen(),
                                (float) color.getBlue()
                        ).getRGB();
                        bufferedImage.setRGB(x, y, rgb);
                    }
                }

                javax.imageio.ImageIO.write(bufferedImage, "png", file);

                showAlert("Succès",
                        "✅ QR Code sauvegardé avec succès !\n\n" +
                                "📁 Emplacement: " + file.getAbsolutePath(),
                        Alert.AlertType.INFORMATION
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la sauvegarde: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void shareQRCode(Challenge challenge) {
        try {
            // Créer une boîte de dialogue pour l'email
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Partager le QR Code");
            dialog.setHeaderText("Envoyer le QR Code du challenge par Email");
            dialog.setContentText("Adresse Email du destinataire:");
            dialog.initOwner(challengeListView.getScene().getWindow());

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().isEmpty()) {
                String email = result.get();
                
                // Générer temporairement l'image pour l'envoi
                String qrData = buildQRData(challenge, 
                    challengeCoachCrud.getCoachesForChallenge(challenge.getIdChallenge()), 
                    challengeRecompenseCrud.getRecompensesByChallenge(challenge.getIdChallenge()));
                
                javafx.scene.image.Image qrImage = generateQRCodeImage(qrData, 500, 500); // HDR Resolution for email
                
                // Sauvegarder dans un fichier temporaire
                File tempFile = File.createTempFile("qrcode_challenge_" + challenge.getIdChallenge(), ".png");
                
                // Conversion JavaFX Image -> BufferedImage -> File
                javafx.scene.image.PixelReader reader = qrImage.getPixelReader();
                java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                        500, 500, java.awt.image.BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < 500; x++) {
                    for (int y = 0; y < 500; y++) {
                        javafx.scene.paint.Color c = reader.getColor(x, y);
                        bufferedImage.setRGB(x, y, new java.awt.Color((float)c.getRed(), (float)c.getGreen(), (float)c.getBlue()).getRGB());
                    }
                }
                javax.imageio.ImageIO.write(bufferedImage, "png", tempFile);

                // Envoyer l'email
                boolean success = EmailSender.sendEmailWithAttachment(
                    email, 
                    "🎖️ QR Code Challenge: " + challenge.getTitre(),
                    "Bonjour,\n\nVous trouverez ci-joint le QR Code pour le challenge : " + challenge.getTitre() + 
                    "\n\nCe QR Code contient tous les détails (coaches, récompenses, etc.)." +
                    "\n\nCordialement,\nL'équipe MotivationManager.",
                    tempFile
                );

                if (success) {
                    showAlert("Succès", "✅ QR Code envoyé avec succès à " + email, Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "❌ Échec de l'envoi de l'email. Vérifiez votre connexion.", Alert.AlertType.ERROR);
                }
                
                tempFile.deleteOnExit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors du partage: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void printQRCode(javafx.scene.image.Image qrImage, Challenge challenge) {
        try {
            // Créer une tâche d'impression
            javafx.print.PrinterJob printerJob = javafx.print.PrinterJob.createPrinterJob();

            if (printerJob != null && printerJob.showPrintDialog(challengeListView.getScene().getWindow())){
                // Configurer la page
                javafx.print.PageLayout pageLayout = printerJob.getPrinter().createPageLayout(
                        javafx.print.Paper.A4,
                        javafx.print.PageOrientation.PORTRAIT,
                        javafx.print.Printer.MarginType.DEFAULT
                );

                // Créer un contenu imprimable
                VBox printableContent = new VBox(20);
                printableContent.setAlignment(Pos.CENTER);
                printableContent.setPadding(new Insets(20));

                // Titre
                Label titleLabel = new Label(challenge.getTitre());
                titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                // QR Code
                ImageView qrView = new ImageView(qrImage);
                qrView.setFitWidth(200);
                qrView.setFitHeight(200);
                qrView.setPreserveRatio(true);

                // Informations
                Label infoLabel = new Label(
                        "ID: " + challenge.getIdChallenge() + "\n" +
                                "Difficulté: " + challenge.getNiveauDifficulte()
                );

                printableContent.getChildren().addAll(titleLabel, qrView, infoLabel);

                // Imprimer
                boolean success = printerJob.printPage(printableContent);
                if (success) {
                    printerJob.endJob();
                    showAlert("Succès", "QR Code envoyé à l'impression", Alert.AlertType.INFORMATION);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'impression: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openCollaborativeWorkspace(Challenge challenge) {
        try {
            Stage stage = new Stage();
            stage.setTitle("🤝 Espace Collaboratif Pro - " + challenge.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(challengeListView.getScene().getWindow());

            // --- MAIN LAYOUT (Glassmorphism) ---
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b); -fx-padding: 0;");

            // --- HEADER AVEC BARRE DE PROGRESSION ---
            VBox header = new VBox(15);
            header.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); -fx-padding: 25; -fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 0 0 1 0;");
            
            HBox titleRow = new HBox(15);
            titleRow.setAlignment(Pos.CENTER_LEFT);
            Label title = new Label("ESPACE COLLABORATIF");
            title.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: white; -fx-letter-spacing: 2px;");
            Region spacerTitle = new Region();
            HBox.setHgrow(spacerTitle, Priority.ALWAYS);
            Label subTitle = new Label(challenge.getTitre().toUpperCase());
            subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-background-color: rgba(56,189,248,0.1); -fx-padding: 5 15; -fx-background-radius: 15;");
            titleRow.getChildren().addAll(title, spacerTitle, subTitle);

            // Progress Section
            VBox progressContainer = new VBox(8);
            HBox progressInfo = new HBox();
            Label progressLabel = new Label("Progression de l'équipe");
            progressLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
            Region pSpacer = new Region();
            HBox.setHgrow(pSpacer, Priority.ALWAYS);
            Label percentLabel = new Label("0%");
            percentLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            progressInfo.getChildren().addAll(progressLabel, pSpacer, percentLabel);

            ProgressBar teamProgress = new ProgressBar(0);
            teamProgress.setMaxWidth(Double.MAX_VALUE);
            teamProgress.setPrefHeight(10);
            teamProgress.setStyle("-fx-accent: #10b981;");

            progressContainer.getChildren().addAll(progressInfo, teamProgress);
            header.getChildren().addAll(titleRow, progressContainer);
            root.setTop(header);

            // --- SIDEBAR (LEADERBOARD) ---
            VBox sidebar = new VBox(20);
            sidebar.setPrefWidth(300);
            sidebar.setStyle("-fx-background-color: rgba(15, 23, 42, 0.4); -fx-padding: 25; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 1 0 0;");
            
            Label lbTitle = new Label("🏆 TOP CONTRIBUTEURS");
            lbTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #f59e0b; -fx-letter-spacing: 1px;");
            
            ListView<TeamMember> leaderboard = new ListView<>();
            leaderboard.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-border-width: 0;");
            leaderboard.setCellFactory(lv -> new ListCell<TeamMember>() {
                @Override
                protected void updateItem(TeamMember m, boolean empty) {
                    super.updateItem(m, empty);
                    if (empty || m == null) {
                        setGraphic(null);
                    } else {
                        HBox row = new HBox(12);
                        row.setAlignment(Pos.CENTER_LEFT);
                        row.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 12; -fx-margin-bottom: 5;");
                        
                        StackPane avatar = new StackPane();
                        Circle bg = new Circle(18, Color.web(getIndex() == 0 ? "#f59e0b" : "#334155"));
                        Label initial = new Label(m.getUserName().substring(0, 1).toUpperCase());
                        initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                        avatar.getChildren().addAll(bg, initial);

                        VBox details = new VBox(2);
                        Label name = new Label(m.getUserName());
                        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
                        Label rank = new Label("#" + (getIndex() + 1) + " dans l'équipe");
                        rank.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");
                        details.getChildren().addAll(name, rank);

                        Region mSpacer = new Region();
                        HBox.setHgrow(mSpacer, Priority.ALWAYS);
                        Label pts = new Label(m.getPoints() + " XP");
                        pts.setStyle("-fx-text-fill: #10b981; -fx-font-weight: 900; -fx-font-size: 12px;");
                        
                        row.getChildren().addAll(avatar, details, mSpacer, pts);
                        setGraphic(row);
                    }
                }
            });

            Button joinBtn = new Button("➕ REJOINDRE L'ÉQUIPE");
            joinBtn.setMaxWidth(Double.MAX_VALUE);
            joinBtn.setCursor(Cursor.HAND);
            joinBtn.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 12; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(37,99,235,0.3), 10, 0, 0, 5);");
            
            sidebar.getChildren().addAll(lbTitle, leaderboard, joinBtn);
            root.setLeft(sidebar);

            // --- CENTER (TASKS & CHAT) ---
            VBox center = new VBox(25);
            center.setStyle("-fx-padding: 25;");
            
            Label tasksTitle = new Label("🎯 OBJECTIFS COMMUNS");
            tasksTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #38bdf8; -fx-letter-spacing: 1px;");
            
            ListView<Task> taskListView = new ListView<>();
            taskListView.setPrefHeight(280);
            taskListView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-border-width: 0;");
            taskListView.setCellFactory(lv -> new ListCell<Task>() {
                @Override
                protected void updateItem(Task t, boolean empty) {
                    super.updateItem(t, empty);
                    if (empty || t == null) {
                        setGraphic(null);
                    } else {
                        HBox row = new HBox(15);
                        row.setAlignment(Pos.CENTER_LEFT);
                        row.setStyle("-fx-padding: 15; -fx-background-color: " + (t.isCompleted() ? "rgba(16,185,129,0.08)" : "rgba(255,255,255,0.04)") + 
                                   "; -fx-background-radius: 15; -fx-border-color: " + (t.isCompleted() ? "rgba(16,185,129,0.3)" : "rgba(255,255,255,0.1)") + "; -fx-border-width: 1;");
                        
                        CheckBox cb = new CheckBox();
                        cb.setSelected(t.isCompleted());
                        cb.setScaleX(1.2); cb.setScaleY(1.2);
                        
                        VBox info = new VBox(5);
                        Label lblT = new Label(t.getTitle());
                        lblT.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; " + (t.isCompleted() ? "-fx-strikethrough: true; -fx-opacity: 0.6;" : ""));
                        Label lblD = new Label(t.getDescription());
                        lblD.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
                        info.getChildren().addAll(lblT, lblD);
                        
                        Region tSpacer = new Region();
                        HBox.setHgrow(tSpacer, Priority.ALWAYS);
                        
                        Label badge = new Label(t.isCompleted() ? "COMPLÉTÉ" : "+" + t.getPoints() + " XP");
                        badge.setStyle("-fx-background-color: " + (t.isCompleted() ? "#10b981" : "#f59e0b") + "; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: 900; -fx-padding: 4 10; -fx-background-radius: 20;");
                        
                        row.getChildren().addAll(cb, info, tSpacer, badge);
                        setGraphic(row);

                        cb.setOnAction(e -> {
                            t.setCompleted(cb.isSelected());
                            taskCrud.update(t);
                            // Animation ici possible
                            refreshLeaderboard(leaderboard, challenge);
                            updateProgress(challenge, teamProgress, percentLabel);
                        });
                    }
                }
            });

            // Chat Section
            VBox chatBox = new VBox(15);
            VBox.setVgrow(chatBox, Priority.ALWAYS);
            chatBox.setStyle("-fx-background-color: rgba(30, 41, 59, 0.4); -fx-background-radius: 20; -fx-padding: 20; -fx-border-color: rgba(255,255,255,0.08);");
            
            Label chatTitle = new Label("💬 CANAL DE MOTIVATION");
            chatTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #f472b6; -fx-letter-spacing: 1px;");
            
            ScrollPane chatScroll = new ScrollPane();
            VBox chatContent = new VBox(12);
            chatScroll.setContent(chatContent);
            chatScroll.setFitToWidth(true);
            chatScroll.setPrefHeight(250);
            chatScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0 10 0 0;");
            chatScroll.vvalueProperty().bind(chatContent.heightProperty()); // Auto-scroll
            
            HBox inputArea = new HBox(12);
            inputArea.setAlignment(Pos.CENTER_LEFT);
            TextField chatInput = new TextField();
            chatInput.setPromptText("Écrire un message motivant...");
            HBox.setHgrow(chatInput, Priority.ALWAYS);
            chatInput.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 20; -fx-border-color: rgba(255,255,255,0.1);");
            
            Button sendBtn = new Button("🚀");
            sendBtn.setCursor(Cursor.HAND);
            sendBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #ec4899, #db2777); -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 45; -fx-min-height: 45; -fx-font-size: 18px; -fx-effect: dropshadow(gaussian, rgba(236,72,153,0.3), 10, 0, 0, 5);");
            
            inputArea.getChildren().addAll(chatInput, sendBtn);
            chatBox.getChildren().addAll(chatTitle, chatScroll, inputArea);
            
            center.getChildren().addAll(tasksTitle, taskListView, chatBox);
            root.setCenter(center);

            // --- LOGIC & REAL-TIME UPDATES ---
            String[] userSessionPseudo = {null};
            int currentUserId = Session.getUserId();
            userSessionPseudo[0] = collaborativeCrud.findPseudoByUserId(currentUserId);

            updateProgress(challenge, teamProgress, percentLabel);
            refreshLeaderboard(leaderboard, challenge);
            taskListView.getItems().setAll(taskCrud.getTasksByChallenge(challenge.getIdChallenge()));
            refreshChat(chatContent, challenge, userSessionPseudo[0]);

            // Simulation du temps réel par Polling
            Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(4), event -> {
                refreshLeaderboard(leaderboard, challenge);
                refreshChat(chatContent, challenge, userSessionPseudo[0]);
                updateProgress(challenge, teamProgress, percentLabel);
            }));
            autoRefresh.setCycleCount(Animation.INDEFINITE);
            autoRefresh.play();

            // S'assurer d'arrêter le polling quand on ferme la fenêtre
            stage.setOnCloseRequest(e -> autoRefresh.stop());

            // Masquer le bouton rejoindre si déjà dans l'équipe
            if (collaborativeCrud.isUserInChallenge(currentUserId, challenge.getIdChallenge())) {
                joinBtn.setVisible(false);
                joinBtn.setManaged(false);
            }

            joinBtn.setOnAction(e -> {
                // Si l'utilisateur a déjà un pseudo enregistré mais n'est pas dans CETTE équipe spécifique
                if (userSessionPseudo[0] != null) {
                    joinExistingTeam(challenge, userSessionPseudo[0], currentUserId, leaderboard, chatContent, joinBtn);
                    return;
                }

                TextInputDialog nameDlg = new TextInputDialog();
                nameDlg.setTitle("Rejoindre l'équipe");
                nameDlg.setHeaderText("DEVENIR UN MEMBRE ACTIF");
                nameDlg.setContentText("Choisissez votre pseudo unique :");
                nameDlg.initOwner(stage);
                
                nameDlg.showAndWait().ifPresent(name -> {
                    userSessionPseudo[0] = name;
                    List<Team> teams = collaborativeCrud.getTeamsByChallenge(challenge.getIdChallenge());
                    Team team;
                    if (teams.isEmpty()) {
                        team = new Team(challenge.getIdChallenge(), "Team Alpha");
                        collaborativeCrud.createTeam(team);
                    } else {
                        team = teams.get(0);
                    }
                    collaborativeCrud.addMember(new TeamMember(team.getId(), currentUserId, name));
                    
                    ChatMessage sysMsg = new ChatMessage(challenge.getIdChallenge(), "SYSTEM", "✨ L'utilisateur '" + name + "' vient de rejoindre l'aventure !");
                    collaborativeCrud.saveChatMessage(sysMsg);
                    
                    joinBtn.setVisible(false);
                    joinBtn.setManaged(false);
                    refreshLeaderboard(leaderboard, challenge);
                    refreshChat(chatContent, challenge, userSessionPseudo[0]);
                    showAlert("Bienvenue", "Félicitations " + name + " ! Vous faites maintenant partie de l'équipe.", Alert.AlertType.INFORMATION);
                });
            });

            sendBtn.setOnAction(e -> {
                String msgText = chatInput.getText().trim();
                if (msgText.isEmpty()) return;

                if (userSessionPseudo[0] == null) {
                    TextInputDialog pseudoDlg = new TextInputDialog();
                    pseudoDlg.setTitle("Pseudo Requis");
                    pseudoDlg.setHeaderText("IDENTIFIEZ-VOUS");
                    pseudoDlg.setContentText("Entrez un nom pour chatter :");
                    pseudoDlg.initOwner(stage);
                    pseudoDlg.showAndWait().ifPresent(name -> {
                        userSessionPseudo[0] = name;
                        sendMessage(challenge, name, msgText, chatInput, chatContent, userSessionPseudo[0]);
                    });
                } else {
                    sendMessage(challenge, userSessionPseudo[0], msgText, chatInput, chatContent, userSessionPseudo[0]);
                }
            });

            chatInput.setOnAction(sendBtn.getOnAction());

            Scene scene = new Scene(root, 1100, 750);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            
            // Fade-in effect
            root.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(800), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur Cruciale", "Échec du chargement de l'espace collaboratif: " + e.getLocalizedMessage(), Alert.AlertType.ERROR);
        }
    }

    private void joinExistingTeam(Challenge challenge, String pseudo, int userId, ListView<TeamMember> leaderboard, VBox chatContent, Button joinBtn) {
        List<Team> teams = collaborativeCrud.getTeamsByChallenge(challenge.getIdChallenge());
        if (teams.isEmpty()) {
            Team team = new Team(challenge.getIdChallenge(), "Team Alpha");
            collaborativeCrud.createTeam(team);
            teams.add(team);
        }
        collaborativeCrud.addMember(new TeamMember(teams.get(0).getId(), userId, pseudo));
        
        ChatMessage sysMsg = new ChatMessage(challenge.getIdChallenge(), "SYSTEM", "🔄 " + pseudo + " est de retour !");
        collaborativeCrud.saveChatMessage(sysMsg);

        joinBtn.setVisible(false);
        joinBtn.setManaged(false);
        refreshLeaderboard(leaderboard, challenge);
        refreshChat(chatContent, challenge, pseudo);
        showAlert("Bon retour", "Ravi de vous revoir " + pseudo + " !", Alert.AlertType.INFORMATION);
    }

    private void updateProgress(Challenge challenge, ProgressBar bar, Label label) {
        List<Task> tasks = taskCrud.getTasksByChallenge(challenge.getIdChallenge());
        if (tasks.isEmpty()) {
            bar.setProgress(0);
            label.setText("0%");
            return;
        }
        long completed = tasks.stream().filter(Task::isCompleted).count();
        double progress = (double) completed / tasks.size();
        bar.setProgress(progress);
        label.setText((int) (progress * 100) + "%");
    }

    private void sendMessage(Challenge challenge, String pseudo, String text, TextField input, VBox container, String currentPseudo) {
        ChatMessage msg = new ChatMessage(challenge.getIdChallenge(), pseudo, text);
        collaborativeCrud.saveChatMessage(msg);
        input.clear();
        refreshChat(container, challenge, currentPseudo);
    }

    private void refreshLeaderboard(ListView<TeamMember> lv, Challenge challenge) {
        List<Team> teams = collaborativeCrud.getTeamsByChallenge(challenge.getIdChallenge());
        if (!teams.isEmpty()) {
            lv.getItems().setAll(collaborativeCrud.getMembers(teams.get(0).getId()));
        }
    }

    private void refreshChat(VBox container, Challenge challenge, String currentPseudo) {
        container.getChildren().clear();
        List<ChatMessage> history = collaborativeCrud.getChatHistory(challenge.getIdChallenge());
        for (ChatMessage msg : history) {
            VBox msgBubble = new VBox(2);
            msgBubble.setMaxWidth(300);
            
            boolean isSystem = "SYSTEM".equals(msg.getSenderName());
            boolean isMe = currentPseudo != null && currentPseudo.equals(msg.getSenderName());
            
            Label sender = new Label(isMe ? "Moi (" + msg.getSenderName() + ")" : msg.getSenderName());
            sender.setStyle("-fx-font-size: 9px; -fx-text-fill: " + (isMe ? "#f472b6" : (isSystem ? "#94a3b8" : "#38bdf8")) + "; -fx-font-weight: bold;");
            
            Label content = new Label(msg.getMessage());
            content.setWrapText(true);
            content.setStyle("-fx-text-fill: " + (isSystem ? "#94a3b8" : "white") + "; " +
                            "-fx-background-color: " + (isMe ? "#e879f933" : (isSystem ? "transparent" : "#3b82f633")) + "; " +
                            "-fx-background-radius: 10; -fx-padding: 8 12; -fx-font-size: 13px;");
            
            msgBubble.getChildren().addAll(sender, content);
            if (isMe) msgBubble.setAlignment(Pos.CENTER_RIGHT);
            else if (isSystem) msgBubble.setAlignment(Pos.CENTER);
            else msgBubble.setAlignment(Pos.CENTER_LEFT);
            
            container.getChildren().add(msgBubble);
        }
        // Auto-scroll to bottom (simulate)
    }

    private String getDifficultyBadgeColor(String difficulty) {
        if (difficulty == null) return "#3498DB";

        switch (difficulty.toLowerCase()) {
            case "facile": return "#27AE60";
            case "moyen": return "#F39C12";
            case "difficile": return "#E74C3C";
            case "expert": return "#8E44AD";
            default: return "#3498DB";
        }
    }
}