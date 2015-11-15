package com.hp.gaia.agent.service;

import com.hp.gaia.agent.config.Credentials;

/**
 * Created by belozovs on 11/15/2015.
 *
 */
public interface UpdatableCredentialsService extends CredentialsService {


    /**
     * Put another credentials definition to the entire credentials map.
     * Exception thrown, if either credentialsId or credentials is null
     *
     * @param credentialsId   - credentials id
     * @param credentials - credentials
     */
    void addCredentials(String credentialsId, Credentials credentials);


}
