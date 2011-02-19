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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import misc.CsvLabel;
import misc.CsvWriter;
import misc.XmlPrinter;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import config.IConfiguration;
import config.QueuePolicy;
import engine.Server.Priority;

/**
 * @author Eli Nazarov, Assaf Israel
 * 
 */
public class StatisticsCollector {

	private static final String AVERAGE_WAITING_TIME_TAG = "AverageWaitingTime";
	private static final String STATISTICAL_MARGIN_TAG = "StatisticalMargin";
	private static final String JOBS_MEAN_LENGTH_TAG = "JobsMeanLength";
	private static final String TERMINATION_STATISTIC_TAG = "TerminationStatistic";
	private static final String QUEUE_SETUP_TAG = "QueueSetup";
	private static final String LOAD_TAG = "Load";
	private static final String DISTRIBUTION_FACTOR_TAG = "DistributionFactor";
	private static final String MEMORY_SIZE_TAG = "MemorySize";
	private static final String SERVER_COUNT_TAG = "ServerCount";
	private static final String JOBS_COUNT_TAG = "JobsCount";
	private static final String MAX_QUEUE_LENGTH_TAG = "MaxQueueLength";
	private static final String AVERAGE_QUEUE_LENGTH_TAG = "AverageQueueLength";
	private static final String LOW_PRIORITY_TAG = "LowPriority";
	private static final String HIGH_PRIORITY_TAG = "HighPriority";
	private static final String SUMMARY_TAG = "Summary";
	private static final String SIMULATION_TAG = "Simulation";
	
	public static final int PERCISION = 8;
	
	private static int serversCount = 0;

	private static StatisticsCollector instance = new StatisticsCollector();

	private Map<JobCompletionState, Long> stats = new HashMap<JobCompletionState, Long>();
	private static Map<JobCompletionState, Double> statsPerc = new HashMap<JobCompletionState, Double>();

	/*
	 * Map between the high priority queue length and the time the queue was in this length
	 */
	private HashMap<Long, Double> lengthTimeHPQueue = new HashMap<Long, Double>();
	private double lastHQUpdateTime = 0;
	private long lastUpdateHQLen = 0;

	/*
	 * Map between the low priority queue length and the time the queue was in this length
	 */
	private HashMap<Long, Double> lengthTimeLPQueue = new HashMap<Long, Double>(); 
	private double lastLQUpdateTime = 0;
	private long lastUpdateLQLen = 0;
	private static long currentExperimentIndex = 1;
	
	private static double totalHPJobsTimeInSystem = 0.0;
	private static double totalLPJobsTimeInSystem = 0.0;

	private static CsvWriter writer;
	
	public StatisticsCollector() {
	}

	public static StatisticsCollector getGlobalCollector() {
		return instance;
	}

	/**
	 * Logs the termination statistics according to the 
	 * termination states of the HP &amp LP jobs.
	 * @param job Only one job is needed to log the termination. 
	 */
	public void reportTermination(Job job) {

		if (Double.compare(job.getJobLength(),0) == 0 || job.ignore) { 
			return;
		}
		
		Job siblingJob = job.getMirrorJob();
		switch (job.associatedQueue.getQueuePriority()) {
		case HIGH:
			recordTerminationCause(job.getState(), siblingJob.getState());
			recordTimeInSystem(job,siblingJob);
			break;
		case LOW:
			recordTerminationCause(siblingJob.getState(), job.getState());
			recordTimeInSystem(siblingJob,job);
			break;
		}
	}

	/**
	 * @param hpJob
	 * @param lpJob
	 */
	private void recordTimeInSystem(Job hpJob, Job lpJob) {

		/*
		 * Not including execution time
		 */
		totalHPJobsTimeInSystem += hpJob.getExecutionStartTime() - hpJob.getCreationTime(); 
		totalLPJobsTimeInSystem += lpJob.getExecutionStartTime() - lpJob.getCreationTime();
	}

