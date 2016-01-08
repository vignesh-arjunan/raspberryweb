package org.raspi.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.Arrays;

/**
 *
 * @author vignesh
 */
public class AlarmEntry implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private short hours;
    private short mins;
    @JsonIgnore
    private LocalTime alarmTime;
    private String[] selectedDays;
    private String chosenMedia;
    private boolean active = true;
    private boolean playList = true;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getActiveString() {
        return active == true ? "Yes" : "No";
    }

    public short getHours() {
        return hours;
    }

    public void setHours(short hours) {
        this.hours = hours;
    }

    public short getMins() {
        return mins;
    }

    public void setMins(short mins) {
        this.mins = mins;
    }

    public boolean isPlayList() {
        return playList;
    }

    public void setPlayList(boolean playList) {
        this.playList = playList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChosenMedia() {
        return chosenMedia;
    }

    public void setChosenMedia(String chosenMedia) {
        this.chosenMedia = chosenMedia;
    }

    public String getDisplayChosenMedia() {
        if (playList) {
            return "PLAYLIST";
        }
        return getChosenMedia();
    }

    public String[] getSelectedDays() {
        return selectedDays;
    }

    public String getSelectedDaysToDisplay() {
        return Arrays.toString(selectedDays);
    }

    public void setSelectedDays(String[] selectedDays) {
        this.selectedDays = selectedDays;
    }

    public LocalTime getAlarmTime() {
        return alarmTime;
    }

    @JsonIgnore
    public String getAlarmDisplayTime() {
        return alarmTime.toString();
    }

    @JsonIgnore
    public void setAlarmTime(LocalTime alarmToSet) {
        this.alarmTime = alarmToSet;
    }

    @Override
    public String toString() {
        return name;
    }

}
