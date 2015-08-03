package com.hp.gaia.provider.jenkins;

import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.DataStream;
import com.hp.gaia.provider.ProxyProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

public class TestDataStream implements DataStream {

    private TestDataConfiguration testDataConfiguration;

    private CredentialsProvider credentialsProvider;

    private ProxyProvider proxyProvider;

    private CloseableHttpClient httpclient;

    public TestDataStream(final TestDataConfiguration testDataConfiguration,
                          final CredentialsProvider credentialsProvider, final ProxyProvider proxyProvider) {
        this.testDataConfiguration = testDataConfiguration;
        this.credentialsProvider = credentialsProvider;
        this.proxyProvider = proxyProvider;
    }

    @Override
    public boolean isNextReady() {
        return false;
    }

    @Override
    public Data next() throws AccessDeniedException {
        if (httpclient == null) {
            httpclient = createHttpClient();
        }

        return null;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(httpclient);
    }

    private CloseableHttpClient createHttpClient() {
        // create and configure HttpClient
        BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        org.apache.http.client.CredentialsProvider httpCredsProvider = new BasicCredentialsProvider();
        // configure proxy & proxy credentials
        Proxy proxy = proxyProvider.getProxy();
        if (!Proxy.NO_PROXY.equals(proxy)) {
            final InetSocketAddress socketAddress = (InetSocketAddress) proxy.address();
            HttpHost httpHost = new HttpHost(socketAddress.getHostName(), socketAddress.getPort());
            requestConfigBuilder.setProxy(httpHost);
            final String proxyUsername = proxyProvider.getProxyUsername();
            final String proxyPassword = proxyProvider.getProxyPassword();
            if (!StringUtils.isEmpty(proxyUsername) && !StringUtils.isEmpty(proxyPassword)) {
                httpCredsProvider.setCredentials(new AuthScope(socketAddress.getHostName(), socketAddress.getPort()),
                        new UsernamePasswordCredentials(proxyUsername, proxyPassword));
            }
        }
        requestConfigBuilder.setTargetPreferredAuthSchemes(asList(AuthSchemes.DIGEST)); // never use HTTP basic
        // configure Jenkins credentials
        Map<String, String> credentials = credentialsProvider.getCredentials();
        if (!StringUtils.isEmpty(credentials.get("username")) && !StringUtils.isEmpty(credentials.get("password"))) {
            URL locationUrl = testDataConfiguration.getLocation();
            httpCredsProvider.setCredentials(new AuthScope(locationUrl.getHost(), locationUrl.getPort()),
                                    new UsernamePasswordCredentials(credentials.get("username"), credentials.get("password")));
        }

        RequestConfig globalConfig = requestConfigBuilder.build();
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(globalConfig)
                .setDefaultCredentialsProvider(httpCredsProvider);
        return httpClientBuilder.build();
    }
}
