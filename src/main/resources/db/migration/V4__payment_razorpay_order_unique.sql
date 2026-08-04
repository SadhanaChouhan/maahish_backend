-- Prevent duplicate Payment rows for the same Razorpay order (callback + webhook race)
ALTER TABLE payments
    ADD UNIQUE KEY uk_payments_razorpay_order_id (razorpay_order_id);
