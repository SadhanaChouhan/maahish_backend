package com.maahish.catalog.util;

import com.maahish.catalog.enums.InventoryStatus;

public final class InventoryStatusUtil {

    public static final int LOW_STOCK_THRESHOLD = 5;

    private InventoryStatusUtil() {
    }

    public static InventoryStatus resolve(Integer stock) {
        if (stock == null || stock <= 0) {
            return InventoryStatus.OUT_OF_STOCK;
        }
        if (stock <= LOW_STOCK_THRESHOLD) {
            return InventoryStatus.LOW_STOCK;
        }
        return InventoryStatus.IN_STOCK;
    }
}
