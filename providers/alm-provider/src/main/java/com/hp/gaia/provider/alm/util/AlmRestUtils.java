package com.hp.gaia.provider.alm.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by belozovs on 8/25/2015.
 * Some auxiliary methods to communicate with ALM 12 ReST API
 */
public class AlmRestUtils {

    private final static Log log = LogFactory.getLog(AlmRestUtils.class);

    private final CloseableHttpClient httpClient;
    private RequestConfig requestConfig;

    public AlmRestUtils(CloseableHttpClient httpClient) {
        int TIMEOUT_MSEC = 10000;
        this.httpClient = httpClient;
        requestConfig = RequestConfig.custom().setConnectionRequestTimeout(TIMEOUT_MSEC).setConnectTimeout(TIMEOUT_MSEC).setSocketTimeout(TIMEOUT_MSEC).build();
    }

    /**
     * Login to ALM
     * Includes authentication and session creation
     * Method does not return any value, all needed cookies (LWSSO_COOKIE_KEY, ALM_USER, QC_SESSION) are stored in CookiesStorage of HttpClient
     *
     * @param baseUri     - as set in providers.json, generally http://<host>:<port>/qcbin
     * @param credentials - map of credentials, as defined in credentials.json
     */
    public void login(URI baseUri, Map<String, String> credentials) {

        String authenticationBody = "<alm-authentication><user>" + credentials.get("username") + "</user><password>" + credentials.get("password") + "</password></alm-authentication>";
        String authenticateString = baseUri.toString() + "/authentication-point/alm-authenticate";
        runPostRequest(URI.create(authenticateString), authenticationBody);

        String createSessionBody = "<session-parameters><client-type>Gaia ReST Client</client-type><time-out>60</time-out></session-parameters>";
        String createSessionString = baseUri.toString() + "/rest/site-session";
        runPostRequest(URI.create(createSessionString), createSessionBody);

    }

    /**
     * Run any GET request against given URI
     * Accept and Content-Type are set to application/xml only (this is the only format supported by ALM)
     * Prerequisites: login passed
     *
     * @param uri - URI to call
     * @return - CloseableHttpResponse for further usage
     */
    public HttpResponse runGetRequest(URI uri) {

        HttpGet httpGet = new HttpGet();

        httpGet.setHeader("Accept", "application/xml");
        httpGet.setHeader("Content-Type", "application/xml");
        httpGet.setConfig(requestConfig);

        httpGet.setURI(uri);

        boolean skipClose = false;
        CloseableHttpResponse httpResponse = null;
        try {
            //if (log.isDebugEnabled()) printAllHeaders(httpGet.getURI(), httpGet.getAllHeaders(), false);
            httpResponse = httpClient.execute(httpGet);
            //if (log.isDebugEnabled()) printAllHeaders(httpGet.getURI(), httpGet.getAllHeaders(), true);

            int status = httpResponse.getStatusLine().getStatusCode();

            if (status == HttpStatus.SC_OK) {
                log.debug("Request " + httpGet.getURI() + " finished successfully");
                skipClose = true;
                return httpResponse;
            } else {
                consumeResponse(httpGet.getURI().toString(), httpResponse);
                throw new RuntimeException("Something went wrong with " + httpGet.getURI() + ", status code " + status + " " +
                        httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (!skipClose) {
                IOUtils.closeQuietly(httpResponse);
            }
        }
    }

    /**
     * Run any POST request against given URI
     * Accept and Content-Type are set to application/xml only (this is the only format supported by ALM)
     * Prerequisites: login passed
     *
     * @param uri - URI to call
     * @return - CloseableHttpResponse for further usage
     */
    public CloseableHttpResponse runPostRequest(URI uri, String body) {

        HttpPost httpPost = new HttpPost();

        httpPost.setHeader("Accept", "application/xml");
        httpPost.setHeader("Content-Type", "application/xml");
        httpPost.setConfig(requestConfig);

        httpPost.setURI(uri);
        HttpEntity bodyEntity = new ByteArrayEntity(body.getBytes(Charset.forName("UTF-8")));
        httpPost.setEntity(bodyEntity);

        boolean skipClose = false;
        CloseableHttpResponse httpResponse = null;
        try {
            //if (log.isDebugEnabled()) printAllHeaders(httpPost.getURI(), httpPost.getAllHeaders(), false);
            httpResponse = httpClient.execute(httpPost);
            //if (log.isDebugEnabled()) printAllHeaders(httpPost.getURI(), httpPost.getAllHeaders(), true);

            int status = httpResponse.getStatusLine().getStatusCode();

            if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
                log.debug("Request " + httpPost.getURI() + " finished successfully");
                skipClose = true;
                return httpResponse;
            } else {
                consumeResponse(httpPost.getURI().toString(), httpResponse);
                throw new RuntimeException("Something went wrong with " + httpPost.getURI() + ", status code " + status + " " +
                        httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (!skipClose) {
                IOUtils.closeQuietly(httpResponse);
            }

        }
    }

    /**
     * Print all headers of request or response
     *
     * @param uri               - URI of the request
     * @param allHeaders        - headers array to pring
     * @param isResponseHeaders - true if headers belong to response, false if headers belong to request
     */
    private void printAllHeaders(URI uri, Header[] allHeaders, boolean isResponseHeaders) {

        StringBuilder sb = new StringBuilder();
        if (isResponseHeaders) {
            sb.append("RESPONSE HEADERS for ");
        } else {
            sb.append("REQUEST HEADERS for ");
        }
        sb.append(uri.toString()).append(": ");
        for (Header h : allHeaders) {
            sb.append(">HEADER NAME: ").append(h.getName()).append(" >HEADER VALUE: ").append(h.getValue());
        }
        log.debug(sb);
    }

    private static void consumeResponse(final String requestUri, final CloseableHttpResponse response) {
        try {
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            // not fatal, just log
            log.error("Failed to receive full response for " + requestUri, e);
        }
    }

    //example: http://localhost:8082/qcbin/rest/domains/Default/projects/bp1/audits?login-form-required=y&query={parent-type[defect];parent-id[%3E0];time[%3E%272015-07-23%2010:06:27%27]}&order-by={time[asc]}
    public URIBuilder prepareGetEntityAuditsUrl(URI locationUri, String domain, String project, String parentType, int parentId, int id, String startTime, int pageSize, int startIndex, String orderByTime) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(locationUri.getScheme()).setHost(locationUri.getHost()).setPort(locationUri.getPort()).setPath(locationUri.getPath() + "/rest/domains/" + domain + "/projects/" + project + "/audits");
        if (id > 0) {
            builder.addParameter("query", "{parent-type[" + parentType + "];parent-id[>" + parentId + "];id[>" + id + "]}");
        } else {
            builder.addParameter("query", "{parent-type[" + parentType + "];parent-id[>" + parentId + "];id[>" + id + "];time[>'" + startTime + "']}");
        }
        builder.addParameter("page-size", Integer.toString(pageSize));
        builder.addParameter("start-index", Integer.toString(startIndex));

        if (StringUtils.isNotEmpty(orderByTime)) {
            if (orderByTime.equals("desc")) {
                builder.addParameter("order-by", "{time[desc]}");
            } else if (orderByTime.equals("asc")) {
                builder.addParameter("order-by", "{time[asc]}");
            }
        }
        return builder;
    }


}
