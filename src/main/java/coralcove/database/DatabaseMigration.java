package coralcove.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Applies schema version 2. Adds user profile and guest Aadhaar columns. */
public final class DatabaseMigration {

    public static final int CURRENT = 2;

    private DatabaseMigration() {
    }

    public static void migrate(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS schema_version (
                        version INTEGER PRIMARY KEY,
                        applied_at TEXT NOT NULL
                    )
                    """);
        }
        int version = 0;
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT COALESCE(MAX(version), 0) FROM schema_version")) {
            if (rs.next()) {
                version = rs.getInt(1);
            }
        }
        if (version < 1) {
            DatabaseInitializer.createSchema(connection);
            DatabaseInitializer.seed(connection);
            try (var ps = connection.prepareStatement(
                    "INSERT INTO schema_version(version, applied_at) VALUES (1, datetime('now'))")) {
                ps.executeUpdate();
            }
            version = 1;
        }
        if (version < 2) {
            addColumnIfNotExists(connection, "users", "display_name", "TEXT");
            addColumnIfNotExists(connection, "users", "email", "TEXT");
            addColumnIfNotExists(connection, "users", "phone", "TEXT");
            addColumnIfNotExists(connection, "users", "avatar_path", "TEXT");
            addColumnIfNotExists(connection, "guests", "aadhar_verified", "INTEGER NOT NULL DEFAULT 0");
            addColumnIfNotExists(connection, "guests", "aadhar_image_path", "TEXT");

            try (var ps = connection.prepareStatement("""
                    UPDATE users SET display_name = 'Aniket Sharma (Owner)',
                                     email = 'owner@coralcove.resorts',
                                     phone = '+91 98765 43210',
                                     avatar_path = 'avatar-owner.png'
                    WHERE username = 'admin' AND (display_name IS NULL OR display_name = '')
                    """)) {
                ps.executeUpdate();
            }

            try (var ps = connection.prepareStatement(
                    "INSERT INTO schema_version(version, applied_at) VALUES (2, datetime('now'))")) {
                ps.executeUpdate();
            }
        }
    }

    private static void addColumnIfNotExists(Connection connection, String table, String column, String type) throws SQLException {
        try (var statement = connection.createStatement();
                var rs = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return;
                }
            }
        }
        try (var statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        }
    }
}
