/**
 * Oblivious Load Balance Simulator
 *
 * 236635 - On the Management and Efficiency of Cloud Based Services (W 2011)
 * CS Faculty, Technion - Institute of Technology 
 *
 * Authors: Assaf Israel, Eli Nazarov, Asi Bross 
 * 
 */
package exceptions;

import engine.Job;

/**
 * @author Asi Bross
 *
 */
public class QueueIsFullException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7937537264384313504L;
	private Job discardedJob;
	/**
	 * 
	 */
	public QueueIsFullException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public QueueIsFullException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public QueueIsFullException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public QueueIsFullException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param job
	 */
	public QueueIsFullException(Job job) {
		discardedJob = job;
	}
	
	public Job getDiscardedJob()
	{
		return discardedJob;
	}

}
