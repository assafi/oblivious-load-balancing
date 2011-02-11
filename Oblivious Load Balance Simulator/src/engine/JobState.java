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

public enum JobState {
	INITIAL, IN_QUEUE, RUNNING, 
	COMPLETED {
		@Override
		public boolean isCompletionState() {
			return true;
		}
	}, 
	DROPPED_ON_SIBLING_COMPLETION {
		@Override
		public boolean isCompletionState() {
			return true;
		}
	},
	DROPPED_ON_FULL_QUEUE {
		@Override
		public boolean isCompletionState() {
			return true;
		}
	};
	public boolean isCompletionState() {
		return false;
	}
}