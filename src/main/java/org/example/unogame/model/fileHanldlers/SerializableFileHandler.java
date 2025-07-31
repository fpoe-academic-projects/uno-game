package org.example.unogame.model.fileHanldlers;

import java.io.*;

public class SerializableFileHandler implements ISerializableFileHandler {

    @Override
    public void serialize(String fileName, Object obj) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(obj);

        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object deserialize(String fileName) throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            ObjectInputStream in = new ObjectInputStream(fis);

            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IOException("Error loading file: " + fileName, e);
        }
    }
} 