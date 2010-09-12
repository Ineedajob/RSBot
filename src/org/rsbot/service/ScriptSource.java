package org.rsbot.service;

import org.rsbot.script.Script;

import java.util.List;

/**
 * @author Jacmob
 */
public interface ScriptSource {

	List<ScriptDefinition> list();

	Script load(ScriptDefinition def) throws ServiceException;

}
