package org.rsbot.service;

import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.util.GlobalFile;

import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Jacmob
 */
public class FileScriptSource implements ScriptSource {

	private final Logger log = Logger.getLogger(getClass().getSimpleName());

	private File file;

	public FileScriptSource(GlobalFile file) {
		this.file = file;
	}


	public List<ScriptDefinition> list() {
		LinkedList<ScriptDefinition> defs = new LinkedList<ScriptDefinition>();
		if (file == null || !file.isDirectory()) {
			return defs;
		}
		String url;
		try {
			url = file.toURI().toURL().toString();
		} catch (MalformedURLException ex) {
			return defs;
		}
		ScriptClassLoader ldr = new ScriptClassLoader(url, ScriptClassLoader.class.getClassLoader());
		for (File f : file.listFiles()) {
			if (f.getName().endsWith(".jar!")) {
        		try {
					for (File j : f.listFiles()) {
						load(new ScriptClassLoader(f.toURI().toURL().toString(),
							ScriptClassLoader.class.getClassLoader()), defs, j, "");
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			} else {
				load(ldr, defs, f, "");
			}
		}
		return defs;
	}

	public Script load(ScriptDefinition def) throws ServiceException {
		if (!(def instanceof FileScriptDefinition)) {
			throw new IllegalArgumentException("Invalid definition!");
		}
		FileScriptDefinition fsd = (FileScriptDefinition) def;
		try {
			return fsd.clazz.asSubclass(Script.class).newInstance();
		} catch (Exception ex) {
			throw new ServiceException(ex.getMessage());
		}
	}

	private void load(ScriptClassLoader loader, LinkedList<ScriptDefinition> scripts, File file, String prefix) {
    	if (file.isDirectory()) {
    		if (!file.getName().endsWith(".jar!") && !file.getName().startsWith(".")) {
				for (File f : file.listFiles()) {
					load(loader, scripts, f, prefix + file.getName() + ".");
	    		}
			}
    	} else {
    		String name = prefix + file.getName();
	        String ext = ".class";
	        if (name.endsWith(ext) && !name.startsWith(".") && !name.contains("!") && !name.contains("$")) {
	            try {
	                name = name.substring(0, name.length() - ext.length());
	                Class<?> clazz;
					try {
						clazz = loader.loadClass(name);
					} catch (Exception e) {
						log.warning(name + " is not a valid script and was ignored!");
						return;
					} catch (VerifyError e) {
						log.warning(name + " is not a valid script and was ignored!");
						return;
					}
	                if (clazz.isAnnotationPresent(ScriptManifest.class)) {
						FileScriptDefinition def = new FileScriptDefinition();
						ScriptManifest manifest = clazz.getAnnotation(ScriptManifest.class);
						def.id = 0;
						def.name = manifest.name();
						def.authors = manifest.authors();
						def.version = manifest.version();
						def.keywords = manifest.keywords();
						def.description = manifest.description();
						def.clazz = clazz;
						def.source = this;
	                    scripts.add(def);
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
    	}
    }

	private static class FileScriptDefinition extends ScriptDefinition {

		Class<?> clazz;

	}

}
