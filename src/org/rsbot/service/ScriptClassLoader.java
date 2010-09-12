package org.rsbot.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ScriptClassLoader extends ClassLoader {
	
	private String url;
	private Map<String, Class<?>> loaded = new HashMap<String, Class<?>>();
	
	public ScriptClassLoader(String url, ClassLoader parent) {
		super(parent);
		this.url = url;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			if (loaded.containsKey(name)) {
				return loaded.get(name);
			}
			URL myUrl = new URL(url + name.replace('.', '/') + ".class");
			URLConnection connection = myUrl.openConnection();
			InputStream input = connection.getInputStream();
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
		} catch (final MalformedURLException ignored) {
		} catch (final IOException ignored) {
		}
		return super.loadClass(name);
	}
}
