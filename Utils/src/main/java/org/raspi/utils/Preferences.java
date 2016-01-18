package org.raspi.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import static org.raspi.utils.Constants.NO_OF_PLAYLISTS;
import org.raspi.utils.SendEmail.MailProvider;

/**
 *
 * @author vignesh
 */
public class Preferences implements Serializable {

    private static final long serialVersionUID = 1L;

//    private List<File> playList = new ArrayList<>();
    private List<PlayListWrapper> playLists = new ArrayList<>(NO_OF_PLAYLISTS);
    private List<AlarmEntry> alarmList = new ArrayList<>();
    private String notificationWhen = "-1";
    private String notificationMsg = "I am up !!";
    private MailProvider mailProvider = MailProvider.GOOGLE;
    private String email = "";
    private String password = "";
    private boolean emailPassswordVerified;
    private boolean startMotionOnStartup;
    private boolean notifyEventStart;
    private boolean notifyAttachment;
    private boolean updateSoftwareAutomatically;

    public Preferences() {
        IntStream.range(0, NO_OF_PLAYLISTS).forEach((index) -> {
            PlayListWrapper playListWrapper = new PlayListWrapper();
            playListWrapper.setIndex(index);
            playLists.add(playListWrapper);
        });
    }

    public List<PlayListWrapper> getPlayLists() {
        return playLists;
    }

    public void setPlayLists(List<PlayListWrapper> playLists) {
        this.playLists = playLists;
    }

    public boolean isUpdateSoftwareAutomatically() {
        return updateSoftwareAutomatically;
    }

    public void setUpdateSoftwareAutomatically(boolean updateSoftwareAutomatically) {
        this.updateSoftwareAutomatically = updateSoftwareAutomatically;
    }

    public String getNotificationMsg() {
        return notificationMsg;
    }

    public void setNotificationMsg(String notificationMsg) {
        this.notificationMsg = notificationMsg;
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

    public boolean isStartMotionOnStartup() {
        return startMotionOnStartup;
    }

    public void setStartMotionOnStartup(boolean startMotionOnStartup) {
        this.startMotionOnStartup = startMotionOnStartup;
    }

    public boolean isEmailPassswordVerified() {
        return emailPassswordVerified;
    }

    public void setEmailPassswordVerified(boolean emailPassswordVerified) {
        this.emailPassswordVerified = emailPassswordVerified;
    }

    public MailProvider getMailProvider() {
        return mailProvider;
    }

    public void setMailProvider(MailProvider mailProvider) {
        this.mailProvider = mailProvider;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNotificationWhen() {
        return notificationWhen;
    }

    public void setNotificationWhen(String notificationWhen) {
        this.notificationWhen = notificationWhen;
    }

    public List<AlarmEntry> getAlarmList() {
        return alarmList;
    }

    public void setAlarmList(List<AlarmEntry> alarmList) {
        Objects.requireNonNull(alarmList, "Alarm List can't be null");
        this.alarmList = new ArrayList<>(alarmList);
    }
}
