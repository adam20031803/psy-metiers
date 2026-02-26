package org.example.controller.motivation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import org.example.dao.motivation.CoachMotivationCrud;
import org.example.dao.motivation.ChallengeCoachCrud;
import org.example.model.motivation.CoachMotivation;
import org.example.model.motivation.Challenge;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;



// ================= IMPORTS POUR EXPORT PDF =================
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.awt.Color;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.text.Normalizer;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;

import javafx.scene.layout.BorderPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;

public class CoachController implements Initializable {

    // ================= LISTVIEW =================
    @FXML private ListView<CoachMotivation> coachListView;

    // ================= FORMULAIRE =================
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Button refreshBtn;
    @FXML private Button challengeBtn;
    @FXML private Button recBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button homeBtn;
    @FXML private Button exportBtn;

    @FXML private TextField nomField;
    @FXML private ComboBox<String> styleComboBox;
    @FXML private TextArea descField;
    @FXML private CheckBox actifField;
    @FXML private TextField specialitesField;

    @FXML private TextField emailField;

    @FXML private Button addBtn;
    @FXML private Button updateBtn;
    @FXML private Button deleteBtn;
    @FXML private Button clearBtn;

    // ================= STATISTIQUES =================
    @FXML private Label totalLabel;
    @FXML private Label actifsLabel;
    @FXML private Label stylesLabel;
    @FXML private Label popularStyleLabel;
    @FXML private Label engagementLabel;

    @FXML private Label selectedCoachLabel;
    @FXML private Label challengesCountLabel;
    @FXML private Label dateAjoutLabel;
    @FXML private Label successRateLabel;

    private ObservableList<CoachMotivation> coachList = FXCollections.observableArrayList();
    private CoachMotivationCrud coachCrud = new CoachMotivationCrud();
    private final ChallengeCoachCrud challengeCoachCrud = new ChallengeCoachCrud();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INITIALIZATION COACH CONTROLLER START ===");

        setupListView();
        setupComboBox();
        setupButtons();
        setupInputValidation();
        loadData();
        updateStatistics();

