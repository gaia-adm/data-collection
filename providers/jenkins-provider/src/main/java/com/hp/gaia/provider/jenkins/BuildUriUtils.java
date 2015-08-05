package com.hp.gaia.provider.jenkins;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class BuildUriUtils {

    private BuildUriUtils() {
    }

    public static String getBuildUriPath(URI locationUri, String buildUriStr) {
        URI buildUri = null;
        try {
            buildUri = new URI(buildUriStr);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String buildUriPath = buildUri.getPath();
        buildUriPath = StringUtils.removeStart(buildUriPath, locationUri.getPath());
        return StringUtils.removeStart(buildUriPath, "/");
    }

    public static String createBuildUri(URI locationUri, String buildUriPath) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(locationUri)
                .path("/" + buildUriPath);
        return uriBuilder.build().toString();
    }

}
