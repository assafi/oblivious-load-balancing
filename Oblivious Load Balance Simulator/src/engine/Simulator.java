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

import engine.Server.Priority;

/**
 * @author Assaf Israel
 *
 */
public class Simulator {

	private EventGenerator eGen;
	private Server[] servers;
	
	private RandomData indexRandomizer = new RandomDataImpl(); 
	
	/**
	 * @param eGen
	 * @param servers
	 */
	public Simulator(EventGenerator eGen, Server[] servers) {
		this.eGen = eGen;
		this.servers = servers;
	}

	/**
	 * 
	 */
	public void execute() {
	
		while (!eGen.done()) {
			Job primaryJob = eGen.nextJob();
			Job secondaryJob = primaryJob; //primaryJob.clone(); CHANGE THIS !!!!
			
			int primaryServerIndex = indexRandomizer.nextInt(0, servers.length - 1);
			int secondaryServerIndex = indexRandomizer.nextInt(0, servers.length - 1);
			
			/*
			 * Making sure the primary server is not the same as the secondary one
			 */
			while (primaryServerIndex == secondaryServerIndex) {
				secondaryServerIndex = indexRandomizer.nextInt(0, servers.length - 1);
			}
			
			Server primaryServer = servers[primaryServerIndex];
			Server secondaryServer = servers[secondaryServerIndex];

			/*
			 * Advancing time before inserting new jobs
			 */
			for (Server server : servers) {
				server.currentTimeChanged(primaryJob.getCreationTime());
			}
			
			primaryServer.AddJob(primaryJob, Priority.HIGH);
			secondaryServer.AddJob(secondaryJob, Priority.LOW);
		}
		
	}

}
