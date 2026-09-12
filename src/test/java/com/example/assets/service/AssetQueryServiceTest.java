package com.example.assets.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.assets.model.Asset;
import com.example.assets.model.AssetType;

class AssetQueryServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-12T12:00:00Z");

    private Asset heroBanner;
    private Asset heroClip;
    private Asset launchGraphic;
    private Asset otherUserHero;
    private Asset oldPhoto;
    private AssetQueryService service;

    @BeforeEach
    void setUp() {
        heroBanner = new Asset("a1", "herobanner.png", "user-9", AssetType.IMAGE, NOW.minusSeconds(60));
        heroClip = new Asset("a2", "HEROClip.mp4", "user-9", AssetType.VIDEO, NOW.minusSeconds(120));
        launchGraphic = new Asset("a3", "launchgraphic.svg", "user-9", AssetType.GRAPHIC, NOW.minusSeconds(180));
        otherUserHero = new Asset("a4", "heroposter.png", "user-2", AssetType.IMAGE, NOW.minusSeconds(30));
        oldPhoto = new Asset("a5", "archivephoto.jpg", "user-9", AssetType.IMAGE, NOW.minusSeconds(10 * 24 * 60 * 60));

        service = new AssetQueryService(
                List.of(heroBanner, heroClip, launchGraphic, otherUserHero, oldPhoto));
    }

    @Test
    void searchByNameReturnsPartialMatches() {
        List<Asset> matches = service.searchByName("user-9", "banner");

        assertEquals(List.of(heroBanner), matches);
    }

    @Test
    void searchByNameIsCaseInsensitive() {
        List<Asset> matches = service.searchByName("user-9", "HERO");

        assertEquals(List.of(heroBanner, heroClip), matches);
    }

    @Test
    void filterByTypeReturnsAssetsOfThatType() {
        assertEquals(List.of(heroBanner, oldPhoto), service.filterByType("user-9", AssetType.IMAGE));
        assertEquals(List.of(heroClip), service.filterByType("user-9", AssetType.VIDEO));
        assertEquals(List.of(launchGraphic), service.filterByType("user-9", AssetType.GRAPHIC));
    }

    @Test
    void recentlyAddedReturnsNewestAssetsUpToCount() {
        List<Asset> matches = service.recentlyAdded("user-9", 2);

        assertEquals(List.of(heroBanner, heroClip), matches);
    }
}
