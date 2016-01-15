package org.raspi.update;

import java.io.IOException;
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
import org.raspi.utils.CheckNetworkAndRebootOrNotify;
import static org.raspi.utils.Constants.DESTINATION;
import static org.raspi.utils.Constants.SOURCE;
import org.raspi.utils.UpdateManager;

/**
 *
 * @author vignesh
 */
@Named
@Singleton
@Startup
public class UpdateBean {

    @Inject
    private PreferencesBean preferencesBean;
    private boolean updateSoftwareAutomatically;
    private boolean updateAvailable;
    private Integer progress = 0;

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public boolean isUpdateSoftwareAutomatically() {
        return updateSoftwareAutomatically;
    }

    public void setUpdateSoftwareAutomatically(boolean updateSoftwareAutomatically) {
        this.updateSoftwareAutomatically = updateSoftwareAutomatically;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
    }

    @PostConstruct
    void init() {
        updateSoftwareAutomatically = preferencesBean.getPreferences().isUpdateSoftwareAutomatically();
    }

    @PreDestroy
    void destroy() {

    }

    public void savePreferences() throws IOException {
        preferencesBean.getPreferences().setUpdateSoftwareAutomatically(updateSoftwareAutomatically);
        preferencesBean.savePreferences();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful", "Updated Preferences Saved"));
    }

    @Schedule(second = "0", minute = "0", hour = "*", info = "Update Timer", persistent = false)
    public void checkUpdateAvailable() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            UpdateManager updateManager = new UpdateManager(SOURCE, DESTINATION, DESTINATION);
            updateAvailable = updateManager.isUpdateAvailable();
            if (updateAvailable && updateSoftwareAutomatically) {
                updateSoftware();
            }
        } catch (IOException ex) {
            Logger.getLogger(UpdateBean.class.getName()).log(Level.SEVERE, null, ex);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Could not check for Updates"));
        }
    }

    public void updateSoftware() {
        FacesContext context = FacesContext.getCurrentInstance();
        System.out.println("in update software");
        UpdateManager updateManager;
        try {
            updateManager = new UpdateManager(SOURCE, DESTINATION, DESTINATION);
        } catch (IOException ex) {
            Logger.getLogger(UpdateBean.class.getName()).log(Level.SEVERE, null, ex);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Could not update software"));
            return;
        }

        new Thread(() -> {
            try {
                if (updateManager.isUpdateAvailable()) {
                    updateManager.update((progress1) -> {
                        this.setProgress(progress1);
                        System.out.print("progress " + progress1);
                    });
                    CheckNetworkAndRebootOrNotify.reboot();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();
    }

}
