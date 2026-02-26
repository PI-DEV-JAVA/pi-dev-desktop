package talentos.pidev.utils;

import org.freedesktop.gstreamer.*;



public class MediaSource {

    public static Element createVideoSource() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("linux")) {
            Element src = ElementFactory.make("v4l2src", "source");
            src.set("device", "/dev/video0"); 
            return src;
        }

        if (os.contains("win")) {
            Element src = ElementFactory.make("ksvideosrc", "source");
            src.set("device-index", 0);
            return src;
        }

        throw new RuntimeException("Unsupported OS: " + os);
    }
}
