package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.AgentIntegrationTest;
import com.hp.gaia.agent.service.CollectionState;
import com.hp.gaia.agent.service.CollectionState.Result;
import com.hp.gaia.agent.service.CollectionState.State;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@DirtiesContext
public class OnPremCollectionStateServiceTest extends AgentIntegrationTest {

    @Autowired
    private OnPremCollectionStateService onPremCollectionStateService;

    @Autowired
    private OnPremProvidersConfigService onPremProvidersConfigService;

    @Test
    public void testReadCollectionStates() throws URISyntaxException {
        onPremProvidersConfigService.init(getConfigFile("provider_configs/collection_state_providers.json"));
        File stateFile = getConfigFile("states/config1-state.json");
        onPremCollectionStateService.init(stateFile.getParentFile());

        try {
            onPremCollectionStateService.getCollectionState("invalidId");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        // config1
        CollectionState collectionState1 = onPremCollectionStateService.getCollectionState("config1");
        assertNotNull(collectionState1);
        assertEquals("config1", collectionState1.getProviderConfigId());
        assertEquals((Long)1438088002703L, collectionState1.getLastCollectionTimestamp());
        assertEquals((Long)1438091605500L, collectionState1.getNextCollectionTimestamp());
        assertEquals("someBookmark", collectionState1.getBookmark());
        assertEquals(State.FINISHED, collectionState1.getState());
        assertEquals(Result.SUCCESS, collectionState1.getResult());

        // config2
        CollectionState collectionState2 = onPremCollectionStateService.getCollectionState("config2");
        assertNotNull(collectionState2);
        assertEquals("config2", collectionState2.getProviderConfigId());
        assertNull(collectionState2.getLastCollectionTimestamp());
        assertNull(collectionState2.getNextCollectionTimestamp());
        assertNull(collectionState2.getBookmark());
        // config2-state.json file has RUNNING state, but this state gets changed the moment agent starts
        assertEquals(State.FINISHED, collectionState2.getState());
        assertEquals(Result.FAILURE, collectionState2.getResult());
    }

    @Test
    public void testSaveCollectionState() throws URISyntaxException, IOException {
        onPremProvidersConfigService.init(getConfigFile("provider_configs/collection_state_providers.json"));
        Path tempDir = Files.createTempDirectory("OnPremCollectionStateServiceTest");
        onPremCollectionStateService.init(tempDir.toFile());

        try {
            onPremCollectionStateService.saveCollectionState(new CollectionState("invalidId"));
            fail("Expected exception");
        } catch (Exception e) {
            // ok
        }

        CollectionState collectionState1 = new CollectionState("config1");
        collectionState1.setResult(Result.SUCCESS);
        collectionState1.setState(State.FINISHED);
        collectionState1.setBookmark("myBookmark");
        collectionState1.setNextCollectionTimestamp(1438088002703L);
        collectionState1.setLastCollectionTimestamp(1438091605500L);
        onPremCollectionStateService.saveCollectionState(collectionState1);
        CollectionState result1 = onPremCollectionStateService.getCollectionState("config1");
        assertEquals(collectionState1, result1);

        // now try to reinit the service to verify the file really contains the value
        onPremCollectionStateService.init(tempDir.toFile());
        CollectionState result2 = onPremCollectionStateService.getCollectionState("config1");
        assertEquals(collectionState1, result2);
    }

    private static File getConfigFile(String name) throws URISyntaxException {
        URL agentJson = OnPremCollectionStateServiceTest.class.getClassLoader().getResource(name);
        assertNotNull(name + " was not found", agentJson);
        return new File(agentJson.toURI());
    }
}
