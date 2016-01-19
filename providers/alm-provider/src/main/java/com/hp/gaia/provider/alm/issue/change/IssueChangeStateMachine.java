package com.hp.gaia.provider.alm.issue.change;

import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.ProxyProvider;
import com.hp.gaia.provider.alm.AlmDataConfig;
import com.hp.gaia.provider.alm.StateMachine;
import com.hp.gaia.provider.alm.util.JsonSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by belozovs on 8/24/2015.
 *
 */
public class IssueChangeStateMachine extends StateMachine {

    private static final Logger log = LogManager.getLogger(IssueChangeStateMachine.class);

    public IssueChangeStateMachine(final AlmDataConfig dataConfig, final CredentialsProvider credentialsProvider, final ProxyProvider proxyProvider, String providerId) {

        super(dataConfig, credentialsProvider, proxyProvider, providerId);
    }

    /**
     * Initializes 1st state based on supplied bookmark.
     * NOTE: inclusive is not in use currently, any data fetch will be done for auditID bigger than bookmarked (i.e., inclusive = false)
     */
    public void doInit(final String bookmark, final boolean inclusive) {

        IssueChangeState state = new IssueChangeState();
        if(bookmark != null){
            IssueChangeBookmark icb = JsonSerializer.deserialize(bookmark, IssueChangeBookmark.class);
            if(icb != null){
                state.setAuditId(icb.getLastAuditId());
            }
        }
        log.debug("Starting with auditId " + state.getAuditId());

        add(state);
    }

    @Override
    protected String getAlmXmlChildTag() {

        return "Audit";
    }

    @Override
    protected String getAlmXmlParentTag() {

        return "Audits";
    }
}
