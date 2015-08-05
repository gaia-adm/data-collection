package com.hp.gaia.provider.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TestDataBookmark {

    @JsonProperty("buildPath")
    private List<BuildInfo> buildPath;

    public TestDataBookmark() {
    }

    public TestDataBookmark(final List<BuildInfo> jobPath) {
        this.buildPath = jobPath;
    }

    public List<BuildInfo> getBuildPath() {
        return buildPath;
    }

    public void setBuildPath(final List<BuildInfo> buildPath) {
        this.buildPath = buildPath;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestDataBookmark{");
        sb.append("buildPath=").append(buildPath);
        sb.append('}');
        return sb.toString();
    }
}
