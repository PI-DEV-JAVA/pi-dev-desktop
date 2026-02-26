package talentos.pidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import talentos.pidev.services.MediaService;

public class MediaController {

    @FXML
    private ImageView cameraView;

    private MediaService videoService;

    @FXML
    public void initialize() {
        // Initialize the service with the ImageView from FXML
        videoService = new MediaService(cameraView);
    }

    @FXML
    public void startCamera() {
        // This starts the thread that connects to your Python script
        videoService.start();
    }

    @FXML
    public void stopCamera() {
        videoService.stop();
    }

    @FXML
    private void stopAll() {
        stopCamera();
    }
    
    // Placeholder for Mic - you'd handle audio similarly or via different logic
    @FXML public void startMic() {}
    @FXML public void stopMic() {}
}