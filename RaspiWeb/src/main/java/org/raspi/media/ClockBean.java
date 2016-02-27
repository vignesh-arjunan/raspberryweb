package org.raspi.media;

import org.raspi.utils.AlarmEntry;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.raspi.timer.PreferencesBean;
import static org.raspi.utils.Constants.PARENT_MEDIA_DIR;
import org.raspi.utils.HDMIControl;
import org.raspi.utils.MediaPlayer;

/**
 *
 * @author vignesh
 */
@Named
@Singleton
@Startup
public class ClockBean {

    private AlarmEntry alarmEntry = new AlarmEntry();
    private AlarmEntry selectedAlarmEntry;
    private List<AlarmEntry> alarmList;
    @Inject
    private PreferencesBean preferencesBean;
    @Inject
    private MediaBean mediaBean;
    private boolean addMode = true;
    private boolean isPlaying;

    public boolean isAddMode() {
        return addMode;
    }

    public void setAddMode(boolean addMode) {
        this.addMode = addMode;
    }

    public AlarmEntry getSelectedAlarmEntry() {
        return selectedAlarmEntry;
    }

    public void setSelectedAlarmEntry(AlarmEntry selectedAlarmEntry) {
        this.selectedAlarmEntry = selectedAlarmEntry;
        alarmEntry = new AlarmEntry();
        setAddMode(false);
        if (selectedAlarmEntry != null) {
            alarmEntry.setAlarmTime(selectedAlarmEntry.getAlarmTime());
            alarmEntry.setChosenMedia(selectedAlarmEntry.getChosenMedia());
            alarmEntry.setHours(selectedAlarmEntry.getHours());
            alarmEntry.setMins(selectedAlarmEntry.getMins());
            alarmEntry.setName(selectedAlarmEntry.getName());
            alarmEntry.setPlayList(selectedAlarmEntry.isPlayList());
            alarmEntry.setSelectedDays(selectedAlarmEntry.getSelectedDays());
            alarmEntry.setSelectedPlayListIndex(selectedAlarmEntry.getSelectedPlayListIndex());
        }
    }

    public void handleRowSelect() {
        setAddMode(false);
    }

    public void handleRowUnselect() {
        setAddMode(true);
    }

    public void clearSelection() {
        if (selectedAlarmEntry != null) {
            setSelectedAlarmEntry(null);
            alarmEntry = new AlarmEntry();
            setAddMode(true);
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Please select", "Please select an Alarm"));
        }
    }

    public List<AlarmEntry> getAlarmList() {
        return alarmList;
    }

    public void setAlarmList(List<AlarmEntry> alarmList) {
        this.alarmList = alarmList;
    }

    public AlarmEntry getAlarmEntry() {
        return alarmEntry;
    }

    public void setAlarmEntry(AlarmEntry alarmEntry) {
        this.alarmEntry = alarmEntry;
    }

    @PostConstruct
    void init() {
        alarmList = preferencesBean.getPreferences().getAlarmList();
        HDMIControl.setHDMIActive(false);
    }

    @PreDestroy
    void destroy() {

    }

