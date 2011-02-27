package org.rsbot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Nader Sleiman
 */
public class Serializer {

    public static boolean serlialize(Object o, File f) {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(o);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found!");
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static Object deserialize(InputStream stream) {
        Object obj = null;
        try {

            ObjectInputStream ois = new ObjectInputStream(stream);
            obj = ois.readObject();
            ois.close();

        } catch (FileNotFoundException e) {
        } catch (IOException i) {
        } catch (ClassNotFoundException c) {
        }
        return obj;
    }

    public static Object deserialize(File f) {
        Object obj = null;
        try {
            FileInputStream fin = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fin);
            obj = ois.readObject();
            ois.close();
            fin.close();
        } catch (Exception e) {
        }
        return obj;
    }
}
