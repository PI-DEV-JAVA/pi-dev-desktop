package talentos.pidev.controllers.formations;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import talentos.pidev.controllers.MainLayoutController;
import talentos.pidev.controllers.quiz.QuizFormController;
import talentos.pidev.dao.FormationDAO;
import talentos.pidev.dao.QuizDAO;
import talentos.pidev.dao.SeanceDAO;
import talentos.pidev.models.Formation;
import talentos.pidev.models.Quiz;
import talentos.pidev.models.Seance;
import talentos.pidev.services.GeoCodingService;

import java.awt.*;
import java.net.URI;
import java.time.format.DateTimeFormatter;

public class FormationFormController {

    // ================== FORMATION FIELDS ==================
    @FXML private Label titleLabel;

    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextArea contenuArea;

    @FXML private ComboBox<String> categorieCombo;
    @FXML private ComboBox<String> difficulteCombo;
    @FXML private ComboBox<String> statutCombo;

    @FXML private ComboBox<String> modeCombo;
    @FXML private TextField lieuField;

    @FXML private TextField formateurField;
    @FXML private TextArea prerequisArea;

    @FXML private TextField capaciteField;
    @FXML private Label errorLabel;

    // ================== WIZARD/TABS ==================
    @FXML private TabPane tabPane;

    // ================== LISTVIEWS ==================
    @FXML private ListView<Seance> lvSeances;
    @FXML private ListView<Seance> lvQuizSeances;

    // ================== DAO/SERVICES ==================
    private final FormationDAO formationDAO = new FormationDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final QuizDAO quizDAO = new QuizDAO();
    private final GeoCodingService geo = new GeoCodingService();

    private final ObservableList<Seance> seancesData = FXCollections.observableArrayList();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ================== STATE ==================
    private Formation editing = null;
    private Runnable onSaved;
    private Runnable onCancel;

    private MainLayoutController mainLayout;
    private Node selfView;

    @FXML
    public void initialize() {
        categorieCombo.getItems().setAll("Développement", "Réseaux", "RH", "Soft Skills", "Data", "Autre");
        difficulteCombo.getItems().setAll("DEBUTANT", "INTERMEDIAIRE", "AVANCE");
        statutCombo.getItems().setAll("OUVERTE", "EN_COURS", "TERMINEE");
        modeCombo.getItems().setAll("PRESENTIEL", "EN_LIGNE", "HYBRIDE");

        categorieCombo.getSelectionModel().selectFirst();
        difficulteCombo.getSelectionModel().selectFirst();
        statutCombo.getSelectionModel().selectFirst();
        modeCombo.getSelectionModel().selectFirst();

        if (titleLabel != null) titleLabel.setText("Ajouter Formation");
        if (errorLabel != null) errorLabel.setText("");

        setupSeancesList();
        setupQuizList();

        disableAdvancedTabs(true);
    }

    private void disableAdvancedTabs(boolean disable) {
        if (tabPane == null || tabPane.getTabs().size() < 3) return;
        tabPane.getTabs().get(1).setDisable(disable);
        tabPane.getTabs().get(2).setDisable(disable);
    }

    public void setOnSaved(Runnable r) { this.onSaved = r; }
    public void setOnCancel(Runnable r) { this.onCancel = r; }

    public void setMainLayout(MainLayoutController m){ this.mainLayout = m; }
    public void setSelfView(Node v){ this.selfView = v; }

    public void setFormation(Formation f) {
        this.editing = f;

        if (titleLabel != null) titleLabel.setText("Modifier Formation");

        nomField.setText(nz(f.getNom()));
        descriptionArea.setText(nz(f.getDescription()));
        contenuArea.setText(nz(f.getContenu()));

        dateDebutPicker.setValue(f.getDateDebut());
        dateFinPicker.setValue(f.getDateFin());

        categorieCombo.setValue(nzNull(f.getCategorie()));
        difficulteCombo.setValue(nzNull(f.getDifficulte()));
        statutCombo.setValue(nzNull(f.getStatut()));
        modeCombo.setValue(nzNull(f.getMode()));

        lieuField.setText(nz(f.getLieu()));
        formateurField.setText(nz(f.getFormateur()));
        prerequisArea.setText(nz(f.getPrerequis()));
        capaciteField.setText(f.getCapaciteMax() > 0 ? String.valueOf(f.getCapaciteMax()) : "");

        if (f.getId() > 0) {
            disableAdvancedTabs(false);
            reloadSeances();
        }
    }

