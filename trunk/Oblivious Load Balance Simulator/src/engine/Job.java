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

import config.LogFactory;

/**
 * @author Asi Bross
 * 
 */
public class Job {
	private static Logger log = LogFactory.getLog(Job.class);

	private Job mirrorJob;
	private double jobLength;
	private double jobCreationTime;
	private double executionStartTime;
	private double executionEndTime;
	private double discardTime;
	private JobState state;

	private static int lastJobCreatedID = 0; // For debug
	public int jobID; // For debug

	// The queue this job belongs to.
	public JobsQueue associatedQueue;

	// The server this job belongs to.
	public Server associatedServer;

	public Job(double jobLength, double creationTime) {
		this(null, jobLength, creationTime);
	}

	@Override
	protected Job clone() {
		return new Job(this.mirrorJob, this.jobLength, this.jobCreationTime);
	}

	public Job(Job mirrorJob, double jobLength, double creationTime) {
		this.mirrorJob = mirrorJob;
		this.jobLength = jobLength;
		this.jobCreationTime = creationTime;
		this.state = JobState.INITIAL;
		jobID = ++lastJobCreatedID;
	}

	public Job getMirrorJob() {
		return mirrorJob;
	}

	public void setMirrorJob(Job mirrorJob) {
		this.mirrorJob = mirrorJob;
	}

	public double getJobLength() {
		return jobLength;
	}

	public double getCreationTime() {
		return jobCreationTime;
	}

	public JobState getState() {
		return state;
	}

	public void setState(JobState state) {
		this.state = state;
	}

	public void discardJob(double currentTime) {
		switch (state) {
		case COMPLETED:
			throw new RuntimeException(
					"This shouldn't have happened, it is not possible for a job to complete while its mirror job was already completed");
		case IN_QUEUE:
			// Alert the queue that one of the jobs was discarded.
			associatedQueue.alertJobDiscarded();
			state = JobState.DROPPED_ON_SIBLING_COMPLETION;
			break;
		case RUNNING:
			if (Double.compare(currentTime,associatedServer.getLocalTime()) > 0) {
				discardTime = currentTime;
			} else {
				discardTime = associatedServer.getLocalTime();
			}
			state = JobState.DROPPED_ON_SIBLING_COMPLETION;
			break;
		default:
			return;
		}
		log.debug(String.format(
				"Job[%d] with priority %s discarded at server %d at %f", jobID,
				associatedQueue.getQueuePriority().name(),
				associatedServer.serverID,currentTime));
	}

	public double getExecutionEndTime() {
		return executionEndTime;
	}

	public void setExecutionEndTime(double time) {
		executionEndTime = time;
	}

	public void setExecutionStartTime(double time) {
		this.executionStartTime = time;
	}

	public double getExecutionStartTime() {
		return executionStartTime;
	}

	public double getDiscardTime() {
		return discardTime;
	}

	public void setDiscardTime(double discardTime) {
		this.discardTime = discardTime;
	}

}
