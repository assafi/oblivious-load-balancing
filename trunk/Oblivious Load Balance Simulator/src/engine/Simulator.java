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
import org.apache.log4j.Logger;

import config.LogFactory;

import engine.Server.Priority;

/**
 * @author Assaf Israel
 * 
 */
public class Simulator {

	private static Logger log = LogFactory.getLog(Simulator.class);
	
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

		long counter = 0;
		while (!eGen.done()) {
			Job primaryJob = eGen.nextJob();
			Job secondaryJob = primaryJob.clone();

			primaryJob.setMirrorJob(secondaryJob);
			secondaryJob.setMirrorJob(primaryJob);

			int primaryServerIndex = indexRandomizer.nextSecureInt(0,
					servers.length - 1);
			int secondaryServerIndex = indexRandomizer.nextSecureInt(0,
					servers.length - 1);

			/*
			 * Making sure the primary server is not the same as the secondary
			 * one
			 */
			while (primaryServerIndex == secondaryServerIndex) {
				secondaryServerIndex = indexRandomizer.nextInt(0,
						servers.length - 1);
			}

			Server primaryServer = servers[primaryServerIndex];
			Server secondaryServer = servers[secondaryServerIndex];

			log.debug("Job #" + counter++ + ", job created at: " + primaryJob.getCreationTime());

			/*
			 * Need to push secondary job first, because the notification of a
			 * HP job to the server is when the job is starting execution (when
			 * the LP job is not inserted to the queue)
			 */
			secondaryServer.AddJob(secondaryJob, Priority.LOW);
			primaryServer.AddJob(primaryJob, Priority.HIGH);
		}
		
		Job finalJob = eGen.finalJob();
		for (Server server : servers) {
			/*
			 * In order to avoid preemption of currently executing LP jobs the
			 * final job is also of low priority.
			 */
			server.AddJob(finalJob, Priority.LOW);
		}
	}
}
