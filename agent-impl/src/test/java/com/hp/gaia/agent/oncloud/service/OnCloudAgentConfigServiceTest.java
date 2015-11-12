package com.hp.gaia.agent.oncloud.service;

import com.hp.gaia.agent.oncloud.GlobalSettings;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by belozovs on 11/11/2015.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(GlobalSettings.class)
public class OnCloudAgentConfigServiceTest extends TestCase {

    OnCloudAgentConfigService agentConfigService = new OnCloudAgentConfigService();
    String gaiaLocation = "http://beta.gaiahub.io:8080";

    @Test
    public void testAllGoodSettingsProvided() throws Exception {
        PowerMock.mockStaticNice(GlobalSettings.class);
        int workerPool = 20;
        int soTimeout = 10;
        int conTimeout = 25;
        EasyMock.expect(GlobalSettings.getGaiaLocation()).andReturn(gaiaLocation).once();
        EasyMock.expect(GlobalSettings.getWorkerPool()).andReturn(workerPool).once();
        EasyMock.expect(GlobalSettings.getSoTimeout()).andReturn(soTimeout).once();
        EasyMock.expect(GlobalSettings.getConnectTimeout()).andReturn(conTimeout).once();
        PowerMock.replay(GlobalSettings.class);

        agentConfigService.init();
        PowerMock.verifyAll();

        assertEquals(gaiaLocation, agentConfigService.getGaiaLocation());
        assertEquals(workerPool, agentConfigService.getWorkerPool());
        assertEquals(soTimeout, agentConfigService.getSoTimeout());
        assertEquals(conTimeout, agentConfigService.getConnectTimeout());
    }

    @Test
    public void testUseDefaultsWherePossible() throws Exception {
        PowerMock.mockStaticPartialNice(GlobalSettings.class, "getGaiaLocation");
        //the same defaults as in OnCloudAgentConfigService; don't want to add getters to default
        int DEFAULT_WORKER_POOL = 5;
        int DEFAULT_SO_TIMEOUT = 60000;
        int DEFAULT_CONNECT_TIMEOUT = 30000;
        EasyMock.expect(GlobalSettings.getGaiaLocation()).andReturn(gaiaLocation).once();
        PowerMock.replay(GlobalSettings.class);

        agentConfigService.init();
        PowerMock.verifyAll();

        assertEquals(gaiaLocation, agentConfigService.getGaiaLocation());
        assertEquals(DEFAULT_WORKER_POOL, agentConfigService.getWorkerPool());
        assertEquals(DEFAULT_SO_TIMEOUT, agentConfigService.getSoTimeout());
        assertEquals(DEFAULT_CONNECT_TIMEOUT, agentConfigService.getConnectTimeout());
    }

    @Test(expected = IllegalStateException.class)
    public void testNoGaiaLocation() throws Exception {
        PowerMock.mockStaticNice(GlobalSettings.class);
        agentConfigService.init();


    }
}