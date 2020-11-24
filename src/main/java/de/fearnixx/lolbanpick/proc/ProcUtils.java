package de.fearnixx.lolbanpick.proc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ProcUtils {

    private static final Logger logger = LoggerFactory.getLogger(ProcUtils.class);
    private static final boolean FORCE_KILL_NPM = "true".equals(System.getProperty("banpick.force_npm_kill", "true"));

    public static void recursiveKill(ProcessHandle proc) {
        logger.info("Children of {}: {}", proc.pid(), proc.descendants());
        proc.descendants().forEach(ProcUtils::recursiveKill);
        logger.info("Destroying {}", proc.pid());
        proc.destroy();
    }

    public static void killByOS(ProcessHandle proc, boolean includeDescendants) {
        if (includeDescendants) {
            proc.descendants().forEach(dec -> killByOS(dec, true));
        }
        try {
            logger.info("Sys-killing: {}", proc.pid());
            Runtime.getRuntime().exec(getKillCommand(proc));
        } catch (IOException e) {
            logger.warn("Failed to kill {}.", proc.pid(), e);
        }
    }

    private static String[] getKillCommand(ProcessHandle proc) {
        if (FORCE_KILL_NPM) {
            return new String[]{"taskkill", "/F", "/PID", Long.toString(proc.pid())};
        } else {
            return new String[]{"taskkill", "/PID", Long.toString(proc.pid())};
        }
    }
}
