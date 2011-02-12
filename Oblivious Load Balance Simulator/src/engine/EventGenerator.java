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

	public static final double STATISTICAL_MARGIN = 0.1;
	
	private long jobsTotal;
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

		// Normalized according to the number of servers
		this.averageArrivalRate = 
			1 / (config.getNumServers() * config.getLoad());
	}

	/**
	 * @return
	 */
	public boolean done() {
		return jobsRemained == 0;
	}

	/**
	 * @param ordinal The sequential number of the job
	 * @return True iff the ordinal number is in the upper or lower
	 * <i>STATISTICAL_MARGIN</i> percentage, False otherwise.
	 */
	public boolean ignoredJob(long ordinal) {
		return ordinal <= STATISTICAL_MARGIN * jobsTotal ||
			ordinal >= (1-STATISTICAL_MARGIN) * jobsTotal;
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
		double jobLength = lengthRandomizer.nextExponential(JOB_MEAN_LENGTH);

		clock += interval;
		return new Job(jobLength, clock,ignoredJob(jobsRemained));
	}

	/**
	 * @return The final Job, with creation time of (current clock + 2 * job
	 *         mean length) and length zero. This job indicates the termination
	 *         of servers activity, and initiate statistics collection.
	 */
	public Job finalJob() {
		return new Job(0.0, clock + 2 * JOB_MEAN_LENGTH,true); // + 10000.0
	}

}
