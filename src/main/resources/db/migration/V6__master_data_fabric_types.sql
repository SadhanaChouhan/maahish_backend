CREATE TABLE IF NOT EXISTS fabric_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) UNIQUE,
    description VARCHAR(500),
    active BIT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

ALTER TABLE products ADD COLUMN IF NOT EXISTS fabric_type_id BIGINT;
ALTER TABLE commission_rules ADD COLUMN IF NOT EXISTS fabric_type_id BIGINT;

INSERT INTO categories (name, slug, description, active, created_at, updated_at)
SELECT 'Maheshwari Sarees', 'maheshwari-sarees', 'Authentic handwoven Maheshwari sarees', 1, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'maheshwari-sarees');

INSERT INTO categories (name, slug, description, active, created_at, updated_at)
SELECT 'Maheshwari Dress Material', 'maheshwari-dress-material', 'Premium Maheshwari dress materials for custom tailoring', 1, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'maheshwari-dress-material');

INSERT INTO fabric_types (name, slug, description, active, created_at, updated_at)
SELECT 'Cotton Silk', 'cotton-silk', 'Classic Maheshwari cotton silk blend', 1, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM fabric_types WHERE slug = 'cotton-silk');

INSERT INTO fabric_types (name, slug, description, active, created_at, updated_at)
SELECT 'Pure Cotton', 'pure-cotton', 'Breathable pure cotton weave', 1, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM fabric_types WHERE slug = 'pure-cotton');

INSERT INTO fabric_types (name, slug, description, active, created_at, updated_at)
SELECT 'Pure Silk', 'pure-silk', 'Luxurious pure silk', 1, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM fabric_types WHERE slug = 'pure-silk');

INSERT INTO fabric_types (name, slug, description, active, created_at, updated_at)
SELECT 'Tissue', 'tissue', 'Lightweight tissue fabric', 1, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM fabric_types WHERE slug = 'tissue');

INSERT INTO fabric_types (name, slug, description, active, created_at, updated_at)
SELECT 'Garbh Rashmi', 'garbh-rashmi', 'Traditional Garbh Rashmi weave', 1, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM fabric_types WHERE slug = 'garbh-rashmi');
