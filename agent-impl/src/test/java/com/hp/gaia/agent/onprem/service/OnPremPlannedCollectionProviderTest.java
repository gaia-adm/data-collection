package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.service.CollectionState;
import com.hp.gaia.agent.service.CollectionState.State;
import com.hp.gaia.agent.service.CollectionStateService;
import com.hp.gaia.agent.service.PlannedCollection;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OnPremPlannedCollectionProviderTest extends EasyMockSupport {

    private static final long MINUTE_MS = 60000L;

    private OnPremPlannedCollectionProvider onPremPlannedCollectionProvider;

    private CollectionStateService collectionStateService;

    private OnPremProvidersConfigService onPremProvidersConfigService;

    @Before
    public void before() {
        collectionStateService = createMock(CollectionStateService.class);
        onPremProvidersConfigService = createMock(OnPremProvidersConfigService.class);
        onPremPlannedCollectionProvider = new OnPremPlannedCollectionProvider();
        onPremPlannedCollectionProvider.setCollectionStateService(collectionStateService);
        onPremPlannedCollectionProvider.setOnPremProvidersConfigService(onPremProvidersConfigService);
    }

    @Test
    public void findNextPlannedCollectionEmpty() {
        // expectations
        expect(onPremProvidersConfigService.getProviderConfigs()).andReturn(Collections.emptyList());
        // replay mocks
        replayAll();
        PlannedCollection plannedCollection = onPremPlannedCollectionProvider.findNextPlannedCollection();
        assertNull(plannedCollection);
        // verify mocks
        verifyAll();
    }

    @Test
    public void findNextPlannedCollectionOneNeverExecuted() {
        List<ProviderConfig> providerConfigs = new ArrayList<>();
        final ProviderConfig testConfig = new ProviderConfig("testConfigId", "testProviderId", null, null, null, 60);
        providerConfigs.add(testConfig);
        // expectations
        expect(onPremProvidersConfigService.getProviderConfigs()).andReturn(providerConfigs);
        expect(collectionStateService.getCollectionState("testConfigId")).andReturn(null);
        // replay mocks
        replayAll();
        PlannedCollection plannedCollection = onPremPlannedCollectionProvider.findNextPlannedCollection();
        assertNotNull(plannedCollection);
        final CollectionState collectionState = plannedCollection.getCollectionState();
        assertNotNull(collectionState);
        assertEquals("testConfigId", collectionState.getProviderConfigId());
        ProviderConfig providerConfig = plannedCollection.getProviderConfig();
        assertEquals(testConfig, providerConfig);
        // verify mocks
        verifyAll();
    }

    @Test
    public void findNextPlannedCollectionOnePending() {
        List<ProviderConfig> providerConfigs = new ArrayList<>();
        final ProviderConfig testConfig = new ProviderConfig("testConfigId", "testProviderId", null, null, null, 60);
        providerConfigs.add(testConfig);
        CollectionState testState = new CollectionState("testConfigId");
        testState.setState(State.PENDING);
        // expectations
        expect(onPremProvidersConfigService.getProviderConfigs()).andReturn(providerConfigs);
        expect(collectionStateService.getCollectionState("testConfigId")).andReturn(testState);
        // replay mocks
        replayAll();
        PlannedCollection plannedCollection = onPremPlannedCollectionProvider.findNextPlannedCollection();
        assertNull(plannedCollection);
        // verify mocks
        verifyAll();
    }

    @Test
    public void findNextPlannedCollectionOneRunning() {
        List<ProviderConfig> providerConfigs = new ArrayList<>();
        final ProviderConfig testConfig = new ProviderConfig("testConfigId", "testProviderId", null, null, null, 60);
        providerConfigs.add(testConfig);
        CollectionState testState = new CollectionState("testConfigId");
        testState.setState(State.RUNNING);
        // expectations
        expect(onPremProvidersConfigService.getProviderConfigs()).andReturn(providerConfigs);
        expect(collectionStateService.getCollectionState("testConfigId")).andReturn(testState);
        // replay mocks
        replayAll();
        PlannedCollection plannedCollection = onPremPlannedCollectionProvider.findNextPlannedCollection();
        assertNull(plannedCollection);
        // verify mocks
        verifyAll();
    }

    @Test
    public void findNextPlannedCollectionOneNextNow() {
        List<ProviderConfig> providerConfigs = new ArrayList<>();
        final ProviderConfig testConfig = new ProviderConfig("testConfigId", "testProviderId", null, null, null, 60);
        providerConfigs.add(testConfig);
        CollectionState testState = new CollectionState("testConfigId");
        testState.setState(State.FINISHED);
        testState.setNextCollectionTimestamp(System.currentTimeMillis() - MINUTE_MS);
        testState.setLastCollectionTimestamp(System.currentTimeMillis() - 61L * MINUTE_MS);
        // expectations
        expect(onPremProvidersConfigService.getProviderConfigs()).andReturn(providerConfigs);
        expect(collectionStateService.getCollectionState("testConfigId")).andReturn(testState);
        // replay mocks
        replayAll();
        PlannedCollection plannedCollection = onPremPlannedCollectionProvider.findNextPlannedCollection();
        assertNotNull(plannedCollection);
        final CollectionState collectionState = plannedCollection.getCollectionState();
        assertNotNull(collectionState);
        assertEquals("testConfigId", collectionState.getProviderConfigId());
        assertEquals(State.FINISHED, collectionState.getState());
        ProviderConfig providerConfig = plannedCollection.getProviderConfig();
        assertEquals(testConfig, providerConfig);
        // verify mocks
        verifyAll();
    }

    @Test
    public void findNextPlannedCollectionOneNextSoon() {
        List<ProviderConfig> providerConfigs = new ArrayList<>();
        final ProviderConfig testConfig = new ProviderConfig("testConfigId", "testProviderId", null, null, null, 60);
        providerConfigs.add(testConfig);
        CollectionState testState = new CollectionState("testConfigId");
        testState.setState(State.FINISHED);
        testState.setNextCollectionTimestamp(System.currentTimeMillis() + MINUTE_MS);
        testState.setLastCollectionTimestamp(System.currentTimeMillis() - 59L * MINUTE_MS);
        // expectations
        expect(onPremProvidersConfigService.getProviderConfigs()).andReturn(providerConfigs);
        expect(collectionStateService.getCollectionState("testConfigId")).andReturn(testState);
        // replay mocks
        replayAll();
        PlannedCollection plannedCollection = onPremPlannedCollectionProvider.findNextPlannedCollection();
        assertNull(plannedCollection);
        // verify mocks
        verifyAll();
    }

    @Test
    public void findNextPlannedCollectionTwoFirstNowSecondNeverExecuted() {
        List<ProviderConfig> providerConfigs = new ArrayList<>();
        final ProviderConfig testConfig1 = new ProviderConfig("testConfigId1", "testProviderId1", null, null, null, 60);
        providerConfigs.add(testConfig1);
        final ProviderConfig testConfig2 = new ProviderConfig("testConfigId2", "testProviderId2", null, null, null, 60);
        providerConfigs.add(testConfig2);
        CollectionState testState = new CollectionState("testConfigId1");
        testState.setState(State.FINISHED);
        testState.setNextCollectionTimestamp(System.currentTimeMillis() - MINUTE_MS);
        testState.setLastCollectionTimestamp(System.currentTimeMillis() - 61L * MINUTE_MS);
        // expectations
        expect(onPremProvidersConfigService.getProviderConfigs()).andReturn(providerConfigs);
        expect(collectionStateService.getCollectionState("testConfigId1")).andReturn(testState);
        expect(collectionStateService.getCollectionState("testConfigId2")).andReturn(null);
        // replay mocks
        replayAll();
        PlannedCollection plannedCollection = onPremPlannedCollectionProvider.findNextPlannedCollection();
        assertNotNull(plannedCollection);
        final CollectionState collectionState = plannedCollection.getCollectionState();
        assertNotNull(collectionState);
        assertEquals("testConfigId2", collectionState.getProviderConfigId());
        ProviderConfig providerConfig = plannedCollection.getProviderConfig();
        assertEquals(testConfig2, providerConfig);
        // verify mocks
        verifyAll();
    }

    @Test
    public void findNextPlannedCollectionTwoNow1() {
        List<ProviderConfig> providerConfigs = new ArrayList<>();
        final ProviderConfig testConfig1 = new ProviderConfig("testConfigId1", "testProviderId1", null, null, null, 60);
        providerConfigs.add(testConfig1);
        final ProviderConfig testConfig2 = new ProviderConfig("testConfigId2", "testProviderId2", null, null, null, 60);
        providerConfigs.add(testConfig2);
        CollectionState testState1 = new CollectionState("testConfigId1");
        testState1.setState(State.FINISHED);
        testState1.setNextCollectionTimestamp(System.currentTimeMillis() - MINUTE_MS);
        testState1.setLastCollectionTimestamp(System.currentTimeMillis() - 61L * MINUTE_MS);
        CollectionState testState2 = new CollectionState("testConfigId2");
        testState2.setState(State.FINISHED);
        testState2.setNextCollectionTimestamp(System.currentTimeMillis() - 2 * MINUTE_MS);
        testState2.setLastCollectionTimestamp(System.currentTimeMillis() - 61L * MINUTE_MS);
        // expectations
        expect(onPremProvidersConfigService.getProviderConfigs()).andReturn(providerConfigs);
        expect(collectionStateService.getCollectionState("testConfigId1")).andReturn(testState1);
        expect(collectionStateService.getCollectionState("testConfigId2")).andReturn(testState2);
        // replay mocks
        replayAll();
        PlannedCollection plannedCollection = onPremPlannedCollectionProvider.findNextPlannedCollection();
        assertNotNull(plannedCollection);
        final CollectionState collectionState = plannedCollection.getCollectionState();
        assertNotNull(collectionState);
        assertEquals("testConfigId2", collectionState.getProviderConfigId());
        ProviderConfig providerConfig = plannedCollection.getProviderConfig();
        assertEquals(testConfig2, providerConfig);
        // verify mocks
        verifyAll();
    }

    @Test
    public void findNextPlannedCollectionTwoNow2() {
        List<ProviderConfig> providerConfigs = new ArrayList<>();
        final ProviderConfig testConfig1 = new ProviderConfig("testConfigId1", "testProviderId1", null, null, null, 60);
        providerConfigs.add(testConfig1);
        final ProviderConfig testConfig2 = new ProviderConfig("testConfigId2", "testProviderId2", null, null, null, 60);
        providerConfigs.add(testConfig2);
        CollectionState testState1 = new CollectionState("testConfigId1");
        testState1.setState(State.FINISHED);
        testState1.setNextCollectionTimestamp(System.currentTimeMillis() - 2 * MINUTE_MS);
        testState1.setLastCollectionTimestamp(System.currentTimeMillis() - 61L * MINUTE_MS);
        CollectionState testState2 = new CollectionState("testConfigId2");
        testState2.setState(State.FINISHED);
        testState2.setNextCollectionTimestamp(System.currentTimeMillis() - MINUTE_MS);
        testState2.setLastCollectionTimestamp(System.currentTimeMillis() - 61L * MINUTE_MS);
        // expectations
        expect(onPremProvidersConfigService.getProviderConfigs()).andReturn(providerConfigs);
        expect(collectionStateService.getCollectionState("testConfigId1")).andReturn(testState1);
        expect(collectionStateService.getCollectionState("testConfigId2")).andReturn(testState2);
        // replay mocks
        replayAll();
        PlannedCollection plannedCollection = onPremPlannedCollectionProvider.findNextPlannedCollection();
        assertNotNull(plannedCollection);
        final CollectionState collectionState = plannedCollection.getCollectionState();
        assertNotNull(collectionState);
        assertEquals("testConfigId1", collectionState.getProviderConfigId());
        ProviderConfig providerConfig = plannedCollection.getProviderConfig();
        assertEquals(testConfig1, providerConfig);
        // verify mocks
        verifyAll();
    }
}
