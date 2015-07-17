package com.hp.gaia.agent.service;

import com.hp.gaia.agent.config.ProtectedValue;
import com.hp.gaia.agent.config.ProtectedValue.Type;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service which handles encryption/decryption of {@link ProtectedValue} instances.
 */
public class ProtectedValueDecrypter {

    private String secret;

    @Autowired
    private DecryptService decryptService;

    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public String decrypt(ProtectedValue protectedValue) {
        if (protectedValue == null) {
            return null;
        }
        if (protectedValue.getType() == Type.ENCRYPTED) {
            return decryptService.decryptValue(protectedValue.getValue(), secret);
        } else {
            return protectedValue.getValue();
        }
    }

    public ProtectedValue encrypt(String value) {
        if (value == null) {
            return null;
        }
        return new ProtectedValue(Type.ENCRYPTED, decryptService.encryptValue(value, secret));
    }
}
