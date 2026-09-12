package com.example.assets.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.example.assets.model.Asset;
import com.example.assets.model.Library;

public class AssetLibraryService {

    private final Map<String, Library> librariesByUserId = new LinkedHashMap<>();

    public Library createLibrary(String userId) {
        return createLibrary(userId, Map.of());
    }

    public Library createLibrary(String userId, Map<String, Asset> assetsById) {
        Library library = new Library(userId, assetsById);
        if (librariesByUserId.containsKey(library.userId())) {
            throw new IllegalArgumentException("library already exists for userId");
        }
        librariesByUserId.put(library.userId(), library);
        return library;
    }

    public Library library(String userId) {
        Library library = librariesByUserId.get(userId);
        if (library == null) {
            throw new IllegalArgumentException("library does not exist for userId");
        }
        return library;
    }

    public void addAsset(Asset asset) {
        Objects.requireNonNull(asset, "asset is required");
        Library library = librariesByUserId.get(asset.userId());
        if (library == null) {
            throw new IllegalArgumentException("library does not exist for userId");
        }
        library.addAsset(asset);
    }

    public void removeAsset(String userId, String assetId) {
        library(userId).removeAsset(assetId);
    }

    public boolean contains(String userId, String assetId) {
        Library library = librariesByUserId.get(userId);
        return library != null && library.contains(assetId);
    }

    public Asset asset(String userId, String assetId) {
        return library(userId).asset(assetId);
    }
}
