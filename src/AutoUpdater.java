import javax.swing.*;
import java.io.*;
import java.nio.file.*;

public class AutoUpdater {
    private static final String CURRENT_VERSION = "1.0";
    private static final String VERSION_FILE = "version.txt";
    private static final String UPDATE_FOLDER = "updates";
    private static final String BACKUP_FOLDER = "backup";

    public static void checkAndUpdate() {
        File updateFolder = new File(UPDATE_FOLDER);
        File versionFile = new File(VERSION_FILE);

        // Create version file if it doesn't exist
        if (!versionFile.exists()) {
            try (FileWriter writer = new FileWriter(versionFile)) {
                writer.write(CURRENT_VERSION);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // Check if update folder exists with new version
        if (!updateFolder.exists() || !updateFolder.isDirectory()) {
            return;
        }

        File updateVersionFile = new File(updateFolder, "version.txt");
        if (!updateVersionFile.exists()) {
            return;
        }

        try {
            String latestVersion = readVersionFile(updateVersionFile);
            String currentVersion = readVersionFile(versionFile);

            if (isNewerVersion(latestVersion, currentVersion)) {
                int choice = JOptionPane.showConfirmDialog(null,
                        "New version " + latestVersion + " is available!\n" +
                        "Current version: " + currentVersion + "\n\n" +
                        "Do you want to update now?",
                        "Update Available",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    performUpdate(latestVersion);
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking for updates: " + e.getMessage());
        }
    }

    private static void performUpdate(String newVersion) throws IOException {
        JDialog progressDialog = new JDialog();
        progressDialog.setTitle("Updating...");
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(null);
        progressDialog.setModal(false);

        JLabel statusLabel = new JLabel("Updating to version " + newVersion + "...", SwingConstants.CENTER);
        progressDialog.add(statusLabel);
        progressDialog.setVisible(true);

        try {
            // Create backup folder
            File backupFolder = new File(BACKUP_FOLDER);
            if (!backupFolder.exists()) {
                backupFolder.mkdir();
            }

            // Backup current source files
            File srcFolder = new File("src");
            if (srcFolder.exists()) {
                backupDirectory(srcFolder.toPath(), new File(backupFolder, "src").toPath());
            }

            // Copy new files from update folder to src
            File updateSrcFolder = new File(UPDATE_FOLDER, "src");
            if (updateSrcFolder.exists()) {
                copyDirectory(updateSrcFolder.toPath(), srcFolder.toPath());
            }

            // Update version file
            try (FileWriter writer = new FileWriter(VERSION_FILE)) {
                writer.write(newVersion);
            }

            progressDialog.dispose();

            JOptionPane.showMessageDialog(null,
                    "Update completed successfully!\n" +
                    "The application will now restart.",
                    "Update Complete",
                    JOptionPane.INFORMATION_MESSAGE);

            // Restart application
            restartApplication();

        } catch (Exception e) {
            progressDialog.dispose();
            JOptionPane.showMessageDialog(null,
                    "Error during update: " + e.getMessage() + "\n" +
                    "Backup saved in '" + BACKUP_FOLDER + "' folder.",
                    "Update Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void restartApplication() {
        try {
            // Get current directory
            String currentDir = System.getProperty("user.dir");

            // Build restart command
            ProcessBuilder pb;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "start", "cmd", "/c", "run.bat");
            } else {
                pb = new ProcessBuilder("sh", "-c", "./run.sh");
            }

            pb.directory(new File(currentDir));
            pb.start();

            // Exit current instance
            System.exit(0);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Please restart the application manually.",
                    "Restart Required",
                    JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }

    private static String readVersionFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String version = reader.readLine();
            return version != null ? version.trim() : "0.0";
        }
    }

    private static boolean isNewerVersion(String latest, String current) {
        try {
            double latestNum = Double.parseDouble(latest);
            double currentNum = Double.parseDouble(current);
            return latestNum > currentNum;
        } catch (NumberFormatException e) {
            return !latest.equals(current);
        }
    }

    private static void backupDirectory(Path source, Path target) throws IOException {
        if (Files.exists(target)) {
            deleteDirectory(target);
        }
        Files.createDirectories(target);

        Files.walk(source)
            .forEach(sourcePath -> {
                try {
                    Path targetPath = target.resolve(source.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    System.err.println("Error backing up: " + e.getMessage());
                }
            });
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source)
            .forEach(sourcePath -> {
                try {
                    Path targetPath = target.resolve(source.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        if (!Files.exists(targetPath)) {
                            Files.createDirectories(targetPath);
                        }
                    } else {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    System.err.println("Error copying: " + e.getMessage());
                }
            });
    }

    private static void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        System.err.println("Error deleting: " + e.getMessage());
                    }
                });
        }
    }
}
