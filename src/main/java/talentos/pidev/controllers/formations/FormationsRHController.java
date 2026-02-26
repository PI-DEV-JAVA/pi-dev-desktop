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

public class FormationsRHController {

    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> triCombo;

    private final FormationDAO formationDAO = new FormationDAO();
    private List<Formation> all = new ArrayList<>();

    private MainLayoutController mainLayout;
    private boolean uiReady = false;
    public void setMainLayout(MainLayoutController mainLayout) {
        this.mainLayout = mainLayout;
        if (uiReady) {
            applySearchAndSort(); // ou refresh() si tu veux recharger DB
        }
        System.out.println("✅ MainLayout injected into FormationsRHController: " + (mainLayout != null));


    }

    @FXML
    public void initialize() {
        uiReady = true;
        refresh();
        System.out.println("FormationsRHController init, mainLayout = " + mainLayout);
        triCombo.getItems().setAll(
                "Date début (asc)",
                "Date début (desc)",
                "Difficulté",
                "Catégorie",
                "Statut"
        );
        triCombo.getSelectionModel().selectFirst();

        // optionnel: auto-search on typing
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applySearchAndSort());
        }

        // optionnel: auto sort on selection
        if (triCombo != null) {
            triCombo.valueProperty().addListener((obs, oldV, newV) -> applySearchAndSort());
        }

        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    public void refresh() {
        try {
            all = formationDAO.getAllFormations();
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
                controller.setRHMode(true);
                controller.setOnChanged(this::refresh);
                controller.setMainLayout(mainLayout);
                cardsContainer.getChildren().add(card);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onAdd() {
        openAddForm();
    }

    private void openAddForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/FormationForm.fxml"));
            Node view = loader.load();

            FormationFormController controller = loader.getController();

            // when saved -> refresh list and go back to RH list
            controller.setOnSaved(() -> {
                refresh();
                goBackToList();
            });

            // cancel -> back to RH list
            controller.setOnCancel(this::goBackToList);

            // show inside main content
            if (mainLayout != null) {
                mainLayout.setView(view);
            }
            else {
                System.out.println("ERROR: mainLayout is NULL in FormationsRHController (injection not done).");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Optional helper for edit mode (use later if needed)
    public void openEditForm(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/FormationForm.fxml"));
            Node view = loader.load();

            FormationFormController controller = loader.getController();
            controller.setFormation(formation);

            controller.setOnSaved(() -> {
                refresh();
                goBackToList();
            });
            controller.setOnCancel(this::goBackToList);

            if (mainLayout != null) {
                mainLayout.setView(view);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goBackToList() {
        if (mainLayout != null) {
            mainLayout.setContent("/fxml/formations/FormationsRH.fxml");
        }
    }

    @FXML
    private void onSearch() {
        applySearchAndSort();
    }

    @FXML
    private void onTriChanged() {
        applySearchAndSort();
    }

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
        } else if ("Statut".equals(tri)) {
            sorted.sort(Comparator.comparing(f -> safe(f.getStatut())));
        }

        renderCards(sorted);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}