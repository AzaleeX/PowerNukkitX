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
    private final WizardConfig wizardConfig = new WizardConfig();
    private boolean skipWizard = false;
    
    /**
     * Configuration holder for wizard settings
     */
    public static class WizardConfig {
        private String language = "eng";
        private String serverName = "PowerNukkitX Server";
        private int port = 19132;
        private String motd = "PowerNukkitX Server";
        private int gamemode = 0;
        private boolean enableWhitelist = false;
        private List<String> whitelistedPlayers = new ArrayList<>();
        private List<String> operators = new ArrayList<>();
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public String getServerName() { return serverName; }
        public void setServerName(String serverName) { this.serverName = serverName; }
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public String getMotd() { return motd; }
        public void setMotd(String motd) { this.motd = motd; }
        
        public int getGamemode() { return gamemode; }
        public void setGamemode(int gamemode) { this.gamemode = gamemode; }
        
        public boolean isEnableWhitelist() { return enableWhitelist; }
        public void setEnableWhitelist(boolean enableWhitelist) { this.enableWhitelist = enableWhitelist; }
        
        public List<String> getWhitelistedPlayers() { return whitelistedPlayers; }
        public void setWhitelistedPlayers(List<String> whitelistedPlayers) { this.whitelistedPlayers = whitelistedPlayers; }
        
        public List<String> getOperators() { return operators; }
        public void setOperators(List<String> operators) { this.operators = operators; }
    }

    public SetupWizard() throws IOException {
        // Initialize JLine terminal
        // Allow dumb terminal and try to be as interactive as possible
        this.terminal = TerminalBuilder.builder()
                .system(true)
                .jna(false)
                .dumb(true)  // Allow dumb terminal as fallback but still try to be interactive
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
     * @param forceSkip If true, automatically skip the wizard
     * @return The wizard configuration
     */
    public WizardConfig run(String predefinedLanguage, boolean forceSkip) {
        try {
            // Step 1: Language selection (mandatory)
            String selectedLanguage = selectLanguage(predefinedLanguage);
            wizardConfig.setLanguage(selectedLanguage);

            // Step 2: If forceSkip is true, skip everything
            if (forceSkip) {
                skipWizard = true;
                terminal.writer().println("Setup wizard skipped via command line flag. Using default configuration.");
                terminal.writer().flush();
            } else {
                // Always try to ask if user wants to skip the wizard
                askSkipWizard();
                
                // Step 3: If not skipping, ask additional configuration questions
                if (!skipWizard) {
                    configureServer();
                }
            }

            return wizardConfig;
        } catch (Exception e) {
            log.error("Error during setup wizard", e);
            wizardConfig.setLanguage("eng"); // Default to English on error
            return wizardConfig;
        }
    }

    /**
     * Handles the language selection process.
     *
     * @param predefinedLanguage Optional predefined language from command line
     * @return Selected language code
     */
    private String selectLanguage(String predefinedLanguage) {
        terminal.writer().println();
        terminal.writer().println("═══════════════════════════════════════════════════════════");
        terminal.writer().println("          PowerNukkitX Setup Wizard");
        terminal.writer().println("═══════════════════════════════════════════════════════════");
        terminal.writer().println();
        terminal.writer().println("Welcome! Please choose a language first!");
        terminal.writer().println();

        // Handle predefined language
        if (predefinedLanguage != null && !predefinedLanguage.isEmpty()) {
            if (validateLanguage(predefinedLanguage)) {
                terminal.writer().println("✓ Using predefined language: " + predefinedLanguage);
                terminal.writer().flush();
                return predefinedLanguage;
            } else {
                terminal.writer().println("✗ Invalid predefined language: " + predefinedLanguage);
                terminal.writer().println("  Please choose a valid language from the list.");
                terminal.writer().flush();
            }
        }

        // Convert to list for indexed access
        List<Map.Entry<String, String>> languageList = new ArrayList<>(availableLanguages.entrySet());
        int selectedIndex = 0;
        
        terminal.writer().println("Use ↑/↓ arrow keys to navigate, Enter to select, or type language code:");
        terminal.writer().println();
        
        // Try arrow key navigation first
        try {
            while (true) {
                // Display menu with current selection highlighted
                displayLanguageMenu(languageList, selectedIndex);
                
                // Read input
                String input = reader.readLine("» Selection [" + languageList.get(selectedIndex).getKey() + "]: ").trim();
                
                if (input.isEmpty()) {
                    // User pressed Enter - use current selection
                    String selected = languageList.get(selectedIndex).getKey();
                    terminal.writer().println("✓ Language selected: " + selected + " (" + availableLanguages.get(selected) + ")");
                    terminal.writer().println();
                    terminal.writer().flush();
                    return selected;
                } else if (validateLanguage(input)) {
                    // User typed a valid language code
                    terminal.writer().println("✓ Language selected: " + input + " (" + availableLanguages.get(input) + ")");
                    terminal.writer().println();
                    terminal.writer().flush();
                    return input;
                } else {
                    // Try to navigate
                    switch (input.toLowerCase()) {
                        case "up", "u" -> {
                            selectedIndex = (selectedIndex - 1 + languageList.size()) % languageList.size();
                        }
                        case "down", "d" -> {
                            selectedIndex = (selectedIndex + 1) % languageList.size();
                        }
                        default -> {
                            terminal.writer().println("⚠ Invalid input. Enter a language code, 'up'/'down' to navigate, or press Enter to select highlighted.");
                            terminal.writer().flush();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reading language input", e);
            return "eng"; // Default to English on error
        }
    }
    
    /**
     * Displays the language menu with the current selection highlighted.
     */
    private void displayLanguageMenu(List<Map.Entry<String, String>> languageList, int selectedIndex) {
        terminal.writer().println("┌──────────────────────────────────────────────────────────┐");
        
        for (int i = 0; i < languageList.size(); i++) {
            Map.Entry<String, String> entry = languageList.get(i);
            String marker = (i == selectedIndex) ? "►" : " ";
            String highlight = (i == selectedIndex) ? "▶ " : "  ";
            terminal.writer().println("│ " + marker + " [" + entry.getKey() + "] " + highlight + entry.getValue());
        }
        
        terminal.writer().println("└──────────────────────────────────────────────────────────┘");
        terminal.writer().flush();
    }

    /**
     * Asks the user if they want to skip the setup wizard.
     */
    private void askSkipWizard() {
        terminal.writer().println();
        terminal.writer().println("═══════════════════════════════════════════════════════════");
        terminal.writer().println("          Additional Setup Configuration");
        terminal.writer().println("═══════════════════════════════════════════════════════════");
        terminal.writer().println();
        terminal.writer().flush();

        while (true) {
            try {
                String input = reader.readLine("» Do you want to skip the set-up wizard? (Y/n): ").trim().toLowerCase();

                if (input.isEmpty() || input.equals("y") || input.equals("yes")) {
                    skipWizard = true;
                    terminal.writer().println("✓ Setup wizard will be skipped. Server will use default configuration.");
                    terminal.writer().println();
                    terminal.writer().flush();
                    break;
                } else if (input.equals("n") || input.equals("no")) {
                    skipWizard = false;
                    terminal.writer().println("✓ Proceeding with setup wizard...");
                    terminal.writer().println();
                    terminal.writer().flush();
                    break;
                } else {
                    terminal.writer().println("⚠ Invalid input. Please enter 'Y' for yes or 'n' for no.");
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
     * Configures server settings through interactive prompts.
     */
    private void configureServer() {
        terminal.writer().println();
        terminal.writer().println("═══════════════════════════════════════════════════════════");
        terminal.writer().println("              Server Configuration");
        terminal.writer().println("═══════════════════════════════════════════════════════════");
        terminal.writer().println();
        terminal.writer().flush();
        
        // Configure server name
        configureServerName();
        
        // Configure server port
        configureServerPort();
        
        // Configure MOTD
        configureMotd();
        
        // Configure gamemode
        configureGamemode();
        
        // Configure whitelist
        configureWhitelist();
        
        // Configure operators
        configureOperators();
        
        terminal.writer().println();
        terminal.writer().println("═══════════════════════════════════════════════════════════");
        terminal.writer().println("          Configuration Complete!");
        terminal.writer().println("═══════════════════════════════════════════════════════════");
        terminal.writer().println();
        terminal.writer().println("✓ Your server will start with these settings.");
        terminal.writer().println();
        terminal.writer().flush();
    }
    
    /**
     * Configures server name.
     */
    private void configureServerName() {
        try {
            terminal.writer().println("─────────────────────────────────────────────────────────");
            String input = reader.readLine("» Server name [PowerNukkitX Server]: ").trim();
            if (!input.isEmpty()) {
                wizardConfig.setServerName(input);
            }
            terminal.writer().println("  ✓ Server name: " + wizardConfig.getServerName());
            terminal.writer().println();
            terminal.writer().flush();
        } catch (Exception e) {
            log.error("Error reading server name", e);
        }
    }
    
    /**
     * Configures server port.
     */
    private void configureServerPort() {
        terminal.writer().println("─────────────────────────────────────────────────────────");
        while (true) {
            try {
                String input = reader.readLine("» Server port [19132]: ").trim();
                if (input.isEmpty()) {
                    break; // Use default
                }
                
                int port = Integer.parseInt(input);
                if (port < 1 || port > 65535) {
                    terminal.writer().println("  ✗ Invalid port. Please enter a number between 1 and 65535.");
                    terminal.writer().flush();
                    continue;
                }
                
                wizardConfig.setPort(port);
                break;
            } catch (NumberFormatException e) {
                terminal.writer().println("  ✗ Invalid port number. Please enter a valid number.");
                terminal.writer().flush();
            } catch (Exception e) {
                log.error("Error reading server port", e);
                break;
            }
        }
        terminal.writer().println("  ✓ Server port: " + wizardConfig.getPort());
        terminal.writer().println();
        terminal.writer().flush();
    }
    
    /**
     * Configures server MOTD (Message of the Day).
     */
    private void configureMotd() {
        terminal.writer().println("─────────────────────────────────────────────────────────");
        try {
            String input = reader.readLine("» Server MOTD [PowerNukkitX Server]: ").trim();
            if (!input.isEmpty()) {
                wizardConfig.setMotd(input);
            }
            terminal.writer().println("  ✓ Server MOTD: " + wizardConfig.getMotd());
            terminal.writer().println();
            terminal.writer().flush();
        } catch (Exception e) {
            log.error("Error reading MOTD", e);
        }
    }
    
    /**
     * Configures default gamemode.
     */
    private void configureGamemode() {
        terminal.writer().println("─────────────────────────────────────────────────────────");
        terminal.writer().println("Available gamemodes:");
        terminal.writer().println("  [0] Survival");
        terminal.writer().println("  [1] Creative");
        terminal.writer().println("  [2] Adventure");
        terminal.writer().println("  [3] Spectator");
        terminal.writer().println();
        terminal.writer().flush();
        
        while (true) {
            try {
                String input = reader.readLine("» Default gamemode [0]: ").trim();
                if (input.isEmpty()) {
                    break; // Use default (0 = Survival)
                }
                
                int gamemode = Integer.parseInt(input);
                if (gamemode < 0 || gamemode > 3) {
                    terminal.writer().println("  ✗ Invalid gamemode. Please enter a number between 0 and 3.");
                    terminal.writer().flush();
                    continue;
                }
                
                wizardConfig.setGamemode(gamemode);
                break;
            } catch (NumberFormatException e) {
                terminal.writer().println("  ✗ Invalid number. Please enter a valid gamemode (0-3).");
                terminal.writer().flush();
            } catch (Exception e) {
                log.error("Error reading gamemode", e);
                break;
            }
        }
        
        String gamemodeName = switch (wizardConfig.getGamemode()) {
            case 1 -> "Creative";
            case 2 -> "Adventure";
            case 3 -> "Spectator";
            default -> "Survival";
        };
        terminal.writer().println("  ✓ Default gamemode: " + gamemodeName);
        terminal.writer().println();
        terminal.writer().flush();
    }
    
    /**
     * Configures whitelist settings.
     */
    private void configureWhitelist() {
        terminal.writer().println("─────────────────────────────────────────────────────────");
        
        while (true) {
            try {
                String input = reader.readLine("» Enable whitelist? (y/N): ").trim().toLowerCase();
                
                if (input.isEmpty() || input.equals("n") || input.equals("no")) {
                    wizardConfig.setEnableWhitelist(false);
                    terminal.writer().println("  ✓ Whitelist: Disabled");
                    terminal.writer().println();
                    terminal.writer().flush();
                    break;
                } else if (input.equals("y") || input.equals("yes")) {
                    wizardConfig.setEnableWhitelist(true);
                    terminal.writer().println("  ✓ Whitelist: Enabled");
                    terminal.writer().flush();
                    
                    // Ask for whitelisted players
                    configureWhitelistedPlayers();
                    break;
                } else {
                    terminal.writer().println("  ✗ Invalid input. Please enter 'y' for yes or 'n' for no.");
                    terminal.writer().flush();
                }
            } catch (Exception e) {
                log.error("Error reading whitelist setting", e);
                break;
            }
        }
    }
    
    /**
     * Configures whitelisted players.
     */
    private void configureWhitelistedPlayers() {
        try {
            terminal.writer().println();
            terminal.writer().println("  Enter whitelisted player names separated by commas");
            terminal.writer().println("  Example: Player1, Player2, Player3");
            terminal.writer().println("  Press Enter to skip:");
            terminal.writer().flush();
            
            String input = reader.readLine("  » ").trim();
            
            if (!input.isEmpty()) {
                String[] players = input.split(",");
                List<String> whitelisted = new ArrayList<>();
                for (String player : players) {
                    String cleanedName = player.trim();
                    if (!cleanedName.isEmpty()) {
                        whitelisted.add(cleanedName);
                    }
                }
                wizardConfig.setWhitelistedPlayers(whitelisted);
                
                if (!whitelisted.isEmpty()) {
                    terminal.writer().println("  ✓ Whitelisted players: " + String.join(", ", whitelisted));
                } else {
                    terminal.writer().println("  ⚠ No players added to whitelist.");
                }
                terminal.writer().println();
                terminal.writer().flush();
            } else {
                terminal.writer().println("  ⚠ No players added to whitelist.");
                terminal.writer().println();
                terminal.writer().flush();
            }
        } catch (Exception e) {
            log.error("Error reading whitelisted players", e);
        }
    }
    
    /**
     * Configures server operators.
     */
    private void configureOperators() {
        terminal.writer().println("─────────────────────────────────────────────────────────");
        try {
            terminal.writer().println("  Enter operator names separated by commas");
            terminal.writer().println("  Example: Admin1, Admin2");
            terminal.writer().println("  Press Enter to skip:");
            terminal.writer().flush();
            
            String input = reader.readLine("  » ").trim();
            
            if (!input.isEmpty()) {
                String[] ops = input.split(",");
                List<String> operators = new ArrayList<>();
                for (String op : ops) {
                    String cleanedName = op.trim();
                    if (!cleanedName.isEmpty()) {
                        operators.add(cleanedName);
                    }
                }
                wizardConfig.setOperators(operators);
                
                if (!operators.isEmpty()) {
                    terminal.writer().println("  ✓ Operators: " + String.join(", ", operators));
                } else {
                    terminal.writer().println("  ⚠ No operators added.");
                }
                terminal.writer().println();
                terminal.writer().flush();
            } else {
                terminal.writer().println("  ⚠ No operators added.");
                terminal.writer().println();
                terminal.writer().flush();
            }
        } catch (Exception e) {
            log.error("Error reading operators", e);
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
        String resourcePath = String.format("language/%s/lang.json", languageCode);
        try (InputStream conf = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            return conf != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Gets the wizard configuration.
     *
     * @return Wizard configuration with all settings
     */
    public WizardConfig getConfig() {
        return wizardConfig;
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
