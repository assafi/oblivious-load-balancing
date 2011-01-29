/**
 * Oblivious Load Balance Simulator
 *
 * 236635 - On the Management and Efficiency of Cloud Based Services (W 2011)
 * CS Faculty, Technion - Institute of Technology 
 *
 * Authors: Assaf Israel, Eli Nazarov, Asi Bross 
 * 
 */
package config.stubs;

import config.IConfiguration;
import config.QueuePolicy;

/**
 * @author Assaf Israel
 *
 */
public class ConfigurationStub implements IConfiguration {

	public double distrbutionFactor;
	public double load;
	public int memorySize;
	public long numJobs;
	public int numServers;
	public QueuePolicy policy;
	
	public ConfigurationStub() {
	}

	@Override
	public double getDistributionFactor() {
		return distrbutionFactor;
	}

	@Override
	public double getLoad() {
		return load;
	}

	@Override
	public int getMemorySize() {
		return memorySize;
	}

	@Override
	public long getNumJobs() {
		return numJobs;
	}

	@Override
	public int getNumServers() {
		return numServers;
	}

	@Override
	public QueuePolicy getPolicy() {
		return policy;
	}

}
