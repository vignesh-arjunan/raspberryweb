package org.raspi.motion;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.raspi.utils.Constants.EVENT_ENDED;
import static org.raspi.utils.Constants.EVENT_MOTION_DETECTED;

/**
 *
 * @author vignesh
 */
public class WatchDir {

    /**
     * Example to watch a directory (or tree) for changes to files.
     */
    private final Path toWatch;
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(30);
    private final boolean recursive;
    private boolean trace = false;
    private Path fileJustCreated;
    private boolean stopThread;
    private Thread notificationThread;

    @SuppressWarnings("unchecked")
    <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    WatchDir(Path dir, boolean recursive) throws IOException {
        this.toWatch = dir;
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.recursive = recursive;

        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;

    }

    void startNotificationThread() {
        notificationThread = new Thread(() -> {
            System.out.println("Starting Notification Thread");
            while (!stopThread) {
                try {
                    queue.take().run();
                } catch (InterruptedException ex) {
                    Logger.getLogger(WatchDir.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
            }
            System.out.println("Stopping Notification Thread");
        });
        notificationThread.start();
    }

    void stopAll() throws InterruptedException {
        stopThread = true;
        queue.clear();
        notificationThread.interrupt();
        notificationThread.join();
    }

    void pushNotification(Runnable runnable) {
        queue.add(runnable);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void subject(Consumer<String> alertConsumer, BiConsumer<String, File> fileConsumer, Runnable onMotionDetected) {
        System.out.println("Starting processEvents Thread");
        while (!stopThread) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                break;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            key.pollEvents().stream().forEach((WatchEvent<?> event) -> {
                WatchEvent.Kind kind = event.kind();
                if (!(kind == OVERFLOW)) {
                    WatchEvent<Path> ev = cast(event);
                    final Path name = ev.context();
                    final Path child = dir.resolve(name);
                    System.out.format("%s: %s\n", event.kind().name(), child);
                    if (event.kind().equals(ENTRY_CREATE) && child.toString().contains(".")) {
                        System.out.println("setting fileJustCreated to " + child);
                        fileJustCreated = child;
                        queue.add(() -> {
                            // System.out.println("sending " + child + " alert");
                            alertConsumer.accept(child.toFile().getName() + " alert");
                        });
                    }

                    if (event.kind().equals(ENTRY_MODIFY) && child.equals(Paths.get(toWatch.toString(), EVENT_MOTION_DETECTED))) {
                        onMotionDetected.run();
                    }

                    if (child.equals(Paths.get(toWatch.toString(), EVENT_ENDED)) && fileJustCreated != null) {
                        final File toBeProcessed = fileJustCreated.toFile();
                        fileJustCreated = null;
                        queue.add(() -> {
                            // System.out.println("sending " + toBeProcessed);
                            fileConsumer.accept("Recording " + toBeProcessed.getName(), toBeProcessed);
                        });
                    }

                    if (recursive && (kind == ENTRY_CREATE)) {
                        try {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException x) {
                            // ignore to keep sample readbale
                        }
                    }
                }
            });

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
        System.out.println("Stopping processEvents Thread");
    }
}
