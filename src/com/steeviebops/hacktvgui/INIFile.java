/*
 * Copyright (C) 2021 Stephen McGarry
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

package com.steeviebops.hacktvgui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Basic INI file reader/writer.
 * Uses regular expressions to read/write the contents of an INI file.
 * @author Stephen McGarry
 * 
 * To use this, provide a string containing the path to your INI file, or a
 * string containing the contents of a file. The getINIValue function will
 * detect what you have provided and act accordingly.
 * 
 * Comments are allowed on their own lines or in-line with values, but not 
 * in-line with section names.
 * 
 */

public class INIFile {
    
    public static boolean getBooleanFromINI(String input, String section, String setting) {
        String v = getINIValue(input, section, setting, "").toLowerCase();
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
    
    public static Integer getIntegerFromINI(String input, String section, String setting) {
        try {
            return Integer.parseInt(getINIValue(input, section, setting, ""));
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static Double getDoubleFromINI(String input, String section, String setting) {
        try {
            return Double.parseDouble(getINIValue(input, section, setting, ""));
        }
        catch (NumberFormatException e) {
            return null;
        }
    }    
    
    public static String getStringFromINI(String input, String section, String setting, String defaultValue, Boolean preserveCase) {
        if (!preserveCase) {
            return getINIValue(input, section, setting, defaultValue).toLowerCase();
        }
        else {
            return (getINIValue(input, section, setting, defaultValue)); 
        }
    }    
    
    public static String getINIValue(String input, String section, String setting, String defaultValue) {
        String fileContents;
        // If the setting string contains a backslash, square brackets or equals, abort immediately.
        if ((setting.contains("\\")) || (setting.contains("[")) || 
                (setting.contains("]")) || (setting.contains("=")) ) {
            System.err.println("INI setting cannot contain a backslash, square brackets or equals symbol.");
            return null;
        }
        // If the input string contains one line, treat it as a file path
        // Otherwise, treat it as the contents of an INI file
        long lines = input.chars().filter(x -> x == '\n').count() + 1;
        if (lines == 1) {
            // Load the specified file to a string named fileContents
            File f = new File(input);
            try {
                fileContents = Files.readString(f.toPath(), StandardCharsets.UTF_8);
            }
            catch (IOException e) {
                return defaultValue;
            }            
        }
        else {
            // Set the fileContents variable to whatever we received from input
            fileContents = input;
        }
        // Remove all CR characters
        fileContents = fileContents.replaceAll("\r\n", "\n");
        fileContents = fileContents.replaceAll("\r", "\n");        
        // Remove any comment lines so they don't interfere with processing
        fileContents = Stream.of(fileContents.split("\n"))
                .filter(s -> !s.startsWith(";"))
                .collect(Collectors.joining("\n"));
        // Remove any white spaces between the setting and value
        fileContents = fileContents.replaceAll("[^\\S\n]([=])[^\\S\n]", "=");
        // Extract the specified section from fileContents to parsedSection
        String parsedSection = null;
        String r1 = "(?ms)^\\[";
        String r2 = "](?:(?!^\\[[^]\\n]+]).)*";
        Pattern pattern = Pattern.compile(r1 + section + r2);
        Matcher matcher = pattern.matcher(fileContents);
        while (matcher.find()) {
            parsedSection = matcher.group(0);
        }
        
        // If the specified section was not found, return the default value
        if (parsedSection == null) return defaultValue;
        
        /* Check for special characters in the provided setting.
         * If found, we need to escape them.
         * We do this by checking for each character in the string 'sc' below.
         */
        String ps;
        String sc = "!@#$%&*()'+,-./:<>?^_`{|}";
        for (int i = 0; i < setting.length(); i++) {
            if (i != setting.length() - 1) {
                // Extract a single character from 'settings' to 'ps'
                ps = setting.substring(i, i + 1);
            }
            else {
                // If we have reached the last character in the sequence, don't
                // try to read the next one as we'll overflow.
                ps = setting.substring(i);
            }
            for (int x = 0; x < sc.length(); x++) {
                // If 'ps' matches one of the characters in 'sc' but is not a backslash
                if ((ps.equals(sc.substring(x, x + 1)) && (i > 0))) {
                    if (!setting.substring(i - 1, i).equals("\\")) {
                        // Match found, append escape character
                        setting = setting.replace(ps, "\\" + ps);
                    }                    
                }
                else if ((ps.equals(sc.substring(x, x + 1)) && (i == 0))) {
                    if (!setting.substring(i).equals("\\")) {
                        // Match found, append escape character
                        setting = setting.replace(ps, "\\" + ps);
                    }        
                }
            }
        }
        
        // Extract the required setting from parsedSection and return its value
        // If value is null, return the specified default value instead
        String result = null;
        
        String regex1 = "(?i)(?<=^";
        String regex2 = "=)[^\n]*";

        Pattern p = Pattern.compile(regex1 + setting + regex2, Pattern.MULTILINE);
        Matcher m = p.matcher(parsedSection);

        while (m.find()) {
            if (result == null) result = m.group(0);
        }
        if (result != null) {
            // Return the result, minus any in-line comments and with
            // leading and/or trailing whitespaces removed.
            return result.replaceAll(";.*", "").trim();
        }
        else {
            return defaultValue;
        } 
    }
    
    public static String setBooleanINIValue (String fileContents, String section, String setting, boolean value) {
        String stringValue;
        if (value == true) {
            stringValue = "true";
        }
        else {
            stringValue = "false";
        }
        return setINIValue(fileContents, section, setting, stringValue);
    }
    
    public static String setIntegerINIValue (String fileContents, String section, String setting, Integer value) {
        return setINIValue(fileContents, section, setting, value.toString());
    }
    
    public static String setDoubleINIValue (String fileContents, String section, String setting, Double value) {
        return setINIValue(fileContents, section, setting, value.toString());
    }   
    
    public static String setLongINIValue (String fileContents, String section, String setting, Long value) {
        return setINIValue(fileContents, section, setting, value.toString());
    }
    
    public static String setINIValue(String fileContents, String section, String setting, String value) {
        /**
         *  The way we save a file is as follows:
         *  - Feed a string into this method using the first parameter
         *  - This string is loaded to a variable named fileContents
         *  - Use a regex to retrieve the section names to an ArrayList
         *  - Use another regex to split the file into its sections
         *  - Query the first ArrayList for the specified section
         *  - If found, retrieve the requested section
         *  - If not found, create a new entry in the lists
         *  - Append the value to the requested section
         *  - Merge all sections and return a string with the new file contents
         * 
         */
        
        // If a blank string was specified, add the section provided to
        // create the contents of a new "file"
        if (fileContents.isBlank()) fileContents = "[" + section + "]\n";
        
        // Remove any CR characters
        fileContents = fileContents.replaceAll("\r\n", "\n");
        fileContents = fileContents.replaceAll("\r", "\n");

        // Retrieve the requested section
        String selectedSection = splitINIfile(fileContents, section);
        
        // Retrieve all INI section names
        String r = "^\\[[^\\]\\n]+]";
        Pattern p = Pattern.compile(r, Pattern.MULTILINE);
        Matcher m = p.matcher(fileContents);
        // Add the results to an ArrayList so we can read it later
        ArrayList <String> iniSectionNames = new ArrayList<>();
        while (m.find()) {
            iniSectionNames.add(m.group().substring(1,m.group().length() -1));
        }
        
        // Retrieve all sections, including data
        ArrayList <String> allSections = new ArrayList<>();
        for (int i = 0; i < iniSectionNames.size(); i++) {
            allSections.add(splitINIfile(fileContents, iniSectionNames.get(i)));
        }
        
        // Query the first ArrayList for the specified section
        for (int i = 0; i < iniSectionNames.size(); i++) {           
            if (selectedSection == null) {
                // If not found, create a new section entry in the lists
                iniSectionNames.add(section);
                selectedSection = "\n[" + section + "]" + "\n" + setting + "=" + value + "\n";
                allSections.add(selectedSection);
                break;
            }
            else if ( ( iniSectionNames.get(i).equals(section) ) ) {
                // If found, retrieve the requested section
                selectedSection = selectedSection.trim() + "\n" + setting + "=" + value + "\n";
                allSections.set(i, selectedSection);
            }
        }
        
        // Merge all sections to a string named newContents
        String newContents = "";
        for (int i = 0; i < allSections.size(); i++) {
            newContents = newContents + allSections.get(i).replace("\n\n", "\n");
        }
        // Make it look nicer by adding an empty line betwen sections
        newContents = newContents.replaceAll("\n\\[", "\n\n\\[");
        
        // Return changes
        return newContents;
    }
    
    public static String splitINIfile(String fileContents, String section) {
        String selectedSection = null;
        // Remove any CR characters
        fileContents = fileContents.replace("\r\n", "\n");
        fileContents = fileContents.replace("\r", "\n");
        // Extract the specified section from fileContents
        String r1 = "^\\[";
        String r2 = "](?:\\n(?:[^\\[\\n].*)?)*";
        Pattern p1 = Pattern.compile(r1 + section + r2, Pattern.MULTILINE);
        Matcher m1 = p1.matcher(fileContents);
        // Add the result to a string
        while (m1.find()) {
            selectedSection = m1.group();
        }
        // Return the string we created
        return selectedSection;
    }
    
}
