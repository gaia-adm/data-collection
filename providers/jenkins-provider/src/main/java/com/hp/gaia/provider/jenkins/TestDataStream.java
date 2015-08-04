package com.hp.gaia.provider.jenkins;

import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.DataStream;
import com.hp.gaia.provider.ProxyProvider;

import java.io.IOException;

public class TestDataStream implements DataStream {

    private final StateMachine stateMachine;

    public TestDataStream(final TestDataConfiguration testDataConfiguration,
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
    public Data next() throws AccessDeniedException {
        return stateMachine.next();
    }

    @Override
    public void close() throws IOException {
        stateMachine.close();
    }
}
