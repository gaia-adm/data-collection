package com.hp.gaia.provider.alm;

import com.hp.gaia.provider.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public abstract class AlmDataProvider implements DataProvider {

    private static final Logger logger = LogManager.getLogger(AlmDataProvider.class);

    protected abstract StateMachine createStateMachine(AlmDataConfig dataConfig, CredentialsProvider credentialsProvider, ProxyProvider proxyProvider, String bookmark, boolean inclusive, String providerId);

    @Override
    public DataStream fetchData(Map<String, String> properties, CredentialsProvider credentialsProvider, ProxyProvider proxyProvider, String bookmark, boolean inclusive) throws AccessDeniedException, InvalidConfigurationException {

        AlmDataConfig dataConfig = getAlmDataConfig(properties);
        logger.debug("About to fetch issue change data for domain " + dataConfig.getDomain() + ", project " + dataConfig.getProject());

        return new AlmDataStream(createStateMachine(dataConfig, credentialsProvider, proxyProvider, bookmark, inclusive, getProviderId()));
    }

    private AlmDataConfig getAlmDataConfig(Map<String, String> properties) {

        URI location;
        String locationString = properties.get("location");
        if (StringUtils.isEmpty(locationString)) {
            throw new InvalidConfigurationException("location is missing");
        }
        try {
            location = new URI(locationString);
        } catch (URISyntaxException e) {
            throw new InvalidConfigurationException("location is not a valid URI");
        }
        String domain = properties.get("domain");
        if (StringUtils.isEmpty(domain)) {
            throw new InvalidConfigurationException("domain is missing");
        }
        String project = properties.get("project");
        if (StringUtils.isEmpty(project)) {
            throw new InvalidConfigurationException("project is missing");
        }
        String historyDaysString = properties.get("init_history_days");
        if (StringUtils.isEmpty(historyDaysString)) {
            logger.warn("init_history_days property is empty for " + location + ", project " + project + ", domain" + domain + ". Using default value: 60");
            historyDaysString = "60";
        }

        int historyDays = 60;
        try {
            historyDays = Integer.parseInt(historyDaysString);
            if (historyDays < 1 || historyDays > 365) {
                logger.error("init_history_days property value is invalid (" + historyDaysString + ") for " + location + ", project " + project + ", domain" + domain + ". Using default value: 60");
            }
        } catch (NumberFormatException nfe) {
            logger.error("init_history_days property value is invalid (" + historyDaysString + ") for " + location + ", project " + project + ", domain" + domain + ". Using default value: 60");
        }

        return new AlmDataConfig(location, domain, project, historyDays);
    }
}
