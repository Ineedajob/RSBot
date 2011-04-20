package org.rsbot.service;

import org.rsbot.script.Script;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Timer
 */
public class ScriptBoxSource implements ScriptSource {

	private final Logger log = Logger.getLogger(getClass().getSimpleName());

	public static class Credentials {
		public String key;
	}

	private Credentials credentials;

	public ScriptBoxSource(Credentials credentials) {
		this.credentials = credentials;
	}

	public List<ScriptDefinition> list() {
		return new LinkedList<ScriptDefinition>();
	}

	public Script load(ScriptDefinition def) throws ServiceException {
		return null;
	}
}
