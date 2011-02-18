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

import java.io.IOException;
import java.util.Map;

/**
 * @author Assaf
 *
 */
public interface IDataWriter {

	/**
	 * @param labels The different data labels 
	 * @throws IOException 
	 */
	public void writeLabels(String[] labels) throws IOException;
	
	/**
	 * Writing the data to a file. the data will be written to the appropriate 
	 * label
	 * @param data Map between labels and data
	 * @throws IOException 
	 */
	public void writeData(Map<String, String> data) throws IOException;
	
	/**
	 * Closes the file stream
	 * @throws IOException 
	 */
	public void close() throws IOException;
}
