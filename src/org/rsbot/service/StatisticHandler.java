package org.rsbot.service;

import java.util.logging.Logger;

/**
 * @author Timer
 */
public class StatisticHandler {

	private static final Logger log = Logger.getLogger(StatisticHandler.class
			                                                   .getName());


	public String reportHackingAttempt(StackTraceElement[] stackTraceElements) {
		//TODO this.
		for (StackTraceElement stackTraceElement : stackTraceElements) {
			log.info(stackTraceElement.getMethodName() + " " + stackTraceElement.getClassName());
		}
		return null;
	}
}
