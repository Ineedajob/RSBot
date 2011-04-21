package org.rsbot.injection;

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import org.rsbot.loader.script.ModScript;
import org.rsbot.util.GlobalConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * @author Timer
 * @author Method
 * @author Aut0r
 * @author Qauters
 */
public class Injector {
	private static Logger log = Logger.getLogger(Injector.class.getName());

	private ClassGen[] loaded;

	private static final String CLASS_OBJECT_INTERACTIVE = "bj";

	public static volatile boolean easterMode = false;

	private static final HashMap<Integer, Integer> ID_MAP = new HashMap<Integer, Integer>();

	public HashMap<String, byte[]> init(ModScript script) throws Exception {
		download(new File(GlobalConfiguration.Paths.getHackCache()), new URL(GlobalConfiguration.Paths.URLs
				.EASTER_MATRIX));
		File hackData = new File(GlobalConfiguration.Paths.getHackCache());
		if (hackData.exists() && hackData.canRead()) {
			log.info("Welcome to Easter Mode!");
			FileInputStream fis = new FileInputStream(hackData);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			String strLine = "";
			while ((strLine = br.readLine()) != null) {
				if (strLine.length() >= 3 && strLine.contains(":")) {
					String[] sp = strLine.split(":");
					if (sp.length == 2) {
						int id = Integer.parseInt(sp[0]);
						int replaceID = Integer.parseInt(sp[1]);
						ID_MAP.put(id, replaceID);
					}
				}
			}
			br.close();
		} else {
			log.info("Failed to download id matrix.");
		}
		ArrayList<ClassGen> classlist = new ArrayList<ClassGen>();
		JarFile cachedJar = new JarFile(GlobalConfiguration.Paths.getClientCache());
		Enumeration<JarEntry> entries = cachedJar.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String name = entry.getName();
			if (name.endsWith(".class")) {
				ClassParser cp = new ClassParser(cachedJar.getInputStream(entry), name);
				classlist.add(new ClassGen(cp.parse()));
			}
		}
		int size = classlist.size();
		loaded = new ClassGen[size];
		classlist.toArray(loaded);
		hackObjects();
		HashMap<String, byte[]> ret = new HashMap<String, byte[]>();
		for (ClassGen cg : loaded) {
			ret.put(cg.getClassName(), script.process(cg.getClassName(), cg.getJavaClass().getBytes()));
		}
		return ret;
	}

	public ClassGen findClass(String className) {
		for (ClassGen clazz : loaded) {
			if (clazz.getClassName().equals(className)) {
				return clazz;
			}
		}
		return null;
	}

	private void hackObjects() {
		ClassGen cg = findClass(CLASS_OBJECT_INTERACTIVE);
		if (cg != null) {
			for (Method method : cg.getMethods()) {
				if (!method.isStatic() && method.getReturnType() instanceof ObjectType) {
					MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
					InstructionHandle handle = null;
					for (InstructionHandle h : mg.getInstructionList().getInstructionHandles()) {
						if (h.getInstruction() instanceof IAND) {
							handle = h;
							break;
						}
					}
					if (handle != null) {
						log.info("Successfully injected!  To enable Easter mode Edit > Easter");
						InstructionFactory fac = new InstructionFactory(cg, cg.getConstantPool());
						InstructionList il = mg.getInstructionList();
						InstructionList nil = new InstructionList();
						nil.append(fac.createInvoke("org.rsbot.injection.Injector", "getId", Type.INT,
								new Type[]{Type.INT}, Constants.INVOKESTATIC));
						il.append(handle, nil);
						mg.setMaxLocals();
						mg.setMaxStack();
						mg.update();
						cg.replaceMethod(method, mg.getMethod());
						log.info("Credits: Method, Aut0r, Timer.");
						break;
					}
				}
			}
		}
	}

	public static int getId(int id) {
		if (easterMode && ID_MAP.containsKey(id)) {
			return ID_MAP.get(id);
		}
		return id;
	}

	private void download(File file, URL url) {
		try {
			URLConnection uc = url.openConnection();
			uc.setConnectTimeout(10000);
			DataInputStream di = new DataInputStream(uc.getInputStream());
			byte[] buffer = new byte[uc.getContentLength()];
			di.readFully(buffer);
			di.close();
			if (buffer.length == 0) {
				log.warning("Could not retrieve id matrix.");
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			if (file.exists() && (!file.canRead() || file.canWrite())) {
				file.setReadable(true);
				file.setWritable(true);
			}
			if (file.exists() && file.canRead() && file.canWrite()) {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(buffer);
				fos.flush();
				fos.close();
			}
		} catch (Exception ignored) {
		}
	}
}
