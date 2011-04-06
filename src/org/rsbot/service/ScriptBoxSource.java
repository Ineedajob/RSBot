package org.rsbot.service;

import org.rsbot.script.Script;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Jacmob
 */
public class ScriptBoxSource implements ScriptSource {

	public static class Credentials {
		public String username;
		public String password;
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
