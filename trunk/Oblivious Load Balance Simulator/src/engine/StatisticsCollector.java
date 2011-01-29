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
import java.util.HashMap;

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
	
	private HashMap<Integer, Double> hQLength = new HashMap<Integer, Double>(); // Map between the high priority queue length and the time the queue was in this length    
	private double lastHQUpdateTime = 0;
	private int lastUpdateHQLen = 0;
	
	private HashMap<Integer, Double> lQLength = new HashMap<Integer, Double>(); // Map between the low priority queue length and the time the queue was in this length
	private double lastLQUpdateTime = 0;
	private int lastUpdateLQLen = 0;
	
	private double totalRunTime = 0;
	
	public StatisticsCollector(){
		
	}
	
	public static StatisticsCollector getGlobalCollector(){
		if (null == instance){
			instance = new StatisticsCollector();
		}
		
		return instance;
	}
	
	
	public void jobCompleted(Job job){
		
		if(job.getJobLength() == 0){ // Job that states  that the run is over
			return;
		}
		
		totalJobNum++;
		totalCompletedJobNum++;
		totalJobsLength += job.getJobLength();
		
		switch(job.associatedQueue.getQueuePriority()){
		case HIGH:
			totalJobHQ++;
			totalCompletedJobHQ++;
			totalJobsLengthHQ += job.getJobLength();
			break;
			
		case LOW:
			totalJobLQ++;
			totalCompletedJobLQ++;
			totalJobsLengthLQ += job.getJobLength();
			break;
		}
	}
	
	public void jobRejected(Job job){
		if(job.getJobLength() == 0)
			return;
		
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
	}
	
	public void jobPreempted(Job job){
		if(job.getJobLength() == 0)
			return;
		
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
	}
	
	public void updateQueueLength(Priority priority, int length, double localTime){
		
		double lenTime = 0;
		switch(priority){
		case HIGH:
			if(hQLength.containsKey(lastUpdateHQLen)){
				lenTime = hQLength.get(lastUpdateHQLen);
				hQLength.remove(lastUpdateHQLen);
			}
			
			hQLength.put(lastUpdateHQLen, lenTime + (lastHQUpdateTime - localTime));
			lastHQUpdateTime = localTime;			
			break;
			
		case LOW:
			if(lQLength.containsKey(lastUpdateLQLen)){
				lenTime = lQLength.get(lastUpdateLQLen);
				lQLength.remove(lastUpdateLQLen);
			}
			
			lQLength.put(lastUpdateLQLen, lenTime + (lastLQUpdateTime - localTime));
			lastLQUpdateTime = localTime;
			break;
		}
	}
	
	public void endCollection(double time){
		totalRunTime = time;
	}
	
	public void updateGlobalCollector(){
		instance.totalJobNum += totalJobNum;
		instance.totalJobHQ += totalJobHQ;
		instance.totalJobLQ += totalJobLQ;
		
		instance.totalCompletedJobNum += totalCompletedJobNum; 
		instance.totalCompletedJobHQ += totalCompletedJobHQ; 
		instance.totalCompletedJobLQ += totalCompletedJobLQ; 

		instance.totalPreemptedJobNum += totalPreemptedJobNum; 
		instance.totalPreemptedJobHQ += totalPreemptedJobHQ;  
		instance.totalPreemptedJobLQ += totalPreemptedJobLQ; 


		instance.totalRejectedJobNum += totalRejectedJobNum; 
		instance.totalRejectedJobHQ += totalRejectedJobHQ; 
		instance.totalRejectedJobLQ += totalRejectedJobLQ;  

		instance.totalJobsLength += totalJobsLength;  
		instance.totalJobsLengthHQ += totalJobsLengthHQ;  
		instance.totalJobsLengthLQ += totalJobsLengthLQ;  

		for (int len : hQLength.keySet()) {
			if (instance.hQLength.containsKey(len)){
				double tmp = instance.hQLength.get(len);
				instance.hQLength.remove(len);
				instance.hQLength.put(len, tmp+hQLength.get(len));
			}
			else{
				instance.hQLength.put(len, hQLength.get(len));
			}
		}
		
		for (int len : lQLength.keySet()) {
			if (instance.lQLength.containsKey(len)){
				double tmp = instance.lQLength.get(len);
				instance.lQLength.remove(len);
				instance.lQLength.put(len, tmp+lQLength.get(len));
			}
			else{
				instance.lQLength.put(len, lQLength.get(len));
			}
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
				double preemptPer = totalJobNum == 0 ? 0 : totalPreemptedJobNum/totalJobNum;
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
				double rejectedPer = totalJobNum == 0 ? 0 : totalRejectedJobNum/totalJobNum;
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
				double averageJobLength = getAverageJobLength();
				xmlPrinter.characters(Double.toString(averageJobLength).toCharArray(), 0, Double.toString(averageJobLength).length());
				xmlPrinter.endElement("", "", "AverageJobLength");
				
				xmlPrinter.startElement("", "", "AverageJobLengthHighPriority", atts);
				double averageJobLengthHQ = getAverageJobLengthHQ();
				xmlPrinter.characters(Double.toString(averageJobLengthHQ).toCharArray(), 0, Double.toString(averageJobLengthHQ).length());
				xmlPrinter.endElement("", "", "AverageJobLengthHighPriority");
				
				xmlPrinter.startElement("", "", "AverageJobLengthLowPriority", atts);
				double averageJobLengthLQ = getAverageJobLengthLQ();
				xmlPrinter.characters(Double.toString(averageJobLengthLQ).toCharArray(), 0, Double.toString(averageJobLengthLQ).length());
				xmlPrinter.endElement("", "", "AverageJobLengthLowPriority");
				
				xmlPrinter.startElement("", "", "HighPriorityQueueMaxLength", atts);
				int maxHQLen = gethQMaxLength();
				xmlPrinter.characters(Integer.toString(maxHQLen).toCharArray(), 0, Integer.toString(maxHQLen).length());
				xmlPrinter.endElement("", "", "HighPriorityQueueMaxLength");
				
				xmlPrinter.startElement("", "", "HighPriorityQueueAverageLength", atts);
				double avgHQLen = gethQAvgLength();
				xmlPrinter.characters(Double.toString(avgHQLen).toCharArray(), 0, Double.toString(avgHQLen).length());
				xmlPrinter.endElement("", "", "HighPriorityQueueAverageLength");
				
				xmlPrinter.startElement("", "", "LowPriorityQueueMaxLength", atts);
				int maxLQLen = getlQMaxLength();
				xmlPrinter.characters(Integer.toString(maxLQLen).toCharArray(), 0, Integer.toString(maxLQLen).length());
				xmlPrinter.endElement("", "", "LowPriorityQueueMaxLength");
				
				xmlPrinter.startElement("", "", "LowPriorityQueueAverageLength", atts);
				double avgLQLen = getlQAvgLength();
				xmlPrinter.characters(Double.toString(avgLQLen).toCharArray(), 0, Double.toString(avgLQLen).length());
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
		return totalJobsLength/totalJobNum;
	}

	/**
	 * @return the averageJobLengthHQ
	 */
	public double getAverageJobLengthHQ() {
		return totalJobsLengthHQ/totalJobNum;
	}

	/**
	 * @return the averageJobLengthLQ
	 */
	public double getAverageJobLengthLQ() {
		return totalJobsLengthLQ/totalJobNum;
	}

	/**
	 * @return the hQMaxLength
	 */
	public int gethQMaxLength() {
		int maxLen = -1;
		for (int len : hQLength.keySet()) {
			if (maxLen < len)
				maxLen = len;
		}
		return maxLen;
	}

	/**
	 * This should be invoked after the simulation is over
	 * @return the hQAvgLength
	 */
	public double gethQAvgLength() {
		double avgLen = 0;
		for (int len : hQLength.keySet()) {
			avgLen += (hQLength.get(len)/totalRunTime)*len;
		}
		
		return avgLen;
	}

	/**
	 * @return the lQMaxLength
	 */
	public int getlQMaxLength() {
		int maxLen = -1;
		for (int len : lQLength.keySet()) {
			if (maxLen < len)
				maxLen = len;
		}
		return maxLen;
	}

	/**
	 * @return the lQAvgLength
	 */
	public double getlQAvgLength() {
		double avgLen = 0;
		for (int len : lQLength.keySet()) {
			avgLen += (lQLength.get(len)/totalRunTime)*len;
		}
		
		return avgLen;
	}
	
	public double getPreemptedPercentage(){
		return totalPreemptedJobNum/totalJobNum;
	}
	
	public double getRejectedPercentage(){
		return totalRejectedJobNum/totalJobNum;
	}
}
