/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.raspi.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.raspi.execute.ProcessExecutor;
import org.raspi.execute.ProcessExecutor.Flags;

/**
 *
 * @author vignesh
 */
public class TempHumidReader {

    private final Float celcius;
    private final Float humidity;

    public TempHumidReader() throws IOException {
        List<String> commands = new ArrayList<>();
        commands.add("/usr/bin/sudo");
        commands.add("/home/pi/Adafruit_Python_DHT/examples/AdafruitDHT.py");
        commands.add("22");
        commands.add("4");
        ProcessExecutor ifconfig = new ProcessExecutor(commands);
        Flags ifconfigFlags = ifconfig.startExecution();
        if (ifconfigFlags.getErrMsg().length() > 0) {
            throw new IllegalStateException("No Data Available");
        }
        StringTokenizer st = new StringTokenizer(ifconfigFlags.getInputMsg(), "\n");
        st.nextToken();
        celcius = Float.parseFloat(st.nextToken());
        humidity = Float.parseFloat(st.nextToken());
    }

    public Float getCelcius() {
        return celcius;
    }

    public Float getHumidity() {
        return humidity;
    }

}
