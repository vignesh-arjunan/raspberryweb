package org.raspi.media;

import org.raspi.utils.AlarmEntry;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private MediaPlayer mediaPlayer;

    public AlarmEntry getSelectedAlarmEntry() {
        return selectedAlarmEntry;
    }

    public void setSelectedAlarmEntry(AlarmEntry selectedAlarmEntry) {
        this.selectedAlarmEntry = selectedAlarmEntry;
        alarmEntry = new AlarmEntry();
        if (selectedAlarmEntry != null) {
            alarmEntry.setAlarmTime(selectedAlarmEntry.getAlarmTime());
            alarmEntry.setChosenMedia(selectedAlarmEntry.getChosenMedia());
            alarmEntry.setHours(selectedAlarmEntry.getHours());
            alarmEntry.setMins(selectedAlarmEntry.getMins());
            alarmEntry.setName(selectedAlarmEntry.getName());
            alarmEntry.setPlayList(selectedAlarmEntry.isPlayList());
            alarmEntry.setSelectedDays(selectedAlarmEntry.getSelectedDays());
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
                    (mediaPlayer = new MediaPlayer(new File(PARENT_MEDIA_DIR + File.separator + alarmEntryItem.getChosenMedia()))).play(true);
                    return;
                }
                mediaBean.playAll();
            } catch (IOException ex) {
                Logger.getLogger(ClockBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    @Schedule(second = "*", minute = "*", hour = "*", info = "HDMI Checker", persistent = false)
    public void secondTimeout() {
        // System.out.println("in second timeout");
        if (!MediaPlayer.isPlayingVideo()) {
            // System.out.println("deactivating HDMI");
            HDMIControl.setHDMIActive(false);
        }
    }    

    private boolean isAlarmDay(AlarmEntry alarmEntry) {
        LocalDate localDate = LocalDate.now();
        return Arrays.stream(alarmEntry.getSelectedDays()).anyMatch(dayOfWeek -> dayOfWeek.equals(localDate.getDayOfWeek().name()));
    }

    public void clearAllAlarms() {
        alarmList.clear();
    }

    public void addAlarm() {
        System.out.println("name " + alarmEntry.getName());
        System.out.println("alarmToSet " + alarmEntry.getAlarmTime());
        System.out.println("selectedDays " + Arrays.toString(alarmEntry.getSelectedDays()));
        System.out.println("chosenMedia " + alarmEntry.getChosenMedia());
        System.out.println("isPlayList() " + alarmEntry.isPlayList());

        if (!alarmEntry.isPlayList() && alarmEntry.getChosenMedia() == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Media Not Chosen"));
            return;
        }

        alarmEntry.setAlarmTime(LocalTime.of(alarmEntry.getHours(), alarmEntry.getMins()));

        if (checkAlarmConflict() || checkUniqueName()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conflict", "Alarm time/name conflicting !!!"));
            return;
        }

        alarmList.add(alarmEntry);
        setAlarmEntry(new AlarmEntry());
    }

    private boolean checkUniqueName() {
        return alarmList.stream().anyMatch((AlarmEntry entry) -> entry.getName().equals(alarmEntry.getName()));
    }

    private boolean checkAlarmConflict() {
        return alarmList.stream()
                .filter((AlarmEntry entry)
                        -> Arrays.asList(alarmEntry.getSelectedDays()).stream().anyMatch((day1) -> (Arrays.asList(entry.getSelectedDays()).stream().anyMatch((day2) -> (day1.equals(day2))))))
                .filter((AlarmEntry entry)
                        -> alarmEntry.getAlarmTime().equals(entry.getAlarmTime()))
                .findAny().isPresent();
    }

    public void clearAlarm() {
        setAlarmEntry(new AlarmEntry());
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
