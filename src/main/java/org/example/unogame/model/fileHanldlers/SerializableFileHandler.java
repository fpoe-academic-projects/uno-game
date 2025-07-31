package org.example.unogame.model.fileHanldlers;

import java.io.*;

/**
 * Implements {@link ISerializableFileHandler} using Java's built-in object serialization.
 * <p>
 * This class provides functionality to save and load serializable objects (such as game state)
 * using standard file I/O streams.
 * </p>
 */
public class SerializableFileHandler implements ISerializableFileHandler {

    /**
     * Serializes the given object and writes it to the specified file using {@link ObjectOutputStream}.
     *
     * @param fileName the name or path of the file where the object will be stored
     * @param obj the object to be serialized
     */
    @Override
    public void serialize(String fileName, Object obj) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace(); // Consider replacing with proper logging if needed
        }
    }

    /**
     * Deserializes an object from the specified file using {@link ObjectInputStream}.
     *
     * @param fileName the name or path of the file from which the object will be loaded
     * @return the deserialized object
     * @throws IOException if there is an error reading the file or creating the input stream
     * @throws ClassNotFoundException if the class of the serialized object cannot be found
     */
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
