package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import talentos.pidev.dao.FormationDAO;
import talentos.pidev.dao.InscriptionDAO;
import talentos.pidev.models.Formation;
import talentos.pidev.models.Inscription;

import java.util.ArrayList;
import java.util.List;

public class InscriptionsRHController {

    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private FlowPane cardsContainer;

    private final InscriptionDAO dao = new InscriptionDAO();
    private final FormationDAO formationDAO = new FormationDAO();

    private Integer formationId = null;   // null => toutes
    private String formationNom = null;

    private List<Inscription> all = new ArrayList<>();
    private List<Formation> formations = new ArrayList<>();

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

    // ===================== UI BUILDERS =====================

    private Node makeFormationHeader(String nom, int count) {
        Label header = new Label("📚 " + nom + "  •  " + count + " inscription(s)");
        header.getStyleClass().add("formation-section-title");
        header.setMinWidth(900); // ajuste selon ton UI
        return header;
    }

    // ✅ plus de throws Exception -> compilation OK
    private Node makeInscriptionCard(Inscription i) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/InscriptionCard.fxml"));
            Node card = loader.load();

            InscriptionCardController controller = loader.getController();
            controller.setData(i);

            controller.setOnChanged(() -> {
                refresh();
                if (onChanged != null) onChanged.run();
            });

            return card;
        } catch (Exception e) {
            e.printStackTrace();
            // fallback node si erreur
            Label err = new Label("Erreur chargement card: " + (i == null ? "" : i.getCandidatEmail()));
            err.getStyleClass().add("muted-text");
            return err;
        }
    }

    // ===================== RENDER GROUPED =====================

    private void renderGroupedAll(List<Formation> formations, List<Inscription> allIns) {
        cardsContainer.getChildren().clear();

        for (Formation f : formations) {
            // ⚠️ adapte si ton getter s'appelle autrement
            List<Inscription> listF = allIns.stream()
                    .filter(i -> i.getFormationId() == f.getId())
                    .toList();

            if (listF.isEmpty()) continue;

            cardsContainer.getChildren().add(makeFormationHeader(f.getNom(), listF.size()));

            for (Inscription ins : listF) {
                cardsContainer.getChildren().add(makeInscriptionCard(ins));
            }
        }
    }

    private void renderGroupedSingle(int formationId, String formationNom, List<Inscription> list) {
        cardsContainer.getChildren().clear();
        cardsContainer.getChildren().add(makeFormationHeader(formationNom, list.size()));

        for (Inscription ins : list) {
            cardsContainer.getChildren().add(makeInscriptionCard(ins));
        }
    }

    // ===================== LOAD =====================

    private void refresh() {
        try {
            if (formationId != null) {
                all = dao.getInscriptionsByFormation(formationId);
                renderGroupedSingle(formationId, formationNom, all);
                return;
            }

            formations = formationDAO.getAllFormations();
            all = dao.getAllInscriptions();
            renderGroupedAll(formations, all);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== SEARCH =====================

    @FXML
    private void onSearch() {
        String q = (searchField.getText() == null) ? "" : searchField.getText().toLowerCase().trim();

        List<Inscription> filtered = all.stream()
                .filter(i ->
                        (i.getCandidatNom() != null && i.getCandidatNom().toLowerCase().contains(q)) ||
                                (i.getCandidatEmail() != null && i.getCandidatEmail().toLowerCase().contains(q))
                )
                .toList();

        if (formationId != null) {
            renderGroupedSingle(formationId, formationNom, filtered);
        } else {
            renderGroupedAll(formations, filtered);
        }
    }
}