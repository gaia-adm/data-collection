package com.hp.gaia.agent.config;

public class SchedulingConfig {

    /**
     * Run period in minutes.
     */
    private int runPeriod;

    public int getRunPeriod() {
        return runPeriod;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SchedulingConfig{");
        sb.append("runPeriod=").append(runPeriod);
        sb.append('}');
        return sb.toString();
    }
}
