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

import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import java.text.DecimalFormat;
import java.io.IOException;
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
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.swing.SwingWorker;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import java.nio.file.InvalidPathException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import javax.imageio.ImageIO;
import javax.swing.KeyStroke;

public class GUI extends javax.swing.JFrame {    
    // Application name
    public static final String APP_NAME = "hacktv-gui";
    
    // Pseudo-random number generator, used for the Randomise playlist option
    private static final Random RND = new Random();
    
    // Boolean used for Microsoft Windows detection and handling
    private boolean runningOnWindows;
    
    // Look and feel arraylist
    private ArrayList<String> lafAL;
    int defaultLaf;
    
    // String to set the directory where this application's JAR is located
    private Path jarDir;
    
    // Strings to set the location and contents of the modes file
    private String modesFilePath;
    private String modesFile;
    private String modesFileVersion;
    private String modesFileLocation;
    private String bpFilePath;
    private String bpFile;
    private String bpFileVersion;
    private String bpFileLocation;
    
    // Declare variables for supported features
    private boolean nicamSupported = false;
    private boolean a2Supported = false;
    private boolean acpSupported = false;

    // Declare a variable to determine the selected fork
    private boolean captainJack;
    private boolean mattstvbarn;

    // Declare Teletext-related variables that are reused across multiple subs
    private String htmlTempFile;
    private String htmlFile;
    private File selectedFile;
    private Path tempDir;
    private String teletextPath;
    private boolean downloadInProgress = false;
    private boolean downloadCancelled = false;

    // Declare variables used for path resolution
    private String hackTVPath;
    private String hackTVDirectory;
    private String defaultHackTVPath;

    // Declare variable for the title bar display
    private String titleBar;
    private boolean titleBarChanged = false;

    // Array used for M3U files
    private final ArrayList<String> playlistURLsAL = new ArrayList<>();
    private String[] playlistNames;

    // Declare a variable for storing the default sample rate for the selected video mode
    // This allows us to revert back to the default if the sample rate is changed by filters or scrambling systems
    private String defaultSampleRate;
    
    // Declare combobox arrays and ArrayLists
    // These are used to store secondary information (frequencies, parameters, etc)
    private long[] frequencyArray;
    private String[] palModeArray;
    private String[] ntscModeArray;
    private String[] secamModeArray;
    private String[] otherModeArray;
    private String[] macModeArray;
    private String[] channelArray;
    private ArrayList<String> scramblingTypeArray;
    private ArrayList<String> scramblingKeyArray;
    private ArrayList<String> scramblingKey2Array;
    private String[] logoArray;
    private String[] tcArray;
    private final ArrayList<String> playlistAL = new ArrayList<>();
    private ArrayList<String> uhfAL;
    private ArrayList<String> vhfAL;

    // Checkbox array for the File > New option
    private javax.swing.JCheckBox[] checkBoxes;
    
    // Preferences node
    public static final Preferences PREFS = Preferences.userNodeForPackage(GUI.class);
    
    // Process ID, used to gracefully close hacktv via the Stop button
    private long hpid;
    
    // Boolean to determine if hacktv is running or not
    private boolean running;
    
    // Boolean to determine if a config file is in the process of loading
    private boolean htvLoadInProgress = false;
    
    // Video line count, used for enabling/disabling certain features
    private int lines;  
    
    // Integer to save the previously selected item in the Mode combobox.
    // Used to revert back if a baseband mode is selected on an unsupported SDR.
    private int previousIndex = 0;
    
    // Allows us to recall the previously selected colour system
    private String prevColour = "";
    
    // Start point in playlist
    private int startPoint = -1;
    
    // Declare variables used for storing parameters
    private String mode = "";
    private long frequency;
    private String scramblingType1 = "";
    private String scramblingKey1 = "";
    private String scramblingType2 = "";
    private String scramblingKey2 = "";
    private int dualVC;
    private boolean sat;
    
    // Default LNB local oscillator frequency in GHz
    public static final double DEFAULT_LO = 9.75;
    
    // Class instances
    final Shared SharedInst = new Shared();
    final INIFile INI = new INIFile();

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sourceButtonGroup = new javax.swing.ButtonGroup();
        modeButtonGroup = new javax.swing.ButtonGroup();
        bandButtonGroup = new javax.swing.ButtonGroup();
        macStereoButtonGroup = new javax.swing.ButtonGroup();
        macSRButtonGroup = new javax.swing.ButtonGroup();
        macCompressionButtonGroup = new javax.swing.ButtonGroup();
        macProtectionButtonGroup = new javax.swing.ButtonGroup();
        templateButtonGroup = new javax.swing.ButtonGroup();
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
        hacktvFileChooser.setFileFilter(createFileFilter());
        outputFileChooser = new JFileChooser();
        outputFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        containerPanel = new javax.swing.JPanel();
        consoleOutputPanel = new javax.swing.JPanel();
        consoleScrollPane = new javax.swing.JScrollPane();
        txtConsoleOutput = new javax.swing.JTextArea();
        tabPane = new javax.swing.JTabbedPane();
        sourceTab = new javax.swing.JPanel();
        sourcePanel = new javax.swing.JPanel();
        sourceSelectPanel = new javax.swing.JPanel();
        radLocalSource = new javax.swing.JRadioButton();
        radTest = new javax.swing.JRadioButton();
        cmbTest = new javax.swing.JComboBox<>();
        btnTestSettings = new javax.swing.JButton();
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
        modeTab = new javax.swing.JPanel();
        modePanel = new javax.swing.JPanel();
        modeButtonPanel = new javax.swing.JPanel();
        radPAL = new javax.swing.JRadioButton();
        radNTSC = new javax.swing.JRadioButton();
        radSECAM = new javax.swing.JRadioButton();
        radBW = new javax.swing.JRadioButton();
        radMAC = new javax.swing.JRadioButton();
        cmbMode = new javax.swing.JComboBox<>();
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
        chkOffset = new javax.swing.JCheckBox();
        txtOffset = new javax.swing.JTextField();
        chkSwapIQ = new javax.swing.JCheckBox();
        chkSVideo = new javax.swing.JCheckBox();
        macPanel = new javax.swing.JPanel();
        chkMacChId = new javax.swing.JCheckBox();
        txtMacChId = new javax.swing.JTextField();
        chkMacMono = new javax.swing.JCheckBox();
        chkMac16k = new javax.swing.JCheckBox();
        chkMacLinear = new javax.swing.JCheckBox();
        chkMacL2 = new javax.swing.JCheckBox();
        vbiPanel = new javax.swing.JPanel();
        chkVITS = new javax.swing.JCheckBox();
        chkACP = new javax.swing.JCheckBox();
        chkWSS = new javax.swing.JCheckBox();
        cmbWSS = new javax.swing.JComboBox<>();
        chkVITC = new javax.swing.JCheckBox();
        chkSecamId = new javax.swing.JCheckBox();
        cmbSecamIdLines = new javax.swing.JComboBox<>();
        chkCC608 = new javax.swing.JCheckBox();
        outputTab = new javax.swing.JPanel();
        additionalOptionsPanel = new javax.swing.JPanel();
        chkGamma = new javax.swing.JCheckBox();
        txtGamma = new javax.swing.JTextField();
        chkOutputLevel = new javax.swing.JCheckBox();
        txtOutputLevel = new javax.swing.JTextField();
        chkVerbose = new javax.swing.JCheckBox();
        chkVolume = new javax.swing.JCheckBox();
        chkDownmix = new javax.swing.JCheckBox();
        txtVolume = new javax.swing.JTextField();
        chkSiS = new javax.swing.JCheckBox();
        frequencyPanel = new javax.swing.JPanel();
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
        chkHackDAC = new javax.swing.JCheckBox();
        lblFl2kAudio = new javax.swing.JLabel();
        cmbFl2kAudio = new javax.swing.JComboBox<>();
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
        chkNoUpdateCheck = new javax.swing.JCheckBox();
        btnSatSettings = new javax.swing.JButton();
        txtStatus = new javax.swing.JTextField();
        runButtonGrid = new javax.swing.JPanel();
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
        menuAstraTemplate = new javax.swing.JMenuItem();
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
            .addComponent(consoleScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
        );
        consoleOutputPanelLayout.setVerticalGroup(
            consoleOutputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(consoleScrollPane)
        );

        sourcePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Source options"));

        sourceSelectPanel.setLayout(new java.awt.GridBagLayout());

        sourceButtonGroup.add(radLocalSource);
        radLocalSource.setText("Local or internet source");
        radLocalSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radLocalSourceActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        sourceSelectPanel.add(radLocalSource, gridBagConstraints);

        sourceButtonGroup.add(radTest);
        radTest.setText("Test card");
        radTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radTestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        sourceSelectPanel.add(radTest, gridBagConstraints);

