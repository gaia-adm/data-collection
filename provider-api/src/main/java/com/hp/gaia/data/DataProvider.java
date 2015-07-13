package com.hp.gaia.data;

import java.util.Map;

public interface DataProvider {

    /**
     * Fetches one or more blocks of data for given bookmark. Each block may be a file or collection of items (issues,
     * tests).
     *
     * @param configuration immutable {@link Map} with configuration parameters necessary for connecting to remote
     * system. Parameters are implementation specific. Credentials are to be stored separately.
     * @param credentialsProvider provides credentials for fetching data from remote system
     * @param bookmark bookmark since when to fetch data. It may be an id, timestamp or any value defined by
     * implementation. It may also be a structured value i.e JSON object.
     * @param inclusive whether to include data with given bookmark or only later. Can be used to choose between re-read
     * of a data block or read of the next data block.
     * @return instance of {@link DataStream} containing individual data blocks. May contain just one or multiple data
     * blocks.
     */
    DataStream fetchData(Map<String, String> configuration, CredentialsProvider credentialsProvider, String bookmark,
                         boolean inclusive)
            throws AccessDeniedException, InvalidConfigurationException, DataFetchException;
}
