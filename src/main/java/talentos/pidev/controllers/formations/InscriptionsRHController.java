package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import talentos.pidev.dao.InscriptionDAO;
import talentos.pidev.models.Inscription;

import java.util.ArrayList;
import java.util.List;

public class InscriptionsRHController {

    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private FlowPane cardsContainer;

    private final InscriptionDAO dao = new InscriptionDAO();

    private int formationId;
    private String formationNom;

    private List<Inscription> all = new ArrayList<>();

    public void setFormation(int formationId, String formationNom) {
        this.formationId = formationId;
        this.formationNom = formationNom;
        titleLabel.setText("Inscriptions - " + formationNom);
        refresh();
    }

    @FXML
    public void initialize() {
        // si on ouvre la page sans passer setFormation
        titleLabel.setText("Inscriptions");
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    public void refresh() {
        try {
            if (formationId == 0) {
                cardsContainer.getChildren().clear();
                return;
            }
            all = dao.getByFormation(formationId);
            applySearch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch() {
        applySearch();
    }

    private void applySearch() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();

        List<Inscription> filtered = all.stream()
                .filter(i ->
                        (i.getCandidatNom() != null && i.getCandidatNom().toLowerCase().contains(q)) ||
                                (i.getCandidatEmail() != null && i.getCandidatEmail().toLowerCase().contains(q))
                )
                .toList();

        renderCards(filtered);
    }

    private void renderCards(List<Inscription> list) {
        cardsContainer.getChildren().clear();

        for (Inscription i : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/InscriptionCard.fxml"));
                Node card = loader.load();

                InscriptionCardController c = loader.getController();
                c.setData(i);
                c.setOnChanged(this::refresh);

                cardsContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
