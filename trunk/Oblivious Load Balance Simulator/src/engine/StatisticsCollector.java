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
	
	private int serverID;
	
	private int totalJobNum = 0; // The total number of jobs (Completed and discarded)
	private int totalJobHQ = 0; // The total number of jobs in HQ (Completed and discarded)
	private int totalJobLQ = 0; // The total number of jobs in LQ (Completed and discarded)
	
	private int totalCompletedJobNum = 0; // The total number of completed jobs
	private int totalCompletedJobHQ = 0; // The total number of completed jobs in HQ 
	private int totalCompletedJobLQ = 0; // The total number of completed jobs in LQ
	
	private int totalDiscardedJobNum = 0; // The total number of discarded jobs
	private int totalDiscardedJobHQ = 0; // The total number of discarded jobs in HQ 
	private int totalDiscardedJobLQ = 0; // The total number of discarded jobs in LQ
	
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
	
	public static StatisticsCollector getInstance(){
		if (null == instance){
			instance = new StatisticsCollector();
		}
		
		return instance;
	}
	
	
	public void jobCompleted(Job job, Priority priority){
		
		totalJobNum++;
		totalCompletedJobNum++;
		totalJobsLength += job.getJobLength();
		averageJobLength = (averageJobLength*(totalCompletedJobNum-1) + job.getJobLength())/totalCompletedJobNum;
		
		switch(priority){
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
	}
	
	public void jobDiscarded(Job job, Priority priority){
		totalJobNum++;
		totalDiscardedJobNum++;
		
		switch(priority){
		case HIGH:
			totalJobHQ++;
			totalDiscardedJobHQ++;
			break;
			
		case LOW:
			totalJobLQ++;
			totalDiscardedJobLQ++;
			break;
		}
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
				xmlPrinter.characters(Integer.toString(totalJobHQ).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalJobHighPriority");
				
				xmlPrinter.startElement("", "", "TotalJobLowPriority", atts);
				xmlPrinter.characters(Integer.toString(totalJobLQ).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalJobLowPriority");
				
				xmlPrinter.startElement("", "", "TotalCompletedJobs", atts);
				xmlPrinter.characters(Integer.toString(totalCompletedJobNum).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalCompletedJobNum");
				
				xmlPrinter.startElement("", "", "TotalCompletedJobHighPriority", atts);
				xmlPrinter.characters(Integer.toString(totalCompletedJobHQ).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalCompletedJobHighPriority");
				
				xmlPrinter.startElement("", "", "TotalCompletedJobLowPriority", atts);
				xmlPrinter.characters(Integer.toString(totalCompletedJobLQ).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalCompletedJobLowPriority");
				
				xmlPrinter.startElement("", "", "TotalDiscardedJobs", atts);
				xmlPrinter.characters(Integer.toString(totalDiscardedJobNum).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalDiscardedJobs");
				
				xmlPrinter.startElement("", "", "TotalDiscardedJobHighPriority", atts);
				xmlPrinter.characters(Integer.toString(totalDiscardedJobHQ).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalDiscardedJobHighPriority");
				
				xmlPrinter.startElement("", "", "TotalDiscardedJobLowPriority", atts);
				xmlPrinter.characters(Integer.toString(totalDiscardedJobLQ).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalDiscardedJobLowPriority");
				
				xmlPrinter.startElement("", "", "TotalJobsLength", atts);
				xmlPrinter.characters(Integer.toString(totalJobsLength).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalJobsLength");
				
				xmlPrinter.startElement("", "", "TotalJobsLengthHighPriority", atts);
				xmlPrinter.characters(Integer.toString(totalJobsLengthHQ).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalJobsLengthHighPriority");
				
				xmlPrinter.startElement("", "", "TotalJobsLengthLowPriority", atts);
				xmlPrinter.characters(Integer.toString(totalJobsLengthLQ).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "TotalJobsLengthLowPriority");
				
				xmlPrinter.startElement("", "", "AverageJobLength", atts);
				xmlPrinter.characters(Double.toString(averageJobLength).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "AverageJobLength");
				
				xmlPrinter.startElement("", "", "AverageJobLengthHighPriority", atts);
				xmlPrinter.characters(Double.toString(averageJobLengthHQ).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "AverageJobLengthHighPriority");
				
				xmlPrinter.startElement("", "", "AverageJobLengthLowPriority", atts);
				xmlPrinter.characters(Double.toString(averageJobLengthLQ).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "AverageJobLengthLowPriority");
				
				xmlPrinter.startElement("", "", "HighPriorityQueueMaxLength", atts);
				xmlPrinter.characters(Integer.toString(hQMaxLength).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "HighPriorityQueueMaxLength");
				
				xmlPrinter.startElement("", "", "HighPriorityQueueAverageLength", atts);
				xmlPrinter.characters(Double.toString(hQAvgLength).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "HighPriorityQueueAverageLength");
				
				xmlPrinter.startElement("", "", "LowPriorityQueueMaxLength", atts);
				xmlPrinter.characters(Integer.toString(lQMaxLength).toCharArray(), 0, Integer.toString(totalJobNum).length());
				xmlPrinter.endElement("", "", "LowPriorityQueueMaxLength");
				
				xmlPrinter.startElement("", "", "LowPriorityQueueAverageLength", atts);
				xmlPrinter.characters(Double.toString(lQAvgLength).toCharArray(), 0, Integer.toString(totalJobNum).length());
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
		return totalDiscardedJobNum;
	}

	/**
	 * @return the totalDiscardedJobHQ
	 */
	public int getTotalDiscardedJobHQ() {
		return totalDiscardedJobHQ;
	}

	/**
	 * @return the totalDiscardedJobLQ
	 */
	public int getTotalDiscardedJobLQ() {
		return totalDiscardedJobLQ;
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
	
	

}
