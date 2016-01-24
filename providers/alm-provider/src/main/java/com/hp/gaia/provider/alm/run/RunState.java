package com.hp.gaia.provider.alm.run;

import com.hp.gaia.provider.Bookmarkable;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.alm.DataImpl;
import com.hp.gaia.provider.alm.State;
import com.hp.gaia.provider.alm.StateContext;
import com.hp.gaia.provider.alm.StateMachine;
import com.hp.gaia.provider.alm.util.AlmRestUtils;
import com.hp.gaia.provider.alm.util.AlmXmlUtils;
import com.hp.gaia.provider.alm.util.JsonSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RunState implements State {

    private final static Log log = LogFactory.getLog(RunState.class);

    private String lastModified;

    public void setLastModified(String lastModified) {

        this.lastModified = lastModified;
    }

    public String getLastModified() {

        return lastModified;
    }

    @Override
    public Bookmarkable execute(StateContext stateContext) {

        try {
            return getRuns(stateContext);
        } catch (URISyntaxException e) {
            log.error(e);
            throw new RuntimeException("Failed to fetch runs; " + e.getMessage());
        }
    }

    private Data getRuns(StateContext stateContext) throws URISyntaxException {

        URI locationUri = stateContext.getDataConfiguration().getLocation();
        String domain = stateContext.getDataConfiguration().getDomain();
        String project = stateContext.getDataConfiguration().getProject();
        Map<String, String> credentials = stateContext.getCredentialsProvider().getCredentials();

        AlmRestUtils almRestUtils = new AlmRestUtils(stateContext.getHttpClient());
        // TODO: use cookie if already logged-in
        almRestUtils.login(locationUri, credentials);
        initLastModified(almRestUtils, locationUri, stateContext.getDataConfiguration().getHistoryDays());
        URIBuilder builder = prepareGetEntityUrl(locationUri, domain, project, StateMachine.PAGE_SIZE, ((StateMachine) stateContext).getNextStartIndex());

        Map<String, String> headers = new HashMap<>(2);
        headers.put(RestConstants.ACCEPT, RestConstants.APPLICATION_JSON);

        return createData(stateContext, almRestUtils.runGetRequest(builder.build(), headers));
    }

    private void initLastModified(AlmRestUtils almRestUtils, URI locationUri, int historyDays) {

        try {
            if (StringUtils.isEmpty(getLastModified())) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Calendar calendar = Calendar.getInstance();
                String time = almRestUtils.getAlmServerTime(locationUri);
                calendar.setTime(format.parse(time));
                calendar.add(Calendar.DATE, historyDays * (-1));
                setLastModified(format.format(calendar.getTime()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ALM run last modified", e);
        }
    }

    //example: http://localhost:8082/qcbin/rest/domains/MY_DOMAIN/projects/MY_PROJECT/runs?query={id[>0];last-modified[%3E%272015-07-23%2010:06:27%27]}}
    private URIBuilder prepareGetEntityUrl(URI locationUri, String domain, String project, int pageSize, int startIndex) {

        URIBuilder builder = new URIBuilder();
        builder.setScheme(locationUri.getScheme()).setHost(locationUri.getHost()).setPort(locationUri.getPort()).setPath(locationUri.getPath() + "/rest/domains/" + domain + "/projects/" + project + "/runs");
        builder.addParameter("query", "{last-modified[>'" + getLastModified() + "']}");
        builder.addParameter("page-size", Integer.toString(pageSize));
        builder.addParameter("start-index", Integer.toString(startIndex));

        return builder;
    }

    //create data object; returns null, if no changes happened since previous collection in order to prevent sending empty XMLs to ResultUploader
    private Data createData(StateContext stateContext, HttpResponse response) {

        Map<String, String> customMetadata = new HashMap<>();
        customMetadata.put("ALM_LOCATION", stateContext.getDataConfiguration().getLocation().toString());
        customMetadata.put("DOMAIN", stateContext.getDataConfiguration().getDomain());
        customMetadata.put("PROJECT", stateContext.getDataConfiguration().getProject());

        try {
            //make the entity repeatable - we need to fetch maximal auditId from the content and let ResultUploadServiceBase class to get the content as well
            HttpEntity entity = response.getEntity();
            ContentType ct = ContentType.get(entity);
            StringEntity stringEntity = new StringEntity(EntityUtils.toString(response.getEntity()), ct);
            response.setEntity(stringEntity);
            log.trace("CONTENT: " + EntityUtils.toString(stringEntity));
        } catch (IOException e) {
            throw new RuntimeException("Cannot make entity repeatable");
        }

        String content;
        try {
            content = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException("Cannot read entity content");
        }

        JSONObject json = new JSONObject(content);
        Object runCount = json.get("TotalResults");
        if (runCount == null || ((String)runCount).isEmpty() || Integer.valueOf(((String)runCount)) == 0) {
            log.debug("No run events happened, there is nothing to sent");
            return null;
        }

        RunBookmark runBookmark = new RunBookmark();
        runBookmark.setLastModified(getLastModified());
        log.debug("New bookmark is set to " + runBookmark.getLastModified());
        String bookmark = JsonSerializer.serialize(runBookmark);

        return new DataImpl(customMetadata, stateContext.getDataType(), (CloseableHttpResponse) response, bookmark);
    }
}