	/**
	 * @param HPCompState
	 *            HP job completion state
	 * @param LPCompState
	 *            LP job completion state
	 */
	private void recordTerminationCause(JobState HPCompState, JobState LPCompState) {
		
		JobCompletionState jcs = new JobCompletionState(HPCompState,
				LPCompState);
		if (!stats.containsKey(jcs)) {
			stats.put(jcs, (long)1);
			return;
		} else {
			stats.put(jcs, stats.remove(jcs) + 1);
		}
	}

	public void updateQueueLength(Priority priority, long length,
			double localTime, boolean ignore) {

		double lenTime = 0;
		
		switch (priority) {
		case HIGH:
			if (lengthTimeHPQueue.containsKey(lastUpdateHQLen)) {
				lenTime = lengthTimeHPQueue.get(lastUpdateHQLen);
			}

			if (!ignore) {
				lengthTimeHPQueue.put(lastUpdateHQLen, lenTime
						+ (localTime - lastHQUpdateTime));
			} else {
				
			}
			lastHQUpdateTime = localTime;
			lastUpdateHQLen = length;
			break;

		case LOW:
			if (lengthTimeLPQueue.containsKey(lastUpdateLQLen)) {
				lenTime = lengthTimeLPQueue.get(lastUpdateLQLen);
			}

			if (!ignore) {
				lengthTimeLPQueue.put(lastUpdateLQLen, lenTime
						+ (localTime - lastLQUpdateTime));
			}
			lastLQUpdateTime = localTime;
			lastUpdateLQLen = length;
			break;
		}

	}

	public void updateGlobalCollector() {

		updateGlobalQueueLengths();
		updateGlobalCompletionStates();
		
		serversCount++;
	}

	private void updateGlobalCompletionStates() {
		for (JobCompletionState jcs : stats.keySet()) {
			if (instance.stats.containsKey(jcs)) {
				instance.stats.put(jcs, instance.stats.get(jcs)
						+ stats.get(jcs));
				continue;
			}
			instance.stats.put(jcs, stats.get(jcs));
		}
	}

	private void updateGlobalQueueLengths() {
		for (long len : lengthTimeHPQueue.keySet()) {
			if (instance.lengthTimeHPQueue.containsKey(len)) {
				instance.lengthTimeHPQueue.put(len, instance.lengthTimeHPQueue
						.get(len)
						+ lengthTimeHPQueue.get(len));
			} else {
				instance.lengthTimeHPQueue.put(len, lengthTimeHPQueue.get(len));
			}
		}

		for (long len : lengthTimeLPQueue.keySet()) {
			if (instance.lengthTimeLPQueue.containsKey(len)) {
				instance.lengthTimeLPQueue.put(len, instance.lengthTimeLPQueue
						.get(len)
						+ lengthTimeLPQueue.get(len));
			} else {
				instance.lengthTimeLPQueue.put(len, lengthTimeLPQueue.get(len));
			}
		}
		
		instance.lastHQUpdateTime = lastHQUpdateTime;
		instance.lastLQUpdateTime = lastLQUpdateTime;
	}

