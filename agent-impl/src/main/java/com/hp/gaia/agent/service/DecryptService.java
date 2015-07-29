package com.hp.gaia.agent.service;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Service capable of encrypting/decrypting strings.
 */
public class DecryptService {

    private byte[] salt = {
            (byte) 0x1d, (byte) 0xb8, (byte) 0x4a, (byte) 0x3b,
            (byte) 0x4f, (byte) 0x7b, (byte) 0x3e, (byte) 0xf5
    };

    private int iteration_count = 20;

    public String decryptValue(String value, String secret) {
        try {
            PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, iteration_count);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(secret.toCharArray());
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEwithSHA1AndDESede"); // NON-NLS
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
            Cipher pbeCipher = Cipher.getInstance("PBEwithSHA1AndDESede"); // NON-NLS
            pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
            return new String(pbeCipher.doFinal(new BASE64Decoder().decodeBuffer(value)), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public String encryptValue(String value, String secret) {
        try {
            PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, iteration_count);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(secret.toCharArray());
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEwithSHA1AndDESede"); // NON-NLS
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
            Cipher pbeCipher = Cipher.getInstance("PBEwithSHA1AndDESede"); // NON-NLS
            pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
            return new BASE64Encoder().encode(pbeCipher.doFinal(value.getBytes()));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
