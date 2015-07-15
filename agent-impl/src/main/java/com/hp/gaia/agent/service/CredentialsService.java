package com.hp.gaia.agent.service;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Allows to retrieve credentials from credentials storage.
 */
public interface CredentialsService {

    /**
     * Retrieves credentials for given credentials id. If the credentialsId is null or credentialsId is invalid, an
     * exception is thrown. All returned credential values are decrypted.
     *
     * @return unmodifiable map with credential values
     */
    @NotNull
    Map<String, String> getCredentials(@NotNull String credentialsId);
}