	public void exportXML(File xmlFile, IConfiguration config) {
		
		XmlPrinter xmlPrinter = new XmlPrinter(xmlFile);
		AttributesImpl defaultAtts = new AttributesImpl();

		try {
			xmlPrinter.startDocument();
							
			xmlPrinter.startElement("", "", SIMULATION_TAG, defaultAtts);

			{
				xmlPrinter.startElement("", "", SUMMARY_TAG, defaultAtts);
				{
					xmlPrinter.startElement("", "", JOBS_COUNT_TAG, defaultAtts);
					char[] jCount = Long.toString(config.getNumJobs()).toCharArray();
					xmlPrinter.characters(jCount, 0, jCount.length);
					xmlPrinter.endElement("", "", JOBS_COUNT_TAG);
					
					xmlPrinter.startElement("", "", SERVER_COUNT_TAG, defaultAtts);
					char[] sCount = Integer.toString(config.getNumServers()).toCharArray();
					xmlPrinter.characters(sCount, 0, sCount.length);
					xmlPrinter.endElement("", "", SERVER_COUNT_TAG);
					
					xmlPrinter.startElement("", "", JOBS_MEAN_LENGTH_TAG, defaultAtts);
					char[] jml = (String.format("%." + PERCISION + "f", config.getJobMeanLength())).toCharArray();
					xmlPrinter.characters(jml, 0, jml.length);
					xmlPrinter.endElement("", "", JOBS_MEAN_LENGTH_TAG);
					
					xmlPrinter.startElement("", "", STATISTICAL_MARGIN_TAG, defaultAtts);
					char[] sm = (String.format("%." + PERCISION + "f", config.getStatisticalMargin())).toCharArray();
					xmlPrinter.characters(sm, 0, sm.length);
					xmlPrinter.endElement("", "", STATISTICAL_MARGIN_TAG);
					
					AttributesImpl policyAtts = new AttributesImpl(); 
					if (config.getPolicy().equals(QueuePolicy.FINITE)) {
						policyAtts.addAttribute("", "", "policy", "", QueuePolicy.FINITE.name());
						xmlPrinter.startElement("", "", QUEUE_SETUP_TAG, policyAtts);
						{
							xmlPrinter.startElement("", "", MEMORY_SIZE_TAG, defaultAtts);
							char[] mSize = Integer.toString(config.getMemorySize()).toCharArray();
							xmlPrinter.characters(mSize, 0, mSize.length);
							xmlPrinter.endElement("", "", MEMORY_SIZE_TAG);
							
							xmlPrinter.startElement("", "", DISTRIBUTION_FACTOR_TAG, defaultAtts);
							char[] dFactor = (String.format("%." + PERCISION + "f", config.getDistributionFactor())).toCharArray();
							xmlPrinter.characters(dFactor, 0, dFactor.length);
							xmlPrinter.endElement("", "", DISTRIBUTION_FACTOR_TAG);
						}
						xmlPrinter.endElement("", "", QUEUE_SETUP_TAG);
					} else {
						policyAtts.addAttribute("", "", "policy", "", QueuePolicy.INFINITE.name());
						xmlPrinter.startElement("", "", QUEUE_SETUP_TAG, policyAtts);
						xmlPrinter.endElement("", "", QUEUE_SETUP_TAG);
					}
					
					xmlPrinter.startElement("", "", LOAD_TAG, defaultAtts);
					char[] load = (String.format("%." + PERCISION + "f", config.getLoad())).toCharArray();
					xmlPrinter.characters(load, 0, load.length);
					xmlPrinter.endElement("", "", LOAD_TAG);
					
					for (JobCompletionState jcs : statsPerc.keySet()) {
						AttributesImpl terminationAtts = new AttributesImpl(); 
						terminationAtts.addAttribute("", "", "HPTerminationState", "", jcs.HPJobCompletionState.name());
						terminationAtts.addAttribute("", "", "LPTerminationState", "", jcs.LPJobCompletionState.name());
						xmlPrinter.startElement("", "", TERMINATION_STATISTIC_TAG, terminationAtts);
						char[] perc = (String.format("%." + PERCISION + "f", statsPerc.get(jcs))).toCharArray();
						xmlPrinter.characters(perc, 0, perc.length);
						xmlPrinter.endElement("", "", TERMINATION_STATISTIC_TAG);
					}
				}

				xmlPrinter.endElement("", "", SUMMARY_TAG);
	
				xmlPrinter.startElement("", "", HIGH_PRIORITY_TAG, defaultAtts);
				{
					xmlPrinter.startElement("", "", AVERAGE_QUEUE_LENGTH_TAG, defaultAtts);
					{
						char[] length = String.format("%." + PERCISION + "f", getHPQueueAvgLength())
						.toCharArray();
						xmlPrinter.characters(length, 0, length.length);
					}
					xmlPrinter.endElement("", "", AVERAGE_QUEUE_LENGTH_TAG);		
					
					xmlPrinter.startElement("", "", MAX_QUEUE_LENGTH_TAG, defaultAtts);
					{
						char[] max = Long.toString(getHPQueueMaxLength()).toCharArray();
						xmlPrinter.characters(max, 0, max.length);
					}
					xmlPrinter.endElement("","",MAX_QUEUE_LENGTH_TAG);
					
					xmlPrinter.startElement("", "", AVERAGE_WAITING_TIME_TAG, defaultAtts);
					{
						char[] awt = String.format("%." + PERCISION + "f", getAverageHPJobsWaitingTime(config)).
						toCharArray();
						xmlPrinter.characters(awt, 0, awt.length);
					}
					xmlPrinter.endElement("","",AVERAGE_WAITING_TIME_TAG);
				}
				xmlPrinter.endElement("", "", HIGH_PRIORITY_TAG);
							
				xmlPrinter.startElement("", "", LOW_PRIORITY_TAG, defaultAtts);
				{
					xmlPrinter.startElement("", "", AVERAGE_QUEUE_LENGTH_TAG, defaultAtts);
					{
						char[] length = String.format("%." + PERCISION + "f", getLPQueueAvgLength())
						.toCharArray();
						xmlPrinter.characters(length, 0, length.length);
					}
					xmlPrinter.endElement("", "", AVERAGE_QUEUE_LENGTH_TAG);		
					
					xmlPrinter.startElement("", "", MAX_QUEUE_LENGTH_TAG, defaultAtts);
					{
						char[] max = Long.toString(getLPQueueMaxLength()).toCharArray();
						xmlPrinter.characters(max, 0, max.length);
					}
					xmlPrinter.endElement("","",MAX_QUEUE_LENGTH_TAG);
					
					xmlPrinter.startElement("", "", AVERAGE_WAITING_TIME_TAG, defaultAtts);
					{
						char[] awt = String.format("%." + PERCISION + "f", getAverageLPJobsWaitingTime(config)).
						toCharArray();
						xmlPrinter.characters(awt, 0, awt.length);
					}
					xmlPrinter.endElement("","",AVERAGE_WAITING_TIME_TAG);
				}

				xmlPrinter.endElement("", "", LOW_PRIORITY_TAG);

			}
			xmlPrinter.endElement("", "", SIMULATION_TAG);
			
			xmlPrinter.endDocument();
			
		} catch (SAXException se) {
			throw new RuntimeException(se);
		}
					
	}

