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

import org.apache.log4j.Logger;

import config.LogFactory;

/**
 * @author Assaf Israel
 * 
 */
public class JobCompletionState {
	final public JobState HPJobCompletionState;
	final public JobState LPJobCompletionState;
	
	private static Logger log = LogFactory.getLog(JobCompletionState.class);

	public JobCompletionState(JobState _HPJobCompletionState,
			JobState _LPJobCompletionState) {
		
		if (!_HPJobCompletionState.isCompletionState() && !_LPJobCompletionState.isCompletionState()) {
			String errStr = "Incorrect completion states: HP-" + _HPJobCompletionState.name() + " ; LP-" + _LPJobCompletionState.name();
			log.fatal(errStr);
			throw new RuntimeException(errStr);
		}
		
		HPJobCompletionState = _HPJobCompletionState;
		LPJobCompletionState = _LPJobCompletionState;
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof JobCompletionState)) {
			return false;
		}
		
		JobCompletionState jcs = (JobCompletionState)obj;
		return jcs.HPJobCompletionState == HPJobCompletionState
				&& jcs.LPJobCompletionState == LPJobCompletionState;
	}
	
	@Override
	public int hashCode() {
		return HPJobCompletionState.ordinal() + LPJobCompletionState.ordinal()*JobState.values().length;
	}
}
