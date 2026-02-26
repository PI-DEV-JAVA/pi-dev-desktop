package talentos.pidev.controllers;

import javafx.fxml.FXML;

public class SidebarController {

    @FXML
    private void dashboard() {
        MainLayoutController.getInstance()
                .navigate("DashboardView.fxml", "Dashboard");
    }

    @FXML
    private void interviews() {
        MainLayoutController.getInstance()
                .navigate("InterviewView.fxml", "Interviews");
    }

    @FXML
    private void activities() {
        MainLayoutController.getInstance()
                .navigate("activities.fxml", "Activities");
    }

    @FXML
    private void projects() {
        MainLayoutController.getInstance()
                .navigate("projects.fxml", "Projects");
    }
    @FXML
    private void ToDoList() {
        MainLayoutController.getInstance()
                .navigate("activity_employee.fxml", "ToDoList");
    }

    @FXML
    private void reports() {}

    @FXML
    private void settings() {}
}
