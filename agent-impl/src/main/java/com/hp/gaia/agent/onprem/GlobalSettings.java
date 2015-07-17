package com.hp.gaia.agent.onprem;

import java.io.File;

public class GlobalSettings {

    private static String workingDir;

    private GlobalSettings() {
    }

    public static String getWorkingDir() {
        return workingDir;
    }

    public static File getConfigFile(final String configName) {
        return new File(getWorkingDir() + File.separatorChar + "conf" + File.separatorChar + configName);
    }

    public static File getConfigDir() {
        return new File(getWorkingDir() + File.separatorChar + "conf" + File.separatorChar);
    }

    static void setWorkingDir(String newDir) {
        workingDir = newDir;
    }
}
