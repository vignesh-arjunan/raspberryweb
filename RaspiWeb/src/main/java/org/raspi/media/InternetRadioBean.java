package org.raspi.media;

import java.io.File;
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
import static org.raspi.utils.Constants.PARENT_MEDIA_DIR;
import org.raspi.utils.InternetRadioStation;
import org.raspi.utils.MediaPlayer;

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

    public boolean isAddMode() {
        return addMode;
    }

    public void setAddMode(boolean addMode) {
        this.addMode = addMode;
    }

    public InternetRadioStation getSelectedInternetRadioStationEntry() {
        return selectedInternetRadioStationEntry;
    }

    public void setSelectedInternetRadioStationEntry(InternetRadioStation selectedStationEntry) {
        this.selectedInternetRadioStationEntry = selectedStationEntry;
        internetRadioStationEntry = new InternetRadioStation();
        setAddMode(false);
        if (selectedStationEntry != null) {
            internetRadioStationEntry.setStation(selectedStationEntry.getStation());
            internetRadioStationEntry.setUri(selectedStationEntry.getUri());
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
            setSelectedInternetRadioStationEntry(null);
            internetRadioStationEntry = new InternetRadioStation();
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Please select", "Please select an Station"));
        }
        setAddMode(true);
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

    public void clearAllStations() {
        internetRadioStationList.clear();
        internetRadioStationEntry = new InternetRadioStation();
        setSelectedInternetRadioStationEntry(null);
        setAddMode(true);
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
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "URL cannot be empty"));
            return;
        }

        try {
            new URI(internetRadioStationEntry.getUri().trim());
        } catch (URISyntaxException ex) {
            Logger.getLogger(InternetRadioBean.class.getName()).log(Level.SEVERE, null, ex);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "URL not valid"));
            return;
        }

        internetRadioStationEntry.setStation(internetRadioStationEntry.getStation().trim());
        internetRadioStationEntry.setUri(internetRadioStationEntry.getUri().trim());

        if (isAddMode() && checkDuplicateStation()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conflict", "Station / URL conflicting !!!"));
            return;
        }

        List<InternetRadioStation> internetRadioStationListToCheck = new ArrayList<>(internetRadioStationList);
        internetRadioStationListToCheck.remove(internetRadioStationEntry);
        if ((!isAddMode()) && (checkStationConflict(internetRadioStationListToCheck, internetRadioStationEntry))) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conflict", "Station / URL conflicting !!!"));
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

    private boolean checkDuplicateStation() {
        return internetRadioStationList.stream()
                .anyMatch(
                        (InternetRadioStation entry) -> (entry.getUri().equals(internetRadioStationEntry.getUri()) || entry.getStation().equals(internetRadioStationEntry.getStation()))
                );
    }

    private static boolean checkStationConflict(List<InternetRadioStation> internetRadioStationListToCheck, InternetRadioStation internetRadioStationEntryToCheck) {
        return internetRadioStationListToCheck.stream()
                .filter((InternetRadioStation entry)
                        -> internetRadioStationEntryToCheck.getStation().equals(entry.getStation()) || internetRadioStationEntryToCheck.getUri().equals(entry.getUri()))
                .findAny().isPresent();
    }

    public void saveStationList() throws IOException {
        preferencesBean.getPreferences().setStations(internetRadioStationList);
        preferencesBean.savePreferences();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful", "Station List Saved"));
    }

    public void deleteStation() {
        if (selectedInternetRadioStationEntry != null) {
            internetRadioStationList.remove(selectedInternetRadioStationEntry);
            internetRadioStationEntry = new InternetRadioStation();
            setSelectedInternetRadioStationEntry(null);
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Please select", "Please select an Station"));
        }
        setAddMode(true);
    }

    public void playStation() {
        if (selectedInternetRadioStationEntry != null) {
            try {
                mediaBean.setMediaPlayer(new MediaPlayer(selectedInternetRadioStationEntry.getUri()));
                mediaBean.getMediaPlayer().play(true);
            } catch (IOException ex) {
                Logger.getLogger(InternetRadioBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Please select", "Please select an Station"));
            setAddMode(true);
        }
    }

    public void saveWhatWhen() throws IOException {
        preferencesBean.savePreferences();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful", "Preferences Saved"));
    }
}
