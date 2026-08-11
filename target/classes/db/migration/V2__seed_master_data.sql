-- Production-safe master data seeds (idempotent).

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

INSERT INTO shipping_rules (rule_name, rule_type, shipping_charge, minimum_order_amount, is_first_order_only, priority, active, created_at, updated_at)
SELECT 'Default Shipping Charge', 'DEFAULT_CHARGE', 99.00, NULL, 0, 100, 1, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM shipping_rules WHERE rule_type = 'DEFAULT_CHARGE');

INSERT INTO shipping_rules (rule_name, rule_type, shipping_charge, minimum_order_amount, is_first_order_only, priority, active, created_at, updated_at)
SELECT 'First Order Free Delivery', 'FIRST_ORDER_FREE', 0.00, NULL, 1, 10, 1, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM shipping_rules WHERE rule_type = 'FIRST_ORDER_FREE');

INSERT INTO shipping_rules (rule_name, rule_type, shipping_charge, minimum_order_amount, is_first_order_only, priority, active, created_at, updated_at)
SELECT 'Free Shipping Above Minimum Order', 'MINIMUM_ORDER_FREE', 0.00, 4999.00, 0, 20, 1, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM shipping_rules WHERE rule_type = 'MINIMUM_ORDER_FREE');
