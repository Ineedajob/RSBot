package org.rsbot.injector;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Qauters
 */
class HookData {

	static class CharData {
		byte[] c;
		byte[] i;
	}

	static class ClassData {
		String official_name;
		String injected_name;
	}

	static class FieldData {
		String official_class_name;
		String official_field_name;
		String injected_field_name;
		String injected_field_signature;
	}

	static class StaticFieldData {
		String official_class_name;
		String official_field_name;
		String injected_field_name;
		String injected_field_signature;
	}

	static class RSObjectsData {
		String method_name;
		String method_signature;
		String[] object_class_names;
	}

	static class MasterXYData {
		String class_name;
		String method_name;
		String method_signature;
		int append_index;
		int aload;
		int iload_x;
		int iload_y;
	}

	static class RenderData {
		int append_index;

		String class_name;
		String method_name;
		String method_signature;

		String render_class_name;
		String render_field_name;
		String render_field_signature;

		String renderData_class_name;
		String renderData_field_name;
		String renderData_field_signature;
	}

	static class MessageEventData {
		String class_name;
		String method_name;
		String method_signature;
		int append_index;
		int id;
		int sender;
		int message;
	}

	private static class Stream {
		byte[] data;
		int off;

		Stream(byte[] data) {
			this.data = data;
		}

		int readByte() {
			return (0xFF & data[off++]);
		}

		int readWord() {
			return (((0xFF & data[off++]) << 8) +
					((0xFF & data[off++])));
		}

		String readString() {
			String s = "";
			while (data[off] != 0) {
				s += String.valueOf((char) data[off++]);
			}
			off++;
			return s;
		}
	}

	ArrayList<ClassData> classes = new ArrayList<ClassData>();
	ArrayList<FieldData> fields = new ArrayList<FieldData>();
	ArrayList<StaticFieldData> staticFields = new ArrayList<StaticFieldData>();

	MasterXYData masterXY = new MasterXYData();
	MessageEventData messageEvent = new MessageEventData();
	RSObjectsData rsObjects = new RSObjectsData();
	RenderData render = new RenderData();
	CharData charData = new CharData();

	int version;

	HookData(byte[] data) {
		//Initialize stream
		Stream s_data = new Stream(data);

		//Read version
		version = s_data.readWord();

		//Read classes
		int classes_length = s_data.readWord();
		for (int i = 0; i < classes_length; i++) {
			ClassData c = new ClassData();
			c.injected_name = s_data.readString();
			c.official_name = s_data.readString();

			classes.add(c);
		}

		//Read fields
		int fields_length = s_data.readWord();
		for (int i = 0; i < fields_length; i++) {
			FieldData f = new FieldData();
			f.injected_field_name = s_data.readString();
			f.injected_field_signature = s_data.readString();
			f.official_class_name = s_data.readString();
			f.official_field_name = s_data.readString();

			fields.add(f);
		}

		//Read Static fields
		int static_fields_length = s_data.readWord();
		for (int i = 0; i < static_fields_length; i++) {
			StaticFieldData f = new StaticFieldData();
			f.injected_field_name = s_data.readString();
			f.injected_field_signature = s_data.readString();
			f.official_class_name = s_data.readString();
			f.official_field_name = s_data.readString();

			staticFields.add(f);
		}

		//Read master x/y info
		masterXY.class_name = s_data.readString();
		masterXY.method_name = s_data.readString();
		masterXY.method_signature = s_data.readString();
		masterXY.append_index = s_data.readWord();
		masterXY.aload = s_data.readByte();
		masterXY.iload_x = s_data.readByte();
		masterXY.iload_y = s_data.readByte();

		//Read server message listener info
		messageEvent.class_name = s_data.readString();
		messageEvent.method_name = s_data.readString();
		messageEvent.method_signature = s_data.readString();
		messageEvent.append_index = s_data.readWord();
		messageEvent.id = s_data.readByte();
		messageEvent.sender = s_data.readByte();
		messageEvent.message = s_data.readByte();

		//Read RSObjects info
		rsObjects.method_name = s_data.readString();
		rsObjects.method_signature = s_data.readString();
		rsObjects.object_class_names = new String[s_data.readByte()];
		for (int i = 0; i < rsObjects.object_class_names.length; i++)
			rsObjects.object_class_names[i] = s_data.readString();

		//Read update render data info
		render.append_index = s_data.readWord();

		render.class_name = s_data.readString();
		render.method_name = s_data.readString();
		render.method_signature = s_data.readString();

		render.render_class_name = s_data.readString();
		render.render_field_name = s_data.readString();
		render.render_field_signature = s_data.readString();

		render.renderData_class_name = s_data.readString();
		render.renderData_field_name = s_data.readString();
		render.renderData_field_signature = s_data.readString();

		//Read tiles height info (deprecated)
		s_data.readString();
		s_data.readString();

		//Read char data
		int c_length = s_data.readByte();
		charData.c = new byte[c_length];
		charData.c = Arrays.copyOfRange(s_data.data, s_data.off, s_data.off + c_length);
		s_data.off += c_length;

		int i_length = s_data.readByte();
		charData.i = new byte[i_length];
		charData.i = Arrays.copyOfRange(s_data.data, s_data.off, s_data.off + i_length);
		s_data.off += i_length;
	}
}
