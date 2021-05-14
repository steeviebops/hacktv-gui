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
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.net.URL;
import java.net.URLConnection;
import java.net.URISyntaxException;
import javax.swing.JCheckBox;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.FileInputStream;;
import javax.swing.filechooser.FileFilter;
import java.awt.Cursor;
import java.util.prefs.Preferences;
import java.security.CodeSource;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import javax.swing.SwingWorker;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.UnsupportedLookAndFeelException;

public class GUI extends javax.swing.JFrame {    
    // Application name
    String AppName = "hacktv-gui";
    
    // Boolean used for Microsoft Windows detection and handling
    Boolean RunningOnWindows;
    
    // Run button text (used for the generate syntax option)
    String RunButtonText;
    
    // Get user's home directory, used for file open dialogues
    String UserHomeDir = System.getProperty("user.home");
    
    // String to set the directory where this application's JAR is located
    String JarDir;
    
    // Declare a variable for NICAM stereo status
    Boolean NICAMSupported;

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
    Boolean DownloadInProgress = false;
    Boolean DownloadCancelled = false;

    // Declare variables used for path resolution
    String HackTVPath;
    String HackTVDirectory;
    String DefaultHackTVPath;
    String OS_SEP;

    // Declare variable for the title bar display
    String TitleBar;
    Boolean TitleBarChanged = false;

    // Array used for M3U files
    ArrayList<String> PlaylistURLsAL;
    String[] PlaylistNames;

    /* Declare a variable for storing the default sample rate for the selected video mode
     * This allows us to revert back to the default if the sample rate is changed by filters or scrambling systems
     * FMSampleRate specifies the recommended sample rate for the pre-emphasis filter
     */
    String DefaultSampleRate;
    String FMSampleRate;
    
    // Declare combobox arrays and ArrayLists
    // These are used to store secondary information (frequencies, parameters, etc)
    int[] FrequencyArray;
    String[] VideoModeArray;
    String[] ChannelArray;
    String[] WSSModeArray;
    String[] ARCorrectionModeArray;
    ArrayList<String> ScramblingTypeArray;
    ArrayList<String> ScramblingKeyArray;
    ArrayList<String> ScramblingKey2Array;
    String[] LogoArray;
    String[] TestCardArray;
    
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
    
    // Declare variables used for storing parameters
    ArrayList<String> AllArgs = new ArrayList<>();
    String InputSource = "";
    String Sys = "";
    long Frequency;
    int SampleRate;
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
                // Comment out the next line if you want to use GTK+ on
                // supported desktop environments but it doesn't look great
                // in my opinion.
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
        // Prevent window from being resized below the current size
        this.setMinimumSize(this.getSize());
        // Set the JarDir variable so we know where we're located
        try {
            // Get the current directory path
            CodeSource codeSource = GUI.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            JarDir = jarFile.getParentFile().getPath();         
        } catch (URISyntaxException ex) {
            System.out.println(ex);
        }
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
                    JOptionPane.showMessageDialog(null, "A helper application (windows-kill.exe) is required when running this application on Windows.\n"
                + "You can download it from https://github.com/alirdn/windows-kill/releases/\n"
                + "Please save it in the same directory as this application and restart.\n"          
                            + "This message will only be shown once.", AppName, JOptionPane.WARNING_MESSAGE);
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
        addWSSModes();
        addARCorrectionOptions();
        addTestCardOptions();
        addLogoOptions();
        addOutputDevices();
        loadPreferences();
        detectFork();
        // Set default values when form loads
        radLocalSource.doClick();
        radPAL.doClick();
        chkAudio.setSelected(true);
        chkNICAM.setSelected(true);
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
        chkMacChId = new javax.swing.JCheckBox();
        txtMacChId = new javax.swing.JTextField();
        chkFMDev = new javax.swing.JCheckBox();
        txtFMDev = new javax.swing.JTextField();
        chkVideoFilter = new javax.swing.JCheckBox();
        chkColour = new javax.swing.JCheckBox();
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
        txtVolume = new javax.swing.JTextField();
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
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/com/steeviebops/resources/test.gif")).getImage());

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

        cmbARCorrection.setEnabled(false);

        cmbM3USource.setEnabled(false);

        cmbTest.setEnabled(false);

