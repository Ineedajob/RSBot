package org.rsbot.service;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Logger;

/**
 * @author Timer
 */
public class StatisticHandler {

	private static final Logger log = Logger.getLogger(StatisticHandler.class.getName());
	private static final String mainSite = "http://powerbot.org/report";
	private static final String reportURL = "/submit.php";
	private static final String VARIAB = "data";
	private static final String PARAM = "?";
	private static final String VALUE = "=";

	public static void ReportHackingAttempt(StackTraceElement[] stackTraceElements) throws Exception {
		String hackingString = "Hack-Attempt\n";
		for (StackTraceElement stackTraceElement : stackTraceElements) {
			hackingString += stackTraceElement.getMethodName() + " " + stackTraceElement.getClassName() + "\n";
		}
		String encoded = URLEncoder.encode(hackingString, "UTF-8");
		sendURL(new URL(mainSite + reportURL + PARAM + VARIAB + VALUE + encoded));
	}

	public static void ReportRandom(String name, String info) throws Exception {
		String randomString = "Random-Report\n[" + name + "] " + info;
		String encoded = URLEncoder.encode(randomString, "UTF-8");
		sendURL(new URL(mainSite + reportURL + PARAM + VARIAB + VALUE + encoded));
	}

	private static void sendURL(URL url) throws Exception {
		URLConnection urlConnection = url.openConnection();
		InputStream is = urlConnection.getInputStream();
		DataInputStream dis = new DataInputStream(is);
		byte[] buffer = new byte[urlConnection.getContentLength()];
		dis.readFully(buffer);
		dis.close();
		is.close();
		log.info(new String(buffer));
	}
}
