package de.fearnixx.lolbanpick;

import java.io.File;

public class Constants {

    public static final File appsDir = new File("./cache").getAbsoluteFile();

    public static final String NODE_URI = "https://nodejs.org/dist/v14.15.1/node-v14.15.1-win-x64.zip";
    public static final String NODE_VERSION = "14.15.1";
    public static final File NODE_DIR = new File(appsDir, "node-v14.15.1-win-x64");
    public static final File NPM_EXECUTABLE = new File(NODE_DIR, "npm.cmd");

    public static final String GITWIN_URI = "https://github.com/git-for-windows/git/releases/download/v2.29.2.windows.2/PortableGit-2.29.2.2-64-bit.7z.exe";
    public static final String GITWIN_VERSION = "2.29.2_windows.2";
    public static final File GITWIN_DIR = new File(appsDir, "git-for-windows");

    public static final String PICKBAN_ARCHIVE = "https://github.com/FearNixx/lol-pick-ban-ui/archive/t/ptero-docker.zip";
    public static final File PICKBAN_DIR = new File(appsDir, "lol-pick-ban-ui-t-ptero-docker");
    public static final File PICKBAN_EULAYOUT_DIR = new File(PICKBAN_DIR, "layouts/layout-volu-europe");
}
