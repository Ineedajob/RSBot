package org.rsbot.bot;

import org.rsbot.util.GlobalConfiguration;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Timer
 */
class WebLoader {

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

}
