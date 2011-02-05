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

import config.IConfiguration;

/**
 * @author Assaf Israel
 *
 */
public class EventGenerator {

	private static final double EPSILON = 1e-8;
	private long jobsRemained;
	private double clock = 0.0;
	
	private final double JOB_MEAN_LENGTH = 1.0;
	private double averageArrivalRate;
	
	private RandomData intervalRandomizer = new RandomDataImpl(); 
	private RandomData lengthRandomizer = new RandomDataImpl();
	
	/**
	 * @param config
	 */
	public EventGenerator(IConfiguration config) {
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
		this.averageArrivalRate = 1/(double)config.getNumServers() * config.getLoad(); 
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
		double jobLength = lengthRandomizer.nextExponential(JOB_MEAN_LENGTH);
		
		/*
		 * According to the definition of exponential probability the result cannot be <=0 !
		 */
		assert(interval > EPSILON);
		assert(jobLength > EPSILON);

		clock += interval;
		return new Job(jobLength, clock);
	}

}
