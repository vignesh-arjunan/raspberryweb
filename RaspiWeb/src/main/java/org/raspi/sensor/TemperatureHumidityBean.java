package org.raspi.sensor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.raspi.utils.Constants;
import org.raspi.utils.TempHumidReader;

/**
 *
 * @author vignesh
 */
@Named
@Singleton
@Startup
public class TemperatureHumidityBean {

//    private Config config;
    private HazelcastInstance hazelcastInstance;
    private ITopic<Float> temperatureTopic;
    private ITopic<Float> humidityTopic;
    private ITopic<String> motionTopic;
    private Float temperature;
    private Float humidity;

    public Float getTemperature() {
        return temperature;
    }

    public Float getHumidity() {
        return humidity;
    }

    public ITopic<String> getMotionTopic() {
        return motionTopic;
    }

//    @PostConstruct
    void init() {

        Context ctx;
        try {
            ctx = new InitialContext();
            hazelcastInstance = (HazelcastInstance) ctx.lookup(Constants.HAZELCAST_CONTEXT);
            temperatureTopic = hazelcastInstance.getTopic(Constants.TEMPERATURE_TOPIC);
            humidityTopic = hazelcastInstance.getTopic(Constants.HUMIDITY_TOPIC);
            motionTopic = hazelcastInstance.getTopic(Constants.MOTION_TOPIC);
        } catch (NamingException ex) {
            Logger.getLogger(TemperatureHumidityBean.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String generateNotificationMsg(String msg) {
        if (getTemperature() != null && getHumidity() != null) {
            return msg + (". Temperature : " + getTemperature() + " C. Humidity : " + getHumidity() + " %.");
        }
        return msg;
    }

//    @Schedule(second = "0", minute = "*/1", hour = "*", info = "NotificationBean Timer 4", persistent = false)
    public void readTempHumidity() {
        TempHumidReader tempHumidReader;
        try {
            tempHumidReader = new TempHumidReader();
            temperature = tempHumidReader.getCelcius();
            humidity = tempHumidReader.getHumidity();
            temperatureTopic.publish(temperature);
            humidityTopic.publish(humidity);

        } catch (Exception exception) {
            Logger.getLogger(TemperatureHumidityBean.class.getName()).log(Level.SEVERE, null, exception);
        }
    }

}
