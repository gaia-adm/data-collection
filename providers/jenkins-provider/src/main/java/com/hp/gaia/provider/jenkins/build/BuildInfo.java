package com.hp.gaia.provider.jenkins.build;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BuildInfo {

    @JsonProperty("job")
    private String job;

    @JsonProperty("buildNumber")
    private int buildNumber;

    // does not include locationUri from configuration
    @JsonProperty("uriPath")
    private String uriPath;

    public BuildInfo() {
    }

    public BuildInfo(final String job, final int buildNumber, final String uriPath) {
        this.job = job;
        this.buildNumber = buildNumber;
        this.uriPath = uriPath;
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

    public String getUriPath() {
        return uriPath;
    }

    public void setUriPath(final String uriPath) {
        this.uriPath = uriPath;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JobInfo{");
        sb.append("job='").append(job).append('\'');
        sb.append(", buildNumber=").append(buildNumber);
        sb.append(", uriPath='").append(uriPath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
