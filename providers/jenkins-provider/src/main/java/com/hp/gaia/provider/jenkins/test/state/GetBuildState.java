package com.hp.gaia.provider.jenkins.test.state;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.hp.gaia.provider.Bookmarkable;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.jenkins.build.BuildDetails;
import com.hp.gaia.provider.jenkins.build.BuildInfo;
import com.hp.gaia.provider.jenkins.build.BuildUriUtils;
import com.hp.gaia.provider.jenkins.common.BookmarkableImpl;
import com.hp.gaia.provider.jenkins.common.DataImpl;
import com.hp.gaia.provider.jenkins.test.JenkinsTestDataConfig;
import com.hp.gaia.provider.jenkins.util.JsonSerializer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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
import java.util.Map;

public class GetBuildState implements State {
    private static final Logger logger = LogManager.getLogger(GetBuildState.class);

    private final List<BuildInfo> buildPath;

    private final boolean inclusive;

    public GetBuildState(final List<BuildInfo> buildPath, final boolean inclusive) {
        this.buildPath = buildPath;
        this.inclusive = inclusive;
    }

    @Override
    public Bookmarkable execute(final StateContext stateContext) {
        BuildDetails buildDetails = getBuildDetails(stateContext);
        Bookmarkable data = null;
        if (buildDetails != null) {
            prepareNextStates(stateContext, buildDetails);
            if (inclusive) {
                data = getTestData(stateContext, buildDetails);
            }
        }

        if (data != null) {
            return data;
        } else {
            return createBookmarkable(buildPath);
        }
    }

    private Data getTestData(final StateContext stateContext, final BuildDetails buildDetails) {
        CloseableHttpClient httpClient = stateContext.getHttpClient();
        URI locationUri = stateContext.getTestDataConfiguration().getLocation();

        BuildInfo buildInfo = buildPath.get(buildPath.size() - 1);
        final String buildUri = BuildUriUtils.createBuildUri(locationUri, buildInfo.getUriPath());
        // do not transfer stdout,stderr,stacktrace as these can get very long
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(buildUri)
                .path("/testReport/api/json")
                .queryParam("tree", "duration,failCount,passCount,skipCount,suites[cases[age,className,duration,errorDetails,failedSince,name,skipped,skippedMessage,status],duration,id,name,timestamp]");
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
            if (statusCode == 404) {
                consumeResponse(requestUri, response);
                logger.debug("No tests found for " + buildInfo.getJob() + "/" + buildInfo.getBuildNumber());
                return null;
            }
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

    private Data createData(final StateContext stateContext, final CloseableHttpResponse response,
                            final BuildDetails buildDetails) {
        Map<String, String> customMetadata = new HashMap<>();
        BuildInfo buildInfo = buildPath.get(buildPath.size() - 1);
        customMetadata.put("BUILD_SERVER_URI", stateContext.getTestDataConfiguration().getLocation().toString());
        customMetadata.put("JOB_NAME", buildInfo.getJob());
        customMetadata.put("BUILD_URI_PATH", buildInfo.getUriPath());
        customMetadata.put("BUILD_NUMBER", String.valueOf(buildDetails.getNumber()));
        customMetadata.put("BUILD_RESULT", String.valueOf(buildDetails.getResult()));
        customMetadata.put("BUILD_TIMESTAMP", String.valueOf(buildDetails.getTimestamp()));
        final BuildInfo rootBuild = buildPath.get(0);
        customMetadata.put("ROOT_JOB_NAME", rootBuild.getJob());
        customMetadata.put("ROOT_BUILD_NUMBER", String.valueOf(rootBuild.getBuildNumber()));
        JenkinsTestDataConfig testDataConfiguration = stateContext.getTestDataConfiguration();
        // also add custom tags
        List<String> customTags = testDataConfiguration.getCustomTags();
        if (customTags != null) {
            Map<String, String> parameters = buildDetails.getParameters();
            for (String customTag : customTags) {
                if (parameters.containsKey(customTag)) {
                    customMetadata.put(customTag, parameters.get(customTag));
                }
            }
            customMetadata.put("CUSTOM_TAGS", StringUtils.join(customTags, ','));
        }

        TestDataBookmark testDataBookmark = new TestDataBookmark(buildPath);
        String bookmark = JsonSerializer.serialize(testDataBookmark);

        return new DataImpl(customMetadata, "jenkins/test", response, bookmark);
    }

    private void prepareNextStates(final StateContext stateContext, final BuildDetails buildDetails) {
        // sub-builds
        List<BuildInfo> buildInfos = buildDetails.getSubBuilds();
        if (!CollectionUtils.isEmpty(buildInfos)) {
            for (BuildInfo buildInfo : buildInfos) {
                List<BuildInfo> newBuildPath = new ArrayList<>(buildPath);
                newBuildPath.add(buildInfo);
                stateContext.add(new GetBuildState(newBuildPath, true));
            }
        }
        // matrix build ref
        if (buildDetails.getMatrixBuildRef() != null) {
            BuildInfo matrixBuildRef = buildDetails.getMatrixBuildRef();
            List<BuildInfo> newBuildPath = new ArrayList<>(buildPath);
            newBuildPath.add(matrixBuildRef);
            stateContext.add(new GetBuildState(newBuildPath, true));
        }
    }

    private BuildDetails getBuildDetails(final StateContext stateContext) {
        CloseableHttpClient httpClient = stateContext.getHttpClient();
        URI locationUri = stateContext.getTestDataConfiguration().getLocation();

        BuildInfo buildInfo = buildPath.get(buildPath.size() - 1);
        final String buildUri = BuildUriUtils.createBuildUri(locationUri, buildInfo.getUriPath());
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(buildUri)
                .path("/api/json")
                .queryParam("tree", "actions[parameters[name,value]],number,building,result,timestamp,url,runs[number,url],subBuilds[buildNumber,jobName,url]");
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
                consumeResponse(requestUri, response);
                logger.debug("Build " + buildInfo.getJob() + "/" + buildInfo.getBuildNumber() + " not found");
                return null;
            }
            if (!(statusCode >= 200 && statusCode < 300)) {
                consumeResponse(requestUri, response);
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
        JenkinsTestDataConfig testDataConfiguration = stateContext.getTestDataConfiguration();
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
            TextNode urlNode = (TextNode) rootNode.get("url");
            buildDetails.setUrl(urlNode.asText());
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
                            String buildUriPath = BuildUriUtils.getBuildUriPath(testDataConfiguration.getLocation(), refUrl);
                            buildDetails.setMatrixBuildRef(new BuildInfo(buildInfo.getJob(), refNumber, buildUriPath));
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
                        refUrl = StringUtils.removeStart(refUrl, "/");
                        buildDetails.getSubBuilds().add(new BuildInfo(jobName, refNumber, refUrl));
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }

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

    private static Bookmarkable createBookmarkable(final List<BuildInfo> buildPath) {
        TestDataBookmark testDataBookmark = new TestDataBookmark(buildPath);
        String bookmark = JsonSerializer.serialize(testDataBookmark);
        return new BookmarkableImpl(bookmark);
    }
}
