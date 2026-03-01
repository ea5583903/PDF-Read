import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class VersionChecker {
    private static final String CURRENT_VERSION = "1.0";
    private static final String VERSION_FILE = "version.txt";
    private String latestVersion;

    public VersionChecker() {
        checkVersion();
    }

    private void checkVersion() {
        File versionFile = new File(VERSION_FILE);

        if (!versionFile.exists()) {
            // Create version file with current version
            try (FileWriter writer = new FileWriter(versionFile)) {
                writer.write(CURRENT_VERSION);
                latestVersion = CURRENT_VERSION;
            } catch (IOException e) {
                latestVersion = CURRENT_VERSION;
            }
        } else {
            // Read version from file
            try (BufferedReader reader = new BufferedReader(new FileReader(versionFile))) {
                String fileVersion = reader.readLine();
                if (fileVersion != null && !fileVersion.trim().isEmpty()) {
                    latestVersion = fileVersion.trim();
                } else {
                    latestVersion = CURRENT_VERSION;
                }
            } catch (IOException e) {
                latestVersion = CURRENT_VERSION;
            }
        }
    }

    public boolean isNewVersionAvailable() {
        if (latestVersion == null) {
            return false;
        }

        // Compare versions (simple string comparison for now)
        try {
            double current = Double.parseDouble(CURRENT_VERSION);
            double latest = Double.parseDouble(latestVersion);
            return latest > current;
        } catch (NumberFormatException e) {
            return !CURRENT_VERSION.equals(latestVersion);
        }
    }

    public String getLatestVersion() {
        return latestVersion != null ? latestVersion : CURRENT_VERSION;
    }

    public String getCurrentVersion() {
        return CURRENT_VERSION;
    }

    // Method to manually set a new version in the version.txt file
    public static void updateVersionFile(String newVersion) {
        try (FileWriter writer = new FileWriter(VERSION_FILE)) {
            writer.write(newVersion);
        } catch (IOException e) {
            System.err.println("Error updating version file: " + e.getMessage());
        }
    }
}
