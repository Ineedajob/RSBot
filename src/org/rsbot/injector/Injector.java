package org.rsbot.injector;

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.ConstantClass;
import com.sun.org.apache.bcel.internal.classfile.ConstantUtf8;
import com.sun.org.apache.bcel.internal.classfile.Field;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import org.rsbot.client.Model;
import org.rsbot.client.ModelCapture;
import org.rsbot.util.GlobalConfiguration;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

import static org.rsbot.injector.HookData.ClassData;
import static org.rsbot.injector.HookData.FieldData;
import static org.rsbot.injector.HookData.StaticFieldData;

public class Injector {

	private static final boolean LOAD_LOCAL = false;
	private static final String ACCESSOR_DESC = "org/rsbot/client/";
	private static final String ACCESSOR_PACKAGE = "org.rsbot.client.";

	private static final Object LOCK = new Object();

	private Logger log = Logger.getLogger(Injector.class.getName());

	private ClassGen[] loaded;
	private HookData hd = null;

	private int world;

	public Injector() {
		if (LOAD_LOCAL) {
			try {
				hd = new HookData(download(new File("info.dat").toURI().toURL()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				// hook data are intentionally not cached
				hd = new HookData(download(new URL(GlobalConfiguration.Paths.URLs.UPDATE)));
			} catch (Exception e) {
				log.severe("Unable to download hook data.");
				log.severe("Please check your firewall and internet connection.");
			}
		}

		world = 1 + new Random().nextInt(169);
	}

	public String generateTargetName() {
		if (hd == null) {
			return null;
		}
		String s = "";
		for (byte b : hd.charData.i)
			s += (char) hd.charData.c[b];

		return s;
	}

	private int getCachedVersion() {
		try {
			File versionFile = new File(GlobalConfiguration.Paths.getVersionCache());
			BufferedReader reader = new BufferedReader(new FileReader(versionFile));
			int version = Integer.parseInt(reader.readLine());
			reader.close();
			return version;
		} catch (Exception e) {
			return 0;
		}
	}

	private JarFile getJar(boolean loader) {
		while (true) {
			try {
				String s = "jar:http://world" + world + "." + generateTargetName() + ".com/";
				if (loader)
					s += "loader.jar!/";
				else
					s += generateTargetName() + ".jar!/";

				URL url = new URL(s);
				return ((JarURLConnection) url.openConnection()).getJarFile();
			} catch (Exception ignored) {
			}
		}
	}

	public HashMap<String, byte[]> getClasses() {
		synchronized (Injector.LOCK) {
			try {
				if (hd == null) {
					return null;
				}

				ArrayList<ClassGen> classlist = new ArrayList<ClassGen>();

				if (hd.version != getCachedVersion()) {
					log.info("Downloading loader");
					JarFile loaderJar = getJar(true);
					log.info("Downloading client");
					JarFile clientJar = getJar(false);

					Enumeration<JarEntry> entries = clientJar.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String name = entry.getName();
						if (name.endsWith(".class")) {
							ClassParser cp = new ClassParser(clientJar.getInputStream(entry), name);
							classlist.add(new ClassGen(cp.parse()));
						}
					}

					entries = loaderJar.entries();
					ArrayList<ClassGen> loaderclasslist = new ArrayList<ClassGen>();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String name = entry.getName();
						if (name.endsWith(".class")) {
							loaderclasslist.add(new ClassGen(new ClassParser(loaderJar.getInputStream(entry), name).parse()));
						}
					}

					log.info("Parsing loader");
					ClassGen[] loader = new ClassGen[loaderclasslist.size()];
					loaderclasslist.toArray(loader);
					String[] classNames = new String[5];
					for (ClassGen cg : loader) {
						if (!cg.getClassName().equals("loader")) continue;

						for (Method m : cg.getMethods()) {
							if (!m.getName().equals("run")) continue;
							InstructionSearcher s = new InstructionSearcher(cg, m);
							s.nextLDC("client");
							for (int i = 0; i < 5; i++)
								classNames[i] = (String) ((LDC) s.previous("LDC")).getValue(cg.getConstantPool());
							break;
						}
					}

					for (ClassGen cg : loader) {
						for (String name : classNames) {
							if (!cg.getClassName().equals(name)) continue;

							ClassGen ccg = null;
							for (Iterator<ClassGen> it = classlist.iterator(); it.hasNext(); ccg = it.next()) {
								if (ccg == null) continue;

								if (ccg.getClassName().equals(name)) {
									classlist.remove(ccg);
									break;
								}
							}
							classlist.add(cg);
						}
					}

					int size = classlist.size();
					loaded = new ClassGen[size];
					classlist.toArray(loaded);

					//Check version
					if (getRSBuild() != hd.version) {
						String message = GlobalConfiguration.NAME + " is currently outdated, please wait patiently for a new version.";
						log.severe(message);
						JOptionPane.showMessageDialog(null, message, "Outdated", JOptionPane.WARNING_MESSAGE);
						return new HashMap<String, byte[]>();
					}

					cacheClient();
				} else {
					log.info("Loading client #" + hd.version);

					try {
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
					} catch (Exception e) {
						File versionFile = new File(GlobalConfiguration.Paths.getVersionCache());
						if (versionFile.delete()) {
							log.info("Error loading client, redownloading...");
							return getClasses();
						} else {
							log.severe("Error loading cached client.");
							return new HashMap<String, byte[]>();
						}
					}
				}

				log.info("Injecting client #" + hd.version);

				//Interface and TileData ClassGen
				ClassGen cgInterface = null;

				//Inject interfaces
				ClassData[] classes = hd.classes.toArray(new ClassData[hd.classes.size()]);
				for (ClassGen cg : loaded) {
					for (ClassData cd : classes) {
						if (cg.getClassName().equals(cd.official_name)) {
							if (cd.injected_name.equals("RSInterface"))
								cgInterface = cg;

							cg.addInterface(ACCESSOR_PACKAGE + cd.injected_name);
							break;
						}
					}
				}

				hackAnimatedModel();
				hackCharacterModel();
				hackGroundModel();

				log.info("Preparing christmas decorations");
				hackChristmas();
				log.info("Enable christmas in the Edit menu!");

				//Inject masterx/y fields
				cgInterface.addField(new FieldGen(0, Type.INT, "masterX", cgInterface.getConstantPool()).getField());
				cgInterface.addField(new FieldGen(0, Type.INT, "masterY", cgInterface.getConstantPool()).getField());

				//Inject fields
				FieldData[] fields = hd.fields.toArray(new FieldData[hd.fields.size()]);
				for (ClassGen cg : loaded) {
					for (FieldData fd : fields) {
						if (cg.getClassName().equals(fd.official_class_name)) {
							injectGetter(cg, Type.getType(fd.injected_field_signature),
									fd.injected_field_name, fd.official_field_name);
						}
					}
				}

				//Inject static fields
				StaticFieldData[] staticFields = hd.staticFields.toArray(new StaticFieldData[hd.staticFields.size()]);
				ClassGen client = findClass("client");
				String[] username = new String[2];
				for (StaticFieldData fd : staticFields) {
					if (fd.injected_field_name.equals("getCurrentUsername"))
						username = new String[]{fd.official_class_name, fd.official_field_name};
					injectGetter(client, Type.getType(fd.injected_field_signature),
							fd.injected_field_name, fd.official_class_name + "." + fd.official_field_name);
				}

				//Inject master x/y
				ClassGen c_masterxy = findClass(hd.masterXY.class_name);
				Method m_masterxy = c_masterxy.containsMethod(hd.masterXY.method_name, hd.masterXY.method_signature);

				InstructionFactory fac = new InstructionFactory(c_masterxy, c_masterxy.getConstantPool());
				MethodGen mgn = new MethodGen(m_masterxy, c_masterxy.getClassName(), c_masterxy.getConstantPool());
				InstructionList il = mgn.getInstructionList();
				InstructionHandle[] ih = il.getInstructionHandles();

				InstructionHandle ih_append = ih[hd.masterXY.append_index];
				ih_append = il.append(ih_append, new ALOAD(hd.masterXY.aload));
				ih_append = il.append(ih_append, new ILOAD(hd.masterXY.iload_x));
				ih_append = il.append(ih_append, fac.createPutField(cgInterface.getClassName(), "masterX", Type.INT));
				ih_append = il.append(ih_append, new ALOAD(hd.masterXY.aload));
				ih_append = il.append(ih_append, new ILOAD(hd.masterXY.iload_y));
				il.append(ih_append, fac.createPutField(cgInterface.getClassName(), "masterY", Type.INT));

				mgn.setInstructionList(il);
				mgn.setMaxLocals();
				mgn.setMaxStack();
				mgn.update();
				c_masterxy.replaceMethod(m_masterxy, mgn.getMethod());

				//Inject message listener

				ClassGen c_sml = findClass(hd.messageEvent.class_name);
				Method m_sml = c_sml.containsMethod(hd.messageEvent.method_name, hd.messageEvent.method_signature);
				fac = new InstructionFactory(c_sml, c_sml.getConstantPool());
				mgn = new MethodGen(m_sml, c_sml.getClassName(), c_sml.getConstantPool());
				il = mgn.getInstructionList();
				ih = il.getInstructionHandles();

				ih_append = ih[hd.messageEvent.append_index];
				ih_append = il.append(ih_append, fac.createGetStatic("client", "callback",
						Type.getType(org.rsbot.client.Callback.class)));
				ih_append = il.append(ih_append, new ILOAD(hd.messageEvent.id));
				ih_append = il.append(ih_append, new ALOAD(hd.messageEvent.sender));
				ih_append = il.append(ih_append, new ALOAD(hd.messageEvent.message));
				il.append(ih_append, fac.createInvoke(ACCESSOR_PACKAGE + "Callback",
						"notifyMessage", Type.VOID, new Type[]{Type.INT, Type.STRING, Type.STRING},
						Constants.INVOKEINTERFACE));

				mgn.setInstructionList(il);
				mgn.setMaxLocals();
				mgn.setMaxStack();
				mgn.update();
				c_sml.replaceMethod(m_sml, mgn.getMethod());

				//RSObjects
				for (String RSObject : hd.rsObjects.object_class_names) {
					ClassGen rso = findClass(RSObject);
					Method rso_id = rso.containsMethod(hd.rsObjects.method_name, hd.rsObjects.method_signature);
					QIS s = new QIS(rso, rso_id);
					s.gotoEnd();

					if (!(s.previous(ReturnInstruction.class) instanceof IRETURN))
						continue;

					InstructionHandle[][] arguments = s.getArgumentInstructions();
					if (arguments.length != 1)
						continue;

					//Create method, since we have different types of getID we have to reconstruct it in the client
					//and not using the bot to figure it out!
					il = new InstructionList();
					for (InstructionHandle ih1 : arguments[0])
						il.append(ih1.getInstruction());
					il.append(s.current());

					MethodGen mg_getid = new MethodGen(
							Constants.ACC_PUBLIC | Constants.ACC_FINAL, // access_flags
							Type.INT, // return_type
							null, // arguement_types
							null, // arguement_names
							"getID", // method_name
							rso.getClassName(), // class_name;
							il, // instruction_list;
							rso.getConstantPool() // constant_pool_gen
					);
					mg_getid.stripAttributes(true);
					mg_getid.setMaxLocals();
					mg_getid.setMaxStack();

					rso.addMethod(mg_getid.getMethod());
				}

				//Render data
				ClassGen c_rd = findClass(hd.render.class_name);
				Method m_rd = c_rd.containsMethod(hd.render.method_name, hd.render.method_signature);
				fac = new InstructionFactory(c_rd, c_rd.getConstantPool());
				mgn = new MethodGen(m_rd, c_rd.getClassName(), c_rd.getConstantPool());
				il = mgn.getInstructionList();
				ih = il.getInstructionHandles();

				ih_append = ih[hd.render.append_index];
				ih_append = il.append(ih_append, fac.createGetStatic("client", "callback",
						Type.getType(org.rsbot.client.Callback.class)));
				ih_append = il.append(ih_append, fac.createGetStatic(hd.render.render_class_name,
						hd.render.render_field_name, Type.getType(hd.render.render_field_signature)));
				ih_append = il.append(ih_append, fac.createGetStatic(hd.render.renderData_class_name,
						hd.render.renderData_field_name, Type.getType(hd.render.renderData_field_signature)));
				il.append(ih_append, fac.createInvoke(ACCESSOR_PACKAGE + "Callback", "updateRenderInfo",
						Type.VOID, new Type[]{Type.getType(org.rsbot.client.Render.class),
								Type.getType(org.rsbot.client.RenderData.class)}, Constants.INVOKEINTERFACE));

				mgn.setInstructionList(il);
				mgn.setMaxLocals();
				mgn.setMaxStack();
				mgn.update();
				c_rd.replaceMethod(m_rd, mgn.getMethod());

				//Hack mouse/keyboard/canvas/signlink
				hackMouse();
				hackKeyboard();
				hackCanvas();
				hackRender();
				hackHeapSize();
				hackSignUID(username);

				//Insert callback
				insertCallback();
				insertRXTEACallback();

				//Return the classes
				HashMap<String, byte[]> ret = new HashMap<String, byte[]>();
				for (ClassGen cg : loaded) {
					ret.put(cg.getClassName(), cg.getJavaClass().getBytes());
				}

				return ret;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private int getRSBuild() throws Exception {
		ClassGen client = findClass("client");
		Method main = client.containsMethod("main", "([Ljava/lang/String;)V");
		MethodGen mg = new MethodGen(main, "client", client.getConstantPool());
		Instruction instructions[] = mg.getInstructionList().getInstructions();
		boolean foundWidth = false, foundHeight = false;
		for (int i = instructions.length - 1; i >= 0; i--) {
			Instruction instruction = instructions[i];
			short opcode = instruction.getOpcode();
			if (opcode == 17) {
				SIPUSH sipush = (SIPUSH) instruction;
				short value = sipush.getValue().shortValue();
				if (value != 1024 && value != 768 && value > 400 && value < 1000) {
					return value;
				}
				if (value == 1024 || value == 768) {
					if (value == 1024) {
						if (foundWidth) return value;
						else foundWidth = true;
					} else {
						if (foundHeight) return value;
						else foundHeight = true;
					}
				}
			}
		}
		return -1;
	}

	private void cacheClient() {
		try {
			File file = new File(GlobalConfiguration.Paths.getClientCache());
			FileOutputStream stream = new FileOutputStream(file);
			JarOutputStream out = new JarOutputStream(stream);

			for (ClassGen cg : loaded) {
				out.putNextEntry(new JarEntry(cg.getClassName() + ".class"));
				out.write(cg.getJavaClass().getBytes());
			}

			out.close();
			stream.close();

			FileWriter writer = new FileWriter(GlobalConfiguration.Paths.getVersionCache());
			writer.write(Integer.toString(hd.version));
			writer.close();
		} catch (IOException ignored) {
		}
	}

	private void insertRXTEACallback() {
		for (ClassGen cg : loaded) {
			ConstantPoolGen cpg = cg.getConstantPool();
			if (cpg.lookupString("m") != -1 && cpg.lookupString("_") != -1) {
				for (Method m : cg.getMethods()) {
					if (m.isStatic() && m.getReturnType().equals(Type.VOID)) {
						InstructionSearcher s = new InstructionSearcher(cg, m);
						if (s.nextLDC("m") != null && s.nextLDC("um") != null) {
							s.setPosition(0);
							s.nextPattern("ICONST MULTIANEWARRAY PUTSTATIC");
							PUTSTATIC put_region_keys = (PUTSTATIC) s.next("PUTSTATIC");
							if (s.nextFieldInstruction(put_region_keys.getClassName(cpg),
									put_region_keys.getFieldName(cpg)) == null) {
								continue;
							}
							if (put_region_keys != null) {
								MethodGen mg = new MethodGen(m, cg.getClassName(), cg.getConstantPool());
								InstructionList il = mg.getInstructionList();
								InstructionList nil = new InstructionList();
								InstructionFactory fac = new InstructionFactory(cg, cpg);

								InstructionHandle[] handles = il.getInstructionHandles();
								InstructionHandle inject = null;
								ILOAD load = null;

								for (int i = 0; i < handles.length; ++i) {
									if (handles[i].getInstruction() instanceof LDC) {
										LDC ldc = (LDC) handles[i].getInstruction();
										if (ldc.getValue(cpg).equals("m")) {
											for (int j = i - 1; j > 0; --j) {
												if (handles[j].getInstruction() instanceof IASTORE) {
													inject = handles[j];
													i = j;
													break;
												}
											}
											for (int j = i - 1; j > 0; --j) {
												if (handles[j].getInstruction() instanceof GETSTATIC) {
													load = (ILOAD) handles[j + 1].getInstruction();
													break;
												}
											}
											break;
										}
									}
								}

								nil.append(new DUP());
								nil.append(new DUP());
								nil.append(new ICONST(-1));
								nil.append(new IXOR());
								nil.append(new SWAP());
								nil.append(new SIPUSH((short) hd.version));
								nil.append(new BIPUSH((byte) 16));
								nil.append(new ISHL());
								nil.append(new IOR());
								nil.append(new SIPUSH((short) hd.version));
								nil.append(fac.createGetStatic(put_region_keys.getClassName(cpg),
										put_region_keys.getFieldName(cpg), new ArrayType(Type.INT, 2)));
								nil.append(InstructionFactory.createLoad(Type.INT, load.getIndex()));
								nil.append(InstructionFactory.createArrayLoad(new ArrayType(Type.INT, 1)));
								nil.append(new DUP());
								nil.append(new ICONST(0));
								nil.append(InstructionFactory.createArrayLoad(Type.INT));
								nil.append(new ICONST(-1));
								nil.append(new IXOR());
								nil.append(new SWAP());
								nil.append(fac.createInvoke(
										"org.rsbot.injector.Injector",
										"visit_region_tinydec",
										Type.VOID,
										new Type[]{Type.INT, Type.INT, Type.INT, Type.INT, new ArrayType(Type.INT, 1)},
										Constants.INVOKESTATIC));

								il.insert(inject, nil);
								mg.setMaxLocals();
								mg.setMaxStack();
								mg.update();
								cg.replaceMethod(m, mg.getMethod());
							}
						}
					}
				}
			}
		}
	}

	// --------------------

	private static final ExecutorService pool = Executors.newSingleThreadExecutor();
	private static volatile boolean submit_enabled = true;

	@SuppressWarnings("unused")
	public static void visit_region_tinydec(
			final int region,
			final int id,
			final int version,
			final int key,
			final int[] keys) {
		if (!submit_enabled) {
			return;
		}
		pool.submit(new Runnable() {
			public void run() {
				if (keys.length != 4) {
					return;
				}
				try {
					URL url = new URL(GlobalConfiguration.Paths.URLs.UPDATER + "xtea.php");
					HttpURLConnection connect = (HttpURLConnection) url.openConnection();
					connect.setRequestMethod("POST");
					connect.setDoOutput(true);
					connect.setDoInput(true);
					connect.setUseCaches(false);
					connect.setAllowUserInteraction(false);
					StringBuilder write = new StringBuilder().
							append("id=").append(id).
							append("&ver=").append(version).
							append("&reg=").append(region).
							append("&key=").append(key).
							append("&keys=");
					for (int i = 0; i < keys.length; ++i) {
						write.append(keys[i]);
						if (i != 3) {
							write.append('.');
						}
					}
					Writer writer = new OutputStreamWriter(connect.getOutputStream(), "UTF-8");
					writer.write(write.toString());
					writer.flush();
					writer.close();
					BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
					String response;
					if ((response = in.readLine()) != null && response.equals("stop")) {
						submit_enabled = false;
					}
					connect.disconnect();
				} catch (IOException ignored) {
				}
			}
		});
	}

	// --------------------

	private void insertCallback() {
		ClassGen client = findClass("client");

		//Insert field callback
		Field f = new FieldGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
				Type.getType(org.rsbot.client.Callback.class), "callback", client.getConstantPool()).getField();
		client.addField(f);

		//Insert getCallback
		{
			InstructionList il = new InstructionList();
			InstructionFactory factory = new InstructionFactory(client, client.getConstantPool());
			il.append(factory.createGetStatic("client", f.getName(), f.getType()));
			il.append(InstructionFactory.createReturn(f.getType()));

			MethodGen mg = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_FINAL, // access_flags
					f.getType(), // return_type
					null, // arguement_types
					null, // arguement_names
					"getCallback", // method_name
					"client", // class_name;
					il, // instruction_list;
					client.getConstantPool() // constant_pool_gen
			);
			mg.setMaxLocals();
			mg.setMaxStack();

			client.addMethod(mg.getMethod());
		}

		//Insert setCallback
		{
			InstructionList il = new InstructionList();
			InstructionFactory factory = new InstructionFactory(client, client.getConstantPool());
			il.append(new ALOAD(1));
			il.append(factory.createPutStatic("client", f.getName(), f.getType()));
			il.append(new RETURN());

			MethodGen mg = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_FINAL, // access_flags
					Type.VOID, // return_type
					new Type[]{f.getType()}, // arguement_types
					null, // arguement_names
					"setCallback", // method_name
					"client", // class_name;
					il, // instruction_list;
					client.getConstantPool() // constant_pool_gen
			);
			mg.setMaxLocals();
			mg.setMaxStack();

			client.addMethod(mg.getMethod());
		}
	}

