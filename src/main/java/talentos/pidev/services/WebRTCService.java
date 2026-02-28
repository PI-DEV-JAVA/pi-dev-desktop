package talentos.pidev.services;

import java.io.File;
import java.io.IOException;

public class WebRTCService {
    private static Process publisherProcess;
    private static Process subscriberProcess;
    private static final String SCRIPTS_PATH = "/home/speedweed/Desktop/PI_dev/webRTCHandler";

    public static void startScripts() {
        if (isAnyScriptRunning()) {
            System.out.println("Scripts already running. Skipping start.");
            return;
        }

        try {
            File directory = new File(SCRIPTS_PATH);

            ProcessBuilder pubBuilder = new ProcessBuilder("/home/speedweed/Desktop/PI_dev/webRTCHandler/venv/bin/python3", "publisher.py");
            pubBuilder.directory(directory); 
            pubBuilder.inheritIO();
            publisherProcess = pubBuilder.start();

            ProcessBuilder subBuilder = new ProcessBuilder("/home/speedweed/Desktop/PI_dev/webRTCHandler/venv/bin/python3", "multi_subscribers.py");
            subBuilder.directory(directory); 
            subBuilder.inheritIO();
            subscriberProcess = subBuilder.start();

            System.out.println("Python background services initialized in: " + SCRIPTS_PATH);
        } catch (IOException e) {
            System.err.println("Could not start Python scripts. Check if the path is correct!");
            e.printStackTrace();
        }
    }

    public static boolean isAnyScriptRunning() {
        return (publisherProcess != null && publisherProcess.isAlive()) || 
               (subscriberProcess != null && subscriberProcess.isAlive());
    }

    public static void stopScripts() {
        
        if (publisherProcess != null) publisherProcess.destroyForcibly();
        if (subscriberProcess != null) subscriberProcess.destroyForcibly();
        
        publisherProcess = null;
        subscriberProcess = null;
    }

}
