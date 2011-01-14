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
import java.util.Queue;

import engine.Server.Priority;
import exceptions.QueueIsFullException;

/**
 * @author Asi Bross
 *
 */
public class JobsQueue {

	private Queue<Job> queue;
	private Priority priority;
	private int size;
	private int maxSize;
	
	/**
	 * 
	 */
	public JobsQueue(Priority priority, int maxSize) {
		queue = new LinkedList<Job>();
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
		if(size() == maxSize)
		{
			throw new QueueIsFullException(job);
		}
		queue.add(job);
		size++;
	}
	
	public Job dequeue()
	{
		if(isEmpty())
		{
			queue.clear();
			throw new NoSuchElementException();
		}
		while(queue.peek().wasDiscarded())
		{
			queue.remove();
		}
		Job retJob = queue.remove();
		size--;
		if(isEmpty())
		{
			queue.clear();
		}
		return retJob;
	}

	/**
	 * 
	 */
	public void alertJobDiscarded() {
		size--;
	}
	
}
