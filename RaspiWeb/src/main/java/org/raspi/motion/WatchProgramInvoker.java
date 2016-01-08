package org.raspi.motion;

import org.raspi.utils.SendEmail;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

/**
 *
 * @author vignesh
 */
public class WatchProgramInvoker {

    static void usage() {
        System.err.println("usage: java WatchDir [-r] dir");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        // parse arguments
        if (args.length == 0 || args.length > 2) {
            usage();
        }
        boolean recursive = false;
        int dirArg = 0;
        if (args[0].equals("-r")) {
            if (args.length < 2) {
                usage();
            }
            recursive = true;
            dirArg++;
        }

        // register directory and process its events
        File file = new File("/tmp/WatcherStarted");
        System.out.println("created tmp file " + file.createNewFile());
        Path dir = Paths.get(args[dirArg]);
        WatchDir watchDir = new WatchDir(dir, recursive);
        watchDir.startNotificationThread();
        watchDir.subject((s1) -> {
            try {
                SendEmail.send("USERNAME", "PASSWORD", "USERNAME", "USERNAME", s1, "attachment coming shortly", null, SendEmail.MailProvider.GOOGLE);
            } catch (MessagingException | IOException ex) {
                Logger.getLogger(WatchProgramInvoker.class.getName()).log(Level.SEVERE, null, ex);
            }
        },
                (s, f) -> {
                    try {
                        SendEmail.send("USERNAME", "PASSWORD", "USERNAME", "USERNAME", s, "PFA", f, SendEmail.MailProvider.GOOGLE);
                    } catch (MessagingException | IOException ex) {
                        Logger.getLogger(WatchProgramInvoker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }, () -> {
                });

    }
}
