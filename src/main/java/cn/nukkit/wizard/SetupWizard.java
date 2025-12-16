package cn.nukkit.wizard;

import lombok.extern.slf4j.Slf4j;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.ParsedLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Interactive setup wizard using JLine for better user experience.
 * Provides language selection and configuration options with navigation and auto-completion.
 * Implements AutoCloseable for proper resource management.
 */
@Slf4j
public class SetupWizard implements AutoCloseable {
    /** Regex pattern for validating language codes (3 lowercase letters) */
    private static final String LANGUAGE_CODE_PATTERN = "^[a-z]{3}$";
    
    private final Terminal terminal;
    private final LineReader reader;
    private final Map<String, String> availableLanguages;
    private String selectedLanguage = null;
    private boolean skipWizard = false;

    public SetupWizard() throws IOException {
        // Initialize JLine terminal
        this.terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        // Load available languages
        this.availableLanguages = loadAvailableLanguages();

        // Build line reader with completer
        this.reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new LanguageCompleter(availableLanguages.keySet()))
                .option(LineReader.Option.CASE_INSENSITIVE, true)
                .option(LineReader.Option.AUTO_LIST, true)
                .option(LineReader.Option.LIST_PACKED, true)
                .build();
    }

    /**
     * Loads available languages from the language.list resource file.
     *
     * @return Map of language codes to language names
     */
    private Map<String, String> loadAvailableLanguages() {
        Map<String, String> languages = new LinkedHashMap<>();
        try (InputStream languageList = getClass().getClassLoader().getResourceAsStream("language/language.list")) {
            if (languageList == null) {
                throw new IllegalStateException("language/language.list is missing. If you are running a development version, make sure you have run 'git submodule update --init --recursive'.");
            }

            try (Scanner scanner = new Scanner(languageList)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("=>");
                    if (parts.length == 2) {
                        String code = parts[0].trim();
                        String name = parts[1].trim();
                        languages.put(code, name);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load language list", e);
            // Fallback to English
            languages.put("eng", "English");
        }
        return languages;
    }

    /**
     * Runs the complete setup wizard.
     *
     * @param predefinedLanguage Optional predefined language from command line
     * @return The selected language code
     */
    public String run(String predefinedLanguage) {
        try {
            // Step 1: Language selection (mandatory)
            selectedLanguage = selectLanguage(predefinedLanguage);

            // Step 2: Ask if user wants to skip the wizard
            if (selectedLanguage != null) {
                askSkipWizard();
            }

            return selectedLanguage;
        } catch (Exception e) {
            log.error("Error during setup wizard", e);
            return "eng"; // Default to English on error
        }
    }

    /**
     * Handles the language selection process.
     *
     * @param predefinedLanguage Optional predefined language from command line
     * @return Selected language code
     */
    private String selectLanguage(String predefinedLanguage) {
        terminal.writer().println("\n=== PowerNukkitX Setup Wizard ===");
        terminal.writer().println("Welcome! Please choose a language first!\n");
        terminal.writer().println("Available languages:");

        // Display available languages
        for (Map.Entry<String, String> entry : availableLanguages.entrySet()) {
            terminal.writer().println("  " + entry.getKey() + " => " + entry.getValue());
        }
        terminal.writer().println();
        terminal.writer().flush();

        // Handle predefined language
        if (predefinedLanguage != null && !predefinedLanguage.isEmpty()) {
            if (validateLanguage(predefinedLanguage)) {
                terminal.writer().println("Using predefined language: " + predefinedLanguage);
                terminal.writer().flush();
                return predefinedLanguage;
            } else {
                terminal.writer().println("Invalid predefined language: " + predefinedLanguage);
                terminal.writer().println("Please choose a valid language from the list above.");
                terminal.writer().flush();
            }
        }

        // Interactive language selection
        while (true) {
            try {
                String input = reader.readLine("Enter language code (or press TAB for auto-completion): ").trim();

                if (input.isEmpty()) {
                    terminal.writer().println("Language selection is mandatory. Please enter a language code.");
                    terminal.writer().flush();
                    continue;
                }

                if (validateLanguage(input)) {
                    terminal.writer().println("Language selected: " + input + " (" + availableLanguages.get(input) + ")");
                    terminal.writer().flush();
                    return input;
                } else {
                    terminal.writer().println("Invalid language code. Please choose from the list above.");
                    terminal.writer().flush();
                }
            } catch (Exception e) {
                log.error("Error reading language input", e);
                return "eng"; // Default to English on error
            }
        }
    }

    /**
     * Asks the user if they want to skip the setup wizard.
     */
    private void askSkipWizard() {
        terminal.writer().println();
        terminal.writer().println("=== Additional Setup ===");
        terminal.writer().flush();

        while (true) {
            try {
                String input = reader.readLine("Do you want to skip the set-up wizard? (Y/n): ").trim().toLowerCase();

                if (input.isEmpty() || input.equals("y") || input.equals("yes")) {
                    skipWizard = true;
                    terminal.writer().println("Setup wizard will be skipped. Server will use default configuration.");
                    terminal.writer().flush();
                    break;
                } else if (input.equals("n") || input.equals("no")) {
                    skipWizard = false;
                    terminal.writer().println("Proceeding with setup wizard...");
                    terminal.writer().flush();
                    // Additional setup steps can be added here in the future
                    break;
                } else {
                    terminal.writer().println("Invalid input. Please enter 'Y' for yes or 'n' for no.");
                    terminal.writer().flush();
                }
            } catch (Exception e) {
                log.error("Error reading skip wizard input", e);
                skipWizard = true;
                break;
            }
        }
    }

    /**
     * Validates if the given language code exists.
     * Includes security check to prevent path traversal attacks.
     *
     * @param languageCode Language code to validate
     * @return true if valid, false otherwise
     */
    private boolean validateLanguage(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) {
            return false;
        }

        // Security: prevent path traversal attacks by validating the language code format
        // Language codes should only contain lowercase letters (3 characters typically)
        if (!languageCode.matches(LANGUAGE_CODE_PATTERN)) {
            log.warn("Invalid language code format (must be 3 lowercase letters): {}", languageCode);
            return false;
        }

        // Check if language exists in available languages
        if (availableLanguages.containsKey(languageCode)) {
            return true;
        }

        // Also check if the language resource file exists
        try (InputStream conf = getClass().getClassLoader().getResourceAsStream("language/" + languageCode + "/lang.json")) {
            return conf != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Gets the selected language code.
     *
     * @return Selected language code
     */
    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    /**
     * Checks if the user wants to skip the wizard.
     *
     * @return true if wizard should be skipped, false otherwise
     */
    public boolean isSkipWizard() {
        return skipWizard;
    }

    /**
     * Closes the terminal and releases resources.
     */
    public void close() {
        try {
            if (terminal != null) {
                terminal.close();
            }
        } catch (IOException e) {
            log.error("Error closing terminal", e);
        }
    }

    /**
     * Language completer for auto-completion support.
     */
    private static class LanguageCompleter implements Completer {
        private final Set<String> languageCodes;

        public LanguageCompleter(Set<String> languageCodes) {
            this.languageCodes = languageCodes;
        }

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            String word = line.word();
            String wordLower = word.toLowerCase();
            for (String langCode : languageCodes) {
                if (langCode.startsWith(wordLower)) {
                    candidates.add(new Candidate(langCode));
                }
            }
        }
    }
}
