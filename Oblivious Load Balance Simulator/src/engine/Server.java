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
import engine.Job.JobState;
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
	
	private StatisticsCollector statisticsCollector;
	
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
		statisticsCollector = new StatisticsCollector();
		hpQueue = new JobsQueue(statisticsCollector, Priority.HIGH, hpQueueMaxSize);
		lpQueue = new JobsQueue(statisticsCollector, Priority.LOW, lpQueueMaxSize);
		serverID = ++lastServerCreatedID;
		log.info(String.format("A server was created with ID = %d", serverID));
	}
	
	public void AddJob(Job job, Priority priority)
	{
		JobsQueue jobsQueue = (priority == Priority.HIGH) ? hpQueue : lpQueue;
		job.associatedQueue = jobsQueue;
		try {
			jobsQueue.enqueue(job);
			job.setState(JobState.IN_QUEUE);
			log.debug(String.format("New job added to server %d with priority %s", serverID, priority.toString()));
		} catch (QueueIsFullException e) {
			job.setState(JobState.REJECTED);
			statisticsCollector.jobRejected(job);
			log.debug(String.format("Queue with priority %s in Server %d is full and rejected a job.", priority.toString(), serverID));
		}
	}
	
	public void currentTimeChanged(long currentTime)
	{
		if((null == currentJob) || (currentJob.getState() == JobState.DISCARDED))
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
			else if((currentJob.getExecutionStartTime() + currentJob.getJobLength()) == currentTime)
			{
				// The current job was fully executed 
				currentJob.setExecutionEndTime(currentTime);
				currentJob.setState(JobState.COMPLETED);
				statisticsCollector.jobCompleted(currentJob);
				if(currentJob.associatedQueue.getQueuePriority() == Priority.LOW)
				{
					// Low priority, therefore signaling the HQ after processing.
					currentJob.getMirrorJob().discardJob(currentTime);
				}
				log.debug(String.format("a job completed successfully in server ", serverID));
				executeNextJob(currentTime);
			}
			else if((currentJob.associatedQueue.getQueuePriority() == Priority.LOW) && (!hpQueue.isEmpty())) 
			{ 
				currentJob.setExecutionEndTime(currentTime);
				currentJob.setState(JobState.PREEMPTED);
				//TODO: Alert the statistics collector that a low priority job was preempt because a high priority job needs to run.
				executeNextJob(currentTime);
			}
		}
	}

	private void executeNextJob(long currentTime) {
		if(!hpQueue.isEmpty())
		{
			log.debug(String.format("Executing next job from high priority Queue in server %d", serverID));
			currentJob = hpQueue.dequeue();
			currentJob.setState(JobState.RUNNING);
			// High priority, hence signaling the LQ before processing
			currentJob.getMirrorJob().discardJob(currentTime);
		}
		else if(!lpQueue.isEmpty())
		{
			log.debug(String.format("Executing next job from low priority Queue in server %d", serverID));
			currentJob = lpQueue.dequeue();
			currentJob.setState(JobState.RUNNING);
		}
		else
		{
			log.debug(String.format("No jobs to execute in server %d", serverID));
			currentJob = null;	
		}
	}

}
