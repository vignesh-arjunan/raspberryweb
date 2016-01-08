package org.raspi.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.raspi.execute.ProcessExecutor;

/**
 *
 * @author vignesh
 */
public class Motion {

    public static void start() throws IOException {
        try {
            killAllMotion();
            // invoking motion
            List<String> commands = new ArrayList<>();
            commands.add("/usr/bin/motion");
            commands.add("-c");
            commands.add("/etc/motion/motion.conf");
            ProcessExecutor motion = new ProcessExecutor(commands);
            motion.startExecutionNonBlocking();
        } catch (IOException ex) {
            Logger.getLogger(Motion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void killAllMotion() throws IOException {
        // killing already running motion process
        List<String> commands = new ArrayList<>();
        commands.add("/usr/bin/killall");
        commands.add("-e");
        commands.add("motion");
        ProcessExecutor killer = new ProcessExecutor(commands);
        killer.startExecution();
    }
}
