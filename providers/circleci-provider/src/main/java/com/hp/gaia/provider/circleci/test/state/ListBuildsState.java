package com.hp.gaia.provider.circleci.test.state;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.hp.gaia.provider.Bookmarkable;
import com.hp.gaia.provider.circleci.build.BuildDetails;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListBuildsState implements State {

    private static final Logger logger = LogManager.getLogger(ListBuildsState.class);

    private static final int LIMIT = 30;

    private static final int MAX_BUILDS_LIMIT = 200;

    private Integer fromBuild;

    private boolean inclusive;

    public ListBuildsState(final Integer fromBuild, final boolean inclusive) {
        this.fromBuild = fromBuild;
        this.inclusive = inclusive;
    }

    @Override
    public Bookmarkable execute(final StateContext stateContext) {
        List<BuildDetails> buildDetailsList = getBuildsForCollection(stateContext);
        for (BuildDetails buildDetails : buildDetailsList) {
            stateContext.add(new GetTestResultsState(buildDetails));
        }

        // this state class always returns null
        return null;
    }

    private List<BuildDetails> getBuildsForCollection(final StateContext stateContext) {
        CircleTestDataConfig testDataConfiguration = stateContext.getTestDataConfiguration();
        CloseableHttpClient httpClient = stateContext.getHttpClient();
        String username = testDataConfiguration.getUsername();
        String project = testDataConfiguration.getProject();

        LinkedList<BuildDetails> buildDetailsList = new LinkedList<>();
        int offset = 0;
        while(true) {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://circleci.com/api/v1/project")
                    .path("/" + username)
                    .path("/" + project)
                    .queryParam("circle-token", stateContext.getCircleToken())
                    .queryParam("offset", offset)
                    .queryParam("limit", LIMIT);

            final String requestUri = uriBuilder.build().encode().toString();
            HttpGet httpGet = new HttpGet(requestUri);
            httpGet.setHeader("Accept", "application/json");
            logger.debug("Fetching data from " + requestUri);

            CloseableHttpResponse response = null;
            try {
                response = httpClient.execute(httpGet);
            } catch (IOException e) {
                throw new RuntimeException("Failed to fetch builds", e);
            }
            // check response code
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (!(statusCode >= 200 && statusCode < 300)) {
                    consumeResponse(requestUri, response);
                    throw new RuntimeException("Failed to fetch builds, status code " + statusCode + " " +
                            response.getStatusLine().getReasonPhrase());
                }
                // 2xx ok, parse build list
                List<BuildDetails> retBuildList = parseBuildList(stateContext, response);
                for (BuildDetails buildDetails : retBuildList) {
                    if ("running".equals(buildDetails.getStatus())) {
                        // skip running builds and any builds before it
                        buildDetailsList.clear();
                        continue;
                    }
                    if (fromBuild == null || buildDetails.getNumber() > fromBuild ||
                            (buildDetails.getNumber() == fromBuild && inclusive)) {
                        if (buildDetailsList.size() == MAX_BUILDS_LIMIT) {
                            buildDetailsList.removeFirst();
                        }
                        buildDetailsList.add(buildDetails);
                    } else {
                        // we reached bookmark
                        return buildDetailsList;
                    }
                }
                if (retBuildList.size() < LIMIT) {
                    // we reached the final page
                    break;
                }
            } finally {
                IOUtils.closeQuietly(response);
            }
            offset+=LIMIT;
        }
        return buildDetailsList;
    }

    private static List<BuildDetails> parseBuildList(final StateContext stateContext,
                                                     final CloseableHttpResponse response) {
        List<BuildDetails> buildDetailsList = new ArrayList<>();
        InputStream is = null;
        try {
            is = response.getEntity().getContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            JsonNode jsonNode = JsonSerializer.getObjectMapper().readTree(is);
            ArrayNode rootNode = (ArrayNode) jsonNode;
            for (int i = 0; i < rootNode.size(); i++) {
                ObjectNode buildNode = (ObjectNode) rootNode.get(i);
                BuildDetails buildDetails = parseBuildDetails(buildNode);
                buildDetailsList.add(buildDetails);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return buildDetailsList;
    }

    private static BuildDetails parseBuildDetails(ObjectNode buildNode) {
        BuildDetails buildDetails = new BuildDetails();

        NumericNode buildNumNode = (NumericNode) buildNode.get("build_num");
        buildDetails.setNumber(buildNumNode.asInt());
        TextNode buildUrlNode = (TextNode) buildNode.get("build_url");
        buildDetails.setBuildUrl(buildUrlNode.asText());
        TextNode startTimeNode = (TextNode) buildNode.get("start_time");
        buildDetails.setStartTime(startTimeNode.asText());
        TextNode statusNode = (TextNode) buildNode.get("status");
        buildDetails.setStatus(statusNode.asText());

        TextNode vcsUrlNode = (TextNode) buildNode.get("vcs_url");
        buildDetails.setVcsUrl(vcsUrlNode.asText());
        TextNode reponameNode = (TextNode) buildNode.get("reponame");
        buildDetails.setReponame(reponameNode.asText());
        TextNode branchNode = (TextNode) buildNode.get("branch");
        buildDetails.setBranch(branchNode.asText());

        return buildDetails;
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
