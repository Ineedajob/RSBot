package org.rsbot.service;

import org.rsbot.script.Script;
import org.rsbot.util.GlobalConfiguration;
import org.rsbot.util.IniParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Timer
 * @author Paris
 */
public class ScriptDeliveryNetwork implements ScriptSource {
	private static final Logger log = Logger.getLogger("ScriptDelivery");
	private static final ScriptDeliveryNetwork instance = new ScriptDeliveryNetwork();
	private String key;
	private final String defaultKey = "0000000000000000000000000000000000000000";

	private ScriptDeliveryNetwork() {
		key = defaultKey;
		load();
	}

	public static ScriptDeliveryNetwork getInstance() {
		return instance;
	}

	private void load() {
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

		if (!enabled) {
			log.warning("Service disabled: " + error);
		}

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
