package org.raspi.utils;

import org.raspi.execute.ProcessExecutor;
import org.raspi.execute.ProcessExecutor.Flags;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;

/**
 *
 * @author vignesh
 */
public class CheckNetworkAndRebootOrUpdate {

    public static void check(String msg, String email, String password, SendEmail.MailProvider mailProvider) throws IOException, MessagingException {
        // create the ping command as a list of strings
        List<String> commands = new ArrayList<>();
        commands.add("/bin/ping");
        commands.add("-c");
        commands.add("5");
        commands.add("www.google.com");
        ProcessExecutor ping = new ProcessExecutor(commands);
        Flags flags = ping.startExecution();

        if (flags.isReadError()) {
            commands = new ArrayList<>();
            commands.add("/usr/bin/sudo");
            commands.add("/sbin/reboot");
            ProcessExecutor shutdown = new ProcessExecutor(commands);
            Flags shutdownFlags = shutdown.startExecution();
            System.out.println("shutdownFlags.isReadError() " + shutdownFlags.isReadError());
        } else {
            commands = new ArrayList<>();
            commands.add("/sbin/ifconfig");
            ProcessExecutor ifconfig = new ProcessExecutor(commands);
            Flags ifconfigFlags = ifconfig.startExecution();
            System.out.println("ifconfigFlags.isReadError() " + ifconfigFlags.isReadError());
            SendEmail.send(email, password, email, email, msg, ifconfigFlags.getInputMsg(), null, mailProvider);
        }
    }

    public CheckNetworkAndRebootOrUpdate() {
    }
}
