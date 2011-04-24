package org.rsbot.loader.script.adapter;

import org.rsbot.loader.asm.ClassAdapter;
import org.rsbot.loader.asm.ClassVisitor;

/**
 * @author Jacmob
 */
public class AddInterfaceAdapter extends ClassAdapter {

	private final String inter;

	public AddInterfaceAdapter(ClassVisitor delegate, String inter) {
		super(delegate);
		this.inter = inter;
	}

	public void visit(
			final int version,
			final int access,
			final String name,
			final String signature,
			final String superName,
			final String[] interfaces) {
		String[] inters = new String[interfaces.length + 1];
		System.arraycopy(interfaces, 0, inters, 0, interfaces.length);
		inters[interfaces.length] = inter;
		cv.visit(version, access, name, signature, superName, inters);
	}

}
