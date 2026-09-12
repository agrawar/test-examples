package com.example.assets.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.example.assets.model.Asset;
import com.example.assets.model.AssetType;

public class AssetQueryService {

    private final List<Asset> assets;

    public AssetQueryService(List<Asset> assets) {
        this.assets = List.copyOf(Objects.requireNonNull(assets, "assets is required"));
    }

    /**
     * Case-insensitive partial match on asset file names for the given user.
     */
    public List<Asset> searchByName(String userId, String partialName) {
        String query = partialName.toLowerCase(Locale.ROOT);
        List<Asset> matches = new ArrayList<>();
        for (Asset asset : assets) {
            if (userId.equals(asset.userId())
                    && asset.name().toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(asset);
            }
        }
        return matches;
    }

    /**
     * Returns the user's assets of the given type.
     */
    public List<Asset> filterByType(String userId, AssetType type) {
        List<Asset> matches = new ArrayList<>();
        for (Asset asset : assets) {
            if (userId.equals(asset.userId()) && type == asset.type()) {
                matches.add(asset);
            }
        }
        return matches;
    }

    /**
     * Returns the user's {@code count} most recently added assets, newest first.
     */
    public List<Asset> recentlyAdded(String userId, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        return assets.stream()
                .filter(asset -> userId.equals(asset.userId()))
                .sorted(Comparator.comparing(Asset::timestamp).reversed())
                .limit(count)
                .toList();
    }
}
