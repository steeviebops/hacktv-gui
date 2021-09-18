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
import java.util.prefs.Preferences;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.SwingWorker;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JComboBox;
import javax.swing.UnsupportedLookAndFeelException;

public class GUI extends javax.swing.JFrame {    
    // Application name
    final String AppName = "hacktv-gui";
    
    // Boolean used for Microsoft Windows detection and handling
    final boolean RunningOnWindows;
    
    // Get user's home directory, used for file open dialogues
    final String UserHomeDir = System.getProperty("user.home");
    
    // String to set the directory where this application's JAR is located
    final Path JarDir;
    
    // Strings to set the location and contents of the Modes.ini file
    String ModesFilePath;
    String ModesFile;
    String ModesFileVersion;
    String ModesFileLocation;
    
    // Declare variables for supported features
    boolean NICAMSupported = false;
    boolean A2Supported = false;
    boolean ACPSupported = false;

    // Declare a variable to determine the selected fork
    String Fork;

    // Declare Teletext-related variables that are reused across multiple subs
    String DownloadURL;
    String HTMLTempFile;
    String HTMLFile;
    String HTMLString;
    File SelectedFile;
    Path TempDir;
    String TeletextPath;
    boolean DownloadInProgress = false;
    boolean DownloadCancelled = false;

    // Declare variables used for path resolution
    String HackTVPath;
    String HackTVDirectory;
    final String DefaultHackTVPath;
    final String OS_SEP;

    // Declare variable for the title bar display
    String TitleBar;
    boolean TitleBarChanged = false;

    // Array used for M3U files
    ArrayList<String> PlaylistURLsAL;
    String[] PlaylistNames;

    /* Declare a variable for storing the default sample rate for the selected video mode
     * This allows us to revert back to the default if the sample rate is changed by filters or scrambling systems
     * FMSampleRate specifies the recommended sample rate for the pre-emphasis filter
     */
    String DefaultSampleRate;
    
    // Declare combobox arrays and ArrayLists
    // These are used to store secondary information (frequencies, parameters, etc)
    int[] FrequencyArray;
    String[] PALModeArray;
    String[] NTSCModeArray;
    String[] SECAMModeArray;
    String[] OtherModeArray;
    String[] MACModeArray;
    String[] ChannelArray;
    String[] WSSModeArray;
    String[] ARCorrectionModeArray;
    ArrayList<String> ScramblingTypeArray;
    ArrayList<String> ScramblingKeyArray;
    ArrayList<String> ScramblingKey2Array;
    String[] LogoArray;
    String[] TCArray;
    ArrayList<String> PlaylistAL = new ArrayList<>();
    
    // Preferences node
    Preferences Prefs = Preferences.userNodeForPackage(GUI.class);
    
    // Process ID, used to gracefully close hacktv via the Stop button
    long pid;
    
    // Boolean to determine if hacktv is running or not
    boolean Running;
    
    // Boolean to determine if a config file is in the process of loading
    boolean HTVLoadInProgress = false;
    
    // Video line count. At the moment, we just use this for enabling/disabling
    // the test card dropdown, supported on 625 only.
    int Lines;  
    
    // Integer to save the previously selected item in the VideoFormat combobox.
    // Used to revert back if a baseband mode is selected on an unsupported SDR.
    int PreviousIndex = 0;
    boolean Baseband;
    
    // Start point in playlist
    int StartPoint = -1;
    
    // Declare variables used for storing parameters
    String InputSource = "";
    String Mode = "";
    long Frequency;
    int SampleRate;
    int PixelRate;
    String SubtitlesParam = "";
    String AudioParam = "";
    String NICAMParam = "";
    String ACPParam = "";
    String RepeatParam = "";
    String WssParam = "";
    String WssMode = "";
    String ScramblingType1 = "";
    String ScramblingKey1 = "";
    String ScramblingType2 = "";
    String ScramblingKey2 = "";
    String ScrambleAudio = "";
    String TeletextParam = "";
    String TeletextSource = "";
    String RFampParam = "";
    String FMDevParam = "";
    int FMDevValue;
    String GammaParam = "";
    String OutputLevelParam = "";
    String FilterParam = "";
    String PositionParam = "";
    String TimestampParam = "";
    String LogoParam = "";
    String LogoFileName = "";
    String VerboseParam = "";
    String EMMParam = "";
    String TruncatedCardNumber = "";
    String ShowECMParam = "";
    String ScalingMode = "";
    String InterlaceParam = "";
    String ShowCardSerial = "";
    String FindKey = "";
    String VITS = "";
    String ChIDParam = "";
    String ChID = "";
    String ColourParam = "";
    String SysterPermTable = "";
    String OutputDevice = "";
    String AntennaParam = "";
    String AntennaName = "";
    String FileType = "";
    String VolumeParam = "";
    String VolumeValue = "";
    String DownmixParam = "";
    String TeletextSubtitlesParam = "";
    String A2StereoParam = "";
    // End parameter variables
    
