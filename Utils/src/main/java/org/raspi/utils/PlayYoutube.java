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
public class PlayYoutube {

    public static String read(String youtubeURL) throws IOException {
        List<String> commands = new ArrayList<>();
        commands.add("/usr/bin/youtube-dl");
        commands.add("-g");
        commands.add(youtubeURL);
        ProcessExecutor ifconfig = new ProcessExecutor(commands);
        Flags ifconfigFlags = ifconfig.startExecution();
        if (ifconfigFlags.getErrMsg().length() > 0) {
            throw new IllegalArgumentException("URL invalid " + youtubeURL);
        }
        return ifconfigFlags.getInputMsg();
    }

}
