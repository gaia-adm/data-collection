package com.hp.gaia.agent.service;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.provider.Data;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpMessage;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public abstract class ResultUploadServiceBase implements ResultUploadService {

    private static final Logger logger = LogManager.getLogger(ResultUploadServiceBase.class);

    private CloseableHttpClient httpclient;

    @Autowired
    protected AgentConfigService agentConfigService;

    public void init(int maxPoolSize) {
        // create HTTP client
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxPoolSize);

        // socket configuration
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(agentConfigService.getSoTimeout()).build();
        cm.setDefaultSocketConfig(socketConfig);

        // configure default request, no need to keep cookies
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setConnectTimeout(agentConfigService.getConnectTimeout())
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES);
        configureProxy(requestConfigBuilder);
        RequestConfig globalConfig = requestConfigBuilder.build();

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(globalConfig);
        configureProxyCredentials(httpClientBuilder);
        httpclient = httpClientBuilder.build();
    }

    @PreDestroy
    public void shutdown() {
        IOUtils.closeQuietly(httpclient);
    }

    @Override
    public void sendData(final ProviderConfig providerConfig, final Data data) {
        final String uploadDataURI = getUploadDataURI(data);
        InputStream is = null;
        try {
            is = data.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException("Failed to send data to " + uploadDataURI, e);
        }
        try {
            HttpPost httpRequest = new HttpPost(uploadDataURI);
            ContentType contentType = ContentType.create(data.getMimeType(), data.getCharset());
            InputStreamEntity reqEntity = new InputStreamEntity(is, -1, contentType);
            reqEntity.setChunked(true);
            httpRequest.setEntity(reqEntity);
            configureAuthentication(providerConfig, httpRequest);

            logger.debug("Sending data to " + uploadDataURI);
            CloseableHttpResponse response = null;
            try {
                response = httpclient.execute(httpRequest);
            } catch (IOException e) {
                throw new RuntimeException("Failed to send data to " + uploadDataURI, e);
            }
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (!(statusCode >= 200 && statusCode < 300)) {
                    throw new RuntimeException("Failed to send data to " + uploadDataURI + ", status code " + statusCode + " " + response.getStatusLine().getReasonPhrase());
                }
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    // not fatal, just log
                    logger.error("Failed to receive full response for " + uploadDataURI, e);
                }
            } finally {
                IOUtils.closeQuietly(response);
            }
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private String getUploadDataURI(Data data) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(agentConfigService.getGaiaLocation())
                .path("/result-upload/rest/v1/upload-data");
        builder.queryParam("dataType", data.getDataType());
        Map<String, String> metadata = data.getCustomMetadata();
        if (metadata != null) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                builder.queryParam("c_" + entry.getKey(), entry.getValue());
            }
        }
        return builder.build().encode().toString();
    }

    protected abstract void configureAuthentication(ProviderConfig providerConfig, HttpMessage httpRequest);

    protected abstract void configureProxy(RequestConfig.Builder requestConfigBuilder);

    protected abstract void configureProxyCredentials(HttpClientBuilder httpClientBuilder);
}
