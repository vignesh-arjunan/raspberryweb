package org.raspi.utils;

import org.raspi.execute.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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
    private String videoURI;
    private ProcessExecutor player;
    private static MediaPlayer mediaPlayer;

    public static boolean isPlayingVideo() {
        if (mediaPlayer == null) {
            return false;
        }
        return mediaPlayer.getPlayer().getProcess().isAlive() && mediaPlayer.isVideo();
    }

    public MediaPlayer(File file) {
        Objects.requireNonNull(file, "File cannot be null");
        toBePlayed = file;
    }

    public MediaPlayer(String videoURL) {
        Objects.requireNonNull(videoURL, "videoURL cannot be null");
        this.videoURI = videoURL;
    }

    public File getToBePlayed() {
        return toBePlayed;
    }

    public boolean isVideo() {
        if (videoURI != null) {
            return true;
        }
        return Stream.of(Constants.MediaFormat.values())
                .filter(format -> toBePlayed.getAbsolutePath().toUpperCase().endsWith(format.name()))
                .findAny().get().isHasVideo();
    }

    public boolean isPlaying() {
        return player.getProcess().isAlive();
    }

    public ProcessExecutor getPlayer() {
        return player;
    }

    public void play(boolean async) throws IOException {
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
            System.out.println("videoURI " + videoURI);
            commands.add(videoURI.trim());
        }

        commands.forEach(cmd -> System.out.println("command " + cmd));

        if (isVideo()) {
            HDMIControl.setHDMIActive(true);
        }
        mediaPlayer = this;

        player = new ProcessExecutor(commands);
        if (async) {
            player.startExecutionNonBlocking();
            return;
        }
        player.startExecution();
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
