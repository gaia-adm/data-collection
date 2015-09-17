package com.hp.gaia.provider.agm;

import com.hp.gaia.provider.CredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Created by belozovs on 8/24/2015.
 *
 */
public interface StateContext {

    AgmIssueChangeDataConfig getIssueChangeDataConfiguration();

    CredentialsProvider getCredentialsProvider();

    /**
     * Returns {@link CloseableHttpClient} that may be used to execute requests against Jenkins instance.
     */
    CloseableHttpClient getHttpClient();

    /**
     * Adds state to stack to be executed next.
     */
    void add(State state);

    String getDataType();

}
