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

import java.io.File;
import java.io.IOException;

/**
 * @author Assaf
 * 
 */
public interface IDataFileWriter extends IDataWriter {
	
	/**
	 * @param file
	 * @param encoding
	 * @throws IOException
	 */
	public void openFile(File file, String encoding) throws IOException;
	
	/**
	 * @return The current file extension that matches the IDataFileWriter files format
	 */
	public String getExtension();
	
}
