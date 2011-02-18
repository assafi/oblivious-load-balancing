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


/**
 * @author Assaf Israel
 *
 */
public class Configuration implements IConfiguration {
	
	private int numServers = 0;
	
	private QueuePolicy policy = null;

	private double load = 0.0;

	private int memorySize = 0;

	private double dFactor = 0.0;

	private long numJobs = 0;

	private double jobMeanLength = 1.0;

	private double statisticalMargin = 0.0;
	
	public Configuration() {}
	

	public int getNumServers() {
		return numServers;
	}

	public QueuePolicy getPolicy() {
		return policy;
	}

	public double getLoad() {
		return load;
	}
	
	public int getMemorySize() {
		return memorySize;
	}
	
	public double getDistributionFactor() {
		return dFactor;
	}

	public long getNumJobs() {
		return numJobs;
	}

	public double getJobMeanLength() {
		return jobMeanLength;
	}

	public double getStatisticalMargin() {
		return statisticalMargin;
	}
	
	public void setPolicy(QueuePolicy policy) {
		this.policy = policy;
	}

	public void setdFactor(double dFactor) {
		this.dFactor = dFactor;
	}
	
	public void setNumServers(int numServers) {
		this.numServers = numServers;
	}
	
	public void setNumJobs(long numJobs) {
		this.numJobs = numJobs;
	}
	
	public void setMemorySize(int memorySize) {
		this.memorySize = memorySize;
	}
	
	public void setLoad(double load) {
		this.load = load;
	}
	
	public void setJobMeanLength(double jobMeanLength) {
		this.jobMeanLength = jobMeanLength;
	}

	public void setStatisticalMargin(double statisticalMargin) {
		this.statisticalMargin = statisticalMargin;
	}
}
