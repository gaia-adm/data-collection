package com.hp.gaia.agent.onprem.provider.run;

import com.hp.gaia.agent.AgentIntegrationTest;
import com.hp.gaia.agent.collection.DataCollectionTask;
import com.hp.gaia.agent.onprem.service.OnPremCollectionStateService;
import com.hp.gaia.agent.onprem.service.OnPremCredentialsService;
import com.hp.gaia.agent.onprem.service.OnPremProvidersConfigService;
import com.hp.gaia.agent.service.ResultUploadService;
import org.junit.Before;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertNotNull;

@DirtiesContext
@ContextConfiguration({"classpath*:/Spring/test-gaia-provider-run-context.xml"})
public class AlmRunDataProviderTest extends AgentIntegrationTest {

    @Autowired
    private OnPremCredentialsService onPremCredentialsService;
    @Autowired
    private OnPremCollectionStateService onPremCollectionStateService;
    @Autowired
    private OnPremProvidersConfigService onPremProvidersConfigService;
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

    private static File getConfigFile(String name) throws URISyntaxException {

        URL agentJson = AlmRunDataProviderTest.class.getClassLoader().getResource(name);
        assertNotNull(name + " was not found", agentJson);

        return new File(agentJson.toURI());
    }
}
