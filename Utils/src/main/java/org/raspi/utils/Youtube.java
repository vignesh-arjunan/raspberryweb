/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.raspi.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.raspi.execute.ProcessExecutor;
import org.raspi.execute.ProcessExecutor.Flags;

/**
 *
 * @author vignesh
 */
public class Youtube {

    public static String getStreamingURI(String youtubeURL) throws IOException {
        List<String> commands = new ArrayList<>();
        commands.add("/usr/local/bin/youtube-dl");
        commands.add("-g");
        commands.add(youtubeURL);
        ProcessExecutor processExecutor = new ProcessExecutor(commands);
        Flags ifconfigFlags = processExecutor.startExecution();
        if (ifconfigFlags.getErrMsg().length() > 0) {
            throw new IllegalArgumentException("URL invalid " + youtubeURL);
        }
        return ifconfigFlags.getInputMsg();
    }
    
     public void killAllYoutubeDl() throws IOException {
        // killing already running players 
        List<String> commands = new ArrayList<>();
        commands.add("/usr/bin/killall");
        commands.add("-e");
        commands.add("youtube-dl");
        ProcessExecutor killer = new ProcessExecutor(commands);
        killer.startExecutionNonBlocking();
        
        commands.clear();
        commands.add("/bin/rm");    
        commands.add("-f");
        commands.add("/home/pi/*.part");
        ProcessExecutor partFileRemoves = new ProcessExecutor(commands);
        partFileRemoves.startExecutionNonBlocking();
    }

    private ProcessExecutor processExecutor;

    public String download(String youtubeURL) throws IOException {
        List<String> commands = new ArrayList<>();
        commands.add("/usr/local/bin/youtube-dl");
        commands.add(youtubeURL);
        processExecutor = new ProcessExecutor(commands);
        Flags ifconfigFlags = processExecutor.startExecution();
        if (ifconfigFlags.getErrMsg().length() > 0) {
            throw new IllegalArgumentException("URL invalid " + youtubeURL);
        }
        return ifconfigFlags.getInputMsg();
    }

    public ProcessExecutor getProcessExecutor() {
        return processExecutor;
    }
}
