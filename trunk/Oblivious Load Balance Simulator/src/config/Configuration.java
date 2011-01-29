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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Assaf Israel
 *
 */
public class Configuration extends DefaultHandler implements IConfiguration {

	private static Configuration instance = null; 
	
	private int numServers = 0;
	private QueuePolicy policy = null;
	private double load = 0.0;
	private int memorySize = 0;
	private double dFactor = 0.0;
	private long numJobs = 0;
	
	Stack<String> xmlTags = new Stack<String>();

	private static String tempVal = "";
	
	private static Logger log = LogFactory.getLog(Configuration.class);
	
	private Configuration() {}
	
	/**
	 * @param xmlFilePath - The path for the XML configuration file 
	 * @throws IOException
	 */
	public void parseFile(String xmlFilePath) throws IOException {
		File xmlFile = new File(xmlFilePath);
		if (!xmlFile.exists()) {
			throw new FileNotFoundException("File " + xmlFilePath + " not found");
		}
		
		if (!xmlFile.canRead()) {
			throw new IOException("Can't read file " + xmlFile.getName());
		}
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser parser = spf.newSAXParser();
			parser.parse(xmlFile, this);
		} catch (SAXException se) {
			throw new RuntimeException(se);
		} catch (ParserConfigurationException pce) {
			throw new RuntimeException(pce);
		}	
		
		log.debug("XML File " + xmlFile.getName() + " parsed.");
		log.debug("No. Servers: " + numServers);
		log.debug("Load: " + load);
		log.debug("Queue policy: " + policy.name());
		if (policy.equals(QueuePolicy.FINITE)) {
			log.debug("Memory size: " + memorySize);
			log.debug("Distribution factor: " + dFactor);
		}
		log.debug("No. Jobs: " + numJobs);
	}

	public static Configuration getInstance() {
		if (null != instance) {
			return instance;
		}
		synchronized (Configuration.class) {
			instance = new Configuration();
		}
		return instance;
	}
	
	public void startElement(String uri, String localName, String qName, 
			Attributes attributes) throws SAXException {
		
		if ("queue-policy".equals(qName)) {
			String typeStr = attributes.getValue("policy");
			if (null == typeStr) {
				throw new SAXException("Missing policy attribute in queue-policy");
			}
			try {
				policy = QueuePolicy.process(typeStr);
			} catch (Exception e) {
				throw new SAXException(e);
			}
			if (policy.equals(QueuePolicy.FINITE)) {
				String dFactorStr = attributes.getValue("dFactor");
				if (null == dFactorStr) {
					throw new SAXException("Missing dFactor attribute in queue-policy");
				}
				dFactor = Double.valueOf(dFactorStr);
			}
		}
		
		tempVal = "";
		xmlTags.push(qName);
	}
	
	public void characters (char[] ch, int start, int length) 
		throws SAXException {
		
		tempVal  += new String(ch,start,length).replaceAll("[\n\t ]*", "");
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ("count".equals(qName)) {
			if (xmlTags.contains("servers-config")) {
				numServers = Integer.valueOf(tempVal);
			} else if (xmlTags.contains("jobs-config")) {
				numJobs = Long.valueOf(tempVal);
			}
		} else if ("queue-policy".equals(qName)) {
			if (policy.equals(QueuePolicy.FINITE)) {
				memorySize = Integer.valueOf(tempVal);
			}
		} else if ("load".equals(qName)) {
			load = Double.valueOf(tempVal);
		}
		xmlTags.pop();
	}

	public int getNumServers() {
		return numServers;
	}

	public QueuePolicy getPolicy() {
		return policy;
	}

	public double getLoad() {
		return load;
	}
	
	public int getMemorySize() {
		return memorySize;
	}
	
	public double getDistributionFactor() {
		return dFactor;
	}

	public long getNumJobs() {
		return numJobs;
	}
}
