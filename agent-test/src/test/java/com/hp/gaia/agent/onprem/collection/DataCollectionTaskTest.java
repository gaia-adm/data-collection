package com.hp.gaia.agent.onprem.collection;

import com.hp.gaia.agent.AgentIntegrationTest;
import com.hp.gaia.agent.MyData;
import com.hp.gaia.agent.collection.DataCollectionTask;
import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.onprem.service.OnPremCollectionStateService;
import com.hp.gaia.agent.onprem.service.OnPremCredentialsService;
import com.hp.gaia.agent.onprem.service.OnPremProvidersConfigService;
import com.hp.gaia.agent.service.CollectionState;
import com.hp.gaia.agent.service.CollectionState.Result;
import com.hp.gaia.agent.service.CollectionState.State;
import com.hp.gaia.agent.service.DataProviderRegistry;
import com.hp.gaia.agent.service.ResultUploadService;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.InvalidConfigurationException;
import com.hp.gaia.provider.test.TestProvider1;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@DirtiesContext
public class DataCollectionTaskTest extends AgentIntegrationTest {

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

    @Test
    public void testSuccess() throws URISyntaxException, IOException {
        onPremCredentialsService.init(getConfigFile("credentials_configs/collection_task.json"));
        onPremProvidersConfigService.init(getConfigFile("provider_configs/collection_task.json"));
        Path tempDir = Files.createTempDirectory("DataCollectionTaskTest");
        onPremCollectionStateService.init(tempDir.toFile());
        TestProvider1 testProvider1 = (TestProvider1) dataProviderRegistry.getDataProvider("testProvider1");
        testProvider1.reset();
        // expectations
        Capture<ProviderConfig> providerConfigCapture = new Capture<>();
        Capture<Data> dataCapture = new Capture<>();
        resultUploadService.sendData(capture(providerConfigCapture), capture(dataCapture));
        // replay mocks
        replay(resultUploadService);
        ProviderConfig providerConfig = onPremProvidersConfigService.getProviderConfig("testConfig1");
        CollectionState collectionState = new CollectionState("testConfig1");
        collectionState.setBookmark("bookmark1");
        dataCollectionTask.init(providerConfig, collectionState);
        dataCollectionTask.run();
        // verify provider
        Map<String, String> properties = testProvider1.getProperties();
        assertNotNull(properties);
        assertEquals(1, properties.size());
        assertEquals("Prague", properties.get("city"));
        Map<String, String> credentials = testProvider1.getCredentials();
        assertNotNull(credentials);
        assertEquals(3, credentials.size());
        assertEquals("testUsername", credentials.get("username"));
        assertEquals("testSecretKey", credentials.get("secretKey"));
        assertEquals("testPassword", credentials.get("password"));
        Proxy proxy = testProvider1.getProxy();
        InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
        assertEquals("proxy.bbn.hp.com", proxyAddress.getHostName());
        assertEquals(8082, proxyAddress.getPort());
        assertEquals("user2", testProvider1.getProxyUsername());
        assertEquals("secretPassword", testProvider1.getProxyPassword());
        assertEquals("bookmark1", testProvider1.getLastBookmark());
        assertTrue(testProvider1.isDataStreamClosed());
        // verify other objects
        MyData data = (MyData) dataCapture.getValue();
        assertTrue(data.isClosed());
        ProviderConfig capturedProviderConfig = providerConfigCapture.getValue();
        assertEquals(providerConfig, capturedProviderConfig);
        collectionState = onPremCollectionStateService.getCollectionState("testConfig1");
        assertEquals("bookmark2", collectionState.getBookmark());
        assertEquals(State.FINISHED, collectionState.getState());
        assertEquals(Result.SUCCESS, collectionState.getResult());
        assertNotNull(collectionState.getLastCollectionTimestamp());
        assertNotNull(collectionState.getNextCollectionTimestamp());
        verify(resultUploadService);
    }

