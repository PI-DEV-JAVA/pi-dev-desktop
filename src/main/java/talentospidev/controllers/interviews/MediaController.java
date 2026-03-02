package talentospidev.controllers.interviews;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import talentospidev.models.User;
import talentospidev.models.interviews.ChatMessage;
import talentospidev.models.interviews.InterviewMeet;
import talentospidev.services.AuthService;
import talentospidev.services.ChatService;
// import talentos.pidev.models.schema.ChatMessage;
// import talentos.pidev.models.schema.InterviewMeet;
// import talentos.pidev.services.ChatService;
// import talentos.pidev.services.MediaService;
// import talentos.pidev.services.WebRTCService;
import talentospidev.services.MediaService;
import talentospidev.services.WebRTCService;

import java.io.BufferedReader;
import java.io.IOException;
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
    private ServerSocket discoverSocket;

    private ChatService client;
    // private String currentRoom = "1234";
    private String username = "speedweed";
    private InterviewMeet meet;
    private User user;

    private volatile boolean isDiscoveryActive = false;

    public void initData(InterviewMeet meet){
        this.meet=meet;
        this.username="speedweed";
        this.user=AuthService.getCurrentUser();
        startDiscoveryListener();
        WebRTCService.startScripts(meet.getId(),user.getEmail());
        

        try {
            client = new ChatService(new URI("ws://4.233.136.0:3000"), this,Long.toString(meet.getId()),user.getEmail());
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize() {
        isDiscoveryActive = true;
        

        Platform.runLater(() -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) videoGrid.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                stopAll();
            });
        });
    }

    @FXML
    private void toggleMute() {
        isMuted = !isMuted;
        muteBtn.setStyle(isMuted ? "-fx-background-color: #ea4335; -fx-background-radius: 50;"
                : "-fx-background-color: #3c4043; -fx-background-radius: 50;");
    }

    @FXML
    private void toggleCam() {
        sendPublisherCommand("TOGGLE_CAM");
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
            ChatMessage msg = new ChatMessage("chat", Long.toString(meet.getId()), user.getEmail(), text);

            client.sendMessage(msg);

            displayMessage("You: " + text, true);
            messageInput.clear();
        }
    }
    private void cleanupSocket() {
        try {
            if (discoverSocket != null && !discoverSocket.isClosed()) {
                discoverSocket.close();
            }
        } catch (IOException e) {
        }
    }

    private void startDiscoveryListener() {
        Thread discovery = new Thread(() -> {
            try {
                discoverSocket = new ServerSocket(8888);
                discoverSocket.setReuseAddress(true);

                System.out.println("Java Discovery Server active on port 8888...");

                while (isDiscoveryActive) {
                    Socket client = discoverSocket.accept();

                    try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                        String msg = in.readLine();
                        if (msg != null && msg.startsWith("NEW_PORT:")) {
                            int port = Integer.parseInt(msg.split(":")[1]);
                            addStream(port);
                        } else if (msg.startsWith("REMOVE_PORT:")) {
                            int port = Integer.parseInt(msg.split(":")[1]);
                            removeStream(port);
                        }
                    }
                }
            } catch (Exception e) {
                if (isDiscoveryActive) {
                    System.err.println("Discovery Error: " + e.getMessage());
                } else {
                    System.out.println("Discovery Server closed safely.");
                }
            } finally {
                cleanupSocket();
            }
        });
        discovery.setDaemon(true);
        discovery.start();
    }

    private void addStream(int port) {
        if (activeServices.containsKey(port))
            return;

        Platform.runLater(() -> {
            VBox container = new VBox();
            container.setAlignment(Pos.CENTER);
            container.setUserData(port);
            container.setStyle("-fx-background-color: #3c4043; -fx-background-radius: 10; -fx-overflow-hidden: true;");

            ImageView iv = new ImageView();

            iv.setPreserveRatio(true);

            iv.fitWidthProperty().bind(
                    videoGrid.widthProperty().subtract(30).divide(2));

            iv.fitHeightProperty().bind(
                    videoGrid.heightProperty().subtract(30).divide(2));

            container.getChildren().add(iv);
            videoGrid.getChildren().add(container);

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
                videoGrid.getChildren().removeIf(node -> node.getUserData() != null && node.getUserData().equals(port));
            }
        });
    }

    private void sendPublisherCommand(String command) {
        new Thread(() -> {
            try (Socket socket = new Socket("127.0.0.1", 8890);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(command);
            } catch (IOException e) {
                System.err.println("Publisher control error: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void stopAll() {
        try (Socket s = new Socket("127.0.0.1", 8889);
                PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
            out.println("SHUTDOWN");
        } catch (Exception e) {
            System.out.println("Could not reach Python Control Port.");
        }
        sendPublisherCommand("TERMINATE");
        activeServices.values().forEach(MediaService::stop);
        activeServices.clear();

        this.isDiscoveryActive = false;
        cleanupSocket();
        videoGrid.getChildren().clear();

        Platform.runLater(() -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) videoGrid.getScene().getWindow();
            stage.close();
        });

    }
}