package org.raspi.sensor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.raspi.utils.Constants;

/**
 *
 * @author vignesh
 */
@Named
@Singleton
@Startup
public class PIRMotionBean {

    private HazelcastInstance hazelcastInstance;
    private ITopic<String> motionTopic;
    // create gpio controller
    private GpioController gpio;

//    @PostConstruct
    void init() {

        Context ctx;
        try {
            ctx = new InitialContext();
            hazelcastInstance = (HazelcastInstance) ctx.lookup(Constants.HAZELCAST_CONTEXT);
            motionTopic = hazelcastInstance.getTopic(Constants.MOTION_TOPIC);
        } catch (NamingException ex) {
            Logger.getLogger(TemperatureHumidityBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        gpio = GpioFactory.getInstance();
        // provision gpio pin #01 as an input pin with its internal pull down resistor enabled
        GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01, PinPullResistance.PULL_DOWN);
        myButton.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
            // display pin state on console            
            Logger.getLogger(PIRMotionBean.class.getName()).log(Level.INFO, " --> GPIO PIN STATE CHANGE: {0} = {1}, timeout = {2}", new Object[]{event.getPin(), event.getState()});
            if (event.getState() == PinState.HIGH) {
                motionTopic.publish("Motion Detected");
            }
        });
    }

//    @PreDestroy
    void destroy() {
        gpio.shutdown();
    }
}
