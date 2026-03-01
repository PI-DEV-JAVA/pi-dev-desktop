package talentos.pidev.controllers.formations;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import talentos.pidev.controllers.MainLayoutController;
import talentos.pidev.controllers.quiz.QuizPassageController;
import talentos.pidev.dao.*;
import talentos.pidev.models.Formation;
import talentos.pidev.models.Seance;
import talentos.pidev.services.GeoCodingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FormationCandidatDetailsController {

    @FXML private Label lbTitle;
    @FXML private Label lbInfo1;
    @FXML private Label lbInfo2;
    @FXML private Label lbInfo3;

    // ✅ LISTVIEW
    @FXML private ListView<Seance> lvSeances;

    private final FormationDAO formationDAO = new FormationDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final QuizDAO quizDAO = new QuizDAO();
    private final TentativeSeanceDAO tentativeSeanceDAO = new TentativeSeanceDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    private final GeoCodingService geo = new GeoCodingService();

    private final ObservableList<Seance> data = FXCollections.observableArrayList();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private MainLayoutController mainLayout;

    private int formationId;
    private String formationNom;
    private String candidatEmail;

    public void setMainLayout(MainLayoutController mainLayout) {
        this.mainLayout = mainLayout;
    }

    public void init(int formationId, String formationNom, String candidatEmail) {
        this.formationId = formationId;
        this.formationNom = formationNom;
        this.candidatEmail = candidatEmail;

        if (lbTitle != null) lbTitle.setText("Ma formation : " + formationNom);

        setupList();
        loadFormationInfos();
        reloadSeances();
    }

    // ====================== LISTVIEW UI ======================
    private void setupList() {
        if (lvSeances == null) return;

        lvSeances.setItems(data);

        lvSeances.setCellFactory(list -> new ListCell<>() {

            private final Label lbTitre = new Label();
            private final Label lbType = new Label();
            private final Label lbDates = new Label();
            private final Label lbLieuLien = new Label();
            private final Label lbQuiz = new Label();

            private final Button btnOpen = new Button("Ouvrir");
            private final Button btnQuiz = new Button("Passer Quiz");

            private final HBox header = new HBox(10);
            private final VBox left = new VBox(6);
            private final VBox right = new VBox(8);
            private final HBox root = new HBox(14);

            {
                lbTitre.getStyleClass().add("list-title");
                lbDates.getStyleClass().add("muted-text");
                lbLieuLien.getStyleClass().add("muted-text");
                lbQuiz.getStyleClass().add("muted-text");

                lbType.getStyleClass().add("status-badge");

                btnOpen.getStyleClass().add("btn-secondary");
                btnQuiz.getStyleClass().add("btn-success");

                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                header.getChildren().addAll(lbTitre, spacer, lbType);

                left.getChildren().addAll(header, lbDates, lbLieuLien, lbQuiz);

                right.getChildren().addAll(btnOpen, btnQuiz);

                root.getChildren().addAll(left, right);
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

                // -------- Title / Type badge --------
                lbTitre.setText(nz(s.getTitre()));

                String type = nz(s.getType()).toUpperCase();
                lbType.setText(type);

                lbType.getStyleClass().removeIf(c -> c.startsWith("status-"));
                if ("PRESENTIEL".equals(type)) lbType.getStyleClass().add("status-open");
                else lbType.getStyleClass().add("status-encours");

                // -------- Dates --------
                String debut = (s.getDateDebut() == null) ? "-" : s.getDateDebut().format(dtf);
                String fin = (s.getDateFin() == null) ? "-" : s.getDateFin().format(dtf);
                lbDates.setText("🕒 " + debut + "  →  " + fin);

                boolean pres = "PRESENTIEL".equalsIgnoreCase(s.getType());

                // -------- Lieu / Lien --------
                if (pres) {
                    lbLieuLien.setText("📍 " + nz(s.getAdresse()));
                } else {
                    String url = nz(s.getVideoPath());
                    lbLieuLien.setText("🔗 " + (url.isBlank() ? "Lien non défini" : url));
                }

                // -------- Quiz / Note --------
                Integer quizId = null;
                boolean hasQuiz = false;
                try {
                    quizId = quizDAO.findQuizIdBySeance(s.getId());
                    hasQuiz = (quizId != null);
                } catch (Exception ignored) {}

                TentativeSeanceDAO.TentativeSeanceInfo t = safeFindTentative(candidatEmail, s.getId());
                boolean alreadyPassed = (t != null);
                boolean finished = isSeanceFinished(s);

                if (!hasQuiz) {
                    lbQuiz.setText("Aucun quiz");
                } else if (t == null) {
                    lbQuiz.setText("Quiz: À passer");
                } else {
                    lbQuiz.setText("Quiz: Note " + t.score + " / " + t.total);
                }

                // -------- Buttons actions --------
                btnOpen.setOnAction(e -> {
                    if (pres) openMap(s);
                    else openLink(s);
                });

                btnQuiz.setOnAction(e -> passQuiz(s));

                // -------- Access rules --------
                boolean canPass = hasQuiz && finished && !alreadyPassed;
                btnQuiz.setDisable(!canPass);

                if (!hasQuiz) btnQuiz.setText("Aucun quiz");
                else if (alreadyPassed) btnQuiz.setText("Déjà passé");
                else if (!finished) btnQuiz.setText("Après séance");
                else btnQuiz.setText("Passer Quiz");

                Tooltip tt = null;
                if (hasQuiz && !finished) tt = new Tooltip("Le quiz sera disponible après la fin de la séance.");
                else if (hasQuiz && alreadyPassed) tt = new Tooltip("Vous avez déjà passé ce quiz.");
                btnQuiz.setTooltip(tt);

                setGraphic(root);
            }
        });
    }

    // ====================== INFOS ======================
    private void loadFormationInfos() {
        try {
            Formation f = formationDAO.getById(formationId);
            if (f != null) {
                lbInfo1.setText("Formateur: " + nz(f.getFormateur()) + " | Catégorie: " + nz(f.getCategorie()) + " | Difficulté: " + nz(f.getDifficulte()));
                lbInfo2.setText("Dates: " + (f.getDateDebut() == null ? "-" : f.getDateDebut()) + " → " + (f.getDateFin() == null ? "-" : f.getDateFin()));
                lbInfo3.setText("Mode: " + nz(f.getMode()) + (f.getLieu() != null && !f.getLieu().isBlank() ? (" | Lieu: " + f.getLieu()) : ""));
            } else {
                lbInfo1.setText("Formation: " + formationNom);
                lbInfo2.setText("Candidat: " + candidatEmail);
                lbInfo3.setText("");
            }
        } catch (Exception e) {
            lbInfo1.setText("Formation: " + formationNom);
            lbInfo2.setText("Candidat: " + candidatEmail);
            lbInfo3.setText("");
        }
    }

    private void reloadSeances() {
        try {
            data.setAll(seanceDAO.findByFormation(formationId));
            if (lvSeances != null) lvSeances.refresh();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Chargement séances: " + e.getMessage()).show();
        }
    }

    // ====================== OPEN MAP/LINK ======================
    private void openMap(Seance s) {
        try {
            if (s.getLatitude() != null && s.getLongitude() != null) {
                geo.openOSM(s.getLatitude(), s.getLongitude());
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Emplacement non défini pour cette séance.").show();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Map: " + e.getMessage()).show();
        }
    }

    private void openLink(Seance s) {
        try {
            String url = s.getVideoPath();
            if (url == null || url.isBlank()) {
                new Alert(Alert.AlertType.INFORMATION, "Aucun lien pour cette séance.").show();
                return;
            }
            geo.openUrl(url);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lien: " + e.getMessage()).show();
        }
    }

    // ====================== QUIZ ======================
    private void passQuiz(Seance s) {
        try {
            if (s == null) return;

            Integer quizId = quizDAO.findQuizIdBySeance(s.getId());
            if (quizId == null) {
                new Alert(Alert.AlertType.INFORMATION, "Aucun quiz lié à cette séance.").show();
                return;
            }

            // ✅ 1) Accessible فقط بعد نهاية séance
            if (!isSeanceFinished(s)) {
                String fin = (s.getDateFin() == null) ? "-" : s.getDateFin().format(dtf);
                new Alert(Alert.AlertType.INFORMATION,
                        "Le quiz sera disponible après la fin de la séance.\nFin prévue : " + fin
                ).show();
                return;
            }

            // ✅ 2) No retake
            if (hasAlreadyPassed(candidatEmail, s.getId())) {
                new Alert(Alert.AlertType.INFORMATION,
                        "Vous avez déjà passé ce quiz. Vous ne pouvez pas le repasser."
                ).show();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuizPassage.fxml"));
            Node view = loader.load();

            QuizPassageController c = loader.getController();

            c.setOnFinished((score, total) -> {
                try {
                    // ✅ double sécurité
                    if (hasAlreadyPassed(candidatEmail, s.getId())) {
                        new Alert(Alert.AlertType.INFORMATION,
                                "Quiz déjà enregistré. Impossible de sauvegarder une 2ème tentative."
                        ).show();
                    } else {
                        tentativeSeanceDAO.upsertScore(candidatEmail, s.getId(), quizId, score, total);

                        double note = (total == 0) ? 0 : (score * 1.0 / total) * 20.0;
                        inscriptionDAO.updateScoreQuiz(formationId, candidatEmail, note);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Erreur sauvegarde tentative: " + ex.getMessage()).show();
                }

                reloadSeances();
                if (mainLayout != null) mainLayout.setView(getSelfView());
            });

            c.setOnExit(() -> {
                if (mainLayout != null) mainLayout.setView(getSelfView());
                reloadSeances();
            });

            c.start(quizId, "Quiz séance: " + nz(s.getTitre()), candidatEmail);

            if (mainLayout != null) mainLayout.setView(view);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ouverture quiz: " + e.getMessage()).show();
        }
    }

    private Node getSelfView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/FormationCandidatDetails.fxml"));
            Node view = loader.load();
            FormationCandidatDetailsController c = loader.getController();
            c.setMainLayout(mainLayout);
            c.init(formationId, formationNom, candidatEmail);
            return view;
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void onBack() {
        if (mainLayout != null) {
            mainLayout.setContent("/fxml/formations/FormationsCandidat.fxml");
        }
    }

    // ====================== HELPERS ======================
    private String nz(String s) { return s == null ? "" : s; }

    private boolean isSeanceFinished(Seance s) {
        if (s == null || s.getDateFin() == null) return false;
        return !LocalDateTime.now().isBefore(s.getDateFin());
    }

    private TentativeSeanceDAO.TentativeSeanceInfo safeFindTentative(String email, int seanceId) {
        try {
            return tentativeSeanceDAO.findByEmailAndSeance(email, seanceId);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean hasAlreadyPassed(String email, int seanceId) {
        return safeFindTentative(email, seanceId) != null;
    }
}