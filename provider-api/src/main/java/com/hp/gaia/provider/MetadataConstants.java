package com.hp.gaia.provider;

/**
 * Holds constants that may be returned from {@link Data#getMetadata()}.
 */
public final class MetadataConstants {
    /**
     * metric-type(test,build,defect,scm)
     */
    public static final String METRIC = "metric";

    /**
     * automatic-test,commit,fork
     */
    public static final String CATEGORY = "category";

    private MetadataConstants() {
    }
}
