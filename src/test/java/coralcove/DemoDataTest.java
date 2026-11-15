package coralcove;

import coralcove.service.DemoDataService;
import coralcove.service.LodgeService;
import coralcove.service.SettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class DemoDataTest extends DbFixture {
    @Test
    public void loadAndResetTouchesOnlySampleRows() {
        DemoDataService demo = new DemoDataService();
        demo.load();
        Assertions.assertTrue(SettingsService.demoLoaded());
        Assertions.assertEquals(30, new LodgeService().search(null, null, null, null).size());
        try {
            demo.load();
            Assertions.fail("second load should be refused");
        } catch (coralcove.util.AppException ex) {
            Assertions.assertTrue(ex.getMessage().contains("already loaded"));
        }
    }

    @Test
    public void resetLeavesAnEmptyOperationalSet() {
        DemoDataService demo = new DemoDataService();
        demo.load();
        demo.reset();
        Assertions.assertFalse(SettingsService.demoLoaded());
        Assertions.assertEquals(0, new LodgeService().search(null, null, null, null).size());
        demo.load();
        Assertions.assertEquals(30, new LodgeService().search(null, null, null, null).size());
    }
}
