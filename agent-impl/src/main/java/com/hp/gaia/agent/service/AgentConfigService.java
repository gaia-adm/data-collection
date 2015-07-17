package com.hp.gaia.agent.service;

import javax.validation.constraints.NotNull;

/**
 * Allows to retrieve agent configuration.
 */
public interface AgentConfigService {

    /**
     * Returns URL of the GAIA location.
     */
    @NotNull
    String getGaiaLocation();

    /**
     * Returns worker pool size.
     */
    int getWorkerPool();

    int getSoTimeout();

    int getConnecTimeout();
}