	public double getAverageLPJobsWaitingTime(IConfiguration config) {
		return averageWaitingTime(totalLPJobsTimeInSystem,config);
	}

	public double getAverageHPJobsWaitingTime(IConfiguration config) {
		return averageWaitingTime(totalHPJobsTimeInSystem,config);
	}
	
	/**
	 * @param totalJobsTimeInSystem
	 * @param config
	 * @return
	 */
	private double averageWaitingTime(double totalJobsTimeInSystem,
			IConfiguration config) {
		if (totalJobsTimeInSystem < 0) {
			System.err.println("Total jobs time in system is negative: " + totalJobsTimeInSystem);
		}
		long numJobs = (long)(config.getNumJobs() * (1 - 2*config.getStatisticalMargin()));
		return totalJobsTimeInSystem / numJobs;
	}

	/**
	 * @return the hQMaxLength
	 */
	public long getHPQueueMaxLength() {
		long maxLen = 0;
		for (long len : instance.lengthTimeHPQueue.keySet()) {
			if (maxLen < len)
				maxLen = len;
		}
		return maxLen;
	}

	/**
	 * This should be invoked after the simulation is over
	 * 
	 * @return the hQAvgLength
	 */
	public double getHPQueueAvgLength() {
		double avgLen = 0.0;
		double totalReportedTime = 0.0;
		
		for (double time : instance.lengthTimeHPQueue.values()) {
			totalReportedTime += time;
		}
		
		for (long len : instance.lengthTimeHPQueue.keySet()) {
			avgLen += (instance.lengthTimeHPQueue.get(len) / totalReportedTime) * len;
		}

		return avgLen;
	}

