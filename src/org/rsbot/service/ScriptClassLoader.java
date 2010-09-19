package org.rsbot.service;

import org.rsbot.util.GlobalFile;

import java.io.*;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class ScriptClassLoader extends ClassLoader {
	
	private File file;
	private Map<String, Class<?>> loaded = new HashMap<String, Class<?>>();
	
	public ScriptClassLoader(File file, ClassLoader parent) {
		super(parent);
		this.file = file;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			if (loaded.containsKey(name)) {
				return loaded.get(name);
			}
			GlobalFile f = new GlobalFile(file, name.replace('.', '/') + ".class");
			InputStream input = new FileInputStream(f);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int data = input.read();

			while (data != -1) {
				buffer.write(data);
				data = input.read();
			}
			input.close();

			byte[] classData = buffer.toByteArray();
			
			Class<?> clazz = defineClass(name, classData, 0, classData.length);
			loaded.put(name, clazz);

			return clazz;
		} catch (MalformedURLException ignored) {
		} catch (IOException ignored) {
		}
		return super.loadClass(name);
	}
}
