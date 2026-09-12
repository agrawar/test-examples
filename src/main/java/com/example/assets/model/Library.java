package com.example.assets.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class Library {
    private static final int MAX_ASSETS = 1000;

    private final String userId;
    private final Map<String, Asset> assetsById;

    public Library(String userId) {
        this(userId, Map.of());
    }

    public Library(String userId, Map<String, Asset> assetsById) {
        this.userId = required("userId", userId);
        this.assetsById = new LinkedHashMap<>();
        Objects.requireNonNull(assetsById, "assetsById is required");
        if (assetsById.size() > MAX_ASSETS) {
            throw new IllegalArgumentException("library is full. please remove an asset to add a new one.");
        }
        assetsById.forEach(this::put);
    }

    public String userId() {
        return userId;
    }

    public Map<String, Asset> assetsById() {
        return Collections.unmodifiableMap(assetsById);
    }

    public Set<String> assetIds() {
        return Collections.unmodifiableSet(assetsById.keySet());
    }

    public Asset asset(String assetId) {
        return assetsById.get(assetId);
    }

    public void addAsset(Asset asset) {
        Objects.requireNonNull(asset, "asset is required");
        if (assetsById.containsKey(asset.assetId())) {
            throw new IllegalArgumentException("assetId already exists. pleasee provide a new assetId.");
        }
        if (assetsById.size() >= MAX_ASSETS) {
            throw new IllegalArgumentException("library is full. please remove an asset to add a new one.");
        }
        put(asset.assetId(), asset);
    }

    public void removeAsset(String assetId) {
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("assetId is required. please provide a valid assetId.");
        }
        if (!assetsById.containsKey(assetId)) {
            throw new IllegalArgumentException("assetId does not exist. please provide a valid assetId.");
        }
        assetsById.remove(assetId);
    }

    public boolean contains(String assetId) {
        return assetsById.containsKey(assetId);
    }

    private void put(String assetId, Asset asset) {
        Objects.requireNonNull(asset, "asset is required");
        if (!assetId.equals(asset.assetId())) {
            throw new IllegalArgumentException("map key must match assetId");
        }
        if (!userId.equals(asset.userId())) {
            throw new IllegalArgumentException("asset userId must match library userId");
        }
        assetsById.put(assetId, asset);
    }

    private static String required(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
