package talentos.pidev.utils;

import java.nio.ByteBuffer;

import dev.onvoid.webrtc.media.video.I420Buffer;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoFrameBuffer;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class videoUtils {
    public static void renderFrameToFX(VideoFrame frame, WritableImage image) {
        I420Buffer i420 = frame.buffer.toI420();
        int width = i420.getWidth();
        int height = i420.getHeight();


        // if ((int) image.getWidth() != width || (int) image.getHeight() != height) {
        // System.out.println("bbbbb");

        //     return;
        // }

        PixelWriter writer = image.getPixelWriter();

        ByteBuffer yPlane = i420.getDataY();
        ByteBuffer uPlane = i420.getDataU();
        ByteBuffer vPlane = i420.getDataV();

        int strideY = i420.getStrideY();
        int strideU = i420.getStrideU();
        int strideV = i420.getStrideV();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int Y = yPlane.get(y * strideY + x) & 0xFF;
                int U = uPlane.get((y / 2) * strideU + x / 2) & 0xFF;
                int V = vPlane.get((y / 2) * strideV + x / 2) & 0xFF;

                int r = (int) (Y + 1.402 * (V - 128));
                int g = (int) (Y - 0.344136 * (U - 128) - 0.714136 * (V - 128));
                int b = (int) (Y + 1.772 * (U - 128));

                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));

                writer.setColor(x, y, Color.rgb(r, g, b));
            }
        }
    }
};