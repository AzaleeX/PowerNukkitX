package cn.nukkit.wizard;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SetupWizard functionality.
 */
public class SetupWizardTest {

    @Test
    void test_languageResourcesExist() {
        // Test that common language files exist
        String[] languages = {"eng", "chs", "fra", "deu", "spa"};
        
        for (String lang : languages) {
            InputStream langFile = getClass().getClassLoader()
                    .getResourceAsStream("language/" + lang + "/lang.json");
            assertNotNull(langFile, "Language file should exist for: " + lang);
        }
    }

    @Test
    void test_languageListExists() {
        // Test that the language list file exists
        InputStream languageList = getClass().getClassLoader()
                .getResourceAsStream("language/language.list");
        assertNotNull(languageList, "language.list file should exist");
    }
}
