package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.onprem.GlobalSettings;

import java.io.File;

/**
 * Base class for configuration services.
 */
public abstract class ConfigurationService {

    protected static File getConfigFile(final String configName) {
        return GlobalSettings.getConfigFile(configName);
    }

    protected static void verifyFile(File file) {
        if (!file.exists()) {
            throw new RuntimeException("Configuration file " + file.getAbsolutePath() + " doesn't exist");
        }
        if (!file.canRead()) {
            throw new RuntimeException("Configuration file " + file.getAbsolutePath() + " is not readable");
        }
    }
}
