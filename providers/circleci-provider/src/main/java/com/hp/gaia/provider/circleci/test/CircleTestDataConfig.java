package com.hp.gaia.provider.circleci.test;

public class CircleTestDataConfig {

    private final String username;

    private final String project;

    public CircleTestDataConfig(final String username, final String project) {
        this.username = username;
        this.project = project;
    }

    public String getUsername() {
        return username;
    }

    public String getProject() {
        return project;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CircleTestDataConfig{");
        sb.append("username='").append(username).append('\'');
        sb.append(", project='").append(project).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
