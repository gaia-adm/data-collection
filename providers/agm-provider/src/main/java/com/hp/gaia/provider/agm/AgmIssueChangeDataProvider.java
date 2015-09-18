package com.hp.gaia.provider.agm;

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
 * Data provider for AGM issue change data collection
 */
@Component
public class AgmIssueChangeDataProvider implements DataProvider{

    private static final Logger logger = LogManager.getLogger(AgmIssueChangeDataProvider.class);

    @Override
    public String getProviderId() {
        return "agm/issue/change";
    }

    @Override
    public DataStream fetchData(Map<String, String> properties, CredentialsProvider credentialsProvider, ProxyProvider proxyProvider, String bookmark, boolean inclusive) throws AccessDeniedException, InvalidConfigurationException {

        AgmIssueChangeDataConfig dataConfig = getAgmDataConfig(properties);
        logger.debug("About to fetch issue change data for domain " + dataConfig.getDomain() + ", project " + dataConfig.getProject());
        return new AgmIssueChangeDataStream(dataConfig, credentialsProvider, proxyProvider, bookmark, inclusive, getProviderId());
    }

    private AgmIssueChangeDataConfig getAgmDataConfig(Map<String, String> properties) {

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
        String tenantId = properties.get("tenantId");
        if(StringUtils.isEmpty(tenantId)){
            throw new InvalidConfigurationException("tenantId is missing");
        }

        return new AgmIssueChangeDataConfig(location, domain, project, tenantId);
    }
}
