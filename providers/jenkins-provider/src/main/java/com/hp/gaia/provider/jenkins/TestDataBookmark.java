package com.hp.gaia.provider.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TestDataBookmark {

    @JsonProperty("jobPath")
    private List<JobInfo> jobPath;

    public List<JobInfo> getJobPath() {
        return jobPath;
    }

    public void setJobPath(final List<JobInfo> jobPath) {
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
