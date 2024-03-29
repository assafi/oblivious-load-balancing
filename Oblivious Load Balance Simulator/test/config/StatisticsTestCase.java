/**
 * Oblivious Load Balance Simulator
 *
 * 236635 - On the Management and Efficiency of Cloud Based Services (W 2011)
 * CS Faculty, Technion - Institute of Technology 
 *
 * Authors: Assaf Israel, Eli Nazarov, Asi Bross 
 * 
 */
package config;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import engine.StatisticsCollector;

/**
 * @author Eli Nazarov
 *
 */
public class StatisticsTestCase {

	private static File outputXMLFile = new File("testStatFile.xml");;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		if (outputXMLFile.exists()) {
			outputXMLFile.delete();
		}
	}

	/**
	 * Test method for {@link engine.StatisticsCollector#exportXML(java.io.File)}.
	 * @throws IOException 
	 */
	@Test
	public void testExportXML() throws IOException {
		
		ConfigurationTestCase.createTempXML();
		StatisticsCollector tmp = new StatisticsCollector();
		
		ExperimentsConfiguration experimentsConfiguration = new ExperimentsConfiguration();
		experimentsConfiguration.parseFile(ConfigurationTestCase.xmlFile.getAbsolutePath());
		IConfiguration config = experimentsConfiguration.getAllExperimentsConfigurations().get(0);
		
		tmp.exportXML(outputXMLFile,config);
		ConfigurationTestCase.destroyTempXML();
		
	}

}
