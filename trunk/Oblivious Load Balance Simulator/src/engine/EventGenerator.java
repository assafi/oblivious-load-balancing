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

	private final double statisticalMargin;

	private final long jobsTotal;
	private long jobsRemained;
	private double clock = 0.0;

	private final double jobMeanLength;
	private double averageArrivalRate;

	private RandomData intervalRandomizer = new RandomDataImpl();
	private RandomData lengthRandomizer = new RandomDataImpl();
	
	private static final int DOUBLE_PRECISION = 7; 
	

	/**
	 * @param config
	 */
	public EventGenerator(IConfiguration config) {
		this.jobsRemained = config.getNumJobs();
		this.jobsTotal = config.getNumJobs();

		if (jobsRemained < 1) {
			throw new IllegalArgumentException(
					"Number of jobs must be positive.");
		}

		if (config.getNumServers() < 2) {
			throw new IllegalArgumentException(
					"Number of servers must exceed 1.");
		}

		if (config.getLoad() <= 0.0 || config.getLoad() > 1.0) {
			throw new IllegalArgumentException(
					"Load must be in the range (0,1].");
		}


		if ((jobMeanLength = config.getJobMeanLength()) <= 0.0) {
			throw new IllegalArgumentException(
					"Job mean length must be a positive number");
		}
		
		// Normalized according to the number of servers
		this.averageArrivalRate = jobMeanLength / (config.getNumServers() * config
				.getLoad());
		
		if ((statisticalMargin = config.getStatisticalMargin()) < 0.0
				|| config.getStatisticalMargin() > 0.5) {
			throw new IllegalArgumentException(
					"Statistical margin must be in the range [0,0.5)");
		}
	}

	/**
	 * @return
	 */
	public boolean done() {
		return jobsRemained == 0;
	}

	/**
	 * @param ordinal
	 *            The sequential number of the job
	 * @return True iff the ordinal number is in the upper or lower
	 *         <i>STATISTICAL_MARGIN</i> percentage, False otherwise.
	 */
	public boolean ignoredJob(long ordinal) {
		return ordinal <= statisticalMargin * jobsTotal
				|| ordinal >= (1 - statisticalMargin) * jobsTotal;
	}

	/**
	 * @return
	 */
	public Job nextJob() {
		if (jobsRemained == 0) {
			return finalJob();
		}
		jobsRemained--;

		double interval = intervalRandomizer
				.nextExponential(averageArrivalRate);
		double jobLength = lengthRandomizer.
				nextExponential(jobMeanLength);
		
		clock += interval;
		return new Job(jobLength, clock, ignoredJob(jobsRemained));
	}

/**
	 * @param nextExponential
	 * @return
	 */
	@SuppressWarnings("unused")
	private double roundDouble(double doubleNum) {
		long round = (long) (doubleNum * (Math.pow(10, DOUBLE_PRECISION)));
		return round / (double)(Math.pow(10, DOUBLE_PRECISION));
	}

//	/**
//	 * @param average
//	 * @return
//	 */
//	private double exponential(double average) {
//		return -Math.log(1-new Random().nextDouble()) * average;
//	}

	/**
	 * @return The final Job, with creation time of (current clock + 2 * job
	 *         mean length) and length zero. This job indicates the termination
	 *         of servers activity, and initiate statistics collection.
	 */
	public Job finalJob() {
		return new Job(0.0, clock + 2 * jobMeanLength, true); // + 10000.0
	}

}
