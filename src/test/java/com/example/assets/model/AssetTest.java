package com.example.assets.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class AssetTest {

    @Test
    void createsAssetWithCallerProvidedId() {
        Instant createdAt = Instant.parse("2026-09-12T12:00:00Z");

        Asset asset = new Asset("asset-1", "herobanner.png", "user-9", AssetType.IMAGE, createdAt);

        assertEquals("asset-1", asset.assetId());
        assertEquals("herobanner.png", asset.name());
        assertEquals("user-9", asset.userId());
        assertEquals(AssetType.IMAGE, asset.type());
        assertEquals(createdAt, asset.timestamp());
    }

    @Test
    void defaultsTimestampWhenOmitted() {
        Asset asset = new Asset("asset-2", "launchclip.mp4", "user-9", AssetType.VIDEO, null);

        assertNotNull(asset.timestamp());
    }

    @Test
    void requiresAssetIdAndName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Asset(" ", "banner.svg", "user-9", AssetType.GRAPHIC, Instant.now()));
        assertThrows(IllegalArgumentException.class,
                () -> new Asset("asset-3", null, "user-9", AssetType.GRAPHIC, Instant.now()));
        assertThrows(NullPointerException.class,
                () -> new Asset("asset-3", "banner.svg", "user-9", null, Instant.now()));
    }
}
