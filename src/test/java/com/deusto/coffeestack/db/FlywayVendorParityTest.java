package com.deusto.coffeestack.db;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Asegura que las carpetas de migraciones Flyway por dialecto
 * (mysql/ y postgresql/) contienen exactamente los mismos ficheros V*.sql.
 *
 * <p>Si añades una nueva migración en un dialecto, debes añadirla también en el otro.
 */
class FlywayVendorParityTest {

    private static final Path MYSQL_DIR =
            Paths.get("src/main/resources/db/migration/mysql");
    private static final Path POSTGRES_DIR =
            Paths.get("src/main/resources/db/migration/postgresql");

    @Test
    void mysqlAndPostgresqlMigrationsHaveSameFilenames() throws IOException {
        List<String> mysqlFiles = listSqlFilenames(MYSQL_DIR);
        List<String> postgresFiles = listSqlFilenames(POSTGRES_DIR);

        assertThat(mysqlFiles)
                .as("Cada migración V*.sql debe existir tanto en mysql/ como en postgresql/")
                .containsExactlyInAnyOrderElementsOf(postgresFiles);
    }

    private static List<String> listSqlFilenames(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".sql"))
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .toList();
        }
    }
}
