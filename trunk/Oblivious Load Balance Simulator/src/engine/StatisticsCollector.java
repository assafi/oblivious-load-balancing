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

import java.io.File;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import misc.XmlPrinter;

import engine.Server.Priority;

/**
 * @author Eli Nazarov
 *
 */
public class StatisticsCollector {
	
	private static StatisticsCollector instance;
	
	private int totalJobNum = 0; // The total number of jobs (Completed, preempted and rejected) both in HQ and LQ
	private int totalJobHQ = 0; // The total number of jobs in HQ (Completed, preempted and rejected)
	private int totalJobLQ = 0; // The total number of jobs in LQ (Completed, preempted and rejected)
	
	private int totalCompletedJobNum = 0; // The total number of completed jobs
	private int totalCompletedJobHQ = 0; // The total number of completed jobs in HQ 
	private int totalCompletedJobLQ = 0; // The total number of completed jobs in LQ
	
	private int totalPreemptedJobNum = 0; // The total number of preempted jobs due to sibling job completion
	private int totalPreemptedJobHQ = 0; // The total number of preempted jobs in HQ 
	private int totalPreemptedJobLQ = 0; // The total number of preempted jobs in LQ
	 
	
	private int totalRejectedJobNum = 0; // The total number of rejected jobs due to full queue
	private int totalRejectedJobHQ = 0; // The total number of rejected jobs in HQ 
	private int totalRejectedJobLQ = 0; // The total number of rejected jobs in LQ
	
	private int totalJobsLength = 0; // The total length of all completed jobs
	private int totalJobsLengthHQ = 0; // The total length of all completed jobs in HQ
	private int totalJobsLengthLQ = 0; // The total length of all completed jobs in LQ
	
	private double averageJobLength = 0; // The average length of all completed jobs 
	private double averageJobLengthHQ = 0; // The average length of all completed jobs in HQ
	private double averageJobLengthLQ = 0; // The average length of all completed jobs in LQ
	
	private int hQMaxLength = 0; //The maximum length of high priority queue;
	private double hQAvgLength = 0; //The average length of high priority queue;
	private int hQLengthUpdatesNum = 0;
	
	private int lQMaxLength = 0; //The maximum length of low priority queue;
	private double lQAvgLength = 0; //The average length of low priority queue;
	private int lQLengthUpdatesNum = 0;
	
	public StatisticsCollector(){
		
	}
	
	public static StatisticsCollector getGlobalCollector(){
		if (null == instance){
			instance = new StatisticsCollector();
		}
		
		return instance;
	}
	
	
	public void jobCompleted(Job job){
		
		totalJobNum++;
		totalCompletedJobNum++;
		totalJobsLength += job.getJobLength();
		averageJobLength = (averageJobLength*(totalCompletedJobNum-1) + job.getJobLength())/totalCompletedJobNum;
		
		switch(job.associatedQueue.getQueuePriority()){
		case HIGH:
			totalJobHQ++;
			totalCompletedJobHQ++;
			totalJobsLengthHQ += job.getJobLength();
			averageJobLengthHQ = (averageJobLengthHQ*(totalCompletedJobHQ-1) + job.getJobLength())/totalCompletedJobHQ;
			break;
			
		case LOW:
			totalJobLQ++;
			totalCompletedJobLQ++;
			totalJobsLengthLQ += job.getJobLength();
			averageJobLengthLQ = (averageJobLengthLQ*(totalCompletedJobLQ-1) + job.getJobLength())/totalCompletedJobLQ;
			
			break;
		}
		
		getGlobalCollector().jobCompleted(job);
	}
	
	public void jobRejected(Job job){
		totalJobNum++;
		totalRejectedJobNum++;
		
		switch(job.associatedQueue.getQueuePriority()){
		case HIGH:
			totalJobHQ++;
			totalRejectedJobHQ++;
			break;
			
		case LOW:
			totalJobLQ++;
			totalRejectedJobLQ++;
			break;
		}
		
		getGlobalCollector().jobRejected(job);
	}
	
