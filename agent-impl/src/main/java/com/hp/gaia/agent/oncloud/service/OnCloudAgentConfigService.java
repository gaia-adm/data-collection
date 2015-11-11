package com.hp.gaia.agent.oncloud.service;

import com.hp.gaia.agent.config.AgentConfig;
import com.hp.gaia.agent.config.ProtectedValue;
import com.hp.gaia.agent.config.ProtectedValue.Type;
import com.hp.gaia.agent.config.Proxy;
import com.hp.gaia.agent.oncloud.GlobalSettings;
import com.hp.gaia.agent.onprem.config.ConfigUtils;
import com.hp.gaia.agent.service.AgentConfigService;
import com.hp.gaia.agent.service.ProtectedValueDecrypter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class OnCloudAgentConfigService implements AgentConfigService {

    private static final int DEFAULT_WORKER_POOL = 5;
    private static final int DEFAULT_SO_TIMEOUT = 60000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
/*    private static final String DEFAULT_RABBITMQ_HOST = "rabbitmq-master.skydns.local";
    private static final String DEFAULT_RABBITMQ_USER = "admin";
    private static final String DEFAULT_RABBITMQ_PASSWORD = "";*/

    private AgentConfig agentConfig = new AgentConfig();

/*    @Autowired
    private ProtectedValueDecrypter protectedValueDecrypter;*/

    public void init() {

        if (StringUtils.isEmpty(GlobalSettings.getGaiaLocation())) {
            System.out.println("Gaia location is not provided, exiting");
            throw new IllegalStateException("Gaia location is not provided, cannot continue");
//            System.exit(2);
        } else {
            agentConfig.setGaiaLocation(GlobalSettings.getGaiaLocation());
        }

        if (StringUtils.isEmpty(GlobalSettings.getWorkerPool())) {
            System.out.println("Worker pool size is not provided, using default");
            agentConfig.setWorkerPool(DEFAULT_WORKER_POOL);
        } else {
            try {
                agentConfig.setWorkerPool(Integer.valueOf(GlobalSettings.getWorkerPool()));
            } catch (NumberFormatException nfe) {
                System.out.println("Provided workere pool value is invalid, using default");
                agentConfig.setWorkerPool(DEFAULT_WORKER_POOL);
            }
        }

        if (StringUtils.isEmpty(GlobalSettings.getConnectTimeout())) {
            System.out.println("Connection timeout is not provided, using default");
            agentConfig.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        } else {
            try {
                agentConfig.setConnectTimeout(Integer.valueOf(GlobalSettings.getConnectTimeout()));
            } catch (NumberFormatException nfe) {
                System.out.println("Provided connection timeout value is invalid, using default");
                agentConfig.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
            }
        }

        if (StringUtils.isEmpty(GlobalSettings.getSoTimeout())) {
            System.out.println("Socket timeout is not provided, using default");
            agentConfig.setSoTimeout(DEFAULT_SO_TIMEOUT);
        } else {
            try {
                agentConfig.setSoTimeout(Integer.valueOf(GlobalSettings.getSoTimeout()));
            } catch (NumberFormatException nfe) {
                System.out.println("Provided socket timeout value is invalid, using default");
                agentConfig.setSoTimeout(DEFAULT_SO_TIMEOUT);
            }
        }

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
        return agentConfig.getWorkerPool() != null ? agentConfig.getWorkerPool() : DEFAULT_WORKER_POOL;
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
    public int getConnectTimeout() {
        return agentConfig.getConnectTimeout() != null ? agentConfig.getConnectTimeout() :
                DEFAULT_CONNECT_TIMEOUT;
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
