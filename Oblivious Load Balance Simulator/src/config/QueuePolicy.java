/**
 * Oblivious Load Balance Simulator
 *
 * 236635 - On the Management and Efficiency of Cloud Based Services (W 2011)
 * CS Faculty, Technion - Institute of Technology 
 *
 * Authors: Assaf Israel, Eli Nazarov, Asi Bross 
 * 
 */
package config;

/**
 * @author Assaf Israel
 *
 */
public enum QueuePolicy {
	FINITE,
	INFINITE;

	/**
	 * @param typeStr Name of policy type
	 * @return corresponding policy type Enum 
	 * @throws Exception thrown in case of invalid policy type
	 */
	public static QueuePolicy process(String typeStr) throws Exception {
		for (QueuePolicy value : values()) {
			if (value.name().toLowerCase().equals(typeStr.toLowerCase())) {
				return value;
			}
		}
		throw new Exception("Invalid queue policy type");
	}
}
