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

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;

import config.Configuration;

/**
 * @author Assaf Israel
 *
 */
public class EventGenerator {

	private Configuration config;
	
	private long jobsRemained;
	private double clock = 0.0;
	
	private final double JOB_MEAN_LENGTH = 1.0;
	private double averageArrivalRate;
	
	private RandomData intervalRandomizer = new RandomDataImpl(); 
	private RandomData lengthRandomizer = new RandomDataImpl();
	
	/**
	 * @param config
	 */
	public EventGenerator(Configuration config) {
		this.config = config;
		this.jobsRemained = config.getNumJobs();
		
		if (jobsRemained < 1) {
			throw new IllegalArgumentException("Number of jobs must be positive.");
		}
		
		if (config.getNumServers() < 2) {
			throw new IllegalArgumentException("Number of servers must exceed 1.");
		}

		if (config.getLoad() <= 0.0 || config.getLoad() > 1.0) {
			throw new IllegalArgumentException("Load must be in the range (0,1].");
		}
		
		// Normalized according to the number of servers
		this.averageArrivalRate = 1/config.getNumServers() * config.getLoad(); 
	}

	/**
	 * @return
	 */
	public boolean done() {
		return jobsRemained == 0;
	}

	/**
	 * @return
	 */
	public Job nextJob() {
		double interval = intervalRandomizer.nextExponential(averageArrivalRate);
		double jonLength = lengthRandomizer.nextExponential(JOB_MEAN_LENGTH);

		clock += interval;
//		Job nextJob = new Job(null, jobLength, clock);
		return null;
	}

}
