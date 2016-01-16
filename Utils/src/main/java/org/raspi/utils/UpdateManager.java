/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.raspi.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.raspi.utils.Constants.DESTINATION;
import static org.raspi.utils.Constants.SOURCE;
import static org.raspi.utils.Constants.TEMP;

public class UpdateManager {

    private final String source;
    private final File destination;
    private final File temp;
    private boolean isUpdateAvailable;
    private long contentLength;
    private long totalDownloaded;

    public UpdateManager(String source, File destination, File temp) throws MalformedURLException, IOException {
        this.source = source;
        this.destination = destination;
        this.temp = temp;
        checkUpdateAvailable();
    }

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    public long getContentLength() {
        return contentLength;
    }

    public long getTotalDownloaded() {
        return totalDownloaded;
    }

    private void checkUpdateAvailable() throws MalformedURLException, IOException {
        URLConnection connection = new URL(source).openConnection();
        Date lastModified = new Date(connection.getLastModified());
        System.out.println("lastModified " + lastModified);
        contentLength = connection.getContentLengthLong();
        System.out.println("contentLength " + contentLength);
        isUpdateAvailable = destination.exists() && connection.getLastModified() > destination.lastModified();
        System.out.println("isUpdateAvailable " + isUpdateAvailable);
    }

    public void downloadLatestAndUndeployCurrent() throws IOException, MalformedURLException, FileNotFoundException, InterruptedException {
        if (!isUpdateAvailable) {
            throw new IllegalStateException("Update not available !!!");
        }
        URL url = new URL(source);
        Files.deleteIfExists(temp.toPath());
        try (InputStream in = new BufferedInputStream(url.openStream());
                FileOutputStream fos = new FileOutputStream(temp)) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) != -1) {
                fos.write(buf, 0, n);
                totalDownloaded += n;
                System.out.println(new Double((100.0 * getTotalDownloaded()) / getContentLength()).intValue());
            }
        }
        System.out.println("total " + totalDownloaded);
        Files.delete(destination.toPath());        
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        final String SOURCE = "https://bitbucket.org/vignesh_arjunan/raspberryweb/downloads/RaspiWeb.war";
//        final File DESTINATION = new File("D:\\Downloads\\RaspiWeb.war");
//        final File TEMP = new File("D:\\Downloads\\RaspiWeb.war.temp");
        UpdateManager updateManager = new UpdateManager(SOURCE, DESTINATION, TEMP);
        Thread thread = new Thread(() -> {
            try {
                if (updateManager.isUpdateAvailable()) {
                    updateManager.downloadLatestAndUndeployCurrent();
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(UpdateManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        thread.start();

        thread.join();
        // deploying latest from TEMP
        Files.move(TEMP.toPath(), DESTINATION.toPath(), StandardCopyOption.ATOMIC_MOVE);
    }

}
