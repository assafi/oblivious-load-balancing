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
public interface IConfiguration {

	public int getNumServers();

	public QueuePolicy getPolicy();

	public double getLoad();
	
	public int getMemorySize();
	
	public double getDistributionFactor();

	public long getNumJobs();
}