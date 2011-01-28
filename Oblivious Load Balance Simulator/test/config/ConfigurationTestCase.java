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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Assaf Israel
 *
 */
public class ConfigurationTestCase {

	private final static String XML_FILE_NAME = "tempXML.xml"; 
	private final static String XML_CONTENT = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<configuration>\n" +
		"	<servers-config>\n" +
		"		<count>1</count>\n" +
		"		<queue-policy policy=\"finite\" dFactor=\"0.5\">10</queue-policy>\n" +
		"	</servers-config>\n" +
		"	<jobs-config>\n" +
		"		<load>0.2</load>\n" +
		"		<count>1000</count>\n" +	
		"	</jobs-config>\n" +
		"</configuration>";
	private static File xmlFile = null;
	
	@BeforeClass
	public static void createTempXML() throws IOException {
		xmlFile = new File(XML_FILE_NAME);
		if (xmlFile.exists()) {
			xmlFile.delete();
		}
		xmlFile.createNewFile();
		
		BufferedWriter out = new BufferedWriter(new FileWriter(xmlFile));
		
		out.write(XML_CONTENT);
		out.close();
	}
	
	@AfterClass
	public static void destroyTempXML() {
		if (xmlFile.exists()) {
			xmlFile.delete();
		}
	}
	
	@Test
	public void testGoodPath() throws IOException {
		
		Configuration config = Configuration.getInstance();
		config.parseFile(xmlFile.getAbsolutePath());
		
		assertEquals(1,config.getNumServers());
		assertEquals(QueuePolicy.FINITE,config.getPolicy());
		assertEquals(10,config.getMemorySize());
		assertTrue((config.getLoad() - 0.2) == 0.0);
		assertTrue((config.getDistributionFactor() - 0.5) == 0.0);
		assertEquals(1000,config.getNumJobs());
	}
}
