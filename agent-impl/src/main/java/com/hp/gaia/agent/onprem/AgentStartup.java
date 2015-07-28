package com.hp.gaia.agent.onprem;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Main class for on-premise agent startup.
 */
public class AgentStartup {

    public static void main(String[] args) {
        try {
            init();
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

    private static void init() throws MalformedURLException {
        GlobalSettings.setWorkingDir(getWorkingDir());
        // setup logging configuration
        File logFile = GlobalSettings.getConfigFile("log4j2.xml");
        URL logFileUrl = Paths.get(logFile.toURI()).toUri().toURL();
        System.getProperties().put("log4j.configurationFile", logFileUrl.toString());
        // startup Spring context
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"classpath*:/Spring/gaia-*-context.xml"}, false);
        context.refresh();
        context.registerShutdownHook();
    }

    private static String getWorkingDir() {
        String gaiaAgentHome = System.getenv("GAIA_AGENT_HOME");
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
