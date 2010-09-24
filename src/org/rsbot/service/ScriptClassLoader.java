package org.rsbot.service;

import org.rsbot.injector.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Jacmob
 */
public class ScriptClassLoader extends URLClassLoader {

	public ScriptClassLoader(URL... urls) {
		super(urls);
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> clazz = super.loadClass(name);

		if (clazz != null) {
			ClassLoader loader = clazz.getClassLoader();
			if (loader != null) {
				InputStream stream = loader.getResourceAsStream(name.replace('.', '/') + ".class");
				try {
					ClassReader r = new ClassReader(stream);
					r.accept(new ClassVerifier(), 0);
				} catch (IOException ex) {
					throw new RuntimeException("Class verification failed!");
				}
			}
		}

		return clazz;
	}

	private static class ClassVerifier implements ClassVisitor {

		public void visit(
				final int version,
				final int access,
				final String name,
				final String signature,
				final String superName,
				final String[] interfaces) {

		}

		public void visitSource(final String source, final String debug) {

		}

		public void visitOuterClass(
				final String owner,
				final String name,
				final String desc) {

		}

		public AnnotationVisitor visitAnnotation(
				final String desc,
				final boolean visible) {
			return null;
		}

		public void visitAttribute(final Attribute attr) {

		}

		public void visitInnerClass(
				final String name,
				final String outerName,
				final String innerName,
				final int access) {

		}

		public FieldVisitor visitField(
				final int access,
				final String name,
				final String desc,
				final String signature,
				final Object value) {
			return null;
		}

		public MethodVisitor visitMethod(
				final int access,
				final String name,
				final String desc,
				final String signature,
				final String[] exceptions) {
			// TODO
			return null;
		}

		public void visitEnd() {

		}

	}

}
