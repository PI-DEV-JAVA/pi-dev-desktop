package talentos.pidev.services;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import java.io.DataInputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MediaService {
    private final ImageView cameraView;
    private WritableImage writableImage;
    private boolean running = false;
    private Thread thread;

    public MediaService(ImageView cameraView) {
        this.cameraView = cameraView;
        // Match the resolution set in your Python script (640x480)
        this.writableImage = new WritableImage(640, 480);
        this.cameraView.setImage(writableImage);
    }

    public void start() {
        running = true;
        thread = new Thread(this::listenToPython);
        thread.setDaemon(true);
        thread.start();
    }

    private void listenToPython() {
        try (Socket socket = new Socket("127.0.0.1", 9999);
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            
            // 4 bytes per pixel now (640 * 480 * 4 = 1,228,800 bytes)
            byte[] frameBuffer = new byte[640 * 480 * 4]; 
    
            while (running) {
                long size = in.readLong(); 
                in.readFully(frameBuffer);
    
                Platform.runLater(() -> {
                    writableImage.getPixelWriter().setPixels(
                        0, 0, 640, 480, 
                        PixelFormat.getByteBgraInstance(), // This is the most stable 4-byte format
                        frameBuffer, 0, 640 * 4 // Stride is now width * 4
                    );
                });
            }
        } catch (Exception e) { /* ... */ }
    }

    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }
}
