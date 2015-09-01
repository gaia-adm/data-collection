package com.hp.gaia.provider.alm;

import com.hp.gaia.provider.Data;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by belozovs on 8/24/2015.
 * Data returned as the result of ALM issue change collection
 */
public class DataImpl implements Data {

    private final Map<String, String> customMetadata;
    private final String dataType;
    private final CloseableHttpResponse response;
    private final String bookmark;

    public DataImpl(Map<String, String> customMetadata, String dataType, CloseableHttpResponse response, String bookmark) {
        this.customMetadata = customMetadata;
        this.dataType = dataType;
        this.response = response;
        this.bookmark = bookmark;
    }

    @Override
    public Map<String, String> getCustomMetadata() {
        return customMetadata;
    }

    @Override
    public String getDataType() {
        return dataType;
    }

    @Override
    public String getMimeType() {
        return ContentType.get(response.getEntity()).getMimeType();
    }

    @Override
    public String getCharset() {
        Charset charset = ContentType.get(response.getEntity()).getCharset();
        if (charset != null) {
            return charset.name();
        } else {
            return null;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return response.getEntity().getContent();
    }

    @Override
    public String bookmark() {
        return bookmark;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(response);
    }

    public CloseableHttpResponse getResponse() {
        return response;
    }
}
