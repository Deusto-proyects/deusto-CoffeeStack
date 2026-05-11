-- Sprint 4: registro de mermas, roturas y ajustes de inventario
CREATE TABLE movimientos_inventario (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    lote_id         BIGINT       NOT NULL,
    tipo_movimiento VARCHAR(30)  NOT NULL,
    cantidad        DOUBLE       NOT NULL,
    motivo          VARCHAR(300) NOT NULL,
    usuario         VARCHAR(120) NOT NULL,
    fecha_hora      TIMESTAMP    NOT NULL,
    CONSTRAINT fk_mov_lote FOREIGN KEY (lote_id) REFERENCES lotes (id)
);
