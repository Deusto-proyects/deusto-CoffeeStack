-- Sprint 3 final: datos de demostración para arranque limpio.
-- NOTA: el usuario "admin/admin123" (ROOT) lo crea DataInitializer en arranque,
-- por eso aquí solo poblamos datos de negocio.

-- Proveedores
INSERT INTO proveedores (nombre, contacto, email, telefono, activo) VALUES
  ('Cafés del Mundo', 'Juan Pérez', 'ventas@cafesdelmundo.es', '944100100', TRUE),
  ('Lácteos Norte',    'Ana Ruiz',   'pedidos@lacteosnorte.es', '944200200', TRUE);

-- Insumos
INSERT INTO insumos (nombre, unidad_medida, stock_minimo_alerta, activo) VALUES
  ('Café molido',     'kg',       2.0,  TRUE),
  ('Leche entera',    'litros',   5.0,  TRUE),
  ('Azúcar blanco',   'kg',       1.0,  TRUE),
  ('Vasos cartón',    'unidades', 50.0, TRUE);

-- Lotes con precio de compra (para reporte de consumo con coste)
INSERT INTO lotes (insumo_id, proveedor_id, numero_lote, cantidad_inicial, cantidad_actual, fecha_vencimiento, precio_compra)
SELECT i.id, p.id, 'L-CAFE-001', 20.0, 17.0, CURRENT_DATE + INTERVAL '60 days', 18.50
FROM insumos i, proveedores p WHERE i.nombre = 'Café molido' AND p.nombre = 'Cafés del Mundo';

INSERT INTO lotes (insumo_id, proveedor_id, numero_lote, cantidad_inicial, cantidad_actual, fecha_vencimiento, precio_compra)
SELECT i.id, p.id, 'L-LECHE-001', 30.0, 22.0, CURRENT_DATE + INTERVAL '10 days', 1.20
FROM insumos i, proveedores p WHERE i.nombre = 'Leche entera' AND p.nombre = 'Lácteos Norte';

INSERT INTO lotes (insumo_id, numero_lote, cantidad_inicial, cantidad_actual, fecha_vencimiento, precio_compra)
SELECT id, 'L-AZUCAR-001', 10.0, 9.0, CURRENT_DATE + INTERVAL '365 days', 0.95
FROM insumos WHERE nombre = 'Azúcar blanco';

INSERT INTO lotes (insumo_id, numero_lote, cantidad_inicial, cantidad_actual, fecha_vencimiento, precio_compra)
SELECT id, 'L-VASOS-001', 500.0, 380.0, NULL, 0.04
FROM insumos WHERE nombre = 'Vasos cartón';

-- Movimientos (VENTA y MERMA) en los últimos 25 días para alimentar el reporte de consumo.
INSERT INTO movimientos_inventario (lote_id, tipo_movimiento, cantidad, motivo, usuario, fecha_hora)
SELECT l.id, 'VENTA', 0.5, 'Venta de prueba (demo)', 'system', NOW() - INTERVAL '22 days'
FROM lotes l WHERE l.numero_lote = 'L-CAFE-001';

INSERT INTO movimientos_inventario (lote_id, tipo_movimiento, cantidad, motivo, usuario, fecha_hora)
SELECT l.id, 'VENTA', 0.7, 'Venta de prueba (demo)', 'system', NOW() - INTERVAL '18 days'
FROM lotes l WHERE l.numero_lote = 'L-CAFE-001';

INSERT INTO movimientos_inventario (lote_id, tipo_movimiento, cantidad, motivo, usuario, fecha_hora)
SELECT l.id, 'VENTA', 1.0, 'Venta de prueba (demo)', 'system', NOW() - INTERVAL '12 days'
FROM lotes l WHERE l.numero_lote = 'L-CAFE-001';

INSERT INTO movimientos_inventario (lote_id, tipo_movimiento, cantidad, motivo, usuario, fecha_hora)
SELECT l.id, 'MERMA', 0.8, 'Caducidad lote leche', 'system', NOW() - INTERVAL '15 days'
FROM lotes l WHERE l.numero_lote = 'L-LECHE-001';

INSERT INTO movimientos_inventario (lote_id, tipo_movimiento, cantidad, motivo, usuario, fecha_hora)
SELECT l.id, 'VENTA', 4.0, 'Venta de prueba (demo)', 'system', NOW() - INTERVAL '8 days'
FROM lotes l WHERE l.numero_lote = 'L-LECHE-001';

INSERT INTO movimientos_inventario (lote_id, tipo_movimiento, cantidad, motivo, usuario, fecha_hora)
SELECT l.id, 'VENTA', 3.2, 'Venta de prueba (demo)', 'system', NOW() - INTERVAL '4 days'
FROM lotes l WHERE l.numero_lote = 'L-LECHE-001';

INSERT INTO movimientos_inventario (lote_id, tipo_movimiento, cantidad, motivo, usuario, fecha_hora)
SELECT l.id, 'VENTA', 0.5, 'Venta de prueba (demo)', 'system', NOW() - INTERVAL '5 days'
FROM lotes l WHERE l.numero_lote = 'L-AZUCAR-001';

INSERT INTO movimientos_inventario (lote_id, tipo_movimiento, cantidad, motivo, usuario, fecha_hora)
SELECT l.id, 'VENTA', 120.0, 'Venta de prueba (demo)', 'system', NOW() - INTERVAL '6 days'
FROM lotes l WHERE l.numero_lote = 'L-VASOS-001';

INSERT INTO movimientos_inventario (lote_id, tipo_movimiento, cantidad, motivo, usuario, fecha_hora)
SELECT l.id, 'ROTURA', 5.0, 'Caja accidentada (demo)', 'system', NOW() - INTERVAL '10 days'
FROM lotes l WHERE l.numero_lote = 'L-VASOS-001';
