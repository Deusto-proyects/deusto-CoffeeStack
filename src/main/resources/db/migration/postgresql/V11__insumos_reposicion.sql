-- Sprint 3: parámetros de reposición por insumo (lead time del proveedor y días de cobertura objetivo).
-- Se usan para calcular la cantidad sugerida de compra y el nivel de urgencia.
ALTER TABLE insumos ADD COLUMN lead_time_dias INT NOT NULL DEFAULT 7;
ALTER TABLE insumos ADD COLUMN dias_cobertura INT NOT NULL DEFAULT 14;
