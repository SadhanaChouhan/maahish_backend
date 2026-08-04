-- Soft-reserve inventory during pending checkout (released on expiry/cancel, deducted on fulfill)
ALTER TABLE products
    ADD COLUMN reserved_stock INT NOT NULL DEFAULT 0 AFTER stock;
