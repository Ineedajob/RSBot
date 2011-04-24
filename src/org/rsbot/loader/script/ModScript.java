package org.rsbot.loader.script;

import org.rsbot.loader.asm.ClassAdapter;
import org.rsbot.loader.asm.ClassReader;
import org.rsbot.loader.asm.ClassVisitor;
import org.rsbot.loader.asm.ClassWriter;
import org.rsbot.loader.script.adapter.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jacmob
 */
public class ModScript {

	public static interface Opcodes {
		int ATTRIBUTE = 1;
		int GET_STATIC = 2;
		int GET_FIELD = 3;
		int ADD_FIELD = 4;
		int ADD_METHOD = 5;
		int ADD_INTERFACE = 6;
		int SET_SUPER = 7;
		int SET_SIGNATURE = 8;
		int INSERT_CODE = 9;
		int OVERRIDE_CLASS = 10;
	}

	public static final int MAGIC = 0xFADFAD;

	private String name;
	private int version;
	private Map<String, String> attributes;
	private Map<String, ClassAdapter> adapters;
	private Map<String, ClassWriter> writers;

	public ModScript(byte[] data) throws ParseException {
		load(new Buffer(data));
	}

	public String getName() {
		return name;
	}

	public int getVersion() {
		return version;
	}

	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public byte[] process(String key, byte[] data) {
		ClassAdapter adapter = adapters.get(key);
		if (adapter != null) {
			ClassReader reader = new ClassReader(data);
			reader.accept(adapter, ClassReader.SKIP_FRAMES);
			return writers.get(key).toByteArray();
		}
		return data;
	}

	public byte[] process(String key, InputStream is) throws IOException {
		ClassAdapter adapter = adapters.get(key);
		if (adapter != null) {
			ClassReader reader = new ClassReader(is);
			reader.accept(adapter, ClassReader.SKIP_FRAMES);
			return writers.get(key).toByteArray();
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int n;
		while ((n = is.read(buffer)) != -1) {
			os.write(buffer, 0, n);
		}
		return os.toByteArray();
	}

	private void load(Buffer buff) throws ParseException {
		if (buff.g4() != ModScript.MAGIC) {
			throw new ParseException("Bad magic!");
		}
		attributes = new HashMap<String, String>();
		adapters = new HashMap<String, ClassAdapter>();
		writers = new HashMap<String, ClassWriter>();
		name = buff.gstr();
		version = buff.g2();
		int num = buff.g2();
		while (num-- > 0) {
			int op = buff.g1();
			if (op == Opcodes.ATTRIBUTE) {
				String key = buff.gstr();
				String value = buff.gstr();
				attributes.put(key, new StringBuilder(value).reverse().toString());
			} else if (op == Opcodes.GET_STATIC || op == Opcodes.GET_FIELD) {
				String clazz = buff.gstr();
				int count = buff.g2(), ptr = 0;
				AddGetterAdapter.Field[] fields = new AddGetterAdapter.Field[count];
				while (ptr < count) {
					AddGetterAdapter.Field f = new AddGetterAdapter.Field();
					f.getter_access = buff.g4();
					f.getter_name = buff.gstr();
					f.getter_desc = buff.gstr();
					f.owner = buff.gstr();
					f.name = buff.gstr();
					f.desc = buff.gstr();

					fields[ptr++] = f;
				}
				adapters.put(clazz, new AddGetterAdapter(delegate(clazz), op == Opcodes.GET_FIELD, fields));
			} else if (op == Opcodes.ADD_FIELD) {
				String clazz = buff.gstr();
				int count = buff.g2(), ptr = 0;
				AddFieldAdapter.Field[] fields = new AddFieldAdapter.Field[count];
				while (ptr < count) {
					AddFieldAdapter.Field f = new AddFieldAdapter.Field();
					f.access = buff.g4();
					f.name = buff.gstr();
					f.desc = buff.gstr();
					fields[ptr++] = f;
				}
				adapters.put(clazz, new AddFieldAdapter(delegate(clazz), fields));
			} else if (op == Opcodes.ADD_METHOD) {
				String clazz = buff.gstr();
				int count = buff.g2(), ptr = 0;
				AddMethodAdapter.Method[] methods = new AddMethodAdapter.Method[count];
				while (ptr < count) {
					AddMethodAdapter.Method m = new AddMethodAdapter.Method();
					m.access = buff.g4();
					m.name = buff.gstr();
					m.desc = buff.gstr();
					byte[] code = new byte[buff.g4()];
					buff.gdata(code, code.length, 0);
					m.code = code;
					m.max_locals = buff.g1();
					m.max_stack = buff.g1();
					methods[ptr++] = m;
				}
				adapters.put(clazz, new AddMethodAdapter(delegate(clazz), methods));
			} else if (op == Opcodes.ADD_INTERFACE) {
				String clazz = buff.gstr();
				String inter = buff.gstr();
				adapters.put(clazz, new AddInterfaceAdapter(delegate(clazz), inter));
			} else if (op == Opcodes.SET_SUPER) {
				String clazz = buff.gstr();
				String superName = buff.gstr();
				adapters.put(clazz, new SetSuperAdapter(delegate(clazz), superName));
			} else if (op == Opcodes.SET_SIGNATURE) {
				String clazz = buff.gstr();
				int count = buff.g2(), ptr = 0;
				SetSignatureAdapter.Signature[] signatures = new SetSignatureAdapter.Signature[count];
				while (ptr < count) {
					SetSignatureAdapter.Signature s = new SetSignatureAdapter.Signature();
					s.name = buff.gstr();
					s.desc = buff.gstr();
					s.new_access = buff.g4();
					s.new_name = buff.gstr();
					s.new_desc = buff.gstr();
					signatures[ptr++] = s;
				}
				adapters.put(clazz, new SetSignatureAdapter(delegate(clazz), signatures));
			} else if (op == Opcodes.INSERT_CODE) {
				String clazz = buff.gstr();
				String name = buff.gstr();
				String desc = buff.gstr();
				int count = buff.g1();
				Map<Integer, byte[]> fragments = new HashMap<Integer, byte[]>();
				while (count-- > 0) {
					int off = buff.g2();
					byte[] code = new byte[buff.g4()];
					buff.gdata(code, code.length, 0);
					fragments.put(off, code);
				}
				adapters.put(clazz, new InsertCodeAdapter(delegate(clazz),
						name, desc, fragments, buff.g1(), buff.g1()));
			} else if (op == Opcodes.OVERRIDE_CLASS) {
				String old_clazz = buff.gstr();
				String new_clazz = buff.gstr();
				int count = buff.g1();
				while (count-- > 0) {
					String clazz = buff.gstr();
					adapters.put(clazz, new OverrideClassAdapter(delegate(clazz), old_clazz, new_clazz));
				}
			}
		}
	}

	private ClassVisitor delegate(String clazz) {
		ClassAdapter delegate = adapters.get(clazz);
		if (delegate == null) {
			ClassWriter writer = new ClassWriter(0);
			writers.put(clazz, writer);
			return writer;
		} else {
			return delegate;
		}
	}

}
