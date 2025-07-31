package org.example.unogame.model.fileHanldlers;

import java.io.IOException;

/**
 * Defines methods for serializing and deserializing game objects to and from files.
 * <p>
 * Implementations of this interface should handle how game data is persisted
 * (e.g., saving the current game state) and restored.
 * </p>
 */
public interface ISerializableFileHandler {

    /**
     * Serializes the given object and writes it to the specified file.
     *
     * @param fileName the name (or path) of the file to write the serialized object to
     * @param obj the object to be serialized
     */
    void serialize(String fileName, Object obj);

    /**
     * Deserializes an object from the specified file.
     *
     * @param fileName the name (or path) of the file to read the object from
     * @return the deserialized object
     * @throws IOException if an I/O error occurs while reading the file
     * @throws ClassNotFoundException if the class of the serialized object cannot be found
     */
    Object deserialize(String fileName) throws IOException, ClassNotFoundException;
}
