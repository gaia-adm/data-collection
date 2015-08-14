package com.hp.gaia.provider.circleci.build;

public class BuildDetails {

    private int number;

    // "running", "success", "failed"
    private String status;

    // "https://circleci.com/gh/gaia-adm/result-processing/23"
    private String buildUrl;

    // "2015-08-13T15:06:30.389Z"
    private String startTime;

    // "master"
    private String branch;

    // "result-processing"
    private String reponame;

    // "https://github.com/gaia-adm/result-processing"
    private String vcsUrl;

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public void setBuildUrl(final String buildUrl) {
        this.buildUrl = buildUrl;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(final String startTime) {
        this.startTime = startTime;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(final String branch) {
        this.branch = branch;
    }

    public String getReponame() {
        return reponame;
    }

    public void setReponame(final String reponame) {
        this.reponame = reponame;
    }

    public String getVcsUrl() {
        return vcsUrl;
    }

    public void setVcsUrl(final String vcsUrl) {
        this.vcsUrl = vcsUrl;
    }
}
