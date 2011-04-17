package org.rsbot.gui;

import org.rsbot.util.GlobalConfiguration;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;
import java.util.logging.Logger;

/**
 * @author Paris
 */
public class SplashAd extends JDialog implements MouseListener {
	private static Logger log = Logger.getLogger(SplashAd.class.getName());

	private static final long serialVersionUID = 1L;

	private static final String CACHED_IMAGE = "advert.png";
	private static final String CACHED_FORMAT = "png";

	private String link;
	private String image;
	private String text;
	private boolean popup = false;
	private int refresh = 60 * 60 * 24; // 1 day default
	private long updated = 0;

	public SplashAd(JFrame owner) {
		super(owner);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setUndecorated(true);
		setTitle("Advertisement");
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		if (!sync()) {
			dispose();
			return;
		}

		File file = new File(GlobalConfiguration.Paths.getCacheDirectory(), CACHED_IMAGE);

		if (file.exists()) {
			long cached = file.lastModified();
			if (cached < updated || cached < new Date().getTime() - refresh * 1000) {
				if (!file.delete()) {
					file.deleteOnExit();
				}
			}
		}

		if (!file.exists()) {
			try {
				BufferedImage img = ImageIO.read(new URL(image));
				ImageIO.write(img, CACHED_FORMAT, file);
				if (popup) {
					BotGUI.openURL(link);
				}
			} catch (Exception ignored) {
			}
		}

		if (text != null && text.length() != 0) {
			log.info(text);
		}

		try {
			BufferedImage img = ImageIO.read(file);
			setSize(img.getWidth(), img.getHeight());

			JLabel label = new JLabel();
			label.setIcon(new ImageIcon(img));
			add(label);
		} catch (IOException ignored) {
		}

		addMouseListener(this);
	}

	private boolean sync() {
		HashMap<String, String> keys = new HashMap<String, String>(7);
		InputStreamReader stream = null;
		BufferedReader reader = null;

		try {
			URLConnection connection = new URL(GlobalConfiguration.Paths.URLs.AD_INFO).openConnection();
			stream = new InputStreamReader(connection.getInputStream());
			reader = new BufferedReader(stream);
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				int z;
				z = line.indexOf('#');
				if (z != -1) {
					line = line.substring(0, z);
				}
				z = line.indexOf('=');
				if (z == -1) {
					continue;
				}
				String key = line.substring(0, z).trim(), value =
						z == line.length() ? "" : line.substring(z + 1).trim();
				keys.put(key, value);
			}
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}

		if (keys.isEmpty() || !keys.containsKey("enabled") || !parseBool(keys.get("enabled"))) {
			return false;
		}
		if (!keys.containsKey("link")) {
			return false;
		} else {
			link = keys.get("link");
		}
		if (!keys.containsKey("image")) {
			return false;
		} else {
			image = keys.get("image");
		}
		if (keys.containsKey("text")) {
			text = keys.get("text");
		}
		if (keys.containsKey("popup")) {
			popup = parseBool(keys.get("popup"));
		}
		if (keys.containsKey("refresh")) {
			refresh = Integer.parseInt(keys.get("refresh"));
		}
		if (keys.containsKey("updated")) {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
				formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
				updated = formatter.parse(keys.get("updated")).getTime();
			} catch (ParseException e) {
			}
		}

		return true;
	}

	private boolean parseBool(String mode) {
		return mode.equals("1") || mode.equalsIgnoreCase("true") || mode.equalsIgnoreCase("yes");
	}

	public void display() {
		setLocationRelativeTo(getOwner());
		setVisible(true);

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				dispose();
			}
		}, 5000);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		BotGUI.openURL(link);
		dispose();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

}
