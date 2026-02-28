package talentos.pidev.services;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import java.io.DataInputStream;
import java.net.Socket;

public class MediaService {
    private final ImageView imageView;
    private final int port;
    private final WritableImage writableImage;
    private volatile boolean running = false;
    private Thread thread;

    public MediaService(ImageView imageView, int port) {
        this.imageView = imageView;
        this.port = port;
        this.writableImage = new WritableImage(640, 480);
        this.imageView.setImage(writableImage);
    }

    public ImageView getImageView() { return this.imageView; }

    public void start() {
        if (running) return;
        running = true;
        thread = new Thread(this::listenToPython);
        thread.setDaemon(true);
        thread.setName("Stream-Receiver-" + port);
        thread.start();
    }

    private void listenToPython() {
        byte[] frameBuffer = new byte[640 * 480 * 4];
        while (running) {
            try (Socket socket = new Socket("127.0.0.1", port);
                 DataInputStream in = new DataInputStream(socket.getInputStream())) {
                
                socket.setSoTimeout(5000); 
                while (running) {
                    long size = in.readLong();
                    if (size <= 0) break;
                    in.readFully(frameBuffer, 0, (int) size);
    
                    Platform.runLater(() -> {
                        writableImage.getPixelWriter().setPixels(
                            0, 0, 640, 480,
                            PixelFormat.getByteBgraInstance(),
                            frameBuffer, 0, 640 * 4
                        );
                    });
                }
            } catch (Exception e) {
                try { Thread.sleep(1000); } catch (InterruptedException ie) { break; }
            }
        }
    }

    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }
}