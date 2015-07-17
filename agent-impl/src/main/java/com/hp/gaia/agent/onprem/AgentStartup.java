package com.hp.gaia.agent.onprem;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

/**
 * Main class for on-premise agent startup. Handles command line options.
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

    private static void init() {
        GlobalSettings.setWorkingDir(getWorkingDir());
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
