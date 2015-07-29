package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.config.Proxy;
import com.hp.gaia.agent.service.AgentConfigService;
import com.hp.gaia.agent.service.ResultUploadServiceBase;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpMessage;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URL;

public class OnPremResultUploadService extends ResultUploadServiceBase {

    // for tests
    void setAgentConfigService(final AgentConfigService agentConfigService) {
        this.agentConfigService = agentConfigService;
    }

    @Override
    protected void configureAuthentication(ProviderConfig providerConfig, HttpMessage httpRequest) {
        httpRequest.addHeader("Authorization", "Bearer " + getOnPremAgentConfigService().getAccessToken());
    }

    @Override
    protected void configureProxy(RequestConfig.Builder requestConfigBuilder) {
        final Proxy proxy = getOnPremAgentConfigService().getProxy();
        if (proxy != null) {
            if (!StringUtils.isEmpty(proxy.getHttpProxy())) {
                URL url = proxy.getHttpProxyURL();
                HttpHost httpHost = new HttpHost(url.getHost(), url.getPort());
                requestConfigBuilder.setProxy(httpHost);
            }
        }
    }

    @Override
    protected void configureProxyCredentials(final HttpClientBuilder httpClientBuilder) {
        // configure proxy credentials on http client
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Proxy proxy = getOnPremAgentConfigService().getProxy();
        if (proxy != null) {
            String proxyPassword = getProxyPassword();
            if (!StringUtils.isEmpty(proxy.getHttpProxy()) && !StringUtils.isEmpty(proxy.getHttpProxyUser()) &&
                    !StringUtils.isEmpty(proxyPassword)) {
                URL proxyURL = proxy.getHttpProxyURL();
                credsProvider.setCredentials(new AuthScope(proxyURL.getHost(), proxyURL.getPort()),
                        new UsernamePasswordCredentials(proxy.getHttpProxyUser(), proxyPassword));
            }
        }
        httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
    }

    private String getProxyPassword() {
        Proxy proxy = getOnPremAgentConfigService().getProxy();
        if (proxy != null && proxy.getHttpProxyPassword() != null) {
            // value is always decrypted outside of onPremAgentConfigService
            return proxy.getHttpProxyPassword().getValue();
        }
        return null;
    }

    private OnPremAgentConfigService getOnPremAgentConfigService() {
        return (OnPremAgentConfigService) agentConfigService;
    }
}
