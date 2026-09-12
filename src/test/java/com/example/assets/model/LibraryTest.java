package com.example.assets.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class LibraryTest {

    @Test
    void storesUserIdAndAssetIds() {
        Library library = new Library("user-9", Set.of("asset-1"));

        assertEquals("user-9", library.userId());
        assertTrue(library.contains("asset-1"));
    }

    @Test
    void addsAssetId() {
        Library library = new Library("user-9");

        library.addAsset("asset-2");

        assertTrue(library.contains("asset-2"));
        assertEquals(Set.of("asset-2"), library.assetIds());
    }
    
    @Test
    void removesAssetId() {
        Library library = new Library("user-9", Set.of("asset-1"));

        library.removeAsset("asset-1");

        assertFalse(library.contains("asset-1"));
        assertEquals(Set.of(), library.assetIds());
    }
    
    @Test
    void requiresUserIdAndAssetId() {
        assertThrows(NullPointerException.class, () -> new Library(null));
        Library library = new Library("user-9");
        assertThrows(IllegalArgumentException.class, () -> library.addAsset(" "));
    }
}
