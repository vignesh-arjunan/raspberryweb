package org.raspi.utils;

import java.io.File;

/**
 *
 * @author vignesh
 */
public class Constants {

    public enum MediaFormat {

        MP3(false), MP4(true), FLV(true), MOV(true), AVI(true);
        private final boolean hasVideo;

        private MediaFormat(boolean hasVideo) {
            this.hasVideo = hasVideo;
        }

        public boolean isHasVideo() {
            return hasVideo;
        }

    }

    public static final File PARENT_MEDIA_DIR = new File("/home/pi");
    public static final File MOTION_DIR = new File("/tmp/motion");
    public static final File PREFERENCES_FILE = new File("/home/pi/Preferences");
    public static final String EVENT_STARTED = "event_started";
    public static final String EVENT_MOTION_DETECTED = "event_motion_detected";
    public static final String EVENT_ENDED = "event_ended";
    public static final String HAZELCAST_CONTEXT = "payara/Hazelcast";
    public static final String TEMPERATURE_TOPIC = "temperature_topic";
    public static final String HUMIDITY_TOPIC = "humidity_topic";
    public static final String MOTION_TOPIC = "motion_topic";
    public static final String SOURCE = "https://bitbucket.org/vignesh_arjunan/raspberryweb/downloads/RaspiWeb.war";
    public static final File DESTINATION = new File("/etc/glassfish4/glassfish/domains/domain1/autodeploy/RaspiWeb.war");
//    public static final File DESTINATION = new File("/Users/vignesh/RaspiWeb.war");    
    public static final File TEMP = new File("/tmp/RaspiWeb.war");
}
