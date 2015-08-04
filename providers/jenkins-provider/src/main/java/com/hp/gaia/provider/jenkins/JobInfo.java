package com.hp.gaia.provider.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobInfo {

    @JsonProperty("job")
    private String job;

    @JsonProperty("buildNumber")
    private int buildNumber;

    @JsonProperty("url")
    private String url;

    public JobInfo(final String job, final int buildNumber, final String url) {
        this.job = job;
        this.buildNumber = buildNumber;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JobInfo{");
        sb.append("job='").append(job).append('\'');
        sb.append(", buildNumber=").append(buildNumber);
        sb.append(", url='").append(url).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
