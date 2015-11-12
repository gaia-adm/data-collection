package com.hp.gaia.agent.oncloud;

import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * obtain global settings - some of those that stored in agent.json for onprem version:
 * gaiaLocation, workerPool, soTimeout, connectTimeout
 */

public class GlobalSettings {

    private static final int DEFAULT_WORKER_POOL = 5;
    private static final int DEFAULT_SO_TIMEOUT = 60000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;


    private static String workingDir;

    private GlobalSettings() {
    }

    public static String getGaiaLocation() {
        if(!StringUtils.isEmpty(System.getenv("gaiaLocation"))){
            return System.getenv("gaiaLocation");
        } else {
            throw new IllegalStateException("Gaia location is not provided, cannot continue");
        }
    }

    public static int getWorkerPool() {
        if (!StringUtils.isEmpty(System.getenv("workerPool"))) {
            try {
                return Integer.parseInt(System.getenv("workerPool"));
            } catch (NumberFormatException nfe) {
                System.out.println("Worker pool size is not provided or invalid, using default");
            }
        }
        return DEFAULT_WORKER_POOL;
    }

    public static int getSoTimeout() {
        if (!StringUtils.isEmpty(System.getenv("soTimeout"))) {
            try {
                return Integer.parseInt(System.getenv("soTimeout"));
            } catch (NumberFormatException nfe) {
                System.out.println("Socket timeout is not provided or invalid, using default");
            }
        }
        return DEFAULT_SO_TIMEOUT;
    }

    public static int getConnectTimeout() {
        if (!StringUtils.isEmpty(System.getenv("connectTimeout"))) {
            try {
                return Integer.parseInt(System.getenv("connectTimeout"));
            } catch (NumberFormatException nfe) {
                System.out.println("Connection timeout is not provided or invalid, using default");
            }
        }
        return DEFAULT_CONNECT_TIMEOUT;
    }

    public static String getRabbitMqHost() {
        return System.getenv("rabbitmqHost");
    }

    public static Integer getRabbitMqPort() {
        if (StringUtils.isEmpty(System.getenv("rabbitmqPort"))) {
            try {
                return Integer.parseInt(System.getenv("rabbitmqPort"));
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid RabbitMQ port provided, using default");
            }
        }
        return 5672;
    }

    public static String getRabbitMqUser() {
        return System.getenv("rabbitmqUser");
    }

    public static String getRabbitMqPassword() {
        return System.getenv("rabbitmqPassword");
    }

    public static String getWorkingDir() {
        return workingDir;
    }

    static void setWorkingDir(String newDir) {
        workingDir = newDir;
    }

    public static File getConfigFile(final String configName) {
        return new File(getWorkingDir() + File.separatorChar + "conf" + File.separatorChar + configName);
    }

    public static File getConfigDir() {
        return new File(getWorkingDir() + File.separatorChar + "conf");
    }


}
