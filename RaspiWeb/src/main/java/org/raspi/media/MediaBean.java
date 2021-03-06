package org.raspi.media;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileStore;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.raspi.utils.AlarmEntry;
import org.raspi.utils.Constants;
import static org.raspi.utils.Constants.PARENT_MEDIA_DIR;
import static org.raspi.utils.Constants.USB_MEDIA_DIR;
import org.raspi.utils.FileValidator;
import org.raspi.utils.MediaPlayer;
import org.raspi.utils.MountUSB;
import org.raspi.utils.Youtube;

/**
 *
 * @author vignesh
 */
@Named
@ApplicationScoped
public class MediaBean implements Runnable {

    private final File parentMediaDir = PARENT_MEDIA_DIR;
    private List<File> mediaFiles;
    private final ArrayBlockingQueue<File> playListQueue = new ArrayBlockingQueue<>(200);
    private final Thread playerThread = new Thread(this);
    private boolean stopPlayerThread;
    private boolean repeat;
    private File selectedFile;
    @Inject
    private PlayListBean playListBean;
    @Inject
    private ClockBean clockBean;
    private final NumberFormat nf = NumberFormat.getNumberInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
    private String youtubeURL;
    private MediaPlayer mediaPlayer;
    private int selectedPlayListIndex = 1;
    private boolean isDownloading;
    private boolean waslastIssuedDownloadFailure;
    private Youtube youtube;

    public String getLastDownloadMsg() {
        System.out.println("in getLastDownloadMsg");
        if (youtube != null
                && youtube.getProcessExecutor().isStarted()
                && youtube.getProcessExecutor().isBlocking()) {
            return youtube.getProcessExecutor().getFlags().getLastInputMsg();
        }
        System.out.println("waslastIssuedDownloadFailure " + waslastIssuedDownloadFailure);
        System.out.println("No Active Download in progress");
        return "No Active Download in progress";
    }

