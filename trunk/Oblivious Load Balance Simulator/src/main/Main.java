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
import engine.Server;

/**
 * @author Assaf Israel
 *
 */
public class Main {

	private static final int NUM_EXPECTED_ARGUMENTS = 1;
	
	private static final String USAGE = "<xml-config-file>";
	
	private static Logger log = LogFactory.getLog(Main.class);
	
	private static int steps = 1;
	
	private static Server[] servers = null;
	
	public static void main(String[] args) {
		
		log.info("Load balance simulator invoked");
		if (args.length != NUM_EXPECTED_ARGUMENTS) {
			usage("Wrong number of arguments");
			System.exit(1);
		}
		
		setup(args[0]);
		execute();
		collectStats();
		
		log.info("Simulation concluded");
	}

	/**
	 * @param config
	 */
	private static void setup(String configFilePath) {

		log.info(step() + "Retrieving system configurations.");
		Configuration config = Configuration.getInstance();
		try {
			config.parseFile(configFilePath);
		} catch (Exception e) {
			usage(e.getMessage());
			System.exit(1);
		}
		
		log.info(step() + "Configuring servers");
		Server.SetServersConfiguration(config);
		for (int i = 0; i < config.getNumServers(); i++) {
			servers[i] = new Server();
		}
	}

	/**
	 * 
	 */
	private static void execute() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 */
	private static void collectStats() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @return
	 */
	private static String step() {
		return "Step " + steps++ + ". ";
	}

	/**
	 * @param string
	 */
	private static void usage(String error) {
		log.error(error);
		System.err.println(USAGE);
	}
}
