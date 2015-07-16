package com.hp.gaia.agent.service;

import com.hp.gaia.provider.DataProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of all discovered {@link DataProvider} s.
 */
public class DataProviderRegistry {

    private Map<String, DataProvider> dataProviderMap;

    @Autowired(required = false)
    private List<DataProvider> dataProviders;

    @PostConstruct
    private void init() {
        dataProviderMap = new HashMap<>();
        if (dataProviders != null) {
            for (DataProvider dataProvider : dataProviders) {
                if (dataProviderMap.containsKey(dataProvider.getProviderId())) {
                    throw new IllegalStateException("Duplicate providerId '" + dataProvider.getProviderId() + "'");
                }
                dataProviderMap.put(dataProvider.getProviderId(), dataProvider);
            }
        }
    }

    /**
     * Returns instance of {@link DataProvider} for given providerId.
     *
     * @throws IllegalArgumentException if the providerId is invalid
     */
    public DataProvider getDataProvider(String providerId) {
        DataProvider dataProvider = dataProviderMap.get(providerId);
        if (dataProvider == null) {
            throw new IllegalStateException("Provider '" + providerId + "' not found");
        }
        return dataProvider;
    }
}
