package org.raspi.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.raspi.execute.ProcessExecutor;
import static org.raspi.utils.Constants.USB_MEDIA_DIR;

/**
 *
 * @author vignesh
 */
public class MountUSB {

    public static void mount() throws IOException {
        try {
            // invoking mount
            List<String> commands = new ArrayList<>();
            commands.add("/bin/mount");
            commands.add("-t");
            commands.add("vfat");
            commands.add("/dev/sda1");
            commands.add(USB_MEDIA_DIR.getAbsolutePath());
            ProcessExecutor mount = new ProcessExecutor(commands);
            mount.startExecution();
        } catch (IOException ex) {
            Logger.getLogger(MountUSB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
