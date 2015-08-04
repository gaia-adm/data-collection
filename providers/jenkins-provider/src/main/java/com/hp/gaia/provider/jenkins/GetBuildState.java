package com.hp.gaia.provider.jenkins;

import com.hp.gaia.provider.Data;

import java.util.List;

public class GetBuildState implements State {

    private List<JobInfo> jobPath;

    private boolean inclusive;

    public GetBuildState(final List<JobInfo> jobPath, final boolean inclusive) {
        this.jobPath = jobPath;
        this.inclusive = inclusive;
    }

    @Override
    public Data execute(final StateContext stateContext) {
        return null;
    }
}
