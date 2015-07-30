package com.hp.gaia.agent;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Base class for agent integration tests.
 */
@ContextConfiguration({"classpath*:/Spring/gaia-*shared-context.xml", "classpath*:/Spring/gaia-*test-context.xml"})
public abstract class AgentIntegrationTest extends AbstractJUnit4SpringContextTests {

}
