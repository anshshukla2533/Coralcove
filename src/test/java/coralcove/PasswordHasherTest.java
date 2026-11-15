package coralcove;

import coralcove.util.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class PasswordHasherTest {
    @Test
    public void hashVerifiesAndRejectsWrongOrMalformed() {
        String stored = PasswordHasher.hash("Admin@123");
        Assertions.assertTrue(PasswordHasher.verify("Admin@123", stored));
        Assertions.assertFalse(PasswordHasher.verify("admin@123", stored));
        Assertions.assertFalse(PasswordHasher.verify("Admin@123", "plaintext"));
        Assertions.assertFalse(PasswordHasher.verify(null, stored));
        Assertions.assertTrue(stored.startsWith("pbkdf2$"));
    }
}
