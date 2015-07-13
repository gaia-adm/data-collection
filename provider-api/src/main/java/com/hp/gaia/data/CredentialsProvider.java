package com.hp.gaia.data;

import java.util.Map;

public interface CredentialsProvider {

    Map<String, String> getCredentials();
}
