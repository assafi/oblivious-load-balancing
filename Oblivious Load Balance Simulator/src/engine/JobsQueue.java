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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import engine.Job.JobState;
import engine.Server.Priority;
import exceptions.QueueIsFullException;

/**
 * @author Asi Bross
 *
 */
public class JobsQueue {

	private LinkedList<Job> list;
	private Priority priority;
	private int size;
	private int maxSize;
	private StatisticsCollector statisticsCollector;
	
	private Server associatedServer;
	/**
	 * 
	 */
	public JobsQueue(StatisticsCollector statisticsCollector, Server associatedServer, Priority priority, int maxSize) {
		this.statisticsCollector = statisticsCollector;
		this.associatedServer = associatedServer; 
		list = new LinkedList<Job>();
		priority = this.priority;
		maxSize = this.maxSize;
		size = 0;
	}
	
	public int getQueueMaxSize()
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
	
	public int size()
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
		statisticsCollector.updateQueueLength(priority, size(), associatedServer.getLocalTime());
	}
	
	public void addFirst(Job job)
	{
		// Assume that the size of the queue didn't reach its limit 
		list.addFirst(job);
		size++;
		statisticsCollector.updateQueueLength(priority, size(), associatedServer.getLocalTime());
	}
	
	public Job dequeue()
	{
		if(isEmpty())
		{
			list.clear();
			throw new NoSuchElementException();
		}
		while(list.peek().getState() == JobState.DISCARDED)
		{
			list.remove();
		}
		Job retJob = list.remove();
		size--;
		statisticsCollector.updateQueueLength(priority, size(), associatedServer.getLocalTime());
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
		while(list.peek().getState() == JobState.DISCARDED)
		{
			list.remove();
		}
		return list.peek();
	}

	public void alertJobDiscarded() {
		size--;
		statisticsCollector.updateQueueLength(priority, size(), associatedServer.getLocalTime());
	}
	
}
