package com.example.assets.model;

import java.time.Instant;
import java.util.Objects;

/**
 * An uploaded customer file.
 *
 * @param name file name, including extension (for example {@code herobanner.png})
 */
public record Asset(
        String assetId,
        String name,
        String userId,
        AssetType type,
        Instant timestamp
) {
    public Asset {
        assetId = required("assetId", assetId);
        name = required("name", name);
        type = Objects.requireNonNull(type, "type is required");
        timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    private static String required(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
