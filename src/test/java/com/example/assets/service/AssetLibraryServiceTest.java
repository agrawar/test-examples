package com.example.assets.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.assets.model.Asset;
import com.example.assets.model.AssetType;
import com.example.assets.model.Library;

class AssetLibraryServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-12T12:00:00Z");

    private AssetLibraryService service;

    @BeforeEach
    void setUp() {
        service = new AssetLibraryService();
    }

    @Test
    void createsLibraryForUser() {
        Library library = service.createLibrary("user-9");

        assertEquals("user-9", library.userId());
        assertEquals(service.library("user-9"), library);
    }

    @Test
    void createsLibraryWithAssetProperties() {
        Asset banner = asset("asset-1", "herobanner.png");

        Library library = service.createLibrary("user-9", Map.of("asset-1", banner));

        assertTrue(service.contains("user-9", "asset-1"));
        assertEquals(banner, service.asset("user-9", "asset-1"));
        assertEquals(Map.of("asset-1", banner), library.assetsById());
    }

    @Test
    void addsAssetToUserLibrary() {
        service.createLibrary("user-9");
        Asset clip = asset("asset-2", "HEROClip.mp4");

        service.addAsset(clip);

        assertTrue(service.contains("user-9", "asset-2"));
        assertEquals(Set.of("asset-2"), service.library("user-9").assetIds());
        assertEquals(clip, service.asset("user-9", "asset-2"));
    }

    @Test
    void removesAssetFromUserLibrary() {
        Asset banner = asset("asset-1", "herobanner.png");
        service.createLibrary("user-9", Map.of("asset-1", banner));

        service.removeAsset("user-9", "asset-1");

        assertFalse(service.contains("user-9", "asset-1"));
        assertNull(service.asset("user-9", "asset-1"));
        assertEquals(Set.of(), service.library("user-9").assetIds());
    }

    @Test
    void rejectsDuplicateLibraryForUser() {
        service.createLibrary("user-9");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.createLibrary("user-9"));

        assertEquals("library already exists for userId", error.getMessage());
    }

    @Test
    void rejectsAddWhenLibraryIsMissing() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.addAsset(asset("asset-1", "herobanner.png")));

        assertEquals("library does not exist for userId", error.getMessage());
    }

    @Test
    void rejectsNullAsset() {
        service.createLibrary("user-9");

        assertThrows(NullPointerException.class, () -> service.addAsset(null));
    }

    @Test
    void rejectsAssetForDifferentUser() {
        service.createLibrary("user-9");
        Asset otherUserAsset = new Asset("asset-3", "other.png", "user-2", AssetType.IMAGE, NOW);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.addAsset(otherUserAsset));

        assertEquals("library does not exist for userId", error.getMessage());
    }

    @Test
    void rejectsDuplicateAssetId() {
        Asset banner = asset("asset-1", "herobanner.png");
        service.createLibrary("user-9", Map.of("asset-1", banner));

        assertThrows(IllegalArgumentException.class, () -> service.addAsset(banner));
    }

    @Test
    void createLibraryRejectsMoreThanMaxAssets() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.createLibrary("user-9", assets(1001)));

        assertEquals("library is full. please remove an asset to add a new one.", error.getMessage());
    }

    @Test
    void addAssetRejectsWhenLibraryIsFull() {
        service.createLibrary("user-9", assets(1000));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.addAsset(asset("asset-extra", "extra.png")));

        assertEquals("library is full. please remove an asset to add a new one.", error.getMessage());
    }

    private static Map<String, Asset> assets(int count) {
        Map<String, Asset> assets = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            Asset item = asset("asset-" + i, "file" + i + ".png");
            assets.put(item.assetId(), item);
        }
        return assets;
    }

    private static Asset asset(String assetId, String name) {
        return new Asset(assetId, name, "user-9", AssetType.IMAGE, NOW);
    }
}
