package lichenlzc.talking.server;

import org.apache.log4j.Logger;

public class Log {
	
	private static Logger log=Logger.getLogger(Log.class);
	
	public static synchronized void writeDebugLog(Object obj) {
		log.debug(obj);
	}
	
	public static synchronized void writeErrorLog(Object obj) {
		log.error(obj);
	}
	
	public static synchronized void writeInfoLog(Object obj) {
		log.info(obj);
	}
}