	/**
	 * @return the lQMaxLength
	 */
	public long getLPQueueMaxLength() {
		long maxLen = 0;
		for (long len : instance.lengthTimeLPQueue.keySet()) {
			if (maxLen < len)
				maxLen = len;
		}
		return maxLen;
	}

	/**
	 * @return the lQAvgLength
	 */
	public double getLPQueueAvgLength() {
		double avgLen = 0;
		double totalReportedTime = 0.0;
		
		for (double time : instance.lengthTimeLPQueue.values()) {
			totalReportedTime += time;
		}
		for (long len : instance.lengthTimeLPQueue.keySet()) {
			avgLen += (instance.lengthTimeLPQueue.get(len) / totalReportedTime) * len;
		}

		return avgLen;
	}

	/**
	 * prepares the final statistics of the simulation
	 */
	public void finalizeStats() {
		currentExperimentIndex++;
		long completedJobsCount = 0;
		for (long completionCount : stats.values()) {
			completedJobsCount += completionCount;
		}
		
		for (JobCompletionState jcs : stats.keySet()) {
			statsPerc.put(jcs, (stats.get(jcs) / (double)completedJobsCount));
		}
	}

	/**
	 * @param outputFile
	 * @param config
	 * @throws IOException 
	 */
	public void exportCSV(File outputFile, IConfiguration config) throws IOException {
		if (writer == null) {
			throw new RuntimeException("CSV writer not initialized");
		}
		Map<String,String> data = new HashMap<String, String>(CsvLabel.values().length + 5);
		for (CsvLabel label : CsvLabel.values()) {
			label.injectData(data,this,config);
		}
		
		injectJobCompletionData(data,JobState.DROPPED_ON_FULL_QUEUE,JobState.DROPPED_ON_FULL_QUEUE);
		injectJobCompletionData(data,JobState.COMPLETED,JobState.DROPPED_ON_SIBLING_COMPLETION);
		injectJobCompletionData(data,JobState.COMPLETED,JobState.DROPPED_ON_FULL_QUEUE);
		injectJobCompletionData(data,JobState.DROPPED_ON_FULL_QUEUE,JobState.COMPLETED);
		injectJobCompletionData(data,JobState.DROPPED_ON_SIBLING_COMPLETION,JobState.COMPLETED);
		
		writer.writeData(data);
	}

	private void injectJobCompletionData(Map<String, String> data, JobState hpJobCompletionState, JobState lpJobCompletionState) {
		JobCompletionState jcs = new JobCompletionState(hpJobCompletionState, lpJobCompletionState);
		if (statsPerc.containsKey(jcs)) {
			data.put(main.Main.hplpLabel(hpJobCompletionState,lpJobCompletionState), 
					String.format("%." + PERCISION + "f",statsPerc.get(new JobCompletionState(hpJobCompletionState,lpJobCompletionState))));
			return;
		}
		data.put(main.Main.hplpLabel(hpJobCompletionState,lpJobCompletionState), "0");
	}

	public static long getCurrentExperimentIndex() {
		return currentExperimentIndex;
	}

	/**
	 * @param writer
	 */
	public static void setWriter(CsvWriter _writer) {
		writer = _writer;
	}

	public static void close() throws IOException {
		writer.close();
	}

	/**
	 * Prepares the global StatisticsCollector for the next experiment.
	 */
	public static void reset() {
		serversCount = 0;
		statsPerc.clear();
		instance.lengthTimeHPQueue = new HashMap<Long, Double>();
		instance.lastHQUpdateTime = 0;
		instance.lastUpdateHQLen = 0;
		instance.lengthTimeLPQueue = new HashMap<Long, Double>(); 
		instance.lastLQUpdateTime = 0;
		instance.lastUpdateLQLen = 0;
		totalHPJobsTimeInSystem = 0.0;
		totalLPJobsTimeInSystem = 0.0;
	}
}
