package talentos.pidev.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import talentos.pidev.models.schema.ChatMessage;
import talentos.pidev.services.ChatService;
import talentos.pidev.services.MediaService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

public class MediaController {

    @FXML
    private TilePane videoGrid;
    @FXML
    private VBox chatBox;
    @FXML
    private TextField messageInput;
    @FXML
    private Button muteBtn, camBtn;

    private boolean isMuted = false;
    private boolean isCamOff = false;
    private final ConcurrentHashMap<Integer, MediaService> activeServices = new ConcurrentHashMap<>();

    private ChatService client;
    private String currentRoom = "1234";
    private String username = "speedweed";

    @FXML
    public void initialize() {
        startDiscoveryListener();

        try {
            client = new ChatService(new URI("ws://4.233.136.0:3000"), this);
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleMute() {
        isMuted = !isMuted;
        muteBtn.setStyle(isMuted ? "-fx-background-color: #ea4335; -fx-background-radius: 50;"
                : "-fx-background-color: #3c4043; -fx-background-radius: 50;");
    }

    @FXML
    private void toggleCam() {
        isCamOff = !isCamOff;
        camBtn.setStyle(isCamOff ? "-fx-background-color: #ea4335; -fx-background-radius: 50;"
                : "-fx-background-color: #3c4043; -fx-background-radius: 50;");
       
    }

    public void displayMessage(String text, boolean isUser) {
        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(280);

        String style = isUser
                ? "-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 8;"
                : "-fx-background-color: #f1f3f4; -fx-text-fill: #202124; -fx-background-radius: 10; -fx-padding: 8;";

        msgLabel.setStyle(style);
        chatBox.getChildren().add(msgLabel);
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (!text.isEmpty() && client != null && client.isOpen()) {
            ChatMessage msg = new ChatMessage("chat", currentRoom, username, text);

            client.sendMessage(msg);

            displayMessage("You: " + text, true);
            messageInput.clear();
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
                        if (msg.startsWith("NEW_PORT:")) {
                            int port = Integer.parseInt(msg.split(":")[1]);
                            addStream(port);
                        } else if (msg.startsWith("REMOVE_PORT:")) {
                            int port = Integer.parseInt(msg.split(":")[1]);
                            removeStream(port);
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
        if (activeServices.containsKey(port))
            return;

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

    private void removeStream(int port) {
        Platform.runLater(() -> {
            MediaService service = activeServices.remove(port);
            if (service != null) {
                service.stop();
                videoGrid.getChildren().removeIf(node -> {
                    if (node instanceof ImageView) {
                        return ((ImageView) node).equals(service.getImageView());
                    }
                    return false;
                });
            }
        });
    }

    @FXML
    private void stopAll() {
        activeServices.values().forEach(MediaService::stop);
        activeServices.clear();
        videoGrid.getChildren().clear();

        try (Socket s = new Socket("127.0.0.1", 8889);
                PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
            out.println("SHUTDOWN");
        } catch (Exception e) {
            System.out.println("Could not reach Python Control Port.");
        }
    }
}