        System.out.println("=== INITIALIZATION COACH CONTROLLER END ===");
    }

    private void setupListView() {
        coachListView.setCellFactory(param -> new ListCell<CoachMotivation>() {
            private final HBox card = new HBox(15);
            private final VBox content = new VBox(8);
            private final HBox header = new HBox(10);
            private final HBox infoRow = new HBox(15);
            private final HBox bottomRow = new HBox(10);
            private final VBox rightColumn = new VBox(5);

            private final Label nameLabel = new Label();
            private final Label statusLabel = new Label();
            private final Label styleLabel = new Label();
            private final Label descLabel = new Label();
            private final Label challengesLabel = new Label();

            private final HBox actionBox = new HBox(5);
            private final Button detailsBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");

            {
                // Configuration initiale des composants
                card.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 15; " +
                        "-fx-border-color: rgba(255,255,255,0.1); " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1; " +
                        "-fx-cursor: hand;");
                card.setAlignment(Pos.CENTER_LEFT);
                card.setMaxWidth(Double.MAX_VALUE);

                // Configuration du header
                header.setAlignment(Pos.CENTER_LEFT);

                nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: black;");
                nameLabel.setWrapText(true);
                nameLabel.setMaxWidth(250);


                // Après la ligne où vous récupérez challengeCount


// Ajouter dans rightColumn (à côté de idLabel ou challengeBadge)
                 // ou l'ajouter avec les autres

                statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10;");

                // Style
                styleLabel.setStyle("-fx-text-fill: #9B59B6; -fx-font-size: 12px; -fx-font-weight: bold;");

                // Description
                descLabel.setWrapText(true);
                descLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
                descLabel.setMaxWidth(350);
                descLabel.setMaxHeight(40);

                // Configuration de la ligne d'info
                infoRow.setAlignment(Pos.CENTER_LEFT);
                infoRow.getChildren().addAll(styleLabel);

                // Configuration de la ligne du bas
                bottomRow.setAlignment(Pos.CENTER_LEFT);
                bottomRow.setSpacing(15);

                // Label pour challenges
                challengesLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 11px; -fx-font-weight: bold;");

                // Configuration de la colonne de droite
                rightColumn.setAlignment(Pos.CENTER_RIGHT);
                rightColumn.setMinWidth(100);

                // Configuration des boutons d'action
                actionBox.setAlignment(Pos.CENTER_RIGHT);

                detailsBtn.setStyle("-fx-background-color: linear-gradient(to right, #3498DB, #2980B9); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 8 17; " +
                        "-fx-font-size: 20px; -fx-cursor: hand;");
                detailsBtn.setTooltip(new Tooltip("Voir les détails"));

                editBtn.setStyle("-fx-background-color: linear-gradient(to right, #9B59B6, #8E44AD); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 8 17; " +
                        "-fx-font-size: 20px; -fx-cursor: hand;");
                editBtn.setTooltip(new Tooltip("Modifier"));

                actionBox.getChildren().addAll(detailsBtn, editBtn);

                // Assemblage des composants
                VBox leftContent = new VBox(10);
                leftContent.getChildren().addAll(header, descLabel, infoRow, bottomRow);

                HBox mainContent = new HBox(15);
                mainContent.setAlignment(Pos.CENTER_LEFT);
                mainContent.getChildren().addAll(leftContent, rightColumn);

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
                card.setOnMouseClicked(e -> {
                    CoachMotivation coach = getItem();
                    if (coach != null) {
                        coachListView.getSelectionModel().select(coach);
                        loadCoachData(coach);
                        updateCoachStatistics(coach);
                    }
                });
            }


            @Override
            protected void updateItem(CoachMotivation coach, boolean empty) {
                super.updateItem(coach, empty);

                if (empty || coach == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Mise à jour des données
                    nameLabel.setText("👤 " + coach.getNomCoach());

                    // Statut avec badge coloré
                    if (coach.isActif()) {
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

                    // Style
                    styleLabel.setText("🎭 " + coach.getStyle());

                    // Description tronquée si trop longue
                    String desc = coach.getDescription();
                    if (desc.length() > 100) {
                        desc = desc.substring(0, 97) + "...";
                    }
                    descLabel.setText(desc);

                    // ===== CARTE EMAIL DANS LA PARTIE GAUCHE =====
                    // Créer une carte pour l'email (visible dans leftContent)
                    HBox emailCard = new HBox(8);
                    emailCard.setAlignment(Pos.CENTER_LEFT);
                    emailCard.setStyle("-fx-background-color: rgba(255,255,255,0.9); " +
                            "-fx-background-radius: 12; " +
                            "-fx-padding: 5 12; " +
                            "-fx-border-color: rgba(0,0,0,0.1); " +
                            "-fx-border-radius: 12; " +
                            "-fx-border-width: 1; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
                    emailCard.setMaxWidth(300);

                    Label emailIcon = new Label("📧");
                    emailIcon.setStyle("-fx-font-size: 14px;");

                    String emailText = coach.getEmail();
                    Label emailLabel = new Label();

                    if (emailText != null && !emailText.isEmpty() && !emailText.equals("null")) {
                        emailLabel.setText(emailText);
                        emailLabel.setStyle("-fx-font-size: 13px; " +
                                "-fx-font-weight: 600; " +
                                "-fx-text-fill: #000000;"); // Noir pur
                        emailLabel.setWrapText(true);

                        // Tronquer si trop long
                        if (emailText.length() > 30) {
                            emailLabel.setText(emailText.substring(0, 27) + "...");
                            emailLabel.setTooltip(new Tooltip("Email: " + emailText));
                        }

                        emailCard.getChildren().addAll(emailIcon, emailLabel);
                    } else {
                        // Si pas d'email, afficher un placeholder discret
                        emailLabel.setText("Email non renseigné");
                        emailLabel.setStyle("-fx-font-size: 12px; " +
                                "-fx-font-style: italic; " +
                                "-fx-text-fill: #7F8C8D;"); // Gris
                        emailCard.getChildren().addAll(emailIcon, emailLabel);
                    }

                    // Mise à jour du header
                    header.getChildren().clear();
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    header.getChildren().addAll(nameLabel, spacer, statusLabel);

                    // Challenges associés
                    int challengeCount = challengeCoachCrud.getChallengeCountForCoach(coach.getIdCoach());

                    if (challengeCount == 0) {
                        challengesLabel.setText("🏆 Aucun challenge");
                        challengesLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");
                    } else {
                        challengesLabel.setText("🏆 " + challengeCount + " challenge" + (challengeCount != 1 ? "s" : ""));
                        challengesLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 11px; -fx-font-weight: bold;");
                    }

                    // Mise à jour de la ligne du bas
                    bottomRow.getChildren().clear();
                    bottomRow.getChildren().addAll(challengesLabel);

                    // Configuration de la colonne de droite (statistiques)
                    rightColumn.getChildren().clear();

                    VBox statsBox = new VBox(5);
                    statsBox.setAlignment(Pos.CENTER_RIGHT);

                    Label idLabel = new Label("#" + coach.getIdCoach());
                    idLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.6);");

                    // Badge pour le nombre de challenges
                    HBox challengeBadge = new HBox(5);
                    challengeBadge.setAlignment(Pos.CENTER);
                    Label challengeIcon = new Label("🏆");
                    Label challengeCountLabel = new Label(String.valueOf(challengeCount));
                    challengeCountLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: black;");
                    challengeBadge.setStyle("-fx-background-color: rgba(155,89,182,0.2); -fx-background-radius: 10; -fx-padding: 3 8;");
                    challengeBadge.getChildren().addAll(challengeIcon, challengeCountLabel);

                    statsBox.getChildren().addAll(idLabel, challengeBadge);
                    rightColumn.getChildren().add(statsBox);

                    // ===== Mettre à jour leftContent pour inclure l'emailCard =====
                    VBox leftContent = new VBox(8);
                    leftContent.getChildren().addAll(header, emailCard, descLabel, infoRow, bottomRow);

                    HBox mainContent = new HBox(15);
                    mainContent.setAlignment(Pos.CENTER_LEFT);
                    mainContent.getChildren().addAll(leftContent, rightColumn);

                    content.getChildren().setAll(mainContent, actionBox);

                    // Mise à jour des actions des boutons
                    detailsBtn.setOnAction(e -> showCoachDetail(coach));
                    editBtn.setOnAction(e -> {
                        coachListView.getSelectionModel().select(coach);
                        loadCoachData(coach);
                        updateCoachStatistics(coach);
                    });

                    setGraphic(card);
                }
            }
        });

        // Sélection d'un élément
        coachListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        loadCoachData(newVal);
                        updateCoachStatistics(newVal);
                    }
                }
        );

        // Style de la ListView
        coachListView.setStyle("-fx-background-color: transparent; " +
                "-fx-background-insets: 0; " +
                "-fx-padding: 0;");
    }

    private void setupComboBox() {
        // Initialiser les options du ComboBox de style
        styleComboBox.getItems().addAll(
                "Motivation",
                "Leadership",
                "Productivité",
                "Mindset",
                "Gestion du temps",
                "Développement personnel",
                "Coaching sportif",
                "Coaching professionnel",
                "Autre"
        );

        // Valeur par défaut
        if (!styleComboBox.getItems().isEmpty()) {
            styleComboBox.setValue("Motivation");
        }
    }

    private void setupButtons() {
        addBtn.setOnAction(e -> addCoach());
        updateBtn.setOnAction(e -> updateCoach());
        deleteBtn.setOnAction(e -> deleteCoach());
        clearBtn.setOnAction(e -> clearForm());
        searchBtn.setOnAction(e -> searchCoaches());
        refreshBtn.setOnAction(e -> loadData());

        // Navigation
        challengeBtn.setOnAction(e -> goToChallenges());
        recBtn.setOnAction(e -> goToRecompenses());
        if (dashboardBtn != null) dashboardBtn.setOnAction(e -> goToDashboard());
        if (homeBtn != null) homeBtn.setOnAction(e -> onGoHome());
        exportBtn.setOnAction(e -> exportToPDF());
    }

    // ==================== CONTRÔLE DE SAISIE ====================
    private void setupInputValidation() {
        // Validation pour le nom du coach
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (Character.isDigit(newValue.charAt(0))) {
                    showFieldError(nomField, "Le nom ne doit pas commencer par un chiffre");
                    return;
                }
                if (newValue.matches("\\d+")) {
                    showFieldError(nomField, "Le nom ne doit pas être composé uniquement de chiffres");
                    return;
                }
                if (!newValue.matches(".*[a-zA-Z].*")) {
                    showFieldError(nomField, "Le nom doit contenir au moins une lettre");
                    return;
                }
                if (newValue.length() > 50) {
                    showFieldError(nomField, "Le nom ne doit pas dépasser 50 caractères");
                    return;
                }
                clearFieldError(nomField);
            }
        });



        // Validation pour l'email (à ajouter après la validation du nom)
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    showFieldError(emailField, "Format d'email invalide");
                    return;
                }
                if (newValue.length() > 100) {
                    showFieldError(emailField, "L'email ne doit pas dépasser 100 caractères");
                    return;
                }
                clearFieldError(emailField);
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
                if (!newValue.matches("(?s).*[\\p{L}\\p{M}].*")){
                    showFieldError(descField, "La description doit contenir au moins une lettre");
                    return;
                }
                if (newValue.length() > 300) {
                    showFieldError(descField, "La description ne doit pas dépasser 300 caractères");
                    return;
                }
                clearFieldError(descField);
            }
        });
    }

    private void showFieldError(Control field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        if (field instanceof TextInputControl) {
            ((TextInputControl) field).setTooltip(new Tooltip(message));
        } else if (field instanceof ComboBox) {
            field.setTooltip(new Tooltip(message));
        }
    }

    private void clearFieldError(Control field) {
        field.setStyle("");
        field.setTooltip(null);
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation du nom
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            showFieldError(nomField, "Le nom du coach est obligatoire");
            showAlert("Validation", "Le nom du coach est obligatoire", Alert.AlertType.WARNING);
            nomField.requestFocus();
            isValid = false;
        } else if (Character.isDigit(nom.charAt(0))) {
            showFieldError(nomField, "Le nom ne doit pas commencer par un chiffre");
            showAlert("Validation", "Le nom ne doit pas commencer par un chiffre", Alert.AlertType.WARNING);
            nomField.requestFocus();
            isValid = false;
        } else if (nom.matches("\\d+")) {
            showFieldError(nomField, "Le nom ne doit pas être composé uniquement de chiffres");
            showAlert("Validation", "Le nom ne doit pas être composé uniquement de chiffres", Alert.AlertType.WARNING);
            nomField.requestFocus();
            isValid = false;
        } else if (!nom.matches(".*[a-zA-Z].*")) {
            showFieldError(nomField, "Le nom doit contenir au moins une lettre");
            showAlert("Validation", "Le nom doit contenir au moins une lettre", Alert.AlertType.WARNING);
            nomField.requestFocus();
            isValid = false;
        } else if (nom.length() > 50) {
            showFieldError(nomField, "Le nom ne doit pas dépasser 50 caractères");
            showAlert("Validation", "Le nom ne doit pas dépasser 50 caractères", Alert.AlertType.WARNING);
            nomField.requestFocus();
            isValid = false;
        } else {
            clearFieldError(nomField);
        }




        // Validation de l'email (à ajouter après la validation du nom)
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showFieldError(emailField, "L'email est obligatoire");
            if (isValid) {
                showAlert("Validation", "L'email est obligatoire", Alert.AlertType.WARNING);
                emailField.requestFocus();
            }
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showFieldError(emailField, "Format d'email invalide");
            if (isValid) {
                showAlert("Validation", "Format d'email invalide (ex: nom@domaine.com)", Alert.AlertType.WARNING);
                emailField.requestFocus();
            }
            isValid = false;
        } else if (email.length() > 100) {
            showFieldError(emailField, "L'email ne doit pas dépasser 100 caractères");
            if (isValid) {
                showAlert("Validation", "L'email ne doit pas dépasser 100 caractères", Alert.AlertType.WARNING);
                emailField.requestFocus();
            }
            isValid = false;
        } else {
            clearFieldError(emailField);
        }


        // Validation du style (ComboBox)
        String style = styleComboBox.getValue();
        if (style == null || style.isEmpty()) {
            showFieldError(styleComboBox, "Le style est obligatoire");
            if (isValid) {
                showAlert("Validation", "Le style est obligatoire", Alert.AlertType.WARNING);
                styleComboBox.requestFocus();
            }
            isValid = false;
        } else {
            clearFieldError(styleComboBox);
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
        } else if (!description.matches("(?s).*[\\p{L}\\p{M}].*")) {
            showFieldError(descField, "La description doit contenir au moins une lettre");
            if (isValid) {
                showAlert("Validation", "La description doit contenir au moins une lettre", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else if (description.length() > 300) {
            showFieldError(descField, "La description ne doit pas dépasser 300 caractères");
            if (isValid) {
                showAlert("Validation", "La description ne doit pas dépasser 300 caractères", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else {
            clearFieldError(descField);
        }

        return isValid;
    }

    private void searchCoaches() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        List<CoachMotivation> allCoaches = coachCrud.readAll();
        ObservableList<CoachMotivation> filteredList = FXCollections.observableArrayList();

        for (CoachMotivation coach : allCoaches) {
            if (coach.getNomCoach().toLowerCase().contains(keyword) ||
                    coach.getStyle().toLowerCase().contains(keyword) ||
                    coach.getDescription().toLowerCase().contains(keyword)) {
                filteredList.add(coach);
            }
        }

        coachList.setAll(filteredList);
        updateStatistics();
    }

    private void loadData() {
        try {
            List<CoachMotivation> coaches = coachCrud.readAll();
            coachList.setAll(coaches);
            coachListView.setItems(coachList);

            // Mettre à jour les statistiques
            updateStatistics();
            updateStyleStatistics(coaches);

            System.out.println("Chargement terminé. " + coaches.size() + " coach(s) trouvé(s).");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addCoach() {
        if (!validateForm()) return;

        try {
            // Remplacer la ligne de création du coach
            CoachMotivation coach = new CoachMotivation(
                    nomField.getText().trim(),
                    emailField.getText().trim(),  // NOUVEAU : ajouter l'email
                    styleComboBox.getValue(),
                    descField.getText().trim()
            );

            coach.setActif(actifField.isSelected());

            coachCrud.create(coach);
            showAlert("Succès", "Coach ajouté avec succès!", Alert.AlertType.INFORMATION);
            clearForm();
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateCoach() {
        CoachMotivation selected = coachListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Sélectionnez un coach à modifier", Alert.AlertType.WARNING);
            return;
        }

        if (!validateForm()) return;

        try {
            selected.setNomCoach(nomField.getText().trim());
            selected.setEmail(emailField.getText().trim());
            selected.setStyle(styleComboBox.getValue());
            selected.setDescription(descField.getText().trim());
            selected.setActif(actifField.isSelected());

            coachCrud.update(selected);
            showAlert("Succès", "Coach modifié avec succès!", Alert.AlertType.INFORMATION);
            coachListView.refresh();
            updateStatistics();
            updateStyleStatistics(coachList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteCoach() {
        CoachMotivation selected = coachListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Sélectionnez un coach à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le coach");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer : " + selected.getNomCoach() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    coachCrud.delete(selected.getIdCoach());
                    showAlert("Succès", "Coach désactivé!", Alert.AlertType.INFORMATION);
                    clearForm();
                    loadData();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void loadCoachData(CoachMotivation coach) {
        nomField.setText(coach.getNomCoach());
        emailField.setText(coach.getEmail());
        styleComboBox.setValue(coach.getStyle());
        descField.setText(coach.getDescription());
        actifField.setSelected(coach.isActif());

        // Effacer les erreurs de validation lors du chargement
        clearFieldError(nomField);
        clearFieldError(styleComboBox);
        clearFieldError(descField);
    }

    private void clearForm() {
        nomField.clear();
        emailField.clear();
        styleComboBox.setValue("Motivation");
        descField.clear();
        if (specialitesField != null) {
            specialitesField.clear();
        }
        actifField.setSelected(true);
        coachListView.getSelectionModel().clearSelection();

        // Effacer toutes les erreurs de validation
        clearFieldError(nomField);
        clearFieldError(styleComboBox);
        clearFieldError(descField);

        // Réinitialiser les statistiques du coach sélectionné
        if (selectedCoachLabel != null) selectedCoachLabel.setText("Aucun coach sélectionné");
        if (challengesCountLabel != null) challengesCountLabel.setText("0");
        if (dateAjoutLabel != null) dateAjoutLabel.setText("--/--/----");
        if (successRateLabel != null) successRateLabel.setText("--%");
    }

    private void goToChallenges() {
        Stage stage = (Stage) challengeBtn.getScene().getWindow();
        org.example.controller.SceneUtil.switchTo(stage, "/org/example/ui/motivation/MainView.fxml", "Gestion des Challenges");
    }

    private void goToRecompenses() {
        Stage stage = (Stage) recBtn.getScene().getWindow();
        org.example.controller.SceneUtil.switchTo(stage, "/org/example/ui/motivation/RecompenseView.fxml", "Gestion des Récompenses");
    }

    private void goToDashboard() {
        Stage stage = (Stage) dashboardBtn.getScene().getWindow();
        org.example.controller.SceneUtil.switchTo(stage, "/org/example/ui/motivation/dashboard_statistiques.fxml", "📊 Dashboard Statistiques");
    }

    private void onGoHome() {
        Stage stage = (Stage) homeBtn.getScene().getWindow();
        org.example.controller.SceneUtil.switchTo(stage, "/org/example/ui/motivation/MainView.fxml", "Challenge Manager Pro");
    }

   /* private void exportToPDF() {
        // À implémenter: exporter les coaches en PDF
        showAlert("Export PDF", "Fonction d'export PDF à implémenter", Alert.AlertType.INFORMATION);
    }*/

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateStatistics() {
        if (coachList.isEmpty()) {
            if (totalLabel != null) totalLabel.setText("0");
            if (actifsLabel != null) actifsLabel.setText("0");
            if (stylesLabel != null) stylesLabel.setText("0");
            if (popularStyleLabel != null) popularStyleLabel.setText("Aucun");
            if (engagementLabel != null) engagementLabel.setText("0%");
            return;
        }

        // Calcul des statistiques
        int total = coachList.size();
        long actifs = coachList.stream().filter(CoachMotivation::isActif).count();

        // Mise à jour des labels avec vérification null
        if (totalLabel != null) totalLabel.setText(String.valueOf(total));
        if (actifsLabel != null) actifsLabel.setText(String.valueOf(actifs));

        // Compter les styles uniques
        long uniqueStyles = coachList.stream()
                .map(CoachMotivation::getStyle)
                .distinct()
                .count();

        if (stylesLabel != null) stylesLabel.setText(String.valueOf(uniqueStyles));

        // Calcul du taux d'engagement
        if (engagementLabel != null) {
            long activeCoaches = coachList.stream().filter(CoachMotivation::isActif).count();
            int engagementRate = total > 0 ? (int) ((activeCoaches * 100) / total) : 0;
            engagementLabel.setText(engagementRate + "%");
        }
    }

    private void updateStyleStatistics(List<CoachMotivation> coaches) {
        if (coaches.isEmpty()) {
            if (popularStyleLabel != null) popularStyleLabel.setText("Aucun");
            return;
        }

        // Trouver le style le plus populaire
        // Simplifié pour l'exemple
        if (popularStyleLabel != null) {
            // Ici vous pourriez ajouter une logique pour trouver le style le plus fréquent
            popularStyleLabel.setText("Motivation");
        }
    }

    private void updateCoachStatistics(CoachMotivation coach) {
        if (coach == null) return;

        if (selectedCoachLabel != null) {
            selectedCoachLabel.setText(coach.getNomCoach());
        }

        // Récupérer le nombre de challenges assignés
        if (challengesCountLabel != null) {
            int challengeCount = challengeCoachCrud.getChallengeCountForCoach(coach.getIdCoach());
            challengesCountLabel.setText(String.valueOf(challengeCount));
        }

        // Date d'ajout (simulée - vous pourriez ajouter une date dans votre modèle)
        if (dateAjoutLabel != null) {
            dateAjoutLabel.setText("--/--/----");
        }

        // Taux de succès (simulé)
        if (successRateLabel != null) {
            successRateLabel.setText("85%");
        }
    }

    // Méthode pour afficher les détails du coach
    // Méthode pour afficher les détails du coach - VERSION AMÉLIORÉE
    private void showCoachDetail(CoachMotivation coach) {
        try {
            // Création de la fenêtre modale
            Stage detailStage = new Stage();
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setTitle("Détails du Coach - " + coach.getNomCoach());
            detailStage.setResizable(false);

            // ========== CARTE PRINCIPALE AVEC FOND MODERNE ==========
            VBox mainCard = new VBox(0);
            mainCard.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #c3cfe2); " +
                    "-fx-background-radius: 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0.5, 0, 5);");
            mainCard.setPrefSize(650, 750);
            mainCard.setMaxSize(650, 750);

            // ========== EN-TÊTE AVEC DÉGRADÉ DYNAMIQUE ==========
            VBox headerBox = new VBox();
            String headerColor = coach.isActif() ?
                    "linear-gradient(to right, #667eea, #764ba2)" :
                    "linear-gradient(to right, #868f96, #596164)";
            headerBox.setStyle("-fx-background-color: " + headerColor + "; " +
                    "-fx-background-radius: 20 20 0 0; " +
                    "-fx-padding: 25;");
            headerBox.setAlignment(Pos.CENTER);

            // Avatar avec cercle
            StackPane avatarContainer = new StackPane();
            avatarContainer.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                    "-fx-background-radius: 50; " +
                    "-fx-padding: 15;");

            Label avatarLabel = new Label("👤");
            avatarLabel.setStyle("-fx-font-size: 64px; -fx-text-fill: white;");
            avatarContainer.getChildren().add(avatarLabel);

            // Nom du coach
            Label headerNameLabel = new Label(coach.getNomCoach());
            headerNameLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: white; " +
                    "-fx-font-family: 'Segoe UI';");

            VBox headerContent = new VBox(15);
            headerContent.setAlignment(Pos.CENTER);
            headerContent.getChildren().addAll(avatarContainer, headerNameLabel);
            headerBox.getChildren().add(headerContent);

            // ========== CONTENU PRINCIPAL ==========
            VBox contentBox = new VBox(25);
            contentBox.setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 0 0 20 20; " +
                    "-fx-padding: 30;");
            contentBox.setPrefHeight(600);

            // Badge de statut animé
            HBox statusBadge = new HBox();
            statusBadge.setAlignment(Pos.CENTER);

            Label statusLabel = new Label(coach.isActif() ? "● ACTIF" : "● INACTIF");
            String statusColor = coach.isActif() ? "#2ECC71" : "#E74C3C";
            statusLabel.setStyle("-fx-background-color: " + statusColor + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 10 25; " +
                    "-fx-background-radius: 25; " +
                    "-fx-font-size: 14px; " +
                    "-fx-effect: dropshadow(gaussian, " + statusColor + ", 10, 0, 0, 2);");
            statusBadge.getChildren().add(statusLabel);

            // ========== GRILLE D'INFORMATIONS ==========
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(20);
            infoGrid.setVgap(20);
            infoGrid.setStyle("-fx-padding: 10 0;");

            // Style avec icône
            Label styleIcon = new Label("🎯");
            styleIcon.setStyle("-fx-font-size: 24px;");
            Label styleTitle = new Label("Style de coaching");
            styleTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");
            Label styleValue = new Label(coach.getStyle());
            styleValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8E44AD;");
            VBox styleBox = new VBox(5, styleIcon, styleTitle, styleValue);
            infoGrid.add(styleBox, 0, 0);

            // ID Coach
            Label idIcon = new Label("🆔");
            idIcon.setStyle("-fx-font-size: 24px;");
            Label idTitle = new Label("ID Coach");
            idTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");
            Label idValue = new Label("#" + coach.getIdCoach());
            idValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3498DB;");
            VBox idBox = new VBox(5, idIcon, idTitle, idValue);
            infoGrid.add(idBox, 1, 0);

            // Description (boîte spéciale)
            VBox descBox = new VBox(10);
            descBox.setStyle("-fx-background-color: #F8F9FA; " +
                    "-fx-background-radius: 15; " +
                    "-fx-padding: 20;");

            Label descTitle = new Label("📝 Description");
            descTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495E;");

            TextArea descArea = new TextArea(coach.getDescription());
            descArea.setWrapText(true);
            descArea.setEditable(false);
            descArea.setPrefRowCount(5);
            descArea.setStyle("-fx-background-color: transparent; " +
                    "-fx-border-color: transparent; " +
                    "-fx-font-size: 14px; " +
                    "-fx-text-fill: #2C3E50; " +
                    "-fx-font-family: 'Segoe UI';");
            descBox.getChildren().addAll(descTitle, descArea);

            // ========== STATISTIQUES AVANCÉES ==========
            VBox statsBox = new VBox(20);
            statsBox.setStyle("-fx-background-color: linear-gradient(to right, #667eea15, #764ba215); " +
                    "-fx-background-radius: 15; " +
                    "-fx-padding: 20;");

            Label statsTitle = new Label("📊 STATISTIQUES DÉTAILLÉES");
            statsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: #2C3E50;");

            // Récupérer les statistiques
            int challengeCount = challengeCoachCrud.getChallengeCountForCoach(coach.getIdCoach());

            // Grille de statistiques
            GridPane statsGrid = new GridPane();
            statsGrid.setHgap(15);
            statsGrid.setVgap(15);

            // Challenges
            VBox challengeStat = createStatBox("🏆", "Challenges", String.valueOf(challengeCount), "#F39C12");
            statsGrid.add(challengeStat, 0, 0);

            // Taux de succès
            VBox successStat = createStatBox("📈", "Taux succès", "85%", "#27AE60");
            statsGrid.add(successStat, 1, 0);

            // Date d'ajout
            VBox dateStat = createStatBox("📅", "Depuis", "15/03/2024", "#3498DB");
            statsGrid.add(dateStat, 0, 1);

            // Niveau
            VBox levelStat = createStatBox("⚡", "Niveau", "Expert", "#E74C3C");
            statsGrid.add(levelStat, 1, 1);

            statsBox.getChildren().addAll(statsTitle, statsGrid);

            // ========== BOUTONS D'ACTION ==========
            HBox actionButtons = new HBox(15);
            actionButtons.setAlignment(Pos.CENTER);
            actionButtons.setStyle("-fx-padding: 20 0 0 0;");

            // Bouton Modifier
            Button editBtn = new Button("✏️ Modifier");
            editBtn.setStyle("-fx-background-color: #9B59B6; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 12 30; " +
                    "-fx-background-radius: 25; " +
                    "-fx-font-size: 14px; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #9B59B6, 8, 0, 0, 2);");
            editBtn.setOnAction(e -> {
                detailStage.close();
                coachListView.getSelectionModel().select(coach);
                loadCoachData(coach);
            });

            // Bouton Challenges
            Button viewChallengesBtn = new Button("🏆 Voir Challenges");
            viewChallengesBtn.setStyle("-fx-background-color: #3498DB; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 12 30; " +
                    "-fx-background-radius: 25; " +
                    "-fx-font-size: 14px; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #3498DB, 8, 0, 0, 2);");
            viewChallengesBtn.setOnAction(e -> {
                detailStage.close();
                showCoachChallenges(coach);
            });

            // Bouton Fermer
            Button closeBtn = new Button("❌ Fermer");
            closeBtn.setStyle("-fx-background-color: #E74C3C; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 12 30; " +
                    "-fx-background-radius: 25; " +
                    "-fx-font-size: 14px; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #E74C3C, 8, 0, 0, 2);");
            closeBtn.setOnAction(e -> detailStage.close());

            actionButtons.getChildren().addAll(editBtn, viewChallengesBtn, closeBtn);

            // Assemblage final
            contentBox.getChildren().addAll(
                    statusBadge,
                    infoGrid,
                    descBox,
                    statsBox,
                    actionButtons
            );

            mainCard.getChildren().addAll(headerBox, contentBox);

            // Création de la scène
            Scene scene = new Scene(mainCard);
            detailStage.setScene(scene);
            detailStage.initOwner(coachListView.getScene().getWindow());
            detailStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Méthode utilitaire pour créer une boîte de statistique
    /**
     * Crée une boîte de statistique stylisée pour les challenges
     */
    private VBox createStatBox(String icon, String label, String value, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +  // ← Fond semi-transparent
                "-fx-background-radius: 10; " +
                "-fx-padding: 10 20;");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: " + color + ";");

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.7);");

        box.getChildren().addAll(iconLabel, valueLabel, labelLabel);
        return box;
    }




    /* ================= EXPORT PDF ================= */
    /* ================= EXPORT PDF ================= */
    /* ================= EXPORT PDF ================= */
    private void exportToPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les coaches en PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );

            fileChooser.setInitialFileName("coaches_" +
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

            // Pages suivantes: Details des coaches
            createCoachDetailsPages(document);

            // Sauvegarder le document
            document.save(file);
            document.close();

            showAlert("Succes",
                    "PDF genere avec succes!\n\n" +
                            "Fichier: " + file.getName() + "\n" +
                            "Chemin: " + file.getAbsolutePath() + "\n" +
                            "Nombre de coaches: " + coachList.size(),
                    Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la generation du PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void createCoverPage(PDDocument document) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // Fond colore en haut
            contentStream.setNonStrokingColor(new Color(155, 89, 182));
            contentStream.addRect(0, pageHeight - 200, pageWidth, 200);
            contentStream.fill();

            // Titre principal
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 36);
            contentStream.newLineAtOffset(50, pageHeight - 100);
            contentStream.showText("RAPPORT DES COACHES");
            contentStream.endText();

            // Sous-titre
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 16);
            contentStream.newLineAtOffset(50, pageHeight - 140);
            contentStream.showText("Analyse complete et statistiques");
            contentStream.endText();

            // Date de generation
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.newLineAtOffset(50, pageHeight - 250);
            contentStream.showText("Date de generation: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            contentStream.endText();

            // Statistiques rapides (cartes colorees)
            float cardY = pageHeight - 500;
            float cardHeight = 100;
            float cardWidth = 150;
            float spacing = 20;

            long actifs = coachList.stream().filter(CoachMotivation::isActif).count();
            long inactifs = coachList.size() - actifs;

            // Carte 1: Total
            drawStatCard(contentStream, 50, cardY, cardWidth, cardHeight,
                    new Color(155, 89, 182), "TOTAL",
                    String.valueOf(coachList.size()), "Coaches");

            // Carte 2: Actifs
            drawStatCard(contentStream, 50 + cardWidth + spacing, cardY, cardWidth, cardHeight,
                    new Color(46, 204, 113), "ACTIFS",
                    String.valueOf(actifs), "Coaches");

            // Carte 3: Inactifs
            drawStatCard(contentStream, 50 + 2 * (cardWidth + spacing), cardY, cardWidth, cardHeight,
                    new Color(231, 76, 60), "INACTIFS",
                    String.valueOf(inactifs), "Coaches");

            // Pied de page avec ligne decorative
            contentStream.setStrokingColor(new Color(155, 89, 182));
            contentStream.setLineWidth(3);
            contentStream.moveTo(50, 100);
            contentStream.lineTo(pageWidth - 50, 100);
            contentStream.stroke();

            // Texte du pied de page
            contentStream.beginText();
            contentStream.setNonStrokingColor(new Color(127, 140, 141));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 10);
            contentStream.newLineAtOffset(50, 70);
            contentStream.showText("Genere automatiquement par le systeme de gestion des coaches");
            contentStream.endText();
        }
    }

    private void drawStatCard(PDPageContentStream contentStream, float x, float y,
                              float width, float height, Color color,
                              String label, String value, String unit) throws Exception {
        // Fond de la carte
        contentStream.setNonStrokingColor(color);
        contentStream.addRect(x, y, width, height);
        contentStream.fill();

        // Label
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(x + 10, y + height - 25);
        contentStream.showText(label);
        contentStream.endText();

        // Valeur (grand)
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 32);
        contentStream.newLineAtOffset(x + 10, y + height - 65);
        contentStream.showText(value);
        contentStream.endText();

        // Unite
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.WHITE);
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

            // En-tete de la page
            contentStream.setNonStrokingColor(new Color(155, 89, 182));
            contentStream.addRect(0, pageHeight - 80, pageWidth, 80);
            contentStream.fill();

            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
            contentStream.newLineAtOffset(50, pageHeight - 50);
            contentStream.showText("STATISTIQUES DETAILLEES");
            contentStream.endText();

            float currentY = pageHeight - 120;

            // Section: Repartition par style
            currentY = drawSectionTitle(contentStream, currentY, "Repartition par style");
            currentY -= 20;

            Map<String, Long> styleStats = coachList.stream()
                    .collect(Collectors.groupingBy(CoachMotivation::getStyle, Collectors.counting()));

            for (Map.Entry<String, Long> entry : styleStats.entrySet()) {
                currentY = drawStatLine(contentStream, currentY, entry.getKey(), entry.getValue().toString());
            }

            currentY -= 30;

            // Section: Statut des coaches
            currentY = drawSectionTitle(contentStream, currentY, "Statut des coaches");
            currentY -= 20;

            long actifs = coachList.stream().filter(CoachMotivation::isActif).count();
            long inactifs = coachList.size() - actifs;

            currentY = drawStatLine(contentStream, currentY, "Coaches actifs", String.valueOf(actifs));
            currentY = drawStatLine(contentStream, currentY, "Coaches inactifs", String.valueOf(inactifs));

            // Numero de page
            contentStream.beginText();
            contentStream.setNonStrokingColor(new Color(127, 140, 141));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(pageWidth - 80, 30);
            contentStream.showText("Page 2");
            contentStream.endText();
        }
    }

    private float drawSectionTitle(PDPageContentStream contentStream, float y, String title) throws Exception {
        contentStream.setNonStrokingColor(new Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, y);
        contentStream.showText(title);
        contentStream.endText();

        // Ligne sous le titre
        contentStream.setStrokingColor(new Color(155, 89, 182));
        contentStream.setLineWidth(2);
        contentStream.moveTo(50, y - 5);
        contentStream.lineTo(300, y - 5);
        contentStream.stroke();

        return y - 10;
    }

    private float drawStatLine(PDPageContentStream contentStream, float y, String label, String value) throws Exception {
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        contentStream.newLineAtOffset(70, y);
        contentStream.showText(label);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(155, 89, 182));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(300, y);
        contentStream.showText(value);
        contentStream.endText();

        return y - 25;
    }

    private void createCoachDetailsPages(PDDocument document) throws Exception {
        int pageNumber = 3;
        int coachesPerPage = 2;

        for (int i = 0; i < coachList.size(); i += coachesPerPage) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                // En-tete de la page
                contentStream.setNonStrokingColor(new Color(155, 89, 182));
                contentStream.addRect(0, pageHeight - 60, pageWidth, 60);
                contentStream.fill();

                contentStream.beginText();
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
                contentStream.newLineAtOffset(50, pageHeight - 40);
                contentStream.showText("DETAIL DES COACHES");
                contentStream.endText();

                float currentY = pageHeight - 100;

                for (int j = i; j < Math.min(i + coachesPerPage, coachList.size()); j++) {
                    CoachMotivation coach = coachList.get(j);
                    currentY = drawCoachCard(contentStream, coach, currentY, pageWidth);
                    currentY -= 40;
                }

                // Numero de page
                contentStream.beginText();
                contentStream.setNonStrokingColor(new Color(127, 140, 141));
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(pageWidth - 80, 30);
                contentStream.showText("Page " + pageNumber);
                contentStream.endText();
            }
            pageNumber++;
        }
    }

    private float drawCoachCard(PDPageContentStream contentStream, CoachMotivation coach,
                                float y, float pageWidth) throws Exception {
        float cardHeight = 200;
        float margin = 50;
        float cardWidth = pageWidth - 2 * margin;

        Color cardColor = coach.isActif() ?
                new Color(236, 240, 241) : new Color(250, 235, 235);

        contentStream.setNonStrokingColor(cardColor);
        contentStream.addRect(margin, y - cardHeight, cardWidth, cardHeight);
        contentStream.fill();

        Color borderColor = coach.isActif() ?
                new Color(46, 204, 113) : new Color(231, 76, 60);

        contentStream.setNonStrokingColor(borderColor);
        contentStream.addRect(margin, y - cardHeight, 10, cardHeight);
        contentStream.fill();

        float textX = margin + 20;
        float textY = y - 25;

        // Nom du coach
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(44, 62, 80));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contentStream.newLineAtOffset(textX, textY);
        String nom = coach.getNomCoach();
        if (nom.length() > 40) {
            nom = nom.substring(0, 37) + "...";
        }
        contentStream.showText("Coach: " + removeAccents(nom));
        contentStream.endText();

        textY -= 25;

        // Style
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(155, 89, 182));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(textX, textY);
        contentStream.showText("Style: " + removeAccents(coach.getStyle()));
        contentStream.endText();

        textY -= 25;

        // Description
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.newLineAtOffset(textX, textY);
        String desc = coach.getDescription();
        if (desc.length() > 80) {
            desc = desc.substring(0, 77) + "...";
        }
        contentStream.showText(removeAccents(desc));
        contentStream.endText();

        textY -= 25;

        int challengeCount = challengeCoachCrud.getChallengeCountForCoach(coach.getIdCoach());

        // ID
        drawInfoItem(contentStream, textX, textY, "ID:", String.valueOf(coach.getIdCoach()));

        // Statut
        drawInfoItem(contentStream, textX + 200, textY, "Statut:",
                coach.isActif() ? "ACTIF" : "INACTIF");

        textY -= 20;

        // Challenges
        drawInfoItem(contentStream, textX, textY, "Challenges:",
                String.valueOf(challengeCount));

        // Badge de statut
        float badgeX = margin + cardWidth - 100;
        float badgeY = y - 30;

        contentStream.setNonStrokingColor(borderColor);
        contentStream.addRect(badgeX, badgeY, 80, 25);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        contentStream.newLineAtOffset(badgeX + 10, badgeY + 8);
        contentStream.showText(coach.isActif() ? "ACTIF" : "INACTIF");
        contentStream.endText();

        return y - cardHeight;
    }

    private void drawInfoItem(PDPageContentStream contentStream, float x, float y,
                              String label, String value) throws Exception {
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(127, 140, 141));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(label);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        contentStream.newLineAtOffset(x + 60, y);
        contentStream.showText(removeAccents(value));
        contentStream.endText();
    }

    // Methode utilitaire pour supprimer les accents
    private String removeAccents(String text) {
        if (text == null) return "";

        text = text.replace("\n", " ")
                .replace("\r", " ");

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    // Méthode pour afficher les challenges du coach




    // ================= AFFICHAGE DES CHALLENGES DU COACH =================
    /**
     * Affiche une fenêtre modale avec tous les challenges assignés au coach
     * avec un design professionnel et créatif
     */
    private void showCoachChallenges(CoachMotivation coach) {
        try {
            // Récupérer les challenges du coach
            List<Challenge> challenges = challengeCoachCrud.getChallengesForCoach(coach.getIdCoach());

            if (challenges.isEmpty()) {
                showAlert("Information", "Aucun challenge assigné à " + coach.getNomCoach(), Alert.AlertType.INFORMATION);
                return;
            }

            // Création de la fenêtre modale
            Stage challengesStage = new Stage();
            challengesStage.initModality(Modality.APPLICATION_MODAL);
            challengesStage.setTitle("🏆 Challenges de " + coach.getNomCoach());
            challengesStage.initOwner(coachListView.getScene().getWindow());

            // ========== CONTENEUR PRINCIPAL ==========
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a1a2e, #16213e, #0f3460);");

            // ========== EN-TÊTE AVEC BANDEAU ==========
            VBox headerBox = new VBox();
            headerBox.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
                    "-fx-padding: 25;");

            HBox headerContent = new HBox(20);
            headerContent.setAlignment(Pos.CENTER_LEFT);

            // Icône et titre
            Label iconLabel = new Label("🏆");
            iconLabel.setStyle("-fx-font-size: 48px;");

            VBox titleBox = new VBox(5);
            Label titleLabel = new Label("CHALLENGES ASSIGNÉS");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: white;");

            Label subtitleLabel = new Label("Coach: " + coach.getNomCoach() + " • " + challenges.size() + " challenge(s)");
            subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");

            titleBox.getChildren().addAll(titleLabel, subtitleLabel);
            headerContent.getChildren().addAll(iconLabel, titleBox);

            // Badge de statistique
            HBox statsBadge = new HBox(10);
            statsBadge.setAlignment(Pos.CENTER_RIGHT);
            statsBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 20; -fx-padding: 10 20;");

            Label completedLabel = new Label("✓ " + calculateCompletedChallenges(challenges) + " complétés");
            completedLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-weight: bold;");

            Label pendingLabel = new Label("⏳ " + calculatePendingChallenges(challenges) + " en cours");
            pendingLabel.setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");

            statsBadge.getChildren().addAll(completedLabel, new Label("|"), pendingLabel);

            HBox.setHgrow(titleBox, Priority.ALWAYS);
            headerContent.getChildren().add(statsBadge);
            headerBox.getChildren().add(headerContent);

            // ========== CONTENU PRINCIPAL ==========
            VBox contentBox = new VBox(20);
            contentBox.setStyle("-fx-padding: 25; -fx-background-color: transparent;");
            contentBox.setAlignment(Pos.TOP_CENTER);

            // Liste des challenges avec design de cartes
            ListView<Challenge> challengesList = new ListView<>();
            challengesList.setItems(FXCollections.observableArrayList(challenges));
            challengesList.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");

            // CellFactory personnalisée pour des cartes design
            challengesList.setCellFactory(list -> new ListCell<Challenge>() {
                private final VBox card = new VBox(15);
                private final HBox header = new HBox(15);
                private final HBox content = new HBox(20);
                private final HBox footer = new HBox(10);

                {
                    // Style de la carte
                    card.setStyle("-fx-background-color: rgba(255,255,255,0.1);" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 20;" +
                            "-fx-border-color: rgba(255,255,255,0.15);" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");
                    card.setMaxWidth(Double.MAX_VALUE);
                    card.setPrefHeight(200);

                    // Effet au survol
                    card.setOnMouseEntered(e ->
                            card.setStyle("-fx-background-color: rgba(255,255,255,0.15);" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-padding: 20;" +
                                    "-fx-border-color: #667eea;" +
                                    "-fx-border-radius: 15;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 15, 0, 0, 5);")
                    );

                    card.setOnMouseExited(e ->
                            card.setStyle("-fx-background-color: rgba(255,255,255,0.1);" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-padding: 20;" +
                                    "-fx-border-color: rgba(255,255,255,0.15);" +
                                    "-fx-border-radius: 15;" +
                                    "-fx-border-width: 1;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);")
                    );
                }

                @Override
                protected void updateItem(Challenge challenge, boolean empty) {
                    super.updateItem(challenge, empty);

                    if (empty || challenge == null) {
                        setGraphic(null);
                    } else {
                        // ===== HEADER =====
                        header.getChildren().clear();

                        // Icône du challenge
                        Label iconLabel = new Label(getIconForChallenge(challenge));
                        iconLabel.setStyle("-fx-font-size: 32px;");

                        // Titre et difficulté
                        VBox titleBox = new VBox(5);
                        Label titleLabel = new Label(challenge.getTitre());
                        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

                        Label difficultyLabel = new Label("🎯 " + challenge.getNiveauDifficulte());
                        difficultyLabel.setStyle(getDifficultyStyle(challenge.getNiveauDifficulte()));

                        titleBox.getChildren().addAll(titleLabel, difficultyLabel);

                        // Badge de statut
                        Label statusBadge = new Label(challenge.isActif() ? "● ACTIF" : "○ INACTIF");
                        statusBadge.setStyle(challenge.isActif() ?
                                "-fx-background-color: rgba(46,204,113,0.2); -fx-text-fill: #2ECC71; -fx-font-weight: bold; -fx-padding: 5 15; -fx-background-radius: 20;" :
                                "-fx-background-color: rgba(231,76,60,0.2); -fx-text-fill: #E74C3C; -fx-font-weight: bold; -fx-padding: 5 15; -fx-background-radius: 20;"
                        );

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        header.getChildren().addAll(iconLabel, titleBox, spacer, statusBadge);

                        // ===== CONTENU =====
                        content.getChildren().clear();

                        // Colonne gauche - Description
                        VBox descBox = new VBox(5);
                        descBox.setPrefWidth(300);

                        Label descTitle = new Label("📝 Description");
                        descTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #B4C6E7;");

                        Label descValue = new Label(challenge.getDescription());
                        descValue.setWrapText(true);
                        descValue.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 13px;");

                        descBox.getChildren().addAll(descTitle, descValue);

                        // Colonne droite - Informations
                        VBox infoBox = new VBox(10);
                        infoBox.setAlignment(Pos.CENTER_RIGHT);
                        infoBox.setPrefWidth(200);

                        // Durée
                        HBox durationRow = new HBox(10);
                        durationRow.setAlignment(Pos.CENTER_LEFT);
                        Label durationIcon = new Label("⏱️");
                        Label durationValue = new Label(challenge.getDureeJours() + " jours");
                        durationValue.setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;");
                        durationRow.getChildren().addAll(durationIcon, durationValue);

                        // Type
                        HBox typeRow = new HBox(10);
                        typeRow.setAlignment(Pos.CENTER_LEFT);
                        Label typeIcon = new Label("📂");
                        Label typeValue = new Label(challenge.getTypeChallenge());
                        typeValue.setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;");
                        typeRow.getChildren().addAll(typeIcon, typeValue);

                        infoBox.getChildren().addAll(durationRow, typeRow);

                        content.getChildren().addAll(descBox, infoBox);

                        // ===== FOOTER =====
                        footer.getChildren().clear();
                        footer.setAlignment(Pos.CENTER_RIGHT);

                        // ✅ BOUTON VOIR DÉTAILS CORRIGÉ (AJOUTEZ CE CODE ICI)
                        Button detailsBtn = new Button("👁️ Voir détails");
                        detailsBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold; " +
                                "-fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");

                        // Empêcher le clic de se propager à la cellule
                        detailsBtn.setOnAction(e -> {
                            e.consume(); // ← IMPORTANT : empêche la propagation
                            showChallengeDetail(challenge);
                        });

                        footer.getChildren().add(detailsBtn);

                        // Assemblage de la carte
                        card.getChildren().setAll(header, content, footer);
                        setGraphic(card);
                    }
                }
            });

            // ========== STATISTIQUES EN BAS ==========
            HBox statsFooter = new HBox(30);
            statsFooter.setAlignment(Pos.CENTER);
            statsFooter.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-padding: 15; -fx-background-radius: 10;");

            // Statistiques détaillées
            VBox totalStats = createStatBox("📊", "Total", String.valueOf(challenges.size()), "#667eea");
            VBox completedStats = createStatBox("✅", "Complétés", String.valueOf(calculateCompletedChallenges(challenges)), "#2ECC71");
            VBox pendingStats = createStatBox("⏳", "En cours", String.valueOf(calculatePendingChallenges(challenges)), "#F39C12");
            VBox activeStats = createStatBox("⚡", "Actifs", String.valueOf(countActiveChallenges(challenges)), "#3498DB");

            statsFooter.getChildren().addAll(totalStats, completedStats, pendingStats, activeStats);

            // ========== BOUTON FERMER ==========
            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setStyle("-fx-padding: 20 0 10 0;");

            Button closeBtn = new Button("❌ Fermer");
            closeBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-background-radius: 25; -fx-padding: 12 40; -fx-font-size: 14px; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> challengesStage.close());
            buttonBox.getChildren().add(closeBtn);

            // Assemblage final
            VBox centerContent = new VBox(20);
            centerContent.setStyle("-fx-padding: 20;");
            centerContent.getChildren().addAll(challengesList, statsFooter, buttonBox);

            root.setTop(headerBox);
            root.setCenter(centerContent);

            // ScrollPane pour le contenu
            // Créer un ScrollPane qui contient root
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(root);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Pas de barre horizontale
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Barre verticale si besoin

// Définir une hauteur maximale pour le contenu pour forcer le scroll
            root.setMaxHeight(800);

            Scene scene = new Scene(scrollPane, 900, 700);
            challengesStage.setScene(scene);
            challengesStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les challenges: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

// ================= MÉTHODES UTILITAIRES =================

    /**
     * Calcule le nombre de challenges complétés (simulé)
     */
    private int calculateCompletedChallenges(List<Challenge> challenges) {
        // Pour l'exemple, on considère que 30% des challenges sont complétés
        return (int) Math.round(challenges.size() * 0.3);
    }

    /**
     * Calcule le nombre de challenges en cours
     */
    private int calculatePendingChallenges(List<Challenge> challenges) {
        return challenges.size() - calculateCompletedChallenges(challenges);
    }

    /**
     * Compte le nombre de challenges actifs
     */
    private int countActiveChallenges(List<Challenge> challenges) {
        return (int) challenges.stream().filter(Challenge::isActif).count();
    }

    /**
     * Retourne une icône appropriée selon le type de challenge
     */
    private String getIconForChallenge(Challenge challenge) {
        if (challenge == null) return "🏆";

        switch (challenge.getTypeChallenge().toLowerCase()) {
            case "programmation": return "💻";
            case "design": return "🎨";
            case "marketing": return "📈";
            case "business": return "💼";
            case "personnel": return "🧘";
            case "sport": return "🏃";
            default: return "🏆";
        }
    }

    /**
     * Retourne le style CSS pour le badge de difficulté
     */
    private String getDifficultyStyle(String difficulty) {
        String baseStyle = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 12;";

        switch (difficulty.toLowerCase()) {
            case "facile": return baseStyle + " -fx-background-color: rgba(46,204,113,0.2); -fx-text-fill: #2ECC71;";
            case "moyen": return baseStyle + " -fx-background-color: rgba(241,196,15,0.2); -fx-text-fill: #F1C40F;";
            case "difficile": return baseStyle + " -fx-background-color: rgba(231,76,60,0.2); -fx-text-fill: #E74C3C;";
            case "expert": return baseStyle + " -fx-background-color: rgba(155,89,182,0.2); -fx-text-fill: #9B59B6;";
            default: return baseStyle + " -fx-background-color: rgba(52,152,219,0.2); -fx-text-fill: #3498DB;";
        }
    }

    /**
     * Crée une boîte de statistique stylisée
     */


    /**
     * Affiche les détails d'un challenge (à implémenter selon votre structure)
     */
    // ================= AFFICHAGE DÉTAILLÉ D'UN CHALLENGE =================
    /**
     * Affiche une carte détaillée et professionnelle d'un challenge
     */
    private void showChallengeDetail(Challenge challenge) {
        try {
            // Création de la fenêtre modale
            Stage detailStage = new Stage();
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setTitle("🏆 Détails du Challenge - " + challenge.getTitre());
            detailStage.initOwner(coachListView.getScene().getWindow());
            detailStage.setResizable(false);

            // ========== CARTE PRINCIPALE ==========
            BorderPane root = new BorderPane();

            // Dégradé de fond selon la difficulté
            String bgColor = getGradientForDifficulty(challenge.getNiveauDifficulte());
            root.setStyle("-fx-background-color: " + bgColor + ";");

            // ========== EN-TÊTE AVEC BANDEAU ==========
            VBox headerBox = new VBox(20);
            headerBox.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-padding: 30; -fx-background-radius: 0 0 20 20;");

            HBox headerContent = new HBox(25);
            headerContent.setAlignment(Pos.CENTER_LEFT);

            // Grande icône
            StackPane iconContainer = new StackPane();
            iconContainer.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 30; -fx-padding: 20;");
            Label iconLabel = new Label(getIconForChallenge(challenge));
            iconLabel.setStyle("-fx-font-size: 64px;");
            iconContainer.getChildren().add(iconLabel);

            // Titre et métadonnées
            VBox titleBox = new VBox(10);

            Label titleLabel = new Label(challenge.getTitre());
            titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");
            titleLabel.setWrapText(true);

            HBox metaBox = new HBox(15);
            metaBox.setAlignment(Pos.CENTER_LEFT);

            // Badge de difficulté
            Label difficultyBadge = new Label(challenge.getNiveauDifficulte());
            difficultyBadge.setStyle(getDifficultyBadgeStyle(challenge.getNiveauDifficulte()));

            // Badge de type
            Label typeBadge = new Label(challenge.getTypeChallenge());
            typeBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-padding: 5 15; -fx-background-radius: 20; -fx-font-size: 14px;");

            metaBox.getChildren().addAll(difficultyBadge, typeBadge);

            titleBox.getChildren().addAll(titleLabel, metaBox);

            headerContent.getChildren().addAll(iconContainer, titleBox);

            // Badge de statut à droite
            HBox statusBox = new HBox();
            statusBox.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(titleBox, Priority.ALWAYS);

            Label statusLabel = new Label(challenge.isActif() ? "● ACTIF" : "● INACTIF");
            statusLabel.setStyle(challenge.isActif() ?
                    "-fx-background-color: rgba(46,204,113,0.3); -fx-text-fill: #2ECC71; -fx-font-weight: bold; -fx-padding: 8 25; -fx-background-radius: 25; -fx-font-size: 14px; -fx-border-color: #2ECC71; -fx-border-radius: 25; -fx-border-width: 1.5;" :
                    "-fx-background-color: rgba(231,76,60,0.3); -fx-text-fill: #E74C3C; -fx-font-weight: bold; -fx-padding: 8 25; -fx-background-radius: 25; -fx-font-size: 14px; -fx-border-color: #E74C3C; -fx-border-radius: 25; -fx-border-width: 1.5;"
            );

            headerContent.getChildren().add(statusBox);
            headerBox.getChildren().add(headerContent);

            // ========== CONTENU PRINCIPAL ==========
            VBox contentBox = new VBox(25);
            contentBox.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 25 25 0 0; -fx-padding: 30;");

            // ===== SECTION DESCRIPTION =====
            VBox descSection = new VBox(15);

            Label descTitle = new Label("📝 DESCRIPTION");
            descTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #2C3E50;");

            TextArea descArea = new TextArea(challenge.getDescription());
            descArea.setWrapText(true);
            descArea.setEditable(false);
            descArea.setPrefRowCount(4);
            descArea.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 15; -fx-border-color: #E0E0E0; -fx-border-radius: 15; " +
                    "-fx-font-size: 14px; -fx-padding: 15; -fx-text-fill: #2C3E50;");

            descSection.getChildren().addAll(descTitle, descArea);

            // ===== GRILLE D'INFORMATIONS =====
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(20);
            infoGrid.setVgap(20);
            infoGrid.setStyle("-fx-padding: 20 0;");

            // Configuration des colonnes
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(33);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(33);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPercentWidth(33);
            infoGrid.getColumnConstraints().addAll(col1, col2, col3);

            // Ligne 1
            VBox durationBox = createInfoCard("⏱️", "DURÉE", challenge.getDureeJours() + " jours", "#3498DB");
            infoGrid.add(durationBox, 0, 0);

            VBox difficultyInfoBox = createInfoCard("🎯", "DIFFICULTÉ", challenge.getNiveauDifficulte(), getDifficultyColor(challenge.getNiveauDifficulte()));
            infoGrid.add(difficultyInfoBox, 1, 0);

            VBox typeInfoBox = createInfoCard("📂", "TYPE", challenge.getTypeChallenge(), "#E67E22");
            infoGrid.add(typeInfoBox, 2, 0);

            // Ligne 2
            VBox idBox = createInfoCard("🆔", "ID CHALLENGE", "#" + challenge.getIdChallenge(), "#7F8C8D");
            infoGrid.add(idBox, 0, 1);

            // Date de création simulée
            String creationDate = LocalDateTime.now().minusDays(15).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            VBox creationBox = createInfoCard("📅", "DATE CRÉATION", creationDate, "#16A085");
            infoGrid.add(creationBox, 1, 1);

            // Date d'expiration
            String expirationDate = LocalDateTime.now().plusDays(challenge.getDureeJours()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            VBox expirationBox = createInfoCard("⏰", "DATE EXPIRATION", expirationDate, "#C0392B");
            infoGrid.add(expirationBox, 2, 1);

            // ===== STATISTIQUES =====
            HBox statsBox = new HBox(20);
            statsBox.setAlignment(Pos.CENTER);
            statsBox.setStyle("-fx-background-color: linear-gradient(to right, #f5f7fa, #c3cfe2); -fx-background-radius: 20; -fx-padding: 20;");

            // Progression (simulée)
            VBox progressBox = new VBox(10);
            progressBox.setAlignment(Pos.CENTER);
            Label progressIcon = new Label("📊");
            progressIcon.setStyle("-fx-font-size: 30px;");
            Label progressValue = new Label("75%");
            progressValue.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #27AE60;");
            Label progressLabel = new Label("Progression");
            progressLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");
            progressBox.getChildren().addAll(progressIcon, progressValue, progressLabel);

            // Participants (simulé)
            VBox participantsBox = new VBox(10);
            participantsBox.setAlignment(Pos.CENTER);
            Label participantsIcon = new Label("👥");
            participantsIcon.setStyle("-fx-font-size: 30px;");
            Label participantsValue = new Label("12");
            participantsValue.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #3498DB;");
            Label participantsLabel = new Label("Participants");
            participantsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");
            participantsBox.getChildren().addAll(participantsIcon, participantsValue, participantsLabel);

            // Taux de réussite (simulé)
            VBox successBox = new VBox(10);
            successBox.setAlignment(Pos.CENTER);
            Label successIcon = new Label("🏆");
            successIcon.setStyle("-fx-font-size: 30px;");
            Label successValue = new Label("85%");
            successValue.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #F39C12;");
            Label successLabel = new Label("Taux réussite");
            successLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");
            successBox.getChildren().addAll(successIcon, successValue, successLabel);

            statsBox.getChildren().addAll(progressBox, participantsBox, successBox);

            // ===== BOUTONS D'ACTION =====
            HBox buttonBox = new HBox(20);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setStyle("-fx-padding: 20 0 0 0;");

            // Bouton Modifier (si nécessaire)
            Button editBtn = new Button("✏️ Modifier");
            editBtn.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-padding: 12 30; -fx-background-radius: 25; -fx-font-size: 14px; -fx-cursor: hand;");
            editBtn.setOnAction(e -> {
                // Vous pouvez implémenter la logique de modification ici
                showAlert("Information", "Fonctionnalité de modification à implémenter", Alert.AlertType.INFORMATION);
            });

            // Bouton Fermer
            Button closeBtn = new Button("❌ Fermer");
            closeBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-padding: 12 40; -fx-background-radius: 25; -fx-font-size: 14px; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> detailStage.close());

            buttonBox.getChildren().addAll(editBtn, closeBtn);

            // ===== ASSEMBLAGE FINAL =====
            contentBox.getChildren().addAll(descSection, infoGrid, statsBox, buttonBox);

            root.setTop(headerBox);
            root.setCenter(contentBox);

            // ===== SCÈNE =====
            Scene scene = new Scene(root, 700, 750);
            detailStage.setScene(scene);
            detailStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

// ================= MÉTHODES UTILITAIRES POUR LA CARTE DÉTAIL =================

    /**
     * Crée une carte d'information pour la grille
     */
    private VBox createInfoCard(String icon, String title, String value, String color) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-border-color: #ECF0F1; -fx-border-radius: 15;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7F8C8D; -fx-uppercase: true;");

        header.getChildren().addAll(iconLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        valueLabel.setWrapText(true);

        card.getChildren().addAll(header, valueLabel);
        return card;
    }

    /**
     * Retourne un dégradé selon la difficulté
     */
    private String getGradientForDifficulty(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "facile": return "linear-gradient(to bottom right, #27AE60, #229954, #1E8449)";
            case "moyen": return "linear-gradient(to bottom right, #F39C12, #E67E22, #D35400)";
            case "difficile": return "linear-gradient(to bottom right, #E74C3C, #C0392B, #A93226)";
            case "expert": return "linear-gradient(to bottom right, #8E44AD, #6C3483, #4A235A)";
            default: return "linear-gradient(to bottom right, #3498DB, #2980B9, #1F618D)";
        }
    }

    /**
     * Retourne le style du badge de difficulté
     */
    private String getDifficultyBadgeStyle(String difficulty) {
        String baseStyle = "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 5 15; -fx-background-radius: 20; -fx-font-size: 14px; -fx-border-width: 1.5; -fx-border-radius: 20;";

        switch (difficulty.toLowerCase()) {
            case "facile": return baseStyle + " -fx-border-color: #2ECC71;";
            case "moyen": return baseStyle + " -fx-border-color: #F39C12;";
            case "difficile": return baseStyle + " -fx-border-color: #E74C3C;";
            case "expert": return baseStyle + " -fx-border-color: #9B59B6;";
            default: return baseStyle + " -fx-border-color: #3498DB;";
        }
    }

    /**
     * Retourne la couleur associée à la difficulté
     */
    private String getDifficultyColor(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "facile": return "#27AE60";
            case "moyen": return "#F39C12";
            case "difficile": return "#E74C3C";
            case "expert": return "#8E44AD";
            default: return "#3498DB";
        }
    }
}