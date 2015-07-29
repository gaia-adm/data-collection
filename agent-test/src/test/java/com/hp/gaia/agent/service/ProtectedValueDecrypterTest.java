package com.hp.gaia.agent.service;

import com.hp.gaia.agent.AgentIntegrationTest;
import com.hp.gaia.agent.config.ProtectedValue;
import com.hp.gaia.agent.config.ProtectedValue.Type;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@DirtiesContext
public class ProtectedValueDecrypterTest extends AgentIntegrationTest {

    @Autowired
    private ProtectedValueDecrypter protectedValueDecrypter;

    @Before
    public void before() {
        protectedValueDecrypter.setSecret("mysecret");
    }

    @Test
    public void testEncryptNull() {
        ProtectedValue result = protectedValueDecrypter.encrypt(null);
        assertNull(result);
    }

    @Test
    public void testEncryptEmpty() {
        ProtectedValue result = protectedValueDecrypter.encrypt("");
        assertNotNull(result);
        assertEquals(Type.ENCRYPTED, result.getType());
        assertEquals("Ogp8+kw5gOc=", result.getValue());
    }

    @Test
    public void testEncryptText() {
        ProtectedValue result = protectedValueDecrypter.encrypt("someText");
        assertNotNull(result);
        assertEquals(Type.ENCRYPTED, result.getType());
        assertEquals("LwK96t1EBfsnqKZiEZDhAw==", result.getValue());
    }

    @Test
    public void testDecryptNull() {
        String result = protectedValueDecrypter.decrypt(null);
        assertNull(result);
    }

    @Test
    public void testDecryptText() {
        String result = protectedValueDecrypter.decrypt(new ProtectedValue(Type.ENCRYPTED, "LwK96t1EBfsnqKZiEZDhAw=="));
        assertEquals("someText", result);
    }
}
