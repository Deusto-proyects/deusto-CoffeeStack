CREATE TABLE receta_items (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    item_id     BIGINT          NOT NULL,
    insumo_id   BIGINT          NOT NULL,
    cantidad    DOUBLE          NOT NULL,
    CONSTRAINT fk_receta_item    FOREIGN KEY (item_id)   REFERENCES items(id),
    CONSTRAINT fk_receta_insumo  FOREIGN KEY (insumo_id) REFERENCES insumos(id),
    CONSTRAINT uq_receta_item_insumo UNIQUE (item_id, insumo_id)
);
