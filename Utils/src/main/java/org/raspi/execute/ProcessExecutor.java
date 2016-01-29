package org.raspi.execute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author vignesh
 */
public class ProcessExecutor {

    private final List<String> commands;
    private Process process;
    private Flags flags;
    private boolean started;
    private boolean blocking;

    public ProcessExecutor(List<String> command) {
        Objects.requireNonNull(command, "command cannot be null");
        this.commands = command;
    }

    public void startExecutionNonBlocking() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(commands);
        System.out.println("commands");
        commands.forEach(System.out::print);
        System.out.println();
        process = pb.start();
        started = true;
        blocking = false;
    }

    public Process getProcess() {
        if (process == null) {
            throw new IllegalStateException("Process not started yet");
        }
        return process;
    }

    public OutputStream getOutputStream() {
        return process.getOutputStream();
    }

    public Flags getFlags() {
        return flags;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public Flags startExecution() throws IOException {

        ProcessBuilder pb = new ProcessBuilder(commands);
        System.out.println("commands");
        commands.forEach(System.out::print);
        System.out.println();
        process = pb.start();
        flags = new Flags();
        started = true;
        blocking = true;

        try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            // read the output from the command       
            System.out.println("Here is the standard output of the command:\n");
            String s;
            while ((s = stdInput.readLine()) != null) {
                System.out.println("----" + s);
                flags.appendInputMsg(s + "\n");
                flags.setReadInput(true);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.err.println("xxxxx" + s);
                flags.appendErrMsg(s + "\n");
                flags.setReadError(true);
            }
        }
        return flags;
    }

    public static class Flags {

        private boolean readInput;
        private String inputMsg = "";
        private String lastInputMsg = "";
        private boolean readError;
        private String errMsg = "";
        private String lastErrMsg = "";

        public boolean isReadInput() {
            return readInput;
        }

        public void setReadInput(boolean readInput) {
            this.readInput = readInput;
        }

        public boolean isReadError() {
            return readError;
        }

        public void setReadError(boolean readError) {
            this.readError = readError;
        }

        public String getInputMsg() {
            return inputMsg;
        }

        public void appendInputMsg(String inputMsg) {
            this.inputMsg = getInputMsg() + inputMsg;
            this.lastInputMsg = inputMsg;
        }

        public String getErrMsg() {
            return errMsg;
        }

        public void appendErrMsg(String errMsg) {
            this.errMsg = getErrMsg() + errMsg;
            this.lastErrMsg = errMsg;
        }

        public String getLastInputMsg() {
            return lastInputMsg;
        }

        public void setLastInputMsg(String lastInputMsg) {
            this.lastInputMsg = lastInputMsg;
        }

        public String getLastErrMsg() {
            return lastErrMsg;
        }

        public void setLastErrMsg(String lastErrMsg) {
            this.lastErrMsg = lastErrMsg;
        }
    }

}
