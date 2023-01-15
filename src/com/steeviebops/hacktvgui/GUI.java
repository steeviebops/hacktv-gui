/*
 * Copyright (C) 2023 Stephen McGarry
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

import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import java.text.DecimalFormat;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.math.BigDecimal;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Path;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JCheckBox;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.LineNumberReader;
import javax.swing.filechooser.FileFilter;
import java.awt.Cursor;
import java.awt.Desktop;
import java.util.prefs.Preferences;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.swing.SwingWorker;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import java.nio.file.InvalidPathException;
import javax.imageio.ImageIO;
import javax.swing.KeyStroke;

public class GUI extends javax.swing.JFrame {    
    // Application name
    private static final String APP_NAME = "hacktv-gui";
    
    // Boolean used for Microsoft Windows detection and handling
    private final boolean RunningOnWindows;
    
    // Look and feel arraylist
    private ArrayList<String> LaFAL;
    int defaultLaF;
    
    // String to set the directory where this application's JAR is located
    private final Path JarDir;
    
    // Strings to set the location and contents of the Modes.ini file
    private String ModesFilePath;
    private String ModesFile;
    private String ModesFileVersion;
    private String ModesFileLocation;
    
    // Declare variables for supported features
    private boolean NICAMSupported = false;
    private boolean A2Supported = false;
    private boolean ACPSupported = false;

    // Declare a variable to determine the selected fork
    private boolean CaptainJack;

    // Declare Teletext-related variables that are reused across multiple subs
    private String HTMLTempFile;
    private String HTMLFile;
    private File SelectedFile;
    private Path TempDir;
    private String TeletextPath;
    private boolean DownloadInProgress = false;
    private boolean DownloadCancelled = false;

    // Declare variables used for path resolution
    private String HackTVPath;
    private String HackTVDirectory;
    private final String DefaultHackTVPath;

    // Declare variable for the title bar display
    private String TitleBar;
    private boolean TitleBarChanged = false;

    // Array used for M3U files
    private ArrayList<String> PlaylistURLsAL;
    private String[] PlaylistNames;

    // Declare a variable for storing the default sample rate for the selected video mode
    // This allows us to revert back to the default if the sample rate is changed by filters or scrambling systems
    private String DefaultSampleRate;
    
    // Declare combobox arrays and ArrayLists
    // These are used to store secondary information (frequencies, parameters, etc)
    private long[] FrequencyArray;
    private String[] PALModeArray;
    private String[] NTSCModeArray;
    private String[] SECAMModeArray;
    private String[] OtherModeArray;
    private String[] MACModeArray;
    private String[] ChannelArray;
    private String[] WSSModeArray;
    private ArrayList<String> ScramblingTypeArray;
    private ArrayList<String> ScramblingKeyArray;
    private ArrayList<String> ScramblingKey2Array;
    private String[] LogoArray;
    private String[] TCArray;
    private final ArrayList<String> PlaylistAL = new ArrayList<>();
    private ArrayList<String> UhfAL;
    private ArrayList<String> VhfAL;

    // Checkbox array for the File > New option
    private javax.swing.JCheckBox[] CheckBoxes;
    
    // Preferences node
    private final Preferences Prefs = Preferences.userNodeForPackage(GUI.class);
    
    // Process ID, used to gracefully close hacktv via the Stop button
    private long hpid;
    
    // Boolean to determine if hacktv is running or not
    private boolean Running;
    
    // Boolean to determine if a config file is in the process of loading
    private boolean HTVLoadInProgress = false;
    
    // Video line count. At the moment, we just use this for enabling/disabling
    // the test card dropdown.
    private int Lines;  
    
    // Integer to save the previously selected item in the Mode combobox.
    // Used to revert back if a baseband mode is selected on an unsupported SDR.
    private int PreviousIndex = 0;
    
    // Start point in playlist
    private int StartPoint = -1;
    
    // Declare variables used for storing parameters
    private String Mode = new String();
    private long Frequency;
    private String ScramblingType1 = new String();
    private String ScramblingKey1 = new String();
    private String ScramblingType2 = new String();
    private String ScramblingKey2 = new String();
    
    // Class instances
    final Shared SharedInst = new Shared();
    final INIFile INI = new INIFile();
    
    // Main method
    public static void main(String args[]) {
        // Pre-initialisation macOS tasks
        // These need to be done before creating the GUI class instance.
        // We'll set the dock icon later because that need to be done after
        // the GUI class instance is created.
        if (System.getProperty("os.name").contains("Mac")) {
            // Put app name in the menu bar
            System.setProperty("apple.awt.application.name", APP_NAME);
            // Use the Mac menu bar
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // Set light/dark mode to current setting, seems to be broken
            // System.setProperty("apple.awt.application.appearance", "system");
        }
        try {
            // Create GUI class instance
            var g = new GUI(args);
            // If the emergency reset command is specified, remove all prefs.
            // This is a safety net, in case any bad preferences prevent us from running.
            // We handle this as early as possible to ensure it will work correctly.
            if ( (args.length > 0) && (args[0].toLowerCase().equals("reset")) ||
                    (args.length > 0) && (args[0].toLowerCase().equals("-reset")) ||
                    (args.length > 0) && (args[0].toLowerCase().equals("--reset")) ||
                    (args.length > 0) && (args[0].toLowerCase().equals("/reset")) ) {
                // Reset all preferences and exit
                g.resetPreferences();
            }
            // Set window icon. The process to do this is OS-specific.
            // For macOS, we need to use the ICON_IMAGE feature of the Taskbar
            // class, so we'll check if the current environment supports it.
            // Taskbar is supported on Windows, but ICON_IMAGE is not.
            // So we need to check for both to avoid issues on Windows.
            // Just checking for ICON_IMAGE will cause an exception on
            // platforms that don't support Taskbar at all (e.g. KDE).
            if ((Taskbar.isTaskbarSupported()) && 
                    (Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE))) {
                var t = Taskbar.getTaskbar();
                try {
                    // Use the largest icon we have
                    List<Image> ic = g.setIcons();
                    t.setIconImage(ic.get(ic.size() - 1));
                }
                catch (IOException | UnsupportedOperationException e) {
                    System.err.println("Icon load failed, using defaults.\n" + e);
                }                
            }
            else {
                // Set icon without using Taskbar class
                try {
                    g.setIconImages(g.setIcons());
                }
                catch (IOException | IllegalArgumentException e) {
                    System.err.println("Icon load failed, using defaults.\n" + e);
                }                
            }
            // Prevent window from being resized below the current size
            g.setMinimumSize(g.getSize());
            g.setVisible(true);
        }
	catch (HeadlessException e) {
            // Catch this error if we find we're running on a headless JRE or an
            // OS with no GUI support (e.g. WSL or Unix without X).
            System.err.println("A fatal error occurred while attempting to "
                    + "initialise the window, please see details below.\n" + 
                    e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Create new form
     * @param args
     */
    public GUI(String[] args) {
        // Add a shutdown hook to run exit tasks
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cleanupBeforeExit();
            }
        });
        // Set look and feel
        setLaF();
        // Initialise Swing components
        initComponents();
        // Set the JarDir variable so we know where we're located
        JarDir = Path.of(SharedInst.getCurrentDirectory());
        // Check operating system and set OS-specific options
        if (System.getProperty("os.name").contains("Windows")) {
            RunningOnWindows = true;
            DefaultHackTVPath = System.getProperty("user.dir") + File.separator + "hacktv.exe";
            // Does windows-kill.exe exist in the current directory?
            if ( !Files.exists(Path.of(JarDir + "/windows-kill.exe")) ) {
                // Disable the "Use windows-kill instead of PowerShell" option
                chkWindowsKill.setSelected(false);
                chkWindowsKill.setEnabled(false);
            }
            else {
                if (Prefs.getInt("windows-kill", 0) == 1) {
                    chkWindowsKill.setSelected(true);
                }
                else {
                    chkWindowsKill.setSelected(false);
                }
            }
        }
        else {
            RunningOnWindows = false;
            DefaultHackTVPath = "/usr/local/bin/hacktv";
            // Hide the "Use windows-kill instead of PowerShell" option
            chkWindowsKill.setVisible(false);
            // Hide the Download button on the GUI Settings tab
            btnDownloadHackTV.setVisible(false);
        }
        // Post-initialisation macOS tasks
        if (System.getProperty("os.name").contains("Mac")) {
            // Move About to the application menu
            // Remove it and Exit from Help and File, respectively
            menuAbout.setVisible(false);
            sepAboutSeparator.setVisible(false);
            menuExit.setVisible(false);
            sepExitSeparator.setVisible(false);
            var desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                desktop.setAboutHandler( e -> {
                    menuAbout.doClick();
                } );
            }
            if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
                desktop.setQuitHandler( (e, response) -> {
                    response.performQuit();
                } );
            }
        }
        populateCheckboxArray();
        loadPreferences();
        detectFork();
        selectModesFile();
        openModesFile();
        populateVideoModes();
        addWSSModes();
        addARCorrectionOptions();
        addOutputDevices();
        addCeefaxRegions();
        if (CaptainJack) {
            captainJack();
        }
        else {
            fsphil();
        }
        // Populate the look and feel combobox
        populateLaFList();
        // Set default values when form loads
        radLocalSource.doClick();
        selectDefaultMode();
        cmbM3USource.setVisible(false);
        txtGain.setText("0");
        updateMenu.setVisible(false);
        // End default value load
        checkMRUList();
        if (Prefs.getInt("noupdatecheck", 0) == 1) {
            chkNoUpdateCheck.setSelected(true);
        }
        else {
            checkForUpdates(true);
        }
        // If any command line parameters were specified, handle them
        if (args.length > 0) {
            // If the specified file has a .htv extension, open it
            if (args[0].endsWith(".htv")) {
                SelectedFile = new File(args[0]);
                checkSelectedFile(SelectedFile);
            }
            else if (args[0].endsWith(".m3u")) {
                txtSource.setText(args[0]);
                m3uHandler(args[0],0);
            }
            else {
                // Otherwise, assume it's a source file and populate the source
                // text box with it.
                txtSource.setText(args[0]);
            }
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        SourceButtonGroup = new javax.swing.ButtonGroup();
        VideoFormatButtonGroup = new javax.swing.ButtonGroup();
        BandButtonGroup = new javax.swing.ButtonGroup();
        MacStereoButtonGroup = new javax.swing.ButtonGroup();
        MacSRButtonGroup = new javax.swing.ButtonGroup();
        MacCompressionButtonGroup = new javax.swing.ButtonGroup();
        MacProtectionButtonGroup = new javax.swing.ButtonGroup();
        TemplateButtonGroup = new javax.swing.ButtonGroup();
        sourceFileChooser = new JFileChooser();
        sourceFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        teletextFileChooser = new JFileChooser();
        teletextFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        teletextFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("All teletext files (*.tti, *.t42)", "tti", "t42"));
        teletextFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Teletext files (*.tti)", "tti"));
        teletextFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Teletext containers (*.t42)", "t42"));
        teletextFileChooser.setAcceptAllFileFilterUsed(true);
        configFileChooser = new JFileChooser();
        configFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        configFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("hacktv configuration file (*.htv)", "htv"));
        configFileChooser.setAcceptAllFileFilterUsed(true);
        hacktvFileChooser = new JFileChooser();
        hacktvFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        hacktvFileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                // always accept directories
                if (file.isDirectory())
                return true;
                // but only files with specific name
                if (!System.getProperty("os.name").contains("Windows")) {
                    return file.getName().equals("hacktv");
                }
                else {
                    return file.getName().equals("hacktv.exe");
                }
            }
            @Override
            public String getDescription() {
                if (!System.getProperty("os.name").contains("Windows")) {
                    return "hacktv binaries (hacktv)";
                }
                else {
                    return "hacktv binaries (hacktv.exe)";
                }
            }
        });
        outputFileChooser = new JFileChooser();
        outputFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        containerPanel = new javax.swing.JPanel();
        consoleOutputPanel = new javax.swing.JPanel();
        consoleScrollPane = new javax.swing.JScrollPane();
        txtConsoleOutput = new javax.swing.JTextArea();
        tabPane = new javax.swing.JTabbedPane();
        sourceTab = new javax.swing.JPanel();
        SourcePanel = new javax.swing.JPanel();
        radLocalSource = new javax.swing.JRadioButton();
        radTest = new javax.swing.JRadioButton();
        chkRepeat = new javax.swing.JCheckBox();
        chkTimestamp = new javax.swing.JCheckBox();
        chkInterlace = new javax.swing.JCheckBox();
        chkPosition = new javax.swing.JCheckBox();
        chkLogo = new javax.swing.JCheckBox();
        chkSubtitles = new javax.swing.JCheckBox();
        txtPosition = new javax.swing.JTextField();
        cmbLogo = new javax.swing.JComboBox<>();
        txtSubtitleIndex = new javax.swing.JTextField();
        lblSubtitleIndex = new javax.swing.JLabel();
        chkARCorrection = new javax.swing.JCheckBox();
        cmbARCorrection = new javax.swing.JComboBox<>();
        cmbTest = new javax.swing.JComboBox<>();
        playlistScrollPane = new javax.swing.JScrollPane();
        lstPlaylist = new javax.swing.JList<>();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnPlaylistDown = new javax.swing.JButton();
        btnPlaylistUp = new javax.swing.JButton();
        btnPlaylistStart = new javax.swing.JButton();
        chkRandom = new javax.swing.JCheckBox();
        txtSource = new javax.swing.JTextField();
        cmbM3USource = new javax.swing.JComboBox<>();
        btnSourceBrowse = new javax.swing.JButton();
        outputTab = new javax.swing.JPanel();
        FrequencyPanel = new javax.swing.JPanel();
        lblOutputDevice = new javax.swing.JLabel();
        cmbOutputDevice = new javax.swing.JComboBox<>();
        rfPanel = new javax.swing.JPanel();
        radCustom = new javax.swing.JRadioButton();
        radVHF = new javax.swing.JRadioButton();
        radUHF = new javax.swing.JRadioButton();
        lblChannel = new javax.swing.JLabel();
        txtFrequency = new javax.swing.JTextField();
        lblFrequency = new javax.swing.JLabel();
        lblGain = new javax.swing.JLabel();
        txtGain = new javax.swing.JTextField();
        cmbChannel = new javax.swing.JComboBox<>();
        chkAmp = new javax.swing.JCheckBox();
        lblAntennaName = new javax.swing.JLabel();
        txtAntennaName = new javax.swing.JTextField();
        cmbFileType = new javax.swing.JComboBox<>();
        lblFileType = new javax.swing.JLabel();
        cmbRegion = new javax.swing.JComboBox<>();
        lblOutputDevice2 = new javax.swing.JLabel();
        txtOutputDevice = new javax.swing.JTextField();
        VideoFormatPanel = new javax.swing.JPanel();
        cmbMode = new javax.swing.JComboBox<>();
        radPAL = new javax.swing.JRadioButton();
        radNTSC = new javax.swing.JRadioButton();
        radSECAM = new javax.swing.JRadioButton();
        radBW = new javax.swing.JRadioButton();
        radMAC = new javax.swing.JRadioButton();
        lblSampleRate = new javax.swing.JLabel();
        txtSampleRate = new javax.swing.JTextField();
        chkAudio = new javax.swing.JCheckBox();
        chkNICAM = new javax.swing.JCheckBox();
        chkPixelRate = new javax.swing.JCheckBox();
        txtPixelRate = new javax.swing.JTextField();
        chkVideoFilter = new javax.swing.JCheckBox();
        chkColour = new javax.swing.JCheckBox();
        chkA2Stereo = new javax.swing.JCheckBox();
        chkFMDev = new javax.swing.JCheckBox();
        txtFMDev = new javax.swing.JTextField();
        chkInvertVideo = new javax.swing.JCheckBox();
        chkMacChId = new javax.swing.JCheckBox();
        txtMacChId = new javax.swing.JTextField();
        PlaybackTab = new javax.swing.JPanel();
        VBIPanel = new javax.swing.JPanel();
        chkVITS = new javax.swing.JCheckBox();
        chkACP = new javax.swing.JCheckBox();
        chkWSS = new javax.swing.JCheckBox();
        cmbWSS = new javax.swing.JComboBox<>();
        chkVITC = new javax.swing.JCheckBox();
        AdditionalOptionsPanel = new javax.swing.JPanel();
        chkGamma = new javax.swing.JCheckBox();
        txtGamma = new javax.swing.JTextField();
        chkOutputLevel = new javax.swing.JCheckBox();
        txtOutputLevel = new javax.swing.JTextField();
        chkVerbose = new javax.swing.JCheckBox();
        chkVolume = new javax.swing.JCheckBox();
        chkDownmix = new javax.swing.JCheckBox();
        txtVolume = new javax.swing.JTextField();
        macPanel = new javax.swing.JPanel();
        radMacStereo = new javax.swing.JRadioButton();
        radMac32k = new javax.swing.JRadioButton();
        radMacCompanded = new javax.swing.JRadioButton();
        radMacMono = new javax.swing.JRadioButton();
        radMac16k = new javax.swing.JRadioButton();
        radMacLinear = new javax.swing.JRadioButton();
        radMacL1 = new javax.swing.JRadioButton();
        radMacL2 = new javax.swing.JRadioButton();
        lblMacAudioMode = new javax.swing.JLabel();
        lblMacSampleRate = new javax.swing.JLabel();
        lblMacCompression = new javax.swing.JLabel();
        lblMacProtection = new javax.swing.JLabel();
        teletextTab = new javax.swing.JPanel();
        teletextPanel = new javax.swing.JPanel();
        chkTeletext = new javax.swing.JCheckBox();
        txtTeletextSource = new javax.swing.JTextField();
        btnTeletextBrowse = new javax.swing.JButton();
        chkTextSubtitles = new javax.swing.JCheckBox();
        lblTextSubtitleIndex = new javax.swing.JLabel();
        txtTextSubtitleIndex = new javax.swing.JTextField();
        downloadPanel = new javax.swing.JPanel();
        btnTeefax = new javax.swing.JButton();
        btnSpark = new javax.swing.JButton();
        lblDownload = new javax.swing.JLabel();
        lblTeefax = new javax.swing.JLabel();
        lblSpark = new javax.swing.JLabel();
        pbTeletext = new javax.swing.JProgressBar();
        btnNMSCeefax = new javax.swing.JButton();
        lblNMSCeefax = new javax.swing.JLabel();
        scramblingTab = new javax.swing.JPanel();
        scramblingPanel = new javax.swing.JPanel();
        cmbScramblingType = new javax.swing.JComboBox<>();
        cmbScramblingKey1 = new javax.swing.JComboBox<>();
        cmbScramblingKey2 = new javax.swing.JComboBox<>();
        lblScramblingSystem = new javax.swing.JLabel();
        lblScramblingKey = new javax.swing.JLabel();
        lblVC2ScramblingKey = new javax.swing.JLabel();
        emmPanel = new javax.swing.JPanel();
        chkActivateCard = new javax.swing.JCheckBox();
        chkDeactivateCard = new javax.swing.JCheckBox();
        lblEMMCardNumber = new javax.swing.JLabel();
        txtCardNumber = new javax.swing.JTextField();
        chkShowCardSerial = new javax.swing.JCheckBox();
        chkFindKeys = new javax.swing.JCheckBox();
        scramblingOptionsPanel = new javax.swing.JPanel();
        chkScrambleAudio = new javax.swing.JCheckBox();
        cmbSysterPermTable = new javax.swing.JComboBox<>();
        lblSysterPermTable = new javax.swing.JLabel();
        lblECMaturity = new javax.swing.JLabel();
        cmbECMaturity = new javax.swing.JComboBox<>();
        chkECppv = new javax.swing.JCheckBox();
        lblECprognum = new javax.swing.JLabel();
        lblECprogcost = new javax.swing.JLabel();
        txtECprognum = new javax.swing.JTextField();
        txtECprogcost = new javax.swing.JTextField();
        chkNoDate = new javax.swing.JCheckBox();
        chkShowECM = new javax.swing.JCheckBox();
        settingsTab = new javax.swing.JPanel();
        pathPanel = new javax.swing.JPanel();
        txtHackTVPath = new javax.swing.JTextField();
        btnHackTVPath = new javax.swing.JButton();
        lblFork = new javax.swing.JLabel();
        lblSpecifyLocation = new javax.swing.JLabel();
        lblDetectedBuikd = new javax.swing.JLabel();
        btnDownloadHackTV = new javax.swing.JButton();
        resetSettingsPanel = new javax.swing.JPanel();
        btnResetAllSettings = new javax.swing.JButton();
        btnClearMRUList = new javax.swing.JButton();
        lblClearMRU = new javax.swing.JLabel();
        lblClearAll = new javax.swing.JLabel();
        generalSettingsPanel = new javax.swing.JPanel();
        chkSyntaxOnly = new javax.swing.JCheckBox();
        chkLocalModes = new javax.swing.JCheckBox();
        lblLookAndFeel = new javax.swing.JLabel();
        cmbLookAndFeel = new javax.swing.JComboBox<>();
        cmbNMSCeefaxRegion = new javax.swing.JComboBox<>();
        lblNMSCeefaxRegion = new javax.swing.JLabel();
        chkWindowsKill = new javax.swing.JCheckBox();
        chkNoUpdateCheck = new javax.swing.JCheckBox();
        txtStatus = new javax.swing.JTextField();
        RunButtonGrid = new javax.swing.JPanel();
        btnRun = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        menuNew = new javax.swing.JMenuItem();
        menuOpen = new javax.swing.JMenuItem();
        menuSave = new javax.swing.JMenuItem();
        menuSaveAs = new javax.swing.JMenuItem();
        sepMruSeparator = new javax.swing.JPopupMenu.Separator();
        menuMRUFile1 = new javax.swing.JMenuItem();
        menuMRUFile2 = new javax.swing.JMenuItem();
        menuMRUFile3 = new javax.swing.JMenuItem();
        menuMRUFile4 = new javax.swing.JMenuItem();
        sepExitSeparator = new javax.swing.JPopupMenu.Separator();
        menuExit = new javax.swing.JMenuItem();
        templatesMenu = new javax.swing.JMenu();
        menuFreqSelect = new javax.swing.JMenu();
        menuIF = new javax.swing.JRadioButtonMenuItem();
        menuKuBand = new javax.swing.JRadioButtonMenuItem();
        menuKaBand = new javax.swing.JRadioButtonMenuItem();
        menuAstra975Template = new javax.swing.JMenuItem();
        menuAstra10Template = new javax.swing.JMenuItem();
        menuBSBTemplate = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        menuWiki = new javax.swing.JMenuItem();
        menuGithubRepo = new javax.swing.JMenuItem();
        menuUpdateCheck = new javax.swing.JMenuItem();
        sepAboutSeparator = new javax.swing.JPopupMenu.Separator();
        menuAbout = new javax.swing.JMenuItem();
        updateMenu = new javax.swing.JMenu();
        menuDownloadUpdate = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("GUI frontend for hacktv");
        setName("mainFrame"); // NOI18N

        sourceFileChooser.setMultiSelectionEnabled(true);

        teletextFileChooser.setDialogTitle("Select a teletext file or directory");
        teletextFileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);

        consoleOutputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Console output"));

        txtConsoleOutput.setEditable(false);
        txtConsoleOutput.setBackground(new java.awt.Color(0, 0, 0));
        txtConsoleOutput.setColumns(20);
        txtConsoleOutput.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        txtConsoleOutput.setForeground(new java.awt.Color(255, 255, 255));
        txtConsoleOutput.setLineWrap(true);
        txtConsoleOutput.setRows(5);
        txtConsoleOutput.setWrapStyleWord(true);
        txtConsoleOutput.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        txtConsoleOutput.addMouseListener(new ContextMenuListener());
        consoleScrollPane.setViewportView(txtConsoleOutput);

        javax.swing.GroupLayout consoleOutputPanelLayout = new javax.swing.GroupLayout(consoleOutputPanel);
        consoleOutputPanel.setLayout(consoleOutputPanelLayout);
        consoleOutputPanelLayout.setHorizontalGroup(
            consoleOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(consoleScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
        );
        consoleOutputPanelLayout.setVerticalGroup(
            consoleOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(consoleScrollPane)
        );

        SourcePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Source options"));

        SourceButtonGroup.add(radLocalSource);
        radLocalSource.setText("Local or internet source");
        radLocalSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radLocalSourceActionPerformed(evt);
            }
        });

        SourceButtonGroup.add(radTest);
        radTest.setText("Test card");
        radTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radTestActionPerformed(evt);
            }
        });

        chkRepeat.setText("Repeat indefinitely");

        chkTimestamp.setText("Overlay timestamp");

        chkInterlace.setText("Interlaced video");

        chkPosition.setText("Start position (minutes)");
        chkPosition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkPositionActionPerformed(evt);
            }
        });

        chkLogo.setText("Overlay logo");
        chkLogo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLogoActionPerformed(evt);
            }
        });

        chkSubtitles.setText("Subtitles");
        chkSubtitles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSubtitlesActionPerformed(evt);
            }
        });

        txtPosition.setEnabled(false);
        txtPosition.addMouseListener(new ContextMenuListener());
        txtPosition.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPositionKeyTyped(evt);
            }
        });

        cmbLogo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        cmbLogo.setEnabled(false);
        cmbLogo.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbLogoMouseWheelMoved(evt);
            }
        });

        txtSubtitleIndex.setEnabled(false);
        txtSubtitleIndex.addMouseListener(new ContextMenuListener());
        txtSubtitleIndex.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtSubtitleIndexKeyTyped(evt);
            }
        });

        lblSubtitleIndex.setText("Index (optional)");
        lblSubtitleIndex.setEnabled(false);

        chkARCorrection.setText("16:9 source on 4:3 display");
        chkARCorrection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkARCorrectionActionPerformed(evt);
            }
        });

        cmbARCorrection.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        cmbARCorrection.setEnabled(false);
        cmbARCorrection.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbARCorrectionMouseWheelMoved(evt);
            }
        });

        cmbTest.setEnabled(false);
        cmbTest.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbTestMouseWheelMoved(evt);
            }
        });

        lstPlaylist.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lstPlaylistKeyPressed(evt);
            }
        });
        lstPlaylist.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstPlaylistValueChanged(evt);
            }
        });
        playlistScrollPane.setViewportView(lstPlaylist);

        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnRemove.setText("Remove");
        btnRemove.setEnabled(false);
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        btnPlaylistDown.setText("˅");
        btnPlaylistDown.setEnabled(false);
        btnPlaylistDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlaylistDownActionPerformed(evt);
            }
        });

        btnPlaylistUp.setText("˄");
        btnPlaylistUp.setEnabled(false);
        btnPlaylistUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlaylistUpActionPerformed(evt);
            }
        });

        btnPlaylistStart.setText("Play first");
        btnPlaylistStart.setEnabled(false);
        btnPlaylistStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlaylistStartActionPerformed(evt);
            }
        });

        chkRandom.setText("Randomise playlist");
        chkRandom.setEnabled(false);

        txtSource.addMouseListener(new ContextMenuListener());

        cmbM3USource.setEnabled(false);
        cmbM3USource.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbM3USourceMouseWheelMoved(evt);
            }
        });

        btnSourceBrowse.setText("Browse...");
        btnSourceBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSourceBrowseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout SourcePanelLayout = new javax.swing.GroupLayout(SourcePanel);
        SourcePanel.setLayout(SourcePanelLayout);
        SourcePanelLayout.setHorizontalGroup(
            SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SourcePanelLayout.createSequentialGroup()
                        .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkRepeat)
                            .addComponent(chkTimestamp)
                            .addComponent(chkInterlace)
                            .addComponent(chkRandom))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                        .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SourcePanelLayout.createSequentialGroup()
                                .addComponent(chkSubtitles)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 113, Short.MAX_VALUE)
                                .addComponent(lblSubtitleIndex)
                                .addGap(18, 18, 18)
                                .addComponent(txtSubtitleIndex, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(SourcePanelLayout.createSequentialGroup()
                                .addComponent(chkPosition)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SourcePanelLayout.createSequentialGroup()
                                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chkARCorrection)
                                    .addComponent(chkLogo))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cmbLogo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbARCorrection, 0, 148, Short.MAX_VALUE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SourcePanelLayout.createSequentialGroup()
                        .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SourcePanelLayout.createSequentialGroup()
                                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(playlistScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                                    .addGroup(SourcePanelLayout.createSequentialGroup()
                                        .addComponent(radLocalSource)
                                        .addGap(39, 39, 39)
                                        .addComponent(radTest)
                                        .addGap(18, 18, 18)
                                        .addComponent(cmbTest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(SourcePanelLayout.createSequentialGroup()
                                .addComponent(txtSource, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbM3USource, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(6, 6, 6)))
                        .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnRemove, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnPlaylistUp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnPlaylistDown, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnAdd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnPlaylistStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSourceBrowse, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        SourcePanelLayout.setVerticalGroup(
            SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radLocalSource)
                    .addComponent(radTest)
                    .addComponent(cmbTest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbM3USource)
                    .addComponent(txtSource)
                    .addComponent(btnSourceBrowse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(SourcePanelLayout.createSequentialGroup()
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPlaylistStart, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPlaylistUp, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnPlaylistDown, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(playlistScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkSubtitles)
                    .addComponent(txtSubtitleIndex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSubtitleIndex)
                    .addComponent(chkRandom))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkPosition)
                    .addComponent(txtPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkRepeat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkLogo)
                    .addComponent(cmbLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkInterlace))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cmbARCorrection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(chkARCorrection))
                    .addComponent(chkTimestamp))
                .addContainerGap())
        );

        javax.swing.GroupLayout sourceTabLayout = new javax.swing.GroupLayout(sourceTab);
        sourceTab.setLayout(sourceTabLayout);
        sourceTabLayout.setHorizontalGroup(
            sourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        sourceTabLayout.setVerticalGroup(
            sourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SourcePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabPane.addTab("Source", sourceTab);

        FrequencyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Frequency and TX options"));

        lblOutputDevice.setText("Output device");

        cmbOutputDevice.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbOutputDeviceMouseWheelMoved(evt);
            }
        });
        cmbOutputDevice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbOutputDeviceActionPerformed(evt);
            }
        });

        rfPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("RF options"));

        BandButtonGroup.add(radCustom);
        radCustom.setText("Custom");
        radCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radCustomActionPerformed(evt);
            }
        });

        BandButtonGroup.add(radVHF);
        radVHF.setText("VHF");
        radVHF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radVHFActionPerformed(evt);
            }
        });

        BandButtonGroup.add(radUHF);
        radUHF.setText("UHF");
        radUHF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radUHFActionPerformed(evt);
            }
        });

        lblChannel.setText("Channel");

        txtFrequency.setEditable(false);
        txtFrequency.addMouseListener(new ContextMenuListener());
        txtFrequency.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtFrequencyKeyTyped(evt);
            }
        });

        lblFrequency.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFrequency.setText("Frequency (MHz)");

        lblGain.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblGain.setText("TX gain (dB)");

        txtGain.addMouseListener(new ContextMenuListener());
        txtGain.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtGainKeyTyped(evt);
            }
        });

        cmbChannel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbChannelMouseWheelMoved(evt);
            }
        });
        cmbChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbChannelActionPerformed(evt);
            }
        });

        chkAmp.setText("TX RF amplifier");
        chkAmp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAmpActionPerformed(evt);
            }
        });

        lblAntennaName.setText("Antenna name");

        txtAntennaName.addMouseListener(new ContextMenuListener());

        cmbFileType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "uint8", "int8", "uint16", "int16", "int32", "float" }));
        cmbFileType.setSelectedIndex(-1);
        cmbFileType.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbFileTypeMouseWheelMoved(evt);
            }
        });

        lblFileType.setText("File type");

        cmbRegion.setEnabled(false);
        cmbRegion.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbRegionMouseWheelMoved(evt);
            }
        });
        cmbRegion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbRegionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout rfPanelLayout = new javax.swing.GroupLayout(rfPanel);
        rfPanel.setLayout(rfPanelLayout);
        rfPanelLayout.setHorizontalGroup(
            rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rfPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radUHF)
                    .addComponent(lblFrequency)
                    .addComponent(lblGain)
                    .addComponent(lblChannel))
                .addGap(24, 24, 24)
                .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radVHF)
                    .addComponent(txtGain, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbChannel, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rfPanelLayout.createSequentialGroup()
                        .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAntennaName)
                            .addComponent(lblFileType))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtAntennaName)
                            .addGroup(rfPanelLayout.createSequentialGroup()
                                .addComponent(cmbFileType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 122, Short.MAX_VALUE))))
                    .addGroup(rfPanelLayout.createSequentialGroup()
                        .addComponent(chkAmp)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(rfPanelLayout.createSequentialGroup()
                        .addComponent(radCustom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        rfPanelLayout.setVerticalGroup(
            rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rfPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radCustom)
                    .addComponent(radUHF)
                    .addComponent(radVHF)
                    .addComponent(cmbRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblAntennaName)
                        .addComponent(txtAntennaName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblChannel)
                        .addComponent(cmbChannel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFileType)
                        .addComponent(cmbFileType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFrequency)
                        .addComponent(txtFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtGain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblGain)
                    .addComponent(chkAmp))
                .addContainerGap())
        );

        lblOutputDevice2.setText("Serial number (optional)");

        txtOutputDevice.addMouseListener(new ContextMenuListener());

        javax.swing.GroupLayout FrequencyPanelLayout = new javax.swing.GroupLayout(FrequencyPanel);
        FrequencyPanel.setLayout(FrequencyPanelLayout);
        FrequencyPanelLayout.setHorizontalGroup(
            FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FrequencyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblOutputDevice)
                .addGap(18, 18, 18)
                .addComponent(cmbOutputDevice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblOutputDevice2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtOutputDevice)
                .addContainerGap())
            .addComponent(rfPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        FrequencyPanelLayout.setVerticalGroup(
            FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FrequencyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOutputDevice)
                    .addComponent(cmbOutputDevice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOutputDevice2)
                    .addComponent(txtOutputDevice))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rfPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        VideoFormatPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Video mode options"));

        cmbMode.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbModeMouseWheelMoved(evt);
            }
        });
        cmbMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbModeActionPerformed(evt);
            }
        });

        VideoFormatButtonGroup.add(radPAL);
        radPAL.setText("PAL");
        radPAL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radPALActionPerformed(evt);
            }
        });

        VideoFormatButtonGroup.add(radNTSC);
        radNTSC.setText("NTSC");
        radNTSC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radNTSCActionPerformed(evt);
            }
        });

        VideoFormatButtonGroup.add(radSECAM);
        radSECAM.setText("SECAM");
        radSECAM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radSECAMActionPerformed(evt);
            }
        });

        VideoFormatButtonGroup.add(radBW);
        radBW.setText("Black and white");
        radBW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radBWActionPerformed(evt);
            }
        });

        VideoFormatButtonGroup.add(radMAC);
        radMAC.setText("MAC");
        radMAC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radMACActionPerformed(evt);
            }
        });

        lblSampleRate.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblSampleRate.setText("Sample rate (MHz)");

        txtSampleRate.setToolTipText("");
        txtSampleRate.addMouseListener(new ContextMenuListener());
        txtSampleRate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtSampleRateKeyTyped(evt);
            }
        });

        chkAudio.setText("Audio enabled");
        chkAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAudioActionPerformed(evt);
            }
        });

        chkNICAM.setText("NICAM stereo");
        chkNICAM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNICAMActionPerformed(evt);
            }
        });

        chkPixelRate.setText("Pixel rate (MHz)");
        chkPixelRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkPixelRateActionPerformed(evt);
            }
        });

        txtPixelRate.setEnabled(false);
        txtPixelRate.addMouseListener(new ContextMenuListener());
        txtPixelRate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPixelRateKeyTyped(evt);
            }
        });

        chkVideoFilter.setText("VSB-AM filter");
        chkVideoFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkVideoFilterActionPerformed(evt);
            }
        });

        chkColour.setText("Disable colour");

        chkA2Stereo.setText("A2 (Zweikanalton) stereo");
        chkA2Stereo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkA2StereoActionPerformed(evt);
            }
        });

        chkFMDev.setText("FM deviation (MHz)");
        chkFMDev.setEnabled(false);
        chkFMDev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkFMDevActionPerformed(evt);
            }
        });

        txtFMDev.setEnabled(false);
        txtFMDev.addMouseListener(new ContextMenuListener());
        txtFMDev.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtFMDevKeyTyped(evt);
            }
        });

        chkInvertVideo.setText("Invert video polarity");

        chkMacChId.setText("Override MAC channel ID");
        chkMacChId.setEnabled(false);
        chkMacChId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMacChIdActionPerformed(evt);
            }
        });

        txtMacChId.setEnabled(false);
        txtMacChId.addMouseListener(new ContextMenuListener());
        txtMacChId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtMacChIdKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout VideoFormatPanelLayout = new javax.swing.GroupLayout(VideoFormatPanel);
        VideoFormatPanel.setLayout(VideoFormatPanelLayout);
        VideoFormatPanelLayout.setHorizontalGroup(
            VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                        .addComponent(chkInvertVideo)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(cmbMode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                                .addComponent(radPAL)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addComponent(radNTSC)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addComponent(radSECAM)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addComponent(radBW)
                                .addGap(18, 18, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, VideoFormatPanelLayout.createSequentialGroup()
                                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chkAudio)
                                    .addComponent(chkNICAM)
                                    .addComponent(chkA2Stereo)
                                    .addComponent(chkColour))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 142, Short.MAX_VALUE)
                                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(chkPixelRate)
                                            .addComponent(lblSampleRate))
                                        .addGap(74, 74, 74))
                                    .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(chkVideoFilter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(chkFMDev)
                                            .addComponent(chkMacChId))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))))
                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtPixelRate, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtSampleRate, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(radMAC, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtFMDev, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtMacChId))))
                .addContainerGap())
        );
        VideoFormatPanelLayout.setVerticalGroup(
            VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radPAL)
                    .addComponent(radNTSC)
                    .addComponent(radSECAM)
                    .addComponent(radBW)
                    .addComponent(radMAC))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSampleRate)
                    .addComponent(chkAudio)
                    .addComponent(txtSampleRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPixelRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkPixelRate))
                        .addGap(2, 2, 2)
                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chkA2Stereo)
                            .addComponent(chkFMDev)
                            .addComponent(txtFMDev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(chkNICAM))
                .addGap(2, 2, 2)
                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkColour)
                    .addComponent(chkMacChId)
                    .addComponent(txtMacChId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkInvertVideo)
                    .addComponent(chkVideoFilter))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout outputTabLayout = new javax.swing.GroupLayout(outputTab);
        outputTab.setLayout(outputTabLayout);
        outputTabLayout.setHorizontalGroup(
            outputTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(outputTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(VideoFormatPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(FrequencyPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        outputTabLayout.setVerticalGroup(
            outputTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outputTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(VideoFormatPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FrequencyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabPane.addTab("Output", outputTab);

        VBIPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("VBI options"));

        chkVITS.setText("VITS test signal");

        chkACP.setText("Macrovision ACP");

        chkWSS.setText("Widescreen signalling (WSS) on line 23");
        chkWSS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkWSSActionPerformed(evt);
            }
        });

        cmbWSS.setEnabled(false);
        cmbWSS.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbWSSMouseWheelMoved(evt);
            }
        });

        chkVITC.setText("VITC (Vertical Interval Time Code)");

        javax.swing.GroupLayout VBIPanelLayout = new javax.swing.GroupLayout(VBIPanel);
        VBIPanel.setLayout(VBIPanelLayout);
        VBIPanelLayout.setHorizontalGroup(
            VBIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VBIPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(VBIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(VBIPanelLayout.createSequentialGroup()
                        .addComponent(chkWSS)
                        .addGap(18, 18, 18)
                        .addComponent(cmbWSS, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(chkACP)
                    .addComponent(chkVITS)
                    .addComponent(chkVITC))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        VBIPanelLayout.setVerticalGroup(
            VBIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VBIPanelLayout.createSequentialGroup()
                .addGroup(VBIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkWSS)
                    .addComponent(cmbWSS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(chkACP, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(chkVITS)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkVITC)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        AdditionalOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Additional options"));

        chkGamma.setText("Gamma correction");
        chkGamma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkGammaActionPerformed(evt);
            }
        });

        txtGamma.setEnabled(false);
        txtGamma.addMouseListener(new ContextMenuListener());
        txtGamma.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtGammaKeyTyped(evt);
            }
        });

        chkOutputLevel.setText("Output level");
        chkOutputLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkOutputLevelActionPerformed(evt);
            }
        });

        txtOutputLevel.addMouseListener(new ContextMenuListener());
        txtOutputLevel.setEnabled(false);
        txtOutputLevel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtOutputLevelKeyTyped(evt);
            }
        });

        chkVerbose.setText("Verbose output");

        chkVolume.setText("Adjust volume");
        chkVolume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkVolumeActionPerformed(evt);
            }
        });

        chkDownmix.setText("Downmix 5.1 audio to 2.0");

        txtVolume.setEnabled(false);
        txtVolume.addMouseListener(new ContextMenuListener());
        txtVolume.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtVolumeKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout AdditionalOptionsPanelLayout = new javax.swing.GroupLayout(AdditionalOptionsPanel);
        AdditionalOptionsPanel.setLayout(AdditionalOptionsPanelLayout);
        AdditionalOptionsPanelLayout.setHorizontalGroup(
            AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdditionalOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkGamma)
                    .addComponent(chkOutputLevel)
                    .addComponent(chkVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtOutputLevel, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                    .addComponent(txtGamma, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                    .addComponent(txtVolume))
                .addGap(18, 18, 18)
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkVerbose)
                    .addComponent(chkDownmix))
                .addContainerGap(120, Short.MAX_VALUE))
        );
        AdditionalOptionsPanelLayout.setVerticalGroup(
            AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdditionalOptionsPanelLayout.createSequentialGroup()
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkGamma)
                    .addComponent(txtGamma, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkDownmix))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkOutputLevel)
                    .addComponent(txtOutputLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkVerbose))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkVolume))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        macPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("MAC audio options"));

        MacStereoButtonGroup.add(radMacStereo);
        radMacStereo.setText("Stereo");

        MacSRButtonGroup.add(radMac32k);
        radMac32k.setText("32 kHz");

        MacCompressionButtonGroup.add(radMacCompanded);
        radMacCompanded.setText("Companded");

        MacStereoButtonGroup.add(radMacMono);
        radMacMono.setText("Mono");

        MacSRButtonGroup.add(radMac16k);
        radMac16k.setText("16 kHz");

        MacCompressionButtonGroup.add(radMacLinear);
        radMacLinear.setText("Linear");

        MacProtectionButtonGroup.add(radMacL1);
        radMacL1.setText("Level 1");

        MacProtectionButtonGroup.add(radMacL2);
        radMacL2.setText("Level 2");

        lblMacAudioMode.setText("Audio channels");

        lblMacSampleRate.setText("Audio sample rate");

        lblMacCompression.setText("Compression");

        lblMacProtection.setText("Protection level");

        javax.swing.GroupLayout macPanelLayout = new javax.swing.GroupLayout(macPanel);
        macPanel.setLayout(macPanelLayout);
        macPanelLayout.setHorizontalGroup(
            macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(macPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMacAudioMode)
                    .addComponent(lblMacSampleRate)
                    .addComponent(lblMacCompression)
                    .addComponent(lblMacProtection))
                .addGap(18, 18, 18)
                .addGroup(macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radMacCompanded)
                    .addComponent(radMacL1)
                    .addComponent(radMac32k)
                    .addComponent(radMacStereo))
                .addGap(18, 18, 18)
                .addGroup(macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radMacL2)
                    .addComponent(radMacLinear)
                    .addComponent(radMac16k)
                    .addComponent(radMacMono))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        macPanelLayout.setVerticalGroup(
            macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(macPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radMacStereo)
                    .addComponent(radMacMono)
                    .addComponent(lblMacAudioMode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radMac32k)
                    .addComponent(radMac16k)
                    .addComponent(lblMacSampleRate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radMacCompanded)
                    .addComponent(radMacLinear)
                    .addComponent(lblMacCompression))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radMacL1)
                    .addComponent(radMacL2)
                    .addComponent(lblMacProtection))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout PlaybackTabLayout = new javax.swing.GroupLayout(PlaybackTab);
        PlaybackTab.setLayout(PlaybackTabLayout);
        PlaybackTabLayout.setHorizontalGroup(
            PlaybackTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PlaybackTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PlaybackTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(macPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(VBIPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AdditionalOptionsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        PlaybackTabLayout.setVerticalGroup(
            PlaybackTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlaybackTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(VBIPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AdditionalOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(macPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(87, Short.MAX_VALUE))
        );

        tabPane.addTab("Playback", PlaybackTab);

        teletextPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Teletext options"));

        chkTeletext.setText("Enable teletext");
        chkTeletext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTeletextActionPerformed(evt);
            }
        });

        txtTeletextSource.setEnabled(false);
        txtTeletextSource.addMouseListener(new ContextMenuListener());

        btnTeletextBrowse.setText("Browse...");
        btnTeletextBrowse.setEnabled(false);
        btnTeletextBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTeletextBrowseActionPerformed(evt);
            }
        });

        chkTextSubtitles.setText("Subtitles (page 888)");
        chkTextSubtitles.setEnabled(false);
        chkTextSubtitles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTextSubtitlesActionPerformed(evt);
            }
        });

        lblTextSubtitleIndex.setText("Subtitle index (optional)");
        lblTextSubtitleIndex.setEnabled(false);

        txtTextSubtitleIndex.setEnabled(false);
        txtTextSubtitleIndex.addMouseListener(new ContextMenuListener());
        txtTextSubtitleIndex.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtTextSubtitleIndexKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout teletextPanelLayout = new javax.swing.GroupLayout(teletextPanel);
        teletextPanel.setLayout(teletextPanelLayout);
        teletextPanelLayout.setHorizontalGroup(
            teletextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(teletextPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(teletextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(teletextPanelLayout.createSequentialGroup()
                        .addComponent(chkTeletext)
                        .addGap(286, 420, Short.MAX_VALUE))
                    .addGroup(teletextPanelLayout.createSequentialGroup()
                        .addGroup(teletextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(teletextPanelLayout.createSequentialGroup()
                                .addComponent(txtTeletextSource, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnTeletextBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(teletextPanelLayout.createSequentialGroup()
                                .addComponent(chkTextSubtitles)
                                .addGap(18, 18, 18)
                                .addComponent(lblTextSubtitleIndex)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtTextSubtitleIndex, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        teletextPanelLayout.setVerticalGroup(
            teletextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(teletextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkTeletext)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(teletextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnTeletextBrowse)
                    .addComponent(txtTeletextSource))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(teletextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkTextSubtitles)
                    .addComponent(lblTextSubtitleIndex)
                    .addComponent(txtTextSubtitleIndex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        downloadPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Download options"));
        downloadPanel.setEnabled(false);

        btnTeefax.setText("Teefax");
        btnTeefax.setEnabled(false);
        btnTeefax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTeefaxActionPerformed(evt);
            }
        });

        btnSpark.setText("SPARK");
        btnSpark.setEnabled(false);
        btnSpark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSparkActionPerformed(evt);
            }
        });

        lblDownload.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDownload.setText("Please choose a teletext service to download.");
        lblDownload.setEnabled(false);

        lblTeefax.setText("<html>Download Teefax.</html>");
        lblTeefax.setEnabled(false);

        lblSpark.setText("<html>Download SPARK from TVARK. https://www.tvark.org/</html>");
        lblSpark.setEnabled(false);

        btnNMSCeefax.setText("Ceefax");
        btnNMSCeefax.setEnabled(false);
        btnNMSCeefax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNMSCeefaxActionPerformed(evt);
            }
        });

        lblNMSCeefax.setText("<html>Download a Ceefax recreation from Nathan Media Services<br>https://www.nathanmediaservices.co.uk/</html>");
        lblNMSCeefax.setEnabled(false);

        javax.swing.GroupLayout downloadPanelLayout = new javax.swing.GroupLayout(downloadPanel);
        downloadPanel.setLayout(downloadPanelLayout);
        downloadPanelLayout.setHorizontalGroup(
            downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(downloadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pbTeletext, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDownload, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(downloadPanelLayout.createSequentialGroup()
                        .addGroup(downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnNMSCeefax, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                            .addComponent(btnTeefax, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                            .addComponent(btnSpark, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTeefax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSpark, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNMSCeefax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 90, Short.MAX_VALUE)))
                .addContainerGap())
        );
        downloadPanelLayout.setVerticalGroup(
            downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, downloadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDownload)
                .addGap(18, 18, 18)
                .addGroup(downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTeefax, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTeefax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSpark, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSpark, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnNMSCeefax, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(downloadPanelLayout.createSequentialGroup()
                        .addComponent(lblNMSCeefax, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                        .addGap(4, 4, 4)))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(pbTeletext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout teletextTabLayout = new javax.swing.GroupLayout(teletextTab);
        teletextTab.setLayout(teletextTabLayout);
        teletextTabLayout.setHorizontalGroup(
            teletextTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(teletextTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(teletextTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(teletextPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(downloadPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        teletextTabLayout.setVerticalGroup(
            teletextTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(teletextTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(teletextPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downloadPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabPane.addTab("Teletext", teletextTab);

        scramblingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Scrambling options"));

        cmbScramblingType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        cmbScramblingType.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbScramblingTypeMouseWheelMoved(evt);
            }
        });
        cmbScramblingType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbScramblingTypeActionPerformed(evt);
            }
        });

        cmbScramblingKey1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        cmbScramblingKey1.setEnabled(false);
        cmbScramblingKey1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbScramblingKey1MouseWheelMoved(evt);
            }
        });
        cmbScramblingKey1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbScramblingKey1ActionPerformed(evt);
            }
        });

        cmbScramblingKey2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        cmbScramblingKey2.setEnabled(false);
        cmbScramblingKey2.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbScramblingKey2MouseWheelMoved(evt);
            }
        });
        cmbScramblingKey2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbScramblingKey2ActionPerformed(evt);
            }
        });

        lblScramblingSystem.setText("Scrambling system");

        lblScramblingKey.setText("Access type");

        lblVC2ScramblingKey.setText("VC2 access type");

        emmPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("VideoCrypt options"));

        chkActivateCard.setText("Activate card");
        chkActivateCard.setEnabled(false);
        chkActivateCard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkActivateCardActionPerformed(evt);
            }
        });

        chkDeactivateCard.setText("Deactivate card");
        chkDeactivateCard.setEnabled(false);
        chkDeactivateCard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDeactivateCardActionPerformed(evt);
            }
        });

        lblEMMCardNumber.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblEMMCardNumber.setText("Card number");
        lblEMMCardNumber.setEnabled(false);

        txtCardNumber.setEnabled(false);
        txtCardNumber.addMouseListener(new ContextMenuListener());
        txtCardNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCardNumberKeyTyped(evt);
            }
        });

        chkShowCardSerial.setText("Show card serial");
        chkShowCardSerial.setEnabled(false);

        chkFindKeys.setText("Find keys on PPV card");
        chkFindKeys.setEnabled(false);

        javax.swing.GroupLayout emmPanelLayout = new javax.swing.GroupLayout(emmPanel);
        emmPanel.setLayout(emmPanelLayout);
        emmPanelLayout.setHorizontalGroup(
            emmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(emmPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(emmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkFindKeys)
                    .addComponent(chkDeactivateCard)
                    .addComponent(chkActivateCard)
                    .addGroup(emmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lblEMMCardNumber, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtCardNumber, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(chkShowCardSerial))
                .addContainerGap(95, Short.MAX_VALUE))
        );
        emmPanelLayout.setVerticalGroup(
            emmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(emmPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkActivateCard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkDeactivateCard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblEMMCardNumber)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCardNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkShowCardSerial)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkFindKeys)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        scramblingOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Format-specific options"));
        scramblingOptionsPanel.setToolTipText("");

        chkScrambleAudio.setText("Scramble audio");
        chkScrambleAudio.setEnabled(false);

        cmbSysterPermTable.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default", "1", "2" }));
        cmbSysterPermTable.setEnabled(false);
        cmbSysterPermTable.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbSysterPermTableMouseWheelMoved(evt);
            }
        });

        lblSysterPermTable.setText("Syster permutation table");
        lblSysterPermTable.setEnabled(false);

        lblECMaturity.setText("EuroCrypt maturity rating");
        lblECMaturity.setEnabled(false);

        cmbECMaturity.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
        cmbECMaturity.setSelectedIndex(-1);
        cmbECMaturity.setEnabled(false);
        cmbECMaturity.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbECMaturityMouseWheelMoved(evt);
            }
        });

        chkECppv.setText("EuroCrypt pay-per-view mode");
        chkECppv.setEnabled(false);
        chkECppv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkECppvActionPerformed(evt);
            }
        });

        lblECprognum.setText("Programme number");
        lblECprognum.setEnabled(false);

        lblECprogcost.setText("Programme cost");
        lblECprogcost.setEnabled(false);

        txtECprognum.setEnabled(false);
        txtECprognum.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtECprognumKeyTyped(evt);
            }
        });

        txtECprogcost.setEnabled(false);
        txtECprogcost.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtECprogcostKeyTyped(evt);
            }
        });

        chkNoDate.setText("No date");
        chkNoDate.setEnabled(false);

        javax.swing.GroupLayout scramblingOptionsPanelLayout = new javax.swing.GroupLayout(scramblingOptionsPanel);
        scramblingOptionsPanel.setLayout(scramblingOptionsPanelLayout);
        scramblingOptionsPanelLayout.setHorizontalGroup(
            scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scramblingOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(scramblingOptionsPanelLayout.createSequentialGroup()
                        .addComponent(lblSysterPermTable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                        .addComponent(cmbSysterPermTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(scramblingOptionsPanelLayout.createSequentialGroup()
                        .addComponent(lblECMaturity)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbECMaturity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scramblingOptionsPanelLayout.createSequentialGroup()
                        .addGroup(scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblECprognum)
                            .addComponent(lblECprogcost))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtECprogcost)
                            .addComponent(txtECprognum, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(scramblingOptionsPanelLayout.createSequentialGroup()
                        .addGroup(scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkScrambleAudio)
                            .addComponent(chkNoDate)
                            .addComponent(chkECppv))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        scramblingOptionsPanelLayout.setVerticalGroup(
            scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scramblingOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkScrambleAudio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSysterPermTable)
                    .addComponent(cmbSysterPermTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblECMaturity)
                    .addComponent(cmbECMaturity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkECppv)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblECprognum)
                    .addComponent(txtECprognum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblECprogcost)
                    .addComponent(txtECprogcost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkNoDate)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        chkShowECM.setText("Show ECMs on console");
        chkShowECM.setEnabled(false);

        javax.swing.GroupLayout scramblingPanelLayout = new javax.swing.GroupLayout(scramblingPanel);
        scramblingPanel.setLayout(scramblingPanelLayout);
        scramblingPanelLayout.setHorizontalGroup(
            scramblingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scramblingPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(chkShowECM)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(scramblingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scramblingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(scramblingPanelLayout.createSequentialGroup()
                        .addComponent(scramblingOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(emmPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(scramblingPanelLayout.createSequentialGroup()
                        .addGroup(scramblingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblScramblingSystem)
                            .addComponent(lblScramblingKey)
                            .addComponent(lblVC2ScramblingKey))
                        .addGap(18, 18, 18)
                        .addGroup(scramblingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cmbScramblingKey1, 0, 405, Short.MAX_VALUE)
                            .addComponent(cmbScramblingType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbScramblingKey2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        scramblingPanelLayout.setVerticalGroup(
            scramblingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scramblingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scramblingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbScramblingType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblScramblingSystem))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(scramblingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbScramblingKey1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblScramblingKey))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(scramblingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbScramblingKey2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVC2ScramblingKey))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkShowECM)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(scramblingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(emmPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scramblingOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout scramblingTabLayout = new javax.swing.GroupLayout(scramblingTab);
        scramblingTab.setLayout(scramblingTabLayout);
        scramblingTabLayout.setHorizontalGroup(
            scramblingTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scramblingTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scramblingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        scramblingTabLayout.setVerticalGroup(
            scramblingTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scramblingTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scramblingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(86, Short.MAX_VALUE))
        );

        tabPane.addTab("Scrambling", scramblingTab);

        pathPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Path to hacktv"));

        txtHackTVPath.setEditable(false);
        txtHackTVPath.addMouseListener(new ContextMenuListener());

        btnHackTVPath.setText("Browse...");
        btnHackTVPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHackTVPathActionPerformed(evt);
            }
        });

        lblFork.setText("fork");

        lblSpecifyLocation.setText("Specify the location of hacktv here.");

        lblDetectedBuikd.setText("Detected build:");

        btnDownloadHackTV.setText("Download...");
        btnDownloadHackTV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownloadHackTVActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pathPanelLayout = new javax.swing.GroupLayout(pathPanel);
        pathPanel.setLayout(pathPanelLayout);
        pathPanelLayout.setHorizontalGroup(
            pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pathPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pathPanelLayout.createSequentialGroup()
                        .addComponent(lblSpecifyLocation)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pathPanelLayout.createSequentialGroup()
                        .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pathPanelLayout.createSequentialGroup()
                                .addComponent(lblDetectedBuikd)
                                .addGap(18, 18, 18)
                                .addComponent(lblFork)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(txtHackTVPath, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnDownloadHackTV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnHackTVPath, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pathPanelLayout.setVerticalGroup(
            pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pathPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSpecifyLocation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnHackTVPath, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtHackTVPath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDetectedBuikd)
                    .addComponent(lblFork)
                    .addComponent(btnDownloadHackTV))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        resetSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Reset settings"));

        btnResetAllSettings.setText("Reset all settings...");
        btnResetAllSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetAllSettingsActionPerformed(evt);
            }
        });

        btnClearMRUList.setText("Clear MRU list...");
        btnClearMRUList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearMRUListActionPerformed(evt);
            }
        });

        lblClearMRU.setText("Clears the list of recently opened files.");

        lblClearAll.setText("Clears all settings.");
        lblClearAll.setToolTipText("");

        javax.swing.GroupLayout resetSettingsPanelLayout = new javax.swing.GroupLayout(resetSettingsPanel);
        resetSettingsPanel.setLayout(resetSettingsPanelLayout);
        resetSettingsPanelLayout.setHorizontalGroup(
            resetSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resetSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resetSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnClearMRUList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnResetAllSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(resetSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblClearAll)
                    .addComponent(lblClearMRU))
                .addContainerGap(191, Short.MAX_VALUE))
        );
        resetSettingsPanelLayout.setVerticalGroup(
            resetSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resetSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resetSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClearMRUList)
                    .addComponent(lblClearMRU))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(resetSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnResetAllSettings)
                    .addComponent(lblClearAll))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        generalSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General settings"));

        chkSyntaxOnly.setText("Generate syntax only");
        chkSyntaxOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSyntaxOnlyActionPerformed(evt);
            }
        });

        chkLocalModes.setText("Always use local copy of Modes.ini (restart required)");
        chkLocalModes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLocalModesActionPerformed(evt);
            }
        });

        lblLookAndFeel.setText("Theme");

        cmbLookAndFeel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbLookAndFeelMouseWheelMoved(evt);
            }
        });
        cmbLookAndFeel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbLookAndFeelActionPerformed(evt);
            }
        });

        cmbNMSCeefaxRegion.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbNMSCeefaxRegionMouseWheelMoved(evt);
            }
        });
        cmbNMSCeefaxRegion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbNMSCeefaxRegionActionPerformed(evt);
            }
        });

        lblNMSCeefaxRegion.setText("Ceefax (NMS) region");

        chkWindowsKill.setText("Use windows-kill instead of PowerShell for stopping hacktv");
        chkWindowsKill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkWindowsKillActionPerformed(evt);
            }
        });

        chkNoUpdateCheck.setText("Do not check for updates on startup");
        chkNoUpdateCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNoUpdateCheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout generalSettingsPanelLayout = new javax.swing.GroupLayout(generalSettingsPanel);
        generalSettingsPanel.setLayout(generalSettingsPanelLayout);
        generalSettingsPanelLayout.setHorizontalGroup(
            generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkNoUpdateCheck)
                    .addComponent(chkWindowsKill)
                    .addComponent(chkSyntaxOnly)
                    .addComponent(chkLocalModes)
                    .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                        .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblLookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNMSCeefaxRegion))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbNMSCeefaxRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbLookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        generalSettingsPanelLayout.setVerticalGroup(
            generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                .addComponent(chkSyntaxOnly)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkLocalModes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkWindowsKill)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkNoUpdateCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbNMSCeefaxRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNMSCeefaxRegion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLookAndFeel)
                    .addComponent(cmbLookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout settingsTabLayout = new javax.swing.GroupLayout(settingsTab);
        settingsTab.setLayout(settingsTabLayout);
        settingsTabLayout.setHorizontalGroup(
            settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resetSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pathPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(generalSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        settingsTabLayout.setVerticalGroup(
            settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pathPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generalSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(43, Short.MAX_VALUE))
        );

        tabPane.addTab("GUI settings", settingsTab);

        txtStatus.setEditable(false);
        txtStatus.addMouseListener(new ContextMenuListener());

        RunButtonGrid.setLayout(new java.awt.GridBagLayout());

        btnRun.setText("Run hacktv");
        btnRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 113;
        gridBagConstraints.ipady = 33;
        RunButtonGrid.add(btnRun, gridBagConstraints);

        javax.swing.GroupLayout containerPanelLayout = new javax.swing.GroupLayout(containerPanel);
        containerPanel.setLayout(containerPanelLayout);
        containerPanelLayout.setHorizontalGroup(
            containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtStatus)
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tabPane)
                            .addComponent(RunButtonGrid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(consoleOutputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        containerPanelLayout.setVerticalGroup(
            containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(tabPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(RunButtonGrid, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(consoleOutputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        fileMenu.setText("File");

        menuNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menuNew.setText("New");
        menuNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuNewActionPerformed(evt);
            }
        });
        fileMenu.add(menuNew);

        menuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menuOpen.setText("Open...");
        menuOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpenActionPerformed(evt);
            }
        });
        fileMenu.add(menuOpen);

        menuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menuSave.setText("Save...");
        menuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveActionPerformed(evt);
            }
        });
        fileMenu.add(menuSave);

        menuSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        menuSaveAs.setText("Save as...");
        menuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveAsActionPerformed(evt);
            }
        });
        fileMenu.add(menuSaveAs);
        fileMenu.add(sepMruSeparator);

        menuMRUFile1.setText("MenuMRUFile1");
        menuMRUFile1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMRUFile1ActionPerformed(evt);
            }
        });
        fileMenu.add(menuMRUFile1);

        menuMRUFile2.setText("MenuMRUFile2");
        menuMRUFile2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMRUFile2ActionPerformed(evt);
            }
        });
        fileMenu.add(menuMRUFile2);

        menuMRUFile3.setText("MenuMRUFile3");
        menuMRUFile3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMRUFile3ActionPerformed(evt);
            }
        });
        fileMenu.add(menuMRUFile3);

        menuMRUFile4.setText("MenuMRUFile4");
        menuMRUFile4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMRUFile4ActionPerformed(evt);
            }
        });
        fileMenu.add(menuMRUFile4);
        fileMenu.add(sepExitSeparator);

        menuExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        menuExit.setText("Exit");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
            }
        });
        fileMenu.add(menuExit);

        menuBar.add(fileMenu);

        templatesMenu.setText("Templates");

        menuFreqSelect.setText("Frequency selection");

        TemplateButtonGroup.add(menuIF);
        menuIF.setSelected(true);
        menuIF.setText("Intermediate frequency (IF)");
        menuIF.setActionCommand("0");
        menuIF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuIFActionPerformed(evt);
            }
        });
        menuFreqSelect.add(menuIF);

        TemplateButtonGroup.add(menuKuBand);
        menuKuBand.setText("Second harmonic (Ku band)");
        menuKuBand.setActionCommand("1");
        menuKuBand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuKuBandActionPerformed(evt);
            }
        });
        menuFreqSelect.add(menuKuBand);

        TemplateButtonGroup.add(menuKaBand);
        menuKaBand.setText("Fourth harmonic (Ka band)");
        menuKaBand.setActionCommand("2");
        menuKaBand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuKaBandActionPerformed(evt);
            }
        });
        menuFreqSelect.add(menuKaBand);

        templatesMenu.add(menuFreqSelect);

        menuAstra975Template.setText("Astra analogue STB (9.75 GHz)...");
        menuAstra975Template.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAstra975TemplateActionPerformed(evt);
            }
        });
        templatesMenu.add(menuAstra975Template);

        menuAstra10Template.setText("Astra analogue STB (10 GHz)...");
        menuAstra10Template.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAstra10TemplateActionPerformed(evt);
            }
        });
        templatesMenu.add(menuAstra10Template);

        menuBSBTemplate.setText("BSB D-MAC STB...");
        menuBSBTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuBSBTemplateActionPerformed(evt);
            }
        });
        templatesMenu.add(menuBSBTemplate);

        menuBar.add(templatesMenu);

        helpMenu.setText("Help");

        menuWiki.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        menuWiki.setText("Wiki page");
        menuWiki.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuWikiActionPerformed(evt);
            }
        });
        helpMenu.add(menuWiki);

        menuGithubRepo.setText("GitHub repository");
        menuGithubRepo.setToolTipText("");
        menuGithubRepo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuGithubRepoActionPerformed(evt);
            }
        });
        helpMenu.add(menuGithubRepo);

        menuUpdateCheck.setText("Check for updates");
        menuUpdateCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuUpdateCheckActionPerformed(evt);
            }
        });
        helpMenu.add(menuUpdateCheck);
        helpMenu.add(sepAboutSeparator);

        menuAbout.setText("About");
        menuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAboutActionPerformed(evt);
            }
        });
        helpMenu.add(menuAbout);

        menuBar.add(helpMenu);

        updateMenu.setText("Update available");

        menuDownloadUpdate.setText("Download update");
        menuDownloadUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuDownloadUpdateActionPerformed(evt);
            }
        });
        updateMenu.add(menuDownloadUpdate);

        menuBar.add(updateMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private List<Image> setIcons() throws IOException {
        // Add form icons
        List<Image> icons = new ArrayList<>();
        icons.add(ImageIO.read(getClass().getResource("/com/steeviebops/resources/test.gif")));
        /* WIP
        icons.add(ImageIO.read(getClass().getResource("/com/steeviebops/resources/icon16.png")));
        icons.add(ImageIO.read(getClass().getResource("/com/steeviebops/resources/icon32.png")));
        icons.add(ImageIO.read(getClass().getResource("/com/steeviebops/resources/icon48.png")));
        */
        return icons;
    }
    
    private void populateCheckboxArray() {
    /*  This array is used by the File > New option to reset all checkboxes to
        default values. Be sure to add any new checkboxes to this list.
    */    
        CheckBoxes=new JCheckBox[] {
            chkRepeat,
            chkTimestamp,
            chkInterlace,
            chkPosition,
            chkLogo,
            chkSubtitles,
            chkARCorrection,
            chkAudio,
            chkNICAM,
            chkMacChId,
            chkFMDev,
            chkVideoFilter,
            chkAmp,
            chkVITS,
            chkACP,
            chkWSS,
            chkGamma,
            chkOutputLevel,
            chkVerbose,
            chkTeletext,
            chkActivateCard,
            chkDeactivateCard,
            chkShowCardSerial,
            chkScrambleAudio,
            chkFindKeys,
            chkShowECM,
            chkColour,
            chkVolume,
            chkDownmix,
            chkTextSubtitles,
            chkA2Stereo,
            chkPixelRate,
            chkRandom,
            chkNoDate,
            chkInvertVideo
        };
    }
    
    private void setLaF() {
        LaFAL = new ArrayList <> ();
        UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lookAndFeel : lookAndFeels) {
            // Get the implementation class for the look and feel
            LaFAL.add(lookAndFeel.getClassName());
            // Is this the system default?
            if (UIManager.getSystemLookAndFeelClassName().equals(LaFAL.get(LaFAL.size() - 1))) {
                defaultLaF = LaFAL.size() - 1;
            }
        }
        // Add FlatLaf
        // Not enabled by default, needs FlatLaf JAR dependency in classpath
        // https://search.maven.org/artifact/com.formdev/flatlaf/2.2/jar
        try {
            // Check to see if the FlatLaf class is available to us
            Class.forName("com.formdev.flatlaf.FlatLightLaf");
            // Add the L&Fs
            LaFAL.add("com.formdev.flatlaf.FlatLightLaf");
            LaFAL.add("com.formdev.flatlaf.FlatDarkLaf");
            LaFAL.add("com.formdev.flatlaf.FlatIntelliJLaf");
            LaFAL.add("com.formdev.flatlaf.FlatDarculaLaf");
            System.setProperty("flatlaf.menuBarEmbedded", "false");
            //System.setProperty("flatlaf.useWindowDecorations", "false");
        }
        catch (ClassNotFoundException e) {
            // No need to do anything, we just won't load FlatLaf stuff
        }
        UIManager.put("swing.boldMetal", false);
        // Safeguard if the LookAndFeel preference is out of bounds
        int v = Prefs.getInt("LookAndFeel", defaultLaF);
        if ((v >= (LaFAL.size())) || (v < 0)) {
            // Use default L&F and reset preference
            System.out.println("Specified look and feel not found, reverting to default.");
            changeLaF(defaultLaF);
            Prefs.putInt("LookAndFeel", defaultLaF);
        }
        else {
            // Use the value we got from preferences
            changeLaF(v);
        }
    }
    
    private void populateLaFList() {
        ArrayList<String> LAF = new ArrayList <> ();
        UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lookAndFeel : lookAndFeels) {
            // Get the name of the look and feel
            LAF.add(lookAndFeel.getName());
        }
        // Add FlatLaf if available
        if (LaFAL.contains("com.formdev.flatlaf.FlatLightLaf")) {
            LAF.add("FlatLaf (light)");
            LAF.add("FlatLaf (dark)");
            LAF.add("FlatLaf (IntelliJ-style)");
            LAF.add("FlatLaf (Darcula-style)");
        }
        String[] lf = new String[LAF.size()];
        for (int i = 0; i < LAF.size(); i++) {
            lf[i] = LAF.get(i);
        }
        cmbLookAndFeel.setModel(new DefaultComboBoxModel<>(lf));
        cmbLookAndFeel.setSelectedIndex(Prefs.getInt("LookAndFeel", defaultLaF));
    }
    
    private void changeLaF(int i) {
        String l = LaFAL.get(i);
        // Only change L&F if different to the current one
        if (!l.equals(UIManager.getLookAndFeel().getClass().getName())) {
            try {
                UIManager.setLookAndFeel(l);
                SwingUtilities.updateComponentTreeUI(this);
                this.pack();
                Prefs.putInt("LookAndFeel", i);
            }
            catch (ClassNotFoundException | IllegalAccessException | 
                    InstantiationException | UnsupportedLookAndFeelException ex) {
                System.err.println(ex);
            }            
        }
    }
    
    private void checkForUpdates(boolean Silent) {
        // Queries the Github API for the latest release
        SwingWorker<Integer, Void> updateWorker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                try {
                    // Get the current version's date code using getVersion and
                    // remove the dashes so we can use parseInt later.
                    String cv = getVersion().replaceAll("-", "");
                    String a = SharedInst.downloadToString("https://api.github.com/repos/steeviebops/hacktv-gui/releases/latest");
                    String q = "tag_name";
                    if (cv.isBlank()) return -1;
                    if ((a != null) && (a.contains(q))) {
                        String nv = a.substring(a.indexOf(q) + 11, a.indexOf(q) + 19);
                        int nvi = Integer.parseInt(nv);
                        int cvi = Integer.parseInt(cv.substring(cv.length() -8, cv.length()));
                        if (nvi > cvi) {
                            return 0;
                        }
                        else {
                            return 1;
                        }
                    }
                }
                catch (IOException ioe) {
                    // Probably a connection error
                    return 2;
                }
                catch (NumberFormatException nfe) {
                    // Unexpected data received, report
                    System.err.println(nfe);
                }
                return 999;
            }
            @Override
            protected void done() {
                Integer status;
                try {
                    status = get();
                }
                catch (InterruptedException | ExecutionException e) {
                    status = 998;
                }
                switch (status) {
                    case -1:
                        // No current version number found.
                        // This can happen if running directly from an IDE.
                        // Don't do anything in this case.
                        break;
                    case 0:
                        // Update available
                        if (Silent) {
                            updateMenu.setVisible(true);
                            return;
                        }
                        int q = JOptionPane.showConfirmDialog(null, "An update is available.\n"
                                + "Would you like to find out more?", APP_NAME, JOptionPane.YES_NO_OPTION);
                        if (q == JOptionPane.YES_OPTION) menuDownloadUpdate.doClick();
                        break;
                    case 1:
                        // No update available
                        if (Silent) return;
                        messageBox("No updates are available at this time.", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case 2:
                        // Connection error
                        if (Silent) return;
                        messageBox("An error occurred while attempting to contact the Github server\n"
                           + "Please check your internet connection and try again.", JOptionPane.ERROR_MESSAGE);
                        break;
                    default:
                        // Unknown error
                        if (Silent) return;
                        System.err.println("Error code: " + status);
                        messageBox("An unknown error occurred while attempting to contact the Github server.", JOptionPane.ERROR_MESSAGE);
                        break;
                }
            }
        };
        updateWorker.execute();
    }
    
    private String getVersion() {
        // Get application version by checking the timestamp on the class file
        String cp = System.getProperty("java.class.path");
        // If classpath contains multiple paths, remove all but the first
        if (!RunningOnWindows) {
            if (cp.contains(":")) {
                cp = cp.substring(0, cp.indexOf(":"));
            }
        }
        else if (cp.contains(";")) {
            cp = cp.substring(0, cp.indexOf(";"));
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String classFilePath = "/com/steeviebops/hacktvgui/GUI.class";
            Date date;
            if (Files.exists(Path.of(cp))) {
                date = SharedInst.getLastUpdatedTime(cp, classFilePath);
                if (date != null) {
                    return "\nCompilation date: " + sdf.format(date);
                }
                else {
                    return "";
                }
            }
            else {
                return "";
            }
        }
        catch (NumberFormatException | InvalidPathException e) {
              return "";
        }
    }
    
    private void mouseWheelComboBoxHandler(int evt, JComboBox jcb) {
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
    
    private void messageBox(String msg, int type) {
        // type can be any of the following (from -1 to 3)
        // PLAIN_MESSAGE, ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE
        // or QUESTION_MESSAGE
        JOptionPane.showMessageDialog(null, msg, APP_NAME, type);
    }

    private void createTempDirectory() {
        // Creates a temp directory for us to use.
        // This is deleted on exit so don't save anything useful here!
        if (TempDir == null) {
            try {
                TempDir = Files.createTempDirectory(APP_NAME);
            }
            catch (IOException ex) {
                System.err.println(ex);
                messageBox("An error occurred while creating the temp directory.", JOptionPane.ERROR_MESSAGE);
                resetTeletextButtons();
            }
        }        
    }
    
    private void selectModesFile() {
        if ((Prefs.getInt("UseLocalModesFile", 0)) == 1) {
            if (Files.exists(Path.of(JarDir + File.separator + "Modes.ini"))) {
                // Use the local file
                ModesFilePath = JarDir + File.separator + "Modes.ini";
            }
            else {
                // Use the embedded copy
                ModesFilePath = "/com/steeviebops/resources/" + getFork() + "/Modes.ini";
            }
        }
        else if ( (Files.exists(Path.of(JarDir + File.separator + "Modes.ini"))) &&
                (cmbOutputDevice.getItemCount() == 0) ) {
            int q = JOptionPane.showConfirmDialog(null, "A Modes.ini file was found in the current directory.\n"
                    + "Do you want to use this file?\n"
                    + "You can suppress this prompt on the GUI settings tab.", APP_NAME, JOptionPane.YES_NO_OPTION);
            if (q == JOptionPane.YES_OPTION) {
                // Use the local file
                ModesFilePath = JarDir + File.separator + "Modes.ini";
            }
            else {
                // Download from Github
                downloadModesFile();
            }
        }
        else {
            // Download from Github
            downloadModesFile();
        }
    }
    
    private void downloadModesFile() {
        // Download modes.ini directly to the ModesFile string
        String u = "https://raw.githubusercontent.com/steeviebops/hacktv-gui/main/src/com/steeviebops/resources/" + getFork() + "/Modes.ini";
        try {
            ModesFile = SharedInst.downloadToString(u);
        }
        catch (IOException ex) {
                System.err.println("Error downloading modes.ini... " + ex);
                messageBox("Unable to download the modes file from Github.\n"
                        + "Using embedded copy instead, which may not be up to date.", JOptionPane.ERROR_MESSAGE);
                // Use the embedded copy
                ModesFilePath = "/com/steeviebops/resources/" + getFork() + "/Modes.ini";
        }
    } 
    
    private void openModesFile() {
        if ( (ModesFilePath == null) && (ModesFile != null) ) {
            ModesFileLocation = "online";
        }
        else if (ModesFilePath.startsWith("/com/steeviebops/resources/")) {
            // Read the embedded modes.ini to the ModesFile string
            try {
                if (getClass().getResourceAsStream(ModesFilePath) != null) {
                    InputStream is = getClass().getResourceAsStream(ModesFilePath);
                    ModesFile = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    ModesFileLocation = "embedded";
                }
                else {
                    // getResourceAsStream returned null, throw an exception to avoid NPE
                    throw new FileSystemNotFoundException("Unable to open embedded resource " + ModesFilePath);
                }
            }
            catch (IOException | FileSystemNotFoundException ex) {
                // No modes file to load, we cannot continue
                messageBox("Critical error, unable to read the embedded modes file.\n"
                        + "The application will now exit.", JOptionPane.ERROR_MESSAGE);
                ModesFileLocation = null;
                System.err.println(ex);
                System.exit(1);
            }
        }
        else {
            // Read the modes.ini we specified previously
            File f = new File(ModesFilePath);
            try {
                ModesFile = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                ModesFileLocation = "external";
            }
            catch (IOException e) {
                // Load failed, retry with the embedded file
                messageBox("Unable to read the modes file.\n"
                        + "Retrying with the embedded copy, which may not be up to date.", JOptionPane.WARNING_MESSAGE);
                ModesFilePath = "/com/steeviebops/resources/" + getFork() + "Modes.ini";
                ModesFileLocation = "embedded";
                openModesFile();
                return;
            }
        }
        // Read modes.ini file version
        ModesFileVersion = INI.getStringFromINI(ModesFile, "Modes.ini", "FileVersion", "unknown", true);
    }
    
    private void populateVideoModes() {
        PALModeArray = addVideoModes("pal", 1);
        NTSCModeArray = addVideoModes("ntsc", 1);
        SECAMModeArray = addVideoModes("secam", 1);
        OtherModeArray = addVideoModes("other", 1);
        MACModeArray = addVideoModes("mac", 1);
        if (PALModeArray[0].isBlank()) radPAL.setEnabled(false);
        if (NTSCModeArray[0].isBlank()) radNTSC.setEnabled(false);
        if (SECAMModeArray[0].isBlank()) radSECAM.setEnabled(false);
        if (OtherModeArray[0].isBlank()) radBW.setEnabled(false);
        if (MACModeArray[0].isBlank()) radMAC.setEnabled(false);
    }
    
    private void selectDefaultMode() {
        if (radPAL.isEnabled()) { 
            radPAL.doClick();
        }
        else if (radNTSC.isEnabled()) {
            radNTSC.doClick();
        }
        else if (radSECAM.isEnabled()) {
            radSECAM.doClick();
        }
        else if (radBW.isEnabled()) {
            radBW.doClick();
        }
        else if (radMAC.isEnabled()) {
            radMAC.doClick();
        }
        else {
            messageBox("No video systems were found. The Modes.ini file may be invalid or corrupted.\n"
                    + "The application will now exit.", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        if (chkAudio.isEnabled()) {
            chkAudio.setSelected(true);
            if (NICAMSupported) {
                chkNICAM.setSelected(true);
            }
            else if (A2Supported) {
                chkA2Stereo.setSelected(true);
            }
        }
    }
    
    private String[] addVideoModes(String ColourStandard, int returnValue) {
        /*
         * ColourStandard specifies what to look for (pal, ntsc, secam, other or mac)
         * returnValue specifies what we return in the array
         * 0 = return the friendly name of the mode
         * 1 = return the mode parameter used at the command line
         */
        
        String m = INI.getStringFromINI(ModesFile, "videomodes", ColourStandard, "", false);
        String[] q;
        
        String regex = "(,\\s*)";
        
        // q contains the modes defined in modes.ini for the specified standard
        q = m.split(regex);
        
        if (returnValue == 1) {
            return q;
        }
        else {
            // Check if the specified modes are defined, if not, don't add them
            ArrayList <String> ml = new ArrayList <> ();
            for (String mode : q) {
                String a = INI.getStringFromINI(ModesFile, mode, "name", "", true);
                if (!a.isBlank()) {
                    // Add the friendly name of the mode
                    ml.add(a);
                }
                else {
                    // Add the mode itself
                    ml.add("Unnamed mode '" + mode + "'");
                }                
            }
            // Convert the ArrayList to an array to populate the combobox
            String[] b = new String[ml.size()];
            for(int i = 0; i < b.length; i++) {
                b[i] = ml.get(i);    
            }
            return b;
        }
    }

    private void loadPreferences(){
        // Check preferences node for the path to hacktv
        // If not found, use the default
        HackTVPath = Prefs.get("HackTVPath", DefaultHackTVPath);
        // Load the full path to a variable so we can use getParent on it and
        // get its parent directory path
        HackTVDirectory = new File(HackTVPath).getParent();
        txtHackTVPath.setText(HackTVPath);
        // Check status of UseLocalModesFile
        if (Prefs.getInt("UseLocalModesFile", 0) == 1) {
            chkLocalModes.setSelected(true);
        }
    }
    
    private void resetPreferences() {
        // Delete everything from the preference store and exit immediately.
        if ( Prefs.get("HackTVPath", null) != null ) Prefs.remove("HackTVPath");
        if ( Prefs.get("File1", null) != null ) Prefs.remove("File1");
        if ( Prefs.get("File2", null) != null ) Prefs.remove("File2");
        if ( Prefs.get("File3", null) != null ) Prefs.remove("File3");
        if ( Prefs.get("File4", null) != null ) Prefs.remove("File4");
        if ( Prefs.get("UseLocalModesFile", null) != null ) Prefs.remove("UseLocalModesFile");
        if ( Prefs.get("LookAndFeel", null) != null ) Prefs.remove("LookAndFeel");
        if ( Prefs.get("CeefaxRegion", null) != null ) Prefs.remove("CeefaxRegion");
        if ( Prefs.get("SuppressWarnings", null) != null ) Prefs.remove("SuppressWarnings");
        if ( Prefs.get("windows-kill", null) != null ) Prefs.remove("windows-kill");
        if ( Prefs.get("lastdir", null) != null ) Prefs.remove("lastdir");
        if ( Prefs.get("lastfdir", null) != null ) Prefs.remove("lastfdir");
        if ( Prefs.get("lasttxdir", null) != null ) Prefs.remove("lasttxdir");
        if ( Prefs.get("lasthtvdir", null) != null ) Prefs.remove("lasthtvdir");
        if ( Prefs.get("noupdatecheck", null) != null ) Prefs.remove("noupdatecheck");
        // Not used anymore but we should still remove it if present
        if ( Prefs.get("MissingKillWarningShown", null) != null ) Prefs.remove("MissingKillWarningShown");
        if ( Prefs.get("ytdl", null) != null ) Prefs.remove("ytdl");
        System.out.println("All preferences have been reset to defaults.");
        System.exit(0);
    }
    
    private void detectFork() {
        /*  Loads the hacktv binary into RAM and attempts to find a specified
         *  string to detect what build or fork it is.
         */
        
        // Check if the specified path does not exist or is a directory
        if (!Files.exists(Path.of(HackTVPath))) {
            lblFork.setText("Not found");
            CaptainJack = false;
            return;
        }
        else if (Files.isDirectory(Path.of(HackTVPath))) {
            lblFork.setText("Invalid path");
            CaptainJack = false;
            return;    
        }
        /*  Check the size of the specified file.
         *  If larger than 100MB, call the fsphil method and don't go any further.
         *  This is to avoid memory leaks or hangs by loading a bad file.
         */
        File f = new File(HackTVPath);
        if (f.length() > 104857600) {
            lblFork.setText("Invalid file (too large)");
            CaptainJack = false;
            return;
        }
        // Test the specified file by loading it into memory using a BufferedReader.
        // This is more memory-efficient than loading the entire file to a byte array.
        boolean b = false;
        try {
            String c;
            try (BufferedReader br1 = new BufferedReader(new FileReader(HackTVPath))) {
                while ((c = br1.readLine()) != null) {
                    if (c.contains("--enableemm")) {
                        b = true;
                        lblFork.setText("Captain Jack");
                        CaptainJack = true;
                    }
                    else if (c.contains("Both VC1 and VC2 cannot be used together")) {
                        b = true;
                        lblFork.setText("fsphil");
                        CaptainJack = false;
                    }
                    // If we found a match, stop processing
                    if (b) break;
                }
            }
            // Run garbage collection to save memory
            System.gc();
            }
        catch (IOException ex) {
            lblFork.setText("File access error");
            CaptainJack = false;
            return;
        }
        if (!b) {
            lblFork.setText("Invalid file (not hacktv?)");
            CaptainJack = false;
        }     
    }
    
    private String getFork() {
        if (CaptainJack) {
            return "CaptainJack";
        }
        else {
            return "fsphil";
        }
    }

    private void fsphil() {
        // Disable features unsupported in fsphil's build
        if (chkTimestamp.isSelected()) chkTimestamp.doClick();
        if (chkLogo.isSelected()) chkLogo.doClick();
        if (chkSubtitles.isSelected()) chkSubtitles.doClick();
        if (chkARCorrection.isSelected()) chkARCorrection.doClick();
        if (chkPosition.isSelected()) chkPosition.doClick();
        if (chkVolume.isSelected()) chkVolume.doClick();
        if (chkDownmix.isSelected()) chkDownmix.doClick();
        chkTimestamp.setEnabled(false);
        chkLogo.setEnabled(false);
        chkSubtitles.setEnabled(false);
        chkARCorrection.setEnabled(false);
        chkPosition.setEnabled(false);
        chkVolume.setEnabled(false);
        chkDownmix.setEnabled(false);
        if ( radPAL.isSelected() || radSECAM.isSelected() ) {
            add625ScramblingTypes();
        }
        else if ( radMAC.isSelected() ) {
            addMACScramblingTypes();
        }
        if (radTest.isSelected()){
            cmbTest.setEnabled(false);
            cmbTest.setSelectedIndex(-1);
        }
    }
    
    private void captainJack() {
        // Enable features supported in Captain Jack's build
        chkLogo.setEnabled(true);
        addLogoOptions();
        if ( !radTest.isSelected() ) {
            chkPosition.setEnabled(true);
            chkTimestamp.setEnabled(true);
            chkPosition.setEnabled(true);
            chkSubtitles.setEnabled(true);
            chkARCorrection.setEnabled(true);
            chkVolume.setEnabled(true);
            chkDownmix.setEnabled(true);
        }
        if ( radPAL.isSelected() || radSECAM.isSelected() ) {
            add625ScramblingTypes();
        }
        else if ( radMAC.isSelected() ) {
            addMACScramblingTypes();
        }
        if (radTest.isSelected()){
            cmbTest.setEnabled(true);
            cmbTest.setSelectedIndex(0);
        }
    }
    
    private void populatePlaylist() {
        // Convert PlaylistAL to an array so we can populate lstPlaylist with it
        String[] pl = new String[PlaylistAL.size()];
        for(int i = 0; i < pl.length; i++) {
            if ((StartPoint == i) && (StartPoint != -1)) {
                // Add an asterisk to the start of the string to designate it
                // as the start point of the playlist
                pl[i] = "* " + PlaylistAL.get(i);
            }
            else {
                pl[i] = PlaylistAL.get(i);
            }
        }
        // Populate lstPlaylist using the contents of pl[]
        lstPlaylist.setListData(pl);
        // Enable or disable random option
        if (PlaylistAL.size() > 1) {
            chkRandom.setEnabled(true);
        }
        else {
            if (chkRandom.isSelected()) chkRandom.doClick();
            chkRandom.setEnabled(false);
        }
    }
    
    private void checkMRUList() {
        // Get MRU values and display in the File menu
        String ConfigFile1 = Prefs.get("File1", "");
        String ConfigFile2 = Prefs.get("File2", "");
        String ConfigFile3 = Prefs.get("File3", "");
        String ConfigFile4 = Prefs.get("File4", "");
        if ( !ConfigFile1.isEmpty() ) {
            sepMruSeparator.setVisible(true);
            menuMRUFile1.setText(ConfigFile1);
            menuMRUFile1.setVisible(true);
            btnClearMRUList.setEnabled(true);
        }
        else {
            menuMRUFile1.setVisible(false);
        }
        if ( !ConfigFile2.isEmpty() ) {
            sepMruSeparator.setVisible(true);
            menuMRUFile2.setText(ConfigFile2);
            menuMRUFile2.setVisible(true);
            btnClearMRUList.setEnabled(true);
        }
        else {
            menuMRUFile2.setVisible(false);
        }
        if ( !ConfigFile3.isEmpty() ) {
            sepMruSeparator.setVisible(true);
            menuMRUFile3.setText(ConfigFile3);
            menuMRUFile3.setVisible(true);
            btnClearMRUList.setEnabled(true);
        }
        else {
            menuMRUFile3.setVisible(false);
        }
        if ( !ConfigFile4.isEmpty() ) {
            sepMruSeparator.setVisible(true);
            menuMRUFile4.setText(ConfigFile4);
            menuMRUFile4.setVisible(true);
            btnClearMRUList.setEnabled(true);
        }
        else {
            menuMRUFile4.setVisible(false);
        }
        if ( (ConfigFile1.isEmpty()) && (ConfigFile2.isEmpty()) && 
                (ConfigFile3.isEmpty()) && (ConfigFile4.isEmpty()) ){
            sepMruSeparator.setVisible(false);
            btnClearMRUList.setEnabled(false);
        }
    }    
        
    private void updateMRUList (String FilePath) {
        String ConfigFile1 = Prefs.get("File1", "");
        String ConfigFile2 = Prefs.get("File2", "");
        String ConfigFile3 = Prefs.get("File3", "");
        String ConfigFile4 = Prefs.get("File4", "");
        if (FilePath.equals(ConfigFile2)) {
            Prefs.put("File2", ConfigFile1);
            Prefs.put("File1", FilePath);
            checkMRUList();
        }
        else if (FilePath.equals(ConfigFile3)) {
            Prefs.put("File3", ConfigFile2);
            Prefs.put("File2", ConfigFile1);
            Prefs.put("File1", FilePath);   
            checkMRUList(); 
        }
        else if (FilePath.equals(ConfigFile4)) {
            Prefs.put("File4", ConfigFile3);
            Prefs.put("File3", ConfigFile2);
            Prefs.put("File2", ConfigFile1);
            Prefs.put("File1", FilePath);
            checkMRUList();
        }
        else if (FilePath.equals(ConfigFile1)) {
            // Do nothing
        }
        else {
            if (!ConfigFile3.isEmpty()) { Prefs.put("File4", ConfigFile3); }
            if (!ConfigFile2.isEmpty()) { Prefs.put("File3", ConfigFile2); }
            if (!ConfigFile1.isEmpty()) { Prefs.put("File2", ConfigFile1); }
            Prefs.put("File1", FilePath);
            checkMRUList();
        }
    }
    
    private void saveFilePrompt() {
        // Opens the save file dialogue
        int result = configFileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // Check if the saved file has a .htv extension or not.
            // If it does not, then append one.
            SelectedFile = new File (SharedInst.stripQuotes(configFileChooser.getSelectedFile().toString()));
            if (!SelectedFile.toString().toLowerCase().endsWith(".htv")) {
                SelectedFile = new File(SelectedFile + ".htv");
            }
            // Create file
            try {
                if (!SelectedFile.createNewFile()) {
                    /* File exists, prompt to overwrite.
                     * If yes, go to the save method. If no, then restart this
                     * method so the user can select another file. Java doesn't
                     * appear to support file overwrite prompts in its dialogues
                     * so this is a workaround/hack.
                    */
                    int q = JOptionPane.showConfirmDialog(null, SelectedFile.getName() + " already exists.\n"
                            + "Do you want to overwrite it?", APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (q == JOptionPane.YES_OPTION) {
                        saveConfigFile(SelectedFile);
                    }
                    else {
                        saveFilePrompt();
                    }
                }
                else {
                    saveConfigFile(SelectedFile);
                }
            } catch (IOException ex) {
                    messageBox("An error occurred while writing to this file. "
                            + "You may not have the correct permissions to write to this location.", JOptionPane.ERROR_MESSAGE);       
            }
        }    
    }
    
    private void checkSelectedFile(File SourceFile) {
        String f;
        try {
            // Check if the file is too large. We really don't need to read
            // anything larger than a few kilobytes but we'll set it to 1 MB.
            if (SourceFile.length() < 1048576)  {
                // Read the file into memory
                f = Files.readString(SourceFile.toPath(), StandardCharsets.UTF_8);
                // Remove a UTF-8 BOM if it exists
                f = f.replaceAll("\\A\uFEFF", "");
            }
            else {
                messageBox("Invalid configuration file.", JOptionPane.WARNING_MESSAGE);
                System.err.println("File too large (> 1MB)");
                return;
            }
            // Check the file to see if it's in the correct format.
            if ( (INI.splitINIfile(f, "hacktv")) != null ) {
                // This is OK, continue opening this file
                HTVLoadInProgress = true;
                if (openConfigFile(f)) {
                    // Display the opened filename in the title bar
                    // Back up the original title once
                    if (!TitleBarChanged) {
                        TitleBar = this.getTitle();
                        TitleBarChanged = true;
                    }
                    this.setTitle(TitleBar + " - " + SourceFile.getName());
                    // Remove the ellipsis after Save to follow standard UI guidelines
                    menuSave.setText("Save");
                    updateMRUList(SourceFile.toString());    
                }
                HTVLoadInProgress = false;
            }
            else {
                // No idea what we've read here, abort
                messageBox("Invalid configuration file.", JOptionPane.WARNING_MESSAGE);
                System.err.println("[hacktv] section not found");
            }
        } catch (MalformedInputException ex) {
                messageBox("Invalid configuration file.", JOptionPane.WARNING_MESSAGE);
                System.err.println("The specified file contains invalid data.");
        } catch (IOException iox) {
                // File is inaccessible, so stop
                messageBox("The specified file could not be opened.\n"
                        + "It may have been removed, or you may not have the correct permissions to access it.", JOptionPane.ERROR_MESSAGE); 
        }
    }
    
    private boolean openConfigFile(String fileContents) throws IOException {
        /**
         * HTV configuration file loader.
         * 
         * We read the file as a Windows INI format. The syntax for strings is as follows:
         * 
         * INI.getStringFromINI(source file or string, "section", "setting", "Default value", Preserve case?);
         * 
         * If the first parameter is a single line string, it's treated as a file path
         * Otherwise, it's treated as the contents of the file
         * 
         * If the setting is not specified in the file, use the default value specified
         * 
         * If preserve-case is set to true, the value is returned as-is
         * If false, it is converted to lower case so we can manage it more easily
         * At present, we enable case-sensitivity for file and channel names only
         */
        // Check that the fork value matches the one we're using
        String ImportedFork = INI.getStringFromINI(fileContents, "hacktv-gui3", "fork", "", false);
        String WrongFork = "This file was created with a different fork of " +
            "hacktv. We will attempt to process the file but some options " +
            "may not be available.";
        if ((!CaptainJack) && (ImportedFork.equals("captainjack"))) {
            messageBox(WrongFork, JOptionPane.WARNING_MESSAGE);
        }
        else if ((CaptainJack) && (!ImportedFork.equals("captainjack"))) {
            messageBox(WrongFork, JOptionPane.WARNING_MESSAGE);
        }
        // Reset all controls
        resetAllControls();
        /* Output device
           For this, we look for hackrf, soapysdr or fl2k. An empty value will be
           interpreted as hackrf. Anything other than these values is handled
           as an output file.
         */
        String ImportedOutputDevice = INI.getStringFromINI(fileContents, "hacktv", "output", "hackrf", false);
        if ((ImportedOutputDevice.isEmpty()) || (ImportedOutputDevice.toLowerCase().startsWith("hackrf"))) {
            cmbOutputDevice.setSelectedIndex(0);
            if (ImportedOutputDevice.contains(":")) {
                txtOutputDevice.setText(ImportedOutputDevice.split(":")[1]);
            }
        }
        else if (ImportedOutputDevice.toLowerCase().startsWith("soapysdr")) {
            cmbOutputDevice.setSelectedIndex(1);
            if (ImportedOutputDevice.contains(":")) {
                txtOutputDevice.setText(ImportedOutputDevice.split(":")[1]);
            }
        }
        else if (ImportedOutputDevice.toLowerCase().startsWith("fl2k")) {
            cmbOutputDevice.setSelectedIndex(2);
            if (ImportedOutputDevice.contains(":")) {
                txtOutputDevice.setText(ImportedOutputDevice.split(":")[1]);
            }
        }
        else {
            cmbOutputDevice.setSelectedIndex(3);
            txtOutputDevice.setText(ImportedOutputDevice);
        }
        // Video mode
        String ImportedVideoMode = INI.getStringFromINI(fileContents, "hacktv", "mode", "", false);
        boolean ModeFound = false;
        for (int i = 0; i < PALModeArray.length; i++) {
            // Check if the mode we imported is in the PAL mode array
            if (PALModeArray[i].equals(ImportedVideoMode)) {
                radPAL.doClick();
                cmbMode.setSelectedIndex(i);
                ModeFound = true;
                break;
            }
            // Check the 'alt' value to see if we find a match there
            else if (checkAltModeNames(PALModeArray[i], ImportedVideoMode)) {
                radPAL.doClick();
                cmbMode.setSelectedIndex(i);
                ModeFound = true;
                break;
            }
        }
        if (!ModeFound) {
            // Check the NTSC mode array, and so on...
            for (int i = 0; i < NTSCModeArray.length; i++) {
                if (NTSCModeArray[i].equals(ImportedVideoMode)) {
                    radNTSC.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
                else if (checkAltModeNames(NTSCModeArray[i], ImportedVideoMode)) {
                        radNTSC.doClick();
                        cmbMode.setSelectedIndex(i);
                        ModeFound = true;
                        break;
                }
            }      
        }
        if (!ModeFound) {
            for (int i = 0; i < SECAMModeArray.length; i++) {
                if (SECAMModeArray[i].equals(ImportedVideoMode)) {
                    radSECAM.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
                else if (checkAltModeNames(SECAMModeArray[i], ImportedVideoMode)) {
                    radSECAM.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
            }
        }
        if (!ModeFound) {
            for (int i = 0; i < OtherModeArray.length; i++) {
                if (OtherModeArray[i].equals(ImportedVideoMode)) {
                    radBW.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
                else if (checkAltModeNames(OtherModeArray[i], ImportedVideoMode)) {
                    radBW.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
            }
        }
        if (!ModeFound) {
            for (int i = 0; i < MACModeArray.length; i++) {
                if (MACModeArray[i].equals(ImportedVideoMode)) {
                    radMAC.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
                else if (checkAltModeNames(MACModeArray[i], ImportedVideoMode)) {
                    radMAC.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
            }
        }
        if (!ModeFound) {
           invalidConfigFileValue("video mode", ImportedVideoMode);
           resetAllControls();
           return false;
        }
        // Input source or test card
        String ImportedSource = INI.getStringFromINI(fileContents, "hacktv", "input", "", true);
        String M3USource = (INI.getStringFromINI(fileContents, "hacktv-gui3", "m3usource", "", true));
        Integer M3UIndex = (INI.getIntegerFromINI(fileContents, "hacktv-gui3", "m3uindex"));
        if (ImportedSource.toLowerCase().startsWith("test:")) {
            radTest.doClick();
            if (CaptainJack) {
                String ImportedTC = ImportedSource.replace("test:", "");
                boolean TCFound = false;
                if (!ImportedTC.isEmpty()) {
                    for (int i = 0; i <= TCArray.length - 1; i++) {
                        if ( (TCArray[i].toLowerCase()).equals(ImportedTC) ) {
                            cmbTest.setSelectedIndex(i);
                            TCFound = true;
                            break;
                        }
                    }
                    if (!TCFound) {
                        invalidConfigFileValue("test card", ImportedTC);
                    }
                }                
            }
        }
        else if (!M3USource.isEmpty()) {
            File M3UFile = new File(M3USource);
            // If the source is an M3U file...
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            // Spawn M3UHandler using the index value we got above.
            m3uHandler(M3UFile.getAbsolutePath(), M3UIndex);
            txtSource.setText(M3USource);
        }
        else if (INI.getBooleanFromINI(fileContents, "hacktv-gui3", "playlist")) {
            // Split the [playlist] section from the HTV file.
            // We then split the section into an array (minus the header) 
            // and use that to populate PlaylistAL.
            if (INI.splitINIfile(fileContents, "playlist") != null) {
                String[] pl = INI.splitINIfile(fileContents, "playlist").split("\\n");
                for (int i = 1; i < pl.length; i++) {
                    PlaylistAL.add(pl[i]);
                }
                if ((INI.getIntegerFromINI(fileContents, "hacktv-gui3", "playliststart")) != null) {
                    StartPoint = INI.getIntegerFromINI(fileContents, "hacktv-gui3", "playliststart") - 1;
                    // Don't accept values lower than one
                    if (StartPoint < 1) StartPoint = -1;
                }
                chkRandom.setSelected(INI.getBooleanFromINI(fileContents, "hacktv-gui3", "random"));
                populatePlaylist();
            }
        }
        else {
            if (!ImportedSource.endsWith(".m3u")) txtSource.setText(ImportedSource);
        }
        // Frequency or channel number
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            // Return a value of -250 if the value is null so we can handle it
            String NoFrequencyOrChannel = "No frequency or valid channel number was found in the configuration file. Load aborted.";
            String ImportedChannel = INI.getStringFromINI(fileContents, "hacktv-gui3", "channel", "", true);
            String ImportedBandPlan = INI.getStringFromINI(fileContents, "hacktv-gui3", "bandplan", "", false);
            Double ImportedFrequency;
            if (INI.getDoubleFromINI(fileContents, "hacktv", "frequency") != null) {
                ImportedFrequency = INI.getDoubleFromINI(fileContents, "hacktv", "frequency");
            }
            else {
                ImportedFrequency = Double.parseDouble("-250");
            }
            if ((ImportedChannel.isEmpty()) && (ImportedFrequency == -250)) {
                messageBox(NoFrequencyOrChannel, JOptionPane.WARNING_MESSAGE);
                resetAllControls();
                return false;
            }
            else if (ImportedChannel.isEmpty()) {
                radCustom.doClick();
                Double Freq = ImportedFrequency / 1000000;
                txtFrequency.setText(Double.toString(Freq).replace(".0",".00"));
            }
            else {
                // Try to find the channel name by trying UHF first
                boolean ChannelFound = false;
                radUHF.doClick();
                // Check the available bandplans for the one specified and set the region accordingly
                for (int ub = 0; ub < UhfAL.size(); ub++) {
                    if (UhfAL.get(ub).equals(ImportedBandPlan)) {
                        cmbRegion.setSelectedIndex(ub);
                    }
                }
                // Search for the specified channel
                for (int i = 0; i <= cmbChannel.getItemCount() - 1; i++) {
                    if ( (ChannelArray[i].toLowerCase()).equals(ImportedChannel.toLowerCase()) ) {
                        cmbChannel.setSelectedIndex(i);
                        ChannelFound = true;
                    }
                }                    
                // If not found, try VHF
                if (!ChannelFound) {
                    radVHF.doClick();
                    // Check the available bandplans for the one specified and set the region accordingly
                    for (int vb = 0; vb < VhfAL.size(); vb++) {
                        if (VhfAL.get(vb).equals(ImportedBandPlan)) {
                            cmbRegion.setSelectedIndex(vb);
                        }
                    }
                    // Search for the specified channel
                    for (int i = 0; i <= cmbChannel.getItemCount() - 1; i++) {
                        if ( (ChannelArray[i].toLowerCase()).equals(ImportedChannel.toLowerCase()) ) {
                            cmbChannel.setSelectedIndex(i);
                            ChannelFound = true;
                        }
                    }
                }
                // If still not found, generate an error and use the frequency instead of the channel
                if (!ChannelFound) {
                    if (ImportedFrequency != -250) {
                        radCustom.doClick();
                        Double Freq = ImportedFrequency / 1000000;
                        txtFrequency.setText(Double.toString(Freq).replace(".0",""));
                        invalidConfigFileValue("channel", ImportedChannel);
                    }
                    else {
                        // If not found, and the frequency is also blank, abort
                        messageBox(NoFrequencyOrChannel, JOptionPane.WARNING_MESSAGE);
                        resetAllControls();
                        return false;
                    }
                }  
            }
        }
        // Gain
        if (INI.getIntegerFromINI(fileContents, "hacktv", "gain") != null) {
            txtGain.setText(INI.getIntegerFromINI(fileContents, "hacktv", "gain").toString());
        }
        // If value is null and output device is hackrf or soapysdr, set gain to zero
        else if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            txtGain.setText("0");
        }
        // Amp
        if (cmbOutputDevice.getSelectedIndex() == 0) {
            if (INI.getBooleanFromINI(fileContents, "hacktv", "amp")) {
                chkAmp.doClick();
            }            
        }
        // FM deviation
        if ((chkFMDev.isEnabled()) && (INI.getDoubleFromINI(fileContents, "hacktv", "deviation") != null)) {
            Double ImportedDeviation = (INI.getDoubleFromINI(fileContents, "hacktv", "deviation") / 1000000);
            chkFMDev.doClick();
            txtFMDev.setText(ImportedDeviation.toString().replace(".0",""));
        }
        // Output level
        String ImportedLevel = INI.getStringFromINI(fileContents, "hacktv", "level", "", false);
        if (!ImportedLevel.isEmpty()) {
            chkOutputLevel.doClick();
            txtOutputLevel.setText(ImportedLevel);
        }
        // Gamma
        String ImportedGamma = INI.getStringFromINI(fileContents, "hacktv", "gamma", "", false);
        if (!ImportedGamma.isEmpty()) {
            chkGamma.doClick();
            txtGamma.setText(ImportedGamma);
        }
        // Repeat
        if (chkRepeat.isEnabled()) {
            if (INI.getBooleanFromINI(fileContents, "hacktv", "repeat")) {
                chkRepeat.doClick();
            }
        }
        // Position
        if (chkPosition.isEnabled()) {
            if (INI.getIntegerFromINI(fileContents, "hacktv", "position") != null) {
                chkPosition.doClick();
                txtPosition.setText(INI.getIntegerFromINI(fileContents, "hacktv", "position").toString());
            }
        }
        // Verbose mode
        if (INI.getBooleanFromINI(fileContents, "hacktv", "verbose")) {
            chkVerbose.doClick();
        }
        // Logo
        if (chkLogo.isEnabled()) {
            String ImportedLogo = INI.getStringFromINI(fileContents, "hacktv", "logo", "", true).toLowerCase();
            // Check first if the imported string is a .png file.
            // hacktv now contains its own internal resources so external files
            // are no longer supported.
            if (ImportedLogo.endsWith(".png")) {
                messageBox(
                     "hacktv no longer supports external logo files. Logo option disabled.", JOptionPane.WARNING_MESSAGE);
            }
            else if (!ImportedLogo.isBlank()) {
                boolean logoFound = false;
                for (int i = 0; i <= cmbLogo.getItemCount() - 1; i++) {
                    if ( (LogoArray[i].toLowerCase()).equals(ImportedLogo) ) {
                        chkLogo.doClick();
                        cmbLogo.setSelectedIndex(i);
                        logoFound = true;
                        break;
                    }
                }
                if (!logoFound) {
                    invalidConfigFileValue("logo", ImportedLogo);
                }
            }
        }
        // Timestamp
        if (chkTimestamp.isEnabled()) {
            if (INI.getBooleanFromINI(fileContents, "hacktv", "timestamp")) {
                chkTimestamp.doClick();
            }
        }
        // Interlace
        if (INI.getBooleanFromINI(fileContents, "hacktv", "interlace")) {
            chkTimestamp.doClick();
        }
        // Teletext
        String ImportedTeletext = INI.getStringFromINI(fileContents, "hacktv", "teletext", "", true);
        if (!ImportedTeletext.isEmpty()) {
            chkTeletext.doClick();
            if (ImportedTeletext.toLowerCase().startsWith("raw:")) {
                txtTeletextSource.setText(ImportedTeletext.substring(4));
            }
            else {
                txtTeletextSource.setText(ImportedTeletext);
            }
        }
        // WSS
        if ((INI.getIntegerFromINI(fileContents, "hacktv", "wss")) != null) {
            Integer ImportedWSS = (INI.getIntegerFromINI(fileContents, "hacktv", "wss"));
            // Only accept values between 1 and 5
            if ((ImportedWSS > 0) && (ImportedWSS <= 5)) {
                chkWSS.doClick();
                // Since we increased the value by one when saving, decrease by one when loading
                cmbWSS.setSelectedIndex(ImportedWSS - 1);
            }
        }
        /* Aspect ratio correction for 16:9 content on 4:3 displays
         * If the arcorrection value is not defined, leave the option unchecked
         * Otherwise, check the option and process it as normal
         */
        if (chkARCorrection.isEnabled()) {
            if ((INI.getIntegerFromINI(fileContents, "hacktv", "arcorrection")) != null) {
                Integer ImportedAR = (INI.getIntegerFromINI(fileContents, "hacktv", "arcorrection"));
                chkARCorrection.doClick();
                cmbARCorrection.setSelectedIndex(ImportedAR);
            }
        }
        // Scrambling system
        String ImportedScramblingSystem = INI.getStringFromINI(fileContents, "hacktv", "scramblingtype", "", false);
        String ImportedKey = INI.getStringFromINI(fileContents, "hacktv", "scramblingkey", "", false);
        String ImportedKey2 = INI.getStringFromINI(fileContents, "hacktv", "scramblingkey2", "", false);
        if ((radPAL.isSelected()) || radSECAM.isSelected()) {
            switch (ImportedScramblingSystem) {
                case "":
                    cmbScramblingType.setSelectedIndex(0);
                    break;
                case "videocrypt":
                    cmbScramblingType.setSelectedIndex(1);
                    break;
                case "videocrypt2":
                    cmbScramblingType.setSelectedIndex(2);
                    break;
                case "videocrypt1+2":
                    cmbScramblingType.setSelectedIndex(3);
                    break;
                case "videocrypts":
                    cmbScramblingType.setSelectedIndex(4);
                    break;
                case "syster":
                    cmbScramblingType.setSelectedIndex(5);
                    break;
                case "d11":
                    if (CaptainJack) {
                        cmbScramblingType.setSelectedIndex(6);
                        break;
                    }
                    // WARNING: NO BREAK, move on to default
                case "systercnr":
                    if (CaptainJack) {
                        cmbScramblingType.setSelectedIndex(7);
                        break;
                    }
                    // WARNING: NO BREAK, move on to default
                case "systerls+cnr":
                    if (CaptainJack) {
                        cmbScramblingType.setSelectedIndex(8);
                        break;
                    }
                    // WARNING: NO BREAK, move on to default
                default:
                    invalidConfigFileValue("scrambling system", ImportedScramblingSystem);
                    ImportedScramblingSystem = "";
                    break;
            }
        }
        else if (radMAC.isSelected()) {
            switch (ImportedScramblingSystem) {
                case "":
                    cmbScramblingType.setSelectedIndex(0);
                    break;
                case "single-cut":
                    cmbScramblingType.setSelectedIndex(1);
                    break;
                case "double-cut":
                    cmbScramblingType.setSelectedIndex(2);
                    break;
                default:
                    invalidConfigFileValue("scrambling system", ImportedScramblingSystem);
                    ImportedScramblingSystem = "";
                    break;
            }
        }
        // Scrambling key/viewing card type (including VC1 side of dual VC1/2 mode)
        if ( (!ImportedScramblingSystem.isEmpty()) ) {
            if (ImportedKey.isEmpty()) ImportedKey = ImportedKey.replace("", "blank");
            ImportedKey = ImportedKey.replace("eurocrypt ", "");
            int k = ScramblingKeyArray.indexOf(ImportedKey);
            if (k == -1) {
                if (ImportedKey.equals("blank")) ImportedKey = ImportedKey.replace("blank", "");
                if (!ImportedScramblingSystem.equals("videocrypt1+2")) {
                    invalidConfigFileValue("access type", ImportedKey);
                }
                else {
                    invalidConfigFileValue("VideoCrypt I scrambling key", ImportedKey);
                }
            }
            else {
                cmbScramblingKey1.setSelectedIndex(k);
            }
        }
        // VC2 side of dual VC1/2 mode
        if (cmbScramblingType.getSelectedIndex() == 3) {
            int k2 = ScramblingKey2Array.indexOf(ImportedKey2); 
            if (k2 == -1) {
                invalidConfigFileValue("VideoCrypt II scrambling key", ImportedKey2);
            }
            else {
                cmbScramblingKey2.setSelectedIndex(k2);
            }            
        }
        // EMM
        if ( (chkActivateCard.isEnabled()) && (chkDeactivateCard.isEnabled()) ) {
            if ((INI.getIntegerFromINI(fileContents, "hacktv", "emm")) != null) {
                Integer ImportedEMM = (INI.getIntegerFromINI(fileContents, "hacktv", "emm"));
                String ImportedCardNumber;
                String Imported13Prefix;
                if ( (ImportedEMM.equals(1)) || (ImportedEMM.equals(2)) ){
                    if (ImportedEMM.equals(1)) { chkActivateCard.doClick() ;}
                    if (ImportedEMM.equals(2)) { chkDeactivateCard.doClick() ;}
                    ImportedCardNumber = INI.getStringFromINI(fileContents, "hacktv", "cardnumber", "", false);
                    Imported13Prefix = INI.getStringFromINI(fileContents, "hacktv-gui3", "13digitprefix", "", false);
                    // The ImportedCardNumber value only contains 8 digits of the card number
                    // To find the check digit, we run the CalculateLuhnCheckDigit method and append the result
                    if (SharedInst.isNumeric(Imported13Prefix + ImportedCardNumber)) txtCardNumber.setText(Imported13Prefix + 
                            ImportedCardNumber + SharedInst.CalculateLuhnCheckDigit(Long.parseLong(ImportedCardNumber)));
                }
            }
        }
        // Show card serial
        if (INI.getBooleanFromINI(fileContents, "hacktv", "showserial")) {
            chkShowCardSerial.doClick();
        }
        // Brute force PPV key
        if (INI.getBooleanFromINI(fileContents, "hacktv", "findkey")) {
            chkFindKeys.doClick();
        }
        // Scramble audio
        if (INI.getBooleanFromINI(fileContents, "hacktv", "scramble-audio")) {
            chkScrambleAudio.doClick();
        }
        // Syster permutation table
        int ImportedPermutationTable;
        if (INI.getIntegerFromINI(fileContents, "hacktv", "permutationtable") != null) {
            ImportedPermutationTable = INI.getIntegerFromINI(fileContents, "hacktv", "permutationtable");
            if ( (CaptainJack) && (ScramblingType1.equals("--syster")) || (ScramblingType1.equals("--systercnr")) ) {
                if ( (ImportedPermutationTable >= 0 ) &&
                        (ImportedPermutationTable < cmbSysterPermTable.getItemCount()) ) 
                cmbSysterPermTable.setSelectedIndex(ImportedPermutationTable);
            }
        }    
        // EuroCrypt maturity rating
        int ImportedMaturityRating;
        if (INI.getIntegerFromINI(fileContents, "hacktv", "ec-mat-rating") != null) {
            ImportedMaturityRating = INI.getIntegerFromINI(fileContents, "hacktv", "ec-mat-rating");
            if ( (cmbECMaturity.isEnabled()) && (ImportedMaturityRating >= 0) && (ImportedMaturityRating <= 15) ) {
                cmbECMaturity.setSelectedIndex(ImportedMaturityRating);
            }
        }
        // EuroCrypt PPV
        int ImportedProgNumber;
        int ImportedProgCost;
        if ( (chkECppv.isEnabled()) && (INI.getBooleanFromINI(fileContents, "hacktv", "ec-ppv")) ) {
            chkECppv.doClick();
            if (INI.getIntegerFromINI(fileContents, "hacktv", "ec-ppv-num") != null) {
                ImportedProgNumber = INI.getIntegerFromINI(fileContents, "hacktv", "ec-ppv-num");
            }
            else {
                ImportedProgNumber = 0;
            }
            if (INI.getIntegerFromINI(fileContents, "hacktv", "ec-ppv-cost") != null) {
                ImportedProgCost = INI.getIntegerFromINI(fileContents, "hacktv", "ec-ppv-cost");
            }
            else {
                ImportedProgCost = 0;
            }
            txtECprognum.setText(String.valueOf(ImportedProgNumber));
            txtECprogcost.setText(String.valueOf(ImportedProgCost));
        }
        // EuroCrypt "No Date" setting
        if (INI.getBooleanFromINI(fileContents, "hacktv", "ec-nodate")) {
            chkNoDate.doClick();
        }
        // ACP
        if (INI.getBooleanFromINI(fileContents, "hacktv", "acp")) {
            chkACP.doClick();
        }
        // Filter
        if (INI.getBooleanFromINI(fileContents, "hacktv", "filter")) {
            chkVideoFilter.doClick();
        }
        // Audio
        if (INI.getBooleanFromINI(fileContents, "hacktv", "audio") == false) {
            if (chkAudio.isSelected() ) { chkAudio.doClick(); }
        }
        // NICAM
        if (INI.getBooleanFromINI(fileContents, "hacktv", "nicam") == false) {
            if (chkNICAM.isSelected() ) { chkNICAM.doClick(); }
        }
        // A2 Stereo
        if (INI.getBooleanFromINI(fileContents, "hacktv", "a2stereo") == true) {
            if ( (!chkA2Stereo.isSelected()) && (A2Supported) ) chkA2Stereo.doClick();
        }
        // ECM
        if (INI.getBooleanFromINI(fileContents, "hacktv", "showecm")) {
            chkShowECM.doClick();
        }
        // VITS
        if (INI.getBooleanFromINI(fileContents, "hacktv", "vits")) {
            chkVITS.doClick();
        }
        // Subtitles
        if (INI.getBooleanFromINI(fileContents, "hacktv", "subtitles")) {
            chkSubtitles.doClick();
            if ( (INI.getIntegerFromINI(fileContents, "hacktv", "subtitleindex")) != null ) {
                txtSubtitleIndex.setText(Integer.toString((INI.getIntegerFromINI(fileContents, "hacktv", "subtitleindex"))));
            }
        }
        // MAC channel ID
        String ImportedChID = INI.getStringFromINI(fileContents, "hacktv", "chid", "", true);
        if (!ImportedChID.isEmpty()) {
            if (!chkMacChId.isSelected()) chkMacChId.doClick();
            txtMacChId.setText(ImportedChID);
        }
        // MAC audio options
        if (radMAC.isSelected()) {
            if (INI.getStringFromINI(fileContents, "hacktv", "mac-audio-mode", "stereo", false).equals("mono")) {
                radMacMono.setSelected(true);
            }
            if (INI.getStringFromINI(fileContents, "hacktv", "mac-audio-quality", "high", false).equals("medium")) {
                radMac16k.setSelected(true);
            }
            if (INI.getStringFromINI(fileContents, "hacktv", "mac-audio-compression", "companded", false).equals("linear")) {
                radMacLinear.setSelected(true);
            }
            if (INI.getStringFromINI(fileContents, "hacktv", "mac-audio-protection", "l1", false).equals("l2")) {
                radMacL2.setSelected(true);
            }
        }
        // Disable colour
        if (chkColour.isEnabled()) {
            // Accept both UK and US English spelling
            if ( (INI.getBooleanFromINI(fileContents, "hacktv", "nocolour")) ||
                    (INI.getBooleanFromINI(fileContents, "hacktv", "nocolor")) ){
                chkColour.doClick();
            }
        }
        // Invert video polarity
        if (INI.getBooleanFromINI(fileContents, "hacktv", "invert-video") ){
            chkInvertVideo.doClick();
        }
        // SoapySDR antenna name
        if (cmbOutputDevice.getSelectedIndex() == 1) {
            txtAntennaName.setText(INI.getStringFromINI(fileContents, "hacktv", "antennaname", "", false));
        }
        // Output file type
        if (cmbOutputDevice.getSelectedIndex() == 3) {
            switch (INI.getStringFromINI(fileContents, "hacktv", "filetype", "", false)) {
                case "uint8":
                    cmbFileType.setSelectedIndex(0);
                    break;
                case "int8":
                    cmbFileType.setSelectedIndex(1);
                    break;
                case "uint16":
                    cmbFileType.setSelectedIndex(2);
                    break;
                case "int16":
                    cmbFileType.setSelectedIndex(3);
                    break;
                case "int32":
                    cmbFileType.setSelectedIndex(4);
                    break;
                case "float":
                    cmbFileType.setSelectedIndex(5);
                    break;
                default:
                    cmbFileType.setSelectedIndex(3);
                    break;
            }
        }
        // Volume
        String ImportedVolume = INI.getStringFromINI(fileContents, "hacktv", "volume", "", false);
        if (!ImportedVolume.isEmpty()) {
            chkVolume.doClick();
            txtVolume.setText(ImportedVolume);
        }
        // Downmix
        if (INI.getBooleanFromINI(fileContents, "hacktv", "downmix")) {
            chkDownmix.doClick();
        }
        // Teletext subtitles
        if ( (INI.getBooleanFromINI(fileContents, "hacktv", "tx-subtitles")) ){
            chkTextSubtitles.doClick();
            if ( (INI.getIntegerFromINI(fileContents, "hacktv", "tx-subindex")) != null ) {
                txtTextSubtitleIndex.setText(Integer.toString((INI.getIntegerFromINI(fileContents, "hacktv", "tx-subindex"))));
            }
        }
        else if ( (INI.getBooleanFromINI(fileContents, "hacktv", "teletextsubtitles")) ){
            chkTextSubtitles.doClick();
            if ( (INI.getIntegerFromINI(fileContents, "hacktv", "teletextsubindex")) != null ) {
                txtTextSubtitleIndex.setText(Integer.toString((INI.getIntegerFromINI(fileContents, "hacktv", "teletextsubindex"))));
            }
        }
        // Pixel rate
        Double ImportedPixelRate;
        if ((INI.getDoubleFromINI(fileContents, "hacktv", "pixelrate")) != null) {
            if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
            ImportedPixelRate = (INI.getDoubleFromINI(fileContents, "hacktv", "pixelrate") / 1000000);
            txtPixelRate.setText(ImportedPixelRate.toString().replace(".0","")); 
        }
        // Sample rate (default to 16 MHz if not specified)
        // Add this last so other changes don't interfere with the value in the
        // configuration file.
        Double ImportedSampleRate;
        if ((INI.getDoubleFromINI(fileContents, "hacktv", "samplerate")) != null) {
            ImportedSampleRate = (INI.getDoubleFromINI(fileContents, "hacktv", "samplerate") / 1000000);
        }
        else {
            ImportedSampleRate = Double.parseDouble("16");
            messageBox("No sample rate specified, defaulting to 16 MHz.", JOptionPane.INFORMATION_MESSAGE);
        }
        txtSampleRate.setText(ImportedSampleRate.toString().replace(".0",""));
        btnRun.requestFocusInWindow();
        // This must be the last line in this method, it confirms that 
        // everything ran as planned.
        return true;
    }
    
    private boolean checkAltModeNames(String modeToCheck, String alt) {
        /*
         * Modes.ini now supports an alt (meaning 'alternative') setting, which
         * can be used to report a second option that represents that mode.
         * This is used by B/G and D/K so both options are accepted.
         *
         * For example, checkAltModeNames("g", "b") will check if section 'g'
         * contains an alt value of 'b' and return true if it finds it.
         */
        return (INI.getStringFromINI(ModesFile, modeToCheck, "alt", "", false).equals(alt));
    }
    
    private void invalidConfigFileValue (String settingName, String value) {
        /*
        * This method is just used to generate an error when an invalid value is found in a config file
        * Saves us writing out the same error message multiple times
        * To use it, just feed two values or variables into it and they will be added to the message below 
        */
        // If an incorrect scrambling system/key was specified, disable scrambling
        if ( (settingName.contains("scrambling system")) ||
                (settingName.contains("scrambling key")) ||
                (settingName.contains("VideoCrypt I scrambling key"))||
                (settingName.contains("VideoCrypt II scrambling key")) ) {
            cmbScramblingType.setSelectedIndex(0);
        }
        messageBox("The " + settingName + '\u0020' + '\u0022' + value + '\u0022' + 
                " specified in the configuration file could not be found.\n" +
                "The file may have been created in a newer version or the value is invalid.",
                JOptionPane.WARNING_MESSAGE);
    }
    
    private void saveConfigFile (File DestinationFileName) {
        /**
         * HTV configuration file writer.
         * Saves the current state to a configuration file with a .htv extension.
         */
        String DestinationFile = DestinationFileName.toString();
        // Check the frequency to commit it to a variable before we start
        // If invalid, then abort
        if (!checkCustomFrequency()) return;
        // Create a new, empty string to populate the new file with.
        String FileContents = "";
        // Output device
        switch (cmbOutputDevice.getSelectedIndex()) {
            case 0:
                if (txtOutputDevice.getText().isBlank()) {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "output", "hackrf");
                }
                else {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "output", "hackrf:" + txtOutputDevice.getText());
                }
                break;
            case 1:
                if (txtOutputDevice.getText().isBlank()) {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "output", "soapysdr");
                }
                else {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "output", "soapysdr:" + txtOutputDevice.getText());
                }
                break;
            case 2:
                if (txtOutputDevice.getText().isBlank()) {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "output", "fl2k");
                }
                else {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "output", "fl2k:" + txtOutputDevice.getText());
                }                
                break;
            case 3:
                if (txtOutputDevice.getText().isBlank()) {
                    messageBox("Please select an output file or change the output device.", JOptionPane.WARNING_MESSAGE);
                }
                else {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "output", txtOutputDevice.getText());
                }                
                break;
            default:
                break;
        }
        // Save current fork if applicable
        if (CaptainJack) FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "fork", "CaptainJack");
        // Input source or test card
        if (PlaylistAL.size() > 0) {
            // We'll populate the playlist section later
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv-gui3", "playlist", 1);
            // Set start point of playlist
            if (StartPoint != -1) FileContents = INI.setIntegerINIValue(FileContents, "hacktv-gui3", "playliststart", StartPoint + 1);
            // Random option
            if (chkRandom.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv-gui3", "random", 1);
        }
        else {
            if (radTest.isSelected()) {
                if ((CaptainJack) && (cmbTest.getComponentCount() > 1)) {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "input", "test:" + TCArray[cmbTest.getSelectedIndex()]);
                }
                else {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "input", "test:colourbars");
                }
            }
            else if (txtSource.getText().toLowerCase().endsWith(".m3u")) {
                // Check if the M3U exists
                if (Files.exists(Path.of(txtSource.getText()))) {
                    // Save the selected item from the Extended M3U file
                    int M3UIndex = cmbM3USource.getSelectedIndex();
                    FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "m3usource", txtSource.getText());
                    FileContents = INI.setIntegerINIValue(FileContents, "hacktv-gui3", "m3uindex", M3UIndex);
                    FileContents = INI.setINIValue(FileContents, "hacktv", "input", PlaylistURLsAL.get(M3UIndex));    
                }
                else {
                    // Save path as-is. This may or may not be valid but will be caught when re-opened.
                    FileContents = INI.setINIValue(FileContents, "hacktv", "input", txtSource.getText());
                }
            }
            else {
                FileContents = INI.setINIValue(FileContents, "hacktv", "input", txtSource.getText());
            }
        }
        // Video format/mode
        FileContents = INI.setINIValue(FileContents, "hacktv", "mode", Mode);
        // Frequency and channel
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            if (!radCustom.isSelected()) {
                FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "channel", cmbChannel.getSelectedItem().toString());
                // Save band plan identifier, this uses the section name from modes.ini
                if (radUHF.isSelected()) {
                    FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "bandplan", UhfAL.get(cmbRegion.getSelectedIndex()));
                }
                else if (radVHF.isSelected()) {
                    FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "bandplan", VhfAL.get(cmbRegion.getSelectedIndex()));
                }
            }
            FileContents = INI.setLongINIValue(FileContents, "hacktv", "frequency", Frequency);
        }
        // Sample rate
        if (SharedInst.isNumeric(txtSampleRate.getText())) {
            FileContents = INI.setLongINIValue(FileContents, "hacktv", "samplerate", (long) (Double.parseDouble(txtSampleRate.getText()) * 1000000));
        }
        // Pixel rate
        if (SharedInst.isNumeric(txtPixelRate.getText())) {
            FileContents = INI.setLongINIValue(FileContents, "hacktv", "pixelrate", (long) (Double.parseDouble(txtPixelRate.getText()) * 1000000));
        }
        // Gain
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "gain", Integer.parseInt(txtGain.getText()));
        }
        // RF Amp
        if (chkAmp.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "amp", 1); }
        // Output level
        if (chkOutputLevel.isSelected()) { FileContents = INI.setINIValue(FileContents, "hacktv", "level", txtOutputLevel.getText());}
        // FM deviation
        if (chkFMDev.isSelected()) { FileContents = INI.setLongINIValue(FileContents, "hacktv", "deviation", (long) (Double.parseDouble(txtFMDev.getText()) * 1000000)); }
        // Gamma
        if (chkGamma.isSelected()) { FileContents = INI.setINIValue(FileContents, "hacktv", "gamma", txtGamma.getText()); }
        // Repeat
        if (chkRepeat.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "repeat", 1); }
        // Position
        if (chkPosition.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "position", Integer.parseInt(txtPosition.getText()));}
        // Verbose
        if (chkVerbose.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "verbose", 1); }
        // Logo
        if (chkLogo.isSelected()) { FileContents = INI.setINIValue(FileContents, "hacktv", "logo", LogoArray[cmbLogo.getSelectedIndex()]) ; }
        // Timestamp
        if (chkTimestamp.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "timestamp", 1); }
        // Interlace
        if (chkInterlace.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "interlace", 1); }
        // Teletext
        if (txtTeletextSource.getText().endsWith(".t42")) {
            FileContents = INI.setINIValue(FileContents, "hacktv", "teletext", "raw:" + txtTeletextSource.getText());
        }
        else if (!txtTeletextSource.getText().isEmpty()) {
            FileContents = INI.setINIValue(FileContents, "hacktv", "teletext", txtTeletextSource.getText());
        }
        /* WSS
         * We increase the value by one, because zero is interpreted as "option disabled" while 1 is
         * interpreted as "auto". We will subtract this again when opening.
        */
        if (chkWSS.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "wss", cmbWSS.getSelectedIndex() + 1); }
        // AR Correction
        if (chkARCorrection.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "arcorrection", cmbARCorrection.getSelectedIndex()); }
        // Scrambling
        // VideoCrypt I+II
        if (cmbScramblingType.getSelectedIndex() == 3) {
            FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingtype", "videocrypt1+2");
            FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey", ScramblingKey1);
            FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey2", ScramblingKey2);
        }
        // Syster dual mode (line shuffling + cut-and-rotate)
        else if ( cmbScramblingType.getSelectedIndex() == 8) {
            FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingtype", "systerls+cnr");
            FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey", ScramblingKey1);
        }
        else if ( (ScramblingType1.equals("--single-cut")) || (ScramblingType1.equals("--double-cut")) ) {
            FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingtype", ScramblingType1.substring(2));
            if (!ScramblingType2.isEmpty()) {
                FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey", ScramblingType2.substring(2) + '\u0020' + ScramblingKey2);
            }
            else {
                FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey", "");
            }
        }
        else if (cmbScramblingType.getSelectedIndex() != 0) {
            FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingtype", ScramblingType1.substring(2));
            FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey", ScramblingKey1);
        }
        if (chkActivateCard.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "emm", 1);
        }
        else if (chkDeactivateCard.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "emm", 2);

        }
        if (SharedInst.isNumeric(txtCardNumber.getText())) {
            switch (txtCardNumber.getText().length()) {
                case 9:
                    FileContents = INI.setINIValue(FileContents, "hacktv", "cardnumber", txtCardNumber.getText().substring(0, 8));
                    break;
                case 13:
                    FileContents = INI.setINIValue(FileContents, "hacktv", "cardnumber", txtCardNumber.getText().substring(4, 12));
                    FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "13digitprefix", txtCardNumber.getText().substring(0, 4));
                    break;
                case 8:
                    FileContents = INI.setINIValue(FileContents, "hacktv", "cardnumber", txtCardNumber.getText());
                    break;
                default:
                    break;
            }
        }
        // Syster permutation table
        if ( (cmbSysterPermTable.getSelectedIndex() == 1) || (cmbSysterPermTable.getSelectedIndex() == 2) ) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "permutationtable", cmbSysterPermTable.getSelectedIndex());
        }
        // EuroCrypt maturity rating
        if (cmbECMaturity.getSelectedIndex() > 0) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "ec-mat-rating", cmbECMaturity.getSelectedIndex());
        }
        // EuroCrypt PPV
        if (chkECppv.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "ec-ppv", 1);
            if (!txtECprognum.getText().isBlank()) {
                FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "ec-ppv-num", Integer.parseInt(txtECprognum.getText()));
            }
            if (!txtECprogcost.getText().isBlank()) {
                FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "ec-ppv-cost", Integer.parseInt(txtECprogcost.getText()));
            }
        }
        // EuroCrypt "No Date" setting
        if (chkNoDate.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "ec-nodate", 1);
        }
        // Show card serial
        if (chkShowCardSerial.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "showserial", 1); }
        // Brute force PPV key
        if (chkFindKeys.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "findkey", 1); }
        // Scramble audio
        if (chkScrambleAudio.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "scramble-audio", 1); }
        // ACP
        if (chkACP.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "acp", 1); }
        // Filter
        if (chkVideoFilter.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "filter", 1); }
        // Audio
        if ( (chkAudio.isSelected()) && (chkAudio.isEnabled()) ) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "audio", 1);
        }
        else if (chkAudio.isEnabled()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "audio", 0); 
        }
        // NICAM
        if (chkNICAM.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "nicam", 1);
        }
        else if ( (!chkNICAM.isSelected()) && (NICAMSupported) ) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "nicam", 0);
        }
        // A2 stereo
        if (chkA2Stereo.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "a2stereo", 1);
        }
        else if ( (!chkA2Stereo.isSelected()) && (A2Supported) ) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "a2stereo", 0);
        }
        // Show ECMs
        if (chkShowECM.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "showecm", 1); }
        // Subtitles
        if (chkSubtitles.isSelected()) { 
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "subtitles", 1); 
            FileContents = INI.setINIValue(FileContents, "hacktv", "subtitleindex", txtSubtitleIndex.getText());
        }
        // VITS
        if (chkVITS.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "vits", 1); }
        // Disable colour
        if (chkColour.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "nocolour", 1); }
        // Invert video
        if (chkInvertVideo.isSelected()) { FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "invert-video", 1); }
        // MAC channel ID
        if (chkMacChId.isSelected()) { FileContents = INI.setINIValue(FileContents, "hacktv", "chid", txtMacChId.getText()); }
        // MAC audio options
        if (radMacMono.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "mac-audio-mode", "mono");
        if (radMac16k.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "mac-audio-quality", "medium");
        if (radMacLinear.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "mac-audio-compression", "linear");
        if (radMacL2.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "mac-audio-protection", "l2");
        // SoapySDR antenna name
        if (cmbOutputDevice.getSelectedIndex() == 1) {
            if (!txtAntennaName.getText().isBlank())
            FileContents = INI.setINIValue(FileContents, "hacktv", "antennaname", txtAntennaName.getText());
        }
        // Output file type
        if (cmbOutputDevice.getSelectedIndex() == 3) {
            FileContents = INI.setINIValue(FileContents, "hacktv", "filetype", cmbFileType.getItemAt(cmbFileType.getSelectedIndex()));
        }
        // Volume
        if (chkVolume.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "volume", txtVolume.getText());
        // Downmix
        if (chkDownmix.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "downmix", 1);
        // Teletext subtitles
        if (chkTextSubtitles.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "tx-subtitles", 1);
            FileContents = INI.setINIValue(FileContents, "hacktv", "tx-subindex", txtTextSubtitleIndex.getText());
        }
        // The playlist doesn't follow a standard INI format. We just dump the
        // playlist array into the file as-is.
        if (PlaylistAL.size() > 0) {
            FileContents = FileContents + "\n[playlist]\n";
            for (int i = 1; i <= PlaylistAL.size(); i++) {
                FileContents = FileContents + PlaylistAL.get(i - 1) + "\n";
            }
        }
        // Commit to disk
        try (FileWriter fw = new FileWriter(DestinationFileName, StandardCharsets.UTF_8)) {
            fw.write(FileContents);
        }
        catch (IOException e) {
            messageBox("An error occurred while writing to this file. "
                    + "The file may be read-only or you may not have the correct permissions.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Display the opened filename in the title bar
        // Back up the original title once
        if (!TitleBarChanged) { 
            TitleBar = this.getTitle();
            TitleBarChanged = true;
        }
        this.setTitle(TitleBar + " - " + DestinationFileName.getName());
       // Remove the ellipsis after Save to follow standard UI guidelines
        menuSave.setText("Save");
        updateMRUList(DestinationFile);
    }
         
    private void m3uHandler(String SourceFile, int M3UIndex) {
        /** Basic M3U file parser.
        *  This reads the channel name from each even-numbered line and the
        *  URL from each odd-numbered line. They're added to arrays to populate
        *  a combobox.
        *  The M3UIndex variable is an integer value which specifies the index
        *  number to select in the combobox.
        */
        String fileHeader;
        try (BufferedReader br2 = new BufferedReader(new FileReader(SourceFile, StandardCharsets.UTF_8))) {
            LineNumberReader lnr2 = new LineNumberReader(br2);
            fileHeader = lnr2.readLine();
        }
        catch (MalformedInputException mie) {
            // Retry using ISO-8859-1
            try (BufferedReader br3 = new BufferedReader(new FileReader(SourceFile, StandardCharsets.ISO_8859_1))) {
                LineNumberReader lnr3 = new LineNumberReader(br3);
                fileHeader = lnr3.readLine();
            }
            catch (IOException ioe) {
                // File is inaccessible or unreadable, so stop
                System.err.println(ioe);
                messageBox("The specified file could not be opened.\n"
                        + "It may have been removed, or you may not have the correct permissions to access it.", JOptionPane.ERROR_MESSAGE); 
                resetM3UItems(false);
                return;
            }
        } 
        catch (IOException ex) {
            // File is inaccessible, so stop
            System.err.println(ex);
            messageBox("The specified file could not be opened.\n"
                    + "It may have been removed, or you may not have the correct permissions to access it.", JOptionPane.ERROR_MESSAGE); 
            resetM3UItems(false);
            return;       
        }        
        if ( (fileHeader == null)) {
            messageBox("Invalid file format.", JOptionPane.ERROR_MESSAGE);
            resetM3UItems(false);
            return;
        }
        // Check that the file is in the correct format by loading its first line
        // We use endsWith to avoid problems caused by Unicode BOMs
        else if (!fileHeader.endsWith("#EXTM3U") ) {
            boolean utf8;
            // Treat the file as a standard text-only playlist
            Path fp = Paths.get(SourceFile);
            List<String> pls;
            try {
                pls = Files.readAllLines(fp, StandardCharsets.UTF_8);
                utf8 = true;
            }
            catch (MalformedInputException mie){
                // Retry using ISO-8859-1
                try {
                    pls = Files.readAllLines(fp, StandardCharsets.ISO_8859_1);
                    utf8 = false;
                }
                catch (IOException ioe) {
                    // File is unreadable, so stop
                    System.err.println(ioe);
                    messageBox("Invalid file format.", JOptionPane.ERROR_MESSAGE);
                    resetM3UItems(false);
                    return;
                }
            }
            catch (IOException ex) {
                // File is unreadable, so stop
                System.err.println(ex);
                messageBox("Invalid file format.", JOptionPane.ERROR_MESSAGE);
                resetM3UItems(false);
                return;
            }
            // Define an ArrayList and a string to include missing paths
            List<String> toRemove = new ArrayList<>();
            String removed = new String();
            // Run through the imported playlist to check if the paths exist
            int i = 0;
            for (String file : pls) {
                // Skip if this is a URL or test card
                if ( (!file.startsWith("http:")) &&
                        (!file.startsWith("https:")) &&
                        (!file.startsWith("test:")) ) {
                    if (!Files.exists(Path.of(file))) {
                        if ( (!utf8) && (RunningOnWindows)) {
                            // We may have encountered an encoding bug so retry
                            // by converting the files string to MS-DOS CP 850
                            try {
                                String f850 = new String(file.getBytes("ISO-8859-1"), "CP850");
                                if (Files.exists(Path.of(f850))) {
                                    // Worked! Change the item in pls.
                                    String f8 = new String(f850.getBytes(), "UTF-8");
                                    pls.set(i, f8);
                                }
                            }
                            catch (InvalidPathException | UnsupportedEncodingException e) {
                                // Add the item to the "to remove" list
                                // Also add it to the removed string so we can present it to the user
                                toRemove.add(file);
                                removed = removed + file + "\n";
                            }
                        }
                        else {
                            // Add the item to the "to remove" list
                            // Also add it to the removed string so we can present it to the user
                            toRemove.add(file);
                            removed = removed + file + "\n";
                        }
                    }
                }
                i++;
            }
            // Did we add any files to be removed? If so, remove them and alert.
            if (toRemove.size() > 0) {
                pls.removeAll(toRemove);
                messageBox("Some files could not be found and have been removed from the playlist.\n" +
                        removed, JOptionPane.WARNING_MESSAGE);
            }
            // Add the imported playlist to PlaylistAL and populate
            PlaylistAL.addAll(pls);
            populatePlaylist();
            resetM3UItems(false);
            return;
        }
        // Handler for Extended M3U files (with #EXTM3U header)
        // Set mouse cursor to busy
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Temporarily disable the radio buttons, Browse and Run buttons, and menus
        btnSourceBrowse.setEnabled(false);
        radLocalSource.setEnabled(false);
        radTest.setEnabled(false);
        btnRun.setEnabled(false);
        btnAdd.setEnabled(false);
        // Hide the source file textbox and show the combobox
        txtSource.setVisible(false);
        cmbM3USource.setVisible(true);
        cmbM3USource.setEnabled(false);
        fileMenu.setEnabled(false);
        templatesMenu.setEnabled(false);
        // Prevent the comobobox from auto-resizing
        Dimension d = new Dimension(360,22);
        cmbM3USource.setPreferredSize(d);
        // Remove any existing items from the combobox
        cmbM3USource.removeAllItems();
        cmbM3USource.addItem("Loading playlist file, please wait...");
        // Create a SwingWorker to do the disruptive stuff
        SwingWorker<Boolean, Double> m3uWorker = new SwingWorker<Boolean, Double>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                String M3UNames = new String();
                ArrayList<String> PlaylistNamesAL = new ArrayList <> ();
                PlaylistURLsAL = new ArrayList <> ();
                try {
                    // Read source file to a list.
                    List<String> fl = Files.readAllLines(Path.of(SourceFile));
                    // Remove the first line as this contains the #EXTM3U header
                    // We don't need it and if there's a BOM in front, it'll
                    // just mess things up.
                    fl.remove(0);
                    // Loop over the list, only adding lines that either start
                    // with #EXTINF, are not blank, and do not start with #
                    int ln = 0;
                    for (String l : fl) {
                        // Publish a decimal value for the percentage indicator
                        publish((double) ln / fl.size());
                        if (l.startsWith("#EXTINF:")) {
                            // Read names to M3UNames so we can parse them
                            M3UNames = M3UNames + l + System.lineSeparator();
                        }
                        else if ((!l.startsWith("#")) && (!l.isBlank())) {
                            // Read URLs directly to the arraylist
                            PlaylistURLsAL.add(l);
                        }
                        ln++;
                    }
                }
                catch (IOException ex) {
                    System.err.println(ex);
                    return false;
                }
                // Done, publish 100%
                publish(1.0);
                // Use a regex to retrieve the contents of each line after the
                // last comma. This contains the channel name.
                Pattern p = Pattern.compile(".*,\\s*(.*)");
                Matcher m = p.matcher(M3UNames);
                while (m.find()) {
                    PlaylistNamesAL.add(m.group(1));
                }
                // Check that we got something, if not then stop.
                if (PlaylistNamesAL.isEmpty()) {
                    return false;
                }
                else {
                    // Check for duplicate entries in the ArrayList because
                    // Swing doesn't handle them too well.
                    // We append the index number as a workaround.
                    // This could probably be done better.
                    for (int i = 0, j = 0; i < PlaylistNamesAL.size(); i++) {
                        if (i != j) {
                            if ( PlaylistNamesAL.get(j).equals(PlaylistNamesAL.get(i)) ) {
                                PlaylistNamesAL.set(i, PlaylistNamesAL.get(i) + " #" + i);
                            }
                        }
                        if (i == PlaylistNamesAL.size() -1) {
                            j++;
                            if ( j == PlaylistNamesAL.size() ) break;
                            i = 0;
                        }
                    }
                    // Convert ArrayList to an array so we can populate the combobox
                    PlaylistNames = new String[PlaylistNamesAL.size()];
                    for (int i = 0; i < PlaylistNamesAL.size(); i++) {
                        PlaylistNames[i] = PlaylistNamesAL.get(i);
                    }
                    return true;
                }
            } // End doInBackground()

            @Override
            protected void done() {
                // Retrieve the return value of doInBackground.
                boolean status;
                try {
                    status = get();
                }
                catch (InterruptedException | ExecutionException ex) {
                    status = false;   
                }
                if (status) {
                    // Enable and populate the combobox
                    cmbM3USource.setEnabled(true);
                    cmbM3USource.setModel(new DefaultComboBoxModel <> (PlaylistNames));
                    cmbM3USource.setSelectedIndex(M3UIndex);
                    // Repaint the combobox (resolves an issue with it not showing the
                    // correct entry on the Metal L&F after loading an M3U file).
                    cmbM3USource.repaint();
                    // Reset cursor and re-enable the radio buttons that we disabled
                    resetM3UItems(true);
                }
                else {
                    messageBox(
                            "An error occurred while processing this file. "
                                    + "It may be invalid or corrupted.", JOptionPane.ERROR_MESSAGE);
                    resetM3UItems(false); 
                }
            } // End done()

            @Override
            protected void process(List<Double> chunks) {
                short p = (short) (chunks.get(chunks.size()-1) * 100);
                // Taskbar/dock progress if supported
                if (Taskbar.isTaskbarSupported()) {
                    var t = Taskbar.getTaskbar();
                    if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
                        t.setWindowProgressValue(GUI.this, p);
                    }
                    else if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                        t.setProgressValue((short) p);
                    }
                }
                cmbM3USource.removeAllItems();
                cmbM3USource.addItem("Loading playlist file, please wait... " + p + "%");
            }
          }; // End SwingWorker
        m3uWorker.execute();       
    }     
    
    private void resetM3UItems(boolean LoadSuccessful) {
        // Reset whatever we changed back to default upon thread exit
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        radLocalSource.setEnabled(true);
        radTest.setEnabled(true);
        btnSourceBrowse.setEnabled(true);
        btnRun.setEnabled(true);
        btnAdd.setEnabled(true);
        fileMenu.setEnabled(true);
        templatesMenu.setEnabled(true);
        // Reset taskbar/dock progress bars
        if (Taskbar.isTaskbarSupported()) {
            var t = Taskbar.getTaskbar();
            if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
                t.setWindowProgressState(GUI.this, Taskbar.State.OFF);
            }
            else if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                t.setProgressValue(-1);
            }
        }
        if (!LoadSuccessful) {
            // Hide the combobox and show the source textbox
            // Use this for a load failure
            //resetM3UItems(true);
            txtSource.setVisible(true);
            txtSource.setText("");
            cmbM3USource.setVisible(false);
            cmbM3USource.setEnabled(false);   
        }
    }
    
    private void resetAllControls() {
        // Clear status bar
        txtStatus.setText("");
        // Uncheck all checkboxes
        for(JCheckBox cb: CheckBoxes){
            if ( cb.isSelected() ) { cb.doClick(); }
        }
        // Hide M3U combobox if enabled, and set source to empty
        if (cmbM3USource.isEnabled()) {
            cmbM3USource.setEnabled(false);
            cmbM3USource.setVisible(false);
            txtSource.setVisible(true);
            txtSource.setText("");    
        }
        else {
            txtSource.setText("");
        }
        // If a baseband mode is selected, reset the video format to zero to
        // avoid unnecessary error messages.
        if (!radCustom.isEnabled()) cmbMode.setSelectedIndex(0);
        // Reset output device to HackRF
        cmbOutputDevice.setSelectedIndex(0);
        // Reset playlist start point
        StartPoint = -1;
        // Select default radio buttons and comboboxes
        cmbRegion.setEnabled(false);
        radLocalSource.doClick();
        radPAL.doClick();
        radUHF.doClick();
        cmbScramblingType.setSelectedIndex(0);
        // Reset gain to zero
        txtGain.setText("0");
        // Re-enable audio option
        if (! chkAudio.isSelected() ) { chkAudio.doClick(); }
        // Clear playlist
        btnRemove.setEnabled(false);
        btnPlaylistUp.setEnabled(false);
        btnPlaylistDown.setEnabled(false);
        PlaylistAL.clear();
        populatePlaylist();
        // Restore title bar to default
        if (TitleBarChanged) { this.setTitle(TitleBar); }
        // Restore ellipsis to Save option
        if (menuSave.getText().equals("Save")) { menuSave.setText("Save..."); }
    }
    
    private void addCeefaxRegions() {
        // Populate the Ceefax regions to the combobox in GUI settings
        String[] CeefaxRegions = {
            "East",
            "East Midlands",
            "London",
            "Northern Ireland",
            "Scotland",
            "South",
            "South West",
            "Wales",
            "West",
            "Worldwide",
            "Yorkshire & Lincolnshire"
        };
        cmbNMSCeefaxRegion.removeAllItems();
        cmbNMSCeefaxRegion.setModel(new DefaultComboBoxModel<>(CeefaxRegions));
        // Read a previously saved region from the prefs store.
        // If not found or invalid, default to Worldwide.
        int i = Prefs.getInt("CeefaxRegion", 9);
        if ( (i + 1 <= cmbNMSCeefaxRegion.getItemCount()) && (i >= 0) ) {
            cmbNMSCeefaxRegion.setSelectedIndex(i);
        }
        else {
            cmbNMSCeefaxRegion.setSelectedIndex(9);
        }
    }

    private void downloadTeletext(String url, String destinationFile, String HTMLString) {
        ArrayList<String> TeletextLinks = new ArrayList<>();
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Downloads the specified file from URL and saves it to DestinationFile
        // Create temp directory if it does not exist
        createTempDirectory();
        // Specify the destination location of the HTML file we will download
        String DownloadPath = TempDir + File.separator + destinationFile;
        try {
            DownloadInProgress = true;
            // If the file already exists from a previous attempt, delete it
            File f = new File(DownloadPath);
            if (f.exists()) SharedInst.deleteFSObject(f.toPath());
            // Download the index page
            txtStatus.setText("Downloading index page from " + url);
            SharedInst.download(url, DownloadPath);
        }
        catch (IOException ex) {
            System.err.println(ex);
            messageBox("An error occurred while downloading files. "
                    + "Please ensure that you are connected to the internet and try again.", JOptionPane.ERROR_MESSAGE);
            txtStatus.setText("Cancelled");
            resetTeletextButtons();
            return;
        }
        // Create a SwingWorker to do the disruptive stuff
        SwingWorker<Integer, Integer> downloadPages = new SwingWorker<Integer, Integer>() {
            @Override
            protected Integer doInBackground() throws Exception {
                File f;
                Path fd = Paths.get(TempDir + File.separator + HTMLTempFile);
                String dUrl = url;
                // Try to read the downloaded index file to a string
                try {
                    HTMLFile = Files.readString(fd);
                }
                catch (IOException ex) {
                    System.err.println(ex);
                }
                // Search the string for the pattern defined in the teletext button
                Pattern pattern = Pattern.compile(HTMLString, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(HTMLFile);
                while (matcher.find()) {
                    // Populate the results to the TeletextLinks array
                    // Filter TTI and TTIX files only
                    if ( (matcher.group(1).endsWith(".tti")) || (matcher.group(1).endsWith(".ttix")) ) {
                        TeletextLinks.add(matcher.group(1));
                    }
                }
                if (TeletextLinks.isEmpty()) {
                    return 3;
                }
                switch (dUrl) {
                    case "https://github.com/spark-teletext/spark-teletext/":
                        // Set SPARK prerequisites - change URL first
                        dUrl = "https://raw.githubusercontent.com/spark-teletext/spark-teletext/master/";
                        f = new File(TempDir + File.separator + "spark");
                        break;
                    case "https://internal.nathanmediaservices.co.uk/svn/ceefax/national/":
                        // Set Ceefax temp directory
                        f = new File(TempDir + File.separator + "ceefax");
                        break;
                    case "http://teastop.plus.com/svn/teletext/":
                        // Set Teefax temp directory
                        f = new File(TempDir + File.separator + "teefax");
                        break;
                    default:
                        if (dUrl.startsWith("https://internal.nathanmediaservices.co.uk/svn/ceefax/")) {
                            // This is most likely a Ceefax region
                            f = new File(TempDir + File.separator + "ceefax_region");
                        }
                        else {
                            System.err.println("Unknown teletext URL");
                            return 997;
                        }
                        break;
                }
                /*  Delete this directory if it already exists (e.g. from a previous
                    download attempt).
                */
                if (Files.exists(f.toPath())) {
                    try {
                        SharedInst.deleteFSObject(f.toPath());
                    }
                    catch (IOException ex) {
                        System.err.println(ex);
                    }
                }
                // Create temp directory
                if (!f.isDirectory()) f.mkdirs();
                TeletextPath = f.toString();

                for(int i = 0; i < TeletextLinks.size(); i++) {
                    int j = i+1;
                    if ( TeletextLinks.get(i).endsWith("tti") || TeletextLinks.get(i).endsWith("ttix") ) {
                        try {
                            // If the Stop button has been pressed, then stop
                            if (DownloadCancelled) {
                                DownloadCancelled = false;
                                DownloadInProgress = false;
                                return 1;
                            }
                            publish(j);
                            // Do the actual downloading
                            SharedInst.download(dUrl + TeletextLinks.get(i), TeletextPath + File.separator + TeletextLinks.get(i));
                            // Stop when the integer value reaches the size of the teletext array
                            if (j == TeletextLinks.size()) return 0;
                        }
                        catch (IOException ex) {
                            return 2;
                        }
                    }
                }
                return 998;
            }

            @Override
            protected void done() {
                int status;
                try {
                    // Retrieve the status code from doInBackground.
                    status = get();
                }
                catch (InterruptedException | ExecutionException ex) {
                    System.err.println(ex);
                    status = 999;
                }
                switch (status) {
                    case 0:
                        // All good
                        txtStatus.setText("Done");
                        txtTeletextSource.setText(TeletextPath);
                        // Check if we just downloaded Ceefax
                        if (url.equals("https://internal.nathanmediaservices.co.uk/svn/ceefax/national/")) {
                            // It's not enough to just download the national files, we also need a region
                            String[] CeefaxRegionArray = new String[] {
                                "East",
                                "EastMidlands",
                                "London",
                                "NorthernIreland",
                                "Scotland",
                                "South",
                                "SouthWest",
                                "Wales",
                                "West",
                                "Worldwide",
                                "Yorks&Lincs"
                            };
                            HTMLTempFile = "ceefax_region.xml";
                            String nUrl = "https://internal.nathanmediaservices.co.uk/svn/ceefax/"
                                    + CeefaxRegionArray[cmbNMSCeefaxRegion.getSelectedIndex()] + "/";
                            // Download regional index page
                            downloadTeletext(nUrl, HTMLTempFile, HTMLString);
                        }
                        else if (HTMLTempFile.equals("ceefax_region.xml")) {
                            // Move the regional files to the national directory
                            File rd = new File(TempDir + "/ceefax_region");
                            File nd = new File(TempDir + "/ceefax");
                            if ( (rd.isDirectory()) && (nd.isDirectory()) ) {
                                File[] files = rd.listFiles();
                                for (File file : files) {
                                    try {
                                        Files.move(file.toPath(), Path.of(nd + File.separator + file.getName()), StandardCopyOption.REPLACE_EXISTING);
                                    }
                                    catch (IOException e) {
                                        messageBox("An error occurred when merging the regional Ceefax data with the national data.\n"
                                                + e, JOptionPane.WARNING_MESSAGE);
                                        break;
                                    }
                                }
                            }
                            // Reset the source directory to the national directory,
                            // which hopefully now has the regional files merged into it
                            txtTeletextSource.setText(TeletextPath.substring(0, TeletextPath.length() - 7));
                        }
                        break;
                    case 1:
                        // Download cancelled by the user
                        pbTeletext.setValue(0);
                        txtStatus.setText("Cancelled");
                        break;
                    case 2:
                        // The index page was downloaded but a teletext page failed.
                        // Connection failure?
                        messageBox("An error occurred while downloading files. "
                                + "Please ensure that you are connected to the internet and try again.", JOptionPane.ERROR_MESSAGE);
                        pbTeletext.setValue(0);
                        txtStatus.setText("Failed");
                        break;
                    case 3:
                        // The index page was downloaded but we didn't find anything.
                        // Most likely means that we need to revise this!
                        messageBox("No teletext files were found.", JOptionPane.ERROR_MESSAGE);
                        pbTeletext.setValue(0);
                        txtStatus.setText("Failed");
                        break;
                    default:
                        messageBox("An unknown error has occurred, code " + status, JOptionPane.ERROR_MESSAGE);
                        pbTeletext.setValue(0);
                        txtStatus.setText("Failed");
                        break;
                }
                pbTeletext.setValue(0);
                resetTeletextButtons();
            }

            @Override
            protected void process(List<Integer> chunks) {
                /* Set progress bar values.
                 * Set minimum to zero.
                 * Set maximum to the size of the teletext links array.
                 */
                pbTeletext.setMinimum(0);
                pbTeletext.setMaximum(TeletextLinks.size());
                // Retrieve the values from publish() and use them to increment
                // the progress bar and display in the status bar.
                int i = chunks.get(chunks.size()-1);
                // Show progress in status bar
                double pc = (double) i / TeletextLinks.size() * 100;
                txtStatus.setText("Downloading page " + TeletextLinks.get(i -1)
                        +  '\u0020' + "(" + i + " of " + TeletextLinks.size() + ")"
                        +  '\u0020' + (short) pc + "%");
                // Taskbar/dock progress if supported
                if (Taskbar.isTaskbarSupported()) {
                    var t = Taskbar.getTaskbar();
                    if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
                        t.setWindowProgressValue(GUI.this, (short) pc);
                    }
                    else if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                        t.setProgressValue((short) pc);
                    }
                }
                // Increment progress bar by one
                pbTeletext.setValue(i);
            }
        };
        downloadPages.execute();
    }
    
    private void resetTeletextButtons() {
        // Resets the labels of the teletext buttons back to defaults and
        // re-enables them.
        btnTeefax.setText("Teefax");
        btnTeefax.setEnabled(true);
        lblTeefax.setEnabled(true);
        btnSpark.setText("SPARK");
        btnSpark.setEnabled(true);
        lblSpark.setEnabled(true);
        btnNMSCeefax.setText("Ceefax");
        btnNMSCeefax.setEnabled(true);
        lblNMSCeefax.setEnabled(true);
        chkTeletext.setEnabled(true);
        txtTeletextSource.setEnabled(true);
        btnTeletextBrowse.setEnabled(true);
        btnRun.setEnabled(true);
        // Reset taskbar/dock progress bars
        if (Taskbar.isTaskbarSupported()) {
            var t = Taskbar.getTaskbar();
            if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
                t.setWindowProgressState(GUI.this, Taskbar.State.OFF);
            }
            else if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                t.setProgressValue(-1);
            }
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        DownloadInProgress = false;
    }
      
    private void enableColourControl() {
        chkColour.setEnabled(true);
    }
    
    private void disableColourControl() {
        if (chkColour.isSelected()) {
            chkColour.doClick();
            chkColour.setEnabled(false);
        }
        else {
            chkColour.setEnabled(false);
        }        
    }

    private void astraTemplate(Double localOscillator) {
        int q = JOptionPane.showConfirmDialog(null, "This will load template values for an Astra satellite receiver configured for a "
                + localOscillator + " GHz LO LNB.\n"
                        + "All current settings will be cleared. Do you wish to continue?", APP_NAME, JOptionPane.YES_NO_OPTION);
        if (q == JOptionPane.YES_OPTION) {
            // Reset all controls
            resetAllControls();
            // Select PAL-FM mode
            int a = -1;
            for (int i = 0; i < PALModeArray.length; i++) {
                if (PALModeArray[i].equals("pal-fm")) {
                    a = i;
                }
            }
            if (a != -1) {
                cmbMode.setSelectedIndex(a);
            }
            else {
                messageBox("Unable to find the PAL-FM mode, which is required for this template.", JOptionPane.ERROR_MESSAGE);
                resetAllControls();
                return;
            }
            // Set custom frequency
            radCustom.doClick();
            // Enable pre-emphasis filter and enable FM deviation option
            chkVideoFilter.doClick();
            chkFMDev.doClick();
            // Get selected frequency and set deviation
            // fg is the STB frequency, we default to the old frequency for
            // The Children's Channel (10.994 GHz)
            double fg = 10.994;
            // Convert fg to MHz for clarity, we'll use this value internally
            int f = (int) (fg * 1000);
            int lo = (int) ((double) localOscillator * 1000);
            int of;
            switch (Integer.parseInt(TemplateButtonGroup.getSelection().getActionCommand())) {
                case 0:
                    // IF
                    of = f - lo;
                    txtFMDev.setText("10");
                    break;
                case 1:
                    // Ku band
                    of = f / 2;
                    txtFMDev.setText("8");
                    break;
                case 2:
                    // Ka band LNB at 21.2 GHz LO (Saorsat Inverto)
                    // Below is a formula to select the transmission frequency
                    // based on the receiver's IF
                    // (((Ku frequency - Ku local oscillator) * -1) + Ka local oscillator) / 4
                    of = (((f - lo) * -1) + 21200) / 4;
                    txtFMDev.setText("4");
                    // Invert video polarity, a 21.2 GHz Ka LNB is negative IF
                    chkInvertVideo.doClick();
                    break;
                default:
                    of = -999;
                    System.err.println("Frequency error");
                    break;
            }
            // Set frequency to the value we got above
            txtFrequency.setText(String.valueOf(of));
            messageBox("Template values have been loaded. Tune your receiver to "
                    + fg + " GHz and run hacktv.", JOptionPane.INFORMATION_MESSAGE);    
        }
    }    
    
    private void enableScrambling() {
        cmbScramblingType.setEnabled(true);
        lblScramblingSystem.setEnabled(true);
        scramblingPanel.setEnabled(true);
    }    
    
    private void disableScrambling() {
        ArrayList<String> ScramblingTypeAL = new ArrayList<>();
        ScramblingTypeAL.add("No scrambling");
        ScramblingTypeArray = new ArrayList<>();
        ScramblingTypeArray.add("");
        cmbScramblingType.removeAllItems();
        // Convert to an array so we can populate
        String[] ScramblingType = new String[ScramblingTypeAL.size()];
        for(int i = 0; i < ScramblingType.length; i++) {
            ScramblingType[i] = ScramblingTypeAL.get(i);
        } 
        cmbScramblingType.setModel(new DefaultComboBoxModel<>(ScramblingType));
        cmbScramblingType.setSelectedIndex(0);
        cmbScramblingType.setEnabled(false);
        lblScramblingSystem.setEnabled(false);
        scramblingPanel.setEnabled(false);
    }      
    
    private void add625ScramblingTypes() {
        ArrayList<String> ScramblingTypeAL = new ArrayList<>();
        ScramblingTypeAL.add("No scrambling");
        ScramblingTypeAL.add("VideoCrypt I");
        ScramblingTypeAL.add("VideoCrypt II");
        ScramblingTypeAL.add("VideoCrypt I+II");
        ScramblingTypeAL.add("VideoCrypt S");
        ScramblingTypeAL.add("Nagravision Syster");
        if (CaptainJack) { 
            ScramblingTypeAL.add("Discret 11");
            ScramblingTypeAL.add("Nagravision Syster (cut-and-rotate mode)");
            ScramblingTypeAL.add("Nagravision Syster (line shuffle and cut-and-rotate modes)");
        }
        ScramblingTypeArray = new ArrayList<>();
        ScramblingTypeArray.add("");
        ScramblingTypeArray.add("--videocrypt");
        ScramblingTypeArray.add("--videocrypt2");
        ScramblingTypeArray.add("--videocrypt");
        ScramblingTypeArray.add("--videocrypts");
        ScramblingTypeArray.add("--syster");
        if (CaptainJack) {
            ScramblingTypeArray.add("--d11");
            ScramblingTypeArray.add("--systercnr");
            ScramblingTypeArray.add("--syster");
        }
        cmbScramblingType.removeAllItems();
        
        // Convert to an array so we can populate
        String[] ScramblingType = new String[ScramblingTypeAL.size()];
        for(int i = 0; i < ScramblingType.length; i++) {
            ScramblingType[i] = ScramblingTypeAL.get(i);
        } 
        cmbScramblingType.setModel(new DefaultComboBoxModel<>(ScramblingType));
        cmbScramblingType.setSelectedIndex(0);
    }
    
    private void addMACScramblingTypes() {
        ArrayList<String> ScramblingTypeAL = new ArrayList<>();
        ScramblingTypeAL.add("No scrambling");
        ScramblingTypeAL.add("Single cut");
        ScramblingTypeAL.add("Double cut");
        
        ScramblingTypeArray = new ArrayList<>();
            ScramblingTypeArray.add("");
            ScramblingTypeArray.add("--single-cut");
            ScramblingTypeArray.add("--double-cut");
            
        cmbScramblingType.removeAllItems();
        
        // Convert to an array so we can populate
        String[] ScramblingType = new String[ScramblingTypeAL.size()];
        for(int i = 0; i < ScramblingType.length; i++) {
            ScramblingType[i] = ScramblingTypeAL.get(i);
        } 
        cmbScramblingType.setModel(new DefaultComboBoxModel<>(ScramblingType));
        cmbScramblingType.setSelectedIndex(0);
    }
    
    private void addScramblingKey() {
        // In the clear (no scrambling)
        if (ScramblingType1.isEmpty()) {
            scramblingOptionsPanel.setEnabled(false);
            emmPanel.setEnabled(false);
            disableScramblingKey1();
            cmbScramblingKey1.setSelectedIndex(-1);
            disableScramblingKey2();
            ScramblingType2 = "";
            ScramblingKey1 = "";
            ScramblingKey2 = "";
            configureScramblingOptions();
            txtSampleRate.setText(DefaultSampleRate);
            if (chkPixelRate.isSelected()) chkPixelRate.doClick();
            return;
        }
        else {
            enableScramblingKey1();
            scramblingOptionsPanel.setEnabled(true);
            emmPanel.setEnabled(true);    
        }
        // Get the scrambling system name  
        String sconf = ScramblingTypeArray.get(cmbScramblingType.getSelectedIndex()).substring(2);
        switch (sconf) {
            case "videocrypt":
                // Set pixel rate to 28 MHz (multiples of 14 are OK)
                if ((!chkPixelRate.isSelected()) && !HTVLoadInProgress) chkPixelRate.doClick();
                if (!HTVLoadInProgress) txtPixelRate.setText("28");
                disableScramblingKey2();
                sconf = "videocrypt";
                break;
            case "videocrypt2":
                // Set pixel rate to 28 MHz (multiples of 14 are OK)
                if ((!chkPixelRate.isSelected()) && !HTVLoadInProgress) chkPixelRate.doClick();
                if (!HTVLoadInProgress) txtPixelRate.setText("28");
                disableScramblingKey2();
                sconf = "videocrypt2";
                break;
            case "videocrypts":
                disableScramblingKey2();
                // Set pixel rate to 17.75 MHz (more accurately 17.734475 but
                // this is reported by hacktv as unsuitable for 625/50)
                if ((!chkPixelRate.isSelected()) && !HTVLoadInProgress) chkPixelRate.doClick();
                if (!HTVLoadInProgress) txtPixelRate.setText("17.75");
                sconf = "videocrypts";
                break;
            case "syster":
                // No pixel rate required for Syster
                disableScramblingKey2();
                sconf = "syster";
                break;
            case "d11":
                // Set pixel rate to 17.75 MHz
                if ((!chkPixelRate.isSelected()) && !HTVLoadInProgress) chkPixelRate.doClick();
                if (!HTVLoadInProgress) txtPixelRate.setText("17.75");
                disableScramblingKey2();
                sconf = "syster";
                break;
            case "systercnr":
                // Set pixel rate to 17.75 MHz
                if ((!chkPixelRate.isSelected()) && !HTVLoadInProgress) chkPixelRate.doClick();
                if (!HTVLoadInProgress) txtPixelRate.setText("17.75");
                disableScramblingKey2();
                sconf = "syster";
                break;
            case "single-cut":
            case "double-cut":
                disableScramblingKey2();
                sconf = "eurocrypt";
                break;
            default:
                // This should never run
                break;
        }
        // Extract (from ModesFile) the scrambling key section that we need
        String slist = INI.splitINIfile(ModesFile, sconf);
        if (slist == null) {
            ScramblingType1 = "";
            cmbScramblingType.setSelectedIndex(0);
            addScramblingKey();
            messageBox("The scrambling key information in Modes.ini appears to be "
                    + "missing or corrupt for the selected scrambling type.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // We just want the commands so remove everything after =
        slist = slist.replaceAll("\\=.*", "");       
        // Remove commented out lines
        slist = Stream.of(slist.split("\n"))
                .filter(f -> !f.contains(";"))
                .collect(Collectors.joining("\n"));
        String[] ScramblingKey = slist.substring(slist.indexOf("\n") + 1).split("\\r?\\n");
        // Extract friendly names and add commands to an ArrayList
        ScramblingKeyArray = new ArrayList<>();
        if ( (sconf.equals("single-cut"))|| (sconf.equals("double-cut")) ) {
            ScramblingKey[0] = ("No conditional access (free");
            ScramblingKeyArray.clear();
            ScramblingKeyArray.add("");
        }
        for (int i = 0; i < ScramblingKey.length; i++) {
            ScramblingKeyArray.add(ScramblingKey[i]);
            ScramblingKey[i] = INI.getStringFromINI(ModesFile , sconf, ScramblingKey[i], "", true);
        }
        // Populate key 1 combobox
        cmbScramblingKey1.setModel(new DefaultComboBoxModel<>(ScramblingKey));
        cmbScramblingKey1.setSelectedIndex(0);
        
        // VC1+2 dual mode    
        if (cmbScramblingType.getSelectedIndex() == 3) {
            final String sconf2 = "videocrypt2";
            if (CaptainJack) {
                enableScramblingKey1();
                enableScramblingKey2();
            }
            else {
                disableScramblingKey1();
                disableScramblingKey2();
            }
            cmbScramblingKey2.removeAllItems();
            // Extract (from ModesFile) the VC2 scrambling key section
            String slist2 = INI.splitINIfile(ModesFile, sconf2);
            if (slist2 == null) {
                messageBox("The scrambling key information in Modes.ini appears to be "
                        + "missing or corrupt for the selected scrambling type.", JOptionPane.WARNING_MESSAGE);
                cmbScramblingType.setSelectedIndex(0);
                return;
            }
            // We just want the commands so remove everything after =
            slist2 = slist2.replaceAll("\\=.*", "");       
            // Remove commented out lines
            slist2 = Stream.of(slist2.split("\n"))
                    .filter(f -> !f.contains(";"))
                    .collect(Collectors.joining("\n"));
            String[] ScramblingKey2A = slist2.substring(slist2.indexOf("\n") +1).split("\\r?\\n");
            // Extract friendly names and add commands to an ArrayList
            ScramblingKey2Array = new ArrayList<>();
            for (int i = 0; i < ScramblingKey2A.length; i++) {
                ScramblingKey2Array.add(ScramblingKey2A[i]);
                ScramblingKey2A[i] = INI.getStringFromINI(ModesFile , sconf2, ScramblingKey2A[i], "", true);
            }
            // Populate key 2 combobox
            cmbScramblingKey2.setModel(new DefaultComboBoxModel<>(ScramblingKey2A));
            cmbScramblingKey2.setSelectedIndex(0);
        }
    }
    
    private void configureScramblingOptions() {
        // Enable the Scramble audio option if supported
        if ( ((ScramblingType1).equals("--single-cut")) || 
                ((ScramblingType1).equals("--double-cut")) ||
                ((ScramblingType1).equals("--syster")) ||
                ((ScramblingType1).equals("--d11")) ||
                ((ScramblingType1).equals("--systercnr")) ) {
            chkScrambleAudio.setEnabled(true);
        }
        else {
            if (chkScrambleAudio.isSelected()) {
                chkScrambleAudio.doClick();
            }
            chkScrambleAudio.setEnabled(false);
        }
        // Enable EuroCrypt maturity rating
        if ((CaptainJack) && (ScramblingType2.equals("--eurocrypt"))) {
            lblECMaturity.setEnabled(true);
            cmbECMaturity.setEnabled(true);
            cmbECMaturity.setSelectedIndex(0);
        }
        else {
            lblECMaturity.setEnabled(false);
            cmbECMaturity.setEnabled(false);
            cmbECMaturity.setSelectedIndex(-1);
        }
        // Enable EuroCrypt PPV and "no date" options
        if ((CaptainJack) && (ScramblingType2.equals("--eurocrypt"))) {
            chkECppv.setEnabled(true);
            chkNoDate.setEnabled(true);
        }
        else {
            if (chkECppv.isSelected()) chkECppv.doClick();
            if (chkNoDate.isSelected()) chkNoDate.doClick();
            chkECppv.setEnabled(false);
            chkNoDate.setEnabled(false);
        }        
        // Enable card serial option
        if ( ((ScramblingType1).equals("--videocrypt")) || 
                ((ScramblingType1).equals("--videocrypt2")) ) {
            if (CaptainJack) { chkShowCardSerial.setEnabled(true); }
        }
        else {
            if (chkShowCardSerial.isSelected()) {
                chkShowCardSerial.doClick();
            }
            chkShowCardSerial.setEnabled(false);
        }
        // Enable EMM options on sky07, sky09 and VC2 conditional modes
        if ( ((ScramblingKey1).equals("sky07")) ||
                ((ScramblingKey1).equals("sky09")) )
        {
            chkActivateCard.setEnabled(true);
            chkDeactivateCard.setEnabled(true);
        }
        else if ( ((ScramblingType1).equals("--videocrypt2")) &&
                ((ScramblingKey1).equals("conditional")) )
        {
            chkActivateCard.setEnabled(true);
            chkDeactivateCard.setEnabled(true);
        }
        else {
            if (chkActivateCard.isSelected()) { chkActivateCard.doClick(); }
            if (chkDeactivateCard.isSelected()) { chkDeactivateCard.doClick(); }
            chkActivateCard.setEnabled(false);
            chkDeactivateCard.setEnabled(false);
            lblEMMCardNumber.setEnabled(false);
        }
        // Enable PPV findkey option
        if ( (ScramblingKey1.equals("ppv")) ) {
            chkFindKeys.setEnabled(true);
        }
        else {
            if ( chkFindKeys.isSelected()) { chkFindKeys.doClick(); }
            chkFindKeys.setEnabled(false);
        }
        // Enable permutation table options (Syster-based modes)
        if ( ((ScramblingType1).equals("--syster")) || (ScramblingType1).equals("--systercnr")) {
            if (CaptainJack) {
                lblSysterPermTable.setEnabled(true);
                cmbSysterPermTable.setEnabled(true);
                cmbSysterPermTable.setSelectedIndex(0);
            }
        }
        else {
            lblSysterPermTable.setEnabled(false);
            cmbSysterPermTable.setEnabled(false);
            cmbSysterPermTable.setSelectedIndex(-1);
        }
        // Enable ECM option and disable ACP (if not a MAC mode)
        if (cmbScramblingType.getSelectedIndex() == 0) {
            if ( chkShowECM.isSelected() ) { chkShowECM.doClick(); }
            chkShowECM.setEnabled(false);
            if (ACPSupported) {
                enableACP();
            }
            else {
                disableACP();
            }
        }
        else {
            if (CaptainJack) { chkShowECM.setEnabled(true); }
            disableACP();
        }
    }
    
    private void enableScramblingKey1() {
        lblScramblingKey.setEnabled(true);
        cmbScramblingKey1.setEnabled(true);
    }
    
    private void disableScramblingKey1() {
        lblScramblingKey.setEnabled(false);
        cmbScramblingKey1.setEnabled(false);
    }
    
    private void enableScramblingKey2() {
        lblVC2ScramblingKey.setEnabled(true);
        cmbScramblingKey2.setEnabled(true);
    }
    
    private void disableScramblingKey2() {
        cmbScramblingKey2.setEnabled(false);
        cmbScramblingKey2.setSelectedIndex(-1);
        lblVC2ScramblingKey.setEnabled(false);
    }
    
    private void enableWSS() {
        chkWSS.setEnabled(true);
    }
    
    private void disableWSS() {
        if (chkWSS.isSelected()) { chkWSS.doClick(); }
        chkWSS.setEnabled(false);
    }
    
    private void addWSSModes() {
        String[] WSSMode = {
            "auto",
            "4:3",
            "16:9",
            "14:9 letterbox",
            "16:9 letterbox"
        };
        WSSModeArray = new String[] {
            "auto",
            "4:3",
            "16:9",
            "14:9-letterbox",
            "16:9-letterbox"
        };
        cmbWSS.removeAllItems();
        cmbWSS.setModel(new DefaultComboBoxModel<>(WSSMode));
        cmbWSS.setSelectedIndex(-1);
    }
    
    private ArrayList<String> checkWSS() {
        // Populate WSS parameters if enabled
        var al = new ArrayList<String>();
        if (chkWSS.isSelected()) {
            al.add("--wss");
            al.add(WSSModeArray[cmbWSS.getSelectedIndex()]);
        }
        return al;
    }
    
    private void addARCorrectionOptions() {
        String[] ARCorrectionMode = {
            "Stretched",
            "Letterboxed",
            "Cropped"
        };
        cmbARCorrection.removeAllItems();
        cmbARCorrection.setModel(new DefaultComboBoxModel<>(ARCorrectionMode));
        cmbARCorrection.setSelectedIndex(0);
    }
    
    private ArrayList<String> checkARCorrectionOptions() {
        var al = new ArrayList<String>();
        if (chkARCorrection.isSelected()) {
            switch (cmbARCorrection.getSelectedIndex()) {
                case 1:
                    al.add("--letterbox");
                    break;
                case 2:
                    al.add("--pillarbox");
                    break;
                default:
                    break;
            }
        }
        return al;
    }
    
    private void addLogoOptions() {
        // Extract (from ModesFile) the logo list
        String logos = INI.splitINIfile(ModesFile, "logos");
        if (logos == null) {
            // If nothing was found, disable the logo options and stop
            if (chkLogo.isSelected()) chkLogo.doClick();
            chkLogo.setEnabled(false);
            return;
        }
        // We just want the commands so remove everything after =
        logos = logos.replaceAll("\\=.*", "");       
        // Remove commented out lines
        logos = Stream.of(logos.split("\n"))
                .filter(f -> !f.contains(";"))
                .collect(Collectors.joining("\n"));     
        // Add a headerless string to LogoArray by splitting off the first line
        LogoArray = logos.substring(logos.indexOf("\n") +1).split("\\r?\\n");
        // Populate LogoNames by reading ModesFile using what we added
        // to LogoArray.
        String[] LogoNames = new String[LogoArray.length];
        for (int i = 0; i < LogoArray.length; i++) {
            LogoNames[i] = (INI.getStringFromINI(ModesFile, "logos", LogoArray[i], "", true));
        }
        cmbLogo.removeAllItems();
        cmbLogo.setModel(new DefaultComboBoxModel<>(LogoNames));
        if (!chkLogo.isSelected()) cmbLogo.setSelectedIndex(-1);
    }
    
    private ArrayList<String> checkLogo() {
        var al = new ArrayList<String>();
        // Populate logo parameters if enabled
        if (chkLogo.isSelected()) {
            al.add("--logo");
            al.add(LogoArray[cmbLogo.getSelectedIndex()]);
        }
        return al;
    }
    
    private void addTestCardOptions() {
        String testcards;
        // Backwards compatibility. [testcards] refers to 625 line cards.
        // So we set l to a blank string on 625, otherwise we populate it with
        // the line count.
        // e.g, if l is set to 525 we'll query Modes.ini for [testcards525].
        String l = new String();
        if (Lines != 625) l = Integer.toString(Lines);
        // Extract (from ModesFile) the test card list
        testcards = INI.splitINIfile(ModesFile, "testcards" + l);
        if (testcards == null) {
            // If nothing was found, disable the test card combobox
            // Use a dummy string to preserve the length of the combobox
            // This won't be seen as the combobox is disabled
            String[] TCNames = {"No items found"};
            cmbTest.setEnabled(false);
            cmbTest.removeAllItems();
            cmbTest.setModel(new DefaultComboBoxModel<>(TCNames));
            cmbTest.setSelectedIndex(-1);
        }
        else {
            // We just want the commands so remove everything after =
            testcards = testcards.replaceAll("\\=.*", "");
            // Add a headerless string to TCArray by splitting off the first line
            TCArray = testcards.substring(testcards.indexOf("\n") +1).split("\\r?\\n");
            // Populate TCNames by reading ModesFile using what we added
            // to TCArray.
            String[] TCNames = new String[TCArray.length];
            for (int i = 0; i < TCArray.length; i++) {
                TCNames[i] = INI.getStringFromINI(ModesFile, "testcards" + l, TCArray[i], "", true);
            }
            cmbTest.removeAllItems();
            cmbTest.setModel(new DefaultComboBoxModel<>(TCNames));
            if (!radTest.isSelected()) {
                cmbTest.setSelectedIndex(-1);
            }
            else {
                cmbTest.setSelectedIndex(0);
            }        
        }
    }
    
    private ArrayList<String> checkTestCard() {
        var al = new ArrayList<String>();
        if (cmbTest.isEnabled()) {
            al.add("test:" + TCArray[cmbTest.getSelectedIndex()]);
        }
        else if (radTest.isSelected()) {
            al.add("test:colourbars");
        }
        return al;
    }
    
    private void addOutputDevices() {
        String[] od = {
            "HackRF",
            "SoapySDR",
            "FL2000",
            "File"
        };
        cmbOutputDevice.removeAllItems();
        cmbOutputDevice.setModel(new DefaultComboBoxModel<>(od));
        cmbOutputDevice.setSelectedIndex(0);
    }
    
    private void enableRFOptions() {
        txtGain.setText("0");
        txtGain.setEnabled(true);
        lblGain.setEnabled(true);
        radCustom.setEnabled(true);
        txtFrequency.setEnabled(true);
        txtFrequency.setEditable(false);
        lblChannel.setEnabled(true);
        lblFrequency.setEnabled(true);
        rfPanel.setEnabled(true);
    }
    
    private void disableRFOptions() {
        txtGain.setText("");
        txtGain.setEnabled(false);
        lblGain.setEnabled(false);
        radUHF.setEnabled(false);
        radVHF.setEnabled(false);
        radCustom.setEnabled(false);
        BandButtonGroup.clearSelection();
        cmbChannel.setEnabled(false);
        cmbChannel.setSelectedIndex(-1);
        lblChannel.setEnabled(false);
        lblFrequency.setEnabled(false);
        txtFrequency.setText("");
        txtFrequency.setEnabled(false);
        cmbRegion.setEnabled(false);
        cmbRegion.removeAllItems();
        // Add a blank item to prevent the combobox from enlarging on some L&Fs
        cmbRegion.addItem("");
        if (chkAmp.isSelected()) chkAmp.doClick();
        chkAmp.setEnabled(false);
        lblAntennaName.setEnabled(false);
        txtAntennaName.setText("");
        txtAntennaName.setEnabled(false);
    }
    
    private void checkMode() {
        // Here, we read the selected combobox index and use that number to get
        // the corresponding video format from the required mode array.
        if (radPAL.isSelected()) {
            Mode = PALModeArray[cmbMode.getSelectedIndex()];
        }
        else if (radNTSC.isSelected()) {
            Mode = NTSCModeArray[cmbMode.getSelectedIndex()];
        }
        else if (radSECAM.isSelected()) {
            Mode = SECAMModeArray[cmbMode.getSelectedIndex()];
        }
        else if (radBW.isSelected()) {
            Mode = OtherModeArray[cmbMode.getSelectedIndex()];
        }
        else if (radMAC.isSelected()) {
            Mode = MACModeArray[cmbMode.getSelectedIndex()];
        }
        // Save the line count from the previously selected mode
        int oldLines = Lines;
        // Start reading the section we found above, starting with line count
        if (INI.getIntegerFromINI(ModesFile, Mode, "lines") != null) {
            Lines = INI.getIntegerFromINI(ModesFile, Mode, "lines");
        }
        else {
            messageBox("Unable to read the \"lines\" value for this mode. "
                    + "Defaulting to 525.", JOptionPane.WARNING_MESSAGE);
            if (cmbMode.getItemCount() > 1) {
                cmbMode.setSelectedIndex(PreviousIndex);
            }
            else {
                // Default to 525 lines so we don't enable 625-specific stuff
                Lines = 525;
            }
        }
        switch (INI.getStringFromINI(ModesFile, Mode, "modulation", "", false)) {
            case "vsb":
                if (!chkVideoFilter.isEnabled()) chkVideoFilter.setEnabled(true);
                disableFMDeviation();
                break;
            case "fm":
                if (!chkVideoFilter.isEnabled()) chkVideoFilter.setEnabled(true);
                enableFMDeviation();
                break;
            case "baseband":
                if (!checkBasebandSupport()) return;
                break;
            default:
                messageBox("No modulation specified, defaulting to VSB.", JOptionPane.INFORMATION_MESSAGE);
                if (!chkVideoFilter.isEnabled()) chkVideoFilter.setEnabled(true);
                disableFMDeviation();
                break;
        }
        if (INI.getDoubleFromINI(ModesFile, Mode, "sr") != null) {
            DefaultSampleRate = Double.toString(INI.getDoubleFromINI(ModesFile, Mode, "sr") / 1000000).replace(".0", "");
        }
        else {
            messageBox("No sample rate specified, defaulting to 16 MHz.", JOptionPane.INFORMATION_MESSAGE);
            DefaultSampleRate = "16";
        }
        if (INI.getBooleanFromINI(ModesFile, Mode, "colour")) {
            enableColourControl();
        }
        else {
            disableColourControl();
        }
        if (INI.getBooleanFromINI(ModesFile, Mode, "audio")) {
            enableAudioOption();
        }
        else {
            disableAudioOption();
        }
        if (INI.getBooleanFromINI(ModesFile, Mode, "nicam")) {
            enableNICAM();
        }
        else {
            disableNICAM();
        }
        if (INI.getBooleanFromINI(ModesFile, Mode, "a2stereo")) {
            enableA2Stereo();
        }
        else {
            disableA2Stereo();
        }
        if (INI.getBooleanFromINI(ModesFile, Mode, "teletext")) {
            enableTeletext();
        }
        else {
            disableTeletext();
        }
        if (INI.getBooleanFromINI(ModesFile, Mode, "wss")) {
            enableWSS();
        }
        else {
            disableWSS();
        }
        if (INI.getBooleanFromINI(ModesFile, Mode, "vits")) {
            enableVITS();
        }
        else {
            disableVITS();
        }
        if ((Lines == 625) || (Lines == 525)) {
            chkVITC.setEnabled(true);
        }
        else {
            if (chkVITC.isSelected()) chkVITC.doClick();
            chkVITC.setEnabled(false);
        }
        if (INI.getBooleanFromINI(ModesFile, Mode, "acp")) {
            ACPSupported = true;
            enableACP();
        }
        else {
            ACPSupported = false;
            disableACP();
        }
        if (INI.getBooleanFromINI(ModesFile, Mode, "scrambling")) {
            enableScrambling();
            if ((radPAL.isSelected()) || radSECAM.isSelected() ) {
                add625ScramblingTypes();
            }
            else if (radMAC.isSelected()) {
                addMACScramblingTypes();
            }
        }
        else {
            disableScrambling();
        }
        if (radMAC.isSelected()) {
            configureMacPanel(true);
            if (chkMacChId.isSelected()) chkMacChId.doClick();
            chkAudio.setEnabled(false);
        }
        else {
            configureMacPanel(false);
        }
        // Check if the line count varies from the previous mode
        // If so, refresh the available test cards
        if (oldLines != Lines) addTestCardOptions();
        // Check for UHF and VHF bandplans
        // We now support up to five bandplans per band. uhf and vhf are the
        // default and these names are retained for backwards compatibility.
        // Additional bandplans can be added from uhf2 to uhf5, or vhf2 to vhf5.
        UhfAL = new ArrayList<>();
        VhfAL = new ArrayList<>();
        for (int i = 0; i <=5; i++) {
            // The string below is merged below to find uhf2-5
            String s;
            switch (i) {
                case 0:
                    // There's no uhf0 setting so pass an empty string
                    s = "";
                    break;
                case 1:
                    // There's no uhf1 setting so skip this
                    continue;
                default:
                    s = Integer.toString(i);
                    break;
            }
            String u = INI.getStringFromINI(ModesFile, Mode, "uhf" + s, "0", false);
            if (!u.equals("0")) {
                UhfAL.add(u);
            }
            else {
                break;
            }
        }
        for (int j = 0; j <=5; j++) {
            // The string below is merged below to find vhf2-vhf5
            String t;
            switch (j) {
                case 0:
                    // There's no vhf0 setting, so pass an empty string
                    t = "";
                    break;
                case 1:
                    // There's no vhf1 setting, so skip this
                    continue;
                default:
                    t = Integer.toString(j);
                    break;
            }
            String v = INI.getStringFromINI(ModesFile, Mode, "vhf" + t, "0", false);
            if (!v.equals("0")) {
                VhfAL.add(v);
            }
            else {
                break;
            }
        }
        if (UhfAL.isEmpty()) {
            disableUHF();
        }
        else {
            enableUHF();
        }
        if (VhfAL.isEmpty()) {
            disableVHF();
        }
        else {
            enableVHF();
        }
        // Check if UHF or VHF are enabled. If so, select one (UHF first).
        // If neither are enabled, select the Custom option.
        if (radUHF.isEnabled()) {
            radUHF.doClick();
        }
        else if (radVHF.isEnabled()) {
            radVHF.doClick();
        }
        else {
            radCustom.doClick();
        }
}
    
    private void enableAudioOption() {
        chkAudio.setEnabled(true);
        if (!chkAudio.isSelected()) chkAudio.doClick();
    }
    
    private void disableAudioOption() {
        chkAudio.setSelected(false);
        chkAudio.setEnabled(false);
    }
    
    private boolean checkBasebandSupport() {
    // Check if the selected output device supports baseband modes or not
        if ( (cmbOutputDevice.getSelectedIndex() == 2) ||
                (cmbOutputDevice.getSelectedIndex() == 3) ) {
            disableRFOptions();
            if (chkVideoFilter.isSelected()) chkVideoFilter.doClick();
            chkVideoFilter.setEnabled(false);
            return true;
        }
        else {
            messageBox("This mode is not supported by the selected output device.", JOptionPane.WARNING_MESSAGE);
            cmbMode.setSelectedIndex(PreviousIndex);
            checkMode();
            return false;
        }
    }
    
    private void enableUHF() {
        // Only enable if output device is hackrf or soapysdr
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            if (!radCustom.isEnabled()) enableRFOptions();
            radUHF.setEnabled(true);
        }
    }
    
    private void enableVHF() {
        // Only enable if output device is hackrf or soapysdr
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            if (!radCustom.isEnabled()) enableRFOptions();
            radVHF.setEnabled(true);
        }
    }    

    private void disableUHF() {
        radUHF.setEnabled(false);
    }
    
    private void disableVHF() {
        radVHF.setEnabled(false);
    }
    
    private void enableACP() {
        chkACP.setEnabled(true);
    }
    
    private void disableACP() {
        if (chkACP.isSelected()) {
            chkACP.doClick();
        }
        chkACP.setEnabled(false);
    }    
    
    private void enableVITS() {
        chkVITS.setEnabled(true);
    }
    
    private void disableVITS() {
        if (chkVITS.isSelected()) {
            chkVITS.doClick();
        }
        chkVITS.setEnabled(false);
    }
    
    private void enableTeletext() {
        chkTeletext.setEnabled(true);
        teletextPanel.setEnabled(true);
        if ((CaptainJack) && (radLocalSource.isSelected())) {
            chkTextSubtitles.setEnabled(true);
        }
    }  
    
    private void disableTeletext() {
        if (chkTeletext.isSelected()) {
            chkTeletext.doClick();
        } 
        chkTeletext.setEnabled(false);
        teletextPanel.setEnabled(false);
        if (chkTextSubtitles.isSelected()) chkTextSubtitles.doClick();
        chkTextSubtitles.setEnabled(false);
    }
    
    private ArrayList<String> checkTeletextSource() {
        var al = new ArrayList<String>();
        if (chkTeletext.isSelected()) {
            al.add("--teletext");
            // If the txtTeletextSource field contains quotes, remove them
            if ((txtTeletextSource.getText()).contains(String.valueOf((char)34))) {
                txtTeletextSource.setText(txtTeletextSource.getText().replaceAll(String.valueOf((char)34), ""));
            }
            if ((txtTeletextSource.getText()).isEmpty()) {
                // Create a temp directory if it does not exist
                createTempDirectory();
                // Copy the demo page resource to the temp directory
                try {
                    SharedInst.copyResource("/com/steeviebops/resources/demo.tti", TempDir.toString() + "/demo.tti", this.getClass());   
                    if ( (RunningOnWindows) && TempDir.toString().contains(" ") ) {
                        al.add('\u0022' + TempDir.toString() + File.separator + "demo.tti" + '\u0022');
                    }
                    else {
                        al.add(TempDir.toString() + File.separator + "demo.tti");
                    }
                } catch (IOException ex) {
                    System.err.println("An error occurred while attempting to copy to the temp directory: " + ex);
                    return null;
                }
            }
            else if ( (txtTeletextSource.getText().toLowerCase().endsWith(".t42")) 
                    && (RunningOnWindows) && (txtTeletextSource.getText().contains(" ")) ) {
                al.add("raw:" + '\u0022' + txtTeletextSource.getText() + '\u0022');
            }
            else if (txtTeletextSource.getText().toLowerCase().endsWith(".t42")) {
                al.add("raw:" + txtTeletextSource.getText());
            }
            else if ( (RunningOnWindows) && (txtTeletextSource.getText().contains(" ")) ) {
                al.add('\u0022' + txtTeletextSource.getText() + '\u0022');
            }
            else {
                al.add(txtTeletextSource.getText());
            }
            if ( (chkTextSubtitles.isSelected()) && (!txtTeletextSource.getText().isBlank()) ) {
                String p888err = "This directory contains a teletext file (P888.tti) for page 888. "
                        + "This could cause hacktv to crash when teletext subtitles are enabled. "
                        + "Please move or delete this file and try again.";
                String p888warn = "This directory contains teletext files in the page 800 range. "
                        + "This could cause subtitles to be unreliable. Please move these files "
                        + "if you encounter problems.";
                // If the teletext source is set to SPARK with subtitles enabled, delete their page 888 to avoid issues
                if ( (TempDir != null) && (txtTeletextSource.getText().contains(TempDir + File.separator + "spark")) ) {
                    if ( (Files.exists(Path.of(TempDir + "/spark/P888.tti"))) || (Files.exists(Path.of(TempDir + "/spark/p888.tti"))) ) {
                        try {
                            SharedInst.deleteFSObject(Path.of(TempDir + "/spark/P888.tti"));
                        }
                        catch (IOException ex) {
                            messageBox(p888err, JOptionPane.ERROR_MESSAGE);
                            return null;
                        }                    
                    }
                }
                // If the teletext source contains a P888.tti file, abort because hacktv will crash.
                // The latter two if statements are to prevent a NPE if an absolute path is specified.
                else if ( (Files.exists(Path.of(txtTeletextSource.getText() + "/P888.tti"))) || 
                        (txtTeletextSource.getText().toLowerCase().endsWith("p888.tti")) ||
                        (txtTeletextSource.getText().toLowerCase().endsWith("p888.ttix")) ) {
                    messageBox(p888err, JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                // If the directory contains any text files in the page 800 range (p8*.tti or p8*.ttix)
                // generate a warning because this can prevent subtitles from running in real time.
                if ( (SharedInst.wildcardFind(txtTeletextSource.getText(), "p8", ".tti") > 0) || 
                        (SharedInst.wildcardFind(txtTeletextSource.getText(), "p8", ".ttix") > 0) ) {
                    messageBox(p888warn, JOptionPane.WARNING_MESSAGE);
                    return null;
                }
            }
        }
        return al;
    }
    
    private void configureMacPanel(boolean e) {
        // Enable or disable MAC video-related options based on the boolean
        // sent to this method
        if (e) {
            radMacStereo.setSelected(true);
            radMac32k.setSelected(true);
            radMacCompanded.setSelected(true);
            radMacL1.setSelected(true);
        }
        else {
            if (chkMacChId.isSelected()) chkMacChId.doClick();
            MacStereoButtonGroup.clearSelection();
            MacSRButtonGroup.clearSelection();
            MacCompressionButtonGroup.clearSelection();
            MacProtectionButtonGroup.clearSelection();
        }
        macPanel.setEnabled(e);
        chkMacChId.setEnabled(e);
        lblMacAudioMode.setEnabled(e);
        radMacStereo.setEnabled(e);
        radMacMono.setEnabled(e);
        lblMacSampleRate.setEnabled(e);
        radMac32k.setEnabled(e);
        radMac16k.setEnabled(e);
        lblMacCompression.setEnabled(e);
        radMacCompanded.setEnabled(e);
        radMacLinear.setEnabled(e);
        lblMacProtection.setEnabled(e);
        radMacL1.setEnabled(e);
        radMacL2.setEnabled(e);
    }
    
    private void enableNICAM() {
        if (chkAudio.isSelected()) {
            chkNICAM.setEnabled(true);
            if (!chkNICAM.isSelected()) chkNICAM.doClick();
            NICAMSupported = true;
        }
    }
       
    private void disableNICAM() {
        chkNICAM.setEnabled(false);
        chkNICAM.setSelected(false);
        NICAMSupported = false;
    }
    
    private void enableA2Stereo() {
        A2Supported = true;
        if (chkAudio.isSelected()) {
            chkA2Stereo.setEnabled(true);
        }
        if (!chkNICAM.isEnabled()) {
            chkA2Stereo.doClick();
        }
    }
       
    private void disableA2Stereo() {
        A2Supported = false;
        chkA2Stereo.setEnabled(false);
        chkA2Stereo.setSelected(false);
    }    
    
    private void enableFMDeviation() {
        chkFMDev.setEnabled(true);
        // The --filter parameter enables VSB filtering on AM, or CCIR-405 FM 
        // pre-emphasis filtering on FM, so change the Filter checkbox
        // description to suit  
        chkVideoFilter.setText("FM video pre-emphasis filter");
    }
    
    private void disableFMDeviation() {
        if (chkFMDev.isSelected()) chkFMDev.doClick();
        chkFMDev.setEnabled(false);
        txtFMDev.setText("");
        txtFMDev.setEnabled(false);
        if (chkVideoFilter.isSelected()) {
            chkVideoFilter.setSelected(false);
            txtSampleRate.setText(DefaultSampleRate);
        }
        // Revert Filter checkbox name to VSB-AM
        chkVideoFilter.setText("VSB-AM filter");
    }
    
    private void populateBandPlan(String band) {
        txtFrequency.setEditable(false);
        try {
            // Get the bandplan list from the requested video mode and band
            String bpname = INI.getStringFromINI(ModesFile, Mode, band, "", false);
            // Extract (from ModesFile) the bandplan section that we need
            String bp = INI.splitINIfile(ModesFile, bpname);
            if (bp == null) {
                 messageBox(band + " was not found in modes.ini", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            // We just want the channel names/numbers so remove everything after =
            bp = bp.replaceAll("\\=.*", "");       
            // Remove commented out lines
            bp = Stream.of(bp.split("\n"))
                    .filter(f -> !f.contains(";"))
                    .collect(Collectors.joining("\n"));
            // Remove region identifier line and chid line if they exist
            bp = Stream.of(bp.split("\n"))
                    .filter(g -> !g.contains("region"))
                    .collect(Collectors.joining("\n"));
            bp = Stream.of(bp.split("\n"))
                    .filter(g -> !g.contains("chid"))
                    .collect(Collectors.joining("\n"));
            // Add a headerless string to ChannelArray by splitting off the first line
            ChannelArray = bp.substring(bp.indexOf("\n") +1).split("\\r?\\n");
            // Populate FrequencyArray by reading ModesFile using what we added
            // to ChannelArray.
            FrequencyArray = new long[ChannelArray.length];
            for (int i = 0; i < ChannelArray.length; i++) {
                if (INI.getLongFromINI(ModesFile,  bpname, ChannelArray[i]) != null) {
                    FrequencyArray[i] = INI.getLongFromINI(ModesFile,  bpname, ChannelArray[i]);
                }
                else {
                     messageBox("Invalid data returned from Modes.ini, section name: "
                             + bpname, JOptionPane.ERROR_MESSAGE);
                     return;
                }
            }
            // Enable cmbChannel and populate it with the contents of ChannelArray
            cmbChannel.setEnabled(true);       
            cmbChannel.removeAllItems();
            cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
            cmbChannel.setSelectedIndex(0);  
        }
        catch (IllegalArgumentException ex) {
            System.err.println(ex);
            messageBox("The bandplan data in Modes.ini appears to be "
                    + "missing or corrupt for the selected band.", JOptionPane.WARNING_MESSAGE);
            radCustom.doClick();
            // Disable the band that failed
            if (band.startsWith("uhf")) {
                radUHF.setEnabled(false);
            }
            else if (band.startsWith("vhf")) {
                radVHF.setEnabled(false);
            }
        }
    }
    
    private void youtubedl(String input) {
        // yt-dlp frontend. Pass the download URL as a string.
        // youtube-dl is no longer supported
        int q = JOptionPane.showConfirmDialog(null, "We will now attempt to use"
                + " yt-dlp to stream the requested video.\n" +
            "Do you wish to continue?", APP_NAME, JOptionPane.YES_NO_OPTION);
        if (q == JOptionPane.YES_OPTION) {
            final String ytp;
            String url;
            if (RunningOnWindows) {
                ytp = "yt-dlp.exe";
            }
            else {
                ytp = "yt-dlp";
            }
            // Remove the ytdl: prefix if specified
            if (input.toLowerCase().startsWith("ytdl:")) {
                url = input.substring(5);
            } 
            else {
                url = input;
            }
            chkSyntaxOnly.setEnabled(false);
            btnRun.setEnabled(false);
            txtStatus.setText("Checking URL, please wait...");
            // Check if the provided URL is a live stream or not
            SwingWorker <String, Void> checkYTDL = new SwingWorker <String, Void> () {
                @Override
                protected String doInBackground() throws Exception {
                    ProcessBuilder yt = new ProcessBuilder(ytp, "-g", url);
                    yt.redirectErrorStream(true);
                    String f = null;
                    // Try to start the process
                    try {
                        Process pr = yt.start();
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()))) {
                            // Capture the output
                            String a;
                            while ((a = br.readLine()) != null) {
                                f = a;
                            }
                        }
                    }
                    catch (IOException ex) {
                        return "";
                    }
                    return f;
                }
                @Override
                protected void done() {
                    String u;
                    try {
                        u = get();
                    }
                    catch (InterruptedException | ExecutionException e) {
                        return;
                    }
                    // If it's a live stream, set the manifest (m3u8) URL as the
                    // source and restart. We don't need yt-dlp for this.
                    if ( (u != null) && (u.endsWith(".m3u8")) ) {
                        txtSource.setText(u);
                        populateArguments("");
                    }
                    else {
                        populateArguments(ytp);
                    }
                    btnRun.setEnabled(true);
                }
            };
            checkYTDL.execute();
        }
    }

    private String checkInput() {
        // Skip this method if the playlist is populated
        if (PlaylistAL.size() > 0) return "";
        if (radLocalSource.isSelected()) {
            if (cmbM3USource.isVisible()) {
                return PlaylistURLsAL.get(cmbM3USource.getSelectedIndex());
            }
            else if ( (txtSource.getText().contains("://youtube.com/")) ||
                      (txtSource.getText().contains("://www.youtube.com/")) ||
                      (txtSource.getText().contains("://youtu.be/")) ||
                      (txtSource.getText().startsWith("ytdl:")) ) {
                // Invoke the yt-dlp handler
                if (!chkSyntaxOnly.isSelected()) {
                    youtubedl(txtSource.getText());
                }
                else {
                    messageBox("yt-dlp is not supported in syntax only mode.", JOptionPane.WARNING_MESSAGE);
                }
                // Return null as we're going to restart if the download is successful
                return null;
            }
            else if (!txtSource.getText().isBlank()) { 
                return txtSource.getText().replace("\"", "");
            }
            else {
                tabPane.setSelectedIndex(0);
                 messageBox("Please specify an input file to broadcast or choose the test card option.", JOptionPane.WARNING_MESSAGE);
                 return null;
            }
        }
        else {
            return "";
        }
    }
    
    private Integer checkSampleRate() {
        if (SharedInst.isNumeric( txtSampleRate.getText())) {
            Double SR = Double.parseDouble(txtSampleRate.getText());
            return (int) (SR * 1000000);
        }
        else {
            tabPane.setSelectedIndex(1);
            messageBox("Please specify a valid sample rate in MHz.", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }
    
    private ArrayList<String> checkPixelRate() {
        var al = new ArrayList<String>();
        if (chkPixelRate.isSelected()) {
            try {
                Double PR = Double.parseDouble(txtPixelRate.getText());
                int PixelRate = (int) (PR * 1000000);
                al.add("--pixelrate");
                al.add(Integer.toString(PixelRate));
            }
            catch (NumberFormatException nfe) {
                tabPane.setSelectedIndex(1);
                messageBox("Please specify a valid pixel rate in MHz.", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        return al;
    }
   
    private ArrayList<String> checkFMDeviation() {
        var al = new ArrayList<String>();
        if (chkFMDev.isSelected()) {
            if (SharedInst.isNumeric(txtFMDev.getText())) {
                al.add("--deviation");
                Double d = Double.parseDouble(txtFMDev.getText());
                int i = (int) (d * 1000000);
                al.add(Integer.toString(i));
            }
            else {
                tabPane.setSelectedIndex(1);
                messageBox("Please specify a valid deviation in MHz.", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        return al;
    }
    
    private boolean checkCustomFrequency(){
        // This method is only required for custom frequencies, so we skip it
        // and return true if the custom radio button is not selected
        if (radCustom.isSelected()) {
            BigDecimal CustomFreq;
            BigDecimal Multiplier = new BigDecimal(1000000);
            String InvalidInput = "Please specify a frequency between 1 MHz and 7250 MHz.";
            if ( (SharedInst.isNumeric( txtFrequency.getText())) && (!txtFrequency.getText().contains(" ")) ){
                CustomFreq = new BigDecimal(txtFrequency.getText());
                if ( (CustomFreq.longValue() < 1) || (CustomFreq.longValue() > 7250) ) {
                    messageBox(InvalidInput, JOptionPane.WARNING_MESSAGE);
                    tabPane.setSelectedIndex(1);
                    return false;
                }
                else {
                    // Multiply the big decimal by 1,000,000 to get the frequency in Hz.
                    // Then set the Frequency variable to the long value of the BigDecimal.
                    CustomFreq = CustomFreq.multiply(Multiplier);
                    Frequency = CustomFreq.longValue();
                    return true;
                }
            }
            else {
                messageBox(InvalidInput, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(1);
                return false;  
            }
        }
        return true;
    }
    
    private ArrayList<String> checkMacChId() {
        var al = new ArrayList<String>();
        if (chkMacChId.isSelected()) {
            if (txtMacChId.getText().matches("^[0-9a-fA-F]+$")) {
                al.add("--chid");
                al.add("0x" + txtMacChId.getText());
            }
            else {
                tabPane.setSelectedIndex(1);
                messageBox("Please specify a valid hexadecimal channel ID.", JOptionPane.WARNING_MESSAGE);
                return null;
            }            
        }
        return al;
    }
    
    private ArrayList<String> checkGamma() {
        var al = new ArrayList<String>();
        if (chkGamma.isSelected()) {
            if (SharedInst.isNumeric(txtGamma.getText())) {
                al.add("--gamma");
                al.add(txtGamma.getText());
            }
            else {    
                tabPane.setSelectedIndex(2);
                messageBox("Gamma should be a decimal value.", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        return al;
    }
    
    private ArrayList<String> checkOutputLevel() {
        var al = new ArrayList<String>();
        if (chkOutputLevel.isSelected()) {
            if (SharedInst.isNumeric(txtOutputLevel.getText())) {
                al.add("--level");
                al.add(txtOutputLevel.getText());
            }
            else {
                tabPane.setSelectedIndex(2);
                messageBox("Output level should be a decimal value.", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        return al;
    }
    
    private ArrayList<String> checkPosition() {
        var al = new ArrayList<String>();
        if (chkPosition.isSelected()) {
            if (!SharedInst.isNumeric(txtPosition.getText())) {
                tabPane.setSelectedIndex(0);
                messageBox("Please specify a valid position.", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            else {
                al.add("--position");
                al.add(txtPosition.getText());     
            }
        }
        return al;
    }
    
    private ArrayList<String> checkGain() {
        var al = new ArrayList<String>();
        String InvalidGain = "Gain should be between 0 and 47 dB.";
        if (SharedInst.isNumeric(txtGain.getText())) {
            int g = Integer.parseInt(txtGain.getText());
            if ( (g >= 0) && (g <= 47) ) {
                al.add("-g");
                al.add(txtGain.getText());
            }
            else {
                messageBox(InvalidGain, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(1);
                return null;
            }
        }
        else {
            messageBox(InvalidGain, JOptionPane.WARNING_MESSAGE);
            tabPane.setSelectedIndex(1);
            return null;
        }
        return al;
    }
    
    private String checkCardNumber() {
        /*  Sky viewing cards use the Luhn algorithm to verify if the card
         *  number is valid. So we will use it here too.
         *  Issue 07 cards have either 13-digit or 9-digit numbers.
         *  Issue 09 cards are 9-digit only. So we restrict input to these lengths.
         *  If an 8-digit number is entered, this is passed to hacktv without
         *  any checks.
         */
        if ( (txtCardNumber.isEnabled()) && (ScramblingType1.equals("--videocrypt")) ) {
            String TruncatedCardNumber;
            String LuhnCheckFailed = "Card number appears to be invalid (Luhn check failed).";
            String InvalidCardNumber = "Card number should be exactly 8, 9 or 13 digits.";
            // Make sure that the input is numeric only
            if (!SharedInst.isNumeric(txtCardNumber.getText())) {
                 messageBox(InvalidCardNumber, JOptionPane.WARNING_MESSAGE);
                 return null;
            }
            else if (txtCardNumber.getText().length() == 9) {
                // Do a Luhn check on the provided card number
                if (!SharedInst.LuhnCheck(Long.parseLong(txtCardNumber.getText()))) {
                    messageBox(LuhnCheckFailed, JOptionPane.WARNING_MESSAGE);
                    return null;
                }
                else {
                    // Make sure that we're not trying to send EMMs to the wrong card type.
                    if (!checkEMMCardType(txtCardNumber.getText())) return null;
                    // hacktv doesn't use the check digit so strip it out
                    return txtCardNumber.getText().substring(0,8);
                }
            }
            else if (txtCardNumber.getText().length() == 13) {
                // Only digits 4-13 of 13-digit card numbers are checked, so we
                // need to strip out the first four digits.
                TruncatedCardNumber = txtCardNumber.getText().substring(4,13);
                if (!SharedInst.LuhnCheck(Long.parseLong(TruncatedCardNumber))) {
                    messageBox(LuhnCheckFailed, JOptionPane.WARNING_MESSAGE);
                    return null;
                }
                else {
                    // Make sure that we're not trying to send EMMs to the wrong card type.
                    if (!checkEMMCardType(TruncatedCardNumber)) return null;
                    // hacktv doesn't use the check digit so strip it out
                    return txtCardNumber.getText().substring(4,12);
                }
            }
            else if (txtCardNumber.getText().length() == 8) {
                // Make sure that we're not trying to send EMMs to the wrong card type.
                if (!checkEMMCardType(txtCardNumber.getText())) return null;
                // Pass the digits unaltered and without Luhn checking
                return txtCardNumber.getText();
            }
            else {
                tabPane.setSelectedIndex(4);
                messageBox(InvalidCardNumber, JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        else if ( (txtCardNumber.isEnabled()) && (ScramblingType1.equals("--videocrypt2")) ) {
            // Pass the digits unaltered and without Luhn checking for MultiChoice cards
            // This is temporary until I work out how to handle them
            return txtCardNumber.getText();
        }
        else {
            // If the txtCardNumber textbox is disabled, return null and exit
            return null;
        }
    }
    
    private boolean checkEMMCardType(String cardNumber) {
        // Make sure that we're not trying to send EMMs to the wrong card type.
        // Used info from settopbox.org to get a rough idea of the range and
        // make an educated guess based on that information.
        // If you have a legitimate card that fails this check, let me know.
        String WrongCardType = "The card number you entered appears to be for a different issue.\n"
                + "Using EMMs on the wrong card type may irreparably damage the card.";
        switch (ScramblingKey1) {
            case "sky07":
                short s7 = Short.parseShort(cardNumber.substring(0,3));
                if ((s7 > 30) && (s7 < 800)) {
                    messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    return true;
                }
            case "sky09":
                short s9 = Short.parseShort(cardNumber.substring(0,3));
                if ((s9 < 190) || (s9 > 250)) {
                    messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    return true;
                }
            default:
                return true;
        }
    }
    
    private void showEMMWarning() {
        if ( (!HTVLoadInProgress) && (!Prefs.get("SuppressWarnings", "0").equals("1")) ) {
            messageBox(
                "Care is advised when using this option.\n" +
                "Incorrect use may permanently damage the viewing card.\n" +
                "Do not use this option on an issue number other than the one selected.",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }
    
    private void checkTestCardStatus() {
        if ( (!cmbTest.isEnabled())
                && (CaptainJack)
                && (!HTVLoadInProgress)
                && cmbTest.getItemCount() > 1 ) {
            // Enable cmbTest (test card dropdown)
            cmbTest.setEnabled(true);
            cmbTest.setSelectedIndex(0);
        }
        else if (HTVLoadInProgress == true) {
            // Do nothing so we don't interrupt the file loading process
        }
        else if ((cmbTest.isEnabled()) && (CaptainJack) ) {
            // Do nothing if cmbTest is already enabled on a supported mode.
            // This prevents the test card from resetting back to bars when
            // changing video modes.
        }
        else {
            // Disable cmbTest
            cmbTest.setEnabled(false);
            cmbTest.setSelectedIndex(-1);
        }
    }
    
    private ArrayList<String> checkOutputDevice() {
        var al = new ArrayList<String>();
        switch (cmbOutputDevice.getSelectedIndex()) {
            case 3:
                // If File is selected, check if the path is blank
                if (txtOutputDevice.getText().isBlank()) {
                    tabPane.setSelectedIndex(1);
                     messageBox("Please select an output file or change the output device.",
                             JOptionPane.WARNING_MESSAGE);
                     return null;
                }
                // Do not allow file output to go to the console.
                // Bad things will happen, such as hanging the GUI and consuming large amounts of RAM!
                else if ( (txtOutputDevice.getText().equals("-")) ||
                        (txtOutputDevice.getText().equals("/dev/stdout")) ||
                        (txtOutputDevice.getText().equals("/dev/stderr")) ||
                        (txtOutputDevice.getText().toLowerCase().equals("con")) ) {
                     messageBox("Outputting to the console is not supported.",
                             JOptionPane.ERROR_MESSAGE);
                     return null;
                }
                else {
                    al.add("-o");
                    if ( (RunningOnWindows) && txtOutputDevice.getText().contains(" ") ) {
                        al.add('\u0022' + txtOutputDevice.getText() + '\u0022');
                    }
                    else {
                        al.add(txtOutputDevice.getText());
                    }
                    if (cmbFileType.getSelectedIndex() != 3) {
                        al.add ("-t");
                        al.add(cmbFileType.getItemAt(cmbFileType.getSelectedIndex()));
                    }
                }
                break;
            case 2:
                // fl2k
                al.add("-o");
                if (!txtOutputDevice.getText().isBlank()) {
                    al.add("fl2k:" + txtOutputDevice.getText());
                }
                else {
                    al.add("fl2k");
                }
                break;
            case 1:
                // SoapySDR
                al.add("-o");
                if (!txtOutputDevice.getText().isBlank()) {
                    al.add("soapysdr:" + txtOutputDevice.getText());
                }
                else {
                    al.add("soapysdr");
                }
                if (!txtAntennaName.getText().isBlank()) {
                    al.add("--antenna");
                    al.add(txtAntennaName.getText());
                }
                break;
            case 0:
                // HackRF
                if (!txtOutputDevice.getText().isBlank()) {
                    al.add("-o");
                    al.add("hackrf:" + txtOutputDevice.getText());
                }
                break;
            default:
                // This should never run
                break;
        }
        return al;
    }
    
    private ArrayList<String> checkVolume() {
        var al = new ArrayList<String>();
        // Only check volume if the option is enabled
        if (chkVolume.isSelected()) {
            if (SharedInst.isNumeric(txtVolume.getText())) {
                al.add("--volume");
                al.add(txtVolume.getText());
            }
            else {
                 messageBox("Volume should be a numeric or decimal value.", JOptionPane.WARNING_MESSAGE);
                 return null;
            }
        }
        return al;
    }
    
    private ArrayList<String> checkMacOptions() {
        // If any of these radio buttons are selected, return their arguments.
        // We don't need to return the arguments for the other options (such as
        // 32 kHz audio or stereo) because they're defaults anyway).
        var al = new ArrayList<String>();
        if (radMacMono.isSelected()) al.add("--mac-audio-mono");
        if (radMac16k.isSelected()) al.add("--mac-audio-medium-quality");
        if (radMacLinear.isSelected()) al.add("--mac-audio-linear");
        if (radMacL2.isSelected()) al.add("--mac-audio-l2-protection");
        return al;
    }
    
    private void populateArguments(String ytdl) {
        /* The ytdl parameter above is used to determine if this method was
           launched using the yt-dlp handler. If not blank, it will be used
           later to launch hacktv with the yt-dlp pipe creator. If blank, 
           hacktv is launched without it. */
        var allArgs = new ArrayList<String>();
        // hacktv path
        allArgs.add(HackTVPath);
        // Output device
        if (checkOutputDevice() != null) {
            allArgs.addAll(checkOutputDevice());
        }
        else {
            return;
        }
        // Video mode
        allArgs.add("-m");
        allArgs.add(Mode);
        // Only add frequency for HackRF or SoapySDR
        if ( (cmbOutputDevice.getSelectedIndex() == 0) ||
                (cmbOutputDevice.getSelectedIndex() == 1) ) {
            if (!checkCustomFrequency()) return;
            allArgs.add("-f");
            allArgs.add(Long.toString(Frequency));
        }
        // Add subtitles here, we need to make sure that subtitles is not the 
        // last parameter if no index is specified. Otherwise hacktv reports 
        // that no input has been specified. We do this by putting it before a
        // mandatory parameter, in this case sample rate.
        if (chkSubtitles.isSelected()) {
            allArgs.add("--subtitles");
            if (!txtSubtitleIndex.getText().isEmpty()) {
                allArgs.add(txtSubtitleIndex.getText());
            }
        }
        if (chkTextSubtitles.isSelected()) {
            allArgs.add("--tx-subtitles");
            if (!txtTextSubtitleIndex.getText().isEmpty()) {
                allArgs.add(txtTextSubtitleIndex.getText());
            }
        }
        // Sample rate
        allArgs.add("-s");
        if (checkSampleRate() != null) {
            allArgs.add(Integer.toString(checkSampleRate()));
        }
        else {
            return;
        }
        // Pixel rate
        if (checkPixelRate() != null) {
            allArgs.addAll(checkPixelRate());
        }
        else {
            return;
        }
        // Only add gain for HackRF or SoapySDR
        if (txtGain.isEnabled()) {
            if (checkGain() != null) {
                allArgs.addAll(checkGain());
            }
            else {
                return;
            }
        }
        if (chkAmp.isSelected()) allArgs.add("--amp");
        allArgs.addAll(checkWSS());
        allArgs.addAll(checkARCorrectionOptions());
        allArgs.addAll(checkLogo());
        if (radMAC.isSelected()) {
            if (checkMacChId() == null) return;
            allArgs.addAll(checkMacOptions());
            allArgs.addAll(checkMacChId());
        }
        if ( (chkAudio.isEnabled()) && (!chkAudio.isSelected()) ){
            allArgs.add("--noaudio");
        }
        else if ( (chkNICAM.isEnabled())
                && (!chkNICAM.isSelected()) 
                && (!chkA2Stereo.isSelected()) ) {
            allArgs.add("--nonicam");
        }
        else if (chkA2Stereo.isSelected()) {
            allArgs.add("--a2stereo");
        }
        if (chkACP.isSelected()) allArgs.add("--acp");
        if (chkRepeat.isSelected()) allArgs.add("--repeat");
        if (!ScramblingType1.isEmpty()) allArgs.add(ScramblingType1);
        if (!ScramblingKey1.isEmpty()) allArgs.add(ScramblingKey1);
        if (!ScramblingType2.isEmpty()) allArgs.add(ScramblingType2);
        if (!ScramblingKey2.isEmpty()) allArgs.add(ScramblingKey2);
        if (cmbECMaturity.getSelectedIndex() > 0) {
            allArgs.add("--ec-mat-rating");
            allArgs.add(Integer.toString(cmbECMaturity.getSelectedIndex()));
        }
        if (chkECppv.isSelected()) {
            allArgs.add("--ec-ppv");
            String n = txtECprognum.getText();
            String c = txtECprogcost.getText();
            if (n.isEmpty()) n = "0";
            if (c.isEmpty()) c = "0";
            allArgs.add(n + "," + c);
        }
        if (chkNoDate.isSelected()) allArgs.add("--nodate");
        if (chkScrambleAudio.isSelected()) {
            if (ScramblingType1.equals("--single-cut") ||
                (ScramblingType1.equals("--double-cut")) ) {
                allArgs.add("--scramble-audio");
            }
            else {
                allArgs.add("--systeraudio");
            }
        }
        switch (cmbSysterPermTable.getSelectedIndex()) {
            case 1:
                allArgs.add("--key-table-1");
                break;
            case 2:
                allArgs.add("--key-table-2");
                break;
            default:
                break;
        }
        // The functions below can return null as an error code, so check for
        // this and stop if necessary.
        if (checkTeletextSource() != null)  {
            allArgs.addAll(checkTeletextSource());
        }
        else {
            return;
        }
        if (checkFMDeviation() != null) {
            allArgs.addAll(checkFMDeviation());
        }
        else {
            return;
        }
        if (checkGamma() != null) {
            allArgs.addAll(checkGamma());
        }
        else {
            return;
        }
        if (checkOutputLevel() != null) {
            allArgs.addAll(checkOutputLevel());
        }
        else {
            return;
        }
        if (checkPosition() != null) {
            allArgs.addAll(checkPosition());
        }
        else {
            return;
        }
        if (checkVolume() != null) {
            allArgs.addAll(checkVolume());
        }
        else {
            return;
        }
        if (chkTimestamp.isSelected()) allArgs.add("--timestamp");
        if (chkVideoFilter.isSelected()) allArgs.add("--filter");
        if (chkVerbose.isSelected()) allArgs.add("--verbose");
        if (txtCardNumber.isEnabled()) {
            String c = checkCardNumber();
            if (c != null) {
                if (chkActivateCard.isSelected()) {
                    allArgs.add("--enableemm");
                }
                else if (chkDeactivateCard.isSelected()) {
                    allArgs.add("--disableemm");
                }
                allArgs.add(c);
            }
        }
        if (chkShowECM.isSelected()) allArgs.add("--showecm");
        if (chkInterlace.isSelected()) allArgs.add("--interlace");
        if (chkShowCardSerial.isSelected()) allArgs.add("--showserial");
        if (chkFindKeys.isSelected()) allArgs.add("--findkey");
        if (chkVITS.isSelected()) allArgs.add("--vits");
        if (chkVITC.isSelected()) allArgs.add("--vitc");
        if (chkColour.isSelected()) allArgs.add("--nocolour");
        if (chkInvertVideo.isSelected()) allArgs.add("--invert-video");
        if (chkDownmix.isSelected()) allArgs.add("--downmix");
        // Finally, add the source video or test option
        if (ytdl.isBlank()) {
            String InputSource = checkInput();
            if (InputSource == null) return;
            if (PlaylistAL.size() > 0) {
                if (chkRandom.isSelected()) {
                    // Set the start point as the first item
                    if (StartPoint != -1) allArgs.add(PlaylistAL.get(StartPoint));
                    new Random().ints(0, PlaylistAL.size())
                        .distinct()
                        .limit(PlaylistAL.size())
                        .forEach(
                            r -> {
                                // Add the rest. except for the start point or test cards
                                if ( (!PlaylistAL.get(r).startsWith("test:")) && (r != StartPoint) ) {
                                    if ( (RunningOnWindows) && (PlaylistAL.get(r).contains(" "))) {
                                        allArgs.add('\u0022' + PlaylistAL.get(r) + '\u0022');
                                    }
                                    else {
                                       allArgs.add(PlaylistAL.get(r));
                                    }
                                }
                           }
                    );
                }
                else {
                    // Move through PlaylistAL, starting at the value defined by StartPoint.
                    // When we reach the end of the array, start again at zero until we
                    // reach PlaylistAL.size() minus one.
                    int i = StartPoint;
                    int j = 0;
                    if (i == -1) i++;
                    while (j < PlaylistAL.size()) {
                        if ( (i == PlaylistAL.size()) && (StartPoint != 0) ) {
                            i = 0;
                        }
                        if ( (PlaylistAL.get(i).startsWith("test:")) ||
                            (PlaylistAL.get(i).startsWith("http")) ) {
                            allArgs.add(PlaylistAL.get(i));
                        }
                        else {
                            if ( (RunningOnWindows) && PlaylistAL.get(i).contains(" ") ) {
                                allArgs.add('\u0022' + PlaylistAL.get(i) + '\u0022');
                            }
                            else {
                                allArgs.add(PlaylistAL.get(i));
                            }
                        }
                        i++;
                        j++;
                    }
                }  
            }
            else if ( (RunningOnWindows) && InputSource.contains(" ")) {
                // Add quotation marks if path contains whitespaces on Windows
                allArgs.add('\u0022' + InputSource + '\u0022');
            }
            else if (radTest.isSelected()) {
                allArgs.addAll(checkTestCard());
            }
            else {
                allArgs.add(InputSource);
            }
            // Arguments textbox handling - clear it first
            if (!txtStatus.getText().isEmpty()) txtStatus.setText("");
            // Start a for loop to populate the textbox, using the arraylist 
            // size as the finish value.
            for (int i = 1; i < allArgs.size() ; i++) {
                // Add value 1 (mode) first and then add all other values. I've
                // set it up this way to prevent a leading space from being
                // printed in the textbox.
                if (i == 1) { 
                    txtStatus.setText(allArgs.get(i)); 
                }
                else {
                    txtStatus.setText( txtStatus.getText() + 
                            '\u0020' + allArgs.get(i) );
                }
            }            
        }
        else {
            // Specify stdIn as the source
            allArgs.add("-");
        }
        // End add to arraylist
        // If "Generate syntax only" is enabled, stop here
        if (chkSyntaxOnly.isSelected()) return;
        // Change the Run button and disable some other options
        preRunTasks();
        // Clear the console
        txtConsoleOutput.setText("");
        // If a YouTube URL was specified, call its method.
        // Otherwise, call the standard one.
        if (!ytdl.isBlank()) {
            runYTDLpipe(ytdl, allArgs);
        }
        else {
            runHackTV(allArgs);
        }
    }
    
    private void runHackTV(ArrayList<String> allArgs) {
        // Spawn a new SwingWorker to run hacktv
        SwingWorker <Boolean, String> runTV = new SwingWorker <Boolean, String> () {
            @Override
            protected Boolean doInBackground() {
                // Create process with the ArrayList we populated above
                ProcessBuilder pb = new ProcessBuilder(allArgs);
                pb.redirectErrorStream(true);
                pb.directory(new File(HackTVDirectory));
                // Try to start the process
                try {
                    Process p = pb.start();
                    // Get the PID of the process we just started
                    hpid = p.pid();
                    // Capture the output of hacktv
                    int a;
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        while ( (a = br.read()) != -1 ) {
                            // br.read() returns an integer value 'a' which is the ASCII
                            // number of a character it has received from the process.
                            // We convert 'a' to the actual character and publish it.
                            // When the process has closed, br.read() will return -1
                            // which will exit this loop.
                            publish(String.valueOf((char)a));
                        }
                    }
                    publish("\n" + "hacktv stopped");
                }
                catch (IOException ex) {
                    return false;
                }
                return true;
            } // End doInBackground

            // Update the GUI from this method.
            @Override
            protected void done() {
                // Get the status code from doInBackground() and return an
                // error if it failed.
                try {
                    if (!get()) {
                        messageBox("An error occurred while attempting to run hacktv.", JOptionPane.ERROR_MESSAGE);
                    }
                }
                catch (InterruptedException | ExecutionException e) {
                    System.err.println(e);
                }
                /* If an invalid parameter is passed to hacktv, it usually
                   responds with its usage message.
                   Here, we check if the first line of the usage has been
                   returned. If so, we assume that one of the parameters we fed 
                   is not supported.
                */
                if (txtConsoleOutput.getText().contains("Usage: hacktv [options] input [input...]")) {
                    messageBox("This copy of hacktv does not appear to support one or more"
                            + " of the selected options. Please update hacktv and try again."
                            , JOptionPane.WARNING_MESSAGE);
                }
                // Revert what we changed before starting
                postRunTasks();    
            }
            // Update the GUI from this method.
            @Override
            protected void process(List<String> chunks) {
                // Here we receive the values from publish() and display
                // them in the console
                for (String o : chunks) {
                    txtConsoleOutput.append(o);
                }
            }// End of process
        }; // End of SwingWorker
        runTV.execute();
    }
    
    private void runYTDLpipe(String ytp, ArrayList<String> allArgs) {
        String u;
        if (txtSource.getText().toLowerCase().startsWith("ytdl:")) {
            u = txtSource.getText().substring(5);
        }
        else {
            u = txtSource.getText();
        }
        // Populate yt-dlp parameters
        // The "--ignore-config" parameter tells yt-dlp to ignore any local
        // configuration files which may conflict with what we need here.
        var ytargs = new ArrayList<String>();
        ytargs.add(ytp);
        ytargs.add("--ignore-config");
        ytargs.add("-q");
        ytargs.add("-o");
        ytargs.add("-");
        ytargs.add(u);
        // Populate arguments textbox
        for (int i = 0; i < ytargs.size() ; i++) {
            if (i == 0) { 
                txtStatus.setText(ytargs.get(i)); 
            }
            else {
                txtStatus.setText( txtStatus.getText() + 
                        '\u0020' + ytargs.get(i) );
            }
        }   
        if (RunningOnWindows) {
            txtStatus.setText( txtStatus.getText() + " | hacktv.exe");
        }
        else {
            txtStatus.setText( txtStatus.getText() + " | hacktv");
        }
        for (int i = 1; i < allArgs.size() ; i++) {
            txtStatus.setText(txtStatus.getText() + '\u0020' + allArgs.get(i));
        }
        // Spawn a new SwingWorker to run yt-dlp and hacktv
        SwingWorker <Boolean, String> runTV = new SwingWorker <Boolean, String> () {
            @Override
            protected Boolean doInBackground() {
                // Create two processes, one for yt-dlp and the other for hacktv
                List<ProcessBuilder> pb = Arrays.asList(
                    new ProcessBuilder(ytargs)
                        // Redirect any yt-dlp errors to the Java console
                        .redirectError(ProcessBuilder.Redirect.INHERIT),
                    new ProcessBuilder(allArgs)
                        .redirectOutput(ProcessBuilder.Redirect.PIPE)
                        .redirectErrorStream(true)
                );
                try {
                    // Start the processes using startPipeline, which will pipe
                    // stdOut of yt-dlp to stdIn of hacktv
                    List<Process> p = ProcessBuilder.startPipeline(pb);
                    // Get the yt-dlp process
                    Process y = (Process) p.get(0);
                    // Get the hacktv process
                    Process h = (Process) p.get(1);
                    // Get the PID of hacktv
                    hpid = h.pid();
                    // Capture the output of hacktv
                    int a;
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(h.getInputStream()))) {
                        while ( (a = br.read()) != -1 ) {
                            publish(String.valueOf((char)a));
                        }
                    }
                    publish("\n" + "hacktv stopped");
                    // End yt-dlp if it's still running
                    // yt-dlp can spawn a child process, we need to kill this
                    // process instead of the parent. So check for it.
                    if (y.descendants().count() > 0) {                        
                        y.descendants().forEach(d -> {
                            d.destroy();
                        });
                        if (y.isAlive()) y.destroy();
                    }
                    else if (y.isAlive()) {
                        y.destroy();
                    }
                }
                catch (IOException ex) {
                    return false;
                }
                return true;
            } // End doInBackground
            @Override
            protected void process(List<String> chunks) {
                // Here we receive the values from publish() and display
                // them in the console
                for (String o : chunks) {
                    txtConsoleOutput.append(o);
                }
            }// End of process
            @Override
            protected void done() {
                // Get the status code from doInBackground() and return an
                // error if it failed.
                try {
                    if (!get()) {
                        messageBox("An error occurred while attempting to run yt-dlp or hacktv.", JOptionPane.ERROR_MESSAGE);
                    }
                }
                catch (InterruptedException | ExecutionException e) {
                    System.err.println(e);
                }
                /* If an invalid parameter is passed to hacktv, it usually
                   responds with its usage message.
                   Here, we check if the first line of the usage has been
                   returned. If so, we assume that one of the parameters we fed 
                   is not supported.
                */
                if (txtConsoleOutput.getText().contains("Usage: hacktv [options] input [input...]")) {
                    messageBox("This copy of hacktv does not appear to support one or more"
                            + " of the selected options. Please update hacktv and try again."
                            , JOptionPane.WARNING_MESSAGE);
                }
                // Revert button to display Run instead of Stop
                postRunTasks();
            }// End of done
        }; // End of SwingWorker
        runTV.execute();
    }
    
    private void stopTV(long pid) {
        /*  To stop hacktv gracefully, it needs to be sent a SIGINT signal.
         *  Under Unix/POSIX systems this is easy, just run kill -2 and the PID.
         * 
         *  Under Windows it's not so easy.
         *  We have implemented this using PowerShell but it's quite heavy
         *  so another option is an external helper such as:
         *  https://github.com/ElyDotDev/windows-kill/releases
         */
        // Don't do anything if the PID is zero
        if (pid == 0) return;
        if (RunningOnWindows) {
            if (chkWindowsKill.isSelected()) {
                try {
                    // Run windows-kill.exe from this path and feed the PID to it
                    ProcessBuilder StopHackTV = new ProcessBuilder
                        (JarDir + "\\windows-kill.exe", "-2", Long.toString(pid));
                    Process p = StopHackTV.start();
                }
                catch (IOException ex)  {
                    System.err.println(ex);
                    messageBox("An error occurred while attempting to stop hacktv using windows-kill.\n"
                            + "Try using PowerShell instead, see help for details.", JOptionPane.ERROR_MESSAGE);
                    btnRun.setEnabled(true);
                }
            }
            else {
                // Call the PowerShell method and feed the PID to it
                try {
                    psKill(pid);
                }
                catch (IOException ex) {
                    System.err.println(ex);
                    messageBox("An error occurred while attempting to stop hacktv using PowerShell.\n"
                            + "Try using windows-kill instead, see help for details.", JOptionPane.ERROR_MESSAGE);
                    btnRun.setEnabled(true);
                }
            }
        }
        else {
            try {
                // Run kill and feed the PID to it
                ProcessBuilder StopHackTV = new ProcessBuilder
                    ("kill", "-2", Long.toString(pid));
                Process p = StopHackTV.start();
            }
            catch (IOException ex)  {
                System.err.println(ex);
            }
        }
    }
    
    private void psKill(long pid) throws IOException {
        // Uses PowerShell to gracefully close hacktv on Windows
        // The following string is PowerShell/C# code to implement the
        // Win32 GenerateConsoleCtrlEvent API.
        
        // I decided to use a single clear string (with escape characters where
        // necessary) rather than risking triggering AV software by using
        // EncodedCommand. I've divided the string into lines here for clarity.
        String ps1 = 
                "Add-Type -Namespace 'steeviebops' -Name 'hacktvgui' -MemberDefinition '"
                +     "[DllImport(\\\"kernel32.dll\\\")]public static extern bool FreeConsole();"
                +     "[DllImport(\\\"kernel32.dll\\\")]public static extern bool AttachConsole(uint p);"
                +     "[DllImport(\\\"kernel32.dll\\\")]public static extern bool GenerateConsoleCtrlEvent(uint e, uint p);"
                +     "public static void SendCtrlC(uint p){"
                +         "FreeConsole();"
                +         "AttachConsole(p);"
                +         "GenerateConsoleCtrlEvent(0, 0);"
                +     "}';"
                + "[steeviebops.hacktvgui]::SendCtrlC(" + pid + ")";
        // Run powershell.exe and feed the above command string to it
        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-noprofile", "-nologo", "-command", ps1);
        Process p = pb.start();
        // Redirect PowerShell output to stderr for troubleshooting
        /*int a;
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ( (a = br.read()) != -1 ) {
            System.err.print(String.valueOf((char)a));
        }*/
    }
    
    private void preRunTasks() {
        btnRun.setText("Stop hacktv");
        chkSyntaxOnly.setEnabled(false);
        btnHackTVPath.setEnabled(false);
        if (RunningOnWindows) btnDownloadHackTV.setEnabled(false);
        Running = true;
    }
    
    private void postRunTasks() {
        btnRun.setText("Run hacktv");
        if (!btnRun.isEnabled()) btnRun.setEnabled(true);
        chkSyntaxOnly.setEnabled(true);
        btnHackTVPath.setEnabled(true);
        if (RunningOnWindows) btnDownloadHackTV.setEnabled(true);
        Running = false;
    }
    
    private void cleanupBeforeExit() {
        // Check if a teletext download is in progress
        // If so, then abort
        if (DownloadInProgress) DownloadCancelled = true;
        // Check if hacktv is running, if so then exit it
        if (Running) {
            stopTV(hpid);
        }
        // Delete temp directory and files before exit
        if (TempDir != null) {
            try {
                SharedInst.deleteFSObject(TempDir.resolve(TempDir));
            } catch (IOException ex) {
                System.err.println("An error occurred while attempting to delete the temp directory: " + ex);
            }
        }
    }

    private void btnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunActionPerformed
        if (!Running) {
            if (DownloadInProgress) {
                DownloadCancelled = true;
                btnRun.setEnabled(false);
            }
            else if ( (!chkSyntaxOnly.isSelected()) && (!Files.exists(Path.of(HackTVPath))) || ((HackTVPath.isBlank())) ) {
                messageBox("Unable to find hacktv. Please go to the GUI settings tab to add its location.", JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(5);
            }
            else {
                populateArguments("");
            }
        }
        else {
            // Disable the stop button if using PowerShell to close
            if ((RunningOnWindows) && (!chkWindowsKill.isSelected())) {
                btnRun.setEnabled(false);
            }
            stopTV(hpid);
        }
    }//GEN-LAST:event_btnRunActionPerformed
             
    private void menuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAboutActionPerformed
        JOptionPane.showMessageDialog(null,
                APP_NAME +
                getVersion() +
                "\nUsing " + ModesFileLocation + " Modes.ini file, version " + ModesFileVersion +
                "\n\nCreated 2020-2023 by Stephen McGarry.\n" +
                "Provided under the terms of the General Public Licence (GPL) v2 or later.\n\n" +
                "https://github.com/steeviebops/hacktv-gui\n\n",
            "About " + APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_menuAboutActionPerformed

    private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_menuExitActionPerformed
      
    private void txtCardNumberKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCardNumberKeyTyped
        if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
        else if (txtCardNumber.getText().length() >= 13) {
            evt.consume();
        }
    }//GEN-LAST:event_txtCardNumberKeyTyped

    private void chkDeactivateCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDeactivateCardActionPerformed
        if (chkDeactivateCard.isSelected()) {
            chkActivateCard.setSelected(false);
            txtCardNumber.setEnabled(true);
            lblEMMCardNumber.setEnabled(true);
            showEMMWarning();
        }
        else {
            if ( !chkActivateCard.isSelected()) {
                txtCardNumber.setText("");
                txtCardNumber.setEnabled(false);
                lblEMMCardNumber.setEnabled(false);
            }
        }
    }//GEN-LAST:event_chkDeactivateCardActionPerformed

    private void chkActivateCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkActivateCardActionPerformed
        if (chkActivateCard.isSelected()) {
            chkDeactivateCard.setSelected(false);
            txtCardNumber.setEnabled(true);
            lblEMMCardNumber.setEnabled(true);
            showEMMWarning();
        }
        else {
            if ( !chkDeactivateCard.isSelected()) {
                txtCardNumber.setText("");
                txtCardNumber.setEnabled(false);
                lblEMMCardNumber.setEnabled(false);
            }
        }
    }//GEN-LAST:event_chkActivateCardActionPerformed

    private void cmbScramblingKey2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbScramblingKey2ActionPerformed
        /* This combobox is only currently used for dual VideoCrypt I/II mode.
        So, we check that this mode is selected, and that the combobox is
        not blank.
        */
        if ( (cmbScramblingKey2.getSelectedIndex() != -1) &&
            (cmbScramblingType.getSelectedIndex() == 3) ) {
            ScramblingType2 = "--videocrypt2";
            ScramblingKey2 = ScramblingKey2Array.get(cmbScramblingKey2.getSelectedIndex());
        }
    }//GEN-LAST:event_cmbScramblingKey2ActionPerformed

    private void cmbScramblingKey1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbScramblingKey1ActionPerformed
        if (cmbScramblingKey1.getSelectedIndex() != -1) {
            if (ScramblingType1.equals("--single-cut") ||
                (ScramblingType1.equals("--double-cut")) ) {
                ScramblingKey1 = "";
                ScramblingKey2 = ScramblingKeyArray.get(cmbScramblingKey1.getSelectedIndex());
                /* Free access mode doesn't use the --eurocrypt option, so
                check before adding.
                */
                if (!ScramblingKey2.contains("blank")) {
                    ScramblingType2 = "--eurocrypt";
                }
                else {
                    ScramblingType2 = "";
                    ScramblingKey2 = "";
                }
            }
            else {
                ScramblingKey1 = ScramblingKeyArray.get(cmbScramblingKey1.getSelectedIndex());
                if (!cmbScramblingKey2.isEnabled()) {
                    ScramblingType2 = "";
                    ScramblingKey2 = "";
                }
                /* If Syster dual mode (line shuffle+cut-and-rotate) is enabled,
                 * set up CNR as a secondary scrambling type and duplicate the 
                 * scrambling key to the CNR mode - you can't use different
                 * access keys simultaneously.
                 */
                if ( (ScramblingType1.equals("--syster")) && (cmbScramblingType.getSelectedIndex() == 8) ) {
                    ScramblingType2 = "--systercnr";
                    ScramblingKey2 = ScramblingKey1;    
                }
                // Delete the "blank" parameter if specified
                // This is used as a placeholder for modes which don't use
                // an additional parameter
                if (ScramblingKey1.equals("blank")) {
                    ScramblingKey1 = "";
                }
            }
            configureScramblingOptions();
        }
    }//GEN-LAST:event_cmbScramblingKey1ActionPerformed

    private void cmbScramblingTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbScramblingTypeActionPerformed
        if (cmbScramblingType.getSelectedIndex() != -1) {
            ScramblingType1 = ScramblingTypeArray.get(cmbScramblingType.getSelectedIndex());
            addScramblingKey();
        }
    }//GEN-LAST:event_cmbScramblingTypeActionPerformed

    private void btnSparkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSparkActionPerformed
        if ((btnSpark.getText()).contains("Stop")) {
            DownloadCancelled = true;
        }
        else {
            btnSpark.setText("Stop");
            chkTeletext.setEnabled(false);
            txtTeletextSource.setEnabled(false);
            btnTeletextBrowse.setEnabled(false);
            btnTeefax.setEnabled(false);
            lblTeefax.setEnabled(false);
            btnNMSCeefax.setEnabled(false);
            lblNMSCeefax.setEnabled(false);
            btnRun.setEnabled(false);
            DownloadCancelled = false;
            // Set variables
            String dUrl = "https://github.com/spark-teletext/spark-teletext/";
            String HTMLString = ".tti\">(.*?)</a>";
            HTMLTempFile = "spark.html";
            // Download index page
            downloadTeletext(dUrl, HTMLTempFile, HTMLString);
        }
    }//GEN-LAST:event_btnSparkActionPerformed

    private void btnTeefaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeefaxActionPerformed
        if ((btnTeefax.getText()).contains("Stop")) {
            DownloadCancelled = true;
        }
        else {
            btnTeefax.setText("Stop");
            chkTeletext.setEnabled(false);
            txtTeletextSource.setEnabled(false);
            btnTeletextBrowse.setEnabled(false);
            btnSpark.setEnabled(false);
            lblSpark.setEnabled(false);
            btnNMSCeefax.setEnabled(false);
            lblNMSCeefax.setEnabled(false);
            btnRun.setEnabled(false);
            DownloadCancelled = false;
            // Set variables
            String dUrl = "http://teastop.plus.com/svn/teletext/";
            String HTMLString = "\">(.*?)</a>";
            HTMLTempFile = "teefax.html";
            // Download index page
            downloadTeletext(dUrl, HTMLTempFile, HTMLString);
        }
    }//GEN-LAST:event_btnTeefaxActionPerformed

    private void btnTeletextBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeletextBrowseActionPerformed
        // Retrieve the last used directory from the prefs store if it exists
        teletextFileChooser.setCurrentDirectory(
            new File(Prefs.get("lasttxdir", System.getProperty("user.home")))
        );
        int result = teletextFileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            // Save the chosen directory to prefs
            Prefs.put("lasttxdir", teletextFileChooser.getCurrentDirectory().toString());
            File f = teletextFileChooser.getSelectedFile();
            txtTeletextSource.setText(SharedInst.stripQuotes(f.getAbsolutePath()));
        }
    }//GEN-LAST:event_btnTeletextBrowseActionPerformed

    private void chkTeletextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTeletextActionPerformed
        if (chkTeletext.isSelected()) {
            btnTeletextBrowse.setEnabled(true);
            txtTeletextSource.setEnabled(true);
            downloadPanel.setEnabled(true);
            btnTeefax.setEnabled(true);
            btnSpark.setEnabled(true);
            btnNMSCeefax.setEnabled(true);
            lblDownload.setEnabled(true);
            lblTeefax.setEnabled(true);
            lblSpark.setEnabled(true);
            lblNMSCeefax.setEnabled(true);
        }
        else {
            btnTeletextBrowse.setEnabled(false);
            txtTeletextSource.setText("");
            txtTeletextSource.setEnabled(false);
            downloadPanel.setEnabled(false);
            btnTeefax.setEnabled(false);
            btnSpark.setEnabled(false);
            btnNMSCeefax.setEnabled(false);
            lblDownload.setEnabled(false);
            lblTeefax.setEnabled(false);
            lblSpark.setEnabled(false);
            lblNMSCeefax.setEnabled(false);
        }
    }//GEN-LAST:event_chkTeletextActionPerformed

    private void chkOutputLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkOutputLevelActionPerformed
        if (chkOutputLevel.isSelected()) {
            txtOutputLevel.setEnabled(true);
        }
        else {
            txtOutputLevel.setText("");
            txtOutputLevel.setEnabled(false);
        }
    }//GEN-LAST:event_chkOutputLevelActionPerformed

    private void chkGammaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGammaActionPerformed
        if (chkGamma.isSelected()) {
            txtGamma.setEnabled(true);
        }
        else {
            txtGamma.setText("");
            txtGamma.setEnabled(false);
        }
    }//GEN-LAST:event_chkGammaActionPerformed

    private void chkWSSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkWSSActionPerformed
        if (chkWSS.isSelected()) {
            cmbWSS.setEnabled(true);
            cmbWSS.setSelectedIndex(0);
        }
        else {
            cmbWSS.setEnabled(false);
            cmbWSS.setSelectedIndex(-1);
        }
    }//GEN-LAST:event_chkWSSActionPerformed

    private void chkVideoFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkVideoFilterActionPerformed
        if (!chkVideoFilter.isSelected()) {
            if ( ScramblingType1.equals("--videocrypt") || ScramblingType1.equals("--videocrypt2") ) {
                if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
                txtPixelRate.setText("14");
            }
            else {
                txtSampleRate.setText(DefaultSampleRate);
            }
        }
    }//GEN-LAST:event_chkVideoFilterActionPerformed

    private void chkPixelRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPixelRateActionPerformed
        if (chkPixelRate.isSelected()) {
            txtPixelRate.setEnabled(true);
            txtPixelRate.setText(txtSampleRate.getText());
        }
        else {
            txtPixelRate.setText("");
            txtPixelRate.setEnabled(false);
        }
    }//GEN-LAST:event_chkPixelRateActionPerformed

    private void txtMacChIdKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMacChIdKeyTyped
        if (txtMacChId.getText().length() >= 4) {
            evt.consume();
        }
        else {
            String c = String.valueOf((char)evt.getKeyChar());
            if (SharedInst.isHex(c)) {
                evt.setKeyChar(c.toUpperCase().toCharArray()[0]);
            }
            else {
                evt.consume();
            }
        }
    }//GEN-LAST:event_txtMacChIdKeyTyped

    private void chkMacChIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMacChIdActionPerformed
        if (chkMacChId.isSelected()) {
            txtMacChId.setEnabled(true);
        }
        else {
            txtMacChId.setText("");
            txtMacChId.setEnabled(false);
        }
    }//GEN-LAST:event_chkMacChIdActionPerformed

    private void chkNICAMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNICAMActionPerformed
        if (chkNICAM.isSelected()) {
            if (chkA2Stereo.isSelected()) chkA2Stereo.doClick();
        }
    }//GEN-LAST:event_chkNICAMActionPerformed

    private void chkAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAudioActionPerformed
        if (chkAudio.isSelected()) {
            if (NICAMSupported) {
                chkNICAM.setEnabled(true);
                chkNICAM.doClick();
            }
            if (A2Supported) chkA2Stereo.setEnabled(true);
        }
        else {
            if (chkNICAM.isSelected()) chkNICAM.doClick();
            chkNICAM.setEnabled(false);
            if (chkA2Stereo.isSelected()) chkA2Stereo.doClick();
            chkA2Stereo.setEnabled(false);
        }
    }//GEN-LAST:event_chkAudioActionPerformed

    private void radMACActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMACActionPerformed
        cmbMode.removeAllItems();
        cmbMode.setModel( new DefaultComboBoxModel<>( addVideoModes("mac", 0) ) );
        cmbMode.setSelectedIndex(0);
    }//GEN-LAST:event_radMACActionPerformed

    private void radBWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radBWActionPerformed
        cmbMode.removeAllItems();
        cmbMode.setModel( new DefaultComboBoxModel<>( addVideoModes("other", 0) ) );
        cmbMode.setSelectedIndex(0);
    }//GEN-LAST:event_radBWActionPerformed

    private void radSECAMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radSECAMActionPerformed
        cmbMode.removeAllItems();
        cmbMode.setModel( new DefaultComboBoxModel<>( addVideoModes("secam", 0) ) );
        cmbMode.setSelectedIndex(0);
    }//GEN-LAST:event_radSECAMActionPerformed

    private void radNTSCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radNTSCActionPerformed
        cmbMode.removeAllItems();
        cmbMode.setModel( new DefaultComboBoxModel<>( addVideoModes("ntsc", 0) ) );
        cmbMode.setSelectedIndex(0);
    }//GEN-LAST:event_radNTSCActionPerformed

    private void radPALActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radPALActionPerformed
        cmbMode.removeAllItems();
        cmbMode.setModel( new DefaultComboBoxModel<>( addVideoModes("pal", 0) ) );
        cmbMode.setSelectedIndex(0);
        // End populate
    }//GEN-LAST:event_radPALActionPerformed

    private void cmbModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbModeActionPerformed
        if (cmbMode.getSelectedIndex() != -1) {
            checkMode();
            /* Save the currently selected item into PreviousIndex.
               We can use this to revert the change if an unsupported mode is
               selected later.
            */
            PreviousIndex = cmbMode.getSelectedIndex();
            // Set sample rate
            txtSampleRate.setText(DefaultSampleRate);
            // If test card is selected, see if the selected mode is supported
            if (radTest.isSelected()) checkTestCardStatus();
        }
    }//GEN-LAST:event_cmbModeActionPerformed

    private void chkARCorrectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkARCorrectionActionPerformed
        if (chkARCorrection.isSelected()) {
            cmbARCorrection.setEnabled(true);
        }
        else {
            cmbARCorrection.setEnabled(false);
            cmbARCorrection.setSelectedIndex(0);
        }
    }//GEN-LAST:event_chkARCorrectionActionPerformed

    private void chkSubtitlesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSubtitlesActionPerformed
        if (chkSubtitles.isSelected()) {
            lblSubtitleIndex.setEnabled(true);
            txtSubtitleIndex.setEnabled(true);
        }
        else {
            lblSubtitleIndex.setEnabled(false);
            txtSubtitleIndex.setText("");
            txtSubtitleIndex.setEnabled(false);
        }
    }//GEN-LAST:event_chkSubtitlesActionPerformed

    private void chkLogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLogoActionPerformed
        if (chkLogo.isSelected()) {
            cmbLogo.setEnabled(true);
            cmbLogo.setSelectedIndex(0);
        }
        else {
            // Disable the cmbLogo combobox and clear its variables
            cmbLogo.setSelectedIndex(-1);
            cmbLogo.setEnabled(false);
        }
    }//GEN-LAST:event_chkLogoActionPerformed

    private void chkPositionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPositionActionPerformed
        if (chkPosition.isSelected()) {
            txtPosition.setEnabled(true);
        }
        else {
            txtPosition.setText("");
            txtPosition.setEnabled(false);
        }
    }//GEN-LAST:event_chkPositionActionPerformed

    private void btnSourceBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSourceBrowseActionPerformed
        // Retrieve the last used directory from the prefs store if it exists
        sourceFileChooser.setCurrentDirectory(
            new File(Prefs.get("lastdir", System.getProperty("user.home")))
        );
        int returnVal = sourceFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] f = sourceFileChooser.getSelectedFiles();
            // Save the chosen directory to prefs
            Prefs.put("lastdir", sourceFileChooser.getCurrentDirectory().toString());
            if (f.length > 1) {
                for (File fn : f) {
                    if ((!fn.toString().toLowerCase().endsWith(".m3u"))
                            && (!fn.toString().toLowerCase().endsWith(".htv"))) {
                        PlaylistAL.add(fn.toString());
                    }
                }
                populatePlaylist();
            }
            else {
                File file = new File (SharedInst.stripQuotes(f[0].toString()));
                if(file.getAbsolutePath().toLowerCase().endsWith(".m3u")) {
                    // If the source is an M3U file, pass it to the M3U handler
                    txtSource.setText(file.getAbsolutePath());
                    m3uHandler(file.getAbsolutePath(),0);
                }
                else if (file.getAbsolutePath().toLowerCase().endsWith(".htv")) {
                    // Don't try to process a file with a .HTV extension
                    messageBox("Configuration files should be opened from the File menu.", JOptionPane.WARNING_MESSAGE);    
                }
                else {
                    txtSource.setVisible(true);
                    cmbM3USource.setVisible(false);
                    cmbM3USource.setEnabled(false);
                    txtSource.setText(file.getAbsolutePath());
                }                
            }

        }
    }//GEN-LAST:event_btnSourceBrowseActionPerformed

    private void radTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radTestActionPerformed
        // Disable all options in the frame
        if (chkRepeat.isSelected()) chkRepeat.doClick();
        if (chkPosition.isSelected()) chkPosition.doClick();
        if (chkTimestamp.isSelected()) chkTimestamp.doClick();
        if (chkInterlace.isSelected()) chkInterlace.doClick();
        if (chkSubtitles.isSelected()) chkSubtitles.doClick();
        if (chkDownmix.isSelected()) chkDownmix.doClick();
        if (chkVolume.isSelected()) chkVolume.doClick();
        chkRepeat.setEnabled(false);
        chkPosition.setEnabled(false);
        chkTimestamp.setEnabled(false);
        chkInterlace.setEnabled(false);
        chkSubtitles.setEnabled(false);
        chkDownmix.setEnabled(false);
        chkVolume.setEnabled(false);
        txtSource.setEnabled(false);
        btnSourceBrowse.setEnabled(false);
        txtSource.setText("");
        if (chkARCorrection.isSelected()) { chkARCorrection.doClick(); }
        chkARCorrection.setEnabled(false);
        if (chkTextSubtitles.isSelected()) chkTextSubtitles.doClick();
        chkTextSubtitles.setEnabled(false);    
        if ( cmbM3USource.isVisible() ) {
            cmbM3USource.setVisible(false);
            cmbM3USource.setEnabled(false);
            txtSource.setVisible(true);
        }
        // Enable test card dropdown
        if ((CaptainJack) && (cmbTest.getItemCount() > 1)) {
            cmbTest.setEnabled(true);
            cmbTest.setSelectedIndex(0);
        }
    }//GEN-LAST:event_radTestActionPerformed

    private void radLocalSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radLocalSourceActionPerformed
        // Enable all options in the frame
        chkRepeat.setEnabled(true);
        chkInterlace.setEnabled(true);
        txtSource.setEnabled(true);
        btnSourceBrowse.setEnabled(true);
        if (CaptainJack) {
            chkPosition.setEnabled(true);
            chkTimestamp.setEnabled(true);
            chkARCorrection.setEnabled(true);
            chkVolume.setEnabled(true);
            chkDownmix.setEnabled(true);
            chkSubtitles.setEnabled(true);
            // Disable test card dropdown
            cmbTest.setSelectedIndex(-1);
            cmbTest.setEnabled(false);
            if (chkTeletext.isEnabled()) {
                chkTextSubtitles.setEnabled(true);
            }
        }
    }//GEN-LAST:event_radLocalSourceActionPerformed

    private void chkAmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAmpActionPerformed
        if (chkAmp.isSelected()) {
            if ( (!HTVLoadInProgress) && (Prefs.getInt("SuppressWarnings", 0) != 1))
                messageBox("Care is advised when using this option.\n" +
                    "Incorrect use may permanently damage the amplifier.",
                    JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_chkAmpActionPerformed

    private void cmbChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbChannelActionPerformed
        if ( cmbChannel.getSelectedIndex() != -1) {
            Frequency = FrequencyArray[cmbChannel.getSelectedIndex()];
            // Convert the imported value so we can display it in MHz on-screen
            DecimalFormat df = new DecimalFormat("0.00");
            double input = Frequency;
            txtFrequency.setText((df.format(input / 1000000)));
            // Retrieve MAC channel ID
            if (radMAC.isSelected()) {
                String b = new String();
                if (radUHF.isSelected()) {
                    b = "uhf";
                }
                else if (radUHF.isSelected()) {
                    b = "vhf";
                }
                // Retrieve bandplan
                String bp = INI.getStringFromINI(ModesFile, Mode, b, "", false);
                // Retrieve channel ID list
                String c = INI.getStringFromINI(ModesFile, bp, "chid", "", false);
                // Retrieve ID using the channel name from the ID list
                // This name must be identical to the name specified in the bandplan
                String id = INI.getStringFromINI(ModesFile, c, ChannelArray[cmbChannel.getSelectedIndex()], "", false).toUpperCase();
                if (id.isBlank()) {
                    // Nothing found, deselect the channel ID checkbox
                    if (chkMacChId.isSelected()) chkMacChId.doClick();
                }
                else {
                    // If channel ID checkbox is deselected, select it
                    if (!chkMacChId.isSelected()) chkMacChId.doClick();
                    txtMacChId.setText(id);
                }
            }
        }
    }//GEN-LAST:event_cmbChannelActionPerformed

    private void txtFrequencyKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFrequencyKeyTyped
        if(txtFrequency.getText().length()>9) {
            evt.consume();
        }
    }//GEN-LAST:event_txtFrequencyKeyTyped

    private void radUHFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radUHFActionPerformed
        // Get the name of the currently selected VHF region and look it up
        String sv = new String();
        if (cmbRegion.getSelectedIndex() > 0) sv = cmbRegion.getItemAt(cmbRegion.getSelectedIndex());
        // Set region and alternate plans for UHF
        cmbRegion.setEnabled(false);
        cmbRegion.removeAllItems();
        for (int i = 0; i < UhfAL.size(); i++) {
            cmbRegion.addItem(INI.getStringFromINI(ModesFile, UhfAL.get(i), "region", UhfAL.get(i), true));
        }
        // Enable the region combobox if multiple options are available.
        if (cmbRegion.getItemCount() > 1) cmbRegion.setEnabled(true);
        populateBandPlan("uhf");
        // If multiple regions are available, see if there's a UHF region with the
        // same name as the previously selected VHF one. If so, select it.
        if ( (UhfAL.size() > 1) && (VhfAL.size() > 1) ) {
            for (int r = 0; r < cmbRegion.getItemCount(); r++) {
                String su = cmbRegion.getItemAt(r);
                if (sv.equals(su)) {
                    cmbRegion.setSelectedIndex(r);
                    break;
                }
            }        
        }
    }//GEN-LAST:event_radUHFActionPerformed

    private void radVHFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radVHFActionPerformed
        // Get the name of the currently selected UHF band plan and look it up
        String su = new String();
        if (cmbRegion.getSelectedIndex() > 0) su = cmbRegion.getItemAt(cmbRegion.getSelectedIndex());
        // Set region and alternate plans for VHF
        cmbRegion.setEnabled(false);
        cmbRegion.removeAllItems();
        for (int i = 0; i < VhfAL.size(); i++) {
            cmbRegion.addItem(INI.getStringFromINI(ModesFile, VhfAL.get(i), "region", VhfAL.get(i), true));
        }
        // Enable the region combobox if multiple options are available.
        if (cmbRegion.getItemCount() > 1) cmbRegion.setEnabled(true);
        populateBandPlan("vhf");
        // If multiple regions are available, see if there's a VHF region with the
        // same name as the previously selected UHF one. If so, select it.
        if ( (UhfAL.size() > 1) && (VhfAL.size() > 1) ) {
            for (int r = 0; r < cmbRegion.getItemCount(); r++) {
                String sv = cmbRegion.getItemAt(r);
                if (su.equals(sv)) {
                    cmbRegion.setSelectedIndex(r);
                    break;
                }
            }            
        }
    }//GEN-LAST:event_radVHFActionPerformed

    private void radCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radCustomActionPerformed
        txtFrequency.setEditable(true);
        cmbChannel.setEnabled(false);
        cmbChannel.setSelectedIndex(-1);
        cmbRegion.setEnabled(false);
        cmbRegion.removeAllItems();
        // Add a blank item to prevent the combobox from enlarging on some L&Fs
        cmbRegion.addItem("");
    }//GEN-LAST:event_radCustomActionPerformed

    private void menuNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNewActionPerformed
        resetAllControls();
    }//GEN-LAST:event_menuNewActionPerformed

    private void menuAstra975TemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAstra975TemplateActionPerformed
        astraTemplate(9.75);
    }//GEN-LAST:event_menuAstra975TemplateActionPerformed

    private void menuAstra10TemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAstra10TemplateActionPerformed
        astraTemplate(10.0);
    }//GEN-LAST:event_menuAstra10TemplateActionPerformed

    private void menuBSBTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuBSBTemplateActionPerformed
        int q = JOptionPane.showConfirmDialog(null, "This will load template values for a BSB satellite receiver.\n"
                + "All current settings will be cleared. Do you wish to continue?", APP_NAME, JOptionPane.YES_NO_OPTION);
        if (q == JOptionPane.YES_OPTION) {
            // Reset all controls
            resetAllControls();
            // Select D-MAC FM mode
            radMAC.doClick();
            int a = -1;
            for (int i = 0; i < MACModeArray.length; i++) {
                if (MACModeArray[i].equals("dmac-fm")) {
                    a = i;
                }
            }
            if (a != -1) {
                cmbMode.setSelectedIndex(a);
            }
            else {
                messageBox("Unable to find the DMAC-FM mode, which is required for this template.", JOptionPane.ERROR_MESSAGE);
                resetAllControls();
                return;
            }
            // Enable pre-emphasis filter and set FM deviation to 11 MHz
            int b = -1;
            chkVideoFilter.doClick();
            chkFMDev.doClick();
            txtFMDev.setText("11");
            // Set IF to Galaxy channel by looking it up in the frequency table
            for (int i = 0; i < FrequencyArray.length; i++) {
                if (FrequencyArray[i] == 1092560000) {
                    b = i;
                }
            }
            if (b != -1) {
                cmbChannel.setSelectedIndex(b);
            }
            else {
                messageBox("Unable to find the Galaxy channel, which is required for this template.", JOptionPane.ERROR_MESSAGE);
                resetAllControls();
                return;
            }
            
            messageBox("Template values have been loaded. Tune your receiver to the Galaxy "
                    + "channel, or change this in the channel dropdown box on the Output tab.", JOptionPane.INFORMATION_MESSAGE);    
        }
    }//GEN-LAST:event_menuBSBTemplateActionPerformed

    private void btnHackTVPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHackTVPathActionPerformed
        // Retrieve the last used directory from the prefs store if it exists
        hacktvFileChooser.setCurrentDirectory(
            new File(Prefs.get("lasthtvdir", System.getProperty("user.home")))
        );
        hacktvFileChooser.setAcceptAllFileFilterUsed(true);
        int returnVal = hacktvFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // Save the chosen directory to prefs
            Prefs.put("lasthtvdir", hacktvFileChooser.getCurrentDirectory().toString());
            File file = hacktvFileChooser.getSelectedFile();
            HackTVPath = SharedInst.stripQuotes(file.toString());
            txtHackTVPath.setText(HackTVPath);
            // Store the specified path.
            Prefs.put("HackTVPath", HackTVPath);
            // Load the full path to a variable so we can use getParent on it
            // and get its parent directory path
            HackTVDirectory = new File(HackTVPath).getParent();
            // Detect what were provided with
            detectFork();   
            selectModesFile();
            openModesFile();
            cmbRegion.setEnabled(false);
            populateVideoModes();
            selectDefaultMode();
            if (CaptainJack) {
                captainJack();
            }
            else {
                fsphil();
            }
            addTestCardOptions();
        }
    }//GEN-LAST:event_btnHackTVPathActionPerformed

    private void menuOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenActionPerformed
        int result = configFileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            SelectedFile = configFileChooser.getSelectedFile();
            SelectedFile = new File(SharedInst.stripQuotes(SelectedFile.toString()));
            checkSelectedFile(SelectedFile);
        }
    }//GEN-LAST:event_menuOpenActionPerformed

    private void menuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveAsActionPerformed
        saveFilePrompt();
    }//GEN-LAST:event_menuSaveAsActionPerformed

    private void menuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveActionPerformed
        if (menuSave.getText().contains("...")) {
            saveFilePrompt();
        }
        else {
            saveConfigFile(SelectedFile);
        }
    }//GEN-LAST:event_menuSaveActionPerformed

    private void menuMRUFile2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile2ActionPerformed
        SelectedFile = new File(menuMRUFile2.getText());
        checkSelectedFile(SelectedFile);
    }//GEN-LAST:event_menuMRUFile2ActionPerformed

    private void menuMRUFile1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile1ActionPerformed
        SelectedFile = new File(menuMRUFile1.getText());
        checkSelectedFile(SelectedFile);
    }//GEN-LAST:event_menuMRUFile1ActionPerformed

    private void menuMRUFile3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile3ActionPerformed
        SelectedFile = new File(menuMRUFile3.getText());
        checkSelectedFile(SelectedFile);
    }//GEN-LAST:event_menuMRUFile3ActionPerformed

    private void menuMRUFile4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile4ActionPerformed
        SelectedFile = new File(menuMRUFile4.getText());
        checkSelectedFile(SelectedFile);
    }//GEN-LAST:event_menuMRUFile4ActionPerformed

    private void btnClearMRUListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearMRUListActionPerformed
        int q = JOptionPane.showConfirmDialog(null, "This will clear the list of most recently used "
                + "files from the File menu. Do you wish to continue?", APP_NAME, JOptionPane.YES_NO_OPTION);
        if (q == JOptionPane.YES_OPTION) {
            if ( Prefs.get("File1", null) != null ) Prefs.remove("File1");
            if ( Prefs.get("File2", null) != null ) Prefs.remove("File2");
            if ( Prefs.get("File3", null) != null ) Prefs.remove("File3");
            if ( Prefs.get("File4", null) != null ) Prefs.remove("File4");
            checkMRUList();
        }
    }//GEN-LAST:event_btnClearMRUListActionPerformed

    private void btnResetAllSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetAllSettingsActionPerformed
        int q = JOptionPane.showConfirmDialog(null, "This will remove all of this application's "
                + "saved settings and exit. Do you wish to continue?", APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (q == JOptionPane.YES_OPTION) {
            resetPreferences();
        }
    }//GEN-LAST:event_btnResetAllSettingsActionPerformed

    private void chkSyntaxOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSyntaxOnlyActionPerformed
        if (chkSyntaxOnly.isSelected()) {
            btnRun.setText("Generate syntax");
        }
        else {
            btnRun.setText("Run hacktv");
        }
    }//GEN-LAST:event_chkSyntaxOnlyActionPerformed

    private void cmbOutputDeviceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbOutputDeviceActionPerformed
        String ModeChanged = "The selected video mode has been changed "
                + "because this output device does not support it. Please select another mode.";
        boolean bb = INI.getStringFromINI(ModesFile, Mode, "modulation", "", false).equals("baseband");
        if (!txtOutputDevice.getText().isBlank()) txtOutputDevice.setText("");
        switch(cmbOutputDevice.getSelectedIndex()) {
            // hackrf
            case 0:
                lblOutputDevice2.setText("Serial number (optional)");
                if (!radCustom.isEnabled()) {
                    // If a baseband mode is selected, reset it to something else
                    if (bb) {
                        messageBox(ModeChanged, JOptionPane.WARNING_MESSAGE);
                        cmbMode.setSelectedIndex(0);
                    }
                    // If the RF panel is disabled, enable it and call checkMode
                    // to re-populate the channel options correctly
                    enableRFOptions();
                    checkMode();
                }
                txtGain.setEnabled(true);
                txtGain.setText("0");
                lblGain.setEnabled(true);
                chkAmp.setEnabled(true);
                lblAntennaName.setEnabled(false);
                txtAntennaName.setEnabled(false);
                txtAntennaName.setText("");
                lblFileType.setEnabled(false);
                cmbFileType.setEnabled(false);
                cmbFileType.setSelectedIndex(-1);
                break;
            // soapysdr
            case 1:
                lblOutputDevice2.setText("Device options");
                if (!radCustom.isEnabled()) {
                    if (bb) {
                        messageBox(ModeChanged, JOptionPane.WARNING_MESSAGE);   
                        cmbMode.setSelectedIndex(0);
                    }
                    enableRFOptions();
                    checkMode();
                }
                txtGain.setEnabled(true);
                txtGain.setText("0");
                lblGain.setEnabled(true);
                chkAmp.setEnabled(false);
                lblAntennaName.setEnabled(true);
                txtAntennaName.setEnabled(true);
                lblFileType.setEnabled(false);
                cmbFileType.setEnabled(false);
                cmbFileType.setSelectedIndex(-1);
                break;
            // fl2k
            case 2:
                lblOutputDevice2.setText("Device number (optional)");
                // fl2k is baseband only for now so disable all RF options
                disableRFOptions();
                rfPanel.setEnabled(false);
                break;
            // Output to file
            case 3:
                lblOutputDevice2.setText("Destination file");
                disableRFOptions();
                rfPanel.setEnabled(true);
                lblFileType.setEnabled(true);
                cmbFileType.setEnabled(true);
                cmbFileType.setSelectedIndex(3);
                // Opens the save file dialogue, but only if selected by the user
                if (HTVLoadInProgress == false) {
                    // Retrieve the last used directory from the prefs store if it exists
                    outputFileChooser.setCurrentDirectory(
                        new File(Prefs.get("lastfdir", System.getProperty("user.home")))
                    );
                    int result = outputFileChooser.showSaveDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        // Save the chosen directory to prefs
                        Prefs.put("lastfdir", outputFileChooser.getCurrentDirectory().toString());
                        File o = outputFileChooser.getSelectedFile();
                        txtOutputDevice.setText(o.toString());
                    }
                }
                break;
            default:
                System.err.println("Output device error");
                break;
        }
    }//GEN-LAST:event_cmbOutputDeviceActionPerformed

    private void chkVolumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkVolumeActionPerformed
        if (chkVolume.isSelected()) {
            txtVolume.setEnabled(true);
        }
        else {
            txtVolume.setEnabled(false);
            txtVolume.setText("");
        }
    }//GEN-LAST:event_chkVolumeActionPerformed

    private void chkTextSubtitlesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTextSubtitlesActionPerformed
        if (chkTextSubtitles.isSelected()) {
            lblTextSubtitleIndex.setEnabled(true);
            txtTextSubtitleIndex.setEnabled(true); 
        }
        else {
            txtTextSubtitleIndex.setEnabled(false);
            txtTextSubtitleIndex.setText("");
            lblTextSubtitleIndex.setEnabled(false);
        }
    }//GEN-LAST:event_chkTextSubtitlesActionPerformed

    private void chkA2StereoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkA2StereoActionPerformed
        if (chkA2Stereo.isSelected()) {
            if (chkNICAM.isSelected()) {
                chkNICAM.doClick();
            }
            else {
            }
        }
    }//GEN-LAST:event_chkA2StereoActionPerformed

    private void chkLocalModesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLocalModesActionPerformed
        if (chkLocalModes.isSelected()) {
            Prefs.putInt("UseLocalModesFile", 1);
        }
        else {
            Prefs.putInt("UseLocalModesFile", 0);
        }
    }//GEN-LAST:event_chkLocalModesActionPerformed

    private void chkFMDevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkFMDevActionPerformed
        if (chkFMDev.isSelected()) {
            txtFMDev.setEnabled(true);
        }
        else {
            txtFMDev.setText("");
            txtFMDev.setEnabled(false);
        }
    }//GEN-LAST:event_chkFMDevActionPerformed

    private void lstPlaylistValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstPlaylistValueChanged
        String playFirst = "Play first";
        String reset = "Reset";
        // Is the playlist empty?
        if (lstPlaylist.getSelectedIndex() == -1) {
            btnPlaylistUp.setEnabled(false);
            btnPlaylistDown.setEnabled(false);
            btnRemove.setEnabled(false);
            btnPlaylistStart.setEnabled(false);
            btnPlaylistStart.setText(playFirst);
        }
        // Are multiple items selected? If so, disable the up/down buttons
        else if (lstPlaylist.getSelectedIndices().length > 1) {
            btnPlaylistUp.setEnabled(false);
            btnPlaylistDown.setEnabled(false);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(false);
            btnPlaylistStart.setText(playFirst);
            chkRandom.setEnabled(true);
        }
        // Does the playlist contain only one item?
        else if ( (lstPlaylist.getSelectedIndex() == 0) && (PlaylistAL.size() == 1) ) {
            btnPlaylistUp.setEnabled(false);
            btnPlaylistDown.setEnabled(false);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(false);
            btnPlaylistStart.setText(playFirst);
            if (chkRandom.isSelected()) chkRandom.doClick();
            chkRandom.setEnabled(false);
        }
        // Is the selected item an intermediate item? (not the first or last)
        else if ( (lstPlaylist.getSelectedIndex() != 0) && (lstPlaylist.getSelectedIndex() != PlaylistAL.size() - 1) ) {
            btnPlaylistUp.setEnabled(true);
            btnPlaylistDown.setEnabled(true);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(true);
            if (lstPlaylist.getSelectedIndex() == StartPoint) {
                btnPlaylistStart.setText(reset);
            }
            else {
                btnPlaylistStart.setText(playFirst);
            }
            chkRandom.setEnabled(true);
        }
        // Is the first item in the playlist selected?
        else if ( (lstPlaylist.getSelectedIndex() == 0) && (PlaylistAL.size() > 1) ) {
            btnPlaylistUp.setEnabled(false);
            btnPlaylistDown.setEnabled(true);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(true);
            if (lstPlaylist.getSelectedIndex() == StartPoint) {
                btnPlaylistStart.setText(reset);
            }
            else {
                btnPlaylistStart.setText(playFirst);
            }
            chkRandom.setEnabled(true);
        }
        // Is the last item in the playlist selected?
        else if (lstPlaylist.getSelectedIndex() == PlaylistAL.size() - 1) {
            btnPlaylistUp.setEnabled(true);
            btnPlaylistDown.setEnabled(false);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(true);
            if (lstPlaylist.getSelectedIndex() == StartPoint) {
                btnPlaylistStart.setText(reset);
            }
            else {
                btnPlaylistStart.setText(playFirst);
            }
            chkRandom.setEnabled(true);
        }
    }//GEN-LAST:event_lstPlaylistValueChanged

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (cmbM3USource.isVisible()) {
            // Add the URL from the selected M3U item to the playlist
            PlaylistAL.add(PlaylistURLsAL.get(cmbM3USource.getSelectedIndex()));
        }
        // Don't add YouTube or other yt-dlp compatible URLs to the playlist
        else if ( (txtSource.getText().contains("://youtube.com/")) ||
                  (txtSource.getText().contains("://www.youtube.com/")) ||
                  (txtSource.getText().contains("://youtu.be/")) ||
                  (txtSource.getText().startsWith("ytdl:")) ) {
            messageBox("Unable to add this URL to the playlist.\n"
                        + "The yt-dlp handler is only supported for single URLs at present.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        else if ( (txtSource.isEnabled()) && (!txtSource.getText().isBlank()) ) {
            // Add whatever is in txtSource to PlaylistAL
            PlaylistAL.add(txtSource.getText());
        }
        else if (radTest.isSelected()) {
            for (String s : PlaylistAL) {
                if (s.startsWith("test:")) {
                    messageBox("Only one test card can be added to the playlist.\n"
                        + "It should also be placed as the last item in the playlist.", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (cmbTest.isEnabled()) {
                // Add the selected test card
                PlaylistAL.add("test:" + TCArray[cmbTest.getSelectedIndex()]);
            }
            else {
                // Add the test card
                PlaylistAL.add("test:colourbars");
            }
        }
        else {
            btnSourceBrowse.doClick();
            if (!txtSource.getText().isBlank()) btnAdd.doClick();
            return;
        }
        // Enable or disable random option
        if (PlaylistAL.size() > 1) {
            chkRandom.setEnabled(true);
        }
        else {
            if (chkRandom.isSelected()) chkRandom.doClick();
            chkRandom.setEnabled(false);
        }
        populatePlaylist();
        txtSource.setText("");
        lstPlaylist.setSelectedIndex(PlaylistAL.size() -1);
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int[] ia = lstPlaylist.getSelectedIndices();
        // Process the selection array in reverse order and remove the items from the arraylist
        for (int j = ia.length -1; j >= 0; j--) {
            // Remove the requested item from the arraylist
            PlaylistAL.remove(ia[j]);
            // If the item removed was the start point, or if only one item
            // is left, reset StartPoint to default
            if ((ia[j] == StartPoint) || (PlaylistAL.size() < 2)) {
                StartPoint = -1;
            }                
            // If the item removed was before the start point, reduce StartPoint
            // by one so the selected item remains selected
            else if (ia[j] < StartPoint) {
                StartPoint = StartPoint - 1;
            }
            // Re-populate the playlist with the new arraylist values
            populatePlaylist();
        }
        // If only one item was selected...
        if (ia.length == 1) {
            // If the last item in the list was selected, select whatever
            // was the second from last (and is now last).
            if (PlaylistAL.size() == ia[0]) {
                lstPlaylist.setSelectedIndex(ia[0] - 1);
            }
            // Otherwise, select the item that corresponds to the same index
            // as the item we removed.
            else {
                lstPlaylist.setSelectedIndex(ia[0]);
            }
        }
        // If the Remove button has been disabled, highlight the Add button
        // instead. Otherwise, re-select Remove.
        if (btnRemove.isEnabled()) {
            btnRemove.requestFocusInWindow();
        }
        else {
            btnAdd.requestFocusInWindow();
        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnPlaylistUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlaylistUpActionPerformed
        int i = lstPlaylist.getSelectedIndex();
        // If the item above the selected item is the start point, shift the
        // start point up by one so the selected start point will remain selected
        if (i - 1 == StartPoint) {
            StartPoint = StartPoint + 1;
        }
        // If the selected item is the start point, shift the start point down
        // by one so the selected start point will remain selected
        else if (i == StartPoint) {
            StartPoint = StartPoint - 1;
        }
        if (i > 0) {
            PlaylistAL.add(i - 1, PlaylistAL.get(i));
            PlaylistAL.remove(i + 1);
            populatePlaylist();
            lstPlaylist.setSelectedIndex(i - 1);
            lstPlaylist.ensureIndexIsVisible(lstPlaylist.getSelectedIndex());
        }
        btnPlaylistDown.setEnabled(true);
        
        if (i == 1) {
            // As we have reached the top of the list, disable the Up button
            btnPlaylistUp.setEnabled(false);
        }
        else {
            btnPlaylistUp.requestFocusInWindow();
        }
    }//GEN-LAST:event_btnPlaylistUpActionPerformed

    private void btnPlaylistDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlaylistDownActionPerformed
        int i = lstPlaylist.getSelectedIndex();
        // If the item below the selected item is the start point, shift the
        // start point down by one so the selected start point will remain selected
        if (i + 1 == StartPoint) {
            StartPoint = StartPoint - 1;
        }
        // If the selected item is the start point, shift the start point up
        // by one so the selected start point will remain selected
        else if (i == StartPoint) {
            StartPoint = StartPoint + 1;
        }
        if ( (i >= 0) && (i != PlaylistAL.size() - 1) ) {
            PlaylistAL.add(i + 2, PlaylistAL.get(i));
            PlaylistAL.remove(i);
            populatePlaylist();
            lstPlaylist.setSelectedIndex(i + 1);
            lstPlaylist.ensureIndexIsVisible(lstPlaylist.getSelectedIndex());
        }
        btnPlaylistUp.setEnabled(true);
        if (i == PlaylistAL.size() - 2) {
            // As we have reached the bottom of the list, disable the Down button
            btnPlaylistDown.setEnabled(false);
        }
        else {
            btnPlaylistDown.requestFocusInWindow();
        }
    }//GEN-LAST:event_btnPlaylistDownActionPerformed

    private void cmbModeMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbModeMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbMode);
    }//GEN-LAST:event_cmbModeMouseWheelMoved

    private void cmbWSSMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbWSSMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbWSS);
    }//GEN-LAST:event_cmbWSSMouseWheelMoved

    private void cmbLogoMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbLogoMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbLogo);
    }//GEN-LAST:event_cmbLogoMouseWheelMoved

    private void cmbARCorrectionMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbARCorrectionMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbARCorrection);
    }//GEN-LAST:event_cmbARCorrectionMouseWheelMoved

    private void cmbOutputDeviceMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbOutputDeviceMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbOutputDevice);
    }//GEN-LAST:event_cmbOutputDeviceMouseWheelMoved

    private void cmbChannelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbChannelMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbChannel);
    }//GEN-LAST:event_cmbChannelMouseWheelMoved

    private void cmbFileTypeMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbFileTypeMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbFileType);
    }//GEN-LAST:event_cmbFileTypeMouseWheelMoved

    private void cmbScramblingTypeMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbScramblingTypeMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbScramblingType);
    }//GEN-LAST:event_cmbScramblingTypeMouseWheelMoved

    private void cmbScramblingKey1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbScramblingKey1MouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbScramblingKey1);
    }//GEN-LAST:event_cmbScramblingKey1MouseWheelMoved

    private void cmbScramblingKey2MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbScramblingKey2MouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbScramblingKey2);
    }//GEN-LAST:event_cmbScramblingKey2MouseWheelMoved

    private void cmbSysterPermTableMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbSysterPermTableMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbSysterPermTable);
    }//GEN-LAST:event_cmbSysterPermTableMouseWheelMoved

    private void cmbTestMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbTestMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbTest);
    }//GEN-LAST:event_cmbTestMouseWheelMoved

    private void btnPlaylistStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlaylistStartActionPerformed
        // Don't set a test card as the start point of the playlist.
        // It never ends, so the playlist becomes pointless.
        int s = lstPlaylist.getSelectedIndex();
        if (PlaylistAL.get(s).startsWith("test:")) {
            messageBox("Test cards cannot be set as the start point of a playlist.", JOptionPane.WARNING_MESSAGE);
        }
        else if (s == StartPoint) {
            // Reset the start point
            StartPoint = -1;
            populatePlaylist();
        }
        else {
            // Set the start point
            StartPoint = s;
            populatePlaylist();
        }
        // Reselect the item that was selected before the playlist was updated
        lstPlaylist.setSelectedIndex(s);
        btnPlaylistStart.requestFocusInWindow();
    }//GEN-LAST:event_btnPlaylistStartActionPerformed

    private void lstPlaylistKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lstPlaylistKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            btnRemove.doClick();
            lstPlaylist.requestFocusInWindow();
        }
    }//GEN-LAST:event_lstPlaylistKeyPressed

    private void btnDownloadHackTVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownloadHackTVActionPerformed
        SwingWorker<String, Void> downloadHackTV = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    createTempDirectory();
                    String t = TempDir.toString();
                    String downloadPath = t + File.separator + "hacktv.zip";
                    String tmpExePath = t + File.separator + "hacktv.exe";
                    String exePath = JarDir + File.separator + "hacktv.exe";
                    // Download hacktv.zip from Captain Jack
                    SharedInst.download("https://filmnet.plus/hacktv/hacktv.zip", downloadPath);
                    // Unzip what we got to the temp directory
                    SharedInst.UnzipFile(downloadPath, t);
                    // If hacktv.exe exists in the temp directory, delete the zip
                    // and attempt to move it to the working directory
                    if (Files.exists(Path.of(tmpExePath))) {
                        SharedInst.deleteFSObject(Path.of(downloadPath));
                        Files.move(Path.of(tmpExePath), Path.of(exePath), StandardCopyOption.REPLACE_EXISTING);
                        return exePath;
                    }
                    else {
                        return null;
                    }
                }
                catch (IOException ex) {
                    System.err.println(ex);
                    var err = new StringWriter();
                    ex.printStackTrace(new PrintWriter(err));
                    if (err.toString().contains("CertificateExpiredException")) {
                        return "CertificateExpiredException";
                    }
                    else {
                        return null;
                    }
                }
            } // End doInBackground()
            @Override
            protected void done() {
                // Retrieve the return value of doInBackground.
                String exePath;
                try {
                    exePath = get();
                }
                catch (InterruptedException | ExecutionException ex) {
                    exePath = null;   
                }
                if (exePath != null) {
                    // Set location of hacktv so we can find it later
                    if (Files.exists(Path.of(exePath))) {
                        HackTVPath = exePath;
                        txtHackTVPath.setText(exePath);
                        // Store the specified path in the preferences store.
                        Prefs.put("HackTVPath", HackTVPath);
                        // Load the full path to a variable so we can use getParent on it
                        // and get its parent directory path
                        HackTVDirectory = new File(HackTVPath).getParent();    
                        // Detect what were provided with
                        detectFork();
                        selectModesFile();
                        openModesFile();
                        if (CaptainJack) {
                            captainJack();
                        }
                        else {
                            fsphil();
                        }
                        addTestCardOptions();
                    }
                }
                else if ( (exePath != null) && (exePath.equals("CertificateExpiredException")) ) {
                    messageBox("Download failed due to an expired SSL/TLS certificate.\n"
                            + "Please ensure that your system date is correct. "
                            + "Otherwise, please try again later.", JOptionPane.WARNING_MESSAGE);
                }
                else {
                    messageBox("An error occurred while downloading hacktv.\n"
                            + "Please ensure that you have write permissions to the "
                            + "application directory and that you have internet access.", JOptionPane.WARNING_MESSAGE);
                }
                btnDownloadHackTV.setEnabled(true);
            } // End done()
        }; // End SwingWorker
        int q = JOptionPane.showConfirmDialog(null, 
                "Would you like to download the latest build of hacktv from Captain Jack's repository?\n"
                    + "This requires an internet connection and will only work if you have write access "
                    + "to the directory where this application is located.", APP_NAME, JOptionPane.YES_NO_OPTION);
        if (q == JOptionPane.YES_OPTION) {
            btnDownloadHackTV.setEnabled(false);
            downloadHackTV.execute();
        }
    }//GEN-LAST:event_btnDownloadHackTVActionPerformed

    private void menuGithubRepoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuGithubRepoActionPerformed
        String u = "https://github.com/steeviebops/hacktv-gui/";
        try {
            SharedInst.launchBrowser(u);
        }
        catch (IOException | UnsupportedOperationException e) {
            messageBox("Unable to launch default browser.", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_menuGithubRepoActionPerformed

    private void cmbLookAndFeelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbLookAndFeelActionPerformed
        if (this.isVisible()) changeLaF(cmbLookAndFeel.getSelectedIndex());
    }//GEN-LAST:event_cmbLookAndFeelActionPerformed

    private void chkECppvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkECppvActionPerformed
        if (chkECppv.isSelected()) {
            lblECprognum.setEnabled(true);
            txtECprognum.setEnabled(true);
            lblECprogcost.setEnabled(true);
            txtECprogcost.setEnabled(true);
        }
        else {
            lblECprognum.setEnabled(false);
            txtECprognum.setText("");
            txtECprognum.setEnabled(false);
            lblECprogcost.setEnabled(false);
            txtECprogcost.setText("");
            txtECprogcost.setEnabled(false);
        }
    }//GEN-LAST:event_chkECppvActionPerformed

    private void txtGammaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtGammaKeyTyped
        if (evt.getKeyChar() == '\u002e') {
            if (txtGamma.getText().contains(".")) {
                evt.consume();
            }
        }
        else if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtGammaKeyTyped

    private void txtOutputLevelKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtOutputLevelKeyTyped
        if (evt.getKeyChar() == '\u002e') {
            if (txtOutputLevel.getText().contains(".")) {
                evt.consume();
            }
        }
        else if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtOutputLevelKeyTyped

    private void txtVolumeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtVolumeKeyTyped
        if (evt.getKeyChar() == '\u002e') {
            if (txtVolume.getText().contains(".")) {
                evt.consume();
            }
        }
        else if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtVolumeKeyTyped

    private void txtSampleRateKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSampleRateKeyTyped
        if (evt.getKeyChar() == '\u002e') {
            if (txtSampleRate.getText().contains(".")) {
                evt.consume();
            }
        }
        else if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtSampleRateKeyTyped

    private void txtPixelRateKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPixelRateKeyTyped
        if (evt.getKeyChar() == '\u002e') {
            if (txtPixelRate.getText().contains(".")) {
                evt.consume();
            }
        }
        else if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtPixelRateKeyTyped

    private void txtFMDevKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFMDevKeyTyped
        if (evt.getKeyChar() == '\u002e') {
            if (txtFMDev.getText().contains(".")) {
                evt.consume();
            }
        }
        else if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtFMDevKeyTyped

    private void txtGainKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtGainKeyTyped
        if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtGainKeyTyped

    private void txtSubtitleIndexKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSubtitleIndexKeyTyped
        if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtSubtitleIndexKeyTyped

    private void txtPositionKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPositionKeyTyped
        if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtPositionKeyTyped

    private void txtTextSubtitleIndexKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTextSubtitleIndexKeyTyped
        if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtTextSubtitleIndexKeyTyped

    private void txtECprognumKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtECprognumKeyTyped
        if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtECprognumKeyTyped

    private void txtECprogcostKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtECprogcostKeyTyped
        if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtECprogcostKeyTyped

    private void menuWikiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuWikiActionPerformed
        String u = "https://github.com/steeviebops/hacktv-gui/wiki";
        try {
            SharedInst.launchBrowser(u);
        }
        catch (IOException | UnsupportedOperationException e) {
            messageBox("Unable to launch default browser.", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_menuWikiActionPerformed

    private void cmbNMSCeefaxRegionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbNMSCeefaxRegionActionPerformed
        Prefs.putInt("CeefaxRegion", cmbNMSCeefaxRegion.getSelectedIndex());
    }//GEN-LAST:event_cmbNMSCeefaxRegionActionPerformed

    private void btnNMSCeefaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNMSCeefaxActionPerformed
        if ((btnNMSCeefax.getText()).contains("Stop")) {
            DownloadCancelled = true;
        }
        else {
            btnNMSCeefax.setText("Stop");
            chkTeletext.setEnabled(false);
            txtTeletextSource.setEnabled(false);
            btnTeletextBrowse.setEnabled(false);
            btnTeefax.setEnabled(false);
            lblTeefax.setEnabled(false);
            btnSpark.setEnabled(false);
            lblSpark.setEnabled(false);
            btnRun.setEnabled(false);
            DownloadCancelled = false;
            // Set variables
            String dUrl = "https://internal.nathanmediaservices.co.uk/svn/ceefax/national/";
            String HTMLString = "name=\"(.*?)\"";
            HTMLTempFile = "ceefax_national.xml";
            // Download index page
            downloadTeletext(dUrl, HTMLTempFile, HTMLString);
        }
    }//GEN-LAST:event_btnNMSCeefaxActionPerformed

    private void cmbLookAndFeelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbLookAndFeelMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbLookAndFeel);
    }//GEN-LAST:event_cmbLookAndFeelMouseWheelMoved

    private void cmbNMSCeefaxRegionMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbNMSCeefaxRegionMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbNMSCeefaxRegion);
    }//GEN-LAST:event_cmbNMSCeefaxRegionMouseWheelMoved

    private void cmbRegionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbRegionActionPerformed
        int i = cmbRegion.getSelectedIndex();
        if (i == -1) return;
        if (i == 0) {
            if (radUHF.isSelected()) populateBandPlan("uhf");
            if (radVHF.isSelected()) populateBandPlan("vhf");
        }
        else {
            if (radUHF.isSelected()) populateBandPlan("uhf" + (i + 1));
            if (radVHF.isSelected()) populateBandPlan("vhf" + (i + 1));
        }
    }//GEN-LAST:event_cmbRegionActionPerformed

    private void cmbRegionMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbRegionMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbRegion);
    }//GEN-LAST:event_cmbRegionMouseWheelMoved

    private void chkWindowsKillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkWindowsKillActionPerformed
        if (chkWindowsKill.isSelected()) {
            Prefs.putInt("windows-kill", 1);
        }
        else {
            Prefs.putInt("windows-kill", 0);
        }
    }//GEN-LAST:event_chkWindowsKillActionPerformed

    private void cmbM3USourceMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbM3USourceMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbM3USource);
    }//GEN-LAST:event_cmbM3USourceMouseWheelMoved

    private void cmbECMaturityMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbECMaturityMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbECMaturity);
    }//GEN-LAST:event_cmbECMaturityMouseWheelMoved

    private void menuDownloadUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuDownloadUpdateActionPerformed
        try {
            SharedInst.launchBrowser("https://github.com/steeviebops/hacktv-gui/releases/latest");
        }
        catch (IOException | UnsupportedOperationException e) {
            messageBox("Unable to launch default browser.", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_menuDownloadUpdateActionPerformed

    private void chkNoUpdateCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNoUpdateCheckActionPerformed
       if (chkNoUpdateCheck.isSelected()) {
           Prefs.putInt("noupdatecheck", 1);
       }
       else {
           Prefs.putInt("noupdatecheck", 0);
       }
    }//GEN-LAST:event_chkNoUpdateCheckActionPerformed

    private void menuUpdateCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuUpdateCheckActionPerformed
        checkForUpdates(false);
    }//GEN-LAST:event_menuUpdateCheckActionPerformed

    private void menuKuBandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuKuBandActionPerformed
        // Disable harmonic options on BSB receivers until I get the chance to test
        menuBSBTemplate.setEnabled(false);
        if (Prefs.getInt("SuppressWarnings", 0) != 1) {
            messageBox("Care is advised when using harmonics.\n" +
                    "Please be aware of local laws regarding interference.", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_menuKuBandActionPerformed

    private void menuKaBandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuKaBandActionPerformed
        // Disable harmonic options on BSB receivers until I get the chance to test
        menuBSBTemplate.setEnabled(false);
        if (Prefs.getInt("SuppressWarnings", 0) != 1) {
            messageBox("Care is advised when using harmonics.\n" +
                    "Please be aware of local laws regarding interference.", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_menuKaBandActionPerformed

    private void menuIFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuIFActionPerformed
        menuBSBTemplate.setEnabled(true);
    }//GEN-LAST:event_menuIFActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AdditionalOptionsPanel;
    private javax.swing.ButtonGroup BandButtonGroup;
    private javax.swing.JPanel FrequencyPanel;
    private javax.swing.ButtonGroup MacCompressionButtonGroup;
    private javax.swing.ButtonGroup MacProtectionButtonGroup;
    private javax.swing.ButtonGroup MacSRButtonGroup;
    private javax.swing.ButtonGroup MacStereoButtonGroup;
    private javax.swing.JPanel PlaybackTab;
    private javax.swing.JPanel RunButtonGrid;
    private javax.swing.ButtonGroup SourceButtonGroup;
    private javax.swing.JPanel SourcePanel;
    private javax.swing.ButtonGroup TemplateButtonGroup;
    private javax.swing.JPanel VBIPanel;
    private javax.swing.ButtonGroup VideoFormatButtonGroup;
    private javax.swing.JPanel VideoFormatPanel;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClearMRUList;
    private javax.swing.JButton btnDownloadHackTV;
    private javax.swing.JButton btnHackTVPath;
    private javax.swing.JButton btnNMSCeefax;
    private javax.swing.JButton btnPlaylistDown;
    private javax.swing.JButton btnPlaylistStart;
    private javax.swing.JButton btnPlaylistUp;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnResetAllSettings;
    private javax.swing.JButton btnRun;
    private javax.swing.JButton btnSourceBrowse;
    private javax.swing.JButton btnSpark;
    private javax.swing.JButton btnTeefax;
    private javax.swing.JButton btnTeletextBrowse;
    private javax.swing.JCheckBox chkA2Stereo;
    private javax.swing.JCheckBox chkACP;
    private javax.swing.JCheckBox chkARCorrection;
    private javax.swing.JCheckBox chkActivateCard;
    private javax.swing.JCheckBox chkAmp;
    private javax.swing.JCheckBox chkAudio;
    private javax.swing.JCheckBox chkColour;
    private javax.swing.JCheckBox chkDeactivateCard;
    private javax.swing.JCheckBox chkDownmix;
    private javax.swing.JCheckBox chkECppv;
    private javax.swing.JCheckBox chkFMDev;
    private javax.swing.JCheckBox chkFindKeys;
    private javax.swing.JCheckBox chkGamma;
    private javax.swing.JCheckBox chkInterlace;
    private javax.swing.JCheckBox chkInvertVideo;
    private javax.swing.JCheckBox chkLocalModes;
    private javax.swing.JCheckBox chkLogo;
    private javax.swing.JCheckBox chkMacChId;
    private javax.swing.JCheckBox chkNICAM;
    private javax.swing.JCheckBox chkNoDate;
    private javax.swing.JCheckBox chkNoUpdateCheck;
    private javax.swing.JCheckBox chkOutputLevel;
    private javax.swing.JCheckBox chkPixelRate;
    private javax.swing.JCheckBox chkPosition;
    private javax.swing.JCheckBox chkRandom;
    private javax.swing.JCheckBox chkRepeat;
    private javax.swing.JCheckBox chkScrambleAudio;
    private javax.swing.JCheckBox chkShowCardSerial;
    private javax.swing.JCheckBox chkShowECM;
    private javax.swing.JCheckBox chkSubtitles;
    private javax.swing.JCheckBox chkSyntaxOnly;
    private javax.swing.JCheckBox chkTeletext;
    private javax.swing.JCheckBox chkTextSubtitles;
    private javax.swing.JCheckBox chkTimestamp;
    private javax.swing.JCheckBox chkVITC;
    private javax.swing.JCheckBox chkVITS;
    private javax.swing.JCheckBox chkVerbose;
    private javax.swing.JCheckBox chkVideoFilter;
    private javax.swing.JCheckBox chkVolume;
    private javax.swing.JCheckBox chkWSS;
    private javax.swing.JCheckBox chkWindowsKill;
    private javax.swing.JComboBox<String> cmbARCorrection;
    private javax.swing.JComboBox<String> cmbChannel;
    private javax.swing.JComboBox<String> cmbECMaturity;
    private javax.swing.JComboBox<String> cmbFileType;
    private javax.swing.JComboBox<String> cmbLogo;
    private javax.swing.JComboBox<String> cmbLookAndFeel;
    private javax.swing.JComboBox<String> cmbM3USource;
    private javax.swing.JComboBox<String> cmbMode;
    private javax.swing.JComboBox<String> cmbNMSCeefaxRegion;
    private javax.swing.JComboBox<String> cmbOutputDevice;
    private javax.swing.JComboBox<String> cmbRegion;
    private javax.swing.JComboBox<String> cmbScramblingKey1;
    private javax.swing.JComboBox<String> cmbScramblingKey2;
    private javax.swing.JComboBox<String> cmbScramblingType;
    private javax.swing.JComboBox<String> cmbSysterPermTable;
    private javax.swing.JComboBox<String> cmbTest;
    private javax.swing.JComboBox<String> cmbWSS;
    private javax.swing.JFileChooser configFileChooser;
    private javax.swing.JPanel consoleOutputPanel;
    private javax.swing.JScrollPane consoleScrollPane;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JPanel downloadPanel;
    private javax.swing.JPanel emmPanel;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPanel generalSettingsPanel;
    private javax.swing.JFileChooser hacktvFileChooser;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel lblAntennaName;
    private javax.swing.JLabel lblChannel;
    private javax.swing.JLabel lblClearAll;
    private javax.swing.JLabel lblClearMRU;
    private javax.swing.JLabel lblDetectedBuikd;
    private javax.swing.JLabel lblDownload;
    private javax.swing.JLabel lblECMaturity;
    private javax.swing.JLabel lblECprogcost;
    private javax.swing.JLabel lblECprognum;
    private javax.swing.JLabel lblEMMCardNumber;
    private javax.swing.JLabel lblFileType;
    private javax.swing.JLabel lblFork;
    private javax.swing.JLabel lblFrequency;
    private javax.swing.JLabel lblGain;
    private javax.swing.JLabel lblLookAndFeel;
    private javax.swing.JLabel lblMacAudioMode;
    private javax.swing.JLabel lblMacCompression;
    private javax.swing.JLabel lblMacProtection;
    private javax.swing.JLabel lblMacSampleRate;
    private javax.swing.JLabel lblNMSCeefax;
    private javax.swing.JLabel lblNMSCeefaxRegion;
    private javax.swing.JLabel lblOutputDevice;
    private javax.swing.JLabel lblOutputDevice2;
    private javax.swing.JLabel lblSampleRate;
    private javax.swing.JLabel lblScramblingKey;
    private javax.swing.JLabel lblScramblingSystem;
    private javax.swing.JLabel lblSpark;
    private javax.swing.JLabel lblSpecifyLocation;
    private javax.swing.JLabel lblSubtitleIndex;
    private javax.swing.JLabel lblSysterPermTable;
    private javax.swing.JLabel lblTeefax;
    private javax.swing.JLabel lblTextSubtitleIndex;
    private javax.swing.JLabel lblVC2ScramblingKey;
    private javax.swing.JList<String> lstPlaylist;
    private javax.swing.JPanel macPanel;
    private javax.swing.JMenuItem menuAbout;
    private javax.swing.JMenuItem menuAstra10Template;
    private javax.swing.JMenuItem menuAstra975Template;
    private javax.swing.JMenuItem menuBSBTemplate;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuDownloadUpdate;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenu menuFreqSelect;
    private javax.swing.JMenuItem menuGithubRepo;
    private javax.swing.JRadioButtonMenuItem menuIF;
    private javax.swing.JRadioButtonMenuItem menuKaBand;
    private javax.swing.JRadioButtonMenuItem menuKuBand;
    private javax.swing.JMenuItem menuMRUFile1;
    private javax.swing.JMenuItem menuMRUFile2;
    private javax.swing.JMenuItem menuMRUFile3;
    private javax.swing.JMenuItem menuMRUFile4;
    private javax.swing.JMenuItem menuNew;
    private javax.swing.JMenuItem menuOpen;
    private javax.swing.JMenuItem menuSave;
    private javax.swing.JMenuItem menuSaveAs;
    private javax.swing.JMenuItem menuUpdateCheck;
    private javax.swing.JMenuItem menuWiki;
    private javax.swing.JFileChooser outputFileChooser;
    private javax.swing.JPanel outputTab;
    private javax.swing.JPanel pathPanel;
    private javax.swing.JProgressBar pbTeletext;
    private javax.swing.JScrollPane playlistScrollPane;
    private javax.swing.JRadioButton radBW;
    private javax.swing.JRadioButton radCustom;
    private javax.swing.JRadioButton radLocalSource;
    private javax.swing.JRadioButton radMAC;
    private javax.swing.JRadioButton radMac16k;
    private javax.swing.JRadioButton radMac32k;
    private javax.swing.JRadioButton radMacCompanded;
    private javax.swing.JRadioButton radMacL1;
    private javax.swing.JRadioButton radMacL2;
    private javax.swing.JRadioButton radMacLinear;
    private javax.swing.JRadioButton radMacMono;
    private javax.swing.JRadioButton radMacStereo;
    private javax.swing.JRadioButton radNTSC;
    private javax.swing.JRadioButton radPAL;
    private javax.swing.JRadioButton radSECAM;
    private javax.swing.JRadioButton radTest;
    private javax.swing.JRadioButton radUHF;
    private javax.swing.JRadioButton radVHF;
    private javax.swing.JPanel resetSettingsPanel;
    private javax.swing.JPanel rfPanel;
    private javax.swing.JPanel scramblingOptionsPanel;
    private javax.swing.JPanel scramblingPanel;
    private javax.swing.JPanel scramblingTab;
    private javax.swing.JPopupMenu.Separator sepAboutSeparator;
    private javax.swing.JPopupMenu.Separator sepExitSeparator;
    private javax.swing.JPopupMenu.Separator sepMruSeparator;
    private javax.swing.JPanel settingsTab;
    private javax.swing.JFileChooser sourceFileChooser;
    private javax.swing.JPanel sourceTab;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JFileChooser teletextFileChooser;
    private javax.swing.JPanel teletextPanel;
    private javax.swing.JPanel teletextTab;
    private javax.swing.JMenu templatesMenu;
    private javax.swing.JTextField txtAntennaName;
    private javax.swing.JTextField txtCardNumber;
    private javax.swing.JTextArea txtConsoleOutput;
    private javax.swing.JTextField txtECprogcost;
    private javax.swing.JTextField txtECprognum;
    private javax.swing.JTextField txtFMDev;
    private javax.swing.JTextField txtFrequency;
    private javax.swing.JTextField txtGain;
    private javax.swing.JTextField txtGamma;
    private javax.swing.JTextField txtHackTVPath;
    private javax.swing.JTextField txtMacChId;
    private javax.swing.JTextField txtOutputDevice;
    private javax.swing.JTextField txtOutputLevel;
    private javax.swing.JTextField txtPixelRate;
    private javax.swing.JTextField txtPosition;
    private javax.swing.JTextField txtSampleRate;
    private javax.swing.JTextField txtSource;
    private javax.swing.JTextField txtStatus;
    private javax.swing.JTextField txtSubtitleIndex;
    private javax.swing.JTextField txtTeletextSource;
    private javax.swing.JTextField txtTextSubtitleIndex;
    private javax.swing.JTextField txtVolume;
    private javax.swing.JMenu updateMenu;
    // End of variables declaration//GEN-END:variables
}
