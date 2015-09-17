package com.hp.gaia.provider.agm;

import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.Bookmarkable;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.ProxyProvider;
import com.hp.gaia.provider.agm.util.AgmXmlUtils;
import com.hp.gaia.provider.agm.util.JsonSerializer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.LinkedList;

/**
 * Created by belozovs on 8/24/2015.
 *
 */
public class StateMachine implements Closeable, StateContext {

    private static final Logger log = LogManager.getLogger(StateMachine.class);

    private final AgmIssueChangeDataConfig dataConfig;
    private final ProxyProvider proxyProvider;
    private final CredentialsProvider credentialsProvider;

    private String dataType;

    private CloseableHttpClient httpclient;

    private final LinkedList<State> stack = new LinkedList<>();

    public final static int PAGE_SIZE = 100;
    private int nextStartIndex = 1;
    private int totalReceivedResults = 0;

    public StateMachine(final AgmIssueChangeDataConfig dataConfig, final CredentialsProvider credentialsProvider, final ProxyProvider proxyProvider, String providerId) {
        this.dataConfig = dataConfig;
        this.proxyProvider = proxyProvider;
        this.credentialsProvider = credentialsProvider;
        this.dataType = providerId;
    }

    public int getNextStartIndex() {
        return nextStartIndex;
    }

    /**
     * Initializes 1st state based on supplied bookmark.
     * NOTE: inclusive is not in use currently, any data fetch will be done for auditID bigger than bookmarked (i.e., inclusive = false)
     */
    public void init(final String bookmark, final boolean inclusive) {
        this.httpclient = createHttpClient();

        IssueChangeState state = new IssueChangeState();
        if(bookmark != null){
            IssueChangeBookmark icb = JsonSerializer.deserialize(bookmark, IssueChangeBookmark.class);
            if(icb != null){
                state.setAuditId(icb.getLastAuditId());
            }
        }
        log.debug("Starting with auditId " + state.getAuditId());

        add(state);
    }


    //invoke the collection
    public Bookmarkable next() throws AccessDeniedException {
        Bookmarkable data = null;
        State state;
        while(!stack.isEmpty()) {
            state = stack.removeFirst();
            data = state.execute(this);
            if (data != null) {
                try {
                    String content = EntityUtils.toString(((DataImpl) data).getResponse().getEntity());
                    AgmXmlUtils agmXmlUtils = new AgmXmlUtils();
                    int totalResults = agmXmlUtils.getIntegerAttributeValue(content, "Audits", "TotalResults");
                    int resultsReceived = agmXmlUtils.countTags(content, "Audit");
                    log.debug("Results received in the last request: " + resultsReceived);
                    totalReceivedResults += resultsReceived;
                    log.debug("Total results received in current iteration: " + totalReceivedResults);
                    if(totalReceivedResults< totalResults){
                        log.debug("Some data is still waiting, another request should executed; total results: " + totalResults + ", received till now: " + totalReceivedResults + ", left: " + (totalResults-totalReceivedResults));
                        add(state);
                    }
                } catch (IOException e) {
                    log.error(e);
                    throw new RuntimeException("Failed to read the response; " + e.getMessage());
                }
                nextStartIndex = nextStartIndex +PAGE_SIZE;
                break;
            }
        }
        return data;
    }

    @Override
    public String getDataType() {
        return dataType;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(httpclient);
    }

    @Override
    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    @Override
    public AgmIssueChangeDataConfig getIssueChangeDataConfiguration() {
        return dataConfig;
    }

    @Override
    public CloseableHttpClient getHttpClient() {
        return httpclient;
    }

    @Override
    public void add(State state) {
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

        RequestConfig globalConfig = requestConfigBuilder.build();
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(globalConfig)
                .setDefaultCredentialsProvider(httpCredsProvider);
        return httpClientBuilder.build();
    }
}
