package com.josyantl.joblens.document.infrastructure.storage;

import com.josyantl.joblens.document.application.DocumentStorage;
import com.josyantl.joblens.document.application.exception.DocumentStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.StandardOpenOption;
import java.util.Set;

@Component
public class LocalDocumentStorage implements DocumentStorage {
    private static final Set<PosixFilePermission> ROOT_PERMISSIONS = PosixFilePermissions.fromString("rwx------");
    private static final Set<PosixFilePermission> FILE_PERMISSIONS = PosixFilePermissions.fromString("rw-------");
    private final Path root;

    public LocalDocumentStorage(@Value("${joblens.documents.storage-root:${java.io.tmpdir}/joblens-documents}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    public void store(String key, byte[] content) {
        try {
            ensureSecureRoot();
            Path path = resolve(key);
            Files.write(path, content, StandardOpenOption.CREATE_NEW);
            ensurePermissions(path, FILE_PERMISSIONS);
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

    private void ensureSecureRoot() throws IOException {
        Files.createDirectories(root);
        ensurePermissions(root, ROOT_PERMISSIONS);
    }

    private void ensurePermissions(Path path, Set<PosixFilePermission> expected) throws IOException {
        if (!Files.getFileStore(path).supportsFileAttributeView("posix")) return;
        Set<PosixFilePermission> actual = Files.getPosixFilePermissions(path);
        if (!actual.equals(expected)) Files.setPosixFilePermissions(path, expected);
    }
}
