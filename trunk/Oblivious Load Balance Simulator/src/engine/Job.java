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
	private double jobLength;
	private double jobCreationTime;
	private double executionStartTime;
	private double executionEndTime;
	private double discardTime;
	public double getDiscardTime() {
		return discardTime;
	}

	public void setDiscardTime(double discardTime) {
		this.discardTime = discardTime;
	}

	private JobState state;
	
	public enum JobState { INITIAL, IN_QUEUE, RUNNING, COMPLETED, DISCARDED, PREEMPTED, REJECTED }
	
	// This queue this job belongs to.
	public JobsQueue associatedQueue;

	// The server this job belongs to.
	public Server associatedServer;
	
	public Job(double jobLength, double creationTime)
	{
		this.jobLength = jobLength;
		this.jobCreationTime = creationTime;
	}
	
	@Override
	protected Job clone() {
		Job ret;
		if(this.mirrorJob == null)
		{
			ret = new Job(this.jobLength, this.jobCreationTime);
		}
		else
		{
			ret = new Job(this.mirrorJob, this.jobLength, this.jobCreationTime);
		}
		return ret;
	}

	public Job(Job mirrorJob, double jobLength, double creationTime)
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
	
	public void setMirrorJob(Job mirrorJob)
	{
		this.mirrorJob = mirrorJob;
	}

	public double getJobLength()
	{
		return jobLength;
	}
	
	public double getCreationTime()
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
	
	public void discardJob(double currentTime)
	{
		switch (state) {
		case COMPLETED:
			throw new RuntimeException("This shouldn't have happened, it is not possible for a job to complete while its mirror job was already completed");
		case IN_QUEUE:
			// Alert the queue that one of the jobs he has was discarded.
			associatedQueue.alertJobDiscarded();
		case RUNNING:
			discardTime = currentTime;
			state = JobState.DISCARDED;
			break;
		default:
			return;
		}
	}
	
	public double getExecutionEndTime()
	{
		return executionEndTime;
	}
	
	public void setExecutionEndTime(double time)
	{
		executionEndTime = time;
	}

	public void setExecutionStartTime(double time) {
		this.executionStartTime = time;
	}
	
	public double getExecutionStartTime() {
		return executionStartTime;
	}
	
}
