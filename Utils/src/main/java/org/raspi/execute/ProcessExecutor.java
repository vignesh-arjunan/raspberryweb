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

    final List<String> command;
    private Process process;

    public ProcessExecutor(List<String> command) {
        Objects.requireNonNull(command, "command cannot be null");
        this.command = command;
    }

    public void startExecutionNonBlocking() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        process = pb.start();
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

    public Flags startExecution() throws IOException {

        ProcessBuilder pb = new ProcessBuilder(command);
        process = pb.start();
        Flags flags = new Flags();

        try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            // read the output from the command       
            System.out.println("Here is the standard output of the command:\n");
            String s;
            while ((s = stdInput.readLine()) != null) {
                System.out.println("----" + s);
                flags.setInputMsg(flags.getInputMsg() + s + "\n");
                flags.setReadInput(true);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.err.println("xxxxx" + s);
                flags.setErrMsg(flags.getErrMsg() + s + "\n");
                flags.setReadError(true);
            }
        }
        return flags;
    }

    public static class Flags {

        private boolean readInput;
        private String inputMsg = "";
        private boolean readError;
        private String errMsg = "";

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

        public void setInputMsg(String inputMsg) {
            this.inputMsg = inputMsg;
        }

        public String getErrMsg() {
            return errMsg;
        }

        public void setErrMsg(String errMsg) {
            this.errMsg = errMsg;
        }

    }

}
