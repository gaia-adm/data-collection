package com.hp.gaia.provider;

import java.net.Proxy;

public interface ProxyProvider {

    /**
     * Returns configured proxy server or {@link Proxy#NO_PROXY} in case direct connection should be used.
     */
    Proxy getProxy();

    String getProxyUsername();

    String getProxyPassword();
}
