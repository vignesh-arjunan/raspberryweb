package org.raspi.timer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.raspi.media.MediaBean;
import static org.raspi.utils.Constants.PREFERENCES_FILE;
import org.raspi.utils.Preferences;

/**
 *
 * @author vignesh
 */
@Startup
@Singleton
public class PreferencesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Preferences preferences = new Preferences();
    private final ObjectMapper mapper = new ObjectMapper();

    public Preferences getPreferences() {
        return preferences;
    }

    @PostConstruct
    public void init() {
        if (PREFERENCES_FILE.exists()) {
            try {
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                preferences = mapper.readValue(PREFERENCES_FILE, Preferences.class);
                preferences.getAlarmList().stream().forEach((entry) -> {
                    entry.setAlarmTime(LocalTime.of(entry.getHours(), entry.getMins()));
                });
            } catch (IOException ex) {
                Logger.getLogger(MediaBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void savePreferences() throws FileNotFoundException, IOException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(PREFERENCES_FILE, preferences);
    }

//    public static void main(String[] args) throws IOException {
//        PreferencesBean preferencesBean = new PreferencesBean();
//        preferencesBean.init();
//    }
}
