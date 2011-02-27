
package org.rsbot.script.util;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Nader Sleiman
 */
public class FrameUtil {

    public static void setTheme(JFrame f,String name)
    {
            try {

                    f.setDefaultLookAndFeelDecorated(true);
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin."+name);
                    SwingUtilities.updateComponentTreeUI(f);
                     SwingUtilities.updateComponentTreeUI(f);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Couldn't load the look and feel Substance.");
                }
}
}
