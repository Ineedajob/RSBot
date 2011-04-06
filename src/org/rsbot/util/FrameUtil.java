package org.rsbot.util;

import javax.swing.*;

/**
 * @author Aut0r
 */
class FrameUtil {

	@SuppressWarnings("static-access")
	public static boolean setTheme(JFrame f, String name) {
		try {

			JFrame.setDefaultLookAndFeelDecorated(true);
			UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin." + name);
			SwingUtilities.updateComponentTreeUI(f);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
