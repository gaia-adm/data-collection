package com.hp.gaia.provider.jenkins.common;

import com.hp.gaia.provider.Data;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class DataImpl extends BookmarkableImpl implements Data {

    private final Map<String, String> customMetadata;

    private final String dataType;

    private final CloseableHttpResponse response;

    public DataImpl(final Map<String, String> customMetadata, final String dataType,
                    final CloseableHttpResponse response, final String bookmark) {
        super(bookmark);
        this.customMetadata = customMetadata;
        this.dataType = dataType;
        this.response = response;
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
    public void close() throws IOException {
        IOUtils.closeQuietly(response);
    }
}
