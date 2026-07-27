-- Flyway migration: order notes on orders table
ALTER TABLE orders ADD COLUMN IF NOT EXISTS order_notes VARCHAR(500);
