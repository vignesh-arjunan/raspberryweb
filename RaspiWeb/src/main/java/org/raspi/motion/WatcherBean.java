package org.raspi.motion;

import org.raspi.utils.SendEmail;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import org.raspi.media.MediaBean;
//import org.raspi.media.SpeakBean;
import org.raspi.timer.PreferencesBean;
import static org.raspi.utils.Constants.MOTION_DIR;
import org.raspi.utils.FileValidator;

/**
 *
 * @author vignesh
 */
@Named
@DependsOn("PreferencesBean")
@Singleton
@Startup
public class WatcherBean {

    private final File dir = MOTION_DIR;
    private WatchDir watchDir;
    private Thread listenerThread;
    @Inject
    private PreferencesBean preferencesBean;
    private boolean notifyEventStart;
    private boolean notifyAttachment;
    private final List<String> errors = new ArrayList<>();
    private final File motionDir = MOTION_DIR;
    private List<File> recordingFiles;
    private File selectedFile;
    @Inject
    private MediaBean mediaBean;
    private final int max_size = 50;
    private final FileFilter fileFilter = file -> file.getName().contains(".") && file.length() > 0 && file.exists();
    private final Predicate<File> filePredicateToRemoveBadFiles = file -> file.length() == 0 || !file.exists();

    public List<String> getErrors() {
        return errors;
    }

    public void clearErrors() {
        errors.clear();
    }

    public boolean isNotifyEventStart() {
        return notifyEventStart;
    }

    public void setNotifyEventStart(boolean notifyEventStart) {
        this.notifyEventStart = notifyEventStart;
    }

    public boolean isNotifyAttachment() {
        return notifyAttachment;
    }

    public void setNotifyAttachment(boolean notifyAttachment) {
        this.notifyAttachment = notifyAttachment;
    }

    @PostConstruct
    void init() {
        setNotifyEventStart(preferencesBean.getPreferences().isNotifyEventStart());
        setNotifyAttachment(preferencesBean.getPreferences().isNotifyAttachment());

        dir.mkdir();

        try {
            watchDir = new WatchDir(dir.toPath(), false);
            watchDir.startNotificationThread();
            listenerThread = new Thread(() -> {
                watchDir.subject((subject) -> {

                    loadRecordingFiles();

                    if (preferencesBean.getPreferences().isEmailPassswordVerified() && notifyEventStart) {
                        String email = preferencesBean.getPreferences().getEmail();
                        String password = preferencesBean.getPreferences().getPassword();
                        SendEmail.MailProvider mailProvider = preferencesBean.getPreferences().getMailProvider();
                        try {
                            SendEmail.send(email, password, email, email, subject, "attachment coming shortly", null, mailProvider);
                        } catch (MessagingException | IOException ex) {
                            Logger.getLogger(WatcherBean.class.getName()).log(Level.SEVERE, null, ex);
                            errors.add("Could not send alert for " + subject);
                        }
                    }
                },
                        (subject, file) -> {
                            String email = preferencesBean.getPreferences().getEmail();
                            String password = preferencesBean.getPreferences().getPassword();
                            SendEmail.MailProvider mailProvider = preferencesBean.getPreferences().getMailProvider();
                            if (preferencesBean.getPreferences().isEmailPassswordVerified() && notifyAttachment) {
                                try {
                                    SendEmail.send(email, password, email, email, subject, "PFA", file, mailProvider);
                                    System.out.println("deleting " + file + ", status " + file.delete());
                                } catch (MessagingException | IOException ex) {
                                    Logger.getLogger(WatcherBean.class.getName()).log(Level.SEVERE, null, ex);
                                    errors.add("Could not send attachement for " + file);
                                }
                            }
                        }, () -> {
                        });
            });
            listenerThread.start();
        } catch (IOException ex) {
            Logger.getLogger(WatcherBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        loadRecordingFiles();
    }

    public void pushNotification(Runnable runnable) {
        watchDir.pushNotification(runnable);
    }

    @PreDestroy
    void destroy() {
        try {
            watchDir.stopAll();
        } catch (InterruptedException ex) {
            Logger.getLogger(WatcherBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        listenerThread.interrupt();
        try {
            listenerThread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(WatcherBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadRecordingFiles() {
        Comparator<File> fileComparator = (f1, f2) -> Long.compareUnsigned(f1.lastModified(), f2.lastModified());
        Stream<File> fileStream = Stream.of(motionDir.listFiles(fileFilter)).sorted(fileComparator.reversed());
        recordingFiles = fileStream.limit(max_size).collect(toList());
        recordingFiles.removeIf(filePredicateToRemoveBadFiles);
        fileStream = Stream.of(motionDir.listFiles(fileFilter)).sorted(fileComparator.reversed());
        Object[] allFiles = fileStream.toArray();
        fileStream = Stream.of(motionDir.listFiles(fileFilter)).sorted(fileComparator.reversed());
        IntStream.range(0, (int) fileStream.count())
                .forEach((index) -> {
                    if ((index + 1) > max_size) {
                        ((File) allFiles[index]).delete();
                    }
                });
    }

    public List<File> getRecordingFiles() {
        return recordingFiles;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    public void playSelectedFile() throws IOException, MessagingException {
        if (!FileValidator.validateFile(selectedFile)) {
            return;
        }
        mediaBean.setSelectedFile(selectedFile);
        mediaBean.playSelectedFile();
    }

    public void deliverAndDelete() throws IOException, MessagingException {
        if (!FileValidator.validateFile(selectedFile)) {
            return;
        }
        if (!preferencesBean.getPreferences().isEmailPassswordVerified()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Email Setting Not Done", "Please save your email settings"));
            return;
        }

        String email = preferencesBean.getPreferences().getEmail();
        String password = preferencesBean.getPreferences().getPassword();
        SendEmail.MailProvider mailProvider = preferencesBean.getPreferences().getMailProvider();

        SendEmail.send(email, password, email, email, "Delivering Recording " + selectedFile.getName(), "PFA", selectedFile, mailProvider);
        System.out.println("selectedFile.delete() " + selectedFile.delete());
        loadRecordingFiles();
    }

    public void deleteAll() {
        Stream.of(motionDir.listFiles(fileFilter)).forEach((File f) -> f.delete());
        loadRecordingFiles();
    }

    public void deleteSelectedFile() {
        if (!FileValidator.validateFile(selectedFile)) {
            return;
        }
        System.out.println("selectedFile.delete() " + selectedFile.delete());
        loadRecordingFiles();
    }
}
