package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.AgentIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

@DirtiesContext
public class OnPremCredentialsServiceTest extends AgentIntegrationTest {

    @Autowired
    private OnPremCredentialsService onPremCredentialsService;

    @Test
    public void testConfigEmpty() throws URISyntaxException {
        File configFile = getConfigFile("credentials_configs/credentials_empty.json");
        onPremCredentialsService.init(configFile);
        // invalid id
        try {
            onPremCredentialsService.getCredentials("someId");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    @Test
    public void testConfigOne() throws URISyntaxException {
        File configFile = getConfigFile("credentials_configs/credentials_one.json");
        onPremCredentialsService.init(configFile);
        // invalid id
        try {
            onPremCredentialsService.getCredentials("invalidId");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        // valid id
        Map<String, String> credentials = onPremCredentialsService.getCredentials("testCredentialsId");
        assertNotNull(credentials);
        assertEquals(3, credentials.size());
        assertEquals("testPassword", credentials.get("password"));
        assertEquals("testSecretKey", credentials.get("secretKey"));
        assertEquals("testUsername", credentials.get("username"));
    }

    @Test
    public void testConfigTwo() throws URISyntaxException {
        File configFile = getConfigFile("credentials_configs/credentials_two.json");
        onPremCredentialsService.init(configFile);
        // valid id1
        Map<String, String> credentials1 = onPremCredentialsService.getCredentials("testCredentialsId1");
        assertNotNull(credentials1);
        assertEquals(3, credentials1.size());
        assertEquals("testPassword1", credentials1.get("password"));
        assertEquals("testSecretKey1", credentials1.get("secretKey"));
        assertEquals("testUsername1", credentials1.get("username"));
        // valid id2
        Map<String, String> credentials2 = onPremCredentialsService.getCredentials("testCredentialsId2");
        assertNotNull(credentials2);
        assertEquals(3, credentials2.size());
        assertEquals("testPassword2", credentials2.get("password"));
        assertEquals("testSecretKey2", credentials2.get("secretKey"));
        assertEquals("testUsername2", credentials2.get("username"));
    }

    private static File getConfigFile(String name) throws URISyntaxException {
        URL agentJson = OnPremCredentialsServiceTest.class.getClassLoader().getResource(name);
        assertNotNull(name + " was not found", agentJson);
        return new File(agentJson.toURI());
    }

}
