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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class INIFile implements Serializable {

    private static final long serialVersionUID = 4971225065132584766L;
    private final Map<String, LinkedHashMap<String, String>> sections = new LinkedHashMap<>();

    // Loading and saving
    public void loadFromDisk(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            load(reader);
        }
    }
    
    public void loadFromResource(String resourcePath) throws IOException {
        try (var is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new FileNotFoundException(
                    "Embedded resource not found: " + resourcePath);
            }
            try (var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                load(reader);
            }
        }
    }

    public void load(Reader reader) throws IOException {
        try (var br = new BufferedReader(reader)) {
            String currentSection = "";
            sections.clear();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and full-line comments
                if (line.isEmpty() || line.startsWith(";")) {
                    continue;
                }
                // Section
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1).trim();
                    sections.putIfAbsent(currentSection, new LinkedHashMap<>());
                    continue;
                }
                // Key/value
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    sections.computeIfAbsent(currentSection, k -> new LinkedHashMap<>()).put(key, value);
                }
            }
        }
    }

    public void save(Path path) throws IOException {
        Files.writeString(path, toString());
    }

    // Getters
    public Integer getInt(String section, String key) {
        try {
            return Integer.valueOf(get(section, key, ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long getLong(String section, String key) {
        try {
            return Long.valueOf(get(section, key, ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Double getDouble(String section, String key) {
        try {
            return Double.valueOf(get(section, key, ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public boolean getBoolean(String section, String key) {
        switch(get(section, key, "")) {
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

    public String get(String section, String key) {
        return get(section, key, null);
    }

    public String get(String section, String key, String defaultValue) {
        var sec = sections.get(section);
        if (sec == null) return defaultValue;
        String result = sec.get(key);
        if (result == null) return defaultValue;
        for (int i = 1; i < result.length(); i++) {
            if (result.charAt(i) == ';' && 
                Character.isWhitespace(result.charAt(i - 1))) {
                return result.substring(0, i).trim();
            }
        }
        return result.trim();
    }   
    
    public String[] getKeys(String section) {
        return sections.get(section).keySet().toArray(String[]::new);
    }

    // Setters
    public void setInt(String section, String key, int value) {
        set(section, key, Integer.toString(value));
    }

    public void setLong(String section, String key, long value) {
        set(section, key, Long.toString(value));
    }
    
    public void setDouble(String section, String key, double value) {
        set(section, key, Double.toString(value));
    }

    public void setBoolean(String section, String key, boolean value) {
        set(section, key, value ? "1" : "0");
    }

    public void set(String section, String key, String value) {
        sections.computeIfAbsent(section, s -> new LinkedHashMap<>()).put(key, value);
    }

    public void removeKey(String section, String key) {
        var sec = sections.get(section);
        if (sec != null) {
            sec.remove(key);
            if (sec.isEmpty()) {
                sections.remove(section);
            }
        }
    }

    public void removeSection(String section) {
        sections.remove(section);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (var sectionEntry : sections.entrySet()) {
            sb.append("[").append(sectionEntry.getKey()).append("]\n");
            for (var kv : sectionEntry.getValue().entrySet()) {
                sb.append(kv.getKey())
                  .append("=")
                  .append(kv.getValue())
                  .append("\n");
            }
            sb.append("\n");
        }
        // Remove double line-endings at the end of the StringBuilder if they exist
        int l = sb.length();
        if (sb.substring(l - 2, l).equals("\n\n")) {
            sb.delete(l - 1, l);
        }
        return sb.toString();
    }
    
}

