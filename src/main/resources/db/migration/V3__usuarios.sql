-- Sprint 2: user management - roles and permissions

CREATE TABLE usuarios (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(60)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol           VARCHAR(20)  NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE
);

-- NOTE: the default admin user (admin / admin123, ROOT) is created at startup
-- by DataInitializer using the application's PasswordEncoder, so the BCrypt
-- hash is always correct.