        cmbTest.setEnabled(false);
        cmbTest.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbTestMouseWheelMoved(evt);
            }
        });
        cmbTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        sourceSelectPanel.add(cmbTest, gridBagConstraints);

        btnTestSettings.setText("Settings...");
        btnTestSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestSettingsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 10);
        sourceSelectPanel.add(btnTestSettings, gridBagConstraints);

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

        txtPosition.setEditable(false);
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

        txtSubtitleIndex.setEditable(false);
        txtSubtitleIndex.setEnabled(false);
        txtSubtitleIndex.addMouseListener(new ContextMenuListener());
        txtSubtitleIndex.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtSubtitleIndexKeyTyped(evt);
            }
        });

        lblSubtitleIndex.setText("Index (optional)");
        lblSubtitleIndex.setEnabled(false);

        chkARCorrection.setText("Aspect ratio scaling");
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

        lstPlaylist.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.disabledBackground"));
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

        javax.swing.GroupLayout sourcePanelLayout = new javax.swing.GroupLayout(sourcePanel);
        sourcePanel.setLayout(sourcePanelLayout);
        sourcePanelLayout.setHorizontalGroup(
            sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sourcePanelLayout.createSequentialGroup()
                        .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkRepeat)
                            .addComponent(chkTimestamp)
                            .addComponent(chkInterlace)
                            .addComponent(chkRandom))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 75, Short.MAX_VALUE)
                        .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(sourcePanelLayout.createSequentialGroup()
                                .addComponent(chkSubtitles)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 116, Short.MAX_VALUE)
                                .addComponent(lblSubtitleIndex)
                                .addGap(18, 18, 18)
                                .addComponent(txtSubtitleIndex, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(sourcePanelLayout.createSequentialGroup()
                                .addComponent(chkPosition)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourcePanelLayout.createSequentialGroup()
                                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chkARCorrection)
                                    .addComponent(chkLogo))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cmbLogo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbARCorrection, 0, 148, Short.MAX_VALUE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourcePanelLayout.createSequentialGroup()
                        .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(playlistScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
                            .addGroup(sourcePanelLayout.createSequentialGroup()
                                .addComponent(txtSource, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbM3USource, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(6, 6, 6)
                        .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnRemove, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnPlaylistUp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnPlaylistDown, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnAdd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnPlaylistStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSourceBrowse, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(sourcePanelLayout.createSequentialGroup()
                        .addComponent(sourceSelectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        sourcePanelLayout.setVerticalGroup(
            sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourcePanelLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(sourceSelectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbM3USource)
                    .addComponent(txtSource)
                    .addComponent(btnSourceBrowse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(sourcePanelLayout.createSequentialGroup()
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
                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkSubtitles)
                    .addComponent(txtSubtitleIndex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSubtitleIndex)
                    .addComponent(chkRandom))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkPosition)
                    .addComponent(txtPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkRepeat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkLogo)
                    .addComponent(cmbLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkInterlace))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
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
                .addComponent(sourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        sourceTabLayout.setVerticalGroup(
            sourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sourcePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabPane.addTab("Source", sourceTab);

        modePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Video mode options"));

        modeButtonPanel.setLayout(new java.awt.GridLayout(1, 0));

        modeButtonGroup.add(radPAL);
        radPAL.setText("PAL");
        radPAL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radPALActionPerformed(evt);
            }
        });
        modeButtonPanel.add(radPAL);

        modeButtonGroup.add(radNTSC);
        radNTSC.setText("NTSC");
        radNTSC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radNTSCActionPerformed(evt);
            }
        });
        modeButtonPanel.add(radNTSC);

        modeButtonGroup.add(radSECAM);
        radSECAM.setText("SECAM");
        radSECAM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radSECAMActionPerformed(evt);
            }
        });
        modeButtonPanel.add(radSECAM);

        modeButtonGroup.add(radBW);
        radBW.setText("Black and white");
        radBW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radBWActionPerformed(evt);
            }
        });
        modeButtonPanel.add(radBW);

        modeButtonGroup.add(radMAC);
        radMAC.setText("MAC");
        radMAC.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        radMAC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radMACActionPerformed(evt);
            }
        });
        modeButtonPanel.add(radMAC);

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

        lblSampleRate.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblSampleRate.setText("Sample rate (MHz)");

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

        txtPixelRate.setEditable(false);
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

        txtFMDev.setEditable(false);
        txtFMDev.setEnabled(false);
        txtFMDev.addMouseListener(new ContextMenuListener());
        txtFMDev.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtFMDevKeyTyped(evt);
            }
        });

        chkInvertVideo.setText("Invert video polarity");

        chkOffset.setText("Offset (MHz)");
        chkOffset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkOffsetActionPerformed(evt);
            }
        });

        txtOffset.setEditable(false);
        txtOffset.setEnabled(false);
        txtOffset.addMouseListener(new ContextMenuListener());
        txtOffset.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtOffsetKeyTyped(evt);
            }
        });

        chkSwapIQ.setText("Swap I and Q samples");

        chkSVideo.setText("S-Video");
        chkSVideo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSVideoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout modePanelLayout = new javax.swing.GroupLayout(modePanel);
        modePanel.setLayout(modePanelLayout);
        modePanelLayout.setHorizontalGroup(
            modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbMode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(modeButtonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, modePanelLayout.createSequentialGroup()
                        .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkAudio)
                            .addComponent(chkNICAM)
                            .addComponent(chkA2Stereo)
                            .addComponent(chkColour)
                            .addComponent(chkSwapIQ)
                            .addComponent(chkInvertVideo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkSVideo)
                            .addGroup(modePanelLayout.createSequentialGroup()
                                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(modePanelLayout.createSequentialGroup()
                                        .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(chkPixelRate)
                                            .addComponent(lblSampleRate))
                                        .addGap(74, 74, 74))
                                    .addGroup(modePanelLayout.createSequentialGroup()
                                        .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(chkVideoFilter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(modePanelLayout.createSequentialGroup()
                                                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(chkFMDev)
                                                    .addComponent(chkOffset))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtOffset)
                                    .addComponent(txtPixelRate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                                    .addComponent(txtSampleRate, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtFMDev, javax.swing.GroupLayout.Alignment.TRAILING))))))
                .addContainerGap())
        );
        modePanelLayout.setVerticalGroup(
            modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modePanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(modeButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSampleRate)
                    .addComponent(chkAudio)
                    .addComponent(txtSampleRate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPixelRate)
                    .addComponent(chkPixelRate)
                    .addComponent(chkNICAM))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkA2Stereo)
                    .addComponent(chkFMDev)
                    .addComponent(txtFMDev))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkColour)
                    .addComponent(chkOffset)
                    .addComponent(txtOffset))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkInvertVideo)
                    .addComponent(chkVideoFilter))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkSwapIQ)
                    .addComponent(chkSVideo))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        macPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("MAC options"));

        chkMacChId.setText("Override channel ID");
        chkMacChId.setEnabled(false);
        chkMacChId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMacChIdActionPerformed(evt);
            }
        });

        txtMacChId.setEditable(false);
        txtMacChId.setEnabled(false);
        txtMacChId.addMouseListener(new ContextMenuListener());
        txtMacChId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtMacChIdKeyTyped(evt);
            }
        });

        chkMacMono.setText("Mono audio");
        chkMacMono.setEnabled(false);

        chkMac16k.setText("16 kHz audio sample rate");
        chkMac16k.setEnabled(false);

        chkMacLinear.setText("Linear audio");
        chkMacLinear.setEnabled(false);

        chkMacL2.setText("Level 2 audio protection");
        chkMacL2.setEnabled(false);

        javax.swing.GroupLayout macPanelLayout = new javax.swing.GroupLayout(macPanel);
        macPanel.setLayout(macPanelLayout);
        macPanelLayout.setHorizontalGroup(
            macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(macPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(macPanelLayout.createSequentialGroup()
                        .addComponent(chkMacChId)
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addComponent(txtMacChId, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(macPanelLayout.createSequentialGroup()
                        .addGroup(macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkMacMono)
                            .addComponent(chkMac16k)
                            .addComponent(chkMacLinear)
                            .addComponent(chkMacL2))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        macPanelLayout.setVerticalGroup(
            macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(macPanelLayout.createSequentialGroup()
                .addGroup(macPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkMacChId)
                    .addComponent(txtMacChId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMacMono)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMac16k)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMacLinear)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMacL2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        vbiPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("VBI options"));

        chkVITS.setText("VITS test signal");

        chkACP.setText("Macrovision ACP");

        chkWSS.setText("Widescreen signalling (WSS)");
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

        chkSecamId.setText("SECAM field ID");
        chkSecamId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSecamIdActionPerformed(evt);
            }
        });

        cmbSecamIdLines.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        cmbSecamIdLines.setSelectedIndex(-1);
        cmbSecamIdLines.setEnabled(false);
        cmbSecamIdLines.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cmbSecamIdLinesMouseWheelMoved(evt);
            }
        });

        chkCC608.setText("Closed captions (CEA/EIA-608)");

        javax.swing.GroupLayout vbiPanelLayout = new javax.swing.GroupLayout(vbiPanel);
        vbiPanel.setLayout(vbiPanelLayout);
        vbiPanelLayout.setHorizontalGroup(
            vbiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(vbiPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(vbiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(vbiPanelLayout.createSequentialGroup()
                        .addGroup(vbiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(vbiPanelLayout.createSequentialGroup()
                                .addGroup(vbiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chkACP)
                                    .addComponent(chkVITS))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(vbiPanelLayout.createSequentialGroup()
                                .addComponent(chkSecamId)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbSecamIdLines, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(vbiPanelLayout.createSequentialGroup()
                                .addComponent(chkWSS)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbWSS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addGroup(vbiPanelLayout.createSequentialGroup()
                        .addGroup(vbiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkVITC)
                            .addComponent(chkCC608))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        vbiPanelLayout.setVerticalGroup(
            vbiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(vbiPanelLayout.createSequentialGroup()
                .addGroup(vbiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkWSS)
                    .addComponent(cmbWSS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkACP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkVITS)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkVITC)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(vbiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkSecamId)
                    .addComponent(cmbSecamIdLines, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkCC608)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout modeTabLayout = new javax.swing.GroupLayout(modeTab);
        modeTab.setLayout(modeTabLayout);
        modeTabLayout.setHorizontalGroup(
            modeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modeTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(modeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(modeTabLayout.createSequentialGroup()
                        .addComponent(vbiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(macPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        modeTabLayout.setVerticalGroup(
            modeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modeTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(modePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(macPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vbiPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabPane.addTab("Mode", modeTab);

        additionalOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Additional options"));

        chkGamma.setText("Gamma correction");
        chkGamma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkGammaActionPerformed(evt);
            }
        });

        txtGamma.setEditable(false);
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
        txtOutputLevel.setEditable(false);
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

        txtVolume.setEditable(false);
        txtVolume.setEnabled(false);
        txtVolume.addMouseListener(new ContextMenuListener());
        txtVolume.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtVolumeKeyTyped(evt);
            }
        });

        chkSiS.setText("Sound-in-Syncs");

        javax.swing.GroupLayout additionalOptionsPanelLayout = new javax.swing.GroupLayout(additionalOptionsPanel);
        additionalOptionsPanel.setLayout(additionalOptionsPanelLayout);
        additionalOptionsPanelLayout.setHorizontalGroup(
            additionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(additionalOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(additionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(additionalOptionsPanelLayout.createSequentialGroup()
                        .addGroup(additionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkGamma)
                            .addComponent(chkVolume))
                        .addGap(18, 18, 18)
                        .addGroup(additionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtOutputLevel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                            .addComponent(txtGamma, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtVolume, javax.swing.GroupLayout.Alignment.LEADING)))
                    .addComponent(chkOutputLevel))
                .addGap(18, 18, 18)
                .addGroup(additionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(chkDownmix, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkVerbose)
                    .addComponent(chkSiS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        additionalOptionsPanelLayout.setVerticalGroup(
            additionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(additionalOptionsPanelLayout.createSequentialGroup()
                .addGroup(additionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkGamma)
                    .addComponent(txtGamma, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkDownmix))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(additionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkOutputLevel)
                    .addComponent(txtOutputLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkSiS))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(additionalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkVolume)
                    .addComponent(chkVerbose))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        frequencyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Frequency and TX options"));

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

        bandButtonGroup.add(radCustom);
        radCustom.setText("Custom");
        radCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radCustomActionPerformed(evt);
            }
        });

        bandButtonGroup.add(radVHF);
        radVHF.setText("VHF");
        radVHF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radVHFActionPerformed(evt);
            }
        });

        bandButtonGroup.add(radUHF);
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
                                .addGap(0, 127, Short.MAX_VALUE))))
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
                .addGap(6, 6, 6)
                .addGroup(rfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radCustom)
                    .addComponent(radUHF)
                    .addComponent(radVHF)
                    .addComponent(cmbRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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

        chkHackDAC.setText("HackDAC");
        chkHackDAC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkHackDACActionPerformed(evt);
            }
        });

        lblFl2kAudio.setText("Audio mode");

        cmbFl2kAudio.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Stereo", "S/PDIF" }));

        javax.swing.GroupLayout frequencyPanelLayout = new javax.swing.GroupLayout(frequencyPanel);
        frequencyPanel.setLayout(frequencyPanelLayout);
        frequencyPanelLayout.setHorizontalGroup(
            frequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(frequencyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblOutputDevice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbOutputDevice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblOutputDevice2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtOutputDevice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFl2kAudio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbFl2kAudio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkHackDAC)
                .addGap(10, 10, 10))
            .addComponent(rfPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        frequencyPanelLayout.setVerticalGroup(
            frequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(frequencyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(frequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOutputDevice)
                    .addComponent(cmbOutputDevice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOutputDevice2)
                    .addComponent(txtOutputDevice)
                    .addComponent(chkHackDAC)
                    .addComponent(lblFl2kAudio)
                    .addComponent(cmbFl2kAudio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rfPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout outputTabLayout = new javax.swing.GroupLayout(outputTab);
        outputTab.setLayout(outputTabLayout);
        outputTabLayout.setHorizontalGroup(
            outputTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(outputTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(frequencyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(additionalOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        outputTabLayout.setVerticalGroup(
            outputTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(frequencyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(additionalOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(148, Short.MAX_VALUE))
        );

        tabPane.addTab("Output", outputTab);

        teletextPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Teletext options"));

        chkTeletext.setText("Enable teletext");
        chkTeletext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTeletextActionPerformed(evt);
            }
        });

        txtTeletextSource.setEditable(false);
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

        txtTextSubtitleIndex.setEditable(false);
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
                        .addGap(286, 425, Short.MAX_VALUE))
                    .addGroup(teletextPanelLayout.createSequentialGroup()
                        .addGroup(teletextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(teletextPanelLayout.createSequentialGroup()
                                .addComponent(txtTeletextSource, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
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
                        .addGap(0, 95, Short.MAX_VALUE)))
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

        txtCardNumber.setEditable(false);
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
                .addContainerGap(100, Short.MAX_VALUE))
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

        txtECprognum.setEditable(false);
        txtECprognum.setEnabled(false);
        txtECprognum.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtECprognumKeyTyped(evt);
            }
        });

        txtECprogcost.setEditable(false);
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
                .addContainerGap(99, Short.MAX_VALUE))
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
                            .addComponent(txtHackTVPath, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnDownloadHackTV, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
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
                .addContainerGap(196, Short.MAX_VALUE))
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

        chkLocalModes.setText("Always use local copy of modes files (do not download)");
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
        cmbLookAndFeel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cmbLookAndFeelMouseEntered(evt);
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

        chkNoUpdateCheck.setText("Do not check for updates on startup");
        chkNoUpdateCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNoUpdateCheckActionPerformed(evt);
            }
        });

        btnSatSettings.setText("Satellite receiver settings...");
        btnSatSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSatSettingsActionPerformed(evt);
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
                    .addComponent(chkSyntaxOnly)
                    .addComponent(chkLocalModes)
                    .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                        .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblLookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNMSCeefaxRegion))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbNMSCeefaxRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbLookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btnSatSettings))
                .addContainerGap(237, Short.MAX_VALUE))
        );
        generalSettingsPanelLayout.setVerticalGroup(
            generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                .addComponent(chkSyntaxOnly)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkLocalModes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkNoUpdateCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbNMSCeefaxRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNMSCeefaxRegion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLookAndFeel)
                    .addComponent(cmbLookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSatSettings)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGap(2, 2, 2)
                .addComponent(resetSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(54, Short.MAX_VALUE))
        );

        tabPane.addTab("GUI settings", settingsTab);

        txtStatus.setEditable(false);
        txtStatus.addMouseListener(new ContextMenuListener());

        runButtonGrid.setLayout(new java.awt.GridBagLayout());

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
        runButtonGrid.add(btnRun, gridBagConstraints);

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
                            .addComponent(runButtonGrid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(runButtonGrid, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        menuAstraTemplate.setText("Astra analogue STB...");
        menuAstraTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAstraTemplateActionPerformed(evt);
            }
        });
        templatesMenu.add(menuAstraTemplate);

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
                resetPreferences();
                return;                    
            }
        }
        // Pre-initialisation macOS tasks
        // These need to be done before creating the GUI class instance.
        // We'll set the dock icon later because that needs to be done after
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
            SwingUtilities.invokeLater(() -> {
                // Create GUI class instance
                final var g = new GUI();
                if (g.populateUI(args)) {
                    // Prevent window from being resized below the current size
                    g.setMinimumSize(g.getSize());
                    g.setVisible(true);
                }
                else {
                    System.exit(1);
                }                
            });
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
    
    private boolean populateUI(String[] args) {
        // Add a shutdown hook to run exit tasks
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cleanupBeforeExit();
            }
        });
        // Set application icons
        setIcons();
        // Set look and feel
        setLaf();
        // Initialise Swing components
        initComponents();
        // If the IntelliJ themes are installed, limit the size of the Theme combobox
        // This prevents the combobox from reducing the size of the console pane
        if (lafAL.get(lafAL.size() - 1).startsWith("com.formdev.flatlaf.intellijthemes")) {
            cmbLookAndFeel.setPreferredSize(new Dimension(166, cmbLookAndFeel.getSize().height));
        }
        // Set the jarDir variable so we know where we're located
        jarDir = Path.of(SharedInst.getCurrentDirectory());
        // Check operating system and set OS-specific options
        if (System.getProperty("os.name").contains("Windows")) {
            runningOnWindows = true;
            defaultHackTVPath = System.getProperty("user.dir") + File.separator + "hacktv.exe";
        }
        else {
            runningOnWindows = false;
            defaultHackTVPath = "/usr/local/bin/hacktv";
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
        migratePreferences();
        loadPreferences();
        detectFork();
        selectModesFile();
        if (!openModesFile()) return false;
        if (!openBandPlanFile()) return false;
        populateVideoModes();
        addARCorrectionOptions();
        populateWSS();
        addOutputDevices();
        addCeefaxRegions();
        if (captainJack) {
            captainJack();
        }
        else {
            fsphil();
        }
        // Populate the look and feel combobox
        populateLafList();
        // Set default values when form loads
        radLocalSource.doClick();
        if (!selectDefaultMode()) return false;
        cmbM3USource.setVisible(false);
        txtGain.setText("0");
        updateMenu.setVisible(false);
        // End default value load
        checkMRUList();
        if (PREFS.getInt("noupdatecheck", 0) == 1) {
            chkNoUpdateCheck.setSelected(true);
        }
        else {
            checkForUpdates(true);
        }
        // If any command line parameters were specified, handle them
        if (args.length > 0) {
            // If the specified file has a .htv extension, open it
            if (args[0].toLowerCase(Locale.ENGLISH).endsWith(".htv")) {
                selectedFile = new File(args[0]);
                checkSelectedFile(selectedFile);
            }
            else if (args[0].toLowerCase(Locale.ENGLISH).endsWith(".m3u")) {
                txtSource.setText(args[0]);
                m3uHandler(args[0],0);
            }
            else {
                // Otherwise, assume it's a source file and populate the source
                // text box with it.
                txtSource.setText(args[0]);
            }
        }
        return true;
    }
    
    private static FileFilter createFileFilter() {
        // Creates a custom FileFilter for hacktv binaries
        return new FileFilter() {
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
        };
    }
    
    private void setIcons() {
        var icons = new ArrayList<Image>();
        try {
            icons.add(ImageIO.read(getClass().getClassLoader().getResource("ie/bops/resources/ebubars.png")));
        }
        catch (IOException | IllegalArgumentException e) {
            System.err.println("Icon load failed, using default.\n" + e);
            return;
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
            // Use the largest icon we have
            t.setIconImage(icons.get(icons.size() - 1));              
        }
        else {
            // Set icon without using Taskbar class
            setIconImages(icons);              
        }
    }

    private void populateCheckboxArray() {
    /*  This array is used by the File > New option to reset all checkboxes to
        default values. Be sure to add any new checkboxes to this list.
    */    
        checkBoxes=new JCheckBox[] {
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
            chkInvertVideo,
            chkVITC,
            chkSecamId,
            chkOffset,
            chkSwapIQ,
            chkSiS,
            chkSVideo,
            chkCC608
        };
    }
    
    private void setLaf() {
        lafAL = new ArrayList<>();
        UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lookAndFeel : lookAndFeels) {
            // Get the implementation class for the look and feel
            // Don't add the GTK+ theme on Linux, it renders very poorly and is 
            // the default on many distros.
            if (!lookAndFeel.getClassName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")){
                lafAL.add(lookAndFeel.getClassName());
            }
            // Is this the system default?
            if (UIManager.getSystemLookAndFeelClassName().equals(lafAL.get(lafAL.size() - 1))) {
                defaultLaf = lafAL.size() - 1;
            }
        }
        // Use normal fonts on Metal look and feel, rather than bold
        UIManager.put("swing.boldMetal", false);
        // Add FlatLaf
        // Not enabled by default, needs FlatLaf JAR dependency in classpath
        // https://repo1.maven.org/maven2/com/formdev/flatlaf/
        boolean flatLaf;
        try {
            // Check to see if the FlatLaf class is available to us
            Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLaf = true;
        }
        catch (ClassNotFoundException e) {
           flatLaf = false;
        }
        if (flatLaf) {
            // Load embedded flatlaf.ini
            // Also sets FlatLaf system properties
            String flConf = loadFlatLafINI(true);
            // Add the look and feels
            if (flConf != null) lafAL.addAll(parseThemesINI(flConf, "core-themes", false));
            // Version 3?
            boolean v3;
            try {
                Class.forName("com.formdev.flatlaf.themes.FlatMacLightLaf");
                v3 = true;
            }
            catch (ClassNotFoundException e) {
                v3 = false;
            }
            if ((v3) && (flConf != null)) {
                // Add FlatLaf v3 themes
                lafAL.addAll(parseThemesINI(flConf, "core-themes-v3", false));
            }
            // IntelliJ themes?
            boolean ij;
            try {
                Class.forName("com.formdev.flatlaf.intellijthemes.Utils");
                ij = true;
            }
            catch (ClassNotFoundException e) {
                ij = false;
            }
            if ((ij) && (flConf != null)) {
                // Read the IntellJ themes from flConf
                lafAL.addAll(parseThemesINI(flConf, "intellij-themes", false));
                lafAL.addAll(parseThemesINI(flConf, "materialthemeuilite", false));
            }
        }
        // Safeguard if the LookAndFeel preference is out of bounds
        int v = PREFS.getInt("lookandfeel", defaultLaf);
        if ((v >= (lafAL.size())) || (v < 0)) {
            // Use default L&F and reset preference
            System.out.println("Specified look and feel not found, reverting to default.");
            changeLaf(defaultLaf);
            PREFS.putInt("lookandfeel", defaultLaf);
        }
        else {
            // Use the value we got from preferences
            changeLaf(v);
        }
    }
    
    private void populateLafList() {
        var LAF = new ArrayList<String>();
        var lookAndFeels = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lookAndFeel : lookAndFeels) {
            // Get the name of the look and feel
            if (!lookAndFeel.getName().equals("GTK+")) {
                LAF.add(lookAndFeel.getName());
            }
        }
        // Add FlatLaf if available
        if (lafAL.contains("com.formdev.flatlaf.FlatLightLaf")) {
            LAF.addAll(parseThemesINI(loadFlatLafINI(false), "core-themes", true));
            if (lafAL.contains("com.formdev.flatlaf.themes.FlatMacLightLaf")) {
                LAF.addAll(parseThemesINI(loadFlatLafINI(false), "core-themes-v3", true));
                for (String s : lafAL) {
                    if (s.startsWith("com.formdev.flatlaf.intellijthemes")) {
                        LAF.addAll(parseThemesINI(loadFlatLafINI(false), "intellij-themes", true));
                        LAF.addAll(parseThemesINI(loadFlatLafINI(false), "materialthemeuilite", true));
                        break;
                    }
                }
            }
        }
        var lf = new String[LAF.size()];
        for (int i = 0; i < LAF.size(); i++) {
            lf[i] = LAF.get(i);
        }
        cmbLookAndFeel.setModel(new DefaultComboBoxModel<>(lf));
        cmbLookAndFeel.setSelectedIndex(PREFS.getInt("lookandfeel", defaultLaf));
    }
    
    private String loadFlatLafINI(boolean loadProperties) {
        // Read the embedded flatlaf.ini file
        String r = "ie/bops/resources/flatlaf.ini";
        String f;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(r)) {
            if (is == null) {
                throw new FileSystemNotFoundException("Unable to open embedded resource " + r);
            }
            else {
                f = new String(is.readAllBytes(), StandardCharsets.UTF_8);                           
            }
        }
        catch (IOException | FileSystemNotFoundException ex) {
            System.err.println(ex);
            return null;
        }
        if (loadProperties) {
            // Set FlatLaf system properties
            System.setProperty("flatlaf.useWindowDecorations",
                    Boolean.toString(INI.getBooleanFromINI(f, "flatlaf", "UseWindowDecorations"))
            );
            System.setProperty("flatlaf.menuBarEmbedded",
                    Boolean.toString(INI.getBooleanFromINI(f, "flatlaf", "MenuBarEmbedded"))
            );
            System.setProperty("flatlaf.useRoundedPopupBorder",
                    Boolean.toString(INI.getBooleanFromINI(f, "flatlaf", "UseRoundedPopupBorder"))
            );
        }
        return f;
    }
    
    private ArrayList<String> parseThemesINI(String iniFile, String iniSection, boolean names) {
        var name = new ArrayList<String>();
        var lafClassName = new ArrayList<String>();
        String s = INI.splitINIfile(iniFile, iniSection);
        if (s != null) {
            String[] sa = s.substring(s.indexOf("\n") +1).split("\n");
            String cn = "";
            for (String a : sa) {
                if (a.startsWith("class")) {
                    cn = a.split("=")[1];
                }
                else {
                    String[] sa2 = a.split("=");
                    if (!names) {
                        lafClassName.add(cn + '\u002e' + sa2[0]);
                    }
                    else {
                        name.add("FlatLaf (" + sa2[1] + ")");
                    }
                }
            }
            if (!names) {
                return lafClassName;
            }
            else {
                return name;
            }
        }
        else {
            return null;
        }
    }
    
    private void changeLaf(int i) {
        String l = lafAL.get(i);
        // Only change L&F if different to the current one
        if (!l.equals(UIManager.getLookAndFeel().getClass().getName())) {
            try {
                UIManager.setLookAndFeel(l);
                SwingUtilities.updateComponentTreeUI(this);
                this.pack();
                // Colour of JList resets on L&F change so reset it
                if (this.isVisible() && (playlistAL.isEmpty())) {
                    // Set the background colour of the JList to disabledBackground
                    lstPlaylist.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.disabledBackground"));
                }
                else if (this.isVisible()) {
                    // Set tie background colour of the JList to background (enabled)
                    lstPlaylist.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
                }
                PREFS.putInt("lookandfeel", i);
            }
            catch (ClassNotFoundException c) {
                String err = "The requested look and feel cannot be found.\n"
                        + "The current version of FlatLaf may not support it.";
                if (this.isVisible()) {
                    messageBox(err, JOptionPane.ERROR_MESSAGE);
                }
                else {
                    System.err.println();
                }
                changeLaf(defaultLaf);
                PREFS.putInt("lookandfeel", defaultLaf);
            }    
            catch (IllegalAccessException | InstantiationException | 
                    UnsupportedLookAndFeelException ex) {
                System.err.println(ex);
            }            
        }
    }
    
    private void checkForUpdates(boolean Silent) {
        // Queries the Github API for the latest release
        var updateWorker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                try {
                    // Get the current version's date code using getVersion and
                    // remove the dashes so we can use parseInt later.
                    String cv = getVersion().replaceAll("-", "");
                    if (cv.equals("n/a")) return -1;
                    String a = SharedInst.downloadToString("https://api.github.com/repos/steeviebops/hacktv-gui/releases/latest");
                    String q = "tag_name";
                    if (a.contains(q)) {
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
                        // Don't do anything in this case as we have no version
                        // number to check against.
                        break;
                    case 0:
                        // Update available
                        if (Silent) {
                            updateMenu.setVisible(true);
                            return;
                        }
                        if (JOptionPane.showConfirmDialog(null, "An update is available.\n"
                                + "Would you like to find out more?", APP_NAME, JOptionPane.YES_NO_OPTION)
                                == JOptionPane.YES_OPTION) menuDownloadUpdate.doClick();
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
                        System.err.println("Error code: " + status);
                        if (Silent) return;
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
        if (!runningOnWindows) {
            if (cp.contains(":")) {
                cp = cp.substring(0, cp.indexOf(":"));
            }
        }
        else if (cp.contains(";")) {
            cp = cp.substring(0, cp.indexOf(";"));
        }
        try {
            var sdf = new SimpleDateFormat("yyyy-MM-dd");
            String classFilePath = "/ie/bops/hacktvgui/GUI.class";
            Date date;
            if (Files.exists(Path.of(cp))) {
                date = SharedInst.getLastUpdatedTime(cp, classFilePath);
                if (date != null) {
                    return sdf.format(date);
                }
                else {
                    return "n/a";
                }
            }
            else {
                return "n/a";
            }
        }
        catch (NumberFormatException | InvalidPathException e) {
              return "n/a";
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
        if (tempDir == null) {
            try {
                tempDir = Files.createTempDirectory(APP_NAME);
            }
            catch (IOException ex) {
                System.err.println(ex);
                messageBox("An error occurred while creating the temp directory.", JOptionPane.ERROR_MESSAGE);
                resetTeletextButtons();
            }
        }        
    }
    
    private void selectModesFile() {
        int q;
        // Does a modes file exist in the application directory?
        if ( ((Files.exists(Path.of(jarDir + File.separator + getFork() + ".ini"))) ||
                (Files.exists(Path.of(jarDir + "/bandplans.ini"))) ||
                (Files.exists(Path.of(jarDir + "/Modes.ini")))) ) {
            // If yes, and UseLocalModesFile is 1, use local file.
            if ((PREFS.getInt("uselocalmodesfile", 0)) == 1) {
                q = JOptionPane.YES_OPTION;
            }
            // If yes, and UseLocalModesFile is 0, prompt.
            else {
                q = JOptionPane.showConfirmDialog(null, "A modes file was found in the current directory.\n"
                    + "Do you want to use this file?\n"
                    + "You can suppress this prompt on the GUI settings tab.", APP_NAME, JOptionPane.YES_NO_OPTION);
            }
        }
        // If no, and "UseLocalModesFile" is 0, download
        else if ((PREFS.getInt("uselocalmodesfile", 0)) == 0) {
            q = JOptionPane.NO_OPTION;
        }
        // If no, and UseLocalModesFile is 1, use embedded file
        else {
            q = JOptionPane.YES_OPTION;
        }
        if (q == JOptionPane.YES_OPTION) {
            // Use embedded or local file, depending on what is available
            if (Files.exists(Path.of(jarDir + File.separator + getFork() + ".ini"))) {
                // Use the local file
                modesFilePath = jarDir + File.separator + getFork() + ".ini";
            }
            else if (Files.exists(Path.of(jarDir + "/Modes.ini"))) {
                // Use the local Modes.ini (v4 or earlier) file
                modesFilePath = jarDir + "/Modes.ini";  
            }
            else {
                // Use the embedded copy
                modesFilePath = "ie/bops/resources/" + getFork() + ".ini";
            }
            if (Files.exists(Path.of(jarDir + "/bandplans.ini"))) {
                // Use the local file
                bpFilePath = jarDir + "/bandplans.ini";
            }
            else {
                // Use the embedded copy
                bpFilePath = "ie/bops/resources/bandplans.ini";
            }
        }
        else {
            // Download from Github
            String v = "https://raw.githubusercontent.com/steeviebops/hacktv-gui/main/src/ie/bops/resources/" + getFork() + ".ini";
            String b = "https://raw.githubusercontent.com/steeviebops/hacktv-gui/main/src/ie/bops/resources/bandplans.ini";
            modesFile = downloadModesFile(v);
            bpFile = downloadModesFile(b);
        }
        // Reopen modes file after config change
        if (this.isVisible()) {
            openModesFile();
            openBandPlanFile();
            cmbRegion.setEnabled(false);
            populateVideoModes();
            selectDefaultMode();
        }
    }
    
    private String downloadModesFile(String url) {
        // Downloads files directly to a string
        String v = getFork() + ".ini";
        String b = "bandplans.ini";
        String targetFile;
        try {
            targetFile = SharedInst.downloadToString(url);
            if (url.endsWith(v)) {
                modesFilePath = "";
            }
            else if (url.endsWith(b)) {
                bpFilePath = "";
            }
        }
        catch (IOException | URISyntaxException ex) {
            // Use the embedded copy
            String f = "";
            if (url.endsWith(v)) {
                System.err.println("Error downloading " + v + "...\n" + ex);
                modesFilePath = "ie/bops/resources/" + getFork() + ".ini";
                f = v;
            }
            else if (url.endsWith(b)) {
                System.err.println("Error downloading " + b + "...\n" + ex);
                bpFilePath = "ie/bops/resources/bandplans.ini";
                f = b;
            }
            messageBox("Unable to download the " + f + " file from Github.\n"
                    + "Using embedded copy instead, which may not be up to date.", JOptionPane.ERROR_MESSAGE);
            return "";
        }
        return targetFile;
    } 
    
    private boolean openModesFile() {
        if ( (modesFilePath.isEmpty()) && (modesFile != null) ) {
            modesFileLocation = "online";
        }
        else if (modesFilePath.startsWith("ie/bops/resources/")) {
            // Read the embedded videomodes.ini to the modesFile string
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(modesFilePath)) {
                if (is == null) {
                    throw new FileSystemNotFoundException("Unable to open embedded resource " + modesFilePath);
                }
                else {
                    modesFile = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    modesFileLocation = "embedded";                            
                }
            }
            catch (IOException | FileSystemNotFoundException ex) {
                // No modes file to load, we cannot continue
                messageBox("Critical error, unable to read the embedded modes file.\n"
                        + "The application will now exit.", JOptionPane.ERROR_MESSAGE);
                System.err.println(ex);
                return false;
            }
        }
        else {
            // Read the videomodes.ini we specified previously
            var f = new File(modesFilePath);
            try {
                modesFile = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                modesFileLocation = "external";
            }
            catch (IOException e) {
                // Load failed, retry with the embedded file
                messageBox("Unable to read the modes file.\n"
                        + "Retrying with the embedded copy, which may not be up to date.", JOptionPane.WARNING_MESSAGE);
                modesFilePath = "ie/bops/resources/" + getFork() + getFork() + ".ini";
                modesFileLocation = "embedded";
                openModesFile();
            }
        }
        // Read modes file version
        modesFileVersion = INI.getStringFromINI(modesFile, "Modes.ini", "FileVersion", "unknown", true);
        return true;
    }
    
    private boolean openBandPlanFile() {
        String m = modesFileVersion.replace("c","");
        if ( (SharedInst.isNumeric(m)) && (Double.parseDouble(m) < 5.00) ) {
            // This is a v4 or older Modes.ini file
            // The main difference between v5 and the earlier formats is that
            // v5 split out the band plans into a separate file.
            // So the easiest way to read an older version is to simply
            // duplicate modesFile to bpFile.
            System.out.println("Version 4.x or earlier modes file detected.");
            bpFile = modesFile;
            bpFileLocation = "legacy";
            bpFileVersion = modesFileVersion;
        }
        else if ( (bpFilePath.isEmpty()) && (bpFile != null) ) {
            bpFileLocation = "online";
        }
        else if (bpFilePath.startsWith("ie/bops/resources/")) {
            // Read the embedded bandplans.ini to the bpFile string
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(bpFilePath)) {
                if (is == null) {
                    throw new FileSystemNotFoundException("Unable to open embedded resource " + bpFilePath);
                }
                else {
                    bpFile = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    bpFileLocation = "embedded";                            
                }
            }
            catch (IOException | FileSystemNotFoundException ex) {
                // No modes file to load, we cannot continue
                messageBox("Critical error, unable to read the embedded band plans file.\n"
                        + "The application will now exit.", JOptionPane.ERROR_MESSAGE);
                System.err.println(ex);
                return false;
            }
        }
        else {
            // Read the bandplans.ini we specified previously
            var f = new File(bpFilePath);
            try {
                bpFile = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                bpFileLocation = "external";
            }
            catch (IOException e) {
                // Load failed, retry with the embedded file
                messageBox("Unable to read the band plans file.\n"
                        + "Retrying with the embedded copy, which may not be up to date.", JOptionPane.WARNING_MESSAGE);
                bpFilePath = "ie/bops/resources/bandplans.ini";
                bpFileLocation = "embedded";
                openBandPlanFile();
            }
        }
        // Read bandplans.ini file version if not in legacy mode
        if (!bpFileLocation.equals("legacy")) {
            bpFileVersion = INI.getStringFromINI(bpFile, "bandplans.ini", "Version", "unknown", true);
        }        
        return true;
    }
    
    private void populateVideoModes() {
        palModeArray = addVideoModes("pal", 1);
        ntscModeArray = addVideoModes("ntsc", 1);
        secamModeArray = addVideoModes("secam", 1);
        otherModeArray = addVideoModes("other", 1);
        macModeArray = addVideoModes("mac", 1);
        if (palModeArray[0].isBlank()) radPAL.setEnabled(false);
        if (ntscModeArray[0].isBlank()) radNTSC.setEnabled(false);
        if (secamModeArray[0].isBlank()) radSECAM.setEnabled(false);
        if (otherModeArray[0].isBlank()) radBW.setEnabled(false);
        if (macModeArray[0].isBlank()) radMAC.setEnabled(false);
    }
    
    private boolean selectDefaultMode() {
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
            messageBox("No video systems were found. The " + getFork() + ".ini file may be invalid or corrupted.\n"
                    + "The application will now exit.", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (chkAudio.isEnabled()) {
            chkAudio.setSelected(true);
            if (nicamSupported) {
                chkNICAM.setSelected(true);
            }
            else if ((a2Supported) && (lines == 625)) {
                chkA2Stereo.setSelected(true);
            }
        }
        return true;
    }
    
    private String[] addVideoModes(String ColourStandard, int returnValue) {
        /*
         * ColourStandard specifies what to look for (pal, ntsc, secam, other or mac)
         * returnValue specifies what we return in the array
         * 0 = return the friendly name of the mode
         * 1 = return the mode parameter used at the command line
         */
        
        String m = INI.getStringFromINI(modesFile, "videomodes", ColourStandard, "", false);
        String[] q;
        
        String regex = "(,\\s*)";
        
        // q contains the modes defined in modes file for the specified standard
        q = m.split(regex);
        
        if (returnValue == 1) {
            return q;
        }
        else {
            // Check if the specified modes are defined, if not, don't add them
            var ml = new ArrayList<String>();
            for (String s : q) {
                String a = INI.getStringFromINI(modesFile, s, "name", "", true);
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
            var b = new String[ml.size()];
            for(int i = 0; i < b.length; i++) {
                b[i] = ml.get(i);    
            }
            return b;
        }
    }

    private void migratePreferences() {
        try {
            if (runningOnWindows ? PREFS.keys().length > 1 : PREFS.keys().length > 0) return;
            if (Preferences.userRoot().nodeExists("com/steeviebops/hacktvgui")) {
                var oldPrefs = Preferences.userRoot().node("com/steeviebops/hacktvgui");
                if (runningOnWindows ? oldPrefs.keys().length > 1 : oldPrefs.keys().length > 0) {
                    // Convert preferences to new format
                    for (String key : oldPrefs.keys()) {
                        PREFS.put(key.toLowerCase(Locale.ENGLISH), oldPrefs.get(key, null));
                    }
                }
                PREFS.flush();
                // Remove old preferences node
                //oldPrefs.parent().removeNode();
                //oldPrefs.flush();
                System.out.println("Successfully migrated preferences node.");                
            }
        }
        catch (BackingStoreException ex) {
            System.err.println("Error importing old preference store: " + ex.getMessage());
        }
    }
    
    private void loadPreferences(){
        if (PREFS.getInt("hackdac", 0) == 1) chkHackDAC.setSelected(true);
        // Check preferences node for the path to hacktv
        // If not found, use the default
        if (runningOnWindows) {
            hackTVPath = PREFS.get("hacktvpath", defaultHackTVPath);
        }
        else {
            hackTVPath = PREFS.get("hacktvpath", null);
            if (hackTVPath == null) {
                // Check if hacktv exists at /usr/bin/hacktv, which is the
                // package manager's path. Otherwise use the default.
                if (Files.exists(Path.of("/usr/bin/hacktv"))) {
                    hackTVPath = "/usr/bin/hacktv";
                }
                else {
                    hackTVPath = defaultHackTVPath;
                }
            }
        }
        
        // Load the full path to a variable so we can use getParent on it and
        // get its parent directory path
        hackTVDirectory = new File(hackTVPath).getParent();
        txtHackTVPath.setText(hackTVPath);
        // Check status of UseLocalModesFile
        if (PREFS.getInt("uselocalmodesfile", 0) == 1) {
            chkLocalModes.setSelected(true);
        }
    }
    
    private static void resetPreferences() {
        // Delete the preference store and everything in it
        try {
            PREFS.removeNode();
            System.out.println("All preferences have been reset to defaults.");
        }
        catch (BackingStoreException e) {
            System.err.println("Reset failed: " + e.getMessage());
        }
    }
    
    private void detectFork() {
        /*  Loads the hacktv binary into RAM and attempts to find a specified
         *  string to detect what build or fork it is.
         */
        
        // Check if the specified path does not exist or is a directory
        if (!Files.exists(Path.of(hackTVPath))) {
            lblFork.setText("Not found");
            captainJack = false;
            mattstvbarn = false;
            return;
        }
        else if (Files.isDirectory(Path.of(hackTVPath))) {
            lblFork.setText("Invalid path");
            captainJack = false;
            mattstvbarn = false;
            return;    
        }
        /*  Check the size of the specified file.
         *  If larger than 100MB, call the fsphil method and don't go any further.
         *  This is to avoid memory leaks or hangs by loading a bad file.
         */
        var f = new File(hackTVPath);
        if (f.length() > 104857600) {
            lblFork.setText("Invalid file (too large)");
            captainJack = false;
            mattstvbarn = false;
            return;
        }
        // Create a set of strings to look for in the binary
        Set<String> foundStrings;
        try {
            foundStrings = extractMatchingStrings(Paths.get(hackTVPath), 10, Set.of(
                "Usage: hacktv [options] input [input...]",
                "--enableemm",
                "pm8546g.bin"
            ));
            if (foundStrings.isEmpty()) {
                lblFork.setText("Invalid file (not hacktv?)");
                captainJack = false;
                mattstvbarn = false;
                return;
            }
            else if (foundStrings.contains("Usage: hacktv [options] input [input...]")) {
                captainJack = foundStrings.contains("--enableemm");
                mattstvbarn = foundStrings.contains("pm8546g.bin");
                if (captainJack) {
                    lblFork.setText("Captain Jack");
                }
                else if (mattstvbarn) {
                    lblFork.setText("Matt's TV Barn");
                }
                else {
                    lblFork.setText("fsphil");
                }
            }
            // Get the hacktv version if supported, by running hacktv --version
            var pb = new ProcessBuilder(hackTVPath, "--version");
            String v;
            Process p = pb.start();
            try (var br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                // We only need the first line
                v = br.readLine();
            }
            if ((v != null) && (!v.isBlank())) {
                lblFork.setText(lblFork.getText() + " (" + v.substring(v.indexOf(" ") + 1) + ")");
            }
        }
        catch (IOException ex) {
            lblFork.setText("File access error");
            captainJack = false;
            mattstvbarn = false;
        }
    }
    
    private Set<String> extractMatchingStrings(Path binaryPath, int minLength, Set<String> targets) throws IOException {
        var found = new HashSet<String>();
        try (var is = new BufferedInputStream(Files.newInputStream(binaryPath))) {
            var baos = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) != -1) {
                // Only write printable ASCII characters to the ByteArrayOutputStream
                if (b >= 32 && b <= 126) {
                    baos.write(b);
                }
                else {
                    if (baos.size() >= minLength) {
                        String s = baos.toString("US-ASCII");
                        for (String target : targets) {
                            if (s.contains(target)) {
                                found.add(target);
                                // Early return if all found
                                if (found.containsAll(targets)) return found;
                            }
                        }
                    }
                    baos.reset();
                }
            }
        }
        return found;
    }

    private String getFork() {
        if (captainJack) {
            return "captainjack";
        }
        else {
            return "fsphil";
        }
    }

    private void fsphil() {
        // Enable test signal settings button
        btnTestSettings.setVisible(mattstvbarn);
        // Disable features unsupported in fsphil's build
        if (chkTimestamp.isSelected()) chkTimestamp.doClick();
        if (chkLogo.isSelected()) chkLogo.doClick();
        if (chkSubtitles.isSelected()) chkSubtitles.doClick();
        if (chkPosition.isSelected()) chkPosition.doClick();
        if (chkVolume.isSelected()) chkVolume.doClick();
        if (chkDownmix.isSelected()) chkDownmix.doClick();
        chkTimestamp.setEnabled(false);
        chkLogo.setEnabled(false);
        chkSubtitles.setEnabled(false);
        chkPosition.setEnabled(false);
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
        // Hide test signal settings button
        btnTestSettings.setVisible(false);
        // Enable features supported in Captain Jack's build
        chkLogo.setEnabled(true);
        addLogoOptions();
        if ( !radTest.isSelected() ) {
            chkPosition.setEnabled(true);
            chkTimestamp.setEnabled(true);
            chkPosition.setEnabled(true);
            chkSubtitles.setEnabled(true);
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
        if (playlistAL.isEmpty()) {
            // Set the background colour of the JList to disabledBackground
            lstPlaylist.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.disabledBackground"));
        }
        else {
            // Set tie background colour of the JList to background (enabled)
            lstPlaylist.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        }
        // Convert playlistAL to an array so we can populate lstPlaylist with it
        var pl = new String[playlistAL.size()];
        for(int i = 0; i < pl.length; i++) {
            if ((startPoint == i) && (startPoint != -1)) {
                // Add an asterisk to the start of the string to designate it
                // as the start point of the playlist
                pl[i] = "* " + playlistAL.get(i);
            }
            else {
                pl[i] = playlistAL.get(i);
            }
        }
        // Populate lstPlaylist using the contents of pl[]
        lstPlaylist.setListData(pl);
        // Enable or disable random option
        if (playlistAL.size() > 1) {
            chkRandom.setEnabled(true);
        }
        else {
            if (chkRandom.isSelected()) chkRandom.doClick();
            chkRandom.setEnabled(false);
        }
    }
    
    private void checkMRUList() {
        // Get MRU values and display in the File menu
        String ConfigFile1 = PREFS.get("file1", "");
        String ConfigFile2 = PREFS.get("file2", "");
        String ConfigFile3 = PREFS.get("file3", "");
        String ConfigFile4 = PREFS.get("file4", "");
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
        String ConfigFile1 = PREFS.get("file1", "");
        String ConfigFile2 = PREFS.get("file2", "");
        String ConfigFile3 = PREFS.get("file3", "");
        String ConfigFile4 = PREFS.get("file4", "");
        if (FilePath.equals(ConfigFile2)) {
            PREFS.put("file2", ConfigFile1);
            PREFS.put("file1", FilePath);
            checkMRUList();
        }
        else if (FilePath.equals(ConfigFile3)) {
            PREFS.put("file3", ConfigFile2);
            PREFS.put("file2", ConfigFile1);
            PREFS.put("file1", FilePath);   
            checkMRUList(); 
        }
        else if (FilePath.equals(ConfigFile4)) {
            PREFS.put("file4", ConfigFile3);
            PREFS.put("file3", ConfigFile2);
            PREFS.put("file2", ConfigFile1);
            PREFS.put("file1", FilePath);
            checkMRUList();
        }
        else if (FilePath.equals(ConfigFile1)) {
            // Do nothing
        }
        else {
            if (!ConfigFile3.isEmpty()) PREFS.put("file4", ConfigFile3);
            if (!ConfigFile2.isEmpty()) PREFS.put("file3", ConfigFile2);
            if (!ConfigFile1.isEmpty()) PREFS.put("file2", ConfigFile1);
            PREFS.put("file1", FilePath);
            checkMRUList();
        }
    }
    
    private void saveFilePrompt() {
        // Opens the save file dialogue
        int result = configFileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // Check if the saved file has a .htv extension or not.
            // If it does not, then append one.
            selectedFile = new File (SharedInst.stripQuotes(configFileChooser.getSelectedFile().toString()));
            if (!selectedFile.toString().toLowerCase(Locale.ENGLISH).endsWith(".htv")) {
                selectedFile = new File(selectedFile + ".htv");
            }
            // Create file
            try {
                if (!selectedFile.createNewFile()) {
                    /* File exists, prompt to overwrite.
                     * If yes, go to the save method. If no, then restart this
                     * method so the user can select another file. Java doesn't
                     * appear to support file overwrite prompts in its dialogues
                     * so this is a workaround/hack.
                    */
                    if (JOptionPane.showConfirmDialog(null, selectedFile.getName() + " already exists.\n"
                            + "Do you want to overwrite it?", APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                            == JOptionPane.YES_OPTION) {
                        saveConfigFile(selectedFile);
                    }
                    else {
                        saveFilePrompt();
                    }
                }
                else {
                    saveConfigFile(selectedFile);
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
                htvLoadInProgress = true;
                if (openConfigFile(f)) {
                    // Display the opened filename in the title bar
                    // Back up the original title once
                    if (!titleBarChanged) {
                        titleBar = this.getTitle();
                        titleBarChanged = true;
                    }
                    this.setTitle(titleBar + " - " + SourceFile.getName());
                    // Remove the ellipsis after Save to follow standard UI guidelines
                    menuSave.setText("Save");
                    updateMRUList(SourceFile.toString());    
                }
                htvLoadInProgress = false;
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
        if ((!captainJack) && (ImportedFork.equals("captainjack"))) {
            messageBox(WrongFork, JOptionPane.WARNING_MESSAGE);
        }
        else if ((captainJack) && (!ImportedFork.equals("captainjack"))) {
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
        if ((ImportedOutputDevice.isEmpty()) || (ImportedOutputDevice.toLowerCase(Locale.ENGLISH).startsWith("hackrf"))) {
            cmbOutputDevice.setSelectedIndex(0);
            if (ImportedOutputDevice.contains(":")) {
                txtOutputDevice.setText(ImportedOutputDevice.split(":")[1]);
            }
        }
        else if (ImportedOutputDevice.toLowerCase(Locale.ENGLISH).startsWith("soapysdr")) {
            cmbOutputDevice.setSelectedIndex(1);
            if (ImportedOutputDevice.contains(":")) {
                txtOutputDevice.setText(ImportedOutputDevice.split(":")[1]);
            }
        }
        else if (ImportedOutputDevice.toLowerCase(Locale.ENGLISH).startsWith("fl2k")) {
            cmbOutputDevice.setSelectedIndex(2);
            if (ImportedOutputDevice.contains(":")) {
                txtOutputDevice.setText(ImportedOutputDevice.split(":")[1]);
            }
            switch (INI.getStringFromINI(fileContents, "hacktv", "fl2k-audio", "", false)) {
                case "stereo":
                    cmbFl2kAudio.setSelectedIndex(1);
                    break;
                case "spdif":
                    cmbFl2kAudio.setSelectedIndex(2);
                    break;
                default:
                    cmbFl2kAudio.setSelectedIndex(0);
                    break;
            }
        }
        else {
            cmbOutputDevice.setSelectedIndex(3);
            txtOutputDevice.setText(ImportedOutputDevice);
        }
        // Video mode
        String ImportedVideoMode = INI.getINIValue(fileContents, "hacktv", "mode", "");
        boolean ModeFound = false;
        for (int i = 0; i < palModeArray.length; i++) {
            // Check if the mode we imported is in the PAL mode array
            if (palModeArray[i].equals(ImportedVideoMode)) {
                radPAL.doClick();
                cmbMode.setSelectedIndex(i);
                ModeFound = true;
                break;
            }
            // Check the 'alt' value to see if we find a match there
            else if (checkAltModeNames(palModeArray[i], ImportedVideoMode)) {
                radPAL.doClick();
                cmbMode.setSelectedIndex(i);
                ModeFound = true;
                break;
            }
        }
        if (!ModeFound) {
            // Check the NTSC mode array, and so on...
            for (int i = 0; i < ntscModeArray.length; i++) {
                if (ntscModeArray[i].equals(ImportedVideoMode)) {
                    radNTSC.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
                else if (checkAltModeNames(ntscModeArray[i], ImportedVideoMode)) {
                        radNTSC.doClick();
                        cmbMode.setSelectedIndex(i);
                        ModeFound = true;
                        break;
                }
            }      
        }
        if (!ModeFound) {
            for (int i = 0; i < secamModeArray.length; i++) {
                if (secamModeArray[i].equals(ImportedVideoMode)) {
                    radSECAM.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
                else if (checkAltModeNames(secamModeArray[i], ImportedVideoMode)) {
                    radSECAM.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
            }
        }
        if (!ModeFound) {
            for (int i = 0; i < otherModeArray.length; i++) {
                if (otherModeArray[i].equals(ImportedVideoMode)) {
                    radBW.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
                else if (checkAltModeNames(otherModeArray[i], ImportedVideoMode)) {
                    radBW.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
            }
        }
        if (!ModeFound) {
            for (int i = 0; i < macModeArray.length; i++) {
                if (macModeArray[i].equals(ImportedVideoMode)) {
                    radMAC.doClick();
                    cmbMode.setSelectedIndex(i);
                    ModeFound = true;
                    break;
                }
                else if (checkAltModeNames(macModeArray[i], ImportedVideoMode)) {
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
        // Did the mode load successfully?
        if (!ImportedVideoMode.equals(mode)) {
           resetAllControls();
           return false;
        }
        // Is this a baseband mode?
        boolean bb = INI.getINIValue(modesFile, ImportedVideoMode, "modulation", "").equals("baseband");
        // Input source or test card
        String ImportedSource = INI.getStringFromINI(fileContents, "hacktv", "input", "", true);
        String M3USource = (INI.getStringFromINI(fileContents, "hacktv-gui3", "m3usource", "", true));
        Integer M3UIndex = (INI.getIntegerFromINI(fileContents, "hacktv-gui3", "m3uindex"));
        if (ImportedSource.toLowerCase(Locale.ENGLISH).startsWith("test:")) {
            radTest.doClick();
            if (captainJack) {
                String ImportedTC = ImportedSource.replace("test:", "");
                boolean TCFound = false;
                if (!ImportedTC.isEmpty()) {
                    for (int i = 0; i <= tcArray.length - 1; i++) {
                        if ((tcArray[i].split("\\s*,\\s*")[0].toLowerCase(Locale.ENGLISH)).equals(ImportedTC)) {
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
            var M3UFile = new File(M3USource);
            // If the source is an M3U file...
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            // Spawn M3UHandler using the index value we got above.
            m3uHandler(M3UFile.getAbsolutePath(), M3UIndex);
            txtSource.setText(M3USource);
        }
        else if (INI.getBooleanFromINI(fileContents, "hacktv-gui3", "playlist")) {
            // Split the [playlist] section from the HTV file.
            // We then split the section into an array (minus the header) 
            // and use that to populate playlistAL.
            if (INI.splitINIfile(fileContents, "playlist") != null) {
                String[] pl = INI.splitINIfile(fileContents, "playlist").split("\\n");
                for (int i = 1; i < pl.length; i++) {
                    playlistAL.add(pl[i]);
                }
                if ((INI.getIntegerFromINI(fileContents, "hacktv-gui3", "playliststart")) != null) {
                    startPoint = INI.getIntegerFromINI(fileContents, "hacktv-gui3", "playliststart") - 1;
                    // Don't accept values lower than one
                    if (startPoint < 1) startPoint = -1;
                }
                chkRandom.setSelected(INI.getBooleanFromINI(fileContents, "hacktv-gui3", "random"));
                populatePlaylist();
            }
        }
        else {
            if ( (!ImportedSource.endsWith(".m3u")) || (!ImportedSource.endsWith(".m3u8")) ) txtSource.setText(ImportedSource);
        }
        // Frequency or channel number
        if ( ((cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1)) && (!bb) ) {
            String NoFrequencyOrChannel = "No frequency or valid channel number was found in the configuration file. Load aborted.";
            String ImportedChannel = INI.getStringFromINI(fileContents, "hacktv-gui3", "channel", "", true);
            String ImportedBandPlan = INI.getStringFromINI(fileContents, "hacktv-gui3", "bandplan", "", false);
            Double ImportedFrequency;
            if (INI.getDoubleFromINI(fileContents, "hacktv", "frequency") != null) {
                ImportedFrequency = INI.getDoubleFromINI(fileContents, "hacktv", "frequency");
            }
            else {
                // Return a value of -250 if the value is null so we can handle it
                ImportedFrequency = Double.valueOf("-250");
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
                boolean bpFound = false;
                radUHF.doClick();
                // Check the available band plans for the one specified and set the region accordingly
                if (uhfAL.contains(ImportedBandPlan)) {
                    bpFound = true;
                    for (int ub = 0; ub < uhfAL.size(); ub++) {
                        if (uhfAL.get(ub).equals(ImportedBandPlan)) {
                            cmbRegion.setSelectedIndex(ub);
                            // Search for the specified channel
                            for (int i = 0; i <= cmbChannel.getItemCount() - 1; i++) {
                                if ( (channelArray[i].toLowerCase(Locale.ENGLISH)).equals(ImportedChannel.toLowerCase(Locale.ENGLISH)) ) {
                                    cmbChannel.setSelectedIndex(i);
                                    ChannelFound = true;
                                }
                            }
                        }
                    }
                }
                // If not found, try VHF
                else if (!ChannelFound) {
                    radVHF.doClick();
                    // Check the available band plans for the one specified and set the region accordingly
                    if (vhfAL.contains(ImportedBandPlan)) {
                        bpFound = true;
                        for (int vb = 0; vb < vhfAL.size(); vb++) {
                            if (vhfAL.get(vb).equals(ImportedBandPlan)) {
                                cmbRegion.setSelectedIndex(vb);
                                // Search for the specified channel
                                for (int i = 0; i <= cmbChannel.getItemCount() - 1; i++) {
                                    if ( (channelArray[i].toLowerCase(Locale.ENGLISH)).equals(ImportedChannel.toLowerCase(Locale.ENGLISH)) ) {
                                        cmbChannel.setSelectedIndex(i);
                                        ChannelFound = true;
                                    }
                                }
                            }
                        }
                    }
                }
                // If still not found, generate an error and use the frequency instead of the channel
                if (!ChannelFound) {
                    if (!bpFound) {
                        /* 
                            Versions 2024-09-01 and earlier did not properly check for
                            band plans while parsing HTV files. Save files from older 
                            versions, from before the addition of multiple band plans,
                            could load an incorrect band plan as a result (if more
                            than one had a matching channel number). These files will
                            now return an error but the fix is simple, the correct
                            region/band/channel just needs to be selected and
                            the file resaved.
                        */
                        radCustom.doClick();
                        Double Freq = ImportedFrequency / 1000000;
                        txtFrequency.setText(Double.toString(Freq).replace(".0",""));
                        invalidConfigFileValue("band plan", ImportedBandPlan);                        
                    }
                    else if (ImportedFrequency != -250) {
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
        // SECAM field ID
        if ((INI.getBooleanFromINI(fileContents, "hacktv", "secam-field-id")) && radSECAM.isSelected()) {
            chkSecamId.doClick();
            if (INI.getIntegerFromINI(fileContents, "hacktv", "secam-field-id-lines") != null) {
                int id = INI.getIntegerFromINI(fileContents, "hacktv", "secam-field-id-lines");
                if ((id >= 1) && (id <= 8)) {
                    cmbSecamIdLines.setSelectedIndex(id - 1);
                }
            }
        }
        // Swap IQ
        if ((INI.getBooleanFromINI(fileContents, "hacktv", "swap-iq")) &&
                (!INI.getStringFromINI(modesFile, mode, "modulation", "", false).equals("baseband")) ) {
            chkSwapIQ.doClick();
        }
        // Gain
        if (INI.getIntegerFromINI(fileContents, "hacktv", "gain") != null) {
            txtGain.setText(INI.getIntegerFromINI(fileContents, "hacktv", "gain").toString());
        }
        // If value is null and output device is hackrf or soapysdr, set gain to zero
        else if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            if (!bb) txtGain.setText("0");
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
            String ImportedLogo = INI.getStringFromINI(fileContents, "hacktv", "logo", "", true).toLowerCase(Locale.ENGLISH);
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
                    if ( (logoArray[i].toLowerCase(Locale.ENGLISH)).equals(ImportedLogo) ) {
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
            if (ImportedTeletext.toLowerCase(Locale.ENGLISH).startsWith("raw:")) {
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
        if ((INI.getIntegerFromINI(fileContents, "hacktv", "arcorrection")) != null) {
            Integer ImportedAR = (INI.getIntegerFromINI(fileContents, "hacktv", "arcorrection"));
            chkARCorrection.doClick();
            cmbARCorrection.setSelectedIndex(ImportedAR);
        }
        // Scrambling system
        String ImportedScramblingSystem = INI.getStringFromINI(fileContents, "hacktv", "scramblingtype", "", false);
        String ImportedKey = INI.getStringFromINI(fileContents, "hacktv", "scramblingkey", "", false);
        String ImportedKey2 = INI.getStringFromINI(fileContents, "hacktv", "scramblingkey2", "", false);
        if ((radPAL.isSelected()) || radSECAM.isSelected()) {
            int i;
            switch (ImportedScramblingSystem) {
                case "":
                    i = 0;
                    break;
                case "videocrypt":
                    i = scramblingTypeArray.indexOf("--videocrypt");
                    break;
                case "videocrypt2":
                    i = scramblingTypeArray.indexOf("--videocrypt2");
                    break;
                case "videocrypt1+2":
                    if (dualVC != -1) {
                        i = dualVC;
                    }
                    else {
                        i = -1;
                    }
                    break;
                case "videocrypts":
                    i = scramblingTypeArray.indexOf("--videocrypts");
                    break;
                case "syster":
                    i = scramblingTypeArray.indexOf("--syster");
                    break;
                case "d11":
                    i = scramblingTypeArray.indexOf("--d11");
                    break;
                case "systercnr":
                    i = scramblingTypeArray.indexOf("--systercnr");
                    break;
                case "systerls+cnr":
                    i = scramblingTypeArray.indexOf("--systercnr") + 1;
                    break;
                default:
                    i = -1;
                    break;
            }
            if (i == -1) {
                invalidConfigFileValue("scrambling system", ImportedScramblingSystem);
                ImportedScramblingSystem = "";
            }
            else {
                cmbScramblingType.setSelectedIndex(i);
            }
        }
        else if (radMAC.isSelected()) {
            int i;
            switch (ImportedScramblingSystem) {
                case "":
                    i = 0;
                    break;
                case "single-cut":
                    i = scramblingTypeArray.indexOf("--single-cut");
                    break;
                case "double-cut":
                    i = scramblingTypeArray.indexOf("--double-cut");
                    break;
                default:
                    i = -1;
                    break;
            }
            if (i == -1) {
                invalidConfigFileValue("scrambling system", ImportedScramblingSystem);
                ImportedScramblingSystem = "";
            }
            else {
                cmbScramblingType.setSelectedIndex(i);
            }
        }            
        // Scrambling key/viewing card type (including VC1 side of dual VC1/2 mode)
        if ( (!ImportedScramblingSystem.isEmpty()) ) {
            if (ImportedKey.isEmpty()) ImportedKey = ImportedKey.replace("", "blank");
            ImportedKey = ImportedKey.replace("eurocrypt ", "");
            int k = scramblingKeyArray.indexOf(ImportedKey);
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
            int k2 = scramblingKey2Array.indexOf(ImportedKey2); 
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
                    if (ImportedEMM.equals(1)) chkActivateCard.doClick();
                    if (ImportedEMM.equals(2)) chkDeactivateCard.doClick();
                    ImportedCardNumber = INI.getStringFromINI(fileContents, "hacktv", "cardnumber", "", false);
                    // Handling of legacy files
                    if (ImportedCardNumber.length() == 8) {
                        Imported13Prefix = INI.getStringFromINI(fileContents, "hacktv-gui3", "13digitprefix", "", false);
                        // The ImportedCardNumber value only contains 8 digits of the card number
                        // To find the check digit, we run the CalculateLuhnCheckDigit method and append the result
                        if (SharedInst.isNumeric(Imported13Prefix + ImportedCardNumber)) txtCardNumber.setText(Imported13Prefix + 
                        ImportedCardNumber + SharedInst.calculateLuhnCheckDigit(Long.parseLong(ImportedCardNumber)));
                    }
                    else {
                        // Pass the full card number through
                        if (SharedInst.isNumeric(ImportedCardNumber)) txtCardNumber.setText(ImportedCardNumber);
                    }
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
            if ( (scramblingType1.equals("--syster")) || (scramblingType1.equals("--systercnr")) ) {
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
            if (chkAudio.isSelected() ) chkAudio.doClick();
        }
        // NICAM
        if (INI.getBooleanFromINI(fileContents, "hacktv", "nicam") == false) {
            if (chkNICAM.isSelected() ) chkNICAM.doClick();
        }
        // A2 Stereo
        if (INI.getBooleanFromINI(fileContents, "hacktv", "a2stereo") == true) {
            if ( (!chkA2Stereo.isSelected()) && (a2Supported) ) chkA2Stereo.doClick();
        }
        // ECM
        if (INI.getBooleanFromINI(fileContents, "hacktv", "showecm")) {
            chkShowECM.doClick();
        }
        // VITS
        if (INI.getBooleanFromINI(fileContents, "hacktv", "vits")) {
            chkVITS.doClick();
        }
        // VITC
        if (INI.getBooleanFromINI(fileContents, "hacktv", "vitc")) {
            chkVITC.doClick();
        }
        // SiS
        if (INI.getBooleanFromINI(fileContents, "hacktv", "sis")) {
            chkSiS.doClick();
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
                chkMacMono.setSelected(true);
            }
            if (INI.getStringFromINI(fileContents, "hacktv", "mac-audio-quality", "high", false).equals("medium")) {
                chkMac16k.setSelected(true);
            }
            if (INI.getStringFromINI(fileContents, "hacktv", "mac-audio-compression", "companded", false).equals("linear")) {
                chkMacLinear.setSelected(true);
            }
            if (INI.getStringFromINI(fileContents, "hacktv", "mac-audio-protection", "l1", false).equals("l2")) {
                chkMacL2.setSelected(true);
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
        // S-Video mode
        if (INI.getBooleanFromINI(fileContents, "hacktv", "s-video") ){
            if (chkSVideo.isEnabled()) chkSVideo.doClick();
        }
        // Closed captioning
        if (INI.getBooleanFromINI(fileContents, "hacktv", "cc608") ){
            if (chkCC608.isEnabled()) chkCC608.doClick();
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
                default:
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
        // Offset
        Double ImportedOffset;
        if ((INI.getDoubleFromINI(fileContents, "hacktv", "offset")) != null) {
            if (!chkOffset.isSelected()) chkOffset.doClick();
            ImportedOffset = (INI.getDoubleFromINI(fileContents, "hacktv", "offset") / 1000000);
            txtOffset.setText(ImportedOffset.toString().replace(".0","")); 
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
            ImportedSampleRate = Double.valueOf("16");
            messageBox("No sample rate specified, defaulting to 16 MHz.", JOptionPane.INFORMATION_MESSAGE);
        }
        txtSampleRate.setText(ImportedSampleRate.toString().replace(".0",""));
        // Philips test signal
        String importedTS = INI.getStringFromINI(fileContents, "hacktv", "testsignal", "", false);
        if (mattstvbarn) {
            boolean TSFound = false;
            if (!importedTS.isEmpty()) {
                for (int i = 0; i < tcArray.length; i++) {
                    int j = tcArray[i].indexOf(",");
                    if (j == -1) continue;
                    if ((tcArray[i].substring(0, j).toLowerCase(Locale.ENGLISH)).equals(importedTS)) {
                        radTest.doClick();
                        cmbTest.setSelectedIndex(i);
                        if (!ImportedSource.isBlank()) txtSource.setText(ImportedSource);
                        TSFound = true;
                        break;
                    }
                }
                if (!TSFound) {
                    invalidConfigFileValue("test signal", importedTS);
                }
            }
        }
        else if (!importedTS.isBlank()) {
            messageBox("The selected build of hacktv does not support the " +
                    importedTS + " test signal.\n" +
                    "The setting will be skipped.", JOptionPane.WARNING_MESSAGE);
        }
        // Repeat
        if (chkRepeat.isEnabled()) {
            if (INI.getBooleanFromINI(fileContents, "hacktv", "repeat")) {
                chkRepeat.doClick();
            }
        }
        btnRun.requestFocusInWindow();
        // This must be the last line in this method, it confirms that 
        // everything ran as planned.
        return true;
    }
    
    private boolean checkAltModeNames(String modeToCheck, String alt) {
        /*
         * The modes file supports an alt (meaning 'alternative') setting, which
         * can be used to report a second option that represents that mode.
         * This is used by B/G and D/K so both options are accepted.
         *
         * For example, checkAltModeNames("g", "b") will check if section 'g'
         * contains an alt value of 'b' and return true if it finds it.
         */
        return (INI.getStringFromINI(modesFile, modeToCheck, "alt", "", false).equals(alt));
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
                "The file may have been created in a different version of the application, or the value is invalid.",
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
                switch (cmbFl2kAudio.getSelectedIndex()) {
                    case 1:
                        FileContents = INI.setINIValue(FileContents, "hacktv", "fl2k-audio", "stereo");
                        break;
                    case 2:
                        FileContents = INI.setINIValue(FileContents, "hacktv", "fl2k-audio", "spdif");
                        break;
                    default:
                        break;
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
        if (captainJack) FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "fork", "CaptainJack");
        // Input source or test card
        if (!playlistAL.isEmpty()) {
            // We'll populate the playlist section later
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv-gui3", "playlist", 1);
            // Set start point of playlist
            if (startPoint != -1) FileContents = INI.setIntegerINIValue(FileContents, "hacktv-gui3", "playliststart", startPoint + 1);
            // Random option
            if (chkRandom.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv-gui3", "random", 1);
        }
        else {
            // We'll add Philips patterns later, if any
            if ( (radTest.isSelected()) && (!isPhilipsTestSignal()) ) {
                if ((cmbTest.isEnabled()) && (tcArray != null)) {
                    String[] sa = tcArray[cmbTest.getSelectedIndex()].split("\\s*,\\s*");
                    if (sa.length > 0) FileContents = INI.setINIValue(FileContents, "hacktv", "input", "test:" + sa[0]);
                }
                else {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "input", "test:colourbars");
                }
            }
            else if ((txtSource.getText().toLowerCase(Locale.ENGLISH).endsWith(".m3u")) ||
                    (txtSource.getText().toLowerCase(Locale.ENGLISH).endsWith(".m3u8"))) {
                // Check if the M3U exists
                if (Files.exists(Path.of(txtSource.getText()))) {
                    // Save the selected item from the Extended M3U file
                    int M3UIndex = cmbM3USource.getSelectedIndex();
                    FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "m3usource", txtSource.getText());
                    FileContents = INI.setIntegerINIValue(FileContents, "hacktv-gui3", "m3uindex", M3UIndex);
                    FileContents = INI.setINIValue(FileContents, "hacktv", "input", playlistURLsAL.get(M3UIndex));    
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
        FileContents = INI.setINIValue(FileContents, "hacktv", "mode", mode);
        // Is this a baseband mode?
        boolean bb = (INI.getINIValue(modesFile, mode, "modulation", "").equals("baseband"));
        // Frequency and channel
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            if ( (!radCustom.isSelected()) && (!bb) ) {
                FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "channel", cmbChannel.getSelectedItem().toString());
                // Save band plan identifier, this uses the section name from modes file
                if (radUHF.isSelected()) {
                    FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "bandplan", uhfAL.get(cmbRegion.getSelectedIndex()));
                }
                else if (radVHF.isSelected()) {
                    FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "bandplan", vhfAL.get(cmbRegion.getSelectedIndex()));
                }
            }
            if (sat) {
                // Save the IF to the frequency field for backwards compatibility
                // The Ku frequency will be retrieved from the band plan if it exists
                long f = calculateFrequency(frequency, false);
                if (f == ((Long.MIN_VALUE + 256))) {
                    return;
                }
                else {
                    FileContents = INI.setLongINIValue(FileContents, "hacktv", "frequency", f);
                    // This setting is not yet used for anything, but we may need it in future
                    FileContents = INI.setINIValue(FileContents, "hacktv-gui3", "satellite", "1");
                }
            }
            else {
                if (!bb) FileContents = INI.setLongINIValue(FileContents, "hacktv", "frequency", frequency);
            }
        }
        // Sample rate
        if (SharedInst.isNumeric(txtSampleRate.getText())) {
            FileContents = INI.setLongINIValue(FileContents, "hacktv", "samplerate", (long) (Double.parseDouble(txtSampleRate.getText()) * 1000000));
        }
        // Pixel rate
        if (SharedInst.isNumeric(txtPixelRate.getText())) {
            FileContents = INI.setLongINIValue(FileContents, "hacktv", "pixelrate", (long) (Double.parseDouble(txtPixelRate.getText()) * 1000000));
        }
        // Offset
        if (SharedInst.isNumeric(txtOffset.getText())) {
            FileContents = INI.setLongINIValue(FileContents, "hacktv", "offset", (long) (Double.parseDouble(txtOffset.getText()) * 1000000));
        }
        // SECAM field ID
        if (chkSecamId.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "secam-field-id", 1);
            int id = cmbSecamIdLines.getSelectedIndex() + 1;
            if ((id >= 1) && (id <= 8)) {
                FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "secam-field-id-lines", id);
            }
        }
        // Swap IQ
        if (chkSwapIQ.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "swap-iq", 1);
        }
        // Gain
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            if (!bb) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "gain", Integer.valueOf(txtGain.getText()));
        }
        // RF Amp
        if (chkAmp.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "amp", 1);
        // Output level
        if (chkOutputLevel.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "level", txtOutputLevel.getText());
        // FM deviation
        if (chkFMDev.isSelected()) FileContents = INI.setLongINIValue(FileContents, "hacktv", "deviation", (long) (Double.parseDouble(txtFMDev.getText()) * 1000000));
        // Gamma
        if (chkGamma.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "gamma", txtGamma.getText());
        // Repeat
        if (chkRepeat.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "repeat", 1);
        // Position
        if (chkPosition.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "position", Integer.valueOf(txtPosition.getText()));
        // Verbose
        if (chkVerbose.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "verbose", 1);
        // Logo
        if (chkLogo.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "logo", logoArray[cmbLogo.getSelectedIndex()]) ;
        // Timestamp
        if (chkTimestamp.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "timestamp", 1);
        // Interlace
        if (chkInterlace.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "interlace", 1);
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
        if (chkWSS.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "wss", cmbWSS.getSelectedIndex() + 1);
        // AR Correction
        if (chkARCorrection.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "arcorrection", cmbARCorrection.getSelectedIndex());
        // Scrambling
        switch (scramblingTypeArray.get(cmbScramblingType.getSelectedIndex())) {
            case (""):
                // Scrambling disabled, do nothing
                break;
            case ("--videocrypt"):
                if (scramblingType2.isEmpty()) {
                    // VideoCrypt I only
                    FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingtype", scramblingType1.substring(2));
                    FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey", scramblingKey1);
                }
                else {
                    // VideoCrypt I+II
                    FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingtype", "videocrypt1+2");
                    FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey", scramblingKey1);
                    FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey2", scramblingKey2);
                }
                break;
            case ("--syster"):
                if (scramblingType2.isEmpty()) {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingtype", scramblingType1.substring(2));
                    FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey", scramblingKey1);
                }
                else {
                    // Syster dual mode (line shuffling + cut-and-rotate)
                    FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingtype", "systerls+cnr");
                    FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey", scramblingKey1);   
                }
                break;
            case ("--single-cut"):
            case ("--double-cut"):
                FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingtype", scramblingType1.substring(2));
                if (!scramblingType2.isEmpty()) {
                    FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey", scramblingType2.substring(2) + '\u0020' + scramblingKey2);
                }
                break;
            default:
                FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingtype", scramblingType1.substring(2));
                FileContents = INI.setINIValue(FileContents, "hacktv", "scramblingkey", scramblingKey1);
                break;
        }
        if (chkActivateCard.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "emm", 1);
        }
        else if (chkDeactivateCard.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "emm", 2);
        }
        if (SharedInst.isNumeric(txtCardNumber.getText())) {
            FileContents = INI.setINIValue(FileContents, "hacktv", "cardnumber", txtCardNumber.getText());
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
                FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "ec-ppv-num", Integer.valueOf(txtECprognum.getText()));
            }
            if (!txtECprogcost.getText().isBlank()) {
                FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "ec-ppv-cost", Integer.valueOf(txtECprogcost.getText()));
            }
        }
        // EuroCrypt "No Date" setting
        if (chkNoDate.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "ec-nodate", 1);
        }
        // Show card serial
        if (chkShowCardSerial.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "showserial", 1);
        // Brute force PPV key
        if (chkFindKeys.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "findkey", 1);
        // Scramble audio
        if (chkScrambleAudio.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "scramble-audio", 1);
        // ACP
        if (chkACP.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "acp", 1);
        // Filter
        if (chkVideoFilter.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "filter", 1);
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
        else if ( (!chkNICAM.isSelected()) && (nicamSupported) ) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "nicam", 0);
        }
        // A2 stereo
        if (chkA2Stereo.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "a2stereo", 1);
        }
        else if ( (!chkA2Stereo.isSelected()) && (a2Supported) ) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "a2stereo", 0);
        }
        // Show ECMs
        if (chkShowECM.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "showecm", 1);
        // Subtitles
        if (chkSubtitles.isSelected()) { 
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "subtitles", 1); 
            FileContents = INI.setINIValue(FileContents, "hacktv", "subtitleindex", txtSubtitleIndex.getText());
        }
        // VITS
        if (chkVITS.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "vits", 1);
        // VITC
        if (chkVITC.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "vitc", 1);
        // SiS
        if (chkSiS.isSelected()) {
            FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "sis", 1);
            // This setting has been added for possible future use but is not currently read
            FileContents = INI.setINIValue(FileContents, "hacktv", "sismode", "dcsis");
        }
        // Disable colour
        if (chkColour.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "nocolour", 1);
        // S-Video
        if (chkSVideo.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "s-video", 1);
        // Closed captioning
        if (chkCC608.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "cc608", 1);
        // Invert video
        if (chkInvertVideo.isSelected()) FileContents = INI.setIntegerINIValue(FileContents, "hacktv", "invert-video", 1);
        // MAC channel ID
        if (chkMacChId.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "chid", txtMacChId.getText());
        // MAC audio options
        if (chkMacMono.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "mac-audio-mode", "mono");
        if (chkMac16k.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "mac-audio-quality", "medium");
        if (chkMacLinear.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "mac-audio-compression", "linear");
        if (chkMacL2.isSelected()) FileContents = INI.setINIValue(FileContents, "hacktv", "mac-audio-protection", "l2");
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
        // Philips test signals
        if (isPhilipsTestSignal()) {
            String sa[] = tcArray[cmbTest.getSelectedIndex()].split("\\s*,\\s*");
            if (sa.length > 0) FileContents = INI.setINIValue(FileContents, "hacktv", "testsignal", sa[0]);
        }
        // The playlist doesn't follow a standard INI format. We just dump the
        // playlist array into the file as-is.
        if (!playlistAL.isEmpty()) {
            var sb = new StringBuilder();
            FileContents = FileContents + "\n\n[playlist]\n";
            for (int i = 1; i <= playlistAL.size(); i++) {
                sb.append(playlistAL.get(i - 1)).append("\n");
            }
            FileContents = FileContents + sb.toString();
        }
        // Commit to disk
        try {
            Files.writeString(DestinationFileName.toPath(), FileContents, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            messageBox("An error occurred while writing to this file. "
                    + "The file may be read-only or you may not have the correct permissions.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Display the opened filename in the title bar
        // Back up the original title once
        if (!titleBarChanged) { 
            titleBar = this.getTitle();
            titleBarChanged = true;
        }
        this.setTitle(titleBar + " - " + DestinationFileName.getName());
       // Remove the ellipsis after Save to follow standard UI guidelines
        menuSave.setText("Save");
        updateMRUList(DestinationFile);
    }
         
    private void m3uHandler(String SourceFile, int M3UIndex) {
        /** Basic M3U file parser.
        *   The M3UIndex variable is an integer value which specifies the index
        *   number to select in the combobox.
        */
        File f = new File(SourceFile);
        // Prompt if the file is larger than 5 MB as it could take a while...
        if (f.length() > 5242880) {
            if (JOptionPane.showConfirmDialog(null,
                    "The selected file is quite large and may take a long time to open.\n" +
                    "Do you want to continue anyway?",
                    APP_NAME, JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                txtSource.setText("");
                return;
            }
        }
        String fileHeader;
        try (var br2 = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
            try (var lnr2 = new LineNumberReader(br2)) {
                fileHeader = lnr2.readLine();
            }
        }
        catch (MalformedInputException mie) {
            // Retry using ISO-8859-1
            try (var br3 = new BufferedReader(new FileReader(f, StandardCharsets.ISO_8859_1))) {
                try (var lnr3 = new LineNumberReader(br3)) {
                    fileHeader = lnr3.readLine();
                }
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
        // We use 'contains' rather than 'startsWith' to avoid problems caused by Unicode BOMs
        else if (!fileHeader.contains("#EXTM3U") ) {
            boolean utf8;
            // Treat the file as a standard text-only playlist
            List<String> pls;
            try {
                pls = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
                utf8 = true;
            }
            catch (MalformedInputException mie){
                // Retry using ISO-8859-1
                try {
                    pls = Files.readAllLines(f.toPath(), StandardCharsets.ISO_8859_1);
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
            var toRemove = new ArrayList<String>();
            String removed = "";
            // Run through the imported playlist to check if the paths exist
            int i = 0;
            for (String file : pls) {
                // Skip if this is a URL or test card
                if ( (!file.startsWith("http:")) &&
                        (!file.startsWith("https:")) &&
                        (!file.startsWith("test:")) ) {
                    if (!Files.exists(Path.of(file))) {
                        if ( (!utf8) && (runningOnWindows)) {
                            // We may have encountered an encoding bug so retry
                            // by converting the files string to MS-DOS CP 850
                            try {
                                var f850 = new String(file.getBytes("ISO-8859-1"), "CP850");
                                if (Files.exists(Path.of(f850))) {
                                    // Worked! Change the item in pls.
                                    var f8 = new String(f850.getBytes("UTF-8"), "UTF-8");
                                    pls.set(i, f8);
                                }
                                else {
                                    // Add the item to the "to remove" list
                                    // Also add it to the removed string so we can present it to the user
                                    toRemove.add(file);
                                    removed = removed + file + "\n";
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
            if (!toRemove.isEmpty()) {
                pls.removeAll(toRemove);
                messageBox("Some files could not be found and have been removed from the playlist.\n" +
                        removed, JOptionPane.WARNING_MESSAGE);
            }
            // Add the imported playlist to playlistAL and populate
            playlistAL.addAll(pls);
            populatePlaylist();
            resetM3UItems(false);
            return;
        }
        // Handler for Extended M3U files (with #EXTM3U header)
        // Set mouse cursor to busy
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Clear playlistURLsAL if it's not empty
        if (!playlistURLsAL.isEmpty()) playlistURLsAL.clear();
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
        var d = new Dimension(360,22);
        cmbM3USource.setPreferredSize(d);
        // Remove any existing items from the combobox
        cmbM3USource.removeAllItems();
        cmbM3USource.addItem("Loading playlist file, please wait...");
        // Create a SwingWorker to do the disruptive stuff
        var m3uWorker = new SwingWorker<Boolean, Double>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                String M3UNames = "";
                var playlistNamesAL = new ArrayList<String>();
                try {
                    // Read source file to a list.
                    List<String> fl = Files.readAllLines(f.toPath());
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
                            playlistURLsAL.add(l);
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
                    playlistNamesAL.add(m.group(1));
                }
                // Check that we got something, if not then stop.
                if (playlistNamesAL.isEmpty()) {
                    return false;
                }
                else {
                    // Check for duplicate entries in the ArrayList because
                    // Swing doesn't handle them too well.
                    // We append the index number as a workaround.
                    // This could probably be done better.
                    for (int i = 0, j = 0; i < playlistNamesAL.size(); i++) {
                        if (i != j) {
                            if ( playlistNamesAL.get(j).equals(playlistNamesAL.get(i)) ) {
                                playlistNamesAL.set(i, playlistNamesAL.get(i) + " #" + i);
                            }
                        }
                        if (i == playlistNamesAL.size() -1) {
                            j++;
                            if ( j == playlistNamesAL.size() ) break;
                            i = 0;
                        }
                    }
                    // Convert ArrayList to an array so we can populate the combobox
                    playlistNames = new String[playlistNamesAL.size()];
                    for (int i = 0; i < playlistNamesAL.size(); i++) {
                        playlistNames[i] = playlistNamesAL.get(i);
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
                    cmbM3USource.setModel(new DefaultComboBoxModel <> (playlistNames));
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
        for(JCheckBox cb: checkBoxes){
            if ( cb.isSelected() ) cb.doClick();
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
        startPoint = -1;
        // Select default radio buttons and comboboxes
        cmbRegion.setEnabled(false);
        radLocalSource.doClick();
        radPAL.doClick();
        radUHF.doClick();
        cmbScramblingType.setSelectedIndex(0);
        // Reset gain to zero
        txtGain.setText("0");
        // Re-enable audio option
        if (! chkAudio.isSelected() ) chkAudio.doClick();
        // Clear playlist
        btnRemove.setEnabled(false);
        btnPlaylistUp.setEnabled(false);
        btnPlaylistDown.setEnabled(false);
        playlistAL.clear();
        populatePlaylist();
        // Restore title bar to default
        if (titleBarChanged) this.setTitle(titleBar);
        // Restore ellipsis to Save option
        if (menuSave.getText().equals("Save")) menuSave.setText("Save...");
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
        int i = PREFS.getInt("ceefaxregion", 9);
        if ( (i + 1 <= cmbNMSCeefaxRegion.getItemCount()) && (i >= 0) ) {
            cmbNMSCeefaxRegion.setSelectedIndex(i);
        }
        else {
            cmbNMSCeefaxRegion.setSelectedIndex(9);
        }
    }

    private void downloadTeletext(String url, String destinationFile, String regex) {
        var TeletextLinks = new ArrayList<String>();
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Downloads the specified file from URL and saves it to DestinationFile
        // Create temp directory if it does not exist
        createTempDirectory();
        // Specify the destination location of the HTML file we will download
        String DownloadPath = tempDir + File.separator + destinationFile;
        try {
            downloadInProgress = true;
            // If the file already exists from a previous attempt, delete it
            var f = new File(DownloadPath);
            if (f.exists()) Shared.deleteFSObject(f.toPath());
            // Download the index page
            txtStatus.setText("Downloading index page from " + url);
            SharedInst.download(url, DownloadPath);
        }
        catch (IOException | URISyntaxException ex) {
            System.err.println(ex);
            messageBox("An error occurred while downloading files. "
                    + "Please ensure that you are connected to the internet and try again.", JOptionPane.ERROR_MESSAGE);
            txtStatus.setText("Cancelled");
            resetTeletextButtons();
            return;
        }
        // Create a SwingWorker to do the disruptive stuff
        var downloadPages = new SwingWorker<Integer, Integer>() {
            @Override
            protected Integer doInBackground() throws Exception {
                File f;
                Path fd = Paths.get(tempDir + File.separator + htmlTempFile);
                String dUrl = url;
                // Try to read the downloaded index file to a string
                try {
                    htmlFile = Files.readString(fd);
                }
                catch (IOException ex) {
                    System.err.println(ex);
                }
                // Search the string for the pattern defined in the teletext button
                Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(htmlFile);
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
                    case "https://api.github.com/repos/spark-teletext/spark-teletext/contents/":
                        // Set SPARK prerequisites - change URL first
                        dUrl = "https://raw.githubusercontent.com/spark-teletext/spark-teletext/master/";
                        f = new File(tempDir + File.separator + "spark");
                        break;
                    case "https://feeds.nmsni.co.uk/svn/ceefax/national/":
                        // Set Ceefax temp directory
                        f = new File(tempDir + File.separator + "ceefax");
                        break;
                    case "http://teastop.plus.com/svn/teletext/":
                        // Set Teefax temp directory
                        f = new File(tempDir + File.separator + "teefax");
                        break;
                    default:
                        if (dUrl.startsWith("https://internal.nathanmediaservices.co.uk/svn/ceefax/")) {
                            f = new File(tempDir + File.separator + "ceefax");
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
                        Shared.deleteFSObject(f.toPath());
                    }
                    catch (IOException ex) {
                        System.err.println(ex);
                    }
                }
                // Create download directory
                if (!f.isDirectory()) {
                    if (!f.mkdirs()) System.err.println("Unable to create directory " + f.toString());
                }
                teletextPath = f.toString();

                for(int i = 0; i < TeletextLinks.size(); i++) {
                    int j = i+1;
                    if ( TeletextLinks.get(i).endsWith("tti") || TeletextLinks.get(i).endsWith("ttix") ) {
                        try {
                            // If the Stop button has been pressed, then stop
                            if (downloadCancelled) {
                                downloadCancelled = false;
                                downloadInProgress = false;
                                return 1;
                            }
                            publish(j);
                            // Do the actual downloading
                            SharedInst.download(dUrl + TeletextLinks.get(i), teletextPath + File.separator + TeletextLinks.get(i));
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
                        txtTeletextSource.setText(teletextPath);
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
        txtTeletextSource.setEditable(true);
        btnTeletextBrowse.setEnabled(true);
        btnRun.setEnabled(true);
        // Reset hacktv download button
        if (runningOnWindows) btnDownloadHackTV.setEnabled(true);
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
        downloadInProgress = false;
    }
    
    private void downloadHackTV_Win32(String dUrl) {
        // Downloads the latest pre-compiled Windows build from my build server
        // The download URL is sent here from the download dialogue
        btnDownloadHackTV.setText("Cancel");
        // Disable Teletext download options so they don't interfere
        if (chkTeletext.isSelected()) {
            btnTeefax.setEnabled(false);
            btnSpark.setEnabled(false);
            btnNMSCeefax.setEnabled(false);
            lblTeefax.setEnabled(false);
            lblSpark.setEnabled(false);
            lblNMSCeefax.setEnabled(false);
            lblDownload.setEnabled(false);
            downloadPanel.setEnabled(false);            
        }
        downloadInProgress = true;
        txtStatus.setText("Connecting to " + dUrl);
        var downloadHackTV = new SwingWorker<String, Integer>() {
            long p;
            long size;
            @Override
            protected String doInBackground() throws Exception {
                createTempDirectory();
                String t = tempDir.toString();
                String downloadPath = t + File.separator + "hacktv.zip";
                String tmpExePath = t + File.separator + "hacktv.exe";
                String exePath = jarDir + File.separator + "hacktv.exe";
                String readmePath = t + File.separator + "readme.txt";
                var testSignalPath = Path.of(t + File.separator + "testsignals");
                var con = new URI(dUrl).toURL().openConnection();
                size = con.getContentLengthLong();
                try (var in = new BufferedInputStream(con.getInputStream());
                    var out = new FileOutputStream(downloadPath)) {
                    byte buffer[] = new byte[1024];
                    int b;
                    while (((b = in.read(buffer, 0, 1024)) != -1) && (!downloadCancelled)) {
                        publish(b);
                        out.write(buffer, 0, b);
                    }
                    out.close();
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
                if (downloadCancelled) {
                    // Delete the partially downloaded file and return
                    Files.deleteIfExists(Path.of(downloadPath));
                    return "";
                }
                else {
                    // Unzip what we got to the temp directory
                    SharedInst.unzipFile(downloadPath, t);
                    // If hacktv.exe exists in the temp directory, attempt to
                    // move it to the working directory
                    if (Files.exists(Path.of(tmpExePath))) {
                        // Delete the readme file that was extracted from the zip
                        if (Files.exists(Path.of(readmePath))) {
                            Shared.deleteFSObject(Path.of(readmePath));
                        }
                        Files.move(Path.of(tmpExePath), Path.of(exePath), StandardCopyOption.REPLACE_EXISTING);
                        // If downloading a build with included test signals
                        if (Files.exists(testSignalPath) && Files.isDirectory(testSignalPath)) {
                            var tsd = Path.of(jarDir + File.separator + "testsignals");
                            if (!Files.exists(tsd)) Files.createDirectory(tsd);
                            if (Files.isDirectory(tsd)) {
                                var d = testSignalPath.toFile().listFiles();
                                if (d != null) {
                                    for (File f : d) {
                                        Files.move(
                                                f.toPath(),
                                                Path.of(tsd + File.separator + f.getName()),
                                                StandardCopyOption.REPLACE_EXISTING
                                        );
                                    }
                                }
                                PREFS.put("testdir", tsd.toString());
                                Shared.deleteFSObject(testSignalPath);
                            }
                        }
                        // Clean up temp directory
                        Shared.deleteFSObject(Path.of(downloadPath));
                        return exePath;
                    }
                    else {
                        return null;
                    }
                }
            } // End doInBackground()
            @Override
            protected void process(List<Integer> c) {
                for (int i : c) {
                    p = p + i;
                    double d = (double) p / size * 100;
                    txtStatus.setText("Downloading: " + (int) d + "%");
                    if (Taskbar.isTaskbarSupported()) {
                        var t = Taskbar.getTaskbar();
                        if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
                            t.setWindowProgressValue(GUI.this, (short) d);
                        }
                        else if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                            t.setProgressValue((short) d);
                        }
                    } 
                }
            }
            @Override
            protected void done() {
                downloadInProgress = false;
                if (!btnDownloadHackTV.isEnabled()) btnDownloadHackTV.setEnabled(true);
                btnDownloadHackTV.setText("Download...");
                // Re-enable Teletext download options
                if (chkTeletext.isSelected()) {
                    btnTeefax.setEnabled(true);
                    btnSpark.setEnabled(true);
                    btnNMSCeefax.setEnabled(true);
                    lblTeefax.setEnabled(true);
                    lblSpark.setEnabled(true);
                    lblNMSCeefax.setEnabled(true);
                    lblDownload.setEnabled(true);
                    downloadPanel.setEnabled(true);                    
                }
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
                // Retrieve the return value of doInBackground.
                String exePath;
                try {
                    exePath = get();
                }
                catch (InterruptedException | ExecutionException ex) {
                    System.err.println(ex);
                    exePath = null;
                }
                if (exePath == null) {
                    messageBox("An error occurred while downloading hacktv.\n"
                            + "Please ensure that you have write permissions to the "
                            + "application directory and that you have internet access.", JOptionPane.WARNING_MESSAGE);
                    txtStatus.setText("Failed");
                    downloadCancelled = false;
                }
                else if (exePath.isEmpty()) {
                    txtStatus.setText("Cancelled");
                    downloadCancelled = false;
                }
                else if (exePath.equals("CertificateExpiredException")) {
                    messageBox("Download failed due to an expired SSL/TLS certificate.\n"
                            + "Please ensure that your system date is correct. "
                            + "Otherwise, please try again later.", JOptionPane.WARNING_MESSAGE);
                }
                else {
                    // Set location of hacktv so we can find it later
                    if (Files.exists(Path.of(exePath))) {
                        hackTVPath = exePath;
                        txtHackTVPath.setText(exePath);
                        // Store the specified path in the preferences store.
                        PREFS.put("hacktvpath", hackTVPath);
                        // Load the full path to a variable so we can use getParent on it
                        // and get its parent directory path
                        hackTVDirectory = new File(hackTVPath).getParent();    
                        // Detect what were provided with
                        detectFork();
                        selectModesFile();
                        if (captainJack) {
                            captainJack();
                        }
                        else {
                            fsphil();
                        }
                        addTestCardOptions();
                        txtStatus.setText("Done");
                    }
                }
            } // End done()
        }; // End SwingWorker
        downloadHackTV.execute();
    }
    
    private void enableScrambling() {
        cmbScramblingType.setEnabled(true);
        lblScramblingSystem.setEnabled(true);
        scramblingPanel.setEnabled(true);
    }    
    
    private void disableScrambling() {
        var ScramblingTypeAL = new ArrayList<String>();
        ScramblingTypeAL.add("No scrambling");
        scramblingTypeArray = new ArrayList<>();
        scramblingTypeArray.add("");
        cmbScramblingType.removeAllItems();
        // Convert to an array so we can populate
        var ScramblingType = new String[ScramblingTypeAL.size()];
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
        var ScramblingTypeAL = new ArrayList<String>();
        ScramblingTypeAL.add("No scrambling");
        scramblingTypeArray = new ArrayList<>();
        scramblingTypeArray.add("");
        dualVC = -2;
        // Check if modes file contains a section for these scrambling systems
        String vc1 = INI.splitINIfile(modesFile, "videocrypt");
        String vc2 = INI.splitINIfile(modesFile, "videocrypt2");
        String vcs = INI.splitINIfile(modesFile, "videocrypts");
        String syster = INI.splitINIfile(modesFile, "syster");
        if (vc1 != null) {
            ScramblingTypeAL.add("VideoCrypt I");
            scramblingTypeArray.add("--videocrypt");
        }
        if (vc2 != null) {
            ScramblingTypeAL.add("VideoCrypt II");
            scramblingTypeArray.add("--videocrypt2");
        }
        if ((vc1 != null) && (vc2 != null)) {
            ScramblingTypeAL.add("VideoCrypt I+II");
            scramblingTypeArray.add("--videocrypt");
            // Specify that both key fields should be enabled for this system
            dualVC = scramblingTypeArray.size() - 1;
        }
        if (vcs != null) {
            ScramblingTypeAL.add("VideoCrypt S");
            scramblingTypeArray.add("--videocrypts");
        }
        if (syster != null) {
            ScramblingTypeAL.add("Nagravision Syster");
            scramblingTypeArray.add("--syster");
            ScramblingTypeAL.add("Discret 11");
            ScramblingTypeAL.add("Nagravision Syster (cut-and-rotate mode)");
            ScramblingTypeAL.add("Nagravision Syster (line shuffle and cut-and-rotate modes)");
            scramblingTypeArray.add("--d11");
            scramblingTypeArray.add("--systercnr");
            scramblingTypeArray.add("--syster");
        }
        if (ScramblingTypeAL.size() == 1) {
            // No systems found, disable the scrambling tab
            disableScrambling();
        }
        cmbScramblingType.removeAllItems();
        // Convert to an array so we can populate
        var ScramblingType = new String[ScramblingTypeAL.size()];
        for (int i = 0; i < ScramblingType.length; i++) {
            ScramblingType[i] = ScramblingTypeAL.get(i);
        }
        cmbScramblingType.setModel(new DefaultComboBoxModel<>(ScramblingType));
        cmbScramblingType.setSelectedIndex(0);
    }
    
    private void addMACScramblingTypes() {
        var ScramblingTypeAL = new ArrayList<String>();
        scramblingTypeArray = new ArrayList<>();
        ScramblingTypeAL.add("No scrambling");
        scramblingTypeArray.add("");
        dualVC = -2;
        if (INI.splitINIfile(modesFile, "eurocrypt") != null) {
            ScramblingTypeAL.add("Single cut");
            ScramblingTypeAL.add("Double cut");
            scramblingTypeArray.add("--single-cut");
            scramblingTypeArray.add("--double-cut");            
        }
        if (ScramblingTypeAL.size() == 1) {
            // No systems found, disable the scrambling tab
            disableScrambling();
        }
        cmbScramblingType.removeAllItems();
        // Convert to an array so we can populate
        var ScramblingType = new String[ScramblingTypeAL.size()];
        for(int i = 0; i < ScramblingType.length; i++) {
            ScramblingType[i] = ScramblingTypeAL.get(i);
        } 
        cmbScramblingType.setModel(new DefaultComboBoxModel<>(ScramblingType));
        cmbScramblingType.setSelectedIndex(0);
    }
    
    private void addScramblingKey() {
        // In the clear (no scrambling)
        if (scramblingType1.isEmpty()) {
            scramblingOptionsPanel.setEnabled(false);
            emmPanel.setEnabled(false);
            disableScramblingKey1();
            cmbScramblingKey1.setSelectedIndex(-1);
            disableScramblingKey2();
            scramblingType2 = "";
            scramblingKey1 = "";
            scramblingKey2 = "";
            configureScramblingOptions();
            txtSampleRate.setText(defaultSampleRate);
            if (chkPixelRate.isSelected()) chkPixelRate.doClick();
            return;
        }
        else {
            enableScramblingKey1();
            scramblingOptionsPanel.setEnabled(true);
            emmPanel.setEnabled(true);    
        }
        // Get the scrambling system name  
        String sconf = scramblingTypeArray.get(cmbScramblingType.getSelectedIndex()).substring(2);
        switch (sconf) {
            case "videocrypt":
                // Set pixel rate to 28 MHz (multiples of 14 are OK)
                if ((!chkPixelRate.isSelected()) && !htvLoadInProgress) chkPixelRate.doClick();
                if (!htvLoadInProgress) txtPixelRate.setText("28");
                disableScramblingKey2();
                sconf = "videocrypt";
                break;
            case "videocrypt2":
                // Set pixel rate to 28 MHz (multiples of 14 are OK)
                if ((!chkPixelRate.isSelected()) && !htvLoadInProgress) chkPixelRate.doClick();
                if (!htvLoadInProgress) txtPixelRate.setText("28");
                disableScramblingKey2();
                sconf = "videocrypt2";
                break;
            case "videocrypts":
                disableScramblingKey2();
                // Set pixel rate to 17.75 MHz (more accurately 17.734475 but
                // this is reported by hacktv as unsuitable for 625/50)
                if ((!chkPixelRate.isSelected()) && !htvLoadInProgress) chkPixelRate.doClick();
                if (!htvLoadInProgress) txtPixelRate.setText("17.75");
                sconf = "videocrypts";
                break;
            case "syster":
                // No pixel rate required for Syster
                disableScramblingKey2();
                sconf = "syster";
                break;
            case "d11":
            case "systercnr":
                // Set pixel rate to 17.75 MHz
                if ((!chkPixelRate.isSelected()) && !htvLoadInProgress) chkPixelRate.doClick();
                if (!htvLoadInProgress) txtPixelRate.setText("17.75");
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
        // Extract (from modesFile) the scrambling key section that we need
        String slist = INI.splitINIfile(modesFile, sconf);
        // If the INI section is present but no data is contained in it, stop
        if ((slist.trim().lines().count()) <= 1) {
            scramblingType1 = "";
            cmbScramblingType.setSelectedIndex(0);
            addScramblingKey();
            messageBox("The scrambling key information in " + getFork() + ".ini appears to be "
                    + "missing or corrupt for the selected scrambling type.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        else {
            // We just want the commands so remove everything after =
            slist = slist.replaceAll("\\=.*", "");
        }
        // Add commands to an ArrayList
        scramblingKeyArray = new ArrayList<>();
        scramblingKeyArray.addAll(Arrays.asList(slist.substring(slist.indexOf("\n") + 1).split("\\r?\\n")));
        // Extract friendly names and populate the combobox with them
        var skn = new String[scramblingKeyArray.size()];
        for (int i = 0; i < scramblingKeyArray.size(); i++) {
            skn[i] = INI.getStringFromINI(modesFile , sconf, scramblingKeyArray.get(i), "", true);
        }
        // Populate key 1 combobox
        cmbScramblingKey1.setModel(new DefaultComboBoxModel<>(skn));
        cmbScramblingKey1.setSelectedIndex(0);
        
        // VC1+2 dual mode
        if (cmbScramblingType.getSelectedIndex() == dualVC) {
            String sconf2 = "videocrypt2";
            if (captainJack) {
                enableScramblingKey1();
                enableScramblingKey2();
            }
            else {
                disableScramblingKey1();
                disableScramblingKey2();
            }
            cmbScramblingKey2.removeAllItems();
            // Extract (from modesFile) the VC2 scrambling key section
            String slist2 = INI.splitINIfile(modesFile, sconf2);
            if ((slist2.trim().lines().count()) <= 1) {
                scramblingType1 = "";
                cmbScramblingType.setSelectedIndex(0);
                addScramblingKey();
                messageBox("The scrambling key information in " + getFork() + ".ini appears to be "
                        + "missing or corrupt for the secondary scrambling type.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // We just want the commands so remove everything after =
            slist2 = slist2.replaceAll("\\=.*", "");
            // Add commands to an ArrayList
            scramblingKey2Array = new ArrayList<>();
            scramblingKey2Array.addAll(Arrays.asList(slist2.substring(slist2.indexOf("\n") + 1).split("\\r?\\n")));
            // Extract friendly names and populate the combobox with them
            var skn2 = new String[scramblingKey2Array.size()];
            for (int i = 0; i < scramblingKey2Array.size(); i++) {
                skn2[i] = INI.getStringFromINI(modesFile , sconf2, scramblingKey2Array.get(i), "", true);
            }
            // Populate key 2 combobox
            cmbScramblingKey2.setModel(new DefaultComboBoxModel<>(skn2));
            cmbScramblingKey2.setSelectedIndex(0);
        }
    }
    
    private void configureScramblingOptions() {
        // Enable the Scramble audio option if supported
        if ( ((scramblingType1).equals("--single-cut")) || 
                ((scramblingType1).equals("--double-cut")) ||
                ((scramblingType1).equals("--syster")) ||
                ((scramblingType1).equals("--d11")) ||
                ((scramblingType1).equals("--systercnr")) ) {
            chkScrambleAudio.setEnabled(true);
        }
        else {
            if (chkScrambleAudio.isSelected()) {
                chkScrambleAudio.doClick();
            }
            chkScrambleAudio.setEnabled(false);
        }
        // Enable EuroCrypt maturity rating
        if (scramblingType2.equals("--eurocrypt")) {
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
        if (scramblingType2.equals("--eurocrypt")) {
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
        if ( ((scramblingType1).equals("--videocrypt")) || 
                ((scramblingType1).equals("--videocrypt2")) ) {
            if (captainJack) chkShowCardSerial.setEnabled(true);
        }
        else {
            if (chkShowCardSerial.isSelected()) {
                chkShowCardSerial.doClick();
            }
            chkShowCardSerial.setEnabled(false);
        }
        // Enable EMM options on supported modes
        boolean emmSupported;
        if (scramblingType1.equals("--videocrypt")) {
            switch (scramblingKey1) {
                case "sky06":
                case "sky07":
                case "sky09":
                case "skynz01":
                case "skynz02":
                    emmSupported = true;
                    break;
                default:
                    emmSupported = false;
                    break;
            }
        }
        else emmSupported = (scramblingType1.equals("--videocrypt2")) && scramblingKey1.equals("conditional");
        if (emmSupported) {
            chkActivateCard.setEnabled(true);
            chkDeactivateCard.setEnabled(true);
        }
        else {
            if (chkActivateCard.isSelected()) chkActivateCard.doClick();
            if (chkDeactivateCard.isSelected()) chkDeactivateCard.doClick();
            chkActivateCard.setEnabled(false);
            chkDeactivateCard.setEnabled(false);
        }
        // Enable PPV findkey option
        if ( (scramblingKey1.equals("ppv")) ) {
            chkFindKeys.setEnabled(true);
        }
        else {
            if ( chkFindKeys.isSelected()) chkFindKeys.doClick();
            chkFindKeys.setEnabled(false);
        }
        // Enable permutation table options (Syster-based modes)
        if ( ((scramblingType1).equals("--syster")) || (scramblingType1).equals("--systercnr")) {
            lblSysterPermTable.setEnabled(true);
            cmbSysterPermTable.setEnabled(true);
            cmbSysterPermTable.setSelectedIndex(0);
        }
        else {
            lblSysterPermTable.setEnabled(false);
            cmbSysterPermTable.setEnabled(false);
            cmbSysterPermTable.setSelectedIndex(-1);
        }
        // Enable ECM option and disable ACP if not supported
        if (cmbScramblingType.getSelectedIndex() == 0) {
            if ( chkShowECM.isSelected() ) chkShowECM.doClick();
            chkShowECM.setEnabled(false);
            if (acpSupported) {
                enableACP();
            }
            else {
                disableACP();
            }
        }
        else if (radMAC.isSelected()) {
            chkShowECM.setEnabled(true);
        }
        else if (captainJack) {
            chkShowECM.setEnabled(true);
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
        if (chkWSS.isSelected()) chkWSS.doClick();
        chkWSS.setEnabled(false);
    }
    
    private void populateWSS() {
        var WSSModesAL = new ArrayList<String>();
        WSSModesAL.add("auto");
        WSSModesAL.add("4:3");
        WSSModesAL.add("14:9 letterbox");
        WSSModesAL.add("14:9 top");
        WSSModesAL.add("16:9 letterbox");
        WSSModesAL.add("16:9 top");
        WSSModesAL.add("16:9+-letterbox");
        WSSModesAL.add("14:9 window");
        WSSModesAL.add("16:9");
        // Convert to an array so we can populate
        var WSSModes = new String[WSSModesAL.size()];
        for(int i = 0; i < WSSModes.length; i++) {
            WSSModes[i] = WSSModesAL.get(i);
        } 
        cmbWSS.removeAllItems();
        cmbWSS.setModel(new DefaultComboBoxModel<>(WSSModes));
        cmbWSS.setSelectedIndex(0);
    }
    
    private ArrayList<String> checkWSS() {
        // Populate WSS parameters if enabled
        var al = new ArrayList<String>();
        if (chkWSS.isSelected()) {
            var wssModes = new String[] {
                "auto",
                "4:3",
                "14:9-letterbox",
                "14:9-top",
                "16:9-letterbox",
                "16:9-top",
                "16:9+-letterbox",
                "14:9-window",
                "16:9"
            };
            al.add("--wss");
            al.add(wssModes[cmbWSS.getSelectedIndex()]);
        }
        return al;
    }
    
    private void addARCorrectionOptions() {
        var ARModesAL = new ArrayList<String>();
        if (!captainJack) {
            ARModesAL.add("Stretched");
            ARModesAL.add("Fit");
            ARModesAL.add("Fill");
            ARModesAL.add("None");
        }
        else {
            ARModesAL.add("Stretched");
            ARModesAL.add("Letterboxed");
            ARModesAL.add("Cropped");
        }
        // Convert to an array so we can populate
        var ARCorrectionMode = new String[ARModesAL.size()];
        for(int i = 0; i < ARCorrectionMode.length; i++) {
            ARCorrectionMode[i] = ARModesAL.get(i);
        } 
        cmbARCorrection.removeAllItems();
        cmbARCorrection.setModel(new DefaultComboBoxModel<>(ARCorrectionMode));
        cmbARCorrection.setSelectedIndex(0);
    }
    
    private ArrayList<String> checkARCorrectionOptions() {
        var al = new ArrayList<String>();
        if (chkARCorrection.isSelected()) {
            if (!captainJack) {
                switch (cmbARCorrection.getSelectedIndex()) {
                    case 1:
                        al.add("--fit");
                        al.add("fit");
                        break;
                    case 2:
                        al.add("--fit");
                        al.add("fill");
                        break;
                    case 3:
                        al.add("--fit");
                        al.add("none");
                        break;
                    default:
                        break;
                }
            } else {
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

        }
        return al;
    }
    
    private void addLogoOptions() {
        // Extract (from modesFile) the logo list
        String logos = INI.splitINIfile(modesFile, "logos");
        if (logos == null) {
            // If nothing was found, disable the logo options and stop
            if (chkLogo.isSelected()) chkLogo.doClick();
            chkLogo.setEnabled(false);
            return;
        }
        // We just want the commands so remove everything after =
        logos = logos.replaceAll("\\=.*", "");          
        // Add a headerless string to logoArray by splitting off the first line
        logoArray = logos.substring(logos.indexOf("\n") +1).split("\\r?\\n");
        // Populate LogoNames by reading modesFile using what we added
        // to logoArray.
        var LogoNames = new String[logoArray.length];
        for (int i = 0; i < logoArray.length; i++) {
            LogoNames[i] = (INI.getStringFromINI(modesFile, "logos", logoArray[i], "", true));
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
            al.add(logoArray[cmbLogo.getSelectedIndex()]);
        }
        return al;
    }
    
    private void addTestCardOptions() {
        // Reads the available test cards/signals/patterns from the modes file
        String t;
        if ((tcArray == null) || (tcArray.length == 0)) tcArray = new String[0];
        if (captainJack) {
            // Extract (from modesFile) the test card list
            t = INI.splitINIfile(modesFile, "testcards");
        }
        else if (mattstvbarn) {
            // PM5644/PT8631 emulation
            String c = getSelectedColourSystem();
            if (c.isEmpty()) {
                // Unsupported mode, set to null (we'll catch it later)
                t = null;
            }
            else {
                // Extract (from modesFile) the test signal list (e.g. [testsignals_625_pal])
                // This will return null if the section does not exist
                t = INI.splitINIfile(modesFile, "testsignals" + "_" + Integer.toString(lines) + "_" + c);
            }
        }
        else {
            t = null;
        }
        if (t == null) {
            // If nothing was found, disable the test card combobox
            // Use a dummy string to preserve the length of the combobox
            // This won't be seen as the combobox is disabled
            String[] n = {"No items found"};
            cmbTest.setEnabled(false);
            cmbTest.removeAllItems();
            cmbTest.setModel(new DefaultComboBoxModel<>(n));
            cmbTest.setSelectedIndex(-1);
        }
        else {
            // Variable for storing pattern directory location
            String p = PREFS.get("testdir", hackTVDirectory);
            // Remove the first line from t (the INI header) and split each line to tcArray
            tcArray = t.replace("=", ",").substring(t.indexOf("\n") +1).split("\n");
            /* tcArray contains five pieces of data, separated by commas:
               [0]      [1]           [2]               [3]                 [4]
               Command, Display name, Pattern filename, Text/clock support, Sample rate
             */
            String[] tcNames;
            if (captainJack) {
                // Populate tcNames by adding element 1 of tcArray
                tcNames = new String[tcArray.length];
                for (int i = 0; i < tcArray.length; i++) {
                    String[] sa = tcArray[i].split(",");
                    if (sa.length > 1) tcNames[i] = sa[1];
                }
            }
            else if ((mattstvbarn) && (Files.exists(Path.of(p + File.separator + "pm8546g.bin")))) {
                // PM5644/PT8631 emulation continued
                var tal = new ArrayList<String>();
                for (String s : tcArray) {
                    // Split tcArray to another array and check the existence of
                    // the third element of each item (the filename).
                    String[] sa = s.split("\\s*,\\s*");
                    if (sa.length > 2) {
                        if (Files.exists(Path.of(p + File.separator + sa[2]))) {
                            // Add command line argument to temp tal ArrayList
                            tal.add(sa[0]);
                        }
                        else {
                            System.err.println("Unable to add test pattern " + sa[0] + ".\n"
                                    + "The file " + sa[2] + " could not be found.");
                        }
                    }
                    else {
                        // Treat this as an internal hacktv pattern (not emulated Philips)
                        // Add command line argument to temp tal ArrayList
                        tal.add(sa[0]);
                    } 
                }
                // If the number of elements in tcArray and tal do not match,
                // then something was removed.
                // We need to repopulate tcArray with the correct data.
                if (tcArray.length != tal.size()) {
                    tcArray = new String[tal.size()];
                    for (int i = 0; i < tal.size(); i++) {
                        // Query the modes file to get the missing parameters back
                        String s = INI.getStringFromINI(
                                modesFile,
                                "testsignals" + "_" + Integer.toString(lines) + "_" + getSelectedColourSystem(),
                                tal.get(i),
                                "",
                                true
                        );
                        if (!s.isBlank()) tcArray[i] = tal.get(i) + "," + s;
                    }
                }
                // Populate tcNames by adding element 1 of tcArray
                tcNames = new String[tcArray.length];
                for (int i = 0; i < tcArray.length; i++) {
                    String[] sa = tcArray[i].split("\\s*,\\s*");
                    if (sa.length > 1) tcNames[i] = sa[1];
                }
            }
            else {
                // Initialise empty array
                tcNames = new String[0];
            }
            // Populate cmbTest combobox
            if ((tcNames.length) > 0) {
                cmbTest.removeAllItems();
                cmbTest.setModel(new DefaultComboBoxModel<>(tcNames));
                if (!radTest.isSelected()) {
                    cmbTest.setSelectedIndex(-1);
                }
                else {
                    cmbTest.setSelectedIndex(0);
                }
            }
            else {
                // If nothing was found, disable the test card combobox
                // Use a dummy string to preserve the length of the combobox
                // This won't be seen as the combobox is disabled
                tcArray = new String[]{};
                String[] n = {"No items found"};
                cmbTest.setEnabled(false);
                cmbTest.removeAllItems();
                cmbTest.setModel(new DefaultComboBoxModel<>(n));
                cmbTest.setSelectedIndex(-1);
            }
        }
    }
    
    private String getSelectedColourSystem() {
        if (radPAL.isSelected()) {
            return "pal";
        }
        else if (radNTSC.isSelected()) {
            return "ntsc";
        }
        else if (radSECAM.isSelected()) {
            return "secam";
        }
        else {
            return "";
        }
    }
    
    private ArrayList<String> checkTestCard() {
        var al = new ArrayList<String>();
        if (cmbTest.isEnabled()) {
            if (captainJack) {
                String[] sa = tcArray[cmbTest.getSelectedIndex()].split(",");
                if (!sa[0].isBlank()) al.add("test:" + sa[0]);
            }
            else if (mattstvbarn) {
                String sa[] = tcArray[cmbTest.getSelectedIndex()].split("\\s*,\\s*");
                if (sa[0].equals("colourbars")) {
                    // Use internal hacktv bars rather than the Philips one
                    if (!playlistAL.contains("test:" + sa[0])) al.add("test:" + sa[0]);
                    return al;
                }
                else if (isPhilipsTestSignal()) {
                    // Check sample and pixel rate
                    String tcsr = getTCSampleRate();
                    String err = "The selected test pattern requires a pixel rate (or sample rate) of " + tcsr + " MHz.";
                    if (chkPixelRate.isSelected()) {
                        if (!txtPixelRate.getText().equals(tcsr)) {
                           messageBox(err, JOptionPane.WARNING_MESSAGE);
                           return null;
                        }
                    }
                    else if (!txtSampleRate.getText().equals(tcsr)) {
                        messageBox(err, JOptionPane.WARNING_MESSAGE);
                        return null;
                    }                
                    // Test signals location
                    String p = PREFS.get("testdir", hackTVDirectory);
                    al.add("--testsignals-path");
                    if ((runningOnWindows) && (p.matches(".*\\s.*"))) {
                        al.add('\u0022' + p + '\u0022');
                    }
                    else {
                        al.add(p);
                    }
                    // Add test signal parameters
                    al.add("--testsignal");
                    al.add(sa[0]);
                    // Check if the selected pattern supports text insertion
                    if ((sa.length >= 3) && (sa[3].equals("1"))) {
                        String t1 = PREFS.get("philipstext1", "");
                        String t2 = PREFS.get("philipstext2", "");
                        // Populate text fields and clock/date
                        if (!t1.isBlank()) {
                            al.add("--text1");
                            al.add('\u0022' + t1 + '\u0022');
                        }  
                        if (!t2.isBlank()) {
                            al.add("--text2");
                            al.add('\u0022' + t2 + '\u0022');
                        }
                        switch (PREFS.getInt("philipsclock", 0)) {
                            case 0:
                            default:
                                // Clock off
                                break;
                            case 1:
                                // Clock on
                                al.add("--clockmode");
                                al.add("time");
                                break;
                            case 2:
                                // Clock and date on
                                al.add("--clockmode");
                                al.add("datetime");
                                break;
                        }
                    }
                    return al;
                }
            }
        }
        else if (radTest.isSelected()) {
            if (!playlistAL.contains("test:colourbars")) al.add("test:colourbars");
        }
        return al;
    }
    
    private boolean isPhilipsTestSignal() {
        if (!mattstvbarn) return false;
        if (cmbTest.getSelectedIndex() == -1) return false;
        String[] sa = tcArray[cmbTest.getSelectedIndex()].split("\\s*,\\s*");
        String d = PREFS.get("testdir", hackTVDirectory);
        return ((sa.length > 3) && (Files.exists(Path.of(d + File.separator + sa[2]))));
    }
    
    private String getTCSampleRate() {
        // Philips patterns use a fixed sample rate, usually 13.5 or 20 MHz.
        // Default sample rate (if not defined) is 13.5 MHz
        String sr = "13.5";
        // Check if the sample rate has been overriden for the selected pattern
        String sa[] = tcArray[cmbTest.getSelectedIndex()].split("\\s*,\\s*");
        if ((sa.length == 5) && (SharedInst.isNumeric(sa[4]))) {
            return sa[4];
        }
        else {
            return sr;
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
        txtGain.setEditable(true);
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
        txtGain.setEditable(false);
        lblGain.setEnabled(false);
        radUHF.setEnabled(false);
        radVHF.setEnabled(false);
        radCustom.setEnabled(false);
        bandButtonGroup.clearSelection();
        cmbChannel.setEnabled(false);
        cmbChannel.setSelectedIndex(-1);
        lblChannel.setEnabled(false);
        lblFrequency.setEnabled(false);
        txtFrequency.setText("");
        txtFrequency.setEnabled(false);
        txtFrequency.setEditable(false);
        cmbRegion.setEnabled(false);
        cmbRegion.removeAllItems();
        // Add a blank item to prevent the combobox from enlarging on some L&Fs
        cmbRegion.addItem("");
        if (chkAmp.isSelected()) chkAmp.doClick();
        chkAmp.setEnabled(false);
        lblAntennaName.setEnabled(false);
        txtAntennaName.setText("");
        txtAntennaName.setEnabled(false);
        txtAntennaName.setEditable(false);
    }
    
    private void disableSourceOptions() {
        // Disable all options in the source frame
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
        btnSourceBrowse.setEnabled(false);
        txtSource.setEnabled(false);
        txtSource.setText("");
        txtSource.setEditable(false);
        if (chkARCorrection.isSelected()) chkARCorrection.doClick();
        chkARCorrection.setEnabled(false);
        if (chkTextSubtitles.isSelected()) chkTextSubtitles.doClick();
        chkTextSubtitles.setEnabled(false);
        if ( cmbM3USource.isVisible() ) {
            cmbM3USource.setVisible(false);
            cmbM3USource.setEnabled(false);
            txtSource.setVisible(true);
        }
    }
    
    private void checkMode() {
        // Here, we read the selected combobox index and use that number to get
        // the corresponding video format from the required mode array.
        if (radPAL.isSelected()) {
            mode = palModeArray[cmbMode.getSelectedIndex()];
        }
        else if (radNTSC.isSelected()) {
            mode = ntscModeArray[cmbMode.getSelectedIndex()];
        }
        else if (radSECAM.isSelected()) {
            mode = secamModeArray[cmbMode.getSelectedIndex()];
        }
        else if (radBW.isSelected()) {
            mode = otherModeArray[cmbMode.getSelectedIndex()];
        }
        else if (radMAC.isSelected()) {
            mode = macModeArray[cmbMode.getSelectedIndex()];
        }
        if (radSECAM.isSelected()) {
            chkSecamId.setEnabled(true);
        }
        else {
            if (chkSecamId.isSelected()) chkSecamId.doClick();
            chkSecamId.setEnabled(false);
        }
        // Save the line count from the previously selected mode
        int oldLines = lines;
        // Start reading the section we found above, starting with line count
        if (INI.getIntegerFromINI(modesFile, mode, "lines") != null) {
            lines = INI.getIntegerFromINI(modesFile, mode, "lines");
        }
        else {
            messageBox("Unable to read the \"lines\" value for this mode. "
                    + "Defaulting to 525.", JOptionPane.WARNING_MESSAGE);
            if (cmbMode.getItemCount() > 1) {
                cmbMode.setSelectedIndex(previousIndex);
            }
            else {
                // Default to 525 lines so we don't enable 625-specific stuff
                lines = 525;
            }
        }
        boolean baseband = false;
        switch (INI.getStringFromINI(modesFile, mode, "modulation", "", false)) {
            case "vsb":
                if (!chkVideoFilter.isEnabled()) chkVideoFilter.setEnabled(true);
                if (!chkSwapIQ.isEnabled()) chkSwapIQ.setEnabled(true);
                if (!chkAmp.isEnabled()) chkAmp.setEnabled(true);
                disableFMDeviation();
                sat = false;
                break;
            case "fm":
                if (!chkVideoFilter.isEnabled()) chkVideoFilter.setEnabled(true);
                if (!chkSwapIQ.isEnabled()) chkSwapIQ.setEnabled(true);
                if (!chkAmp.isEnabled()) chkAmp.setEnabled(true);
                enableFMDeviation();
                sat = true;
                break;
            case "baseband":
                if (!checkBasebandSupport()) return;
                baseband = true;
                sat = false;
                break;
            default:
                messageBox("No modulation specified, defaulting to VSB.", JOptionPane.INFORMATION_MESSAGE);
                if (!chkVideoFilter.isEnabled()) chkVideoFilter.setEnabled(true);
                disableFMDeviation();
                break;
        }
        if (INI.getDoubleFromINI(modesFile, mode, "sr") != null) {
            if ((cmbOutputDevice.getSelectedIndex() == 0) && (PREFS.getInt("hackdac", 0) == 1) && (baseband)) {
                // HackDAC works at 13.5 MHz only 
                defaultSampleRate = "13.5";
                txtSampleRate.setEnabled(false);
            }
            else {
                defaultSampleRate = Double.toString(INI.getDoubleFromINI(modesFile, mode, "sr") / 1000000).replace(".0", "");
                if (!txtSampleRate.isEnabled()) txtSampleRate.setEnabled(true);
            }
        }
        else {
            messageBox("No sample rate specified, defaulting to 16 MHz.", JOptionPane.INFORMATION_MESSAGE);
            defaultSampleRate = "16";
            if (!txtSampleRate.isEnabled()) txtSampleRate.setEnabled(true);
        }
        if ( (INI.getBooleanFromINI(modesFile, mode, "colour")) &&
                ( (radPAL.isSelected()) ||
                  (radNTSC.isSelected()) ||
                  (radSECAM.isSelected()) )
                ) {
            chkColour.setEnabled(true);
        }
        else {
            if (chkColour.isSelected()) {
                chkColour.doClick();
                chkColour.setEnabled(false);
            }
            else {
                chkColour.setEnabled(false);
            }
        }
        if (INI.getBooleanFromINI(modesFile, mode, "audio")) {
            enableAudioOption();
        }
        else {
            disableAudioOption();
        }
        if (INI.getBooleanFromINI(modesFile, mode, "nicam")) {
            enableNICAM();
        }
        else {
            disableNICAM();
        }
        if (INI.getBooleanFromINI(modesFile, mode, "a2stereo")) {
            enableA2Stereo();
        }
        else {
            disableA2Stereo();
        }
        if (INI.getBooleanFromINI(modesFile, mode, "teletext")) {
            enableTeletext();
        }
        else {
            disableTeletext();
        }
        if (INI.getBooleanFromINI(modesFile, mode, "wss")) {
            enableWSS();
        }
        else {
            disableWSS();
        }
        if (INI.getBooleanFromINI(modesFile, mode, "vits")) {
            enableVITS();
        }
        else {
            disableVITS();
        }
        if ( (!radMAC.isSelected()) && ((lines == 625) || (lines == 525)) ) {
            chkVITC.setEnabled(true);
        }
        else {
            if (chkVITC.isSelected()) chkVITC.doClick();
            chkVITC.setEnabled(false);
        }
        if (INI.getBooleanFromINI(modesFile, mode, "acp")) {
            acpSupported = true;
            enableACP();
        }
        else {
            acpSupported = false;
            disableACP();
        }
        if (INI.getBooleanFromINI(modesFile, mode, "scrambling")) {
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
        if (lines == 625) {
            chkSiS.setEnabled(true);
        }
        else {
            if (chkSiS.isSelected()) chkSiS.doClick();
            chkSiS.setEnabled(false);
        }
        // Enable S-Video option for baseband modes for FL2K and file output
        if ( (baseband) && ( (cmbOutputDevice.getSelectedIndex() == 2) ||
                (cmbOutputDevice.getSelectedIndex() == 3) ) &&
                ( (radPAL.isSelected()) || (radNTSC.isSelected()) ||
                    (radSECAM.isSelected()) ) ) {
            chkSVideo.setEnabled(true);
        }
        else {
            if (chkSVideo.isSelected()) chkSVideo.doClick();
            chkSVideo.setEnabled(false);
        }
        if (!radMAC.isSelected() && (lines == 625 || lines == 525)) {
            chkCC608.setEnabled(true);
        }
        else {
            if (chkCC608.isSelected()) chkCC608.doClick();
            chkCC608.setEnabled(false);
        }
        // If the colour system (PAL/NTSC/SECAM) or line count varies from the previous mode...
        if ( (!getSelectedColourSystem().equals(prevColour)) || (oldLines != lines)) {
            // ...refresh the available test cards
            addTestCardOptions();
        }
        // Save the current colour system to prevColour so we can recall this later
        prevColour = getSelectedColourSystem();
        // Check for UHF and VHF band plans
        // We now support up to five band plans per band. uhf and vhf are the
        // default and these names are retained for backwards compatibility.
        // Additional band plans can be added from uhf2 to uhf5, or vhf2 to vhf5.
        uhfAL = new ArrayList<>();
        vhfAL = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
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
            String u = INI.getStringFromINI(modesFile, mode, "uhf" + s, "0", false);
            if (!u.equals("0")) {
                // If the UHF radio button label was renamed, set it back
                if (radUHF.getText().equals("Satellite")) radUHF.setText("UHF");
                sat = false;
                uhfAL.add(u);
            }
            else {
                // Check if any satellite band plans are defined
                u = INI.getStringFromINI(modesFile, mode, "sat" + s, "0", false);
                if (!u.equals("0")) {
                    // Rename the UHF radio button label to Satellite instead
                    radUHF.setText("Satellite");
                    sat = true;
                    uhfAL.add(u);
                }
                else {
                    break;
                }
            }
        }
        for (int j = 0; j <= 5; j++) {
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
            String v = INI.getStringFromINI(modesFile, mode, "vhf" + t, "0", false);
            if (!v.equals("0")) {
                vhfAL.add(v);
            }
            else {
                break;
            }
        }
        if (uhfAL.isEmpty()) {
            disableUHF();
        }
        else {
            enableUHF();
        }
        if (vhfAL.isEmpty()) {
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
        // Check if the selected output device supports baseband modes or not.
        if ( ((cmbOutputDevice.getSelectedIndex() == 0) && (PREFS.getInt("hackdac", 0) == 1)) ||
                (cmbOutputDevice.getSelectedIndex() == 2) ||
                (cmbOutputDevice.getSelectedIndex() == 3) ) {
            disableRFOptions();
            if (chkVideoFilter.isSelected()) chkVideoFilter.doClick();
            chkVideoFilter.setEnabled(false);
            if (chkSwapIQ.isSelected()) chkSwapIQ.doClick();
            chkSwapIQ.setEnabled(false);
            return true;
        }
        else {
            String err = "This mode is not supported by the selected output device.";
            if (cmbOutputDevice.getSelectedIndex() == 0) {
                err += "\nIf you have a HackDAC board, enable HackDAC support on the Output tab.";
            }
            messageBox(err, JOptionPane.WARNING_MESSAGE);
            if (cmbMode.getSelectedIndex() != previousIndex) {
                cmbMode.setSelectedIndex(previousIndex);
                checkMode();
                return false;
            }
            else {
                // Fallback for when the previousIndex value matches the current mode
                // This would cause an error message loop otherwise
                List<String> l;
                if (Arrays.asList(palModeArray).contains(mode)) {
                    l = Arrays.asList(palModeArray);
                }
                else if (Arrays.asList(ntscModeArray).contains(mode)) {
                    l = Arrays.asList(ntscModeArray);
                }
                else if (Arrays.asList(secamModeArray).contains(mode)) {
                    l = Arrays.asList(secamModeArray);
                }
                else if (Arrays.asList(otherModeArray).contains(mode)) {
                    l = Arrays.asList(otherModeArray);
                }
                else if (Arrays.asList(macModeArray).contains(mode)) {
                    l = Arrays.asList(macModeArray);
                }
                else {
                    // Should never trigger
                    System.err.println("Unexpected error");
                    return false;
                }
                for (String s : l) {
                    String m = INI.getStringFromINI(modesFile, s, "modulation", "", false);
                    if ((m.equals("vsb")) || (m.equals("fm"))) {
                        cmbMode.setSelectedIndex(l.indexOf(s));
                        return true;
                    }
                }
                // No VSB or FM mode found. This is fatal, we can't continue.
                throw new Error("Fatal error: unable to find a suitable mode to revert to.");
            }
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
        if ((captainJack) && (radLocalSource.isSelected())) {
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
    
    private ArrayList<String> checkTeletextSource(boolean silent) {
        var al = new ArrayList<String>();
        if (chkTeletext.isSelected()) {
            al.add("--teletext");
            // If the txtTeletextSource field contains quotes, remove them
            if ((txtTeletextSource.getText()).matches(".*\\s.*")) {
                txtTeletextSource.setText(txtTeletextSource.getText().replaceAll(String.valueOf((char)34), ""));
            }
            if ((txtTeletextSource.getText()).isBlank()) {
                tabPane.setSelectedIndex(3);
                messageBox("Please specify a directory that contains teletext files, or a teletext archive file.", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            else if ( (txtTeletextSource.getText().toLowerCase(Locale.ENGLISH).endsWith(".t42")) 
                    && (runningOnWindows) && (txtTeletextSource.getText().matches(".*\\s.*")) ) {
                al.add("raw:" + '\u0022' + txtTeletextSource.getText() + '\u0022');
            }
            else if (txtTeletextSource.getText().toLowerCase(Locale.ENGLISH).endsWith(".t42")) {
                al.add("raw:" + txtTeletextSource.getText());
            }
            else if ( (runningOnWindows) && (txtTeletextSource.getText().matches(".*\\s.*")) ) {
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
                if ( (tempDir != null) && (txtTeletextSource.getText().contains(tempDir + File.separator + "spark")) ) {
                    if ( (Files.exists(Path.of(tempDir + File.separator + "spark/P888.tti")))
                            || (Files.exists(Path.of(tempDir + File.separator + "spark/p888.tti"))) ) {
                        try {
                            Shared.deleteFSObject(Path.of(tempDir + File.separator + "spark/P888.tti"));
                        }
                        catch (IOException ex) {
                            if (!silent) messageBox(p888err, JOptionPane.ERROR_MESSAGE);
                            return null;
                        }                    
                    }
                }
                // If the teletext source contains a P888.tti file, abort because hacktv will crash.
                // The latter two if statements are to prevent a NPE if an absolute path is specified.
                else if ( (Files.exists(Path.of(txtTeletextSource.getText() + File.separator + "P888.tti"))) || 
                        (txtTeletextSource.getText().toLowerCase(Locale.ENGLISH).endsWith("p888.tti")) ||
                        (txtTeletextSource.getText().toLowerCase(Locale.ENGLISH).endsWith("p888.ttix")) ) {
                    if (!silent) messageBox(p888err, JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                // If the directory contains any text files in the page 800 range (p8*.tti or p8*.ttix)
                // generate a warning because this can prevent subtitles from running in real time.
                if ( (SharedInst.wildcardFind(txtTeletextSource.getText(), "p8", ".tti") > 0) || 
                        (SharedInst.wildcardFind(txtTeletextSource.getText(), "p8", ".ttix") > 0) ) {
                    if (!silent) messageBox(p888warn, JOptionPane.WARNING_MESSAGE);
                    return al;
                }
            }
        }
        return al;
    }
    
    private void configureMacPanel(boolean e) {
        // Enable or disable MAC video-related options based on the boolean
        // sent to this method
        if (!e) {
            if (chkMacChId.isSelected()) chkMacChId.doClick();
            if (chkMacMono.isSelected()) chkMacMono.doClick();
            if (chkMac16k.isSelected()) chkMac16k.doClick();
            if (chkMacLinear.isSelected()) chkMacLinear.doClick();
            if (chkMacL2.isSelected()) chkMacL2.doClick();
        }
        macPanel.setEnabled(e);
        chkMacChId.setEnabled(e);
        chkMacMono.setEnabled(e);
        chkMac16k.setEnabled(e);
        chkMacLinear.setEnabled(e);
        chkMacL2.setEnabled(e);
    }
    
    private void enableNICAM() {
        if (chkAudio.isSelected()) {
            chkNICAM.setEnabled(true);
            if (!chkNICAM.isSelected()) chkNICAM.doClick();
            nicamSupported = true;
        }
    }
       
    private void disableNICAM() {
        chkNICAM.setEnabled(false);
        chkNICAM.setSelected(false);
        nicamSupported = false;
    }
    
    private void enableA2Stereo() {
        a2Supported = true;
        if (chkAudio.isSelected()) {
            chkA2Stereo.setEnabled(true);
        }
        if ((!chkNICAM.isEnabled()) && (lines == 625)) {
            chkA2Stereo.doClick();
        }
    }
       
    private void disableA2Stereo() {
        a2Supported = false;
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
        txtFMDev.setEditable(false);
        if (chkVideoFilter.isSelected()) {
            chkVideoFilter.setSelected(false);
            txtSampleRate.setText(defaultSampleRate);
        }
        // Revert Filter checkbox name to VSB-AM
        chkVideoFilter.setText("VSB-AM filter");
    }
    
    private void populateBandPlan(String band) {
        // If a satellite band plan has been specified, change the band
        if ((band).contains("uhf") && (sat)) band = band.replace("uhf", "sat");
        txtFrequency.setEditable(false);
        try {
            // Get the band plan list from the requested video mode and band
            String bpname = INI.getStringFromINI(modesFile, mode, band, "", false);
            // Extract (from bpFile) the band plan section that we need
            String bp = INI.splitINIfile(bpFile, bpname);
            if (bp == null) {
                 messageBox(band + " was not found in bandplans.ini", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            // We just want the channel names/numbers so remove everything after =
            bp = bp.replaceAll("\\=.*", "");
            // Remove region identifier, chid and local oscillator lines if they exist.
            // These should not be processed here.
            bp = Stream.of(bp.split("\n"))
                    .filter(g -> !g.contains("region"))
                    .collect(Collectors.joining("\n"));
            bp = Stream.of(bp.split("\n"))
                    .filter(g -> !g.contains("chid"))
                    .collect(Collectors.joining("\n"));
            bp = Stream.of(bp.split("\n"))
                    .filter(g -> !g.contains("lo"))
                    .collect(Collectors.joining("\n"));
            // Add a headerless string to channelArray by splitting off the first line
            channelArray = bp.substring(bp.indexOf("\n") +1).split("\\r?\\n");
            // Populate frequencyArray by reading modesFile using what we added
            // to channelArray.
            frequencyArray = new long[channelArray.length];
            for (int i = 0; i < channelArray.length; i++) {
                if (INI.getLongFromINI(bpFile, bpname, channelArray[i]) != null) {
                    frequencyArray[i] = INI.getLongFromINI(bpFile, bpname, channelArray[i]);
                }
                else {
                     messageBox("Invalid data returned from bandplans.ini, section name: "
                             + bpname, JOptionPane.ERROR_MESSAGE);
                     return;
                }
            }        
            // Enable cmbChannel and populate it with the contents of channelArray
            cmbChannel.setEnabled(true);       
            cmbChannel.removeAllItems();
            cmbChannel.setModel(new DefaultComboBoxModel<>(channelArray));
            cmbChannel.setSelectedIndex(0);  
        }
        catch (IllegalArgumentException ex) {
            System.err.println(ex);
            messageBox("The band plan data in bandplans.ini appears to be "
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
        if (JOptionPane.showConfirmDialog(null, "We will now attempt to use"
                + " yt-dlp to stream the requested video.\n" +
            "Do you wish to continue?", APP_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            final String ytp;
            String url;
            if (runningOnWindows) {
                ytp = "yt-dlp.exe";
            }
            else {
                // Auto-detect yt-dlp location
                String p = PREFS.get("ytdlppath", "");
                if (p.isBlank()) p = getYtDlpPath();
                if (p.isBlank()) {
                    ytp = "yt-dlp";
                }
                else {
                    if (!p.endsWith(File.separator)) p = p + File.separator;
                    // If the detected path was not found, discard it
                    if (!Files.exists(Path.of(p + "yt-dlp"))) {
                        p = "";
                        PREFS.remove("ytdlppath");
                    }
                    else {
                        PREFS.put("ytdlppath", p.substring(0, p.lastIndexOf(File.separator)));
                    }
                    ytp = p + "yt-dlp";                    
                }
            }
            // Remove the ytdl: prefix if specified
            if (input.toLowerCase(Locale.ENGLISH).startsWith("ytdl:")) {
                url = input.substring(5);
            } 
            else {
                url = input;
            }
            chkSyntaxOnly.setEnabled(false);
            btnRun.setEnabled(false);
            txtStatus.setText("Checking URL, please wait...");
            // Check if the provided URL is a live stream or not
            var checkYTDL = new SwingWorker <String, Void> () {
                @Override
                protected String doInBackground() throws Exception {
                    var yt = new ProcessBuilder(ytp, "-g", url);
                    yt.redirectErrorStream(true);
                    String f = null;
                    // Try to start the process
                    try {
                        Process pr = yt.start();
                        try (var br = new BufferedReader(new InputStreamReader(pr.getInputStream(), StandardCharsets.UTF_8))) {
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
        if (!playlistAL.isEmpty()) return "";
        if ( (radLocalSource.isSelected()) || (isPhilipsTestSignal())) {
            if (cmbM3USource.isVisible()) {
                return playlistURLsAL.get(cmbM3USource.getSelectedIndex());
            }
            else if ( (txtSource.getText().contains("://youtube.com/")) ||
                      (txtSource.getText().contains("://www.youtube.com/")) ||
                      (txtSource.getText().contains("://youtu.be/")) ||
                      (txtSource.getText().startsWith("ytdl:")) ) {
                // Invoke the yt-dlp handler
                if (isPhilipsTestSignal()) {
                    messageBox("yt-dlp is not supported with test cards.", JOptionPane.WARNING_MESSAGE);
                }
                else if (!chkSyntaxOnly.isSelected()) {
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
                if (!isPhilipsTestSignal()) {
                    tabPane.setSelectedIndex(0);
                    messageBox("Please specify an input file to broadcast or choose the test card option.", JOptionPane.WARNING_MESSAGE);
                    return null;
                }
                else {
                    // No file defined, return "test" to play standard GLITS tone
                    return "test";
                }
            }
        }
        else {
            return "";
        }
    }
    
    private Integer checkSampleRate() {
        if (SharedInst.isNumeric( txtSampleRate.getText())) {
            Double SR = Double.valueOf(txtSampleRate.getText());
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
                Double PR = Double.valueOf(txtPixelRate.getText());
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
    
    private ArrayList<String> checkOffset() {
        var al = new ArrayList<String>();
        if (chkOffset.isSelected()) {
            try {
                Double PR = Double.valueOf(txtOffset.getText());
                int offset = (int) (PR * 1000000);
                al.add("--offset");
                al.add(Integer.toString(offset));
            }
            catch (NumberFormatException nfe) {
                tabPane.setSelectedIndex(1);
                messageBox("Please specify a valid offset in MHz.", JOptionPane.WARNING_MESSAGE);
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
                Double d = Double.valueOf(txtFMDev.getText());
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
    
    private long calculateFrequency(long inputFreq, boolean silent) {
        // Calculates the intermediate frequency (IF) or harmonic frequency to
        // be sent to hacktv, based on the specified LNB local oscillator or 
        // harmonic settings.
        String err = "The current configuration is not supported by the HackRF device.\n"
            + "Please try a different frequency.";
        // This is the value we'll return if an error is found
        long errValue = Long.MIN_VALUE + 256;
        int rxd = PREFS.getInt("rxdevice", 0);
        if ((sat) && (rxd == 1)) {
            // Direct reception from a Ku band LNB
            // Divide Ku frequency by the chosen harmonic
            long f = inputFreq / getHarmonic();
            if ( (!silent) && ((f > 7250000000L) || (f < 1000000)) ) {
                System.err.println("Frequency of first harmonic (" + f + ") is invalid.");
                messageBox(err, JOptionPane.WARNING_MESSAGE);
                return errValue;
            }
            else {
                return(f);
            }
        }
        else if ((sat) && (rxd == 2)) {
            // Direct reception from a standard Ku band LNB using a BSB receiver
            // Recalculate the transmission frequency based on the IF
            long bsbLO = 10769180000L; // Standard LO of BSB Squarials/LNBs
            long vlo = (long) (PREFS.getDouble("localoscillator", DEFAULT_LO) * 1000000000);
            long f = (inputFreq - bsbLO + vlo) / getHarmonic();
            if ( (!silent) && ((f > 7250000000L) || (f < 1000000)) ) {
                System.err.println("Frequency of first harmonic (" + f + ") is invalid.");
                messageBox(err, JOptionPane.WARNING_MESSAGE);
                return errValue;
            }
            else {
                return(f);
            }
        }
        else if ((sat) && (rxd == 3)) {
            /* Saorsat Ka band LNB mode
               These LNBs aren't fully supported on the receivers that we're
               targeting, you can't enter a 21.2 GHz LO. So we do some trickery
               to calculate the first harmonic. Negate the Ku-band frequency 
               from the band plan and add it to the Ka LO and the Ku LO.
               As the Ka LO is higher than the input frequency, the resulting
               IF is inverted. You should use the "Invert video" option to
               cancel this out.
               
               An example of this LNB can be found at:
               https://www.inverto.tv/lnb/130/twin-ka-circular-dual-polarity-lnb23mm-197-202ghz-lo212o-ghz
            */
            long kaLO = 21200000000L;
            long f = (-inputFreq + kaLO + getLO()) / getHarmonic();
            if ( (!silent) && ((f > 7250000000L) || (f < 1000000)) ) {
                System.err.println("Frequency of first harmonic (" + f + ") is invalid.");
                messageBox(err, JOptionPane.WARNING_MESSAGE);
                return errValue;
            }
            else {
                return(f);
            }
        }
        else {
            long f = (inputFreq - getLO()) / getHarmonic();
            // Convoluted if statement here...
            if (
              // Satellite mode enabled
              (sat) &&
              // And we're not in silent mode
              (!silent) &&
              // And we're using the first harmonic
              (PREFS.getInt("harmonic", 1) == 1) &&
              // And if a custom frequency is selected and "apply LO to custom frequencies" is enabled,
              // or if a custom frequency is not selected
              ( ((radCustom.isSelected()) && (PREFS.get("applyloforcustomfreq", "0").equals("1"))) ||
                  (!radCustom.isSelected()) ) &&
              // And frequency is not between 950 and 2150 MHz
              ((f < 950000000L) || (f > 2150000000L)) 
            ) {
                int q = JOptionPane.showConfirmDialog(null, 
                        "This frequency may be outside of your receiver's tuning range.\n" +
                        "Would you like to continue anyway?", APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (q == JOptionPane.NO_OPTION) {
                    return errValue;
                }
            }
            if ( (!silent) && ((f > 7250000000L) || (f < 1000000)) ) {
                System.err.println("Frequency of first harmonic (" + f + ") is invalid.");
                messageBox(err, JOptionPane.WARNING_MESSAGE);
                return errValue;
            }
            else {
                return(f);
            }
        }
    }    
    private long getLO() {
        // Returns the local oscillator frequency to be used in calculating
        // the IF or harmonic frequency. Only run on satellite modes.
        if (!sat) return 0;
        // If "Apply these settings to custom frequencies" is disabled, and
        // the Custom radio button is selected, return zero.
        if ( ((PREFS.get("applyloforcustomfreq", "0")).equals("0")) &&
                (radCustom.isSelected()) ) return 0;
        // Check first if there's a hardcoded LO in the band plan.
        // This will override any user-defined LO.
        // Retrieve the band plan by checking the region combobox index.
        int i = cmbRegion.getSelectedIndex();
        String b;
        if (i > 0) {
            b = "sat" + String.valueOf(i + 1);
        }
        else {
            b = "sat";
        }
        String bp = INI.getStringFromINI(modesFile, mode, b, "", false);
        if ((INI.getLongFromINI(bpFile, bp, "lo")) != null) {
            long lo = INI.getLongFromINI(bpFile, bp, "lo");
            return lo;
        }
        else {
            Double ulo = PREFS.getDouble("localoscillator", DEFAULT_LO);
            // Convert imported LO from GHz to Hz.
            return (long) (ulo * 1000000000);
        }
    }
    
    private int getHarmonic() {
        // Only run on satellite modes
        if (!sat) return 1;
        // Get harmonic setting
        switch (PREFS.get("harmonic", "1")) {
            case "1":
            default:
                return 1;
            case "2":
                return 2;
            case "3":
                return 3;
            case "4":
                return 4;
        }
    }
    
    private boolean checkCustomFrequency(){
        if (radCustom.isSelected()) {
            boolean s = false;
            if ((sat) && (PREFS.get("applyloforcustomfreq", "0").equals("1"))) {
                s = true;
            }
            BigDecimal CustomFreq;
            var Multiplier = new BigDecimal(1000000);
            String InvalidInput = "Please specify a frequency between 1 MHz and 7250 MHz.";
            String SatHint = "\nIf you're trying to use a frequency for a satellite receiver, " +
                    "enable the \"Apply these settings for custom frequencies\" option in " +
                    "\"Satellite receiver settings\" on the GUI Settings tab.";
            if (SharedInst.isNumeric(txtFrequency.getText().trim())){
                CustomFreq = new BigDecimal(txtFrequency.getText().trim());
                if ( (!s) && ( (CustomFreq.longValue() < 1) || (CustomFreq.longValue() > 7250) ) ) {
                    if (sat) {
                        messageBox(InvalidInput + SatHint, JOptionPane.WARNING_MESSAGE);
                    }
                    else {
                        messageBox(InvalidInput, JOptionPane.WARNING_MESSAGE);
                    }
                    tabPane.setSelectedIndex(2);
                    return false;
                }
                else {
                    // Multiply the big decimal by 1,000,000 to get the frequency in Hz.
                    // Then set the Frequency variable to the long value of the BigDecimal.
                    CustomFreq = CustomFreq.multiply(Multiplier);
                    frequency = CustomFreq.longValue();
                    return true;
                }
            }
            else {
                messageBox(InvalidInput, JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(2);
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
                tabPane.setSelectedIndex(2);
                return null;
            }
        }
        else {
            messageBox(InvalidGain, JOptionPane.WARNING_MESSAGE);
            tabPane.setSelectedIndex(2);
            return null;
        }
        return al;
    }
    
    private String checkCardNumber(String cardNumber) {
        switch (scramblingType1) {
            case "--videocrypt":
                return checkVC1CardNumber(cardNumber);
            case "--videocrypt2":
                return checkVC2CardNumber(cardNumber);
            default:
                return null;
        }
    }
    
    private String checkVC1CardNumber(String cardNumber) {
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
        String length = "";
        boolean qs = false;
        switch (scramblingKey1) {
            case "sky06":
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
                    break;
                }
            case "sky07":
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
                break;
            case "sky09":
                // 9 digit cards only
                length = "9";
                if (cardNumber.length() == 9) {
                    keyStart = 0;
                    keyEnd = 9;
                }
                break;
            case "skynz01":
            case "skynz02":
                // 11-digit cards, only digits 4-11 are Luhn checked
                length = "11";
                if (cardNumber.length() == 11) {
                    keyStart = 4;
                    keyEnd = 11;
                }
                break;
            default:
                break;
        }
        if (!SharedInst.isNumeric(cardNumber) || keyStart == -1) {
            tabPane.setSelectedIndex(4);
            messageBox("Card number should be exactly " + length + " digits.", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        else if ((!qs) && (!SharedInst.luhnCheck(Long.valueOf(cardNumber.substring(keyStart, keyEnd))))) {
            tabPane.setSelectedIndex(4);
            messageBox("Card number appears to be invalid (Luhn check failed).", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        else {
            // Make sure that we're not trying to send EMMs to the wrong card type.
            if (!checkEMMCardType(cardNumber)) {
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
    
    private String checkVC2CardNumber(String cardNumber) {
        // 11-digit numbers only, no Luhn check on these cards
        if (cardNumber.length() == 11) {
            // This is probably wrong!
            return cardNumber.substring(3);
        }
        else {
            tabPane.setSelectedIndex(4);
            messageBox("Card number should be exactly 11 digits.", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }
    
    private boolean checkQuickStartCard(String cardNumber) {
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
        if (SharedInst.calculateLuhnCheckDigit(Long.parseLong(issueNumber + reversedNumber)) == checkDigit) {
            return true;
        }
        else {
            tabPane.setSelectedIndex(4);
            messageBox("Card number appears to be invalid (Luhn check failed).", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }
    
    private boolean checkEMMCardType(String cardNumber) {
        // Make sure that we're not trying to send EMMs to the wrong card type.
        // Used info from settopbox.org to get a rough idea of the range and
        // make an educated guess based on that information.
        // If you have a legitimate card that fails this check, let me know.
        String WrongCardType = "The card number you entered appears to be for a different issue.\n"
                + "Using EMMs on the wrong card type may irreparably damage the card.";
        switch (scramblingKey1) {
            case "sky06":
                String s6 = cardNumber.substring(0,2);
                // Carry out a basic card number check, ensure it starts with 06.
                if (!s6.equals("06")) {
                    messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    return true;
                }
            case "sky07":
                // Only digits 4-13 of a 13-digit card numbers are checked on 07.
                // We need to strip out the first four digits.
                short s7;
                switch(txtCardNumber.getText().length()) {
                    case 13:
                        if (!txtCardNumber.getText().substring(0,2).equals("07")) {
                            messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                        s7 = Short.parseShort(txtCardNumber.getText().substring(4,7));
                        break;
                    case 9:
                        s7 = Short.parseShort(cardNumber.substring(0,3));
                        break;
                    default:
                        messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                        return false;
                }              
                if ((s7 > 30) && (s7 < 800)) {
                    messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    return true;
                }
            case "sky09":
                short s9 = Short.parseShort(cardNumber.substring(0,3));
                if ((cardNumber.length() != 9) || ((s9 < 190) || (s9 > 250))) {
                    messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    return true;
                }
            case "skynz01":
                short snz1 = Short.parseShort(cardNumber.substring(0,2));
                // Carry out a basic card number check, ensure it starts with 01.
                if ((cardNumber.length() != 11) || (snz1 != 1)) {
                    messageBox(WrongCardType, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    return true;
                }
            case "skynz02":
                short snz2 = Short.parseShort(cardNumber.substring(0,2));
                // Carry out a basic card number check, ensure it starts with 02.
                if ((cardNumber.length() != 11) || (snz2 != 2)) {
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
        if ( (!htvLoadInProgress) && (!PREFS.get("SuppressWarnings", "0").equals("1")) ) {
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
                && (!htvLoadInProgress)
                && cmbTest.getItemCount() > 1 ) {
            // Enable cmbTest (test card dropdown)
            cmbTest.setEnabled(true);
            cmbTest.setSelectedIndex(0);
        }
        else if (htvLoadInProgress == true) {
            // Do nothing so we don't interrupt the file loading process
        }
        else if (cmbTest.isEnabled()) {
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
                    tabPane.setSelectedIndex(2);
                     messageBox("Please select an output file or change the output device.",
                             JOptionPane.WARNING_MESSAGE);
                     return null;
                }
                // Do not allow file output to go to the console.
                // Bad things will happen, such as hanging the GUI and consuming large amounts of RAM!
                else if ( (txtOutputDevice.getText().equals("-")) ||
                        (txtOutputDevice.getText().equals("/dev/stdout")) ||
                        (txtOutputDevice.getText().equals("/dev/stderr")) ||
                        (txtOutputDevice.getText().toLowerCase(Locale.ENGLISH).equals("con")) ) {
                     messageBox("Outputting to the console is not supported.",
                             JOptionPane.ERROR_MESSAGE);
                     return null;
                }
                else {
                    al.add("-o");
                    if ( (runningOnWindows) && txtOutputDevice.getText().contains(" ") ) {
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
                switch (cmbFl2kAudio.getSelectedIndex()) {
                    case 0:
                    default:
                        // No audio
                        break;
                    case 1:
                        // Stereo
                        al.add("--fl2k-audio");
                        al.add("stereo");
                        break;
                    case 2:
                        // S/PDIF
                        al.add("--fl2k-audio");
                        al.add("spdif");
                        break;
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
        // If any of these options are selected, return their arguments.
        // We don't need to return the arguments for unchecked options (such as
        // 32 kHz audio or stereo) because they're defaults anyway).
        var al = new ArrayList<String>();
        if (chkMacMono.isSelected()) al.add("--mac-audio-mono");
        if (chkMac16k.isSelected()) al.add("--mac-audio-medium-quality");
        if (chkMacLinear.isSelected()) al.add("--mac-audio-linear");
        if (chkMacL2.isSelected()) al.add("--mac-audio-l2-protection");
        return al;
    }
    
    private String getYtDlpPath() {
        // This method attempts to find yt-dlp on *nix by checking some common
        // locations, as well as by retrieving paths that were defined in
        // terminal configuration files in the user's home directory.
        if (runningOnWindows) return ""; // Not required on Windows
        // This method is only needed if no underlying terminal is running.
        if (isTerminal()) return "";
        // Prioritise a binary in the current directory
        if (Files.exists(Path.of(jarDir + File.separator + "yt-dlp"))) {
            return jarDir + File.separator;
        }
        // Check default Homebrew paths
        if (Files.exists(Path.of("/opt/homebrew/bin/yt-dlp"))) {
            return "/opt/homebrew/bin/"; // MacOS on Apple Silicon
        }
        if (Files.exists(Path.of("/usr/local/bin/yt-dlp"))) {
            return "/usr/local/bin/"; // MacOS on x64
        }
        if (Files.exists(Path.of("/home/linuxbrew/.linuxbrew/bin/yt-dlp"))) {
            return "/home/linuxbrew/.linuxbrew/bin/"; // Linux
        }
        String home = System.getProperty("user.home") + File.separator;
        String s1 = findTerminalPaths(Path.of(home + ".bashrc"));
        if (!s1.isEmpty()) return s1;
        String s2 = findTerminalPaths(Path.of(home + ".bash_profile"));
        if (!s2.isEmpty()) return s2;
        String s3 = findTerminalPaths(Path.of(home + ".zshrc"));
        if (!s3.isEmpty()) return s3;
        String s4 = findTerminalPaths(Path.of(home + ".zshenv"));
        if (!s4.isEmpty()) return s4;
        // Nothing found, let's hope it's in the system path!
        return "";
    }
    
    private boolean isTerminal() {
        // Java 21 or earlier is simple; the console object (named 'c' below) is
        // null if an underlying terminal is not present, or non-null if it is.
        // But in Java 22 or later, this should never be null, so we need to
        // check if the object is really a terminal or not.
        // If we were targeting JRE 22, we could simply query c.isTerminal()
        // but this is not possible under older JDKs, so we need to
        // use the reflection API to invoke the method.
        var c = System.console();
        if (c == null) return false;
        if (Runtime.version().feature() < 22) return true;
        try {
            var m = c.getClass().getMethod("isTerminal");
            // m.invoke(c) returns true if it is a terminal
            return (boolean) m.invoke(c);
        }
        catch (NoSuchMethodException e) {
            // This should never trigger, as we have already eliminated
            // older JRE versions that don't support the isTerminal method.
            // But if it did, we'd return false.
            return false;
        }
        catch (IllegalAccessException
                | IllegalArgumentException
                | SecurityException
                | InvocationTargetException
                ex) {
            return true;
        }
    }
    
    private String findTerminalPaths(Path p) {
        if (!Files.exists(p)) return "";
        String c;
        String pathString = null;
        String[] pathArray;
        var f = new File(p.toUri());
        try (var br1 = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
            while ((c = br1.readLine()) != null) {
                if (c.trim().startsWith("export PATH")) {
                    pathString = c;
                }
            }
        }
        catch (IOException e) {
            return "";
        }
        if (pathString == null) return "";
        pathArray = pathString.substring(pathString.indexOf("=") + 1).split(File.pathSeparator);
        for (String s : pathArray) {
            String testPath = s.replace("$HOME", System.getProperty("user.home"));
            if (testPath.endsWith(File.separator)) testPath = testPath.substring(0, testPath.lastIndexOf(File.separator));
            if ((!s.endsWith("$PATH")) && (Files.exists(Path.of(testPath +  File.separator + "yt-dlp")))) {
                // yt-dlp found at this location
                return testPath + File.separator;
            }
        }
        // Nothing found
        return "";
    }
    
    private void populateArguments(String ytdl) {
        /* The ytdl parameter above is used to determine if this method was
           launched using the yt-dlp handler. If not blank, it will be used
           later to launch hacktv with the yt-dlp pipe creator. If blank, 
           hacktv is launched without it. */
        var allArgs = new ArrayList<String>();
        // hacktv path
        allArgs.add(hackTVPath);
        // Output device
        if (checkOutputDevice() != null) {
            allArgs.addAll(checkOutputDevice());
        }
        else {
            return;
        }
        // Video mode
        allArgs.add("-m");
        allArgs.add(mode);
        // Only add frequency for HackRF (not in baseband mode) or SoapySDR
        String mod = (INI.getStringFromINI(modesFile, mode, "modulation", "", false));        
        if ( ((cmbOutputDevice.getSelectedIndex() == 0) && (!mod.equals("baseband"))) ||
                (cmbOutputDevice.getSelectedIndex() == 1) ) {
            if (!checkCustomFrequency()) return;
            long f = calculateFrequency(frequency, false);
            if (f == ((Long.MIN_VALUE + 256))) {
                return;
            }
            else {
                allArgs.add("-f");
                allArgs.add(Long.toString(f));
            }
            
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
        // Offset
        if (checkOffset() != null) {
            allArgs.addAll(checkOffset());
        }
        else {
            return;
        }        
        // SECAM field ID
        if (chkSecamId.isSelected()) {
            allArgs.add("--secam-field-id");
            int l = cmbSecamIdLines.getSelectedIndex() + 1;
            if (l != 9) {
                allArgs.add("--secam-field-id-lines");
                allArgs.add(String.valueOf(l));
            }
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
        if (chkSwapIQ.isSelected()) allArgs.add("--swap-iq");
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
        if (!scramblingType1.isEmpty()) allArgs.add(scramblingType1);
        if (!scramblingKey1.isEmpty()) allArgs.add(scramblingKey1);
        if (!scramblingType2.isEmpty()) allArgs.add(scramblingType2);
        if (!scramblingKey2.isEmpty()) allArgs.add(scramblingKey2);
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
            if (scramblingType1.equals("--single-cut") ||
                (scramblingType1.equals("--double-cut")) ) {
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
        if (checkFMDeviation() != null) {
            allArgs.addAll(checkFMDeviation());
        }
        else {
            return;
        }
        // The true parameter here suppresses any error messages, used here to 
        // present a non-fatal error so it is not presented twice.
        var txt = checkTeletextSource(true);
        if (txt != null)  {
            allArgs.addAll(txt);
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
            String c = checkCardNumber(txtCardNumber.getText());
            if (c == null) return;
            if (chkActivateCard.isSelected()) {
                allArgs.add("--enableemm");
            }
            else if (chkDeactivateCard.isSelected()) {
                allArgs.add("--disableemm");
            }
            allArgs.add(c);
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
        if (chkSiS.isSelected()) {
            allArgs.add("--sis");
            allArgs.add("dcsis");
        }
        if (chkSVideo.isSelected()) allArgs.add("--s-video");
        if (chkCC608.isSelected()) allArgs.add("--cc608");
        // Finally, add the source video or test option
        if (ytdl.isBlank()) {
            String InputSource = checkInput();
            if (InputSource == null) return;
            // Add test card options if defined
            if (radTest.isSelected()) {
                if (checkTestCard() != null) {
                    allArgs.addAll(checkTestCard());
                }
                else {
                    return;
                }
            }
            if (!playlistAL.isEmpty()) {
                if (chkRandom.isSelected()) {
                    // Set the start point as the first item
                    if (startPoint != -1) {
                        if ( (runningOnWindows) && (playlistAL.get(startPoint).contains(" "))) {
                            allArgs.add('\u0022' + playlistAL.get(startPoint) + '\u0022');
                        }
                        else {
                            allArgs.add(playlistAL.get(startPoint));
                        }
                    }
                    RND.ints(0, playlistAL.size())
                        .distinct()
                        .limit(playlistAL.size())
                        .forEach(
                            r -> {
                                // Add the rest. except for the start point or test cards
                                if ( (!playlistAL.get(r).startsWith("test:")) && (r != startPoint) ) {
                                    if ( (runningOnWindows) && (playlistAL.get(r).contains(" "))) {
                                        allArgs.add('\u0022' + playlistAL.get(r) + '\u0022');
                                    }
                                    else {
                                       allArgs.add(playlistAL.get(r));
                                    }
                                }
                            }
                        );
                }
                else {
                    // Move through playlistAL, starting at the value defined by startPoint.
                    // When we reach the end of the array, start again at zero until we
                    // reach playlistAL.size() minus one.
                    int i = startPoint;
                    int j = 0;
                    if (i == -1) i++;
                    while (j < playlistAL.size()) {
                        if ( (i == playlistAL.size()) && (startPoint != 0) ) {
                            i = 0;
                        }
                        if ( (playlistAL.get(i).startsWith("test:")) ||
                            (playlistAL.get(i).startsWith("http")) ) {
                            allArgs.add(playlistAL.get(i));
                        }
                        else {
                            if ( (runningOnWindows) && playlistAL.get(i).contains(" ") ) {
                                allArgs.add('\u0022' + playlistAL.get(i) + '\u0022');
                            }
                            else {
                                allArgs.add(playlistAL.get(i));
                            }
                        }
                        i++;
                        j++;
                    }
                }  
            }
            else if ( (runningOnWindows) && InputSource.contains(" ")) {
                // Add quotation marks if path contains whitespaces on Windows
                allArgs.add('\u0022' + InputSource + '\u0022');
            }
            else {
                if (!InputSource.isEmpty()) allArgs.add(InputSource);
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
        var runTV = new SwingWorker <String, String> () {
            @Override
            protected String doInBackground() {
                // Create process with the ArrayList we populated above
                var pb = new ProcessBuilder(allArgs);
                pb.redirectErrorStream(true);
                // Set working directory to hacktv location
                pb.directory(new File(hackTVDirectory));
                // Try to start the process
                try {
                    Process p = pb.start();
                    // Get the PID of the process we just started
                    hpid = p.pid();
                    // Capture the output of hacktv
                    try (var br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                        int a;
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
                    return ex.getMessage();
                }
                return null;
            } // End doInBackground

            // Update the GUI from this method.
            @Override
            protected void done() {
                // Get the status code from doInBackground() and return an
                // error if it failed.
                try {
                    String r = get();
                    if (r != null) {
                        messageBox("An error occurred while attempting to run hacktv.\n" +
                                r, JOptionPane.ERROR_MESSAGE);
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
                // them in the console.
                // We need to handle carriage returns (CRs) differently on
                // Windows, due to it using CR+LF for new lines. But this
                // method works on other systems too.
                boolean cr = false;
                for (String o : chunks) {
                    // Start of CR handling.
                    if (o.equals("\r")) {
                        // Let the next loop know that we found a CR
                        cr = true;
                    }
                    else if ((cr) && (!o.equals("\n"))) {
                        String s = txtConsoleOutput.getText();
                        txtConsoleOutput.replaceRange(o, s.lastIndexOf("\n") + 1, s.length());
                        cr = false;
                    }
                    // End of CR handling
                    else {
                        // Append the character as normal
                        txtConsoleOutput.append(o);
                    }
                }
            }// End of process
        }; // End of SwingWorker
        runTV.execute();
    }
    
    private void runYTDLpipe(String ytp, ArrayList<String> allArgs) {
        String u;
        if (txtSource.getText().toLowerCase(Locale.ENGLISH).startsWith("ytdl:")) {
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
        //ytargs.add("-q");
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
        if (runningOnWindows) {
            txtStatus.setText( txtStatus.getText() + " | hacktv.exe");
        }
        else {
            txtStatus.setText( txtStatus.getText() + " | hacktv");
        }
        for (int i = 1; i < allArgs.size() ; i++) {
            txtStatus.setText(txtStatus.getText() + '\u0020' + allArgs.get(i));
        }
        // Spawn a new SwingWorker to run yt-dlp and hacktv
        var runTV = new SwingWorker <String, String> () {
            @Override
            protected String doInBackground() {
                // Create two processes, one for yt-dlp and the other for hacktv
                List<ProcessBuilder> pb = Arrays.asList(
                    new ProcessBuilder(ytargs)
                        // Redirect yt-dlp status to the Java console
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
                    try (var br = new BufferedReader(new InputStreamReader(h.getInputStream(), StandardCharsets.UTF_8))) {
                        int a;
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
                    return ex.getMessage();
                }
                return null;
            } // End doInBackground
            @Override
            protected void process(List<String> chunks) {
                boolean cr = false;
                for (String o : chunks) {
                    // Start of carriage return (CR) handling.
                    if (o.equals("\r")) {
                        // Let the next loop know that we found a CR
                        cr = true;
                    }
                    else if ((cr) && (!o.equals("\n"))) {
                        String s = txtConsoleOutput.getText();
                        txtConsoleOutput.replaceRange(o, s.lastIndexOf("\n") + 1, s.length());
                        cr = false;
                    }
                    // End of CR handling
                    else {
                        // Append the character as normal
                        txtConsoleOutput.append(o);
                    }
                }
            }// End of process
            @Override
            protected void done() {
                // Get the status code from doInBackground() and return an
                // error if it failed.
                try {
                    String r = get();
                    if (r != null) {
                        messageBox("An error occurred while attempting to run yt-dlp or hacktv.\n" +
                                r , JOptionPane.ERROR_MESSAGE);
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
        if (!runningOnWindows) {
            try {
                // Run kill and feed the PID to it
                var stopHackTV = new ProcessBuilder
                    ("kill", "-2", Long.toString(pid));
                stopHackTV.start();
            }
            catch (IOException ex)  {
                System.err.println(ex);
            }
        }
        else {
            // Does windows-kill.exe exist in our directory?
            if (Files.exists(Path.of(jarDir + File.separator + "windows-kill.exe"))) {
                try {
                    // Run windows-kill.exe from this path and feed the PID to it
                    var windowsKill = new ProcessBuilder
                        (jarDir + File.separator + "windows-kill.exe", "-2", Long.toString(pid));
                    windowsKill.start();
                }
                catch (IOException ex)  {
                    System.err.println(ex.getMessage());
                    // Retry with PowerShell, if that fails then force kill.
                    if (!psKill(pid)) taskKill(pid);
                }
            }
            else {
                // Otherwise use PowerShell. If that fails then force kill.
                if (!psKill(pid)) taskKill(pid);
            }
        }
    }
    
    private boolean psKill(long pid) {
        // Uses PowerShell to gracefully close hacktv on Windows
        // The following string is PowerShell/C# code to implement the
        // Win32 GenerateConsoleCtrlEvent API.
        
        // I decided to use a single clear string (with escape characters where
        // necessary) rather than risking triggering AV software by using
        // EncodedCommand. I've divided the string into lines here for clarity.
        String ps1 =
                "& {Add-Type -Namespace 'steeviebops' -Name 'hacktvgui' -MemberDefinition '"
                +     "[DllImport(\\\"kernel32.dll\\\")]public static extern bool FreeConsole();"
                +     "[DllImport(\\\"kernel32.dll\\\")]public static extern bool AttachConsole(uint p);"
                +     "[DllImport(\\\"kernel32.dll\\\")]public static extern bool GenerateConsoleCtrlEvent(uint e, uint p);"
                +     "public static void SendCtrlC(uint p){"
                +         "FreeConsole();"
                +         "AttachConsole(p);"
                +         "GenerateConsoleCtrlEvent(0, 0);"
                +     "}';"
                + "[steeviebops.hacktvgui]::SendCtrlC($args[0])}";
        // Run powershell.exe and feed the above command string to it
        var pb = new ProcessBuilder("powershell.exe", "-noprofile", "-nologo", "-command", ps1, Long.toString(pid))
                // Redirect PowerShell output to stderr for troubleshooting
                .redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            pb.start();
            return true;
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    private void taskKill(long pid) {
        // Last resort option on Windows if the other stop options have failed.
        // This will forcibly terminate hacktv using taskkill.exe.
        System.err.println("Unable to stop hacktv using PowerShell or windows-kill. "
                + "Stopping using taskkill, which is not a clean shutdown.");
        var k = new ProcessBuilder("taskkill.exe", "/pid", Long.toString(pid), "/f");
        try {
            k.start();
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }
    
    private void preRunTasks() {
        btnRun.setText("Stop hacktv");
        chkSyntaxOnly.setEnabled(false);
        btnHackTVPath.setEnabled(false);
        if (runningOnWindows) btnDownloadHackTV.setEnabled(false);
        running = true;
    }
    
    private void postRunTasks() {
        btnRun.setText("Run hacktv");
        if (!btnRun.isEnabled()) btnRun.setEnabled(true);
        chkSyntaxOnly.setEnabled(true);
        btnHackTVPath.setEnabled(true);
        if (runningOnWindows) btnDownloadHackTV.setEnabled(true);
        running = false;
    }
    
    private void cleanupBeforeExit() {
        // Check if a teletext download is in progress
        // If so, then abort
        if (downloadInProgress) downloadCancelled = true;
        // Check if hacktv is running, if so then exit it
        if (running) {
            stopTV(hpid);
        }
        // Delete temp directory and files before exit
        if (tempDir != null) {
            try {
                Shared.deleteFSObject(tempDir.resolve(tempDir));
            } catch (IOException ex) {
                System.err.println("An error occurred while attempting to delete the temp directory: " + ex);
            }
        }
    }

    private void btnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunActionPerformed
        if (!running) {
            if (downloadInProgress) {
                downloadCancelled = true;
                btnRun.setEnabled(false);
            }
            else if ( (!chkSyntaxOnly.isSelected()) && (!Files.exists(Path.of(hackTVPath))) || ((hackTVPath.isBlank())) ) {
                messageBox("Unable to find hacktv. Please go to the GUI settings tab to add its location.", JOptionPane.WARNING_MESSAGE);
                tabPane.setSelectedIndex(5);
            }
            else {
                populateArguments("");
            }
        }
        else {
            btnRun.setEnabled(false);
            stopTV(hpid);
        }
    }//GEN-LAST:event_btnRunActionPerformed
             
    private void menuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAboutActionPerformed
        String v = getVersion();
        // Get the current year for copyright notice.
        String y;
        if (v.equals("n/a")) {
            y = "";
        } else {
            y = " 2020-" + v.substring(0, 4);
        }
        // Get JRE version
        String jv = String.valueOf(Runtime.version().feature());
        JOptionPane.showMessageDialog(null,
                APP_NAME +
                "\nBuild date: " + v +
                "\nUsing " + modesFileLocation + " modes file, version " + modesFileVersion +
                "\nUsing " + bpFileLocation + " band plan file, version " + bpFileVersion +
                "\nUsing Java Runtime Environment version " + jv +
                "\n\nCreated" + y + " by Stephen McGarry.\n" +
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
            txtCardNumber.setEditable(true);
            lblEMMCardNumber.setEnabled(true);
            showEMMWarning();
        }
        else {
            if ( !chkActivateCard.isSelected()) {
                txtCardNumber.setText("");
                txtCardNumber.setEnabled(false);
                txtCardNumber.setEditable(false);
                lblEMMCardNumber.setEnabled(false);
            }
        }
    }//GEN-LAST:event_chkDeactivateCardActionPerformed

    private void chkActivateCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkActivateCardActionPerformed
        if (chkActivateCard.isSelected()) {
            chkDeactivateCard.setSelected(false);
            txtCardNumber.setEnabled(true);
            txtCardNumber.setEditable(true);
            lblEMMCardNumber.setEnabled(true);
            showEMMWarning();
        }
        else {
            if ( !chkDeactivateCard.isSelected()) {
                txtCardNumber.setText("");
                txtCardNumber.setEnabled(false);
                txtCardNumber.setEditable(false);
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
            scramblingType2 = "--videocrypt2";
            scramblingKey2 = scramblingKey2Array.get(cmbScramblingKey2.getSelectedIndex());
        }
    }//GEN-LAST:event_cmbScramblingKey2ActionPerformed

    private void cmbScramblingKey1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbScramblingKey1ActionPerformed
        if (cmbScramblingKey1.getSelectedIndex() != -1) {
            if (scramblingType1.equals("--single-cut") ||
                (scramblingType1.equals("--double-cut")) ) {
                scramblingKey1 = "";
                scramblingKey2 = scramblingKeyArray.get(cmbScramblingKey1.getSelectedIndex());
                /* Free access mode doesn't use the --eurocrypt option, so
                check before adding.
                */
                if (!scramblingKey2.contains("blank")) {
                    scramblingType2 = "--eurocrypt";
                }
                else {
                    scramblingType2 = "";
                    scramblingKey2 = "";
                }
            }
            else {
                scramblingKey1 = scramblingKeyArray.get(cmbScramblingKey1.getSelectedIndex());
                if (!cmbScramblingKey2.isEnabled()) {
                    scramblingType2 = "";
                    scramblingKey2 = "";
                }
                /* If Syster dual mode (line shuffle+cut-and-rotate) is enabled,
                 * set up CNR as a secondary scrambling type and duplicate the 
                 * scrambling key to the CNR mode - you can't use different
                 * access keys simultaneously.
                 */
                if ( (scramblingType1.equals("--syster")) && (cmbScramblingType.getSelectedIndex() == 8) ) {
                    scramblingType2 = "--systercnr";
                    scramblingKey2 = scramblingKey1;    
                }
                // Delete the "blank" parameter if specified
                // This is used as a placeholder for modes which don't use
                // an additional parameter
                if (scramblingKey1.equals("blank")) {
                    scramblingKey1 = "";
                }
            }
            configureScramblingOptions();
        }
    }//GEN-LAST:event_cmbScramblingKey1ActionPerformed

    private void cmbScramblingTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbScramblingTypeActionPerformed
        if (cmbScramblingType.getSelectedIndex() != -1) {
            scramblingType1 = scramblingTypeArray.get(cmbScramblingType.getSelectedIndex());
            addScramblingKey();
        }
    }//GEN-LAST:event_cmbScramblingTypeActionPerformed

    private void btnSparkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSparkActionPerformed
        if ((btnSpark.getText()).contains("Stop")) {
            downloadCancelled = true;
        }
        else {
            btnSpark.setText("Stop");
            chkTeletext.setEnabled(false);
            txtTeletextSource.setEnabled(false);
            txtTeletextSource.setEditable(false);
            btnTeletextBrowse.setEnabled(false);
            btnTeefax.setEnabled(false);
            lblTeefax.setEnabled(false);
            btnNMSCeefax.setEnabled(false);
            lblNMSCeefax.setEnabled(false);
            btnRun.setEnabled(false);
            downloadCancelled = false;
            // Disable hacktv download button so it doesn't interfere
            if (runningOnWindows) btnDownloadHackTV.setEnabled(false);
            // Set variables
            String dUrl = "https://api.github.com/repos/spark-teletext/spark-teletext/contents/";
            String regex = ".*?name\"\\s?:\\s?\"([\\w\\s\\.]+)";
            htmlTempFile = "spark.json";
            // Download index page
            downloadTeletext(dUrl, htmlTempFile, regex);
        }
    }//GEN-LAST:event_btnSparkActionPerformed

    private void btnTeefaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeefaxActionPerformed
        if ((btnTeefax.getText()).contains("Stop")) {
            downloadCancelled = true;
        }
        else {
            btnTeefax.setText("Stop");
            chkTeletext.setEnabled(false);
            txtTeletextSource.setEnabled(false);
            txtTeletextSource.setEditable(false);
            btnTeletextBrowse.setEnabled(false);
            btnSpark.setEnabled(false);
            lblSpark.setEnabled(false);
            btnNMSCeefax.setEnabled(false);
            lblNMSCeefax.setEnabled(false);
            btnRun.setEnabled(false);
            downloadCancelled = false;
            // Disable hacktv download button so it doesn't interfere
            if (runningOnWindows) btnDownloadHackTV.setEnabled(false);
            // Set variables
            String dUrl = "http://teastop.plus.com/svn/teletext/";
            String regex = "\">(.*?)</a>";
            htmlTempFile = "teefax.html";
            // Download index page
            downloadTeletext(dUrl, htmlTempFile, regex);
        }
    }//GEN-LAST:event_btnTeefaxActionPerformed

    private void btnTeletextBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeletextBrowseActionPerformed
        // Retrieve the last used directory from the prefs store if it exists
        teletextFileChooser.setCurrentDirectory(
            new File(PREFS.get("lasttxdir", System.getProperty("user.home")))
        );
        int result = teletextFileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            // Save the chosen directory to prefs
            PREFS.put("lasttxdir", teletextFileChooser.getCurrentDirectory().toString());
            File f = teletextFileChooser.getSelectedFile();
            txtTeletextSource.setText(SharedInst.stripQuotes(f.getAbsolutePath()));
        }
    }//GEN-LAST:event_btnTeletextBrowseActionPerformed

    private void chkTeletextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTeletextActionPerformed
        if (chkTeletext.isSelected()) {
            btnTeletextBrowse.setEnabled(true);
            txtTeletextSource.setEnabled(true);
            txtTeletextSource.setEditable(true);
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
            txtTeletextSource.setEditable(false);
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
            txtOutputLevel.setEditable(true);
        }
        else {
            txtOutputLevel.setText("");
            txtOutputLevel.setEnabled(false);
            txtOutputLevel.setEditable(false);
        }
    }//GEN-LAST:event_chkOutputLevelActionPerformed

    private void chkGammaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGammaActionPerformed
        if (chkGamma.isSelected()) {
            txtGamma.setEnabled(true);
            txtGamma.setEditable(true);
        }
        else {
            txtGamma.setText("");
            txtGamma.setEnabled(false);
            txtGamma.setEditable(false);
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
            if ( scramblingType1.equals("--videocrypt") || scramblingType1.equals("--videocrypt2") ) {
                if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
                txtPixelRate.setText("14");
            }
            else {
                txtSampleRate.setText(defaultSampleRate);
            }
        }
    }//GEN-LAST:event_chkVideoFilterActionPerformed

    private void chkPixelRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPixelRateActionPerformed
        if (chkPixelRate.isSelected()) {
            txtPixelRate.setEnabled(true);
            txtPixelRate.setEditable(true);
            txtPixelRate.setText(txtSampleRate.getText());
        }
        else {
            txtPixelRate.setText("");
            txtPixelRate.setEnabled(false);
            txtPixelRate.setEditable(false);
        }
    }//GEN-LAST:event_chkPixelRateActionPerformed

    private void txtMacChIdKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMacChIdKeyTyped
        if (txtMacChId.getText().length() >= 4) {
            evt.consume();
        }
        else {
            String c = String.valueOf((char)evt.getKeyChar());
            if (SharedInst.isHex(c)) {
                evt.setKeyChar(c.toUpperCase(Locale.ENGLISH).toCharArray()[0]);
            }
            else {
                evt.consume();
            }
        }
    }//GEN-LAST:event_txtMacChIdKeyTyped

    private void chkMacChIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMacChIdActionPerformed
        if (chkMacChId.isSelected()) {
            txtMacChId.setEnabled(true);
            txtMacChId.setEditable(true);
        }
        else {
            txtMacChId.setText("");
            txtMacChId.setEnabled(false);
            txtMacChId.setEditable(false);
        }
    }//GEN-LAST:event_chkMacChIdActionPerformed

    private void chkNICAMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNICAMActionPerformed
        if (chkNICAM.isSelected()) {
            if (chkA2Stereo.isSelected()) chkA2Stereo.doClick();
        }
    }//GEN-LAST:event_chkNICAMActionPerformed

    private void chkAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAudioActionPerformed
        if (chkAudio.isSelected()) {
            if (nicamSupported) {
                chkNICAM.setEnabled(true);
                chkNICAM.doClick();
            }
            if ((a2Supported)&& (lines == 625)) chkA2Stereo.setEnabled(true);
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
    }//GEN-LAST:event_radPALActionPerformed

    private void cmbModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbModeActionPerformed
        if (cmbMode.getSelectedIndex() != -1) {
            checkMode();
            /* Save the currently selected item into previousIndex.
               We can use this to revert the change if an unsupported mode is
               selected later.
            */
            previousIndex = cmbMode.getSelectedIndex();
            // Set sample rate
            txtSampleRate.setText(defaultSampleRate);
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
            txtSubtitleIndex.setEditable(true);
        }
        else {
            lblSubtitleIndex.setEnabled(false);
            txtSubtitleIndex.setText("");
            txtSubtitleIndex.setEnabled(false);
            txtSubtitleIndex.setEditable(false);
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
            txtPosition.setEditable(true);
        }
        else {
            txtPosition.setText("");
            txtPosition.setEnabled(false);
            txtPosition.setEditable(false);
        }
    }//GEN-LAST:event_chkPositionActionPerformed

    private void btnSourceBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSourceBrowseActionPerformed
        // Retrieve the last used directory from the prefs store if it exists
        sourceFileChooser.setCurrentDirectory(
            new File(PREFS.get("lastdir", System.getProperty("user.home")))
        );
        int returnVal = sourceFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] f = sourceFileChooser.getSelectedFiles();
            // Save the chosen directory to prefs
            PREFS.put("lastdir", sourceFileChooser.getCurrentDirectory().toString());
            if (f.length > 1) {
                for (File fn : f) {
                    if (((!fn.toString().toLowerCase(Locale.ENGLISH).endsWith(".m3u"))
                            || (!fn.toString().toLowerCase(Locale.ENGLISH).endsWith(".m3u8")))
                            && (!fn.toString().toLowerCase(Locale.ENGLISH).endsWith(".htv"))) {
                        playlistAL.add(fn.toString());
                    }
                }
                populatePlaylist();
            }
            else {
                var file = new File (SharedInst.stripQuotes(f[0].toString()));
                if ( (file.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(".m3u"))
                      || (file.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(".m3u8")) ) {
                    // If the source is an M3U file, pass it to the M3U handler
                    txtSource.setText(file.getAbsolutePath());
                    m3uHandler(file.getAbsolutePath(),0);
                }
                else if (file.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(".htv")) {
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
        // Enable test card dropdown
        if ((!captainJack) && (cmbTest.getItemCount() > 1)) {
            cmbTest.setEnabled(true);
            cmbTest.setSelectedIndex(0);
        }
        else if ((captainJack) && (cmbTest.getItemCount() > 1)) {
            cmbTest.setEnabled(true);
            cmbTest.setSelectedIndex(0);
            disableSourceOptions();
        }
        else {
            disableSourceOptions();
        }
    }//GEN-LAST:event_radTestActionPerformed

    private void radLocalSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radLocalSourceActionPerformed
        // Enable all options in the frame
        chkRepeat.setEnabled(true);
        chkInterlace.setEnabled(true);
        txtSource.setEnabled(true);
        txtSource.setEditable(true);
        btnSourceBrowse.setEnabled(true);
        chkARCorrection.setEnabled(true);
        chkVolume.setEnabled(true);
        // Disable test card dropdown
        cmbTest.setSelectedIndex(-1);
        cmbTest.setEnabled(false);
        if (captainJack) {
            chkPosition.setEnabled(true);
            chkTimestamp.setEnabled(true);
            chkDownmix.setEnabled(true);
            chkSubtitles.setEnabled(true);
            if (chkTeletext.isEnabled()) {
                chkTextSubtitles.setEnabled(true);
            }
        }
    }//GEN-LAST:event_radLocalSourceActionPerformed

    private void chkAmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAmpActionPerformed
        if (chkAmp.isSelected()) {
            if ( (!htvLoadInProgress) && (PREFS.getInt("SuppressWarnings", 0) != 1))
                messageBox("Care is advised when using this option.\n" +
                    "Incorrect use may permanently damage the amplifier.",
                    JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_chkAmpActionPerformed

    private void cmbChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbChannelActionPerformed
        if ( cmbChannel.getSelectedIndex() != -1) {
            frequency = frequencyArray[cmbChannel.getSelectedIndex()];
            // Convert the imported value so we can display it in MHz on-screen
            var df = new DecimalFormat("0.00");
            double input;
            if (PREFS.getInt("showrealfrequency", 0) == 1) {
                // Calculate TX frequency
                input = calculateFrequency(frequency, true);
            }
            else {
                // Use the frequency defined in the band plan
                input = frequency;
            }
            txtFrequency.setText((df.format(input / 1000000)));
            // Retrieve MAC channel ID
            if (radMAC.isSelected()) {
                String b = "";
                if ((radUHF.isSelected()) && (!sat)) {
                    b = "uhf";
                }
                else if ((radUHF.isSelected()) && (sat)) {
                    b = "sat";
                }
                else if (radVHF.isSelected()) {
                    b = "vhf";
                }
                // Retrieve band plan
                String bp = INI.getStringFromINI(modesFile, mode, b, "", false);
                // Retrieve channel ID list
                String c = INI.getStringFromINI(bpFile, bp, "chid", "", false);
                // Retrieve ID using the channel name from the ID list
                // This name must be identical to the name specified in the band plan
                String id = INI.getStringFromINI(bpFile, c, channelArray[cmbChannel.getSelectedIndex()], "", false).toUpperCase(Locale.ENGLISH);
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
        String sv = "";
        if (cmbRegion.getSelectedIndex() > 0) sv = cmbRegion.getItemAt(cmbRegion.getSelectedIndex());
        // Set region and alternate plans for UHF
        cmbRegion.setEnabled(false);
        cmbRegion.removeAllItems();
        for (int i = 0; i < uhfAL.size(); i++) {
            cmbRegion.addItem(INI.getStringFromINI(bpFile, uhfAL.get(i), "region", uhfAL.get(i), true));
        }
        // Enable the region combobox if multiple options are available.
        if (cmbRegion.getItemCount() > 1) {
            cmbRegion.setEnabled(true);
            if (!sat) {
                populateBandPlan("uhf");
            }
            else {
                // Populate satellite band plans instead of UHF ones
                populateBandPlan("sat");
            }
        }
        // If multiple regions are available, see if there's a UHF region with the
        // same name as the previously selected VHF one. If so, select it.
        if ( (uhfAL.size() > 1) && (vhfAL.size() > 1) ) {
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
        String su = "";
        if (cmbRegion.getSelectedIndex() > 0) su = cmbRegion.getItemAt(cmbRegion.getSelectedIndex());
        // Set region and alternate plans for VHF
        cmbRegion.setEnabled(false);
        cmbRegion.removeAllItems();
        for (int i = 0; i < vhfAL.size(); i++) {
            cmbRegion.addItem(INI.getStringFromINI(bpFile, vhfAL.get(i), "region", vhfAL.get(i), true));
        }
        // Enable the region combobox if multiple options are available.
        if (cmbRegion.getItemCount() > 1) {
            cmbRegion.setEnabled(true);
            populateBandPlan("vhf");
        }
        // If multiple regions are available, see if there's a VHF region with the
        // same name as the previously selected UHF one. If so, select it.
        if ( (uhfAL.size() > 1) && (vhfAL.size() > 1) ) {
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

    private void btnHackTVPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHackTVPathActionPerformed
        // Retrieve the last used directory from the prefs store if it exists
        hacktvFileChooser.setCurrentDirectory(
            new File(PREFS.get("lasthtvdir", System.getProperty("user.home")))
        );
        hacktvFileChooser.setAcceptAllFileFilterUsed(true);
        int returnVal = hacktvFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // Save the chosen directory to prefs
            PREFS.put("lasthtvdir", hacktvFileChooser.getCurrentDirectory().toString());
            File file = hacktvFileChooser.getSelectedFile();
            hackTVPath = SharedInst.stripQuotes(file.toString());
            txtHackTVPath.setText(hackTVPath);
            // Store the specified path.
            PREFS.put("hacktvpath", hackTVPath);
            // Load the full path to a variable so we can use getParent on it
            // and get its parent directory path
            hackTVDirectory = new File(hackTVPath).getParent();
            // Detect what were provided with
            detectFork();
            selectModesFile();
            addTestCardOptions();
            addARCorrectionOptions();
            if (captainJack) {
                captainJack();
            }
            else {
                fsphil();
            }
            if (radTest.isSelected()) radTest.doClick();
        }
    }//GEN-LAST:event_btnHackTVPathActionPerformed

    private void menuOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenActionPerformed
        int result = configFileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = configFileChooser.getSelectedFile();
            selectedFile = new File(SharedInst.stripQuotes(selectedFile.toString()));
            checkSelectedFile(selectedFile);
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
            saveConfigFile(selectedFile);
        }
    }//GEN-LAST:event_menuSaveActionPerformed

    private void menuMRUFile2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile2ActionPerformed
        selectedFile = new File(menuMRUFile2.getText());
        checkSelectedFile(selectedFile);
    }//GEN-LAST:event_menuMRUFile2ActionPerformed

    private void menuMRUFile1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile1ActionPerformed
        selectedFile = new File(menuMRUFile1.getText());
        checkSelectedFile(selectedFile);
    }//GEN-LAST:event_menuMRUFile1ActionPerformed

    private void menuMRUFile3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile3ActionPerformed
        selectedFile = new File(menuMRUFile3.getText());
        checkSelectedFile(selectedFile);
    }//GEN-LAST:event_menuMRUFile3ActionPerformed

    private void menuMRUFile4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile4ActionPerformed
        selectedFile = new File(menuMRUFile4.getText());
        checkSelectedFile(selectedFile);
    }//GEN-LAST:event_menuMRUFile4ActionPerformed

    private void btnClearMRUListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearMRUListActionPerformed
        if (JOptionPane.showConfirmDialog(null, "This will clear the list of most recently used "
                + "files from the File menu. Do you wish to continue?", APP_NAME, JOptionPane.YES_NO_OPTION)  == JOptionPane.YES_OPTION) {
            if ( PREFS.get("file1", null) != null ) PREFS.remove("file1");
            if ( PREFS.get("file2", null) != null ) PREFS.remove("file2");
            if ( PREFS.get("file3", null) != null ) PREFS.remove("file3");
            if ( PREFS.get("file4", null) != null ) PREFS.remove("file4");
            checkMRUList();
        }
    }//GEN-LAST:event_btnClearMRUListActionPerformed

    private void btnResetAllSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetAllSettingsActionPerformed
        if (JOptionPane.showConfirmDialog(null, "This will remove all of this application's "
                + "saved settings and exit. Do you wish to continue?",
                APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            resetPreferences();
            this.dispose();
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
        boolean bb = INI.getStringFromINI(modesFile, mode, "modulation", "", false).equals("baseband");
        if (!txtOutputDevice.getText().isBlank()) txtOutputDevice.setText("");
        switch(cmbOutputDevice.getSelectedIndex()) {
            // hackrf
            case 0:
                chkHackDAC.setVisible(true);
                lblFl2kAudio.setVisible(false);
                cmbFl2kAudio.setVisible(false);
                cmbFl2kAudio.setSelectedIndex(0);
                lblOutputDevice2.setText("Serial number (optional)");
                if (!radCustom.isEnabled()) {
                    // If a baseband mode is selected and HackDAC is not enabled,
                    // reset the mode to something else
                    if ( (bb) && (PREFS.getInt("hackdac", 0) == 0) ) {
                        messageBox(ModeChanged, JOptionPane.WARNING_MESSAGE);
                        cmbMode.setSelectedIndex(0);
                    }
                    // If the RF panel is disabled, enable it and call checkMode
                    // to re-populate the channel options correctly
                    enableRFOptions();
                    checkMode();
                }
                if (!bb) {
                    txtGain.setEnabled(true);
                    txtGain.setEditable(true);
                    txtGain.setText("0");
                    lblGain.setEnabled(true);
                    chkAmp.setEnabled(true);                    
                }
                lblAntennaName.setEnabled(false);
                txtAntennaName.setEnabled(false);
                txtAntennaName.setText("");
                txtAntennaName.setEditable(false);
                lblFileType.setEnabled(false);
                cmbFileType.setEnabled(false);
                cmbFileType.setSelectedIndex(-1);
                break;
            // soapysdr
            case 1:
                chkHackDAC.setVisible(false);
                lblFl2kAudio.setVisible(false);
                cmbFl2kAudio.setVisible(false);
                cmbFl2kAudio.setSelectedIndex(0);
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
                txtGain.setEditable(true);
                txtGain.setText("0");
                lblGain.setEnabled(true);
                chkAmp.setEnabled(false);
                lblAntennaName.setEnabled(true);
                txtAntennaName.setEnabled(true);
                txtAntennaName.setEditable(true);
                lblFileType.setEnabled(false);
                cmbFileType.setEnabled(false);
                cmbFileType.setSelectedIndex(-1);
                break;
            // fl2k
            case 2:
                chkHackDAC.setVisible(false);
                lblFl2kAudio.setVisible(true);
                cmbFl2kAudio.setVisible(true);
                cmbFl2kAudio.setSelectedIndex(0);
                lblOutputDevice2.setText("Device number (optional)");
                // fl2k is baseband only for now so disable all RF options
                disableRFOptions();
                rfPanel.setEnabled(false);
                // Enable S-Video option if a baseband mode is selected
                if ( (bb) && ( (radPAL.isSelected()) || (radNTSC.isSelected()) ||
                    (radSECAM.isSelected()) ) ) chkSVideo.setEnabled(true);
                break;
            // Output to file
            case 3:
                chkHackDAC.setVisible(false);
                lblFl2kAudio.setVisible(false);
                cmbFl2kAudio.setVisible(false);
                cmbFl2kAudio.setSelectedIndex(0);
                lblOutputDevice2.setText("Destination file");
                disableRFOptions();
                rfPanel.setEnabled(true);
                // Enable S-Video option if a baseband mode is selected
                if ( (bb) && ( (radPAL.isSelected()) || (radNTSC.isSelected()) ||
                    (radSECAM.isSelected()) ) ) chkSVideo.setEnabled(true);
                lblFileType.setEnabled(true);
                cmbFileType.setEnabled(true);
                cmbFileType.setSelectedIndex(3);
                // Opens the save file dialogue, but only if selected by the user
                if (htvLoadInProgress == false) {
                    // Retrieve the last used directory from the prefs store if it exists
                    outputFileChooser.setCurrentDirectory(
                        new File(PREFS.get("lastfdir", System.getProperty("user.home")))
                    );
                    int result = outputFileChooser.showSaveDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        // Save the chosen directory to prefs
                        PREFS.put("lastfdir", outputFileChooser.getCurrentDirectory().toString());
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
            txtVolume.setEditable(true);
        }
        else {
            txtVolume.setEnabled(false);
            txtVolume.setText("");
            txtVolume.setEditable(false);
        }
    }//GEN-LAST:event_chkVolumeActionPerformed

    private void chkTextSubtitlesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTextSubtitlesActionPerformed
        if (chkTextSubtitles.isSelected()) {
            lblTextSubtitleIndex.setEnabled(true);
            txtTextSubtitleIndex.setEnabled(true); 
            txtTextSubtitleIndex.setEditable(true);
        }
        else {
            txtTextSubtitleIndex.setEnabled(false);
            txtTextSubtitleIndex.setText("");
            txtTextSubtitleIndex.setEditable(false);
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
            PREFS.putInt("uselocalmodesfile", 1);
        }
        else {
            PREFS.putInt("uselocalmodesfile", 0);
        }
        // Reopen modes file with new settings
        selectModesFile();
    }//GEN-LAST:event_chkLocalModesActionPerformed

    private void chkFMDevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkFMDevActionPerformed
        if (chkFMDev.isSelected()) {
            txtFMDev.setEnabled(true);
            txtFMDev.setEditable(true);
        }
        else {
            txtFMDev.setText("");
            txtFMDev.setEnabled(false);
            txtFMDev.setEditable(false);
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
        else if ( (lstPlaylist.getSelectedIndex() == 0) && (playlistAL.size() == 1) ) {
            btnPlaylistUp.setEnabled(false);
            btnPlaylistDown.setEnabled(false);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(false);
            btnPlaylistStart.setText(playFirst);
            if (chkRandom.isSelected()) chkRandom.doClick();
            chkRandom.setEnabled(false);
        }
        // Is the selected item an intermediate item? (not the first or last)
        else if ( (lstPlaylist.getSelectedIndex() != 0) && (lstPlaylist.getSelectedIndex() != playlistAL.size() - 1) ) {
            btnPlaylistUp.setEnabled(true);
            btnPlaylistDown.setEnabled(true);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(true);
            if (lstPlaylist.getSelectedIndex() == startPoint) {
                btnPlaylistStart.setText(reset);
            }
            else {
                btnPlaylistStart.setText(playFirst);
            }
            chkRandom.setEnabled(true);
        }
        // Is the first item in the playlist selected?
        else if ( (lstPlaylist.getSelectedIndex() == 0) && (playlistAL.size() > 1) ) {
            btnPlaylistUp.setEnabled(false);
            btnPlaylistDown.setEnabled(true);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(true);
            if (lstPlaylist.getSelectedIndex() == startPoint) {
                btnPlaylistStart.setText(reset);
            }
            else {
                btnPlaylistStart.setText(playFirst);
            }
            chkRandom.setEnabled(true);
        }
        // Is the last item in the playlist selected?
        else if (lstPlaylist.getSelectedIndex() == playlistAL.size() - 1) {
            btnPlaylistUp.setEnabled(true);
            btnPlaylistDown.setEnabled(false);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(true);
            if (lstPlaylist.getSelectedIndex() == startPoint) {
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
            playlistAL.add(playlistURLsAL.get(cmbM3USource.getSelectedIndex()));
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
        else if ((isPhilipsTestSignal()) && (txtSource.getText().isBlank())) {
            // Don't add Philips test cards
            messageBox("Adding a Philips test pattern to the playlist is not supported. "
                    + "Click \"Run hacktv\" to use it without the playlist.\n"
                    + "However, you can add audio files to the playlist, which "
                    + "will be played over the test pattern.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        else if ( (txtSource.isEnabled()) && (!txtSource.getText().isBlank()) ) {
            // Add whatever is in txtSource to playlistAL
            playlistAL.add(txtSource.getText());
        }
        else if (radTest.isSelected()) {
            for (String s : playlistAL) {
                if (s.startsWith("test:")) {
                    messageBox("Only one test card can be added to the playlist.\n"
                        + "It should also be placed as the last item in the playlist.", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (cmbTest.isEnabled()) {
                // Add the selected test card
                String[] sa = tcArray[cmbTest.getSelectedIndex()].split("\\s*,\\s*");
                if (sa.length > 0) playlistAL.add("test:" + sa[0]);
            }
            else {
                // Add the test card
                playlistAL.add("test:colourbars");
            }
        }
        else {
            btnSourceBrowse.doClick();
            if (!txtSource.getText().isBlank()) btnAdd.doClick();
            return;
        }
        // Enable or disable random option
        if (playlistAL.size() > 1) {
            chkRandom.setEnabled(true);
        }
        else {
            if (chkRandom.isSelected()) chkRandom.doClick();
            chkRandom.setEnabled(false);
        }
        populatePlaylist();
        txtSource.setText("");
        lstPlaylist.setSelectedIndex(playlistAL.size() -1);
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int[] ia = lstPlaylist.getSelectedIndices();
        // If only one item was selected, put it back in the source box
        if (ia.length == 1) {
            if (radLocalSource.isSelected()) {
                txtSource.setText(playlistAL.get(ia[0]));
            }
            else if ((radTest.isSelected()) && cmbTest.isEnabled()) {
                for (int i = 0; i < tcArray.length; i++) {
                    if (tcArray[i].equals(playlistAL.get(ia[0]).substring(5))) {
                        cmbTest.setSelectedIndex(i);
                    }
                }
            }
        }
        // Process the selection array in reverse order and remove the items from the arraylist
        for (int j = ia.length -1; j >= 0; j--) {
            // Remove the requested item from the arraylist
            playlistAL.remove(ia[j]);
            // If the item removed was the start point, or if only one item
            // is left, reset startPoint to default
            if ((ia[j] == startPoint) || (playlistAL.size() < 2)) {
                startPoint = -1;
            }                
            // If the item removed was before the start point, reduce startPoint
            // by one so the selected item remains selected
            else if (ia[j] < startPoint) {
                startPoint = startPoint - 1;
            }
            // Re-populate the playlist with the new arraylist values
            populatePlaylist();
        }
        // If only one item was selected...
        if (ia.length == 1) {
            // If the last item in the list was selected, select whatever
            // was the second from last (and is now last).
            if (playlistAL.size() == ia[0]) {
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
        if (i - 1 == startPoint) {
            startPoint = startPoint + 1;
        }
        // If the selected item is the start point, shift the start point down
        // by one so the selected start point will remain selected
        else if (i == startPoint) {
            startPoint = startPoint - 1;
        }
        if (i > 0) {
            playlistAL.add(i - 1, playlistAL.get(i));
            playlistAL.remove(i + 1);
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
        if (i + 1 == startPoint) {
            startPoint = startPoint - 1;
        }
        // If the selected item is the start point, shift the start point up
        // by one so the selected start point will remain selected
        else if (i == startPoint) {
            startPoint = startPoint + 1;
        }
        if ( (i >= 0) && (i != playlistAL.size() - 1) ) {
            playlistAL.add(i + 2, playlistAL.get(i));
            playlistAL.remove(i);
            populatePlaylist();
            lstPlaylist.setSelectedIndex(i + 1);
            lstPlaylist.ensureIndexIsVisible(lstPlaylist.getSelectedIndex());
        }
        btnPlaylistUp.setEnabled(true);
        if (i == playlistAL.size() - 2) {
            // As we have reached the bottom of the list, disable the Down button
            btnPlaylistDown.setEnabled(false);
        }
        else {
            btnPlaylistDown.requestFocusInWindow();
        }
    }//GEN-LAST:event_btnPlaylistDownActionPerformed

    private void cmbModeMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbModeMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbMode);
    }//GEN-LAST:event_cmbModeMouseWheelMoved

    private void cmbWSSMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbWSSMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbWSS);
    }//GEN-LAST:event_cmbWSSMouseWheelMoved

    private void cmbLogoMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbLogoMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbLogo);
    }//GEN-LAST:event_cmbLogoMouseWheelMoved

    private void cmbARCorrectionMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbARCorrectionMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbARCorrection);
    }//GEN-LAST:event_cmbARCorrectionMouseWheelMoved

    private void cmbOutputDeviceMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbOutputDeviceMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbOutputDevice);
    }//GEN-LAST:event_cmbOutputDeviceMouseWheelMoved

    private void cmbChannelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbChannelMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbChannel);
    }//GEN-LAST:event_cmbChannelMouseWheelMoved

    private void cmbFileTypeMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbFileTypeMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbFileType);
    }//GEN-LAST:event_cmbFileTypeMouseWheelMoved

    private void cmbScramblingTypeMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbScramblingTypeMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbScramblingType);
    }//GEN-LAST:event_cmbScramblingTypeMouseWheelMoved

    private void cmbScramblingKey1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbScramblingKey1MouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbScramblingKey1);
    }//GEN-LAST:event_cmbScramblingKey1MouseWheelMoved

    private void cmbScramblingKey2MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbScramblingKey2MouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbScramblingKey2);
    }//GEN-LAST:event_cmbScramblingKey2MouseWheelMoved

    private void cmbSysterPermTableMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbSysterPermTableMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbSysterPermTable);
    }//GEN-LAST:event_cmbSysterPermTableMouseWheelMoved

    private void cmbTestMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbTestMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbTest);
    }//GEN-LAST:event_cmbTestMouseWheelMoved

    private void btnPlaylistStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlaylistStartActionPerformed
        // Don't set a test card as the start point of the playlist.
        // It never ends, so the playlist becomes pointless.
        int s = lstPlaylist.getSelectedIndex();
        if (playlistAL.get(s).startsWith("test:")) {
            messageBox("Test cards cannot be set as the start point of a playlist.", JOptionPane.WARNING_MESSAGE);
        }
        else if (s == startPoint) {
            // Reset the start point
            startPoint = -1;
            populatePlaylist();
        }
        else {
            // Set the start point
            startPoint = s;
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
        if (downloadInProgress) {
            btnDownloadHackTV.setEnabled(false);
            downloadCancelled = true;
            return;
        }
        var ds = new DownloadButtonDialogue(this, true);
        ds.setVisible(true);
        String s = ds.getSelection();
        if (s != null) downloadHackTV_Win32(s);
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
        if (this.isVisible()) {
            SwingUtilities.invokeLater(() -> {
                changeLaf(cmbLookAndFeel.getSelectedIndex());    
            });
        }
    }//GEN-LAST:event_cmbLookAndFeelActionPerformed

    private void chkECppvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkECppvActionPerformed
        if (chkECppv.isSelected()) {
            lblECprognum.setEnabled(true);
            txtECprognum.setEnabled(true);
            txtECprognum.setEditable(true);
            lblECprogcost.setEnabled(true);
            txtECprogcost.setEnabled(true);
            txtECprogcost.setEditable(true);
        }
        else {
            lblECprognum.setEnabled(false);
            txtECprognum.setText("");
            txtECprognum.setEnabled(false);
            txtECprognum.setEditable(false);
            lblECprogcost.setEnabled(false);
            txtECprogcost.setText("");
            txtECprogcost.setEnabled(false);
            txtECprogcost.setEditable(false);
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
        PREFS.putInt("ceefaxregion", cmbNMSCeefaxRegion.getSelectedIndex());
    }//GEN-LAST:event_cmbNMSCeefaxRegionActionPerformed

    private void btnNMSCeefaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNMSCeefaxActionPerformed
        if ((btnNMSCeefax.getText()).contains("Stop")) {
            downloadCancelled = true;
        }
        else {
            btnNMSCeefax.setText("Stop");
            chkTeletext.setEnabled(false);
            txtTeletextSource.setEnabled(false);
            txtTeletextSource.setEditable(false);
            btnTeletextBrowse.setEnabled(false);
            btnTeefax.setEnabled(false);
            lblTeefax.setEnabled(false);
            btnSpark.setEnabled(false);
            lblSpark.setEnabled(false);
            btnRun.setEnabled(false);
            downloadCancelled = false;
            // Disable hacktv download button so it doesn't interfere
            if (runningOnWindows) btnDownloadHackTV.setEnabled(false);
            // Set variables
            var CeefaxRegionArray = new String[] {
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
            htmlTempFile = "ceefax.xml";
            String regex = "name=\"(.*?)\"";
            // Download index page
            downloadTeletext("https://internal.nathanmediaservices.co.uk/svn/ceefax/"
                                    + CeefaxRegionArray[cmbNMSCeefaxRegion.getSelectedIndex()] + "/", htmlTempFile, regex);
        }
    }//GEN-LAST:event_btnNMSCeefaxActionPerformed

    private void cmbLookAndFeelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbLookAndFeelMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbLookAndFeel);
    }//GEN-LAST:event_cmbLookAndFeelMouseWheelMoved

    private void cmbNMSCeefaxRegionMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbNMSCeefaxRegionMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbNMSCeefaxRegion);
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
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbRegion);
    }//GEN-LAST:event_cmbRegionMouseWheelMoved

    private void cmbM3USourceMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbM3USourceMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbM3USource);
    }//GEN-LAST:event_cmbM3USourceMouseWheelMoved

    private void cmbECMaturityMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbECMaturityMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbECMaturity);
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
           PREFS.putInt("noupdatecheck", 1);
       }
       else {
           PREFS.putInt("noupdatecheck", 0);
       }
    }//GEN-LAST:event_chkNoUpdateCheckActionPerformed

    private void menuUpdateCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuUpdateCheckActionPerformed
        checkForUpdates(false);
    }//GEN-LAST:event_menuUpdateCheckActionPerformed

    private void chkOffsetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkOffsetActionPerformed
        if (chkOffset.isSelected()) {
            txtOffset.setEnabled(true);
            txtOffset.setEditable(true);
        }
        else {
            txtOffset.setText("");
            txtOffset.setEnabled(false);
            txtOffset.setEditable(false);
        }
    }//GEN-LAST:event_chkOffsetActionPerformed

    private void txtOffsetKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtOffsetKeyTyped
        if (evt.getKeyChar() == '\u002e') {
            if (txtOffset.getText().contains(".")) {
                evt.consume();
            }
        }
        else if (!SharedInst.isNumeric(String.valueOf((char)evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtOffsetKeyTyped

    private void btnSatSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSatSettingsActionPerformed
        // Show the setting dialogue box
        var sd = new SatSettingsDialogue(this, true);
        sd.setVisible(true);
        // See if a setting has changed. If so, refresh the channel combobox.
        if ( (sd.settingsChanged()) && (cmbChannel.isEnabled()) ) {
            cmbChannel.setSelectedIndex(cmbChannel.getSelectedIndex());
        }
    }//GEN-LAST:event_btnSatSettingsActionPerformed

    private void menuBSBTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuBSBTemplateActionPerformed
        if (JOptionPane.showConfirmDialog(null, "This will load template values for a BSB satellite receiver.\n"
            + "All current settings will be cleared. Do you wish to continue?",
            APP_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // Reset all controls
            resetAllControls();
            // Select D-MAC FM mode
            radMAC.doClick();
            int a = -1;
            for (int i = 0; i < macModeArray.length; i++) {
                if (macModeArray[i].equals("dmac-fm")) {
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
            if ( (uhfAL.get(0).equals("bsb")) && channelArray.length >= 5) {
                cmbChannel.setSelectedIndex(2);
            }
            else {
                messageBox("Unable to find the BSB band plan, which is required for this template.", JOptionPane.ERROR_MESSAGE);
                resetAllControls();
                return;                
            }
            // Enable pre-emphasis filter and enable FM deviation option
            chkVideoFilter.doClick();
            chkFMDev.doClick();
            // Set deviation according to the configured harmonic value.
            switch (PREFS.get("harmonic", "1")) {
                case "1":
                default:
                    txtFMDev.setText("11");
                    break;
                case "2":
                    txtFMDev.setText("8");
                    break;
                case "3":
                    txtFMDev.setText("6");
                    break;
                case "4":
                    txtFMDev.setText("4");
                    break;
            }
            messageBox("Template values have been loaded. Tune your receiver to the Galaxy "
                + "channel, or change this in the channel dropdown box on the Output tab.", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_menuBSBTemplateActionPerformed

    private void menuAstraTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAstraTemplateActionPerformed
        if (JOptionPane.showConfirmDialog(null, "This will load template values for an Astra satellite receiver.\n"
                        + "All current settings will be cleared. Do you wish to continue?", APP_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // Reset all controls
            resetAllControls();
            // Select PAL-FM mode
            int a = -1;
            for (int i = 0; i < palModeArray.length; i++) {
                if (palModeArray[i].equals("pal-fm")) {
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
            boolean f = false;
            for (int i = 0 ; i < frequencyArray.length; i++) {
                if (frequencyArray[i] == 10993750000L) {
                    cmbChannel.setSelectedIndex(i);
                    f = true;
                    break;
                }
            }
            if (!f) {
                messageBox("Unable to find the Astra band plan, which is required for this template.", JOptionPane.ERROR_MESSAGE);
                resetAllControls();
                return;                
            }
            // Enable pre-emphasis filter and enable FM deviation option
            chkVideoFilter.doClick();
            chkFMDev.doClick();
            // Set deviation according to the configured harmonic value.
            switch (PREFS.get("harmonic", "1")) {
                case "1":
                default:
                    txtFMDev.setText("11");
                    break;
                case "2":
                    txtFMDev.setText("8");
                    break;
                case "3":
                    txtFMDev.setText("6");
                    break;
                case "4":
                    txtFMDev.setText("4");
                    break;
            }
            var df = new DecimalFormat("0.00000");
            double input = frequency;
            String s = df.format(input / 1000000000);
            messageBox("Template values have been loaded. Tune your receiver to "
                    + s + " GHz and run hacktv.", JOptionPane.INFORMATION_MESSAGE);
            
        }
    }//GEN-LAST:event_menuAstraTemplateActionPerformed

    private void chkSecamIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSecamIdActionPerformed
        if (chkSecamId.isSelected()) {
            cmbSecamIdLines.setSelectedIndex(8);
            cmbSecamIdLines.setEnabled(true);
        }
        else {
            cmbSecamIdLines.setEnabled(false);
            cmbSecamIdLines.setSelectedIndex(-1);
        }
    }//GEN-LAST:event_chkSecamIdActionPerformed

    private void cmbSecamIdLinesMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbSecamIdLinesMouseWheelMoved
        SharedInst.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbSecamIdLines);
    }//GEN-LAST:event_cmbSecamIdLinesMouseWheelMoved

    private void chkColourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkColourActionPerformed
        // Disable "S-Video" option when "Disable colour" selected
        if ((chkColour.isSelected()) && (chkSVideo.isEnabled())) {
            if (chkSVideo.isSelected()) chkSVideo.doClick();
            chkSVideo.setEnabled(false);
        }
        else {
            if ( (INI.getStringFromINI(modesFile, mode, "modulation", "vsb", false).equals("baseband")) &&
                ( (cmbOutputDevice.getSelectedIndex() == 2)  ||
                (cmbOutputDevice.getSelectedIndex() == 3) ) &&
                ( (radPAL.isSelected()) || (radNTSC.isSelected()) ||
                    (radSECAM.isSelected()) ) ) {
                chkSVideo.setEnabled(true);
            }
        }
    }//GEN-LAST:event_chkColourActionPerformed

    private void chkSVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSVideoActionPerformed
        // Disable "Disable colour" option when S-Video selected
        if (chkSVideo.isSelected()) {
            if (chkColour.isSelected()) chkColour.doClick();
            chkColour.setEnabled(false);
        }
        else {
            if ( ((INI.getIntegerFromINI(modesFile, mode, "colour") == 1)) &&
                ( (radPAL.isSelected()) || (radNTSC.isSelected()) ||
                    (radSECAM.isSelected()) ) ){
                chkColour.setEnabled(true);
            }
        }
    }//GEN-LAST:event_chkSVideoActionPerformed

    private void cmbTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTestActionPerformed
        if ((!captainJack) && (radTest.isSelected())) {
            if (isPhilipsTestSignal()) {
                // Philips patterns use a fixed sample rate, usually 13.5 or 20 MHz.
                String tsr = getTCSampleRate();
                if (!txtSampleRate.getText().equals(tsr)) {
                    if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
                    txtPixelRate.setText(tsr);
                }
                txtSource.setEnabled(true);
                txtSource.setEditable(true);
                btnSourceBrowse.setEnabled(true);
                chkRepeat.setEnabled(true);
            }
            else {
                disableSourceOptions();
            }
        }
    }//GEN-LAST:event_cmbTestActionPerformed

    private void chkHackDACActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkHackDACActionPerformed
        if (chkHackDAC.isSelected()) {
            PREFS.putInt("hackdac", 1);
        }
        else {
            PREFS.remove("hackdac");
            if (INI.getStringFromINI(modesFile, mode, "modulation", "", false).equals("baseband")) checkBasebandSupport();
        }
    }//GEN-LAST:event_chkHackDACActionPerformed

    private void btnTestSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestSettingsActionPerformed
        // Show the setting dialogue box
        var td = new TestSettingsDialogue(this, true, hackTVDirectory);
        td.setVisible(true);
        if (td.settingsChanged()) {
            addTestCardOptions();
            if (radTest.isSelected()) radTest.doClick();
        }
    }//GEN-LAST:event_btnTestSettingsActionPerformed

    private void cmbLookAndFeelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmbLookAndFeelMouseEntered
        // Show tooltip as the friendly name may be longer than the combobox
        cmbLookAndFeel.setToolTipText(cmbLookAndFeel.getItemAt(cmbLookAndFeel.getSelectedIndex()));
    }//GEN-LAST:event_cmbLookAndFeelMouseEntered
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel additionalOptionsPanel;
    private javax.swing.ButtonGroup bandButtonGroup;
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
    private javax.swing.JButton btnSatSettings;
    private javax.swing.JButton btnSourceBrowse;
    private javax.swing.JButton btnSpark;
    private javax.swing.JButton btnTeefax;
    private javax.swing.JButton btnTeletextBrowse;
    private javax.swing.JButton btnTestSettings;
    private javax.swing.JCheckBox chkA2Stereo;
    private javax.swing.JCheckBox chkACP;
    private javax.swing.JCheckBox chkARCorrection;
    private javax.swing.JCheckBox chkActivateCard;
    private javax.swing.JCheckBox chkAmp;
    private javax.swing.JCheckBox chkAudio;
    private javax.swing.JCheckBox chkCC608;
    private javax.swing.JCheckBox chkColour;
    private javax.swing.JCheckBox chkDeactivateCard;
    private javax.swing.JCheckBox chkDownmix;
    private javax.swing.JCheckBox chkECppv;
    private javax.swing.JCheckBox chkFMDev;
    private javax.swing.JCheckBox chkFindKeys;
    private javax.swing.JCheckBox chkGamma;
    private javax.swing.JCheckBox chkHackDAC;
    private javax.swing.JCheckBox chkInterlace;
    private javax.swing.JCheckBox chkInvertVideo;
    private javax.swing.JCheckBox chkLocalModes;
    private javax.swing.JCheckBox chkLogo;
    private javax.swing.JCheckBox chkMac16k;
    private javax.swing.JCheckBox chkMacChId;
    private javax.swing.JCheckBox chkMacL2;
    private javax.swing.JCheckBox chkMacLinear;
    private javax.swing.JCheckBox chkMacMono;
    private javax.swing.JCheckBox chkNICAM;
    private javax.swing.JCheckBox chkNoDate;
    private javax.swing.JCheckBox chkNoUpdateCheck;
    private javax.swing.JCheckBox chkOffset;
    private javax.swing.JCheckBox chkOutputLevel;
    private javax.swing.JCheckBox chkPixelRate;
    private javax.swing.JCheckBox chkPosition;
    private javax.swing.JCheckBox chkRandom;
    private javax.swing.JCheckBox chkRepeat;
    private javax.swing.JCheckBox chkSVideo;
    private javax.swing.JCheckBox chkScrambleAudio;
    private javax.swing.JCheckBox chkSecamId;
    private javax.swing.JCheckBox chkShowCardSerial;
    private javax.swing.JCheckBox chkShowECM;
    private javax.swing.JCheckBox chkSiS;
    private javax.swing.JCheckBox chkSubtitles;
    private javax.swing.JCheckBox chkSwapIQ;
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
    private javax.swing.JComboBox<String> cmbARCorrection;
    private javax.swing.JComboBox<String> cmbChannel;
    private javax.swing.JComboBox<String> cmbECMaturity;
    private javax.swing.JComboBox<String> cmbFileType;
    private javax.swing.JComboBox<String> cmbFl2kAudio;
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
    private javax.swing.JComboBox<String> cmbSecamIdLines;
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
    private javax.swing.JPanel frequencyPanel;
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
    private javax.swing.JLabel lblFl2kAudio;
    private javax.swing.JLabel lblFork;
    private javax.swing.JLabel lblFrequency;
    private javax.swing.JLabel lblGain;
    private javax.swing.JLabel lblLookAndFeel;
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
    private javax.swing.ButtonGroup macCompressionButtonGroup;
    private javax.swing.JPanel macPanel;
    private javax.swing.ButtonGroup macProtectionButtonGroup;
    private javax.swing.ButtonGroup macSRButtonGroup;
    private javax.swing.ButtonGroup macStereoButtonGroup;
    private javax.swing.JMenuItem menuAbout;
    private javax.swing.JMenuItem menuAstraTemplate;
    private javax.swing.JMenuItem menuBSBTemplate;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuDownloadUpdate;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenuItem menuGithubRepo;
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
    private javax.swing.ButtonGroup modeButtonGroup;
    private javax.swing.JPanel modeButtonPanel;
    private javax.swing.JPanel modePanel;
    private javax.swing.JPanel modeTab;
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
    private javax.swing.JPanel runButtonGrid;
    private javax.swing.JPanel scramblingOptionsPanel;
    private javax.swing.JPanel scramblingPanel;
    private javax.swing.JPanel scramblingTab;
    private javax.swing.JPopupMenu.Separator sepAboutSeparator;
    private javax.swing.JPopupMenu.Separator sepExitSeparator;
    private javax.swing.JPopupMenu.Separator sepMruSeparator;
    private javax.swing.JPanel settingsTab;
    private javax.swing.ButtonGroup sourceButtonGroup;
    private javax.swing.JFileChooser sourceFileChooser;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JPanel sourceSelectPanel;
    private javax.swing.JPanel sourceTab;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JFileChooser teletextFileChooser;
    private javax.swing.JPanel teletextPanel;
    private javax.swing.JPanel teletextTab;
    private javax.swing.ButtonGroup templateButtonGroup;
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
    private javax.swing.JTextField txtOffset;
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
    private javax.swing.JPanel vbiPanel;
    // End of variables declaration//GEN-END:variables
}
