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
	private double localTime;
	
	public double getLocalTime() {
		return localTime;
	}

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
	
	public Server() {
		statisticsCollector = new StatisticsCollector();
		hpQueue = new JobsQueue(statisticsCollector, this, Priority.HIGH, hpQueueMaxSize);
		lpQueue = new JobsQueue(statisticsCollector, this, Priority.LOW, lpQueueMaxSize);
		serverID = ++lastServerCreatedID;
		log.info(String.format("A server was created with ID = %d", serverID));
	}
	
	public void AddJob(Job job, Priority priority)
	{
		// Updating the server's local time
		currentTimeChanged(job.getCreationTime());
		
		if(Double.compare(localTime, job.getCreationTime()) != 0)
		{
			throw new RuntimeException("This is not possible, at this stage the local time should equal the job's creation time");
		}
		
		if(Double.compare(job.getJobLength(), 0.0) == 0)
		{
			this.shutDown(localTime);
			return;
		}
		
		JobsQueue jobsQueue = (priority == Priority.HIGH) ? hpQueue : lpQueue;
		job.associatedQueue = jobsQueue;
		job.associatedServer = this;
		try {
			jobsQueue.enqueue(job);
			job.setState(JobState.IN_QUEUE);
			log.debug(String.format("New job added to server %d with priority %s at %f", serverID, priority.toString(), job.getCreationTime()));
		} catch (QueueIsFullException e) {
			job.setState(JobState.REJECTED);
			statisticsCollector.jobRejected(job);
			log.debug(String.format("Queue with priority %s in Server %d is full and rejected a job.", priority.toString(), serverID));
		}
	}
	
	private void shutDown(double currentTime) {
		hpQueue = null;
		lpQueue = null;
		statisticsCollector.endCollection(currentTime);
	}

	public void currentTimeChanged(double currentTime)
	{
		if(Double.compare(localTime, currentTime) >= 0)
		{
			return;
		}
		
		while(Double.compare(localTime, currentTime) < 0)
		{
			if(currentJob != null) // A job is running
			{
				handleRunningJob(currentTime);
			}
			else
			{
				handleNextJob(currentTime);
			}
		}
	}

	private void handleRunningJob(double currentTime) {
		double jobAproxEndTime = currentJob.getExecutionStartTime() + currentJob.getJobLength();
		if(currentJob.associatedQueue.getQueuePriority() == Priority.LOW)
		{
			// It is possible that the mirror server may finish the job as HP before this server does
			// hence we are updating that server's local time.
			double safeUpdateTime = Math.min(currentTime, jobAproxEndTime);
			currentJob.getMirrorJob().associatedServer.currentTimeChanged(safeUpdateTime);
			if(currentJob.getState() == JobState.DISCARDED)
			{
				localTime = currentJob.getDiscardTime();
				currentJob = null;
			}
			else if(!hpQueue.isEmpty())
			{
				// putting the LP job back in the queue
				currentJob.setState(JobState.IN_QUEUE);
				lpQueue.addFirst(currentJob);
				statisticsCollector.jobPreempted(currentJob);
				currentJob = null;
			}
			else if(Double.compare(jobAproxEndTime, currentTime) < 0)
			{
				// The LP job completed successfully
				localTime = jobAproxEndTime;
				currentJob.setExecutionEndTime(localTime);
				currentJob.setState(JobState.COMPLETED);
				currentJob.getMirrorJob().discardJob(localTime);
				statisticsCollector.jobCompleted(currentJob);
				log.debug(String.format("a job completed successfully in server %d at %f", serverID, localTime));
				currentJob = null;
			}
			else
			{
				// The job still needs to run
				localTime = currentTime;	
			}
		}
		else // a HP job
		{
			if(Double.compare(jobAproxEndTime, currentTime) < 0)
			{
				localTime = jobAproxEndTime;
				currentJob.setExecutionEndTime(localTime);
				currentJob.setState(JobState.COMPLETED);
				statisticsCollector.jobCompleted(currentJob);
				log.debug(String.format("a job completed successfully in server %d at %f", serverID, localTime));
				currentJob = null;
			}
			else
			{
				localTime = currentTime;
			}
		}
	}
	
	private void handleNextJob(double currentTime) {
		if(!hpQueue.isEmpty())
		{
			currentJob = hpQueue.dequeue();
			// Giving the mirror server a chance to execute this job as LP before
			// doing it in this server as HP.
			currentJob.getMirrorJob().associatedServer.currentTimeChanged(localTime);
			if(currentJob.getState() == JobState.DISCARDED)
			{
				currentJob = null;
			}
			else
			{
				log.debug(String.format("Executing next job from high priority Queue in server %d", serverID));
				currentJob.setState(JobState.RUNNING);
				currentJob.setExecutionStartTime(localTime);
				// High priority, hence signaling the LQ before processing
				currentJob.getMirrorJob().discardJob(localTime);	
			}
		}
		else if(!lpQueue.isEmpty())
		{
			currentJob = lpQueue.dequeue();
			
			// It is possible that the mirror server may finish the job as HP before this server does
			// hence we are updating that server's local time.
			double safeUpdateTime = Math.min(currentTime, localTime + currentJob.getJobLength());
			currentJob.getMirrorJob().associatedServer.currentTimeChanged(safeUpdateTime);
			if(currentJob.getState() == JobState.DISCARDED)
			{
				currentJob = null;
				return;
			}
			log.debug(String.format("Executing next job from low priority Queue in server %d", serverID));
			currentJob.setState(JobState.RUNNING);
			currentJob.setExecutionStartTime(localTime);
		}
		else
		{
			log.debug(String.format("No jobs to execute in server %d", serverID));
			currentJob = null;
			localTime = currentTime;
		}	
	}
	
}
