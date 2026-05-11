-- Sprint 3 final: auditoría JPA sobre usuarios.
ALTER TABLE usuarios ADD COLUMN created_at TIMESTAMP NULL;
ALTER TABLE usuarios ADD COLUMN created_by VARCHAR(60) NULL;
ALTER TABLE usuarios ADD COLUMN updated_at TIMESTAMP NULL;
ALTER TABLE usuarios ADD COLUMN updated_by VARCHAR(60) NULL;
