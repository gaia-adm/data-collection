package com.hp.gaia.provider.agm;

import java.net.URI;

/**
 * Created by belozovs on 8/23/2015.
 * In-memory representation of AGM configuration defined in providers.json
 */
public class AgmIssueChangeDataConfig {

    private final URI location;
    private final String domain;
    private final String project;
    private final String tenantId;

    public AgmIssueChangeDataConfig(URI location, String domain, String project, String tenantId) {
        this.location = location;
        this.domain = domain;
        this.project = project;
        this.tenantId = tenantId;
    }

    public URI getLocation() {
        return location;
    }

    public String getDomain() {
        return domain;
    }

    public String getProject() { return project; }

    public String getTenantId() { return tenantId; }
}
