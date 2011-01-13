/**
 * Job Scheduling - MAGMA 2010
 *
 * Author: Assaf Israel, IBM Research - Haifa
 * 
 * LogFactory.java
 *
 */
package config;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LogFactory {
	
	public static final String LOG_PROP_FILE = "log.properties";
	
	private static boolean configured = false;
	
	public synchronized static Logger getLog(Class<?> _class) {
		if (!configured) {
			PropertyConfigurator.configure(
					LogFactory.class.getResource(LOG_PROP_FILE));
			configured = true;
		}
		return Logger.getLogger(_class);
	}
}