    @FXML
    private void onCancel() {
        if (onCancel != null) onCancel.run();
    }

    @FXML
    private void onSaveAndGoSeances() {
        if (!saveFormationInternal()) return;

        disableAdvancedTabs(false);
        reloadSeances();

        if (tabPane != null) tabPane.getSelectionModel().select(1);
    }

    private boolean saveFormationInternal() {
        if (errorLabel != null) errorLabel.setText("");

        String nom = nz(nomField.getText());
        if (nom.isEmpty()) { setError("Nom obligatoire."); return false; }

        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            setError("Dates obligatoires."); return false;
        }
        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
            setError("Date fin doit être après date début."); return false;
        }

        int cap = 0;
        try {
            String capTxt = nz(capaciteField.getText());
            if (!capTxt.isEmpty()) cap = Integer.parseInt(capTxt);
            if (cap < 0) { setError("Capacité invalide."); return false; }
        } catch (NumberFormatException e) {
            setError("Capacité max doit être un nombre (ex: 20)."); return false;
        }

        try {
            Formation f = (editing == null) ? new Formation() : editing;

            f.setNom(nom);
            f.setDescription(nz(descriptionArea.getText()));
            f.setDateDebut(dateDebutPicker.getValue());
            f.setDateFin(dateFinPicker.getValue());
            f.setContenu(nz(contenuArea.getText()));

            f.setCategorie(categorieCombo.getValue());
            f.setDifficulte(difficulteCombo.getValue());
            f.setStatut(statutCombo.getValue());

            f.setMode(modeCombo.getValue());
            f.setLieu(nz(lieuField.getText()));
            f.setFormateur(nz(formateurField.getText()));
            f.setPrerequis(nz(prerequisArea.getText()));
            f.setCapaciteMax(cap);

            if (editing == null || f.getId() == 0) {
                int id = formationDAO.addFormation(f);
                f.setId(id);
                if (id <= 0) throw new RuntimeException("ID formation non généré.");
                editing = f;
            } else {
                formationDAO.updateFormation(f);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            setError("Erreur: " + e.getMessage());
            return false;
        }
    }

    @FXML private void onBackToFormationTab() { if (tabPane != null) tabPane.getSelectionModel().select(0); }

    @FXML
    private void onGoQuizTab() {
        if (seancesData.isEmpty()) {
            setError("Ajoute au moins une séance avant de continuer vers Quiz.");
            return;
        }
        if (tabPane != null) tabPane.getSelectionModel().select(2);
    }

    @FXML private void onBackToSeancesTab() { if (tabPane != null) tabPane.getSelectionModel().select(1); }

    @FXML
    private void onFinish() {
        if (onSaved != null) onSaved.run();
    }

    // ================== LISTVIEW SEANCES ==================
    private void setupSeancesList() {
        if (lvSeances == null) return;

        lvSeances.setItems(seancesData);

        lvSeances.setCellFactory(list -> new ListCell<>() {

            private final Label lbTitre = new Label();
            private final Label lbType = new Label();
            private final Label lbDates = new Label();
            private final Label lbLieu = new Label();

            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel = new Button("Supprimer");
            private final Button btnExtra = new Button(); // Map ou Lien

            private final HBox top = new HBox(10);
            private final VBox left = new VBox(4);
            private final HBox actions = new HBox(8);
            private final HBox root = new HBox(14);

            {
                lbTitre.getStyleClass().add("list-title");
                lbDates.getStyleClass().add("muted-text");
                lbLieu.getStyleClass().add("muted-text");

                lbType.getStyleClass().add("status-badge");

                btnEdit.getStyleClass().add("btn-secondary");
                btnDel.getStyleClass().add("btn-secondary");
                btnExtra.getStyleClass().add("btn-secondary");

                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                top.getChildren().addAll(lbTitre, spacer, lbType);

                left.getChildren().addAll(top, lbDates, lbLieu);

                actions.getChildren().addAll(btnEdit, btnDel, btnExtra);

                root.getChildren().addAll(left, actions);
                HBox.setHgrow(left, javafx.scene.layout.Priority.ALWAYS);

                root.getStyleClass().add("list-row");
            }

            @Override
            protected void updateItem(Seance s, boolean empty) {
                super.updateItem(s, empty);

                if (empty || s == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                lbTitre.setText(nz(s.getTitre()));

                String type = nz(s.getType()).toUpperCase();
                lbType.setText(type);

                // badge color classes
                lbType.getStyleClass().removeIf(c -> c.startsWith("status-"));
                if ("PRESENTIEL".equals(type)) lbType.getStyleClass().add("status-open");
                else lbType.getStyleClass().add("status-encours");

                String debut = (s.getDateDebut() == null) ? "-" : s.getDateDebut().format(dtf);
                String fin = (s.getDateFin() == null) ? "-" : s.getDateFin().format(dtf);
                lbDates.setText("🕒 " + debut + "  →  " + fin);

                boolean pres = "PRESENTIEL".equalsIgnoreCase(s.getType());
                if (pres) {
                    lbLieu.setText("📍 " + nz(s.getAdresse()));
                    btnExtra.setText("Map");
                    btnExtra.setDisable(s.getLatitude() == null || s.getLongitude() == null);
                    btnExtra.setOnAction(e -> onOpenMap(s));
                } else {
                    String url = nz(s.getVideoPath());
                    lbLieu.setText("🔗 " + (url.isBlank() ? "Lien non défini" : url));
                    btnExtra.setText("Ouvrir lien");
                    btnExtra.setDisable(url.isBlank());
                    btnExtra.setOnAction(e -> onOpenDrive(s));
                }

                btnEdit.setOnAction(e -> openSeanceForm(s));
                btnDel.setOnAction(e -> onDeleteSeance(s));

                setGraphic(root);
            }
        });
    }

    // ================== LISTVIEW QUIZ ==================
    private void setupQuizList() {
        if (lvQuizSeances == null) return;

        lvQuizSeances.setItems(seancesData);

        lvQuizSeances.setCellFactory(list -> new ListCell<>() {

            private final Label lbSeance = new Label();
            private final Label lbQuiz = new Label();
            private final Label lbDuree = new Label();
            private final Button btnManage = new Button("Gérer Quiz");

            private final VBox left = new VBox(4);
            private final HBox root = new HBox(14);

            {
                lbSeance.getStyleClass().add("list-title");
                lbQuiz.getStyleClass().add("muted-text");
                lbDuree.getStyleClass().add("muted-text");

                btnManage.getStyleClass().add("btn-success");

                left.getChildren().addAll(lbSeance, lbQuiz, lbDuree);

                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                root.getChildren().addAll(left, spacer, btnManage);
                HBox.setHgrow(left, javafx.scene.layout.Priority.ALWAYS);

                root.getStyleClass().add("list-row");
            }

            @Override
            protected void updateItem(Seance s, boolean empty) {
                super.updateItem(s, empty);

                if (empty || s == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                lbSeance.setText("📌 " + nz(s.getTitre()));

                try {
                    Quiz q = quizDAO.findBySeanceId(s.getId());
                    if (q == null) {
                        lbQuiz.setText("Quiz: Non défini");
                        lbDuree.setText("Durée: -");
                        btnManage.setDisable(false); // ouvre pour créer
                    } else {
                        lbQuiz.setText("Quiz: " + nz(q.getTitre()));
                        lbDuree.setText("Durée: " + q.getDureeMinutes() + " min");
                        btnManage.setDisable(false);
                    }
                } catch (Exception e) {
                    lbQuiz.setText("Quiz: Erreur");
                    lbDuree.setText("");
                    btnManage.setDisable(true);
                }

                btnManage.setOnAction(e -> onManageQuiz(s));

                setGraphic(root);
            }
        });
    }

    private void reloadSeances() {
        try {
            if (editing == null || editing.getId() == 0) return;
            seancesData.setAll(seanceDAO.findByFormation(editing.getId()));

            if (lvSeances != null) lvSeances.refresh();
            if (lvQuizSeances != null) lvQuizSeances.refresh();

        } catch (Exception e) {
            setError("Chargement séances: " + e.getMessage());
        }
    }

    @FXML
    private void onAddSeance() {
        if (editing == null || editing.getId() == 0) {
            setError("Enregistre d'abord la formation.");
            return;
        }
        openSeanceForm(null);
    }

    private void openSeanceForm(Seance toEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/SeanceForm.fxml"));
            Node view = loader.load();

            SeanceFormController controller = loader.getController();
            controller.setFormationId(editing.getId());
            controller.setSeanceToEdit(toEdit);

            controller.setOnDone(() -> {
                try {
                    FXMLLoader loaderReload = new FXMLLoader(getClass().getResource("/fxml/formations/FormationForm.fxml"));
                    Node newView = loaderReload.load();

                    FormationFormController ctrl = loaderReload.getController();
                    ctrl.setMainLayout(mainLayout);
                    ctrl.setFormation(editing);

                    mainLayout.setView(newView);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            if (mainLayout != null) mainLayout.setView(view);

        } catch (Exception e) {
            e.printStackTrace();
            setError("Ouverture SeanceForm: " + e.getMessage());
        }
    }

    private void onDeleteSeance(Seance s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la séance '" + s.getTitre() + "' ?", ButtonType.OK, ButtonType.CANCEL);

        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    seanceDAO.delete(s.getId());
                    reloadSeances();
                } catch (Exception e) {
                    setError("Suppression: " + e.getMessage());
                }
            }
        });
    }

    private void onOpenMap(Seance s) {
        try {
            if (s.getLatitude() != null && s.getLongitude() != null) {
                geo.openOSM(s.getLatitude(), s.getLongitude());
            } else {
                setError("Séance non géolocalisée.");
            }
        } catch (Exception e) {
            setError("Map: " + e.getMessage());
        }
    }

    private void onOpenDrive(Seance s) {
        try {
            if (s.getVideoPath() == null || s.getVideoPath().isBlank()) return;
            Desktop.getDesktop().browse(new URI(s.getVideoPath().trim()));
        } catch (Exception e) {
            setError("Ouverture lien: " + e.getMessage());
        }
    }

    private void onManageQuiz(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuizForm.fxml"));
            Node view = loader.load();

            QuizFormController controller = loader.getController();

            Quiz existing = quizDAO.findBySeanceId(seance.getId());
            controller.setQuizToEdit(existing);

            controller.setSeanceId(seance.getId());
            controller.setOnDone(() -> {
                try {
                    FXMLLoader loaderReload = new FXMLLoader(getClass().getResource("/fxml/formations/FormationForm.fxml"));
                    Node newView = loaderReload.load();

                    FormationFormController ctrl = loaderReload.getController();
                    ctrl.setMainLayout(mainLayout);
                    ctrl.setFormation(editing);

                    mainLayout.setView(newView);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            if (mainLayout != null) mainLayout.setView(view);

        } catch (Exception e) {
            e.printStackTrace();
            setError("Ouverture QuizForm: " + e.getMessage());
        }
    }

    // ================== HELPERS ==================
    private void setError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg);
        else new Alert(Alert.AlertType.ERROR, msg).show();
    }

    private String nz(String s) { return s == null ? "" : s.trim(); }
    private String nzNull(String s) { return (s == null || s.isBlank()) ? null : s; }
}