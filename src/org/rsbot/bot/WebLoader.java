package org.rsbot.bot;

import org.rsbot.util.GlobalConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * @author Timer
 * @author Paris
 */
class WebLoader {
	private final Logger log = Logger.getLogger(WebLoader.class.getName());

	public boolean load() {
		try {
			File webFile = new File(GlobalConfiguration.Paths.getWebCache());
			download(webFile, new URL(GlobalConfiguration.Paths.URLs.WEB));
			return webFile.exists() && webFile.canRead() && webFile.canWrite();
		} catch (Exception ignored) {
		}
		return false;
	}

	private void download(File file, URL url) {
		try {
			URLConnection uc = url.openConnection();
			uc.setConnectTimeout(10000);
			DataInputStream di = new DataInputStream(uc.getInputStream());
			byte[] buffer = new byte[uc.getContentLength()];
			di.readFully(buffer);
			di.close();
			buffer = ungzip(buffer);
			if (buffer.length == 0) {
				log.warning("Could not retrieve web matrix");
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			if (file.exists() && (!file.canRead() || file.canWrite())) {
				file.setReadable(true);
				file.setWritable(true);
			}
			if (file.exists() && file.canRead() && file.canWrite()) {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(buffer);
				fos.flush();
				fos.close();
			}
		} catch (Exception ignored) {
		}
	}

	/*
	 * Ungzips a binary buffer if it is gzipped.
	 */
	private byte[] ungzip(byte[] data) {
		if (data.length < 2) {
			return data;
		}

		int header = (data[0] | data[1] << 8) ^ 0xffff0000;
		if (header != GZIPInputStream.GZIP_MAGIC) {
			return data;
		}

		try {
			ByteArrayInputStream b = new ByteArrayInputStream(data);
			GZIPInputStream gzin = new GZIPInputStream(b);
			ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
			for (int c = gzin.read(); c != -1; c = gzin.read()) {
				out.write(c);
			}
			return out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return data;
		}
	}
}
