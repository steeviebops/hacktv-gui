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

import java.awt.HeadlessException;
import java.util.Locale;
import javax.swing.SwingUtilities;

public class Main {
    // Main method
    public static void main(String args[]) {
        // If the emergency reset command is specified, remove all prefs.
        // This is a safety net, in case any bad preferences prevent us from running.
        // We handle this as early as possible to ensure it will work correctly.
        if (args.length > 0) {
            switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "reset":
                case "-reset":
                case "--reset":
                case "/reset":
                // Reset all preferences and exit
                Shared.resetPreferences();
                return;                    
            }
        }
        // Pre-initialisation macOS tasks
        // These need to be done before creating the GUI class instance.
        // We'll set the dock icon later because that needs to be done after
        // the GUI class instance is created.
        if (System.getProperty("os.name").contains("Mac")) {
            // Put app name in the menu bar
            System.setProperty("apple.awt.application.name", GUI.APP_NAME);
            // Use the Mac menu bar
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // Set light/dark mode to current setting, seems to be broken
            // System.setProperty("apple.awt.application.appearance", "system");
        }
        try {
            SwingUtilities.invokeLater(() -> {
                // Create GUI class instance
                final var g = new GUI();
                int s = g.populateUI(args);
                if (s == 0) {
                    // Prevent window from being resized below the current size
                    g.setMinimumSize(g.getSize());
                    g.setVisible(true);
                }
                else {
                    System.exit(s);
                }
            });
        }
	catch (HeadlessException e) {
            // Catch this error if we find we're running on a headless JRE or an
            // OS with no GUI support (e.g. WSL or Unix without X).
            System.err.println("A fatal error occurred while attempting to "
                    + "initialise the window, please see details below.\n" + 
                    e.getMessage());
            System.exit(-1);
        }
    }
    

    
}
