package com.hp.gaia.provider.agm;

import com.hp.gaia.provider.Bookmarkable;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.agm.util.AgmRestUtils;
import com.hp.gaia.provider.agm.util.AgmXmlUtils;
import com.hp.gaia.provider.agm.util.JsonSerializer;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by belozovs on 8/24/2015.
 * Actual AGM issue change data collector
 */
public class IssueChangeState implements State {

    private final static Log log = LogFactory.getLog(IssueChangeState.class);

    private int auditId = 0;

    public int getAuditId() {
        return auditId;
    }

    public void setAuditId(int auditId) {
        this.auditId = auditId;
    }


    @Override
    public Bookmarkable execute(StateContext stateContext) {

        try {
            return getIssueChanges(stateContext);
        } catch (URISyntaxException e) {
            log.error(e);
            throw new RuntimeException("Failed to fetch issue changes; " + e.getMessage());
        }
    }

    //Login to AGM and bring issue changes appear in audit tables starting from auditID stored in previous run bookmark
    private Data getIssueChanges(StateContext stateContext) throws URISyntaxException {

        URI locationUri = stateContext.getIssueChangeDataConfiguration().getLocation();
        String domain = stateContext.getIssueChangeDataConfiguration().getDomain();
        String project = stateContext.getIssueChangeDataConfiguration().getProject();
        String tenantId = stateContext.getIssueChangeDataConfiguration().getTenantId();
        Map<String, String> credentials = stateContext.getCredentialsProvider().getCredentials();

        AgmRestUtils agmRestUtils = new AgmRestUtils(stateContext.getHttpClient());
        agmRestUtils.login(locationUri, credentials, tenantId);
        log.debug("Logged in successfully with user " + credentials.get("username"));
        URIBuilder builder = agmRestUtils.prepareGetEntityAuditsUrl(locationUri, domain, project, "defect", 0, auditId, StateMachine.PAGE_SIZE, ((StateMachine) stateContext).getNextStartIndex(), "asc");
        return createData(stateContext, agmRestUtils.runGetRequest(builder.build()));
    }

    //create data object; returns null, if no changes happened since previous collection in order to prevent sending empty XMLs to ResultUploader
    private Data createData(StateContext stateContext, HttpResponse response) {

        Map<String, String> customMetadata = new HashMap<>();
        customMetadata.put("AGM_LOCATION", stateContext.getIssueChangeDataConfiguration().getLocation().toString());
        customMetadata.put("DOMAIN", stateContext.getIssueChangeDataConfiguration().getDomain());
        customMetadata.put("PROJECT", stateContext.getIssueChangeDataConfiguration().getProject());

        try {
            //make the entity repeatable - we need to fetch maximal auditId from the content and let ResultUploadServiceBase class to get the content as well
            HttpEntity entity = response.getEntity();
            ContentType ct = ContentType.get(entity);
            StringEntity stringEntity = new StringEntity(EntityUtils.toString(response.getEntity()), ct);
            response.setEntity(stringEntity);
            log.debug("CONTENT: " + EntityUtils.toString(stringEntity));
        } catch (IOException e) {
            throw new RuntimeException("Cannot make entity repeatable");
        }


        AgmXmlUtils agmXmlUtils = new AgmXmlUtils();
        IssueChangeBookmark icb = new IssueChangeBookmark();
        String content;
        try {
            content = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException("Cannot read entity content");
        }

        if (agmXmlUtils.countTags(content, "Audit") == 0) {
            log.debug("No issue change events happened, there is nothing to sent");
            return null;
        }

        icb.setLastAuditId(agmXmlUtils.getHighestTagIntegerValue(content, "Id"));
        log.debug("New bookmark is set to " + icb.getLastAuditId());
        String bookmark = JsonSerializer.serialize(icb);

        return new DataImpl(customMetadata, stateContext.getDataType(), (CloseableHttpResponse) response, bookmark);

    }

}
