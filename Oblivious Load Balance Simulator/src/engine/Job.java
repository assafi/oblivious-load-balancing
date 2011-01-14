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
	private boolean discarded = false;
	private boolean completedSuccessfully = false;
	
	// This association helps keep the queue aware of the number of jobs
	// that weren't discarded.
	public JobsQueue associatedQueue;
	
	public Job(Job mirrorJob, int jobLength, long creationTime)
	{
		this.mirrorJob = mirrorJob;
		this.jobLength = jobLength;
		this.jobCreationTime = creationTime;
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
	
	public void discardJob(long currentTime)
	{
		if(completedSuccessfully)
		{
			return;
		}
		discarded = true;
		executionEndTime = currentTime;
		
		// Alert the queue that one of the jobs he has was discarded.
		associatedQueue.alertJobDiscarded();
	}
	
	public boolean wasDiscarded()
	{
		return discarded;
	}
	
	public void setJobCompletedSuccessfully()
	{
		completedSuccessfully = true;
	}
	
	public boolean wasJobCompletedSuccessfully()
	{
		return completedSuccessfully;
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
