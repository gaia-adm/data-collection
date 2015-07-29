package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.MyData;
import com.hp.gaia.agent.MyHttpRequestHandler;
import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.provider.MetadataConstants;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.SocketUtils;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests against mock result-upload-service using nanohttpd.
 */
public class OnPremResultUploadServiceTest extends EasyMockSupport {

    private OnPremResultUploadService onPremResultUploadService;

    private OnPremAgentConfigService agentConfigService;

    private MyHttpRequestHandler myHttpRequestHandler;

    private HttpServer server;

    private int port;

    @Before
    public void before() throws IOException {
        port = SocketUtils.findAvailableTcpPort();
        myHttpRequestHandler = new MyHttpRequestHandler();
        server = ServerBootstrap.bootstrap()
                .setListenerPort(port)
                .setServerInfo("Test/1.1")
                .registerHandler("*", myHttpRequestHandler)
                .create();
        server.start();

        onPremResultUploadService = new OnPremResultUploadService();
        agentConfigService = createMock(OnPremAgentConfigService.class);
        onPremResultUploadService.setAgentConfigService(agentConfigService);
    }

    @After
    public void after() {
        onPremResultUploadService.shutdown();
        server.stop();
    }

    @Test
    public void testSendData200() {
        // expectations
        expect(agentConfigService.getConnectTimeout()).andReturn(30000);
        expect(agentConfigService.getSoTimeout()).andReturn(60000);
        expect(agentConfigService.getGaiaLocation()).andReturn("http://localhost:" + port);
        expect(agentConfigService.getProxy()).andReturn(null).anyTimes();
        expect(agentConfigService.getAccessToken()).andReturn("myAccessToken").anyTimes();
        // replay mocks
        replayAll();
        onPremResultUploadService.init(5);
        final String expectedUriPath = "/result-upload/rest/v1/upload-file";
        ProviderConfig providerConfig = new ProviderConfig("testConfigId", "testProviderId", null, null, null, 60);
        Map<String, String> metadata = new HashMap<>();
        metadata.put(MetadataConstants.METRIC, "testMetric");
        metadata.put(MetadataConstants.CATEGORY, "testCategory");
        String content = "{\"testKey\": \"testValue\"}";
        MyData myData = new MyData(metadata, "application/json; charset=utf-8", content.getBytes(Charset.forName("utf-8")), "bookmark");
        myHttpRequestHandler.setExpectedUriPath(expectedUriPath);
        onPremResultUploadService.sendData(providerConfig, myData);
        // verify HTTP request
        assertEquals(expectedUriPath, myHttpRequestHandler.getLastRequestUriPath());
        assertEquals(metadata, myHttpRequestHandler.getLastParams());
        Map<String, String> httpHeaders = myHttpRequestHandler.getLastHeaders();
        assertEquals("application/json; charset=utf-8", httpHeaders.get("Content-Type"));
        assertEquals("Bearer myAccessToken", httpHeaders.get("Authorization"));
        assertEquals("chunked", httpHeaders.get("Transfer-Encoding"));
        String lastContent = new String(myHttpRequestHandler.getLastContent(), Charset.forName("utf-8"));
        assertEquals(content, lastContent);
        // verify mocks
        verifyAll();
    }

    @Test
    public void testSendData500() {
        // expectations
        expect(agentConfigService.getConnectTimeout()).andReturn(30000);
        expect(agentConfigService.getSoTimeout()).andReturn(60000);
        expect(agentConfigService.getGaiaLocation()).andReturn("http://localhost:" + port);
        expect(agentConfigService.getProxy()).andReturn(null).anyTimes();
        expect(agentConfigService.getAccessToken()).andReturn("myAccessToken").anyTimes();
        // replay mocks
        replayAll();
        onPremResultUploadService.init(5);
        ProviderConfig providerConfig = new ProviderConfig("testConfigId", "testProviderId", null, null, null, 60);
        Map<String, String> metadata = new HashMap<>();
        metadata.put(MetadataConstants.METRIC, "testMetric");
        metadata.put(MetadataConstants.CATEGORY, "testCategory");
        String content = "{\"testKey\": \"testValue\"}";
        MyData myData = new MyData(metadata, "application/json; charset=utf-8", content.getBytes(Charset.forName("utf-8")), "bookmark");
        myHttpRequestHandler.setExpectedUriPath(null);
        try {
            onPremResultUploadService.sendData(providerConfig, myData);
            fail("Expected exception");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("status code 500"));
        }
        // verify mocks
        verifyAll();
    }
}
