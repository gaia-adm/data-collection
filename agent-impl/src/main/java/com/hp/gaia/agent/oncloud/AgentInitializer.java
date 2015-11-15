package com.hp.gaia.agent.oncloud;

import com.hp.gaia.agent.oncloud.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;

/**
 * Bean responsible for Agent initialization. Kicks off initialization of various services.
 */
public class AgentInitializer {

    @Autowired
    private OnCloudAgentConfigService onCloudAgentConfigService;

    @Autowired
    private OnCloudProvidersConfigService onCloudProvidersConfigService;

    @Autowired
    private OnCloudCollectionStateService onCloudCollectionStateService;

    @Autowired
    private OnCloudCredentialsService onCloudCredentialsService;

    @Autowired
    private OnCloudResultUploadService onCloudResultUploadService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @PostConstruct
    public void init() {
        onCloudAgentConfigService.init();
        onCloudProvidersConfigService.init();
        onCloudCredentialsService.init();
        onCloudCollectionStateService.init();
        onCloudResultUploadService.init(onCloudAgentConfigService.getWorkerPool());
        threadPoolTaskExecutor.setMaxPoolSize(onCloudAgentConfigService.getWorkerPool());
        threadPoolTaskExecutor.setCorePoolSize(onCloudAgentConfigService.getWorkerPool());
    }

}