    @Schedule(second = "0", minute = "*", hour = "*", info = "Speak Timer 1", persistent = false)
    public void minuteTimeout() {
        alarmList.stream().filter((alarmEntryItem) -> {
            LocalTime localTime = LocalTime.now();

            return (alarmEntryItem.isActive()
                    && alarmEntryItem.getAlarmTime().getHour() == localTime.getHour()
                    && alarmEntryItem.getAlarmTime().getMinute() == localTime.getMinute()
                    && isAlarmDay(alarmEntryItem));
        }).forEach((AlarmEntry alarmEntryItem) -> {
            try {
                if (!alarmEntryItem.isPlayList()) {
                    mediaBean.setMediaPlayer(new MediaPlayer(new File(PARENT_MEDIA_DIR + File.separator + alarmEntryItem.getChosenMedia())));
                    mediaBean.getMediaPlayer().play(true);
                } else {
                    mediaBean.playAll(alarmEntryItem.getSelectedPlayListIndex() - 1);
                }
            } catch (IOException ex) {
                Logger.getLogger(ClockBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    @Schedule(second = "*", minute = "*", hour = "*", info = "HDMI Checker", persistent = false)
    public void secondTimeout() {
        // System.out.println("in second timeout");

        if (MediaPlayer.isPlaying() && !isPlaying) {
            isPlaying = true;
            HDMIControl.setHDMIActive(isPlaying);
        } else if (!MediaPlayer.isPlaying() && isPlaying) {
            isPlaying = false;
            HDMIControl.setHDMIActive(isPlaying);
        } else {
            // do nothing
        }
    }

    private boolean isAlarmDay(AlarmEntry alarmEntry) {
        LocalDate localDate = LocalDate.now();
        return Arrays.stream(alarmEntry.getSelectedDays()).anyMatch(dayOfWeek -> dayOfWeek.equals(localDate.getDayOfWeek().name()));
    }

    public void clearAllAlarms() {
        alarmList.clear();
    }

    public String getButtonLabel() {
        if (isAddMode()) {
            return "Add Alarm";
        }
        return "Edit Alarm";
    }

    public void addEditAlarm() {
        System.out.println("name " + alarmEntry.getName());
        System.out.println("alarmToSet " + alarmEntry.getAlarmTime());
        System.out.println("selectedDays " + Arrays.toString(alarmEntry.getSelectedDays()));
        System.out.println("chosenMedia " + alarmEntry.getChosenMedia());
        System.out.println("isPlayList() " + alarmEntry.isPlayList());
        System.out.println("selectedPlayListIndex() " + alarmEntry.getSelectedPlayListIndex());

        if (alarmEntry.getName() == null || alarmEntry.getName().trim().isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Alarm Name cannot be empty"));
            return;
        }

        if (alarmEntry.getSelectedDays() == null || alarmEntry.getSelectedDays().length == 0) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Alarm Day not selected"));
            return;
        }

        if (!alarmEntry.isPlayList() && alarmEntry.getChosenMedia() == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Media Not Chosen"));
            return;
        }

        alarmEntry.setName(alarmEntry.getName().trim());
        alarmEntry.setAlarmTime(LocalTime.of(alarmEntry.getHours(), alarmEntry.getMins()));

        if ((isAddMode()) && (checkAlarmConflict(alarmList, alarmEntry) || checkDuplicateName())) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conflict", "Alarm time/name conflicting !!!"));
            return;
        }

        List<AlarmEntry> alarmListToCheck = new ArrayList<>(alarmList);
        alarmListToCheck.remove(alarmEntry);
        if ((!isAddMode()) && (checkAlarmConflict(alarmListToCheck, alarmEntry))) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conflict", "Alarm time conflicting !!!"));
            return;
        }

        if (isAddMode()) {
            alarmList.add(alarmEntry);
        } else {
            int index = alarmList.indexOf(alarmEntry);
            alarmList.remove(index);
            alarmList.add(index, alarmEntry);
            clearSelection();
        }

        setAlarmEntry(new AlarmEntry());
    }

    private boolean checkDuplicateName() {
        return alarmList.stream().anyMatch((AlarmEntry entry) -> entry.getName().equals(alarmEntry.getName()));
    }

    private static boolean checkAlarmConflict(List<AlarmEntry> alarmListToCheck, AlarmEntry alarmEntryToCheck) {
        return alarmListToCheck.stream()
                .filter((AlarmEntry entry)
                        -> Arrays.asList(alarmEntryToCheck.getSelectedDays()).stream().anyMatch((day1) -> (Arrays.asList(entry.getSelectedDays()).stream().anyMatch((day2) -> (day1.equals(day2))))))
                .filter((AlarmEntry entry)
                        -> alarmEntryToCheck.getAlarmTime().equals(entry.getAlarmTime()))
                .findAny().isPresent();
    }

    public void saveAlarmList() throws IOException {
        preferencesBean.getPreferences().setAlarmList(alarmList);
        preferencesBean.savePreferences();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful", "Alarm List Saved"));
    }

    public void deleteAlarm() {
        if (selectedAlarmEntry != null) {
            alarmList.remove(selectedAlarmEntry);
            alarmEntry = new AlarmEntry();
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Please select", "Please select an Alarm"));
        }
    }

    public void activateAlarm() {
        activateDeactvteAlarms((AlarmEntry alarmEntry1) -> {
            if (alarmEntry.isActive()) {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Already Active", "Alarm is already Active"));
            } else {
                alarmEntry.setActive(true);
            }
        });

    }

    public void deActivateAlarm() {
        activateDeactvteAlarms((AlarmEntry alarmEntry1) -> {
            if (!selectedAlarmEntry.isActive()) {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Already InActive", "Alarm is already InActive"));
            } else {
                selectedAlarmEntry.setActive(false);
            }
        });

    }

    private void activateDeactvteAlarms(Consumer<AlarmEntry> consumer) {
        if (selectedAlarmEntry != null) {
            consumer.accept(selectedAlarmEntry);
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Please select", "Please select an Alarm"));
        }
    }

    public void saveWhatWhen() throws IOException {
        preferencesBean.savePreferences();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful", "Preferences Saved"));
    }

    public String[] getAllDays() {
        return new String[]{"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
    }
}
