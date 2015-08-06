package com.hp.gaia.provider.jenkins;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.hp.gaia.provider.Bookmarkable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fetches sub-builds of parent build and selects those that follow leaf build in <code>jobPath</code>.
 */
public class GetBuildSiblingsState implements State {
    private static final Logger logger = LogManager.getLogger(GetBuildSiblingsState.class);

    private List<BuildInfo> jobPath;

    public GetBuildSiblingsState(final List<BuildInfo> jobPath) {
        if (jobPath == null || jobPath.size() < 2) {
            throw new IllegalArgumentException("jobPath must have more than one element");
        }
        this.jobPath = jobPath;
    }

    @Override
    public Bookmarkable execute(final StateContext stateContext) {
        List<BuildInfo> subBuilds = getSubBuilds(stateContext);
        prepareNextStates(stateContext, subBuilds);
        return null;
    }

    private void prepareNextStates(final StateContext stateContext, final List<BuildInfo> subBuilds) {
        // prepare next state for parent of our parent
        if (jobPath.size() > 2) {
            List<BuildInfo> newJobPath = new ArrayList<>(jobPath.subList(0, jobPath.size() - 1));
            stateContext.add(new GetBuildSiblingsState(newJobPath));
        }
        if (subBuilds != null) {
            // prepare next states for siblings
            BuildInfo leafBuild = jobPath.get(jobPath.size() - 1);
            for (BuildInfo subBuild : subBuilds) {
                // create states only for builds before selected build (due to usage of stack)
                if (ObjectUtils.equals(leafBuild.getUriPath(), subBuild.getUriPath())) {
                    break;
                }
                List<BuildInfo> newJobPath = new ArrayList<>(jobPath.subList(0, jobPath.size() - 1));
                newJobPath.add(subBuild);
                stateContext.add(new GetBuildState(newJobPath, true));
            }
        }
    }

    private List<BuildInfo> getSubBuilds(final StateContext stateContext) {
        CloseableHttpClient httpClient = stateContext.getHttpClient();
        URI locationUri = stateContext.getTestDataConfiguration().getLocation();

        BuildInfo parentBuild = jobPath.get(jobPath.size() - 2);
        final String buildUri = BuildUriUtils.createBuildUri(locationUri, parentBuild.getUriPath());
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(buildUri)
                .path("/api/json")
                .queryParam("tree", "subBuilds[buildNumber,jobName,url]");
        final String requestUri = uriBuilder.build().encode().toString();
        HttpGet httpGet = new HttpGet(requestUri);
        httpGet.setHeader("Accept", "application/json");
        logger.debug("Fetching data from " + requestUri);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch test data", e);
        }
        // check response code
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 404) {
                consumeResponse(requestUri, response);
                logger.debug("Parent build " + parentBuild.getJob() + "/" + parentBuild.getBuildNumber() + " not found");
                return Collections.emptyList();
            }
            if (!(statusCode >= 200 && statusCode < 300)) {
                consumeResponse(requestUri, response);
                throw new RuntimeException("Failed to fetch parent build, status code " + statusCode + " " +
                        response.getStatusLine().getReasonPhrase());
            } else {
                // 2xx ok, parse sub-builds
                return parseSubBuilds(stateContext, response);
            }
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    private static List<BuildInfo> parseSubBuilds(final StateContext stateContext, final CloseableHttpResponse response) {
        List<BuildInfo> subBuilds = new ArrayList<>();
        InputStream is = null;
        try {
            is = response.getEntity().getContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            ObjectNode rootNode = (ObjectNode) JsonSerializer.getObjectMapper().readTree(is);
            // get sub builds
            if (rootNode.has("subBuilds")) {
                ArrayNode subBuildsArr = (ArrayNode) rootNode.get("subBuilds");
                if (subBuildsArr != null) {
                    for (int i = 0; i < subBuildsArr.size(); i++) {
                        ObjectNode buildRefNode = (ObjectNode) subBuildsArr.get(i);
                        NumericNode refNumberNode = (NumericNode) buildRefNode.get("buildNumber");
                        int refNumber = refNumberNode.asInt();
                        TextNode jobNameNode = (TextNode) buildRefNode.get("jobName");
                        String jobName = jobNameNode.asText();
                        TextNode refUrlNode = (TextNode) buildRefNode.get("url");
                        String refUrl = refUrlNode.asText();
                        refUrl = StringUtils.removeStart(refUrl, "/");
                        subBuilds.add(new BuildInfo(jobName, refNumber, refUrl));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        return subBuilds;
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
