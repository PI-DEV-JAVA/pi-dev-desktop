package talentos.pidev.services;


import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import talentos.pidev.utils.MediaSource;

import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.AppSink;


import java.nio.ByteBuffer;

public class GStreamerService {

    private Pipeline pipeline;
    private Pipeline micPipeline;
    private AppSink appSink;
    private final ImageView imageView;

    private WritableImage fxImage;
    private int width = 640;
    private int height = 480;

    private volatile boolean cameraRunning = false;

    public GStreamerService(ImageView imageView) {
        this.imageView = imageView;
        Gst.init("GStreamerService");
    }

    public void startCamera() {
        if (cameraRunning) return;
        cameraRunning = true;

        pipeline = new Pipeline("cameraPipeline");

        Element source = MediaSource.createVideoSource();
        Element convert = ElementFactory.make("videoconvert", "convert");
        appSink = (AppSink) ElementFactory.make("appsink", "sink");

        appSink.set("emit-signals", true);
        appSink.set("sync", false);
        appSink.setCaps(Caps.fromString(
                "video/x-raw,format=RGB,width=640,height=480"
        ));

        pipeline.addMany(source, convert, appSink);
        Element.linkMany(source, convert, appSink);

        fxImage = new WritableImage(width, height);

        appSink.connect((AppSink.NEW_SAMPLE) sink -> {
            Sample sample = sink.pullSample();
            if (sample == null) return FlowReturn.OK;

            Buffer buffer = sample.getBuffer();
            ByteBuffer bb = buffer.map(false);

            if (bb != null) {
                byte[] data = new byte[bb.remaining()];
                bb.get(data);
                buffer.unmap();

                Platform.runLater(() -> {
                    fxImage.getPixelWriter().setPixels(
                            0, 0, width, height,
                            PixelFormat.getByteRgbInstance(),
                            data, 0, width * 3
                    );
                    imageView.setImage(fxImage);
                });
            }

            sample.dispose();
            return FlowReturn.OK;
        });

        new Thread(() -> pipeline.play(), "gst-camera-thread").start();
    }

    public void stopCamera() {
        cameraRunning = false;
        if (pipeline != null) {
            pipeline.stop();
            pipeline.dispose();
            pipeline = null;
        }
        Platform.runLater(() -> imageView.setImage(null));
        
    }

    public void startMicrophone() {
        if (micPipeline != null) return;

         micPipeline = (Pipeline) Gst.parseLaunch(
                "autoaudiosrc ! audioconvert ! audioresample ! autoaudiosink"
        );
        micPipeline.play();
    }

    public void stopMic(){
        if (micPipeline != null) {
            micPipeline.stop();
            micPipeline = null;
        }
    }
}
