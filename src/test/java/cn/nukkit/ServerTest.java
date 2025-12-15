package cn.nukkit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Server utility methods
 */
public class ServerTest {

    @Test
    void testBroadcastChannelConstants() {
        assertEquals("nukkit.broadcast.admin", Server.BROADCAST_CHANNEL_ADMINISTRATIVE, 
            "Admin broadcast channel should be correct");
        assertEquals("nukkit.broadcast.user", Server.BROADCAST_CHANNEL_USERS, 
            "User broadcast channel should be correct");
    }

    @Test
    void testGetGamemodeString_Survival() {
        assertEquals("%gameMode.survival", Server.getGamemodeString(Player.SURVIVAL), 
            "Survival mode string should be correct");
        assertEquals("Survival", Server.getGamemodeString(Player.SURVIVAL, true), 
            "Direct survival mode string should be correct");
    }

    @Test
    void testGetGamemodeString_Creative() {
        assertEquals("%gameMode.creative", Server.getGamemodeString(Player.CREATIVE), 
            "Creative mode string should be correct");
        assertEquals("Creative", Server.getGamemodeString(Player.CREATIVE, true), 
            "Direct creative mode string should be correct");
    }

    @Test
    void testGetGamemodeString_Adventure() {
        assertEquals("%gameMode.adventure", Server.getGamemodeString(Player.ADVENTURE), 
            "Adventure mode string should be correct");
        assertEquals("Adventure", Server.getGamemodeString(Player.ADVENTURE, true), 
            "Direct adventure mode string should be correct");
    }

    @Test
    void testGetGamemodeString_Spectator() {
        assertEquals("%gameMode.spectator", Server.getGamemodeString(Player.SPECTATOR), 
            "Spectator mode string should be correct");
        assertEquals("Spectator", Server.getGamemodeString(Player.SPECTATOR, true), 
            "Direct spectator mode string should be correct");
    }

    @Test
    void testGetGamemodeString_Unknown() {
        assertEquals("UNKNOWN", Server.getGamemodeString(999), 
            "Unknown gamemode should return UNKNOWN");
        assertEquals("UNKNOWN", Server.getGamemodeString(-1, true), 
            "Unknown gamemode with direct flag should return UNKNOWN");
    }

    @Test
    void testGetGamemodeFromString_Numeric() {
        assertEquals(Player.SURVIVAL, Server.getGamemodeFromString("0"), 
            "String '0' should map to SURVIVAL");
        assertEquals(Player.CREATIVE, Server.getGamemodeFromString("1"), 
            "String '1' should map to CREATIVE");
        assertEquals(Player.ADVENTURE, Server.getGamemodeFromString("2"), 
            "String '2' should map to ADVENTURE");
        assertEquals(Player.SPECTATOR, Server.getGamemodeFromString("3"), 
            "String '3' should map to SPECTATOR");
    }

    @Test
    void testGetGamemodeFromString_FullName() {
        assertEquals(Player.SURVIVAL, Server.getGamemodeFromString("survival"), 
            "String 'survival' should map to SURVIVAL");
        assertEquals(Player.CREATIVE, Server.getGamemodeFromString("creative"), 
            "String 'creative' should map to CREATIVE");
        assertEquals(Player.ADVENTURE, Server.getGamemodeFromString("adventure"), 
            "String 'adventure' should map to ADVENTURE");
        assertEquals(Player.SPECTATOR, Server.getGamemodeFromString("spectator"), 
            "String 'spectator' should map to SPECTATOR");
    }

    @Test
    void testGetGamemodeFromString_ShortName() {
        assertEquals(Player.SURVIVAL, Server.getGamemodeFromString("s"), 
            "String 's' should map to SURVIVAL");
        assertEquals(Player.CREATIVE, Server.getGamemodeFromString("c"), 
            "String 'c' should map to CREATIVE");
        assertEquals(Player.ADVENTURE, Server.getGamemodeFromString("a"), 
            "String 'a' should map to ADVENTURE");
        assertEquals(Player.SPECTATOR, Server.getGamemodeFromString("spc"), 
            "String 'spc' should map to SPECTATOR");
        assertEquals(Player.SPECTATOR, Server.getGamemodeFromString("view"), 
            "String 'view' should map to SPECTATOR");
        assertEquals(Player.SPECTATOR, Server.getGamemodeFromString("v"), 
            "String 'v' should map to SPECTATOR");
    }

    @Test
    void testGetGamemodeFromString_CaseInsensitive() {
        assertEquals(Player.SURVIVAL, Server.getGamemodeFromString("SURVIVAL"), 
            "Uppercase 'SURVIVAL' should map to SURVIVAL");
        assertEquals(Player.CREATIVE, Server.getGamemodeFromString("Creative"), 
            "Mixed case 'Creative' should map to CREATIVE");
        assertEquals(Player.ADVENTURE, Server.getGamemodeFromString("AdVeNtUrE"), 
            "Mixed case 'AdVeNtUrE' should map to ADVENTURE");
    }

    @Test
    void testGetGamemodeFromString_WithWhitespace() {
        assertEquals(Player.SURVIVAL, Server.getGamemodeFromString("  survival  "), 
            "String with whitespace should be trimmed and map to SURVIVAL");
        assertEquals(Player.CREATIVE, Server.getGamemodeFromString(" 1 "), 
            "Numeric string with whitespace should be trimmed and map to CREATIVE");
    }

