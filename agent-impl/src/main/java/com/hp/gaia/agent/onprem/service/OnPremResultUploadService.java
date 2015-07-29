package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.config.Proxy;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;

public class OnPremResultUploadService extends ResultUploadServiceBase {

    @Autowired
    private OnPremAgentConfigService onPremAgentConfigService;

    @Override
    protected void configureAuthentication(ProviderConfig providerConfig, HttpMessage httpRequest) {
        httpRequest.addHeader("Authorization", "Bearer " + onPremAgentConfigService.getAccessToken());
    }

    @Override
    protected void configureProxy(RequestConfig.Builder requestConfigBuilder) {
        if (onPremAgentConfigService.getProxy() != null) {
            Proxy proxy = onPremAgentConfigService.getProxy();
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
        Proxy proxy = onPremAgentConfigService.getProxy();
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

    public String getProxyPassword() {
        Proxy proxy = onPremAgentConfigService.getProxy();
        if (proxy != null && proxy.getHttpProxyPassword() != null) {
            // value is always decrypted outside of onPremAgentConfigService
            return proxy.getHttpProxyPassword().getValue();
        }
        return null;
    }
}
