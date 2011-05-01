package org.rsbot.loader;

import org.rsbot.loader.asm.ClassReader;
import org.rsbot.loader.script.ModScript;
import org.rsbot.loader.script.ParseException;
import org.rsbot.util.GlobalConfiguration;
import org.rsbot.util.HttpAgent;

import javax.swing.*;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

/**
 * @author Jacmob
 */
public class ClientLoader {

	private final Logger log = Logger.getLogger(ClientLoader.class.getName());

	private ModScript script;
	private Map<String, byte[]> classes;
	private int world = nextWorld();

	public void init(URL script, File cache) throws IOException, ParseException {
		byte[] data = null;
		FileInputStream fis = null;
		
		try{
			HttpAgent.download(script, cache);
			fis = new FileInputStream(cache);
			data = load(fis);
		} catch (IOException ioe) {
			log.severe("Could not load ModScript data");
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ioe1) { }
		}
		
		this.script = new ModScript(data);
	}

	public void load(File cache, File version_file) throws IOException {
		classes = new HashMap<String, byte[]>();
		int version = script.getVersion();
		String target = script.getAttribute("target");

		int cached_version = 0;
		if (cache.exists() && version_file.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(version_file));
			cached_version = Integer.parseInt(reader.readLine());
			reader.close();
		}

		if (version <= cached_version) {
			JarFile jar = new JarFile(cache);

			checkVersion(jar.getInputStream(jar.getJarEntry("client.class")));

			log.info("Processing client");
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.endsWith(".class")) {
					name = name.substring(0, name.length() - 6).replace('/', '.');
					classes.put(name, script.process(name, jar.getInputStream(entry)));
				}
			}
		} else {
			log.info("Downloading client: " + target);
			JarFile loader = getJar(target, true);
			JarFile client = getJar(target, false);

			List<String> replace = Arrays.asList(script.getAttribute("replace").split(" "));

			Enumeration<JarEntry> entries = client.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.endsWith(".class")) {
					name = name.substring(0, name.length() - 6).replace('/', '.');
					classes.put(name, load(client.getInputStream(entry)));
				}
			}

			entries = loader.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.endsWith(".class")) {
					name = name.substring(0, name.length() - 6).replace('/', '.');
					if (replace.contains(name)) {
						classes.put(name, load(loader.getInputStream(entry)));
					}
				}
			}

			FileOutputStream stream = new FileOutputStream(cache);
			JarOutputStream out = new JarOutputStream(stream);

			for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
				out.putNextEntry(new JarEntry(entry.getKey() + ".class"));
				out.write(entry.getValue());
			}

			out.close();
			stream.close();

			int client_version = 0;

			try {
				client_version = checkVersion(new ByteArrayInputStream(classes.get("client")));
			} finally {
				if (client_version != 0) {
					FileWriter writer = new FileWriter(GlobalConfiguration.Paths.getVersionCache());
					writer.write(Integer.toString(client_version));
					writer.close();
				}
			}

			log.info("Processing client");
			for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
				entry.setValue(script.process(entry.getKey(), entry.getValue()));
			}

		}
	}

	public Map<String, byte[]> getClasses() {
		return classes;
	}

	public String getTargetName() {
		return script.getAttribute("target");
	}

	private int checkVersion(InputStream in) throws IOException {
		ClassReader reader = new ClassReader(in);
		VersionVisitor vv = new VersionVisitor();
		reader.accept(vv, ClassReader.SKIP_FRAMES);
		if (vv.getVersion() != script.getVersion()) {
			JOptionPane.showMessageDialog(
					null,
					GlobalConfiguration.NAME + " does not yet support the latest version of this game client.\n" +
							"Our developers are currently ensuring that the bot can understand any new game content.\n" +
							"This process also ensures that the bot client remains undetectable.\n" +
							"This application will update itself when opened after the update is complete.\n" +
							"Try again in a few minutes or check the powerbot.org announcements for more information.\n" +
							"If this does not give you an exact time, refrain from asking as no one else will be able to.",
					"Outdated (" + script.getName() + ")",
					JOptionPane.INFORMATION_MESSAGE);
			throw new IOException("ModScript #" + script.getVersion() + " != #" + vv.getVersion());
		}
		return vv.getVersion();
	}

	private JarFile getJar(String target, boolean loader) {
		while (true) {
			try {
				String s = "jar:http://world" + world + "." + target + ".com/";
				if (loader) {
					s += "loader.jar!/";
				} else {
					s += target + ".jar!/";
				}
				URL url = new URL(s);
				JarURLConnection juc = (JarURLConnection) url.openConnection();
				juc.setConnectTimeout(5000);
				return juc.getJarFile();
			} catch (Exception ignored) {
				world = nextWorld();
			}
		}
	}

	private int nextWorld() {
		return 1 + new Random().nextInt(169);
	}

	private byte[] load(InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int n;
		while ((n = is.read(buffer)) != -1) {
			os.write(buffer, 0, n);
		}
		return os.toByteArray();
	}
}
