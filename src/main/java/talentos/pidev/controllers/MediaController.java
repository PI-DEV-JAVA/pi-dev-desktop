package talentos.pidev.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import talentos.pidev.services.MediaService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class MediaController {

    @FXML private TilePane videoGrid;
    @FXML private VBox chatBox;
    @FXML private TextField messageInput;
    @FXML private Button muteBtn, camBtn;

    private boolean isMuted = false;
    private boolean isCamOff = false;
    private final ConcurrentHashMap<Integer, MediaService> activeServices = new ConcurrentHashMap<>();

    @FXML
    public void initialize() {
        startDiscoveryListener();
    }

    @FXML
    private void toggleMute() {
        isMuted = !isMuted;
        muteBtn.setStyle(isMuted ? "-fx-background-color: #ea4335; -fx-background-radius: 50;" : "-fx-background-color: #3c4043; -fx-background-radius: 50;");
        // Add logic to tell Python to stop sending audio bytes
    }

    @FXML
    private void toggleCam() {
        isCamOff = !isCamOff;
        camBtn.setStyle(isCamOff ? "-fx-background-color: #ea4335; -fx-background-radius: 50;" : "-fx-background-color: #3c4043; -fx-background-radius: 50;");
        // To locally "hide" cam, you could set opacity of videoGrid to 0 or send a signal to Python
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText();
        if (text != null && !text.isEmpty()) {
            Label msgLabel = new Label("You: " + text);
            msgLabel.setWrapText(true);
            msgLabel.setStyle("-fx-background-color: #f1f3f4; -fx-padding: 8; -fx-background-radius: 10;");
            chatBox.getChildren().add(msgLabel);
            messageInput.clear();
            // Logic to send text to other users via socket/Janus
        }
    }

    private void startDiscoveryListener() {
        Thread discovery = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(8888)) {
                System.out.println("Java Discovery Server active on port 8888...");
                while (true) {
                    try (Socket client = server.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                        
                        String msg = in.readLine(); // Expects "NEW_PORT:9991"
                        if (msg != null && msg.startsWith("NEW_PORT:")) {
                            int port = Integer.parseInt(msg.split(":")[1]);
                            addStream(port);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        discovery.setDaemon(true);
        discovery.start();
    }

    private void addStream(int port) {
        if (activeServices.containsKey(port)) return;

        Platform.runLater(() -> {
            ImageView iv = new ImageView();
            iv.setFitWidth(320);
            iv.setPreserveRatio(true);
            iv.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

            videoGrid.getChildren().add(iv);

            MediaService service = new MediaService(iv, port);
            service.start();
            activeServices.put(port, service);
        });
    }

    @FXML
    private void stopAll() {
        activeServices.values().forEach(MediaService::stop);
        activeServices.clear();
        videoGrid.getChildren().clear();
    }
}