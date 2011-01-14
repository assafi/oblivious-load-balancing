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

import org.apache.log4j.Logger;
import config.Configuration;
import config.LogFactory;
import exceptions.QueueIsFullException;

/**
 * @author Asi Bross
 *
 */
public class Server {
	private static Logger log = LogFactory.getLog(Server.class);
	private static Configuration config;
	private static int lpQueueMaxSize;
	private static int hpQueueMaxSize;
	
	private static int lastServerCreatedID = 0; // For debug
	
	private int serverID; // For debug
	private JobsQueue lpQueue; 
	private JobsQueue hpQueue;
	private Job currentJob = null;
	
	public enum Priority { HIGH, LOW };
	
	public static void SetServersConfiguration(Configuration config)
	{
		Server.config = config;
		int totalSlots = Server.config.getMemorySize();
		double dFactor = Server.config.getDistributionFactor();
		hpQueueMaxSize = (int)Math.round(totalSlots * dFactor);
		lpQueueMaxSize = totalSlots - hpQueueMaxSize;
		log.info("Server configuration was set.");
		log.info(String.format("Max size of HP Queue is %d", hpQueueMaxSize));
		log.info(String.format("Max size of LP Queue is %d", lpQueueMaxSize));
	}
	
	/**
	 * @param config System configuration
	 */
	public Server() {
		hpQueue = new JobsQueue(Priority.HIGH, hpQueueMaxSize);
		lpQueue = new JobsQueue(Priority.LOW, lpQueueMaxSize);
		serverID = ++lastServerCreatedID;
		log.info(String.format("A server was created with ID = %d", serverID));
	}
	
	public void AddJob(Job job, Priority priority)
	{
		JobsQueue jobsQueue = (priority == Priority.HIGH) ? hpQueue : lpQueue;
		job.associatedQueue = jobsQueue;
		try {
			jobsQueue.enqueue(job);
			log.info(String.format("New job added to server %d with priority %s", serverID, priority.toString()));
		} catch (QueueIsFullException e) {
			log.info(String.format("Queue with priority %s in Server %d is full and rejected a job.", priority.toString(), serverID));
			// TODO: Alert the log that a job was rejected because of queue size
		}
	}
	
	public void currentTimeChanged(long currentTime)
	{
		if(null == currentJob)
		{
			// No job is currently being executed
			executeNextJob(currentTime);
		}
		else
		{
			if((currentJob.getExecutionStartTime() + currentJob.getJobLength()) < currentTime)
			{
				log.error(String.format("A job was execute more then it should have in server %d", serverID));
				throw new RuntimeException("This shouldn't have happened, it means that a job was executed more then it was intended.");
			}
			if((currentJob.getExecutionStartTime() + currentJob.getJobLength()) == currentTime)
			{
				currentJob.setExecutionEndTime(currentTime);
				currentJob.setJobCompletedSuccessfully();
				currentJob.getMirrorJob().discardJob(currentTime);
				log.info(String.format("a job completed successfully in server ", serverID));
				// TODO: Alert the log that a job was finished.
				executeNextJob(currentTime);
			}
		}
	}

	/**
	 * 
	 */
	private void executeNextJob(long currentTime) {
		if(!hpQueue.isEmpty())
		{
			log.info(String.format("Executing next job from high priority Queue in server %d", serverID));
			currentJob = hpQueue.dequeue();
		}
		else if(!lpQueue.isEmpty())
		{
			log.info(String.format("Executing next job from low priority Queue in server %d", serverID));
			currentJob = lpQueue.dequeue();
		}
		else
		{
			log.info(String.format("No jobs to execute in server %d", serverID));
			currentJob = null;	
		}
	}

}
