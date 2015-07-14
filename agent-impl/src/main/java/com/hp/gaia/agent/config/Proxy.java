package com.hp.gaia.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Proxy {

    @JsonProperty("httpProxy")
    private String httpProxy;

    @JsonProperty("httpProxyUser")
    private String httpProxyUser;

    @JsonProperty("httpProxyPassword")
    private String httpProxyPassword;

    public String getHttpProxy() {
        return httpProxy;
    }

    public String getHttpProxyUser() {
        return httpProxyUser;
    }

    public String getHttpProxyPassword() {
        return httpProxyPassword;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Proxy{");
        sb.append("httpProxy='").append(httpProxy).append('\'');
        sb.append(", httpProxyUser='").append(httpProxyUser).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
