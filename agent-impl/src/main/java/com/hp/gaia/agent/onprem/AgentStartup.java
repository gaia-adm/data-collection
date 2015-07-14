package com.hp.gaia.agent.onprem;

import com.hp.gaia.agent.config.AgentConfig;
import com.hp.gaia.agent.config.ConfigFactory;
import com.hp.gaia.agent.config.CredentialsConfig;
import com.hp.gaia.agent.config.ProvidersConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for on-premise agent startup. Handles command line options.
 */
public class AgentStartup {

    private static Logger logger = Logger.getLogger(AgentStartup.class.getName());

    private static final String PROVIDERS_CONFIG = "providers.json";
    private static final String AGENT_CONFIG = "agent.json";
    private static final String CREDENTIALS_CONFIG = "credentials.json";

    public static void main(String[] args) {
        Options opts = createOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(opts, args);
        } catch (Exception e) {
            System.err.println("Failed to parse command line options: " + e.getMessage());
            printHelp(opts);
            System.exit(2);
        }
        if (cmd.hasOption("h")) {
            printHelp(opts);
            System.exit(0);
        }
        File providersConfigFile = getConfigFile(cmd, PROVIDERS_CONFIG, "p");
        File agentConfigFile = getConfigFile(cmd, AGENT_CONFIG, "a");
        File credentialsConfigFile = getConfigFile(cmd, CREDENTIALS_CONFIG, "c");

        // verify files exist
        try {
            verifyFile(providersConfigFile);
            verifyFile(agentConfigFile);
            verifyFile(credentialsConfigFile);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            printHelp(opts);
            System.exit(2);
        }

        // parse configuration files
        ProvidersConfig providersConfig;
        AgentConfig agentConfig;
        CredentialsConfig credentialsConfig;
        try {
            providersConfig = ConfigFactory.readConfig(providersConfigFile, ProvidersConfig.class);
            agentConfig = ConfigFactory.readConfig(agentConfigFile, AgentConfig.class);
            credentialsConfig = ConfigFactory.readCredentialsConfig(credentialsConfigFile);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception while reading configuration file", e);
            System.exit(3);
        }
    }

    private static File getConfigFile(CommandLine cmd, final String defaultConfigName, final String optionName) {
        String filePath = cmd.getOptionValue(optionName);
        File file;
        if (StringUtils.isEmpty(filePath)) {
            file = new File(defaultConfigName);
        } else {
            file = new File(filePath);
        }
        return file;
    }

    private static void verifyFile(File file) {
        if (!file.exists()) {
            throw new RuntimeException("Configuration file " + file.getAbsolutePath() + " doesn't exist");
        }
        if (!file.canRead()) {
            throw new RuntimeException("Configuration file " + file.getAbsolutePath() + " is not readable");
        }
    }

    private static void printHelp(Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("gaia-agent", opts);
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(Option.builder("p")
                .hasArg(true)
                .argName("FILE")
                .longOpt("providers-config")
                .desc("path to providers.json file configuring data providers")
                .required(false)
                .build());
        options.addOption(Option.builder("c")
                .hasArg(true)
                .argName("FILE")
                .longOpt("credentials-config")
                .desc("path to credentials.json file storing credentials")
                .required(false)
                .build());
        options.addOption(Option.builder("a")
                .hasArg(true)
                .argName("FILE")
                .longOpt("agent-config")
                .desc("path to agent.json file storing agent configuration")
                .required(false)
                .build());
        options.addOption(Option.builder("h").hasArg(false)
                .longOpt("help")
                .desc("prints help")
                .required(false)
                .build());
        return options;
    }
}
