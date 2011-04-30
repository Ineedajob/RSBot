package org.rsbot;

import java.io.IOException;
import java.net.URLDecoder;

import org.rsbot.util.GlobalConfiguration;

/**
 * @author Paris
 */
public class Boot {
	public static void main(String[] args) throws IOException {
		String location = Boot.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		location = URLDecoder.decode(location, "UTF-8").replaceAll("\\\\", "/");
		final String app = Application.class.getCanonicalName();
		final String flags = "-Xmx1024m -Dsun.java2d.d3d=false";
		boolean sh = true;
		final char q = '"', s = ' ';
		StringBuilder param = new StringBuilder(64);
		
		switch (GlobalConfiguration.getCurrentOperatingSystem()) {
		case WINDOWS:
			sh = false;
			param.append("javaw");
			param.append(s);
			param.append(flags);
			break;
		case MAC:
			param.append("java");
			param.append(s);
			param.append(flags);
			param.append(s);
			param.append("-Xdock:name=");
			param.append(q);
			param.append(GlobalConfiguration.NAME);
			param.append(q);
			param.append(s);
			param.append("-Xdock:icon=");
			param.append(q);
			param.append(GlobalConfiguration.Paths.Resources.ICON);
			param.append(q);
			break;
		default:
			param.append("java");
			param.append(s);
			param.append(flags);
			break;
		}

		param.append(s);
		param.append("-classpath");
		param.append(s);
		param.append(q);
		param.append(location);
		param.append(q);
		param.append(s);
		param.append(app);
		
		final Runtime run = Runtime.getRuntime();
		
		if (sh) {
			run.exec(new String[] { "/bin/sh", "-c", param.toString() } );
		} else {
			run.exec(param.toString());
		}
	}
}
