package com.horacio.ecomarket.usuarios.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Component
@Slf4j
public class DatabaseSchemaFixer implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    public DatabaseSchemaFixer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        fixOrphanPasswordColumn();
    }

    private void fixOrphanPasswordColumn() {
        try {
            DatabaseMetaData meta = jdbc.getDataSource().getConnection().getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "perfil_usuario", "password")) {
                if (rs.next()) {
                    log.info("Eliminando columna 'password' huerfana de perfil_usuario...");
                    jdbc.execute("ALTER TABLE perfil_usuario DROP COLUMN password");
                    log.info("Columna 'password' eliminada correctamente.");
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo corregir la columna 'password' (probablemente ya no existe): {}", e.getMessage());
        }
    }
}
