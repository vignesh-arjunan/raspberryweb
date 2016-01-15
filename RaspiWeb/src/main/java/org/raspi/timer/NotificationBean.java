package org.raspi.timer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import org.raspi.motion.MotionBean;
import org.raspi.utils.SendEmail;
import org.raspi.utils.SendEmail.MailProvider;
import org.raspi.motion.WatcherBean;
import org.raspi.sensor.TemperatureHumidityBean;
import org.raspi.utils.CheckNetworkAndRebootOrNotify;

/**
 *
 * @author vignesh
 */
@Named
@DependsOn({"WatcherBean", "MotionBean"})
@Startup
@Singleton
public class NotificationBean {

    private String when;
    private MailProvider mailProvider;
    private String email;
    private String password;
    private String notificationMsg;
    @Inject
    private WatcherBean watcherBean;
    @Inject
    private PreferencesBean preferencesBean;
    @Inject
    private MotionBean motionBean;
    @Inject
    private TemperatureHumidityBean temperaureHumidityBean;

    public MailProvider getGoogle() {
        return MailProvider.GOOGLE;
    }

    public MailProvider getYahoo() {
        return MailProvider.YAHOO;
    }

    public MailProvider getMicrosoft() {
        return MailProvider.MICROSOFT;
    }

    public String getNotificationMsg() {
        return notificationMsg;
    }

    public void setNotificationMsg(String notificationMsg) {
        this.notificationMsg = notificationMsg;
    }

    public MailProvider getMailProvider() {
        return mailProvider;
    }

    public void setMailProvider(MailProvider mailProvider) {
        System.out.println("mailProvider : " + mailProvider);
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

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        System.out.println("when " + when);
        this.when = when;
    }

    @PostConstruct
    void init() {
        when = preferencesBean.getPreferences().getNotificationWhen();
        mailProvider = preferencesBean.getPreferences().getMailProvider();
        email = preferencesBean.getPreferences().getEmail();
        password = preferencesBean.getPreferences().getPassword();
        notificationMsg = preferencesBean.getPreferences().getNotificationMsg();
    }

    public void savePreferences() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            SendEmail.send(email, password, email, email, "Congrats, Email setup working !!!", "Your Raspberry Pi is sending Mails successfully !!", null, mailProvider);
        } catch (MessagingException | IOException ex) {
            Logger.getLogger(NotificationBean.class.getName()).log(Level.SEVERE, null, ex);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Email Password Authentication Failed"));
            return;
        }
        preferencesBean.getPreferences().setStartMotionOnStartup(motionBean.isStartMotionOnStartup());
        preferencesBean.getPreferences().setNotificationWhen(when);
        preferencesBean.getPreferences().setMailProvider(mailProvider);
        preferencesBean.getPreferences().setEmail(email);
        preferencesBean.getPreferences().setPassword(password);
        preferencesBean.getPreferences().setNotificationMsg(notificationMsg);
        preferencesBean.getPreferences().setEmailPassswordVerified(true);
        preferencesBean.getPreferences().setNotifyEventStart(watcherBean.isNotifyEventStart());
        preferencesBean.getPreferences().setNotifyAttachment(watcherBean.isNotifyAttachment());
        preferencesBean.savePreferences();
        context.addMessage(null, new FacesMessage("Successful", "Preferences Saved"));
    }

    @Schedule(second = "0", minute = "*/15", hour = "*", info = "NotificationBean Timer 1", persistent = false)
    public void fifteenMinuteTimeout() {
        if (Integer.parseInt(when) == 15 && preferencesBean.getPreferences().isEmailPassswordVerified()) {
            watcherBean.pushNotification(() -> {
                try {
                    CheckNetworkAndRebootOrNotify.check(temperaureHumidityBean.generateNotificationMsg(notificationMsg), email, password, mailProvider);
                } catch (MessagingException | IOException ex) {
                    Logger.getLogger(NotificationBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    @Schedule(second = "0", minute = "*/30", hour = "*", info = "NotificationBean Timer 2", persistent = false)
    public void thirtyMinuteTimeout() {
        if (Integer.parseInt(when) == 30 && preferencesBean.getPreferences().isEmailPassswordVerified()) {
            watcherBean.pushNotification(() -> {
                try {
                    CheckNetworkAndRebootOrNotify.check(temperaureHumidityBean.generateNotificationMsg(notificationMsg), email, password, mailProvider);
                } catch (MessagingException | IOException ex) {
                    Logger.getLogger(NotificationBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    @Schedule(second = "0", minute = "0", hour = "*", info = "NotificationBean Timer 3", persistent = false)
    public void hourTimeout() {
        if (Integer.parseInt(when) == 0 && preferencesBean.getPreferences().isEmailPassswordVerified()) {
            watcherBean.pushNotification(() -> {
                try {
                    CheckNetworkAndRebootOrNotify.check(temperaureHumidityBean.generateNotificationMsg(notificationMsg), email, password, mailProvider);
                } catch (MessagingException | IOException ex) {
                    Logger.getLogger(NotificationBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }
}
