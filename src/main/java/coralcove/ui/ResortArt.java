package coralcove.ui;

import coralcove.model.Resort;
import coralcove.service.ResortService;
import coralcove.service.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.image.Image;

public final class ResortArt {
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    private ResortArt() {
    }

    public static Image image(String name) {
        if (name == null || name.isBlank()) {
            return image("resort-beach");
        }
        return CACHE.computeIfAbsent(name, key -> {
            var url = ResortArt.class.getResource("/coralcove/media/" + key + ".jpg");
            if (url == null) {
                return null;
            }
            return new Image(url.toExternalForm(), 1280, 720, true, true, true);
        });
    }

    public static Image forZone(String zone) {
        if (zone == null) {
            return image("zone-beach");
        }
        return switch (zone.toUpperCase()) {
            case "CLIFF" -> image("resort-cliff");
            case "GARDEN" -> image("zone-garden");
            case "LAGOON" -> image("zone-lagoon");
            default -> image("zone-beach");
        };
    }

    public static Image forResort(Resort resort) {
        if (resort != null && resort.getName() != null && resort.getName().toLowerCase().contains("cliff")) {
            return image("resort-cliff");
        }
        return image("resort-beach");
    }

    public static Image currentResort() {
        Long id = Session.resortFilter();
        if (id == null) {
            return image("resort-beach");
        }
        for (Resort resort : new ResortService().list(false)) {
            if (resort.getId() == id) {
                return forResort(resort);
            }
        }
        return image("resort-beach");
    }

    public static Image forExperience(String category) {
        String text = category == null ? "" : category.toLowerCase();
        if (text.contains("spa") || text.contains("well")) {
            return image("exp-spa");
        }
        if (text.contains("boat") || text.contains("cruise") || text.contains("water")) {
            return image("exp-boat");
        }
        return image("exp-restaurant");
    }
}
