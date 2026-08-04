-- Idempotent commission rules for production (mirrors local CommissionRuleSeeder)
INSERT INTO commission_rules (name, fabric_type_id, commission_percentage, enabled, created_at, updated_at)
SELECT 'Pure Cotton Saree', ft.id, 8.00, 1, NOW(6), NOW(6)
FROM fabric_types ft
WHERE ft.slug = 'pure-cotton'
  AND NOT EXISTS (
      SELECT 1 FROM commission_rules cr
      WHERE cr.name = 'Pure Cotton Saree' AND cr.fabric_type_id = ft.id
  );

INSERT INTO commission_rules (name, fabric_type_id, commission_percentage, enabled, created_at, updated_at)
SELECT 'Cotton Silk Saree', ft.id, 10.00, 1, NOW(6), NOW(6)
FROM fabric_types ft
WHERE ft.slug = 'cotton-silk'
  AND NOT EXISTS (
      SELECT 1 FROM commission_rules cr
      WHERE cr.name = 'Cotton Silk Saree' AND cr.fabric_type_id = ft.id
  );

INSERT INTO commission_rules (name, fabric_type_id, commission_percentage, enabled, created_at, updated_at)
SELECT 'Pure Silk Saree', ft.id, 12.00, 1, NOW(6), NOW(6)
FROM fabric_types ft
WHERE ft.slug = 'pure-silk'
  AND NOT EXISTS (
      SELECT 1 FROM commission_rules cr
      WHERE cr.name = 'Pure Silk Saree' AND cr.fabric_type_id = ft.id
  );
