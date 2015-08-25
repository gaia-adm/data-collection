package com.hp.gaia.provider.alm;

import com.hp.gaia.provider.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by belozovs on 8/23/2015.
 * Data provider for ALM issue change data collection
 */
@Component
public class AlmIssueChangeDataProvider implements DataProvider{

    private static final Logger logger = LogManager.getLogger(AlmIssueChangeDataProvider.class);

    @Override
    public String getProviderId() {
        return "alm/issue/change";
    }

    @Override
    public DataStream fetchData(Map<String, String> properties, CredentialsProvider credentialsProvider, ProxyProvider proxyProvider, String bookmark, boolean inclusive) throws AccessDeniedException, InvalidConfigurationException {

        AlmIssueChangeDataConfig dataConfig = getAlmDataConfig(properties);
        logger.debug("About to fetch issue change data for domain " + dataConfig.getDomain() + ", project " + dataConfig.getProject());
        return new AlmIssueChangeDataStream(dataConfig, credentialsProvider, proxyProvider, bookmark, inclusive, getProviderId());
    }

    private AlmIssueChangeDataConfig getAlmDataConfig(Map<String, String> properties) {

        URI location;

        String locationString = properties.get("location");
        if(StringUtils.isEmpty(locationString)) {
            throw new InvalidConfigurationException("location is missing");
        }
        try {
            location = new URI(locationString);
        } catch (URISyntaxException e) {
            throw new InvalidConfigurationException("location is not a valid URI");
        }
        String domain = properties.get("domain");
        if(StringUtils.isEmpty(domain)){
            throw new InvalidConfigurationException("domain is missing");
        }
        String project = properties.get("project");
        if(StringUtils.isEmpty(project)){
            throw new InvalidConfigurationException("project is missing");
        }
        return new AlmIssueChangeDataConfig(location , domain, project);
    }
}