    @Test
    void testGetGamemodeFromString_Invalid() {
        assertEquals(-1, Server.getGamemodeFromString("invalid"), 
            "Invalid string should return -1");
        assertEquals(-1, Server.getGamemodeFromString("99"), 
            "Invalid numeric string should return -1");
        assertEquals(-1, Server.getGamemodeFromString(""), 
            "Empty string should return -1");
    }

    @Test
    void testGetDifficultyFromString_Numeric() {
        assertEquals(0, Server.getDifficultyFromString("0"), 
            "String '0' should map to peaceful (0)");
        assertEquals(1, Server.getDifficultyFromString("1"), 
            "String '1' should map to easy (1)");
        assertEquals(2, Server.getDifficultyFromString("2"), 
            "String '2' should map to normal (2)");
        assertEquals(3, Server.getDifficultyFromString("3"), 
            "String '3' should map to hard (3)");
    }

    @Test
    void testGetDifficultyFromString_FullName() {
        assertEquals(0, Server.getDifficultyFromString("peaceful"), 
            "String 'peaceful' should map to 0");
        assertEquals(1, Server.getDifficultyFromString("easy"), 
            "String 'easy' should map to 1");
        assertEquals(2, Server.getDifficultyFromString("normal"), 
            "String 'normal' should map to 2");
        assertEquals(3, Server.getDifficultyFromString("hard"), 
            "String 'hard' should map to 3");
    }

    @Test
    void testGetDifficultyFromString_ShortName() {
        assertEquals(0, Server.getDifficultyFromString("p"), 
            "String 'p' should map to peaceful (0)");
        assertEquals(1, Server.getDifficultyFromString("e"), 
            "String 'e' should map to easy (1)");
        assertEquals(2, Server.getDifficultyFromString("n"), 
            "String 'n' should map to normal (2)");
        assertEquals(3, Server.getDifficultyFromString("h"), 
            "String 'h' should map to hard (3)");
    }

    @Test
    void testGetDifficultyFromString_CaseInsensitive() {
        assertEquals(0, Server.getDifficultyFromString("PEACEFUL"), 
            "Uppercase 'PEACEFUL' should map to 0");
        assertEquals(1, Server.getDifficultyFromString("Easy"), 
            "Mixed case 'Easy' should map to 1");
        assertEquals(2, Server.getDifficultyFromString("NoRmAl"), 
            "Mixed case 'NoRmAl' should map to 2");
    }

    @Test
    void testGetDifficultyFromString_WithWhitespace() {
        assertEquals(0, Server.getDifficultyFromString("  peaceful  "), 
            "String with whitespace should be trimmed and map to 0");
        assertEquals(1, Server.getDifficultyFromString(" 1 "), 
            "Numeric string with whitespace should be trimmed and map to 1");
    }

    @Test
    void testGetDifficultyFromString_Invalid() {
        assertEquals(-1, Server.getDifficultyFromString("invalid"), 
            "Invalid string should return -1");
        assertEquals(-1, Server.getDifficultyFromString("99"), 
            "Invalid numeric string should return -1");
        assertEquals(-1, Server.getDifficultyFromString(""), 
            "Empty string should return -1");
    }

    @Test
    void testGetGamemodeFromString_AllValidOptions() {
        // Test all valid gamemode strings
        String[] survivalStrings = {"0", "survival", "s"};
        String[] creativeStrings = {"1", "creative", "c"};
        String[] adventureStrings = {"2", "adventure", "a"};
        String[] spectatorStrings = {"3", "spectator", "spc", "view", "v"};

        for (String str : survivalStrings) {
            assertEquals(Player.SURVIVAL, Server.getGamemodeFromString(str),
                "String '" + str + "' should map to SURVIVAL");
        }

        for (String str : creativeStrings) {
            assertEquals(Player.CREATIVE, Server.getGamemodeFromString(str),
                "String '" + str + "' should map to CREATIVE");
        }

        for (String str : adventureStrings) {
            assertEquals(Player.ADVENTURE, Server.getGamemodeFromString(str),
                "String '" + str + "' should map to ADVENTURE");
        }

        for (String str : spectatorStrings) {
            assertEquals(Player.SPECTATOR, Server.getGamemodeFromString(str),
                "String '" + str + "' should map to SPECTATOR");
        }
    }

    @Test
    void testGetDifficultyFromString_AllValidOptions() {
        // Test all valid difficulty strings
        String[] peacefulStrings = {"0", "peaceful", "p"};
        String[] easyStrings = {"1", "easy", "e"};
        String[] normalStrings = {"2", "normal", "n"};
        String[] hardStrings = {"3", "hard", "h"};

        for (String str : peacefulStrings) {
            assertEquals(0, Server.getDifficultyFromString(str),
                "String '" + str + "' should map to peaceful (0)");
        }

        for (String str : easyStrings) {
            assertEquals(1, Server.getDifficultyFromString(str),
                "String '" + str + "' should map to easy (1)");
        }

        for (String str : normalStrings) {
            assertEquals(2, Server.getDifficultyFromString(str),
                "String '" + str + "' should map to normal (2)");
        }

        for (String str : hardStrings) {
            assertEquals(3, Server.getDifficultyFromString(str),
                "String '" + str + "' should map to hard (3)");
        }
    }
}
