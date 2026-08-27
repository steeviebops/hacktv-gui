/*
 * Copyright (C) 2025 Stephen McGarry
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

/**
* Various functions and methods not directly related to the GUI code.
*/

import com.formdev.flatlaf.json.Json;
import com.formdev.flatlaf.json.ParseException;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.prefs.BackingStoreException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

public class Shared implements Serializable {
    
    // Application name, used in message boxes and the About box
    public static final String APP_NAME = "hacktv-gui";
    
    // URL for downloading configuration files and hacktv updates
    public static final String DOWNLOAD_SERVER = "https://download.bops.ie/";
    
    // VideoCrypt scrambling keys for which EMM operation is permitted
    public static final Set<String> EMM_KEYS = Set.of("sky06", "sky07", "sky09", "skynz01", "skynz02");
    
    private static final long serialVersionUID = -8155770639405775482L;
    
    public static void resetPreferences() {
        // Delete the preference store and everything in it
        try {
            MainWindow.PREFS.removeNode();
            System.out.println("All preferences have been reset to defaults.");
        }
        catch (BackingStoreException e) {
            System.err.println("Reset failed: " + e.getMessage());
        }
    }
  
    public static int calculateLuhnCheckDigit(long input) {
        // Calculates a check digit for the specified input using the Luhn algorithm
        long t = 0;
        // Read backwards, doubling every other digit
        for (long l = input; l > 0; l = l / 100) {
            // Double l and add it to t.
            // If the result is greater than 9, the formula below will
            // add the individual digits, e.g. 14 is 1 + 4 = 5.
            t = t + ( ((l % 10 * 2) / 10) + ((l % 10 * 2) % 10) );
        }
        // Read backwards again, add the remaining digits as-is
        for (long l = input / 10; l > 0; l = l / 100) {
            t = t + (l % 10);
        }
        // Multiply t by 9, the result of Mod10 is the check digit
        return (int) ((t * 9) % 10);
    }
        
    public static boolean luhnCheck(Long input) {
         // Feed the full number to this method and it will return true or 
         // false based on whether the check digit is valid or not.
        return calculateLuhnCheckDigit(input / 10) == (input % 10);
    }
    
    public static boolean isNumeric(String strNum) {
	if (strNum == null) {
	    return false;
	}
	try {
	    Double.valueOf(strNum);
	}
        catch (NumberFormatException nfe) {
	    return false;
	}
	return true;
    }
    
    public static boolean isHex(String input) {
	if (input == null) return false;
        return input.matches("^[0-9a-fA-F]+$");
    }
    
