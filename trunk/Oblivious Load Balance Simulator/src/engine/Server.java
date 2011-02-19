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

import config.IConfiguration;
import config.LogFactory;
import config.QueuePolicy;
import exceptions.QueueIsFullException;

/**
 * @author Asi Bross, Assaf Israel
 *
 */
public class Server {
	private static Logger log = LogFactory.getLog(Server.class);
	private static IConfiguration config;
	private static long lpQueueMaxSize;
	private static long hpQueueMaxSize;
	
	private static int lastServerCreatedID = 0; // For debug
	public int serverID; // For debug
	
	private JobsQueue lpQueue; 
	private JobsQueue hpQueue;
	private Job currentJob = null;
	private double localTime;
	
	public double getLocalTime() {
		return localTime;
	}

	private StatisticsCollector statisticsCollector;
	
	public enum Priority { HIGH, LOW };
	
	public static void SetServersConfiguration(IConfiguration config)
	{
		Server.config = config;
		
		if (QueuePolicy.FINITE.equals(config.getPolicy())) {
			int totalSlots = Server.config.getMemorySize();
			double dFactor = Server.config.getDistributionFactor();
			hpQueueMaxSize = (int)Math.round(totalSlots * dFactor);
			lpQueueMaxSize = totalSlots - hpQueueMaxSize;
			log.debug(String.format("Max size of HP Queue is %d", hpQueueMaxSize));
			log.debug(String.format("Max size of LP Queue is %d", lpQueueMaxSize));
		} else {
			hpQueueMaxSize = Long.MAX_VALUE;
			lpQueueMaxSize = Long.MAX_VALUE;
		}
		log.debug("Server configuration was set.");
	}
	