        javax.swing.GroupLayout SourcePanelLayout = new javax.swing.GroupLayout(SourcePanel);
        SourcePanel.setLayout(SourcePanelLayout);
        SourcePanelLayout.setHorizontalGroup(
            SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, SourcePanelLayout.createSequentialGroup()
                        .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkRepeat)
                            .addComponent(chkTimestamp)
                            .addComponent(chkInterlace)
                            .addComponent(chkARCorrection))
                        .addGap(23, 23, 23)
                        .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SourcePanelLayout.createSequentialGroup()
                                .addComponent(chkSubtitles)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
                                .addComponent(lblSubtitleIndex)
                                .addGap(18, 18, 18)
                                .addComponent(txtSubtitleIndex, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(SourcePanelLayout.createSequentialGroup()
                                .addComponent(chkLogo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                                .addComponent(cmbLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(SourcePanelLayout.createSequentialGroup()
                                .addComponent(cmbARCorrection, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(SourcePanelLayout.createSequentialGroup()
                                .addComponent(chkPosition)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(SourcePanelLayout.createSequentialGroup()
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
                        .addComponent(btnSourceBrowse)))
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
                    .addComponent(txtSource, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbM3USource, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 9, Short.MAX_VALUE)
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkRepeat)
                    .addComponent(chkSubtitles)
                    .addComponent(txtSubtitleIndex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSubtitleIndex))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkInterlace)
                    .addComponent(chkPosition)
                    .addComponent(txtPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkTimestamp)
                    .addComponent(chkLogo)
                    .addComponent(cmbLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbARCorrection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkARCorrection))
                .addContainerGap())
        );

        VideoFormatPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Video format options"));

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

        chkMacChId.setText("Override MAC channel ID");
        chkMacChId.setEnabled(false);
        chkMacChId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMacChIdActionPerformed(evt);
            }
        });

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

        chkFMDev.setText("FM deviation (MHz)");
        chkFMDev.setEnabled(false);
        chkFMDev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkFMDevActionPerformed(evt);
            }
        });

        txtFMDev.setEnabled(false);

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
                                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chkAudio)
                                    .addComponent(chkNICAM)
                                    .addComponent(chkColour))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 156, Short.MAX_VALUE)
                                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chkFMDev)
                                    .addComponent(chkMacChId)
                                    .addComponent(lblSampleRate))
                                .addGap(44, 44, 44))
                            .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                                .addComponent(chkVideoFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                                .addComponent(radPAL)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addComponent(radNTSC)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addComponent(radSECAM)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addComponent(radBW)
                                .addGap(18, 18, Short.MAX_VALUE)))
                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtFMDev, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtMacChId, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtSampleRate, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(radMAC, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        VideoFormatPanelLayout.setVerticalGroup(
            VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(VideoFormatPanelLayout.createSequentialGroup()
                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtFMDev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkFMDev))
                        .addGap(2, 2, 2)
                        .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkColour)
                            .addGroup(VideoFormatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtMacChId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(chkMacChId))))
                    .addComponent(chkNICAM))
                .addGap(2, 2, 2)
                .addComponent(chkVideoFilter)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout sourceTabLayout = new javax.swing.GroupLayout(sourceTab);
        sourceTab.setLayout(sourceTabLayout);
        sourceTabLayout.setHorizontalGroup(
            sourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(VideoFormatPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        sourceTabLayout.setVerticalGroup(
            sourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SourcePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(VideoFormatPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabPane.addTab("Source and video mode", sourceTab);

        FrequencyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Frequency and TX options"));

        lblOutputDevice.setText("Output device");

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
                                .addGap(0, 0, Short.MAX_VALUE))))
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
                    .addComponent(txtOutputDevice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rfPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        chkVerbose.setText("Enable verbose output");
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

        txtVolume.setEnabled(false);

        javax.swing.GroupLayout AdditionalOptionsPanelLayout = new javax.swing.GroupLayout(AdditionalOptionsPanel);
        AdditionalOptionsPanel.setLayout(AdditionalOptionsPanelLayout);
        AdditionalOptionsPanelLayout.setHorizontalGroup(
            AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdditionalOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(chkGamma, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkOutputLevel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkVerbose, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtOutputLevel)
                    .addComponent(txtGamma, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE))
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AdditionalOptionsPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(chkVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(AdditionalOptionsPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(chkDownmix)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        AdditionalOptionsPanelLayout.setVerticalGroup(
            AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdditionalOptionsPanelLayout.createSequentialGroup()
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkGamma)
                    .addComponent(txtGamma, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkVolume)
                    .addComponent(txtVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(AdditionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkOutputLevel)
                    .addComponent(txtOutputLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkDownmix))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkVerbose))
        );

        javax.swing.GroupLayout outputTabLayout = new javax.swing.GroupLayout(outputTab);
        outputTab.setLayout(outputTabLayout);
        outputTabLayout.setHorizontalGroup(
            outputTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(outputTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(FrequencyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(VBIPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AdditionalOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        outputTabLayout.setVerticalGroup(
            outputTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(FrequencyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(VBIPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AdditionalOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        tabPane.addTab("Output", outputTab);

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
                    .addComponent(txtTeletextSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        cmbScramblingType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbScramblingTypeActionPerformed(evt);
            }
        });

        cmbScramblingKey1.setEnabled(false);
        cmbScramblingKey1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbScramblingKey1ActionPerformed(evt);
            }
        });

        cmbScramblingKey2.setEnabled(false);
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
                            .addComponent(cmbScramblingKey1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbScramblingKey2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbScramblingType, 0, 369, Short.MAX_VALUE))
                        .addGap(0, 22, Short.MAX_VALUE))))
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
                .addContainerGap(82, Short.MAX_VALUE))
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
                    .addGroup(pathPanelLayout.createSequentialGroup()
                        .addComponent(txtHackTVPath, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnHackTVPath))
                    .addGroup(pathPanelLayout.createSequentialGroup()
                        .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSpecifyLocation)
                            .addGroup(pathPanelLayout.createSequentialGroup()
                                .addComponent(lblDetectedBuikd)
                                .addGap(18, 18, 18)
                                .addComponent(lblFork)))
                        .addGap(0, 0, Short.MAX_VALUE)))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        javax.swing.GroupLayout generalSettingsPanelLayout = new javax.swing.GroupLayout(generalSettingsPanel);
        generalSettingsPanel.setLayout(generalSettingsPanelLayout);
        generalSettingsPanelLayout.setHorizontalGroup(
            generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkSyntaxOnly)
                .addGap(18, 18, 18)
                .addComponent(lblSyntaxOptionDisabled)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        generalSettingsPanelLayout.setVerticalGroup(
            generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkSyntaxOnly)
                    .addComponent(lblSyntaxOptionDisabled))
                .addContainerGap(70, Short.MAX_VALUE))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
            chkTextSubtitles
        };
    }
    
    private void loadPreferences(){
        // Check preferences node for the path to hacktv
        // If not found, use the default
        HackTVPath = Prefs.get("HackTVPath", DefaultHackTVPath);
        // Load the full path to a variable so we can use getParent on it and
        // get its parent directory path
        HackTVDirectory = new File(HackTVPath).getParent();
        txtHackTVPath.setText(HackTVPath);
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
            fsphil();
            return;
        } else if (Files.isDirectory(Path.of(HackTVPath))) {
            lblFork.setText("Invalid path");
            fsphil();
            return;            
        }
        /*  Check the size of the specified file.
         *  If larger than 30MB, call the fsphil method and don't go any further.
         *  This is for security reasons and to avoid memory leaks.
         */
        File f = new File(HackTVPath);
        if (f.length() > 31457280) { 
            lblFork.setText("Invalid file (too large)");
            fsphil();
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
                    captainJack();
                }
                else if (c.contains("Both VC1 and VC2 cannot be used together")) {
                    b = true;
                    lblFork.setText("fsphil");
                    fsphil();
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
            fsphil();
            return;
        }
        if (!b) {
            lblFork.setText("Invalid file (not hacktv?)");
            fsphil();
            return;            
        }        
    }

    private void fsphil() {
        // Disable features unsupported in fsphil's build
        Fork = "";
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
            addPALScramblingTypes();
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
        Fork = "CJ";
        chkLogo.setEnabled(true);
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
            addPALScramblingTypes();
        }
        else if ( radMAC.isSelected() ) {
            addMACScramblingTypes();
        }
        if (radTest.isSelected()){
            cmbTest.setEnabled(true);
            cmbTest.setSelectedIndex(0);
        }        
    }
    
    public static boolean isNumeric(String strNum) {
	if (strNum == null) {
	    return false;
	}
	try {
	    double d = Double.parseDouble(strNum);
	} catch (NumberFormatException nfe) {
	    return false;
	}
	return true;
    }
    
    public static int wildcardFind(String pathToScan, String startsWith, String endsWith) {
        // Returns the number of files found in a directory with the specified start and end strings
        // Case insensitive, feed it with lowercase filenames
        String fileToFilter;
        File folderToScan = new File(pathToScan);
        File[] listOfFiles = folderToScan.listFiles();
        int c = 0;
        // If the specified directory does not exist, return 0 and stop
        if (!Files.exists(folderToScan.toPath())) return 0;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                fileToFilter = listOfFile.getName();
                // If a file is found, increment c by one
                if (fileToFilter.toLowerCase().startsWith(startsWith)
                        && fileToFilter.toLowerCase().endsWith(endsWith)) {
                    c = c + 1;
                }
            }
        }
        return c;
    }    
    
    private void deleteFSObject(Path pathToBeDeleted) throws IOException {
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
    
    private String stripQuotes(String FilePath) {
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
            SelectedFile = new File (stripQuotes(configFileChooser.getSelectedFile().toString()));
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
        String FileContents;
        try {
            // Read the first line of the file using a BufferedReader
            BufferedReader br1 = new BufferedReader(new FileReader(SourceFile));
            LineNumberReader lnr1 = new LineNumberReader(br1);
            FileContents = lnr1.readLine();
            br1.close();
            // Check that the file is not empty
            if (FileContents == null) {
                JOptionPane.showMessageDialog(null, "Invalid configuration file.", AppName, JOptionPane.ERROR_MESSAGE);  
                return;
            }
            /* Check the file to see if it's in the correct format.
               We no longer support the legacy format used by 2.x and earlier.*/
            if ( (FileContents.contains("[hacktv-gui]")) || (FileContents.contains("hacktv-gui configuration file")) ) {
                JOptionPane.showMessageDialog(null, "This file was created in an older version of the application.\n"
                    + "This Java version no longer supports this format. Please use the Windows VB6 version "
                        + "(3.0 or later) to convert the file to the current format.", AppName, JOptionPane.INFORMATION_MESSAGE);
            } else if ( FileContents.contains("[hacktv]") ) {
                // This is OK, continue opening this file
                HTVLoadInProgress = true;
                if (openConfigFile(SourceFile.toString())) {
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
                // No idea what we've read here, so stop
                JOptionPane.showMessageDialog(null, "Invalid configuration file.", AppName, JOptionPane.ERROR_MESSAGE);                           
            }
        } catch (IOException ex) {      
            JOptionPane.showMessageDialog(null, "The specified file could not be found. "
                    + "It may have been moved or deleted.", AppName, JOptionPane.ERROR_MESSAGE);         
        }
    }
    
    private boolean openConfigFile(String SourceFile) {
        /**
         * HTV configuration file loader.
         * 
         * We read the file as a Windows INI format. The syntax for strings is as follows:
         * 
         * INIFile.getStringFromINI(SourceFile, "section", "setting", "Default value", Preserve-case?);
         * 
         * If the setting is not specified in the file, use the default value specified
         * If preserve-case is set to true, the value is returned as-is
         * If false, it is converted to lower case so we can manage it more easily
         * At present, we enable case-sensitivity for file and channel names only
         */
        // Check that the fork value matches the one we're using
        String ImportedFork = (INIFile.getStringFromINI(SourceFile, "hacktv-gui3", "fork", "", false));
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
        /* Output device (case sensitive)
           For this, we look for hackrf, soapysdr or fl2k. A null value will be
           interpreted as hackrf. Anything other than these values is handled
           as an output file.
         */
        String ImportedOutputDevice = (INIFile.getStringFromINI(SourceFile, "hacktv", "output", "hackrf", true));
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
        String ImportedSource = (INIFile.getStringFromINI(SourceFile, "hacktv", "input", "", true));
        String M3USource = (INIFile.getStringFromINI(SourceFile, "hacktv-gui3", "m3usource", "", true));
        Integer M3UIndex = (INIFile.getIntegerFromINI(SourceFile, "hacktv-gui3", "m3uindex"));
        if (ImportedSource.toLowerCase().startsWith("test:")) {
            radTest.doClick();
            if (Fork == "CJ") {
                ImportedSource = ImportedSource.toLowerCase().split(":")[1];
                if (ImportedSource.equals("pm5544")) {
                    cmbTest.setSelectedIndex(1);
                }
                else if (ImportedSource.equals("ueitm")) {
                    cmbTest.setSelectedIndex(2);
                }
                else if (ImportedSource.equals("fubk")) {
                    cmbTest.setSelectedIndex(3);
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
        else {
            txtSource.setText(ImportedSource);
        }
        // Video format
        String ImportedVideoFormat = (INIFile.getStringFromINI(SourceFile, "hacktv", "mode", "", false));
        switch (ImportedVideoFormat) {
            // PAL video formats
            case "i":
                radPAL.doClick();
                cmbVideoFormat.setSelectedIndex(0);
                break;
            case "b":
            case "g":
                radPAL.doClick();
                cmbVideoFormat.setSelectedIndex(1);
                break;
            case "pal-fm":
                radPAL.doClick();
                cmbVideoFormat.setSelectedIndex(2);
                break;
            case "pal-m":
                radPAL.doClick();
                cmbVideoFormat.setSelectedIndex(3);
                break;
            // NTSC video formats
            case "m":
                radNTSC.doClick();
                cmbVideoFormat.setSelectedIndex(0);
                break;
            case "ntsc-fm":
                radNTSC.doClick();
                cmbVideoFormat.setSelectedIndex(1);
                break;
            case "ntsc-bs":
                radNTSC.doClick();
                cmbVideoFormat.setSelectedIndex(2);
                break;
            case "apollo-fsc-fm":
                radNTSC.doClick();
                cmbVideoFormat.setSelectedIndex(3);
                break;
            case "m-cbs405":
                radNTSC.doClick();
                cmbVideoFormat.setSelectedIndex(4);
                break;
            // SECAM video formats
            case "l":
                radSECAM.doClick();
                cmbVideoFormat.setSelectedIndex(0);
                break;
            case "d":
            case "k":
                radSECAM.doClick();
                cmbVideoFormat.setSelectedIndex(1);
                break;
            case "secam-fm":
                radSECAM.doClick();
                cmbVideoFormat.setSelectedIndex(2);
                break;
            // Legacy black and white video formats
            case "a":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(0);
                break;
            case "e":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(1);
                break;
            case "240-am":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(2);
                break;
            case "30-am":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(3);
                break;
            case "apollo-fm":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(4);
                break;
            case "nbtv-am":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(5);
                break;
            // MAC video formats
            case "d2mac-am":
                radMAC.doClick();
                cmbVideoFormat.setSelectedIndex(0);
                break;
            case "d2mac-fm":
                radMAC.doClick();
                cmbVideoFormat.setSelectedIndex(1);
                break;
            case "dmac-am":
                radMAC.doClick();
                cmbVideoFormat.setSelectedIndex(2);
                break;
            case "dmac-fm":
                radMAC.doClick();
                cmbVideoFormat.setSelectedIndex(3);
                break;
            // Baseband video formats
            case "pal":
                radPAL.doClick();
                cmbVideoFormat.setSelectedIndex(4);
                break; 
            case "525pal":
                radPAL.doClick();
                cmbVideoFormat.setSelectedIndex(5);
                break;
            case "ntsc":
                radNTSC.doClick();
                cmbVideoFormat.setSelectedIndex(5);
                break; 
            case "cbs405":
                radNTSC.doClick();
                cmbVideoFormat.setSelectedIndex(6);
                break;
            case "secam":
                radSECAM.doClick();
                cmbVideoFormat.setSelectedIndex(3);
                break;
            case "405":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(6);
                break;
            case "819":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(7);
                break;
            case "240":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(8);
                break;
            case "30":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(9);
                break;
            case "apollo":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(10);
                break;
            case "nbtv":
                radBW.doClick();
                cmbVideoFormat.setSelectedIndex(11);
                break;
            case "d2mac":
                radMAC.doClick();
                cmbVideoFormat.setSelectedIndex(4);
                break;
            case "dmac":
                radMAC.doClick();
                cmbVideoFormat.setSelectedIndex(5);
                break;
            // Unknown
            default:
                invalidConfigFileValue("video format", ImportedVideoFormat);
                resetAllControls();
                return false;
        }
        // Frequency or channel number
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            // Return a value of -250 if the value is null so we can handle it
            String NoFrequencyOrChannel = "No frequency or valid channel number was found in the configuration file. Load aborted.";
            String ImportedChannel = (INIFile.getStringFromINI(SourceFile, "hacktv-gui3", "channel", "", true));
            Double ImportedFrequency;
            if (INIFile.getDoubleFromINI(SourceFile, "hacktv", "frequency") != null) {
                ImportedFrequency = (INIFile.getDoubleFromINI(SourceFile, "hacktv", "frequency"));
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
                Boolean ChannelFound = false;
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
        if (INIFile.getIntegerFromINI(SourceFile, "hacktv", "gain") != null) {
            txtGain.setText(INIFile.getIntegerFromINI(SourceFile, "hacktv", "gain").toString());
        }
        // If value is null and output device is hackrf or soapysdr, set gain to zero
        else if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            txtGain.setText("0");
        }
        // Amp
        if (cmbOutputDevice.getSelectedIndex() == 0) {
            if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "amp")) {
                chkAmp.doClick();
            }            
        }
        // FM deviation
        if ((chkFMDev.isEnabled()) && (INIFile.getDoubleFromINI(SourceFile, "hacktv", "deviation") != null)) {
            Double ImportedDeviation = (INIFile.getDoubleFromINI(SourceFile, "hacktv", "deviation") / 1000000);
            chkFMDev.doClick();
            txtFMDev.setText(ImportedDeviation.toString().replace(".0",""));
        }
        // Output level
        String ImportedLevel = (INIFile.getStringFromINI(SourceFile, "hacktv", "level", "", false));
        if (!ImportedLevel.isEmpty()) {
            chkOutputLevel.doClick();
            txtOutputLevel.setText(ImportedLevel);
        }
        // Gamma
        String ImportedGamma = (INIFile.getStringFromINI(SourceFile, "hacktv", "gamma", "", false));
        if (!ImportedGamma.isEmpty()) {
            chkGamma.doClick();
            txtGamma.setText(ImportedGamma);
        }
        // Repeat
        if (chkRepeat.isEnabled()) {
            if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "repeat")) {
                chkRepeat.doClick();
            }
        }
        // Position
        if (chkPosition.isEnabled()) {
            if (INIFile.getIntegerFromINI(SourceFile, "hacktv", "position") != null) {
                chkPosition.doClick();
                txtPosition.setText(INIFile.getIntegerFromINI(SourceFile, "hacktv", "position").toString());
            }
        }
        // Verbose mode
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "verbose")) {
            chkVerbose.doClick();
        }
        // Logo
        if (chkLogo.isEnabled()) {
            String ImportedLogo = (INIFile.getStringFromINI(SourceFile, "hacktv", "logo", "", true)).toLowerCase();
            // Check first if the imported string is a .png file.
            // hacktv now contains its own internal resources so external files
            // are no longer supported.
            if (ImportedLogo.endsWith(".png")) {
                JOptionPane.showMessageDialog(null, 
                     "hacktv no longer supports external logo files. Logo option disabled.", AppName, JOptionPane.WARNING_MESSAGE);
            }
            else if (!ImportedLogo.isBlank()) {
                for (int i = 0; i <= cmbLogo.getItemCount() - 1; i++) {
                    if ( (LogoArray[i].toLowerCase()).equals(ImportedLogo) ) {
                        chkLogo.doClick();
                        cmbLogo.setSelectedIndex(i);
                    }
                }
            }
        }
        // Timestamp
        if (chkTimestamp.isEnabled()) {
            if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "timestamp")) {
                chkTimestamp.doClick();
            }
        }
        // Interlace
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "interlace")) {
            chkTimestamp.doClick();
        }
        // Teletext
        String ImportedTeletext = (INIFile.getStringFromINI(SourceFile, "hacktv", "teletext", "", true));
        if (!ImportedTeletext.isEmpty()) {
            chkTeletext.doClick();
            if (ImportedTeletext.toLowerCase().startsWith("raw:")) {
                txtTeletextSource.setText(ImportedTeletext.substring(4));
            } else {
                txtTeletextSource.setText(ImportedTeletext);
            }
        }
        // WSS
        if ((INIFile.getIntegerFromINI(SourceFile, "hacktv", "wss")) != null) {
            Integer ImportedWSS = (INIFile.getIntegerFromINI(SourceFile, "hacktv", "wss"));
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
            if ((INIFile.getIntegerFromINI(SourceFile, "hacktv", "arcorrection")) != null) {
                Integer ImportedAR = (INIFile.getIntegerFromINI(SourceFile, "hacktv", "arcorrection"));
                chkARCorrection.doClick();
                cmbARCorrection.setSelectedIndex(ImportedAR);
            }
        }
        // Scrambling system
        String ImportedScramblingSystem = (INIFile.getStringFromINI(SourceFile, "hacktv", "scramblingtype", "", false));
        String ImportedKey = (INIFile.getStringFromINI(SourceFile, "hacktv", "scramblingkey", "", false));
        String ImportedKey2 = (INIFile.getStringFromINI(SourceFile, "hacktv", "scramblingkey2", "", false));        
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
        // Scrambling key/viewing card type
        // Nothing
        if ( (ImportedScramblingSystem.isEmpty()) /*&& (ImportedKey.isEmpty())*/ ) {
            // Do nothing
        // VideoCrypt 1/2/S (including VC1 side of dual VC1/2 mode)    
        } else if (ImportedKey.equals("free")) {
            cmbScramblingKey1.setSelectedIndex(0);
        } else if (ImportedKey.equals("conditional")) {
            // VC2 conditional mode only supported on Captain Jack fork
            if ( (ImportedScramblingSystem.equals("videocrypt2")) && (Fork.equals("CJ")) ) {
                cmbScramblingKey1.setSelectedIndex(1);
            // VC1 conditional mode only supported on fsphil's build
            } else if ( (ImportedScramblingSystem.equals("videocrypt")) && (Fork.equals("")) ) {
                cmbScramblingKey1.setSelectedIndex(1);
            // VCS conditional mode (supported on both)
            } else if (ImportedScramblingSystem.equals("videocrypts")) {
                cmbScramblingKey1.setSelectedIndex(1);
            }
        } else if ( (ImportedKey.equals("sky12")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(1);
        } else if ( (ImportedKey.equals("sky11")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(2);
        } else if ( (ImportedKey.equals("sky10")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(3);
        } else if ( (ImportedKey.equals("sky10ppv")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(4);
        } else if ( (ImportedKey.equals("sky09")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(5);
        } else if ( (ImportedKey.equals("sky07")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(6);
        } else if ( (ImportedKey.equals("sky03")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(7);
        } else if ( (ImportedKey.equals("tac")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(8);
        } else if ( (ImportedKey.equals("tac2")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(9);
        } else if ( (ImportedKey.equals("xtea")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(10);
        } else if ( (ImportedKey.equals("ppv")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(11);
        // Syster/D11/SysterC&R
        }  else if ( (ImportedScramblingSystem.equals("syster") && 
                (ImportedKey.equals("")) && (Fork != "CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(0);            
        }  else if ( (ImportedKey.equals("premiere-fa")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(0);            
        }  else if ( (ImportedKey.equals("premiere-ca")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(1);            
        }  else if ( (ImportedKey.equals("cfrfa")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(2);            
        }  else if ( (ImportedKey.equals("cfrca")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(3);            
        }  else if ( (ImportedKey.equals("cplfa")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(4);            
        }  else if ( (ImportedKey.equals("cesfa")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(5);            
        }  else if ( (ImportedKey.equals("ntvfa")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(6);
        // Eurocrypt            
        }  else if ( (ImportedKey.equals("")) && (ImportedScramblingSystem.equals("single-cut")) ) {
            cmbScramblingKey1.setSelectedIndex(0);            
        }  else if ( (ImportedKey.equals("")) && (ImportedScramblingSystem.equals("double-cut")) ) {
            cmbScramblingKey1.setSelectedIndex(0);            
        }  else if (ImportedKey.equals("eurocrypt filmnet")) {
            cmbScramblingKey1.setSelectedIndex(1); 
        }  else if (ImportedKey.equals("eurocrypt tv1000")) {
            cmbScramblingKey1.setSelectedIndex(2); 
        }  else if (ImportedKey.equals("eurocrypt ctv")) {
            cmbScramblingKey1.setSelectedIndex(3); 
        }  else if (ImportedKey.equals("eurocrypt ctvs")) {
            cmbScramblingKey1.setSelectedIndex(4); 
        }  else if (ImportedKey.equals("eurocrypt tvplus")) {
            cmbScramblingKey1.setSelectedIndex(5); 
        }  else if (ImportedKey.equals("eurocrypt tvs")) {
            cmbScramblingKey1.setSelectedIndex(6); 
        }  else if (ImportedKey.equals("eurocrypt rdv")) {
            cmbScramblingKey1.setSelectedIndex(7); 
        }  else if (ImportedKey.equals("eurocrypt nrk")) {
            cmbScramblingKey1.setSelectedIndex(8);             
        }  else if ( (ImportedKey.equals("cplus")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(9);            
        }  else if ( (ImportedKey.equals("tv3update")) && (Fork.equals("CJ")) ) {
            cmbScramblingKey1.setSelectedIndex(10);
        } else {
            if ( ImportedScramblingSystem != "videocrypt1+2") {
                invalidConfigFileValue("scrambling key", ImportedKey);
            } else {
                invalidConfigFileValue("VideoCrypt I scrambling key", ImportedKey);
            }
        }
        // VC2 side of dual VC1/2 mode
        if (cmbScramblingType.getSelectedIndex() == 3) {
            if (ImportedKey2.equals("free")) {
                cmbScramblingKey2.setSelectedIndex(0);
            } else if ( (ImportedKey2.equals("conditional")) && (Fork.equals("CJ")) ) {
                cmbScramblingKey2.setSelectedIndex(1);
            } else {
                invalidConfigFileValue("VideoCrypt II scrambling key", ImportedKey2);
            }
        }
        // EMM
        if ( (chkActivateCard.isEnabled()) && (chkDeactivateCard.isEnabled()) ) {
            if ((INIFile.getIntegerFromINI(SourceFile, "hacktv", "emm")) != null) {
                Integer ImportedEMM = (INIFile.getIntegerFromINI(SourceFile, "hacktv", "emm"));
                String ImportedCardNumber;
                String Imported13Prefix;
                if ( (ImportedEMM.equals(1)) || (ImportedEMM.equals(2)) ){
                    if (ImportedEMM.equals(1)) { chkActivateCard.doClick() ;}
                    if (ImportedEMM.equals(2)) { chkDeactivateCard.doClick() ;}
                    ImportedCardNumber = (INIFile.getStringFromINI(SourceFile, "hacktv", "cardnumber", "", false));
                    Imported13Prefix = (INIFile.getStringFromINI(SourceFile, "hacktv-gui3", "13digitprefix", "", false));
                    // The ImportedCardNumber value only contains 8 digits of the card number
                    // To find the check digit, we run the CalculateLuhnCheckDigit method and append the result
                    txtCardNumber.setText(Imported13Prefix + ImportedCardNumber + Luhn.CalculateLuhnCheckDigit(ImportedCardNumber));
                }
            }
        }
        // Show card serial
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "showserial")) {
            chkShowCardSerial.doClick();
        }
        // Brute force PPV key
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "findkey")) {
            chkFindKeys.doClick();
        }
        // Scramble audio
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "scramble-audio")) {
            chkScrambleAudio.doClick();
        }
        // Syster permutation table
        Integer ImportedPermutationTable;
        if (INIFile.getIntegerFromINI(SourceFile, "hacktv", "permutationtable") != null) {
            ImportedPermutationTable = INIFile.getIntegerFromINI(SourceFile, "hacktv", "permutationtable");
            if ( (Fork.equals("CJ")) && (ScramblingType1.equals("--syster")) || (ScramblingType1.equals("--systercnr")) ) {
                if ( (ImportedPermutationTable >= 0 ) &&
                        (ImportedPermutationTable < cmbSysterPermTable.getItemCount()) ) 
                cmbSysterPermTable.setSelectedIndex(ImportedPermutationTable);
            }
        }   
        // Sample rate (default to 16 MHz if not specified)
        Double ImportedSampleRate;
        if ((INIFile.getDoubleFromINI(SourceFile, "hacktv", "samplerate")) != null) {
            ImportedSampleRate = (INIFile.getDoubleFromINI(SourceFile, "hacktv", "samplerate") / 1000000);
        } else {
            ImportedSampleRate = Double.parseDouble("16");
            JOptionPane.showMessageDialog(null, "No sample rate specified, defaulting to 16 MHz.", AppName, JOptionPane.INFORMATION_MESSAGE);
        }
        txtSampleRate.setText(ImportedSampleRate.toString().replace(".0",""));        
        // ACP
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "acp")) {
            chkACP.doClick();
        }
        // Filter
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "filter")) {
            chkVideoFilter.doClick();
        }
        // Audio
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "audio") == false) {
            if (chkAudio.isSelected() ) { chkAudio.doClick(); }
        }
        // NICAM
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "nicam") == false) {
            if (chkNICAM.isSelected() ) { chkNICAM.doClick(); }
        }
        // ECM
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "showecm")) {
            chkShowECM.doClick();
        }
        // VITS
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "vits")) {
            chkVITS.doClick();
        }
        // Subtitles
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "subtitles")) {
            chkSubtitles.doClick();
            if ( (INIFile.getIntegerFromINI(SourceFile, "hacktv", "subtitleindex")) != null ) {
                txtSubtitleIndex.setText(Integer.toString((INIFile.getIntegerFromINI(SourceFile, "hacktv", "subtitleindex"))));
            }
        }
        // MAC channel ID
        String ImportedChID = (INIFile.getStringFromINI(SourceFile, "hacktv", "chid", "", true));
        if (!ImportedChID.isEmpty()) {
            if (!chkMacChId.isSelected()) chkMacChId.doClick();
            txtMacChId.setText(ImportedChID);
        }
        // Disable colour
        if (chkColour.isEnabled()) {
            // Accept both UK and US English spelling
            if ( (INIFile.getBooleanFromINI(SourceFile, "hacktv", "nocolour")) ||
                    (INIFile.getBooleanFromINI(SourceFile, "hacktv", "nocolor")) ){
                chkColour.doClick();
            }
        }
        // SoapySDR antenna name
        if (cmbOutputDevice.getSelectedIndex() == 1) {
            txtAntennaName.setText(INIFile.getStringFromINI(SourceFile, "hacktv", "antennaname", "", false));
        }
        // Output file type
        if (cmbOutputDevice.getSelectedIndex() == 3) {
            switch (INIFile.getStringFromINI(SourceFile, "hacktv", "filetype", "", false)) {
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
        String ImportedVolume = (INIFile.getStringFromINI(SourceFile, "hacktv", "volume", "", false));
        if (!ImportedVolume.isEmpty()) {
            chkVolume.doClick();
            txtVolume.setText(ImportedVolume);
        }
        // Downmix
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "downmix")) {
            chkDownmix.doClick();
        }
        // Teletext subtitles
        if (INIFile.getBooleanFromINI(SourceFile, "hacktv", "teletextsubtitles")) {
            chkTextSubtitles.doClick();
            if ( (INIFile.getIntegerFromINI(SourceFile, "hacktv", "teletextsubindex")) != null ) {
                txtTextSubtitleIndex.setText(Integer.toString((INIFile.getIntegerFromINI(SourceFile, "hacktv", "teletextsubindex"))));
            }
        }
        // This must be the last line in this method, it confirms that 
        // everything ran as planned.
        return true;
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
        // Write file structure
        String FileContents = "[hacktv]\n[hacktv-gui3]\n";
        // Save current fork if applicable
        if (Fork.equals("CJ")) FileContents = INIFile.setINIValue(FileContents, "hacktv-gui3", "fork", "CaptainJack");
        // Output device
        switch (cmbOutputDevice.getSelectedIndex()) {
            case 0:
                if (txtOutputDevice.getText().isBlank()) {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "hackrf");
                }
                else {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "hackrf" + ":" + txtOutputDevice.getText());
                }
                break;
            case 1:
                if (txtOutputDevice.getText().isBlank()) {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "soapysdr");
                }
                else {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "soapysdr" + ":" + txtOutputDevice.getText());
                }
                break;
            case 2:
                if (txtOutputDevice.getText().isBlank()) {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "fl2k");
                }
                else {
                    FileContents = INIFile.setINIValue(FileContents, "hacktv", "output", "fl2k" + ":" + txtOutputDevice.getText());
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
        // Input source or test card
        if (radTest.isSelected()) {
            if ((Fork == "CJ") && (Lines == 625)) {
                FileContents = INIFile.setINIValue(FileContents, "hacktv", "input", TestCardArray[cmbTest.getSelectedIndex()]);
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
        // Video format/mode
        FileContents = INIFile.setINIValue(FileContents, "hacktv", "mode", Sys);
        // Frequency and channel
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) && (!radCustom.isSelected()) ) { 
            FileContents = INIFile.setINIValue(FileContents, "hacktv-gui3", "channel", cmbChannel.getSelectedItem().toString());
            FileContents = INIFile.setLongINIValue(FileContents, "hacktv", "frequency", Frequency);
        }
        // Sample rate
        FileContents = INIFile.setLongINIValue(FileContents, "hacktv", "samplerate", (long) (Double.parseDouble(txtSampleRate.getText()) * 1000000));
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
            FileContents = INIFile.setIntegerINIValue(FileContents, "hacktv", "teletextsubtitles", 1);
            FileContents = INIFile.setINIValue(FileContents, "hacktv", "teletextsubindex", txtTextSubtitleIndex.getText());
        }
        // Commit to disk
        try {
            FileWriter fw = new FileWriter(DestinationFileName);
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
        // Check that the file is in the correct format by loading its first line
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(SourceFile, StandardCharsets.UTF_8));
            LineNumberReader lnr2 = new LineNumberReader(br2);
            String FileContents = lnr2.readLine();
            br2.close();
            // We use endsWith to avoid problems caused by Unicode BOMs
            if ( (FileContents == null) || (!FileContents.endsWith("#EXTM3U")) ) {
                JOptionPane.showMessageDialog(null, "Invalid file format, only Extended M3U files are supported.", AppName, JOptionPane.ERROR_MESSAGE);
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
                    /* Increase linecount by one. This is done to ensure that
                     * an M3U file without a newline at the end will still be
                     * parsed correctly. Without this, the last line will not
                     * get added to the array and will result in an out of 
                     * bounds exception if selected. Files with a newline are 
                     * not affected because the extra line is ignored.
                    */
                    int linecount = countLines(fd.toFile()) + 1;
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
                ex.printStackTrace();
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
    
    private int countLines(File file) throws IOException {
        // By fhucho at https://stackoverflow.com/questions/1277880/how-can-i-get-the-count-of-line-in-a-file-in-an-efficient-way
        int l = 0;

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[8];
        int read;

        while ((read = fis.read(buffer)) != -1) {
            for (int i = 0; i < read; i++) {
                if (buffer[i] == '\n') l++;
            }
        }

        fis.close();

        return l;
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
        // Select default radio buttons and comboboxes
        radLocalSource.doClick();
        radPAL.doClick();
        radUHF.doClick();
        cmbScramblingType.setSelectedIndex(0);
        // Reset gain to zero
        txtGain.setText("0");
        // Re-enable audio option
        if (! chkAudio.isSelected() ) { chkAudio.doClick(); }
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
            if (f.exists()) { deleteFSObject(f.toPath()); }
            // Download the index page
            txtAllOptions.setText("Downloading index page from " + url);
            download(url, DownloadPath);
        }
        catch (Exception ex) {
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
                    deleteFSObject(f.toPath());
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
                        download(DownloadURL + TeletextLinks.get(i), TeletextPath + OS_SEP + TeletextLinks.get(i));
                        // Stop when the integer value reaches the size of the teletext array
                        if (j == TeletextLinks.size() ) { return 0; }
                    }
                    catch (Exception ex) {
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
                if (status == 0) {
                    // All good
                    txtAllOptions.setText("Done");
                    txtTeletextSource.setText(TeletextPath);
                }
                else if (status == 1) {
                    // Download cancelled by the user
                    pbTeletext.setValue(0);
                    txtAllOptions.setText("Cancelled");
                }
                else if (status == 2) {
                    // The index page was downloaded but a teletext page failed.
                    // Connection failure?
                    JOptionPane.showMessageDialog(null, "An error occurred while downloading files. "
                            + "Please ensure that you are connected to the internet and try again.", AppName, JOptionPane.ERROR_MESSAGE);
                    pbTeletext.setValue(0);
                    txtAllOptions.setText("Failed");
                }
                else if(status == 3) {
                    // The index page was downloaded but we didn't find anything.
                    // Most likely means that we need to revise this!
                    JOptionPane.showMessageDialog(null, "No teletext files were found.", AppName, JOptionPane.ERROR_MESSAGE);
                    pbTeletext.setValue(0);
                    txtAllOptions.setText("Failed");
                }
                else {
                    JOptionPane.showMessageDialog(null, "An unknown error has occurred, code " + status, AppName, JOptionPane.ERROR_MESSAGE);
                    pbTeletext.setValue(0);
                    txtAllOptions.setText("Failed");
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
    
    public static void download(String url, String fileName) throws Exception {
        URLConnection connection = new URL(url).openConnection();
        connection.setUseCaches(false);
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, Paths.get(fileName));  
        }
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
            cmbVideoFormat.setSelectedIndex(2);
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
        cmbScramblingType.setEnabled(false);
        cmbScramblingType.setSelectedIndex(0);
        lblScramblingSystem.setEnabled(false);
        scramblingPanel.setEnabled(false);
    }      
    
    private void addPALScramblingTypes() {
    /* Population of scrambling systems is different to other comboboxes. We 
       populate an ArrayList and convert it to an Array to populate the 
       combobox. I chose to do it this way because it saves the need to maintain
       separate arrays for forks.
    */
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
    /* Population of scrambling systems is different to other comboboxes. We 
       populate an ArrayList and convert it to an Array to populate the 
       combobox. I chose to do it this way because it saves the need to maintain
       separate arrays for forks.
    */
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
        ArrayList<String> ScramblingKeyAL = new ArrayList<>();
        ArrayList<String> ScramblingKey2AL = new ArrayList<>();    
        // In the clear (no scrambling)
        if (ScramblingType1.isEmpty()) {
            cmbScramblingKey1.setEnabled(false);
            cmbScramblingKey1.setSelectedIndex(-1);
            lblScramblingKey.setEnabled(false);
            scramblingOptionsPanel.setEnabled(false);
            emmPanel.setEnabled(false);
            disableScramblingKey2();
            ScramblingType2 = "";
            ScramblingKey1 = "";
            ScramblingKey2 = "";
            configureScramblingOptions();
            if (chkVideoFilter.isSelected()) {
                if (!FMSampleRate.isEmpty()) txtSampleRate.setText(FMSampleRate);
            } else {
                txtSampleRate.setText(DefaultSampleRate);
            }
            return;
        }
        else {
            cmbScramblingKey1.setEnabled(true);
            lblScramblingKey.setEnabled(true);
            scramblingOptionsPanel.setEnabled(true);
            emmPanel.setEnabled(true);            
        }
        // VideoCrypt I (and VideoCrypt II if both are selected)
        if (ScramblingType1 == "--videocrypt") {
            // Set sample rate to 14 MHz
            txtSampleRate.setText("14");
            ScramblingKeyAL.add("Free access/soft scrambled (no card required)");
            if (Fork == "CJ") {
                ScramblingKeyAL.add("Conditional access (Sky 12 card)");
                ScramblingKeyAL.add("Conditional access (Sky 11 card)");
                ScramblingKeyAL.add("Conditional access (Sky 10 card)");
                ScramblingKeyAL.add("Pay-per-view mode (Sky 10 card)");
                ScramblingKeyAL.add("Conditional access (Sky 09 card)");
                ScramblingKeyAL.add("Conditional access (Sky 07 or 06 card)");
                ScramblingKeyAL.add("Conditional access (Sky 02, 03 or 04 card)");
                ScramblingKeyAL.add("Conditional access (Old Adult Channel card)");
                ScramblingKeyAL.add("Conditional access (Newer Adult Channel card)");
                ScramblingKeyAL.add("Conditional access (xtea mode)");
                ScramblingKeyAL.add("Pay-per-view mode (e.g. phone cards)");
            }   
            else if ( cmbScramblingType.getSelectedIndex() == 1 ) {
                ScramblingKeyAL.add("Conditional access (Sky 11 card)");
            }
            ScramblingKeyArray = new ArrayList<>();
            ScramblingKeyArray.add("free");
            if (Fork == "CJ") {
                ScramblingKeyArray.add("sky12");
                ScramblingKeyArray.add("sky11");
                ScramblingKeyArray.add("sky10");
                ScramblingKeyArray.add("sky10ppv");
                ScramblingKeyArray.add("sky09");
                ScramblingKeyArray.add("sky07");
                ScramblingKeyArray.add("sky03");
                ScramblingKeyArray.add("tac1");
                ScramblingKeyArray.add("tac2");
                ScramblingKeyArray.add("xtea");
                ScramblingKeyArray.add("ppv");
            }
            else {
                ScramblingKeyArray.add("conditional");
            }
            // VideoCrypt II side of dual mode
            if ( cmbScramblingType.getSelectedIndex() == 3 ) {
                enableScramblingKey2();
                ScramblingKey2AL.add("Free access/soft scrambled (no card required)");
                if (Fork == "CJ") {
                    ScramblingKey2AL.add("Conditional access (MultiChoice card)");
                }
                ScramblingKey2Array = new ArrayList<>();
                ScramblingKey2Array.add("free");
                if (Fork == "CJ") {
                    ScramblingKey2Array.add("conditional");
                }
            } else {
                disableScramblingKey2();
            }
        }
        // VideoCrypt II
        else if (ScramblingType1 == "--videocrypt2") {
            // Set sample rate to 14 MHz
            txtSampleRate.setText("14");
            disableScramblingKey2();
            ScramblingKeyAL.add("Free access/soft scrambled (no card required)");
            if (Fork == "CJ") {
                ScramblingKeyAL.add("Conditional access (MultiChoice card)");
            }
            ScramblingKeyArray = new ArrayList<>();
            ScramblingKeyArray.add("free");
            if (Fork == "CJ") {
                ScramblingKeyArray.add("conditional");
            }                          
        }
        // VideoCrypt S
        else if (ScramblingType1.equals("--videocrypts") ) {
            // Set sample rate to 14 MHz (may not be correct!)
            txtSampleRate.setText("14");
            disableScramblingKey2();
            ScramblingKeyAL.add("Free access/soft scrambled (no card required)");
            ScramblingKeyAL.add("Conditional access (BBC Select card)");
            ScramblingKeyArray = new ArrayList<>();
            ScramblingKeyArray.add("free");
            ScramblingKeyArray.add("conditional");
        }
        // Syster, Discret 11 or SysterC&R
        else if (ScramblingType1.equals("--syster") || 
                (ScramblingType1.equals("--d11")) || 
                (ScramblingType1.equals("--systercnr")) ) {
            // Set sample rate to 20 MHz
            txtSampleRate.setText("20");
            if (Fork.equals("CJ")) {
                disableScramblingKey2();
                ScramblingKeyAL.add("Free access (Premiere Germany)");
                ScramblingKeyAL.add("Conditional access (Premiere Germany)");
                ScramblingKeyAL.add("Free access (Canal+ France)");
                ScramblingKeyAL.add("Conditional access (Canal+ France)");
                ScramblingKeyAL.add("Free access (Canal+ Poland)");
                ScramblingKeyAL.add("Free access (Canal+ Spain)");
                ScramblingKeyAL.add("Free access (HTB+ Russia)");
            }
            else {
                ScramblingKeyAL.add("Free access");                
            }
            ScramblingKeyArray = new ArrayList<>();
            if (Fork.equals("CJ")) {
                ScramblingKeyArray.add("premiere-fa");
                ScramblingKeyArray.add("premiere-ca");
                ScramblingKeyArray.add("cfrfa");
                ScramblingKeyArray.add("cfrca");
                ScramblingKeyArray.add("cplfa");
                ScramblingKeyArray.add("cesfa");
                ScramblingKeyArray.add("ntvfa");
            }
            else {
                ScramblingKeyArray.add("");
            }
        }
        // EuroCrypt
        else if ( ScramblingType1.equals("--single-cut") || 
                (ScramblingType1.equals("--double-cut")) ) {
            disableScramblingKey2();
            ScramblingKeyAL.add("No conditional access (free)");
            ScramblingKeyAL.add("EuroCrypt M (FilmNet card)");
            ScramblingKeyAL.add("EuroCrypt M (TV1000 card)");
            ScramblingKeyAL.add("EuroCrypt M (CTV card)");
            ScramblingKeyAL.add("EuroCrypt M (TV Plus card)");
            ScramblingKeyAL.add("EuroCrypt S2 (TVS Denmark card)");
            ScramblingKeyAL.add("EuroCrypt S2 (RDV card)");
            ScramblingKeyAL.add("EuroCrypt S2 (NRK card)");
            ScramblingKeyAL.add("EuroCrypt S2 (CTV card)");
            if (Fork.equals("CJ")) {
                ScramblingKeyAL.add("EuroCrypt S2 (Canal+ Nordic card)");
                ScramblingKeyAL.add("EuroCrypt M (Autoupdate mode - tv3update)");
            }
            ScramblingKeyArray = new ArrayList<>();
            ScramblingKeyArray.add("");
            ScramblingKeyArray.add("filmnet");
            ScramblingKeyArray.add("tv1000");
            ScramblingKeyArray.add("ctv");
            ScramblingKeyArray.add("tvplus");
            ScramblingKeyArray.add("tvs");
            ScramblingKeyArray.add("rdv");
            ScramblingKeyArray.add("nrk");
            ScramblingKeyArray.add("ctv");
            if (Fork.equals("CJ")) {
                ScramblingKeyArray.add("cplus");
                ScramblingKeyArray.add("tv3update");
            }
        }
        // End adding scrambling systems above this line
        cmbScramblingKey1.removeAllItems();       
        // Convert to an array so we can populate
        String[] ScramblingKey = new String[ScramblingKeyAL.size()];
        for(int i = 0; i < ScramblingKey.length; i++) {
            ScramblingKey[i] = ScramblingKeyAL.get(i);
        }
        cmbScramblingKey1.setModel(new DefaultComboBoxModel<>(ScramblingKey));
        cmbScramblingKey1.setSelectedIndex(0);
        
        // VC1+2 dual mode
        if (cmbScramblingKey2.isEnabled()) {
            cmbScramblingKey2.removeAllItems();
            // Convert to an array so we can populate
            String[] ScramblingKey2A = new String[ScramblingKey2AL.size()];
            for(int i = 0; i < ScramblingKey2A.length; i++) {
                ScramblingKey2A[i] = ScramblingKey2AL.get(i);
            }
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
        if ( ((ScramblingType1).equals("--syster")) ||
                ((ScramblingType1).equals("--systercnr")) &&
                (Fork == "CJ") ) {
            lblSysterPermTable.setEnabled(true);
            cmbSysterPermTable.setEnabled(true);
            cmbSysterPermTable.setSelectedIndex(0);
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
            if ((cmbScramblingType.getSelectedIndex() == 0) &&
                    (!radMAC.isSelected()) ) {
                enableACP();
            }
        }
        else {
            if (Fork.equals("CJ")) { chkShowECM.setEnabled(true); }
            disableACP();
        }
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
        String[] Logo = {
            "hacktv",
            "Cartoon Network",
            "TV1000",
            "FilmNet1",
            "Canal+",
            "Eurotica",
            "MTV",
            "The Adult Channel",
            "FilmNet",
            "MultiChoice"
        };
        LogoArray = new String[] {
            "hacktv",
            "cartoonnetwork",
            "tv1000",
            "filmnet1",
            "canal+",
            "eurotica",
            "mtv",
            "tac",
            "filmnet",
            "multichoice"
        };
        cmbLogo.removeAllItems();
        cmbLogo.setModel(new DefaultComboBoxModel<>(Logo));
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
        String[] TestCard = {
            "Colour bars",
            "Philips PM5544",
            "UEIT (Soviet)",
            "FuBK"
        };
        TestCardArray = new String[] {
            "test:colourbars",
            "test:pm5544",
            "test:ueitm",
            "test:fubk"
        };
        cmbTest.removeAllItems();
        cmbTest.setModel(new DefaultComboBoxModel<>(TestCard));
        cmbTest.setSelectedIndex(-1);
    }
    
    private void checkTestCard() {
        if (cmbTest.isEnabled()) {
            InputSource = TestCardArray[cmbTest.getSelectedIndex()];
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
    // the corresponding video format from VideoModeArray.
        Sys = VideoModeArray[cmbVideoFormat.getSelectedIndex()];
        switch (Sys) {
            case "i":
            case "g":
                // System I or B/G (625 lines)
                Baseband = false;
                common625Features();
                enableVHF();
                enableUHF();
                disableFMDeviation();
                DefaultSampleRate = "16";
                radUHF.doClick();
                break;
            case "pal-fm":
            case "secam-fm":
                // PAL-FM or SECAM-FM
                Baseband = false;
                common625Features();
                disableVHF();
                disableUHF();
                disableNICAM();
                NICAMSupported = false;
                enableFMDeviation();
                DefaultSampleRate = "16";
                FMSampleRate = "20.25";
                radCustom.doClick();
                break;
            case "m":
            case "pal-m":
                // System M (525 lines) with NTSC or PAL colour
                Baseband = false;
                common525Features();
                enableVHF();
                enableUHF();
                disableNICAM();
                NICAMSupported = false;
                enableColourControl();
                disableFMDeviation();
                DefaultSampleRate = "13.5";
                radUHF.doClick();
                break;
            case "ntsc-fm":
            case "ntsc-bs":
                // NTSC-FM (with analogue or BS audio)
                Baseband = false;
                common525Features();
                disableVHF();
                disableUHF();
                disableNICAM();
                NICAMSupported = false;
                enableColourControl();
                enableFMDeviation();
                DefaultSampleRate = "13.5";
                FMSampleRate = "18";
                radCustom.doClick();
                break;
            case "apollo-fsc-fm":
                // Apollo field sequential color
                Baseband = false;
                Lines = 525;
                disableVHF();
                disableUHF();
                disableColourControl();
                enableFMDeviation();
                DefaultSampleRate = "13.5";
                FMSampleRate = "18";
                radCustom.doClick();
                break;
            case "m-cbs405":
                // CBS field sequential color (405 lines)
                Baseband = false;
                Lines = 405;
                enableUHF();
                enableVHF();
                disableColourControl();
                DefaultSampleRate = "18.954";
                disableFMDeviation();
                radUHF.doClick();
                disableColourControl();
                break;
            case "l":
                // System L (625 lines)
                Baseband = false;
                common625Features();
                enableUHF();
                enableVHF();
                DefaultSampleRate = "16";
                disableFMDeviation();
                radUHF.doClick();
                break;
            case "d":
                // System D/K (625 lines)
                Baseband = false;
                common625Features();
                disableNICAM();
                NICAMSupported = false;
                enableVHF();
                enableUHF();
                disableFMDeviation();
                DefaultSampleRate = "16";
                radUHF.doClick();
                break;
            case "a":
                // System A (405 lines)
                Baseband = false;
                Lines = 405;
                enableAudioOption();
                disableUHF();
                enableVHF();
                radVHF.doClick();
                disableFMDeviation();
                DefaultSampleRate = "6.48";
                break;
            case "e":
                // System E (819 lines)
                Baseband = false;
                Lines = 819;
                enableAudioOption();
                disableUHF();
                enableVHF();
                radVHF.doClick();
                disableFMDeviation();
                DefaultSampleRate = "20.475";
                break;
            case "240-am":
                // Baird 240 lines
                Baseband = false;
                Lines = 240;
                disableAudioOption();
                disableUHF();
                disableVHF();
                radCustom.doClick();
                disableFMDeviation();
                DefaultSampleRate = "1.992";
                break;
            case "30-am":
                // Baird 30 lines
                Baseband = false;
                Lines = 30;
                disableAudioOption();
                disableUHF();
                disableVHF();
                radCustom.doClick();
                disableFMDeviation();
                DefaultSampleRate = "0.1005";
                break;
            case "apollo-fm":
                // Apollo
                Baseband = false;
                Lines = 320;
                enableAudioOption();
                disableUHF();
                disableVHF();
                radCustom.doClick();
                enableFMDeviation();
                DefaultSampleRate = "2.048";
                break;
            case "nbtv-am":
                // NBTV Club standard
                Baseband = false;
                Lines = 32;
                disableAudioOption();
                disableUHF();
                disableVHF();
                radCustom.doClick();
                disableFMDeviation();
                DefaultSampleRate = "0.1";
                break;
            case "d2mac-am":
            case "dmac-am":
                Baseband = false;
                // D(2)-MAC AM
                disableFMDeviation();
                disableUHF();
                radCustom.doClick();
                break;
            case "d2mac-fm":
                Baseband = false;
                // D2-MAC FM
                enableFMDeviation();
                disableUHF();
                radCustom.doClick();
                break;
            case "dmac-fm":
                Baseband = false;
                // D-MAC FM
                enableFMDeviation();
                enableUHF();
                radUHF.doClick();
                break;
            case "pal":
            case "secam":
                // 625 baseband (PAL or SECAM)
                common625Features();
                commonBasebandFeatures();
                DefaultSampleRate = "16";
                break;
            case "525pal":
            case "ntsc":
                // 525 baseband (NTSC or PAL)
                common525Features();
                commonBasebandFeatures();
                DefaultSampleRate = "13.5";
                break;                
            case "d2mac":
            case "dmac":
                // MAC baseband
                commonBasebandFeatures();
                break;
            case "405":
                // 405 line baseband
                Lines = 405;
                commonBasebandFeatures();
                DefaultSampleRate = "6.48";
                break;
            case "819":
                // 819 line baseband
                Lines = 819;
                commonBasebandFeatures();
                DefaultSampleRate = "20.475";
                break;
            case "240":
                // Baird 240 lines baseband
                Lines = 240;
                commonBasebandFeatures();
                DefaultSampleRate = "1.992";
                break;
            case "30":
                // Baird 30 lines baseband
                Lines = 30;
                commonBasebandFeatures();
                DefaultSampleRate = "0.1005";
                break;
            case "nbtv":
                // NBTV Club baseband
                Lines = 32;
                commonBasebandFeatures();
                DefaultSampleRate = "0.1";
                break;
            case "apollo":
                // Apollo baseband
                Lines = 320;
                commonBasebandFeatures();
                DefaultSampleRate = "2.048";
                break;
            case "cbs405":
                // CBS FSC baseband
                Lines = 405;
                commonBasebandFeatures();
                DefaultSampleRate = "18.954";
                break;
            default:
                break;
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
    
    private void common625Features() {
    // Configure features common to 625-line modes
        Lines = 625;
        enableNICAM();
        NICAMSupported = true;
        enableAudioOption();
        enableColourControl();
        enableTeletext();
        enableWSS();
        enableVITS();
        enableACP();
        enableScrambling();
        addPALScramblingTypes();
        disableChannelID();
    }

    private void common525Features() {
    // Configure features common to 525-line modes
        Lines = 525;
        disableNICAM();
        NICAMSupported = false;
        enableAudioOption();
        disableTeletext();
        disableWSS();
        disableScrambling();
        disableACP();
        enableVITS();
        disableChannelID();    
    }
    
    private void commonBWFeatures() {
    // Configure features common to legacy black and white modes
        disableNICAM();
        NICAMSupported = false;
        disableColourControl();
        disableTeletext();
        disableWSS();
        disableScrambling();
        disableACP();
        disableVITS();
        disableChannelID();
    }  
    
    private void commonMACFeatures() {
    // Configure features common to MAC modes
        Lines = 625;
        enableAudioOption();
        chkAudio.setEnabled(false);
        AudioParam = "";
        disableVHF();
        disableNICAM();
        NICAMSupported = false;
        disableColourControl();
        enableTeletext();
        disableWSS();
        disableVITS();
        disableACP();
        enableScrambling();
        addMACScramblingTypes();
        enableChannelID();
        DefaultSampleRate = "20.25";          
    }
    
    private void commonBasebandFeatures() {
    // Configure features common to baseband modes
        if ( (cmbOutputDevice.getSelectedIndex() == 2) ||
                (cmbOutputDevice.getSelectedIndex() == 3) ) {
            Baseband = true;
            disableRFOptions();
            disableFMDeviation();
            disableAudioOption();
            disableNICAM();
            NICAMSupported = false;
            if (chkVideoFilter.isSelected()) chkVideoFilter.doClick();
            chkVideoFilter.setEnabled(false);            
        }
        else {
            JOptionPane.showMessageDialog(null, "This mode is not supported by the selected output device.", AppName, JOptionPane.WARNING_MESSAGE);
            cmbVideoFormat.setSelectedIndex(PreviousIndex);
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
                System.out.println("blah");
                txtTeletextSource.setText(txtTeletextSource.getText().replaceAll(String.valueOf((char)34), ""));
            }
            if ((txtTeletextSource.getText()).isEmpty()) {
                // Create a temp directory if it does not exist
                createTempDirectory();
                // Copy the demo page resource to the temp directory
                try {
                    copyResource("/com/steeviebops/resources/demo.tti", TempDir.toString() + "/demo.tti", this.getClass());   
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
                            deleteFSObject(Path.of(TempDir + "/spark/P888.tti"));
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
                if ( (wildcardFind(txtTeletextSource.getText(), "p8", ".tti") > 0) || 
                        (wildcardFind(txtTeletextSource.getText(), "p8", ".ttix") > 0) ) {
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
            chkNICAM.setSelected(true);
        }
    }
       
    private void disableNICAM() {
        chkNICAM.setEnabled(false);
        chkNICAM.setSelected(false);
        // Clear NICAMParam as we don't need it if NICAM is not supported anyway
        NICAMParam = "";
    }
    
    private void enableFMDeviation() {
        chkFMDev.setEnabled(true);
        // The --filter parameter enables VSB filtering on AM, or CCIR-405 FM 
        // pre-emphasis filtering on FM, so change the Filter checkbox
        // description to suit  
        chkVideoFilter.setText("FM video pre-emphasis filter");
        FMSampleRate = "";
    }
    
    private void disableFMDeviation() {    
        chkFMDev.setEnabled(false);
        chkFMDev.setSelected(false);
        txtFMDev.setText("");
        txtFMDev.setEnabled(false);
        if (chkVideoFilter.isSelected()) {
            chkVideoFilter.setSelected(false);
            txtSampleRate.setText(DefaultSampleRate);
        }
        // Revert Filter checkbox name to VSB-AM
        chkVideoFilter.setText("VSB-AM filter");
        FMSampleRate = "";
    }
       
    private void addEuropeUHFChannels() {
    // Populate with standard Western European UHF channels
    // (E21 to E69, 471.25 to 855.25 MHz)
    // The Channel array contains the channel number displayed in the combobox
    // We add the frequency (in Hz) as a secondary array and pass it raw to hacktv
        ChannelArray = new String[] { 
            "E21", "E22", "E23", "E24", "E25", "E26", "E27", "E28", "E29", "E30",
            "E31", "E32", "E33", "E34", "E35", "E36", "E37", "E38", "E39", "E40",
            "E41", "E42", "E43", "E44", "E45", "E46", "E47", "E48",
        // E49 to E60 were deallocated in 2020, pending allocation to rural 5G mobile services.
            "E49", "E50", "E51", "E52", "E53", "E54", "E55", "E56", "E57", "E58",
            "E59", "E60",
        // E61 to E69 were deallocated after analogue switch-off in 2012. Now used for LTE/4G mobile.
            "E61", "E62", "E63", "E64", "E65", "E66", "E67", "E68", "E69"
        };
        FrequencyArray = new int[] { 
            471250000, 479250000, 487250000, 495250000, 503250000, 511250000,
            519250000, 527250000, 535250000, 543250000, 551250000, 559250000,
            567250000, 575250000, 583250000, 591250000, 599250000, 607250000,
            615250000, 623250000, 631250000, 639250000, 647250000, 655250000,
            663250000, 671250000, 679250000, 687250000, 695250000, 703250000,
            711250000, 719250000, 727250000, 735250000, 743250000, 751250000,
            759250000, 767250000, 775250000, 783250000, 791250000, 799250000,
            807250000, 815250000, 823250000, 831250000, 839250000, 847250000,
            855250000
        };
        cmbChannel.removeAllItems();
        cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
        cmbChannel.setSelectedIndex(0);
        lblRegion.setText("Western Europe");
    }

    private void addSysBVHFChannels() {
    // Populate with standard Western European PAL-B 7 MHz VHF channels
    // (E2 to E12, 48.25 to 224.25 MHz)
    // The Channel array contains the channel number displayed in the combobox
    // We add the frequency (in Hz) as a secondary array
    //
        ChannelArray = new String[] {  
            "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9","E10", "E11", "E12"
        };
        FrequencyArray = new int[] { 
            48250000, 55250000, 62250000, 175250000, 182250000, 189250000, 
            196250000, 203250000, 210250000, 217250000, 224250000
        };
        cmbChannel.removeAllItems();
        cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
        cmbChannel.setSelectedIndex(0);
        lblRegion.setText("Continental Europe");
    }
    
    private void addIrishVHFChannels() {
    // Populate with standard Irish PAL-I 8 MHz VHF channels 
    // (A to J, 45.75 to 223.25 MHz)
    // The Channel array contains the channel number displayed in the combobox
    // We add the frequency (in Hz) as a secondary array
    //
        ChannelArray = new String[] {
        // Channel A was never used but all TVs supported it so we'll keep it   
            "A", "B", "C", "D", "E", "F", "G", "H","I", "J"
        };
        FrequencyArray = new int[] { 
            45750000, 53750000, 61750000, 175250000, 183250000, 191250000,
            199250000, 207250000, 215250000, 223250000
        };
        cmbChannel.removeAllItems();
        cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
        cmbChannel.setSelectedIndex(0);
        lblRegion.setText("Ireland (RTE)");
    }
    
    private void addNTSCVHFChannels() {
    // Populate with standard North American NTSC VHF channels.
    // (A2 to A13, 55.25 to 211.25 MHz)
    // The Channel array contains the channel number displayed in the combobox
    // We add the frequency (in Hz) as a secondary array
        ChannelArray = new String[] {
            "A2", "A3", "A4", "A5", "A6", "A7", "A8" ,"A9", "A10", "A11", "A12",
            "A13"
        };
        FrequencyArray = new int[] { 
            55250000, 61250000, 67250000, 77250000, 83250000, 175250000, 181250000,
            187250000, 193250000, 199250000, 205250000, 211250000
        };
        cmbChannel.removeAllItems();
        cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
        cmbChannel.setSelectedIndex(0);
        lblRegion.setText("North/South America (NTSC)");
    }

    private void addNTSCUHFChannels() {
    // Populate with standard North American NTSC UHF channels.
    // (A14 to A83, 471.25 to 885.25 MHz). Also used in Brazil for PAL-M.
    // The Channel array contains the channel number displayed in the combobox
    // We add the frequency (in Hz) as a secondary array
        ChannelArray = new String[] {
            "A14", "A15", "A16", "A17", "A18", "A19", "A20", "A21", "A22", "A23",
            "A24", "A25", "A26", "A27", "A28", "A29", "A30", "A31", "A32", "A33",
            "A34", "A35", "A36", "A37", "A38", "A39", "A40", "A41", "A42", "A43",
            "A44", "A45", "A46", "A47", "A48", "A49", "A50", "A51", "A52", "A53",
            "A54", "A55", "A56", "A57", "A58", "A59", "A60", "A61", "A62", "A63",
            "A64", "A65", "A66", "A67", "A68", "A69", 
        // A70 to A83 were deallocated by the FCC in 1983 to be used by AMPS
        // analog cellular services
            "A70", "A71", "A72", "A73", "A74", "A75", "A76", "A77", "A78", "A79",
            "A80", "A81", "A82", "A83"
        };
        FrequencyArray = new int[] { 
            471250000, 477250000, 483250000, 489250000, 495250000, 501250000,
            507250000, 513250000, 519250000, 525250000, 531250000, 537250000,
            543250000, 549250000, 555250000, 561250000, 567250000, 573250000,
            579250000, 585250000, 591250000, 597250000, 603250000, 609250000,
            615250000, 621250000, 627250000, 633250000, 639250000, 645250000,
            651250000, 657250000, 663250000, 669250000, 675250000, 681250000,
            687250000, 693250000, 699250000, 705250000, 711250000, 717250000,
            723250000, 729250000, 735250000, 741250000, 747250000, 753250000,
            759250000, 765250000, 771250000, 777250000, 783250000, 789250000,
            795250000, 801250000, 807250000, 813250000, 819250000, 825250000,
            831250000, 837250000, 843250000, 849250000, 855250000, 861250000,
            867250000, 873250000, 879250000, 885250000
        };
        cmbChannel.removeAllItems();
        cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
        cmbChannel.setSelectedIndex(0);
        lblRegion.setText("North/South America (NTSC)");
    }  
     
    private void addFrenchVHFChannels() {
    // Populate with standard French SECAM-L VHF channels 
    // (2 to 10, 47.75 to 216.00 MHz)
    // The Channel array contains the channel number displayed in the combobox
    // We add the frequency (in Hz) as a secondary array
        ChannelArray = new String[] {
            "L2", "L3", "L4", "L5", "L6", "L7", "L8" ,"L9", "L10"
        };
        FrequencyArray = new int[] { 
            55750000, 60500000, 63750000, 176000000, 184000000, 
            192000000, 200000000, 208000000, 216000000
        };
        cmbChannel.removeAllItems();
        cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
        cmbChannel.setSelectedIndex(0);
        lblRegion.setText("France (SECAM)");
    }
    
    private void addSystemAChannels() {
    // Populate with standard British System A 405-line VHF channels 
    // (B1 to B13, 45.00 to 214.75 MHz)
    // The Channel array contains the channel number displayed in the combobox
    // We add the frequency (in Hz) as a secondary array
        ChannelArray = new String[] {
            "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8" ,"B9", "B10", "B11",
            "B12", "B13"
            // B14 was allocated but never used and most TVs didn't support it
            // Uncomment the line below if you want to use it
            // , "B14"
        };
        FrequencyArray = new int[] { 
            45000000, 51750000, 56750000, 61750000, 66750000, 179750000,
            184750000, 189750000, 194750000, 199750000, 204750000, 209750000, 
            214750000 //, 219750000
        };
        cmbChannel.removeAllItems();
        cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
        cmbChannel.setSelectedIndex(0);
        lblRegion.setText("United Kingdom");
    }    

    private void addSystemEChannels() {
    // Populate with standard French System E VHF channels
    // (F2 to F12, 52.40 to 212.85 MHz)
    // The Channel array contains the channel number displayed in the combobox
    // We add the frequency (in Hz) as a secondary array
        ChannelArray = new String[] {
            "F2", "F4", "F5", "F6", "F7", "F8a", "F8" ,"F9", "F10", "F11", "F12"
        };
        FrequencyArray = new int[] { 
            52400000, 65550000, 164000000, 173400000, 177150000, 185250000,
            186550000, 190300000, 199700000, 203450000, 212850000
        };
        cmbChannel.removeAllItems();
        cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
        cmbChannel.setSelectedIndex(0);
        lblRegion.setText("France (819 line)");
    }  
    
    private void addSystemDRussiaChannels() {
    /** Populate with standard Russian SECAM-D VHF channels
    * (1 to 12, 49.75 to 223.25 MHz)
    * Note that channels 4 and 5 overlap with the standard VHF-FM band
    * The Channel array contains the channel number displayed in the combobox
    * We add the frequency (in Hz) as a secondary array
    */
        ChannelArray = new String[] {
            "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8" ,"R9", "R10", "R11",
            "R12"
        };
        FrequencyArray = new int[] { 
            49750000, 59250000, 77250000, 85250000, 93250000, 175250000, 
            183250000, 191250000, 199250000, 207250000, 215250000, 223250000
        };
        cmbChannel.removeAllItems();
        cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
        cmbChannel.setSelectedIndex(0);
        lblRegion.setText("Russia");
    }
    
    private void addBSBChannels() {
    /* Populate with intermediate frequencies (IFs) for BSB satellite receivers
       Based on information provided by fsphil at
       https://www.sanslogic.co.uk/dmac/bsb.html
       Tested and confirmed working on an ITT/Nokia BSB receiver
       The Channel array contains the channel name displayed in the combobox
       We add the frequency (in Hz) as a secondary array
    */
        ChannelArray = new String[] {
            "4 (Now)", "8 (Galaxy)", "12 (Sports Ch)", "16 (Power St)",
            "20 (Movie Ch)",
            // Irish DBS channels are listed below
            // These were never used but are available for use on BSB receivers            
            "2 (Irish DBS)", "6 (Irish DBS)", "10 (Irish DBS)","14 (Irish DBS)",
            "18 (Irish DBS)"
        };
        FrequencyArray = new int[] { 
        // Now/Sky News
        // 11.78502 GHz, IF 1015.84 MHz            
            1015840000,
        // Galaxy/Sky One
        // 11.86174 GHz, IF 1092.56 MHz
            1092560000,
        // The Sports Channel/Sky Sports
        // 11.93846 GHz, IF 1169.28 MHz
            1169280000,
        // The Power Station/Sky Movies Plus
        // 12.01518 GHz, IF 1246.00 MHz
            1246000000, 
        // The Movie Channel
        // 12.09190 GHz, IF 1322.72 MHz
            1322720000,
        // Irish DBS channel
        // 11.74666 GHz, IF 977.48 MHz         
            977480000,
        // Irish DBS channel    
        // 11.82338 GHz, IF 1054.2 MHz
            1054200000,
        // Irish DBS channel
        // 11.90010 GHz, IF 1130.92 MHz
            1130920000,
        // Irish DBS channel
        // 11.97682 GHz, IF 1207.64 MHz
            1207640000,
        // Irish DBS channel
        // 12.05354 GHz, IF 1284.36 MHz        
            1284360000                    
        };
        cmbChannel.removeAllItems();
        cmbChannel.setModel(new DefaultComboBoxModel<>(ChannelArray));
        cmbChannel.setSelectedIndex(0);
        lblRegion.setText("BSB IF");
    }
    
    private boolean checkInputSource() {
        if (radLocalSource.isSelected()) {
            if (cmbM3USource.isVisible()) {
                InputSource = PlaylistURLsAL.get(cmbM3USource.getSelectedIndex());
                return true;
            }
            else if (!txtSource.getText().isBlank()) { 
                InputSource = txtSource.getText();
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
        if (isNumeric( txtSampleRate.getText())) {
            Double SR = Double.parseDouble(txtSampleRate.getText());
            SampleRate = (int) (SR * 1000000);
            return true;
        }
        else {
            JOptionPane.showMessageDialog(null, "Please specify a valid sample rate in MHz.", AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(0);
            return false;
        }
    }
    
    private boolean checkFMDeviation() {
        if (chkFMDev.isSelected()) {
            if (isNumeric(txtFMDev.getText())) {
                Double Deviation = Double.parseDouble(txtFMDev.getText());
                FMDevValue = (int) (Deviation * 1000000);
                return true;
            }
            else {
                JOptionPane.showMessageDialog(null, "Please specify a valid deviation in MHz.", AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(0);
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
            if ( (isNumeric( txtFrequency.getText())) && (!txtFrequency.getText().contains(" ")) ){
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
                tabPane.setSelectedIndex(0);
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
            if (!isNumeric(txtGamma.getText())) {
                JOptionPane.showMessageDialog(null, InvalidGamma, AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(1);
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
            if (!isNumeric(txtOutputLevel.getText())) {
                JOptionPane.showMessageDialog(null, InvalidOutputLevel, AppName, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(1);
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
        if (isNumeric(txtGain.getText())) {
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
            if (!isNumeric(txtCardNumber.getText())) {
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
                tabPane.setSelectedIndex(3);              
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
                        OutputDevice = "-o" + '\"' + txtOutputDevice.getText()+ '\"';
                    }
                    else {
                        // Don't add quotes on Unix systems as this just messes things up
                        OutputDevice = "-o" + txtOutputDevice.getText();
                    }
                    if (cmbFileType.getSelectedIndex() != 3) {
                        FileType = "-t" + cmbFileType.getItemAt(cmbFileType.getSelectedIndex());
                    }
                    return true;
                }
            case 2:
                // fl2k
                if (!txtOutputDevice.getText().isBlank()) {
                    OutputDevice = "-ofl2k" + ":" + txtOutputDevice.getText();
                }
                else {
                    OutputDevice = "-ofl2k";
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
                    OutputDevice = "-osoapysdr" + ":" + txtOutputDevice.getText();
                }
                else {
                    OutputDevice = "-osoapysdr";
                }
                return true;
            case 0:
                // HackRF
                if (!txtOutputDevice.getText().isBlank()) {
                    OutputDevice = "-ohackrf"  + ":" + txtOutputDevice.getText();
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
        if (isNumeric(txtVolume.getText())) {
            VolumeParam = "--volume";
            return true;
        }
        else {
            JOptionPane.showMessageDialog(null, InvalidVolume, AppName, JOptionPane.WARNING_MESSAGE);
            tabPane.setSelectedIndex(1);
            return false;
        }
    }
    
    private void runHackTV() {
        // Call each method and check its response. If false, then stop.
        if (!checkInputSource()) return;
        if (!checkCustomFrequency()) return;
        if (!checkFMDeviation()) return;
        if (!checkGamma()) return;
        if (!checkOutputLevel()) return;
        if (!checkGain()) return;
        if (!checkSampleRate()) return;
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
        AllArgs.add(HackTVPath);
        if (!OutputDevice.isEmpty()) AllArgs.add(OutputDevice);
        AllArgs.add("-m");
        AllArgs.add(Sys);
        // Only add frequency for HackRF or SoapySDR
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1)) {
            AllArgs.add("-f");
            AllArgs.add(Long.toString(Frequency));
        }
        // Add subtitles here, we need to make sure that subtitles is not the last parameter if
        // no index is specified. Otherwise hacktv reports that no input has been specified.
        if (!SubtitlesParam.isEmpty()) AllArgs.add(SubtitlesParam);
        if (!txtSubtitleIndex.getText().isEmpty()) AllArgs.add(txtSubtitleIndex.getText());
        if (!TeletextSubtitlesParam.isEmpty()) AllArgs.add(TeletextSubtitlesParam);
        if (!txtTextSubtitleIndex.getText().isEmpty()) AllArgs.add(txtTextSubtitleIndex.getText());
        AllArgs.add("-s");
        AllArgs.add(Integer.toString(SampleRate));
        // Only add gain for HackRF or SoapySDR
        if (txtGain.isEnabled()) {
            AllArgs.add("-g");
            AllArgs.add(txtGain.getText());
        }
        // Optional values second, see if they're defined first before adding
        if (!ChIDParam.isEmpty()) {AllArgs.add(ChIDParam);}
        if (!ChID.isEmpty()) {AllArgs.add(ChID);}
        if (!AudioParam.isEmpty()) AllArgs.add(AudioParam);
        if (!NICAMParam.isEmpty()) AllArgs.add(NICAMParam);
        if (!ACPParam.isEmpty()) AllArgs.add(ACPParam);
        if (!RepeatParam.isEmpty()) AllArgs.add(RepeatParam);
        if (!WssParam.isEmpty()) AllArgs.add(WssParam);
        if (!WssMode.isEmpty()) AllArgs.add(WssMode);
        if (!ScramblingType1.isEmpty()) AllArgs.add(ScramblingType1);
        if (!ScramblingKey1.isEmpty()) AllArgs.add(ScramblingKey1);
        if (!ScramblingType2.isEmpty()) AllArgs.add(ScramblingType2);
        if (!ScramblingKey2.isEmpty()) AllArgs.add(ScramblingKey2);
        if (!ScrambleAudio.isEmpty()) AllArgs.add(ScrambleAudio);
        if (!SysterPermTable.isEmpty()) AllArgs.add(SysterPermTable);
        if (!TeletextParam.isEmpty()) AllArgs.add(TeletextParam);
        if (!TeletextSource.isEmpty()) AllArgs.add(TeletextSource);
        if (!RFampParam.isEmpty()) AllArgs.add(RFampParam);
        if (!FMDevParam.isEmpty()) AllArgs.add(FMDevParam);
        if (!AntennaParam.isEmpty()) AllArgs.add(AntennaParam);
        if (!AntennaName.isEmpty()) AllArgs.add(AntennaName);
        if (chkFMDev.isSelected()) AllArgs.add(Integer.toString(FMDevValue));
        if (!GammaParam.isEmpty()) AllArgs.add(GammaParam);
        if (!txtGamma.getText().isEmpty()) AllArgs.add(txtGamma.getText());
        if (!OutputLevelParam.isEmpty()) AllArgs.add(OutputLevelParam);
        if (!txtOutputLevel.getText().isEmpty()) AllArgs.add(txtOutputLevel.getText());
        if (!FilterParam.isEmpty()) AllArgs.add(FilterParam);
        if (!PositionParam.isEmpty()) AllArgs.add(PositionParam);
        if (!txtPosition.getText().isEmpty()) AllArgs.add(txtPosition.getText());
        if (!TimestampParam.isEmpty()) AllArgs.add(TimestampParam);
        if (!LogoParam.isEmpty()) AllArgs.add(LogoParam);
        if (!LogoFileName.isEmpty()) AllArgs.add(LogoFileName);
        if (!VerboseParam.isEmpty()) AllArgs.add(VerboseParam);
        if (!EMMParam.isEmpty()) AllArgs.add(EMMParam);
        if (!TruncatedCardNumber.isEmpty()) {AllArgs.add(TruncatedCardNumber);}
        if (!ShowECMParam.isEmpty()) AllArgs.add(ShowECMParam);
        if (!ScalingMode.isEmpty()) AllArgs.add(ScalingMode);
        if (!InterlaceParam.isEmpty()) AllArgs.add(InterlaceParam);
        if (!ShowCardSerial.isEmpty()) AllArgs.add(ShowCardSerial);
        if (!FindKey.isEmpty()) AllArgs.add(FindKey);
        if (!VITS.isEmpty()) AllArgs.add(VITS);
        if (!ColourParam.isEmpty()) AllArgs.add(ColourParam);
        if (!FileType.isEmpty()) AllArgs.add(FileType);
        if (!VolumeParam.isEmpty()) AllArgs.add(VolumeParam);
        if (!txtVolume.getText().isEmpty()) AllArgs.add(txtVolume.getText());
        if (!DownmixParam.isEmpty()) AllArgs.add(DownmixParam);        
        // Finally, add the source video or test option.
        if (RunningOnWindows) {
            // If it's a local path, add quotes to it, but don't for the test 
            // card or a HTTP stream.
            if ( (InputSource.contains("test:")) ||
                (InputSource.startsWith("http")) ) {
                AllArgs.add(InputSource);
            }
            else {
                AllArgs.add('\"' + InputSource + '\"');
            }
        } else {
            // Don't add quotes on Unix systems as this just messes things up
            AllArgs.add(InputSource);
        }
        // End add to arraylist
        
        // Arguments textbox handling - clear it first
        if (!txtAllOptions.getText().isEmpty()) { txtAllOptions.setText(""); }
        /* Start a for loop to populate the textbox, using the arraylist size as
           the finish value.
        */
        for (int i = 1; i < AllArgs.size() ; i++) {
            /* Add value 1 (mode) first and then add all other values. I've set 
               it up this way to prevent a leading space from being printed
               in the textbox.
            */
            if (i == 1) { 
                txtAllOptions.setText(AllArgs.get(i)); 
            }
            else {
                txtAllOptions.setText(
                    txtAllOptions.getText() + '\u0020' + AllArgs.get(i) );
            }
        }
        // If "Generate syntax only" is enabled, stop here
        if (chkSyntaxOnly.isSelected()) {
            // Clear the ArrayList so we can run again with a fresh set
            AllArgs.clear();
            return;
        }
        // Change the Run button to say Stop instead
        changeRunToStop();
        // Clear the console
        txtConsoleOutput.setText("");
        // Spawn a new SwingWorker to run hacktv
        SwingWorker <Void, String> runTV = new SwingWorker <Void, String> () {
            @Override
            protected Void doInBackground() {
                // Create process with the ArrayList we populated above
                ProcessBuilder pb = new ProcessBuilder(AllArgs);
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
                // Revert button to say Run instead of Stop
                changeStopToRun();
                // Clear the ArrayList so we can run again with a fresh set
                AllArgs.clear();                
            }
            // Update the GUI from this method.
            @Override
            protected void process(List<String> chunks) {
                // Here we receive the values from publish() and display
                // them in the console
                for (String o : chunks) {
                    txtConsoleOutput.append(o);
                }
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
            }// End of process
        }; // End of SwingWorker
        runTV.execute();
    }
    
    private void stopTV() {
        /** To stop hacktv gracefully, it needs to be sent a SIGINT signal.
         *  Under Unix/POSIX systems this is easy, just run kill -2 and the PID.
         *  Under Windows it's not so easy, we need an external helper
         *  application. For this, we use:
         *  https://github.com/alirdn/windows-kill/releases
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
        if (DownloadInProgress.equals(true)) { DownloadCancelled = true; }
        // Check if hacktv is running, if so then exit it
        if (Running) stopTV();
        // Delete temp directory and files before exit
        if (TempDir != null) {
            try {
                deleteFSObject(TempDir.resolve(TempDir));
            } catch (IOException ex) {
                System.out.println("An error occurred while attempting to delete the temp directory: " + ex);
            }
        }
    }

    private void btnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunActionPerformed
        if (!Running) {
            if ( (!Files.exists(Path.of(HackTVPath))) || ((HackTVPath == "")) ) {
                JOptionPane.showMessageDialog(null, "Unable to find hacktv. Please go to the GUI settings tab to add its location.", AppName, JOptionPane.ERROR_MESSAGE);
                tabPane.setSelectedIndex(4);
            }
            else {
                runHackTV();
            }
        } else {
            stopTV();
        }
    }//GEN-LAST:event_btnRunActionPerformed
             
    private void menuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAboutActionPerformed
        JOptionPane.showMessageDialog(null, AppName + " (Java version)\nCreated 2020-2021 by Stephen McGarry.\n" +
                "Provided under the terms of the General Public Licence (GPL) v2 or later.\n\n" +
                "https://github.com/steeviebops/jhacktv-gui\n\n", "About " + AppName, JOptionPane.INFORMATION_MESSAGE);
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
                if (!"".contains(ScramblingKey2)) {
                    ScramblingType2 = "--eurocrypt";
                }
                else {
                    ScramblingType2 = "";
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
            SelectedFile = teletextFileChooser.getSelectedFile();
            txtTeletextSource.setText(stripQuotes(SelectedFile.getAbsolutePath()));
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
            if (!FMSampleRate.isEmpty()) txtSampleRate.setText(FMSampleRate);
        }
        else {
            FilterParam = "";
            if (!FMSampleRate.isEmpty()) {
                if ( ScramblingType1.equals("--videocrypt") || ScramblingType1.equals("--videocrypt2") ) {
                    txtSampleRate.setText("14");
                } else {
                    txtSampleRate.setText(DefaultSampleRate);
                }
            }
        }
    }//GEN-LAST:event_chkVideoFilterActionPerformed

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
        }
        else {
            if (NICAMSupported = true) {
                NICAMParam = "--nonicam";
            }
        }
    }//GEN-LAST:event_chkNICAMActionPerformed

    private void chkAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAudioActionPerformed
        if (chkAudio.isSelected()) {
            if (NICAMSupported == true) {
                chkNICAM.setEnabled(true);
                chkNICAM.doClick();
            }
            AudioParam = "";
        }
        else {
            chkNICAM.setSelected(false);
            chkNICAM.setEnabled(false);
            AudioParam = "--noaudio";
        }
    }//GEN-LAST:event_chkAudioActionPerformed

    private void radMACActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMACActionPerformed
        // Configures features supported (or not) by MAC video formats and
        // populates the VideoFormat combobox.
        String[] VideoMode = {
            "D2-MAC (625 lines, 25 fps, AM, digital audio)",
            "D2-MAC (625 lines, 25 fps, FM, digital audio)",
            "D-MAC (625 lines, 25 fps, AM, digital audio)",
            "D-MAC (625 lines, 25 fps, FM, digital audio)",
            "Baseband D2-MAC (625 lines, 25 fps)",
            "Baseband D-MAC (625 lines, 25 fps)"
        };
        /* Populate VideoModeArray with the parameters for each of the modes above.
        When we read the selected index of the combobox, we will use that index
        to query the array for the parameter that we need. */
        VideoModeArray = new String[] {
            "d2mac-am",
            "d2mac-fm",
            "dmac-am",
            "dmac-fm",
            "d2mac",
            "dmac"
        };
        // If scrambling is enabled, disable it first
        cmbScramblingType.setSelectedIndex(0);
        commonMACFeatures();
        cmbVideoFormat.removeAllItems();
        cmbVideoFormat.setModel(new DefaultComboBoxModel<>(VideoMode));
        cmbVideoFormat.setSelectedIndex(0);
        // End populate
    }//GEN-LAST:event_radMACActionPerformed

    private void radBWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radBWActionPerformed
        // Configures features supported (or not) by legacy black and white video
        // formats and populates the VideoFormat combobox.
        String[] VideoMode = {
            "CCIR System A (405 lines, 25 fps, -3.5 MHz AM audio)",
            "CCIR System E (819 lines, 25 fps, +11.15 MHz AM audio)",
            "Baird mechanical (240 lines, 25 fps)",
            "Baird mechanical (30 lines, 12.5 fps)",
            "Apollo (320 lines, 10 fps, FM)",
            "NBTV Club standard (32 lines, 12.5 fps, no audio)",
            "Baseband 405 lines, 25 fps",
            "Baseband 819 lines, 25 fps",
            "Baseband Baird 240 lines, 25 fps",
            "Baseband Baird 30 lines, 12.5 fps",
            "Baseband Apollo (320 lines, 10 fps)",
            "Baseband NBTV Club standard (32 lines, 12.5 fps)"
        };
        /* Populate VideoModeArray with the parameters for each of the modes above.
        When we read the selected index of the combobox, we will use that index
        to query the array for the parameter that we need. */
        VideoModeArray = new String[] {
            "a",
            "e",
            "240-am",
            "30-am",
            "apollo-fm",
            "nbtv-am",
            "405",
            "819",
            "240",
            "30",
            "apollo",
            "nbtv"
        };
        commonBWFeatures();
        cmbVideoFormat.removeAllItems();
        cmbVideoFormat.setModel(new DefaultComboBoxModel<>(VideoMode));
        cmbVideoFormat.setSelectedIndex(0);
        // End populate
    }//GEN-LAST:event_radBWActionPerformed

    private void radSECAMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radSECAMActionPerformed
        // Configures features supported (or not) by SECAM video formats and
        // populates the VideoFormat combobox.
        String[] VideoMode = {
            "SECAM-L (625 lines, 25 fps, 6.5 MHz AM audio)",
            "SECAM-D/K (625 lines, 25 fps, 6.5 MHz FM audio)",
            "SECAM-FM (625 lines, 25 fps, 6.5 MHz FM audio)",
            "Baseband SECAM (625 lines, 25 fps)"
        };
        /* Populate VideoModeArray with the parameters for each of the modes above.
        When we read the selected index of the combobox, we will use that index
        to query the array for the parameter that we need. */
        VideoModeArray = new String[] {
            "l",
            "d",
            "secam-fm",
            "secam"
        };
        cmbVideoFormat.removeAllItems();
        cmbVideoFormat.setModel(new DefaultComboBoxModel<>(VideoMode));
        cmbVideoFormat.setSelectedIndex(0);
        // End populate
    }//GEN-LAST:event_radSECAMActionPerformed

    private void radNTSCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radNTSCActionPerformed
        // Configures features supported (or not) by NTSC video formats and
        // populates the VideoFormat combobox.
        String[] VideoMode = {
            "NTSC-M (525 lines, 29.97 fps, 4.5 MHz FM audio)",
            "NTSC-FM (525 lines, 29.97 fps, 6.5 MHz FM audio)",
            "NTSC-FM BS (525 lines, 29.97 fps, BS digital audio)",
            "Apollo Field Sequential Color (525 lines, 29.97 fps)",
            "CBS Field Sequential Color (405 lines, 72 fps)",
            "Baseband NTSC (525 lines, 30 fps)",
            "Baseband CBS FSC (405 lines, 72 fps)"
        };
        /* Populate VideoModeArray with the parameters for each of the modes above.
        When we read the selected index of the combobox, we will use that index
        to query the array for the parameter that we need. */
        VideoModeArray = new String[] {
            "m",
            "ntsc-fm",
            "ntsc-bs",
            "apollo-fsc-fm",
            "m-cbs405",
            "ntsc",
            "cbs405"
        };
        cmbVideoFormat.removeAllItems();
        cmbVideoFormat.setModel(new DefaultComboBoxModel<>(VideoMode));
        cmbVideoFormat.setSelectedIndex(0);
        // End populate
    }//GEN-LAST:event_radNTSCActionPerformed

    private void radPALActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radPALActionPerformed
        // Configures features supported (or not) by PAL video formats and populates
        // the VideoFormat combobox.
        String[] VideoMode = {
            "PAL-I (625 lines, 25 fps, 6.0 MHz FM audio)",
            "PAL-B/G (625 lines, 25 fps, 5.5 MHz FM audio)",
            "PAL-FM (625 lines, 25 fps, 6.5 MHz FM audio)",
            "PAL-M (525 lines, 30 fps, 4.5 MHz FM audio)",
            "Baseband PAL (625 lines, 25 fps)",
            "Baseband PAL (525 lines, 30 fps)"
        };
        /* Populate VideoModeArray with the parameters for each of the modes above.
        When we read the selected index of the combobox, we will use that index
        to query the array for the parameter that we need. */
        VideoModeArray = new String[] {
            "i",
            "g",
            "pal-fm",
            "pal-m",
            "pal",
            "525pal"
        };
        cmbVideoFormat.removeAllItems();
        cmbVideoFormat.setModel(new DefaultComboBoxModel<>(VideoMode));
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
            File file = sourceFileChooser.getSelectedFile();
            file = new File (stripQuotes(file.toString()));
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
        if ((Fork == "CJ") && (Lines == 625)) {
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
            // Set BSB channel ID
            if ( Sys.equals("dmac-fm") && (cmbChannel.getSelectedIndex() <= 4) ) {
                for(int i = 1; i <= 5; i++) {
                    if ((cmbChannel.getSelectedIndex()) == i-1) {
                        if (!chkMacChId.isSelected()) { chkMacChId.doClick() ;}
                        txtMacChId.setText("00B" + i);
                    }
                }
            } else if ( Sys.equals("dmac-fm") && (cmbChannel.getSelectedIndex() > 4) ) {
                if (chkMacChId.isSelected()) { chkMacChId.doClick() ;}
            }
        }
    }//GEN-LAST:event_cmbChannelActionPerformed

    private void txtFrequencyKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFrequencyKeyTyped
        if(txtFrequency.getText().length()>9) {
            evt.consume();
        }
    }//GEN-LAST:event_txtFrequencyKeyTyped

    private void radUHFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radUHFActionPerformed
        txtFrequency.setEditable(false);
        cmbChannel.setEnabled(true);
        if ((Sys).equals("i")) { addEuropeUHFChannels(); }
        if ((Sys).equals("g")) { addEuropeUHFChannels(); }
        if ((Sys).equals("m")) { addNTSCUHFChannels();}
        if ((Sys).equals("l")) { addEuropeUHFChannels(); }
        if ((Sys).equals("d")) { addEuropeUHFChannels(); }
        if ((Sys).equals("pal-m")) { addNTSCUHFChannels(); }
        if ((Sys).equals("dmac-fm")) { addBSBChannels(); }
        if ((Sys).equals("m-cbs405")) { addNTSCUHFChannels(); }
    }//GEN-LAST:event_radUHFActionPerformed

    private void radVHFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radVHFActionPerformed
        txtFrequency.setEditable(false);
        cmbChannel.setEnabled(true);
        if ((Sys).equals("i")) { addIrishVHFChannels(); }
        if ((Sys).equals("g")) { addSysBVHFChannels(); }
        if ((Sys).equals("m")) { addNTSCVHFChannels();}
        if ((Sys).equals("l")) { addFrenchVHFChannels(); }
        if ((Sys).equals("d")) { addSystemDRussiaChannels(); }
        if ((Sys).equals("a")) { addSystemAChannels(); }
        if ((Sys).equals("e")) { addSystemEChannels(); }
        if ((Sys).equals("pal-m")) { addNTSCVHFChannels(); }
        if ((Sys).equals("m-cbs405")) { addNTSCVHFChannels(); }
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
            cmbVideoFormat.setSelectedIndex(3);
            // Enable pre-emphasis filter and set FM deviation to 16 MHz
            chkVideoFilter.doClick();
            chkFMDev.doClick();
            txtFMDev.setText("11");
            // Set IF to Galaxy channel
            cmbChannel.setSelectedIndex(1);
            JOptionPane.showMessageDialog(null, "Template values have been loaded. Tune your receiver to the Galaxy "
                    + "channel, or change this in the channel dropdown box on the Output tab.", AppName, JOptionPane.INFORMATION_MESSAGE);            
        }
    }//GEN-LAST:event_menuBSBTemplateActionPerformed

    private void btnHackTVPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHackTVPathActionPerformed
        hacktvFileChooser.setAcceptAllFileFilterUsed(true);        
        int returnVal = hacktvFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = hacktvFileChooser.getSelectedFile();
            HackTVPath = stripQuotes(file.toString());
            txtHackTVPath.setText(HackTVPath);
            // Store the specified path.
            Prefs.put("HackTVPath", HackTVPath);
            // Load the full path to a variable so we can use getParent on it
            // and get its parent directory path
            HackTVDirectory = new File(HackTVPath).getParent();
            // Detect what were provided with
            detectFork();
        }
    }//GEN-LAST:event_btnHackTVPathActionPerformed

    private void menuOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenActionPerformed
        int result = configFileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            SelectedFile = configFileChooser.getSelectedFile();
            SelectedFile = new File(stripQuotes(SelectedFile.toString()));
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
            RunButtonText = btnRun.getText();
            btnRun.setText("Generate syntax");
        }
        else {
            btnRun.setText(RunButtonText);
        }
    }//GEN-LAST:event_chkSyntaxOnlyActionPerformed

    private void lblSyntaxOptionDisabledMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSyntaxOptionDisabledMouseClicked
        // Show a message to explain why the syntax option is disabled
        JOptionPane.showMessageDialog(null, "A helper application (windows-kill.exe) is required when running this application on Windows.\n"
                + "You can download it from https://github.com/alirdn/windows-kill/releases/\n"
                + "Please save it in the same directory as this application and restart.", AppName, JOptionPane.INFORMATION_MESSAGE);
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
                OutputDevice = ""; // can also use -ohackrf
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
                        SelectedFile = outputFileChooser.getSelectedFile();
                        txtOutputDevice.setText(SelectedFile.toString());
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
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AdditionalOptionsPanel;
    private javax.swing.ButtonGroup BandButtonGroup;
    private javax.swing.JPanel FrequencyPanel;
    private javax.swing.ButtonGroup SourceButtonGroup;
    private javax.swing.JPanel SourcePanel;
    private javax.swing.JPanel VBIPanel;
    private javax.swing.ButtonGroup VideoFormatButtonGroup;
    private javax.swing.JPanel VideoFormatPanel;
    private javax.swing.JButton btnClearMRUList;
    private javax.swing.JButton btnHackTVPath;
    private javax.swing.JButton btnResetAllSettings;
    private javax.swing.JButton btnRun;
    private javax.swing.JButton btnSourceBrowse;
    private javax.swing.JButton btnSpark;
    private javax.swing.JButton btnTeefax;
    private javax.swing.JButton btnTeletextBrowse;
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
    private javax.swing.JCheckBox chkLogo;
    private javax.swing.JCheckBox chkMacChId;
    private javax.swing.JCheckBox chkNICAM;
    private javax.swing.JCheckBox chkOutputLevel;
    private javax.swing.JCheckBox chkPosition;
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
