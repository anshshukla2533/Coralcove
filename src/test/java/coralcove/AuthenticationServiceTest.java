package coralcove;

import coralcove.config.AppConfig;
import coralcove.model.Role;
import coralcove.model.User;
import coralcove.service.AuthenticationService;
import coralcove.service.Session;
import coralcove.util.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class AuthenticationServiceTest extends DbFixture {
    private final AuthenticationService auth = new AuthenticationService();

    @Test
    public void loginRejectsBadPasswordAndHonoursMustChange() {
        User admin = auth.login(AppConfig.DEFAULT_ADMIN, AppConfig.DEFAULT_PASSWORD);
        Assertions.assertEquals(Role.ADMIN, admin.getRole());
        Assertions.assertTrue(admin.isMustChangePassword());
        try {
            auth.login(AppConfig.DEFAULT_ADMIN, "wrong-pass");
            Assertions.fail("expected failure");
        } catch (AppException ex) {
            Assertions.assertTrue(ex.getMessage().contains("incorrect"));
        }
    }

    @Test
    public void changePasswordClearsFlagAndInactiveUserCannotLogin() {
        User admin = auth.login(AppConfig.DEFAULT_ADMIN, AppConfig.DEFAULT_PASSWORD);
        auth.changePassword(admin.getId(), AppConfig.DEFAULT_PASSWORD, "Newpass12");
        User again = auth.login(AppConfig.DEFAULT_ADMIN, "Newpass12");
        Assertions.assertFalse(again.isMustChangePassword());
        Session.set(again);
        auth.createUser("desk", "Deskpass1", Role.FRONT_DESK);
        User desk = auth.login("desk", "Deskpass1");
        Session.set(again);
        desk.setActive(false);
        auth.updateUser(desk);
        try {
            auth.login("desk", "Deskpass1");
            Assertions.fail("inactive login");
        } catch (AppException ex) {
            Assertions.assertTrue(ex.getMessage().contains("incorrect"));
        }
    }

    @Test
    public void receptionistCannotManageUsers() {
        User desk = new User();
        desk.setId(Session.get().getId());
        desk.setRole(Role.FRONT_DESK);
        desk.setActive(true);
        Session.set(desk);
        try {
            auth.listUsers();
            Assertions.fail("receptionist listed users");
        } catch (AppException ex) {
            Assertions.assertTrue(ex.getMessage().contains("access"));
        }
    }
}
