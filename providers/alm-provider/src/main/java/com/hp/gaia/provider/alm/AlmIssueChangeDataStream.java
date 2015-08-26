package com.hp.gaia.provider.alm;

import com.hp.gaia.provider.*;

import java.io.IOException;

/**
 * Created by belozovs on 8/23/2015.
 * Datastream for collecting ALM issue change data
 */
public class AlmIssueChangeDataStream implements DataStream {

    private final StateMachine stateMachine;

    public AlmIssueChangeDataStream(final AlmIssueChangeDataConfig dataConfig,
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
