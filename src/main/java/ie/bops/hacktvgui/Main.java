/*
 * Copyright (C) 2026 Stephen McGarry
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ie.bops.hacktvgui;

import java.awt.GraphicsEnvironment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    
    // Main method
    public static void main(String args[]) {
        // Don't run on a headless system
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("This application requires a graphical environment.");
            System.exit(-1);
        }
        Integer exitStatus = parseArguments(args);
        if (exitStatus != null) System.exit(exitStatus);
        // Pre-initialisation macOS tasks
        // These need to be done before creating the GUI class instance.
        // We'll set the dock icon later because that needs to be done after
        // the GUI class instance is created.
        if (System.getProperty("os.name").contains("Mac")) {
            // Put app name in the menu bar
            System.setProperty("apple.awt.application.name", Shared.APP_NAME);
            // Use the Mac menu bar
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // Set light/dark mode to current setting, seems to be broken
            // System.setProperty("apple.awt.application.appearance", "system");
        }
        SwingUtilities.invokeLater(() -> {
            try {
                // Prerequisite check
                Class.forName("com.formdev.flatlaf.util.SystemFileChooser");
                // Create MainWindow class instance
                final var g = new MainWindow();
                g.initUI();
                int s = g.postInitUI(args);
                if (s == 0) { // Exit code from postInitUI()
                    g.setVisible(true);
                } else {
                    System.exit(s);
                }
            } catch (ClassNotFoundException e) {
                // Missing FlatLaf library, fatal error
                String err = """
                             Unable to find the FlatLaf library.
                             Please ensure that the lib directory is present in the application
                             directory, and that it contains the FlatLaf JAR files.
                             The application will now exit.""";
                JOptionPane.showMessageDialog(null, err, Shared.APP_NAME, JOptionPane.ERROR_MESSAGE);
                System.exit(-2);
            }
        });
    }
    
    private static Integer parseArguments(String[] args) {
        boolean isWindows = (System.getProperty("os.name").contains("Windows"));
        for (String arg : args) {
            String a = arg.toLowerCase(Locale.ENGLISH);
            Path dllPath = Path.of(System.getProperty("user.dir"), "ConsoleCtrl_" + System.getProperty("os.arch") + ".dll");
            // If the emergency reset command is specified, remove all prefs.
            // This is a safety net, in case any bad preferences prevent us from running.
            // We handle this as early as possible to ensure it will work correctly.
            Set<String> resetArgs = Set.of("reset", "-reset", "--reset", "/reset");
            if (resetArgs.contains(a)) {
                // Reset all preferences and exit
                Shared.resetPreferences();
                return 0;
            } else if (a.equals("/copyhacktv")) {
                if (!isWindows) return 0;
                // Unzip hacktv.zip
                if (!Shared.unzipHackTV()) return 1;
                return 0;
            } else if (a.equals("/copydll")) {
                if (!isWindows) return 0;
                // Copies the JNI DLL for this architecture
                ConsoleCtrlJNI.initialise(Path.of(System.getProperty("user.dir")));
                return 0;
            } else if (a.equals("/copydllandhacktv")) {
                if (!isWindows) return 0;
                // Copies the JNI DLL for this architecture and unzips hacktv
                ConsoleCtrlJNI.initialise(Path.of(System.getProperty("user.dir")));
                // Unzip hacktv.zip if it exists
                if (!Shared.unzipHackTV()) return 1;
                if (!Files.exists(dllPath)) return 1;
                return 0;
            }
        }
        return null;
    }
        
}
