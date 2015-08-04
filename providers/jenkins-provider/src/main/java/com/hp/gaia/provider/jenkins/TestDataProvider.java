package com.hp.gaia.provider.jenkins;

import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.DataProvider;
import com.hp.gaia.provider.DataStream;
import com.hp.gaia.provider.InvalidConfigurationException;
import com.hp.gaia.provider.ProxyProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Jenkins provider of test data.
 */
public class TestDataProvider implements DataProvider {

    private static final Logger logger = LogManager.getLogger(TestDataProvider.class);

    @Override
    public String getProviderId() {
        return "jenkins/test";
    }

    @Override
    public DataStream fetchData(final Map<String, String> properties, final CredentialsProvider credentialsProvider,
                                final ProxyProvider proxyProvider, final String bookmark, final boolean inclusive)
            throws AccessDeniedException, InvalidConfigurationException {
        TestDataConfiguration testDataConfiguration = getTestDataConfiguration(properties);
        logger.debug("About to fetch test data for " + testDataConfiguration.getJob());
        return new TestDataStream(testDataConfiguration, credentialsProvider, proxyProvider, bookmark, inclusive);
    }

    private static TestDataConfiguration getTestDataConfiguration(final Map<String, String> properties) {
        String location = properties.get("location");
        if (StringUtils.isEmpty(location)) {
            throw new InvalidConfigurationException("location is missing");
        }

        URI locationUri;
        try {
            locationUri = new URI(location);
        } catch (URISyntaxException e) {
            throw new InvalidConfigurationException("Invalid location URI", e);
        }
        String job = properties.get("job");
        if (StringUtils.isEmpty(job)) {
            throw new InvalidConfigurationException("job is missing");
        }
        return new TestDataConfiguration(locationUri, job);
    }
}
