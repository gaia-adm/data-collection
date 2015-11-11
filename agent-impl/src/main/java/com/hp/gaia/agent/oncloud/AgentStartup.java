package com.hp.gaia.agent.oncloud;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.MalformedURLException;
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
        // startup Spring context
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"classpath*:/Spring/gaiacloud-agent-context.xml"}, false);
        context.refresh();
        context.registerShutdownHook();
        return context;
    }

}
