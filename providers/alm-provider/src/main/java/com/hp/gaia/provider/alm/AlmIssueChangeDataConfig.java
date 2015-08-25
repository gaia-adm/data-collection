package com.hp.gaia.provider.alm;

import java.net.URI;

/**
 * Created by belozovs on 8/23/2015.
 * In-memory representation of ALM configuration defined in providers.json
 */
public class AlmIssueChangeDataConfig {

    private final URI location;
    private final String domain;
    private final String project;

    public AlmIssueChangeDataConfig(URI location, String domain, String project) {
        this.location = location;
        this.domain = domain;
        this.project = project;
    }

    public URI getLocation() {
        return location;
    }

    public String getDomain() {
        return domain;
    }

    public String getProject() {
        return project;
    }
}
