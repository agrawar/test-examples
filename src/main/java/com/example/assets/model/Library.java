package com.example.assets.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class Library {
    private final String userId;
    private final Set<String> assetIds;

    public Library(String userId) {
        this(userId, Set.of());
    }

    public Library(String userId, Set<String> assetIds) {
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.assetIds = new LinkedHashSet<>(Objects.requireNonNull(assetIds, "assetIds is required"));
    }

    public String userId() {
        return userId;
    }

    public Set<String> assetIds() {
        return Collections.unmodifiableSet(assetIds);
    }

    public void addAsset(String assetId) {
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("assetId is required. please provide a valid assetId.");
        }
        if (assetIds.contains(assetId)) {
            throw new IllegalArgumentException("assetId already exists. pleasee provide a new assetId.");
        }
        if (assetIds.size() >= 1000) {
            throw new IllegalArgumentException("library is full. please remove an asset to add a new one.");
        }
        assetIds.add(assetId);
    }
    
    public void removeAsset(String assetId) {
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("assetId is required. please provide a valid assetId.");
        }
        if (!assetIds.contains(assetId)) {
            throw new IllegalArgumentException("assetId does not exist. please provide a valid assetId.");
        }
        assetIds.remove(assetId);
    }

    public boolean contains(String assetId) {
        return assetIds.contains(assetId);
    }
}
