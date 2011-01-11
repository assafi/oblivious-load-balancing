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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Assaf Israel
 *
 */
public class Configuration {

	/**
	 * @param xmlFilePath - The path for the XML configuration file 
	 * @throws IOException 
	 */
	public Configuration(String xmlFilePath) throws IOException {
		File xmlFile = new File(xmlFilePath);
		if (!xmlFile.exists()) {
			throw new FileNotFoundException("File " + xmlFilePath + " not found");
		}
		
		if (!xmlFile.canRead()) {
			throw new IOException("Can't read file " + xmlFile.getName());
		}
	}

}
