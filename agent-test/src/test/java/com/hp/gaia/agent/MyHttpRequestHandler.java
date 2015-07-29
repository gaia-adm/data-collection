package com.hp.gaia.agent;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpRequestHandler for tests.
 */
public class MyHttpRequestHandler implements HttpRequestHandler {

    private String expectedUriPath;

    private String lastRequestUriPath;

    private Map<String, String> lastParams;

    private Map<String, String> lastHeaders;

    private byte[] lastContent;

    public void setExpectedUriPath(final String expectedUriPath) {
        this.expectedUriPath = expectedUriPath;
    }

    public String getLastRequestUriPath() {
        return lastRequestUriPath;
    }

    public Map<String, String> getLastParams() {
        return lastParams;
    }

    public Map<String, String> getLastHeaders() {
        return lastHeaders;
    }

    public byte[] getLastContent() {
        return lastContent;
    }

    @Override
    public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context)
            throws HttpException, IOException {
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(request.getRequestLine().getUri());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        lastRequestUriPath = uriBuilder.getPath();
        lastHeaders = new HashMap<>();
        for (Header header : request.getAllHeaders()) {
            lastHeaders.put(header.getName(), header.getValue());
        }
        lastParams = new HashMap<>();
        for (NameValuePair nameValuePair : uriBuilder.getQueryParams()) {
            lastParams.put(nameValuePair.getName(), nameValuePair.getValue());
        }
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest httpEntityRequest = (HttpEntityEnclosingRequest) request;
            HttpEntity httpEntity = httpEntityRequest.getEntity();
            final InputStream inputStream = httpEntity.getContent();
            try {
                lastContent = IOUtils.toByteArray(inputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        if (expectedUriPath == null) {
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            StringEntity entity = new StringEntity("Test error", ContentType.create("text/plain", "UTF-8"));
            response.setEntity(entity);
        } else if (expectedUriPath.equals(lastRequestUriPath)) {
            response.setStatusCode(HttpStatus.SC_OK);
        } else {
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            StringEntity entity = new StringEntity("Unexpected URI " + request.getRequestLine().getUri(), ContentType.create("text/plain", "UTF-8"));
            response.setEntity(entity);
        }
    }
}
