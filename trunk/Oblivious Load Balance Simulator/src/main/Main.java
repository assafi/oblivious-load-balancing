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

import misc.CsvLabel;
import misc.CsvWriter;

import org.apache.log4j.Logger;

import config.ExperimentsConfiguration;
import config.IConfiguration;
import config.LogFactory;
import engine.EventGenerator;
import engine.JobState;
import engine.Server;
import engine.Simulator;
import engine.StatisticsCollector;

/**
 * @author Assaf Israel
 * 
 */
public class Main {

	private static boolean csvFileIntitialised = false;
	
	private static final int NUM_EXPECTED_ARGUMENTS = 2;

	private static final String USAGE = "<xml-config-file> <xml-output-file>";

	private static String[] CSV_LABELS = null;

	private static Logger log = LogFactory.getLog(Main.class);

	private static int steps = 1;
	private static int substeps = 1;

	private static Server[] servers = null;

	private static ExperimentsConfiguration experimentsConfiguration;
	
	private static Simulator simulator;
	
	private static StatisticsCollector sc = StatisticsCollector.getGlobalCollector();
	
	private static File outputFile = null;

	public static void main(String[] args) {

		log.info(step() + "Load balance simulator invoked.");
		if (args.length != NUM_EXPECTED_ARGUMENTS) {
			usage("Wrong number of arguments");
			System.exit(1);
		}

		experimentsConfiguration = new ExperimentsConfiguration();
		
		try {
			log.info(step() + "Retrieving system configurations.");
			experimentsConfiguration.parseFile(args[0]);
			for (IConfiguration config : experimentsConfiguration.getAllExperimentsConfigurations()) {
				log.info(step() + "Executing experiment #" + StatisticsCollector.getCurrentExperimentIndex());
				setup(config);
				execute(config);
				collectStats(config, args[1]);
			}
			clean();
			log.info(step() + "Simulation concluded. See results at: \"" 
					+ outputFile.getCanonicalPath() + "\" for details.");
		} catch (Exception e) {
			usage(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

	}

	/**
	 * @throws IOException 
	 * 
	 */
	private static void clean() throws IOException {
		StatisticsCollector.close();
	}

	/**
	 * @param config
	 */
	private static void setup(IConfiguration config) {

		if (config.getNumServers() < 2) {
			usage("Number of servers must exceed 1.");
			System.exit(1);
		}

		log.info(substep() + "Configuring servers.");
		Server.SetServersConfiguration(config);
		
		servers = new Server[config.getNumServers()];
		for (int i = 0; i < config.getNumServers(); i++) {
			servers[i] = new Server();
		}
	}

	private static void execute(IConfiguration config) {
		EventGenerator eGen = new EventGenerator(config);
		simulator = new Simulator(eGen, servers);

		log.info(substep() + "Starting simulation.");
		simulator.execute();
		log.info(substep() + "Simulation completed.");
	}

	private static void collectStats(IConfiguration config, String outputFilePath) {

		log.info(substep() + "Writing results.");
		sc.finalizeStats();
		try {
			if (!csvFileIntitialised) {
				initializeCSV(outputFilePath);
				csvFileIntitialised = true;
			}
			sc.exportCSV(outputFile, config);
		} catch (IOException e) {
			log.error("Error while trying to create/write output file: \""
					+ outputFilePath + "\".\n"
					+ "Simulation summary will be written to log instead.");
			// TODO: WRITE SUMMARY TO LOG HERE
		}
	}

	private static void initializeCSV(String outputFilePath) throws IOException {
		outputFile = new File(outputFilePath);
		if (outputFile.exists()) {
			outputFile.delete();
		}
		CsvWriter writer = new CsvWriter(outputFile, null);
		prepareCsvLabels();
		writer.writeLabels(CSV_LABELS);
		StatisticsCollector.setWriter(writer);
	}

	/**
	 * 
	 */
	private static void prepareCsvLabels() {

		CSV_LABELS = new String[CsvLabel.values().length + 5];
		for (int i = 0; i < CsvLabel.values().length; i++) {
			CSV_LABELS[i] = CsvLabel.values()[i].simpleName();
		}
		
		int nextIndex = CsvLabel.values().length;
		CSV_LABELS[nextIndex++] = hplpLabel(JobState.DROPPED_ON_FULL_QUEUE,JobState.DROPPED_ON_FULL_QUEUE);
		CSV_LABELS[nextIndex++] = hplpLabel(JobState.COMPLETED,JobState.DROPPED_ON_SIBLING_COMPLETION);
		CSV_LABELS[nextIndex++] = hplpLabel(JobState.COMPLETED,JobState.DROPPED_ON_FULL_QUEUE);
		CSV_LABELS[nextIndex++] = hplpLabel(JobState.DROPPED_ON_FULL_QUEUE,JobState.COMPLETED);
		CSV_LABELS[nextIndex++] = hplpLabel(JobState.DROPPED_ON_SIBLING_COMPLETION,JobState.COMPLETED);
	}

	/**
	 * @param hpJobCompletionState
	 * @param lpJobCompletionState
	 * @return
	 */
	public static String hplpLabel(JobState hpJobCompletionState,
			JobState lpJobCompletionState) {
		return "HP-" + hpJobCompletionState.name() + " and LP-" + lpJobCompletionState.name();
	}

	private static String step() {
		substeps = 1;
		return steps++ + ") ";
	}
	
	private static String substep() {
		return (steps - 1) + "." + substeps++ + ") ";
	}

	/**
	 * @param string
	 */
	private static void usage(String error) {
		log.fatal(error);
		System.err.println(USAGE);
	}
}
