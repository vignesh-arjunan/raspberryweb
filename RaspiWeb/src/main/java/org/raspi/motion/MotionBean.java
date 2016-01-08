package org.raspi.motion;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.raspi.timer.PreferencesBean;
import org.raspi.utils.Motion;

/**
 *
 * @author vignesh
 */
@Named
@DependsOn("PreferencesBean")
@Singleton
@Startup
public class MotionBean {

    private boolean startMotionOnStartup;
    @Inject
    private PreferencesBean preferencesBean;

    public boolean isStartMotionOnStartup() {
        return startMotionOnStartup;
    }

    public void setStartMotionOnStartup(boolean startMotionOnStartup) {
        System.out.println("startMotionOnStartup " + startMotionOnStartup);
        this.startMotionOnStartup = startMotionOnStartup;
    }

    @PostConstruct
    void init() {
        startMotionOnStartup = preferencesBean.getPreferences().isStartMotionOnStartup();
        if (startMotionOnStartup) {
            try {
                Motion.start();
            } catch (IOException ex) {
                Logger.getLogger(MotionBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void startMotion() throws IOException {
        Motion.start();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful", "Motion Started"));
    }

    public void stopMotion() throws IOException {
        Motion.killAllMotion();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful", "Motion Stopped"));
    }
}
