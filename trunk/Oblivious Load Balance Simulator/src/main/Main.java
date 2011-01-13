/**
 * Oblivious Load Balance Simulator
 *
 * 236635 - On the Management and Efficiency of Cloud Based Services (W 2011)
 * CS Faculty, Technion - Institute of Technology 
 *
 * Authors: Assaf Israel, Eli Nazarov, Asi Bross 
 * 
 */
package main;

import org.apache.log4j.Logger;

import config.Configuration;
import config.LogFactory;

/**
 * @author Assaf Israel
 *
 */
public class Main {

	private static final int NUM_EXPECTED_ARGUMENTS = 1;
	
	private static final String USAGE = "<xml-config-file>";
	
	private static Logger log = LogFactory.getLog(Main.class);
	
	public static void main(String[] args) {
		
		log.info("Load balance simulator invoked");
		if (args.length != NUM_EXPECTED_ARGUMENTS) {
			usage("Wrong number of arguments");
			System.exit(1);
		}
		
		Configuration config = Configuration.getInstance();
		try {
			config.parseFile(args[0]);
		} catch (Exception e) {
			usage(e.getMessage());
			System.exit(1);
		}

		log.debug("XML file: " + args[0] + " parsed succefully");
		log.info("Done");
	}

	/**
	 * @param string
	 */
	private static void usage(String error) {
		log.error(error);
		System.err.println(USAGE);
	}
}
