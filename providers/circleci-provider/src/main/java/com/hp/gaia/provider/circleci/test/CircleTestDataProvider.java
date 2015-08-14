package com.hp.gaia.provider.circleci.test;

import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.DataProvider;
import com.hp.gaia.provider.DataStream;
import com.hp.gaia.provider.InvalidConfigurationException;
import com.hp.gaia.provider.ProxyProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Circle-CI provider of test data.
 */
@Component
public class CircleTestDataProvider implements DataProvider {

    private static final Logger logger = LogManager.getLogger(CircleTestDataProvider.class);

    @Override
    public String getProviderId() {
        return "circleci/test";
    }

    @Override
    public DataStream fetchData(final Map<String, String> properties, final CredentialsProvider credentialsProvider,
                                final ProxyProvider proxyProvider, final String bookmark, final boolean inclusive)
            throws AccessDeniedException, InvalidConfigurationException {
        CircleTestDataConfig testDataConfig = getCircleTestDataConfig(properties);
        logger.debug("About to fetch test data for " + testDataConfig.getProject());
        return new CircleTestDataStream(testDataConfig, credentialsProvider, proxyProvider, bookmark, inclusive);
    }

    private static CircleTestDataConfig getCircleTestDataConfig(final Map<String, String> properties) {
        String username = properties.get("username");
        if (StringUtils.isEmpty(username)) {
            throw new InvalidConfigurationException("username is missing");
        }
        String project = properties.get("project");
        if (StringUtils.isEmpty(project)) {
            throw new InvalidConfigurationException("project is missing");
        }
        return new CircleTestDataConfig(username, project);
    }
}
