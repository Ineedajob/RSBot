package org.rsbot.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ScriptClassLoader extends ClassLoader {
	
	private final String url;
	private final Map<String, Class<?>> loaded = new HashMap<String, Class<?>>();
	
	public ScriptClassLoader(final String url, final ClassLoader parent) {
		super(parent);
		this.url = url;
	}

	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
		try {
			if (loaded.containsKey(name)) {
				return loaded.get(name);
			}
			final URL myUrl = new URL(url + name.replace('.', '/') + ".class");
			final URLConnection connection = myUrl.openConnection();
			final InputStream input = connection.getInputStream();
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int data = input.read();

			while (data != -1) {
				buffer.write(data);
				data = input.read();
			}

			input.close();

			final byte[] classData = buffer.toByteArray();
			
			Class<?> clazz = defineClass(name, classData, 0, classData.length);
			loaded.put(name, clazz);
			return clazz;

		} catch (final MalformedURLException ignored) {
		} catch (final IOException ignored) {
		}
		return super.loadClass(name);
	}
}
