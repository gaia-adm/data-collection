package com.hp.gaia.provider.jenkins.test;

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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Jenkins provider of test data.
 */
@Component
public class JenkinsTestDataProvider implements DataProvider {

    private static final Logger logger = LogManager.getLogger(JenkinsTestDataProvider.class);

    @Override
    public String getProviderId() {
        return "jenkins/test";
    }

    @Override
    public DataStream fetchData(final Map<String, String> properties, final CredentialsProvider credentialsProvider,
                                final ProxyProvider proxyProvider, final String bookmark, final boolean inclusive)
            throws AccessDeniedException, InvalidConfigurationException {
        JenkinsTestDataConfig testDataConfiguration = getTestDataConfiguration(properties);
        logger.debug("About to fetch test data for " + testDataConfiguration.getJob());
        return new JenkinsTestDataStream(testDataConfiguration, credentialsProvider, proxyProvider, bookmark, inclusive);
    }

    private static JenkinsTestDataConfig getTestDataConfiguration(final Map<String, String> properties) {
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
        String customTagsStr = properties.get("customTags");
        List<String> customTagsList = new ArrayList<>();
        if (!StringUtils.isEmpty(customTagsStr)) {
            String[] customTagsArr = StringUtils.split(customTagsStr, ',');
            for (String customTag : customTagsArr) {
                customTagsList.add(StringUtils.trim(customTag));
            }
        }
        return new JenkinsTestDataConfig(customTagsList, locationUri, job);
    }
}
