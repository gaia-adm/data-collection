package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.AgentIntegrationTest;
import com.hp.gaia.agent.config.ProtectedValue.Type;
import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.config.Proxy;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

@DirtiesContext
public class OnPremProvidersConfigServiceTest extends AgentIntegrationTest {

    @Autowired
    private OnPremProvidersConfigService onPremProvidersConfigService;

    @Test
    public void testConfigEmpty1() throws URISyntaxException {
        File configFile = getConfigFile("provider_configs/providers_empty1.json");
        onPremProvidersConfigService.init(configFile);
        assertEquals(0, onPremProvidersConfigService.getProviderConfigs().size());
        // invalid id
        try {
            onPremProvidersConfigService.getProviderConfig("invalidId");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        assertFalse(onPremProvidersConfigService.isProviderConfig("invalidId"));
    }

    @Test
    public void testConfigEmpty2() throws URISyntaxException {
        File configFile = getConfigFile("provider_configs/providers_empty2.json");
        onPremProvidersConfigService.init(configFile);
        assertEquals(0, onPremProvidersConfigService.getProviderConfigs().size());
        // invalid id
        try {
            onPremProvidersConfigService.getProviderConfig("invalidId");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        assertFalse(onPremProvidersConfigService.isProviderConfig("invalidId"));
    }

    @Test
    public void testConfigOne() throws URISyntaxException {
        File configFile = getConfigFile("provider_configs/providers_one.json");
        onPremProvidersConfigService.init(configFile);
        assertEquals(1, onPremProvidersConfigService.getProviderConfigs().size());

        ProviderConfig providerConfig = onPremProvidersConfigService.getProviderConfig("testConfig1");
        assertNotNull(providerConfig);
        assertEquals("testConfig1", providerConfig.getConfigId());
        assertEquals("testProviderId1", providerConfig.getProviderId());
        assertEquals("credentials1", providerConfig.getCredentialsId());
        assertEquals((Integer)40, providerConfig.getRunPeriod());
        Map<String, String> properties = providerConfig.getProperties();
        assertNotNull(properties);
        assertEquals(1, properties.size());
        assertEquals("Prague", properties.get("city"));
        Proxy proxy = providerConfig.getProxy();
        assertNotNull(proxy);
        assertEquals("http://proxy.bbn.hp.com:8082", proxy.getHttpProxy());
        assertEquals("user2", proxy.getHttpProxyUser());
        assertEquals("secretPassword", proxy.getHttpProxyPassword().getValue());
        assertEquals(Type.PLAIN, proxy.getHttpProxyPassword().getType());

        assertFalse(onPremProvidersConfigService.isProviderConfig("invalidId"));
        assertTrue(onPremProvidersConfigService.isProviderConfig("testConfig1"));
    }

    @Test
    public void testConfigTwo() throws URISyntaxException {
        File configFile = getConfigFile("provider_configs/providers_two.json");
        onPremProvidersConfigService.init(configFile);
        assertEquals(2, onPremProvidersConfigService.getProviderConfigs().size());

        ProviderConfig providerConfig1 = onPremProvidersConfigService.getProviderConfig("testConfig1");
        assertNotNull(providerConfig1);
        assertEquals("testConfig1", providerConfig1.getConfigId());
        assertEquals("testProviderId1", providerConfig1.getProviderId());
        assertNull(providerConfig1.getCredentialsId());
        assertEquals((Integer) 40, providerConfig1.getRunPeriod());
        Map<String, String> properties1 = providerConfig1.getProperties();
        assertNotNull(properties1);
        assertEquals(1, properties1.size());
        assertEquals("Prague", properties1.get("city"));
        Proxy proxy1 = providerConfig1.getProxy();
        assertNotNull(proxy1);
        assertEquals("http://proxy.bbn.hp.com:8082", proxy1.getHttpProxy());
        assertEquals("user2", proxy1.getHttpProxyUser());
        assertEquals("secretPassword2", proxy1.getHttpProxyPassword().getValue());
        assertEquals(Type.PLAIN, proxy1.getHttpProxyPassword().getType());

        ProviderConfig providerConfig2 = onPremProvidersConfigService.getProviderConfig("testConfig2");
        assertNotNull(providerConfig2);
        assertEquals("testConfig2", providerConfig2.getConfigId());
        assertEquals("testProviderId2", providerConfig2.getProviderId());
        assertEquals("credentials2", providerConfig2.getCredentialsId());
        assertEquals((Integer) 50, providerConfig2.getRunPeriod());
        Map<String, String> properties2 = providerConfig2.getProperties();
        assertNotNull(properties2);
        assertEquals(0, properties2.size());
        Proxy proxy2 = providerConfig2.getProxy();
        assertNotNull(proxy2);
        assertEquals("http://proxy.bbn.hp.com:8083", proxy2.getHttpProxy());
        assertEquals("user3", proxy2.getHttpProxyUser());
        assertEquals("secretPassword1", proxy2.getHttpProxyPassword().getValue());
        assertEquals(Type.PLAIN, proxy2.getHttpProxyPassword().getType());

        assertFalse(onPremProvidersConfigService.isProviderConfig("invalidId"));
        assertTrue(onPremProvidersConfigService.isProviderConfig("testConfig1"));
        assertTrue(onPremProvidersConfigService.isProviderConfig("testConfig2"));
    }

    private static File getConfigFile(String name) throws URISyntaxException {
        URL agentJson = OnPremProvidersConfigServiceTest.class.getClassLoader().getResource(name);
        assertNotNull(name + " was not found", agentJson);
        return new File(agentJson.toURI());
    }

}
