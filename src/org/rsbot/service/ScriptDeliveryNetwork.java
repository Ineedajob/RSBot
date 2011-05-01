package org.rsbot.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.rsbot.script.Script;
import org.rsbot.util.GlobalConfiguration;
import org.rsbot.util.HttpAgent;
import org.rsbot.util.IniParser;

/**
 * @author Paris
 */
public class ScriptDeliveryNetwork implements ScriptSource {
	private static final Logger log = Logger.getLogger("ScriptDelivery");
	private static final ScriptDeliveryNetwork instance = new ScriptDeliveryNetwork();
	private String key;
	private final String defaultKey = "0000000000000000000000000000000000000000";
	private final int version = 1;
	private URL base = null;
	
	private ScriptDeliveryNetwork() {
		key = defaultKey;
	}
	
	public void start() {
		if (load()) {
			try {
				init();
			} catch (Exception e) {
				e.printStackTrace();
				log.severe("Could not download scripts from the network");
			}
		}
	}

	public static ScriptDeliveryNetwork getInstance() {
		return instance;
	}

	private boolean load() {
		HashMap<String, String> keys = null;
		boolean enabled = true;
		String error = "could not load control file";

		try {
			URL source = new URL(GlobalConfiguration.Paths.URLs.SDN_CONTROL);
			keys = IniParser.deserialise(source).get(IniParser.emptySection);
		} catch (Exception e) {
			enabled = false;
		}

		if (keys == null || keys.isEmpty() || (keys.containsKey("enabled") && !parseBool(keys.get("enabled")))) {
			enabled = false;
		}

		if (keys.containsKey("error")) {
			error = keys.get("error");
		}
		
		if (keys.containsKey("version")) {
			final int remoteVersion = Integer.parseInt(keys.get("version"));
			if (version != remoteVersion) {
				enabled = false;
				error = "please update your version of the bot";
			}
		}
		
		if (keys.containsKey("url")) {
			try {
				base = new URL(keys.get("url").replace("%key", getKey()));
			} catch (MalformedURLException e) { }
		}
		
		if (base == null)
			enabled = false;

		if (!enabled) {
			log.warning("Service disabled: " + error);
		}
		
		return enabled;
	}
	
	private void init() throws MalformedURLException, IOException {	
		File cache = new File(GlobalConfiguration.Paths.getScriptsNetworkDirectory());

		if (!cache.exists()) {
			cache.mkdirs();
		}

		if (GlobalConfiguration.getCurrentOperatingSystem() == GlobalConfiguration.OperatingSystem.WINDOWS) {
			String path = "\"" + cache.getAbsolutePath() + "\"";
			try {
				Runtime.getRuntime().exec("attrib +H " + path);
			} catch (IOException e) {
			}
		}
		
		BufferedReader br, br1;
		String line, line1;
		HashMap<String, URL> scripts = new HashMap<String, URL>(64);
		
		final File manifest = getChachedFile("manifest.txt");
		final HttpURLConnection con = (HttpURLConnection) HttpAgent.download(base, manifest);
		base = con.getURL();
		br = new BufferedReader(new FileReader(manifest));
		
		while ((line = br.readLine()) != null) {
			final URL packUrl = new URL(base, line);
			long mod = 0;
			final File pack = getChachedFile("pack-" + getFileName(packUrl));
			if (pack.exists())
				mod = pack.lastModified();
			final HttpURLConnection packCon = (HttpURLConnection) HttpAgent.download(packUrl, pack);
			if (pack.lastModified() == mod)
				continue;
			br1 = new BufferedReader(new FileReader(pack));
			while ((line1 = br1.readLine()) != null) {
				final URL scriptUrl = new URL(packCon.getURL(), line1);
				scripts.put(getFileName(scriptUrl), scriptUrl);
			}
			br1.close();
		}
		
		br.close();
		
		if (!scripts.isEmpty())
			sync(scripts);
	}
	
	private void sync(final HashMap<String, URL> scripts) {
		int created = 0, deleted = 0, updated = 0, failed = 0;
		final File dir = new File(GlobalConfiguration.Paths.getScriptsNetworkDirectory());
		ArrayList<File> delete = new ArrayList<File>(64);

		for (final File f : dir.listFiles()) {
			if (f.getName().endsWith(".class"))
				delete.add(f);
		}
		
		for (final Entry<String, URL> key : scripts.entrySet()) {
			final File path = new File(dir, key.getKey());
			if (!path.getName().contains("$")) {
				if (delete.contains(path))
					updated++;
				else
					created++;
			}
			delete.remove(path);
			try {
				System.out.println("Downloading: " + path);
				HttpAgent.download(key.getValue(), path);
			} catch (Exception e) {
				if (!path.getName().contains("$"))
					failed++;
			}
		}
		
		for (final File f : delete) {
			if (!f.delete()) {
				f.deleteOnExit();
			}
			if (!f.getName().contains("$"))
				deleted++;
		}
		
		log.fine(String.format("Downloaded %1$d new scripts, updated %2$d and deleted %3$d (%4$d failed)", created, deleted, updated, failed));
	}
	
	private String getFileName(final URL url) {
		final String path = url.getPath();
		return path.substring(path.lastIndexOf('/') + 1);
	}
	
	private File getChachedFile(final String name) {
		return new File(GlobalConfiguration.Paths.getCacheDirectory(), "sdn-" + name); 
	}

	private boolean parseBool(String mode) {
		return mode.equals("1") || mode.equalsIgnoreCase("true") || mode.equalsIgnoreCase("yes");
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<ScriptDefinition> list() {
		return new LinkedList<ScriptDefinition>();
	}

	public Script load(ScriptDefinition def) throws ServiceException {
		return null;
	}
}
