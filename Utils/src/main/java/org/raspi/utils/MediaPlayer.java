package org.raspi.utils;

import org.raspi.execute.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 * @author vignesh
 */
public class MediaPlayer {

    private File toBePlayed;
    private String URL;
    private ProcessExecutor player;
    private static MediaPlayer mediaPlayer;

    public static boolean isPlaying() {
        if (mediaPlayer == null) {
            return false;
        }
        synchronized (mediaPlayer) {
            return mediaPlayer.getPlayer().getProcess().isAlive();
        }
    }

    public MediaPlayer(File file) {
        Objects.requireNonNull(file, "File cannot be null");
        toBePlayed = file;
    }

    public MediaPlayer(String URL) {
        Objects.requireNonNull(URL, "URL cannot be null");
        this.URL = URL;
    }

    public File getToBePlayed() {
        return toBePlayed;
    }

    public ProcessExecutor getPlayer() {
        return player;
    }

    public synchronized void play(boolean async) throws IOException {
        if (toBePlayed != null) {
            Stream.of(Constants.MediaFormat.values())
                    .filter(format -> toBePlayed.getAbsolutePath().toUpperCase().endsWith(format.name()))
                    .findAny()
                    .orElseThrow(() -> {
                        return new IllegalArgumentException("Only mp3, mp4, flv, mov and avi files allowed");
                    });
        }
        killAllPlayers();
        // invoking player
        List<String> commands = new ArrayList<>();
        commands.add("/usr/bin/omxplayer");
        commands.add("-o");
        commands.add("both");
        if (toBePlayed != null) {
            commands.add(toBePlayed.getAbsolutePath());
        } else {
            System.out.println("videoURI " + URL);
            commands.add(URL.trim());
        }

        commands.forEach(cmd -> System.out.println("command " + cmd));

        mediaPlayer = this;

        player = new ProcessExecutor(commands);
        if (async) {
            player.startExecutionNonBlocking();
        } else {
            player.startExecution();
        }
    }

    public static void killAllPlayers() throws IOException {
        killAllOmxPlayer();
        killAllMplayer();
    }

    private static void killAllOmxPlayer() throws IOException {
        // killing already running players 
        List<String> commands = new ArrayList<>();
        commands.add("/usr/bin/killall");
        commands.add("-e");
        commands.add("omxplayer.bin");
        ProcessExecutor killer = new ProcessExecutor(commands);
        killer.startExecutionNonBlocking();
    }

    private static void killAllMplayer() throws IOException {
        // killing already running players 
        List<String> commands = new ArrayList<>();
        commands.add("/usr/bin/killall");
        commands.add("-e");
        commands.add("mplayer");
        ProcessExecutor killer = new ProcessExecutor(commands);
        killer.startExecutionNonBlocking();
    }

    public static void main(String args[]) throws IOException {
        MediaPlayer mediaPlayer1 = new MediaPlayer(new File("/home/pi/AzhaikiraanMadhavan.mp3"));
        mediaPlayer1.play(true);
        while (mediaPlayer1.isPlaying()) {
            System.out.println("Playing");
        }
    }
}
