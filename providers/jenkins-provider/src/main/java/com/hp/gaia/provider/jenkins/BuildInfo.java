package com.hp.gaia.provider.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BuildInfo {

    @JsonProperty("job")
    private String job;

    @JsonProperty("buildNumber")
    private int buildNumber;

    @JsonProperty("uri")
    private String uri;

    public BuildInfo(final String job, final int buildNumber, final String uri) {
        this.job = job;
        this.buildNumber = buildNumber;
        this.uri = uri;
    }

    public String getJob() {
        return job;
    }

    public void setJob(final String job) {
        this.job = job;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(final int buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JobInfo{");
        sb.append("job='").append(job).append('\'');
        sb.append(", buildNumber=").append(buildNumber);
        sb.append(", uri='").append(uri).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
