package com.hp.gaia.provider.test;

import com.hp.gaia.agent.MyData;
import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.DataProvider;
import com.hp.gaia.provider.DataStream;
import com.hp.gaia.provider.InvalidConfigurationException;
import com.hp.gaia.provider.MetadataConstants;
import com.hp.gaia.provider.ProxyProvider;

import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class TestProvider1 implements DataProvider {

    private Map<String, String> properties;

    private Map<String, String> credentials;

    private Proxy proxy;

    private String proxyUsername;

    private String proxyPassword;

    private String lastBookmark;

    private RuntimeException throwExceptionInFetchData;

    private RuntimeException throwExceptionInNext;

    private boolean dataStreamClosed;

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public String getLastBookmark() {
        return lastBookmark;
    }

    public void setThrowExceptionInFetchData(final RuntimeException throwExceptionInFetchData) {
        this.throwExceptionInFetchData = throwExceptionInFetchData;
    }

    public void setThrowExceptionInNext(final RuntimeException throwExceptionInNext) {
        this.throwExceptionInNext = throwExceptionInNext;
    }

    public boolean isDataStreamClosed() {
        return dataStreamClosed;
    }

    public void reset() {
        properties = null;
        credentials = null;
        proxy = null;
        proxyUsername = null;
        proxyPassword = null;
        lastBookmark = null;
        throwExceptionInFetchData = null;
        throwExceptionInNext = null;
        dataStreamClosed = false;
    }

    @Override
    public String getProviderId() {
        return "testProvider1";
    }

    @Override
    public DataStream fetchData(final Map<String, String> properties, final CredentialsProvider credentialsProvider,
                                final ProxyProvider proxyProvider, final String bookmark, final boolean inclusive)
            throws AccessDeniedException, InvalidConfigurationException {
        this.properties = properties;
        this.lastBookmark = bookmark;
        this.credentials = credentialsProvider.getCredentials();
        this.proxy = proxyProvider.getProxy();
        this.proxyUsername = proxyProvider.getProxyUsername();
        this.proxyPassword = proxyProvider.getProxyPassword();
        if (throwExceptionInFetchData != null) {
            throw throwExceptionInFetchData;
        }
        return new DataStream() {
            private int counter;

            @Override
            public boolean isNextReady() {
                return false;
            }

            @Override
            public Data next() throws AccessDeniedException {
                if (throwExceptionInNext != null) {
                    throw throwExceptionInNext;
                }
                if (counter++ > 0) {
                    return null;
                }
                Map<String, String> metadata = new HashMap<>();
                metadata.put(MetadataConstants.METRIC, "testMetric");
                metadata.put(MetadataConstants.CATEGORY, "testCategory");
                String content = "{\"testKey\": \"testValue\"}";
                MyData myData = new MyData(metadata, "application/json", "UTF-8", content.getBytes(
                                        Charset.forName("UTF-8")), "bookmark2");
                return myData;
            }

            @Override
            public void close() throws IOException {
                dataStreamClosed = true;
            }
        };
    }
}
