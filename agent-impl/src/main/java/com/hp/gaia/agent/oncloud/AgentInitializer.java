package com.hp.gaia.agent.oncloud;

import com.hp.gaia.agent.oncloud.service.OnCloudAgentConfigService;
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
    private OnCloudAgentConfigService onCloudAgentConfigService;

/*    @Autowired
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
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;*/

    @PostConstruct
    public void init() {
        onCloudAgentConfigService.init();
/*        onPremCredentialsService.init(com.hp.gaia.agent.onprem.GlobalSettings.getConfigFile(CREDENTIALS_CONFIG));
        onPremProvidersConfigService.init(com.hp.gaia.agent.onprem.GlobalSettings.getConfigFile(PROVIDERS_CONFIG));
        onPremCollectionStateService.init(getStateDir());
        onPremResultUploadService.init(onPremAgentConfigService.getWorkerPool());
        threadPoolTaskExecutor.setMaxPoolSize(onPremAgentConfigService.getWorkerPool());
        threadPoolTaskExecutor.setCorePoolSize(onPremAgentConfigService.getWorkerPool());*/
    }

/*    private static File getStateDir() {
        return new File(GlobalSettings.getWorkingDir(), STATE_DIR_NAME);
    }*/
}
