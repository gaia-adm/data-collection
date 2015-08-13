package com.hp.gaia.provider.dummy;

import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.Bookmarkable;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.DataProvider;
import com.hp.gaia.provider.DataStream;
import com.hp.gaia.provider.InvalidConfigurationException;
import com.hp.gaia.provider.ProxyProvider;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Provider for dev/test purposes.
 */
@Component
public class DummyDataProvider implements DataProvider {

    @Override
    public String getProviderId() {
        return "dummy";
    }

    @Override
    public DataStream fetchData(final Map<String, String> properties, final CredentialsProvider credentialsProvider,
                                final ProxyProvider proxyProvider, final String bookmark, final boolean inclusive)
            throws AccessDeniedException, InvalidConfigurationException {
        return new DataStream() {

            private int counter = 0;

            @Override
            public void close() throws IOException {
            }

            @Override
            public boolean isNextReady() {
                return false;
            }

            @Override
            public Bookmarkable next() throws AccessDeniedException {
                if (counter++ < 2) {
                    Map<String, String> credentials = credentialsProvider.getCredentials();
                    String proxyPassword = proxyProvider.getProxyPassword();
                    return new DummyData();
                }
                return null;
            }
        };
    }

    private static class DummyData implements Data {

        @Override
        public Map<String, String> getCustomMetadata() {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("key1", "value1");
            return metadata;
        }

        @Override
        public String getDataType() {
            return "dummy/dummy";
        }

        @Override
        public String getMimeType() {
            return "application/json";
        }

        @Override
        public String getCharset() {
            return "utf-8";
        }

        @Override
        public InputStream getInputStream() {
            String json = "[{\"metric\":\"world\"}]";
            return new ByteArrayInputStream(json.getBytes(Charset.forName("utf-8")));
        }

        @Override
        public String bookmark() {
            return String.valueOf(new Random().nextInt(100));
        }

        @Override
        public void close() throws IOException {

        }
    }
}
