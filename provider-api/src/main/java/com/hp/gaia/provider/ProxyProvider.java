package com.hp.gaia.provider;

import java.net.Proxy;
import java.util.List;

public interface ProxyProvider {

    List<Proxy> getProxyList();

    String getProxyUsername();

    String getProxyPassword();
}
