package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.config.ProtectedValue;
import com.hp.gaia.agent.config.ProtectedValue.Type;
import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.config.Proxy;
import com.hp.gaia.agent.onprem.config.ConfigUtils;
import com.hp.gaia.agent.onprem.config.ProvidersConfig;
import com.hp.gaia.agent.service.ProtectedValueDecrypter;
import com.hp.gaia.agent.service.ProvidersConfigService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnPremProvidersConfigService implements ProvidersConfigService {

    private static final int DEFAULT_RUN_PERIOD = 60; // every 60 minutes

    private Map<String, ProviderConfig> providerConfigMap;

    @Autowired
    private ProtectedValueDecrypter protectedValueDecrypter;

    public void init(File providersConfigFile) {
        ProvidersConfig providersConfig = ConfigUtils.readConfig(providersConfigFile, ProvidersConfig.class);

        this.providerConfigMap = new HashMap<>();
        final Proxy globalProxy = providersConfig.getProxy();

        final List<ProviderConfig> providers = providersConfig.getProviders();
        validate(providers);
        boolean saveNewFile = encryptNeededValues(providersConfig);
        if (saveNewFile) {
            File newConfigFile = new File(providersConfigFile.getAbsolutePath() + ".encrypted");
            if (!newConfigFile.exists() || newConfigFile.canWrite()) {
                ConfigUtils.writeConfig(newConfigFile, providersConfig);
            }
        }
        // store provider configs in local map
        if (providers != null) {
            for (final ProviderConfig providerConfig : providers) {
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

    private static void validate(final List<ProviderConfig> providers) {
        Set<String> providerConfigIds = new HashSet<>();
        if (providers != null) {
            for (final ProviderConfig providerConfig : providers) {
                validate(providerConfig);
            }
            for (final ProviderConfig providerConfig : providers) {
                if (providerConfigIds.contains(providerConfig.getConfigId())) {
                    throw new IllegalStateException("Duplicate provider configurationId " + providerConfig.getConfigId());
                }
                providerConfigIds.add(providerConfig.getConfigId());
            }
        }
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
            providerConfig.getProxy().getHttpProxyURL();
        }
    }

    private boolean encryptNeededValues(final ProvidersConfig providersConfig) {
        boolean result = encryptProxyPassword(providersConfig.getProxy());
        final List<ProviderConfig> providerConfigs = providersConfig.getProviders();
        if (providerConfigs != null) {
            for (ProviderConfig providerConfig : providerConfigs) {
                if (encryptProxyPassword(providerConfig.getProxy())) {
                    result = true;
                }
            }
        }

        return result;
    }

    private boolean encryptProxyPassword(Proxy proxy) {
        if (proxy != null) {
            if (proxy.getHttpProxyPassword() != null && proxy.getHttpProxyPassword().getType() == Type.ENCRYPT) {
                ProtectedValue newProxyPassword = protectedValueDecrypter.encrypt(proxy.getHttpProxyPassword().getValue());
                proxy.setHttpProxyPassword(newProxyPassword);
                return true;
            }
        }
        return false;
    }
}
