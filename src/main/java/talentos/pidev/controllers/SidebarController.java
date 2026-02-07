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
                .navigate("ActivityView.fxml", "Activities");
    }

    @FXML
    private void projects() {
        MainLayoutController.getInstance()
                .navigate("ProjectView.fxml", "Projects");
    }

    @FXML
    private void reports() {}

    @FXML
    private void settings() {}
}
