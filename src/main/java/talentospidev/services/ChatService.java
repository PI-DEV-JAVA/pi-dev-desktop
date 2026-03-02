package talentospidev.services;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.google.gson.Gson;
import javafx.application.Platform;
// import talentos.pidev.controllers.MediaController;
// import talentos.pidev.models.schema.ChatMessage;
import talentospidev.controllers.interviews.MediaController;
import talentospidev.models.interviews.ChatMessage;

import java.net.URI;

public class ChatService extends WebSocketClient {
    private final MediaController controller;
    private final String Room,user;
    private final Gson gson = new Gson();

    public ChatService(URI serverUri, MediaController controller,String Room,String user) {
        super(serverUri);
        this.controller = controller;
        this.Room=Room;
        this.user=user;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        sendMessage(new ChatMessage("join", Room, user, ""));
    }

    @Override
    public void onMessage(String message) {
        ChatMessage msg = gson.fromJson(message, ChatMessage.class);
        if ("chat".equals(msg.type)) {
            Platform.runLater(() -> controller.displayMessage(msg.user + ": " + msg.text, false));
        }
    }

    public void sendMessage(ChatMessage msg) {
        send(gson.toJson(msg));
    }

    @Override public void onClose(int i, String s, boolean b) {}
    @Override public void onError(Exception e) { e.printStackTrace(); }
}
