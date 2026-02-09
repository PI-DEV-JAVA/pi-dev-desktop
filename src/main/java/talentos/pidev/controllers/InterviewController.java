package talentos.pidev.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import talentos.pidev.models.dao.InterviewDAO;
import talentos.pidev.models.schema.Interview;

import java.sql.SQLException;
import java.util.List;

public class InterviewController {

    @FXML private TextField searchField;
    @FXML private TableView<Interview> table;
    @FXML private TableColumn<Interview, Long> idCol;
    @FXML private TableColumn<Interview, String> titleCol;
    @FXML private TableColumn<Interview, String> statusCol;
    @FXML private TableColumn<Interview, Double> gradeCol;
    @FXML private TableColumn<Interview, String> createdCol;
    @FXML private TableColumn<Interview, Void> colActions;


    private InterviewDAO dao;

    @FXML
    public void initialize() throws SQLException {
        dao = new InterviewDAO();

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("generalGrade"));
        createdCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadData();

          addActionButtons();

        table.setRowFactory(tv -> {
            TableRow<Interview> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (! row.isEmpty() && event.getButton()== MouseButton.PRIMARY 
                     && event.getClickCount() == 2) {
                    Interview clicked = row.getItem();
                    openForm(clicked);
                }
            });
            return row;
        });
    }

    private void loadData() {
        List<Interview> list = dao.list(searchField.getText(), true);
        table.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void onSearch() { loadData(); }

    @FXML
    private void onCreate() { openForm(null); }

    @FXML
    private void onEdit() {
        Interview selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) openForm(selected);
    }

    @FXML
    private void onDelete() {
        Interview selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            dao.delete(selected.getId());
            loadData();
        }
    }

     private void addActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, delBtn);

            {
                editBtn.setStyle("-fx-background-color: #0D203B; -fx-text-fill: white;");
                delBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Interview interview = getTableView().getItems().get(getIndex());
                    openForm(interview);
                });

                delBtn.setOnAction(event -> {
                    Interview interview = getTableView().getItems().get(getIndex());
                    confirmDelete(interview);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void confirmDelete(Interview interview) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Interview");
        alert.setContentText("Are you sure you want to delete interview: " + interview.getTitle() + "?");
        alert.initModality(Modality.APPLICATION_MODAL);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dao.delete(interview.getId());
                loadData();
            }
        });
    }

    private void openForm(Interview interview) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InterviewForm.fxml"));
            VBox root = loader.load();

            InterviewFormController formController = loader.getController();
            formController.setInterview(interview);
            formController.setDao(dao);
            formController.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(interview == null ? "New Interview" : "Edit Interview");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refresh() { loadData(); }
}
