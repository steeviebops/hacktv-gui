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

// Helper class for populating (and reading) the available test signals
public final class TestSignalOption {
    private final String command;
    private final String displayName;
    private final String patternFilename;
    private final boolean textInsertSupported;
    private final String sampleRate;

    public TestSignalOption(String command, String displayName, String patternFilename, boolean textInsertSupported, String sampleRate) {
        this.command = command;
        this.displayName = displayName;
        this.patternFilename = patternFilename == null ? "" : patternFilename;
        this.textInsertSupported = textInsertSupported;
        this.sampleRate = sampleRate == null ? "" : sampleRate;
    }

    public String command() { return command; }
    public String displayName() { return displayName; }
    public String patternFilename() { return patternFilename; }
    public boolean textInsertSupported() { return textInsertSupported; }
    public String sampleRate() { return sampleRate; }

    @Override public String toString() {
        // Lets the JComboBox display friendly names
        return displayName;
    }
}