	public void jobPreempted(Job job){
		totalJobNum++;
		totalPreemptedJobNum++;
		
		switch(job.associatedQueue.getQueuePriority()){
		case HIGH:
			totalJobHQ++;
			totalPreemptedJobHQ++;
			break;
			
		case LOW:
			totalJobLQ++;
			totalPreemptedJobLQ++;
			break;
		}
		
		getGlobalCollector().jobRejected(job);
	}
	
	public void updateQueueLength(Priority priority, int length){
		
		switch(priority){
		case HIGH:
			hQLengthUpdatesNum++;
			hQMaxLength = length > hQMaxLength ? length : hQMaxLength;
			hQAvgLength = (hQAvgLength*(hQLengthUpdatesNum-1) + length)/hQLengthUpdatesNum;
			break;
			
		case LOW:
			lQLengthUpdatesNum++;
			lQMaxLength = length > lQMaxLength ? length : lQMaxLength;
			lQAvgLength = (lQAvgLength*(lQLengthUpdatesNum-1) + length)/lQLengthUpdatesNum;
			break;
		}
		
		getGlobalCollector().updateQueueLength(priority, length);
	}
	
	public void exportXML(File xmlFile){
		XmlPrinter xmlPrinter= new XmlPrinter(xmlFile);
		AttributesImpl atts = new AttributesImpl();
		
			try {
				xmlPrinter.startDocument();
				
				xmlPrinter.startElement("", "", "Statistics", atts);
				
				xmlPrinter.startElement("", "", "TotalJobs", atts);
				xmlPrinter.characters(Integer.toString(totalJobNum).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalJobs");
								
				xmlPrinter.startElement("", "", "TotalJobHighPriority", atts);
				xmlPrinter.characters(Integer.toString(totalJobHQ).toCharArray(), 0, Integer.toString(totalJobHQ).length());
				xmlPrinter.endElement("", "", "TotalJobHighPriority");
				
				xmlPrinter.startElement("", "", "TotalJobLowPriority", atts);
				xmlPrinter.characters(Integer.toString(totalJobLQ).toCharArray(), 0, Integer.toString(totalJobLQ).length());
				xmlPrinter.endElement("", "", "TotalJobLowPriority");
				
				xmlPrinter.startElement("", "", "TotalCompletedJobs", atts);
				xmlPrinter.characters(Integer.toString(totalCompletedJobNum).toCharArray(), 0, Integer.toString(totalCompletedJobNum).length());
				xmlPrinter.endElement("", "", "TotalCompletedJobNum");
				
				xmlPrinter.startElement("", "", "TotalCompletedJobHighPriority", atts);
				xmlPrinter.characters(Integer.toString(totalCompletedJobHQ).toCharArray(), 0, Integer.toString(totalCompletedJobHQ).length());
				xmlPrinter.endElement("", "", "TotalCompletedJobHighPriority");
				
				xmlPrinter.startElement("", "", "TotalCompletedJobLowPriority", atts);
				xmlPrinter.characters(Integer.toString(totalCompletedJobLQ).toCharArray(), 0, Integer.toString(totalCompletedJobLQ).length());
				xmlPrinter.endElement("", "", "TotalCompletedJobLowPriority");
				
				xmlPrinter.startElement("", "", "TotalPreemptedJobs", atts);
				xmlPrinter.characters(Integer.toString(totalPreemptedJobNum).toCharArray(), 0, Integer.toString(totalPreemptedJobNum).length());
				xmlPrinter.endElement("", "", "TotalPreemptedJobs");
				
				xmlPrinter.startElement("", "", "PreemptedPercentage", atts);
				double preemptPer = totalPreemptedJobNum/totalJobNum;
				xmlPrinter.characters(Double.toString(preemptPer).toCharArray(), 0, Double.toString(preemptPer).length());
				xmlPrinter.endElement("", "", "PreemptedPercentage");
				
				xmlPrinter.startElement("", "", "TotalPreemptedJobHighPriority", atts);
				xmlPrinter.characters(Integer.toString(totalPreemptedJobHQ).toCharArray(), 0, Integer.toString(totalPreemptedJobHQ).length());
				xmlPrinter.endElement("", "", "TotalPreemptedJobHighPriority");
				
				xmlPrinter.startElement("", "", "TotalPreemptedJobLowPriority", atts);
				xmlPrinter.characters(Integer.toString(totalPreemptedJobLQ).toCharArray(), 0, Integer.toString(totalPreemptedJobLQ).length());
				xmlPrinter.endElement("", "", "TotalPreemptedJobLowPriority");
				
				xmlPrinter.startElement("", "", "TotalRejectedJobs", atts);
				xmlPrinter.characters(Integer.toString(totalRejectedJobNum).toCharArray(), 0, Integer.toString(totalRejectedJobNum).length());
				xmlPrinter.endElement("", "", "TotalRejectedJobs");
				
				xmlPrinter.startElement("", "", "RejectedPercentage", atts);
				double rejectedPer = totalRejectedJobNum/totalJobNum;
				xmlPrinter.characters(Double.toString(rejectedPer).toCharArray(), 0, Double.toString(rejectedPer).length());
				xmlPrinter.endElement("", "", "RejectedPercentage");
				
				xmlPrinter.startElement("", "", "TotalRejectedJobHighPriority", atts);
				xmlPrinter.characters(Integer.toString(totalRejectedJobHQ).toCharArray(), 0, Integer.toString(totalRejectedJobHQ).length());
				xmlPrinter.endElement("", "", "TotalRejectedJobHighPriority");
				
				xmlPrinter.startElement("", "", "TotalRejectedJobLowPriority", atts);
				xmlPrinter.characters(Integer.toString(totalRejectedJobLQ).toCharArray(), 0, Integer.toString(totalRejectedJobLQ).length());
				xmlPrinter.endElement("", "", "TotalRejectedJobLowPriority");
				
				xmlPrinter.startElement("", "", "TotalJobsLength", atts);
				xmlPrinter.characters(Integer.toString(totalJobsLength).toCharArray(), 0, Integer.toString(totalJobsLength).length());
				xmlPrinter.endElement("", "", "TotalJobsLength");
				
				xmlPrinter.startElement("", "", "TotalJobsLengthHighPriority", atts);
				xmlPrinter.characters(Integer.toString(totalJobsLengthHQ).toCharArray(), 0, Integer.toString(totalJobsLengthHQ).length());
				xmlPrinter.endElement("", "", "TotalJobsLengthHighPriority");
				
				xmlPrinter.startElement("", "", "TotalJobsLengthLowPriority", atts);
				xmlPrinter.characters(Integer.toString(totalJobsLengthLQ).toCharArray(), 0, Integer.toString(totalJobsLengthLQ).length());
				xmlPrinter.endElement("", "", "TotalJobsLengthLowPriority");
				
				xmlPrinter.startElement("", "", "AverageJobLength", atts);
				xmlPrinter.characters(Double.toString(averageJobLength).toCharArray(), 0, Double.toString(averageJobLength).length());
				xmlPrinter.endElement("", "", "AverageJobLength");
				
				xmlPrinter.startElement("", "", "AverageJobLengthHighPriority", atts);
				xmlPrinter.characters(Double.toString(averageJobLengthHQ).toCharArray(), 0, Double.toString(averageJobLengthHQ).length());
				xmlPrinter.endElement("", "", "AverageJobLengthHighPriority");
				
				xmlPrinter.startElement("", "", "AverageJobLengthLowPriority", atts);
				xmlPrinter.characters(Double.toString(averageJobLengthLQ).toCharArray(), 0, Double.toString(averageJobLengthLQ).length());
				xmlPrinter.endElement("", "", "AverageJobLengthLowPriority");
				
				xmlPrinter.startElement("", "", "HighPriorityQueueMaxLength", atts);
				xmlPrinter.characters(Integer.toString(hQMaxLength).toCharArray(), 0, Integer.toString(hQMaxLength).length());
				xmlPrinter.endElement("", "", "HighPriorityQueueMaxLength");
				
				xmlPrinter.startElement("", "", "HighPriorityQueueAverageLength", atts);
				xmlPrinter.characters(Double.toString(hQAvgLength).toCharArray(), 0, Double.toString(hQAvgLength).length());
				xmlPrinter.endElement("", "", "HighPriorityQueueAverageLength");
				
				xmlPrinter.startElement("", "", "LowPriorityQueueMaxLength", atts);
				xmlPrinter.characters(Integer.toString(lQMaxLength).toCharArray(), 0, Integer.toString(lQMaxLength).length());
				xmlPrinter.endElement("", "", "LowPriorityQueueMaxLength");
				
				xmlPrinter.startElement("", "", "LowPriorityQueueAverageLength", atts);
				xmlPrinter.characters(Double.toString(lQAvgLength).toCharArray(), 0, Double.toString(lQAvgLength).length());
				xmlPrinter.endElement("", "", "LowPriorityQueueAverageLength");
				
				xmlPrinter.endElement("", "", "Statistics");
				
				xmlPrinter.endDocument();
				
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}

	/**
	 * @return the totalJobNum
	 */
	public int getTotalJobNum() {
		return totalJobNum;
	}

	/**
	 * @return the totalJobHQ
	 */
	public int getTotalJobHQ() {
		return totalJobHQ;
	}

	/**
	 * @return the totalJobLQ
	 */
	public int getTotalJobLQ() {
		return totalJobLQ;
	}

	/**
	 * @return the totalCompletedJobNum
	 */
	public int getTotalCompletedJobNum() {
		return totalCompletedJobNum;
	}

	/**
	 * @return the totalCompletedJobHQ
	 */
	public int getTotalCompletedJobHQ() {
		return totalCompletedJobHQ;
	}

	/**
	 * @return the totalCompletedJobLQ
	 */
	public int getTotalCompletedJobLQ() {
		return totalCompletedJobLQ;
	}

	/**
	 * @return the totalDiscardedJobNum
	 */
	public int getTotalDiscardedJobNum() {
		return totalRejectedJobNum;
	}

	/**
	 * @return the totalDiscardedJobHQ
	 */
	public int getTotalDiscardedJobHQ() {
		return totalRejectedJobHQ;
	}

	/**
	 * @return the totalDiscardedJobLQ
	 */
	public int getTotalDiscardedJobLQ() {
		return totalRejectedJobLQ;
	}

	/**
	 * @return the totalJobsLength
	 */
	public int getTotalJobsLength() {
		return totalJobsLength;
	}

	/**
	 * @return the totalJobsLengthHQ
	 */
	public int getTotalJobsLengthHQ() {
		return totalJobsLengthHQ;
	}

	/**
	 * @return the totalJobsLengthLQ
	 */
	public int getTotalJobsLengthLQ() {
		return totalJobsLengthLQ;
	}

	/**
	 * @return the averageJobLength
	 */
	public double getAverageJobLength() {
		return averageJobLength;
	}

	/**
	 * @return the averageJobLengthHQ
	 */
	public double getAverageJobLengthHQ() {
		return averageJobLengthHQ;
	}

	/**
	 * @return the averageJobLengthLQ
	 */
	public double getAverageJobLengthLQ() {
		return averageJobLengthLQ;
	}

	/**
	 * @return the hQMaxLength
	 */
	public int gethQMaxLength() {
		return hQMaxLength;
	}

	/**
	 * @return the hQAvgLength
	 */
	public double gethQAvgLength() {
		return hQAvgLength;
	}

	/**
	 * @return the lQMaxLength
	 */
	public int getlQMaxLength() {
		return lQMaxLength;
	}

	/**
	 * @return the lQAvgLength
	 */
	public double getlQAvgLength() {
		return lQAvgLength;
	}
	
	public double getPreemptedPercentage(){
		return totalPreemptedJobNum/totalJobNum;
	}
	
	public double getRejectedPercentage(){
		return totalRejectedJobNum/totalJobNum;
	}
}
