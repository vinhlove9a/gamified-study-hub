package com.gamifiedstudyhub.backend.platform.storage;

/** Metadata for a stored blob. */
public record StoredObject(String key, long size, String contentType) {
}
