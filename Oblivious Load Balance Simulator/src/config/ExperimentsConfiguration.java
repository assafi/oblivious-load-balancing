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
import java.util.LinkedList;
import java.util.List;
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
public class ExperimentsConfiguration extends DefaultHandler {

	private static Logger log = LogFactory.getLog(ExperimentsConfiguration.class);
	
	private List<IConfiguration> configurations;
	
	private Configuration currentConfiguration = null;
	
	Stack<String> xmlTags = new Stack<String>();

	private String tempVal = "";
	
	public ExperimentsConfiguration()
	{
		configurations = new LinkedList<IConfiguration>();
	}
	
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
		for (IConfiguration config : configurations) {
			log.debug("Experiment Configuration:");
			log.debug("No. Servers: " + config.getNumServers());
			log.debug("Load: " + config.getLoad());
			log.debug("Queue policy: " + config.getPolicy().name());
			if (config.getPolicy().equals(QueuePolicy.FINITE)) {
				log.debug("Memory size: " + config.getMemorySize());
				log.debug("Distribution factor: " + config.getDistributionFactor());
			}
			log.debug("No. Jobs: " + config.getNumJobs());
			log.debug("Jobs mean length: " + config.getJobMeanLength());
			log.debug("Statistical margin: " + config.getStatisticalMargin());
			log.debug("");
		}

	}
	
	
	public List<IConfiguration> getAllExperimentsConfigurations()
	{
		return configurations;
	}
	
	public void startElement(String uri, String localName, String qName, 
			Attributes attributes) throws SAXException {
		
		if(qName.equals("experiment")) {
			if(currentConfiguration != null) {
				throw new SAXException("Missing closing experiment tag");
			}
			currentConfiguration = new Configuration();
		}
		else if ("queue-policy".equals(qName)) {
			String typeStr = attributes.getValue("policy");
			if (null == typeStr) {
				throw new SAXException("Missing policy attribute in queue-policy");
			}
			try {
				currentConfiguration.setPolicy(QueuePolicy.process(typeStr));
			} catch (Exception e) {
				throw new SAXException(e);
			}
			if (currentConfiguration.getPolicy().equals(QueuePolicy.FINITE)) {
				String dFactorStr = attributes.getValue("dFactor");
				if (null == dFactorStr) {
					throw new SAXException("Missing dFactor attribute in queue-policy");
				}
				currentConfiguration.setdFactor(Double.valueOf(dFactorStr));
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
		
		if(qName.equals("experiment")) {
			if(currentConfiguration == null) {
				throw new SAXException("Missing starting experiment tag");
			}
			configurations.add(currentConfiguration);
			currentConfiguration = null;
		}
		else if ("count".equals(qName)) {
			if (xmlTags.contains("servers-config")) {
				currentConfiguration.setNumServers(Integer.valueOf(tempVal));
			} else if (xmlTags.contains("jobs-config")) {
				currentConfiguration.setNumJobs(Long.valueOf(tempVal));
			}
		} else if ("queue-policy".equals(qName)) {
			if (currentConfiguration.getPolicy().equals(QueuePolicy.FINITE)) {
				currentConfiguration.setMemorySize(Integer.valueOf(tempVal));
			}
		} else if ("load".equals(qName)) {
			currentConfiguration.setLoad(Double.valueOf(tempVal));
		} else if ("meanLength".equals(qName)) {
			currentConfiguration.setJobMeanLength(Double.valueOf(tempVal));
		} else if ("statisticsMargin".equals(qName)) {
			currentConfiguration.setStatisticalMargin(Double.valueOf(tempVal));
		}
		xmlTags.pop();
	}
}
