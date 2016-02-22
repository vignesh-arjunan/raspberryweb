package org.raspi.media;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.raspi.timer.PreferencesBean;
import org.raspi.utils.InternetRadioStation;

/**
 *
 * @author vignesh
 */
@Named
@Singleton
@Startup
public class InternetRadioBean {

    private InternetRadioStation internetRadioStationEntry = new InternetRadioStation();
    private InternetRadioStation selectedInternetRadioStationEntry;
    private List<InternetRadioStation> internetRadioStationList;
    @Inject
    private PreferencesBean preferencesBean;
    @Inject
    private MediaBean mediaBean;
    private boolean addMode = true;
    private boolean isVideoPlaying = false;

    public boolean isAddMode() {
        return addMode;
    }

    public void setAddMode(boolean addMode) {
        this.addMode = addMode;
    }

    public InternetRadioStation getSelectedAlarmEntry() {
        return selectedInternetRadioStationEntry;
    }

    public void setSelectedAlarmEntry(InternetRadioStation selectedAlarmEntry) {
        this.selectedInternetRadioStationEntry = selectedAlarmEntry;
        internetRadioStationEntry = new InternetRadioStation();
        setAddMode(false);
        if (selectedAlarmEntry != null) {
            internetRadioStationEntry.setStation(selectedAlarmEntry.getStation());
            internetRadioStationEntry.setUri(selectedAlarmEntry.getUri());
        }
    }

    public void handleRowSelect() {
        setAddMode(false);
    }

    public void handleRowUnselect() {
        setAddMode(true);
    }

    public void clearSelection() {
        if (selectedInternetRadioStationEntry != null) {
            setSelectedAlarmEntry(null);
            internetRadioStationEntry = new InternetRadioStation();
            setAddMode(true);
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Please select", "Please select an Station"));
        }
    }

    public List<InternetRadioStation> getInternetRadioStationList() {
        return internetRadioStationList;
    }

    public void setInternetRadioStationList(List<InternetRadioStation> internetRadioStationList) {
        this.internetRadioStationList = internetRadioStationList;
    }

    public InternetRadioStation getInternetRadioStationEntry() {
        return internetRadioStationEntry;
    }

    public void setInternetRadioStationEntry(InternetRadioStation internetRadioStationEntry) {
        this.internetRadioStationEntry = internetRadioStationEntry;
    }

    @PostConstruct
    void init() {
        internetRadioStationList = preferencesBean.getPreferences().getStations();
    }

    @PreDestroy
    void destroy() {

    }

    public void clearAllAlarms() {
        internetRadioStationList.clear();
    }

    public String getButtonLabel() {
        if (isAddMode()) {
            return "Add Station";
        }
        return "Edit Station";
    }

    public void addEditStation() {
        System.out.println("station " + internetRadioStationEntry.getStation());
        System.out.println("uri " + internetRadioStationEntry.getUri());

        if (internetRadioStationEntry.getStation() == null || internetRadioStationEntry.getStation().trim().isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Station cannot be empty"));
            return;
        }

        if (internetRadioStationEntry.getUri() == null || internetRadioStationEntry.getUri().trim().isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Web Address cannot be empty"));
            return;
        }

        try {
            new URI(internetRadioStationEntry.getUri().trim()).toString();
        } catch (URISyntaxException ex) {
            Logger.getLogger(InternetRadioBean.class.getName()).log(Level.SEVERE, null, ex);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Web Address not valid"));
            return;
        }

        internetRadioStationEntry.setStation(internetRadioStationEntry.getStation().trim());
        internetRadioStationEntry.setUri(internetRadioStationEntry.getUri().trim());

        if (isAddMode() && checkDuplicate()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conflict", "Station/Web Address conflicting !!!"));
            return;
        }

        List<InternetRadioStation> internetRadioStationListToCheck = new ArrayList<>(internetRadioStationList);
        internetRadioStationListToCheck.remove(internetRadioStationEntry);
        if ((!isAddMode()) && (checkAlarmConflict(internetRadioStationListToCheck, internetRadioStationEntry))) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conflict", "Alarm time conflicting !!!"));
            return;
        }

        if (isAddMode()) {
            internetRadioStationList.add(internetRadioStationEntry);
        } else {
            int index = internetRadioStationList.indexOf(internetRadioStationEntry);
            internetRadioStationList.remove(index);
            internetRadioStationList.add(index, internetRadioStationEntry);
            clearSelection();
        }

        setInternetRadioStationEntry(new InternetRadioStation());
    }

    private boolean checkDuplicate() {
        return internetRadioStationList.stream().anyMatch(
                (InternetRadioStation entry) -> entry.getStation().equals(internetRadioStationEntry.getStation()) && entry.getUri().equals(internetRadioStationEntry.getUri())
        );
    }

    private static boolean checkAlarmConflict(List<InternetRadioStation> alarmListToCheck, InternetRadioStation alarmEntryToCheck) {
        return alarmListToCheck.stream()
                .filter((InternetRadioStation entry)
                        -> alarmEntryToCheck.getStation().equals(entry.getStation()) && alarmEntryToCheck.getUri().equals(entry.getUri()))
                .findAny().isPresent();
    }

    public void saveAlarmList() throws IOException {
        preferencesBean.getPreferences().setStations(internetRadioStationList);
        preferencesBean.savePreferences();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful", "Alarm List Saved"));
    }

    public void deleteAlarm() {
        if (selectedInternetRadioStationEntry != null) {
            internetRadioStationList.remove(selectedInternetRadioStationEntry);
            internetRadioStationEntry = new InternetRadioStation();
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
}
