/**
 * Oblivious Load Balance Simulator
 *
 * 236635 - On the Management and Efficiency of Cloud Based Services (W 2011)
 * CS Faculty, Technion - Institute of Technology 
 *
 * Authors: Assaf Israel, Eli Nazarov, Asi Bross 
 * 
 */
package engine;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import config.QueuePolicy;
import config.stubs.ConfigurationStub;

/**
 * @author Assaf Israel
 *
 */
public class EventGeneratorTestCase {

	private static final double EPSILON = 1e-8;
	private static ConfigurationStub config;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		config = new ConfigurationStub();
		config.numJobs = 1000;
		config.numServers = 20;
		config.distrbutionFactor = 0.3;
		config.load = 0.8;
		config.policy = QueuePolicy.FINITE;
	}

	@Test(expected=IllegalArgumentException.class)
	public void testJobCreationBadNumJobs() {
		config.numJobs = 0;
		new EventGenerator(config);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testJobCreationBadNumServers() {
		config.numServers = 1;
		new EventGenerator(config);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testJobCreationBadLoad() {
		config.load = 0.0;
		new EventGenerator(config);
	}
	
	@Test
	public void testJobCreation() {
		EventGenerator eGen = new EventGenerator(config);
		assertFalse(eGen.done());
		
		int counter = 1000;
		while (counter-- != 0) {
			assertFalse(eGen.done());
			Job aJob = eGen.nextJob();
			assertTrue(aJob.getJobLength() > EPSILON);
		}
		
		assertTrue(eGen.done());
		Job finalJob = eGen.nextJob();
		assertTrue(finalJob.getJobLength() < EPSILON);
	}
}
