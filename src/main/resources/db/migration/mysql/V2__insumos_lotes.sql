-- Sprint 1: inventory domain - insumos and lotes

CREATE TABLE insumos (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre              VARCHAR(120)   NOT NULL,
    unidad_medida       VARCHAR(30)    NOT NULL,
    stock_minimo_alerta DOUBLE         NOT NULL DEFAULT 0
);

CREATE TABLE lotes (
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    insumo_id          BIGINT         NOT NULL,
    numero_lote        VARCHAR(60)    NOT NULL,
    cantidad_inicial   DOUBLE         NOT NULL,
    cantidad_actual    DOUBLE         NOT NULL,
    fecha_vencimiento  DATE,
    CONSTRAINT fk_lote_insumo FOREIGN KEY (insumo_id) REFERENCES insumos (id)
);
