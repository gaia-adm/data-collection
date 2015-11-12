package com.hp.gaia.agent.oncloud.service;

import com.hp.gaia.agent.config.AgentConfig;
import com.hp.gaia.agent.config.ProtectedValue;
import com.hp.gaia.agent.oncloud.GlobalSettings;
import com.hp.gaia.agent.service.AgentConfigService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public class OnCloudAgentConfigService implements AgentConfigService {

/*    private static final String DEFAULT_RABBITMQ_HOST = "rabbitmq-master.skydns.local";
    private static final String DEFAULT_RABBITMQ_USER = "admin";
    private static final String DEFAULT_RABBITMQ_PASSWORD = "";*/

    private AgentConfig agentConfig = new AgentConfig();

/*    @Autowired
    private ProtectedValueDecrypter protectedValueDecrypter;*/

    public void init() {

        agentConfig.setGaiaLocation(GlobalSettings.getGaiaLocation());
        agentConfig.setWorkerPool(GlobalSettings.getWorkerPool());
        agentConfig.setConnectTimeout(GlobalSettings.getConnectTimeout());
        agentConfig.setSoTimeout(GlobalSettings.getSoTimeout());

        validate(agentConfig);

    }

    /**
     * Returns access token to use for GAIA connection. Only relevant for on-prem deployment.
     */
    public String getAccessToken() {
        ProtectedValue protectedValue = agentConfig.getAccessToken();
        if (protectedValue != null) {
            return protectedValue.getValue();
        } else {
            return null;
        }
    }

    @Override
    public int getWorkerPool() {
        return agentConfig.getWorkerPool();
    }

    @Override
    public String getGaiaLocation() {
        return agentConfig.getGaiaLocation();
    }

    @Override
    public int getSoTimeout() {
        return agentConfig.getSoTimeout();
    }

    @Override
    public int getConnectTimeout() {
        return agentConfig.getConnectTimeout();
    }

    private static void validate(final AgentConfig agentConfig) {
        Validate.notNull(agentConfig);
        if (StringUtils.isEmpty(agentConfig.getGaiaLocation())) {
            throw new IllegalStateException("gaiaLocation cannot be null or empty");
        }
        if (agentConfig.getSoTimeout() != null && agentConfig.getSoTimeout() <= 0) {
            throw new IllegalStateException("soTimeout cannot be negative");
        }
        if (agentConfig.getConnectTimeout() != null && agentConfig.getConnectTimeout() <= 0) {
            throw new IllegalStateException("connectionTimeout cannot be negative");
        }
        if (agentConfig.getWorkerPool() != null && agentConfig.getWorkerPool() <= 0) {
            throw new IllegalStateException("workerPool must be at least 1");
        }
    }

/*    private void decryptValues(final AgentConfig agentConfig) {
        decryptProxyPassword(agentConfig.getProxy());
        ProtectedValue protectedValue = agentConfig.getAccessToken();
        if (protectedValue != null && protectedValue.getType() == Type.ENCRYPTED) {
            final String accessKey = protectedValueDecrypter.decrypt(protectedValue);
            agentConfig.setAccessToken(new ProtectedValue(Type.PLAIN, accessKey));
        }
    }

    private void decryptProxyPassword(Proxy proxy) {
        if (proxy != null) {
            if (proxy.getHttpProxyPassword() != null && proxy.getHttpProxyPassword().getType() == Type.ENCRYPTED) {
                String newProxyPassword = protectedValueDecrypter.decrypt(proxy.getHttpProxyPassword());
                proxy.setHttpProxyPassword(new ProtectedValue(Type.PLAIN, newProxyPassword));
            }
        }
    }

    private boolean encryptNeededValues(final AgentConfig agentConfig) {
        boolean result = encryptProxyPassword(agentConfig.getProxy());
        ProtectedValue protectedValue = agentConfig.getAccessToken();
        if (protectedValue != null) {
            if (protectedValue.getType() == Type.ENCRYPT) {
                agentConfig.setAccessToken(protectedValueDecrypter.encrypt(protectedValue.getValue()));
                result = true;
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
    }*/
}
