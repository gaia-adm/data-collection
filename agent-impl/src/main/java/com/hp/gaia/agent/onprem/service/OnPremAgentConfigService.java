package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.config.AgentConfig;
import com.hp.gaia.agent.config.Proxy;
import com.hp.gaia.agent.onprem.config.ConfigFactory;
import com.hp.gaia.agent.service.AgentConfigService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class OnPremAgentConfigService extends ConfigurationService implements AgentConfigService {

    private static final String AGENT_CONFIG = "agent.json";

    private static final int DEFAULT_SO_TIMEOUT = 60000;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;

    private AgentConfig agentConfig;

    @PostConstruct
    public void init() {
        final File agentConfigFile = getConfigFile(AGENT_CONFIG);
        verifyFile(agentConfigFile);

        agentConfig = ConfigFactory.readConfig(agentConfigFile, AgentConfig.class);;
        validate(agentConfig);
    }

    /**
     * Returns access token to use for GAIA connection. Only relevant for on-prem deployment.
     */
    public String getAccessToken() {
        return agentConfig.getAccessToken();
    }

    /**
     * Returns proxy to use for GAIA connection. Only relevant for on-prem deployment.
     */
    public Proxy getProxy() {
        return agentConfig.getProxy();
    }

    @Override
    public String getGaiaLocation() {
        return agentConfig.getGaiaLocation();
    }

    @Override
    public int getSoTimeout() {
        return agentConfig.getSoTimeout() != null ? agentConfig.getSoTimeout() : DEFAULT_SO_TIMEOUT;
    }

    @Override
    public int getConnectionTimeout() {
        return agentConfig.getConnectionTimeout() != null ? agentConfig.getConnectionTimeout() : DEFAULT_CONNECTION_TIMEOUT;
    }

    private static void validate(final AgentConfig agentConfig) {
        Validate.notNull(agentConfig);
        if (StringUtils.isEmpty(agentConfig.getGaiaLocation())) {
            throw new IllegalStateException("gaiaLocation cannot be null or empty");
        }
        if (StringUtils.isEmpty(agentConfig.getAccessToken())) {
            throw new IllegalStateException("accessToken cannot be null or empty");
        }
        if (agentConfig.getSoTimeout() != null && agentConfig.getSoTimeout() <= 0) {
            throw new IllegalStateException("soTimeout cannot be negative");
        }
        if (agentConfig.getConnectionTimeout() != null && agentConfig.getConnectionTimeout() <= 0) {
            throw new IllegalStateException("connectionTimeout cannot be negative");
        }
        if (agentConfig.getProxy() != null && !StringUtils.isEmpty(agentConfig.getProxy().getHttpProxy())) {
            // validate proxy URL
            String proxyUrl = agentConfig.getProxy().getHttpProxy();
            try {
                new URL(proxyUrl);
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Proxy URL '" + proxyUrl + "' is invalid", e);
            }
        }
    }
}
