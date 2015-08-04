package com.hp.gaia.provider.jenkins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.hp.gaia.provider.Data;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ListBuildsState implements State {

    private static final Logger logger = LogManager.getLogger(ListBuildsState.class);

    private JobInfo jobInfo;

    private boolean inclusiveParam;

    private boolean skipParam;

    public ListBuildsState() {
    }

    public ListBuildsState(final JobInfo rootJobInfo, final boolean inclusiveParam, final boolean skipParam) {
        this.jobInfo = jobInfo;
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
                .queryParam("tree", "builds[number,url]");
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

    private void processBuildList(StateContext stateContext, final HttpResponse response) {
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
                boolean stop = processBuildNode(stateContext, buildNode);
                if (stop) {
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private boolean processBuildNode(StateContext stateContext, final ObjectNode buildNode) {
        NumericNode numberNode = (NumericNode) buildNode.get("number");
        int number = numberNode.asInt();
        TextNode urlNode = (TextNode) buildNode.get("url");
        String url = urlNode.asText();
        if (jobInfo != null) {
            int startBuildNumber = jobInfo.getBuildNumber();
            if (startBuildNumber > number) {
                return true;
            } else if (startBuildNumber == number) {
                if (!skipParam) {
                    addGetBuildState(stateContext, number, url, inclusiveParam);
                }
                return true;
            } else {
                addGetBuildState(stateContext, number, url, true);
            }
        } else {
            // no start build number was specified, all builds match
            addGetBuildState(stateContext, number, url, true);
        }
        return false;
    }

    private static void addGetBuildState(StateContext stateContext, int number, String url, boolean inclusive) {
        TestDataConfiguration testDataConfiguration = stateContext.getTestDataConfiguration();
        String jobName = testDataConfiguration.getJob();
        List<JobInfo> jobPath = new ArrayList<>();
        jobPath.add(new JobInfo(jobName, number, url));
        GetBuildState getBuildState = new GetBuildState(jobPath, inclusive);
        stateContext.add(getBuildState);
    }
}
