package org.rsbot.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.rsbot.Application;
import org.rsbot.bot.Bot;
import org.rsbot.util.PreferenceData;
import org.rsbot.util.UIDData;

public class RandomAccessFile {
	private UIDData uidData = null;
	private PreferenceData prefData = null;
	private java.io.RandomAccessFile raf = null;
	private Client client = null;

	private byte[] data = null;
	private int offset = 0;

	public RandomAccessFile(String name, String mode) throws FileNotFoundException 
	{
		if(!shouldOverride(name, mode))
			this.raf = new java.io.RandomAccessFile(name, mode);
	}

	public RandomAccessFile(File file, String mode) throws FileNotFoundException 
	{
		if(!shouldOverride(file.getName(), mode))
			this.raf = new java.io.RandomAccessFile(file, mode);
	}

	private boolean shouldOverride(String filename, String mode) throws FileNotFoundException
	{
		if(filename.equals("random.dat"))
			uidData = new UIDData();
		else if(filename.endsWith("preferences.dat"))
			prefData = new PreferenceData(1);
		else if(filename.endsWith("preferences2.dat"))
			prefData = new PreferenceData(2);
		else if(filename.endsWith("preferences3.dat"))
			prefData = new PreferenceData(3);
		else
			return false;

		return true;
	}

	private void checkData()
	{
		if(uidData != null)
		{	
			if(this.client == null) {
				Bot b = Application.getBot(this);
				this.client = b.getClient();
			}
			String accountName = client != null ? client.getCurrentUsername() : "";	

			if(!uidData.getLastUsed().equals(accountName) && data != null) {
				uidData.setUID(uidData.getLastUsed(), data);
				data = uidData.getUID(accountName);
				offset = 0;
			} else if(data == null) {
				data = uidData.getUID(accountName);
				offset = 0;
			}
		}
		else if(prefData != null && data == null)
			data = prefData.get();
	}

	private void saveData()
	{
		if(uidData != null && data !=  null)
		{
			uidData.setUID(client != null ? client.getCurrentUsername() : "", data);
			uidData.save();
		}
		else if(prefData != null && data != null)
			prefData.set(data);
	}

	public void close() throws IOException {
		if(raf != null)
			raf.close();
	}

	public long length() throws IOException {
		checkData();

		if(data != null)
			return data.length;

		return raf.length();
	}

	public int read() throws IOException {
		try	{
			checkData();

			if(data != null) {
				if(data.length <= offset)
					return -1;

				return (0xFF & data[offset++]);
			}

			return raf.read();
		}catch(Exception e) { 
			e.printStackTrace(); 
		}

		return -1;
	}

	public int read(byte[] b, int off, int len) throws IOException 
	{
		checkData();

		if(data != null)
		{
			try {
				if(b.length < off + len)
					len = b.length - off;

				if(data.length < offset + len)
					len = data.length - offset;

				if(len <= 0)
					return -1;

				for(int i = 0; i < len; i++)
					b[off + i] = data[offset++];

				return len;
			} catch(Exception e) {e.printStackTrace(); }
		}

		return raf.read(b, off, len);
	}

	public void seek(long pos) throws IOException {
		checkData();

		if(pos < 0)
			throw new IOException("pos < 0");

		if(data != null)
			offset = (int) pos;
		else
			raf.seek(pos);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		checkData();

		if(data != null)
		{
			//Check arguments
			if(b.length < off + len)
				len = b.length - off;

			if(len <= 0)
				return;

			//Increase buffer if needed
			if(data.length < offset + len)
			{
				byte[] tmp = data;
				data = new byte[offset + len];
				for(int i = 0; i < (offset <= tmp.length ? offset : tmp.length); i ++)
					data[i] = tmp[i];
			}

			//Write bytes
			for(int i = 0; i < len; i++)
				data[offset++] = b[off + i];

			saveData();
		}
		else
			raf.write(b, off, len);
	}

	public void write(int b) throws IOException {
		checkData();

		if(data != null)
		{
			//Increase bufer if needed
			if(data.length < offset + 1)
			{
				byte[] tmp = data;
				data = new byte[offset + 1];
				for(int i = 0; i < (offset <= tmp.length ? offset : tmp.length); i ++)
					data[i] = tmp[i];
			}

			//Write byte
			data[offset++] = (byte) b;
			saveData();
		}
		else
			raf.write(b);
	}
};