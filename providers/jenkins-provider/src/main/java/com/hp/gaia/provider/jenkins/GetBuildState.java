package com.hp.gaia.provider.jenkins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.hp.gaia.provider.Data;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GetBuildState implements State {
    private static final Logger logger = LogManager.getLogger(GetBuildState.class);

    private List<BuildInfo> jobPath;

    private boolean inclusive;

    public GetBuildState(final List<BuildInfo> jobPath, final boolean inclusive) {
        this.jobPath = jobPath;
        this.inclusive = inclusive;
    }

    @Override
    public Data execute(final StateContext stateContext) {
        BuildDetails buildDetails = getBuildDetails(stateContext);
        if (buildDetails != null) {
            prepareNextStates(stateContext, buildDetails);
            // TODO: fetch test data for this build
        }
        return null;
    }

    private void prepareNextStates(final StateContext stateContext, final BuildDetails buildDetails) {
        // sub-builds
        List<BuildInfo> buildInfos = buildDetails.getSubBuilds();
        if (!CollectionUtils.isEmpty(buildInfos)) {
            for (BuildInfo buildInfo : buildInfos) {
                List<BuildInfo> newJobPath = new ArrayList<>(jobPath);
                newJobPath.add(buildInfo);
                stateContext.add(new GetBuildState(newJobPath, true));
            }
        }
        // matrix build ref
        if (buildDetails.getMatrixBuildRef() != null) {
            BuildInfo matrixBuildRef = buildDetails.getMatrixBuildRef();
            List<BuildInfo> newJobPath = new ArrayList<>(jobPath);
            newJobPath.add(matrixBuildRef);
            stateContext.add(new GetBuildState(newJobPath, true));
        }
    }

    private BuildDetails getBuildDetails(final StateContext stateContext) {
        TestDataConfiguration testDataConfiguration = stateContext.getTestDataConfiguration();
        CloseableHttpClient httpClient = stateContext.getHttpClient();

        BuildInfo buildInfo = jobPath.get(jobPath.size() - 1);
        // http://mydtbld0049.isr.hp.com:8080/jenkins/job/AgM-SaaS-Full-Root-master/4013/api/json?pretty=true&tree=actions[parameters[name,value]],number,building,result,timestamp,url,runs[number,url],subBuilds[buildNumber,jobName,url]
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(buildInfo.getUri())
                .path("/api/json")
                .queryParam("tree",
                        "actions[parameters[name,value]],number,building,result,timestamp,runs[number,url],subBuilds[buildNumber,jobName,url]");
        final String requestUri = uriBuilder.build().encode().toString();
        HttpGet httpGet = new HttpGet(requestUri);
        httpGet.setHeader("Accept", "application/json");
        logger.debug("Fetching data from " + requestUri);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch build data", e);
        }
        // check response code
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 404) {
                logger.debug("Build " + buildInfo.getJob() + "/" + buildInfo.getBuildNumber() + " not found");
                return null;
            }
            if (!(statusCode >= 200 && statusCode < 300)) {
                throw new RuntimeException("Failed to fetch build, status code " + statusCode + " " +
                        response.getStatusLine().getReasonPhrase());
            }
            // 2xx ok, parse build list
            return parseBuildDetails(stateContext, buildInfo, response);
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    private BuildDetails parseBuildDetails(final StateContext stateContext, BuildInfo buildInfo, final CloseableHttpResponse response) {
        TestDataConfiguration testDataConfiguration = stateContext.getTestDataConfiguration();
        BuildDetails buildDetails = new BuildDetails();
        InputStream is = null;
        try {
            is = response.getEntity().getContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            ObjectNode rootNode = (ObjectNode) JsonSerializer.getObjectMapper().readTree(is);
            // get basic properties
            BooleanNode buildingNode = (BooleanNode) rootNode.get("building");
            buildDetails.setBuilding(buildingNode.asBoolean());
            NumericNode numberNode = (NumericNode) rootNode.get("number");
            buildDetails.setNumber(numberNode.asInt());
            TextNode resultNode = (TextNode) rootNode.get("result");
            buildDetails.setResult(resultNode.asText());
            NumericNode timestampNode = (NumericNode) rootNode.get("timestamp");
            buildDetails.setTimestamp(timestampNode.asLong());
            // get matrixBuildRef
            if (rootNode.has("runs")) {
                ArrayNode matrixBuildRefs = (ArrayNode) rootNode.get("runs");
                if (matrixBuildRefs != null) {
                    for (int i = 0; i < matrixBuildRefs.size(); i++) {
                        ObjectNode matrixBuildRefNode = (ObjectNode) matrixBuildRefs.get(i);
                        NumericNode refNumberNode = (NumericNode) matrixBuildRefNode.get("number");
                        int refNumber = refNumberNode.asInt();
                        TextNode refUrlNode = (TextNode) matrixBuildRefNode.get("url");
                        String refUrl = refUrlNode.asText();
                        if (buildDetails.getNumber() == refNumber) {
                            // we found the correct matrix build ref
                            buildDetails.setMatrixBuildRef(new BuildInfo(buildInfo.getJob(), refNumber, refUrl));
                        }
                    }
                }
            }
            // get sub builds
            if (rootNode.has("subBuilds")) {
                ArrayNode subBuildsArr = (ArrayNode) rootNode.get("subBuilds");
                if (subBuildsArr != null) {
                    buildDetails.setSubBuilds(new ArrayList<>());
                    for (int i = 0; i < subBuildsArr.size(); i++) {
                        ObjectNode buildRefNode = (ObjectNode) subBuildsArr.get(i);
                        NumericNode refNumberNode = (NumericNode) buildRefNode.get("buildNumber");
                        int refNumber = refNumberNode.asInt();
                        TextNode jobNameNode = (TextNode) buildRefNode.get("jobName");
                        String jobName = jobNameNode.asText();
                        TextNode refUrlNode = (TextNode) buildRefNode.get("url");
                        String refUrl = refUrlNode.asText();
                        // url is relative, make it absolute
                        String buildUri = createBuildUri(testDataConfiguration, refUrl);
                        buildDetails.getSubBuilds().add(new BuildInfo(jobName, refNumber, buildUri));
                    }
                }
            }
            // get parameters
            if (rootNode.has("actions")) {
                ArrayNode actionsArr = (ArrayNode) rootNode.get("actions");
                if (actionsArr != null) {
                    for (int i = 0; i < actionsArr.size(); i++) {
                        ObjectNode actionNode = (ObjectNode) actionsArr.get(i);
                        if (actionNode.has("parameters")) {
                            buildDetails.setParameters(new HashMap<>());
                            ArrayNode parametersArr = (ArrayNode) actionNode.get("parameters");
                            for (int p = 0; p < parametersArr.size(); p++) {
                                ObjectNode paramNode = (ObjectNode) parametersArr.get(p);
                                TextNode nameNode = (TextNode) paramNode.get("name");
                                JsonNode valueNode = paramNode.get("value"); // can be null or other type than text
                                buildDetails.getParameters().put(nameNode.asText(), valueNode != null ? valueNode.asText() : null);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        return buildDetails;
    }

    private String createBuildUri(TestDataConfiguration testDataConfiguration, String relativePath) {
        URI locationUri = testDataConfiguration.getLocation();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(locationUri)
                .path("/" + relativePath);
        return uriBuilder.build().toString();
    }
}
