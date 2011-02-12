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

import java.util.LinkedList;
import java.util.NoSuchElementException;

import engine.Server.Priority;
import exceptions.QueueIsFullException;

/**
 * @author Asi Bross
 *
 */
public class JobsQueue {

	private LinkedList<Job> list;
	private Priority priority;
	private long size;
	private long maxSize;
	private StatisticsCollector statisticsCollector;
	private Server associatedServer;

	public JobsQueue(StatisticsCollector statisticsCollector, Server associatedServer, Priority priority, long maxSize) {
		this.statisticsCollector = statisticsCollector;
		this.associatedServer = associatedServer; 
		list = new LinkedList<Job>();
		this.priority = priority;
		this.maxSize = maxSize;
		size = 0;
	}
	
	public long getQueueMaxSize()
	{
		return maxSize;
	}
	
	public Priority getQueuePriority()
	{
		return priority;
	}
	
	public boolean isEmpty()
	{
		return (size() == 0);
	}
	
	public long size()
	{
		return size;
	}
	
	public void enqueue(Job job) throws QueueIsFullException
	{
		if(size() >= maxSize)
		{
			throw new QueueIsFullException(job);
		}
		list.add(job);
		size++;
		statisticsCollector.updateQueueLength(priority, size(), 
				associatedServer.getLocalTime(), job.ignore);
	}
	
	public void addFirst(Job job) throws QueueIsFullException
	{
		if (size() >= maxSize) {
			throw new QueueIsFullException(job);
		}
		list.addFirst(job);
		size++;
		statisticsCollector.updateQueueLength(priority, size(), 
				associatedServer.getLocalTime(),job.ignore);
	}
	
	public Job dequeue()
	{
		Job retJob = peek(); 
		list.remove();
		size--;
		statisticsCollector.updateQueueLength(priority, size(), 
				associatedServer.getLocalTime(),retJob.ignore);
		if(isEmpty())
		{
			list.clear();
		}
		return retJob;
	}
	
	public Job peek()
	{
		if(isEmpty())
		{
			list.clear();
			throw new NoSuchElementException();
		}
		while(list.peek().getState() == JobState.DROPPED_ON_SIBLING_COMPLETION)
		{
			list.remove();
		}
		return list.peek();
	}

	public void alertJobDiscarded(Job job) {
		size--;
		statisticsCollector.updateQueueLength(priority, size(), 
				associatedServer.getLocalTime(),job.ignore);
	}
	
}
