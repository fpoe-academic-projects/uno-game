package org.example.unogame.model.fileHanldlers;

import java.io.IOException;

public interface ISerializableFileHandler {

    void serialize(String fileName, Object obj);

    Object deserialize (String fileName) throws IOException, ClassNotFoundException;
} 