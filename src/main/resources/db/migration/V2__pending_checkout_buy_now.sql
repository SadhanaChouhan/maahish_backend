-- Buy-now checkout flag: when true, cart is not cleared after payment
ALTER TABLE pending_checkouts ADD COLUMN IF NOT EXISTS buy_now BIT(1) NOT NULL DEFAULT 0;
