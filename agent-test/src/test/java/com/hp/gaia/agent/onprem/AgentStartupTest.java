package com.hp.gaia.agent.onprem;

import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AgentStartupTest {

    @Test
    public void testStartup() throws MalformedURLException, URISyntaxException {
        Map<String, String> env = new HashMap<>(System.getenv());
        File home = getHomeDir("agent_startup_home/home.locator");
        env.put("GAIA_AGENT_HOME", home.getAbsolutePath());
        // startup full spring context
        ConfigurableApplicationContext applicationContext = AgentStartup.init(env);
        applicationContext.stop();
        applicationContext.close();
    }

    private static File getHomeDir(String name) throws URISyntaxException {
        URL agentJson = AgentStartupTest.class.getClassLoader().getResource(name);
        assertNotNull(name + " was not found", agentJson);
        return new File(agentJson.toURI()).getParentFile();
    }
}