    public void getLastDownloadStatus() throws IOException {
        System.out.println("in getLastDownloadStatus");
        if (waslastIssuedDownloadFailure && youtube != null) {
            FacesMessage message = new FacesMessage("Could not Download", "Could not download " + youtube.getFile().getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        } else if (!waslastIssuedDownloadFailure && youtube != null) {
            if (isDownloading) {
                FacesMessage message = new FacesMessage("Download in progress", "Downloading " + youtube.getFile().getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                FacesMessage message = new FacesMessage("Download Complete", "Downloaded " + youtube.getFile().getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        } else {
            FacesMessage message = new FacesMessage("No Active Download", "No Active Download in progress");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void cancelDownload() throws IOException {
        if (isDownloading) {
            youtube.killAllYoutubeDl();
            isDownloading = false;
            waslastIssuedDownloadFailure = false;
            youtube = null;
        }
    }

    public Youtube getYoutube() {
        return youtube;
    }

    public boolean isIsDownloading() {
        return isDownloading;
    }

    public void setIsDownloading(boolean isDownloading) {
        this.isDownloading = isDownloading;
    }

    public String getLastIssuedDownloadURL() throws IOException {
        return youtube != null ? youtube.getFile().getName() : "";
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public String getYoutubeURL() {
        return youtubeURL;
    }

    public void setYoutubeURL(String youtubeURL) {
        this.youtubeURL = youtubeURL;
    }

    public void clearYoutubeURL() {
        this.youtubeURL = "";
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        System.out.println("repeat " + repeat);
        this.repeat = repeat;
    }

    public File getParentMediaDir() {
        return parentMediaDir;
    }

    public List<File> getMediaFiles() {
        return mediaFiles;
    }

    public List<String> getMediaFileNames() {
        return mediaFiles.stream().map(file -> file.getName()).collect(toList());
    }

    public void playAll() throws IOException {
        stopWithoutClearingRepeat();
        playListQueue.addAll(playListBean.getPlayLists().get(playListBean.getSelectedTabIndex() - 1).getPlayList());
    }

    public void playAll(int index) throws IOException {
        stopWithoutClearingRepeat();
        playListQueue.addAll(playListBean.getPlayLists().get(index).getPlayList());
    }

    public void stop() throws IOException {
        setRepeat(false);
        stopWithoutClearingRepeat();
    }

    private void stopWithoutClearingRepeat() throws IOException {
        playListQueue.clear();
        MediaPlayer.killAllPlayers();
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
        System.out.println("selectedFile " + selectedFile);
    }

    public int getSelectedPlayListIndex() {
        return selectedPlayListIndex;
    }

    public void setSelectedPlayListIndex(int selectedPlayListIndex) {
        this.selectedPlayListIndex = selectedPlayListIndex;
    }

    public void addSelectedFile() {
        System.out.println("selectedFile " + selectedFile);
        if (!FileValidator.validateFile(selectedFile)) {
            return;
        }
        if (playListBean.getPlayLists().get((getSelectedPlayListIndex()) - 1).getPlayList().contains(selectedFile)) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Already Added", "Already Added to the Play List " + getSelectedPlayListIndex()));
            return;
        }
        playListBean.getPlayLists().get((getSelectedPlayListIndex()) - 1).getPlayList().add(selectedFile);
        FacesMessage message = new FacesMessage("Successful", "Added " + selectedFile.getName() + " to Play List " + getSelectedPlayListIndex());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void deleteSelectedFile() throws IOException {
        if (!FileValidator.validateFile(selectedFile)) {
            return;
        }

        List<AlarmEntry> referredAlarms = clockBean.getAlarmList().stream().filter(
                (AlarmEntry alarmEntry) -> {
                    if (alarmEntry.isPlayList()) {
                        return playListBean.getPlayLists().get(alarmEntry.getSelectedPlayListIndex() - 1).getPlayList().contains(selectedFile);
                    } else {
                        return new File(PARENT_MEDIA_DIR + File.separator + alarmEntry.getChosenMedia()).equals(selectedFile);
                    }
                }
        ).collect(toList());

        if (referredAlarms.isEmpty()) {
            System.out.println("selectedFile.delete() " + selectedFile.delete());
            loadMediaFiles();
            playListBean.getPlayLists().forEach(list -> list.getPlayList().remove(selectedFile));
            FacesMessage message = new FacesMessage("Successful", "Removed " + selectedFile.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Media Referred in Alarm", "Media Referrd in alarms : " + referredAlarms.stream().map(alarm -> alarm.getName()).collect(Collectors.joining(", "))));
        }
    }

    public void playSelectedFile() throws IOException {
        System.out.println("selectedFile " + selectedFile);
        if (!FileValidator.validateFile(selectedFile)) {
            return;
        }
        stop();
        (mediaPlayer = new MediaPlayer(selectedFile)).play(true);
    }

    private boolean isMediaActive() {
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No active player", "Nothing is being played"));
            return false;
        }
        return true;
    }

    public void pauseOrResume() throws IOException {
        if (isMediaActive()) {
            mediaPlayer.getPlayer().getOutputStream().write("p".getBytes());
            mediaPlayer.getPlayer().getOutputStream().flush();
        }
    }

    public void increaseVolume() throws IOException {
        if (isMediaActive()) {
            mediaPlayer.getPlayer().getOutputStream().write("+".getBytes());
            mediaPlayer.getPlayer().getOutputStream().flush();
        }
    }

    public void decreaseVolume() throws IOException {
        if (isMediaActive()) {
            mediaPlayer.getPlayer().getOutputStream().write("-".getBytes());
            mediaPlayer.getPlayer().getOutputStream().flush();
        }
    }

    public void seekPlus600() throws IOException {
        if (isMediaActive()) {
            mediaPlayer.getPlayer().getOutputStream().write("\\x1B[A".getBytes());
            mediaPlayer.getPlayer().getOutputStream().flush();
        }
    }

    public void seekMinus600() throws IOException {
        if (isMediaActive()) {
            mediaPlayer.getPlayer().getOutputStream().write("\\x1B[B".getBytes());
            mediaPlayer.getPlayer().getOutputStream().flush();
        }
    }

    public void seekPlus30() throws IOException {
        if (isMediaActive()) {
            mediaPlayer.getPlayer().getOutputStream().write("\\x1B[C".getBytes());
            mediaPlayer.getPlayer().getOutputStream().flush();
        }
    }

    public void seekMinus30() throws IOException {
        if (isMediaActive()) {
            mediaPlayer.getPlayer().getOutputStream().write("\\x1B[D".getBytes());
            mediaPlayer.getPlayer().getOutputStream().flush();
        }
    }

    public void playFromURI() {
        if (youtubeURL.trim().isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not play", "URL not entered"));
            return;
        }
        try {
            String videoURL = Youtube.getStreamingURI(new URI(youtubeURL.trim()).toString());
            System.out.println("videoURL " + videoURL);
            stop();
            new Thread(() -> {
                try {
                    (mediaPlayer = new MediaPlayer(videoURL)).play(true);
                } catch (IOException ex) {
                    Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
        } catch (IllegalArgumentException illegalArgumentException) {
            Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, illegalArgumentException);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Input", "Youtube URL is not valid"));
        } catch (IOException | URISyntaxException exception) {
            Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, exception);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not play", "URL not accessible"));
        }
    }

    public synchronized void downloadFromURI() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (youtubeURL.trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not play", "URL not entered"));
            return;
        }
        try {
            if (isDownloading) {
                context.addMessage(null, new FacesMessage("In Progress", "Download of " + youtube.getFile().getName() + " is in Progress"));
                return;
            }
            new URI(youtubeURL.trim()).toString();
            youtube = new Youtube(new URI(youtubeURL.trim()).toString());
            System.out.println("youtube.getFile().exists() " + youtube.getFile().exists());
            if (youtube.getFile().exists()) {
                context.addMessage(null, new FacesMessage("Already Downloaded", "Download of " + youtube.getFile().getName() + " is already done"));
                return;
            }

            if (new File(youtube.getFile().getAbsolutePath() + ".part").exists()) {
                context.addMessage(null, new FacesMessage("Resuming Download", "Resuming download of " + youtube.getFile().getName()));
            }

            waslastIssuedDownloadFailure = false;
            new Thread(() -> {
                try {
                    if (!isDownloading) {
                        isDownloading = true;
                        youtube.download();
                        loadMediaFiles();
                    }
                } catch (Throwable ex) {
                    waslastIssuedDownloadFailure = true;
                    Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    isDownloading = false;
                }
            }).start();

            context.addMessage(null, new FacesMessage("Download Started", "Download of " + youtube.getFile().getName() + " is in Progress"));

        } catch (IllegalArgumentException | IOException illegalArgumentException) {
            Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, illegalArgumentException);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Input", "Youtube URL is not valid"));
        } catch (URISyntaxException exception) {
            Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, exception);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not play", "URL not accessible"));
        }
    }

    public String getName(File toCheck) {
        return toCheck == null ? "" : toCheck.getName().substring(0, toCheck.getName().lastIndexOf("."));
    }

    public String getExt(File toCheck) {
        return toCheck == null ? "" : toCheck.getName().substring(toCheck.getName().lastIndexOf(".") + 1, toCheck.getName().length()).toUpperCase();
    }

    public long getSizeActual(File toCheck) {
        return toCheck == null ? 0 : toCheck.length();
    }

    public String getSize(File toCheck) {
        return toCheck == null ? "" : nf.format(toCheck.length());
    }

    public String getlastModified(File toCheck) {
        return toCheck == null ? "" : sdf.format(new Date(toCheck.lastModified()));
    }

    public void loadMediaFiles() throws IOException {
        MountUSB.mount();
        mediaFiles = new ArrayList<>(Arrays.asList(
                parentMediaDir.listFiles((dir, name) -> Stream.of(Constants.MediaFormat.values())
                        .anyMatch(format -> name.toUpperCase().endsWith(format.name())))));
        mediaFiles.addAll(Arrays.asList(
                USB_MEDIA_DIR.listFiles((dir, name) -> Stream.of(Constants.MediaFormat.values())
                        .anyMatch(format -> name.toUpperCase().endsWith(format.name())))));
    }

    public String getFreeMemUsage() throws IOException {
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            System.out.print(root + ": ");
            FileStore store = Files.getFileStore(root);
            System.out.println("available=" + nf.format(store.getUsableSpace()) + ", total=" + nf.format(store.getTotalSpace()));
            return nf.format(store.getUsableSpace());
        }
        return "";
    }

    public String getTotalMemUsage() throws IOException {
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            System.out.print(root + ": ");
            FileStore store = Files.getFileStore(root);
            System.out.println("available=" + nf.format(store.getUsableSpace()) + ", total=" + nf.format(store.getTotalSpace()));
            return nf.format(store.getTotalSpace());
        }
        return "";
    }

    @PostConstruct
    public void init() {
        try {
            loadMediaFiles();
        } catch (IOException ex) {
            Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        playerThread.start();
    }

    @PreDestroy
    public void stopPlayerThread() {
        stopPlayerThread = true;
        try {
            stop();
        } catch (IOException ex) {
            Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        playerThread.interrupt();
        try {
            playerThread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Thread Stopped");
    }

    @Override
    public void run() {
        System.out.println("Starting Player Thread");
        while (!stopPlayerThread) {
            try {
                // wait until currently playing media is done
                while (!stopPlayerThread && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    Thread.sleep(1000); // check after a second
                }

                File file = playListQueue.take();
                if (file.exists()) {
                    (mediaPlayer = new MediaPlayer(file)).play(true);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, ex);
                break;
            } catch (IOException ex) {
                Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (repeat && playListQueue.isEmpty()) {
                playListQueue.addAll(playListBean.getPlayLists().get(playListBean.getSelectedTabIndex() - 1).getPlayList()); // is this the desired behaviour
            }

        }
        System.out.println("Stopping Player Thread");
    }
}
