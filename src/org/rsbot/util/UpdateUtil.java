package org.rsbot.util;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

/*
Update utility by TheShadow
 */
public class UpdateUtil {

    private static final Logger log = Logger.getLogger(UpdateUtil.class.getName());
    private final Window parent;

    public UpdateUtil(final Window parent) {
        this.parent = parent;
    }

    public void checkUpdate(final boolean verbose) {
        if (verbose) {
            UpdateUtil.log.info("Checking for update...");
        }
        if (getLatestVersion() > GlobalConfiguration.getVersion()) {
            UpdateUtil.log.info("New version available!");
            final int update = JOptionPane.showConfirmDialog(parent,
                    "A newer version is available. Do you wish to update?",
                    "Update Found", JOptionPane.YES_NO_OPTION);
            if (update != 0) {
                UpdateUtil.log.info("Cancelled update");
            }
            if (update == 0) {
                updateBot();
            }
        } else {
            if (verbose) {
                UpdateUtil.log.info("You have the latest version");
            }
        }
    }

    public static boolean download(final String address, final String localFileName) {

        OutputStream out = null;
        URLConnection conn;
        InputStream in = null;
        try {
            final URL url = new URL(address);

            out = new BufferedOutputStream(new FileOutputStream(localFileName));
            conn = url.openConnection();
            in = conn.getInputStream();

            final byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }
        } catch (final Exception exception) {
            UpdateUtil.log.info("Downloading failed!");
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (final IOException ioe) {
                UpdateUtil.log.info("Downloading failed!");
                return false;
            }
            return true;
        }

    }

    private int getLatestVersion() {
        try {
            InputStream is = new URL(GlobalConfiguration.Paths.URLs.VERSION).openConnection().getInputStream();

            int off = 0;
            byte[] b = new byte[2];
            while ((off += is.read(b, off, 2 - off)) != 2) {
            }

            return ((0xFF & b[0]) << 8) + (0xFF & b[1]);
        } catch (final IOException e) {
            UpdateUtil.log.info("Unable to download latest version information");
            return -1;
        }
    }

    private void updateBot() {
        UpdateUtil.log.info("Downloading update...");

        final String jarNew = GlobalConfiguration.NAME + "-"
                + getLatestVersion() + ".jar";

        download(GlobalConfiguration.Paths.URLs.DOWNLOAD, jarNew);

        final String jarOld = GlobalConfiguration.NAME + "-"
                + GlobalConfiguration.getVersion() + ".jar";

        final Runtime run = Runtime.getRuntime();

        try {
            run.exec("java -jar " + jarNew + " delete " + jarOld);
            System.exit(0);
        } catch (final IOException ignored) {
        }
    }
}
