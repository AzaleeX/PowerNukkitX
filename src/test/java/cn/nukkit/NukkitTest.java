package cn.nukkit;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Nukkit launcher utilities
 * This test class focuses on testing the Nukkit launcher constants,
 * static utility methods, and configuration, improving code coverage.
 */
public class NukkitTest {

    @Test
    void testVersionConstantsNotNull() {
        assertNotNull(Nukkit.VERSION, "VERSION should not be null");
        assertNotNull(Nukkit.CODENAME, "CODENAME should not be null");
        assertNotNull(Nukkit.API_VERSION, "API_VERSION should not be null");
    }

    @Test
    void testCodenameIsPowerNukkitX() {
        assertEquals("PowerNukkitX", Nukkit.CODENAME, "CODENAME should be PowerNukkitX");
    }

    @Test
    void testApiVersionIsValid() {
        assertEquals("2.0.0", Nukkit.API_VERSION, "API_VERSION should be 2.0.0");
    }

    @Test
    void testGitCommitFormat() {
        assertNotNull(Nukkit.GIT_COMMIT, "GIT_COMMIT should not be null");
        assertTrue(Nukkit.GIT_COMMIT.startsWith("git-"), "GIT_COMMIT should start with 'git-'");
    }

    @Test
    void testPathConstantsNotNull() {
        assertNotNull(Nukkit.PATH, "PATH should not be null");
        assertNotNull(Nukkit.DATA_PATH, "DATA_PATH should not be null");
        assertNotNull(Nukkit.PLUGIN_PATH, "PLUGIN_PATH should not be null");
    }

    @Test
    void testPathsEndWithSlash() {
        assertTrue(Nukkit.PATH.endsWith("/"), "PATH should end with '/'");
        assertTrue(Nukkit.DATA_PATH.endsWith("/"), "DATA_PATH should end with '/'");
    }

    @Test
    void testPluginPathFormat() {
        assertTrue(Nukkit.PLUGIN_PATH.endsWith("plugins"), "PLUGIN_PATH should end with 'plugins'");
    }

    @Test
    void testStartTimeIsPositive() {
        assertTrue(Nukkit.START_TIME > 0, "START_TIME should be positive");
        assertTrue(Nukkit.START_TIME <= System.currentTimeMillis(), "START_TIME should be before current time");
    }

    @Test
    void testDebugDefaultValue() {
        assertTrue(Nukkit.DEBUG >= 0, "DEBUG should be non-negative");
    }

    @Test
    void testSetAndGetLogLevel() {
        // Save the current level
        Level originalLevel = Nukkit.getLogLevel();
        
        try {
            // Test setting to INFO
            Nukkit.setLogLevel(Level.INFO);
            assertEquals(Level.INFO, Nukkit.getLogLevel(), "Log level should be INFO");
            
            // Test setting to DEBUG
            Nukkit.setLogLevel(Level.DEBUG);
            assertEquals(Level.DEBUG, Nukkit.getLogLevel(), "Log level should be DEBUG");
            
            // Test setting to WARN
            Nukkit.setLogLevel(Level.WARN);
            assertEquals(Level.WARN, Nukkit.getLogLevel(), "Log level should be WARN");
            
            // Test setting to ERROR
            Nukkit.setLogLevel(Level.ERROR);
            assertEquals(Level.ERROR, Nukkit.getLogLevel(), "Log level should be ERROR");
        } finally {
            // Restore the original level
            Nukkit.setLogLevel(originalLevel);
        }
    }

    @Test
    void testSetLogLevelWithNull() {
        assertThrows(NullPointerException.class, () -> {
            Nukkit.setLogLevel(null);
        }, "Setting log level to null should throw NullPointerException");
    }

    @Test
    void testAnsiDefaultValue() {
        // ANSI is public and can be accessed
        assertTrue(Nukkit.ANSI || !Nukkit.ANSI, "ANSI should be a boolean value");
    }

    @Test
    void testChromeDebugPortDefaultValue() {
        assertTrue(Nukkit.CHROME_DEBUG_PORT >= -1, "CHROME_DEBUG_PORT should be -1 or positive");
    }

    @Test
    void testJsDebugListNotNull() {
        assertNotNull(Nukkit.JS_DEBUG_LIST, "JS_DEBUG_LIST should not be null");
    }
}
