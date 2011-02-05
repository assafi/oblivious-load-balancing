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
			Job secondaryJob = primaryJob.clone(); 
			
			primaryJob.setMirrorJob(secondaryJob);
			secondaryJob.setMirrorJob(primaryJob);
			
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
			 * Need to push secondary job first, because the notification of a HP job to the server
			 * is when the job is starting execution (when the LP job is not inserted to the queue) 
			 */
			secondaryServer.AddJob(secondaryJob, Priority.LOW);  
			primaryServer.AddJob(primaryJob, Priority.HIGH);
		}
		
	}
}
