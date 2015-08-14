package com.hp.gaia.provider.circleci.test.state;

import com.hp.gaia.provider.circleci.test.CircleTestDataConfig;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;

public interface StateContext {

    CircleTestDataConfig getTestDataConfiguration();

    String getCircleToken();

    /**
     * Returns {@link HttpClient} that may be used to execute requests against Jenkins instance.
     */
    CloseableHttpClient getHttpClient();

    /**
     * Adds state to stack to be executed next.
     */
    void add(State state);
}
