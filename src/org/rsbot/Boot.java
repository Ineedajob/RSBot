package org.rsbot;

import java.io.IOException;
import java.net.URLDecoder;

public class Boot {
	
	public static void main(String[] args) throws IOException {
		String location = Boot.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		location = URLDecoder.decode(location, "UTF-8").replaceAll("\\\\", "/");
		String os = System.getProperty("os.name").toLowerCase();
		
		if (os.contains("windows")) {
			Runtime.getRuntime().exec("javaw -Xmx512m -classpath \"" +
					location + "\" org.rsbot.Application");
		} else {
			Runtime.getRuntime().exec(new String[] { "/bin/sh",
					"-c", "java -Xmx512m -classpath \"" +
					location + "\" org.rsbot.Application"});
		}
	}
	
}