package org.raspi.media;

/**
 *
 * @author vignesh
 */
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.raspi.timer.PreferencesBean;
import org.raspi.utils.FileValidator;

@Named
@ApplicationScoped
public class PlayListBean {

    private List<File> playList;
    private File selectedFile;
    @Inject
    private PreferencesBean preferencesBean;

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    public List<File> getPlayList() {
        return playList;
    }

    public void clearPlayList() {
        System.out.println("Clearing PlayList");
        playList.clear();
    }

    public void savePlayList() throws IOException {
        preferencesBean.getPreferences().setPlayList(playList);
        preferencesBean.savePreferences();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful", "Play List Saved"));
    }

    @PostConstruct
    public void init() {
        playList = preferencesBean.getPreferences().getPlayList();

    }

    public void deleteSelectedFile() {
        if (!FileValidator.validateFile(selectedFile)) {
            return;
        }
        playList.remove(selectedFile);
    }

    public void upSelectedFile() {
        if (!FileValidator.validateFile(selectedFile)) {
            return;
        }
        int prevIndex = playList.indexOf(selectedFile);
        if (prevIndex == 0) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Reached Top", "Reached Top of the List"));
            return;
        }
        playList.remove(selectedFile);
        playList.add(prevIndex - 1, selectedFile);
    }

    public void downSelectedFile() {
        if (!FileValidator.validateFile(selectedFile)) {
            return;
        }
        int prevIndex = playList.indexOf(selectedFile);
        if (prevIndex == playList.size() - 1) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Reached Bottom", "Reached Bottom of the List"));
            return;
        }
        playList.remove(selectedFile);
        playList.add(prevIndex + 1, selectedFile);
    }
}
