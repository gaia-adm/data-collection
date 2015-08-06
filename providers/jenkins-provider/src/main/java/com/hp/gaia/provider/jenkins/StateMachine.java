package com.hp.gaia.provider.jenkins;

import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.Bookmarkable;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.Data;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import static java.util.Arrays.asList;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of state machine to handle fetching of test data from hierarchical Jenkins builds and matrix builds.
 */
public class StateMachine implements Closeable, StateContext {

    private static final Logger logger = LogManager.getLogger(StateMachine.class);

    private final TestDataConfiguration testDataConfiguration;

    private final CredentialsProvider credentialsProvider;

    private final ProxyProvider proxyProvider;

    private CloseableHttpClient httpclient;

    private final LinkedList<State> stack = new LinkedList<>();

    public StateMachine(final TestDataConfiguration testDataConfiguration,
                        final CredentialsProvider credentialsProvider, final ProxyProvider proxyProvider) {
        this.testDataConfiguration = testDataConfiguration;
        this.credentialsProvider = credentialsProvider;
        this.proxyProvider = proxyProvider;
    }

    /**
     * Initializes 1st state based on supplied bookmark.
     */
    public void init(final String bookmark, final boolean inclusive) {
        httpclient = createHttpClient();
        TestDataBookmark testDataBookmark = JsonSerializer.deserialize(bookmark, TestDataBookmark.class);
        if (testDataBookmark == null || CollectionUtils.isEmpty(testDataBookmark.getBuildPath())) {
            // start from beginning, list all jobs
            add(new ListBuildsState());
        } else {
            // bookmark from root or child job
            final List<BuildInfo> jobPath = testDataBookmark.getBuildPath();
            // add state for listing jobs of root job name
            BuildInfo rootBuildInfo = jobPath.get(0);
            boolean inclusiveParam = false;
            boolean skipParam = false;
            if (jobPath.size() <= 1) {
                inclusiveParam = inclusive;
            } else {
                // more than 1 items in job path, inclusive applies to the last one, skip the rootJobInfo from top
                // as it will be processed from bottom
                skipParam = true;
            }
            add(new ListBuildsState(rootBuildInfo, inclusiveParam, skipParam));
            if (jobPath.size() > 1) {
                add(new GetBuildSiblingsState(jobPath));
                add(new GetBuildState(jobPath, inclusive));
            }
        }
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
            URI locationUri = testDataConfiguration.getLocation();
            httpCredsProvider.setCredentials(new AuthScope(locationUri.getHost(), locationUri.getPort()),
                                    new UsernamePasswordCredentials(credentials.get("username"), credentials.get("password")));
        }

        RequestConfig globalConfig = requestConfigBuilder.build();
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(globalConfig)
                .setDefaultCredentialsProvider(httpCredsProvider);
        return httpClientBuilder.build();
    }

    @Override
    public TestDataConfiguration getTestDataConfiguration() {
        return testDataConfiguration;
    }

    @Override
    public CloseableHttpClient getHttpClient() {
        return httpclient;
    }

    @Override
    public void add(final State state) {
        stack.addFirst(state);
    }
}
