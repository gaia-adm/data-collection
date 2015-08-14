package com.hp.gaia.provider.circleci.test.state;

import com.hp.gaia.provider.Bookmarkable;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.circleci.build.BuildDetails;
import com.hp.gaia.provider.circleci.common.DataImpl;
import com.hp.gaia.provider.circleci.test.CircleTestDataConfig;
import com.hp.gaia.provider.circleci.util.JsonSerializer;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GetTestResultsState implements State {

    private static final Logger logger = LogManager.getLogger(GetTestResultsState.class);

    private BuildDetails buildDetails;

    public GetTestResultsState(final BuildDetails buildDetails) {
        this.buildDetails = buildDetails;
    }

    @Override
    public Bookmarkable execute(final StateContext stateContext) {
        return getTestData(stateContext);
    }

    private Data getTestData(final StateContext stateContext) {
        CircleTestDataConfig testDataConfiguration = stateContext.getTestDataConfiguration();
        CloseableHttpClient httpClient = stateContext.getHttpClient();
        String username = testDataConfiguration.getUsername();
        String project = testDataConfiguration.getProject();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://circleci.com/api/v1/project")
                .path("/" + username)
                .path("/" + project)
                .path("/" + buildDetails.getNumber())
                .path("/tests")
                .queryParam("circle-token", stateContext.getCircleToken());
        final String requestUri = uriBuilder.build().encode().toString();
        HttpGet httpGet = new HttpGet(requestUri);
        httpGet.setHeader("Accept", "application/json");
        logger.debug("Fetching tests from " + requestUri);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch test data", e);
        }
        // check response code
        boolean skipClose = false;
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!(statusCode >= 200 && statusCode < 300)) {
                consumeResponse(requestUri, response);
                throw new RuntimeException("Failed to fetch tests, status code " + statusCode + " " +
                        response.getStatusLine().getReasonPhrase());
            } else {
                // 2xx ok, construct Data
                Data data = createData(stateContext, response, buildDetails);
                skipClose = true;
                return data;
            }
        } finally {
            // else will be closed by Data class
            if (!skipClose) {
                IOUtils.closeQuietly(response);
            }
        }
    }

    private static Data createData(final StateContext stateContext, final CloseableHttpResponse response,
                                   final BuildDetails buildDetails) {
        Map<String, String> customMetadata = new HashMap<>();
        customMetadata.put("SCM_BRANCH", buildDetails.getBranch());
        customMetadata.put("SCM_REPO_NAME", buildDetails.getReponame());
        customMetadata.put("SCM_URL", buildDetails.getVcsUrl());
        customMetadata.put("BUILD_URI", buildDetails.getBuildUrl());
        customMetadata.put("BUILD_NUMBER", String.valueOf(buildDetails.getNumber()));
        customMetadata.put("BUILD_STATUS", String.valueOf(buildDetails.getStatus()));
        customMetadata.put("BUILD_START_TIME", String.valueOf(buildDetails.getStartTime()));

        TestDataBookmark testDataBookmark = new TestDataBookmark(buildDetails.getNumber());
        String bookmark = JsonSerializer.serialize(testDataBookmark);

        return new DataImpl(customMetadata, "circleci/test", response, bookmark);
    }

    private static void consumeResponse(final String requestUri, final CloseableHttpResponse response) {
        try {
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            // not fatal, just log
            logger.error("Failed to receive full response for " + requestUri, e);
        }
    }
}
