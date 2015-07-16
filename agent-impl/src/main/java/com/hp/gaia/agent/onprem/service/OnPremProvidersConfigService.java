package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.config.Proxy;
import com.hp.gaia.agent.onprem.config.ConfigFactory;
import com.hp.gaia.agent.onprem.config.ProvidersConfig;
import com.hp.gaia.agent.service.ProvidersConfigService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnPremProvidersConfigService extends ConfigurationService implements ProvidersConfigService {

    private static final String PROVIDERS_CONFIG = "providers.json";

    private static final int DEFAULT_RUN_PERIOD = 60; // every 60 minutes

    private Map<String, ProviderConfig> providerConfigMap;

    @PostConstruct
    public void init() {
        File providersConfigFile = getConfigFile(PROVIDERS_CONFIG);
        verifyFile(providersConfigFile);
        ProvidersConfig providersConfig = ConfigFactory.readConfig(providersConfigFile, ProvidersConfig.class);

        this.providerConfigMap = new HashMap<>();
        final Proxy globalProxy = providersConfig.getProxy();

        final List<ProviderConfig> providers = providersConfig.getProviders();
        if (providers != null) {
            for (final ProviderConfig providerConfig : providers) {
                validate(providerConfig);
                if (providerConfigMap.containsKey(providerConfig.getConfigId())) {
                    throw new IllegalStateException("Duplicate provider configurationId " + providerConfig.getConfigId());
                }
                providerConfigMap.put(providerConfig.getConfigId(),
                        makeSafeProviderConfig(providerConfig, globalProxy));
            }
        }
    }

    /**
     * Creates safe {@link ProviderConfig} that can be passed outside of the service.
     */
    private static ProviderConfig makeSafeProviderConfig(final ProviderConfig providerConfig, final Proxy globalProxy) {
        return new ProviderConfig(providerConfig.getConfigId(), providerConfig.getProviderId(),
                providerConfig.getProperties() != null ?
                        Collections.unmodifiableMap(providerConfig.getProperties()) :
                        Collections.emptyMap(), providerConfig.getCredentialsId(),
                providerConfig.getProxy() == null ? globalProxy : providerConfig.getProxy(),
                providerConfig.getRunPeriod() == null ? DEFAULT_RUN_PERIOD : providerConfig.getRunPeriod());
    }

    @Override
    public ProviderConfig getProviderConfig(final String providerConfigId) {
        Validate.notNull(providerConfigId);

        final ProviderConfig providerConfig = providerConfigMap.get(providerConfigId);
        if (providerConfig == null) {
            throw new IllegalArgumentException("Invalid providerConfigId " + providerConfigId);
        }

        return providerConfig;
    }

    public List<ProviderConfig> getProviderConfigs() {
        return Collections.unmodifiableList(new ArrayList<>(providerConfigMap.values()));
    }

    @Override
    public boolean isProviderConfig(final String providerConfigId) {
        Validate.notNull(providerConfigId);

        return providerConfigMap.containsKey(providerConfigId);
    }

    private static void validate(final ProviderConfig providerConfig) {
        Validate.notNull(providerConfig);
        if (StringUtils.isEmpty(providerConfig.getConfigId())) {
            throw new IllegalStateException("configId cannot be null or empty");
        }
        if (StringUtils.isEmpty(providerConfig.getProviderId())) {
            throw new IllegalStateException("providerId cannot be null or empty");
        }
        if (providerConfig.getRunPeriod() != null && providerConfig.getRunPeriod() <= 0) {
            throw new IllegalStateException("runPeriod cannot be negative");
        }
        if (providerConfig.getProxy() != null && !StringUtils.isEmpty(providerConfig.getProxy().getHttpProxy())) {
            // validate proxy URL
            String proxyUrl = providerConfig.getProxy().getHttpProxy();
            try {
                new URL(proxyUrl);
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Proxy URL '" + proxyUrl + "' is invalid", e);
            }
        }
    }
}