	private void hackChristmas() {
		ClassGen cg = findClass("dm");
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
					InstructionFactory fac = new InstructionFactory(cg, cg.getConstantPool());
					InstructionList il = mg.getInstructionList();
					InstructionList nil = new InstructionList();
					nil.append(fac.createInvoke("org.rsbot.injector.Injector", "getId", Type.INT, new Type[]{Type.INT}, Constants.INVOKESTATIC));
					il.append(handle, nil);
					mg.setMaxLocals();
					mg.setMaxStack();
					mg.update();
					cg.replaceMethod(method, mg.getMethod());

					break;
				}

			}
		}
	}

	public static volatile boolean christmasMode = false;

	private static final HashMap<Integer, Integer> ID_MAP = new HashMap<Integer, Integer>();

	static {
		// tree
		ID_MAP.put(1276, 47748);
		ID_MAP.put(1278, 47748);
		// oak
		ID_MAP.put(1281, 56933);
		// yew
		ID_MAP.put(1309, 56933);
		// bank booth
		ID_MAP.put(11402, 19038);
		// ge spirit tree
		ID_MAP.put(1317, 47857);
	}

	public static int getId(int id) {
		if (christmasMode && ID_MAP.containsKey(id)) {
			return ID_MAP.get(id);
		}
		return id;
	}

	private void hackCharacterModel() {
		String model = "L" + findClass("Model").getClassName() + ";";
		ObjectType modelCaptureType = (ObjectType) Type.getType(ModelCapture.class);
		ClassGen cg = findClass("RSCharacter");
		Field f = new FieldGen(Constants.ACC_PRIVATE, Type.getType(Model.class), "model", cg.getConstantPool()).getField();
		cg.addField(f);

		for (Method method : cg.getMethods()) {
			if (!method.isStatic() && method.getSignature().contains("[" + model)) {
				MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());

				int idx = 0;
				InstructionHandle handle = null;

				for (InstructionHandle h : mg.getInstructionList().getInstructionHandles()) {
					if (h.getInstruction() instanceof ASTORE) {
						handle = h;
						idx = ((ASTORE) h.getInstruction()).getIndex();
						break;
					}
				}

				if (handle != null) {
					InstructionFactory fac = new InstructionFactory(cg, cg.getConstantPool());

					InstructionList il = mg.getInstructionList();
					InstructionList nil = new InstructionList();

					nil.append(new ALOAD(0));
					nil.append(fac.createNew(modelCaptureType));
					nil.append(new DUP());
					nil.append(new ALOAD(idx));
					nil.append(fac.createInvoke(
							modelCaptureType.getClassName(),
							"<init>",
							Type.VOID,
							new Type[]{Type.getType(Model.class)},
							Constants.INVOKESPECIAL));
					nil.append(fac.createPutField(cg.getClassName(), "model", Type.getType(Model.class)));

					il.append(handle, nil);
					mg.setMaxLocals();
					mg.setMaxStack();
					mg.update();
					cg.replaceMethod(method, mg.getMethod());
				}

				break;
			}
		}
	}

	private void hackAnimatedModel() {
		Type modelType = Type.getType("L" + findClass("Model").getClassName() + ";");
		ObjectType modelCaptureType = (ObjectType) Type.getType(ModelCapture.class);
		Type compositeType = Type.getType("L" + findClass("RSObjectComposite").getClassName() + ";");
		String toolkit = "L" + findClass("Render").getClassName() + ";";

		for (String rsobject : hd.rsObjects.object_class_names) {
			ClassGen cg = findClass(rsobject);

			for (Field field : cg.getFields()) {
				if (field.getType().equals(compositeType)) {
					Field f = new FieldGen(Constants.ACC_PRIVATE, Type.getType(Model.class), "model", cg.getConstantPool()).getField();
					cg.addField(f);

					for (Method method : cg.getMethods()) {
						if (!method.isStatic() && method.getSignature().contains(toolkit)
								&& !method.getName().equals("<init>")
								&& method.getSignature().endsWith(";")) {

							MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());

							int idx = 0;
							InstructionHandle ret = null;

							for (InstructionHandle h : mg.getInstructionList().getInstructionHandles()) {
								if (h.getInstruction() instanceof INVOKEVIRTUAL) {
									if (((INVOKEVIRTUAL) h.getInstruction()).getReturnType(cg.getConstantPool()).equals(modelType)) {
										h = h.getNext();
										if (h.getInstruction() instanceof ASTORE) {
											idx = ((ASTORE) h.getInstruction()).getIndex();
											ret = h;
										}
										break;
									}
								}
							}

							if (ret != null) {
								InstructionFactory fac = new InstructionFactory(cg, cg.getConstantPool());

								InstructionList il = mg.getInstructionList();
								InstructionList nil = new InstructionList();

								nil.append(new ALOAD(0));
								nil.append(fac.createNew(modelCaptureType));
								nil.append(new DUP());
								nil.append(new ALOAD(idx));
								nil.append(fac.createInvoke(
										modelCaptureType.getClassName(),
										"<init>",
										Type.VOID,
										new Type[]{Type.getType(Model.class)},
										Constants.INVOKESPECIAL));
								nil.append(fac.createPutField(cg.getClassName(), "model", Type.getType(Model.class)));

								il.append(ret, nil);
								mg.setMaxLocals();
								mg.setMaxStack();
								mg.update();
								cg.replaceMethod(method, mg.getMethod());
							}
						}
					}

					break;
				}
			}
		}
	}

	private void hackGroundModel() {
		String toolkit = "L" + findClass("Render").getClassName() + ";";
		ObjectType modelCaptureType = (ObjectType) Type.getType(ModelCapture.class);
		ClassGen cg = findClass("RSGroundObject");
		Field f = new FieldGen(Constants.ACC_PRIVATE, Type.getType(Model.class), "model", cg.getConstantPool()).getField();
		cg.addField(f);

		for (Method method : cg.getMethods()) {
			if (!method.isStatic() && method.getCode().getLength() > 1000 && method.getSignature().contains(toolkit)) {
				MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());

				int idx = 0;
				InstructionHandle handle = null;
				InstructionHandle[] handles = mg.getInstructionList().getInstructionHandles();
				for (int i = handles.length - 1; i >= 0; --i) {
					InstructionHandle h = handles[i];
					if (h.getInstruction() instanceof ASTORE && !(handles[i - 1].getInstruction() instanceof ARETURN)) {
						handle = h;
						idx = ((ASTORE) h.getInstruction()).getIndex();
						break;
					}
				}

				if (handle != null) {
					InstructionFactory fac = new InstructionFactory(cg, cg.getConstantPool());

					InstructionList il = mg.getInstructionList();
					InstructionList nil = new InstructionList();

					nil.append(new ALOAD(0));
					nil.append(fac.createNew(modelCaptureType));
					nil.append(new DUP());
					nil.append(new ALOAD(idx));
					nil.append(fac.createInvoke(
							modelCaptureType.getClassName(),
							"<init>",
							Type.VOID,
							new Type[]{Type.getType(Model.class)},
							Constants.INVOKESPECIAL));
					nil.append(fac.createPutField(cg.getClassName(), "model", Type.getType(Model.class)));

					il.append(handle, nil);
					mg.setMaxLocals();
					mg.setMaxStack();
					mg.update();
					cg.replaceMethod(method, mg.getMethod());
				}

				break;
			}
		}
	}

	private void hackCanvas() {
		for (ClassGen cg : loaded) {
			if (cg.getSuperclassName().equals("java.awt.Canvas")) {
				ConstantPoolGen cpg = cg.getConstantPool();
				cpg.setConstant(cg.getSuperclassNameIndex(), new ConstantClass(cpg.addUtf8(ACCESSOR_DESC + "input/Canvas")));
			}
		}
	}

	private void hackMouse() {
		for (ClassGen cg : loaded) {
			String interfaces[] = cg.getInterfaceNames();
			boolean foundMouseListener = false;

			for (String iface : interfaces) {
				if (iface.endsWith("MouseListener"))
					foundMouseListener = true;
				else if (iface.endsWith("MouseWheelListener")) {
					setSuperclassName(findClass(cg.getSuperclassName()), ACCESSOR_DESC + "input/Mouse");
					break;
				}
			}

			if (foundMouseListener) {
				Method methods[] = cg.getMethods();
				for (Method m : methods) {
					String name = m.getName();
					if (name.startsWith("mouse") || name.startsWith("focus"))
						cg.getConstantPool().setConstant(m.getNameIndex(), new ConstantUtf8("_" + name));
				}
			}
		}
	}

	private void hackKeyboard() {
		for (ClassGen cg : loaded) {
			String interfaces[] = cg.getInterfaceNames();
			for (String iface : interfaces) {
				if (iface.endsWith("KeyListener")) {
					setSuperclassName(findClass(cg.getSuperclassName()), ACCESSOR_DESC + "input/Keyboard");

					Method methods[] = cg.getMethods();
					for (Method m : methods) {
						String name = m.getName();
						if (name.startsWith("key") || name.startsWith("focus"))
							cg.getConstantPool().setConstant(m.getNameIndex(), new ConstantUtf8("_" + name));
					}

					return;
				}
			}
		}
	}

	private void hackRender() {
		String modelName = findClass("LDModel").getClassName();

		for (ClassGen cg : loaded) {
			if (cg.getClassName().equals(modelName)) {
				for (Method m : cg.getMethods()) {
					if (!m.isStatic() && !m.isAbstract() && m.getReturnType().equals(Type.VOID)) {
						QIS searcher = new QIS(cg, m);
						SIPUSH push;
						int count = 0;
						while ((push = searcher.next(SIPUSH.class)) != null) {
							if (push.getValue().equals(-5000)) {
								++count;
							}
						}
						if (count == 3) {
							MethodGen mg = new MethodGen(m, cg.getClassName(), cg.getConstantPool());
							InstructionFactory fac = new InstructionFactory(cg);
							InstructionList il = new InstructionList();
							il.append(fac.createGetStatic("client", "callback",
									Type.getType(org.rsbot.client.Callback.class)));
							il.append(fac.createInvoke(ACCESSOR_DESC + "Callback", "getBot",
									Type.getType(org.rsbot.bot.Bot.class),
									new Type[0], Constants.INVOKEINTERFACE));
							il.append(fac.createGetField("org/rsbot/bot/Bot", "disableRendering", Type.BOOLEAN));
							il.append(new IFEQ(mg.getInstructionList().getStart()));
							il.append(new RETURN());

							mg.getInstructionList().insert(il);
							mg.setMaxLocals();
							mg.setMaxStack();
							cg.replaceMethod(m, mg.getMethod());
							return;
						}
					}
				}
			}
		}
	}

	private void hackHeapSize() {
		for (ClassGen cg : loaded) {
			if (cg.getConstantPool().lookupString("maxMemory") > -1) {
				for (Method m : cg.getMethods()) {
					if (m.isStatic()) {
						InstructionSearcher s = new InstructionSearcher(cg, m);
						if (s.nextLDC("maxMemory") != null) {
							MethodGen mg = new MethodGen(m, cg.getClassName(), cg.getConstantPool());
							InstructionHandle handle = null;
							int found = 0;
							for (InstructionHandle h : mg.getInstructionList().getInstructionHandles()) {
								if (found == 2) {
									if (h.getInstruction() instanceof PUTSTATIC) {
										handle = h;
										break;
									}
								} else if (h.getInstruction() instanceof LDC) {
									++found;
								}
							}
							InstructionList il = mg.getInstructionList();
							il.append(il.insert(handle, new POP()), new BIPUSH((byte) 99));
							mg.setInstructionList(il);
							mg.setMaxLocals();
							mg.setMaxStack();
							cg.replaceMethod(m, mg.getMethod());
						}
					}
				}
			}
		}
	}

	private boolean hackSignUID(String[] username) {
		ClassGen cgSeekableFile = null;
		ClassGen cgFileOnDisk = null;

		for (ClassGen cg : loaded) {
			ConstantPoolGen cpg = cg.getConstantPool();
			if (cpg.lookupUtf8(" in file ") != -1)
				cgSeekableFile = cg;
			else if (cpg.lookupString("Warning! fileondisk ") != -1)
				cgFileOnDisk = cg;
		}

		if (cgSeekableFile == null || cgFileOnDisk == null)
			return false;

		Field fFileOnDisk = null;
		for (Field f : cgSeekableFile.getFields()) {
			if (!f.isStatic() && f.getType().toString().equals(cgFileOnDisk.getClassName()))
				fFileOnDisk = f;
		}

		if (fFileOnDisk == null)
			return false;

		// patch part 1
		boolean breakLoop = false;
		for (ClassGen cg : loaded) {
			if (breakLoop) break;
			for (Method m : cg.getMethods()) {
				if (breakLoop) break;
				if (!m.isAbstract() && !m.isNative()) {
					InstructionSearcher s = new InstructionSearcher(cg, m);

					while (s.next("athrow") != null) {
						int T_INDEX = s.index;
						if (!(s.previous() instanceof INVOKESPECIAL)) {
							s.index = T_INDEX;
							continue;
						}
						if (!((InvokeInstruction) s.current()).getClassName(cg.getConstantPool()).equals("java.io.IOException")) {
							s.index = T_INDEX;
							continue;
						}

						if (s.previous("invokevirtual") == null || s.previous("invokevirtual") == null) {
							s.index = T_INDEX;
							continue;
						}
						InvokeInstruction iv = (InvokeInstruction) s.current();
						if (iv.getClassName(cg.getConstantPool()).equals(cgSeekableFile.getClassName())) {
							FieldInstruction fi = s.previousFieldInstruction();
							InstructionFactory fac = new InstructionFactory(cg, cg.getConstantPool());
							MethodGen mgn = new MethodGen(m, cg.getClassName(), cg.getConstantPool());
							InstructionList il = mgn.getInstructionList();
							il.insert(il.insert(
									il.getInstructionHandles()[s.index()],
									fac.createInvoke(cgSeekableFile.getClassName(), "fixFile", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL)),
									fac.createGetStatic(fi.getClassName(cg.getConstantPool()), fi.getFieldName(cg.getConstantPool()), fi.getFieldType(cg.getConstantPool())));
							mgn.setMaxLocals();
							mgn.setMaxStack();
							mgn.update();
							cg.replaceMethod(m, mgn.getMethod());
							breakLoop = true; //Break method and class loop aswell
							break; //Don't continue, because it takes useless time
						}

						s.index = T_INDEX;
					}
				}
			}
		}

		//patch part 2
		InstructionList il2 = new InstructionList();
		InstructionFactory fac2 = new InstructionFactory(cgSeekableFile, cgSeekableFile.getConstantPool());
		MethodGen mgff = new MethodGen(Constants.ACC_PUBLIC, // access flags
				Type.VOID,			   // return type
				Type.NO_ARGS, // argument types
				new String[]{}, // arg names
				"fixFile", cgSeekableFile.getClassName(),	// method, class
				il2, cgSeekableFile.getConstantPool());
		il2.append(new ALOAD(0));
		il2.append(fac2.createGetField(cgSeekableFile.getClassName(), fFileOnDisk.getName(), fFileOnDisk.getType()));
		il2.append(fac2.createNew("java.lang.String"));
		il2.append(new DUP());
		il2.append(fac2.createGetStatic(username[0], username[1], Type.STRING));
		il2.append(fac2.createInvoke(Type.STRING.getClassName(), "getBytes", new ArrayType(Type.BYTE, 1), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		il2.append(fac2.createInvoke("java.lang.String", "<init>", Type.VOID, new Type[]{new ArrayType(Type.BYTE, 1)}, Constants.INVOKESPECIAL));
		il2.append(fac2.createInvoke(cgFileOnDisk.getClassName(), "FixFile", Type.VOID, new Type[]{Type.STRING}, Constants.INVOKEVIRTUAL));
		il2.append(new RETURN());

		mgff.setMaxLocals();
		mgff.setMaxStack();
		cgSeekableFile.addMethod(mgff.getMethod());


		//patch part 3
		createMethodFixFile(cgFileOnDisk);

		return true;
	}

	public static void createMethodFixFile(ClassGen cgFileOnDisk) {
		Field cFile = null;
		Field cRAF = null;
		for (Field f : cgFileOnDisk.getFields()) {
			if (f.getType().toString().equals("java.io.File")) cFile = f;
			if (f.getType().toString().equals("java.io.RandomAccessFile")) cRAF = f;
		}
		if (cFile == null || cRAF == null) return;

		InstructionList il = new InstructionList();
		InstructionFactory fac = new InstructionFactory(cgFileOnDisk, cgFileOnDisk.getConstantPool());
		MethodGen ff = new MethodGen(Constants.ACC_PUBLIC, // access flags
				Type.VOID,			   // return type
				new Type[]{Type.STRING}, // argument types
				new String[]{"username"}, // arg names
				"FixFile", cgFileOnDisk.getClassName(),	// method, class
				il, cgFileOnDisk.getConstantPool());
		//add all the instructions
		InstructionHandle instr1 = il.append(new ALOAD(0));
		il.append(fac.createNew("java.io.File"));
		il.append(new DUP());
		il.append(new ALOAD(0));
		il.append(fac.createGetField(cgFileOnDisk.getClassName(), cFile.getName(), cFile.getType()));
		il.append(fac.createInvoke("java.io.File", "getParent", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		il.append(fac.createNew("java.lang.StringBuilder"));
		il.append(new DUP());
		il.append(new ALOAD(1));
		il.append(fac.createInvoke("java.lang.String", "valueOf", Type.STRING, new Type[]{Type.OBJECT}, Constants.INVOKESTATIC));
		il.append(fac.createInvoke("java.lang.StringBuilder", "<init>", Type.VOID, new Type[]{Type.STRING}, Constants.INVOKESPECIAL));
		il.append(new PUSH(cgFileOnDisk.getConstantPool(), ".dat"));
		il.append(fac.createInvoke("java.lang.StringBuilder", "append", Type.getType(StringBuilder.class), new Type[]{Type.STRING}, Constants.INVOKEVIRTUAL));
		il.append(fac.createInvoke("java.lang.StringBuilder", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		il.append(fac.createInvoke("java.io.File", "<init>", Type.VOID, new Type[]{Type.STRING, Type.STRING}, Constants.INVOKESPECIAL));
		il.append(fac.createPutField(cgFileOnDisk.getClassName(), cFile.getName(), cFile.getType()));
		il.append(new ALOAD(0));
		il.append(fac.createGetField(cgFileOnDisk.getClassName(), cFile.getName(), cFile.getType()));
		il.append(fac.createInvoke("java.io.File", "createNewFile", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		il.append(new POP());
		il.append(new ALOAD(0));
		il.append(fac.createNew("java.io.RandomAccessFile"));
		il.append(new DUP());
		il.append(new ALOAD(0));
		il.append(fac.createGetField(cgFileOnDisk.getClassName(), cFile.getName(), cFile.getType()));
		il.append(new PUSH(cgFileOnDisk.getConstantPool(), "rw"));
		il.append(fac.createInvoke("java.io.RandomAccessFile", "<init>", Type.VOID, new Type[]{Type.getType(File.class), Type.STRING}, Constants.INVOKESPECIAL));
		InstructionHandle instr2 = il.append(fac.createPutField(cgFileOnDisk.getClassName(), cRAF.getName(), cRAF.getType()));
		InstructionHandle instr3 = il.append(new ASTORE(2));
		il.append(fac.createGetStatic("java.lang.System", "out", Type.getType(PrintStream.class)));
		il.append(new PUSH(cgFileOnDisk.getConstantPool(), "###############\r\n# !! ERROR !! #\r\n###############"));
		il.append(fac.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[]{Type.STRING}, Constants.INVOKEVIRTUAL));
		il.append(new ALOAD(2));
		il.append(fac.createInvoke("java.io.IOException", "printStackTrace", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		InstructionHandle instr0 = il.append(new RETURN());
		il.append(instr2, new GOTO(instr0));
		ff.addExceptionHandler(instr1, instr2, instr3, new ObjectType("java.io.IOException"));
		ff.setMaxLocals();
		ff.setMaxStack();
		cgFileOnDisk.addMethod(ff.getMethod());
	}

	public ClassGen findClass(String className) {
		for (ClassGen clazz : loaded) {
			if (clazz.getClassName().equals(className))
				return clazz;
		}

		for (ClassData cd : hd.classes) {
			if (cd.injected_name.equals(className))
				return findClass(cd.official_name);
		}

		return null;
	}

	private byte[] download(URL url) throws IOException {
		URLConnection uc = url.openConnection();
		DataInputStream di = new DataInputStream(uc.getInputStream());
		byte[] buffer = new byte[uc.getContentLength()];
		di.readFully(buffer);
		di.close();
		return buffer;
	}

	private void setSuperclassName(ClassGen cg, String name) {
		ConstantPoolGen cpg = cg.getConstantPool();
		cpg.setConstant(cg.getSuperclassNameIndex(), new ConstantClass(cpg.addUtf8(name)));
	}

	private void injectGetter(ClassGen cg, Type castType, String methodName, String fieldName) {
		if (fieldName.contains(".")) {
			String[] parts = fieldName.split("\\.");
			if (parts.length > 2)
				throw new RuntimeException("Argument 'fieldName': " + fieldName + " contains more then one '.'!");

			ClassGen c = findClass(parts[0]);
			if (c == null)
				throw new RuntimeException("Could not find class: " + parts[0]);

			for (Field f : c.getFields()) {
				if (f.getName().equals(parts[1])) {
					injectGetter(cg, methodName, castType, c.getClassName(), f.getName(), f.getType());
					return;
				}
			}

			throw new RuntimeException("Could not find field: " + parts[1] + " in class: " + parts[0]);
		}

		for (Field f : cg.getFields()) {
			if (f.getName().equals(fieldName)) {
				if (f.getType() instanceof BasicType && !f.getType().equals(castType))
					injectGetter(cg, methodName, castType, cg.getClassName(), f.getName(), f.getType(), true);
				else
					injectGetter(cg, castType, methodName, f);
				return;
			}
		}

		throw new RuntimeException("Could not find field:" + fieldName + " in class: " + cg.getClassName());
	}

	private void injectGetter(ClassGen cg, Type castType, String methodName, Field f) {
		injectGetter(cg, methodName, castType, cg.getClassName(), f.getName(), f.getType());
	}

	private int getFlags(String field_class, String field_name, Type field_type) {
		ClassGen cg = findClass(field_class);
		Field fields[] = cg.getFields();
		for (Field f : fields) {
			if (field_name.equals(f.getName()) && field_type.equals(field_type)) {
				return f.getAccessFlags();
			}
		}
		throw new RuntimeException("Hooked some invalid field -> " + field_type + " " + field_class + "." + field_name);
	}

	private ClassGen injectGetter(ClassGen into, String method_name, Type return_type, String field_class, String field_name, Type field_type) {
		return injectGetter(into, method_name, return_type, field_class, field_name, field_type, false);
	}

	private ClassGen injectGetter(ClassGen into, String method_name, Type return_type, String field_class, String field_name, Type field_type, boolean checkcast) {
		boolean isStatic = (getFlags(field_class, field_name, field_type) & Constants.ACC_STATIC) != 0;
		ConstantPoolGen cpg = into.getConstantPool();
		InstructionList il = new InstructionList();
		InstructionFactory factory = new InstructionFactory(into, cpg);
		String class_name = into.getClassName();

		if (!isStatic)
			il.append(new ALOAD(0));

		il.append(factory.createFieldAccess(field_class, field_name, field_type, isStatic ? Constants.GETSTATIC : Constants.GETFIELD));
		if (checkcast && !(return_type instanceof BasicType))
			il.append(factory.createCheckCast(return_type instanceof ArrayType ? ((ArrayType) return_type) : (ObjectType) return_type));
		else if (checkcast) {
			switch (field_type.getType()) {
				case Constants.T_DOUBLE:
					switch (return_type.getType()) {
						case Constants.T_FLOAT:
							il.append(new D2F());
							break;
						case Constants.T_INT:
							il.append(new D2I());
							break;
						case Constants.T_LONG:
							il.append(new D2L());
							break;
					}
					break;
				case Constants.T_FLOAT:
					switch (return_type.getType()) {
						case Constants.T_DOUBLE:
							il.append(new F2D());
							break;
						case Constants.T_INT:
							il.append(new F2I());
							break;
						case Constants.T_LONG:
							il.append(new F2L());
							break;
					}
					break;
				case Constants.T_INT:
					switch (return_type.getType()) {
						case Constants.T_BYTE:
							il.append(new I2B());
							break;
						case Constants.T_CHAR:
							il.append(new I2C());
							break;
						case Constants.T_DOUBLE:
							il.append(new I2D());
							break;
						case Constants.T_FLOAT:
							il.append(new I2F());
							break;
						case Constants.T_LONG:
							il.append(new I2L());
							break;
						case Constants.T_SHORT:
							il.append(new I2S());
							break;
					}
					break;
				case Constants.T_LONG:
					switch (return_type.getType()) {
						case Constants.T_DOUBLE:
							il.append(new L2D());
							break;
						case Constants.T_FLOAT:
							il.append(new L2F());
							break;
						case Constants.T_INT:
							il.append(new L2I());
							break;
					}
					break;
			}
		}
		il.append(InstructionFactory.createReturn(return_type));

		MethodGen mg = new MethodGen(
				Constants.ACC_PUBLIC | Constants.ACC_FINAL, // access_flags
				return_type, // return_type
				null, // arguement_types
				null, // arguement_names
				method_name, // method_name
				class_name, // class_name;
				il, // instruction_list;
				cpg // constant_pool_gen
		);
		mg.setMaxLocals();
		mg.setMaxStack();

		into.addMethod(mg.getMethod());
		return into;
	}
}
