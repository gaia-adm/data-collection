package com.hp.gaia.provider.jenkins.test;

import java.net.URI;
import java.util.List;

public class TestDataConfiguration {

    private final List<String> customTags;

    private final URI location;

    private final String job;

    public TestDataConfiguration(final List<String> customTags, final URI location, final String job) {
        this.customTags = customTags;
        this.location = location;
        this.job = job;
    }

    public List<String> getCustomTags() {
        return customTags;
    }

    public URI getLocation() {
        return location;
    }

    public String getJob() {
        return job;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestDataConfiguration{");
        sb.append("customTags=").append(customTags);
        sb.append(", location=").append(location);
        sb.append(", job='").append(job).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