    @Test
    public void testExceptionInFetchData() throws URISyntaxException, IOException {
        onPremCredentialsService.init(getConfigFile("credentials_configs/collection_task.json"));
        onPremProvidersConfigService.init(getConfigFile("provider_configs/collection_task.json"));
        Path tempDir = Files.createTempDirectory("DataCollectionTaskTest");
        onPremCollectionStateService.init(tempDir.toFile());
        TestProvider1 testProvider1 = (TestProvider1) dataProviderRegistry.getDataProvider("testProvider1");
        testProvider1.reset();
        testProvider1.setThrowExceptionInFetchData(new InvalidConfigurationException("Test exception"));
        // expectations
        // replay mocks
        replay(resultUploadService);
        ProviderConfig providerConfig = onPremProvidersConfigService.getProviderConfig("testConfig1");
        CollectionState collectionState = new CollectionState("testConfig1");
        collectionState.setBookmark("bookmark1");
        dataCollectionTask.init(providerConfig, collectionState);
        dataCollectionTask.run();
        // verify provider
        Map<String, String> properties = testProvider1.getProperties();
        assertNotNull(properties);
        assertEquals(1, properties.size());
        assertEquals("Prague", properties.get("city"));
        Map<String, String> credentials = testProvider1.getCredentials();
        assertNotNull(credentials);
        assertEquals(3, credentials.size());
        assertEquals("testUsername", credentials.get("username"));
        assertEquals("testSecretKey", credentials.get("secretKey"));
        assertEquals("testPassword", credentials.get("password"));
        Proxy proxy = testProvider1.getProxy();
        InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
        assertEquals("proxy.bbn.hp.com", proxyAddress.getHostName());
        assertEquals(8082, proxyAddress.getPort());
        assertEquals("user2", testProvider1.getProxyUsername());
        assertEquals("secretPassword", testProvider1.getProxyPassword());
        assertEquals("bookmark1", testProvider1.getLastBookmark());
        // verify other objects
        collectionState = onPremCollectionStateService.getCollectionState("testConfig1");
        assertEquals("bookmark1", collectionState.getBookmark());
        assertEquals(State.FINISHED, collectionState.getState());
        assertEquals(Result.FAILURE, collectionState.getResult());
        assertNotNull(collectionState.getLastCollectionTimestamp());
        assertNotNull(collectionState.getNextCollectionTimestamp());
        verify(resultUploadService);
    }

    @Test
    public void testExceptionInDataStreamNext() throws URISyntaxException, IOException {
        onPremCredentialsService.init(getConfigFile("credentials_configs/collection_task.json"));
        onPremProvidersConfigService.init(getConfigFile("provider_configs/collection_task.json"));
        Path tempDir = Files.createTempDirectory("DataCollectionTaskTest");
        onPremCollectionStateService.init(tempDir.toFile());
        TestProvider1 testProvider1 = (TestProvider1) dataProviderRegistry.getDataProvider("testProvider1");
        testProvider1.reset();
        testProvider1.setThrowExceptionInNext(new RuntimeException("Test exception"));
        // expectations
        // replay mocks
        replay(resultUploadService);
        ProviderConfig providerConfig = onPremProvidersConfigService.getProviderConfig("testConfig1");
        CollectionState collectionState = new CollectionState("testConfig1");
        collectionState.setBookmark("bookmark1");
        dataCollectionTask.init(providerConfig, collectionState);
        dataCollectionTask.run();
        // verify provider
        Map<String, String> properties = testProvider1.getProperties();
        assertNotNull(properties);
        assertEquals(1, properties.size());
        assertEquals("Prague", properties.get("city"));
        Map<String, String> credentials = testProvider1.getCredentials();
        assertNotNull(credentials);
        assertEquals(3, credentials.size());
        assertEquals("testUsername", credentials.get("username"));
        assertEquals("testSecretKey", credentials.get("secretKey"));
        assertEquals("testPassword", credentials.get("password"));
        Proxy proxy = testProvider1.getProxy();
        InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
        assertEquals("proxy.bbn.hp.com", proxyAddress.getHostName());
        assertEquals(8082, proxyAddress.getPort());
        assertEquals("user2", testProvider1.getProxyUsername());
        assertEquals("secretPassword", testProvider1.getProxyPassword());
        assertEquals("bookmark1", testProvider1.getLastBookmark());
        assertTrue(testProvider1.isDataStreamClosed());
        // verify other objects
        collectionState = onPremCollectionStateService.getCollectionState("testConfig1");
        assertEquals("bookmark1", collectionState.getBookmark());
        assertEquals(State.FINISHED, collectionState.getState());
        assertEquals(Result.FAILURE, collectionState.getResult());
        assertNotNull(collectionState.getLastCollectionTimestamp());
        assertNotNull(collectionState.getNextCollectionTimestamp());
        verify(resultUploadService);
    }

    private static File getConfigFile(String name) throws URISyntaxException {
        URL agentJson = DataCollectionTaskTest.class.getClassLoader().getResource(name);
        assertNotNull(name + " was not found", agentJson);
        return new File(agentJson.toURI());
    }
}
