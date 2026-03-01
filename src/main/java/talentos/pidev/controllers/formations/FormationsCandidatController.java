    package talentos.pidev.controllers.formations;

    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Node;
    import javafx.scene.control.ComboBox;
    import javafx.scene.control.TextField;
    import javafx.scene.layout.FlowPane;
    import talentos.pidev.controllers.MainLayoutController;
    import talentos.pidev.dao.FormationDAO;
    import talentos.pidev.models.Formation;

    import java.sql.SQLException;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.List;

    public class FormationsCandidatController {

        @FXML private FlowPane cardsContainer;
        @FXML private TextField searchField;
        @FXML private ComboBox<String> triCombo;

        private final FormationDAO formationDAO = new FormationDAO();
        private List<Formation> all = new ArrayList<>();

        // ✅ NEW
        private MainLayoutController mainLayout;
        private boolean uiReady = false;

        public void setMainLayout(MainLayoutController mainLayout) {
            this.mainLayout = mainLayout;

            // re-render after injection (cards need mainLayout)
            if (uiReady) applySearchAndSort();
        }

        @FXML
        public void initialize() {
            triCombo.getItems().setAll(
                    "Date début (asc)",
                    "Date début (desc)",
                    "Catégorie",
                    "Difficulté"
            );
            triCombo.getSelectionModel().selectFirst();

            // optional: live search + sort
            if (searchField != null) {
                searchField.textProperty().addListener((obs, o, n) -> applySearchAndSort());
            }
            if (triCombo != null) {
                triCombo.valueProperty().addListener((obs, o, n) -> applySearchAndSort());
            }

            uiReady = true;
            refresh();
        }

        @FXML
        private void onRefresh() {
            refresh();
        }

        public void refresh() {
            try {
                all = formationDAO.getAllFormations();

                // candidat: فقط ouvertes
                all = all.stream()
                        .filter(f -> f.getStatut() != null && "OUVERTE".equalsIgnoreCase(f.getStatut().trim()))
                        .toList();

                applySearchAndSort();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void renderCards(List<Formation> formations) {
            cardsContainer.getChildren().clear();

            for (Formation f : formations) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/FormationCard.fxml"));
                    Node card = loader.load();

                    FormationCardController controller = loader.getController();
                    controller.setData(f);
                    controller.setRHMode(false); // candidat

                    // ✅ inject main layout so "S'inscrire" opens inside contentPane
                    controller.setMainLayout(mainLayout);

                    // ✅ allow card to refresh after inscription
                    controller.setOnChanged(this::refresh);

                    // ✅ tell card how to open inscription form (no Stage)
                    controller.setOnInscrireRequested(() -> openInscriptionForm(f));

                    cardsContainer.getChildren().add(card);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // ✅ Open InscriptionForm inside MainLayout
        private void openInscriptionForm(Formation formation) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/InscriptionForm.fxml"));
                Node view = loader.load();

                InscriptionFormController controller = loader.getController();
                controller.setFormation(formation.getId(), formation.getNom());
                controller.setMainLayout(mainLayout);

                controller.setOnSaved(() -> {
                    // after inscription saved, refresh (optional) and go back
                    refresh();
                    goBackToList();
                });

                controller.setOnCancel(this::goBackToList);

                if (mainLayout != null) {
                    mainLayout.setView(view);
                } else {
                    System.out.println("ERROR: mainLayout is null in FormationsCandidatController.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void goBackToList() {
            if (mainLayout != null) {
                // ✅ adapte le path à ton FXML candidat réel
                mainLayout.setContent("/fxml/formations/FormationsCandidat.fxml");
            }
        }

        @FXML
        private void onSearch() { applySearchAndSort(); }

        @FXML
        private void onTriChanged() { applySearchAndSort(); }

        private void applySearchAndSort() {
            String q = (searchField.getText() == null) ? "" : searchField.getText().toLowerCase().trim();

            List<Formation> filtered = all.stream()
                    .filter(f ->
                            (f.getNom() != null && f.getNom().toLowerCase().contains(q)) ||
                                    (f.getCategorie() != null && f.getCategorie().toLowerCase().contains(q)) ||
                                    (f.getFormateur() != null && f.getFormateur().toLowerCase().contains(q))
                    )
                    .toList();

            List<Formation> sorted = new ArrayList<>(filtered);
            String tri = triCombo.getValue();

            if ("Date début (asc)".equals(tri)) {
                sorted.sort(Comparator.comparing(Formation::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder())));
            } else if ("Date début (desc)".equals(tri)) {
                sorted.sort(Comparator.comparing(Formation::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            } else if ("Catégorie".equals(tri)) {
                sorted.sort(Comparator.comparing(f -> safe(f.getCategorie())));
            } else if ("Difficulté".equals(tri)) {
                sorted.sort(Comparator.comparing(f -> safe(f.getDifficulte())));
            }

            renderCards(sorted);
        }

        private String safe(String s) { return s == null ? "" : s; }
    }