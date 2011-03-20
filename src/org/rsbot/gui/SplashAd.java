package org.rsbot.gui;

import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.rsbot.util.GlobalConfiguration;

/**
 * User: Zenzie & Paris
 */
public class SplashAd extends JDialog implements MouseListener {
	private static final long serialVersionUID = 1L;
	
	private static final String IMG_TYPE  = "png";

    public SplashAd(JFrame owner) {
    	super(owner);
    	
        File file = new File(GlobalConfiguration.Paths.getCacheDirectory(), "advert." + IMG_TYPE);
        
        if (file.exists() && file.lastModified() < new Date().getTime() - 1000 * 60 * 60 * 24) {
        	try {
        		file.delete();
        	}
        	catch (Exception e) {
        	}
        }
        
        if (!file.exists()) {
            try {
               BufferedImage img = ImageIO.read(new URL(GlobalConfiguration.Paths.URLs.GOLD4RS_IMG));
               ImageIO.write(img, IMG_TYPE, file);
            } catch (Exception e){
            }
        }
        
        try {
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setUndecorated(true);
            setTitle("Advertisement");
            BufferedImage img = ImageIO.read(file);
            setSize(img.getWidth(), img.getHeight());
            
            JLabel label = new JLabel();
            label.setIcon(new ImageIcon(img));
            add(label);
            
            addMouseListener(this);
            setLocationRelativeTo(getOwner());
            setVisible(true);
            
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    dispose();
                }
            }, 5000);
        } catch (IOException e) {
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (Desktop.isDesktopSupported()) {
        	try {
        		Desktop.getDesktop().browse(new URL(GlobalConfiguration.Paths.URLs.GOLD4RS).toURI());
        	}
        	catch (Exception e1) {
        	}
        }
        
        dispose();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
