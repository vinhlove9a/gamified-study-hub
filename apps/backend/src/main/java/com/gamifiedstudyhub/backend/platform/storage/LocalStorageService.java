package com.gamifiedstudyhub.backend.platform.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

/**
 * Filesystem-backed {@link StorageService} for local/dev use. Keys are treated as
 * relative paths under {@code app.storage.local.base-path}; traversal outside the
 * base directory is rejected. Cannot presign, so {@link #presignedGetUrl} is null.
 */
public class LocalStorageService implements StorageService {

    private final Path base;

    public LocalStorageService(StorageProperties properties) {
        this.base = Path.of(properties.getLocal().getBasePath()).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject put(String key, InputStream content, long contentLength, String contentType) {
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
            long size = Files.size(target);
            return new StoredObject(key, size, contentType);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store object: " + key, e);
        }
    }

    @Override
    public InputStream get(String key) {
        try {
            return Files.newInputStream(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read object: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete object: " + key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        return Files.exists(resolve(key));
    }

    @Override
    public String presignedGetUrl(String key, Duration ttl) {
        // Local disk cannot hand out a direct URL; callers stream via get(key).
        return null;
    }

    private Path resolve(String key) {
        Path resolved = base.resolve(key).normalize();
        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException("Illegal storage key (path traversal): " + key);
        }
        return resolved;
    }
}
