package org.rsbot.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Extractor implements Runnable {

	private static void saveTo(InputStream in, String outPath) {
		try {
			OutputStream out = new FileOutputStream(new File(outPath));

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (Exception ignored) {
		}
	}

	private final String[] args;

	public Extractor(String[] args) {
		this.args = args;
	}

	public void run() {
		ClassLoader loader = getClass().getClassLoader();
		String root = GlobalConfiguration.RUNNING_FROM_JAR ?
				GlobalConfiguration.Paths.Resources.ROOT + "/" :
				GlobalConfiguration.Paths.ROOT + File.separator;

		if (GlobalConfiguration.RUNNING_FROM_JAR) {
			try {
				if (GlobalConfiguration.getCurrentOperatingSystem() == GlobalConfiguration.OperatingSystem.WINDOWS) {
					Extractor.saveTo(loader.getResourceAsStream(root + GlobalConfiguration.Paths.COMPILE_SCRIPTS_BAT),
							GlobalConfiguration.Paths.getHomeDirectory() + File.separator + GlobalConfiguration.Paths.COMPILE_SCRIPTS_BAT);
					Extractor.saveTo(loader.getResourceAsStream(root + GlobalConfiguration.Paths.COMPILE_FIND_JDK),
							GlobalConfiguration.Paths.getHomeDirectory() + File.separator + GlobalConfiguration.Paths.COMPILE_FIND_JDK);
				} else {
					Extractor.saveTo(loader.getResourceAsStream(root + GlobalConfiguration.Paths.COMPILE_SCRIPTS_SH),
							GlobalConfiguration.Paths.getHomeDirectory() + File.separator + GlobalConfiguration.Paths.COMPILE_SCRIPTS_SH);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (args.length > 2) {
			if (args[0].toLowerCase().startsWith("delete")) {
				File jarOld = new File(args[1]);
				if (jarOld.exists()) {
					jarOld.delete();
				}
			}
		}
	}
}
