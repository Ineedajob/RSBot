package org.rsbot.service;

import java.util.LinkedList;
import java.util.List;

import org.rsbot.script.Script;

/**
 * @author Timer
 * @author Paris
 */
public class ScriptDeliveryNetwork implements ScriptSource {
	private static ScriptDeliveryNetwork instance = new ScriptDeliveryNetwork();
	private String key;
	private final String defaultKey = "0000000000000000000000000000000000000000";
	
	private ScriptDeliveryNetwork() {
		key = defaultKey;
	}
	
	public static ScriptDeliveryNetwork getInstance() {
		return instance;
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
