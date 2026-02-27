package talentos.pidev.models.schema;

public class ChatMessage {
    public String type; 
    public String room;
    public String user;
    public String text;

    public ChatMessage(String type, String room, String user, String text) {
        this.type = type;
        this.room = room;
        this.user = user;
        this.text = text;
    }
}
