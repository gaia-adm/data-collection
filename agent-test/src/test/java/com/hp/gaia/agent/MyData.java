package com.hp.gaia.agent;

import com.hp.gaia.provider.Data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class MyData implements Data {

    private Map<String, String> metadata;
    private String dataType;
    private String mimeType;
    private String charset;
    private byte[] content;
    private String bookmark;
    private boolean closed;

    public MyData(final Map<String, String> metadata,
                  final String dataType,
                  final String mimeType, final String charset,
                  final byte[] content,
                  final String bookmark) {
        this.metadata = metadata;
        this.dataType = dataType;
        this.mimeType = mimeType;
        this.charset = charset;
        this.content = content;
        this.bookmark = bookmark;
    }

    @Override
    public Map<String, String> getCustomMetadata() {
        return metadata;
    }

    @Override
    public String getDataType() {
        return dataType;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getCharset() {
        return charset;
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
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }
}
