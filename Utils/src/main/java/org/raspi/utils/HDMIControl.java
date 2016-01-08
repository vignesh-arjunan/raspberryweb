/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class HDMIControl {
    public static boolean HDMIActive = true; 
    public static void setHDMIActive(boolean active) {
        HDMIActive = active;
        List<String> commands = new ArrayList<>();
        commands.add("/opt/vc/bin/tvservice");
        if (!active) {
            commands.add("-o"); // off
        } else {
            commands.add("-p"); // on
        }
        ProcessExecutor killer = new ProcessExecutor(commands);
        try {
            killer.startExecution();
        } catch (IOException ex) {
            Logger.getLogger(HDMIControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
