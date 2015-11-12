package com.hp.gaia.agent.oncloud;

import com.hp.gaia.agent.oncloud.GlobalSettings;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Main class for on-cloud agent startup.
 */
public class AgentStartup {

    public static void main(String[] args) {
        try {
            init(System.getenv());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        // wait until interrupted
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    // for tests
    static ConfigurableApplicationContext init(final Map<String, String> env) throws MalformedURLException {
        GlobalSettings.setWorkingDir(getWorkingDir(env));
        // setup logging configuration
        File logFile = GlobalSettings.getConfigFile("log4j2.xml");
        URL logFileUrl = Paths.get(logFile.toURI()).toUri().toURL();
        System.getProperties().put("log4j.configurationFile", logFileUrl.toString());

        // startup Spring context
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"classpath*:/Spring/gaiacloud-agent-context.xml", "classpath*:/Spring/gaia-agent-shared-context.xml", "classpath*:/Spring/gaia-*-provider-context.xml"}, false);
        context.refresh();
        context.registerShutdownHook();
        return context;
    }


    private static String getWorkingDir(final Map<String, String> env) {
        String gaiaAgentHome = env.get("GAIA_AGENT_HOME");
        if (StringUtils.isEmpty(gaiaAgentHome)) {
            return new File("").getAbsolutePath();
        } else {
            File gaiaAgentHomeFile = new File(gaiaAgentHome);
            if (!gaiaAgentHomeFile.exists() || !gaiaAgentHomeFile.isDirectory()) {
                throw new IllegalStateException(gaiaAgentHomeFile.getAbsolutePath() + " must be a directory");
            }
            return gaiaAgentHomeFile.getAbsolutePath();
        }
    }

}
