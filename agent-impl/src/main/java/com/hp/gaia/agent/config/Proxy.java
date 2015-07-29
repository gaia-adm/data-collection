package com.hp.gaia.agent.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.net.MalformedURLException;
import java.net.URL;

public class Proxy {

    @JsonProperty("httpProxy")
    private String httpProxy;

    @JsonProperty("httpProxyUser")
    private String httpProxyUser;

    @JsonProperty("httpProxyPassword")
    @JsonDeserialize(converter = ProtectedValueInConverter.class)
    @JsonSerialize(converter = ProtectedValueOutConverter.class)
    private ProtectedValue httpProxyPassword;

    public Proxy() {
    }

    public Proxy(final String httpProxy, final String httpProxyUser, final ProtectedValue httpProxyPassword) {
        this.httpProxy = httpProxy;
        this.httpProxyUser = httpProxyUser;
        this.httpProxyPassword = httpProxyPassword;
    }

    public String getHttpProxy() {
        return httpProxy;
    }

    @JsonIgnore
    public URL getHttpProxyURL() {
        if (httpProxy == null) {
            return null;
        }
        try {
            return new URL(httpProxy);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Proxy URL '" + httpProxy + "' is invalid", e);
        }
    }

    public String getHttpProxyUser() {
        return httpProxyUser;
    }

    public ProtectedValue getHttpProxyPassword() {
        return httpProxyPassword;
    }

    public void setHttpProxyPassword(final ProtectedValue httpProxyPassword) {
        this.httpProxyPassword = httpProxyPassword;
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
