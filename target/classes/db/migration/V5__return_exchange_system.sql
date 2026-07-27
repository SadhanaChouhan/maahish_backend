ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivered_at DATETIME(6);

CREATE TABLE IF NOT EXISTS return_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    return_type VARCHAR(20) NOT NULL,
    reason VARCHAR(30) NOT NULL,
    description TEXT,
    preferred_resolution VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RETURN_REQUESTED',
    admin_remarks VARCHAR(1000),
    rejection_reason VARCHAR(1000),
    customer_remarks VARCHAR(1000),
    refund_amount DECIMAL(12, 2),
    exchange_tracking_number VARCHAR(100),
    delivered_at_snapshot DATETIME(6) NOT NULL,
    ship_reminder_sent BIT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_return_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_return_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id),
    CONSTRAINT fk_return_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_return_seller FOREIGN KEY (seller_id) REFERENCES sellers(id)
);

CREATE INDEX IF NOT EXISTS idx_return_customer ON return_requests(customer_id);
CREATE INDEX IF NOT EXISTS idx_return_seller ON return_requests(seller_id);
CREATE INDEX IF NOT EXISTS idx_return_status ON return_requests(status);

CREATE TABLE IF NOT EXISTS return_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_request_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    public_id VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_return_image_request FOREIGN KEY (return_request_id) REFERENCES return_requests(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS return_shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_request_id BIGINT NOT NULL UNIQUE,
    courier_company VARCHAR(30) NOT NULL,
    tracking_number VARCHAR(100) NOT NULL,
    dispatch_date DATE NOT NULL,
    receipt_url VARCHAR(500),
    receipt_public_id VARCHAR(255),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_return_shipment_request FOREIGN KEY (return_request_id) REFERENCES return_requests(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS refund_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_request_id BIGINT NOT NULL,
    razorpay_refund_id VARCHAR(100),
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    raw_response TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_refund_return FOREIGN KEY (return_request_id) REFERENCES return_requests(id)
);

CREATE TABLE IF NOT EXISTS return_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_request_id BIGINT NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    remarks VARCHAR(1000),
    changed_by_user_id BIGINT,
    changed_by_role VARCHAR(30),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_return_history_request FOREIGN KEY (return_request_id) REFERENCES return_requests(id) ON DELETE CASCADE
);
