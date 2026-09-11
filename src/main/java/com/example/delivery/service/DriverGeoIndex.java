package com.example.delivery.service;

import com.example.delivery.model.DeliveryDriver;
import com.example.delivery.model.Location;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Grid index so nearby-driver queries do not scan every driver.
 */
final class DriverGeoIndex {

    private static final double KM_PER_DEGREE_LAT = 111.32;

    private final double cellSizeDegrees;
    private final Map<Long, List<DeliveryDriver>> cells = new HashMap<>();

    DriverGeoIndex(Collection<DeliveryDriver> drivers, double cellSizeKm) {
        Objects.requireNonNull(drivers, "drivers");
        if (cellSizeKm <= 0) {
            throw new IllegalArgumentException("cellSizeKm must be positive");
        }
        this.cellSizeDegrees = cellSizeKm / KM_PER_DEGREE_LAT;
        for (DeliveryDriver driver : drivers) {
            Location location = driver.getLocation();
            cells.computeIfAbsent(cellKey(location.getLatitude(), location.getLongitude()), key -> new ArrayList<>())
                    .add(driver);
        }
    }

    List<DeliveryDriver> withinRadius(Location center, double radiusKm) {
        Objects.requireNonNull(center, "center");
        if (radiusKm < 0) {
            throw new IllegalArgumentException("radiusKm must be non-negative");
        }

        double lat = center.getLatitude();
        double dLat = radiusKm / KM_PER_DEGREE_LAT;
        double cosLat = Math.cos(Math.toRadians(lat));
        double kmPerDegreeLon = KM_PER_DEGREE_LAT * Math.max(Math.abs(cosLat), 0.01);
        double dLon = radiusKm / kmPerDegreeLon;

        int minLatCell = latCell(lat - dLat);
        int maxLatCell = latCell(lat + dLat);
        int minLonCell = lonCell(center.getLongitude() - dLon);
        int maxLonCell = lonCell(center.getLongitude() + dLon);

        List<DeliveryDriver> matches = new ArrayList<>();
        for (int latCell = minLatCell; latCell <= maxLatCell; latCell++) {
            for (int lonCell = minLonCell; lonCell <= maxLonCell; lonCell++) {
                List<DeliveryDriver> bucket = cells.get(key(latCell, lonCell));
                if (bucket == null) {
                    continue;
                }
                for (DeliveryDriver driver : bucket) {
                    if (center.distanceTo(driver.getLocation()) <= radiusKm) {
                        matches.add(driver);
                    }
                }
            }
        }
        return matches;
    }

    List<DeliveryDriver> allDrivers() {
        List<DeliveryDriver> all = new ArrayList<>();
        for (List<DeliveryDriver> bucket : cells.values()) {
            all.addAll(bucket);
        }
        return all;
    }

    private int latCell(double latitude) {
        return (int) Math.floor(latitude / cellSizeDegrees);
    }

    private int lonCell(double longitude) {
        return (int) Math.floor(longitude / cellSizeDegrees);
    }

    private long cellKey(double latitude, double longitude) {
        return key(latCell(latitude), lonCell(longitude));
    }

    private static long key(int latCell, int lonCell) {
        return ((long) latCell << 32) | (lonCell & 0xFFFFFFFFL);
    }
}
