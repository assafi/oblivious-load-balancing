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

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import config.Configuration;
import config.LogFactory;
import engine.EventGenerator;
import engine.Server;
import engine.Simulator;
import engine.StatisticsCollector;

/**
 * @author Assaf Israel
 * 
 */
public class Main {

	private static final int NUM_EXPECTED_ARGUMENTS = 2;

	private static final String USAGE = "<xml-config-file> <xml-output-file>";

	private static Logger log = LogFactory.getLog(Main.class);

	private static int steps = 1;

	private static Server[] servers = null;

	private static Configuration config = Configuration.getInstance();
	private static Simulator simulator;

	public static void main(String[] args) {

		log.info("Load balance simulator invoked");
		if (args.length != NUM_EXPECTED_ARGUMENTS) {
			usage("Wrong number of arguments");
			System.exit(1);
		}

		try {
			setup(args[0]);
			execute();
			collectStats(args[1]);
		} catch (IllegalArgumentException iae) {
			usage(iae.getMessage());
			System.exit(1);
		}

		log.info("Simulation concluded");
	}

	/**
	 * @param config
	 */
	private static void setup(String configFilePath) {

		log.info(step() + "Retrieving system configurations.");
		try {
			config.parseFile(configFilePath);
		} catch (Exception e) {
			usage(e.getMessage());
			System.exit(1);
		}

		if (config.getNumServers() < 2) {
			usage("Number of servers must exceed 1.");
			System.exit(1);
		}

		log.info(step() + "Configuring servers.");
		Server.SetServersConfiguration(config);
		
		servers = new Server[config.getNumServers()];
		for (int i = 0; i < config.getNumServers(); i++) {
			servers[i] = new Server();
		}
	}

	/**
	 * 
	 */
	private static void execute() {
		EventGenerator eGen = new EventGenerator(config);
		simulator = new Simulator(eGen, servers);

		log.info(step() + "Starting simulation.");
		simulator.execute();
		log.info(step() + "Simulation completed.");
	}

	/**
	 * 
	 */
	private static void collectStats(String outputFilePath) {

		StatisticsCollector sc = StatisticsCollector.getGlobalCollector();

		File xmlOutputFile = new File(outputFilePath);
		if (xmlOutputFile.exists()) {
			xmlOutputFile.delete();
		}
		try {
			xmlOutputFile.createNewFile();
		} catch (IOException e) {
			log.error("Error while trying to create output file: \""
					+ outputFilePath + "\".\n"
					+ "Simulation summary will be written to log instead.");
			// TODO: WRITE SUMMARY TO LOG HERE
		}
		sc.exportXML(xmlOutputFile);
		log.info("Results are available in XML form at: \""
				+ xmlOutputFile.getAbsolutePath() + "\"");
	}

	/**
	 * @return
	 */
	private static String step() {
		return "Step " + steps++ + ") ";
	}

	/**
	 * @param string
	 */
	private static void usage(String error) {
		log.fatal(error);
		System.err.println(USAGE);
	}
}
