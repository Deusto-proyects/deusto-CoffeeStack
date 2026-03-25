CREATE TABLE proveedores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    contacto VARCHAR(120),
    email VARCHAR(120),
    telefono VARCHAR(30),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE lotes
    ADD COLUMN proveedor_id BIGINT NULL,
    ADD CONSTRAINT fk_lote_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores (id);
