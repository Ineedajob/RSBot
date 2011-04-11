package org.rsbot.gui;

import org.rsbot.util.GlobalConfiguration;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Zenzie
 * @author Paris
 */
public class SplashAd extends JDialog implements MouseListener {

	private static final long serialVersionUID = 1L;

	private static final String CACHED_IMAGE = "advert.png";
	private static final String CACHED_FORMAT = "png";

	public SplashAd(JFrame owner) {
		super(owner);

		File file = new File(GlobalConfiguration.Paths.getCacheDirectory(), CACHED_IMAGE);

		if (file.exists() && file.lastModified() < new Date().getTime() - GlobalConfiguration.AD_EXPIRY) {
			if (!file.delete()) {
				file.deleteOnExit();
			}
		}

		if (!file.exists()) {
			try {
				BufferedImage img = ImageIO.read(new URL(GlobalConfiguration.Paths.URLs.AD_IMG));
				ImageIO.write(img, CACHED_FORMAT, file);
				if (GlobalConfiguration.AD_OPENWEB)
					BotGUI.openURL(GlobalConfiguration.Paths.URLs.AD_LINK);
			} catch (Exception ignored) {
			}
		}

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setUndecorated(true);
		setTitle("Advertisement");
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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
		BotGUI.openURL(GlobalConfiguration.Paths.URLs.AD_LINK);
		dispose();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

}
