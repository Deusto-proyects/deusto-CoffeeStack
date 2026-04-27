-- Tabla de cabecera de ventas
CREATE TABLE ventas (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    usuario     VARCHAR(120)    NOT NULL,
    fecha_hora  DATETIME        NOT NULL
);

-- Tabla de líneas de detalle de cada venta
CREATE TABLE venta_lineas (
    id                  BIGINT  AUTO_INCREMENT PRIMARY KEY,
    venta_id            BIGINT  NOT NULL,
    item_id             BIGINT  NOT NULL,
    cantidad_unidades   INT     NOT NULL,
    CONSTRAINT fk_venta_linea_venta FOREIGN KEY (venta_id) REFERENCES ventas(id),
    CONSTRAINT fk_venta_linea_item  FOREIGN KEY (item_id)  REFERENCES items(id)
);
