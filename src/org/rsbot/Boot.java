package org.rsbot;

import java.io.IOException;
import java.net.URLDecoder;

public class Boot {

	public static void main(String[] args) throws IOException {
		String location = Boot.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		location = URLDecoder.decode(location, "UTF-8").replaceAll("\\\\", "/");
		String os = System.getProperty("os.name").toLowerCase();
		String memory = "-Xmx512m";

		if (os.contains("windows")) {
			Runtime.getRuntime().exec("javaw " + memory + " -classpath \"" +
					location + "\" org.rsbot.Application");
		} else if (os.contains("mac")) {
			Runtime.getRuntime().exec(new String[]{"/bin/sh",
					"-c", "java " + memory + " -Xdock:name=\"RSBot\"" +
							" -Xdock:icon=resources/images/icon.png" +
							" -classpath \"" + location + "\" org.rsbot.Application"});
		} else {
			Runtime.getRuntime().exec(new String[]{"/bin/sh",
					"-c", "java " + memory + " -classpath \"" + location + "\" org.rsbot.Application"});
		}
	}

}