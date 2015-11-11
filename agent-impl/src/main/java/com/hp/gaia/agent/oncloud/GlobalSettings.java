package com.hp.gaia.agent.oncloud;

import java.io.File;

/**
 * obtain global settings - some of those that stored in agent.json for onprem version:
 * gaiaLocation, workerPool, soTimeout, connectTimeout
 */

public class GlobalSettings {

    private GlobalSettings() {
    }

    public static String getGaiaLocation() {
        return System.getenv("gaiaLocation");
    }

    public static String getWorkerPool() {
        return System.getenv("workerPool");
    }

    public static String getSoTimeout() {
        return System.getenv("soTimeout");
    }

    public static String getConnectTimeout() {
        return System.getenv("connectTimeout");
    }
    public static String getRabbitMqHost() {
        return System.getenv("rabbitmqHost");
    }

    public static String getRabbitMqUser() {
        return System.getenv("rabbitmqUser");
    }

    public static String getRabbitMqPassword() {
        return System.getenv("rabbitmqPassword");
    }

/*    public static String getWorkingDir() {
        return workingDir;
    }

    public static File getConfigFile(final String configName) {
        return new File(getWorkingDir() + File.separatorChar + "conf" + File.separatorChar + configName);
    }

    public static File getConfigDir() {
        return new File(getWorkingDir() + File.separatorChar + "conf");
    }

    static void setWorkingDir(String newDir) {
        workingDir = newDir;
    }*/
}
