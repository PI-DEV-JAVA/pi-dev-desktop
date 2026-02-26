package talentos.pidev.controllers;


import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import talentos.pidev.services.GStreamerService;

public class MediaController {

    @FXML
    private ImageView cameraView;

    private GStreamerService mediaService;

    @FXML
    public void initialize() {
        mediaService = new GStreamerService(cameraView);
    }

    @FXML
    private void stopAll() {
        mediaService.stopCamera();
        mediaService.stopMic();
    }

    // @FXML
    // private void stopMedia() {
    //     mediaService.startCamera();
    // }

    @FXML
    public void startCamera() {
        mediaService.startCamera();
        
    }

    @FXML
    public void stopCamera() {
        mediaService.stopCamera();
    }

    @FXML
    public void startMic() {
        mediaService.startMicrophone();
    }

    @FXML
    public void stopMic() {
        mediaService.stopMic();
    }
}