	public Server() {
		statisticsCollector = new StatisticsCollector();
		hpQueue = new JobsQueue(statisticsCollector, this, Priority.HIGH, hpQueueMaxSize);
		lpQueue = new JobsQueue(statisticsCollector, this, Priority.LOW, lpQueueMaxSize);
		serverID = ++lastServerCreatedID;
		log.debug(String.format("A server was created with ID = %d", serverID));
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
			log.debug(String.format("New job[%d] added to server[%d] with priority %s at %f", job.jobID, serverID, priority.toString(), localTime));
		} catch (QueueIsFullException e) {
			job.setState(JobState.DROPPED_ON_FULL_QUEUE);
			job.setExecutionStartTime(job.getCreationTime());
			job.setExecutionEndTime(job.getCreationTime());
			if (job.getMirrorJob().getState().isCompletionState()) {
				/*
				 * Both jobs are completed/discarded and there for we can report Job termination 
				 */
				statisticsCollector.reportTermination(job);
			}
			log.debug(String.format("Queue with priority %s in Server[%d] is full and rejected a job[%d] at %f.", priority.toString(), serverID, job.jobID, localTime));
		}
	}
	
	private void shutDown(double currentTime) {
		hpQueue = null;
		lpQueue = null;
		statisticsCollector.updateGlobalCollector();
	}

	public void currentTimeChanged(double currentTime)
	{
		log.debug(String.format("Time at server[%d] will be advanced to %f", serverID, currentTime));
		if(Double.compare(localTime, currentTime) >= 0)
		{
			return;
		}
		
		{
			while(Double.compare(localTime, currentTime) < 0)
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
		double jobAproxEndTime = getNextJobTerminationTime(currentJob);
		
		if(currentJob.associatedQueue.getQueuePriority() == Priority.LOW)
		{
			// It is possible that the mirror server may finish the job as HP before this server does
			// hence we are updating that server's local time.
			currentJob.getMirrorJob().associatedServer.currentTimeChanged(localTime);
			if(currentJob.getState() == JobState.DROPPED_ON_SIBLING_COMPLETION)
			{
				if (localTime < currentJob.getDiscardTime()) {
					localTime = Math.min(currentJob.getDiscardTime(),currentTime);
				}
				log.debug(String.format("Running LP job[%d] was discarded at server[%d] at %f", currentJob.jobID, serverID, localTime));
				currentJob = null;
			}
			else if(!hpQueue.isEmpty() && Double.compare(jobAproxEndTime, hpQueue.peek().getCreationTime()) == 0)
			{
				// putting the LP job back in the queue
				currentJob.setState(JobState.IN_QUEUE);
				try {
					lpQueue.addFirst(currentJob);
					log.debug(String.format("Running LP job[%d] was preempted at server[%d] at %f", currentJob.jobID, serverID, localTime));
				} catch (QueueIsFullException e) {
					log.debug(String.format("Running LP job[%d] was discarded at server[%d] at %f because it was preempted and the LP queue is full", currentJob.jobID, serverID, localTime));
					currentJob.setState(JobState.DROPPED_ON_FULL_QUEUE);
					if (currentJob.getMirrorJob().getState().equals(JobState.DROPPED_ON_FULL_QUEUE)) {
						/*
						 * In case the HP job has been discarded the Job is considered terminated.
						 */
						statisticsCollector.reportTermination(currentJob);
					}
				}
				localTime = currentTime;
				currentJob = null;
			}
			else if(Double.compare(jobAproxEndTime, currentTime) < 0 && 
					Double.compare(jobAproxEndTime,currentJob.getExecutionStartTime() + currentJob.getJobLength()) == 0)
			{
				// The LP job completed successfully
				localTime = jobAproxEndTime;
				currentJob.setExecutionEndTime(localTime);
				currentJob.setState(JobState.COMPLETED);
				currentJob.getMirrorJob().discardJob(localTime);
				statisticsCollector.reportTermination(currentJob);
				log.debug(String.format("Running LP job[%d] was completed successfully at server[%d] at %f", currentJob.jobID, serverID, localTime));
				currentJob = null;
			}
			else
			{
				// The job still needs to run
				localTime = currentTime;
				log.debug(String.format("Server[%d] local time updated to %f", serverID, localTime));
			}
		}
		else // a HP job
		{
			if(Double.compare(jobAproxEndTime, currentTime) < 0)
			{
				localTime = jobAproxEndTime;
				currentJob.setExecutionEndTime(localTime);
				currentJob.setState(JobState.COMPLETED);
				statisticsCollector.reportTermination(currentJob);
				log.debug(String.format("Running HP job[%d] was completed successfully at server[%d] at %f", currentJob.jobID, serverID, localTime));
				currentJob = null;
			}
			else
			{
				localTime = currentTime;
				log.debug(String.format("Server[%d] local time updated to %f", serverID, localTime));
			}
		}
	}
	
	/**
	 * @param currentJob
	 * @return
	 */
	private double getNextJobTerminationTime(Job currentJob) {
		double normalEndTime = currentJob.getExecutionStartTime() + currentJob.getJobLength();
		if (currentJob.associatedQueue.getQueuePriority().equals(Priority.HIGH)) {
			/*
			 * local LP job cannot stop HP job
			 */
			return normalEndTime;
		}
		double nextHPJobStartTime;
		if (!hpQueue.isEmpty() && 
				normalEndTime > (nextHPJobStartTime = hpQueue.peek().getCreationTime())) {
			return nextHPJobStartTime;
		}
		return normalEndTime;
	}

	private void handleNextJob(double currentTime) {
		if(!hpQueue.isEmpty() && 
			(hpQueue.peek().getCreationTime() <= localTime ||
			 (hpQueue.peek().getCreationTime() <= currentTime &&
				(lpQueue.isEmpty() || 
				 lpQueue.peek().getCreationTime() < hpQueue.peek().getCreationTime()))))
			/*
			 * The last condition checks if there should be a LP job that should start running
			 * between the HP job creation time and the local time.
			 */
		{
			currentJob = hpQueue.peek();
			
			/*
			 * this is a HP job, so it's starting time is always its creation time
			 */
			localTime = Math.max(currentJob.getCreationTime(),localTime); 
			
			/*
			 *  Giving the mirror server a chance to execute this job as LP before
			 *   doing it in this server as HP.
			 */
			currentJob.getMirrorJob().associatedServer.currentTimeChanged(localTime);
			if((currentJob == null) || (currentJob.getState() == JobState.DROPPED_ON_SIBLING_COMPLETION))
			{
				currentJob.getMirrorJob().associatedServer.currentTimeChanged(localTime);
				currentJob = null;
			}
			else
			{
				log.debug(String.format("New HP job[%d] started on server[%d] at %f", currentJob.jobID, serverID, localTime));
				currentJob.setExecutionStartTime(localTime);
				// High priority, hence signaling the LQ before processing
				currentJob.getMirrorJob().discardJob(localTime);
				hpQueue.dequeue();
				currentJob.setState(JobState.RUNNING);
			}
		}
		else if(!lpQueue.isEmpty())
		{
			currentJob = lpQueue.dequeue();
			currentJob.setState(JobState.RUNNING);
			
			/*
			 *  It is possible that the mirror server may finish the job as HP before this server does
			 *  hence we are updating that server's local time.
			 */
			currentJob.setExecutionStartTime(localTime);
			
			/*
			 * We know that the mirror's job server will not get any new job before the "safeUpdateTime",
			 * and there for is safe to alert him of the time change. (localTime could also work here).    
			 */
			double safeUpdateTime = Math.min(currentTime, getNextJobTerminationTime(currentJob));
			currentJob.getMirrorJob().associatedServer.currentTimeChanged(safeUpdateTime);
			if(currentJob == null || currentJob.getState() == JobState.DROPPED_ON_SIBLING_COMPLETION)
			{
				currentJob = null;
				return;
			}
			log.debug(String.format("New LP job[%d] started on server[%d] at %f", currentJob.jobID, serverID, localTime));
		}
		else
		{
			log.debug(String.format("No jobs to execute in server[%d] at %f hence progressing time to %f", serverID, localTime, currentTime));
			currentJob = null;
			localTime = currentTime;
		}	
	}
	
}
