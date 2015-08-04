package com.hp.gaia.provider.jenkins;

import java.net.URI;

public class TestDataConfiguration {

    private final URI location;

    private final String job;

    public TestDataConfiguration(final URI location, final String job) {
        this.location = location;
        this.job = job;
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
        sb.append("location=").append(location);
        sb.append(", job='").append(job).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
