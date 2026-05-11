-- Sprint 3: añade campo activo a insumos para soportar baja lógica (soft-delete)
ALTER TABLE insumos ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
