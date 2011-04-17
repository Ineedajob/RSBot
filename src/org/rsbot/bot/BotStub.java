package org.rsbot.bot;

import org.rsbot.util.GlobalConfiguration;

import javax.swing.*;
import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class BotStub implements AppletStub, AppletContext {

	private final Map<URL, WeakReference<Image>> IMAGE_CACHE = new HashMap<URL, WeakReference<Image>>();
	private final Map<String, InputStream> INPUT_CACHE = Collections
			.synchronizedMap(new HashMap<String, InputStream>(2));

	private final Logger log = Logger.getLogger(BotStub.class.getName());
	private final Applet applet;
	private final URL codeBase;
	private final URL documentBase;
	private boolean isActive;
	private final Map<String, String> parameters;

	public BotStub(final RSLoader applet) {
		this.applet = applet;
		Crawler c = new Crawler("http://www." + applet.getTargetName()
				                        + ".com/");
		parameters = c.getParameters();
		final String world_prefix = c.getWorldPrefix();
		try {
			codeBase = new URL("http://world" + world_prefix + "."
					                   + applet.getTargetName() + ".com");
			documentBase = new URL("http://world" + world_prefix + "."
					                       + applet.getTargetName() + ".com/m0");
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void appletResize(final int x, final int y) {
		final Dimension size = new Dimension(x, y);
		applet.setSize(size);
		applet.setPreferredSize(size);
	}

	public Applet getApplet(final String name) {
		final String thisName = parameters.get("name");
		if (thisName == null) {
			return null;
		}
		return thisName.equals(name) ? applet : null;
	}

	public AppletContext getAppletContext() {
		return this;
	}

	public Enumeration<Applet> getApplets() {
		final Vector<Applet> apps = new Vector<Applet>();
		apps.add(applet);
		return apps.elements();
	}

	public AudioClip getAudioClip(final URL url) {
		log.info("NOT YET IMPLEMENTED getAudioClip=" + url);
		return null;
	}

	public URL getCodeBase() {
		return codeBase;
	}

	public URL getDocumentBase() {
		return documentBase;
	}

	public Image getImage(final URL url) {
		synchronized (IMAGE_CACHE) {
			WeakReference<Image> ref = IMAGE_CACHE.get(url);
			Image img;
			if ((ref == null) || ((img = ref.get()) == null)) {
				img = Toolkit.getDefaultToolkit().createImage(url);
				ref = new WeakReference<Image>(img);
				IMAGE_CACHE.put(url, ref);
			}
			return img;
		}
	}

	public String getParameter(final String s) {
		final String parameter = parameters.get(s);
		if (s != null) {
			return parameter;
		}
		return "";
	}

	public InputStream getStream(final String key) {
		return INPUT_CACHE.get(key);
	}

	public Iterator<String> getStreamKeys() {
		return Collections.unmodifiableSet(INPUT_CACHE.keySet()).iterator();
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(final boolean isActive) {
		this.isActive = isActive;
	}

	public void setStream(final String key, final InputStream stream)
			throws IOException {
		INPUT_CACHE.put(key, stream);
	}

	public void showDocument(final URL url) {
		showDocument(url, "");
	}

	public void showDocument(final URL url, final String target) {
		if (url.toString().contains("outofdate")) {
			final String message = GlobalConfiguration.NAME
					+ " is currently outdated, please wait patiently for a new version.";
			log.severe(message);
			JOptionPane.showMessageDialog(null, message, "Outdated",
			                              JOptionPane.WARNING_MESSAGE);
			File versionFile = new File(
					GlobalConfiguration.Paths.getVersionCache());
			if (versionFile.exists() && !versionFile.delete()) {
				log.warning("Unable to clear cache.");
			}
		} else if (!target.equals("tbi")) {
			log.info("Attempting to show: " + url.toString() + " [" + target
					         + "]");
		}
	}

	public void showStatus(final String status) {
		log.info("Status: " + status);
	}
}
