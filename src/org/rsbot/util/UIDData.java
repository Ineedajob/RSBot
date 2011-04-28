package org.rsbot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;

public class UIDData {

	private static final String newline = System.getProperty("line.separator");
	private static final String separator = "#";

	private final HashMap<String, byte[]> uids = new HashMap<String, byte[]>();
	private String lastUsed = "";

	public UIDData() {
		File fUIDs = new File(GlobalConfiguration.Paths.getUIDsFile());
		if (!fUIDs.exists()) {
			return;
		}

		try {
			final BufferedReader in = new BufferedReader(new FileReader(fUIDs));
			String line;// Used to store the lines from the file
			while ((line = in.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}

				String[] data = line.split(separator, 2);
				uids.put(data[0], data[1].getBytes());
			}
		} catch (Exception ignored) {
		}
	}

	public String getLastUsed() {
		return this.lastUsed;
	}

	public byte[] getUID(String name) {
		if (name.equals("")) {
			name = "DEFAULT";
		}

		lastUsed = name;

		byte[] data = uids.get(name);
		if (data == null) {
			return new byte[0];
		}

		return data;
	}

	public void setUID(String name, byte[] uid) {
		if (name.equals("")) {
			name = "DEFAULT";
		}

		uids.put(name, uid);
	}

	public void save() {
		try {
			File fUIDs = new File(GlobalConfiguration.Paths.getUIDsFile());
			if (fUIDs.exists() || fUIDs.createNewFile()) {
				final FileOutputStream out = new FileOutputStream(fUIDs);
				for (String key : uids.keySet()) {
					out.write(key.getBytes());
					out.write(separator.getBytes());
					out.write(uids.get(key));

					out.write(newline.getBytes());
				}
				out.close();
			}
		} catch (Exception ignored) {
		}
	}
}