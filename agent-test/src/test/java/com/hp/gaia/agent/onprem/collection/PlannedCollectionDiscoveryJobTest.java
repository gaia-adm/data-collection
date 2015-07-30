package com.hp.gaia.agent.onprem.collection;

import com.hp.gaia.agent.AgentIntegrationTest;
import com.hp.gaia.agent.collection.DataCollectionTask;
import com.hp.gaia.agent.collection.PlannedCollectionDiscoveryJob;
import com.hp.gaia.agent.onprem.service.OnPremCollectionStateService;
import com.hp.gaia.agent.onprem.service.OnPremProvidersConfigService;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@DirtiesContext
public class PlannedCollectionDiscoveryJobTest extends AgentIntegrationTest {

    @Autowired
    private PlannedCollectionDiscoveryJob plannedCollectionDiscoveryJob;

    @Autowired
    private OnPremCollectionStateService onPremCollectionStateService;

    @Autowired
    private OnPremProvidersConfigService onPremProvidersConfigService;

    private TaskExecutor taskExecutor;

    @Before
    public void before() {
        taskExecutor = createMock(TaskExecutor.class);
        plannedCollectionDiscoveryJob.setTaskExecutor(taskExecutor);
    }

    @Test
    public void testSuccess() throws URISyntaxException, IOException {
        onPremProvidersConfigService.init(getConfigFile("provider_configs/collection_discovery_job.json"));
        Path tempDir = Files.createTempDirectory("PlannedCollectionDiscoveryJobTest");
        onPremCollectionStateService.init(tempDir.toFile());
        Capture<Runnable> fistCapture = new Capture<>();
        Capture<Runnable> secondCapture = new Capture<>();
        // expectations
        taskExecutor.execute(capture(fistCapture));
        taskExecutor.execute(capture(secondCapture));
        // replay mocks
        replay(taskExecutor);
        plannedCollectionDiscoveryJob.execute();
        Set<String> processedConfigs = new HashSet<>();
        DataCollectionTask firstTask = (DataCollectionTask)fistCapture.getValue();
        processedConfigs.add(firstTask.getProviderConfig().getConfigId());
        DataCollectionTask secondTask = (DataCollectionTask)secondCapture.getValue();
        processedConfigs.add(secondTask.getProviderConfig().getConfigId());
        assertTrue(processedConfigs.contains("testConfig1"));
        assertTrue(processedConfigs.contains("testConfig2"));
        // verify mocks
        verify(taskExecutor);
    }

    private static File getConfigFile(String name) throws URISyntaxException {
        URL agentJson = PlannedCollectionDiscoveryJobTest.class.getClassLoader().getResource(name);
        assertNotNull(name + " was not found", agentJson);
        return new File(agentJson.toURI());
    }
}
