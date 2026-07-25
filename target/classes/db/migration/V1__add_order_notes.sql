-- Optional manual migration for existing databases (JPA ddl-auto=update also adds this column).
ALTER TABLE orders ADD COLUMN IF NOT EXISTS order_notes VARCHAR(500);
