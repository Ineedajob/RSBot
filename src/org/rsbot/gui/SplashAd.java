package org.rsbot.gui;

import org.rsbot.util.GlobalConfiguration;
import org.rsbot.util.HttpAgent;
import org.rsbot.util.IniParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
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
	private int display = 5000;
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
		HashMap<String, String> keys = null;

		try {
			URL source = new URL(GlobalConfiguration.Paths.URLs.AD_INFO);
			final File cache = new File(GlobalConfiguration.Paths.getCacheDirectory(), "ads.txt");
			HttpAgent.download(source, cache);
			BufferedReader reader = new BufferedReader(new FileReader(cache));
			keys = IniParser.deserialise(reader).get(IniParser.emptySection);
			reader.close();
		} catch (Exception e) {
			return false;
		}

		if (keys == null || keys.isEmpty() || !keys.containsKey("enabled") || !parseBool(keys.get("enabled"))) {
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
		if (keys.containsKey("display")) {
			display = Integer.parseInt(keys.get("display"));
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
		}, display);
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
