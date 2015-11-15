package com.hp.gaia.agent.oncloud.service;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.config.Proxy;
import com.hp.gaia.agent.onprem.service.OnPremAgentConfigService;
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

public class OnCloudResultUploadService extends ResultUploadServiceBase {

    // for tests
    void setAgentConfigService(final AgentConfigService agentConfigService) {
        this.agentConfigService = agentConfigService;
    }

    @Override
    protected void configureAuthentication(ProviderConfig providerConfig, HttpMessage httpRequest) {
        System.out.println("No authentication provided meanwhile, please wait...");
        //httpRequest.addHeader("Authorization", "Bearer " + getOnPremAgentConfigService().getAccessToken());
    }

    @Override
    protected void configureProxy(RequestConfig.Builder requestConfigBuilder) {
        System.out.println("OnCloudResultUploadService initialized with no proxy");
    }

    @Override
    protected void configureProxyCredentials(HttpClientBuilder httpClientBuilder) {
        System.out.println("OnCloudResultUploadService initialized with no proxy");
    }


}
