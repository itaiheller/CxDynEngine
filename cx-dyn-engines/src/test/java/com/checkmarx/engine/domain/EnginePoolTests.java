/**
 * 
 */
package com.checkmarx.engine.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import com.checkmarx.engine.domain.DynamicEngine.State;
import com.checkmarx.engine.domain.EnginePool.EnginePoolEntry;
import com.google.common.collect.Iterables;

/**
 * @author rjgey
 *
 */
@RunWith(SpringRunner.class)
public class EnginePoolTests {

	private static final Logger log = LoggerFactory.getLogger(EnginePoolTests.class);

	private EnginePool pool;
	
	public static final EngineSize SMALL = new EngineSize("S", 0, 99999);
	public static final EngineSize MEDIUM = new EngineSize("M", 100000, 499999);
	public static final EngineSize LARGE = new EngineSize("L", 500000, 999999999);
	
	@Before
	public void setUp() throws Exception {
		log.trace("setup()");

		pool = new DefaultEnginePoolBuilder("cx-engine", 300)
			.addEntry(new EnginePoolEntry(SMALL, 3))
			.addEntry(new EnginePoolEntry(MEDIUM, 3))
			.addEntry(new EnginePoolEntry(LARGE, 3))
			.build();
	}

	@Test
	public void testInit() {
		log.trace("testInit()");
		
		pool.logEngines();
		log.debug("{}", pool);

		assertEquals(9, pool.getAllEnginesByName().size());
		
		assertEquals(3, pool.getAllEnginesBySize().size());
		assertEquals(3, pool.getAllEnginesBySize().get(SMALL.getName()).size());
		assertEquals(3, pool.getAllEnginesBySize().get(MEDIUM.getName()).size());
		assertEquals(3, pool.getAllEnginesBySize().get(LARGE.getName()).size());

		assertEquals(3, pool.getUnprovisionedEngines().size());
		assertEquals(3, pool.getUnprovisionedEngines().get(SMALL.getName()).size());
		assertEquals(3, pool.getUnprovisionedEngines().get(MEDIUM.getName()).size());
		assertEquals(3, pool.getUnprovisionedEngines().get(LARGE.getName()).size());
		
		assertEquals(3, pool.getActiveEngines().size());
		assertEquals(0, pool.getActiveEngines().get(SMALL.getName()).size());
		assertEquals(0, pool.getActiveEngines().get(MEDIUM.getName()).size());
		assertEquals(0, pool.getActiveEngines().get(LARGE.getName()).size());

		assertEquals(3, pool.getExpiringEngines().size());
		assertEquals(0, pool.getExpiringEngines().get(SMALL.getName()).size());
		assertEquals(0, pool.getExpiringEngines().get(MEDIUM.getName()).size());
		assertEquals(0, pool.getExpiringEngines().get(LARGE.getName()).size());

		assertEquals(3, pool.getIdleEngines().size());
		assertEquals(0, pool.getIdleEngines().get(SMALL.getName()).size());
		assertEquals(0, pool.getIdleEngines().get(MEDIUM.getName()).size());
		assertEquals(0, pool.getIdleEngines().get(LARGE.getName()).size());
	}
	
	@Test
	public void testAllocate() {
		log.trace("testAllocate()");
		
		DynamicEngine engine;

		engine = pool.allocateEngine(SMALL, State.IDLE);
		assertThat(engine, is(nullValue()));
		engine = pool.allocateEngine(SMALL, State.EXPIRING);
		assertThat(engine, is(nullValue()));
		engine = pool.allocateEngine(SMALL, State.SCANNING);
		assertThat(engine, is(nullValue()));

		engine = pool.allocateEngine(SMALL, State.UNPROVISIONED);
		assertThat(engine, is(notNullValue()));
		pool.changeState(engine, State.IDLE);
		engine = pool.allocateEngine(SMALL, State.UNPROVISIONED);
		assertThat(engine, is(notNullValue()));
		pool.changeState(engine, State.IDLE);
		engine = pool.allocateEngine(SMALL, State.UNPROVISIONED);
		assertThat(engine, is(notNullValue()));
		pool.changeState(engine, State.IDLE);
		engine = pool.allocateEngine(SMALL, State.UNPROVISIONED);
		assertThat(engine, is(nullValue()));
	}
	
	
	@Test
	public void testChangeState() throws InterruptedException {
		log.trace("testChangeState()");
		
		final DynamicEngine engine = Iterables.getFirst(pool.getUnprovisionedEngines().get(SMALL.getName()), null);
		String size = engine.getSize();
		
		pool.changeState(engine, State.SCANNING);
		assertEquals(1, pool.getActiveEngines().get(size).size());
		assertEquals(2, pool.getUnprovisionedEngines().get(size).size());
		assertEquals(State.SCANNING, engine.getState());
		
		Thread.sleep(100);
		assertTrue(engine.getElapsedTime().getMillis() >= 100);

		pool.logEngines();
	}

	@Test
	public void testReplaceEngine() {
		log.trace("testReplaceEngine()");
		
		final DynamicEngine e = 
				Iterables.getFirst(pool.getUnprovisionedEngines().get(SMALL.getName()), null);
		final String name = e.getName();
		
		final DynamicEngine newEngine = new DynamicEngine(name, e.getSize(), 300);
		newEngine.setState(State.IDLE);
		newEngine.setHost(new Host(name, "ip", "url", DateTime.now()));
		assertEquals(newEngine, e);
		
		final DynamicEngine oldEngine = pool.replaceEngine(newEngine);
		log.debug("old: {}", oldEngine);
		log.debug("new: {}", newEngine);

		assertEquals(e, oldEngine);
		assertEquals(newEngine, pool.getEngineByName(name));
		assertNotEquals(newEngine.getState(), oldEngine.getState());
		assertNotEquals(newEngine.getHost(), oldEngine.getHost());
	}

	@Test
	public void testCalcSize() {
		log.trace("testCalcSize()");
		
		assertEquals(SMALL, pool.calcEngineSize(0));
		assertEquals(SMALL, pool.calcEngineSize(1));
		assertEquals(SMALL, pool.calcEngineSize(99999));
		assertEquals(MEDIUM, pool.calcEngineSize(100000));
		assertEquals(MEDIUM, pool.calcEngineSize(499999));
		assertEquals(LARGE, pool.calcEngineSize(500000));
		assertEquals(LARGE, pool.calcEngineSize(999999999));
		assertNull(pool.calcEngineSize(100000000000L));
	}
	
}
