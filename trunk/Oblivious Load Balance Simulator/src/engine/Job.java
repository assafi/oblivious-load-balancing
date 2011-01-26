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

/**
 * @author Asi Bross
 *
 */
public class Job {
	private Job mirrorJob;
	private int jobLength;
	private long jobCreationTime;
	private long executionStartTime;
	private long executionEndTime;
	private JobState state;
	
	public enum JobState { INITIAL, IN_QUEUE, RUNNING, COMPLETED, DISCARDED, PREEMPTED, REJECTED }
	
	// This association helps keep the queue aware of the number of jobs
	// that weren't discarded.
	public JobsQueue associatedQueue;
	
	public Job(Job mirrorJob, int jobLength, long creationTime)
	{
		this.mirrorJob = mirrorJob;
		this.jobLength = jobLength;
		this.jobCreationTime = creationTime;
		this.state = JobState.INITIAL;
	}
	
	public Job getMirrorJob()
	{
		return mirrorJob;
	}

	public int getJobLength()
	{
		return jobLength;
	}
	
	public long getCreationTime()
	{
		return jobCreationTime;
	}
	
	public JobState getState()
	{
		return state;
	}
	
	public void setState(JobState state)
	{
		this.state = state;
	}
	
	public void discardJob(long currentTime)
	{
		switch (state) {
		case COMPLETED:
			throw new RuntimeException("This shouldn't have happened, it is not possible for a job to complete while its mirror job was already completed");
		case IN_QUEUE:
			// Alert the queue that one of the jobs he has was discarded.
			associatedQueue.alertJobDiscarded();
		case RUNNING:
			executionEndTime = currentTime;
			state = JobState.DISCARDED;
			break;
		default:
			return;
		}
	}
	
	public long getExecutionEndTime()
	{
		return executionEndTime;
	}
	
	public void setExecutionEndTime(long time)
	{
		executionEndTime = time;
	}

	public void setExecutionStartTime(long time) {
		this.executionStartTime = time;
	}
	
	public long getExecutionStartTime() {
		return executionStartTime;
	}
	
}
