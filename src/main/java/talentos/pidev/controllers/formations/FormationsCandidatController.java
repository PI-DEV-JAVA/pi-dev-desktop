package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
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

    @FXML
    public void initialize() {
        triCombo.getItems().setAll(
                "Date début (asc)",
                "Date début (desc)",
                "Catégorie",
                "Difficulté"
        );
        triCombo.getSelectionModel().selectFirst();
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
            all = all.stream().filter(f -> "OUVERTE".equalsIgnoreCase(f.getStatut())).toList();
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
                cardsContainer.getChildren().add(card);

            } catch (Exception e) {
                e.printStackTrace();
            }
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
            sorted.sort(Comparator.comparing(Formation::getDateDebut));
        } else if ("Date début (desc)".equals(tri)) {
            sorted.sort(Comparator.comparing(Formation::getDateDebut).reversed());
        } else if ("Catégorie".equals(tri)) {
            sorted.sort(Comparator.comparing(f -> safe(f.getCategorie())));
        } else if ("Difficulté".equals(tri)) {
            sorted.sort(Comparator.comparing(f -> safe(f.getDifficulte())));
        }

        renderCards(sorted);
    }

    private String safe(String s) { return s == null ? "" : s; }
}
