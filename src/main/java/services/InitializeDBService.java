package services;

import db.DBConnection;
import model.ProjectValues;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class InitializeDBService {

    private static StringBuilder STRING_BUILDER = new StringBuilder();

    static boolean initialize(DBConnection database) {
        if (!database.isConnectionAvailable()) {
            return false;
        }

        // 1. CREATE TABLE db_meta
        String createMetaTableSql = """
            CREATE TABLE IF NOT EXISTS db_meta (
                key   VARCHAR(255) PRIMARY KEY,
                value BIGINT NOT NULL DEFAULT 0
            )
            """;

        // 2. CREATE TABLE projects
        STRING_BUILDER.append("CREATE TABLE IF NOT EXISTS projects(");
        for (ProjectValues value : ProjectValues.values()) {
            STRING_BUILDER.append(value.getSqlColumn());
            if (value.getType() == Integer.class) {
                STRING_BUILDER.append(" INT NOT NULL,");
            } else {
                STRING_BUILDER.append(" VARCHAR(255) NOT NULL,");
            }
        }
        STRING_BUILDER.append("data JSONB NOT NULL,");
        STRING_BUILDER.append("active BOOLEAN NOT NULL DEFAULT true, CHECK (project_nr > 9999), PRIMARY KEY(project_nr, version))");
        String createTableSql = STRING_BUILDER.toString();
        STRING_BUILDER = new StringBuilder();

        // 3. Insert projects_version in db_meta
        String ensureMetaRowSql = """
            INSERT INTO db_meta (key, value)
            VALUES ('projects_version', 0)
            ON CONFLICT (key) DO NOTHING
            """;

        // 4. Trigger function
        String createFunctionSql = """
        DO $$
        BEGIN
            IF NOT EXISTS (
                SELECT 1 FROM pg_proc WHERE proname = 'increment_projects_version'
            ) THEN
                EXECUTE '
                    CREATE FUNCTION increment_projects_version()
                    RETURNS TRIGGER AS $func$
                    BEGIN
                        UPDATE db_meta
                        SET value = value + 1
                        WHERE key = ''projects_version'';
                        RETURN NEW;
                    END;
                    $func$ LANGUAGE plpgsql SECURITY DEFINER
                ';
            END IF;
        END;
        $$
        """;

        // 5. Trigger
        String createTriggerSql = """
        DO $$
        BEGIN
            IF NOT EXISTS (
                SELECT 1 FROM pg_trigger WHERE tgname = 'trg_projects_version'
            ) THEN
                EXECUTE '
                    CREATE TRIGGER trg_projects_version
                    AFTER INSERT OR UPDATE OR DELETE
                    ON projects
                    FOR EACH STATEMENT
                    EXECUTE FUNCTION increment_projects_version()
                ';
            END IF;
        END;
        $$
        """;

        try (Connection conn = database.connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createMetaTableSql);
            System.out.println("Tabelle 'db_meta' erfolgreich erstellt oder schon vorhanden.");

            stmt.executeUpdate(createTableSql);
            System.out.println("Tabelle 'projects' erfolgreich erstellt oder schon vorhanden.");

            stmt.executeUpdate(ensureMetaRowSql);
            System.out.println("db_meta-Eintrag 'projects_version' sichergestellt.");

            stmt.executeUpdate(createFunctionSql);
            stmt.executeUpdate(createTriggerSql);
            System.out.println("Trigger 'trg_projects_version' erfolgreich registriert.");

            return true;
        } catch (SQLException e) {
            System.err.println("DB-Initialisierung fehlgeschlagen: " + e.getMessage());
            return false;
        }
    }
}
