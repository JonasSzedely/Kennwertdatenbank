package services;

import db.DBConnection;
import model.ProjectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the database schema on first start.
 * <p>
 * All statements are idempotent, so the initialization can run on every start
 * without changing existing tables or data. Changes to an existing schema are
 * handled by {@link CheckAndRepairDBService}.
 */
class InitializeDBService {

    private static final Logger LOG = LoggerFactory.getLogger(InitializeDBService.class);

    /** Key-value table for metadata such as the data version counter. */
    private static final String CREATE_META_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS db_meta (
            key   VARCHAR(255) PRIMARY KEY,
            value BIGINT NOT NULL DEFAULT 0
        )
        """;

    /** Main table with one row per project version. */
    private static final String CREATE_PROJECTS_TABLE_SQL = buildCreateProjectsSql();

    /** Counter that is incremented on every change to the projects table. */
    private static final String ENSURE_META_ROW_SQL = """
        INSERT INTO db_meta (key, value)
        VALUES ('projects_version', 0)
        ON CONFLICT (key) DO NOTHING
        """;

    /** Function that increments the data version counter. */
    private static final String CREATE_FUNCTION_SQL = """
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

    /** Trigger that calls the function after every change to the projects table. */
    private static final String CREATE_TRIGGER_SQL = """
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

    private InitializeDBService() {
    }

    /**
     * Creates all tables, the data version counter and the change trigger if they
     * do not exist yet.
     * <p>
     * The trigger increments {@code projects_version} in {@code db_meta} after every
     * change to {@code projects}. {@link DBCheckerService} uses this counter to
     * detect changes made by other users.
     *
     * @param database the database connection to use
     * @return {@code true} if the schema is ready, {@code false} if no connection
     *         is available or a statement failed
     */
    static boolean initialize(DBConnection database) {
        if (!database.isConnectionAvailable()) {
            LOG.warn("Database initialization skipped, no database connection");
            return false;
        }

        try (Connection conn = database.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(CREATE_META_TABLE_SQL);
            stmt.executeUpdate(CREATE_PROJECTS_TABLE_SQL);
            stmt.executeUpdate(ENSURE_META_ROW_SQL);
            stmt.executeUpdate(CREATE_FUNCTION_SQL);
            stmt.executeUpdate(CREATE_TRIGGER_SQL);

            LOG.info("Database schema initialized");
            return true;
        } catch (SQLException e) {
            LOG.error("Database initialization failed", e);
            return false;
        }
    }

    /**
     * Builds the CREATE TABLE statement for the projects table.
     * <p>
     * Contains one column per {@link ProjectAttributes}, the cost data as JSON,
     * the active flag for soft delete and the range constraints for project
     * number and version.
     *
     * @return the SQL statement
     */
    private static String buildCreateProjectsSql() {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS projects(");
        for (ProjectAttributes attribute : ProjectAttributes.values()) {
            sb.append(attribute.getSqlColumn())
                    .append(attribute.getType() == Integer.class ? " INT NOT NULL, " : " VARCHAR(255) NOT NULL, ");
        }
        sb.append("data JSONB NOT NULL, ")
                .append("active BOOLEAN NOT NULL DEFAULT true, ")
                .append("CHECK (project_nr > 9999), ")
                .append("CONSTRAINT projects_version_range CHECK (version BETWEEN ")
                .append(ProjectAttributes.VERSION.getMin()).append(" AND ")
                .append(ProjectAttributes.VERSION.getMax()).append("), ")
                .append("PRIMARY KEY (project_nr, version))");
        return sb.toString();
    }
}