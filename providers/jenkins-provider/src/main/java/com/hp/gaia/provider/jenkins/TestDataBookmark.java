package com.hp.gaia.provider.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TestDataBookmark {

    @JsonProperty("jobPath")
    private List<BuildInfo> jobPath;

    public List<BuildInfo> getJobPath() {
        return jobPath;
    }

    public void setJobPath(final List<BuildInfo> jobPath) {
        this.jobPath = jobPath;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestDataBookmark{");
        sb.append("jobPath=").append(jobPath);
        sb.append('}');
        return sb.toString();
    }
}
