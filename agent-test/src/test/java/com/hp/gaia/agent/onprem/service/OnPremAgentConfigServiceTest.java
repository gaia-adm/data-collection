package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.AgentIntegrationTest;
import com.hp.gaia.agent.config.ProtectedValue.Type;
import com.hp.gaia.agent.config.Proxy;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

@DirtiesContext
public class OnPremAgentConfigServiceTest extends AgentIntegrationTest {

    @Autowired
    private OnPremAgentConfigService onPremAgentConfigService;

    @Test
    public void testConfigMinimal() throws URISyntaxException {
        File configFile = getConfigFile("agent_configs/agent_minimal.json");
        onPremAgentConfigService.init(configFile);
        assertEquals("http://result-upload-service:8084", onPremAgentConfigService.getGaiaLocation());
        assertEquals("accessToken1", onPremAgentConfigService.getAccessToken());
        assertEquals(30000, onPremAgentConfigService.getConnectTimeout());
        assertEquals(60000, onPremAgentConfigService.getSoTimeout());
        assertEquals(5, onPremAgentConfigService.getWorkerPool());
        assertNull(onPremAgentConfigService.getProxy());
    }

    @Test
    public void testConfigPlain() throws URISyntaxException {
        File configFile = getConfigFile("agent_configs/agent_plain.json");
        onPremAgentConfigService.init(configFile);
        assertEquals("http://result-upload-service:8085", onPremAgentConfigService.getGaiaLocation());
        assertEquals("accessToken2", onPremAgentConfigService.getAccessToken());
        assertEquals(20000, onPremAgentConfigService.getConnectTimeout());
        assertEquals(40000, onPremAgentConfigService.getSoTimeout());
        assertEquals(6, onPremAgentConfigService.getWorkerPool());
        Proxy proxy = onPremAgentConfigService.getProxy();
        assertNotNull(proxy);
        assertEquals("http://proxy.bbn.hp.com:8081", proxy.getHttpProxy());
        assertEquals("user1", proxy.getHttpProxyUser());
        assertEquals("secretPassword1", proxy.getHttpProxyPassword().getValue());
        assertEquals(Type.PLAIN, proxy.getHttpProxyPassword().getType());
    }

    @Test
    public void testConfigEncrypt() throws URISyntaxException {
        File configFile = getConfigFile("agent_configs/agent_encrypt.json");
        onPremAgentConfigService.init(configFile);
        assertEquals("http://result-upload-service:8086", onPremAgentConfigService.getGaiaLocation());
        assertEquals("accessToken3", onPremAgentConfigService.getAccessToken());
        assertEquals(20000, onPremAgentConfigService.getConnectTimeout());
        assertEquals(40000, onPremAgentConfigService.getSoTimeout());
        assertEquals(6, onPremAgentConfigService.getWorkerPool());
        Proxy proxy = onPremAgentConfigService.getProxy();
        assertNotNull(proxy);
        assertEquals("http://proxy.bbn.hp.com:8082", proxy.getHttpProxy());
        assertEquals("user3", proxy.getHttpProxyUser());
        assertEquals("rCj8wi54deMstZIzLTA8/g==", proxy.getHttpProxyPassword().getValue());
        assertEquals(Type.ENCRYPTED, proxy.getHttpProxyPassword().getType());
    }

    @Test
    public void testConfigEncrypted() throws URISyntaxException {
        File configFile = getConfigFile("agent_configs/agent_encrypted.json");
        onPremAgentConfigService.init(configFile);
        assertEquals("http://result-upload-service:8087", onPremAgentConfigService.getGaiaLocation());
        assertEquals("accessToken3", onPremAgentConfigService.getAccessToken());
        assertEquals(30000, onPremAgentConfigService.getConnectTimeout());
        assertEquals(50000, onPremAgentConfigService.getSoTimeout());
        assertEquals(7, onPremAgentConfigService.getWorkerPool());
        Proxy proxy = onPremAgentConfigService.getProxy();
        assertNotNull(proxy);
        assertEquals("http://proxy.bbn.hp.com:8083", proxy.getHttpProxy());
        assertEquals("user3", proxy.getHttpProxyUser());
        assertEquals("rCj8wi54deMstZIzLTA8/g==", proxy.getHttpProxyPassword().getValue());
        assertEquals(Type.ENCRYPTED, proxy.getHttpProxyPassword().getType());
    }

    private static File getConfigFile(String name) throws URISyntaxException {
        URL agentJson = OnPremAgentConfigServiceTest.class.getClassLoader().getResource(name);
        assertNotNull(name + " was not found", agentJson);
        return new File(agentJson.toURI());
    }
}
