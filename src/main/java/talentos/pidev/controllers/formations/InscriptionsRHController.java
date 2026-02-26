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

    private Integer formationId = null;   // null => all inscriptions
    private String formationNom = null;

    private List<Inscription> all = new ArrayList<>();

    private Runnable onBack;
    private Runnable onChanged;

    public void setOnBack(Runnable r) { this.onBack = r; }
    public void setOnChanged(Runnable r) { this.onChanged = r; }

    @FXML
    private void onBack() {
        if (onBack != null) onBack.run();
    }

    public void setFormation(int formationId, String formationNom) {
        this.formationId = formationId;
        this.formationNom = formationNom;

        if (titleLabel != null) {
            titleLabel.setText("Inscriptions - " + formationNom);
        }

        refresh();
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.setText("Inscriptions (toutes)");
        }
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        try {
            if (formationId == null) {
                all = dao.getAllInscriptions();
            } else {
                all = dao.getInscriptionsByFormation(formationId);
            }

            render(all);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void render(List<Inscription> list) {
        cardsContainer.getChildren().clear();

        for (Inscription i : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/InscriptionCard.fxml"));
                Node card = loader.load();

                InscriptionCardController controller = loader.getController();
                controller.setData(i);

                // ✅ when card changes -> refresh this list + notify parent page (formations) if needed
                controller.setOnChanged(() -> {
                    refresh();
                    if (onChanged != null) onChanged.run();
                });

                cardsContainer.getChildren().add(card);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onSearch() {
        String q = (searchField.getText() == null) ? "" : searchField.getText().toLowerCase().trim();

        List<Inscription> filtered = all.stream()
                .filter(i ->
                        (i.getCandidatNom() != null && i.getCandidatNom().toLowerCase().contains(q)) ||
                                (i.getCandidatEmail() != null && i.getCandidatEmail().toLowerCase().contains(q))
                )
                .toList();

        render(filtered);
    }
}