package com.hp.gaia.provider.alm.run;

import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.ProxyProvider;
import com.hp.gaia.provider.alm.AlmDataConfig;
import com.hp.gaia.provider.alm.AlmDataProvider;
import com.hp.gaia.provider.alm.StateMachine;
import org.springframework.stereotype.Component;

@Component
public class AlmRunDataProvider extends AlmDataProvider {

    @Override
    public String getProviderId() {
        return "alm/run";
    }

    @Override
    protected StateMachine createStateMachine(AlmDataConfig dataConfig, CredentialsProvider credentialsProvider, ProxyProvider proxyProvider, String bookmark, boolean inclusive, String providerId) {

        StateMachine ret = new RunStateMachine(dataConfig, credentialsProvider, proxyProvider, providerId);
        ret.init(bookmark, inclusive);

        return ret;
    }
}
