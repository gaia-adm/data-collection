package com.hp.gaia.provider.jenkins;

import java.util.List;
import java.util.Map;

public class BuildDetails {

    private Map<String, String> parameters;

    private boolean building;

    private int number;

    private String result; // if building, this is null

    private long timestamp;

    private String url;

    private BuildInfo matrixBuildRef;

    private List<BuildInfo> subBuilds;

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public boolean isBuilding() {
        return building;
    }

    public void setBuilding(final boolean building) {
        this.building = building;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public BuildInfo getMatrixBuildRef() {
        return matrixBuildRef;
    }

    public void setMatrixBuildRef(final BuildInfo matrixBuildRef) {
        this.matrixBuildRef = matrixBuildRef;
    }

    public List<BuildInfo> getSubBuilds() {
        return subBuilds;
    }

    public void setSubBuilds(final List<BuildInfo> subBuilds) {
        this.subBuilds = subBuilds;
    }
}
