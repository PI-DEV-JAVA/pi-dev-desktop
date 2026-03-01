package com.pi.controllers;

import com.pi.models.EvenementRh;
import com.pi.services.EvenementService;
import com.pi.utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class CalendrierController implements Initializable {

    @FXML private Label moisAnneeLabel;
    @FXML private GridPane enTeteGrid;
    @FXML private GridPane calendrierGrid;

    private EvenementService evenementService;
    private YearMonth currentYearMonth;
    private List<EvenementRh> evenements;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        evenementService = new EvenementService();
        currentYearMonth = YearMonth.now();

        chargerEvenements();
        afficherCalendrier();
    }

    private void chargerEvenements() {
        try {
            evenements = evenementService.getAllEvenements();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    @FXML
    private void moisPrecedent() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        afficherCalendrier();
    }

    @FXML
    private void moisSuivant() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        afficherCalendrier();
    }

    @FXML
    private void aujourdhui() {
        currentYearMonth = YearMonth.now();
        afficherCalendrier();
    }

    private void afficherCalendrier() {
        moisAnneeLabel.setText(currentYearMonth.format(FORMATTER));
        enTeteGrid.getChildren().clear();
        calendrierGrid.getChildren().clear();

        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (int i = 0; i < 7; i++) {
            Label jourLabel = new Label(jours[i]);
            jourLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            jourLabel.setStyle("-fx-text-fill: #2c3e50; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-background-radius: 5;");
            jourLabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setMargin(jourLabel, new Insets(5));
            enTeteGrid.add(jourLabel, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int row = 1;
        int col = dayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayBox = createDayBox(date);
            calendrierGrid.add(dayBox, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDayBox(LocalDate date) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-border-color: #ecf0f1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-min-height: 100; -fx-min-width: 100;");

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        box.getChildren().add(dayLabel);

        for (EvenementRh e : evenements) {
            if (e.getDateEvent().equals(date)) {
                Label eventLabel = createEventLabel(e);
                box.getChildren().add(eventLabel);
            }
        }

        box.setOnMouseClicked(event -> ouvrirDetailJour(date));
        return box;
    }

    private Label createEventLabel(EvenementRh e) {
        Label label = new Label("📅 " + e.getTitre());

        String couleur;
        switch (e.getTypeEvent().toLowerCase()) {
            case "job fair":
                couleur = "#3498db";
                break;
            case "workshop":
                couleur = "#27ae60";
                break;
            case "conférence":
            case "conference":
                couleur = "#9b59b6";
                break;
            default:
                couleur = "#95a5a6";
        }

        label.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-size: 11;");
        label.setMaxWidth(Double.MAX_VALUE);

        Tooltip tooltip = new Tooltip(e.getTitre() + "\n" + e.getTypeEvent() + "\n" + e.getLieu() + "\nStatut: " + e.getStatut());
        Tooltip.install(label, tooltip);

        return label;
    }

    private void ouvrirDetailJour(LocalDate date) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Événements du " + date);
        info.setHeaderText(null);

        StringBuilder contenu = new StringBuilder();
        for (EvenementRh e : evenements) {
            if (e.getDateEvent().equals(date)) {
                contenu.append("• ").append(e.getTitre())
                        .append(" (").append(e.getTypeEvent()).append(")\n")
                        .append("  Lieu: ").append(e.getLieu()).append("\n")
                        .append("  Statut: ").append(e.getStatut()).append("\n\n");
            }
        }

        if (contenu.length() == 0) {
            contenu.append("Aucun événement ce jour.");
        }

        info.setContentText(contenu.toString());
        info.showAndWait();
    }
}