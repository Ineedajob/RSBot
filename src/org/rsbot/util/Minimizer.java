package org.rsbot.util;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Nadar, Pervy, Aut0r
 */
public class Minimizer {

	private static Image getImage() throws HeadlessException {
		return GlobalConfiguration.getImage(
				GlobalConfiguration.Paths.Resources.ICON,
				GlobalConfiguration.Paths.ICON);

	}

	private static PopupMenu createPopupMenu() throws HeadlessException {
		PopupMenu menu = new PopupMenu();
		MenuItem release = new MenuItem("Release from Tray");

		release.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				comp.setVisible(true);
				SystemTray.getSystemTray().remove(icon);
				icon = null;

			}
		});
		menu.add(release);
		MenuItem exit = new MenuItem("Exit Bot");

		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				System.exit(0);

			}
		});
		menu.add(exit);

		return menu;
	}

	private static TrayIcon icon;
	private static Component comp;
	private static SystemTray tray;

	public static void snapToTray(final Component c) throws Exception {
		comp = c;
		tray = SystemTray.getSystemTray();

		icon = new TrayIcon(getImage().getScaledInstance(
				SystemTray.getSystemTray().getTrayIconSize().width,
				SystemTray.getSystemTray().getTrayIconSize().height, 0),
		                    "Rsbot", createPopupMenu());

		icon.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				c.setVisible(true);
				tray.remove(icon);
			}
		});

		tray.add(icon);

	}

	public static void displayMessage(Object o, MessageType t) {
		String option = null;
		switch (t) {
			case INFO:
				option = "Information";
				break;
			case ERROR:
				option = "Error !";
				break;
			case WARNING:
				option = "Warning !";
				break;
		}
		icon.displayMessage(option, o.toString(), t);
	}
}

