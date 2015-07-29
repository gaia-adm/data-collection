package com.hp.gaia.agent;

import com.hp.gaia.provider.Data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class MyData implements Data {

    private Map<String, String> metadata;
    private String contentType;
    private byte[] content;
    private String bookmark;

    public MyData(final Map<String, String> metadata, final String contentType, final byte[] content,
                  final String bookmark) {
        this.metadata = metadata;
        this.contentType = contentType;
        this.content = content;
        this.bookmark = bookmark;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public String bookmark() {
        return bookmark;
    }

    @Override
    public void close() throws IOException {

    }
}
