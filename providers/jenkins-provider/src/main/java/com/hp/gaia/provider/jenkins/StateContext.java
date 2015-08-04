package com.hp.gaia.provider.jenkins;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;

public interface StateContext {

    TestDataConfiguration getTestDataConfiguration();

    /**
     * Returns {@link HttpClient} that may be used to execute requests against Jenkins instance.
     */
    CloseableHttpClient getHttpClient();

    /**
     * Adds state to stack to be executed next.
     */
    void add(State state);
}
