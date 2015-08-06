package com.hp.gaia.provider;

import javax.validation.constraints.NotNull;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Represents block of data containing measures/metrics. The input stream may contain one (file) or more items (issues,
 * tests - typically one page). A result processor must be registered at GAIA result-processing-service for processing
 * this data.
 */
public interface Data extends Bookmarkable, Closeable {

    /**
     * Returns custom metadata that will be available to processor with prefix 'c_'.
     */
    Map<String, String> getCustomMetadata();

    /**
     * Identifies type of data in {@link Data#getInputStream()}. Typically it will be a string including provider type and
     * data type - i.e 'ALM/tests'. A result processor will be selected based on the data type.
     */
    @NotNull
    String getDataType();

    /**
     * MIME type value (i.e application/xml, text/plain).
     */
    @NotNull
    String getMimeType();

    /**
     * Returns charset of the content. If the content is binary then returns null.
     */
    String getCharset();

    /**
     * Returns input stream for reading data. The data may be binary, textual etc. Its content type is identified by
     * {@link Data#getMimeType()}.
     */
    @NotNull
    InputStream getInputStream() throws IOException;
}