    // Main method
    public static void main(String args[]) {
        if (System.getProperty("os.name").contains("Mac")) {
            // Put app name in the menu bar on MacOS
            System.setProperty("apple.awt.application.name", "hacktv-gui");
            // Use the Mac menu bar
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
	GUI mainForm = new GUI(args);
        // Prevent window from being resized below the current size
        mainForm.setMinimumSize(mainForm.getSize());
	mainForm.setVisible(true);
    }
    
    /**
     * Create new form
     * @param args
     */
    public GUI(String[] args) {
        // If the emergency reset command is specified, remove all prefs.
        // This is a safety net, in case any bad preferences prevent us from running.
        // We handle this as early as possible to ensure it will work correctly.
        if ((args.length > 0) && (args[0].toLowerCase().equals("reset")) ) {
            // Reset all preferences and exit
            resetPreferences(1);
        }
        // Add a shutdown hook to run exit tasks
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cleanupBeforeExit();
            }
        });
        // Set look and feel
        if (System.getProperty("os.name").contains("Linux")) {
            try {
                // Use Metal L&F on all Linux distros
                // Comment out the next two lines if you want to use GTK+ on
                // supported desktop environments but it doesn't look great
                // in my opinion.
                UIManager.put("swing.boldMetal", false);
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
                  System.out.println(ex);
            }
        } else {
            try {
                // Use system default L&F on everything else
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
                System.out.println(ex);
            }
        }
        // Initialise Swing components
        initComponents();
        // Set the JarDir variable so we know where we're located
        JarDir = Path.of(Shared.getCurrentDirectory());
        // Get OS path separator (e.g. / on Unix and \ on Windows)
        OS_SEP = (System.getProperty("file.separator"));
        // Check operating system and set OS-specific options
        if (System.getProperty("os.name").contains("Windows")) {
            RunningOnWindows = true;
            DefaultHackTVPath = System.getProperty("user.dir") + OS_SEP + "hacktv.exe";
            // Does windows-kill.exe exist in the current directory?
            if ( !Files.exists(Path.of(JarDir + "/windows-kill.exe")) ) {
                // Enable the "Generate syntax only" option and prevent it from
                // being disabled, because windows-kill.exe is missing
                chkSyntaxOnly.doClick();
                chkSyntaxOnly.setEnabled(false);
                if (Prefs.get("MissingKillWarningShown", null) == null) {
                int q = JOptionPane.showConfirmDialog(null, "A helper application (windows-kill.exe) is required when running this application on Windows.\n"
                        + "It is available from from https://github.com/ElyDotDev/windows-kill/releases/\n"
                        + "Would you like to download it now?\n\n"
                        + "This message will only be shown once.", AppName, JOptionPane.YES_NO_OPTION);
                    if (q == JOptionPane.YES_OPTION) downloadWindowsKill();
                    Prefs.put("MissingKillWarningShown", "1");
                }
            } else {
                // Hide the "why can't I change this option?" message
                lblSyntaxOptionDisabled.setVisible(false);
            }
        }
        else {
            RunningOnWindows = false;
            lblSyntaxOptionDisabled.setVisible(false);
            DefaultHackTVPath = "/usr/local/bin/hacktv";
        }
        populateCheckboxArray();
        loadPreferences();
        detectFork();        
        selectModesFile();
        openModesFile();
        populateVideoModes();
        addWSSModes();
        addARCorrectionOptions();
        addTestCardOptions();
        addOutputDevices();
        if (Fork.equals("CJ")) {
            captainJack();
        }
        else {
            fsphil();
        }
        // Set default values when form loads
        radLocalSource.doClick();
        selectDefaultMode();
        cmbM3USource.setVisible(false);
        txtGain.setText("0");
        // End default value load
        checkMRUList();
        // If any command line parameters were specified, handle them
        if (args.length > 0) {
            // If the specified file has a .htv extension, open it
            if (args[0].endsWith(".htv")) {
                SelectedFile = new File(args[0]);
                checkSelectedFile(SelectedFile);
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

        SourceButtonGroup = new javax.swing.ButtonGroup();
        VideoFormatButtonGroup = new javax.swing.ButtonGroup();
        BandButtonGroup = new javax.swing.ButtonGroup();
        sourceFileChooser = sourceFileChooser = new JFileChooser(UserHomeDir);
        teletextFileChooser = teletextFileChooser = new JFileChooser();
        teletextFileChooser.setCurrentDirectory(new File(UserHomeDir));

        teletextFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("All teletext files (*.tti, *.t42)", "tti", "t42"));
        teletextFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Teletext files (*.tti)", "tti"));
        teletextFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Teletext containers (*.t42)", "t42"));
        teletextFileChooser.setAcceptAllFileFilterUsed(true);
        configFileChooser = configFileChooser = new JFileChooser();
        configFileChooser.setCurrentDirectory(new File(UserHomeDir));

        configFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("hacktv configuration file (*.htv)", "htv"));
        configFileChooser.setAcceptAllFileFilterUsed(true);
        hacktvFileChooser = hacktvFileChooser = new JFileChooser();
        hacktvFileChooser.setCurrentDirectory(new File(UserHomeDir));
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
                } else {
                    return "hacktv binaries (hacktv.exe)";
                }
            }
        });
        outputFileChooser = outputFileChooser = new JFileChooser();
        outputFileChooser.setCurrentDirectory(new File(UserHomeDir));
        ;
        containerPanel = new javax.swing.JPanel();
        consoleOutputPanel = new javax.swing.JPanel();
        consoleScrollPane = new javax.swing.JScrollPane();
        txtConsoleOutput = new javax.swing.JTextArea();
        tabPane = new javax.swing.JTabbedPane();
        sourceTab = new javax.swing.JPanel();
        SourcePanel = new javax.swing.JPanel();
        radLocalSource = new javax.swing.JRadioButton();
        radTest = new javax.swing.JRadioButton();
        txtSource = new javax.swing.JTextField();
        btnSourceBrowse = new javax.swing.JButton();
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
        cmbM3USource = new javax.swing.JComboBox<>();
        cmbTest = new javax.swing.JComboBox<>();
        playlistScrollPane = new javax.swing.JScrollPane();
        lstPlaylist = new javax.swing.JList<>();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnPlaylistDown = new javax.swing.JButton();
        btnPlaylistUp = new javax.swing.JButton();
        btnPlaylistStart = new javax.swing.JButton();
        chkRandom = new javax.swing.JCheckBox();
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
        lblRegion = new javax.swing.JLabel();
        lblOutputDevice2 = new javax.swing.JLabel();
        txtOutputDevice = new javax.swing.JTextField();
        VideoFormatPanel = new javax.swing.JPanel();
        cmbVideoFormat = new javax.swing.JComboBox<>();
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
        PlaybackTab = new javax.swing.JPanel();
        VBIPanel = new javax.swing.JPanel();
        chkVITS = new javax.swing.JCheckBox();
        chkACP = new javax.swing.JCheckBox();
        chkWSS = new javax.swing.JCheckBox();
        cmbWSS = new javax.swing.JComboBox<>();
        AdditionalOptionsPanel = new javax.swing.JPanel();
        chkGamma = new javax.swing.JCheckBox();
        txtGamma = new javax.swing.JTextField();
        chkOutputLevel = new javax.swing.JCheckBox();
        txtOutputLevel = new javax.swing.JTextField();
        chkVerbose = new javax.swing.JCheckBox();
        chkVolume = new javax.swing.JCheckBox();
        chkDownmix = new javax.swing.JCheckBox();
        chkMacChId = new javax.swing.JCheckBox();
        txtVolume = new javax.swing.JTextField();
        txtMacChId = new javax.swing.JTextField();
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
        chkShowECM = new javax.swing.JCheckBox();
        settingsTab = new javax.swing.JPanel();
        pathPanel = new javax.swing.JPanel();
        txtHackTVPath = new javax.swing.JTextField();
        btnHackTVPath = new javax.swing.JButton();
        lblFork = new javax.swing.JLabel();
        lblSpecifyLocation = new javax.swing.JLabel();
        lblDetectedBuikd = new javax.swing.JLabel();
        resetSettingsPanel = new javax.swing.JPanel();
        btnResetAllSettings = new javax.swing.JButton();
        btnClearMRUList = new javax.swing.JButton();
        lblClearMRU = new javax.swing.JLabel();
        lblClearAll = new javax.swing.JLabel();
        generalSettingsPanel = new javax.swing.JPanel();
        chkSyntaxOnly = new javax.swing.JCheckBox();
        lblSyntaxOptionDisabled = new javax.swing.JLabel();
        chkLocalModes = new javax.swing.JCheckBox();
        btnRun = new javax.swing.JButton();
        txtAllOptions = new javax.swing.JTextField();
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
        menuAstra975Template = new javax.swing.JMenuItem();
        menuAstra10Template = new javax.swing.JMenuItem();
        menuBSBTemplate = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        menuAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("GUI frontend for hacktv");
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/com/steeviebops/resources/test.gif")).getImage());

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
        consoleScrollPane.setViewportView(txtConsoleOutput);

        javax.swing.GroupLayout consoleOutputPanelLayout = new javax.swing.GroupLayout(consoleOutputPanel);
        consoleOutputPanel.setLayout(consoleOutputPanelLayout);
        consoleOutputPanelLayout.setHorizontalGroup(
            consoleOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(consoleScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
        );
        consoleOutputPanelLayout.setVerticalGroup(
            consoleOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(consoleScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
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

        btnSourceBrowse.setText("Browse...");
        btnSourceBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSourceBrowseActionPerformed(evt);
            }
        });

        chkRepeat.setText("Repeat indefinitely");
        chkRepeat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkRepeatActionPerformed(evt);
            }
        });

        chkTimestamp.setText("Overlay timestamp");
        chkTimestamp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTimestampActionPerformed(evt);
            }
        });

        chkInterlace.setText("Interlaced video");
        chkInterlace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkInterlaceActionPerformed(evt);
            }
        });

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

        cmbLogo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        cmbLogo.setEnabled(false);
        cmbLogo.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbLogoMouseWheelMoved(evt);
            }
        });
        cmbLogo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbLogoActionPerformed(evt);
            }
        });

        txtSubtitleIndex.setEnabled(false);

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

        cmbM3USource.setEnabled(false);

        cmbTest.setEnabled(false);
        cmbTest.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbTestMouseWheelMoved(evt);
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
                        .addGap(61, 61, 61)
                        .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SourcePanelLayout.createSequentialGroup()
                                .addComponent(chkSubtitles)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
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
                        .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, SourcePanelLayout.createSequentialGroup()
                                .addComponent(radLocalSource)
                                .addGap(39, 39, 39)
                                .addComponent(radTest)
                                .addGap(18, 18, 18)
                                .addComponent(cmbTest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, SourcePanelLayout.createSequentialGroup()
                                .addComponent(txtSource, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbM3USource, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSourceBrowse))
                    .addGroup(SourcePanelLayout.createSequentialGroup()
                        .addComponent(playlistScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnRemove, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                            .addComponent(btnPlaylistUp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnPlaylistDown, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnAdd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnPlaylistStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                    .addComponent(btnSourceBrowse)
                    .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(txtSource, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(cmbM3USource, javax.swing.GroupLayout.Alignment.LEADING)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sourceTabLayout.setVerticalGroup(
            sourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SourcePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 420, Short.MAX_VALUE)
                .addContainerGap())
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
        txtFrequency.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtFrequencyKeyTyped(evt);
            }
        });

        lblFrequency.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFrequency.setText("Frequency (MHz)");

        lblGain.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblGain.setText("TX gain (dB)");

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

        cmbFileType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "uint8", "int8", "uint16", "int16", "int32", "float" }));
        cmbFileType.setSelectedIndex(-1);
        cmbFileType.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbFileTypeMouseWheelMoved(evt);
            }
        });

        lblFileType.setText("File type");

        lblRegion.setText("Region");

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
                                .addGap(0, 100, Short.MAX_VALUE))))
                    .addGroup(rfPanelLayout.createSequentialGroup()
                        .addComponent(chkAmp)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(rfPanelLayout.createSequentialGroup()
                        .addComponent(radCustom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblRegion)))
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
                    .addComponent(lblRegion))
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

        VideoFormatPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Video format options"));

        cmbVideoFormat.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbVideoFormatMouseWheelMoved(evt);
            }
        });
        cmbVideoFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbVideoFormatActionPerformed(evt);
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

        chkVideoFilter.setText("VSB-AM filter");
        chkVideoFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkVideoFilterActionPerformed(evt);
            }
        });

        chkColour.setText("Disable colour");
        chkColour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkColourActionPerformed(evt);
            }
        });

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

        javax.swing.GroupLayout VideoFormatPanelLayout = new javax.swing.GroupLayout(VideoFormatPanel);
        VideoFormatPanel.setLayout(VideoFormatPanelLayout);
        VideoFormatPanelLayout.setHorizontalGroup(
            VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbVideoFormat, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 118, Short.MAX_VALUE)
                                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(chkPixelRate)
                                            .addComponent(lblSampleRate))
                                        .addGap(74, 74, 74))
                                    .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(chkVideoFilter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(chkFMDev))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))))
                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtPixelRate, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtSampleRate, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(radMAC, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtFMDev, javax.swing.GroupLayout.Alignment.TRAILING))))
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
                .addComponent(cmbVideoFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                    .addComponent(chkVideoFilter)
                    .addComponent(chkColour))
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
                    .addComponent(FrequencyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        outputTabLayout.setVerticalGroup(
            outputTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outputTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(VideoFormatPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FrequencyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        tabPane.addTab("Output", outputTab);

        VBIPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("VBI options"));

        chkVITS.setText("VITS test signal");
        chkVITS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkVITSActionPerformed(evt);
            }
        });

        chkACP.setText("Macrovision ACP");
        chkACP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkACPActionPerformed(evt);
            }
        });

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
                    .addComponent(chkVITS))
                .addContainerGap(146, Short.MAX_VALUE))
        );
        VBIPanelLayout.setVerticalGroup(
            VBIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VBIPanelLayout.createSequentialGroup()
                .addGroup(VBIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkWSS)
                    .addComponent(cmbWSS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkACP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkVITS))
        );

        AdditionalOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Additional options"));

        chkGamma.setText("Gamma correction");
        chkGamma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkGammaActionPerformed(evt);
            }
        });

        txtGamma.setEnabled(false);

        chkOutputLevel.setText("Output level");
        chkOutputLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkOutputLevelActionPerformed(evt);
            }
        });

        txtOutputLevel.setEnabled(false);

        chkVerbose.setText("Verbose output");
        chkVerbose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkVerboseActionPerformed(evt);
            }
        });

        chkVolume.setText("Adjust volume");
        chkVolume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkVolumeActionPerformed(evt);
            }
        });

        chkDownmix.setText("Downmix 5.1 audio to 2.0");
        chkDownmix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDownmixActionPerformed(evt);
            }
        });

        chkMacChId.setText("Override MAC channel ID");
        chkMacChId.setEnabled(false);
        chkMacChId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMacChIdActionPerformed(evt);
            }
        });

        txtVolume.setEnabled(false);

        txtMacChId.setEnabled(false);
        txtMacChId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtMacChIdKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtMacChIdKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtMacChIdKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout AdditionalOptionsPanelLayout = new javax.swing.GroupLayout(AdditionalOptionsPanel);
        AdditionalOptionsPanel.setLayout(AdditionalOptionsPanelLayout);
        AdditionalOptionsPanelLayout.setHorizontalGroup(
            AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdditionalOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(AdditionalOptionsPanelLayout.createSequentialGroup()
                        .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkGamma)
                            .addComponent(chkOutputLevel))
                        .addGap(66, 66, 66)
                        .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtOutputLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtGamma, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18))
                    .addGroup(AdditionalOptionsPanelLayout.createSequentialGroup()
                        .addComponent(chkMacChId)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtMacChId, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkVerbose)
                    .addComponent(chkDownmix)
                    .addGroup(AdditionalOptionsPanelLayout.createSequentialGroup()
                        .addComponent(chkVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                        .addComponent(txtVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        AdditionalOptionsPanelLayout.setVerticalGroup(
            AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdditionalOptionsPanelLayout.createSequentialGroup()
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkGamma)
                    .addComponent(txtGamma, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkVolume)
                    .addComponent(txtVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkOutputLevel)
                    .addComponent(txtOutputLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkDownmix))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkMacChId)
                    .addComponent(chkVerbose)
                    .addComponent(txtMacChId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout PlaybackTabLayout = new javax.swing.GroupLayout(PlaybackTab);
        PlaybackTab.setLayout(PlaybackTabLayout);
        PlaybackTabLayout.setHorizontalGroup(
            PlaybackTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlaybackTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PlaybackTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(VBIPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AdditionalOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        PlaybackTabLayout.setVerticalGroup(
            PlaybackTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlaybackTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(VBIPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AdditionalOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(236, Short.MAX_VALUE))
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

        javax.swing.GroupLayout teletextPanelLayout = new javax.swing.GroupLayout(teletextPanel);
        teletextPanel.setLayout(teletextPanelLayout);
        teletextPanelLayout.setHorizontalGroup(
            teletextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(teletextPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(teletextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(teletextPanelLayout.createSequentialGroup()
                        .addComponent(chkTeletext)
                        .addGap(286, 396, Short.MAX_VALUE))
                    .addGroup(teletextPanelLayout.createSequentialGroup()
                        .addGroup(teletextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(teletextPanelLayout.createSequentialGroup()
                                .addComponent(txtTeletextSource, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
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

        lblTeefax.setText("Download Teefax");
        lblTeefax.setEnabled(false);

        lblSpark.setText("Download SPARK from TVARK");
        lblSpark.setEnabled(false);

        javax.swing.GroupLayout downloadPanelLayout = new javax.swing.GroupLayout(downloadPanel);
        downloadPanel.setLayout(downloadPanelLayout);
        downloadPanelLayout.setHorizontalGroup(
            downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(downloadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(downloadPanelLayout.createSequentialGroup()
                        .addGroup(downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnTeefax, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSpark, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTeefax, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSpark)))
                    .addComponent(pbTeletext, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDownload, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        downloadPanelLayout.setVerticalGroup(
            downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(downloadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDownload)
                .addGap(18, 18, 18)
                .addGroup(downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTeefax, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTeefax))
                .addGap(18, 18, 18)
                .addGroup(downloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSpark, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSpark))
                .addGap(18, 18, 18)
                .addComponent(pbTeletext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addContainerGap(85, Short.MAX_VALUE))
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
        txtCardNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCardNumberKeyTyped(evt);
            }
        });

        chkShowCardSerial.setText("Show card serial");
        chkShowCardSerial.setEnabled(false);
        chkShowCardSerial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowCardSerialActionPerformed(evt);
            }
        });

        chkFindKeys.setText("Find keys on PPV card");
        chkFindKeys.setEnabled(false);
        chkFindKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkFindKeysActionPerformed(evt);
            }
        });

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
                        .addComponent(lblEMMCardNumber, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                        .addComponent(txtCardNumber, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(chkShowCardSerial))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        emmPanelLayout.setVerticalGroup(
            emmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(emmPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addContainerGap())
        );

        scramblingOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Format-specific options"));
        scramblingOptionsPanel.setToolTipText("");

        chkScrambleAudio.setText("Scramble audio");
        chkScrambleAudio.setEnabled(false);
        chkScrambleAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkScrambleAudioActionPerformed(evt);
            }
        });

        cmbSysterPermTable.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default", "1", "2" }));
        cmbSysterPermTable.setEnabled(false);
        cmbSysterPermTable.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbSysterPermTableItemStateChanged(evt);
            }
        });
        cmbSysterPermTable.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbSysterPermTableMouseWheelMoved(evt);
            }
        });

        lblSysterPermTable.setText("Syster permutation table");
        lblSysterPermTable.setEnabled(false);

        javax.swing.GroupLayout scramblingOptionsPanelLayout = new javax.swing.GroupLayout(scramblingOptionsPanel);
        scramblingOptionsPanel.setLayout(scramblingOptionsPanelLayout);
        scramblingOptionsPanelLayout.setHorizontalGroup(
            scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scramblingOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkScrambleAudio)
                    .addComponent(lblSysterPermTable)
                    .addComponent(cmbSysterPermTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(67, Short.MAX_VALUE))
        );
        scramblingOptionsPanelLayout.setVerticalGroup(
            scramblingOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scramblingOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkScrambleAudio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSysterPermTable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbSysterPermTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        chkShowECM.setText("Show ECMs on console");
        chkShowECM.setEnabled(false);
        chkShowECM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowECMActionPerformed(evt);
            }
        });

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
                            .addComponent(cmbScramblingKey1, 0, 381, Short.MAX_VALUE)
                            .addComponent(cmbScramblingKey2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbScramblingType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 10, Short.MAX_VALUE))))
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
                .addComponent(scramblingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(87, Short.MAX_VALUE))
        );

        tabPane.addTab("Scrambling", scramblingTab);

        pathPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Path to hacktv"));

        txtHackTVPath.setEditable(false);

        btnHackTVPath.setText("Browse...");
        btnHackTVPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHackTVPathActionPerformed(evt);
            }
        });

        lblFork.setText("fork");

        lblSpecifyLocation.setText("Specify the location of hacktv here.");

        lblDetectedBuikd.setText("Detected build:");

        javax.swing.GroupLayout pathPanelLayout = new javax.swing.GroupLayout(pathPanel);
        pathPanel.setLayout(pathPanelLayout);
        pathPanelLayout.setHorizontalGroup(
            pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pathPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSpecifyLocation)
                    .addGroup(pathPanelLayout.createSequentialGroup()
                        .addComponent(lblDetectedBuikd)
                        .addGap(18, 18, 18)
                        .addComponent(lblFork))
                    .addComponent(txtHackTVPath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnHackTVPath)
                .addContainerGap())
        );
        pathPanelLayout.setVerticalGroup(
            pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pathPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSpecifyLocation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnHackTVPath)
                    .addComponent(txtHackTVPath))
                .addGap(10, 10, 10)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDetectedBuikd)
                    .addComponent(lblFork))
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
                .addContainerGap(167, Short.MAX_VALUE))
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

        lblSyntaxOptionDisabled.setForeground(new java.awt.Color(0, 0, 128));
        lblSyntaxOptionDisabled.setText("Why can't I change this option?");
        lblSyntaxOptionDisabled.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblSyntaxOptionDisabled.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSyntaxOptionDisabledMouseClicked(evt);
            }
        });

        chkLocalModes.setText("Always use local copy of Modes.ini (restart required)");
        chkLocalModes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLocalModesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout generalSettingsPanelLayout = new javax.swing.GroupLayout(generalSettingsPanel);
        generalSettingsPanel.setLayout(generalSettingsPanelLayout);
        generalSettingsPanelLayout.setHorizontalGroup(
            generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                        .addComponent(chkSyntaxOnly)
                        .addGap(18, 18, 18)
                        .addComponent(lblSyntaxOptionDisabled))
                    .addComponent(chkLocalModes))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        generalSettingsPanelLayout.setVerticalGroup(
            generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkSyntaxOnly)
                    .addComponent(lblSyntaxOptionDisabled))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkLocalModes)
                .addContainerGap(54, Short.MAX_VALUE))
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
                .addContainerGap(80, Short.MAX_VALUE))
        );

        tabPane.addTab("GUI settings", settingsTab);

        btnRun.setText("Run hacktv");
        btnRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunActionPerformed(evt);
            }
        });

        txtAllOptions.setEditable(false);

        javax.swing.GroupLayout containerPanelLayout = new javax.swing.GroupLayout(containerPanel);
        containerPanel.setLayout(containerPanelLayout);
        containerPanelLayout.setHorizontalGroup(
            containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tabPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(containerPanelLayout.createSequentialGroup()
                                .addGap(170, 170, 170)
                                .addComponent(btnRun, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(consoleOutputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(txtAllOptions))
                .addContainerGap())
        );
        containerPanelLayout.setVerticalGroup(
            containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(tabPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnRun, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(consoleOutputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAllOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        fileMenu.setText("File");

        menuNew.setText("New");
        menuNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuNewActionPerformed(evt);
            }
        });
        fileMenu.add(menuNew);

        menuOpen.setText("Open...");
        menuOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpenActionPerformed(evt);
            }
        });
        fileMenu.add(menuOpen);

        menuSave.setText("Save...");
        menuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveActionPerformed(evt);
            }
        });
        fileMenu.add(menuSave);

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

        menuExit.setText("Exit");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
            }
        });
        fileMenu.add(menuExit);

        menuBar.add(fileMenu);

        templatesMenu.setText("Templates");

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

        menuAbout.setText("About");
        menuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAboutActionPerformed(evt);
            }
        });
        helpMenu.add(menuAbout);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
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
            chkRandom
        };
    }
    
    private void downloadWindowsKill(){
        // Downloads windows-kill from ElyDotDev (formerly alirdn) on Github
        try {
            createTempDirectory();
            String t = TempDir.toString();
            String downloadPath = t + OS_SEP + "windows-kill_x64_1.1.4_lib_release.zip";
            String tmpExePath = t + OS_SEP + "windows-kill_x64_1.1.4_lib_release" + OS_SEP + "windows-kill.exe";
            String exePath = JarDir + OS_SEP + "windows-kill.exe";
            String downloadURL = "https://github.com/ElyDotDev/windows-kill/releases/download/1.1.4/windows-kill_x64_1.1.4_lib_release.zip";
            // Start download
            Shared.download(downloadURL, downloadPath);
            // Unzip what we got to the temp directory
            Shared.UnzipFile(downloadPath, t);
            // If windows-kill.exe exists in the temp directory, delete the zip file
            if (Files.exists(Path.of(tmpExePath))) {
                Shared.deleteFSObject(Path.of(downloadPath)); 
                // Move windows-kill.exe from the temp directory to the
                // working directory
                Files.move(Path.of(tmpExePath), Path.of(exePath), REPLACE_EXISTING);
                // Clean up by removing remnants of what we unzipped
                Shared.deleteFSObject(Path.of(tmpExePath).getParent());
            }
            // Remove the syntax-only block
            if (Files.exists(Path.of(exePath))) {
                lblSyntaxOptionDisabled.setVisible(false);
                if (!chkSyntaxOnly.isEnabled()){
                    chkSyntaxOnly.setEnabled(true);
                    chkSyntaxOnly.doClick();
                }
            }
        }
        catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "An error occurred while downloading windows-kill.\n"
                    + "Please ensure that you have write permissions to the application directory and that you have internet access.", AppName, JOptionPane.ERROR_MESSAGE);
        }                        
    }
    
    private void selectModesFile() {
        if ((Prefs.get("UseLocalModesFile", "0")).equals("1")) {
            if (Files.exists(Path.of(JarDir + OS_SEP + "Modes.ini"))) {
                // Use the local file
                ModesFilePath = JarDir + OS_SEP + "Modes.ini";
            }
            else {
                // Use the embedded copy
                ModesFilePath = "/com/steeviebops/resources/" + getFork() + "/Modes.ini";                
            }
        }
        else if (Files.exists(Path.of(JarDir + OS_SEP + "Modes.ini"))) {
            int q = JOptionPane.showConfirmDialog(null, "A Modes.ini file was found in the current directory.\n"
                    + "Do you want to use this file?\n"
                    + "You can suppress this prompt on the GUI settings tab.", AppName, JOptionPane.YES_NO_OPTION);
            if (q == JOptionPane.YES_OPTION) {
                // Use the local file
                ModesFilePath = JarDir + OS_SEP + "Modes.ini";
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
        createTempDirectory();
        String f = TempDir + OS_SEP + "Modes.ini";
        String u = "https://raw.githubusercontent.com/steeviebops/hacktv-gui/main/src/com/steeviebops/resources/" + getFork() + "/Modes.ini";
        try {
            // Delete Modes.ini if it already exists
            if (Files.exists(Path.of(f))) Shared.deleteFSObject(Path.of(f));
            Shared.download(u, f);
            // Use the file we downloaded
            ModesFilePath = f;
        }
        catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Unable to download the modes file from Github.\n"
                        + "Using embedded copy instead, which may not be up to date.", AppName, JOptionPane.ERROR_MESSAGE);
                System.out.println("Error downloading modes.ini... " + ex);
                // Use the embedded copy
                ModesFilePath = "/com/steeviebops/resources/" + getFork() + "/Modes.ini";
        }
    } 
    
    private void openModesFile() {
        if (ModesFilePath.startsWith("/com/steeviebops/resources/")) {
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
                JOptionPane.showMessageDialog(null, "Critical error, unable to read the embedded modes file.\n"
                        + "The application will now exit.", AppName, JOptionPane.ERROR_MESSAGE);
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
                if (ModesFilePath.equals(TempDir + OS_SEP + "Modes.ini")) {
                    ModesFileLocation = "online";
                }
                else {
                    ModesFileLocation = "external";
                }
                
            }
            catch (IOException e) {
                // Load failed, retry with the embedded file
                JOptionPane.showMessageDialog(null, "Unable to read the modes file.\n"
                        + "Retrying with the embedded copy, which may not be up to date.", AppName, JOptionPane.WARNING_MESSAGE);                
                ModesFilePath = "/com/steeviebops/resources/" + getFork() + "Modes.ini";
                ModesFileLocation = "embedded";
                openModesFile();
                return;
            }
        }
        // Read modes.ini file version
        ModesFileVersion = INIFile.getStringFromINI(ModesFile, "Modes.ini", "FileVersion", "unknown", true);
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
            JOptionPane.showMessageDialog(null, "No video systems were found. The Modes.ini file may be invalid or corrupted.\n"
                    + "The application will now exit.", AppName, JOptionPane.ERROR_MESSAGE);
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
        /**
         * ColourStandard specifies what to look for (pal, ntsc, secam, other or mac)
         * returnValue specifies what we return in the array
         * 0 = return the friendly name of the mode
         * 1 = return the mode parameter used at the command line
         */
        
        String m = INIFile.getStringFromINI(ModesFile, "videomodes", ColourStandard, "", false);
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
            for (int i = 0; i < q.length; i++) {
                String a = INIFile.getStringFromINI(ModesFile, q[i], "name", "", true);
                if (!a.isBlank()) {
                    // Add the friendly name of the mode
                    ml.add(a);
                }
                else {
                    // Add the mode itself
                    ml.add("Unnamed mode '" + q[i] + "'");
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
        if (Prefs.get("UseLocalModesFile", "0").equals("1")) {
            chkLocalModes.setSelected(true);
        }
    }
    
    private void resetPreferences(int i) {
        // Delete everything from the preference store and exit immediately.
        // If i is set to 1, a message will be printed to the console.
        // This is used for the emergency reset option from the command line.
        if ( Prefs.get("HackTVPath", null) != null ) Prefs.remove("HackTVPath");
        if ( Prefs.get("File1", null) != null ) Prefs.remove("File1");
        if ( Prefs.get("File2", null) != null ) Prefs.remove("File2");
        if ( Prefs.get("File3", null) != null ) Prefs.remove("File3");
        if ( Prefs.get("File4", null) != null ) Prefs.remove("File4");
        if ( Prefs.get("MissingKillWarningShown", null) != null ) Prefs.remove("MissingKillWarningShown"); 
        if ( Prefs.get("UseLocalModesFile", null) != null ) Prefs.remove("UseLocalModesFile");   
        if (i == 1) System.out.println("All preferences have been reset to defaults.");
        System.exit(0);
    }
    
    private void detectFork() {
        /*  Loads the hacktv binary into RAM and attempts to find a specified
         *  string to detect what build or fork it is.
         */
        
        // Check if the specified path does not exist or is a directory
        if (!Files.exists(Path.of(HackTVPath))) {
            lblFork.setText("Not found");
            Fork = "";
            return;
        } else if (Files.isDirectory(Path.of(HackTVPath))) {
            lblFork.setText("Invalid path");
            Fork = "";
            return;            
        }
        /*  Check the size of the specified file.
         *  If larger than 100MB, call the fsphil method and don't go any further.
         *  This is to avoid memory leaks or hangs by loading a bad file.
         */
        File f = new File(HackTVPath);
        if (f.length() > 104857600) {
            lblFork.setText("Invalid file (too large)");
            Fork = "";
            return;
        }
        // Test the specified file by loading it into memory using a BufferedReader.
        // This is more memory-efficient than loading the entire file to a byte array.
        boolean b = false;
        try {
            String c;
            BufferedReader br1 = new BufferedReader(new FileReader(HackTVPath));
            while ((c = br1.readLine()) != null) {
                if (c.contains("--enableemm")) {
                    b = true;
                    lblFork.setText("Captain Jack");
                    Fork = "CJ";
                }
                else if (c.contains("Both VC1 and VC2 cannot be used together")) {
                    b = true;
                    lblFork.setText("fsphil");
                    Fork = "";
                }
                else {
                    // Clear the line and try another one
                    c = "";
                }
                // If we found a match, stop processing
                if (b) break;
            }
            // We no longer need c so clear it
            c = null;
            br1.close();
            // Run garbage collection to save memory
            System.gc();
            }
        catch (IOException ex) {
            lblFork.setText("File access error");
            Fork = "";
            return;
        }
        if (!b) {
            lblFork.setText("Invalid file (not hacktv?)");
            Fork = "";
            return;            
        }     
    }
    
    private String getFork() {
        if (Fork.equals("CJ")) {
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
                int p = evt * evt; // negative * negative = positive
                if (jcb.getSelectedIndex() - p >= 0) jcb.setSelectedIndex(jcb.getSelectedIndex() - p);
            }
            else if (evt > 0) {
                if (evt + jcb.getSelectedIndex() < jcb.getItemCount()) jcb.setSelectedIndex(jcb.getSelectedIndex() + evt);
            }            
        }
    }

    private void createTempDirectory() {
        // Creates a temp directory for us to use.
        // This is deleted on exit so don't save anything useful here!
        if (TempDir == null) {
            try {
                TempDir = Files.createTempDirectory(AppName);
            }
            catch (IOException ex) {
                System.out.println(ex);
                JOptionPane.showMessageDialog(null, "An error occurred while creating the temp directory.", AppName, JOptionPane.ERROR_MESSAGE);
                resetTeletextButtons();
            }
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
        } else {
            menuMRUFile1.setVisible(false);
        }
        if ( !ConfigFile2.isEmpty() ) {
            sepMruSeparator.setVisible(true);
            menuMRUFile2.setText(ConfigFile2);
            menuMRUFile2.setVisible(true);
            btnClearMRUList.setEnabled(true);
        } else {
            menuMRUFile2.setVisible(false);
        }
        if ( !ConfigFile3.isEmpty() ) {
            sepMruSeparator.setVisible(true);
            menuMRUFile3.setText(ConfigFile3);
            menuMRUFile3.setVisible(true);
            btnClearMRUList.setEnabled(true);
        } else {
            menuMRUFile3.setVisible(false);
        }
        if ( !ConfigFile4.isEmpty() ) {
            sepMruSeparator.setVisible(true);
            menuMRUFile4.setText(ConfigFile4);
            menuMRUFile4.setVisible(true);
            btnClearMRUList.setEnabled(true);
        } else {
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
        } else if (FilePath.equals(ConfigFile3)) {
            Prefs.put("File3", ConfigFile2);
            Prefs.put("File2", ConfigFile1);
            Prefs.put("File1", FilePath);           
            checkMRUList(); 
        } else if (FilePath.equals(ConfigFile4)) {
            Prefs.put("File4", ConfigFile3);
            Prefs.put("File3", ConfigFile2);
            Prefs.put("File2", ConfigFile1);
            Prefs.put("File1", FilePath);
            checkMRUList();
        } else if (FilePath.equals(ConfigFile1)) {
            // Do nothing
        } else {
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
            SelectedFile = new File (Shared.stripQuotes(configFileChooser.getSelectedFile().toString()));
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
                            + "Do you want to replace it?", AppName, JOptionPane.YES_NO_OPTION);
                    if (q == JOptionPane.YES_OPTION) {
                        saveConfigFile(SelectedFile);
                    } else {
                        saveFilePrompt();
                    }
                } else {
                    saveConfigFile(SelectedFile);
                }
            } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "An error occurred while writing to this file. "
                            + "You may not have the correct permissions to write to this location.", AppName, JOptionPane.ERROR_MESSAGE);       
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
                JOptionPane.showMessageDialog(null, "Invalid configuration file.", AppName, JOptionPane.WARNING_MESSAGE);
                System.err.println("File too large (> 1MB)");
                return;
            }
            // Check the file to see if it's in the correct format.
            if ( (INIFile.splitINIfile(f, "hacktv")) != null ) {
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
                JOptionPane.showMessageDialog(null, "Invalid configuration file.", AppName, JOptionPane.WARNING_MESSAGE);
                System.err.println("[hacktv] section not found");
            }
        } catch (MalformedInputException ex) {
                JOptionPane.showMessageDialog(null, "Invalid configuration file.", AppName, JOptionPane.WARNING_MESSAGE);
                System.err.println("The specified file contains invalid data.");
        } catch (IOException iox) {
                // File is inaccessible, so stop
                JOptionPane.showMessageDialog(null, "The specified file could not be opened.\n"
                        + "It may have been removed, or you may not have the correct permissions to access it.", AppName, JOptionPane.ERROR_MESSAGE);         
        }
    }
    
    private boolean openConfigFile(String fileContents) throws IOException {
        /**
         * HTV configuration file loader.
         * 
         * We read the file as a Windows INI format. The syntax for strings is as follows:
         * 
         * INIFile.getStringFromINI(source file or string, "section", "setting", "Default value", Preserve case?);
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
        String ImportedFork = INIFile.getStringFromINI(fileContents, "hacktv-gui3", "fork", "", false);
        String WrongFork = "This file was created with a different fork of " +
            "hacktv. We will attempt to process the file but some options " +
            "may not be available.";
        if ((Fork.isEmpty()) && (!ImportedFork.isEmpty())) {
            JOptionPane.showMessageDialog(null, WrongFork, AppName, JOptionPane.WARNING_MESSAGE);
        }
        if ((Fork.contains("CJ")) && (!ImportedFork.contains("captainjack"))) {
            JOptionPane.showMessageDialog(null, WrongFork, AppName, JOptionPane.WARNING_MESSAGE);
        }
        // Reset all controls
        resetAllControls();
        /* Output device
           For this, we look for hackrf, soapysdr or fl2k. An empty value will be
           interpreted as hackrf. Anything other than these values is handled
           as an output file.
         */
        String ImportedOutputDevice = INIFile.getStringFromINI(fileContents, "hacktv", "output", "hackrf", false);
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
        // Input source or test card
        String ImportedSource = INIFile.getStringFromINI(fileContents, "hacktv", "input", "", true);
        String M3USource = (INIFile.getStringFromINI(fileContents, "hacktv-gui3", "m3usource", "", true));
        Integer M3UIndex = (INIFile.getIntegerFromINI(fileContents, "hacktv-gui3", "m3uindex"));
        if (ImportedSource.toLowerCase().startsWith("test:")) {
            radTest.doClick();
            if (Fork.equals("CJ")) {
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
        else if (INIFile.getBooleanFromINI(fileContents, "hacktv-gui3", "playlist")) {
            // Split the [playlist] section from the HTV file.
            // We then split the section into an array (minus the header) 
            // and use that to populate PlaylistAL.
            if (INIFile.splitINIfile(fileContents, "playlist") != null) {
                String[] pl = INIFile.splitINIfile(fileContents, "playlist").split("\\n");
                for (int i = 1; i < pl.length; i++) {
                    PlaylistAL.add(pl[i]);
                }
                if ((INIFile.getIntegerFromINI(fileContents, "hacktv-gui3", "playliststart")) != null) {
                    StartPoint = INIFile.getIntegerFromINI(fileContents, "hacktv-gui3", "playliststart") - 1;
                    // Don't accept values lower than one
                    if (StartPoint < 1) StartPoint = -1;
                }
                chkRandom.setSelected(INIFile.getBooleanFromINI(fileContents, "hacktv-gui3", "random"));
                populatePlaylist();
            }
        }
        else {
            txtSource.setText(ImportedSource);
        }
        // Video format
        String ImportedVideoMode = INIFile.getStringFromINI(fileContents, "hacktv", "mode", "", false);
        boolean ModeFound = false;
            for (int i = 0; i < PALModeArray.length; i++) {
                // Check if the mode we imported is in the PAL mode array
                if (PALModeArray[i].equals(ImportedVideoMode)) {
                    radPAL.doClick();
                    cmbVideoFormat.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
                // Check the 'alt' value to see if we find a match there
                else if (checkAltModeNames(PALModeArray[i], ImportedVideoMode)) {
                    radPAL.doClick();
                    cmbVideoFormat.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
            }
            if (!ModeFound) {
                // Check the NTSC mode array, and so on...
                for (int i = 0; i < NTSCModeArray.length; i++) {
                    if (NTSCModeArray[i].equals(ImportedVideoMode)) {
                        radNTSC.doClick();
                        cmbVideoFormat.setSelectedIndex(i);
                        ModeFound = true;
                        break;
                    }
                    else if (checkAltModeNames(NTSCModeArray[i], ImportedVideoMode)) {
                            radNTSC.doClick();
                            cmbVideoFormat.setSelectedIndex(i);
                            ModeFound = true;
                            break;
                    }
                }      
            }
            if (!ModeFound) {
                for (int i = 0; i < SECAMModeArray.length; i++) {
                    if (SECAMModeArray[i].equals(ImportedVideoMode)) {
                        radSECAM.doClick();
                        cmbVideoFormat.setSelectedIndex(i);
                        ModeFound = true;
                        break;
                    }
                    else if (checkAltModeNames(SECAMModeArray[i], ImportedVideoMode)) {
                        radSECAM.doClick();
                        cmbVideoFormat.setSelectedIndex(i);
                        ModeFound = true;
                        break;
                    }
                }
            }
            if (!ModeFound) {
                for (int i = 0; i < OtherModeArray.length; i++) {
                    if (OtherModeArray[i].equals(ImportedVideoMode)) {
                        radBW.doClick();
                        cmbVideoFormat.setSelectedIndex(i);
                        ModeFound = true;
                        break;
                    }
                    else if (checkAltModeNames(OtherModeArray[i], ImportedVideoMode)) {
                        radBW.doClick();
                        cmbVideoFormat.setSelectedIndex(i);
                        ModeFound = true;
                        break;
                    }
                }
            }
            if (!ModeFound) {
                for (int i = 0; i < MACModeArray.length; i++) {
                    if (MACModeArray[i].equals(ImportedVideoMode)) {
                        radMAC.doClick();
                        cmbVideoFormat.setSelectedIndex(i);
                        ModeFound = true;
                        break;
                    }
                    else if (checkAltModeNames(MACModeArray[i], ImportedVideoMode)) {
                        radMAC.doClick();
                        cmbVideoFormat.setSelectedIndex(i);
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
        // Frequency or channel number
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            // Return a value of -250 if the value is null so we can handle it
            String NoFrequencyOrChannel = "No frequency or valid channel number was found in the configuration file. Load aborted.";
            String ImportedChannel = INIFile.getStringFromINI(fileContents, "hacktv-gui3", "channel", "", true);
            Double ImportedFrequency;
            if (INIFile.getDoubleFromINI(fileContents, "hacktv", "frequency") != null) {
                ImportedFrequency = INIFile.getDoubleFromINI(fileContents, "hacktv", "frequency");
            } else {
                ImportedFrequency = Double.parseDouble("-250");
            }
            if ((ImportedChannel.isEmpty()) && (ImportedFrequency == -250)) {
                JOptionPane.showMessageDialog(null, NoFrequencyOrChannel, AppName, JOptionPane.WARNING_MESSAGE);
                resetAllControls();
                return false;
            } else if (ImportedChannel.isEmpty()) {
                radCustom.doClick();
                Double Freq = ImportedFrequency / 1000000;
                txtFrequency.setText(Double.toString(Freq).replace(".0",".00"));
            } else {
                // Try to find the channel name by trying UHF first
                boolean ChannelFound = false;
                radUHF.doClick();
                for (int i = 0; i <= cmbChannel.getItemCount() - 1; i++) {
                    if ( (ChannelArray[i].toLowerCase()).equals(ImportedChannel.toLowerCase()) ) {
                        cmbChannel.setSelectedIndex(i);
                        ChannelFound = true;
                    }
                }
                // If not found, try VHF
                if (!ChannelFound) {
                    radVHF.doClick();
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
                    } else {
                        // If not found, and the frequency is also blank, abort
                        JOptionPane.showMessageDialog(null, NoFrequencyOrChannel, AppName, JOptionPane.WARNING_MESSAGE);
                        resetAllControls();
                        return false;
                    }
                }  
            }
        }
        // Gain
        if (INIFile.getIntegerFromINI(fileContents, "hacktv", "gain") != null) {
            txtGain.setText(INIFile.getIntegerFromINI(fileContents, "hacktv", "gain").toString());
        }
        // If value is null and output device is hackrf or soapysdr, set gain to zero
        else if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            txtGain.setText("0");
        }
        // Amp
        if (cmbOutputDevice.getSelectedIndex() == 0) {
            if (INIFile.getBooleanFromINI(fileContents, "hacktv", "amp")) {
                chkAmp.doClick();
            }            
        }
        // FM deviation
        if ((chkFMDev.isEnabled()) && (INIFile.getDoubleFromINI(fileContents, "hacktv", "deviation") != null)) {
            Double ImportedDeviation = (INIFile.getDoubleFromINI(fileContents, "hacktv", "deviation") / 1000000);
            chkFMDev.doClick();
            txtFMDev.setText(ImportedDeviation.toString().replace(".0",""));
        }
        // Output level
        String ImportedLevel = INIFile.getStringFromINI(fileContents, "hacktv", "level", "", false);
        if (!ImportedLevel.isEmpty()) {
            chkOutputLevel.doClick();
            txtOutputLevel.setText(ImportedLevel);
        }
        // Gamma
        String ImportedGamma = INIFile.getStringFromINI(fileContents, "hacktv", "gamma", "", false);
        if (!ImportedGamma.isEmpty()) {
            chkGamma.doClick();
            txtGamma.setText(ImportedGamma);
        }
        // Repeat
        if (chkRepeat.isEnabled()) {
            if (INIFile.getBooleanFromINI(fileContents, "hacktv", "repeat")) {
                chkRepeat.doClick();
            }
        }
        // Position
        if (chkPosition.isEnabled()) {
            if (INIFile.getIntegerFromINI(fileContents, "hacktv", "position") != null) {
                chkPosition.doClick();
                txtPosition.setText(INIFile.getIntegerFromINI(fileContents, "hacktv", "position").toString());
            }
        }
        // Verbose mode
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "verbose")) {
            chkVerbose.doClick();
        }
        // Logo
        if (chkLogo.isEnabled()) {
            String ImportedLogo = INIFile.getStringFromINI(fileContents, "hacktv", "logo", "", true).toLowerCase();
            // Check first if the imported string is a .png file.
            // hacktv now contains its own internal resources so external files
            // are no longer supported.
            if (ImportedLogo.endsWith(".png")) {
                JOptionPane.showMessageDialog(null, 
                     "hacktv no longer supports external logo files. Logo option disabled.", AppName, JOptionPane.WARNING_MESSAGE);
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
            if (INIFile.getBooleanFromINI(fileContents, "hacktv", "timestamp")) {
                chkTimestamp.doClick();
            }
        }
        // Interlace
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "interlace")) {
            chkTimestamp.doClick();
        }
        // Teletext
        String ImportedTeletext = INIFile.getStringFromINI(fileContents, "hacktv", "teletext", "", true);
        if (!ImportedTeletext.isEmpty()) {
            chkTeletext.doClick();
            if (ImportedTeletext.toLowerCase().startsWith("raw:")) {
                txtTeletextSource.setText(ImportedTeletext.substring(4));
            } else {
                txtTeletextSource.setText(ImportedTeletext);
            }
        }
        // WSS
        if ((INIFile.getIntegerFromINI(fileContents, "hacktv", "wss")) != null) {
            Integer ImportedWSS = (INIFile.getIntegerFromINI(fileContents, "hacktv", "wss"));
            // Only accept values between 1 and 5
            if ((ImportedWSS > 0) && (ImportedWSS <= 5)) {
                chkWSS.doClick();
                // Since we increased the value by one when saving, decrease by one when loading
                cmbWSS.setSelectedIndex(ImportedWSS - 1);
            }
        }
        /**
         * Aspect ratio correction for 16:9 content on 4:3 displays
         * If the arcorrection value is not defined, leave the option unchecked
         * Otherwise, check the option and process it as normal
         */
        if (chkARCorrection.isEnabled()) {
            if ((INIFile.getIntegerFromINI(fileContents, "hacktv", "arcorrection")) != null) {
                Integer ImportedAR = (INIFile.getIntegerFromINI(fileContents, "hacktv", "arcorrection"));
                chkARCorrection.doClick();
                cmbARCorrection.setSelectedIndex(ImportedAR);
            }
        }
        // Scrambling system
        String ImportedScramblingSystem = INIFile.getStringFromINI(fileContents, "hacktv", "scramblingtype", "", false);
        String ImportedKey = INIFile.getStringFromINI(fileContents, "hacktv", "scramblingkey", "", false);
        String ImportedKey2 = INIFile.getStringFromINI(fileContents, "hacktv", "scramblingkey2", "", false);
        if ((radPAL.isSelected()) || radSECAM.isSelected()) {
            if (ImportedScramblingSystem.isEmpty()) {
                cmbScramblingType.setSelectedIndex(0);
            } else if (ImportedScramblingSystem.equals("videocrypt")) {
                cmbScramblingType.setSelectedIndex(1);
            } else if (ImportedScramblingSystem.equals("videocrypt2")) {
                cmbScramblingType.setSelectedIndex(2);
            } else if (ImportedScramblingSystem.equals("videocrypt1+2")) {
                cmbScramblingType.setSelectedIndex(3);
            } else if (ImportedScramblingSystem.equals("videocrypts")) {
                cmbScramblingType.setSelectedIndex(4);
            } else if (ImportedScramblingSystem.equals("syster")) {
                cmbScramblingType.setSelectedIndex(5);
            } else if ((ImportedScramblingSystem.equals("d11")) && (Fork.equals("CJ")) ) {
                cmbScramblingType.setSelectedIndex(6);
            } else if ((ImportedScramblingSystem.equals("systercnr")) && (Fork.equals("CJ")) ) {
                cmbScramblingType.setSelectedIndex(7);
            } else if ((ImportedScramblingSystem.equals("systerls+cnr")) && (Fork.equals("CJ")) ) {
                cmbScramblingType.setSelectedIndex(8);
            } else {
                invalidConfigFileValue("scrambling system", ImportedScramblingSystem);
                ImportedScramblingSystem = "";
            }
        } else if (radMAC.isSelected()) {
            if (ImportedScramblingSystem.isEmpty()) {
                cmbScramblingType.setSelectedIndex(0);
            } else if (ImportedScramblingSystem.equals("single-cut")) {
                cmbScramblingType.setSelectedIndex(1);
            } else if (ImportedScramblingSystem.equals("double-cut")) {
                cmbScramblingType.setSelectedIndex(2);
            } else {
                invalidConfigFileValue("scrambling system", ImportedScramblingSystem);
            }
        }
        // Scrambling key/viewing card type (including VC1 side of dual VC1/2 mode)
        if ( (!ImportedScramblingSystem.isEmpty()) ) {
            if (ImportedKey.isEmpty()) ImportedKey = ImportedKey.replace("", "blank");
            ImportedKey = ImportedKey.replace("eurocrypt ", "");
            int k = ScramblingKeyArray.indexOf(ImportedKey);
            if (k == -1) {
                if (ImportedKey.equals("blank")) ImportedKey = ImportedKey.replace("blank", "");
                if (ImportedScramblingSystem != "videocrypt1+2") {
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
            if ((INIFile.getIntegerFromINI(fileContents, "hacktv", "emm")) != null) {
                Integer ImportedEMM = (INIFile.getIntegerFromINI(fileContents, "hacktv", "emm"));
                String ImportedCardNumber;
                String Imported13Prefix;
                if ( (ImportedEMM.equals(1)) || (ImportedEMM.equals(2)) ){
                    if (ImportedEMM.equals(1)) { chkActivateCard.doClick() ;}
                    if (ImportedEMM.equals(2)) { chkDeactivateCard.doClick() ;}
                    ImportedCardNumber = INIFile.getStringFromINI(fileContents, "hacktv", "cardnumber", "", false);
                    Imported13Prefix = INIFile.getStringFromINI(fileContents, "hacktv-gui3", "13digitprefix", "", false);
                    // The ImportedCardNumber value only contains 8 digits of the card number
                    // To find the check digit, we run the CalculateLuhnCheckDigit method and append the result
                    txtCardNumber.setText(Imported13Prefix + ImportedCardNumber + Luhn.CalculateLuhnCheckDigit(ImportedCardNumber));
                }
            }
        }
        // Show card serial
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "showserial")) {
            chkShowCardSerial.doClick();
        }
        // Brute force PPV key
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "findkey")) {
            chkFindKeys.doClick();
        }
        // Scramble audio
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "scramble-audio")) {
            chkScrambleAudio.doClick();
        }
        // Syster permutation table
        Integer ImportedPermutationTable;
        if (INIFile.getIntegerFromINI(fileContents, "hacktv", "permutationtable") != null) {
            ImportedPermutationTable = INIFile.getIntegerFromINI(fileContents, "hacktv", "permutationtable");
            if ( (Fork.equals("CJ")) && (ScramblingType1.equals("--syster")) || (ScramblingType1.equals("--systercnr")) ) {
                if ( (ImportedPermutationTable >= 0 ) &&
                        (ImportedPermutationTable < cmbSysterPermTable.getItemCount()) ) 
                cmbSysterPermTable.setSelectedIndex(ImportedPermutationTable);
            }
        }       
        // ACP
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "acp")) {
            chkACP.doClick();
        }
        // Filter
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "filter")) {
            chkVideoFilter.doClick();
        }
        // Audio
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "audio") == false) {
            if (chkAudio.isSelected() ) { chkAudio.doClick(); }
        }
        // NICAM
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "nicam") == false) {
            if (chkNICAM.isSelected() ) { chkNICAM.doClick(); }
        }
        // A2 Stereo
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "a2stereo") == true) {
            if ( (!chkA2Stereo.isSelected()) && (A2Supported) ) chkA2Stereo.doClick();
        }
        // ECM
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "showecm")) {
            chkShowECM.doClick();
        }
        // VITS
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "vits")) {
            chkVITS.doClick();
        }
        // Subtitles
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "subtitles")) {
            chkSubtitles.doClick();
            if ( (INIFile.getIntegerFromINI(fileContents, "hacktv", "subtitleindex")) != null ) {
                txtSubtitleIndex.setText(Integer.toString((INIFile.getIntegerFromINI(fileContents, "hacktv", "subtitleindex"))));
            }
        }
        // MAC channel ID
        String ImportedChID = INIFile.getStringFromINI(fileContents, "hacktv", "chid", "", true);
        if (!ImportedChID.isEmpty()) {
            if (!chkMacChId.isSelected()) chkMacChId.doClick();
            txtMacChId.setText(ImportedChID);
        }
        // Disable colour
        if (chkColour.isEnabled()) {
            // Accept both UK and US English spelling
            if ( (INIFile.getBooleanFromINI(fileContents, "hacktv", "nocolour")) ||
                    (INIFile.getBooleanFromINI(fileContents, "hacktv", "nocolor")) ){
                chkColour.doClick();
            }
        }
        // SoapySDR antenna name
        if (cmbOutputDevice.getSelectedIndex() == 1) {
            txtAntennaName.setText(INIFile.getStringFromINI(fileContents, "hacktv", "antennaname", "", false));
        }
        // Output file type
        if (cmbOutputDevice.getSelectedIndex() == 3) {
            switch (INIFile.getStringFromINI(fileContents, "hacktv", "filetype", "", false)) {
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
        String ImportedVolume = INIFile.getStringFromINI(fileContents, "hacktv", "volume", "", false);
        if (!ImportedVolume.isEmpty()) {
            chkVolume.doClick();
            txtVolume.setText(ImportedVolume);
        }
        // Downmix
        if (INIFile.getBooleanFromINI(fileContents, "hacktv", "downmix")) {
            chkDownmix.doClick();
        }
        // Teletext subtitles
        if ( (INIFile.getBooleanFromINI(fileContents, "hacktv", "tx-subtitles")) ){
            chkTextSubtitles.doClick();
            if ( (INIFile.getIntegerFromINI(fileContents, "hacktv", "tx-subindex")) != null ) {
                txtTextSubtitleIndex.setText(Integer.toString((INIFile.getIntegerFromINI(fileContents, "hacktv", "tx-subindex"))));
            }
        }
        else if ( (INIFile.getBooleanFromINI(fileContents, "hacktv", "teletextsubtitles")) ){
            chkTextSubtitles.doClick();
            if ( (INIFile.getIntegerFromINI(fileContents, "hacktv", "teletextsubindex")) != null ) {
                txtTextSubtitleIndex.setText(Integer.toString((INIFile.getIntegerFromINI(fileContents, "hacktv", "teletextsubindex"))));
            }
        }
        // Pixel rate
        Double ImportedPixelRate;
        if ((INIFile.getDoubleFromINI(fileContents, "hacktv", "pixelrate")) != null) {
            if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
            ImportedPixelRate = (INIFile.getDoubleFromINI(fileContents, "hacktv", "pixelrate") / 1000000);
            txtPixelRate.setText(ImportedPixelRate.toString().replace(".0","")); 
        }
        // Sample rate (default to 16 MHz if not specified)
        // Add this last so other changes don't interfere with the value in the
        // configuration file.
        Double ImportedSampleRate;
        if ((INIFile.getDoubleFromINI(fileContents, "hacktv", "samplerate")) != null) {
            ImportedSampleRate = (INIFile.getDoubleFromINI(fileContents, "hacktv", "samplerate") / 1000000);
        } else {
            ImportedSampleRate = Double.parseDouble("16");
            JOptionPane.showMessageDialog(null, "No sample rate specified, defaulting to 16 MHz.", AppName, JOptionPane.INFORMATION_MESSAGE);
        }
        txtSampleRate.setText(ImportedSampleRate.toString().replace(".0","")); 
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
        return (INIFile.getStringFromINI(ModesFile, modeToCheck, "alt", "", false).equals(alt));
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
        JOptionPane.showMessageDialog(null, "The " + settingName + '\u0020' + '\u0022' + value + '\u0022' + 
                " specified in the configuration file could not be found.\n" +
                        "The file may have been created in a newer version or the value is invalid.", AppName,  JOptionPane.WARNING_MESSAGE);
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
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "hackrf");
                }
                else {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "hackrf:" + txtOutputDevice.getText());
                }
                break;
            case 1:
                if (txtOutputDevice.getText().isBlank()) {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "soapysdr");
                }
                else {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "soapysdr:" + txtOutputDevice.getText());
                }
                break;
            case 2:
                if (txtOutputDevice.getText().isBlank()) {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "fl2k");
                }
                else {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "fl2k:" + txtOutputDevice.getText());
                }                
                break;
            case 3:
                if (txtOutputDevice.getText().isBlank()) {
                    JOptionPane.showMessageDialog(null, "Please select an output file or change the output device.", AppName, JOptionPane.WARNING_MESSAGE);
                }
                else {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", txtOutputDevice.getText());
                }                
                break;
            default:
                break;
        }
        // Save current fork if applicable
        if (Fork.equals("CJ")) FileContents = INIFile.setINIValue(FileContents, "hacktv-gui3", "fork", "CaptainJack");
        // Input source or test card
        if (PlaylistAL.size() > 0) {
            // We'll populate the playlist section later
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv-gui3", "playlist", 1);
            // Set start point of playlist
            if (StartPoint != -1) FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv-gui3", "playliststart", StartPoint + 1);
            // Random option
            if (chkRandom.isSelected()) FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv-gui3", "random", 1);
        }
        else {
            if (radTest.isSelected()) {
                if ((Fork == "CJ") && (Lines == 625)) {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "input", "test:" + TCArray[cmbTest.getSelectedIndex()]);
                }
                else {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "input", "test:colourbars");
                }
            }
            else if (txtSource.getText().toLowerCase().endsWith(".m3u")) {
                int M3UIndex = cmbM3USource.getSelectedIndex();
                FileContents = INIFile.setINIValue(FileContents, "hacktv-gui3", "m3usource", txtSource.getText());
                FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv-gui3", "m3uindex", M3UIndex);
                FileContents = INIFile.setINIValue(FileContents, "hacktv", "input", PlaylistURLsAL.get(M3UIndex));
            } else {
                FileContents = INIFile.setINIValue(FileContents, "hacktv", "input", txtSource.getText());
            }
        }
        // Video format/mode
        FileContents = INIFile.setINIValue(FileContents, "hacktv", "mode", Mode);
        // Frequency and channel
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            if (!radCustom.isSelected()) {
                FileContents = INIFile.setINIValue(FileContents, "hacktv-gui3", "channel", cmbChannel.getSelectedItem().toString());
            }
            FileContents = INIFile.setLongINIValue(FileContents, "hacktv", "frequency", Frequency);                
        }
        // Sample rate
        if (Shared.isNumeric(txtSampleRate.getText())) {
            FileContents = INIFile.setLongINIValue(FileContents, "hacktv", "samplerate", (long) (Double.parseDouble(txtSampleRate.getText()) * 1000000));
        }
        // Pixel rate
        if (Shared.isNumeric(txtPixelRate.getText())) {
            FileContents = INIFile.setLongINIValue(FileContents, "hacktv", "pixelrate", (long) (Double.parseDouble(txtPixelRate.getText()) * 1000000));
        }
        // Gain
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "gain", Integer.parseInt(txtGain.getText()));
        }
        // RF Amp
        if (chkAmp.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "amp", 1); }
        // Output level
        if (chkOutputLevel.isSelected()) { FileContents = INIFile.setINIValue(FileContents, "hacktv", "level", txtOutputLevel.getText());}
        // FM deviation
        if (chkFMDev.isSelected()) { FileContents = INIFile.setLongINIValue(FileContents, "hacktv", "deviation", (long) (Double.parseDouble(txtFMDev.getText()) * 1000000)); }
        // Gamma
        if (chkGamma.isSelected()) { FileContents = INIFile.setINIValue(FileContents, "hacktv", "gamma", txtGamma.getText()); }
        // Repeat
        if (chkRepeat.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "repeat", 1); }
        // Position
        if (chkPosition.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "position", Integer.parseInt(txtPosition.getText()));}
        // Verbose
        if (chkVerbose.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "verbose", 1); }
        // Logo
        if (chkLogo.isSelected()) { FileContents = INIFile.setINIValue(FileContents, "hacktv", "logo", LogoArray[cmbLogo.getSelectedIndex()]) ; }
        // Timestamp
        if (chkTimestamp.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "timestamp", 1); }
        // Interlace
        if (chkInterlace.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "interlace", 1); }
        // Teletext
        if (txtTeletextSource.getText().endsWith(".t42")) {
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "teletext", "raw:" + txtTeletextSource.getText());
        } else if (!txtTeletextSource.getText().isEmpty()) {
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "teletext", txtTeletextSource.getText());
        }
        /* WSS
         * We increase the value by one, because zero is interpreted as "option disabled" while 1 is
         * interpreted as "auto". We will subtract this again when opening.
        */
        if (chkWSS.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "wss", cmbWSS.getSelectedIndex() + 1); }
        // AR Correction
        if (chkARCorrection.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "arcorrection", cmbARCorrection.getSelectedIndex()); }
        // Scrambling
        // VideoCrypt I+II
        if (cmbScramblingType.getSelectedIndex() == 3) {
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "scramblingtype", "videocrypt1+2");
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "scramblingkey", ScramblingKey1);
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "scramblingkey2", ScramblingKey2);
        }
        // Syster dual mode (line shuffling + cut-and-rotate)
        else if ( cmbScramblingType.getSelectedIndex() == 8) {
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "scramblingtype", "systerls+cnr");
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "scramblingkey", ScramblingKey1);
        }
        else if ( (ScramblingType1.equals("--single-cut")) || (ScramblingType1.equals("--double-cut")) ) {
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "scramblingtype", ScramblingType1.substring(2));
            if (!ScramblingType2.isEmpty()) {
                FileContents = INIFile.setINIValue(FileContents, "hacktv", "scramblingkey", ScramblingType2.substring(2) + '\u0020' + ScramblingKey2);
            }
            else {
                FileContents = INIFile.setINIValue(FileContents, "hacktv", "scramblingkey", "");
            }
        }
        else if (cmbScramblingType.getSelectedIndex() != 0) {
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "scramblingtype", ScramblingType1.substring(2));
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "scramblingkey", ScramblingKey1);
        }
        if (chkActivateCard.isSelected()) {
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "emm", 1);
            if (txtCardNumber.getText().length() == 9) {
                FileContents = INIFile.setINIValue(FileContents, "hacktv", "cardnumber", txtCardNumber.getText().substring(0, 8));
            } else if (txtCardNumber.getText().length() == 13) {
                FileContents = INIFile.setINIValue(FileContents, "hacktv", "cardnumber", txtCardNumber.getText().substring(4, 12));
                FileContents = INIFile.setINIValue(FileContents, "hacktv-gui3", "13digitprefix", txtCardNumber.getText().substring(0, 4));
            } else if (txtCardNumber.getText().length() == 8) {
                FileContents = INIFile.setINIValue(FileContents, "hacktv", "cardnumber", txtCardNumber.getText());
            }
        }
        else if (chkDeactivateCard.isSelected()) {
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "emm", 2);
            if (txtCardNumber.getText().length() == 9) {
                FileContents = INIFile.setINIValue(FileContents, "hacktv", "cardnumber", txtCardNumber.getText().substring(0, 8));
            } else if (txtCardNumber.getText().length() == 13) {
                FileContents = INIFile.setINIValue(FileContents, "hacktv", "cardnumber", txtCardNumber.getText().substring(4, 12));
                FileContents = INIFile.setINIValue(FileContents, "hacktv-gui3", "13digitprefix", txtCardNumber.getText().substring(0, 4));
            } else if (txtCardNumber.getText().length() == 8) {
                FileContents = INIFile.setINIValue(FileContents, "hacktv", "cardnumber", txtCardNumber.getText());
            }
        }
        // Syster permutation table
        if ( (cmbSysterPermTable.getSelectedIndex() == 1) || (cmbSysterPermTable.getSelectedIndex() == 2) ) {
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "permutationtable", cmbSysterPermTable.getSelectedIndex());
        }
        // Show card serial
        if (chkShowCardSerial.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "showserial", 1); }
        // Brute force PPV key
        if (chkFindKeys.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "findkey", 1); }
        // Scramble audio
        if (chkScrambleAudio.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "scramble-audio", 1); }
        // ACP
        if (chkACP.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "acp", 1); }
        // Filter
        if (chkVideoFilter.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "filter", 1); }
        // Audio
        if ( (chkAudio.isSelected()) && (chkAudio.isEnabled()) ) {
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "audio", 1);
        } else if (chkAudio.isEnabled()) {
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "audio", 0); 
        }
        // NICAM
        if (chkNICAM.isSelected()) {
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "nicam", 1);
        } else if ( (!chkNICAM.isSelected()) && (NICAMSupported) ) {
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "nicam", 0);
        }
        // A2 stereo
        if (chkA2Stereo.isSelected()) {
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "a2stereo", 1);
        } else if ( (!chkA2Stereo.isSelected()) && (A2Supported) ) {
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "a2stereo", 0);
        }
        // Show ECMs
        if (chkShowECM.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "showecm", 1); }
        // Subtitles
        if (chkSubtitles.isSelected()) { 
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "subtitles", 1); 
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "subtitleindex", txtSubtitleIndex.getText());
        }
        // VITS
        if (chkVITS.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "vits", 1); }
        // Disable colour
        if (chkColour.isSelected()) { FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "nocolour", 1); }
        // MAC channel ID
        if (chkMacChId.isSelected()) { FileContents = INIFile.setINIValue(FileContents, "hacktv", "chid", txtMacChId.getText()); }
        // SoapySDR antenna name
        if (cmbOutputDevice.getSelectedIndex() == 1) {
            if (!txtAntennaName.getText().isBlank())
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "antennaname", txtAntennaName.getText());
        }
        // Output file type
        if (cmbOutputDevice.getSelectedIndex() == 3) {
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "filetype", cmbFileType.getItemAt(cmbFileType.getSelectedIndex()));
        }
        // Volume
        if (chkVolume.isSelected()) FileContents = INIFile.setINIValue(FileContents, "hacktv", "volume", txtVolume.getText());
        // Downmix
        if (chkDownmix.isSelected()) FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "downmix", 1);
        // Teletext subtitles
        if (chkTextSubtitles.isSelected()) {
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "tx-subtitles", 1);
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "tx-subindex", txtTextSubtitleIndex.getText());
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
        try {
            FileWriter fw = new FileWriter(DestinationFileName, StandardCharsets.UTF_8);
            fw.write(FileContents);
            fw.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "An error occurred while writing to this file. "
                    + "The file may be read-only or you may not have the correct permissions.", AppName, JOptionPane.ERROR_MESSAGE);
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
        /** Basic Extended M3U file parser.
        *  This reads the channel name from each even-numbered line and the
        *  URL from each odd-numbered line. They're added to arrays to populate
        *  a combobox.
        *  The M3UIndex variable is an integer value which specifies the index
        *  number to select in the combobox.
        */
        // Set mouse cursor to busy
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Temporarily disable the radio buttons, Browse and Run buttons, and menus
        btnSourceBrowse.setEnabled(false);
        radLocalSource.setEnabled(false);
        radTest.setEnabled(false);
        btnRun.setEnabled(false);
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
        // Load source file to path
        Path fd = Paths.get(SourceFile);
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(SourceFile, StandardCharsets.UTF_8));
            LineNumberReader lnr2 = new LineNumberReader(br2);
            String FileContents = lnr2.readLine();
            br2.close();
            if ( (FileContents == null)) {
                JOptionPane.showMessageDialog(null, "Invalid file format, only Extended M3U files are supported.", AppName, JOptionPane.ERROR_MESSAGE);
                resetM3UItems(false);
                return;
            }
            // Check that the file is in the correct format by loading its first line
            // We use endsWith to avoid problems caused by Unicode BOMs
            else if (!FileContents.endsWith("#EXTM3U") ) {
                // Treat the file as a standard text-only platlist and populate
                String[] pls = Files.readString(fd, StandardCharsets.UTF_8).split("\\n");
                for (int i = 0; i < pls.length; i++) {
                    PlaylistAL.add(pls[i]);
                }
                populatePlaylist();
                resetM3UItems(false);
                return;
            }
        } catch (HeadlessException | IOException ex) {
            System.out.println(ex);
            resetM3UItems(false);
            return;
        }
        // Create a SwingWorker to do the disruptive stuff
        SwingWorker<Boolean, Void> m3uWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                String M3UNames = "";
                ArrayList <String> PlaylistNamesAL = new ArrayList <> ();
                PlaylistURLsAL = new ArrayList <> ();
                try {
                    // Read source file to a string.
                    // This allows us to manipulate it if necessary.
                    Path fn = Path.of(SourceFile);
                    String fileContents = Files.readString(fn, StandardCharsets.UTF_8); 
                    // Strip out any blank lines
                    fileContents = fileContents.replaceAll("(?m)^[ \t]*\r?\n", "");
                    // Set up a BufferedReader and LineNumberReader to parse
                    // the string we got above
                    BufferedReader br = new BufferedReader(new StringReader(fileContents));
                    LineNumberReader lnr = new LineNumberReader(br);
                    long linecount = fileContents.lines().count();
                    for (int i = 1; i <= linecount; i++) {
                        if (i % 2 == 0) {
                            // Read even-numbered lines to the M3UFile string so we can parse it
                            M3UNames = M3UNames + lnr.readLine() + System.lineSeparator();
                        }
                        else {
                            // Read odd-numbered lines (URLs) directly to the ArrayList
                            PlaylistURLsAL.add(lnr.readLine());
                        }
                    }
                    br.close();
                    // Remove the first entry of the URL ArrayList as this 
                    // contains the file header
                    PlaylistURLsAL.remove(0);
                } catch (IOException ex) {
                    System.out.println(ex);
                    return false;
                }
                // Use a regex to retrieve the contents of each even-numbered 
                // line after the last comma. This contains the channel name.
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
                JOptionPane.showMessageDialog(null, "An error occurred while processing this file. It may be invalid or corrupted.", AppName, JOptionPane.ERROR_MESSAGE);
                resetM3UItems(false); 
            }
           } // End done()
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
        fileMenu.setEnabled(true);
        templatesMenu.setEnabled(true);
        if (!LoadSuccessful) {
            // Hide the combobox and show the source textbox
            // Use this for a load failure
            resetM3UItems(true);
            txtSource.setVisible(true);
            txtSource.setText("");
            cmbM3USource.setVisible(false);
            cmbM3USource.setEnabled(false);           
        }
    }
    
    private void resetAllControls() {
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
        if (!radCustom.isEnabled()) cmbVideoFormat.setSelectedIndex(0);
        // Reset output device to HackRF
        cmbOutputDevice.setSelectedIndex(0);
        // Reset playlist start point
        StartPoint = -1;
        // Select default radio buttons and comboboxes
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
        if (menuSave.getText() == "Save") { menuSave.setText("Save..."); }
    }

    private void downloadTeletext(String url, String destinationFile) {
        ArrayList<String> TeletextLinks = new ArrayList<>();
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Downloads the specified file from URL and saves it to DestinationFile
        // Create temp directory if it does not exist
        createTempDirectory();
        // Specify the destination location of the HTML file we will download
        String DownloadPath = TempDir + OS_SEP + destinationFile;
        try {
            // If the file already exists from a previous attempt, delete it
            File f = new File(DownloadPath);
            if (f.exists()) Shared.deleteFSObject(f.toPath());
            // Download the index page
            txtAllOptions.setText("Downloading index page from " + url);
            Shared.download(url, DownloadPath);
        }
        catch (IOException ex) {
            System.out.println(ex);
            JOptionPane.showMessageDialog(null, "An error occurred while downloading files. "
                    + "Please ensure that you are connected to the internet and try again.", AppName, JOptionPane.ERROR_MESSAGE);
            txtAllOptions.setText("Cancelled");
            resetTeletextButtons();
            return;
        }
        // Create a SwingWorker to do the disruptive stuff
        SwingWorker<Integer, Integer> downloadPages = new SwingWorker<Integer, Integer>() {
            @Override
            protected Integer doInBackground() throws Exception {
            File f;
            Path fd = Paths.get(TempDir + OS_SEP + HTMLTempFile);
            // Try to read the downloaded index file to a string
            try {
                HTMLFile = Files.readString(fd);
            }
            catch (IOException ex) {
                System.out.println(ex);
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
            // Set SPARK prerequisites - change URL first
            if (DownloadURL == "https://github.com/spark-teletext/spark-teletext/") {
                DownloadURL = "https://raw.githubusercontent.com/spark-teletext/spark-teletext/master/";
                f = new File(TempDir + OS_SEP + "spark");
            } else {
            // Set Teefax temp directory
                f = new File(TempDir + OS_SEP + "teefax");
            }
            /*  Delete this directory if it already exists (e.g. from a previous
                download attempt).
            */
            if (Files.exists(f.toPath())) {
                try {
                    Shared.deleteFSObject(f.toPath());
                } catch (IOException ex) {
                    System.out.println(ex);
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
                        Shared.download(DownloadURL + TeletextLinks.get(i), TeletextPath + OS_SEP + TeletextLinks.get(i));
                        // Stop when the integer value reaches the size of the teletext array
                        if (j == TeletextLinks.size() ) { return 0; }
                    }
                    catch (IOException ex) {
                        System.out.println(ex);
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
                } catch (InterruptedException | ExecutionException ex) {
                    System.out.println(ex);
                    status = 999;
                }
                switch (status) {
                    case 0:
                        // All good
                        txtAllOptions.setText("Done");
                        txtTeletextSource.setText(TeletextPath);
                        break;
                    case 1:
                        // Download cancelled by the user
                        pbTeletext.setValue(0);
                        txtAllOptions.setText("Cancelled");
                        break;
                    case 2:
                        // The index page was downloaded but a teletext page failed.
                        // Connection failure?
                        JOptionPane.showMessageDialog(null, "An error occurred while downloading files. "
                                + "Please ensure that you are connected to the internet and try again.", AppName, JOptionPane.ERROR_MESSAGE);
                        pbTeletext.setValue(0);
                        txtAllOptions.setText("Failed");
                        break;
                    case 3:
                        // The index page was downloaded but we didn't find anything.
                        // Most likely means that we need to revise this!
                        JOptionPane.showMessageDialog(null, "No teletext files were found.", AppName, JOptionPane.ERROR_MESSAGE);
                        pbTeletext.setValue(0);
                        txtAllOptions.setText("Failed");
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "An unknown error has occurred, code " + status, AppName, JOptionPane.ERROR_MESSAGE);
                        pbTeletext.setValue(0);
                        txtAllOptions.setText("Failed");
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
                txtAllOptions.setText("Downloading page " + TeletextLinks.get(i -1) + " (" + i + " of " + TeletextLinks.size() + ")");
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
        btnSpark.setText("SPARK");
        btnSpark.setEnabled(true);
        btnRun.setEnabled(true);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    private void enableColourControl() {
        chkColour.setEnabled(true);
    }
    
    private void disableColourControl() {
        if (chkColour.isSelected()) {
            chkColour.doClick();
            chkColour.setEnabled(false);
        } else {
            chkColour.setEnabled(false);
        }        
    }

    private void astraTemplate(Double localOscillator) {
        int q = JOptionPane.showConfirmDialog(null, "This will load template values for an Astra satellite receiver configured for a "
                + localOscillator + " GHz LO LNB.\n"
                        + "All current settings will be cleared. Do you wish to continue?", AppName, JOptionPane.YES_NO_OPTION);
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
                cmbVideoFormat.setSelectedIndex(a);
            }
            else {
                JOptionPane.showConfirmDialog(null, "Unable to find the PAL-FM mode, which is required for this template.", AppName, JOptionPane.ERROR_MESSAGE);
                resetAllControls();
                return;
            }
            // Enable pre-emphasis filter and set FM deviation to 10 MHz
            chkVideoFilter.doClick();
            chkFMDev.doClick();
            txtFMDev.setText("10");
            // Set IF to Sky News
            txtFrequency.setText(String.format("%.2f",(11.377 - localOscillator) * 1000));
            JOptionPane.showMessageDialog(null, "Template values have been loaded. Tune your receiver to Sky News"
                    + " (11.377 GHz) and run hacktv.", AppName, JOptionPane.INFORMATION_MESSAGE);            
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
        if (Fork.equals("CJ")) { 
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
        if (Fork.equals("CJ")) {
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
                if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
                txtPixelRate.setText("28");
                disableScramblingKey2();                
                sconf = "videocrypt";
                break;
            case "videocrypt2":
                // Set pixel rate to 28 MHz (multiples of 14 are OK)
                if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
                txtPixelRate.setText("28");
                disableScramblingKey2();
                sconf = "videocrypt2";
                break;
            case "videocrypts":
                disableScramblingKey2();
                // Set pixel rate to 17.75 MHz (more accurately 17.734475 but
                // this is reported by hacktv as unsuitable for 625/50)
                if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
                txtPixelRate.setText("17.75");
                sconf = "videocrypts";
                break;
            case "syster":
                // No pixel sample rate required for Syster
                disableScramblingKey2();
                sconf = "syster";
                break;
            case "d11":
                // Set pixel rate to 17.75 MHz
                if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
                txtPixelRate.setText("17.75");
                disableScramblingKey2();
                sconf = "syster";
                break;
            case "systercnr":
                // Set pixel rate to 17.75 MHz
                if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
                txtPixelRate.setText("17.75");
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
        String slist = INIFile.splitINIfile(ModesFile, sconf);
        if (slist == null) {
            ScramblingType1 = "";
            cmbScramblingType.setSelectedIndex(0);
            addScramblingKey();
            JOptionPane.showMessageDialog(null, "The scrambling key information in Modes.ini appears to be "
                    + "missing or corrupt for the selected scrambling type.", AppName, JOptionPane.WARNING_MESSAGE);
            return;
        }
        // We just want the commands so remove everything after =
        slist = slist.replaceAll("\\=.*", "");       
        // Remove commented out lines
        slist = Stream.of(slist.split("\n"))
                .filter(f -> !f.contains(";"))
                .collect(Collectors.joining("\n"));
        int count = (int) slist.lines().count();
        String[] ScramblingKey = new String[count - 1];
        ScramblingKey = slist.substring(slist.indexOf("\n") +1).split("\\r?\\n");
        // Extract friendly names and add commands to an ArrayList
        ScramblingKeyArray = new ArrayList<>();
        if ( (sconf == "single-cut")|| (sconf == "double-cut") ) {
            ScramblingKey[0] = ("No conditional access (free");
            ScramblingKeyArray.clear();
            ScramblingKeyArray.add("");
        }
        for (int i = 0; i < ScramblingKey.length; i++) {
            ScramblingKeyArray.add(ScramblingKey[i]);
            ScramblingKey[i] = INIFile.getStringFromINI(ModesFile , sconf, ScramblingKey[i], "", true);
        }
        // Populate key 1 combobox
        cmbScramblingKey1.setModel(new DefaultComboBoxModel<>(ScramblingKey));
        cmbScramblingKey1.setSelectedIndex(0);
        
        // VC1+2 dual mode    
        if (cmbScramblingType.getSelectedIndex() == 3) {
            final String sconf2 = "videocrypt2";
            if (Fork.equals("CJ")) {
                enableScramblingKey1();
                enableScramblingKey2();
            }
            else {
                disableScramblingKey1();
                disableScramblingKey2();
            }
            cmbScramblingKey2.removeAllItems();
            // Extract (from ModesFile) the VC2 scrambling key section
            String slist2 = INIFile.splitINIfile(ModesFile, sconf2);
            if (slist2 == null) {
                JOptionPane.showMessageDialog(null, "The scrambling key information in Modes.ini appears to be "
                        + "missing or corrupt for the selected scrambling type.", AppName, JOptionPane.WARNING_MESSAGE);
                cmbScramblingType.setSelectedIndex(0);
                return;
            }
            // We just want the commands so remove everything after =
            slist2 = slist2.replaceAll("\\=.*", "");       
            // Remove commented out lines
            slist2 = Stream.of(slist2.split("\n"))
                    .filter(f -> !f.contains(";"))
                    .collect(Collectors.joining("\n"));
            int count2 = (int) slist2.lines().count();
            String[] ScramblingKey2A = new String[count2 - 1];
            ScramblingKey2A = slist2.substring(slist2.indexOf("\n") +1).split("\\r?\\n");
            // Extract friendly names and add commands to an ArrayList
            ScramblingKey2Array = new ArrayList<>();
            for (int i = 0; i < ScramblingKey2A.length; i++) {
                ScramblingKey2Array.add(ScramblingKey2A[i]);
                ScramblingKey2A[i] = INIFile.getStringFromINI(ModesFile , sconf2, ScramblingKey2A[i], "", true);
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
        // Enable card serial option
        if ( ((ScramblingType1).equals("--videocrypt")) || 
                ((ScramblingType1).equals("--videocrypt2")) ) {
            if (Fork.equals("CJ")) { chkShowCardSerial.setEnabled(true); }
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
            if (Fork.equals("CJ")) {
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
            if (Fork.equals("CJ")) { chkShowECM.setEnabled(true); }
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
    
    private void checkWSS() {
        // Populate WSS parameters if enabled
        if (chkWSS.isSelected()) {
            WssParam = "--wss";
            WssMode = WSSModeArray[cmbWSS.getSelectedIndex()];
        }
            else {
            WssParam = "";
            WssMode = "";
        }        
    }
    
    private void addARCorrectionOptions() {
        String[] ARCorrectionMode = {
            "Stretched",
            "Letterboxed",
            "Cropped"
        };
        ARCorrectionModeArray = new String[] {
            "",
            "--letterbox",
            "--pillarbox"
        };
        cmbARCorrection.removeAllItems();
        cmbARCorrection.setModel(new DefaultComboBoxModel<>(ARCorrectionMode));
        cmbARCorrection.setSelectedIndex(0);
    }
    
    private void checkARCorrectionOptions() {
        if (chkARCorrection.isSelected()) {
            ScalingMode = ARCorrectionModeArray[cmbARCorrection.getSelectedIndex()];
        }
        else {
            ScalingMode = "";
        }
    }
    
    private void addLogoOptions() {
        // Extract (from ModesFile) the logo list
        String logos = INIFile.splitINIfile(ModesFile, "logos");
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
            LogoNames[i] = (INIFile.getStringFromINI(ModesFile, "logos", LogoArray[i], "", true));
        }
        cmbLogo.removeAllItems();
        cmbLogo.setModel(new DefaultComboBoxModel<>(LogoNames));
        cmbLogo.setSelectedIndex(-1);
    }
    
    private void checkLogo() {
        // Populate logo parameters if enabled
        if (chkLogo.isSelected()) {
            LogoParam = "--logo";
            LogoFileName = LogoArray[cmbLogo.getSelectedIndex()];
        }
            else {
            LogoParam = "";
            LogoFileName = "";
        }        
    }
    
    private void addTestCardOptions() {
        // Extract (from ModesFile) the test card list
        String testcards = INIFile.splitINIfile(ModesFile, "testcards");
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
            // Remove commented out lines
            testcards = Stream.of(testcards.split("\n"))
                    .filter(f -> !f.contains(";"))
                    .collect(Collectors.joining("\n"));     
            // Add a headerless string to TCArray by splitting off the first line
            TCArray = testcards.substring(testcards.indexOf("\n") +1).split("\\r?\\n");
            // Populate TCNames by reading ModesFile using what we added
            // to TCArray.
            String[] TCNames = new String[TCArray.length];
            for (int i = 0; i < TCArray.length; i++) {
                TCNames[i] = INIFile.getStringFromINI(ModesFile, "testcards", TCArray[i], "", true);
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
    
    private void checkTestCard() {
        if (cmbTest.isEnabled()) {
            InputSource = "test:" + TCArray[cmbTest.getSelectedIndex()];
        }
        else if (radTest.isSelected()) {
            InputSource = "test:colourbars";
        }
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
        lblRegion.setText(" ");
        if (chkAmp.isSelected()) chkAmp.doClick();
        chkAmp.setEnabled(false);
        lblAntennaName.setEnabled(false);
        txtAntennaName.setText("");
        txtAntennaName.setEnabled(false);
    }
    
    private void checkVideoFormat() {
    // Here, we read the selected combobox index and use that number to get
    // the corresponding video format from the required mode array.
        if (radPAL.isSelected()) {
            Mode = PALModeArray[cmbVideoFormat.getSelectedIndex()];
        }
        else if (radNTSC.isSelected()) {
            Mode = NTSCModeArray[cmbVideoFormat.getSelectedIndex()];
        }
        else if (radSECAM.isSelected()) {
            Mode = SECAMModeArray[cmbVideoFormat.getSelectedIndex()];
        }
        else if (radBW.isSelected()) {
            Mode = OtherModeArray[cmbVideoFormat.getSelectedIndex()];
        }
        else if (radMAC.isSelected()) {
            Mode = MACModeArray[cmbVideoFormat.getSelectedIndex()];
        }
        // Start reading the section we found above, starting with line count
        if (INIFile.getIntegerFromINI(ModesFile, Mode, "lines") != null) {
            Lines = INIFile.getIntegerFromINI(ModesFile, Mode, "lines");
        }
        else {
            JOptionPane.showMessageDialog(null, "Unable to read the \"lines\" value for this mode. "
                    + "Defaulting to 525.", AppName, JOptionPane.WARNING_MESSAGE);
            if (cmbVideoFormat.getItemCount() > 1) {
                cmbVideoFormat.setSelectedIndex(PreviousIndex);                
            }
            else {
                // Default to 525 lines so we don't enable 625-specific stuff
                Lines = 525;
            }
        }
        switch (INIFile.getStringFromINI(ModesFile, Mode, "modulation", "", false)) {
            case "vsb":
                Baseband = false;
                if (!chkVideoFilter.isEnabled()) chkVideoFilter.setEnabled(true);
                disableFMDeviation();
                break;
            case "fm":
                Baseband = false;
                if (!chkVideoFilter.isEnabled()) chkVideoFilter.setEnabled(true);
                enableFMDeviation();
                break;
            case "baseband":
                Baseband = true;
                if (!checkBasebandSupport()) return;
                break;
            default:
                JOptionPane.showMessageDialog(null, "No modulation specified, defaulting to VSB.", AppName, JOptionPane.INFORMATION_MESSAGE);
                Baseband = false;
                if (!chkVideoFilter.isEnabled()) chkVideoFilter.setEnabled(true);
                disableFMDeviation();
                break;
        }
        if (INIFile.getDoubleFromINI(ModesFile, Mode, "sr") != null) {
            DefaultSampleRate = Double.toString(INIFile.getDoubleFromINI(ModesFile, Mode, "sr") / 1000000).replace(".0", "");
        }
        else {
            JOptionPane.showMessageDialog(null, "No sample rate specified, defaulting to 16 MHz.", AppName, JOptionPane.INFORMATION_MESSAGE);
            DefaultSampleRate = "16";
        }
        if (INIFile.getBooleanFromINI(ModesFile, Mode, "colour")) {
            enableColourControl();
        }
        else {
            disableColourControl();
        }
        if (INIFile.getBooleanFromINI(ModesFile, Mode, "audio")) {
            enableAudioOption();
        }
        else {
            disableAudioOption();
        }
        if (INIFile.getBooleanFromINI(ModesFile, Mode, "nicam")) {
            enableNICAM();
        }
        else {
            disableNICAM();
        }
        if (INIFile.getBooleanFromINI(ModesFile, Mode, "a2stereo")) {
            enableA2Stereo();
        }
        else {
            disableA2Stereo();
        }
        if (INIFile.getBooleanFromINI(ModesFile, Mode, "teletext")) {
            enableTeletext();
        }
        else {
            disableTeletext();
        }
        if (INIFile.getBooleanFromINI(ModesFile, Mode, "wss")) {
            enableWSS();
        }
        else {
            disableWSS();
        }
        if (INIFile.getBooleanFromINI(ModesFile, Mode, "vits")) {
            enableVITS();
        }
        else {
            disableVITS();
        }
        if (INIFile.getBooleanFromINI(ModesFile, Mode, "acp")) {
            ACPSupported = true;
            enableACP();
        }
        else {
            ACPSupported = false;
            disableACP();
        }
        if (INIFile.getBooleanFromINI(ModesFile, Mode, "scrambling")) {
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
            enableChannelID();
            if (chkMacChId.isSelected()) chkMacChId.doClick();
            chkAudio.setEnabled(false);
            AudioParam = "";
        }
        else {
            disableChannelID();
        }
        String u = INIFile.getStringFromINI(ModesFile, Mode, "uhf", "0", false);
        if ( (u.equals("0")) || (u.isBlank())) {
            disableUHF();
        }
        else {
            enableUHF();
        }
        String v = INIFile.getStringFromINI(ModesFile, Mode, "vhf", "0", false);
        if ( (v.equals("0")) || (v.isBlank()) ) {
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
        AudioParam = "";
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
            JOptionPane.showMessageDialog(null, "This mode is not supported by the selected output device.", AppName, JOptionPane.WARNING_MESSAGE);
            cmbVideoFormat.setSelectedIndex(PreviousIndex);
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
        if ((Fork.equals("CJ")) && (radLocalSource.isSelected())) {
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
    
    private boolean checkTeletextSource() {
        if (chkTeletext.isSelected()) {
            // If the txtTeletextSource field contains quotes, remove them
            if ((txtTeletextSource.getText()).contains(String.valueOf((char)34))) {
                txtTeletextSource.setText(txtTeletextSource.getText().replaceAll(String.valueOf((char)34), ""));
            }
            if ((txtTeletextSource.getText()).isEmpty()) {
                // Create a temp directory if it does not exist
                createTempDirectory();
                // Copy the demo page resource to the temp directory
                try {
                    Shared.copyResource("/com/steeviebops/resources/demo.tti", TempDir.toString() + "/demo.tti", this.getClass());   
                    if (RunningOnWindows) {
                        TeletextSource = '\"' + TempDir.toString() + OS_SEP + "demo.tti"+ '\"';
                    }
                    else {
                        TeletextSource = TempDir.toString() + OS_SEP + "demo.tti";
                    }
                } catch (IOException ex) {
                    System.out.println("An error occurred while attempting to copy to the temp directory: " + ex);
                }
            }
            else if ( (txtTeletextSource.getText().toLowerCase().endsWith(".t42")) && (RunningOnWindows) ) {
                TeletextSource = "raw:" + '\"' + txtTeletextSource.getText() + '\"';
            }
            else if (txtTeletextSource.getText().toLowerCase().endsWith(".t42")) {
                TeletextSource = "raw:" + txtTeletextSource.getText();
            }
            else if (RunningOnWindows) {
                TeletextSource = '\"' + txtTeletextSource.getText() + '\"';
            }
            else {
                TeletextSource = txtTeletextSource.getText();
            }
            if ( (chkTextSubtitles.isSelected()) && (!txtTeletextSource.getText().isBlank()) ) {
                String p888err = "This directory contains a teletext file (P888.tti) for page 888. "
                        + "This could cause hacktv to crash when teletext subtitles are enabled. "
                        + "Please move or delete this file and try again.";
                String p888warn = "This directory contains teletext files in the page 800 range. "
                        + "This could cause subtitles to be unreliable. Please move these files "
                        + "if you encounter problems.";
                // If the teletext source is set to SPARK with subtitles enabled, delete their page 888 to avoid issues
                if ( (TempDir != null) && (txtTeletextSource.getText().contains(TempDir + OS_SEP + "spark")) ) {
                    if ( (Files.exists(Path.of(TempDir + "/spark/P888.tti"))) || (Files.exists(Path.of(TempDir + "/spark/p888.tti"))) ) {
                        try {
                            Shared.deleteFSObject(Path.of(TempDir + "/spark/P888.tti"));
                        }
                        catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, p888err, AppName, JOptionPane.ERROR_MESSAGE);
                            return false;
                        }                    
                    }
                }
                // If the teletext source contains a P888.tti file, abort because hacktv will crash.
                // The latter two if statements are to prevent a NPE if an absolute path is specified.
                else if ( (Files.exists(Path.of(txtTeletextSource.getText() + "/P888.tti"))) || 
                        (txtTeletextSource.getText().toLowerCase().endsWith("p888.tti")) ||
                        (txtTeletextSource.getText().toLowerCase().endsWith("p888.ttix")) ) {
                    JOptionPane.showMessageDialog(null, p888err, AppName, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                // If the directory contains any text files in the page 800 range (p8*.tti or p8*.ttix)
                // generate a warning because this can prevent subtitles from running in real time.
                if ( (Shared.wildcardFind(txtTeletextSource.getText(), "p8", ".tti") > 0) || 
                        (Shared.wildcardFind(txtTeletextSource.getText(), "p8", ".ttix") > 0) ) {
                    JOptionPane.showMessageDialog(null, p888warn, AppName, JOptionPane.WARNING_MESSAGE);
                    return true;
                }
            }
        }
        return true;
    }
    
    private void enableChannelID() {
        chkMacChId.setEnabled(true);
    }
    
    private void disableChannelID() {
        if (chkMacChId.isSelected()) {
            chkMacChId.doClick();
        }
        chkMacChId.setEnabled(false);
    }
    
    private void setFrequency() {
    // Here, we read the selected combobox index and use that number to get
    // the corresponding frequency from the Frequency array.
        Frequency = FrequencyArray[cmbChannel.getSelectedIndex()];
    // Convert the imported value so we can display it in MHz on-screen
        DecimalFormat df = new DecimalFormat("0.00");
        double input = Frequency;
        txtFrequency.setText((df.format(input / 1000000))/* .replace(".00", "") */);
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
        // Clear NICAMParam as we don't need it if NICAM is not supported anyway
        NICAMParam = "";
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
        // Clear A2StereoParam as we don't need it if A2 is not supported anyway
        A2StereoParam = "";
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
            String bpname = INIFile.getStringFromINI(ModesFile, Mode, band, "", false);
            // Extract (from ModesFile) the bandplan section that we need
            String bp = INIFile.splitINIfile(ModesFile, bpname);
            if (bp == null) {
                JOptionPane.showMessageDialog(null, "The bandplan data in Modes.ini appears to be "
                        + "missing or corrupt for the selected band.", AppName, JOptionPane.WARNING_MESSAGE);
                switch (band) {
                    case "vhf":
                    radVHF.setEnabled(false);
                    radCustom.doClick();
                    break;
                    case "uhf":
                    radUHF.setEnabled(false);
                    radCustom.doClick();
                    break;
                    default:
                    radCustom.doClick();
                }
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
            FrequencyArray = new int[ChannelArray.length];
            for (int i = 0; i < ChannelArray.length; i++) {
                if (INIFile.getIntegerFromINI(ModesFile,  bpname, ChannelArray[i]) != null) {
                    FrequencyArray[i] = INIFile.getIntegerFromINI(ModesFile,  bpname, ChannelArray[i]);
                }
                else {
                    throw new IllegalArgumentException("Invalid data returned from Modes.ini, section name: " + bpname);
                }
            }
            // Enable cmbChannel and populate it with the contents of ChannelArray
            cmbChannel.setEnabled(true);       
            cmbChannel.removeAllItems();
            cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
            cmbChannel.setSelectedIndex(0);
            // Set region ID if applicable
            lblRegion.setText(INIFile.getStringFromINI(ModesFile, bpname, "region", "", true));            
        }
        catch (IllegalArgumentException ex) {
            System.err.println(ex);
            JOptionPane.showMessageDialog(null, "An unexpected error occurred while attempting to load "
                    + "the bandplan data for this mode. This may be due to incorrect settings in the Modes.ini file.", AppName, JOptionPane.WARNING_MESSAGE);
            radCustom.doClick();
            // Disable the band that failed
            switch (band) {
                case "vhf":
                    radVHF.setEnabled(false);
                    break;
                case "uhf":
                    radUHF.setEnabled(false);
                    break;
                default:
                    break;
            }
        }
    }
    
    private void resetRunButton() {
        DownloadInProgress = false;
        btnRun.setText("Run hacktv");
        btnRun.setEnabled(true);
        txtConsoleOutput.setText("");
    }
    
    private void youtubedl(String input) {
        // youtube-dl frontend. Pass the download URL as a string.
        int q = JOptionPane.showConfirmDialog(null, "This will attempt to use youtube-dl to download the requested video.\n" +
            "The video file will be saved to the temp folder. Do you wish to continue?", AppName, JOptionPane.YES_NO_OPTION);
        if (q == JOptionPane.YES_OPTION) {
            String ytp;
            String url;
            if (RunningOnWindows) {
                // Check the JAR directory
                if (Files.exists(Path.of(JarDir + "/youtube-dl.exe"))) {
                    ytp = JarDir + "/youtube-dl.exe";
                }
                else {
                    // Hope it can be found in the PATH...
                    ytp = "youtube-dl.exe";
                }
            }
            else {
                ytp = "youtube-dl";
            }
            // Remove the ytdl: prefix if specified
            if (input.toLowerCase().startsWith("ytdl:")) {
                url = input.substring(5);
            } 
            else {
                url = input;
            }
            btnRun.setText("Stop download");
            txtAllOptions.setText("Checking URL, please wait...");
            // Check if the supplied URL is a live stream or not
            SwingWorker <String, Void> checkYTDL = new SwingWorker <String, Void> () {
                @Override
                protected String doInBackground() throws Exception {
                    ProcessBuilder yt = new ProcessBuilder(ytp, "-g", url);
                    yt.redirectErrorStream(true);
                    String f = null;
                    // Try to start the process
                    try {
                        Process pr = yt.start();
                        // Capture the output
                        String a;
                        BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                        while ((a = br.readLine()) != null) {
                            f = a;
                        }
                        br.close();
                    }
                    catch (Exception ex) {
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
                        u = "";
                        runHackTV();
                    }
                    // If it's a live stream, set the manifest (m3u8) URL as the
                    // source and restart. We don't need youtube-dl for this.
                    if ( (u != null) && (u.endsWith(".m3u8")) ) {
                        txtSource.setText(u);
                        runHackTV();
                    }
                    else {
                        // Go to the download method
                        startYTDownload(ytp, url);
                    }
                }
            };
            checkYTDL.execute();
        }
    }
        
    private void startYTDownload(String ytp, String url) {
        DownloadInProgress = true;
        createTempDirectory();
        if (txtSource.getText().endsWith(".m3u8")) return;
        // Start the download
        SwingWorker <String, String> runYTDL = new SwingWorker <String, String> () {
            @Override
            protected String doInBackground() throws Exception {
                ProcessBuilder yt = new ProcessBuilder(ytp, url, "-f bestvideo[protocol!=http_dash_segments]");
                yt.directory(TempDir.toFile());
                yt.redirectErrorStream(true);
                String f = null;
                // Try to start the process
                try {
                    Process pr = yt.start();
                    // Capture the output
                    String a;
                    BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    while ((a = br.readLine()) != null) {
                        if (DownloadCancelled) {
                            String l;
                            if (RunningOnWindows) {
                                l = JarDir + "/windows-kill.exe";
                            }
                            else {
                                l = "kill";
                            }
                            ProcessBuilder s = new ProcessBuilder(l, "-2", Long.toString(pr.pid()));
                            s.start();
                            DownloadCancelled = false;
                            // Return a random string that is unlikely to be a real file
                            return "srilcjdpocjsdpovjcmxpiesghohdj";
                        }
                        // Get the destination file name of the downloaded video
                        else if (a.contains("Destination")) {
                            Pattern p = Pattern.compile("(?<=Destination: )[^\n]*");
                            Matcher m = p.matcher(a);
                            while (m.find()) {
                                if (f == null) f = m.group(0);
                            }
                        }
                        else if (a.endsWith("has already been downloaded")) {
                            // File already exists, extract the file name from the error message
                            // by removing the [download] prefix and the suffix text above.
                            f = a.substring(11, a.length() - 28);
                        }
                        else {
                            // Publish the line we received from youtube-dl
                            publish(a);
                        }
                    }
                    br.close();
                }
                catch (Exception ex) {
                    return "siosjafiosrjfiosmehairlhawev";
                }
                return f;
            }
            @Override
            protected void process(List <String> chunks) {
                for (String o: chunks) {
                    txtAllOptions.setText(o);
                }
            }
            @Override
            protected void done() {
                String s = null;
                try {
                    s = get();
                }
                catch (InterruptedException | ExecutionException ex) {
                    s = "siosjafiosrjfiosmehairlhawev";
                }
                if (s == null) {
                    // Download failed, don't print anything so we can see what happened
                    resetRunButton();
                }
                // Check for the random strings we may have sent above
                else if (s == "srilcjdpocjsdpovjcmxpiesghohdj") {
                    txtAllOptions.setText("Download cancelled");
                    resetRunButton();
                }
                else if (s == "siosjafiosrjfiosmehairlhawev") {
                    txtAllOptions.setText("Download failed");
                    JOptionPane.showMessageDialog(null, "Unable to run youtube-dl. Please ensure that it is installed in your " +
                        "system path, or in the same directory as this application.", AppName, JOptionPane.WARNING_MESSAGE);
                    resetRunButton();
                }
                else if (Files.exists(Path.of(TempDir + OS_SEP + s))) {
                    txtAllOptions.setText("File downloaded to \"" + TempDir + OS_SEP + s + "\"");
                    txtSource.setText(TempDir + OS_SEP + s);
                    resetRunButton();
                    // Restart the runHackTV method with the new source file
                    runHackTV();
                }
            }
        };
        runYTDL.execute();                
    }
    

    private boolean checkInput() {
        // Skip this method if the playlist is populated
        if (PlaylistAL.size() > 0) return true;
        if (radLocalSource.isSelected()) {
            if (cmbM3USource.isVisible()) {
                InputSource = PlaylistURLsAL.get(cmbM3USource.getSelectedIndex());
                return true;
            }
            else if ( (txtSource.getText().contains("://youtube.com/")) ||
                      (txtSource.getText().contains("://www.youtube.com/")) ||
                      (txtSource.getText().contains("://youtu.be/")) ||
                      (txtSource.getText().startsWith("ytdl:")) ) {
                // Invoke the youtube-dl handler
                youtubedl(txtSource.getText());
                // Return false as we're going to restart if the download is successful
                return false;
            }
            else if (!txtSource.getText().isBlank()) { 
                InputSource = txtSource.getText().replace("\"", "");
                return true;
            }
            else {
                JOptionPane.showMessageDialog(null, "Please specify an input file to broadcast or choose the test card option.", AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(0);
                return false;
            }
        }
        else {
            return true;
        }
    }
    
    private boolean checkSampleRate() {
        if (Shared.isNumeric( txtSampleRate.getText())) {
            Double SR = Double.parseDouble(txtSampleRate.getText());
            SampleRate = (int) (SR * 1000000);
            return true;
        }
        else {
            JOptionPane.showMessageDialog(null, "Please specify a valid sample rate in MHz.", AppName, JOptionPane.WARNING_MESSAGE);
            tabPane.setSelectedIndex(1);
            return false;
        }
    }
    
    private boolean checkPixelRate() {
        if (chkPixelRate.isSelected()) {
            try {
                Double PR = Double.parseDouble(txtPixelRate.getText());
                PixelRate = (int) (PR * 1000000);
                return true;
            }
            catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Please specify a valid pixel rate in MHz.", AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(1);
                return false;
            }
        }
        else {
            return true;
        }
    }
   
    private boolean checkFMDeviation() {
        if (chkFMDev.isSelected()) {
            if (Shared.isNumeric(txtFMDev.getText())) {
                Double Deviation = Double.parseDouble(txtFMDev.getText());
                FMDevValue = (int) (Deviation * 1000000);
                return true;
            }
            else {
                JOptionPane.showMessageDialog(null, "Please specify a valid deviation in MHz.", AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(1);
                return false;
            }
        }
        else {
            return true;
        }
    }
    
    private boolean checkCustomFrequency(){
        // This method is only required for custom frequencies, so we skip it
        // and return true if the custom radio button is not selected
        if (radCustom.isSelected()) {
            BigDecimal CustomFreq;
            BigDecimal Multiplier = new BigDecimal(1000000);
            String InvalidInput = "Please specify a frequency between 1 MHz and 7250 MHz.";
            if ( (Shared.isNumeric( txtFrequency.getText())) && (!txtFrequency.getText().contains(" ")) ){
                CustomFreq = new BigDecimal(txtFrequency.getText());
                if ( (CustomFreq.longValue() < 1) || (CustomFreq.longValue() > 7250) ) {
                    JOptionPane.showMessageDialog(null, InvalidInput, AppName, JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(null, InvalidInput, AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(1);
                return false;                  
            }
        }
        return true;
    }
    
    private boolean checkMacChId() {
        if (chkMacChId.isSelected()) {
            if (txtMacChId.getText().matches("^[0-9a-fA-F]+$")) {
                ChID = "0x" + txtMacChId.getText();
                return true;
            }
            else {
                JOptionPane.showMessageDialog(null, "Please specify a valid hexadecimal channel ID.", AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(2);
                return false;
            }
        }
        else {
            return true;
        }
    }
    
    private boolean checkGamma() {
        String InvalidGamma = "Gamma should be a decimal value.";
        if (chkGamma.isSelected()) {
            if (!Shared.isNumeric(txtGamma.getText())) {
                JOptionPane.showMessageDialog(null, InvalidGamma, AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(2);
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return true;
        }
    }
    
    private boolean checkOutputLevel() {
        String InvalidOutputLevel = "Output level should be a decimal value.";
        if (chkOutputLevel.isSelected()) {
            if (!Shared.isNumeric(txtOutputLevel.getText())) {
                JOptionPane.showMessageDialog(null, InvalidOutputLevel, AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(2);
                return false;    
            }
            else {
                return true;
            }
        }
        else {
            return true;
        }
    }
    
    private boolean checkGain() {
        // Only check gain if the text field is enabled
        if (!txtGain.isEnabled()) return true;
        String InvalidGain = "Gain should be between 0 and 47 dB.";
        if (Shared.isNumeric(txtGain.getText())) {
            int Gain = Integer.parseInt(txtGain.getText());
            if ( (Gain >= 0) && (Gain <=47) ) {
                return true;
            }
            else {
                JOptionPane.showMessageDialog(null, InvalidGain, AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(1);
                return false;
            }
        }
        else {
            JOptionPane.showMessageDialog(null, InvalidGain, AppName, JOptionPane.WARNING_MESSAGE);
            tabPane.setSelectedIndex(1);
            return false;
        }
    }
    
    private boolean checkCardNumber() {
        /*  Sky viewing cards use the Luhn algorithm to verify if the card
         *  number is valid. So we will use it here too.
         *  Issue 07 cards have either 13-digit or 9-digit numbers.
         *  Issue 09 cards are 9-digit only. So we restrict input to these lengths.
         *  If an 8-digit number is entered, this is passed to hacktv without
         *  any checks.
         */
        if ( (txtCardNumber.isEnabled()) && (ScramblingType1 == "--videocrypt") ) {
            String LuhnCheckFailed = "Card number appears to be invalid (Luhn check failed).";
            String InvalidCardNumber = "Card number should be exactly 8, 9 or 13 digits.";
            if (!Shared.isNumeric(txtCardNumber.getText())) {
                JOptionPane.showMessageDialog(null, InvalidCardNumber, AppName, JOptionPane.WARNING_MESSAGE);
                return false;
            }
            else if (txtCardNumber.getText().length() == 9) {
                if (!Luhn.LuhnCheck(txtCardNumber.getText())) {
                    JOptionPane.showMessageDialog(null, LuhnCheckFailed, AppName, JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                else {
                    // hacktv doesn't use the check digit so strip it out
                    TruncatedCardNumber = txtCardNumber.getText().substring(0,8);
                    return true;
                }
            }
            else if (txtCardNumber.getText().length() == 13) {
                // Only digits 4-13 of 13-digit card numbers are checked, so we
                // need to strip out the first four digits.
                TruncatedCardNumber = txtCardNumber.getText().substring(4,13);
                if (!Luhn.LuhnCheck(TruncatedCardNumber)) {
                    JOptionPane.showMessageDialog(null, LuhnCheckFailed, AppName, JOptionPane.WARNING_MESSAGE);
                    TruncatedCardNumber = "";
                    return false;
                }
                else {
                    // hacktv doesn't use the check digit so strip it out
                    TruncatedCardNumber = txtCardNumber.getText().substring(4,12);
                    return true;
                }
            }
            else if (txtCardNumber.getText().length() == 8) {
                // Pass the digits unaltered and without Luhn checking
                TruncatedCardNumber = txtCardNumber.getText();
                return true;
            }
            else {
                JOptionPane.showMessageDialog(null, InvalidCardNumber, AppName, JOptionPane.WARNING_MESSAGE);  
                tabPane.setSelectedIndex(4);              
                return false;
            }
        }
        else if ( (txtCardNumber.isEnabled()) && (ScramblingType1 == "--videocrypt2") ) {
            // Pass the digits unaltered and without Luhn checking for MultiChoice cards
            // This is temporary until I work out how to handle them
            TruncatedCardNumber = txtCardNumber.getText();
            return true;
        }
        else {
            // If the txtCardNumber textbox is disabled, return true and exit
            TruncatedCardNumber = "";
            return true;
        }
    }
    
    private void checkTestCardStatus() {
        if ( (!cmbTest.isEnabled()) && (Fork == "CJ") && (Lines == 625) && (HTVLoadInProgress == false) ) {
            // Enable cmbTest (test card dropdown)
            cmbTest.setEnabled(true);
            cmbTest.setSelectedIndex(0);
        }
        else if (HTVLoadInProgress == true) {
            // Do nothing so we don't interrupt the file loading process
        }
        else if ((cmbTest.isEnabled()) && (Fork == "CJ") && (Lines == 625) ) {
            // Do nothing if cmbTest is already enabled on a 625-line mode.
            // This prevents the test card from resetting back to bars when
            // changing video modes.
        }
        else {
            // Disable cmbTest
            cmbTest.setEnabled(false);
            cmbTest.setSelectedIndex(-1);
        }
    }
    
    private boolean checkOutputDevice() {
        // Reset SoapySDR and file parameters
        AntennaParam = "";
        AntennaName = "";
        FileType = "";
        switch (cmbOutputDevice.getSelectedIndex()) {
            case 3:
                // If File is selected, check if the path is blank
                if (txtOutputDevice.getText().isBlank()) {
                    JOptionPane.showMessageDialog(null, "Please select an output file or change the output device.", AppName, JOptionPane.WARNING_MESSAGE);
                    tabPane.setSelectedIndex(1);
                    return false;
                }
                // Do not allow file output to go to the console.
                // Bad things will happen, such as hanging the GUI and consuming large amounts of RAM!
                else if ( (txtOutputDevice.getText().equals("-")) ||
                        (txtOutputDevice.getText().equals("/dev/stdout")) ||
                        (txtOutputDevice.getText().equals("/dev/stderr")) ||
                        (txtOutputDevice.getText().toLowerCase().equals("con")) ) {
                    JOptionPane.showMessageDialog(null, "Outputting to the console is not supported.", AppName, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    if (RunningOnWindows) {
                        // Add quotes on Windows systems to handle paths with white spaces
                        OutputDevice = '\"' + txtOutputDevice.getText()+ '\"';
                    }
                    else {
                        // Don't add quotes on Unix systems as this just messes things up
                        OutputDevice = txtOutputDevice.getText();
                    }
                    if (cmbFileType.getSelectedIndex() != 3) {
                        FileType = "-t" + cmbFileType.getItemAt(cmbFileType.getSelectedIndex());
                    }
                    return true;
                }
            case 2:
                // fl2k
                if (!txtOutputDevice.getText().isBlank()) {
                    OutputDevice = "fl2k" + ":" + txtOutputDevice.getText();
                }
                else {
                    OutputDevice = "fl2k";
                }
                return true;
            case 1:
                // SoapySDR
                if (!txtAntennaName.getText().isBlank()) {
                    AntennaParam = "--antenna";
                    AntennaName = txtAntennaName.getText();
                }
                else {
                    AntennaParam = "";
                    AntennaName = "";
                }
                if (!txtOutputDevice.getText().isBlank()) {
                    OutputDevice = "soapysdr" + ":" + txtOutputDevice.getText();
                }
                else {
                    OutputDevice = "soapysdr";
                }
                return true;
            case 0:
                // HackRF
                if (!txtOutputDevice.getText().isBlank()) {
                    OutputDevice = "hackrf"  + ":" + txtOutputDevice.getText();
                }
                else {
                    OutputDevice = "";
                }
                return true;
            default:
                // This should never run
                return false;
        }
    }
    
    private boolean checkVolume() {
        // Only check volume if the text field is enabled
        if (!txtVolume.isEnabled()) return true;
        String InvalidVolume = "Volume should be a numeric or decimal value.";
        if (Shared.isNumeric(txtVolume.getText())) {
            VolumeParam = "--volume";
            return true;
        }
        else {
            JOptionPane.showMessageDialog(null, InvalidVolume, AppName, JOptionPane.WARNING_MESSAGE);
            tabPane.setSelectedIndex(2);
            return false;
        }
    }
    
    private void runHackTV() {
        ArrayList<String> allArgs = new ArrayList<>();
        // Call each method and check its response. If false, then stop.
        if (!checkInput()) return;
        if (!checkCustomFrequency()) return;
        if (!checkFMDeviation()) return;
        if (!checkGamma()) return;
        if (!checkOutputLevel()) return;
        if (!checkGain()) return;
        if (!checkSampleRate()) return;
        if (!checkPixelRate()) return;
        if (!checkMacChId()) return;
        if (!checkCardNumber()) return;
        if (!checkOutputDevice()) return;
        if (!checkVolume()) return;
        if (!checkTeletextSource()) return;
        // These methods don't return anything but we do need to check them. 
        checkWSS();
        checkARCorrectionOptions();
        checkTestCard();
        checkLogo();
        // Add all possible parameters to an arraylist to feed to ProcessBuilder
        allArgs.add(HackTVPath);
        if (!OutputDevice.isEmpty()) {
            allArgs.add("-o");
            allArgs.add(OutputDevice);
        }
        allArgs.add("-m");
        allArgs.add(Mode);
        // Only add frequency for HackRF or SoapySDR
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1)) {
            allArgs.add("-f");
            allArgs.add(Long.toString(Frequency));
        }
        // Add subtitles here, we need to make sure that subtitles is not the last parameter if
        // no index is specified. Otherwise hacktv reports that no input has been specified.
        if (!SubtitlesParam.isEmpty()) allArgs.add(SubtitlesParam);
        if (!txtSubtitleIndex.getText().isEmpty()) allArgs.add(txtSubtitleIndex.getText());
        if (!TeletextSubtitlesParam.isEmpty()) allArgs.add(TeletextSubtitlesParam);
        if (!txtTextSubtitleIndex.getText().isEmpty()) allArgs.add(txtTextSubtitleIndex.getText());
        allArgs.add("-s");
        allArgs.add(Integer.toString(SampleRate));
        if (chkPixelRate.isSelected()) {
            allArgs.add("--pixelrate");
            allArgs.add(Integer.toString(PixelRate));
        }
        // Only add gain for HackRF or SoapySDR
        if (txtGain.isEnabled()) {
            allArgs.add("-g");
            allArgs.add(txtGain.getText());
        }
        // Optional values second, see if they're defined first before adding
        if (!ChIDParam.isEmpty()) {allArgs.add(ChIDParam);}
        if (!ChID.isEmpty()) {allArgs.add(ChID);}
        if (!AudioParam.isEmpty()) allArgs.add(AudioParam);
        if (!NICAMParam.isEmpty()) allArgs.add(NICAMParam);
        if (!A2StereoParam.isEmpty()) allArgs.add(A2StereoParam);
        if (!ACPParam.isEmpty()) allArgs.add(ACPParam);
        if (!RepeatParam.isEmpty()) allArgs.add(RepeatParam);
        if (!WssParam.isEmpty()) allArgs.add(WssParam);
        if (!WssMode.isEmpty()) allArgs.add(WssMode);
        if (!ScramblingType1.isEmpty()) allArgs.add(ScramblingType1);
        if (!ScramblingKey1.isEmpty()) allArgs.add(ScramblingKey1);
        if (!ScramblingType2.isEmpty()) allArgs.add(ScramblingType2);
        if (!ScramblingKey2.isEmpty()) allArgs.add(ScramblingKey2);
        if (!ScrambleAudio.isEmpty()) allArgs.add(ScrambleAudio);
        if (!SysterPermTable.isEmpty()) allArgs.add(SysterPermTable);
        if (!TeletextParam.isEmpty()) allArgs.add(TeletextParam);
        if (!TeletextSource.isEmpty()) allArgs.add(TeletextSource);
        if (!RFampParam.isEmpty()) allArgs.add(RFampParam);
        if (!FMDevParam.isEmpty()) allArgs.add(FMDevParam);
        if (!AntennaParam.isEmpty()) allArgs.add(AntennaParam);
        if (!AntennaName.isEmpty()) allArgs.add(AntennaName);
        if (chkFMDev.isSelected()) allArgs.add(Integer.toString(FMDevValue));
        if (!GammaParam.isEmpty()) allArgs.add(GammaParam);
        if (!txtGamma.getText().isEmpty()) allArgs.add(txtGamma.getText());
        if (!OutputLevelParam.isEmpty()) allArgs.add(OutputLevelParam);
        if (!txtOutputLevel.getText().isEmpty()) allArgs.add(txtOutputLevel.getText());
        if (!FilterParam.isEmpty()) allArgs.add(FilterParam);
        if (!PositionParam.isEmpty()) allArgs.add(PositionParam);
        if (!txtPosition.getText().isEmpty()) allArgs.add(txtPosition.getText());
        if (!TimestampParam.isEmpty()) allArgs.add(TimestampParam);
        if (!LogoParam.isEmpty()) allArgs.add(LogoParam);
        if (!LogoFileName.isEmpty()) allArgs.add(LogoFileName);
        if (!VerboseParam.isEmpty()) allArgs.add(VerboseParam);
        if (!EMMParam.isEmpty()) allArgs.add(EMMParam);
        if (!TruncatedCardNumber.isEmpty()) {allArgs.add(TruncatedCardNumber);}
        if (!ShowECMParam.isEmpty()) allArgs.add(ShowECMParam);
        if (!ScalingMode.isEmpty()) allArgs.add(ScalingMode);
        if (!InterlaceParam.isEmpty()) allArgs.add(InterlaceParam);
        if (!ShowCardSerial.isEmpty()) allArgs.add(ShowCardSerial);
        if (!FindKey.isEmpty()) allArgs.add(FindKey);
        if (!VITS.isEmpty()) allArgs.add(VITS);
        if (!ColourParam.isEmpty()) allArgs.add(ColourParam);
        if (!FileType.isEmpty()) allArgs.add(FileType);
        if (!VolumeParam.isEmpty()) allArgs.add(VolumeParam);
        if (!txtVolume.getText().isEmpty()) allArgs.add(txtVolume.getText());
        if (!DownmixParam.isEmpty()) allArgs.add(DownmixParam);        
        // Finally, add the source video or test option.
        if (PlaylistAL.size() > 0) {
            InputSource = "";
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
                                if (RunningOnWindows) {
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
                if (i == -1) i++;
                for (int j = 0; j < PlaylistAL.size(); j++) {
                    if ( (i == PlaylistAL.size()) && (StartPoint != 0) ) {
                        i = 0;
                    }
                    if ( (PlaylistAL.get(i).contains("test:")) ||
                        (PlaylistAL.get(i).startsWith("http")) ) {
                        allArgs.add(PlaylistAL.get(i));
                    }
                    else {
                        if (RunningOnWindows) {
                            allArgs.add('\u0022' + PlaylistAL.get(i) + '\u0022');
                        }
                        else {
                            allArgs.add(PlaylistAL.get(i));
                        }
                    }
                    i++;
                }                
            }
        }
        else if (RunningOnWindows) {
            // If it's a local path, add quotes to it, but don't for the test 
            // card or a HTTP stream.
            if ( (InputSource.contains("test:")) ||
                (InputSource.startsWith("http")) ) {
                allArgs.add(InputSource);
            }
            else {
                allArgs.add('\"' + InputSource + '\"');
            }
        } else {
            // Don't add quotes on Unix systems as this just messes things up
            allArgs.add(InputSource);
        }
        // End add to arraylist
        
        // Arguments textbox handling - clear it first
        if (!txtAllOptions.getText().isEmpty()) txtAllOptions.setText("");
        /* Start a for loop to populate the textbox, using the arraylist size as
           the finish value.
        */
        for (int i = 1; i < allArgs.size() ; i++) {
            /* Add value 1 (mode) first and then add all other values. I've set 
               it up this way to prevent a leading space from being printed
               in the textbox.
            */
            if (i == 1) { 
                txtAllOptions.setText(allArgs.get(i)); 
            }
            else {
                txtAllOptions.setText(
                    txtAllOptions.getText() + '\u0020' + allArgs.get(i) );
            }
        }
        // If "Generate syntax only" is enabled, stop here
        if (chkSyntaxOnly.isSelected()) return;
        // Change the Run button to display Stop instead
        changeRunToStop();
        // Clear the console
        txtConsoleOutput.setText("");
        // Spawn a new SwingWorker to run hacktv
        SwingWorker <Void, String> runTV = new SwingWorker <Void, String> () {
            @Override
            protected Void doInBackground() {
                // Create process with the ArrayList we populated above
                ProcessBuilder pb = new ProcessBuilder(allArgs);
                pb.directory(new File(HackTVDirectory));
                pb.redirectErrorStream(true);
                // Try to start the process
                try {
                    Process p = pb.start();
                    // Get the PID of the process we just started
                    pid = p.pid();
                    // Capture the output
                    int a;
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ( (a = br.read()) != -1 ) {
                        // br.read() returns an integer value 'a' which is the ASCII
                        // number of a character it has received from the process.
                        // We convert 'a' to the actual character and publish it.
                        // When the process has closed, br.read() will return -1
                        // which will exit this loop.
                        publish(String.valueOf((char)a));
                    }
                    br.close();
                    publish("\n" + "hacktv stopped");
                }
                catch (IOException ex) {
                    JOptionPane.showMessageDialog(null,
                            "An error occurred while attempting to run hacktv", AppName, JOptionPane.ERROR_MESSAGE);
                }
                return null;
            } // End doInBackground

            // Update the GUI from this method.
            @Override
            protected void done() {
                /* If an invalid parameter is passed to hacktv, it usually
                   responds with its usage message.
                   Here, we check if the first line of the usage has been
                   returned. If so, we assume that one of the parameters we fed 
                   is not supported.
                */
                if (txtConsoleOutput.getText().contains("Usage: hacktv [options] input [input...]")) {
                    JOptionPane.showMessageDialog(null, "This copy of hacktv does not appear to support one or more"
                            + " of the selected options. Please update hacktv and try again."
                            , AppName, JOptionPane.WARNING_MESSAGE);
                }
                // Revert button to display Run instead of Stop
                changeStopToRun();            
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
    
    private void stopTV() {
        /** To stop hacktv gracefully, it needs to be sent a SIGINT signal.
         *  Under Unix/POSIX systems this is easy, just run kill -2 and the PID.
         *  Under Windows it's not so easy, we need an external helper
         *  application. For this, we use:
         *  https://github.com/ElyDotDev/windows-kill/releases
         */
        if (RunningOnWindows) {
            try {
                // Run windows-kill.exe from this path and feed the PID to it
                ProcessBuilder StopHackTV = new ProcessBuilder
                    (JarDir + "\\windows-kill.exe", "-2", Long.toString(pid));
                Process p = StopHackTV.start();
            } catch (IOException ex)  {
                System.out.println(ex);
            }
        } else {
            try {
                // Run kill and feed the PID to it
                ProcessBuilder StopHackTV = new ProcessBuilder
                    ("kill", "-2", Long.toString(pid));
                Process p = StopHackTV.start();                
            } catch (IOException ex)  {
                System.out.println(ex);
            }                
        }
    }
    
    private void changeRunToStop() {
        btnRun.setText("Stop hacktv");
        chkSyntaxOnly.setEnabled(false);
        Running = true;
    }
    
    private void changeStopToRun() {
        btnRun.setText("Run hacktv");
        chkSyntaxOnly.setEnabled(true);
        Running = false;
    }
    
    private void cleanupBeforeExit() {
        // Check if a teletext download is in progress
        // If so, then abort
        if (DownloadInProgress) { DownloadCancelled = true; }
        // Check if hacktv is running, if so then exit it
        if (Running) stopTV();
        // Delete temp directory and files before exit
        if (TempDir != null) {
            try {
                Shared.deleteFSObject(TempDir.resolve(TempDir));
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
            else if ( (!chkSyntaxOnly.isSelected()) && (!Files.exists(Path.of(HackTVPath))) || ((HackTVPath == "")) ) {
                JOptionPane.showMessageDialog(null, "Unable to find hacktv. Please go to the GUI settings tab to add its location.", AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(5);
            }
            else {
                runHackTV();
            }
        } else {
            stopTV();
        }
    }//GEN-LAST:event_btnRunActionPerformed
             
    private void menuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAboutActionPerformed
        // Get application version by checking the timestamp on the class file
        String v;
        String mv;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String classFilePath = "/com/steeviebops/hacktvgui/GUI.class";
            Date date;
            if (Files.exists(Path.of(System.getProperty("java.class.path")))) {
                date = Shared.getLastUpdatedTime(System.getProperty("java.class.path"), classFilePath);
                if (date != null) {
                    v = "\nCompilation date: " + sdf.format(date);
                }
                else {
                    v = "";
                }
            }
            else {
                v = "";
            }
        }
        catch (NumberFormatException e) {
              v = "";
        }
        mv = "\nUsing " + ModesFileLocation + " Modes.ini file, version " + ModesFileVersion;
        JOptionPane.showMessageDialog(null, AppName + " (Java version)" + v + mv + "\n\nCreated 2020-2021 by Stephen McGarry.\n" +
                "Provided under the terms of the General Public Licence (GPL) v2 or later.\n\n" +
                "https://github.com/steeviebops/hacktv-gui\n\n", "About " + AppName, JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_menuAboutActionPerformed

    private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_menuExitActionPerformed
      
    private void chkShowECMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowECMActionPerformed
        if (chkShowECM.isSelected()) {
            ShowECMParam = "--showecm";
        }
        else {
            ShowECMParam = "";
        }
    }//GEN-LAST:event_chkShowECMActionPerformed

    private void chkFindKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkFindKeysActionPerformed
        if (chkFindKeys.isSelected()) {
            FindKey = "--findkey";
        }
        else {
            FindKey = "";
        }
    }//GEN-LAST:event_chkFindKeysActionPerformed

    private void chkScrambleAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkScrambleAudioActionPerformed
        if (chkScrambleAudio.isSelected()) {
            if ( (ScramblingType1.equals("--syster")) ||
                (ScramblingType1.equals("--d11")) ||
                (ScramblingType1.equals("--systercnr")) ) {
                ScrambleAudio = "--systeraudio";
            }
            else if (ScramblingType1.equals("--single-cut") ||
                (ScramblingType1.equals("--double-cut")) )
            {
                ScrambleAudio = "--scramble-audio";
            }
        }
        else {
            ScrambleAudio = "";
        }
    }//GEN-LAST:event_chkScrambleAudioActionPerformed

    private void chkShowCardSerialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowCardSerialActionPerformed
        if (chkShowCardSerial.isSelected()) {
            ShowCardSerial = "--showserial";
        }
        else {
            ShowCardSerial = "";
        }
    }//GEN-LAST:event_chkShowCardSerialActionPerformed

    private void txtCardNumberKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCardNumberKeyTyped
        if(txtCardNumber.getText().length()>=13) {
            evt.consume();
        }
    }//GEN-LAST:event_txtCardNumberKeyTyped

    private void chkDeactivateCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDeactivateCardActionPerformed
        if (chkDeactivateCard.isSelected()) {
            chkActivateCard.setSelected(false);
            txtCardNumber.setEnabled(true);
            lblEMMCardNumber.setEnabled(true);
            EMMParam = "--disableemm";
        }
        else {
            EMMParam = "";
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
            EMMParam = "--enableemm";
        }
        else {
            EMMParam = "";
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
                if ( (ScramblingType1 == "--syster") && (cmbScramblingType.getSelectedIndex() == 8) ) {
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
            btnTeefax.setEnabled(false);
            btnRun.setEnabled(false);
            DownloadCancelled = false;
            // Set variables
            DownloadURL = "https://github.com/spark-teletext/spark-teletext/";
            HTMLString = ".tti\">(.*?)</a>";
            HTMLTempFile = "spark.html";
            // Download index page
            downloadTeletext(DownloadURL, HTMLTempFile);
        }
    }//GEN-LAST:event_btnSparkActionPerformed

    private void btnTeefaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeefaxActionPerformed
        if ((btnTeefax.getText()).contains("Stop")) {
            DownloadCancelled = true;
        }
        else {
            btnTeefax.setText("Stop");
            btnSpark.setEnabled(false);
            btnRun.setEnabled(false);
            DownloadCancelled = false;
            // Set variables
            DownloadURL = "http://teastop.plus.com/svn/teletext/";
            HTMLString = "\">(.*?)</a>";
            HTMLTempFile = "teefax.html";
            // Download index page
            downloadTeletext(DownloadURL, HTMLTempFile);
        }
    }//GEN-LAST:event_btnTeefaxActionPerformed

    private void btnTeletextBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeletextBrowseActionPerformed
        int result = teletextFileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File f = teletextFileChooser.getSelectedFile();
            txtTeletextSource.setText(Shared.stripQuotes(f.getAbsolutePath()));
        }
    }//GEN-LAST:event_btnTeletextBrowseActionPerformed

    private void chkTeletextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTeletextActionPerformed
        if (chkTeletext.isSelected()) {
            btnTeletextBrowse.setEnabled(true);
            txtTeletextSource.setEnabled(true);
            TeletextParam = "--teletext";
            downloadPanel.setEnabled(true);
            btnTeefax.setEnabled(true);
            btnSpark.setEnabled(true);
            lblDownload.setEnabled(true);
            lblTeefax.setEnabled(true);
            lblSpark.setEnabled(true);
        }
        else {
            btnTeletextBrowse.setEnabled(false);
            txtTeletextSource.setText("");
            txtTeletextSource.setEnabled(false);
            TeletextParam = "";
            TeletextSource = "";
            downloadPanel.setEnabled(false);
            btnTeefax.setEnabled(false);
            btnSpark.setEnabled(false);
            lblDownload.setEnabled(false);
            lblTeefax.setEnabled(false);
            lblSpark.setEnabled(false);
        }
    }//GEN-LAST:event_chkTeletextActionPerformed

    private void chkVerboseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkVerboseActionPerformed
        if (chkVerbose.isSelected()) {
            VerboseParam = "--verbose";
        }
        else {
            VerboseParam = "";
        }
    }//GEN-LAST:event_chkVerboseActionPerformed

    private void chkOutputLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkOutputLevelActionPerformed
        if (chkOutputLevel.isSelected()) {
            txtOutputLevel.setEnabled(true);
            OutputLevelParam = "--level";
        }
        else {
            txtOutputLevel.setText("");
            txtOutputLevel.setEnabled(false);
            OutputLevelParam = "";
        }
    }//GEN-LAST:event_chkOutputLevelActionPerformed

    private void chkGammaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGammaActionPerformed
        if (chkGamma.isSelected()) {
            txtGamma.setEnabled(true);
            GammaParam = "--gamma";
        }
        else {
            txtGamma.setText("");
            txtGamma.setEnabled(false);
            GammaParam = "";
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

    private void chkACPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkACPActionPerformed
        if (chkACP.isSelected()) {
            ACPParam = "--acp";
        }
        else {
            ACPParam = "";
        }
    }//GEN-LAST:event_chkACPActionPerformed

    private void chkVITSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkVITSActionPerformed
        if (chkVITS.isSelected()) {
            VITS = "--vits";
        }
        else {
            VITS = "";
        }
    }//GEN-LAST:event_chkVITSActionPerformed

    private void chkVideoFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkVideoFilterActionPerformed
        if (chkVideoFilter.isSelected()) {
            FilterParam = "--filter";
        }
        else {
            FilterParam = "";
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
        if(txtMacChId.getText().length()>=4) {
            evt.consume();
        }
    }//GEN-LAST:event_txtMacChIdKeyTyped

    private void txtMacChIdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMacChIdKeyReleased
        txtMacChId.setText(txtMacChId.getText().toUpperCase());
    }//GEN-LAST:event_txtMacChIdKeyReleased

    private void txtMacChIdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMacChIdKeyPressed
        txtMacChId.setText(txtMacChId.getText().toUpperCase());
    }//GEN-LAST:event_txtMacChIdKeyPressed

    private void chkMacChIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMacChIdActionPerformed
        if (chkMacChId.isSelected()) {
            txtMacChId.setEnabled(true);
            ChIDParam = "--chid";
        }
        else {
            txtMacChId.setText("");
            txtMacChId.setEnabled(false);
            ChIDParam = "";
            ChID = "";
        }
    }//GEN-LAST:event_chkMacChIdActionPerformed

    private void chkNICAMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNICAMActionPerformed
        if (chkNICAM.isSelected()) {
            NICAMParam = "";
            if (chkA2Stereo.isSelected()) chkA2Stereo.doClick();
        }
        else {
            if (NICAMSupported = true) {
                if (!chkA2Stereo.isSelected()) NICAMParam = "--nonicam";
            }
        }
    }//GEN-LAST:event_chkNICAMActionPerformed

    private void chkAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAudioActionPerformed
        if (chkAudio.isSelected()) {
            if (NICAMSupported) {
                chkNICAM.setEnabled(true);
                chkNICAM.doClick();
            }
            if (A2Supported) chkA2Stereo.setEnabled(true);
            AudioParam = "";
        }
        else {
            if (chkNICAM.isSelected()) chkNICAM.doClick();
            chkNICAM.setEnabled(false);
            if (chkA2Stereo.isSelected()) chkA2Stereo.doClick();
            chkA2Stereo.setEnabled(false);
            AudioParam = "--noaudio";
            NICAMParam = "";
        }
    }//GEN-LAST:event_chkAudioActionPerformed

    private void radMACActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMACActionPerformed
        cmbVideoFormat.removeAllItems();
        cmbVideoFormat.setModel( new DefaultComboBoxModel<>( addVideoModes("mac", 0) ) );
        cmbVideoFormat.setSelectedIndex(0);
    }//GEN-LAST:event_radMACActionPerformed

    private void radBWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radBWActionPerformed
        cmbVideoFormat.removeAllItems();
        cmbVideoFormat.setModel( new DefaultComboBoxModel<>( addVideoModes("other", 0) ) );
        cmbVideoFormat.setSelectedIndex(0);        
    }//GEN-LAST:event_radBWActionPerformed

    private void radSECAMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radSECAMActionPerformed
        cmbVideoFormat.removeAllItems();
        cmbVideoFormat.setModel( new DefaultComboBoxModel<>( addVideoModes("secam", 0) ) );
        cmbVideoFormat.setSelectedIndex(0);        
    }//GEN-LAST:event_radSECAMActionPerformed

    private void radNTSCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radNTSCActionPerformed
        cmbVideoFormat.removeAllItems();
        cmbVideoFormat.setModel( new DefaultComboBoxModel<>( addVideoModes("ntsc", 0) ) );
        cmbVideoFormat.setSelectedIndex(0);
    }//GEN-LAST:event_radNTSCActionPerformed

    private void radPALActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radPALActionPerformed
        cmbVideoFormat.removeAllItems();
        cmbVideoFormat.setModel( new DefaultComboBoxModel<>( addVideoModes("pal", 0) ) );
        cmbVideoFormat.setSelectedIndex(0);
        // End populate
    }//GEN-LAST:event_radPALActionPerformed

    private void cmbVideoFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbVideoFormatActionPerformed
        if (cmbVideoFormat.getSelectedIndex() != -1) {
            checkVideoFormat();
            /* Save the currently selected item into PreviousIndex.
               We can use this to revert the change if an unsupported mode is
               selected later.
            */
            PreviousIndex = cmbVideoFormat.getSelectedIndex();
            // Set sample rate
            txtSampleRate.setText(DefaultSampleRate);
            // If test card is selected, see if the selected mode is 625 or not
            if (radTest.isSelected()) checkTestCardStatus();
        }
    }//GEN-LAST:event_cmbVideoFormatActionPerformed

    private void chkARCorrectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkARCorrectionActionPerformed
        if (chkARCorrection.isSelected()) {
            cmbARCorrection.setEnabled(true);
        }
        else {
            cmbARCorrection.setEnabled(false);
            cmbARCorrection.setSelectedIndex(0);
        }
    }//GEN-LAST:event_chkARCorrectionActionPerformed

    private void cmbLogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbLogoActionPerformed
        if ( cmbLogo.getSelectedIndex() != -1) {
            LogoFileName = LogoArray[cmbLogo.getSelectedIndex()];
        }
        else {
            LogoFileName = "";
        }
    }//GEN-LAST:event_cmbLogoActionPerformed

    private void chkSubtitlesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSubtitlesActionPerformed
        if (chkSubtitles.isSelected()) {
            lblSubtitleIndex.setEnabled(true);
            SubtitlesParam = "--subtitles";
            txtSubtitleIndex.setEnabled(true);
        }
        else {
            SubtitlesParam = "";
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
            LogoParam = "";
            cmbLogo.setSelectedIndex(-1);
            cmbLogo.setEnabled(false);
        }
    }//GEN-LAST:event_chkLogoActionPerformed

    private void chkPositionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPositionActionPerformed
        if (chkPosition.isSelected()) {
            PositionParam = "--position";
            txtPosition.setEnabled(true);
        }
        else {
            PositionParam = "";
            txtPosition.setText("");
            txtPosition.setEnabled(false);
        }
    }//GEN-LAST:event_chkPositionActionPerformed

    private void chkInterlaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkInterlaceActionPerformed
        if (chkInterlace.isSelected()) {
            InterlaceParam = "--interlace";
        }
        else {
            InterlaceParam = "";
        }
    }//GEN-LAST:event_chkInterlaceActionPerformed

    private void chkTimestampActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTimestampActionPerformed
        if (chkTimestamp.isSelected()) {
            TimestampParam = "--timestamp";
        }
        else {
            TimestampParam = "";
        }
    }//GEN-LAST:event_chkTimestampActionPerformed

    private void chkRepeatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkRepeatActionPerformed
        if (chkRepeat.isSelected()) {
            RepeatParam = "--repeat";
        }
        else {
            RepeatParam = "";
        }
    }//GEN-LAST:event_chkRepeatActionPerformed

    private void btnSourceBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSourceBrowseActionPerformed
        int returnVal = sourceFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] f = sourceFileChooser.getSelectedFiles();
            if (f.length > 1) {
                for (int i = 0; i < f.length; i++) {
                    if ( (!f[i].toString().toLowerCase().endsWith(".m3u")) &&
                           (!f[i].toString().toLowerCase().endsWith(".htv")) ) {
                        PlaylistAL.add(f[i].toString());
                    }
                }
                populatePlaylist();
            }
            else {
                File file = new File (Shared.stripQuotes(f[0].toString()));
                if(file.getAbsolutePath().toLowerCase().endsWith(".m3u")) {
                    // If the source is an M3U file, pass it to the M3U handler
                    txtSource.setText(file.getAbsolutePath());
                    m3uHandler(file.getAbsolutePath(),0);
                } else if (file.getAbsolutePath().toLowerCase().endsWith(".htv")) {
                    // Don't try to process a file with a .HTV extension
                    JOptionPane.showMessageDialog(null, "Configuration files should be opened from the File menu.", AppName, JOptionPane.WARNING_MESSAGE);    
                } else {
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
        if ((Fork == "CJ") && (Lines == 625) && (cmbTest.getItemCount() > 1)) {
            cmbTest.setEnabled(true);
            cmbTest.setSelectedIndex(0);
        }
    }//GEN-LAST:event_radTestActionPerformed

    private void radLocalSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radLocalSourceActionPerformed
        // Enable all options in the frame
        chkRepeat.setEnabled(true);
        chkInterlace.setEnabled(true);
        chkSubtitles.setEnabled(true);
        txtSource.setEnabled(true);
        btnSourceBrowse.setEnabled(true);
        if (Fork == "CJ") {
            chkPosition.setEnabled(true);
            chkTimestamp.setEnabled(true);
            chkARCorrection.setEnabled(true);
            chkVolume.setEnabled(true);
            chkDownmix.setEnabled(true);
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
            RFampParam = "--amp";
        }
        else {
            RFampParam = "";
        }
    }//GEN-LAST:event_chkAmpActionPerformed

    private void cmbChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbChannelActionPerformed
        if ( cmbChannel.getSelectedIndex() != -1) {
            setFrequency();
            // Retrieve MAC channel ID
            if (radMAC.isSelected()) {
                String b = "";
                if (radUHF.isSelected()) {
                    b = "uhf";
                }
                else if (radUHF.isSelected()) {
                    b = "vhf";
                }
                // Retrieve bandplan
                String bp = INIFile.getStringFromINI(ModesFile, Mode, b, "", false);
                // Retrieve channel ID list
                String c = INIFile.getStringFromINI(ModesFile, bp, "chid", "", false);
                // Retrieve ID using the channel name from the ID list
                // This name must be identical to the name specified in the bandplan
                String id = INIFile.getStringFromINI(ModesFile, c, ChannelArray[cmbChannel.getSelectedIndex()], "", false).toUpperCase();
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
        populateBandPlan("uhf");
    }//GEN-LAST:event_radUHFActionPerformed

    private void radVHFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radVHFActionPerformed
        populateBandPlan("vhf");
    }//GEN-LAST:event_radVHFActionPerformed

    private void radCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radCustomActionPerformed
        txtFrequency.setEditable(true);
        cmbChannel.setEnabled(false);
        cmbChannel.setSelectedIndex(-1);
        lblRegion.setText(" ");
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
                + "All current settings will be cleared. Do you wish to continue?", AppName, JOptionPane.YES_NO_OPTION);
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
                cmbVideoFormat.setSelectedIndex(a);
            }
            else {
                JOptionPane.showConfirmDialog(null, "Unable to find the DMAC-FM mode, which is required for this template.", AppName, JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showConfirmDialog(null, "Unable to find the Galaxy channel, which is required for this template.", AppName, JOptionPane.ERROR_MESSAGE);
                resetAllControls();
                return;
            }
            
            JOptionPane.showMessageDialog(null, "Template values have been loaded. Tune your receiver to the Galaxy "
                    + "channel, or change this in the channel dropdown box on the Output tab.", AppName, JOptionPane.INFORMATION_MESSAGE);            
        }
    }//GEN-LAST:event_menuBSBTemplateActionPerformed

    private void btnHackTVPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHackTVPathActionPerformed
        hacktvFileChooser.setAcceptAllFileFilterUsed(true);        
        int returnVal = hacktvFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = hacktvFileChooser.getSelectedFile();
            HackTVPath = Shared.stripQuotes(file.toString());
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
            if (Fork.equals("CJ")) {
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
            SelectedFile = new File(Shared.stripQuotes(SelectedFile.toString()));
            checkSelectedFile(SelectedFile);
        }
    }//GEN-LAST:event_menuOpenActionPerformed

    private void chkColourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkColourActionPerformed
        if (chkColour.isSelected()) {
            ColourParam = "--nocolour";
        }
        else {
            ColourParam = "";
        }
    }//GEN-LAST:event_chkColourActionPerformed

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
                + "files from the File menu. Do you wish to continue?", AppName, JOptionPane.YES_NO_OPTION);
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
                + "saved settings and exit. Do you wish to continue?", AppName, JOptionPane.YES_NO_OPTION);
        if (q == JOptionPane.YES_OPTION) {
            resetPreferences(0);
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

    private void lblSyntaxOptionDisabledMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSyntaxOptionDisabledMouseClicked
        // Show a message to explain why the syntax option is disabled
        int q = JOptionPane.showConfirmDialog(null, "A helper application (windows-kill.exe) is required when running this application on Windows.\n"
                + "It is available from from https://github.com/ElyDotDev/windows-kill/releases/\n"
                + "Would you like to download it now?", AppName, JOptionPane.YES_NO_OPTION);
        if (q == JOptionPane.YES_OPTION) downloadWindowsKill();
    }//GEN-LAST:event_lblSyntaxOptionDisabledMouseClicked

    private void cmbSysterPermTableItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbSysterPermTableItemStateChanged
        if (cmbSysterPermTable.getSelectedIndex() == 1) {
            SysterPermTable = "--key-table-1";
        }
        else if (cmbSysterPermTable.getSelectedIndex() == 2) {
            SysterPermTable = "--key-table-2";
        }
        else {
            SysterPermTable = "";
        }
    }//GEN-LAST:event_cmbSysterPermTableItemStateChanged

    private void cmbOutputDeviceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbOutputDeviceActionPerformed
        String VideoFormatChanged = "The selected video format has been changed because this output device does not support it. Please select another format.";
        if (!txtOutputDevice.getText().isBlank()) txtOutputDevice.setText("");
        switch(cmbOutputDevice.getSelectedIndex()) {
            // hackrf
            case 0:
                lblOutputDevice2.setText("Serial number (optional)");
                OutputDevice = ""; // can also use hackrf
                // If the RF panel is disabled, enable it and call checkVideoFormat
                // to re-populate the channel options correctly
                if (!radCustom.isEnabled()) {
                    // If a baseband mode is selected, reset it to something else
                    if (Baseband) {
                        JOptionPane.showMessageDialog(null, VideoFormatChanged, AppName, JOptionPane.INFORMATION_MESSAGE);   
                        cmbVideoFormat.setSelectedIndex(0);
                    }
                    enableRFOptions();
                    checkVideoFormat();
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
                    if (Baseband) {
                        JOptionPane.showMessageDialog(null, VideoFormatChanged, AppName, JOptionPane.INFORMATION_MESSAGE);   
                        cmbVideoFormat.setSelectedIndex(0);
                    }
                    enableRFOptions();
                    checkVideoFormat();
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
                    int result = outputFileChooser.showSaveDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File o = outputFileChooser.getSelectedFile();
                        txtOutputDevice.setText(o.toString());
                    }
                }
                break;
            default:
                System.out.println("Output device error");
                break;
        }
    }//GEN-LAST:event_cmbOutputDeviceActionPerformed

    private void chkVolumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkVolumeActionPerformed
        if (chkVolume.isSelected()) {
            txtVolume.setEnabled(true);
            VolumeParam = "--volume";
        }
        else {
            txtVolume.setEnabled(false);
            txtVolume.setText("");
            VolumeParam = "";
        }
    }//GEN-LAST:event_chkVolumeActionPerformed

    private void chkDownmixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDownmixActionPerformed
        if (chkDownmix.isSelected()) {
            DownmixParam = "--downmix";
        }
        else {
            DownmixParam = "";
        }
    }//GEN-LAST:event_chkDownmixActionPerformed

    private void chkTextSubtitlesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTextSubtitlesActionPerformed
        if (chkTextSubtitles.isSelected()) {
            TeletextSubtitlesParam = "--tx-subtitles";
            lblTextSubtitleIndex.setEnabled(true);
            txtTextSubtitleIndex.setEnabled(true); 
        }
        else {
            TeletextSubtitlesParam = "";
            txtTextSubtitleIndex.setEnabled(false);
            txtTextSubtitleIndex.setText("");
            lblTextSubtitleIndex.setEnabled(false);
        }
    }//GEN-LAST:event_chkTextSubtitlesActionPerformed

    private void chkA2StereoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkA2StereoActionPerformed
        if (chkA2Stereo.isSelected()) {
            A2StereoParam = "--a2stereo";
            if (chkNICAM.isSelected()) {
                chkNICAM.doClick();
                NICAMParam = "";
            }
            else {
                NICAMParam = "";
            }
        }
        else {
            A2StereoParam = "";
            if ( (chkNICAM.isEnabled()) && (!chkNICAM.isSelected()) ) {
                NICAMParam = "--nonicam";
            }
        }
    }//GEN-LAST:event_chkA2StereoActionPerformed

    private void chkLocalModesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLocalModesActionPerformed
        if (chkLocalModes.isSelected()) {
            Prefs.put("UseLocalModesFile", "1");
        }
        else {
            Prefs.put("UseLocalModesFile", "0");
        }
    }//GEN-LAST:event_chkLocalModesActionPerformed

    private void chkFMDevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkFMDevActionPerformed
        if (chkFMDev.isSelected()) {
            txtFMDev.setEnabled(true);
            FMDevParam = "--deviation";
        }
        else {
            txtFMDev.setText("");
            txtFMDev.setEnabled(false);
            FMDevParam = "";
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
        else if ( (txtSource.getText().contains("://youtube.com/")) ||
                  (txtSource.getText().contains("://www.youtube.com/")) ||
                  (txtSource.getText().contains("://youtu.be/")) ||
                  (txtSource.getText().startsWith("ytdl:")) ) {
            JOptionPane.showMessageDialog(null, "Cannot add this URL to the playlist. "
                        + "The youtube-dl handler is only supported for single files at present.", AppName, JOptionPane.WARNING_MESSAGE);
            return;
        }
        else if ( (txtSource.isEnabled()) && (!txtSource.getText().isBlank()) ) {
            // Add whatever is in txtSource to PlaylistAL
            PlaylistAL.add(txtSource.getText());
        }
        else if (radTest.isSelected()) {
            for (int i = 0; i < PlaylistAL.size(); i++) {
                if (PlaylistAL.get(i).startsWith("test:")) {
                    JOptionPane.showMessageDialog(null, "Only one test card can be added to the playlist.\n"
                        + "It should also be placed as the last item in the playlist.", AppName, JOptionPane.WARNING_MESSAGE);
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

    private void cmbVideoFormatMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbVideoFormatMouseWheelMoved
        mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbVideoFormat);
    }//GEN-LAST:event_cmbVideoFormatMouseWheelMoved

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
            JOptionPane.showMessageDialog(null, "Test cards cannot be set as the start point of a playlist.", AppName, JOptionPane.WARNING_MESSAGE);
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
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AdditionalOptionsPanel;
    private javax.swing.ButtonGroup BandButtonGroup;
    private javax.swing.JPanel FrequencyPanel;
    private javax.swing.JPanel PlaybackTab;
    private javax.swing.ButtonGroup SourceButtonGroup;
    private javax.swing.JPanel SourcePanel;
    private javax.swing.JPanel VBIPanel;
    private javax.swing.ButtonGroup VideoFormatButtonGroup;
    private javax.swing.JPanel VideoFormatPanel;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClearMRUList;
    private javax.swing.JButton btnHackTVPath;
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
    private javax.swing.JCheckBox chkFMDev;
    private javax.swing.JCheckBox chkFindKeys;
    private javax.swing.JCheckBox chkGamma;
    private javax.swing.JCheckBox chkInterlace;
    private javax.swing.JCheckBox chkLocalModes;
    private javax.swing.JCheckBox chkLogo;
    private javax.swing.JCheckBox chkMacChId;
    private javax.swing.JCheckBox chkNICAM;
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
    private javax.swing.JCheckBox chkVITS;
    private javax.swing.JCheckBox chkVerbose;
    private javax.swing.JCheckBox chkVideoFilter;
    private javax.swing.JCheckBox chkVolume;
    private javax.swing.JCheckBox chkWSS;
    private javax.swing.JComboBox<String> cmbARCorrection;
    private javax.swing.JComboBox<String> cmbChannel;
    private javax.swing.JComboBox<String> cmbFileType;
    private javax.swing.JComboBox<String> cmbLogo;
    private javax.swing.JComboBox<String> cmbM3USource;
    private javax.swing.JComboBox<String> cmbOutputDevice;
    private javax.swing.JComboBox<String> cmbScramblingKey1;
    private javax.swing.JComboBox<String> cmbScramblingKey2;
    private javax.swing.JComboBox<String> cmbScramblingType;
    private javax.swing.JComboBox<String> cmbSysterPermTable;
    private javax.swing.JComboBox<String> cmbTest;
    private javax.swing.JComboBox<String> cmbVideoFormat;
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
    private javax.swing.JLabel lblEMMCardNumber;
    private javax.swing.JLabel lblFileType;
    private javax.swing.JLabel lblFork;
    private javax.swing.JLabel lblFrequency;
    private javax.swing.JLabel lblGain;
    private javax.swing.JLabel lblOutputDevice;
    private javax.swing.JLabel lblOutputDevice2;
    private javax.swing.JLabel lblRegion;
    private javax.swing.JLabel lblSampleRate;
    private javax.swing.JLabel lblScramblingKey;
    private javax.swing.JLabel lblScramblingSystem;
    private javax.swing.JLabel lblSpark;
    private javax.swing.JLabel lblSpecifyLocation;
    private javax.swing.JLabel lblSubtitleIndex;
    private javax.swing.JLabel lblSyntaxOptionDisabled;
    private javax.swing.JLabel lblSysterPermTable;
    private javax.swing.JLabel lblTeefax;
    private javax.swing.JLabel lblTextSubtitleIndex;
    private javax.swing.JLabel lblVC2ScramblingKey;
    private javax.swing.JList<String> lstPlaylist;
    private javax.swing.JMenuItem menuAbout;
    private javax.swing.JMenuItem menuAstra10Template;
    private javax.swing.JMenuItem menuAstra975Template;
    private javax.swing.JMenuItem menuBSBTemplate;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenuItem menuMRUFile1;
    private javax.swing.JMenuItem menuMRUFile2;
    private javax.swing.JMenuItem menuMRUFile3;
    private javax.swing.JMenuItem menuMRUFile4;
    private javax.swing.JMenuItem menuNew;
    private javax.swing.JMenuItem menuOpen;
    private javax.swing.JMenuItem menuSave;
    private javax.swing.JMenuItem menuSaveAs;
    private javax.swing.JFileChooser outputFileChooser;
    private javax.swing.JPanel outputTab;
    private javax.swing.JPanel pathPanel;
    private javax.swing.JProgressBar pbTeletext;
    private javax.swing.JScrollPane playlistScrollPane;
    private javax.swing.JRadioButton radBW;
    private javax.swing.JRadioButton radCustom;
    private javax.swing.JRadioButton radLocalSource;
    private javax.swing.JRadioButton radMAC;
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
    private javax.swing.JTextField txtAllOptions;
    private javax.swing.JTextField txtAntennaName;
    private javax.swing.JTextField txtCardNumber;
    private javax.swing.JTextArea txtConsoleOutput;
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
    private javax.swing.JTextField txtSubtitleIndex;
    private javax.swing.JTextField txtTeletextSource;
    private javax.swing.JTextField txtTextSubtitleIndex;
    private javax.swing.JTextField txtVolume;
    // End of variables declaration//GEN-END:variables
    // Checkbox array for the File > New option
    private javax.swing.JCheckBox[] CheckBoxes;
}
