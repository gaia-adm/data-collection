package com.hp.gaia.provider.circleci.test.state;

import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.Bookmarkable;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.ProxyProvider;
import com.hp.gaia.provider.circleci.test.CircleTestDataConfig;
import com.hp.gaia.provider.circleci.util.JsonSerializer;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static java.util.Arrays.asList;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.LinkedList;
import java.util.Map;

public class StateMachine implements Closeable, StateContext {

    private static final Logger logger = LogManager.getLogger(StateMachine.class);

    private final CircleTestDataConfig testDataConfiguration;

    private final ProxyProvider proxyProvider;

    private CloseableHttpClient httpclient;

    private final LinkedList<State> stack = new LinkedList<>();

    private final String circleToken;

    public StateMachine(final CircleTestDataConfig testDataConfiguration, final CredentialsProvider credentialsProvider,
                        final ProxyProvider proxyProvider) {
        this.testDataConfiguration = testDataConfiguration;
        this.proxyProvider = proxyProvider;
        Map<String, String> credentials = credentialsProvider.getCredentials();
        circleToken = credentials.get("circle-token");
    }

    /**
     * Initializes 1st state based on supplied bookmark.
     */
    public void init(final String bookmark, final boolean inclusive) {
        httpclient = createHttpClient();
        Integer fromBuild = null;
        TestDataBookmark testDataBookmark = JsonSerializer.deserialize(bookmark, TestDataBookmark.class);
        if (testDataBookmark != null) {
            fromBuild = testDataBookmark.getBuildNumber();
        }
        add(new ListBuildsState(fromBuild, inclusive));
    }

    public Bookmarkable next() throws AccessDeniedException {
        Bookmarkable data = null;
        State state = null;
        while(!stack.isEmpty()) {
            state = stack.removeFirst();
            data = state.execute(this);
            if (data != null) {
                break;
            }
        }
        if (stack.isEmpty() && data == null) {
            logger.debug("No more data to fetch");
        }
        return data;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(httpclient);
    }

    @Override
    public CircleTestDataConfig getTestDataConfiguration() {
        return testDataConfiguration;
    }

    @Override
    public String getCircleToken() {
        return circleToken;
    }

    @Override
    public CloseableHttpClient getHttpClient() {
        return httpclient;
    }

    @Override
    public void add(final State state) {
        stack.addFirst(state);
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

        RequestConfig globalConfig = requestConfigBuilder.build();
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(globalConfig)
                .setDefaultCredentialsProvider(httpCredsProvider);
        return httpClientBuilder.build();
    }
}
