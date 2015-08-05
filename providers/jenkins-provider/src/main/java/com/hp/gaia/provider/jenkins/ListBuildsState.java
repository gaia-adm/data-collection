package com.hp.gaia.provider.jenkins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.hp.gaia.provider.Data;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
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
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ListBuildsState implements State {

    private static final Logger logger = LogManager.getLogger(ListBuildsState.class);

    private BuildInfo buildInfo;

    private boolean inclusiveParam;

    private boolean skipParam;

    public ListBuildsState() {
    }

    public ListBuildsState(final BuildInfo rootBuildInfo, final boolean inclusiveParam, final boolean skipParam) {
        this.buildInfo = rootBuildInfo;
        this.inclusiveParam = inclusiveParam;
        this.skipParam = skipParam;
    }

    @Override
    public Data execute(final StateContext stateContext) {
        TestDataConfiguration testDataConfiguration = stateContext.getTestDataConfiguration();
        CloseableHttpClient httpClient = stateContext.getHttpClient();
        URI locationUri = testDataConfiguration.getLocation();
        String jobName = testDataConfiguration.getJob();

        // http://mydtbld0049.isr.hp.com:8080/jenkins/job/AgM-SaaS-Full-Root-master/api/json?tree=builds[number,url]
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(locationUri)
                .path("/job")
                .pathSegment(jobName)
                .path("/api/json")
                .queryParam("tree", "builds[number,url,building]");
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
            processBuildList(stateContext, response);
        } finally {
            IOUtils.closeQuietly(response);
        }

        // this state class always returns null
        return null;
    }

    /**
     * Processes list of builds. Note that the build list starts with the latest build and goes into the past.
     */
    private void processBuildList(StateContext stateContext, final HttpResponse response) {
        final TestDataConfiguration testDataConfiguration = stateContext.getTestDataConfiguration();
        LinkedList<State> stack = new LinkedList<>();
        InputStream is = null;
        try {
            is = response.getEntity().getContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            JsonNode jsonNode = JsonSerializer.getObjectMapper().readTree(is);
            ObjectNode rootNode = (ObjectNode) jsonNode;
            ArrayNode buildsArr = (ArrayNode) rootNode.get("builds");
            for (int i = 0; i < buildsArr.size(); i++) {
                ObjectNode buildNode = (ObjectNode) buildsArr.get(i);
                boolean stop = processBuildNode(testDataConfiguration, stack, buildNode);
                if (stop) {
                    break;
                }
            }
            // copy our local stack to StateContext. First item must remain first.
            Iterator<State> stackIterator = stack.descendingIterator();
            while (stackIterator.hasNext()) {
                stateContext.add(stackIterator.next());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private boolean processBuildNode(final TestDataConfiguration testDataConfiguration, final Deque<State> stack,
                                     final ObjectNode buildNode) {
        NumericNode numberNode = (NumericNode) buildNode.get("number");
        int number = numberNode.asInt();
        TextNode urlNode = (TextNode) buildNode.get("url");
        String url = urlNode.asText();
        BooleanNode buildingNode = (BooleanNode) buildNode.get("building");
        boolean building = buildingNode.asBoolean();
        if (!building) {
            if (buildInfo != null) {
                int startBuildNumber = buildInfo.getBuildNumber();
                if (startBuildNumber > number) {
                    return true;
                } else if (startBuildNumber == number) {
                    if (!skipParam) {
                        addGetBuildState(testDataConfiguration, stack, number, url, inclusiveParam);
                    }
                    return true;
                } else {
                    addGetBuildState(testDataConfiguration, stack, number, url, true);
                }
            } else {
                // no start build number was specified, all builds match
                addGetBuildState(testDataConfiguration, stack, number, url, true);
            }
        } else {
            logger.debug("Found build " + number + " still in building state, skipping it and later builds..");
            // we found a build that is still building, clear the stack as we must not process any builds that follow it
            stack.clear();
        }
        return false;
    }

    private static void addGetBuildState(TestDataConfiguration testDataConfiguration, Deque<State> stack,
                                         int number, String url, boolean inclusive) {
        URI locationUri = testDataConfiguration.getLocation();
        String jobName = testDataConfiguration.getJob();
        List<BuildInfo> jobPath = new ArrayList<>();
        jobPath.add(new BuildInfo(jobName, number, BuildUriUtils.getBuildUriPath(locationUri, url)));
        GetBuildState getBuildState = new GetBuildState(jobPath, inclusive);
        stack.addFirst(getBuildState);
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
