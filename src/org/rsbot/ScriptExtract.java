package org.rsbot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.rsbot.util.GlobalConfiguration;

public class ScriptExtract extends Thread {

	private static void saveto(final InputStream in, final String outpath) {
		try {
			final OutputStream out = new FileOutputStream(new File(outpath));

			final byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (final Exception e) {
		}
	}

	private final String[] args;

	public ScriptExtract(final String[] args) {
		this.args = args;
	}

	@Override
	public void run() {
		final ClassLoader loader = getClass().getClassLoader();
		final String root = GlobalConfiguration.RUNNING_FROM_JAR ? GlobalConfiguration.Paths.Resources.ROOT + "/" : GlobalConfiguration.Paths.ROOT + File.separator;

		if (GlobalConfiguration.RUNNING_FROM_JAR) {
			try {
				if (GlobalConfiguration.getCurrentOperatingSystem() == GlobalConfiguration.OperatingSystem.WINDOWS) {
					ScriptExtract.saveto(loader.getResourceAsStream(root + GlobalConfiguration.Paths.COMPILE_SCRIPTS_BAT), GlobalConfiguration.Paths.getHomeDirectory() + File.separator + GlobalConfiguration.Paths.COMPILE_SCRIPTS_BAT);
					ScriptExtract.saveto(loader.getResourceAsStream(root + GlobalConfiguration.Paths.COMPILE_FINDJDK), GlobalConfiguration.Paths.getHomeDirectory() + File.separator + GlobalConfiguration.Paths.COMPILE_FINDJDK);
				} else {
					ScriptExtract.saveto(loader.getResourceAsStream(root + GlobalConfiguration.Paths.COMPILE_SCRIPTS_SH), GlobalConfiguration.Paths.getHomeDirectory() + File.separator + GlobalConfiguration.Paths.COMPILE_SCRIPTS_SH);
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		} else {
			if (args.length > 2) {
				if (args[0].toLowerCase().startsWith("delete")) {
					final File jarOld = new File(args[1]);
					if (jarOld.exists()) {
						jarOld.delete();
					}
				}
			}
		}
	}
}
