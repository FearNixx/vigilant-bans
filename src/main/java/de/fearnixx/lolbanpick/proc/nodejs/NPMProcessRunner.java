package de.fearnixx.lolbanpick.proc.nodejs;

import de.fearnixx.lolbanpick.Constants;
import de.fearnixx.lolbanpick.proc.ProcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class NPMProcessRunner implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NPMProcessRunner.class);

    private final String[] nodeCommand;
    private final AtomicReference<Path> workingDirectory = new AtomicReference<>();
    private final AtomicReference<Process> processRef = new AtomicReference<>();
    private final List<Runnable> doneListeners = new LinkedList<>();
    private final Map<String, String> env = new ConcurrentHashMap<>();

    public NPMProcessRunner(String... nodeCommand) {
        this.nodeCommand = nodeCommand;
        workingDirectory.set(new File(".").getAbsoluteFile().toPath());
        // Avoid cluttering the user cache with any of our stuff.
        // This includes the frequent error logs after killing NPM...
        env.put("NPM_CONFIG_CACHE", Constants.NPM_CACHE_DIR.toPath().toString());
        // Prepend node installation directory to path.
        env.put(Constants.OS_PATH_ENV,
                String.format("\"%s\"%s%s", Constants.NODE_DIR, File.pathSeparator, System.getenv(Constants.OS_PATH_ENV)));
    }

    public synchronized NPMProcessRunner setWorkingDirectory(Path directory) {
        workingDirectory.set(directory);
        return this;
    }

    public synchronized void onDone(Runnable listener) {
        doneListeners.add(listener);
    }

    public NPMProcessRunner env(String key, String value) {
        env.put(key, value);
        return this;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (processRef.get() != null) {
                throw new IllegalStateException("Runner cannot be re-used.");
            }
        }


        Process proc;
        try {
            var cmd = new String[nodeCommand.length + 1];
            cmd[0] = Constants.NPM_EXECUTABLE.toPath().toString();
            System.arraycopy(nodeCommand, 0, cmd, 1, nodeCommand.length);
            final var procBuilder = new ProcessBuilder()
                    .command(cmd)
                    .directory(workingDirectory.get().toFile())
                    .inheritIO();
            procBuilder.environment().putAll(env);
            proc = procBuilder.start();
        } catch (IOException e) {
            logger.error("Error starting \"npm {}\"!", String.join(" ", nodeCommand), e);
            return;
        }
        synchronized (this) {
            processRef.set(proc);
        }
        try {
            if (proc.isAlive()) {
                proc.waitFor();
            }
        } catch (InterruptedException e) {
            logger.warn("Process wait interrupted. Killing.");
            stop();
            Thread.currentThread().interrupt();
        }
        synchronized (this) {
            processRef.set(null);
        }
        logger.info("Process finished.");
        doneListeners.forEach(Runnable::run);
    }

    public void stop() {
        synchronized (this) {
            logger.info("Stopping Node...");
            ProcUtils.killByOS(processRef.get().toHandle(), true);
            processRef.set(null);
        }
    }

    public boolean isAlive() {
        return processRef.get() != null && processRef.get().isAlive();
    }
}
