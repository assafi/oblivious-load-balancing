/**
 * Oblivious Load Balance Simulator
 *
 * 236635 - On the Management and Efficiency of Cloud Based Services (W 2011)
 * CS Faculty, Technion - Institute of Technology 
 *
 * Authors: Assaf Israel, Eli Nazarov, Asi Bross 
 * 
 */
package misc;

import java.util.Map;

import config.IConfiguration;

import engine.StatisticsCollector;

/**
 * @author Assaf Israel
 * 
 */
public enum CsvLabel {

	EXPERIMENT {
		@Override
		public String simpleName() {
			return "Experiment ID";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), Long.toString(StatisticsCollector
					.getCurrentExperimentIndex()));
		}
	},
	SERVERS {
		@Override
		public String simpleName() {
			return "No. Servers";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), Integer.toString(config.getNumServers()));
		}
	},
	JOBS {
		@Override
		public String simpleName() {
			return "No. Jobs";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), Long.toString(config.getNumJobs()));
		}
	},
	LENGTH {
		@Override
		public String simpleName() {
			return "Job mean length";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), String.format("%."
					+ StatisticsCollector.PERCISION + "f",
					config.getJobMeanLength()));
		}
	},
	MARGIN {
		@Override
		public String simpleName() {
			return "Statistical margin";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), String.format("%."
					+ StatisticsCollector.PERCISION + "f",
					config.getStatisticalMargin()));
		}
	},
	POLICY {
		@Override
		public String simpleName() {
			return "Queue policy";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), config.getPolicy().name());
		}
	},
	MEMORY_SIZE {
		@Override
		public String simpleName() {
			return "Memory size";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), Integer.toString(config.getMemorySize()));
		}
	},
	DISTRIBUTION {
		@Override
		public String simpleName() {
			return "Distrbution factor";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), String.format("%."
					+ StatisticsCollector.PERCISION + "f",
					config.getDistributionFactor()));
		}
	},
	LOAD {
		@Override
		public String simpleName() {
			return "Load";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), String.format("%."
					+ StatisticsCollector.PERCISION + "f", config.getLoad()));
		}
	},
	LP_QUEUE_MAX_LENGTH {
		@Override
		public String simpleName() {
			return "LP Queue max length";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(),
					Long.toString(statisticsCollector.getLPQueueMaxLength()));
		}
	},
	LP_QUEUE_AVERAGE_LENGTH {
		@Override
		public String simpleName() {
			return "LP Queue average length";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), String.format("%."
					+ StatisticsCollector.PERCISION + "f",
					statisticsCollector.getLPQueueAvgLength()));
		}
	},
	LP_JOBS_AVERAGE_TIME_IN_SYSTEM {
		@Override
		public String simpleName() {
			return "LP Jobs average time in system";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), String.format("%."
					+ StatisticsCollector.PERCISION + "f",
					statisticsCollector.getAverageLPJobsWaitingTime(config)));
		}
	},
	AVERAGE_LP_TIME_TO_COMPLETION {
		@Override
		public String simpleName() {
			return "Average LP job time to completion";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), String.format("%."
					+ StatisticsCollector.PERCISION + "f",
					statisticsCollector.getAverageLPTimeToCompletion(config)));
		}
	},
	HP_QUEUE_MAX_LENGTH {
		@Override
		public String simpleName() {
			return "HP Queue max length";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(),
					Long.toString(statisticsCollector.getHPQueueMaxLength()));
		}
	},
	HP_QUEUE_AVERAGE_LENGTH {
		@Override
		public String simpleName() {
			return "HP Queue average length";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), String.format("%."
					+ StatisticsCollector.PERCISION + "f",
					statisticsCollector.getHPQueueAvgLength()));
		}
	},
	HP_JOBS_AVERAGE_TIME_IN_SYSTEM {
		@Override
		public String simpleName() {
			return "HP Jobs average time in system";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), String.format("%."
					+ StatisticsCollector.PERCISION + "f",
					statisticsCollector.getAverageHPJobsWaitingTime(config)));
		}
	},
	AVERAGE_HP_TIME_TO_COMPLETION {
		@Override
		public String simpleName() {
			return "Average HP job time to completion";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), String.format("%."
					+ StatisticsCollector.PERCISION + "f",
					statisticsCollector.getAverageHPTimeToCompletion(config)));
		}
	},
	AVERAGE_TIME_TO_COMPLETION {
		@Override
		public String simpleName() {
			return "Average time to completion";
		}

		@Override
		public void injectData(Map<String, String> data,
				StatisticsCollector statisticsCollector, IConfiguration config) {
			data.put(simpleName(), String.format("%."
					+ StatisticsCollector.PERCISION + "f",
					statisticsCollector.getAverageTimeToCompletion(config)));
		}
	},
	;
	public abstract String simpleName();

	/**
	 * @param data
	 *            Data map to inject data
	 * @param statisticsCollector
	 *            Data will be extracted from here according to the label
	 * @param config
	 *            Data will be extracted from here according to the label
	 */
	public abstract void injectData(Map<String, String> data,
			StatisticsCollector statisticsCollector, IConfiguration config);
}
