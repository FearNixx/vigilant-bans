package de.fearnixx.lolbanpick;

import java.io.File;

public class Constants {

    private Constants() { }

    public static final String OS_PATH_ENV = "PATH";
    public static final File appsDir = new File("./cache").getAbsoluteFile();

    public static final String NODE_URI = "https://nodejs.org/dist/v16.15.0/node-v16.15.0-win-x64.zip";
    public static final String NODE_VERSION = "16.15.0";
    public static final File NODE_DIR = new File(appsDir, "node-v16.15.0-win-x64");
    public static final File NPM_EXECUTABLE = new File(NODE_DIR, "npm.cmd");
    public static final File NPM_CACHE_DIR = new File(Constants.appsDir, "npm_cache").getAbsoluteFile();

    public static final String GITWIN_URI = "https://github.com/git-for-windows/git/releases/download/v2.29.2.windows.2/PortableGit-2.29.2.2-64-bit.7z.exe";
    public static final String GITWIN_VERSION = "2.29.2_windows.2";
    public static final File GITWIN_DIR = new File(appsDir, "git-for-windows");

    public static final String PICKBAN_ARCHIVE = "https://github.com/RCVolus/lol-pick-ban-ui/archive/master.zip";
    public static final File REPO_DIR = new File(appsDir, "lol-pick-ban-ui-master");
    public static final File PICKBAN_DIR = new File(REPO_DIR, "backend");
    public static final File PICKBAN_EULAYOUT_DIR = new File(REPO_DIR, "layouts/layout-volu-europe");

    public static final String SOCIAL_TWITTER = "https://twitter.com/fearnixxgaming";
    public static final String SOCIAL_GITHUB = "https://github.com/fearnixx/vigilant-bans";
    public static final String ICONS8 = "https://icons8.com";
    public static final String OVERLAY_URI = "http://localhost:3000/?backend=ws://localhost:8999";
}
