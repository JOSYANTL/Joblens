package com.josyantl.joblens.document.infrastructure.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class LocalDocumentStorageTest {
    private static final Set<PosixFilePermission> ROOT_PERMISSIONS = PosixFilePermissions.fromString("rwx------");
    private static final Set<PosixFilePermission> FILE_PERMISSIONS = PosixFilePermissions.fromString("rw-------");

    @TempDir Path tempDir;

    @Test
    void storeSecuresExistingRootAndStoredFilePermissions() throws Exception {
        assumeTrue(Files.getFileStore(tempDir).supportsFileAttributeView("posix"));
        Path root = tempDir.resolve("documents");
        Files.createDirectories(root);
        Files.setPosixFilePermissions(root, PosixFilePermissions.fromString("rwxr-xr-x"));

        LocalDocumentStorage storage = new LocalDocumentStorage(root.toString());
        storage.store("resume.pdf", "content".getBytes());

        assertThat(Files.getPosixFilePermissions(root)).isEqualTo(ROOT_PERMISSIONS);
        assertThat(Files.getPosixFilePermissions(root.resolve("resume.pdf"))).isEqualTo(FILE_PERMISSIONS);
    }
}
