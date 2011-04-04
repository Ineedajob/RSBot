package org.rsbot.util;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Aut0r
 */
public class FrameUtil {

    @SuppressWarnings("static-access")
	public static boolean setTheme(JFrame f, String name) {
        try {

            f.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin." + name);
            SwingUtilities.updateComponentTreeUI(f);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
