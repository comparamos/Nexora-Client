package com.nexora.client.gui;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public final class NexoraTheme {
    public record Palette(String id, String name, int backgroundTop, int backgroundBottom,
                          int accent, int accentSoft, int text, int mutedText,
                          int[] swatches) {
    }

    private static final List<Palette> PALETTES = List.of(
            palette("sunset", "Sunset", 0xFF100A10, 0xFF24111A, 0xFFFF735E, 0xFFFFAE91,
                    0xFFFFF1E9, 0xFFC99D98,
                    0xFF4A211D, 0xFFFF735E, 0xFF311A1E, 0xFF30171B, 0xFF402022, 0xFFFF8558,
                    0xFFFFAD8B, 0xFFD79A8D, 0xFFE8C5B8, 0xFFFFF1E9, 0xFF9A625B, 0xFFFF9A5E),
            palette("aurora", "Aurora", 0xFF061014, 0xFF0B2025, 0xFF37D6C0, 0xFF87F1DF,
                    0xFFE9FFFA, 0xFF9FCBC4,
                    0xFF153A37, 0xFF37D6C0, 0xFF102D2D, 0xFF102A28, 0xFF133432, 0xFF21BFA7,
                    0xFF62D9C7, 0xFF70C8BC, 0xFFA5E0D6, 0xFFE9FFFA, 0xFF4A8F87, 0xFF39C5AF),
            palette("mint", "Mint", 0xFF081210, 0xFF10231D, 0xFF52D6A5, 0xFF9BECCB,
                    0xFFEFFFF7, 0xFFA4CDB9,
                    0xFF173A30, 0xFF52D6A5, 0xFF122D26, 0xFF102822, 0xFF16352A, 0xFF39BD8D,
                    0xFF70D2AA, 0xFF73BFA2, 0xFFA8D8C2, 0xFFEFFFF7, 0xFF528C75, 0xFF4CC99B),
            palette("peach", "Peach", 0xFF140D0C, 0xFF281714, 0xFFFFA47E, 0xFFFFC5A8,
                    0xFFFFF4EC, 0xFFD1AAA0,
                    0xFF48251E, 0xFFFFA47E, 0xFF321C19, 0xFF2E1916, 0xFF40221B, 0xFFE88C69,
                    0xFFF3B08E, 0xFFD4A18A, 0xFFE6C5B2, 0xFFFFF4EC, 0xFF9B6B5A, 0xFFFFB38D),
            palette("lilac", "Lilac", 0xFF080611, 0xFF110B20, 0xFF9B38FF, 0xFFBCA9FF,
                    0xFFF4F1FF, 0xFFB9ACD8,
                    0xFF241638, 0xFF9B38FF, 0xFF171026, 0xFF151022, 0xFF20152F, 0xFF7946D5,
                    0xFFA986E8, 0xFF8E79B8, 0xFFCBC1E4, 0xFFF4F1FF, 0xFF71618F, 0xFF9B70E8),
            palette("sand", "Sand", 0xFF13110C, 0xFF272116, 0xFFE3C27B, 0xFFF1DCA8,
                    0xFFFFF8E8, 0xFFCEC09F,
                    0xFF423522, 0xFFE3C27B, 0xFF302819, 0xFF2B2418, 0xFF3D321F, 0xFFC9A965,
                    0xFFE2C78D, 0xFFC3B18A, 0xFFE9DDBE, 0xFFFFF8E8, 0xFF92805B, 0xFFD9BC7D),
            palette("dusk", "Dusk", 0xFF0F0B14, 0xFF201328, 0xFFD48BFF, 0xFFE3B6FF,
                    0xFFFFF2FF, 0xFFC8A9D5,
                    0xFF3A1F39, 0xFFD48BFF, 0xFF27162B, 0xFF241429, 0xFF321A36, 0xFFC16CE0,
                    0xFFD29AE0, 0xFFB88AC2, 0xFFE1C5E8, 0xFFFFF2FF, 0xFF8C638D, 0xFFD28BE8),
            palette("prism", "Prism", 0xFF080D18, 0xFF111B2D, 0xFF76B9FF, 0xFFA7D1FF,
                    0xFFF0F7FF, 0xFFAABDD4,
                    0xFF1B2943, 0xFF76B9FF, 0xFF141F35, 0xFF111B2D, 0xFF1A2940, 0xFF579CE0,
                    0xFF85B5EA, 0xFF8FA9C9, 0xFFC2D2E6, 0xFFF0F7FF, 0xFF647D9D, 0xFF6AA9EE)
    );
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("nexora-client.properties");
    private static Palette current = loadSelected();

    private NexoraTheme() {
    }

    public static List<Palette> palettes() {
        return PALETTES;
    }

    public static Palette current() {
        return current;
    }

    public static void select(Palette palette) {
        if (!PALETTES.contains(palette)) {
            throw new IllegalArgumentException("Unknown Nexora theme: " + palette.id());
        }
        current = palette;
        saveSelected(palette);
    }

    private static Palette loadSelected() {
        Properties properties = new Properties();
        if (Files.exists(CONFIG_FILE)) {
            try (InputStream input = Files.newInputStream(CONFIG_FILE)) {
                properties.load(input);
            } catch (IOException exception) {
                throw new IllegalStateException("Could not read Nexora theme settings", exception);
            }
        }
        String selectedId = properties.getProperty("theme", "lilac");
        return PALETTES.stream()
                .filter(palette -> palette.id().equals(selectedId))
                .findFirst()
                .orElse(PALETTES.get(4));
    }

    private static void saveSelected(Palette palette) {
        Properties properties = new Properties();
        properties.setProperty("theme", palette.id());
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_FILE)) {
                properties.store(output, "Nexora Client appearance settings");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save Nexora theme settings", exception);
        }
    }

    private static Palette palette(String id, String name, int backgroundTop, int backgroundBottom,
                                  int accent, int accentSoft, int text, int mutedText, int... swatches) {
        return new Palette(id, name, backgroundTop, backgroundBottom, accent, accentSoft,
                text, mutedText, swatches);
    }
}
