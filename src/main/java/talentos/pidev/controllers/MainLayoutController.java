package talentos.pidev.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class MainLayoutController {

    @FXML
    private StackPane contentPane;

    @FXML
    private Label pageTitle;

    private static MainLayoutController instance;

    public MainLayoutController() {
        instance = this;
    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    public void navigate(String fxml, String title) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/" + fxml);

            if (fxmlUrl == null) {
                System.err.println("❌ Fichier FXML non trouvé: /fxml/" + fxml);
                showErrorView();
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);

            if (pageTitle != null) {
                pageTitle.setText(title);
            }

            System.out.println("✅ Navigué vers: " + fxml + " - " + title);

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la vue: " + fxml);
            e.printStackTrace();
            showErrorView();
        }
    }

    private void showErrorView() {
        try {
            URL errorUrl = getClass().getResource("/fxml/placeholder.fxml");
            if (errorUrl != null) {
                FXMLLoader loader = new FXMLLoader(errorUrl);
                Node errorView = loader.load();
                contentPane.getChildren().clear();
                contentPane.getChildren().add(errorView);
                if (pageTitle != null) {
                    pageTitle.setText("Erreur");
                }
            }
        } catch (Exception ex) {
            System.err.println("❌ Impossible de charger la vue d'erreur");
        }
    }

    @FXML
    public void initialize() {
        System.out.println("✅ MainLayoutController initialisé");
        // Charge OffersCardView par défaut
        navigate("OffersCardView.fxml", "Offres d'emploi");
    }
}
