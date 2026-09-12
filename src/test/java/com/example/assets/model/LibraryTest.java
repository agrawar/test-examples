package com.example.assets.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class LibraryTest {

    private static final Instant NOW = Instant.parse("2026-09-12T12:00:00Z");

    @Test
    void storesUserIdAndAssetPropertiesById() {
        Asset banner = asset("asset-1", "herobanner.png");
        Library library = new Library("user-9", Map.of("asset-1", banner));

        assertEquals("user-9", library.userId());
        assertTrue(library.contains("asset-1"));
        assertEquals(banner, library.asset("asset-1"));
        assertEquals(Map.of("asset-1", banner), library.assetsById());
    }

    @Test
    void addsAssetWithProperties() {
        Library library = new Library("user-9");
        Asset clip = asset("asset-2", "HEROClip.mp4");

        library.addAsset(clip);

        assertTrue(library.contains("asset-2"));
        assertEquals(Set.of("asset-2"), library.assetIds());
        assertEquals(clip, library.asset("asset-2"));
    }

    @Test
    void removesAssetAndItsProperties() {
        Asset banner = asset("asset-1", "herobanner.png");
        Library library = new Library("user-9", Map.of("asset-1", banner));

        library.removeAsset("asset-1");

        assertFalse(library.contains("asset-1"));
        assertNull(library.asset("asset-1"));
        assertEquals(Set.of(), library.assetIds());
    }

    @Test
    void requiresUserIdWhenNull() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new Library(null));

        assertEquals("userId is required", error.getMessage());
    }

    @Test
    void requiresUserIdWhenBlank() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new Library(" "));

        assertEquals("userId is required", error.getMessage());
    }

    @Test
    void rejectsNullAsset() {
        Library library = new Library("user-9");

        assertThrows(NullPointerException.class, () -> library.addAsset(null));
    }

    @Test
    void rejectsAssetForDifferentUser() {
        Library library = new Library("user-9");
        Asset otherUserAsset = new Asset("asset-3", "other.png", "user-2", AssetType.IMAGE, NOW);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> library.addAsset(otherUserAsset));

        assertEquals("asset userId must match library userId", error.getMessage());
    }

    @Test
    void rejectsDuplicateAssetId() {
        Asset banner = asset("asset-1", "herobanner.png");
        Library library = new Library("user-9", Map.of("asset-1", banner));

        assertThrows(IllegalArgumentException.class, () -> library.addAsset(banner));
    }

    @Test
    void rejectsMapKeyThatDoesNotMatchAssetId() {
        Asset banner = asset("asset-1", "herobanner.png");

        assertThrows(IllegalArgumentException.class,
                () -> new Library("user-9", Map.of("wrong-id", banner)));
    }

    @Test
    void constructorRejectsMoreThanMaxAssets() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new Library("user-9", assets(1001)));

        assertEquals("library is full. please remove an asset to add a new one.", error.getMessage());
    }

    @Test
    void constructorAcceptsMaxAssets() {
        Library library = new Library("user-9", assets(1000));

        assertEquals(1000, library.assetIds().size());
    }

    @Test
    void addAssetRejectsWhenLibraryIsFull() {
        Library library = new Library("user-9", assets(1000));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> library.addAsset(asset("asset-extra", "extra.png")));

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
