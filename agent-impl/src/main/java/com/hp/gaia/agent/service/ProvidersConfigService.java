package com.hp.gaia.agent.service;

import com.hp.gaia.agent.config.ProviderConfig;

/**
 * Allows to retrieve configurations of data providers.
 */
public interface ProvidersConfigService {

    /**
     * Returns provider configuration for given providerConfigId. Throws exception if the providerConfigId is null or
     * invalid.
     */
    ProviderConfig getProviderConfig(String providerConfigId);

    /**
     * Returns true if given providerConfigId identifies a valid provider configuration.
     */
    boolean isProviderConfig(String providerConfigId);
}
