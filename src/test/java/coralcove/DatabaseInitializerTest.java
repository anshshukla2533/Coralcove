package coralcove;

import coralcove.database.DatabaseManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class DatabaseInitializerTest extends DbFixture {
    @Test
    public void createsSchemaForeignKeysAndOneAdmin() throws Exception {
        try (Connection connection = DatabaseManager.getConnection();
                Statement statement = connection.createStatement()) {
            try (ResultSet keys = statement.executeQuery("PRAGMA foreign_keys")) {
                Assertions.assertTrue(keys.next());
                Assertions.assertEquals(1, keys.getInt(1));
            }
            try (ResultSet tables = statement.executeQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='stay_bookings'")) {
                Assertions.assertTrue(tables.next());
                Assertions.assertEquals(1, tables.getInt(1));
            }
            try (ResultSet users = statement.executeQuery("SELECT COUNT(*) FROM users WHERE username='admin'")) {
                Assertions.assertTrue(users.next());
                Assertions.assertEquals(1, users.getInt(1));
            }
        }
        DatabaseManager.init(dir.resolve("coralcove.db"));
        try (Connection connection = DatabaseManager.getConnection();
                Statement statement = connection.createStatement();
                ResultSet users = statement.executeQuery("SELECT COUNT(*) FROM users")) {
            Assertions.assertTrue(users.next());
            Assertions.assertEquals(1, users.getInt(1));
        }
    }
}
