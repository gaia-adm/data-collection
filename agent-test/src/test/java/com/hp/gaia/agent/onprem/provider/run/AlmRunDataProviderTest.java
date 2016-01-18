package com.hp.gaia.agent.onprem.provider.run;

import com.hp.gaia.agent.AgentIntegrationTest;
import com.hp.gaia.agent.collection.DataCollectionTask;
import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.onprem.service.OnPremCollectionStateService;
import com.hp.gaia.agent.onprem.service.OnPremCredentialsService;
import com.hp.gaia.agent.onprem.service.OnPremProvidersConfigService;
import com.hp.gaia.agent.service.CollectionState;
import com.hp.gaia.agent.service.DataProviderRegistry;
import com.hp.gaia.agent.service.ResultUploadService;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.alm.run.AlmRunDataProvider;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;

@DirtiesContext
public class AlmRunDataProviderTest extends AgentIntegrationTest {

    @Autowired
    private OnPremCredentialsService onPremCredentialsService;
    @Autowired
    private OnPremCollectionStateService onPremCollectionStateService;
    @Autowired
    private OnPremProvidersConfigService onPremProvidersConfigService;
    @Autowired
    private DataProviderRegistry dataProviderRegistry;
    private ResultUploadService resultUploadService;
    @Autowired
    private ObjectFactory<DataCollectionTask> dataCollectionTaskFactory;
    private DataCollectionTask dataCollectionTask;

    @Before
    public void before() {

        resultUploadService = createMock(ResultUploadService.class);
        dataCollectionTask = dataCollectionTaskFactory.getObject();
        dataCollectionTask.setResultUploadService(resultUploadService);
    }

    // @Test
    public void testSuccess() throws URISyntaxException, IOException {

        onPremCredentialsService.init(getConfigFile("credentials_configs/alm_credentials.json"));
        onPremProvidersConfigService.init(getConfigFile("provider_configs/alm_provider.json"));
        Path tempDir = Files.createTempDirectory("DataCollectionTaskTest");
        onPremCollectionStateService.init(tempDir.toFile());
        AlmRunDataProvider testProvider1 = (AlmRunDataProvider) dataProviderRegistry.getDataProvider("alm/run");
        // expectations
        Capture<ProviderConfig> providerConfigCapture = new Capture<>();
        Capture<Data> dataCapture = new Capture<>();
        resultUploadService.sendData(capture(providerConfigCapture), capture(dataCapture));
        // replay mocks
        replay(resultUploadService);
        ProviderConfig providerConfig = onPremProvidersConfigService.getProviderConfig("1");
        CollectionState collectionState = new CollectionState("1");
        //collectionState.setBookmark("bookmark1");
        dataCollectionTask.init(providerConfig, collectionState);
        dataCollectionTask.run();
    }

    private static File getConfigFile(String name) throws URISyntaxException {

        URL agentJson = AlmRunDataProviderTest.class.getClassLoader().getResource(name);
        assertNotNull(name + " was not found", agentJson);

        return new File(agentJson.toURI());
    }
}
