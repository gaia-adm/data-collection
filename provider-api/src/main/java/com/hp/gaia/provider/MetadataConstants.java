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

    /**
     * test-name,job-name,defect-number,sha-of-commit
     */
    public static final String NAME = "name";

    /**
     * ci-server,qc-name/project,scm-repository
     */
    public static final String SOURCE = "source";

    private MetadataConstants() {
    }
}
