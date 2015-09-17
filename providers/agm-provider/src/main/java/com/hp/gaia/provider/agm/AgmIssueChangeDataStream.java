package com.hp.gaia.provider.agm;

import com.hp.gaia.provider.*;
import com.hp.gaia.provider.agm.StateMachine;

import java.io.IOException;

/**
 * Created by belozovs on 8/23/2015.
 * Datastream for collecting ALM issue change data
 */
public class AgmIssueChangeDataStream implements DataStream {

    private final StateMachine stateMachine;

    public AgmIssueChangeDataStream(final AgmIssueChangeDataConfig dataConfig,
                                    final CredentialsProvider credentialsProvider, final ProxyProvider proxyProvider,
                                    final String bookmark, final boolean inclusive, String providerId) {

        stateMachine = new StateMachine(dataConfig, credentialsProvider, proxyProvider, providerId);
        stateMachine.init(bookmark, inclusive);

    }

    @Override
    public boolean isNextReady() {
        return false;
    }

    @Override
    public Bookmarkable next() throws AccessDeniedException {
        return stateMachine.next();
    }

    @Override
    public void close() throws IOException {
        stateMachine.close();
    }
}
