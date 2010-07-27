package org.rsbot.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class provides a File like access to both normal files and jar files.
 * Including support for directories in jar files.
 *
 * @author Qauters
 */
public class GlobalFile extends File {

    private static final long serialVersionUID = 9081385558893463127L;

    public GlobalFile(final File parent, final String child) {
        super(parent, child + (child.endsWith(".jar") ? "!" + File.separator : ""));
    }

    public GlobalFile(final String pathname) {
        super(pathname + (pathname.endsWith(".jar") ? "!" + File.separator : ""));
    }

    public GlobalFile(final String parent, final String child) {
        super(parent, child + (child.endsWith(".jar") ? "!" + File.separator : ""));
    }

    public GlobalFile(final URI uri) {
        super(uri.toString() + (uri.toString().endsWith(".jar") ? "!" + File.separator : ""));
    }

    @Override
    public boolean canRead() {
        if (isJarEntry())
            return getJarEntries() != null;

        return super.canRead();
    }

    @Override
    public boolean canWrite() {
        if (isJarEntry())
            return false;

        return super.canWrite();
    }

    @Override
    public boolean createNewFile() throws IOException {
        if (isJarEntry())
            return false;

        return super.createNewFile();
    }

    @Override
    public boolean delete() {
        if (isJarEntry())
            return false;

        return super.delete();
    }

    @Override
    public void deleteOnExit() {
        if (!isJarEntry()) {
            super.deleteOnExit();
        }
    }

    @Override
    public boolean exists() {
        if (isJarEntry())
            return getJarEntries() != null;

        return super.exists();
    }

    @Override
    public File getAbsoluteFile() {
        return new GlobalFile(getAbsolutePath());
    }

    @Override
    public File getCanonicalFile() throws IOException {
        return new GlobalFile(getCanonicalPath());
    }

    private String getInJarPath() {
        final String path = getPath();
        return path.substring(path.indexOf("jar!") + 4).replace(File.separator, "/");
    }

    private ArrayList<JarEntry> getJarEntries() {
        if (!isJarEntry())
            return null;

        final File f = new File(getJarPath());
        if (!f.exists())
            return null;

        try {
            final ArrayList<JarEntry> jarEntries = new ArrayList<JarEntry>();
            final JarFile jar = new JarFile(f);
            final Enumeration<JarEntry> entries = jar.entries();
            // Get rel path, without root slash
            final String relPath = getInJarPath().substring(1);

            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String name = entry.getName();
                if (name.startsWith(relPath)) {
                    jarEntries.add(entry);
                }
            }

            return jarEntries;

        } catch (final Exception e) {
        }

        return null;
    }

    private String getJarPath() {
        final String path = getPath();
        return path.substring(0, path.indexOf("jar!") + 3);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getParent() {
        return super.getParent();
    }

    @Override
    public File getParentFile() {
        return super.getParentFile();
    }

    @Override
    public String getPath() {
        String p = super.getPath();
        if (p.contains(".jar!")) {
            p += File.separator;
        }

        return p;
    }

    @Override
    public boolean isDirectory() {
        if (isJarEntry()) {
        	if (exists()) {
        		String path = getPath();
        		path = path.substring(path.lastIndexOf('!'));
        		if (!path.contains(".")) {
        			return true;
        		}
        	}
        	return false;
        }

        return super.isDirectory();
    }

    @Override
    public boolean isFile() {
        return !isDirectory() && exists();
    }

    @Override
    public boolean isHidden() {
        return super.isHidden();
    }

    public boolean isJarEntry() {
        return getPath().contains(".jar!");
    }

    @Override
    public long lastModified() {
        return super.lastModified();
    }

    @Override
    public long length() {
        return super.length();
    }

    @Override
    public String[] list() {
        if (!isDirectory())
            return null;

        if (isJarEntry()) {
            final ArrayList<String> list = new ArrayList<String>();
            final ArrayList<JarEntry> entries = getJarEntries();
            if (entries == null)
                return null;

            for (final JarEntry entry : entries) {
                // Get the name and remove the inJarPath
                final String name = entry.getName().replace(getInJarPath().substring(1), "");
                if (name.length() == 0) {
                	continue;
                }
                final String[] split = name.split("/");
                switch (split.length) {
                    case 0:
                        list.add(split[0]);
                        break;

                    case 1:
                        if (!list.contains(split[0])) {
                            list.add(split[0]);
                        }
                        break;
                }
            }
            return list.toArray(new String[0]);
        }

        return super.list();
    }

    @Override
    public File[] listFiles() {
        final String[] ss = list();
        if (ss == null)
            return null;

        final int n = ss.length;
        final File[] fs = new File[n];
        for (int i = 0; i < n; i++) {
            fs[i] = new GlobalFile(this, ss[i]);
        }

        return fs;

    }

    @Override
    public File[] listFiles(final FileFilter filter) {
        final String ss[] = list();
        if (ss == null)
            return null;

        final ArrayList<File> files = new ArrayList<File>();
        for (final String s : ss) {
            final File f = new GlobalFile(this, s);
            if ((filter == null) || filter.accept(f)) {
                files.add(f);
            }
        }

        return files.toArray(new File[files.size()]);

    }

    @Override
    public File[] listFiles(final FilenameFilter filter) {
        final String ss[] = list();
        if (ss == null)
            return null;

        final ArrayList<File> files = new ArrayList<File>();
        for (final String s : ss) {
            if ((filter == null) || filter.accept(this, s)) {
                files.add(new GlobalFile(this, s));
            }
        }

        return files.toArray(new File[files.size()]);

    }

    @Override
    public boolean mkdir() {
        if (isJarEntry())
            return false;

        return super.mkdir();
    }

    @Override
    public boolean mkdirs() {
        if (isJarEntry())
            return false;

        return super.mkdirs();
    }

    @Override
    public boolean renameTo(final File dest) {
        if (isJarEntry())
            return false;

        return super.renameTo(dest);
    }

    @Override
    public boolean setLastModified(final long time) {
        if (isJarEntry())
            return false;

        return super.setLastModified(time);
    }

    @Override
    public boolean setReadOnly() {
        if (isJarEntry())
            return false;

        return super.setReadOnly();
    }

    @Override
    public URI toURI() {
        try {
            // Generate the uri
            String uri = (isJarEntry() ? "jar:" : "") + super.toURI().toString();

            // Add the missing slash for jar directories
            if (isJarEntry() && isDirectory() && !uri.endsWith("/")) {
                uri += "/";
            }

            return new URI(uri);
        } catch (final Exception e) {
        }

        return super.toURI();
    }
}
