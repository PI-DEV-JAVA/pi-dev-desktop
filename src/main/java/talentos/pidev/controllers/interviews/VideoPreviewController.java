package talentos.pidev.controllers.interviews;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.video.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import talentos.pidev.utils.videoUtils;

public class VideoPreviewController {

    @FXML
    private ImageView videoView;

    private PeerConnectionFactory factory;
    private VideoDeviceSource videoSource;
    private WritableImage fxImage;

    @FXML
    public void initialize() {
        initWebRTC();
    }

    private void initWebRTC() {
        factory = new PeerConnectionFactory();

        var cameras = MediaDevices.getVideoCaptureDevices();
        if (cameras.isEmpty()) {
            System.err.println("No camera found");
            return;
        }

        VideoDevice camera = cameras.get(0);
        videoSource = new VideoDeviceSource();
        videoSource.setVideoCaptureDevice(camera);

        VideoTrack videoTrack = factory.createVideoTrack("camera", videoSource);

        fxImage = new WritableImage(640, 480);

        videoTrack.addSink(frame -> {
            Platform.runLater(()->{
                System.out.println("aaaa");
                videoUtils.renderFrameToFX(frame, fxImage);
                videoView.setImage(fxImage);
            });
        });

        videoSource.start();
    }

    public void dispose() {
        if (videoSource != null) videoSource.stop();
        if (factory != null) factory.dispose();
    }
}