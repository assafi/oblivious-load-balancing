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

import config.Configuration;

/**
 * @author Assaf Israel
 *
 */
public class Main {

	private static final int NUM_EXPECTED_ARGUMENTS = 1;
	
	private static final String USAGE = "<xml-config-file>";
	
	public static void main(String[] args) {
		if (args.length != NUM_EXPECTED_ARGUMENTS) {
			usage("Wrong number of arguments");
			System.exit(1);
		}
		
		Configuration config;
		try {
			config = new Configuration(args[1]);
		} catch (Exception e) {
			usage(e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * @param string
	 */
	private static void usage(String error) {
		System.err.println(error);
		System.err.println(USAGE);
	}
}
