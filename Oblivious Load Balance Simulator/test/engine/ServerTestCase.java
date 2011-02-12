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

import org.junit.Before;
import org.junit.Test;

import config.IConfiguration;
import config.QueuePolicy;
import config.stubs.ConfigurationStub;

import engine.Server.Priority;


/**
 * @author Asi Bross
 *
 */
public class ServerTestCase {

	private static ConfigurationStub config;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		config = new ConfigurationStub();
		config.numJobs = 1000;
		config.numServers = 20;
		config.distrbutionFactor = 0.3;
		config.load = 0.8;
		config.memorySize = 100;
		config.policy = QueuePolicy.FINITE;
	}
	
	@Test
	public void test1()
	{
		Server.SetServersConfiguration((IConfiguration)config);
		Server serverA = new Server();
		Server serverB = new Server();
		
		Job job, jobCopy; 
		
		job = new Job(5.0, 0.0,false);
		jobCopy = job.clone();
		job.setMirrorJob(jobCopy);
		jobCopy.setMirrorJob(job);
		
		serverA.AddJob(job, Priority.HIGH);
		serverB.AddJob(jobCopy, Priority.LOW);
		
		job = new Job(10.0, 2.0,false);
		jobCopy = job.clone();
		job.setMirrorJob(jobCopy);
		jobCopy.setMirrorJob(job);
		
		serverA.AddJob(job, Priority.LOW);
		serverB.AddJob(jobCopy, Priority.HIGH);
		
		job = new Job(10.0, 3.0,false);
		jobCopy = job.clone();
		job.setMirrorJob(jobCopy);
		jobCopy.setMirrorJob(job);
		
		serverA.AddJob(job, Priority.HIGH);
		serverB.AddJob(jobCopy, Priority.LOW);
		
		job = new Job(0.0, 30.0,false);
		jobCopy = job.clone();
		job.setMirrorJob(jobCopy);
		jobCopy.setMirrorJob(job);
		
		serverA.AddJob(job, Priority.HIGH);
		serverB.AddJob(jobCopy, Priority.LOW);
		
	}
	
}