    public static int wildcardFind(String pathToScan, String startsWith, String endsWith) {
        // Returns the number of files found in a directory with the specified start and end strings
        // Case insensitive, feed it with lowercase filenames
        String fileToFilter;
        var folderToScan = new File(pathToScan);
        File[] listOfFiles = folderToScan.listFiles();
        int c = 0;
        // If the specified directory does not exist, return 0 and stop
        if (!Files.exists(folderToScan.toPath())) return 0;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                fileToFilter = listOfFile.getName();
                // If a file is found, increment c by one
                if (fileToFilter.toLowerCase(Locale.ENGLISH).startsWith(startsWith)
                        && fileToFilter.toLowerCase(Locale.ENGLISH).endsWith(endsWith)) {
                    c = c + 1;
                }
            }
        }
        return c;
    }    
    
    public static void deleteFSObject(Path pathToBeDeleted) throws IOException {
        // Deletes the path specified to this method
	Files.walkFileTree(pathToBeDeleted, 
	  new SimpleFileVisitor<Path>() {
	    @Override
	    public FileVisitResult postVisitDirectory(
	      Path dir, IOException exc) throws IOException {
	        Files.delete(dir);
	        return FileVisitResult.CONTINUE;
	        }
	        
	    @Override
	    public FileVisitResult visitFile(
	      Path file, BasicFileAttributes attrs) 
	      throws IOException {
	        Files.delete(file);
	        return FileVisitResult.CONTINUE;
	    }
	});        
    }
    
    public static void copyResource(String res, String dest, Class c) throws IOException {
        InputStream src = c.getResourceAsStream(res);
        Files.copy(src, Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
    }
    
    public static String stripQuotes(String FilePath) {
        // Bug fix for cases where a path containing quotes is pasted into
        // the file open prompt. This causes Swing to prepend the current
        // directory to the path (with the intended file path including
        // quotes at the end). This can cause things to break badly; if 
        // this path is saved to the preferences store it can prevent the 
        // application from opening! So we check for it and strip the path.
        if (FilePath.contains("\\\"") ) {
            FilePath = FilePath.substring(FilePath.indexOf("\""));
        }
        if (FilePath.startsWith("\"")) FilePath = FilePath.substring(1);
        if (FilePath.endsWith("\"")) FilePath = FilePath.substring(0, FilePath.length() -1);
        return FilePath;
    }
    
    public static Date getLastUpdatedTime(String jarFilePath, String classFilePath) {
        try (var jar = new JarFile(jarFilePath)) {
            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry file = enumEntries.nextElement();
                if (file.getName().equals(classFilePath.substring(1))) {
                    long time=file.getTime();
                    return time==-1?null: new Date(time);
                }
            }
        }
        catch (IOException e) {
            return null;
        }
        return null;
     }

    public static void download(String url, Path fileName) throws IOException, URISyntaxException {
        var connection = new URI(url).toURL().openConnection();
        connection.setUseCaches(false);
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, fileName);  
        }
    }
    
    public static String downloadToString(String url) throws IOException, URISyntaxException {
        // Downloads a file directly to a string, bypassing the file system
        var connection = new URI(url).toURL().openConnection();
        connection.setUseCaches(false);
        try (InputStream in = connection.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    // Unzip code courtesy of https://www.baeldung.com/java-compress-and-uncompress
    public static void unzipFile(String fileZip, String destination) throws IOException {
        var destDir = new File(destination);
        var buffer = new byte[1024];
        try (var zis = new ZipInputStream(new FileInputStream(fileZip))) {
            var zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                var newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    var parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    
                    try ( // write file content
                        var fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    // Reset timestamp to original
                    if (!newFile.setLastModified(zipEntry.getTime())) {
                        System.err.println("Failed to set timestamp.");
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }
    
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        var destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
    
    public static String getCurrentDirectory() {
        try {
            // Get the current directory path
            CodeSource codeSource = Shared.class.getProtectionDomain().getCodeSource();
            var jarFile = new File(codeSource.getLocation().toURI().getPath());
            return jarFile.getParentFile().getPath();         
        }
        catch (URISyntaxException ex) {
            System.out.println(ex);
            return "";
        }        
    }
    
    public static void launchBrowser(String u) throws IOException {
        // Try using the native Desktop class
        if ( (Desktop.isDesktopSupported()) &&
               (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) ) {
            Desktop.getDesktop().browse(URI.create(u));
        }
        else {
            // Try using xdg-open
            var p = new ProcessBuilder("xdg-open", u);
            p.start();            
        }
    }
    
    public static void mouseWheelComboBoxHandler(int evt, JComboBox jcb) {
        /*
         * evt contains the number of clicks from the mouse wheel
         * A single spin upwards reports -1
         * A aingle spin downwards reports 1
         *
         * jcb is the name of the JComboBox that you want to manipulate
         */
        if (jcb.isEnabled()) { // Don't do anything if the combobox is disabled
            if (evt < 0) {
                int p = evt * -1; // negative * negative = positive
                if (jcb.getSelectedIndex() - p >= 0) jcb.setSelectedIndex(jcb.getSelectedIndex() - p);
            }
            else if (evt > 0) {
                if (evt + jcb.getSelectedIndex() < jcb.getItemCount()) jcb.setSelectedIndex(jcb.getSelectedIndex() + evt);
            }
        }
    }
    
    public static void toggleCheckBox(JCheckBox jcb, boolean status) {
        if (!status && jcb.isSelected()) jcb.doClick();
        jcb.setEnabled(status);
    }
    
    public static void messageBox(String msg, int type) {
        // type can be any of the following (from -1 to 3)
        // PLAIN_MESSAGE, ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE
        // or QUESTION_MESSAGE
        JOptionPane.showMessageDialog(null, msg, APP_NAME, type);
    }
    
    public static String longToDecimal (long l) {
        return BigDecimal.valueOf(l)
                .divide(BigDecimal.valueOf(1000000))
                .setScale(3, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
    
    public static ComboBoxOption addComboBoxOption(String value, String label){
        return new ComboBoxOption(value, label);
    }
    
    public static String checkCardNumber(String cardNumber, ScramblingInfo selectedCA, String selectedKey) {
        return switch (selectedCA.id()) {
            case "videocrypt" -> checkVC1CardNumber(cardNumber, selectedKey);
            case "videocrypt2" -> checkVC2CardNumber(cardNumber);
            default -> null;
        };
    }
    
    private static String checkVC1CardNumber(String cardNumber, String selectedKey) {
        /* Sky UK/NZ viewing cards use the Luhn algorithm to verify if the
         * card number is valid. So we will use it here too.
         *
         * UK 06/07 cards have either 13-digit or 9-digit numbers.
         * UK 09 cards are 9-digit only.
         * NZ and MultiChoice cards are 11 digits.
         * So we restrict input to these lengths depending on the selected card.
         */
        int keyStart = -1;
        int keyEnd = -1;
        String length;
        boolean qs = false;
        switch (selectedKey) {
            case "sky06" -> {
                // 13-digit (standard) or 9-digit (Quick Start) cards
                length = "9 or 13";
                if (cardNumber.length() == 13) {
                    // Only digits 4-13 of 13-digit card numbers are Luhn checked.
                    // We need to strip out the first four digits.
                    keyStart = 4;
                    keyEnd = 13;
                    break;
                }
                if (cardNumber.length() == 9) {
                    if (checkQuickStartCard(cardNumber)) {
                        qs = true;
                        // Bogus value to get past length check
                        keyStart = -2;
                    }
                    else {
                        // Luhn check failed
                        return null;
                    }
                }
            }
            case "sky07" -> {
                // 13-digit (standard) or 9-digit cards
                length = "9 or 13";
                if (cardNumber.length() == 13) {
                    // Only digits 4-13 of 13-digit card numbers are Luhn checked.
                    // We need to strip out the first four digits.
                    keyStart = 4;
                    keyEnd = 13;
                    break;
                }
                if (cardNumber.length() == 9) {
                    keyStart = 0;
                    keyEnd = 9;
                }
            }
            case "sky09" -> {
                // 9 digit cards only
                length = "9";
                if (cardNumber.length() == 9) {
                    keyStart = 0;
                    keyEnd = 9;
                }
            }
            case "skynz01", "skynz02" -> {
                // 11-digit cards, only digits 4-11 are Luhn checked
                length = "11";
                if (cardNumber.length() == 11) {
                    keyStart = 4;
                    keyEnd = 11;
                }
            }
            default -> {
                return null;
            }
        }
        if (!isNumeric(cardNumber) || keyStart == -1) {
            Shared.messageBox("Card number should be exactly " + length + " digits.", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        else if ((!qs) && (!luhnCheck(Long.valueOf(cardNumber.substring(keyStart, keyEnd))))) {
            Shared.messageBox("Card number appears to be invalid (Luhn check failed).", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        else {
            // Make sure that we're not trying to send EMMs to the wrong card type.
            if (!checkEMMCardType(cardNumber, selectedKey)) {
                return null;
            }
            else if (qs) {
                // Special handling for Quick Start cards
                return cardNumber.substring(2);
            }
            else {
                // hacktv doesn't use the check digit so strip it out
                return cardNumber.substring(keyStart, keyEnd - 1);
            }
        }
    }
    
    private static String checkVC2CardNumber(String cardNumber) {
        // 11-digit numbers only, no Luhn check on these cards
        if (cardNumber.length() == 11) {
            // This is probably wrong!
            return cardNumber.substring(3);
        }
        else {
            Shared.messageBox("Card number should be exactly 11 digits.", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }
    
    private static boolean checkQuickStartCard(String cardNumber) {
        /*
         * BSkyB Quick Start card algorithm, as explained to me by the author of settopbox.org.
         *
         * 1 - Remove the first two digits (issue number).
         * 2 - The first digit of what's remaining is the check digit, so remove that too
         * 3 - Invert the remaining digits (so 123456 becomes 654321)
         * 4 - Prepend the issue number to the inverted digits
         * 5 - Run that through the Luhn check, the result should be the digit
         *     you removed in step 2.
         */
        String issueNumber = cardNumber.substring(0, 2);
        int checkDigit = Integer.parseInt(cardNumber.substring(2, 3));
        String reversedNumber = new StringBuilder(cardNumber.substring(3)).reverse().toString();
        if (calculateLuhnCheckDigit(Long.parseLong(issueNumber + reversedNumber)) == checkDigit) {
            return true;
        }
        else {
            Shared.messageBox("Card number appears to be invalid (Luhn check failed).", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }
    
    private static boolean checkEMMCardType(String cardNumber, String selectedKey) {
        // Make sure that we're not trying to send EMMs to the wrong card type.
        // Used info from settopbox.org to get a rough idea of the range and
        // make an educated guess based on that information.
        // If you have a legitimate card that fails this check, let me know.
        String WrongCardType = """
                               This card number appears to be for a different issue.
                               Using EMMs on the wrong card type may irreparably damage the card.
                               """;
        switch (selectedKey) {
            case "sky06" -> {
                String s6 = cardNumber.substring(0,2);
                // Carry out a basic card number check, ensure it starts with 06.
                if (!s6.equals("06")) {
                    Shared.messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    return true;
                }
            }
            case "sky07" -> {
                // Only digits 4-13 of a 13-digit card numbers are checked on 07.
                // We need to strip out the first four digits.
                int s7;
                switch(cardNumber.length()) {
                    case 13 -> {
                        if (!cardNumber.substring(0,2).equals("07")) {
                            Shared.messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                        s7 = Integer.parseInt(cardNumber.substring(4,7));
                    }
                    case 9 -> s7 = Integer.parseInt(cardNumber.substring(0,3));
                    default -> {
                        Shared.messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                        return false;
                    }              
                }
                if (s7 > 30 && s7 < 800) {
                    Shared.messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    return true;
                }
            }
            case "sky09" -> {
                int s9 = Integer.parseInt(cardNumber.substring(0,3));
                if (cardNumber.length() != 9 || (s9 < 190 || s9 > 250)) {
                    Shared.messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    return true;
                }
            }
            case "skynz01" -> {
                int snz1 = Integer.parseInt(cardNumber.substring(0,2));
                // Carry out a basic card number check, ensure it starts with 01.
                if (cardNumber.length() != 11 || snz1 != 1) {
                    Shared.messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    return true;
                }
            }
            case "skynz02" -> {
                int snz2 = Integer.parseInt(cardNumber.substring(0,2));
                // Carry out a basic card number check, ensure it starts with 02.
                if (cardNumber.length() != 11 || snz2 != 2) {
                    Shared.messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    return true;
                }
            }
            default -> {
                return true;
            }
        }
    }
        
    public static List<String> getHtmlLinks(String htmlString) throws IOException {
        var result = new ArrayList<String>();
            try (var reader = new BufferedReader(new StringReader(htmlString))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String query = "<a href=\"";
                int href = line.indexOf(query);
                if (href != -1) {
                    int start = href + query.length();
                    int end = line.indexOf("\"", start);
                    if (end != -1) {
                        result.add(line.substring(start, end));
                    }
                }
            }
        }
        return result;
    }
    
    public static List<String> queryJson(String jsonString, String query) throws IOException, ParseException {
        // Uses FlatLaf's JSON parser
        var result = new ArrayList<String>();
        try (var reader = new StringReader(jsonString)) {
            Object json = Json.parse(reader);
            switch (json) {
                case Map<?, ?> map -> {
                    Object value = map.get(query);
                    if (value instanceof String id) {
                        result.add(id);
                    }
                }
                case List<?> list -> {
                    for (var item : list) {
                        if (item instanceof Map<?, ?> map) {
                            Object value = map.get(query);
                            if (value instanceof String id) {
                                result.add(id);
                            }
                        }
                    }
                }
                default -> {
                }
            }
        }
        return result;
    }
    
    public static List<String> queryXml(String xmlString, String level1, String level2) throws XMLStreamException, IOException {
        // Uses native Java XML handling
        var result = new ArrayList<String>();
        var factory = XMLInputFactory.newFactory();
        try (var bais = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))) {
            var reader = factory.createXMLStreamReader(bais);
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equals(level1)) {
                        result.add(reader.getAttributeValue(null, level2));
                    }
                }
            }
            reader.close();
        }
        return result;
    }
    
    public static boolean unzipHackTV() {
        // Used by the Windows NSIS installer to extract the downloaded
        // hacktv ZIP file.
        Path zip = Path.of(System.getProperty("user.dir"), "fsphil.zip");
        Path readmePath = Path.of(System.getProperty("user.dir"), "readme.txt");
        try {
            unzipFile(zip.toString(), System.getProperty("user.dir"));
            // Delete the readme file that was extracted from the zip
            if (Files.exists(readmePath)) deleteFSObject(readmePath);
            // Delete the downloaded zip
            deleteFSObject(zip);
            return true;
        } catch (IOException ioe) {
            System.err.println(ioe);
            return false;
        }
    }
    
}
