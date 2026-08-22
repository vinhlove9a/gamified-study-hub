package com.gamifiedstudyhub.backend.platform.storage;

import java.io.InputStream;
import java.time.Duration;

/**
 * Blob-storage abstraction. Two implementations ship pre-wired:
 * <ul>
 *   <li>{@link LocalStorageService} — writes to the local filesystem, the default
 *       ({@code app.storage.provider=local}); needs no cloud credentials.</li>
 *   <li>{@link S3StorageService} — AWS S3 / Cloudflare R2 / MinIO (S3-compatible),
 *       active when {@code app.storage.provider=s3}.</li>
 * </ul>
 *
 * <p>Consumers depend only on this interface, so switching providers is a config
 * change with no code impact. Wired by {@link StorageConfig}.
 */
public interface StorageService {

    /** Store {@code content} under {@code key}, returning metadata about what was written. */
    StoredObject put(String key, InputStream content, long contentLength, String contentType);

    /** Open the stored object's bytes for reading. Caller closes the stream. */
    InputStream get(String key);

    /** Delete the object; a no-op when it does not exist. */
    void delete(String key);

    /** Whether an object exists at {@code key}. */
    boolean exists(String key);

    /**
     * A time-limited direct-download URL when the backend supports it (S3 presigned
     * URL). Returns {@code null} for backends that cannot presign (e.g. local disk),
     * so callers must fall back to streaming through {@link #get(String)}.
     */
    String presignedGetUrl(String key, Duration ttl);
}
