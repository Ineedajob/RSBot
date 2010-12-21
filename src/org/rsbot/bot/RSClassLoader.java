package org.rsbot.bot;

import org.rsbot.injector.Injector;

import java.awt.*;
import java.io.File;
import java.io.FilePermission;
import java.net.SocketPermission;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.Calendar;
import java.util.HashMap;
import java.util.PropertyPermission;

/**
 * @author Alex
 */
public final class RSClassLoader extends ClassLoader {

	private HashMap<String, byte[]> classes;
	private ProtectionDomain domain;
	private String signlink;

	public RSClassLoader(Injector injector) {
		try {
			CodeSource codeSource = new CodeSource(new URL("http://" + injector.generateTargetName() + ".com/"), (CodeSigner[]) null);
			domain = new ProtectionDomain(codeSource, getPermissions());
			classes = injector.getClasses();
			signlink = injector.findClass("Signlink").getClassName();
		} catch (final Exception ignored) {
		}
	}

	private Permissions getPermissions() {
		final Permissions ps = new Permissions();
		ps.add(new AWTPermission("accessEventQueue"));
		ps.add(new PropertyPermission("user.home", "read"));
		ps.add(new PropertyPermission("java.vendor", "read"));
		ps.add(new PropertyPermission("java.version", "read"));
		ps.add(new PropertyPermission("os.name", "read"));
		ps.add(new PropertyPermission("os.arch", "read"));
		ps.add(new PropertyPermission("os.version", "read"));
		ps.add(new SocketPermission("*", "connect,resolve"));//quick fix TODO FIXME
		String uDir = System.getProperty("user.home");
		if (uDir != null) {
			uDir += "/";
		} else {
			uDir = "~/";
		}
		final String[] dirs = {"c:/rscache/", "/rscache/", "c:/windows/", "c:/winnt/", "c:/", uDir, "/tmp/", "."};
		// If "." fails then System.getProperty("user.dir") should work
		final String[] rsDirs = {".jagex_cache_32", ".file_store_32"};
		for (String dir : dirs) {
			final File f = new File(dir);
			ps.add(new FilePermission(dir, "read"));
			if (!f.exists()) {
				continue;
			}
			dir = f.getPath();
			for (final String rsDir : rsDirs) {
				ps.add(new FilePermission(dir + File.separator + rsDir + File.separator + "-", "read"));
				ps.add(new FilePermission(dir + File.separator + rsDir + File.separator + "-", "write"));
			}
		}
		Calendar.getInstance();
		//TimeZone.getDefault();//Now the default is set they don't need permission
		//ps.add(new FilePermission())
		ps.setReadOnly();
		return ps;
	}

	@Override
	public final Class<?> loadClass(String name) throws ClassNotFoundException {
		if (name.equals("SignLink")) {
			name = signlink;
		}
		if (classes.containsKey(name)) {
			final byte buffer[] = classes.remove(name);
			return defineClass(name, buffer, 0, buffer.length, domain);
		}
		return super.loadClass(name);
	}

}
