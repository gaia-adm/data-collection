package com.hp.gaia.provider.jenkins;

import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.DataProvider;
import com.hp.gaia.provider.DataStream;
import com.hp.gaia.provider.InvalidConfigurationException;
import com.hp.gaia.provider.ProxyProvider;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Jenkins provider of test data.
 */
public class TestDataProvider implements DataProvider {

    @Override
    public String getProviderId() {
        return "jenkins/test";
    }

    @Override
    public DataStream fetchData(final Map<String, String> properties, final CredentialsProvider credentialsProvider,
                                final ProxyProvider proxyProvider, final String bookmark, final boolean inclusive)
            throws AccessDeniedException, InvalidConfigurationException {
        TestDataConfiguration testDataConfiguration = getTestDataConfiguration(properties);
        // TODO: add bookmark & inclusive
        return new TestDataStream(testDataConfiguration, credentialsProvider, proxyProvider);
    }

    private static TestDataConfiguration getTestDataConfiguration(final Map<String, String> properties) {
        String location = properties.get("location");
        if (StringUtils.isEmpty(location)) {
            throw new InvalidConfigurationException("location is missing");
        }

        URL locationUrl;
        try {
            locationUrl = new URL(location);
        } catch (MalformedURLException e) {
            throw new InvalidConfigurationException("Invalid location URL", e);
        }
        String job = properties.get("job");
        if (StringUtils.isEmpty(job)) {
            throw new InvalidConfigurationException("job is missing");
        }
        return new TestDataConfiguration(locationUrl, job);
    }
}
