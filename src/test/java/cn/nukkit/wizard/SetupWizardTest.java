package cn.nukkit.wizard;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SetupWizard functionality.
 */
public class SetupWizardTest {

    @Test
    void test_languageResourcesExist() throws IOException {
        // Test that common language files exist
        String[] languages = {"eng", "chs", "fra", "deu", "spa"};
        
        for (String lang : languages) {
            try (InputStream langFile = getClass().getClassLoader()
                    .getResourceAsStream("language/" + lang + "/lang.json")) {
                assertNotNull(langFile, "Language file should exist for: " + lang);
            }
        }
    }

    @Test
    void test_languageListExists() throws IOException {
        // Test that the language list file exists
        try (InputStream languageList = getClass().getClassLoader()
                .getResourceAsStream("language/language.list")) {
            assertNotNull(languageList, "language.list file should exist");
        }
    }
}
