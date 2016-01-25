package com.hp.gaia.provider.alm.run;

import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.ProxyProvider;
import com.hp.gaia.provider.alm.AlmDataConfig;
import com.hp.gaia.provider.alm.StateMachine;
import com.hp.gaia.provider.alm.util.JSONUtils;
import com.hp.gaia.provider.alm.util.JsonSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Created by belozovs on 8/24/2015.
 *
 */
public class RunStateMachine extends StateMachine {

    private static final Logger log = LogManager.getLogger(RunStateMachine.class);

    public RunStateMachine(final AlmDataConfig dataConfig, final CredentialsProvider credentialsProvider, final ProxyProvider proxyProvider, String providerId) {

        super(dataConfig, credentialsProvider, proxyProvider, providerId);
    }

    /**
     * Initializes 1st state based on supplied bookmark.
     * NOTE: inclusive is not in use currently, any data fetch will be done for auditID bigger than bookmarked (i.e., inclusive = false)
     */
    public void doInit(final String bookmark, final boolean inclusive) {

        RunState state = new RunState();
        if(bookmark != null){
            RunBookmark runBookmark = JsonSerializer.deserialize(bookmark, RunBookmark.class);
            if(runBookmark != null){
                state.setLastModified(runBookmark.getLastModified());
            }
        }
        log.debug("Starting with runs last modified: " + state.getLastModified());
        add(state);
    }

    @Override
    protected int getResultsReceived(String content) {

        return JSONUtils.getArraySize(content, "entities");
    }

    @Override
    protected int getTotalResults(String content) {

        return JSONUtils.getIntValue(content, "TotalResults");
    }

    @Override
    protected String getAlmXmlParentTag() {

        return "Entities";
    }

    @Override
    protected String getAlmXmlChildTag() {

        return "Entity";
    }
}
