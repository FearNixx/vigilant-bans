package de.fearnixx.lolbanpick.config;

import java.util.regex.Pattern;

public abstract class ConfigKeys {

    private ConfigKeys() {}

    public static final String FRONTEND = "frontend";
    public static final String ENABLE_SCORES = "scoreEnabled";
    public static final String ENABLE_SPELLS = "spellsEnabled";
    public static final String ENABLE_COACHES = "coachesEnabled";
    public static final String TEAM_BLUE = "blueTeam";
    public static final String TEAM_RED = "redTeam";
    public static final String TEAM_NAME = "name";
    public static final String TEAM_SCORE = "score";
    public static final String TEAM_COACH = "coach";
    public static final String TEAM_COLOR = "color";

    public static final Pattern RGB_PATTERN = Pattern.compile("rgb\\((\\d+),(\\d+),(\\d+)");
}
