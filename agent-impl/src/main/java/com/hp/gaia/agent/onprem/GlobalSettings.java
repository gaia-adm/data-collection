package com.hp.gaia.agent.onprem;

public class GlobalSettings {

    private static String workingDir;

    private GlobalSettings() {
    }

    public static String getWorkingDir() {
        return workingDir;
    }

    static void setWorkingDir(String newDir) {
        workingDir = newDir;
    }
}
