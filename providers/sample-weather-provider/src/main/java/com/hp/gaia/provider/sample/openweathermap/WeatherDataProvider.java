package com.hp.gaia.provider.sample.openweathermap;

import com.hp.gaia.provider.AccessDeniedException;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.DataProvider;
import com.hp.gaia.provider.DataStream;
import com.hp.gaia.provider.InvalidConfigurationException;
import com.hp.gaia.provider.ProxyProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Fetches weather data from <a href="http://api.openweathermap.org/data/2.5/history/city?q=Prague,CZ&type=hour&start=1437572315">http://api.openweathermap.org/data/2.5/history/city</a>.
 */
public class WeatherDataProvider implements DataProvider {

    private static final Logger logger = LogManager.getLogger(WeatherDataProvider.class);

    private static final String CITY = "city";

    private static final String COUNTRY = "country";

    @Override
    public String getProviderId() {
        return "sample/openweathermap/weather";
    }

    @Override
    public DataStream fetchData(final Map<String, String> properties, final CredentialsProvider credentialsProvider,
                                final ProxyProvider proxyProvider, final String bookmark, final boolean inclusive)
            throws AccessDeniedException, InvalidConfigurationException {
        // check properties
        if (StringUtils.isEmpty(properties.get(CITY))) {
            throw new InvalidConfigurationException("Missing city");
        }
        if (StringUtils.isEmpty(properties.get(COUNTRY))) {
            throw new InvalidConfigurationException("Missing country");
        }
        // TODO: use bookmark & inclusive flag
        return new DataStreamImpl(properties.get(CITY), properties.get(COUNTRY), proxyProvider);
    }

    private static class DataStreamImpl implements DataStream {

        private final String city;

        private final String country;

        private final ProxyProvider proxyProvider;

        private CloseableHttpClient httpclient;

        private int counter;

        public DataStreamImpl(final String city, final String country, final ProxyProvider proxyProvider) {
            this.city = city;
            this.country = country;
            this.proxyProvider = proxyProvider;
        }

        @Override
        public boolean isNextReady() {
            return false;
        }

        @Override
        public Data next() throws AccessDeniedException {
            if (counter++ > 0) {
                // workaround for missing bookmark handling, return data only once
                logger.debug("No more data to fetch");
                return null;
            }
            if (httpclient == null) {
                httpclient = createHttpClient();
            }
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("http://api.openweathermap.org")
                    .path("/data/2.5/history/city");
            uriBuilder.queryParam("q", city + "," + country);
            uriBuilder.queryParam("type", "hour");
            final String requestUri = uriBuilder.build().encode().toString();
            HttpGet httpGet = new HttpGet(requestUri);
            // perform request
            logger.debug("Fetching data from " + requestUri);
            CloseableHttpResponse response = null;
            try {
                response = httpclient.execute(httpGet);
            } catch (IOException e) {
                throw new RuntimeException("Failed to fetch weather data", e);
            }
            // check response code
            int statusCode = response.getStatusLine().getStatusCode();
            if (!(statusCode >= 200 && statusCode < 300)) {
                throw new RuntimeException("Failed to fetch weather data, status code " + statusCode + " " +
                        response.getStatusLine().getReasonPhrase());
            }

            return new DataImpl(city, country, response);
        }

        private CloseableHttpClient createHttpClient() {
            // create and configure HttpClient
            BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
            RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
            Proxy proxy = proxyProvider.getProxy();
            if (!Proxy.NO_PROXY.equals(proxy)) {
                // proxy is required, configure it globally
                final InetSocketAddress socketAddress = (InetSocketAddress) proxy.address();
                HttpHost httpHost = new HttpHost(socketAddress.getHostName(), socketAddress.getPort());
                requestConfigBuilder.setProxy(httpHost);
                // TODO: also configure credentials
            }
            RequestConfig globalConfig = requestConfigBuilder.build();
            HttpClientBuilder httpClientBuilder = HttpClients.custom()
                    .setConnectionManager(cm)
                    .setDefaultRequestConfig(globalConfig);
            return httpClientBuilder.build();
        }

        @Override
        public void close() throws IOException {
            IOUtils.closeQuietly(httpclient);
        }
    }

    private static class DataImpl implements Data {

        private final String city;

        private final String country;

        private final CloseableHttpResponse response;

        public DataImpl(final String city, final String country, final CloseableHttpResponse response) {
            this.city = city;
            this.country = country;
            this.response = response;
        }

        @Override
        public Map<String, String> getMetadata() {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("city", city);
            metadata.put("country", country);
            // TODO: consider replacing metric&category with one data type field containing slahes, i.e openweathermap/weather
            // currently metric&category are needed for result upload service and result processing
            metadata.put("metric", "openweathermap");
            metadata.put("category", "weather");
            return metadata;
        }

        @Override
        public String getContentType() {
            return response.getEntity().getContentType().getValue();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return response.getEntity().getContent();
        }

        @Override
        public String bookmark() {
            return null;
        }

        @Override
        public void close() throws IOException {
            IOUtils.closeQuietly(response);
        }
    }
}
