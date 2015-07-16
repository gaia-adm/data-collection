package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.config.Proxy;
import com.hp.gaia.agent.service.ResultUploadService;
import com.hp.gaia.provider.Data;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URL;

public class OnPremResultUploadService implements ResultUploadService {

    private CloseableHttpClient httpclient;

    @Autowired
    private OnPremAgentConfigService onPremAgentConfigService;

    @PostConstruct
    public void init() {
        // create HTTP client
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(5); // TODO: make configurable

        // configure proxy credentials on http client
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        if (onPremAgentConfigService.getProxy() != null) {
            Proxy proxy = onPremAgentConfigService.getProxy();
            if (!StringUtils.isEmpty(proxy.getHttpProxy()) && !StringUtils.isEmpty(proxy.getHttpProxyUser()) &&
                    !StringUtils.isEmpty(proxy.getHttpProxyPassword())) {
                URL proxyURL = proxy.getHttpProxyURL();
                credsProvider.setCredentials(new AuthScope(proxyURL.getHost(), proxyURL.getPort()),
                        new UsernamePasswordCredentials(proxy.getHttpProxyUser(), proxy.getHttpProxyPassword()));
            }
        }
        // socket configuration
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(onPremAgentConfigService.getSoTimeout()).build();
        cm.setDefaultSocketConfig(socketConfig);

        // no need to keep cookies
        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();

        httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultRequestConfig(globalConfig)
                .build();
    }

    @PreDestroy
    public void shutdown() {
        IOUtils.closeQuietly(httpclient);
    }

    @Override
    public void sendData(final ProviderConfig providerConfig, final Data data) {

    }
}
