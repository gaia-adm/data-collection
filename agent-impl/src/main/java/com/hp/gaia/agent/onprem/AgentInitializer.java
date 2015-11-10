package com.hp.gaia.agent.onprem;

import com.hp.gaia.agent.onprem.service.OnPremAgentConfigService;
import com.hp.gaia.agent.onprem.service.OnPremCollectionStateService;
import com.hp.gaia.agent.onprem.service.OnPremCredentialsService;
import com.hp.gaia.agent.onprem.service.OnPremProvidersConfigService;
import com.hp.gaia.agent.onprem.service.OnPremResultUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Bean responsible for Agent initialization. Kicks off initialization of various services.
 */
public class AgentInitializer {

    private static final String AGENT_CONFIG = "agent.json";

    private static final String CREDENTIALS_CONFIG = "credentials.json";

    private static final String PROVIDERS_CONFIG = "providers.json";

    private static final String STATE_DIR_NAME = "state";

    @Autowired
    private OnPremAgentConfigService onPremAgentConfigService;

    @Autowired
    private OnPremCredentialsService onPremCredentialsService;

    @Autowired
    private OnPremProvidersConfigService onPremProvidersConfigService;

    @Autowired
    private OnPremCollectionStateService onPremCollectionStateService;

    @Autowired
    private OnPremResultUploadService onPremResultUploadService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @PostConstruct
    public void init() {
        onPremAgentConfigService.init(GlobalSettings.getConfigFile(AGENT_CONFIG));
        onPremCredentialsService.init(GlobalSettings.getConfigFile(CREDENTIALS_CONFIG));
        onPremProvidersConfigService.init(GlobalSettings.getConfigFile(PROVIDERS_CONFIG));
        onPremCollectionStateService.init(getStateDir());
        onPremResultUploadService.init(onPremAgentConfigService.getWorkerPool());
        threadPoolTaskExecutor.setMaxPoolSize(onPremAgentConfigService.getWorkerPool());
        threadPoolTaskExecutor.setCorePoolSize(onPremAgentConfigService.getWorkerPool());
    }

    private static File getStateDir() {
        return new File(GlobalSettings.getWorkingDir(), STATE_DIR_NAME);
    }
}
