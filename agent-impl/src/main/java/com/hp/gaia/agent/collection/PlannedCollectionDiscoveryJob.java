package com.hp.gaia.agent.collection;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.service.CollectionState;
import com.hp.gaia.agent.service.CollectionState.State;
import com.hp.gaia.agent.service.CollectionStateService;
import com.hp.gaia.agent.service.PlannedCollection;
import com.hp.gaia.agent.service.PlannedCollectionProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

/**
 * Discovers what data needs to be collected and starts the data collection. Executed periodically.
 */
public class PlannedCollectionDiscoveryJob {

    private static final Logger logger = LogManager.getLogger(PlannedCollectionDiscoveryJob.class);

    @Autowired
    private PlannedCollectionProvider plannedCollectionProvider;

    @Autowired
    private CollectionStateService collectionStateService;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private ObjectFactory<DataCollectionTask> dataCollectionTaskFactory;

    // for tests
    public void setTaskExecutor(final TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void execute() {
        PlannedCollection plannedCollection = plannedCollectionProvider.findNextPlannedCollection();

        while(plannedCollection != null) {
            ProviderConfig providerConfig = plannedCollection.getProviderConfig();
            CollectionState collectionState = plannedCollection.getCollectionState();
            logger.debug("Planning to execute data collection for configuration '" + providerConfig.getConfigId() +
                    "' of data provider '" + providerConfig.getProviderId() + "'");
            collectionState.setState(State.PENDING);
            collectionStateService.saveCollectionState(collectionState);
            // execute task
            DataCollectionTask dataCollectionTask = dataCollectionTaskFactory.getObject();
            dataCollectionTask.init(providerConfig, collectionState);
            taskExecutor.execute(dataCollectionTask);

            plannedCollection = plannedCollectionProvider.findNextPlannedCollection();
        }
    }
}
