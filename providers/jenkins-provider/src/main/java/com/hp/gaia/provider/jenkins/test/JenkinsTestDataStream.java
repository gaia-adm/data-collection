package com.hp.gaia.provider.jenkins.test;

import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.Bookmarkable;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.DataStream;
import com.hp.gaia.provider.ProxyProvider;
import com.hp.gaia.provider.jenkins.test.state.StateMachine;

import java.io.IOException;

public class JenkinsTestDataStream implements DataStream {

    private final StateMachine stateMachine;

    public JenkinsTestDataStream(final JenkinsTestDataConfig testDataConfiguration,
                                 final CredentialsProvider credentialsProvider, final ProxyProvider proxyProvider,
                                 final String bookmark, final boolean inclusive) {
        stateMachine = new StateMachine(testDataConfiguration, credentialsProvider, proxyProvider);
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
