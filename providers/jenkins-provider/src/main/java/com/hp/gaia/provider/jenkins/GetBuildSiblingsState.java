package com.hp.gaia.provider.jenkins;

import com.hp.gaia.provider.Data;

import java.util.List;

public class GetBuildSiblingsState implements State {

    private List<JobInfo> jobPath;

    public GetBuildSiblingsState(final List<JobInfo> jobPath) {
        this.jobPath = jobPath;
    }

    @Override
    public Data execute(final StateContext stateContext) {
        return null;
    }
}
