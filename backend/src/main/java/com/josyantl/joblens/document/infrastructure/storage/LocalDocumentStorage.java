package com.josyantl.joblens.document.infrastructure.storage;

import com.josyantl.joblens.document.application.DocumentStorage;
import com.josyantl.joblens.document.application.exception.DocumentStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class LocalDocumentStorage implements DocumentStorage {
    private final Path root;

    public LocalDocumentStorage(@Value("${joblens.documents.storage-root:${java.io.tmpdir}/joblens-documents}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    public void store(String key, byte[] content) {
        try {
            Files.createDirectories(root);
            Files.write(resolve(key), content, StandardOpenOption.CREATE_NEW);
        } catch (IOException exception) {
            throw new DocumentStorageException("Unable to store document", exception);
        }
    }

    @Override
    public byte[] load(String key) {
        try {
            return Files.readAllBytes(resolve(key));
        } catch (IOException exception) {
            throw new DocumentStorageException("Unable to read document", exception);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException exception) {
            throw new DocumentStorageException("Unable to delete document", exception);
        }
    }

    private Path resolve(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) throw new IllegalArgumentException("Invalid storage key");
        return path;
    }
}
