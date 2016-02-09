package com.hp.gaia.provider.alm.issue.change;

import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.ProxyProvider;
import com.hp.gaia.provider.alm.AlmDataConfig;
import com.hp.gaia.provider.alm.AlmDataProvider;
import com.hp.gaia.provider.alm.StateMachine;
import org.springframework.stereotype.Component;

@Component
public class AlmIssueChangeDataProvider extends AlmDataProvider {

    @Override
    public String getProviderId() {
        return "alm/issue/change";
    }

    @Override
    protected StateMachine createStateMachine(AlmDataConfig dataConfig, CredentialsProvider credentialsProvider, ProxyProvider proxyProvider, String bookmark, boolean inclusive, String providerId) {

        StateMachine ret = new IssueChangeStateMachine(dataConfig, credentialsProvider, proxyProvider, providerId);
        ret.init(bookmark, inclusive);

        return ret;
    }
}
