package coralcove;

import coralcove.model.Guest;
import coralcove.model.Role;
import coralcove.model.User;
import coralcove.service.AuthenticationService;
import coralcove.service.GuestService;
import coralcove.service.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProfileAndAadharTest extends DbFixture {

    @Test
    public void userProfileCanBeUpdatedAndQueried() {
        AuthenticationService auth = new AuthenticationService();
        User admin = auth.login("admin", "Admin@123");
        Session.set(admin);

        Assertions.assertNotNull(admin.getDisplayName());
        Assertions.assertEquals("Aniket Sharma (Owner)", admin.getDisplayName());
        Assertions.assertEquals("avatar-owner.png", admin.getAvatarPath());

        // Update profile
        auth.updateProfile(admin.getId(), "Aniket S. (Director)", "aniket@coralcove.resorts", "+91 99999 88888", "avatar-owner.png");

        User reloaded = auth.findById(admin.getId());
        Assertions.assertEquals("Aniket S. (Director)", reloaded.getDisplayName());
        Assertions.assertEquals("aniket@coralcove.resorts", reloaded.getEmail());
        Assertions.assertEquals("+91 99999 88888", reloaded.getPhone());
        Assertions.assertEquals("Aniket S. (Director)", reloaded.getEffectiveName());
    }

    @Test
    public void guestAadharCanBeSavedAndVerified() {
        AuthenticationService auth = new AuthenticationService();
        User admin = auth.login("admin", "Admin@123");
        Session.set(admin);

        GuestService guests = new GuestService();
        Guest guest = new Guest();
        guest.setFullName("Kavita Nair");
        guest.setPhone("9812345678");
        guest.setEmail("kavita.nair@example.com");
        guest.setAadharVerified(true);
        guest.setIdDocType("Aadhaar");
        guest.setIdDocNumber("9876-5432-1098");
        guest.setAadharImagePath("sample-aadhar.png");

        Guest saved = guests.save(guest, true);
        Assertions.assertTrue(saved.isAadharVerified());
        Assertions.assertEquals("sample-aadhar.png", saved.getAadharImagePath());
        Assertions.assertEquals("9876-5432-1098", saved.getIdDocNumber());

        // Test one-click update of aadhar
        guests.updateAadhar(saved.getId(), false, null);
        Guest reloaded = guests.findById(saved.getId());
        Assertions.assertFalse(reloaded.isAadharVerified());
        Assertions.assertNull(reloaded.getAadharImagePath());

        guests.updateAadhar(saved.getId(), true, "data/aadhar/kavita.png");
        Guest verified = guests.findById(saved.getId());
        Assertions.assertTrue(verified.isAadharVerified());
        Assertions.assertEquals("data/aadhar/kavita.png", verified.getAadharImagePath());
    }
}
