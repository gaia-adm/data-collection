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


    @Before
    public void setup() throws Exception {
        PowerMock.mockStaticNice(GlobalSettings.class);
    }

    @Test
    public void testAllGoodSettingsProvided() throws Exception {
        String workerPool = "20";
        String soTimeout = "10";
        String conTimeout = "25";
        EasyMock.expect(GlobalSettings.getGaiaLocation()).andReturn(gaiaLocation).times(2);
        EasyMock.expect(GlobalSettings.getWorkerPool()).andReturn(workerPool).times(2);
        EasyMock.expect(GlobalSettings.getSoTimeout()).andReturn(soTimeout).times(2);
        EasyMock.expect(GlobalSettings.getConnectTimeout()).andReturn(conTimeout).times(2);
        PowerMock.replay(GlobalSettings.class);

        agentConfigService.init();
        PowerMock.verifyAll();

        assertEquals(gaiaLocation, agentConfigService.getGaiaLocation());
        assertEquals(Integer.parseInt(workerPool), agentConfigService.getWorkerPool());
        assertEquals(Integer.parseInt(soTimeout), agentConfigService.getSoTimeout());
        assertEquals(Integer.parseInt(conTimeout), agentConfigService.getConnectTimeout());
    }

    @Test
    public void testUseDefaultsWherePossible() throws Exception {
        //the same defaults as in OnCloudAgentConfigService; don't want to add getters to default
        int DEFAULT_WORKER_POOL = 5;
        int DEFAULT_SO_TIMEOUT = 60000;
        int DEFAULT_CONNECT_TIMEOUT = 30000;
        EasyMock.expect(GlobalSettings.getGaiaLocation()).andReturn(gaiaLocation).times(2);
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
        agentConfigService.init();


    }
}