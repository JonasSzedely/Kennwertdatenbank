package services;

import db.DBConnection;
import model.ProjectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Brings an existing {@code projects} table up to date with the current schema.
 * <p>
 * Adds columns that were introduced in newer versions of the application and
 * ensures that all constraints exist. Existing data is not changed.
 */
class CheckAndRepairDBService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckAndRepairDBService.class);

    private CheckAndRepairDBService() {
    }

    /**
     * Checks the {@code projects} table and adds missing columns and constraints.
     * <p>
     * Missing attribute columns are added with placeholder defaults
     * ({@code -1} for numbers, {@code 'xyz'} for text), so existing rows stay valid.
     * Errors are logged and do not stop the application.
     *
     * @param database the database connection to use
     */
    static void check(DBConnection database) {
        if (!database.isConnectionAvailable()) {
            LOG.warn("Schema check skipped, no database connection");
            return;
        }

        try (Connection conn = database.connect()) {
            Set<String> existingColumns = readColumns(conn);

            for (ProjectAttributes attribute : ProjectAttributes.values()) {
                String column = attribute.getSqlColumn().toLowerCase();
                if (existingColumns.contains(column)) {
                    continue;
                }
                boolean isNumber = attribute.getType() == Integer.class;
                String type = isNumber ? "INT" : "VARCHAR(255)";
                String defaultValue = isNumber ? "-1" : "'xyz'";

                execute(conn, "ALTER TABLE projects ADD COLUMN IF NOT EXISTS "
                        + column + " " + type + " NOT NULL DEFAULT " + defaultValue);
                LOG.info("Added missing column {}", column);
            }

            if (!existingColumns.contains("data")) {
                execute(conn, "ALTER TABLE projects ADD COLUMN IF NOT EXISTS data JSONB NOT NULL DEFAULT '{}'");
                LOG.info("Added missing column data");
            }

            if (!existingColumns.contains("active")) {
                execute(conn, "ALTER TABLE projects ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true");
                LOG.info("Added missing column active");
            }

            ensureVersionConstraint(conn);

            LOG.info("Database schema is up to date");
        } catch (SQLException e) {
            LOG.error("Database schema check failed", e);
        }
    }

    /**
     * Reads the names of all columns of the {@code projects} table.
     *
     * @param conn the open connection
     * @return the column names in lower case
     * @throws SQLException if the metadata cannot be read
     */
    private static Set<String> readColumns(Connection conn) throws SQLException {
        Set<String> columns = new HashSet<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, "projects", null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }
        return columns;
    }

    /**
     * Adds the version range constraint if it does not exist yet.
     * <p>
     * The constraint is added as {@code NOT VALID}, so it applies to new and
     * changed rows only. Existing rows are not checked.
     *
     * @param conn the open connection
     * @throws SQLException if the statement fails
     */
    private static void ensureVersionConstraint(Connection conn) throws SQLException {
        String sql = """
            DO $$
            BEGIN
                IF NOT EXISTS (
                    SELECT 1 FROM pg_constraint WHERE conname = 'projects_version_range'
                ) THEN
                    ALTER TABLE projects
                    ADD CONSTRAINT projects_version_range
                    CHECK (version BETWEEN %d AND %d) NOT VALID;
                END IF;
            END;
            $$
            """.formatted(ProjectAttributes.VERSION.getMin(), ProjectAttributes.VERSION.getMax());

        execute(conn, sql);
    }

    /**
     * Executes a single DDL statement.
     *
     * @param conn the open connection
     * @param sql  the statement to execute
     * @throws SQLException if the statement fails
     */
    private static void execute(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
}