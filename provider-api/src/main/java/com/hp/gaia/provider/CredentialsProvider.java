package com.hp.gaia.provider;

import java.util.Map;

public interface CredentialsProvider {

    Map<String, String> getCredentials();
}
