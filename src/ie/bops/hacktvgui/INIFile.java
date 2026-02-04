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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

/**
 * Basic INI file reader/writer.
 * Uses BufferedReaders to read/write the contents of an INI file.
 * @author Stephen McGarry
 * 
 * To use this, provide a string containing the path to your INI file, or a
 * string containing the contents of a file. The getINIValue function will
 * detect what you have provided and act accordingly.
 * 
 * Comments are allowed on their own lines or in-line with values or section names.
 * 
 */

public class INIFile implements Serializable {
    
    private static final long serialVersionUID = 5132584766497122506L;
    
    /**
     * Returns a Boolean of the specified INI setting. This can be true, false, 1, or 0.
     * If the value was not found, a default value of false is returned.
     * 
     * @param input         A string containing the contents of the INI file, or its path
     * @param section       The section of the INI file that you want to look up
     * @param setting       The setting that you want to look up
     * @return              Returns a Boolean of the specified INI setting or a default value of false
     */
    public boolean getBooleanFromINI(String input, String section, String setting) {
        String v = getINIValue(input, section, setting, "").toLowerCase(Locale.ENGLISH);
        switch (v) {
            case "0":
            case "false":
                return false;
            case "1":
            case "true":
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Returns an integer of the specified INI setting.
     * 
     * @param input         A string containing the contents of the INI file, or its path
     * @param section       The section of the INI file that you want to look up
     * @param setting       The setting that you want to look up
     * @return              An integer value if found, or null if it was not
     */
    public Integer getIntegerFromINI(String input, String section, String setting) {
        try {
            return Integer.valueOf(getINIValue(input, section, setting, ""));
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Returns a double of the specified INI setting.
     * 
     * @param input         A string containing the contents of the INI file, or its path
     * @param section       The section of the INI file that you want to look up
     * @param setting       The setting that you want to look up
     * @return              A double value if found, or null if it was not
     */
    public Double getDoubleFromINI(String input, String section, String setting) {
        try {
            return Double.valueOf(getINIValue(input, section, setting, ""));
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
    
     /**
     * Returns a long of the specified INI setting.
     * 
     * @param input         A string containing the contents of the INI file, or its path
     * @param section       The section of the INI file that you want to look up
     * @param setting       The setting that you want to look up
     * @return              A long value if found, or null if it was not
     */
    public Long getLongFromINI(String input, String section, String setting) {
        try {
            return Long.valueOf(getINIValue(input, section, setting, ""));
        }
        catch (NumberFormatException e) {
            return null;
        }
    }   
    
    /**
     * Returns the specified INI setting as a string value, with the option of altering case.
     * 
     * @param input         A string containing the contents of the INI file, or its path
     * @param section       The section of the INI file that you want to look up
     * @param setting       The setting that you want to look up
     * @param defaultValue  The default value to return if the setting does not exist
     * @param preserveCase  If true, returns the setting as specified in the INI file. If false, returns the setting in lower case.
     * @return              Returns the value of the specified setting, or the specified default value if it does not exist.
     */
    public String getStringFromINI(String input, String section, String setting, String defaultValue, boolean preserveCase) {
        if (!preserveCase) {
            return getINIValue(input, section, setting, defaultValue).toLowerCase(Locale.ENGLISH);
        }
        else {
            return getINIValue(input, section, setting, defaultValue); 
        }
    }    
    
    /**
     * Returns the specified INI setting as a string value.
     * 
     * @param input         A string containing the contents of the INI file, or its path
     * @param section       The section of the INI file that you want to look up
     * @param setting       The setting that you want to look up
     * @param defaultValue  The default value to return if the setting does not exist
     * @return              Returns the value of the specified setting, or the specified default value if it does not exist.
     */
    public String getINIValue(String input, String section, String setting, String defaultValue) {
        // If the input string contains one line, treat it as a file path
        // Otherwise, treat it as the contents of an INI file
        if (!input.contains("\n")) input = loadIniFromDisk(input);
        if (input == null) return defaultValue;
        try (var sr1 = new BufferedReader(new StringReader(input))) {
            // Extract the INI section that we want
            String a;
            String b = null;
            boolean sectionStart = false;
            while ((a = sr1.readLine()) != null) {
                if (a.startsWith("[" + section + "]") && !sectionStart) {
                    // The header matches our query
                    sectionStart = true;
                } else if (!a.startsWith("[") && sectionStart && a.startsWith(setting)) {
                    // The setting matches our query after finding a header match
                    b = a.substring(a.indexOf("=") + 1);
                    break;
                } else if (a.startsWith("[") && sectionStart) {
                    // New section found, stop processing
                    break;
                }
            }
            if (b == null) return defaultValue;
            if (b.contains(";")) b = b.substring(0, b.indexOf(";")).trim();
            return b;
        } catch (IOException e) {
            System.err.println(e);
            return defaultValue;
        }
    }
    
    /**
     * Adds a boolean setting to a provided INI string.
     *
     * @param fileContents  The full INI file contents
     * @param section       The section that you want to write to
     * @param setting       The setting that you want to write to
     * @param value         The new value of the setting
     * @return              Returns fileContents with the new setting added or changed
     */
    public String setBooleanINIValue (String fileContents, String section, String setting, boolean value) {
        String stringValue;
        if (value == true) {
            stringValue = "true";
        }
        else {
            stringValue = "false";
        }
        return setINIValue(fileContents, section, setting, stringValue);
    }
    
    /**
     * Adds an integer setting to a provided INI string.
     *
     * @param fileContents  The full INI file contents
     * @param section       The section that you want to write to
     * @param setting       The setting that you want to write to
     * @param value         The new value of the setting
     * @return              Returns fileContents with the new setting added or changed
     */
    public String setIntegerINIValue (String fileContents, String section, String setting, Integer value) {
        return setINIValue(fileContents, section, setting, value.toString());
    }
    
    /**
     * Adds a double setting to a provided INI string.
     *
     * @param fileContents  The full INI file contents
     * @param section       The section that you want to write to
     * @param setting       The setting that you want to write to
     * @param value         The new value of the setting
     * @return              Returns fileContents with the new setting added or changed
     */
    public String setDoubleINIValue (String fileContents, String section, String setting, Double value) {
        return setINIValue(fileContents, section, setting, value.toString());
    }   
    
    /**
     * Adds a long setting to a provided INI string.
     *
     * @param fileContents  The full INI file contents
     * @param section       The section that you want to write to
     * @param setting       The setting that you want to write to
     * @param value         The new value of the setting
     * @return              Returns fileContents with the new setting added or changed
     */
    public String setLongINIValue (String fileContents, String section, String setting, Long value) {
        return setINIValue(fileContents, section, setting, value.toString());
    }
    
    /**
     * Adds a setting to a provided INI string.
     *
     * @param fileContents  The full INI file contents
     * @param section       The section that you want to write to
     * @param setting       The setting that you want to write to
     * @param value         The new value of the setting
     * @return              Returns fileContents with the new setting added or changed
     */
    public String setINIValue(String fileContents, String section, String setting, String value) {
        try (var sr1 = new BufferedReader(new StringReader(fileContents.trim()))) {
            // Extract the INI section that we want
            String a;
            String b = setting + "=" + value;
            boolean sectionStart = false;
            boolean written = false;
            var sb = new StringBuilder();
            if (fileContents.isBlank()) {
                // Create a new section
                sb.append("[").append(section).append("]\n");
            }
            while ((a = sr1.readLine()) != null) {
                if (a.startsWith("[" + section + "]") && !sectionStart) {
                    // We know we're in the right section
                    sectionStart = true;
                    sb.append(a).append("\n");
                } else if (a.startsWith(setting + "=") && (written)) {
                    // Duplicate entry, skip this one
                    System.err.println("Duplicate setting found, skipped");
                } else if (!a.startsWith("[") && sectionStart && a.startsWith(setting + "=")) {
                    // Setting found in section, append new value instead
                    sb.append(b).append("\n");
                    // We've already applied the setting, don't write another one
                    written = true;
                } else if (a.startsWith("[") && sectionStart) {
                    // End of section but setting not found, append to the end.
                    if (!written) {
                        sb.replace(sb.lastIndexOf("\n"), sb.length(), "");
                        sb.append(b).append("\n\n").append(a).append("\n");
                        written = true;
                        sectionStart = false;
                    }
                } else {
                    sb.append(a).append("\n");
                }
            }
            // Special handling if the section is at the end of the file and the
            // setting doesn't currently exist. Append the new setting to the end.
            if (!written && sb.substring(sb.lastIndexOf("[") + 1, sb.lastIndexOf("]")).equals(section)) {
                sb.replace(sb.lastIndexOf("\n") + 1, sb.length(), b);
                sb.append("\n");
            } else if (!written) {
                // Section does not exist, create at end of file
                sb.append("\n[").append(section).append("]\n").append(b).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    /**
     * Returns a single section of an INI file
     * @param fileContents  The contents of the INI file that you want to split
     * @param section       The section that you want to extract from the file
     * @return              Returns the section specified
     */
    public String splitINIfile(String fileContents, String section) {
        try (var sr1 = new BufferedReader(new StringReader(fileContents))) {
            // Extract the INI section that we want
            String a;
            boolean sectionStart = false;
            var sb = new StringBuilder();
            while ((a = sr1.readLine()) != null) {
                if (a.startsWith("[" + section + "]") && !sectionStart) {
                    // The header matches our query
                    sb.append(a).append("\n");
                    sectionStart = true;
                } else if (!sectionStart || a.startsWith(";")) {
                    // Do nothing
                } else if (a.startsWith("[")) {
                    // New section found, stop processing
                    break;
                } else {
                    if (a.contains(";")) {
                        // Strip out inline comments, just in case
                        sb.append(a.substring(0, a.indexOf(";")).trim());
                    } else {
                        sb.append(a);
                    }
                    sb.append("\n");
                }
            }
            return sb.toString().trim();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    private String loadIniFromDisk(String input) {
        var f = new File(input);
        try {
            return Files.readString(f.toPath(), StandardCharsets.UTF_8);
        } catch (MalformedInputException mie) {
            System.err.println(
                "Error reading " + f + "\nInvalid encoding received, UTF-8 expected.");
            return null;
        } catch (IOException ioe) {
            System.err.println(ioe);
            return null;
        }
    }
    
}
