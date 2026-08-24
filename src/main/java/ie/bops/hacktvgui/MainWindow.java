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

import com.formdev.flatlaf.util.SystemFileChooser;
import ie.bops.hacktvgui.ModeInfo.AudioMode;
import static ie.bops.hacktvgui.ModeInfo.AudioMode.*;
import ie.bops.hacktvgui.ModeInfo.AudioModulation;
import static ie.bops.hacktvgui.ModeInfo.AudioModulation.*;
import ie.bops.hacktvgui.ModeInfo.ColourMode;
import static ie.bops.hacktvgui.ModeInfo.ColourMode.*;
import ie.bops.hacktvgui.ModeInfo.VideoModulation;
import static ie.bops.hacktvgui.ModeInfo.VideoModulation.*;
import ie.bops.hacktvgui.ScramblingSettings.VideoCryptEmmState;
import static ie.bops.hacktvgui.ScramblingSettings.VideoCryptEmmState.*;
import java.awt.CardLayout;
import java.awt.Component;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import java.text.DecimalFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JCheckBox;
import java.io.BufferedReader;
import java.io.FileReader;
import java.awt.Cursor;
import java.awt.Desktop;
import java.util.prefs.Preferences;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
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
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import java.nio.file.InvalidPathException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.xml.stream.XMLStreamException;

public class MainWindow extends javax.swing.JFrame {
    
    // Pseudo-random number generator, used for the Randomise playlist option
    private static final Random RND = new Random();
    
    // Boolean used for Microsoft Windows detection and handling
    private final boolean isWindows = System.getProperty("os.name").contains("Windows");
    private final boolean isMacOS = System.getProperty("os.name").contains("Mac");
    
    // Look and feel
    String defaultLaf;
    
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

    // Declare a variable to determine the selected fork
    private boolean captainJack;
    private boolean supportsPhilipsTestSignal;

    // Declare Teletext-related variables that are reused across multiple subs
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

    // Declare a variable for storing the default sample rate for the selected video mode
    // This allows us to revert back to the default if the sample rate is changed by filters or scrambling systems
    private String defaultSampleRate;
    
    // Declare combobox arrays and ArrayLists
    // These are used to store secondary information (frequencies, parameters, etc)
    private final ArrayList<ModeInfo> modes = new ArrayList<>();

    private final Map<String, Integer> testCommandToIndex = new HashMap<>();

    // Checkbox array for the File > New option
    private javax.swing.JCheckBox[] checkBoxes;
    
    // Preferences node
    public static final Preferences PREFS = Preferences.userNodeForPackage(MainWindow.class);
    
    // hacktv process, used to gracefully close hacktv via the Stop button
    private Process hacktvProcess;
    
    // Boolean to determine if hacktv is running or not
    private boolean running;
    
    // Boolean to determine if a config file is in the process of loading
    private boolean htvLoadInProgress = false;  
    
    // Integer to save the previously selected item in the Mode combobox.
    // Used to revert back if a baseband mode is selected on an unsupported SDR.
    private int previousIndex = 0;
    
    // Allows us to recall the previously selected colour system
    private ColourMode prevColour;
    
    // Playlist model, used for storing items from the JList
    private final DefaultListModel<String> playlistModel = new DefaultListModel<>();
    
    // CA systems and keys
    private final LinkedHashMap<String, ScramblingInfo> scramblingInfo625 = new LinkedHashMap<>();
    private final LinkedHashMap<String, ScramblingInfo> scramblingInfoMac = new LinkedHashMap<>();
    
    // Start point in playlist
    private int startPoint = -1;
    
    // Declare variables used for storing parameters
    //private String mode = "";
    private long frequency;
    
    // Default LNB local oscillator frequency in GHz
    public static final double DEFAULT_LO = 9.75;
    
    // INI class instances (one for each file)
    private final INIFile modesIni = new INIFile();
    private final INIFile bpIni = new INIFile();
    private final INIFile flIni = new INIFile();
    
    // Drag anchor for playlist box
    private int dragAnchor = -1;
    
    // ScramblingSettings object, used for the settings in
    // the Advanced button dialogue on the Scrambling tab.
    private ScramblingSettings scramblingSettings = null;
    
    // MacSettings object
    private MacSettings macSettings = null;
    
    // Frequency list for dropdown on Output tab
    private static final String SATELLITE_BAND = "Satellite";
    private static final String UHF_BAND = "UHF";
    private static final String VHF_BAND = "VHF";
    private static final String CUSTOM_FREQUENCY = "Custom";
    
    private static final String DOWNLOAD_TELETEXT = "Download";
    private static final String STOP_DOWNLOAD = "Stop";
    
    // Boolean used for carriage return (CR) handling when capturing hacktv's output
    boolean cr;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bgAudio = new javax.swing.ButtonGroup();
        bgSource = new javax.swing.ButtonGroup();
        tabPane = new javax.swing.JTabbedPane();
        sourceTab = new javax.swing.JPanel();
        sourceTabContainer = new javax.swing.JPanel();
        btnSourceBrowse = new javax.swing.JButton();
        chkRandom = new javax.swing.JCheckBox();
        chkRepeat = new javax.swing.JCheckBox();
        playlistScrollPane = new javax.swing.JScrollPane();
        lstPlaylist = new javax.swing.JList<>();
        lstPlaylist.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragAnchor = lstPlaylist.locationToIndex(e.getPoint());
            }
        });

        lstPlaylist.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int index = lstPlaylist.locationToIndex(e.getPoint());
                if (dragAnchor != -1 && index != -1) {
                    lstPlaylist.addSelectionInterval(
                        Math.min(dragAnchor, index),
                        Math.max(dragAnchor, index)
                    );
                }
            }
        });

        lstPlaylist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                dragAnchor = -1;
            }
        });

        lstPlaylist.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

                DefaultListModel<?> model = (DefaultListModel<?>) list.getModel();

                // Reset defaults first
                label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                label.setFont(label.getFont().deriveFont(Font.PLAIN));

                // Appear disabled when empty
                if (model.isEmpty() && !isSelected) {
                    label.setForeground(javax.swing.UIManager.getDefaults().getColor("TextArea.disabledBackground"));
                }

                return label;
            }
        });

        playlistModel.addListDataListener(new javax.swing.event.ListDataListener() {
            @Override
            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                updateState();
            }

            @Override
            public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                updateState();
            }

            @Override
            public void contentsChanged(javax.swing.event.ListDataEvent e) {
                updateState();
            }

            private void updateState() {
                lstPlaylist.setBackground(playlistModel.isEmpty() ? 
                    javax.swing.UIManager.getDefaults().getColor("TextArea.disabledBackground"):
                    javax.swing.UIManager.getDefaults().getColor("TextArea.background")
                );
                // Enable or disable random option
                if (playlistModel.size() > 1) {
                    chkRandom.setEnabled(true);
                } else {
                    if (chkRandom.isSelected()) chkRandom.doClick();
                    chkRandom.setEnabled(false);
                }
            }
        });
        sourceCardPanel = new javax.swing.JPanel();
        txtSource = new javax.swing.JTextField();
        cmbM3USource = new javax.swing.JComboBox<>();
        sourceSelectionPanel = new javax.swing.JPanel();
        radLocalSource = new javax.swing.JRadioButton();
        radTest = new javax.swing.JRadioButton();
        cmbTest = new javax.swing.JComboBox<>();
        btnTestSettings = new javax.swing.JButton();
        playlistButtonsPanel = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnPlaylistStart = new javax.swing.JButton();
        btnPlaylistUp = new javax.swing.JButton();
        btnPlaylistDown = new javax.swing.JButton();
        modeTab = new javax.swing.JPanel();
        modeTabContainerPanel = new javax.swing.JPanel();
        modeContainerPanel = new javax.swing.JPanel();
        modePanel = new javax.swing.JPanel();
        spCategory = new javax.swing.JScrollPane();
        lstColour = new javax.swing.JList<>();
        cmbMode = new javax.swing.JComboBox<>();
        lblLinesDesc = new javax.swing.JLabel();
        lblLinesValue = new javax.swing.JLabel();
        lblFieldRateDesc = new javax.swing.JLabel();
        lblFieldRateValue = new javax.swing.JLabel();
        lblAudioModDesc = new javax.swing.JLabel();
        lblVideoModValue = new javax.swing.JLabel();
        lblVideoModDesc = new javax.swing.JLabel();
        lblAudioModValue = new javax.swing.JLabel();
        lblAudioSpacingDesc = new javax.swing.JLabel();
        lblAudioSpacingValue = new javax.swing.JLabel();
        btnMacOptions = new javax.swing.JButton();
        audioPanel = new javax.swing.JPanel();
        radMono = new javax.swing.JRadioButton();
        radNoAudio = new javax.swing.JRadioButton();
        radNICAM = new javax.swing.JRadioButton();
        radA2Stereo = new javax.swing.JRadioButton();
        chkSiS = new javax.swing.JCheckBox();
        advModePanel = new javax.swing.JPanel();
        chkPixelRate = new javax.swing.JCheckBox();
        lblSampleRate = new javax.swing.JLabel();
        txtSampleRate = new javax.swing.JTextField();
        txtPixelRate = new javax.swing.JTextField();
        txtFMDev = new javax.swing.JTextField();
        chkFMDev = new javax.swing.JCheckBox();
        chkColour = new javax.swing.JCheckBox();
        chkInvertVideo = new javax.swing.JCheckBox();
        chkFmFilter = new javax.swing.JCheckBox();
        chkSwapIQ = new javax.swing.JCheckBox();
        chkVsbFilter = new javax.swing.JCheckBox();
        outputTab = new javax.swing.JPanel();
        outputContainerPanel = new javax.swing.JPanel();
        rfPanel = new javax.swing.JPanel();
        lblBand = new javax.swing.JLabel();
        cmbBand = new javax.swing.JComboBox<>();
        lblRegion = new javax.swing.JLabel();
        cmbRegion = new javax.swing.JComboBox<>();
        lblChannel = new javax.swing.JLabel();
        cmbChannel = new javax.swing.JComboBox<>();
        lblFrequency = new javax.swing.JLabel();
        txtFrequency = new javax.swing.JTextField();
        chkLockFrequency = new javax.swing.JCheckBox();
        deviceOptionsPanel = new javax.swing.JPanel();
        chkAmp = new javax.swing.JCheckBox();
        lblGain = new javax.swing.JLabel();
        txtGain = new javax.swing.JTextField();
        lblAntennaName = new javax.swing.JLabel();
        lblFileType = new javax.swing.JLabel();
        chkHackDAC = new javax.swing.JCheckBox();
        txtAntennaName = new javax.swing.JTextField();
        cmbFileType = new javax.swing.JComboBox<>();
        outputDevicePanel = new javax.swing.JPanel();
        lblOutputDevice = new javax.swing.JLabel();
        cmbOutputDevice = new javax.swing.JComboBox<>();
        lblOutputDevice2 = new javax.swing.JLabel();
        txtOutputDevice = new javax.swing.JTextField();
        fl2kOptionsPanel = new javax.swing.JPanel();
        lblFl2kAudio = new javax.swing.JLabel();
        cmbFl2kAudio = new javax.swing.JComboBox<>();
        chkSVideo = new javax.swing.JCheckBox();
        chkOffset = new javax.swing.JCheckBox();
        txtOffset = new javax.swing.JTextField();
        playbackTab = new javax.swing.JPanel();
        playbackContainerPanel = new javax.swing.JPanel();
        playbackOptionsPanel = new javax.swing.JPanel();
        chkAspectRatio = new javax.swing.JCheckBox();
        chkInterlace = new javax.swing.JCheckBox();
        chkGamma = new javax.swing.JCheckBox();
        chkOutputLevel = new javax.swing.JCheckBox();
        chkVolume = new javax.swing.JCheckBox();
        txtVolume = new javax.swing.JTextField();
        txtOutputLevel = new javax.swing.JTextField();
        txtGamma = new javax.swing.JTextField();
        cmbAspectRatio = new javax.swing.JComboBox<>();
        captainJackPanel = new javax.swing.JPanel();
        chkPosition = new javax.swing.JCheckBox();
        txtPosition = new javax.swing.JTextField();
        chkLogo = new javax.swing.JCheckBox();
        cmbLogo = new javax.swing.JComboBox<>();
        chkSubtitles = new javax.swing.JCheckBox();
        txtSubtitleIndex = new javax.swing.JTextField();
        chkTimestamp = new javax.swing.JCheckBox();
        chkDownmix = new javax.swing.JCheckBox();
        teletextTab = new javax.swing.JPanel();
        teletextContainerPanel = new javax.swing.JPanel();
        vbiOptionsPanel = new javax.swing.JPanel();
        chkWSS = new javax.swing.JCheckBox();
        chkACP = new javax.swing.JCheckBox();
        chkVITS = new javax.swing.JCheckBox();
        chkVITC = new javax.swing.JCheckBox();
        chkSecamId = new javax.swing.JCheckBox();
        chkCC608 = new javax.swing.JCheckBox();
        cmbWSS = new javax.swing.JComboBox<>();
        cmbSecamIdLines = new javax.swing.JComboBox<>();
        teletextDownloadPanel = new javax.swing.JPanel();
        cmbTeletextDownload = new javax.swing.JComboBox<>();
        btnTeletextDownload = new javax.swing.JButton();
        lblTeletextDownloadHeader = new javax.swing.JLabel();
        lblTeletextDescription = new javax.swing.JLabel();
        teletextPanel = new javax.swing.JPanel();
        chkTeletext = new javax.swing.JCheckBox();
        chkTeletextSubtitles = new javax.swing.JCheckBox();
        txtTeletextSource = new javax.swing.JTextField();
        txtTeletextSubtitleIndex = new javax.swing.JTextField();
        btnTeletextBrowse = new javax.swing.JButton();
        lblTeletextSubtitleIndex = new javax.swing.JLabel();
        scramblingTab = new javax.swing.JPanel();
        scramblingContainerPanel = new javax.swing.JPanel();
        scramblingPanel = new javax.swing.JPanel();
        lblScrambling1 = new javax.swing.JLabel();
        lblScrambling2 = new javax.swing.JLabel();
        lblScrambling3 = new javax.swing.JLabel();
        cmbScrambling1 = new javax.swing.JComboBox<>();
        cmbScrambling2 = new javax.swing.JComboBox<>();
        cmbScrambling3 = new javax.swing.JComboBox<>();
        btnScramblingOptions = new javax.swing.JButton();
        settingsTab = new javax.swing.JPanel();
        settingsContainerPanel = new javax.swing.JPanel();
        hacktvPathPanel = new javax.swing.JPanel();
        txtHackTVPath = new javax.swing.JTextField();
        btnHackTVPath = new javax.swing.JButton();
        btnDownloadHackTV = new javax.swing.JButton();
        lblHackTVLocation = new javax.swing.JLabel();
        buildLabelPanel = new javax.swing.JPanel();
        lblDetectedBuild = new javax.swing.JLabel();
        lblFork = new javax.swing.JLabel();
        generalSettingsPanel = new javax.swing.JPanel();
        chkSyntaxOnly = new javax.swing.JCheckBox();
        chkLocalModes = new javax.swing.JCheckBox();
        chkUpdateCheck = new javax.swing.JCheckBox();
        btnSatSettings = new javax.swing.JButton();
        comboBoxPanel = new javax.swing.JPanel();
        lblNMSCeefaxRegion = new javax.swing.JLabel();
        lblLookAndFeel = new javax.swing.JLabel();
        cmbNMSCeefaxRegion = new javax.swing.JComboBox<>();
        cmbLookAndFeel = new javax.swing.JComboBox<>();
        resetSettingsPanel = new javax.swing.JPanel();
        btnResetAllSettings = new javax.swing.JButton();
        btnClearMRUList = new javax.swing.JButton();
        buttonPanel = new javax.swing.JPanel();
        btnRun = new javax.swing.JButton();
        btnHideConsole = new javax.swing.JButton();
        consolePanel = new javax.swing.JPanel();
        consoleScrollPane = new javax.swing.JScrollPane();
        txtConsoleOutput = new javax.swing.JTextArea();
        txtStatus = new javax.swing.JTextField();
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
        setTitle("GUI wrapper for hacktv");
        setName("mainFrame"); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        sourceTab.setLayout(new java.awt.GridBagLayout());

        sourceTabContainer.setBorder(javax.swing.BorderFactory.createTitledBorder("Source options"));
        sourceTabContainer.setPreferredSize(new java.awt.Dimension(570, 300));
        sourceTabContainer.setLayout(new java.awt.GridBagLayout());

        btnSourceBrowse.setText("Browse...");
        btnSourceBrowse.addActionListener(this::btnSourceBrowseActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        sourceTabContainer.add(btnSourceBrowse, gridBagConstraints);

        chkRandom.setText("Randomise playlist");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        sourceTabContainer.add(chkRandom, gridBagConstraints);

        chkRepeat.setText("Repeat indefinitely");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        sourceTabContainer.add(chkRepeat, gridBagConstraints);

        lstPlaylist.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.disabledBackground"));
        lstPlaylist.setModel(playlistModel);
        lstPlaylist.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

                // Reset to normal font first
                label.setFont(label.getFont().deriveFont(Font.PLAIN));

                // Apply italic if this is the start point
                if (index == startPoint) {
                    label.setFont(label.getFont().deriveFont(Font.ITALIC));
                }

                return label;
            }
        });
        lstPlaylist.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lstPlaylistKeyPressed(evt);
            }
        });
        lstPlaylist.addListSelectionListener(this::lstPlaylistValueChanged);
        playlistScrollPane.setViewportView(lstPlaylist);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        sourceTabContainer.add(playlistScrollPane, gridBagConstraints);

        sourceCardPanel.setLayout(new java.awt.CardLayout());

        txtSource.addMouseListener(new ContextMenuListener());
        sourceCardPanel.add(txtSource, "textbox");
        sourceCardPanel.add(cmbM3USource, "m3ucombobox");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        sourceTabContainer.add(sourceCardPanel, gridBagConstraints);

        sourceSelectionPanel.setLayout(new java.awt.GridBagLayout());

        bgSource.add(radLocalSource);
        radLocalSource.setText("Local or internet");
        radLocalSource.addActionListener(this::radLocalSourceActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        sourceSelectionPanel.add(radLocalSource, gridBagConstraints);

        bgSource.add(radTest);
        radTest.setText("Test signal");
        radTest.addActionListener(this::radTestActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        sourceSelectionPanel.add(radTest, gridBagConstraints);

        cmbTest.setEnabled(false);
        cmbTest.addMouseWheelListener(this::cmbTestMouseWheelMoved);
        cmbTest.addActionListener(this::cmbTestActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        sourceSelectionPanel.add(cmbTest, gridBagConstraints);

        btnTestSettings.setText("Settings...");
        btnTestSettings.addActionListener(this::btnTestSettingsActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 0, 0);
        sourceSelectionPanel.add(btnTestSettings, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        sourceTabContainer.add(sourceSelectionPanel, gridBagConstraints);

        playlistButtonsPanel.setLayout(new java.awt.GridLayout(5, 1, 0, 4));

        btnAdd.setText("Add");
        btnAdd.addActionListener(this::btnAddActionPerformed);
        playlistButtonsPanel.add(btnAdd);

        btnRemove.setText("Remove");
        btnRemove.setEnabled(false);
        btnRemove.addActionListener(this::btnRemoveActionPerformed);
        playlistButtonsPanel.add(btnRemove);

        btnPlaylistStart.setText("Play first");
        btnPlaylistStart.setEnabled(false);
        btnPlaylistStart.addActionListener(this::btnPlaylistStartActionPerformed);
        playlistButtonsPanel.add(btnPlaylistStart);

        btnPlaylistUp.setText("˄");
        btnPlaylistUp.setEnabled(false);
        btnPlaylistUp.addActionListener(this::btnPlaylistUpActionPerformed);
        playlistButtonsPanel.add(btnPlaylistUp);

        btnPlaylistDown.setText("˅");
        btnPlaylistDown.setEnabled(false);
        btnPlaylistDown.addActionListener(this::btnPlaylistDownActionPerformed);
        playlistButtonsPanel.add(btnPlaylistDown);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        sourceTabContainer.add(playlistButtonsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        sourceTab.add(sourceTabContainer, gridBagConstraints);

        tabPane.addTab("Source", sourceTab);

        modeTab.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        modeTabContainerPanel.setLayout(new java.awt.GridBagLayout());

        modeContainerPanel.setLayout(new java.awt.GridBagLayout());

        modePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Mode options"));
        modePanel.setLayout(new java.awt.GridBagLayout());

        lstColour.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstColour.setPrototypeCellValue(new ColourOption(null, "Black and white XXX"));
        lstColour.setVisibleRowCount(6);
        var width = btnMacOptions.getPreferredSize().width;
        var height = lstColour.getPreferredSize().height;
        lstColour.setPreferredSize(new Dimension(width, height));
        lstColour.addListSelectionListener(this::lstColourValueChanged);
        spCategory.setViewportView(lstColour);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 8);
        modePanel.add(spCategory, gridBagConstraints);

        cmbMode.setPrototypeDisplayValue(new ModeInfo(
            null,
            null,
            "XXXXXXXXXXXXXXXXXXXXXXXXXXX",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            false,
            false,
            null,
            false,
            false,
            false,
            false,
            false,
            false,
            null,
            null,
            null,
            null
        ));
        cmbMode.addMouseWheelListener(this::cmbModeMouseWheelMoved);
        cmbMode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cmbModeMouseEntered(evt);
            }
        });
        cmbMode.addActionListener(this::cmbModeActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 7);
        modePanel.add(cmbMode, gridBagConstraints);

        lblLinesDesc.setText("Lines:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        modePanel.add(lblLinesDesc, gridBagConstraints);

        lblLinesValue.setText("lines");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 8);
        modePanel.add(lblLinesValue, gridBagConstraints);

        lblFieldRateDesc.setText("Frame/field rate:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        modePanel.add(lblFieldRateDesc, gridBagConstraints);

        lblFieldRateValue.setText("fieldrate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 8);
        modePanel.add(lblFieldRateValue, gridBagConstraints);

        lblAudioModDesc.setText("Audio modulation:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        modePanel.add(lblAudioModDesc, gridBagConstraints);

        lblVideoModValue.setText("videomod");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 8);
        modePanel.add(lblVideoModValue, gridBagConstraints);

        lblVideoModDesc.setText("Video modulation:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        modePanel.add(lblVideoModDesc, gridBagConstraints);

        lblAudioModValue.setText("audiomod");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 8);
        modePanel.add(lblAudioModValue, gridBagConstraints);

        lblAudioSpacingDesc.setText("Audio spacing:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 8, 0);
        modePanel.add(lblAudioSpacingDesc, gridBagConstraints);

        lblAudioSpacingValue.setText("audiospacing");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 8, 8);
        modePanel.add(lblAudioSpacingValue, gridBagConstraints);

        btnMacOptions.setText("MAC options");
        btnMacOptions.addActionListener(this::btnMacOptionsActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        modePanel.add(btnMacOptions, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        modeContainerPanel.add(modePanel, gridBagConstraints);

        audioPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Audio options"));
        audioPanel.setLayout(new java.awt.GridBagLayout());

        bgAudio.add(radMono);
        radMono.setText("Mono");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        audioPanel.add(radMono, gridBagConstraints);

        bgAudio.add(radNoAudio);
        radNoAudio.setText("No carrier");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        audioPanel.add(radNoAudio, gridBagConstraints);

        bgAudio.add(radNICAM);
        radNICAM.setText("NICAM stereo");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        audioPanel.add(radNICAM, gridBagConstraints);

        bgAudio.add(radA2Stereo);
        radA2Stereo.setText("A2 (Zweikanalton) stereo");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        audioPanel.add(radA2Stereo, gridBagConstraints);

        chkSiS.setText("Sound-In-Syncs");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        audioPanel.add(chkSiS, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        modeContainerPanel.add(audioPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        modeTabContainerPanel.add(modeContainerPanel, gridBagConstraints);

        advModePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Advanced mode options"));
        advModePanel.setLayout(new java.awt.GridBagLayout());

        chkPixelRate.setText("Pixel rate (MHz)");
        chkPixelRate.addActionListener(this::chkPixelRateActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        advModePanel.add(chkPixelRate, gridBagConstraints);

        lblSampleRate.setText("Sample rate (MHz)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 4);
        advModePanel.add(lblSampleRate, gridBagConstraints);

        txtSampleRate.setPreferredSize(new java.awt.Dimension(48, 22));
        txtSampleRate.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        advModePanel.add(txtSampleRate, gridBagConstraints);

        txtPixelRate.setEditable(false);
        txtPixelRate.setEnabled(false);
        txtPixelRate.setPreferredSize(new java.awt.Dimension(48, 22));
        txtPixelRate.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        advModePanel.add(txtPixelRate, gridBagConstraints);

        txtFMDev.setEditable(false);
        txtFMDev.setEnabled(false);
        txtFMDev.setPreferredSize(new java.awt.Dimension(48, 22));
        txtFMDev.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        advModePanel.add(txtFMDev, gridBagConstraints);

        chkFMDev.setText("FM deviation (MHz)");
        chkFMDev.addActionListener(this::chkFMDevActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        advModePanel.add(chkFMDev, gridBagConstraints);

        chkColour.setText("Colour");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        advModePanel.add(chkColour, gridBagConstraints);

        chkInvertVideo.setText("Invert video");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        advModePanel.add(chkInvertVideo, gridBagConstraints);

        chkFmFilter.setText("FM video pre-emphasis filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        advModePanel.add(chkFmFilter, gridBagConstraints);

        chkSwapIQ.setText("Swap I/Q samples");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        advModePanel.add(chkSwapIQ, gridBagConstraints);

        chkVsbFilter.setText("VSB filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        advModePanel.add(chkVsbFilter, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 7, 7);
        modeTabContainerPanel.add(advModePanel, gridBagConstraints);

        modeTab.add(modeTabContainerPanel);

        tabPane.addTab("Mode", modeTab);

        outputTab.setLayout(new java.awt.GridBagLayout());

        outputContainerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Frequency and TX options"));
        outputContainerPanel.setLayout(new java.awt.GridBagLayout());

        rfPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("RF options"));
        rfPanel.setLayout(new java.awt.GridBagLayout());

        lblBand.setText("Band");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        rfPanel.add(lblBand, gridBagConstraints);

        cmbBand.setPrototypeDisplayValue("XXXXXXXXXXXXX");
        cmbBand.addMouseWheelListener(this::cmbBandMouseWheelMoved);
        cmbBand.addActionListener(this::cmbBandActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        rfPanel.add(cmbBand, gridBagConstraints);

        lblRegion.setText("Region");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        rfPanel.add(lblRegion, gridBagConstraints);

        cmbRegion.setPrototypeDisplayValue(new BandPlan(null, null, "00Continental Europe00", null));
        cmbRegion.addMouseWheelListener(this::cmbRegionMouseWheelMoved);
        cmbRegion.addActionListener(this::cmbRegionActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        rfPanel.add(cmbRegion, gridBagConstraints);

        lblChannel.setText("Channel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        rfPanel.add(lblChannel, gridBagConstraints);

        cmbChannel.setPrototypeDisplayValue(new Channel("XXXXXXXXXXXXXXX", Long.MIN_VALUE, null));
        cmbChannel.addMouseWheelListener(this::cmbChannelMouseWheelMoved);
        cmbChannel.addActionListener(this::cmbChannelActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        rfPanel.add(cmbChannel, gridBagConstraints);

        lblFrequency.setText("Frequency (MHz)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        rfPanel.add(lblFrequency, gridBagConstraints);

        txtFrequency.setEditable(false);
        txtFrequency.setPreferredSize(new java.awt.Dimension(64, 22));
        txtFrequency.addMouseListener(new ContextMenuListener());
        txtFrequency.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtFrequencyKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        rfPanel.add(txtFrequency, gridBagConstraints);

        chkLockFrequency.setText("Lock");
        chkLockFrequency.addActionListener(this::chkLockFrequencyActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        rfPanel.add(chkLockFrequency, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        outputContainerPanel.add(rfPanel, gridBagConstraints);

        deviceOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Other device-specific options"));
        deviceOptionsPanel.setLayout(new java.awt.GridBagLayout());

        chkAmp.setText("TX amplifier");
        chkAmp.addActionListener(this::chkAmpActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        deviceOptionsPanel.add(chkAmp, gridBagConstraints);

        lblGain.setText("TX gain (dB)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        deviceOptionsPanel.add(lblGain, gridBagConstraints);

        txtGain.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 32;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        deviceOptionsPanel.add(txtGain, gridBagConstraints);

        lblAntennaName.setText("Antenna name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        deviceOptionsPanel.add(lblAntennaName, gridBagConstraints);

        lblFileType.setText("File type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        deviceOptionsPanel.add(lblFileType, gridBagConstraints);

        chkHackDAC.setText("HackDAC support");
        chkHackDAC.addActionListener(this::chkHackDACActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        deviceOptionsPanel.add(chkHackDAC, gridBagConstraints);

        txtAntennaName.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        deviceOptionsPanel.add(txtAntennaName, gridBagConstraints);

        cmbFileType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "uint8", "int8", "uint16", "int16", "int32", "float" }));
        cmbFileType.addMouseWheelListener(this::cmbFileTypeMouseWheelMoved);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        deviceOptionsPanel.add(cmbFileType, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        outputContainerPanel.add(deviceOptionsPanel, gridBagConstraints);

        outputDevicePanel.setLayout(new java.awt.GridBagLayout());

        lblOutputDevice.setText("Output device");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        outputDevicePanel.add(lblOutputDevice, gridBagConstraints);

        cmbOutputDevice.addMouseWheelListener(this::cmbOutputDeviceMouseWheelMoved);
        cmbOutputDevice.addActionListener(this::cmbOutputDeviceActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        outputDevicePanel.add(cmbOutputDevice, gridBagConstraints);

        lblOutputDevice2.setText("Serial number");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        outputDevicePanel.add(lblOutputDevice2, gridBagConstraints);

        txtOutputDevice.setColumns(16);
        txtOutputDevice.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        outputDevicePanel.add(txtOutputDevice, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        outputContainerPanel.add(outputDevicePanel, gridBagConstraints);

        fl2kOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("FL2000 options"));
        fl2kOptionsPanel.setLayout(new java.awt.GridBagLayout());

        lblFl2kAudio.setText("FL2K audio mode");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        fl2kOptionsPanel.add(lblFl2kAudio, gridBagConstraints);

        cmbFl2kAudio.addMouseWheelListener(this::cmbFl2kAudioMouseWheelMoved);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        fl2kOptionsPanel.add(cmbFl2kAudio, gridBagConstraints);

        chkSVideo.setText("S-Video output");
        chkSVideo.addActionListener(this::chkSVideoActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        fl2kOptionsPanel.add(chkSVideo, gridBagConstraints);

        chkOffset.setText("Offset (MHz)");
        chkOffset.addActionListener(this::chkOffsetActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        fl2kOptionsPanel.add(chkOffset, gridBagConstraints);

        txtOffset.setEditable(false);
        txtOffset.setEnabled(false);
        txtOffset.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 32;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        fl2kOptionsPanel.add(txtOffset, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        outputContainerPanel.add(fl2kOptionsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        outputTab.add(outputContainerPanel, gridBagConstraints);

        tabPane.addTab("Output", outputTab);

        playbackTab.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        playbackContainerPanel.setLayout(new java.awt.GridBagLayout());

        playbackOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Playback options"));
        playbackOptionsPanel.setLayout(new java.awt.GridBagLayout());

        chkAspectRatio.setText("Aspect ratio scaling");
        chkAspectRatio.addActionListener(this::chkAspectRatioActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        playbackOptionsPanel.add(chkAspectRatio, gridBagConstraints);

        chkInterlace.setText("Update video every field");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        playbackOptionsPanel.add(chkInterlace, gridBagConstraints);

        chkGamma.setText("Gamma correction");
        chkGamma.addActionListener(this::chkGammaActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        playbackOptionsPanel.add(chkGamma, gridBagConstraints);

        chkOutputLevel.setText("Output level");
        chkOutputLevel.addActionListener(this::chkOutputLevelActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        playbackOptionsPanel.add(chkOutputLevel, gridBagConstraints);

        chkVolume.setText("Volume");
        chkVolume.addActionListener(this::chkVolumeActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        playbackOptionsPanel.add(chkVolume, gridBagConstraints);

        txtVolume.setEditable(false);
        txtVolume.setEnabled(false);
        txtVolume.setPreferredSize(new java.awt.Dimension(48, 22));
        txtVolume.addMouseListener(new ContextMenuListener());
        txtVolume.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtVolumeKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        playbackOptionsPanel.add(txtVolume, gridBagConstraints);

        txtOutputLevel.setEditable(false);
        txtOutputLevel.setEnabled(false);
        txtOutputLevel.setPreferredSize(new java.awt.Dimension(48, 22));
        txtOutputLevel.addMouseListener(new ContextMenuListener());
        txtOutputLevel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtOutputLevelKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        playbackOptionsPanel.add(txtOutputLevel, gridBagConstraints);

        txtGamma.setEditable(false);
        txtGamma.setEnabled(false);
        txtGamma.setPreferredSize(new java.awt.Dimension(48, 22));
        txtGamma.addMouseListener(new ContextMenuListener());
        txtGamma.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtGammaKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        playbackOptionsPanel.add(txtGamma, gridBagConstraints);

        cmbAspectRatio.setEnabled(false);
        cmbAspectRatio.addMouseWheelListener(this::cmbAspectRatioMouseWheelMoved);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        playbackOptionsPanel.add(cmbAspectRatio, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        playbackContainerPanel.add(playbackOptionsPanel, gridBagConstraints);

        captainJackPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Captain Jack options"));
        captainJackPanel.setLayout(new java.awt.GridBagLayout());

        chkPosition.setText("Start position (minutes)");
        chkPosition.addActionListener(this::chkPositionActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        captainJackPanel.add(chkPosition, gridBagConstraints);

        txtPosition.setEditable(false);
        txtPosition.setEnabled(false);
        txtPosition.setPreferredSize(new java.awt.Dimension(48, 22));
        txtPosition.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        captainJackPanel.add(txtPosition, gridBagConstraints);

        chkLogo.setText("Overlay logo");
        chkLogo.addActionListener(this::chkLogoActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        captainJackPanel.add(chkLogo, gridBagConstraints);

        cmbLogo.setEnabled(false);
        cmbLogo.addMouseWheelListener(this::cmbLogoMouseWheelMoved);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        captainJackPanel.add(cmbLogo, gridBagConstraints);

        chkSubtitles.setText("Overlay subtitles (index)");
        chkSubtitles.addActionListener(this::chkSubtitlesActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        captainJackPanel.add(chkSubtitles, gridBagConstraints);

        txtSubtitleIndex.setEditable(false);
        txtSubtitleIndex.setEnabled(false);
        txtSubtitleIndex.setPreferredSize(new java.awt.Dimension(48, 22));
        txtSubtitleIndex.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        captainJackPanel.add(txtSubtitleIndex, gridBagConstraints);

        chkTimestamp.setText("Overlay timestamp");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        captainJackPanel.add(chkTimestamp, gridBagConstraints);

        chkDownmix.setText("Downmix 5.1 audio to 2.0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        captainJackPanel.add(chkDownmix, gridBagConstraints);

        playbackContainerPanel.add(captainJackPanel, new java.awt.GridBagConstraints());

        playbackTab.add(playbackContainerPanel);

        tabPane.addTab("Playback", playbackTab);

        teletextTab.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        teletextContainerPanel.setLayout(new java.awt.GridBagLayout());

        vbiOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("VBI options"));
        vbiOptionsPanel.setLayout(new java.awt.GridBagLayout());

        chkWSS.setText("Widescreen signalling (WSS)");
        chkWSS.addActionListener(this::chkWSSActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        vbiOptionsPanel.add(chkWSS, gridBagConstraints);

        chkACP.setText("Analogue Copy Protection");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        vbiOptionsPanel.add(chkACP, gridBagConstraints);

        chkVITS.setText("VITS");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        vbiOptionsPanel.add(chkVITS, gridBagConstraints);

        chkVITC.setText("VITC");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        vbiOptionsPanel.add(chkVITC, gridBagConstraints);

        chkSecamId.setText("SECAM field ID");
        chkSecamId.addActionListener(this::chkSecamIdActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        vbiOptionsPanel.add(chkSecamId, gridBagConstraints);

        chkCC608.setText("Closed captions (CEA/EIA-608)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        vbiOptionsPanel.add(chkCC608, gridBagConstraints);

        cmbWSS.setEnabled(false);
        cmbWSS.addMouseWheelListener(this::cmbWSSMouseWheelMoved);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        vbiOptionsPanel.add(cmbWSS, gridBagConstraints);

        cmbSecamIdLines.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        cmbSecamIdLines.setSelectedIndex(-1);
        cmbSecamIdLines.setEnabled(false);
        cmbSecamIdLines.addMouseWheelListener(this::cmbSecamIdLinesMouseWheelMoved);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        vbiOptionsPanel.add(cmbSecamIdLines, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
        teletextContainerPanel.add(vbiOptionsPanel, gridBagConstraints);

        teletextDownloadPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Teletext downloads"));
        teletextDownloadPanel.setEnabled(false);
        teletextDownloadPanel.setLayout(new java.awt.GridBagLayout());

        cmbTeletextDownload.setEnabled(false);
        cmbTeletextDownload.addMouseWheelListener(this::cmbTeletextDownloadMouseWheelMoved);
        cmbTeletextDownload.addActionListener(this::cmbTeletextDownloadActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        teletextDownloadPanel.add(cmbTeletextDownload, gridBagConstraints);

        btnTeletextDownload.setText("Download");
        btnTeletextDownload.setEnabled(false);
        btnTeletextDownload.addActionListener(this::btnTeletextDownloadActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        teletextDownloadPanel.add(btnTeletextDownload, gridBagConstraints);

        lblTeletextDownloadHeader.setText("Choose a teletext service to download.");
        lblTeletextDownloadHeader.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        teletextDownloadPanel.add(lblTeletextDownloadHeader, gridBagConstraints);

        lblTeletextDescription.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        teletextDownloadPanel.add(lblTeletextDescription, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
        teletextContainerPanel.add(teletextDownloadPanel, gridBagConstraints);

        teletextPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Teletext options"));
        teletextPanel.setLayout(new java.awt.GridBagLayout());

        chkTeletext.setText("Enable teletext");
        chkTeletext.addActionListener(this::chkTeletextActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        teletextPanel.add(chkTeletext, gridBagConstraints);

        chkTeletextSubtitles.setText("Subtitles (page 888)");
        chkTeletextSubtitles.addActionListener(this::chkTeletextSubtitlesActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        teletextPanel.add(chkTeletextSubtitles, gridBagConstraints);

        txtTeletextSource.setEditable(false);
        txtTeletextSource.setColumns(42);
        txtTeletextSource.setEnabled(false);
        txtTeletextSource.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        teletextPanel.add(txtTeletextSource, gridBagConstraints);

        txtTeletextSubtitleIndex.setEditable(false);
        txtTeletextSubtitleIndex.setEnabled(false);
        txtTeletextSubtitleIndex.setPreferredSize(new java.awt.Dimension(48, 22));
        txtTeletextSubtitleIndex.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        teletextPanel.add(txtTeletextSubtitleIndex, gridBagConstraints);

        btnTeletextBrowse.setText("Browse...");
        btnTeletextBrowse.setEnabled(false);
        btnTeletextBrowse.addActionListener(this::btnTeletextBrowseActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        teletextPanel.add(btnTeletextBrowse, gridBagConstraints);

        lblTeletextSubtitleIndex.setText("Subtitle index (optional)");
        lblTeletextSubtitleIndex.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        teletextPanel.add(lblTeletextSubtitleIndex, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 0, 0);
        teletextContainerPanel.add(teletextPanel, gridBagConstraints);

        teletextTab.add(teletextContainerPanel);

        tabPane.addTab("Teletext and VBI", teletextTab);

        scramblingTab.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        scramblingContainerPanel.setLayout(new java.awt.GridBagLayout());

        scramblingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Scrambling options"));
        scramblingPanel.setEnabled(false);
        scramblingPanel.setLayout(new java.awt.GridBagLayout());

        lblScrambling1.setText("Scrambling system");
        lblScrambling1.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        scramblingPanel.add(lblScrambling1, gridBagConstraints);

        lblScrambling2.setText("Access type");
        lblScrambling2.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        scramblingPanel.add(lblScrambling2, gridBagConstraints);

        lblScrambling3.setText("VC2 access type");
        lblScrambling3.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        scramblingPanel.add(lblScrambling3, gridBagConstraints);

        cmbScrambling1.setEnabled(false);
        cmbScrambling1.setPrototypeDisplayValue(new ComboBoxOption("", " Nagravision Syster (line shuffle and cut-and-rotate modes) "));
        cmbScrambling1.addMouseWheelListener(this::cmbScrambling1MouseWheelMoved);
        cmbScrambling1.addActionListener(this::cmbScrambling1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        scramblingPanel.add(cmbScrambling1, gridBagConstraints);

        cmbScrambling2.setEnabled(false);
        cmbScrambling2.addMouseWheelListener(this::cmbScrambling2MouseWheelMoved);
        cmbScrambling2.addActionListener(this::cmbScrambling2ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        scramblingPanel.add(cmbScrambling2, gridBagConstraints);
        cmbScrambling2.getAccessibleContext().setAccessibleName("");

        cmbScrambling3.setEnabled(false);
        cmbScrambling3.addMouseWheelListener(this::cmbScrambling3MouseWheelMoved);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        scramblingPanel.add(cmbScrambling3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        scramblingContainerPanel.add(scramblingPanel, gridBagConstraints);

        btnScramblingOptions.setText("Advanced options...");
        btnScramblingOptions.setEnabled(false);
        btnScramblingOptions.addActionListener(this::btnScramblingOptionsActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
        scramblingContainerPanel.add(btnScramblingOptions, gridBagConstraints);

        scramblingTab.add(scramblingContainerPanel);

        tabPane.addTab("Scrambling", scramblingTab);

        settingsTab.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        settingsContainerPanel.setLayout(new java.awt.GridBagLayout());

        hacktvPathPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Path to hacktv"));
        hacktvPathPanel.setLayout(new java.awt.GridBagLayout());

        txtHackTVPath.setEditable(false);
        txtHackTVPath.setColumns(40);
        txtHackTVPath.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        hacktvPathPanel.add(txtHackTVPath, gridBagConstraints);

        btnHackTVPath.setText("Browse...");
        btnHackTVPath.addActionListener(this::btnHackTVPathActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        hacktvPathPanel.add(btnHackTVPath, gridBagConstraints);

        btnDownloadHackTV.setText("Download...");
        btnDownloadHackTV.addActionListener(this::btnDownloadHackTVActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        hacktvPathPanel.add(btnDownloadHackTV, gridBagConstraints);

        lblHackTVLocation.setText("Specify the location of hacktv here.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        hacktvPathPanel.add(lblHackTVLocation, gridBagConstraints);

        buildLabelPanel.setLayout(new java.awt.GridBagLayout());

        lblDetectedBuild.setText("Detected build:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        buildLabelPanel.add(lblDetectedBuild, gridBagConstraints);

        lblFork.setText("fork");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        buildLabelPanel.add(lblFork, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        hacktvPathPanel.add(buildLabelPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        settingsContainerPanel.add(hacktvPathPanel, gridBagConstraints);

        generalSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General settings"));
        generalSettingsPanel.setLayout(new java.awt.GridBagLayout());

        chkSyntaxOnly.setText("Generate syntax only");
        chkSyntaxOnly.addActionListener(this::chkSyntaxOnlyActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        generalSettingsPanel.add(chkSyntaxOnly, gridBagConstraints);

        chkLocalModes.setText("Always use local copy of modes files (do not download)");
        chkLocalModes.addActionListener(this::chkLocalModesActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        generalSettingsPanel.add(chkLocalModes, gridBagConstraints);

        chkUpdateCheck.setText("Check for updates on startup");
        chkUpdateCheck.addActionListener(this::chkUpdateCheckActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
        generalSettingsPanel.add(chkUpdateCheck, gridBagConstraints);

        btnSatSettings.setText("Satellite receiver settings...");
        btnSatSettings.addActionListener(this::btnSatSettingsActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        generalSettingsPanel.add(btnSatSettings, gridBagConstraints);

        comboBoxPanel.setLayout(new java.awt.GridBagLayout());

        lblNMSCeefaxRegion.setText("NMS Ceefax region");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 5);
        comboBoxPanel.add(lblNMSCeefaxRegion, gridBagConstraints);

        lblLookAndFeel.setText("Theme");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        comboBoxPanel.add(lblLookAndFeel, gridBagConstraints);

        cmbNMSCeefaxRegion.addMouseWheelListener(this::cmbNMSCeefaxRegionMouseWheelMoved);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        comboBoxPanel.add(cmbNMSCeefaxRegion, gridBagConstraints);

        cmbLookAndFeel.setPrototypeDisplayValue(new ComboBoxOption("", "XXXXXXXXXXXXXXXXXXXX"));
        cmbLookAndFeel.addMouseWheelListener(this::cmbLookAndFeelMouseWheelMoved);
        cmbLookAndFeel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cmbLookAndFeelMouseEntered(evt);
            }
        });
        cmbLookAndFeel.addActionListener(this::cmbLookAndFeelActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        comboBoxPanel.add(cmbLookAndFeel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        generalSettingsPanel.add(comboBoxPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        settingsContainerPanel.add(generalSettingsPanel, gridBagConstraints);

        resetSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Reset settings"));
        resetSettingsPanel.setLayout(new java.awt.GridBagLayout());

        btnResetAllSettings.setText("Reset all settings...");
        btnResetAllSettings.addActionListener(this::btnResetAllSettingsActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 5);
        resetSettingsPanel.add(btnResetAllSettings, gridBagConstraints);

        btnClearMRUList.setText("Clear MRU list");
        btnClearMRUList.setToolTipText("Clears the list of recently opened files");
        btnClearMRUList.addActionListener(this::btnClearMRUListActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 5);
        resetSettingsPanel.add(btnClearMRUList, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        settingsContainerPanel.add(resetSettingsPanel, gridBagConstraints);

        settingsTab.add(settingsContainerPanel);

        tabPane.addTab("GUI settings", settingsTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        getContentPane().add(tabPane, gridBagConstraints);

        btnRun.setText("Run hacktv");
        btnRun.addActionListener(this::btnRunActionPerformed);
        buttonPanel.add(btnRun);

        btnHideConsole.setText("Hide console");
        btnHideConsole.addActionListener(this::btnHideConsoleActionPerformed);
        buttonPanel.add(btnHideConsole);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        getContentPane().add(buttonPanel, gridBagConstraints);

        consolePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Console output"));
        consolePanel.setPreferredSize(new java.awt.Dimension(580, 160));
        consolePanel.setLayout(new java.awt.GridLayout(1, 0));

        consoleScrollPane.setBorder(null);

        txtConsoleOutput.setEditable(false);
        txtConsoleOutput.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveBackground"));
        txtConsoleOutput.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        txtConsoleOutput.setLineWrap(true);
        txtConsoleOutput.setRows(7);
        txtConsoleOutput.setWrapStyleWord(true);
        txtConsoleOutput.addMouseListener(new ContextMenuListener());
        consoleScrollPane.setViewportView(txtConsoleOutput);

        consolePanel.add(consoleScrollPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        getContentPane().add(consolePanel, gridBagConstraints);

        txtStatus.setEditable(false);
        txtStatus.setColumns(50);
        txtStatus.addMouseListener(new ContextMenuListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(txtStatus, gridBagConstraints);

        fileMenu.setText("File");

        menuNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menuNew.setText("New");
        menuNew.addActionListener(this::menuNewActionPerformed);
        fileMenu.add(menuNew);

        menuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menuOpen.setText("Open...");
        menuOpen.addActionListener(this::menuOpenActionPerformed);
        fileMenu.add(menuOpen);

        menuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menuSave.setText("Save...");
        menuSave.addActionListener(this::menuSaveActionPerformed);
        fileMenu.add(menuSave);

        menuSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        menuSaveAs.setText("Save as...");
        menuSaveAs.addActionListener(this::menuSaveAsActionPerformed);
        fileMenu.add(menuSaveAs);
        fileMenu.add(sepMruSeparator);

        menuMRUFile1.setText("MenuMRUFile1");
        menuMRUFile1.addActionListener(this::menuMRUFile1ActionPerformed);
        fileMenu.add(menuMRUFile1);

        menuMRUFile2.setText("MenuMRUFile2");
        menuMRUFile2.addActionListener(this::menuMRUFile2ActionPerformed);
        fileMenu.add(menuMRUFile2);

        menuMRUFile3.setText("MenuMRUFile3");
        menuMRUFile3.addActionListener(this::menuMRUFile3ActionPerformed);
        fileMenu.add(menuMRUFile3);

        menuMRUFile4.setText("MenuMRUFile4");
        menuMRUFile4.addActionListener(this::menuMRUFile4ActionPerformed);
        fileMenu.add(menuMRUFile4);
        fileMenu.add(sepExitSeparator);

        menuExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        menuExit.setText("Exit");
        menuExit.addActionListener(this::menuExitActionPerformed);
        fileMenu.add(menuExit);

        menuBar.add(fileMenu);

        templatesMenu.setText("Templates");

        menuAstraTemplate.setText("Astra analogue STB...");
        menuAstraTemplate.addActionListener(this::menuAstraTemplateActionPerformed);
        templatesMenu.add(menuAstraTemplate);

        menuBSBTemplate.setText("BSB D-MAC STB...");
        menuBSBTemplate.addActionListener(this::menuBSBTemplateActionPerformed);
        templatesMenu.add(menuBSBTemplate);

        menuBar.add(templatesMenu);

        helpMenu.setText("Help");

        menuWiki.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        menuWiki.setText("Wiki page");
        menuWiki.addActionListener(this::menuWikiActionPerformed);
        helpMenu.add(menuWiki);

        menuGithubRepo.setText("GitHub repository");
        menuGithubRepo.addActionListener(this::menuGithubRepoActionPerformed);
        helpMenu.add(menuGithubRepo);

        menuUpdateCheck.setText("Check for updates");
        menuUpdateCheck.addActionListener(this::menuUpdateCheckActionPerformed);
        helpMenu.add(menuUpdateCheck);
        helpMenu.add(sepAboutSeparator);

        menuAbout.setText("About");
        menuAbout.addActionListener(this::menuAboutActionPerformed);
        helpMenu.add(menuAbout);

        menuBar.add(helpMenu);

        updateMenu.setText("Update available");

        menuDownloadUpdate.setText("Download update");
        menuDownloadUpdate.addActionListener(this::menuDownloadUpdateActionPerformed);
        updateMenu.add(menuDownloadUpdate);

        menuBar.add(updateMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents
        
    public void initUI() {
        // Set application icons
        setIcons();
        // Get available look and feel options
        var laf = getLaf();
        // Initialise Swing components
        initComponents();
        SwingUtilities.invokeLater(this::pack);
        // Populate look and feel combobox using the laf variable
        populateLafComboBox(laf);
    }
    
    public int postInitUI(String[] args) {
        // Set the jarDir variable so we know where we're located
        jarDir = Path.of(Shared.getCurrentDirectory());
        // Set OS-specific options
        if (isWindows) {
            String arch = System.getProperty("os.arch");
            btnDownloadHackTV.setVisible(arch.equals("amd64") || arch.equals("aarch64"));
            // Initialise JNI library
            ConsoleCtrlJNI.initialise(jarDir);
        }
        defaultHackTVPath = 
                isWindows
                ? Paths.get(jarDir.toString(), "hacktv.exe").toString()
                : "/usr/local/bin/hacktv";
        // Post-initialisation macOS tasks
        if (isMacOS) {
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
        if (!openModesFile()) return 2;
        if (!openBandPlanFile()) return 3;
        if (!addVideoModes()) return 4;
        addARCorrectionOptions();
        populateWSS();
        addFl2kAudioOptions();
        addOutputDevices();
        addCeefaxRegions();
        addTeletextOptions();
        if (captainJack) {
            captainJack();
        } else {
            fsphil();
        }
        // Set default values when form loads
        radLocalSource.doClick();
        var card = (CardLayout) sourceCardPanel.getLayout();
        card.show(sourceCardPanel, "textbox");
        txtGain.setText("0");
        updateMenu.setVisible(false);
        // End default value load
        checkMRUList();
        if (PREFS.getInt("noupdatecheck", 0) == 0) {
            chkUpdateCheck.setSelected(true);
            checkForUpdates(true);
        }
        // If any command line parameters were specified, handle them
        if (args.length > 0) {
            // If the specified file has a .htv extension, open it
            if (args[0].toLowerCase(Locale.ENGLISH).endsWith(".htv")) {
                selectedFile = new File(args[0]);
                checkSelectedFile(selectedFile);
            } else if (args[0].toLowerCase(Locale.ENGLISH).endsWith(".m3u")) {
                txtSource.setText(args[0]);
                m3uHandler(args[0]);
            } else {
                // Otherwise, assume it's a source file and populate the source
                // text box with it.
                txtSource.setText(args[0]);
            }
        }
        pack();
        return 0;
    }
    
    /*private FileFilter createFileFilter() {
        // Creates a custom FileFilter for hacktv binaries
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                // always accept directories
                if (file.isDirectory())
                    return true;
                // but only files with specific name
                if (!isWindows) {
                    return file.getName().equals("hacktv");
                } else {
                    return file.getName().equals("hacktv.exe");
                }
            }
            @Override
            public String getDescription() {
                if (!isWindows) {
                    return "hacktv binaries (hacktv)";
                } else {
                    return "hacktv binaries (hacktv.exe)";
                }
            }
        };
    }*/
    
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
            // chkColour is NOT included here, as it is enabled by default
            chkRepeat,
            chkTimestamp,
            chkInterlace,
            chkPosition,
            chkLogo,
            chkSubtitles,
            chkAspectRatio,
            chkFMDev,
            chkFmFilter,
            chkAmp,
            chkVITS,
            chkACP,
            chkWSS,
            chkGamma,
            chkOutputLevel,
            chkTeletext,
            chkVolume,
            chkDownmix,
            chkTeletextSubtitles,
            chkPixelRate,
            chkRandom,
            chkInvertVideo,
            chkVITC,
            chkSecamId,
            chkOffset,
            chkSwapIQ,
            chkSiS,
            chkSVideo,
            chkCC608,
            chkVsbFilter
        };
    }
    
    private ArrayList<ComboBoxOption> getLaf() {
        // Get the available look and feels
        // This runs before the UI components are defined, so it will return
        // an ArrayList that can be used after the components are initialised.
        int defaultIndex = -1;
        // Define new ArrayLists for the various types
        // Standard JRE look and feels
        var standardLaf = new ArrayList<ComboBoxOption>();
        // FlatLaf themes
        var flCore = new ArrayList<String>();
        var flCorev3 = new ArrayList<String>();
        var flIj = new ArrayList<String>();
        var flIjm = new ArrayList<String>();
        // Temporary integer for default look and feel index
        int i = 0;
        UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lookAndFeel : lookAndFeels) {
            // Get the implementation class for the look and feel
            // Don't add the GTK+ theme on Linux, it renders very poorly and is 
            // the default on many distros.
            if (!lookAndFeel.getClassName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")){
                standardLaf.add(new ComboBoxOption(lookAndFeel.getClassName(), lookAndFeel.getName()));
            }
            // Is this the system default?
            if (UIManager.getSystemLookAndFeelClassName().equals(lookAndFeel.getClassName())) {
                defaultLaf = lookAndFeel.getClassName();
                defaultIndex = i;
            } else {
                i++;
            }
        }
        // Use normal fonts on Metal look and feel, rather than bold
        UIManager.put("swing.boldMetal", false);
        // Add FlatLaf
        // Load embedded flatlaf.ini
        boolean flConf = loadFlatLafINI();
        if (!flConf) return standardLaf;
        flCore.addAll(Arrays.asList(flIni.getKeys("core-themes")));
        // Get version of currently loaded FlatLaf package
        var flPkg = com.formdev.flatlaf.FlatLaf.class.getPackage();
        String flVer = flPkg.getImplementationVersion();
        // Get target FlatLaf version from flatlaf.ini
        String iniVer = flIni.get("flatlaf", "Version", "");
        if (flVer == null) {
            messageBox("Unable to detect FlatLaf version. FlatLaf themes may not work correctly.",
                    JOptionPane.WARNING_MESSAGE);                
        } else if (!flVer.equals(iniVer)) {
            messageBox("The expected FlatLaf version is " + iniVer + ", but version " + flVer +
                    " was detected. FlatLaf themes may not work correctly.",
                    JOptionPane.WARNING_MESSAGE);
        }
        // Version 3?
        boolean v3;
        try {
            Class.forName("com.formdev.flatlaf.themes.FlatMacLightLaf");
            v3 = true;
        } catch (ClassNotFoundException e) {
            v3 = false;
        }
        if (v3) {
            // Add FlatLaf v3 themes
            flCorev3.addAll(Arrays.asList(flIni.getKeys("core-themes-v3")));
        }
        // IntelliJ themes?
        boolean ij;
        try {
            Class.forName("com.formdev.flatlaf.intellijthemes.Utils");
            ij = true;
        } catch (ClassNotFoundException e) {
            ij = false;
        }
        if (ij) {
            // Read the IntellJ themes from flIni
            flIj.addAll(Arrays.asList(flIni.getKeys("intellij-themes")));
            flIjm.addAll(Arrays.asList(flIni.getKeys("materialthemeuilite")));
        }
        standardLaf.addAll(addFlatLafThemes(flCore, "core-themes"));
        standardLaf.addAll(addFlatLafThemes(flCorev3, "core-themes-v3"));
        standardLaf.addAll(addFlatLafThemes(flIj, "intellij-themes"));
        standardLaf.addAll(addFlatLafThemes(flIjm, "materialthemeuilite"));
        if (isMacOS) {
            // Use FlatLafMac as default
            defaultLaf = "com.formdev.flatlaf.themes.FlatMacLightLaf";
            defaultIndex = standardLaf.indexOf(new ComboBoxOption(defaultLaf, ""));
        } else if (!isWindows) {
            // Use FlatLaf as default
            defaultLaf = "com.formdev.flatlaf.FlatLightLaf";
            defaultIndex = standardLaf.indexOf(new ComboBoxOption(defaultLaf, ""));
        }
        // Safeguard if the lookandfeel preference is out of bounds
        int v = PREFS.getInt("lookandfeel", defaultIndex);
        if (v >= standardLaf.size() || v < 0) {
            // Use default look and feel, and reset preference
            System.err.println("Specified look and feel not found, reverting to default.");
            PREFS.putInt("lookandfeel", defaultIndex);
            var fr = (ComboBoxOption) standardLaf.get(defaultIndex);
            setFirstRunLaf(fr.value());
        } else {
            var fr = (ComboBoxOption) standardLaf.get(PREFS.getInt("lookandfeel", defaultIndex));
            setFirstRunLaf(fr.value());
        }
        return standardLaf;
    }
    
    private boolean loadFlatLafINI() {
        // Read the embedded flatlaf.ini file
        String r = "ie/bops/resources/flatlaf.ini";
        try {
            flIni.loadFromResource(r);
        }
        catch (IOException | FileSystemNotFoundException ex) {
            System.err.println(ex);
            return false;
        }
        // Set FlatLaf system properties
        System.setProperty("flatlaf.useWindowDecorations",
                Boolean.toString(flIni.getBoolean("flatlaf", "UseWindowDecorations"))
        );
        System.setProperty("flatlaf.menuBarEmbedded",
                Boolean.toString(flIni.getBoolean("flatlaf", "MenuBarEmbedded"))
        );
        System.setProperty("flatlaf.useRoundedPopupBorder",
                Boolean.toString(flIni.getBoolean("flatlaf", "UseRoundedPopupBorder"))
        );
        return true;
    }
    
    private ArrayList<ComboBoxOption> addFlatLafThemes(ArrayList<String> input, String sectionName) {
        var al = new ArrayList<ComboBoxOption>();
        String className = flIni.get(sectionName, "class");
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).equals("class")) continue;
            String id = className + '\u002e' + input.get(i);
            String fn = "FlatLaf (" + flIni.get(sectionName, input.get(i)) + ")";
            al.add(new ComboBoxOption(id, fn));
        }
        return al;
    }
    
    private void setFirstRunLaf(String lafClassName) {
        // Sets the look and feel when the application is first run
        // This is run before any UI elements are intialised
        try {
            UIManager.setLookAndFeel(lafClassName);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            System.err.println("Error loading look and feel: " + e);
        }
    }
    
    private void populateLafComboBox(ArrayList<ComboBoxOption> laf) {
        // Populate the combobox using the ArrayList we got from getLaf()
        cmbLookAndFeel.setModel(new DefaultComboBoxModel<>(laf.toArray(ComboBoxOption[]::new)));
        cmbLookAndFeel.setSelectedItem(new ComboBoxOption(UIManager.getLookAndFeel().getClass().getName(), ""));
    }
    
    private void changeLaf() {
        // Changes the look and feel on the fly
        var m = (ComboBoxOption) cmbLookAndFeel.getSelectedItem();
        String l = m.value();
        // Only change look and feel if different to the current one
        if (!UIManager.getLookAndFeel().getClass().getName().equals(l)) {
            try {
                UIManager.setLookAndFeel(l);
                SwingUtilities.updateComponentTreeUI(this);
                // Colour of JList resets on L&F change so reset it
                if (isVisible() && (playlistModel.isEmpty())) {
                    // Set the background colour of the JList to disabledBackground
                    lstPlaylist.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.disabledBackground"));
                } else if (isVisible()) {
                    // Set the background colour of the JList to background (enabled)
                    lstPlaylist.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
                }
                // Reset console pane colour and border
                txtConsoleOutput.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.disabledBackground"));
                consoleScrollPane.setBorder(null);
                pack();
                PREFS.putInt("lookandfeel", cmbLookAndFeel.getSelectedIndex());
            } catch (ClassNotFoundException c) {
                String err = 
                        """
                        The requested look and feel cannot be found.
                        The current version of FlatLaf may not support it.""";
                if (isVisible()) {
                    messageBox(err, JOptionPane.ERROR_MESSAGE);
                } else {
                    System.err.println();
                }
                // Reload default look and feel
                var p = new ComboBoxOption(defaultLaf, "");
                cmbLookAndFeel.setSelectedItem(p);
            } catch (IllegalAccessException | InstantiationException | 
                    UnsupportedLookAndFeelException ex) {
                System.err.println(ex);
            }            
        }
    }
    
    private void checkForUpdates(boolean silent) {
        // Queries the URL below for the latest release
        String apiUrl = "https://api.github.com/repos/steeviebops/hacktv-gui/releases/latest";
        String query = "tag_name";
        var updateWorker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws IOException, URISyntaxException {
                try {
                    // Get the current version's date code using getVersion and
                    // remove the dashes so we can use parseInt later.
                    String currentVersion = getVersion().replaceAll("-", "");
                    if (currentVersion.equals("n/a")) return -1;
                    String a = Shared.downloadToString(apiUrl);
                    var r = Shared.queryJson(a, query);
                    if (r != null && !r.isEmpty()) {
                        String newVersion = r.getFirst();
                        int nvi = Integer.parseInt(newVersion);
                        int cvi = Integer.parseInt(currentVersion);
                        if (nvi > cvi) return 1;
                        return 0;
                    } else {
                        return 2;
                    }
                } catch (IOException ioe) {
                    // Probably a connection error
                    return 2;
                } catch (NumberFormatException nfe) {
                    // Unexpected data received, report
                    System.err.println(nfe);
                }
                return 999;
            }
            @Override
            protected void done() {
                int status;
                try {
                    status = get();
                } catch (InterruptedException | ExecutionException e) {
                    status = 998;
                }
                switch (status) {
                    case -1 -> {
                        // No current version number found.
                        // This can happen if running directly from an IDE.
                        // Don't do anything in this case as we have no version
                        // number to check against.
                    }
                    case 0 -> {
                        // No update available
                        if (silent) return;
                        messageBox("No updates are available at this time.", JOptionPane.INFORMATION_MESSAGE);
                    }
                    case 1 -> {
                        // Update available
                        if (silent) {
                            updateMenu.setVisible(true);
                            return;
                        }
                        if (JOptionPane.showConfirmDialog(
                                null, 
                                """
                                An update is available.
                                Would you like to find out more?""",
                                Shared.APP_NAME,
                                JOptionPane.YES_NO_OPTION
                        ) == JOptionPane.YES_OPTION) 
                            menuDownloadUpdate.doClick();
                    }
                    case 2 -> {
                        // Connection error
                        if (silent) return;
                        messageBox("""
                                   An error occurred while attempting to contact the update server.
                                   Please check your internet connection and try again.""",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                    default -> {
                        // Unknown error
                        System.err.println("Error code: " + status);
                        if (silent) return;
                        messageBox("""
                                   An unknown error occurred while attempting to contact the update server.
                                   Please try again later.""", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        updateWorker.execute();
    }
    
    private String getVersion() {
        // Get application version by checking the timestamp on the class file
        String cp = System.getProperty("java.class.path");
        // If classpath contains multiple paths, remove all but the first
        if (!isWindows) {
            if (cp.contains(":")) {
                cp = cp.substring(0, cp.indexOf(":"));
            }
        }
        else if (cp.contains(";")) {
            cp = cp.substring(0, cp.indexOf(";"));
        }
        try {
            var sdf = new SimpleDateFormat("yyyy-MM-dd");
            String classFilePath = "/ie/bops/hacktvgui/MainWindow.class";
            Date date;
            if (Files.exists(Path.of(cp))) {
                date = Shared.getLastUpdatedTime(cp, classFilePath);
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
        JOptionPane.showMessageDialog(null, msg, Shared.APP_NAME, type);
    }

    private void createTempDirectory() {
        // Creates a temp directory for us to use.
        // This is deleted on exit so don't save anything useful here!
        if (tempDir == null) {
            try {
                tempDir = Files.createTempDirectory(Shared.APP_NAME);
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
                q = JOptionPane.showConfirmDialog(
                        null,
                        """
                        A modes file was found in the current directory.
                        Do you want to use this file?
                        You can suppress this prompt on the GUI settings tab.""",
                        Shared.APP_NAME,
                        JOptionPane.YES_NO_OPTION
                );
            }
        }
        // If no, and "UseLocalModesFile" is 0, download
        else if (PREFS.getInt("uselocalmodesfile", 0) == 0) {
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
            // Download from the server specified in DOWNLOAD_SERVER
            String v = Shared.DOWNLOAD_SERVER + "hacktv-gui/" + getFork() + ".ini";
            String b = Shared.DOWNLOAD_SERVER + "hacktv-gui/bandplans.ini";
            modesFile = downloadModesFile(v);
            bpFile = downloadModesFile(b);
        }
        // Reopen modes file after config change
        if (isVisible()) {
            openModesFile();
            openBandPlanFile();
            lblRegion.setEnabled(false);
            cmbRegion.setEnabled(false);
            if (!addVideoModes()) System.exit(4);
        }
    }
    
    private String downloadModesFile(String url) {
        // Downloads files directly to a string
        String v = getFork() + ".ini";
        String b = "bandplans.ini";
        String targetFile;
        try {
            targetFile = Shared.downloadToString(url);
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
            messageBox("Unable to download the " + f + " file.\n" +
                    "Using embedded copy instead, which may not be up to date.\n" +
                    ex, JOptionPane.ERROR_MESSAGE);
            return "";
        }
        return targetFile;
    } 
    
    private boolean openModesFile() {
        if (modesFilePath.isEmpty() && modesFile != null) {
            // Read the downloaded modes file to the INI handler
            try {
                modesIni.load(new StringReader(modesFile));
                modesFileLocation = "online";
            } catch (IOException ioe) {
                // Load failed, retry with the embedded file
                messageBox("""
                           Unable to read the downloaded modes file.
                           Retrying with the embedded copy, which may not be up to date.""",
                        JOptionPane.WARNING_MESSAGE
                );
                modesFilePath = "ie/bops/resources/" + getFork() + ".ini";
                modesFileLocation = "embedded";
                openModesFile();
            }
        }
        else if (modesFilePath.startsWith("ie/bops/resources/")) {
            // Read the embedded videomodes.ini to the INI handler
            try {
                modesIni.loadFromResource(modesFilePath);
                modesFileLocation = "embedded";
            } catch (IOException | FileSystemNotFoundException ex) {
                // No modes file to load, we cannot continue
                messageBox("""
                           Critical error, unable to read the embedded modes file.
                           The application will now exit.""",
                        JOptionPane.ERROR_MESSAGE
                );
                System.err.println(ex);
                return false;
            }
        }
        else {
            // Read the videomodes.ini we specified previously
            try {
                modesIni.loadFromDisk(Path.of(modesFilePath));
                modesFileLocation = "external";
            }
            catch (IOException e) {
                // Load failed, retry with the embedded file
                messageBox("""
                           Unable to read the modes file.
                           Retrying with the embedded copy, which may not be up to date.""",
                        JOptionPane.WARNING_MESSAGE
                );
                modesFilePath = "ie/bops/resources/" + getFork() + getFork() + ".ini";
                modesFileLocation = "embedded";
                openModesFile();
            }
        }
        // Read modes file version
        modesFileVersion = modesIni.get("Modes.ini", "FileVersion", "unknown");
        return true;
    }
    
    private boolean openBandPlanFile() {
        String m = modesFileVersion.replace("c","");
        if (Shared.isNumeric(m) && Double.parseDouble(m) < 5.00) {
            // This is a v4 or older Modes.ini file
            // The main difference between v5 and the earlier formats is that
            // v5 split out the band plans into a separate file.
            // So the easiest way to read an older version is to simply
            // duplicate modesFile to bpFile.
            System.out.println("Version 4.x or earlier modes file detected.");
            bpFile = modesFile;
            bpFileLocation = "legacy";
            bpFileVersion = modesFileVersion;
        } else if (bpFilePath.isEmpty() && bpFile != null) {
            try {
                bpIni.load(new StringReader(bpFile));
                bpFileLocation = "online";
            } catch (IOException ioe) {
                // Load failed, retry with the embedded file
                messageBox("""
                           Unable to read the downloaded band plans file.
                           Retrying with the embedded copy, which may not be up to date.""",
                        JOptionPane.WARNING_MESSAGE);
                bpFilePath = "ie/bops/resources/bandplans.ini";
                bpFileLocation = "embedded";
                openBandPlanFile();
            }
        } else if (bpFilePath.startsWith("ie/bops/resources/")) {
            // Read the embedded bandplans.ini to the bpFile string
            try {
                bpIni.loadFromResource(bpFilePath);
                bpFileLocation = "embedded";
            } catch (IOException | FileSystemNotFoundException ex) {
                // No modes file to load, we cannot continue
                messageBox("""
                           Critical error, unable to read the embedded band plans file.
                           The application will now exit.""",
                        JOptionPane.ERROR_MESSAGE
                );
                System.err.println(ex);
                return false;
            }
        } else {
            // Read the bandplans.ini we specified previously
            try {
                bpIni.loadFromDisk(Path.of(bpFilePath));
                bpFileLocation = "external";
            } catch (IOException e) {
                // Load failed, retry with the embedded file
                messageBox("""
                           Unable to read the band plans file.
                           Retrying with the embedded copy, which may not be up to date.""",
                        JOptionPane.WARNING_MESSAGE
                );
                bpFilePath = "ie/bops/resources/bandplans.ini";
                bpFileLocation = "embedded";
                openBandPlanFile();
            }
        }
        // Read bandplans.ini file version if not in legacy mode
        if (!bpFileLocation.equals("legacy")) {
            bpFileVersion = bpIni.get("bandplans.ini", "Version", "unknown");
        }
        if (Shared.isNumeric(m) && Double.parseDouble(m) < 6.00) {
            messageBox("The " + getFork() + ".ini file was written for an older version of " + Shared.APP_NAME + ". " +
                    "\nThe file will be loaded, but may not be fully compatible." +
                    "\nModes with missing metadata will be grouped into the Other category." +
                    "\nVersion 6.0 or later is recommended, but version " + m + " was detected.",
                    JOptionPane.WARNING_MESSAGE);
        }
        return true;
    }
    
    private boolean addVideoModes() {
        // Retrieve the list of all sections in the modes file
        String[] sections = modesIni.getSections();
        // Below are a list of sections that we want to skip, as we know that 
        // they do not contain valid mode data.
        Set<String> toExclude = Set.of(
            "Modes.ini",
            "videomodes",
            "videocrypt",
            "videocrypt2",
            "videocrypts",
            "syster",
            "eurocrypt",
            "eurocypher",
            "testcards",
            "testcards525",
            "testsignals_625_pal",
            "testsignals_525_ntsc",
            "testsignals_625_secam",
            "logos"
        );
        // Clear any pre-existing modes
        modes.clear();
        // Iterate through the sections to determine if they are valid.
        // We do this by checking against the list above, as well as the
        // existence of the lines setting.
        // If these checks pass, populate the data for that mode.
        for (var s : sections) {
            if (toExclude.contains(s)) continue;
            if (modesIni.get(s, "lines") == null) continue;
            modes.add(getModeData(s));
        }
        if (modes.isEmpty()) {
            // No modes found, we can't continue
            messageBox("No video modes were found. The " + getFork() + ".ini file may be invalid or corrupted.\n"
                    + "The application will now exit.", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        var cm = new ArrayList<ColourOption>();
        // Search the modes array for each system. If found, add it
        if (modes.stream().anyMatch(m -> m.colourMode() == ColourMode.PAL)) {
            cm.add(new ColourOption(ColourMode.PAL, "PAL"));
        }        
        if (modes.stream().anyMatch(m -> m.colourMode() == ColourMode.NTSC)) {
            cm.add(new ColourOption(ColourMode.NTSC, "NTSC"));
        }        
        if (modes.stream().anyMatch(m -> m.colourMode() == ColourMode.SECAM)) {
            cm.add(new ColourOption(ColourMode.SECAM, "SECAM"));
        }        
        if (modes.stream().anyMatch(m -> m.colourMode() == ColourMode.NONE)) {
            cm.add(new ColourOption(ColourMode.NONE, "Black and white"));
        }        
        if (modes.stream().anyMatch(m -> m.colourMode() == ColourMode.MAC)) {
            cm.add(new ColourOption(ColourMode.MAC, "MAC"));
        }        
        if (modes.stream().anyMatch(m -> m.colourMode() == ColourMode.OTHER)) {
            cm.add(new ColourOption(ColourMode.OTHER, "Other"));
        }
        // Create and set a ListModel for the JList
        var listModel = new DefaultListModel<ColourOption>();
        lstColour.setModel(listModel);
        // Add the above to the ListModel for the JList
        listModel.addAll(cm);
        lstColour.setSelectedIndex(0);
        return true;
    }

    private ModeInfo getModeData(String mode) {
        if (modesIni.getKeys(mode).length == 0) {
            throw new IllegalStateException("Specified mode could not be found.");
        }
        VideoModulation mt;
        switch (modesIni.get(mode, "modulation", "")) {
            case "vsb" -> mt = VSB;
            case "fm" -> mt = FM;
            case "baseband" -> mt = UNMODULATED;
            default -> {
                System.err.println("Unexpected modulation type");
                mt = null;
            }
        }
        ColourMode cm;
        switch (modesIni.get(mode, "colourmode", "")) {
            case "none" -> cm = NONE;
            case "pal" -> cm = PAL;
            case "ntsc" -> cm = NTSC;
            case "secam" -> cm = SECAM;
            case "mac" -> cm = MAC;
            case "", "other" -> cm = OTHER;
            default -> {
                System.err.println("Unexpected colour mode");
                cm = null;
            }
        }
        AudioModulation audioMod = null;
        var amt = modesIni.get(mode, "audiomodulation");
        if (amt != null ) {
            switch (amt) {
                case "am" -> audioMod = AM_AUDIO;
                case "fm" -> audioMod = FM_AUDIO;
                case "digital" -> audioMod = DIGITAL_AUDIO;
                case "none" -> audioMod = NO_AUDIO;
                default -> {
                }
            }            
        }
        AudioMode defaultStereoMode = null;
        var am = modesIni.get(mode, "defaultstereo");
        if (am != null) {
            switch (am) {
                case "none":
                case "mono":
                default:
                    defaultStereoMode = MONO;
                    break;
                case "nicam":
                    defaultStereoMode = NICAM;
                    break;
                case "a2":
                    defaultStereoMode = A2;
                    break;
            }            
        }
        String displayName = modesIni.get(mode, "name", mode);
        boolean ns = modesIni.getBoolean(mode, "nonstandard");
        if (ns && displayName != null) {
            // Display an asterisk beside the name to indicate a non-standard mode
            displayName = displayName.concat(" *");
        }
        return new ModeInfo(
                mode,
                modesIni.get(mode, "alt"),
                displayName,
                modesIni.getInt(mode, "lines"),
                modesIni.getDouble(mode, "fieldrate"),
                mt,
                audioMod,
                modesIni.getLong(mode, "audiosubcarrier"),
                modesIni.getLong(mode, "sr"),
                cm,
                modesIni.getBoolean(mode, "audio"),
                modesIni.getBoolean(mode, "nicam"),
                modesIni.getBoolean(mode, "a2stereo"),
                defaultStereoMode,
                modesIni.getBoolean(mode, "teletext"),
                modesIni.getBoolean(mode, "wss"),
                modesIni.getBoolean(mode, "vits"),
                modesIni.getBoolean(mode, "acp"),
                modesIni.getBoolean(mode, "scrambling"),
                ns,
                getBandPlans(mode, "uhf"),
                getBandPlans(mode, "vhf"),
                getBandPlans(mode, "sat"),
                modesIni.get(mode, "description")
        );
    }

    private Map<String, BandPlan> getBandPlans(String mode, String band) {
        if (band == null) return null; // This should never be null or we have a bug!
        var bandPlanMap = new LinkedHashMap<String, BandPlan>();
        var channels = new ArrayList<Channel>();
        for (int i = 0; i <= 4; i++) {
            // Get the uhf/vhf keys from the mode's INI data
            String key = (i == 0) ? band : band + (i + 1);
            String id = modesIni.get(mode, key);
            if (id == null) continue;
            // Query bandplans.ini for its values related to the ID we just found
            var bp = bpIni.getKeys(id);
            if (bp == null) {
                System.err.println("not found");
                continue;
            }
            String region = bpIni.get(id, "region", "");
            // Loop through the values found in bandplans.ini
            for (String channelNumber : bp) {
                Long value = bpIni.getLong(id, channelNumber);
                if (value == null) continue;
                // Skip region ID, chid and local oscillator keys if they exist.
                // These should not be processed here.
                if (channelNumber.equals("region")) continue;
                if (channelNumber.equals("chid")) continue;
                if (channelNumber.equals("lo")) continue;
                // Add all other key/value pairs
                var chidSection = bpIni.get(id, "chid");
                String chid = null;
                if (chidSection != null) {
                    chid = bpIni.get(chidSection, channelNumber);
                }
                var channel = new Channel(channelNumber, value, chid);
                channels.add(channel);
            }
            var bandName = switch (band) {
                case "uhf" -> UHF_BAND;
                case "vhf" -> VHF_BAND;
                case "sat" -> SATELLITE_BAND;
                default -> {
                    System.err.println("Unexpected band: " + band);
                    yield null;
                }
            };
            bandPlanMap.put(id, new BandPlan(id, bandName, region, channels));
        }
        return bandPlanMap;
    }
    
    private void migratePreferences() {
        try {
            if (isWindows ? PREFS.keys().length > 1 : PREFS.keys().length > 0) return;
            if (Preferences.userRoot().nodeExists("com/steeviebops/hacktvgui")) {
                var oldPrefs = Preferences.userRoot().node("com/steeviebops/hacktvgui");
                if (isWindows ? oldPrefs.keys().length > 1 : oldPrefs.keys().length > 0) {
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
        if (isWindows) {
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
    
    private void detectFork() {
        // Sane defaults
        captainJack = false;
        supportsPhilipsTestSignal = false;        
        // Check if the specified path does not exist or is a directory
        if (!Files.exists(Path.of(hackTVPath))) {
            lblFork.setText("Not found");
            return;
        }
        else if (Files.isDirectory(Path.of(hackTVPath))) {
            lblFork.setText("Invalid path");
            return;    
        }
        try {
            // Get the output of hacktv --help
            var pb = new ProcessBuilder(hackTVPath, "--help");
            pb.redirectErrorStream(true);
            var sb = new StringBuilder();
            Process p = pb.start();
            try (var br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                // Add each line from the output to the StringBuilder
                String s;
                while ((s = br.readLine()) != null) sb.append(s).append("\n");
            }
            String invalidFile = "Invalid file (not hacktv?)";
            if (!sb.toString().isBlank()) {
                String output = sb.toString();
                captainJack = output.contains("--enableemm");
                supportsPhilipsTestSignal = output.contains("--testsignal");
                if (captainJack) {
                    lblFork.setText("Captain Jack");
                } else if (supportsPhilipsTestSignal) {
                    lblFork.setText("Matt's TV Barn");
                } else if (output.contains("Usage: hacktv [options] input [input...]")) {
                    lblFork.setText("fsphil");
                } else {
                    lblFork.setText(invalidFile);
                    return;
                }
            } else {
                lblFork.setText(invalidFile);
                return;
            }
            // Get the hacktv version if supported, by running hacktv --version
            String v = getHackTVVersion();
            if (v != null && !v.isBlank()) {
                lblFork.setText(lblFork.getText() + " (" + v.substring(v.indexOf(" ") + 1) + ")");
            }
        }
        catch (IOException  ex) {
            lblFork.setText("File access error");
            System.err.println(ex);
        }
    }
    
    private String getHackTVVersion() {
        // Get the hacktv version if supported, by running hacktv --version
        try {
            var pb = new ProcessBuilder(hackTVPath, "--version");
            String v;
            Process p2 = pb.start();
            try (var br = new BufferedReader(new InputStreamReader(p2.getInputStream(), StandardCharsets.UTF_8))) {
                // We only need the first line
                v = br.readLine();
            }
            if ((v != null) && (!v.isBlank())) {
                v = v.substring(v.indexOf(" ") + 1);
                return v;
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
        return null;
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
        // Enable test signal settings button if supported
        btnTestSettings.setVisible(supportsPhilipsTestSignal);
        // Disable features unsupported in fsphil's build
        captainJackPanel.setEnabled(false);
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
        var c = (ColourOption) lstColour.getSelectedValue();
        if ( c.colourMode() == ColourMode.PAL || c.colourMode() == ColourMode.SECAM) {
            add625ScramblingTypes();
        } else if ( c.colourMode() == ColourMode.MAC ) {
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
        captainJackPanel.setEnabled(true);
        chkLogo.setEnabled(true);
        addLogoOptions();
        // Recalculate the window size, required for macOS
        if (isMacOS) pack();
        if ( !radTest.isSelected() ) {
            chkPosition.setEnabled(true);
            chkTimestamp.setEnabled(true);
            chkPosition.setEnabled(true);
            chkSubtitles.setEnabled(true);
            chkDownmix.setEnabled(true);
        }
        var c = (ColourOption) lstColour.getSelectedValue();
        if ( c.colourMode() == ColourMode.PAL || c.colourMode() == ColourMode.SECAM) {
            add625ScramblingTypes();
        } else if ( c.colourMode() == ColourMode.MAC ) {
            addMACScramblingTypes();
        }
        if (radTest.isSelected()){
            cmbTest.setEnabled(true);
            cmbTest.setSelectedIndex(0);
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
        var configFileChooser = new SystemFileChooser();
        configFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        configFileChooser.addChoosableFileFilter(
                new SystemFileChooser.FileNameExtensionFilter("hacktv configuration file (*.htv)", "htv")
        );
        // Opens the save file dialogue
        int result = configFileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // Check if the saved file has a .htv extension or not.
            // If it does not, then append one.
            selectedFile = new File (Shared.stripQuotes(configFileChooser.getSelectedFile().toString()));
            if (!selectedFile.toString().toLowerCase(Locale.ENGLISH).endsWith(".htv")) {
                selectedFile = new File(selectedFile + ".htv");
            }
            // Create file
            //try {
            //    if (!selectedFile.createNewFile()) {
                    /* File exists, prompt to overwrite.
                     * If yes, go to the save method. If no, then restart this
                     * method so the user can select another file. Java doesn't
                     * appear to support file overwrite prompts in its dialogues
                     * so this is a workaround/hack.
                    */
            //        if (JOptionPane.showConfirmDialog(null, selectedFile.getName() + " already exists.\n"
            //                + "Do you want to overwrite it?", Shared.APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
            //                == JOptionPane.YES_OPTION) {
            //            saveConfigFile(selectedFile);
            //        }
            //        else {
            //            saveFilePrompt();
            //        }
            //    }
            //    else {
                    saveConfigFile(selectedFile);
            //    }
            //} catch (IOException ex) {
            //        messageBox("An error occurred while writing to this file. "
            //                + "You may not have the correct permissions to write to this location.", JOptionPane.ERROR_MESSAGE);       
            //}
        }    
    }
    
    private String extractPlaylist(String input) {
        // Extracts the playlist from a HTV config file
        String target = "[playlist]";
        int length = target.length() + 1;
        if (input == null || !input.contains(target)) return null;
        int s = input.indexOf(target);
        int e = input.lastIndexOf("\n[") + 1;
        // Extract the data from below the [playlist] header
        String t = input.substring(s + length);
        // Section is at the end of the file if 's' and 'e' match
        if (s == e) return t.trim();
        // Find where the next section starts and stop there
        return t.substring(0, t.indexOf("\n[")).trim();
    }
    
    private void checkSelectedFile(File SourceFile) {
        // Create a separate instance for the config file
        var htvFile = new INIFile();
        try {
            // Check if the file is too large. We really don't need to read
            // anything larger than a few kilobytes but we'll set it to 1 MB.
            String iniFile;
            String playlist = null;
            if (SourceFile.length() < 1048576)  {
                /**
                  * Read the file into memory.
                  * 
                  * As this isn't necessarily a standard INI file (it could
                  * have a playlist appended to the end), we won't use the INI 
                  * loadFromDisk() function. Instead, we'll load the file to a
                  * string, extract the playlist from it, and then load that 
                  * string to the INI handler directly.
                  */
                iniFile = Files.readString(SourceFile.toPath(), StandardCharsets.UTF_8);
                // Remove a UTF-8 BOM if it exists
                iniFile = iniFile.replaceAll("\\A\uFEFF", "");
                // Remove any Windows-style line breaks
                iniFile = iniFile.replaceAll("\r\n", "\n");
                htvFile.load(new StringReader(iniFile));
            } else {
                messageBox("Invalid configuration file.", JOptionPane.WARNING_MESSAGE);
                System.err.println("File too large (> 1MB)");
                return;
            }
            // Check the file to see if it's in the correct format.
            if (iniFile.contains("[hacktv]\n")) {
                // This is OK, continue opening this file
                htvLoadInProgress = true;
                if (htvFile.getBoolean("hacktv-gui3", "playlist")) {
                    playlist = extractPlaylist(iniFile);
                }
                if (openConfigFile(htvFile, playlist)) {
                    // Display the opened filename in the title bar
                    // Back up the original title once
                    if (!titleBarChanged) {
                        titleBar = getTitle();
                        titleBarChanged = true;
                    }
                    setTitle(titleBar + " - " + SourceFile.getName());
                    // Remove the ellipsis after Save to follow standard UI guidelines
                    menuSave.setText("Save");
                    updateMRUList(SourceFile.toString());    
                }
                htvLoadInProgress = false;
            } else {
                // No idea what we've read here, abort
                messageBox("Invalid configuration file.", JOptionPane.WARNING_MESSAGE);
                System.err.println("[hacktv] section not found");
            }
        } catch (MalformedInputException ex) {
                messageBox("Invalid configuration file.", JOptionPane.WARNING_MESSAGE);
                System.err.println("The specified file contains invalid data.");
        } catch (IOException iox) {
                // File is inaccessible, so stop
                messageBox(
                        """
                        The specified file could not be opened.
                        It may have been removed, or you may not have the correct permissions to access it.""",
                        JOptionPane.ERROR_MESSAGE
                ); 
        }
    }
    
    private boolean openConfigFile(INIFile htvFile, String playlist) throws IOException {
        // HTV configuration file loader.
        // Check that the fork value matches the one we're using
        String importedFork = htvFile.get("hacktv-gui3", "fork", "").toLowerCase(Locale.ENGLISH);
        String forkMismatch = "This file was created with a different fork of " +
            "hacktv. Some options may not be available.";
        if (!captainJack && importedFork.equals("captainjack")) {
            messageBox(forkMismatch, JOptionPane.WARNING_MESSAGE);
        } else if (captainJack && !importedFork.equals("captainjack")) {
            messageBox(forkMismatch, JOptionPane.WARNING_MESSAGE);
        }
        // Reset all controls
        resetAllControls();
        /* Output device
           For this, we look for hackrf, soapysdr or fl2k. An empty value will be
           interpreted as hackrf. Anything other than these values is handled
           as an output file.
         */
        String iod = htvFile.get("hacktv", "output", "hackrf").toLowerCase(Locale.ENGLISH);
        if (iod.startsWith("hackrf") || iod.startsWith("soapysdr") || iod.startsWith("fl2k")) {
            // Check if the imported value contains a serial number (value separated by a colon)
            String[] od = iod.split(":");
            // Set the combobox
            cmbOutputDevice.setSelectedItem(new ComboBoxOption(od[0], ""));
            // Add serial to the text field
            if (od.length == 2) txtOutputDevice.setText(od[1]);
            // fl2k audio
            if (od[0].equals("fl2k")) {
                var ap = new ComboBoxOption(htvFile.get("hacktv", "fl2k-audio", "").toLowerCase(Locale.ENGLISH), "");
                cmbFl2kAudio.setSelectedItem(ap);
            }
        } else {
            // File output, append as-is
            cmbOutputDevice.setSelectedItem(new ComboBoxOption("file", ""));
            txtOutputDevice.setText(iod);
            // Output file type
            String ft = htvFile.get("hacktv", "filetype", "").toLowerCase(Locale.ENGLISH);
            cmbFileType.setSelectedItem(ft);
        }
        // Video mode
        String importedVideoMode = htvFile.get("hacktv", "mode", "");
        // Search the modes array for this mode
        ModeInfo mi;
        try {
            // Search the modes array for the imported mode.
            // If not found under modeId(), try altModeId() instead.
            mi = modes.stream()
                .filter(m -> m.modeId().equals(importedVideoMode))
                .findFirst()
                .orElseGet(() -> modes.stream()
                    .filter(m -> importedVideoMode.equals(m.altModeId()))
                    .findFirst()
                    .orElseThrow());
            // Create a temporary object to set the list box to the correct system
            var co = new ColourOption(mi.colourMode(), mi.colourMode().toString());
            lstColour.setSelectedValue(co, true);
            cmbMode.setSelectedItem(mi);
        } catch (NoSuchElementException e) {
            invalidConfigFileValue("video mode", importedVideoMode);
            resetAllControls();
            return false;
        }
        var m = (ModeInfo) cmbMode.getSelectedItem();
        // Is this a baseband mode?
        boolean bb = mi.modulation() == UNMODULATED;
        // Input source or test card
        String ImportedSource = htvFile.get("hacktv", "input", "");
        String M3USource = (htvFile.get("hacktv-gui3", "m3usource", ""));
        if (ImportedSource.toLowerCase(Locale.ENGLISH).startsWith("test:")) {
            radTest.doClick();
            if (captainJack) {
                String importedTC = ImportedSource.replace("test:", "").trim().toLowerCase(Locale.ENGLISH);
                Integer idx = testCommandToIndex.get(importedTC);
                if (idx != null) {
                    cmbTest.setSelectedIndex(idx);
                } else if (!importedTC.isEmpty()) {
                    invalidConfigFileValue("test card", importedTC);
                }
            }
        }
        else if (!M3USource.isEmpty()) {
            var M3UFile = new File(M3USource);
            // If the source is an M3U file...
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            // Spawn M3UHandler using the source value we got above.
            m3uHandler(M3UFile.getAbsolutePath(), ImportedSource);
            txtSource.setText(M3USource);
        }
        else if (htvFile.getBoolean("hacktv-gui3", "playlist")) {
            // Use the playlist we got from checkSelectedFile();
            if (playlist != null) {
                String[] pl = playlist.split("\n");
                playlistModel.addAll(Arrays.asList(pl));
                if (htvFile.getInt("hacktv-gui3", "playliststart") != null) {
                    startPoint = htvFile.getInt("hacktv-gui3", "playliststart") - 1;
                    // Don't accept values lower than one
                    if (startPoint < 1) startPoint = -1;
                }
                chkRandom.setSelected(htvFile.getBoolean("hacktv-gui3", "random"));
            }
        }
        else {
            if ( !ImportedSource.endsWith(".m3u") && !ImportedSource.endsWith(".m3u8") ) txtSource.setText(ImportedSource);
        }
        // Frequency or channel number (and MAC channel ID)
        var o = (ComboBoxOption) cmbOutputDevice.getSelectedItem();
        String chid = null;
        if (m.colourMode() == MAC) chid = htvFile.get("hacktv", "chid", "");
        if ( (o.value().equals("hackrf") || o.value().equals("soapysdr")) && (!bb) ) {
            String noFreqOrChannelErr = "No frequency or valid channel number was found in the configuration file. Load aborted.";
            String importedCh = htvFile.get("hacktv-gui3", "channel", "");
            String ImportedBandPlan = htvFile.get("hacktv-gui3", "bandplan", "").toLowerCase(Locale.ENGLISH);
            Long importedFreq;
            if (htvFile.getDouble("hacktv", "frequency") != null) {
                importedFreq = htvFile.getLong("hacktv", "frequency");
            } else {
                messageBox(noFreqOrChannelErr, JOptionPane.WARNING_MESSAGE);
                resetAllControls();
                return false;
            }
            if (importedFreq == null && importedCh.isEmpty()) {
                // If not found, and the frequency is also blank, abort
                messageBox(noFreqOrChannelErr, JOptionPane.WARNING_MESSAGE);
                resetAllControls();
                return false;
            } else if (importedCh.isEmpty()) {
                cmbBand.setSelectedItem(CUSTOM_FREQUENCY);
                double freq = (double) importedFreq / 1000000.0;
                txtFrequency.setText(Double.toString(freq).replace(".0",".00"));
            } else {
                // Try to find the band plan
                BandPlan bp = m.getUhfPlan(ImportedBandPlan);
                // Channel object
                var ch = new Channel(importedCh, importedFreq, chid);
                if (bp == null) bp = m.getVhfPlan(ImportedBandPlan);
                if (bp == null) bp = m.getSatellitePlan(ImportedBandPlan);
                if (bp != null) {
                    cmbBand.setSelectedItem(bp.band());
                    cmbRegion.setSelectedItem(bp);
                    // Try to find the channel
                    int ind = bp.channels().indexOf(ch);
                    if (ind != -1) cmbChannel.setSelectedIndex(ind);
                }
                // Check if the correct channel was applied
                var probe = (Channel) cmbChannel.getSelectedItem();
                if (probe != null && probe.frequency() != ch.frequency()) {
                    // Use a custom frequency instead
                    cmbBand.setSelectedItem(CUSTOM_FREQUENCY);
                    var df2 = new DecimalFormat("0.00");
                    txtFrequency.setText(df2.format((double) importedFreq / 1000000.0));
                }
            }
            // Enable lock frequency option if supported
            if (htvFile.getInt("hacktv-gui3", "lockfrequency") != null && chkLockFrequency.isEnabled()) {
                if (htvFile.getInt("hacktv-gui3", "lockfrequency") == 1) chkLockFrequency.doClick();
            }
        }
        // SECAM field ID
        if (htvFile.getBoolean("hacktv", "secam-field-id") && m.colourMode() == ColourMode.SECAM) {
            chkSecamId.doClick();
            if (htvFile.getInt("hacktv", "secam-field-id-lines") != null) {
                int id = htvFile.getInt("hacktv", "secam-field-id-lines");
                if ((id >= 1) && (id <= 8)) {
                    cmbSecamIdLines.setSelectedIndex(id - 1);
                }
            }
        }
        // Swap IQ
        if (htvFile.getBoolean("hacktv", "swap-iq") &&
                m.modulation() != UNMODULATED ) {
            chkSwapIQ.doClick();
        }
        // Gain
        if (htvFile.getInt("hacktv", "gain") != null) {
            txtGain.setText(htvFile.getInt("hacktv", "gain").toString());
        }
        // If value is null and output device is hackrf or soapysdr, set gain to zero
        else if (o.value().equals("hackrf") || o.value().equals("soapysdr")) {
            if (!bb) txtGain.setText("0");
        }
        // Amp
        if (cmbOutputDevice.getSelectedIndex() == 0) {
            if (htvFile.getBoolean("hacktv", "amp")) {
                chkAmp.doClick();
            }
        }
        // FM deviation
        if ((chkFMDev.isEnabled()) && (htvFile.getDouble("hacktv", "deviation") != null)) {
            Double ImportedDeviation = (htvFile.getDouble("hacktv", "deviation") / 1000000);
            chkFMDev.doClick();
            txtFMDev.setText(ImportedDeviation.toString().replace(".0",""));
        }
        // Output level
        String ImportedLevel = htvFile.get("hacktv", "level", "").toLowerCase(Locale.ENGLISH);
        if (!ImportedLevel.isEmpty()) {
            chkOutputLevel.doClick();
            txtOutputLevel.setText(ImportedLevel);
        }
        // Gamma
        String ImportedGamma = htvFile.get("hacktv", "gamma", "").toLowerCase(Locale.ENGLISH);
        if (!ImportedGamma.isEmpty()) {
            chkGamma.doClick();
            txtGamma.setText(ImportedGamma);
        }
        // Position
        if (chkPosition.isEnabled()) {
            if (htvFile.getInt("hacktv", "position") != null) {
                chkPosition.doClick();
                txtPosition.setText(htvFile.getInt("hacktv", "position").toString());
            }
        }
        // Verbose mode
        /*if (htvFile.getBoolean("hacktv", "verbose")) {
            chkVerbose.doClick();
        }*/
        // Logo
        if (chkLogo.isEnabled()) {
            String importedLogo = htvFile.get("hacktv", "logo", "").toLowerCase(Locale.ENGLISH);
            // Check first if the imported string is a .png file.
            // hacktv now contains its own internal resources so external files
            // are no longer supported.
            if (importedLogo.endsWith(".png")) {
                messageBox(
                     "hacktv no longer supports external logo files. Logo option disabled.", JOptionPane.WARNING_MESSAGE);
            }
            else if (!importedLogo.isBlank()) {
                var probe = new ComboBoxOption(importedLogo, "");
                cmbLogo.setSelectedItem(probe);
                if (!probe.equals(cmbLogo.getSelectedItem())) {
                    invalidConfigFileValue("logo", importedLogo);
                } else {
                    cmbLogo.setEnabled(true);
                    chkLogo.setSelected(true);
                }
            }
        }
        // Timestamp
        if (chkTimestamp.isEnabled()) {
            if (htvFile.getBoolean("hacktv", "timestamp")) {
                chkTimestamp.doClick();
            }
        }
        // Interlace
        if (htvFile.getBoolean("hacktv", "interlace")) {
            chkInterlace.doClick();
        }
        // Teletext
        String ImportedTeletext = htvFile.get("hacktv", "teletext", "");
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
        Integer importedWSS = htvFile.getInt("hacktv", "wss");
        // Only accept values within the range of the combobox
        if (importedWSS != null && (importedWSS > 0 && importedWSS <= cmbWSS.getItemCount())) {
            chkWSS.doClick();
            // Since we increased the value by one when saving, decrease by one when loading
            cmbWSS.setSelectedIndex(importedWSS - 1);
        } else if (importedWSS != null ) {
            System.err.println("WSS value was out of bounds, skipped.");
        }
        /* Aspect ratio correction for 16:9 content on 4:3 displays
         * If the arcorrection value is not defined, leave the option unchecked
         * Otherwise, check the option and process it as normal
         */
        Integer importedAR = (htvFile.getInt("hacktv", "arcorrection"));
        if (importedAR != null && (importedAR >= 0 && importedAR < cmbAspectRatio.getItemCount())) {
            chkAspectRatio.doClick();
            cmbAspectRatio.setSelectedIndex(importedAR);
        } else if (importedAR != null ) {
            System.err.println("Aspect ratio value out of bounds, skipped.");
        }
        // Scrambling system
        String ica = htvFile.get("hacktv", "scramblingtype", "").toLowerCase(Locale.ENGLISH);
        String ik1 = htvFile.get("hacktv", "scramblingkey", "").toLowerCase(Locale.ENGLISH);
        String ik2 = htvFile.get("hacktv", "scramblingkey2", "").toLowerCase(Locale.ENGLISH);
        if (m.colourMode() == ColourMode.PAL || m.colourMode() == ColourMode.SECAM || m.colourMode() == ColourMode.MAC) {
            if (!ica.isBlank()) {
                ComboBoxOption ca;
                switch (ica) {
                    case "single-cut", "double-cut" -> {
                        ca = new ComboBoxOption(ica, "");
                        // Split the scramblingkey value into an array, using
                        // whitespace as the separator. [0] contains the CA
                        // system, while [1] contains the CA key.
                        String[] macCA = ik1.split("\\s");
                        ik1 = macCA[0];
                        if (macCA.length > 1) ik2 = macCA[1];
                    }
                    default -> ca = new ComboBoxOption(ica, "");
                }
                cmbScrambling1.setSelectedItem(ca);
                if (!ca.equals(cmbScrambling1.getSelectedItem())) {
                    invalidConfigFileValue("scrambling system", ica);
                    ica = "";
                }
            }
            // Scrambling key/viewing card type (including VC1 side of dual VC1/2 mode)
            if (!ica.isEmpty() && (!ik1.isEmpty())) {
                ComboBoxOption k1 = new ComboBoxOption(ik1, "");
                cmbScrambling2.setSelectedItem(k1);
                if (!k1.equals(cmbScrambling2.getSelectedItem())) {
                    if (ica.equals("videocrypt1+2")) {
                        invalidConfigFileValue("VideoCrypt I scrambling key", ik1);
                    } else {
                        invalidConfigFileValue("scrambling key", ik1);
                    }
                }
            }
            // VC2 side of dual VC1/2 mode
            // Also the access mode on MAC scrambling
            if (!ik2.isEmpty() && (ica.equals("videocrypt1+2") || ica.equals("single-cut") || ica.equals("double-cut"))) {
                ComboBoxOption k2 = new ComboBoxOption(ik2, "");
                cmbScrambling3.setSelectedItem(k2);
                if (!k2.equals(cmbScrambling3.getSelectedItem())) {
                    String noKey2;
                    if (ica.equals("videocrypt1+2")) {
                        noKey2 = "VideoCrypt II scrambling key";
                    } else {
                        noKey2 = "scrambling key";
                    }
                    invalidConfigFileValue(noKey2, ik2);
                }
            } 
        }
        String importedCardNumber;
        String imported13Prefix;
        VideoCryptEmmState emm = NO_EMM;
        String cardNumber = null;
        // EMM
        var sk1 = (ComboBoxOption) cmbScrambling2.getSelectedItem();
        if (sk1 != null && Shared.EMM_KEYS.contains(sk1.value())) {
            int importedEMM = htvFile.getInt("hacktv", "emm");
            switch (importedEMM) {
                case 0:
                default:
                    break;
                case 1:
                    emm = ENABLE_EMM;
                    break;
                case 2:
                    emm = DISABLE_EMM;
                    break;
            }
        }
        if (emm != NO_EMM) {
            importedCardNumber = htvFile.get("hacktv-gui3", "fullcardnumber");
            if (importedCardNumber == null) importedCardNumber = htvFile.get("hacktv", "cardnumber", "").toLowerCase(Locale.ENGLISH);
            // Handling of legacy files
            if (importedCardNumber.length() == 8) {
                imported13Prefix = htvFile.get("hacktv-gui3", "13digitprefix", "").toLowerCase(Locale.ENGLISH);
                // The importedCardNumber value only contains 8 digits of the card number
                // To find the check digit, we run the CalculateLuhnCheckDigit method and append the result
                if (Shared.isNumeric(imported13Prefix + importedCardNumber)) {
                    cardNumber = imported13Prefix + importedCardNumber +
                            Shared.calculateLuhnCheckDigit(Long.parseLong(importedCardNumber));
                }
            } else {
                // Pass the full card number through
                if (Shared.isNumeric(importedCardNumber)) cardNumber = importedCardNumber;
            }
        }
        boolean cardSerial = htvFile.getBoolean("hacktv", "showserial");
        // Brute force PPV key
        boolean findKey = htvFile.getBoolean("hacktv", "findkey");
        // Scramble audio
        boolean scrambleAudio = htvFile.getBoolean("hacktv", "scramble-audio");
        // Syster permutation table
        int permTable = 0;
        if (htvFile.getInt("hacktv", "permutationtable") != null) {
            Integer importedPermutationTable = htvFile.getInt("hacktv", "permutationtable");
            if (ica.equals("syster") || ica.equals("systercnr") || ica.equals("systerls+cnr")) {
                if (importedPermutationTable != null && (importedPermutationTable >= 0 && importedPermutationTable <= 2)) {
                    permTable = importedPermutationTable;
                }
            }
        }
        int matRating;
        // EuroCrypt maturity rating
        Integer importedMaturityRating = htvFile.getInt("hacktv", "ec-mat-rating");
        if (importedMaturityRating != null && (importedMaturityRating >= 0) && (importedMaturityRating <= 15)) {
            matRating = importedMaturityRating;
        } else {
            matRating = -1;
        }
        String ecProgNumber = null;
        String ecProgCost = null;
        // EuroCrypt PPV
        boolean ecPpv = htvFile.getBoolean("hacktv", "ec-ppv");
        if (ecPpv) {
            ecProgNumber = htvFile.get("hacktv", "ec-ppv-num");
            ecProgCost = htvFile.get("hacktv", "ec-ppv-cost");
        }
        // EuroCrypt "No Date" setting
        boolean ecNoDate = htvFile.getBoolean("hacktv", "ec-nodate");
        // ECM
        boolean showECM = htvFile.getBoolean("hacktv", "showecm");
        scramblingSettings = new ScramblingSettings(
                showECM,
                scrambleAudio,
                permTable,
                matRating,
                ecPpv,
                ecNoDate,
                ecProgNumber,
                ecProgCost,
                emm,
                cardNumber,
                cardSerial,
                findKey
        );
        // ACP
        if (htvFile.getBoolean("hacktv", "acp")) {
            chkACP.doClick();
        }
        // Filter
        if (htvFile.getBoolean("hacktv", "filter")) {
            if (chkVsbFilter.isEnabled()) chkVsbFilter.doClick();
            if (chkFmFilter.isEnabled()) chkFmFilter.doClick();
        }
        // Audio
        if (!htvFile.getBoolean("hacktv", "audio")) {
            if (radMono.isSelected() ) radMono.doClick();
        }
        // NICAM
        if (!htvFile.getBoolean("hacktv", "nicam")) {
            if (radNICAM.isSelected() ) radNICAM.doClick();
        }
        // A2 Stereo
        if (htvFile.getBoolean("hacktv", "a2stereo")) {
            if ( (!radA2Stereo.isSelected()) && (m.a2()) ) radA2Stereo.doClick();
        }
        // VITS
        if (htvFile.getBoolean("hacktv", "vits")) {
            chkVITS.doClick();
        }
        // VITC
        if (htvFile.getBoolean("hacktv", "vitc")) {
            chkVITC.doClick();
        }
        // SiS
        if (htvFile.getBoolean("hacktv", "sis")) {
            chkSiS.doClick();
        }
        // Subtitles
        if (htvFile.getBoolean("hacktv", "subtitles")) {
            chkSubtitles.doClick();
            if ( (htvFile.getInt("hacktv", "subtitleindex")) != null ) {
                txtSubtitleIndex.setText(Integer.toString((htvFile.getInt("hacktv", "subtitleindex"))));
            }
        }
        // MAC audio options
        if (m.colourMode() == ColourMode.MAC) {
            macSettings = new MacSettings(
                    chid,
                    htvFile.get("hacktv", "mac-audio-mode", "stereo").toLowerCase(Locale.ENGLISH).equals("mono"),
                    htvFile.get("hacktv", "mac-audio-quality", "high").toLowerCase(Locale.ENGLISH).equals("medium"),
                    htvFile.get("hacktv", "mac-audio-compression", "companded").toLowerCase(Locale.ENGLISH).equals("linear"),
                    htvFile.get("hacktv", "mac-audio-protection", "l1").toLowerCase(Locale.ENGLISH).equals("l2")
            );
        }
        // Disable colour
        if (chkColour.isEnabled()) {
            // Accept both UK and US English spelling
            if ( (htvFile.getBoolean("hacktv", "nocolour")) ||
                    (htvFile.getBoolean("hacktv", "nocolor")) ){
                chkColour.setSelected(false);
            }
        }
        // S-Video mode
        if (htvFile.getBoolean("hacktv", "s-video") ){
            if (chkSVideo.isEnabled()) chkSVideo.doClick();
        }
        // Closed captioning
        if (htvFile.getBoolean("hacktv", "cc608") ){
            if (chkCC608.isEnabled()) chkCC608.doClick();
        }
        // Invert video polarity
        if (htvFile.getBoolean("hacktv", "invert-video") ){
            chkInvertVideo.doClick();
        }
        // SoapySDR antenna name
        if (cmbOutputDevice.getSelectedIndex() == 1) {
            txtAntennaName.setText(htvFile.get("hacktv", "antennaname", "").toLowerCase(Locale.ENGLISH));
        }
        // Volume
        String ImportedVolume = htvFile.get("hacktv", "volume", "").toLowerCase(Locale.ENGLISH);
        if (!ImportedVolume.isEmpty()) {
            chkVolume.doClick();
            txtVolume.setText(ImportedVolume);
        }
        // Downmix
        if (htvFile.getBoolean("hacktv", "downmix")) {
            chkDownmix.doClick();
        }
        // Teletext subtitles
        if ( (htvFile.getBoolean("hacktv", "tx-subtitles")) ){
            chkTeletextSubtitles.doClick();
            if ( (htvFile.getInt("hacktv", "tx-subindex")) != null ) {
                txtTeletextSubtitleIndex.setText(Integer.toString((htvFile.getInt("hacktv", "tx-subindex"))));
            }
        } else if ( (htvFile.getBoolean("hacktv", "teletextsubtitles")) ){
            chkTeletextSubtitles.doClick();
            if ( (htvFile.getInt("hacktv", "teletextsubindex")) != null ) {
                txtTeletextSubtitleIndex.setText(Integer.toString(htvFile.getInt("hacktv", "teletextsubindex")));
            }
        }
        // Offset
        Double ImportedOffset;
        if (htvFile.getDouble("hacktv", "offset") != null) {
            if (!chkOffset.isSelected()) chkOffset.doClick();
            ImportedOffset = (htvFile.getDouble("hacktv", "offset") / 1000000);
            txtOffset.setText(ImportedOffset.toString().replace(".0","")); 
        }
        // Pixel rate
        Double ImportedPixelRate;
        if ((htvFile.getDouble("hacktv", "pixelrate")) != null) {
            if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
            ImportedPixelRate = (htvFile.getDouble("hacktv", "pixelrate") / 1000000);
            txtPixelRate.setText(ImportedPixelRate.toString().replace(".0","")); 
        }
        // Sample rate (default to 16 MHz if not specified)
        // Add this last so other changes don't interfere with the value in the
        // configuration file.
        Double importedSampleRate = htvFile.getDouble("hacktv", "samplerate");
        if (importedSampleRate != null) {
            importedSampleRate = importedSampleRate / 1000000;
        } else {
            importedSampleRate = Double.valueOf("16");
            messageBox("No sample rate specified, defaulting to 16 MHz.", JOptionPane.INFORMATION_MESSAGE);
        }
        txtSampleRate.setText(importedSampleRate.toString().replace(".0",""));
        // Philips test signal
        String importedTS = htvFile.get("hacktv", "testsignal", "");
        if (supportsPhilipsTestSignal) {
            boolean TSFound = false;
            if (!importedTS.isEmpty()) {
                var model = cmbTest.getModel();
                for (int i = 0; i < model.getSize(); i++) {
                    var opt = model.getElementAt(i);
                    if (opt != null && opt.command() != null &&
                        importedTS.equalsIgnoreCase(opt.command())) {
                        if (!ImportedSource.isBlank()) txtSource.setText(ImportedSource);
                        radTest.doClick();
                        cmbTest.setSelectedIndex(i);
                        TSFound = true;
                        break;
                    }
                }
                if (!TSFound) {
                    invalidConfigFileValue("test signal", importedTS);
                }
            }
        } else if (!importedTS.isBlank()) {
            messageBox("The selected build of hacktv does not support the " +
                    importedTS + " test signal.\n" +
                    "The setting will be skipped.", JOptionPane.WARNING_MESSAGE);
        }
        // Repeat
        if (chkRepeat.isEnabled()) {
            if (htvFile.getBoolean("hacktv", "repeat")) {
                chkRepeat.doClick();
            }
        }
        btnRun.requestFocusInWindow();
        // This must be the last line in this method, it confirms that 
        // everything ran as planned.
        return true;
    }
    
    private void invalidConfigFileValue (String settingName, String value) {
        /*
        * This method is used to generate an error when an invalid value is found in a config file
        * Saves us writing out the same error message multiple times
        * To use it, just feed two values or variables into it and they will be added to the message below 
        */
        // If an incorrect scrambling system/key was specified, disable scrambling
        if ( (settingName.contains("scrambling system")) ||
                (settingName.contains("scrambling key")) ||
                (settingName.contains("VideoCrypt I scrambling key"))||
                (settingName.contains("VideoCrypt II scrambling key")) ) {
            cmbScrambling1.setSelectedIndex(0);
        }
        messageBox("The " + settingName + '\u0020' + '\u0022' + value + '\u0022' + 
                " specified in the configuration file could not be found.\n" +
                "The file may have been created in a different version of the application, or the value is invalid.",
                JOptionPane.WARNING_MESSAGE);
    }
    
    private void saveConfigFile (File destinationFileName) {
        /**
         * HTV configuration file writer.
         * Saves the current state to a configuration file with a .htv extension.
         */
        var m = (ModeInfo) cmbMode.getSelectedItem();
        // Check the frequency to commit it to a variable before we start
        // If invalid, then abort
        if (!checkCustomFrequency()) return;
        // New class instance to create empty file
        var newHtv = new INIFile();
        // Output device
        var om = (ComboBoxOption) cmbOutputDevice.getSelectedItem();
        switch (om.value()) {
            case "hackrf" -> {
                if (txtOutputDevice.getText().isBlank()) {
                    newHtv.set("hacktv", "output", "hackrf");
                }
                else {
                    newHtv.set("hacktv", "output", "hackrf:" + txtOutputDevice.getText());
                }
            }
            case "soapysdr" -> {
                if (txtOutputDevice.getText().isBlank()) {
                    newHtv.set("hacktv", "output", "soapysdr");
                }
                else {
                    newHtv.set("hacktv", "output", "soapysdr:" + txtOutputDevice.getText());
                }
                // SoapySDR antenna name
                if (!txtAntennaName.getText().isBlank()) newHtv.set("hacktv", "antennaname", txtAntennaName.getText());
            }
            case "fl2k" -> {
                if (txtOutputDevice.getText().isBlank()) {
                    newHtv.set("hacktv", "output", "fl2k");
                }
                else {
                    newHtv.set("hacktv", "output", "fl2k:" + txtOutputDevice.getText());
                }
                // fl2k audio
                var fam = (ComboBoxOption) cmbFl2kAudio.getSelectedItem();
                if (!fam.value().isEmpty()) newHtv.set("hacktv", "fl2k-audio", fam.value());
            }
            case "file" -> {
                if (txtOutputDevice.getText().isBlank()) {
                    messageBox("Please select an output file or change the output device.", JOptionPane.WARNING_MESSAGE);
                }
                else {
                    newHtv.set("hacktv", "output", txtOutputDevice.getText());
                    // File type
                    newHtv.set("hacktv", "filetype", cmbFileType.getSelectedItem().toString());
                }
            }
            default -> {
            }
        }
        // Save current fork if applicable
        if (captainJack) newHtv.set("hacktv-gui3", "fork", "CaptainJack");
        // Input source or test card
        if (!playlistModel.isEmpty()) {
            // We'll populate the playlist section later
            newHtv.setInt("hacktv-gui3", "playlist", 1);
            // Set start point of playlist
            if (startPoint != -1) newHtv.setInt("hacktv-gui3", "playliststart", startPoint + 1);
            // Random option
            if (chkRandom.isSelected()) newHtv.setInt("hacktv-gui3", "random", 1);
        } else {
            // We'll add Philips patterns later, if any
            if ( (radTest.isSelected()) && (!isPhilipsTestSignal()) ) {
                var ts = (TestSignalOption) cmbTest.getSelectedItem();
                if ((cmbTest.isEnabled()) && (ts.command() != null)) {
                    newHtv.set("hacktv", "input", "test:" + ts.command());
                } else {
                    newHtv.set("hacktv", "input", "test:colourbars");
                }
            } else if ((txtSource.getText().toLowerCase(Locale.ENGLISH).endsWith(".m3u")) ||
                    (txtSource.getText().toLowerCase(Locale.ENGLISH).endsWith(".m3u8"))) {
                // Check if the M3U exists
                if (Files.exists(Path.of(txtSource.getText()))) {
                    // Save the selected item from the Extended M3U file
                    var m3uSource = (ComboBoxOption) cmbM3USource.getSelectedItem();
                    newHtv.set("hacktv-gui3", "m3usource", txtSource.getText());
                    newHtv.set("hacktv", "input", m3uSource.value());
                    // No longer required but saved for backwards compatibility
                    newHtv.setInt("hacktv-gui3", "m3uindex", cmbM3USource.getSelectedIndex());
                } else {
                    // Save path as-is. This may or may not be valid but will be caught when re-opened.
                    newHtv.set("hacktv", "input", txtSource.getText());
                }
            } else {
                newHtv.set("hacktv", "input", txtSource.getText());
            }
        }
        // Video format/mode
        newHtv.set("hacktv", "mode", m.modeId());
        // Is this a baseband mode?
        boolean bb = m.modulation() == UNMODULATED;
        // Frequency and channel
        var od = ((ComboBoxOption) cmbOutputDevice.getSelectedItem()).value();
        if ( od.equals("hackrf") || od.equals("soapysdr") ) {
            if ( (!cmbBand.getSelectedItem().equals(CUSTOM_FREQUENCY)) && (!bb) ) {
                newHtv.set("hacktv-gui3", "channel", cmbChannel.getSelectedItem().toString());
                // Save band plan identifier, this uses the section name from modes file
                if (!cmbBand.getSelectedItem().equals(CUSTOM_FREQUENCY)) {
                    newHtv.set("hacktv-gui3", "bandplan", (((BandPlan) cmbRegion.getSelectedItem()).id())) ;
                }
            }
            var mode = (ModeInfo) cmbMode.getSelectedItem();
            if (mode.modulation() == FM) {
                // Save the IF to the frequency field for backwards compatibility
                // The Ku frequency will be retrieved from the band plan if it exists
                long f = calculateFrequency(frequency, false);
                if (f == ((Long.MIN_VALUE + 256))) {
                    return;
                }
                else {
                    newHtv.setLong("hacktv", "frequency", f);
                    // This setting is not yet used for anything, but we may need it in future
                    newHtv.set("hacktv-gui3", "satellite", "1");
                }
            }
            else {
                if (!bb) newHtv.setLong("hacktv", "frequency", frequency);
            }
            if (chkLockFrequency.isSelected()) newHtv.setInt("hacktv-gui3", "lockfrequency", 1);
        }
        // Sample rate
        if (Shared.isNumeric(txtSampleRate.getText())) {
            newHtv.setLong("hacktv", "samplerate", (long) (Double.parseDouble(txtSampleRate.getText()) * 1000000));
        }
        // Pixel rate
        if (Shared.isNumeric(txtPixelRate.getText())) {
            newHtv.setLong("hacktv", "pixelrate", (long) (Double.parseDouble(txtPixelRate.getText()) * 1000000));
        }
        // Offset
        if (Shared.isNumeric(txtOffset.getText())) {
            newHtv.setLong("hacktv", "offset", (long) (Double.parseDouble(txtOffset.getText()) * 1000000));
        }
        // SECAM field ID
        if (chkSecamId.isSelected()) {
            newHtv.setInt("hacktv", "secam-field-id", 1);
            int id = cmbSecamIdLines.getSelectedIndex() + 1;
            if ((id >= 1) && (id <= 8)) {
                newHtv.setInt("hacktv", "secam-field-id-lines", id);
            }
        }
        // Swap IQ
        if (chkSwapIQ.isSelected()) {
            newHtv.setInt("hacktv", "swap-iq", 1);
        }
        // Gain
        if ( (cmbOutputDevice.getSelectedIndex() == 0) || (cmbOutputDevice.getSelectedIndex() == 1) ) {
            if (!bb) newHtv.setInt("hacktv", "gain", Integer.parseInt(txtGain.getText()));
        }
        // RF Amp
        if (chkAmp.isSelected()) newHtv.setInt("hacktv", "amp", 1);
        // Output level
        if (chkOutputLevel.isSelected()) newHtv.set("hacktv", "level", txtOutputLevel.getText());
        // FM deviation
        if (chkFMDev.isSelected()) newHtv.setLong("hacktv", "deviation", (long) (Double.parseDouble(txtFMDev.getText()) * 1000000));
        // Gamma
        if (chkGamma.isSelected()) newHtv.set("hacktv", "gamma", txtGamma.getText());
        // Repeat
        if (chkRepeat.isSelected()) newHtv.setInt("hacktv", "repeat", 1);
        // Position
        if (chkPosition.isSelected()) newHtv.setInt("hacktv", "position", Integer.parseInt(txtPosition.getText()));
        // Verbose
        // if (chkVerbose.isSelected()) newHtv.setInt("hacktv", "verbose", 1);
        // Logo
        if (chkLogo.isSelected()) {
            var l = (ComboBoxOption) cmbLogo.getSelectedItem();
            newHtv.set("hacktv", "logo", l.value());
        }
        // Timestamp
        if (chkTimestamp.isSelected()) newHtv.setInt("hacktv", "timestamp", 1);
        // Interlace
        if (chkInterlace.isSelected()) newHtv.setInt("hacktv", "interlace", 1);
        // Teletext
        if (txtTeletextSource.getText().endsWith(".t42")) {
            newHtv.set("hacktv", "teletext", "raw:" + txtTeletextSource.getText());
        }
        else if (!txtTeletextSource.getText().isEmpty()) {
            newHtv.set("hacktv", "teletext", txtTeletextSource.getText());
        }
        /* WSS
         * We increase the value by one, because zero is interpreted as "option disabled" while 1 is
         * interpreted as "auto". We will subtract this again when opening.
        */
        if (chkWSS.isSelected()) newHtv.setInt("hacktv", "wss", cmbWSS.getSelectedIndex() + 1);
        // AR Correction
        if (chkAspectRatio.isSelected()) newHtv.setInt("hacktv", "arcorrection", cmbAspectRatio.getSelectedIndex());
        // Scrambling, go to a new function for this
        saveScramblingSettings(newHtv);
        // ACP
        if (chkACP.isSelected()) newHtv.setInt("hacktv", "acp", 1);
        // Filter
        if (chkVsbFilter.isSelected() || chkFmFilter.isSelected()) newHtv.setInt("hacktv", "filter", 1);
        // Audio
        if (radNoAudio.isSelected() && radMono.isEnabled()) {
            newHtv.setInt("hacktv", "audio", 0); 
        } else if (radMono.isEnabled()) {
            newHtv.setInt("hacktv", "audio", 1); 
        }
        // NICAM
        if (radNICAM.isSelected()) {
            newHtv.setInt("hacktv", "nicam", 1);
        }
        // A2 stereo
        if (radA2Stereo.isSelected()) {
            newHtv.setInt("hacktv", "a2stereo", 1);
        }
        // Subtitles
        if (chkSubtitles.isSelected()) { 
            newHtv.setInt("hacktv", "subtitles", 1); 
            newHtv.set("hacktv", "subtitleindex", txtSubtitleIndex.getText());
        }
        // VITS
        if (chkVITS.isSelected()) newHtv.setInt("hacktv", "vits", 1);
        // VITC
        if (chkVITC.isSelected()) newHtv.setInt("hacktv", "vitc", 1);
        // SiS
        if (chkSiS.isSelected()) {
            newHtv.setInt("hacktv", "sis", 1);
            // This setting has been added for possible future use but is not currently read
            newHtv.set("hacktv", "sismode", "dcsis");
        }
        // Disable colour
        if (!chkColour.isSelected()) newHtv.setInt("hacktv", "nocolour", 1);
        // S-Video
        if (chkSVideo.isSelected()) newHtv.setInt("hacktv", "s-video", 1);
        // Closed captioning
        if (chkCC608.isSelected()) newHtv.setInt("hacktv", "cc608", 1);
        // Invert video
        if (chkInvertVideo.isSelected()) newHtv.setInt("hacktv", "invert-video", 1);
        // MAC settings
        if (macSettings != null) {
            // MAC channel ID
            String chid = macSettings.channelID();
            if (chid != null && !chid.isBlank()) newHtv.set("hacktv", "chid", chid);
            // MAC audio options
            if (macSettings.audioMode()) newHtv.set("hacktv", "mac-audio-mode", "mono");
            if (macSettings.audioQuality()) newHtv.set("hacktv", "mac-audio-quality", "medium");
            if (macSettings.audioCompression()) newHtv.set("hacktv", "mac-audio-compression", "linear");
            if (macSettings.audioProtection()) newHtv.set("hacktv", "mac-audio-protection", "l2");
        }

        // Volume
        if (chkVolume.isSelected()) newHtv.set("hacktv", "volume", txtVolume.getText());
        // Downmix
        if (chkDownmix.isSelected()) newHtv.setInt("hacktv", "downmix", 1);
        // Teletext subtitles
        if (chkTeletextSubtitles.isSelected()) {
            newHtv.setInt("hacktv", "tx-subtitles", 1);
            newHtv.set("hacktv", "tx-subindex", txtTeletextSubtitleIndex.getText());
        }
        // Philips test signals
        if (isPhilipsTestSignal()) {
            var ts = (TestSignalOption) cmbTest.getSelectedItem();
            if (ts.command().equals("colourbars")) {
                newHtv.set("hacktv", "input", "test:colourbars");
            } else {
                newHtv.set("hacktv", "testsignal", ts.command());
            }
        }
        String newFile;
        // The playlist doesn't follow a standard INI format. We just dump the
        // playlist array into the file as-is.
        if (!playlistModel.isEmpty()) {
            var sb = new StringBuilder();
            sb.append(newHtv.toString());
            sb.append("\n[playlist]\n");
            for (int i = 1; i <= playlistModel.size(); i++) {
                sb.append(playlistModel.get(i - 1)).append("\n");
            }
            newFile = sb.toString();
        } else {
            newFile = newHtv.toString();
        }
        // Commit to disk
        try {
            Files.writeString(destinationFileName.toPath(), newFile, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            messageBox("An error occurred while writing to this file. "
                    + "The file may be read-only or you may not have the correct permissions.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Display the opened filename in the title bar
        // Back up the original title once
        if (!titleBarChanged) { 
            titleBar = getTitle();
            titleBarChanged = true;
        }
        setTitle(titleBar + " - " + destinationFileName.getName());
       // Remove the ellipsis after Save to follow standard UI guidelines
        menuSave.setText("Save");
        updateMRUList(destinationFileName.toString());
    }
    
    private INIFile saveScramblingSettings(INIFile newHtv) {
        var s1 = (ComboBoxOption) cmbScrambling1.getSelectedItem();
        // If no scrambling enabled, send the config back without any changes
        if (s1 == null || s1.value().isEmpty()) return newHtv;
        var m = (ModeInfo) cmbMode.getSelectedItem();
        var s2 = (ComboBoxOption) cmbScrambling2.getSelectedItem();
        var s3 = (ComboBoxOption) cmbScrambling3.getSelectedItem();
        ScramblingInfo si;
        if (m.colourMode() != MAC) {
            si = (ScramblingInfo) scramblingInfo625.get(s1.value());
            newHtv.set("hacktv", "scramblingtype", s1.value());
            if (s2 == null) return newHtv;
            newHtv.set("hacktv", "scramblingkey", s2.value());
            if (s3 != null) newHtv.set("hacktv", "scramblingkey2", s3.value());
        } else {
            si = (ScramblingInfo) scramblingInfoMac.get(s2.value());
            newHtv.set("hacktv", "scramblingtype", s1.value());
            if (!s2.value().isEmpty()) {
                String macCA = s2.value();
                String macKey = s3.value();
                if (!macKey.isEmpty()) newHtv.set("hacktv", "scramblingkey", macCA + '\u0020' + macKey);
            }
        }
        // No need to go any further if these are null
        if (si == null || scramblingSettings == null) return newHtv;
        // Show ECM
        if (si.ecmSupported() && scramblingSettings.showECM()) newHtv.setInt("hacktv", "showecm", 1);
        // Scramble audio
        if (si.scrambleAudioSupported() && scramblingSettings.scrambleAudio()) {
            newHtv.setInt("hacktv", "scramble-audio", 1);
        }
        // Only save EMM settings if the current key supports them
        if (si.videocryptFeatures() && Shared.EMM_KEYS.contains(s2.value())) {
            String cardNumber = scramblingSettings.videocryptCardNumber();
            if (cardNumber != null) {
                // EMM
                switch (scramblingSettings.videocryptEmmState()) {
                    case NO_EMM:
                    default:
                        break;
                    case ENABLE_EMM:
                        newHtv.setInt("hacktv", "emm", 1);
                        break;
                    case DISABLE_EMM:
                        newHtv.setInt("hacktv", "emm", 2);
                        break;
                }
                if (scramblingSettings.videocryptEmmState() != NO_EMM) {
                    newHtv.set("hacktv", "cardnumber", Shared.checkCardNumber(cardNumber, si, s2.value()));
                    newHtv.set("hacktv-gui3", "fullcardnumber", cardNumber);
                }
            } 
        }
        if (si.videocryptFeatures()) {
            // Show card serial
            if (scramblingSettings.showCardSerial()) newHtv.setInt("hacktv", "showserial", 1);
            // Brute force PPV key
            if (scramblingSettings.findKeys()) newHtv.setInt("hacktv", "findkey", 1);
        } else if (si.systerFeatures()) {
            // Syster permutation table
            int spt = scramblingSettings.systerPermTable();
            if (spt == 1 || spt == 2) {
                newHtv.setInt("hacktv", "permutationtable", spt);
            }
        } else if (si.eurocryptFeatures()) {
            // EuroCrypt maturity rating
            int ecmat = scramblingSettings.eurocryptMaturityRating();
            if (ecmat > 0) {
                newHtv.setInt("hacktv", "ec-mat-rating", ecmat);
            }
            // EuroCrypt PPV
            if (scramblingSettings.eurocryptPpv()) {
                String ecnum = scramblingSettings.eurocryptProgNumber();
                String eccost = scramblingSettings.eurocryptProgCost();
                newHtv.setInt("hacktv", "ec-ppv", 1);
                if (ecnum != null && !ecnum.isBlank()) {
                    newHtv.set("hacktv", "ec-ppv-num", ecnum);
                }
                if (eccost != null && !eccost.isBlank()) {
                    newHtv.set("hacktv", "ec-ppv-cost", eccost);
                }
            }
            // EuroCrypt "No Date" setting
            if (scramblingSettings.eurocryptNoDate()) {
                newHtv.setInt("hacktv", "ec-nodate", 1);
            }
        }
        return newHtv;
    }
    
    private void m3uHandler(String sourceFile) {
        m3uHandler(sourceFile, null);
    }
    
    private void m3uHandler(String SourceFile, String selectedItem) {
        File f = new File(SourceFile);
        String fileHeader = null;
        var filesRemoved = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(f.toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (fileHeader == null) fileHeader = line;
                if (!fileHeader.contains("#EXTM3U")) {
                    // Skip if this is a URL or test card
                    if ( (!line.startsWith("http:")) &&
                            (!line.startsWith("https:")) &&
                            (!line.startsWith("test:")) ) {
                        if (Files.exists(Path.of(line))) {
                            playlistModel.addElement(line);
                        } else {
                            filesRemoved.append(line).append("\n");
                        }
                    }
                } else {
                    // Call the extended M3U handler
                    extM3UHandler(f, selectedItem);
                    return;
                }
            }
        } catch (IOException ex) {
            // File is inaccessible, so stop
            System.err.println(ex);
            messageBox("""
                       The specified file could not be opened.
                       It may have been removed, or you may not have the correct permissions to access it.""",
                    JOptionPane.ERROR_MESSAGE
            ); 
            resetM3UItems(false);
            return;       
        }
        if (fileHeader == null) {
            messageBox("Invalid file format.", JOptionPane.ERROR_MESSAGE);
            resetM3UItems(false);
            return;
        }
        // Did we remove any files? If so, alert.
        if (!filesRemoved.toString().isBlank()) {
            messageBox("Some files could not be found and have been removed from the playlist.\n" + 
                    filesRemoved.toString(),
                    JOptionPane.WARNING_MESSAGE);
        }
        resetM3UItems(false);
    }
    
    private void extM3UHandler(File f, String selectedItem) {
        // Handler for Extended M3U files (with #EXTM3U header)
        // Set mouse cursor to busy
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Temporarily disable the radio buttons, Browse and Run buttons, and menus
        btnSourceBrowse.setEnabled(false);
        radLocalSource.setEnabled(false);
        radTest.setEnabled(false);
        btnRun.setEnabled(false);
        btnAdd.setEnabled(false);
        // Hide the source file textbox and show the combobox
        var card = (CardLayout) sourceCardPanel.getLayout();
        card.show(sourceCardPanel, "m3ucombobox");
        cmbM3USource.setEnabled(false);
        fileMenu.setEnabled(false);
        templatesMenu.setEnabled(false);
        // Prevent the combobox from auto-resizing
        var d = new Dimension(cmbM3USource.getPreferredSize());
        cmbM3USource.setPreferredSize(d);
        // Remove any existing items from the combobox
        cmbM3USource.removeAllItems();
        cmbM3USource.addItem(new ComboBoxOption("", "Loading playlist file, please wait..."));
        // Create a SwingWorker to do the disruptive stuff
        var m3uWorker = new SwingWorker<ArrayList<ComboBoxOption>, Double>() {
            @Override
            protected ArrayList<ComboBoxOption> doInBackground() throws Exception {
                var pls = new ArrayList<ComboBoxOption>();
                try (BufferedReader reader = Files.newBufferedReader(f.toPath())) {
                    String line;
                    String n = "";
                    String url = "";
                    int l = 1;
                    long lineCount = Files.lines(f.toPath()).count();
                    while ((line = reader.readLine()) != null) {
                        // Publish a decimal value for the percentage indicator
                        publish((double) l / lineCount);
                        if (line.startsWith("#EXTINF:")) {
                            // Read names
                            n = line.substring(line.lastIndexOf(",") + 1).trim();
                        } else if (!line.startsWith("#")) {
                            // Read URLs directly to the arraylist
                            url = line;
                        }
                        if (!url.isBlank() && !n.isBlank()) {
                            pls.add(new ComboBoxOption(url, n));
                            url = "";
                            n = "";  
                        }
                        l++;
                    }
                } catch (IOException ex) {
                    System.err.println(ex);
                    return null;
                }
                // Done, publish 100%
                publish(1.0);
                // Check that we got something, if not then stop.
                if (!pls.isEmpty()) return pls;
                return null;
            } // End doInBackground()
            @Override
            protected void done() {
                // Retrieve the return value of doInBackground.
                try {
                    var result = get();
                    if (result == null) throw new IllegalStateException("Playlist array was null");
                    // Enable and populate the combobox
                    cmbM3USource.setEnabled(true);
                    cmbM3USource.setModel(new DefaultComboBoxModel<>(result.toArray(ComboBoxOption[]::new)));
                    if (selectedItem != null) {
                        // Try to select the item we received
                        var s = new ComboBoxOption(selectedItem, "");
                        cmbM3USource.setSelectedItem(s);
                        if (!s.equals(cmbM3USource.getSelectedItem())) {
                            messageBox(
                            "Could not restore the saved playlist entry. The referenced item was not found in the playlist.",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    // Repaint the combobox (resolves an issue with it not showing the
                    // correct entry on the Metal L&F after loading an M3U file).
                    cmbM3USource.repaint();
                    // Reset cursor and re-enable the radio buttons that we disabled
                    resetM3UItems(true);
                } catch (InterruptedException | ExecutionException | IllegalStateException ex) {
                    System.err.println(ex);
                    messageBox(
                            "An error occurred while processing this file. "
                                    + "It may be invalid or corrupted.", JOptionPane.ERROR_MESSAGE);
                    resetM3UItems(false);   
                }
            } // End done()
            @Override
            protected void process(List<Double> chunks) {
                int p = (int) (chunks.get(chunks.size()-1) * 100);
                // Taskbar/dock progress if supported
                if (Taskbar.isTaskbarSupported()) {
                    var t = Taskbar.getTaskbar();
                    if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
                        t.setWindowProgressValue(MainWindow.this, p);
                    } else if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                        t.setProgressValue(p);
                    }
                }
                cmbM3USource.removeAllItems();
                cmbM3USource.addItem(new ComboBoxOption("", "Loading playlist file, please wait... " + p + "%"));
            }
          }; // End SwingWorker
        m3uWorker.execute();
    }
    
    private void resetM3UItems(boolean LoadSuccessful) {
        // Reset whatever we changed back to default upon thread exit
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
                t.setWindowProgressState(MainWindow.this, Taskbar.State.OFF);
            }
            else if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                t.setProgressValue(-1);
            }
        }
        if (!LoadSuccessful) {
            // Hide the combobox and show the source textbox
            // Use this for a load failure
            resetM3UItems(true);
            txtSource.setText("");
            var card = (CardLayout) sourceCardPanel.getLayout();
            card.show(sourceCardPanel, "textbox"); 
        }
    }
    
    private void resetAllControls() {
        // Clear status bar
        txtStatus.setText("");
        // Reset modal dialogue settings
        scramblingSettings = null;
        macSettings = null;
        // Reselect the default mode
        if (chkLockFrequency.isSelected()) chkLockFrequency.doClick();
        lstColour.setSelectedIndex(0);
        if (cmbMode.getItemCount() > 0) cmbMode.setSelectedIndex(0);
        // Uncheck all checkboxes
        for (JCheckBox cb: checkBoxes){
            if (cb.isSelected()) cb.doClick();
        }
        // Hide M3U combobox if enabled, and set source to empty
        if (cmbM3USource.isEnabled()) {
            var card = (CardLayout) sourceCardPanel.getLayout();
            card.show(sourceCardPanel, "textbox");
        }
        txtSource.setText("");
        // Reset output device to HackRF
        cmbOutputDevice.setSelectedIndex(0);
        // Select default radio buttons and comboboxes
        radLocalSource.doClick();
        // Reset gain to zero
        txtGain.setText("0");
        // Clear playlist
        playlistModel.clear();
        // Restore title bar to default
        if (titleBarChanged) setTitle(titleBar);
        // Restore ellipsis to Save option
        if (menuSave.getText().equals("Save")) menuSave.setText("Save...");
    }
    
    private void addTeletextOptions() {
        cmbTeletextDownload.addItem(Shared.addComboBoxOption("", "Select..."));
        cmbTeletextDownload.addItem(Shared.addComboBoxOption("ceefax", "Ceefax"));
        cmbTeletextDownload.addItem(Shared.addComboBoxOption("teefax", "Teefax"));
        cmbTeletextDownload.addItem(Shared.addComboBoxOption("spark", "SPARK"));
    }
    
    private void addCeefaxRegions() {
        // Populate the Ceefax regions to the combobox in GUI settings
        var ceefaxRegions = new ComboBoxOption[] {
            new ComboBoxOption("East", "East"),
            new ComboBoxOption("EastMidlands", "East Midlands"),
            new ComboBoxOption("London", "London"),
            new ComboBoxOption("NorthernIreland", "Northern Ireland"),
            new ComboBoxOption("Scotland", "Scotland"),
            new ComboBoxOption("South", "South"),
            new ComboBoxOption("SouthWest", "South West"),
            new ComboBoxOption("Wales", "Wales"),
            new ComboBoxOption("West", "West"),
            new ComboBoxOption("Worldwide", "Worldwide"),
            new ComboBoxOption("Yorks&Lincs", "Yorkshire & Lincolnshire")
        };
        cmbNMSCeefaxRegion.setModel(new DefaultComboBoxModel<>(ceefaxRegions));
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

    private void downloadTeletext(String url, String name, String query1, String query2) {
        var teletextLinks = new ArrayList<String>();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        downloadInProgress = true;
        // Create temp directory if it does not exist
        createTempDirectory();
        // Create a SwingWorker to do the disruptive stuff
        var downloadPages = new SwingWorker<String, Integer>() {
            @Override
            protected String doInBackground() throws IOException, URISyntaxException, XMLStreamException {
                // Download the index file
                String index;
                try {
                    // Download the index page
                    txtStatus.setText("Downloading index page from " + url);
                    index = Shared.downloadToString(url);
                } catch (IOException | URISyntaxException ex) {
                    System.err.println(ex);
                    return ex.getMessage();
                }
                // Try to determine the file type
                if (index.trim().startsWith("<html>")) {
                    // HTML
                    teletextLinks.addAll(Shared.getHtmlLinks(index));
                } else if (index.startsWith("[") || index.startsWith("{")) {
                    // JSON
                    teletextLinks.addAll(Shared.queryJson(index, query1));
                } else if (index.startsWith("<?xml")) {
                    // XML
                    teletextLinks.addAll(Shared.queryXml(index, query1, query2));
                } else {
                    // Unknown data
                    return "Incorrect data received from " + url;
                }
                // Remove anything that isn't a TTI or TTIX file
                teletextLinks.removeIf(s -> !s.endsWith(".tti") && !s.endsWith(".ttix"));
                if (teletextLinks.isEmpty()) {
                    // The index page was downloaded but we didn't find anything.
                    // Most likely means that we need to revise this!
                    return "No teletext files were found.";
                }
                File f = new File(tempDir + File.separator + name);
                // Delete this directory if it already exists (e.g. from
                // a previous download attempt).
                if (Files.exists(f.toPath())) {
                    try {
                        Shared.deleteFSObject(f.toPath());
                    } catch (IOException ex) {
                        return ex.getMessage();
                    }
                }
                // Create download directory
                if (!f.isDirectory() && !f.mkdirs()) {
                    return "Unable to create directory " + f.toString();
                }
                teletextPath = f.toString();
                // Iterate through the links array
                for (int i = 0; i < teletextLinks.size(); i++) {
                    String entry = teletextLinks.get(i);
                    String sourceUrl;
                    String destination;
                    // If we received a full URL in the array, as opposed to just a file name
                    if (entry.startsWith("http")) {
                        sourceUrl = entry;
                        // Split out the file name only for the destination variable
                        var u = new URI(entry).toURL();
                        destination = Path.of(u.getPath()).getFileName().toString();
                        // Update the array value so the status bar stays consistent
                        teletextLinks.set(i, destination);
                    } else {
                        // Append the URL received from the previous method to
                        // the sourceUrl variable for download.
                        sourceUrl = url + entry;
                        destination = entry;
                    }
                    try {
                        // If the Stop button has been pressed, then stop
                        if (downloadCancelled) {
                            downloadCancelled = false;
                            downloadInProgress = false;
                            return "";
                        }
                        publish(i + 1);
                        // Start the download
                        Shared.download(sourceUrl, Path.of(teletextPath, destination));
                    } catch (IOException ex) {
                        // The index page was downloaded but a teletext page failed.
                        // Connection failure?
                        ex.getMessage();
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                String status;
                try {
                    // Retrieve the status code from doInBackground.
                    status = get();
                } catch (InterruptedException | ExecutionException ex) {
                    System.err.println(ex);
                    status = ex.toString();
                }
                if (status == null) {
                    // All good
                    txtStatus.setText("Done");
                    txtTeletextSource.setText(teletextPath);                    
                } else if (status.isEmpty()) {
                    // Download cancelled by the user
                    txtStatus.setText("Cancelled");
                } else {
                    messageBox(status, JOptionPane.WARNING_MESSAGE);
                    txtStatus.setText("Failed");
                }
                resetTeletextButtons();
            }

            @Override
            protected void process(List<Integer> chunks) {
                // Retrieve the values from publish() and use them to increment
                // the progress bar and display in the status bar.
                int i = chunks.get(chunks.size()-1);
                // Show progress in status bar
                double pc = (double) i / teletextLinks.size() * 100;
                txtStatus.setText("Downloading page " + teletextLinks.get(i -1)
                        +  '\u0020' + "(" + i + " of " + teletextLinks.size() + ")"
                        +  '\u0020' + (int) pc + "%");
                // Taskbar/dock progress if supported
                if (Taskbar.isTaskbarSupported()) {
                    var t = Taskbar.getTaskbar();
                    if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
                        t.setWindowProgressValue(MainWindow.this, (int) pc);
                    } else if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                        t.setProgressValue((int) pc);
                    }
                }
            }
        };
        downloadPages.execute();
    }
    
    private void resetTeletextButtons() {
        // Resets the labels of the teletext buttons back to defaults and
        // re-enables them.
        teletextDownloadPanel.setEnabled(true);
        lblTeletextDownloadHeader.setEnabled(true);
        cmbTeletextDownload.setEnabled(true);
        btnTeletextDownload.setText(DOWNLOAD_TELETEXT);
        btnTeletextDownload.setEnabled(true);
        chkTeletext.setEnabled(true);
        txtTeletextSource.setEnabled(true);
        txtTeletextSource.setEditable(true);
        btnTeletextBrowse.setEnabled(true);
        btnRun.setEnabled(true);
        // Reset hacktv download button
        if (isWindows) btnDownloadHackTV.setEnabled(true);
        // Reset taskbar/dock progress bars
        if (Taskbar.isTaskbarSupported()) {
            var t = Taskbar.getTaskbar();
            if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
                t.setWindowProgressState(MainWindow.this, Taskbar.State.OFF);
            }
            else if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                t.setProgressValue(-1);
            }
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        downloadInProgress = false;
    }
    
    private void downloadHackTV_Win32(String dUrl) {
        // Downloads the latest pre-compiled Windows build from my build server
        // The download URL is sent here from the download dialogue
        btnDownloadHackTV.setText("Cancel");
        // Disable Teletext download options so they don't interfere
        if (chkTeletext.isSelected()) {
            teletextDownloadPanel.setEnabled(false);
            lblTeletextDownloadHeader.setEnabled(false);
            cmbTeletextDownload.setEnabled(false);
            btnTeletextDownload.setEnabled(false);
            teletextDownloadPanel.setEnabled(false);            
        }
        downloadInProgress = true;
        txtStatus.setText("Connecting to " + dUrl);
        var downloadHackTV = new SwingWorker<String, Integer>() {
            long p;
            volatile long size;
            @Override
            protected String doInBackground() throws IOException, URISyntaxException, MalformedURLException {
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
                    Shared.unzipFile(downloadPath, t);
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
                            t.setWindowProgressValue(MainWindow.this, (int) d);
                        }
                        else if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                            t.setProgressValue((int) d);
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
                    teletextDownloadPanel.setEnabled(true);
                    teletextDownloadPanel.setEnabled(true);
                    lblTeletextDownloadHeader.setEnabled(true);
                    cmbTeletextDownload.setEnabled(true);
                    btnTeletextDownload.setEnabled(true);                 
                }
                // Reset taskbar/dock progress bars
                if (Taskbar.isTaskbarSupported()) {
                    var t = Taskbar.getTaskbar();
                    if (t.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)) {
                        t.setWindowProgressState(MainWindow.this, Taskbar.State.OFF);
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
                    messageBox("""
                               An error occurred while downloading hacktv.
                               Please ensure that you have write permissions to the application directory and that you have internet access.""",
                            JOptionPane.WARNING_MESSAGE
                    );
                    txtStatus.setText("Failed");
                    downloadCancelled = false;
                }
                else if (exePath.isEmpty()) {
                    txtStatus.setText("Cancelled");
                    downloadCancelled = false;
                }
                else if (exePath.equals("CertificateExpiredException")) {
                    messageBox("""
                               Download failed due to an expired SSL/TLS certificate.
                               Please ensure that your system date is correct. Otherwise, please try again later.""",
                            JOptionPane.WARNING_MESSAGE
                    );
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
        cmbScrambling1.setEnabled(true);
        lblScrambling1.setEnabled(true);
        scramblingPanel.setEnabled(true);
    }    
    
    private void disableScrambling() {
        cmbScrambling1.setSelectedIndex(0);
        cmbScrambling1.setEnabled(false);
        lblScrambling1.setEnabled(false);
        scramblingPanel.setEnabled(false);
    }      
    
    private void add625ScramblingTypes() {
        configureScramblingLabels();
        if (cmbScrambling1.getItemCount() > 0) cmbScrambling1.removeAllItems();
        String caType;
        String displayName;
        String lookupId;
        cmbScrambling1.addItem(new ComboBoxOption("", "No scrambling"));
        // Check if modes file contains a section for these scrambling systems
        // Only add those which have keys defined
        int vc1 = modesIni.getKeys("videocrypt").length;
        int vc2 = modesIni.getKeys("videocrypt2").length;
        if (vc1 > 0) {
            caType = "videocrypt";
            displayName = "VideoCrypt I";
            lookupId = "videocrypt";
            cmbScrambling1.addItem(new ComboBoxOption(lookupId, displayName));
            scramblingInfo625.put(lookupId, new ScramblingInfo(
                    caType,
                    displayName,
                    null,
                    28000000L, // Multiples of 14 are OK
                    true,
                    false,
                    false,
                    false,
                    true,
                    addScramblingKeys(caType),
                    null
            ));
        }
        if (vc2 > 0) {
            caType = "videocrypt2";
            displayName = "VideoCrypt II";
            lookupId = "videocrypt2";
            cmbScrambling1.addItem(new ComboBoxOption(lookupId, displayName));
            scramblingInfo625.put(lookupId, new ScramblingInfo(
                    caType,
                    displayName,
                    null,
                    28000000L,
                    true,
                    false,
                    false,
                    false,
                    true,
                    addScramblingKeys(caType),
                    null
            ));
        }
        if (vc1 > 0 && vc2 > 0) {
            caType = "videocrypt";
            String ca2Type = "videocrypt2";
            displayName = "VideoCrypt I+II";
            lookupId = "videocrypt1+2";
            cmbScrambling1.addItem(new ComboBoxOption(lookupId, displayName));
            scramblingInfo625.put(lookupId, new ScramblingInfo(
                    caType,
                    displayName,
                    ca2Type,
                    28000000L,
                    true,
                    false,
                    false,
                    false,
                    true,
                    addScramblingKeys(caType),
                    addScramblingKeys(ca2Type)
            ));
        }        
        if (modesIni.getKeys("videocrypts").length > 0) {
            caType ="videocrypts";
            displayName = "VideoCrypt S";
            lookupId = "videocrypts";
            cmbScrambling1.addItem(new ComboBoxOption(lookupId, displayName));
            scramblingInfo625.put(lookupId, new ScramblingInfo(
                    caType,
                    displayName,
                    null,
                    17750000L, // more accurately 177344750 but this is reported by hacktv as unsuitable for 625/50
                    false,
                    false,
                    false,
                    false,
                    false,
                    addScramblingKeys(caType),
                    null
            ));
        }
        if (modesIni.getKeys("syster").length > 0) {
            caType = "syster";
            displayName = "Nagravision Syster";
            lookupId = "syster";
            cmbScrambling1.addItem(new ComboBoxOption(lookupId, displayName));
            var systerKeys = addScramblingKeys(caType);
            scramblingInfo625.put(lookupId, new ScramblingInfo(
                    caType,
                    displayName,
                    null,
                    null,
                    true,
                    true,
                    true,
                    false,
                    false,
                    systerKeys,
                    null
            ));
            caType = "systercnr";
            displayName = "Nagravision Syster (cut-and-rotate mode)";
            lookupId = "systercnr";
            cmbScrambling1.addItem(new ComboBoxOption(lookupId, displayName));
            scramblingInfo625.put(lookupId, new ScramblingInfo(
                    caType,
                    displayName,
                    null,
                    17750000L,
                    true,
                    true,
                    true,
                    false,
                    false,
                    systerKeys,
                    null
            ));
            caType = "syster";
            lookupId = "systerls+cnr";
            displayName = "Nagravision Syster (line shuffle and cut-and-rotate modes)";
            cmbScrambling1.addItem(new ComboBoxOption(lookupId, displayName));
            scramblingInfo625.put(lookupId, new ScramblingInfo(
                    caType,
                    displayName,
                    "systercnr",
                    17750000L,
                    true,
                    true,
                    true,
                    false,
                    false,
                    systerKeys,
                    null
            ));
            caType = "d11";
            displayName = "Discret 11";
            lookupId = "d11";
            cmbScrambling1.addItem(new ComboBoxOption(lookupId, displayName));
            scramblingInfo625.put(lookupId, new ScramblingInfo(
                    caType,
                    displayName,
                    null,
                    17750000L,
                    true,
                    true,
                    false,
                    false,
                    false,
                    systerKeys,
                    null
            ));
        }
        caType = "d14";
        displayName = "Discret 14";
        lookupId = "d14";
        cmbScrambling1.addItem(new ComboBoxOption(lookupId, displayName));
        scramblingInfo625.put(lookupId, new ScramblingInfo(
                caType,
                displayName,
                null,
                17750000L,
                false,
                false,
                false,
                false,
                false,
                addScramblingKeys(caType),
                null
        ));
        // If no systems were found, disable the scrambling tab
        if (cmbScrambling1.getItemCount() == 1) disableScrambling();
    }
    
    private void addMACScramblingTypes() {
        configureScramblingLabels();
        if (cmbScrambling1.getItemCount() > 0) cmbScrambling1.removeAllItems();
        cmbScrambling1.addItem(new ComboBoxOption("", "No scrambling"));
        cmbScrambling1.addItem(new ComboBoxOption("single-cut", "Single cut"));
        cmbScrambling1.addItem(new ComboBoxOption("double-cut", "Double cut"));
    }
    
    private List<ComboBoxOption> addScramblingKeys(String caType) {
        if (caType == null) return null;
        var keys = modesIni.getKeys(caType);
        if (keys == null) return null;
        var values = new ArrayList<ComboBoxOption>();
        for (String k : keys) {
            String v = modesIni.get(caType, k);
            values.add(new ComboBoxOption(k, v));
        }
        return values;
    }
    
    private void addScramblingKey() {
        var ca = (ComboBoxOption) cmbScrambling1.getSelectedItem();
        if (ca.value() == null || ca.value().isEmpty()) {
            disableScramblingKey1();
            disableScramblingKey2();
            txtSampleRate.setText(defaultSampleRate);
            if (chkPixelRate.isSelected()) chkPixelRate.doClick();
            return;
        }
        cmbScrambling2.removeAllItems();
        cmbScrambling3.removeAllItems();
        
        var si = (ScramblingInfo) scramblingInfo625.get(ca.value());
        if (si.caKeys() == null || si.caKeys().isEmpty()) {
            disableScramblingKey1();
            return;
        }
        
        // Get the keys for the first CA mode
        var caKeys = si.caKeys();
        for (ComboBoxOption c : caKeys) {
            cmbScrambling2.addItem(c);
        }
        lblScrambling2.setEnabled(cmbScrambling2.getItemCount() > 0);
        cmbScrambling2.setEnabled(cmbScrambling2.getItemCount() > 0);
        
        // Get the keys for the second CA mode
        var ca2Keys = si.ca2Keys();
        if (ca2Keys != null) {
            for (ComboBoxOption c : ca2Keys) {
                cmbScrambling3.addItem(c);
            }
        }
        lblScrambling3.setEnabled(cmbScrambling3.getItemCount() > 0);
        cmbScrambling3.setEnabled(cmbScrambling3.getItemCount() > 0);
        
        // Set pixel rate
        if (si.preferredSampleRate() != null) {
            var result = Shared.longToDecimal(si.preferredSampleRate());
            if (!txtSampleRate.getText().equals(result)) {
                if (!chkPixelRate.isSelected()) chkPixelRate.doClick();
                txtPixelRate.setText(result);
            }
        }
    }
    
    private void addMACScramblingCA() {
        var s = (ComboBoxOption) cmbScrambling1.getSelectedItem();
        // In the clear (no scrambling)
        if (s.value().isEmpty()) {
            scramblingPanel.setEnabled(false);
            disableScramblingKey1();
            cmbScrambling2.setSelectedIndex(-1);
            disableScramblingKey2();
            txtSampleRate.setText(defaultSampleRate);
            if (chkPixelRate.isSelected()) chkPixelRate.doClick();
            return;
        } else {
            enableScramblingKey1();
            scramblingPanel.setEnabled(true);
        }
        String displayName;
        cmbScrambling2.removeAllItems();
        displayName = "No conditional access (free)";
        cmbScrambling2.addItem(new ComboBoxOption("", displayName));
        scramblingInfoMac.put("", new ScramblingInfo(
                "",
                displayName,
                null,
                20250000L,
                false,
                false,
                false,
                false,
                false,
                null,
                null
        ));
        // Check the [macscrambling] section for supported CAs
        String[] caTypes = modesIni.getKeys("macscrambling");
        if (caTypes.length == 0) {
            // See if the [eurocrypt] section exists
            int ec = modesIni.getKeys("eurocrypt").length;
            String ecValue = "eurocrypt";
            displayName = "EuroCrypt";
            if (ec > 0) {
                cmbScrambling2.addItem(new ComboBoxOption(ecValue, displayName));
                scramblingInfoMac.put("eurocrypt", new ScramblingInfo(
                    ecValue,
                    displayName,
                    null,
                    20250000L,
                    false,
                    false,
                    false,
                    true,
                    false,
                    null,
                    addScramblingKeys(ecValue)  
                ));
            }
        } else {
            // Get each setting in [macscrambling]
            // This will be used to find a corresponding CA section and the
            // combobox display name
            // This is currently unused may need to be revised in future
            String c;
            for (String ca : caTypes) {
                c = modesIni.get("macscrambling", ca, "");
                if (c.isBlank()) continue;
                cmbScrambling2.addItem(new ComboBoxOption(ca, c));
                scramblingInfoMac.put(ca, new ScramblingInfo(
                        ca,
                        c,
                        null,
                        20250000L,
                        false,
                        false,
                        false,
                        true,
                        false,
                        null,
                        addScramblingKeys(ca)
                ));
            }
        }
    }
    
    private void addMACScramblingKey() {
        var s = (ComboBoxOption) cmbScrambling2.getSelectedItem();
        if (s.value() == null || s.value().isEmpty()) {
            disableScramblingKey2();
            return;
        }
        cmbScrambling3.removeAllItems();
        if (scramblingInfoMac.get(s.value()).ca2Keys() == null) return;
        var ca2Keys = scramblingInfoMac.get(s.value()).ca2Keys();
        if (ca2Keys == null)  {
            return;
        } else {
            enableScramblingKey2();
            for (ComboBoxOption c : ca2Keys) {
                // Don't add deprecated "blank" value
                if (!c.value().equals("blank")) cmbScrambling3.addItem(c);
            }
        }
        if (cmbScrambling3.getItemCount() == 0) disableScramblingKey2();
    }
    
    private ScramblingInfo getScramblingInfo(ModeInfo m, String caLookupId) {
        if (m.colourMode() == ColourMode.MAC) {
            return scramblingInfoMac.get(caLookupId);
        } else {
            return scramblingInfo625.get(caLookupId);
        }
    }
    
    private void configureScramblingLabels() {
        if (((ModeInfo) (cmbMode.getSelectedItem())).colourMode() != ColourMode.MAC) {
            lblScrambling1.setText("Scrambling system");
            lblScrambling2.setText("Access type");
            lblScrambling3.setText("VC2 access type");
        } else {
            lblScrambling1.setText("Scrambling type");
            lblScrambling2.setText("CA system");
            lblScrambling3.setText("CA mode");
        }
    }
    
    private void configureScramblingOptions() {
        var s1 = (ComboBoxOption) cmbScrambling1.getSelectedItem();
        var s2 = (ComboBoxOption) cmbScrambling2.getSelectedItem();
        var s3 = (ComboBoxOption) cmbScrambling3.getSelectedItem();
        String key = null;
        var m = (ModeInfo) cmbMode.getSelectedItem();
        ScramblingInfo si = null;
        if (m.colourMode() == MAC) {
            if (s2 != null) si = getScramblingInfo(m, s2.value());
            if (s3 != null) key = s3.value();
        } else {
            si = getScramblingInfo(m, s1.value());
            if (s2 != null) key = s2.value();
        }
        // Enable/disable ACP
        boolean acp = (si == null && m.acp());
        Shared.toggleCheckBox(chkACP, acp);
        if (si == null) {
            btnScramblingOptions.setEnabled(false);
            return;
        }
        // Don't enable the Scrambling Options button on VCS or D14, or on
        // any VideoCrypt CA unless in Captain Jack mode
        boolean u = 
                (!captainJack && si.id().startsWith("videocrypt")) ||
                si.id().equals("videocrypts") ||
                si.id().equals("d14") ||
                key == null ||
                key.equals("free");
        btnScramblingOptions.setEnabled(!u);
        // Reset scrambling settings if unsupported on current CA or key
        if (!btnScramblingOptions.isEnabled()) scramblingSettings = null;
    }
 
    private void enableScramblingKey1() {
        lblScrambling2.setEnabled(true);
        cmbScrambling2.setEnabled(true);
    }
    
    private void disableScramblingKey1() {
        lblScrambling2.setEnabled(false);
        cmbScrambling2.setEnabled(false);
        cmbScrambling2.removeAllItems();
    }
    
    private void enableScramblingKey2() {
        lblScrambling3.setEnabled(true);
        cmbScrambling3.setEnabled(true);
    }
    
    private void disableScramblingKey2() {
        cmbScrambling3.setEnabled(false);
        cmbScrambling3.setSelectedIndex(-1);
        lblScrambling3.setEnabled(false);
    }
    
    private ArrayList<String> checkScrambling() {
        var al = new ArrayList<String>();
        var m = (ModeInfo) cmbMode.getSelectedItem();
        var c1 = (ComboBoxOption) cmbScrambling1.getSelectedItem();
        if (c1 == null || c1.value().isEmpty()) return al;
        var c2 = (ComboBoxOption) cmbScrambling2.getSelectedItem();
        var c3 = (ComboBoxOption) cmbScrambling3.getSelectedItem();
        ScramblingInfo si = null;
        if (m.colourMode() == ColourMode.MAC) {
            // Scrambling type (single cut or double cut)
            al.add("--" + c1.value());
            if (c2 != null) {
                // CA system (e.g. EuroCrypt)
                if (!c2.value().isEmpty()) {
                    si = scramblingInfoMac.get(c2.value());
                    al.add("--" + c2.value());
                }
                // CA key
                if (c3 != null) al.add(c3.value());
            }
        } else {
            si = scramblingInfo625.get(c1.value());
            // CA1 system
            al.add("--" + si.id());
            // CA1 key
            if (c2 != null) al.add(c2.value());
            if (c3 != null) {
                // CA2 system
                al.add("--" + si.ca2Id());
                // CA2 key
                al.add(c3.value());
            } else if (si.ca2Id() != null && si.systerFeatures()) {
                // Syster's dual mode uses the same key for both modes
                // Use the CA2 system, but the CA1 key
                al.add("--" + si.ca2Id());
                if (c2 != null) al.add(c2.value());
            }
        }
        if (scramblingSettings == null || si == null) return al;
        // Show ECM
        if (si.ecmSupported() && scramblingSettings.showECM()) al.add("--showecm");
        // VideoCrypt
        if (si.videocryptFeatures() && c2 != null && Shared.EMM_KEYS.contains(c2.value())) {
            String fullCardNumber = scramblingSettings.videocryptCardNumber();
            if (fullCardNumber != null) {
                String truncatedCardNumber = Shared.checkCardNumber(fullCardNumber, si, c2.value());
                if (truncatedCardNumber != null && !truncatedCardNumber.isBlank()) {
                    if (scramblingSettings.videocryptEmmState() == ENABLE_EMM) {
                        al.add("--enableemm");
                    } else if (scramblingSettings.videocryptEmmState() == DISABLE_EMM) {
                        al.add("--disableemm");
                    }
                    al.add(truncatedCardNumber);
                }
            }
        }
        if (si.videocryptFeatures()) {
            if (scramblingSettings.showCardSerial()) al.add("--showserial");
            if (scramblingSettings.findKeys()) al.add("--findkey");
        }
        // Scramble audio
        if (si.scrambleAudioSupported() && scramblingSettings.scrambleAudio()) {
            if (c1.value().equals("single-cut") ||
                (c1.value().equals("double-cut")) ) {
                al.add("--scramble-audio");
            } else if (si.systerFeatures()) {
                al.add("--systeraudio");
            }
        }
        // EuroCrypt
        if (si.eurocryptFeatures()) {
            if (scramblingSettings.eurocryptMaturityRating() > 0) {
                al.add("--ec-mat-rating");
                al.add(Integer.toString(scramblingSettings.eurocryptMaturityRating()));
            }
            if (scramblingSettings.eurocryptPpv()) {
                al.add("--ec-ppv");
                String n = scramblingSettings.eurocryptProgNumber();
                String c = scramblingSettings.eurocryptProgCost();
                if (n != null && n.isEmpty()) n = "0";
                if (c != null && c.isEmpty()) c = "0";
                al.add(n + "," + c);
            }
            if (scramblingSettings.eurocryptNoDate()) al.add("--nodate");
        }
        // Syster
        if (si.systerFeatures()) {
            switch (scramblingSettings.systerPermTable()) {
                case 1 -> al.add("--key-table-1");
                case 2 -> al.add("--key-table-2");
                default -> {
                }
            }
        }
        return al;
    }
      
    private void setWSS(boolean b) {
        if (!b && chkWSS.isSelected()) chkWSS.doClick();
        chkWSS.setEnabled(b);
    }
    
    private void populateWSS() {
        var wssOptions = new ComboBoxOption[] {
            new ComboBoxOption("auto", "auto"),
            new ComboBoxOption("4:3", "4:3"),
            new ComboBoxOption("14:9-letterbox", "14:9 letterbox"),
            new ComboBoxOption("14:9-top", "14:9 top"),
            new ComboBoxOption("16:9-letterbox", "16:9 letterbox"),
            new ComboBoxOption("16:9-top", "16:9 top"),
            new ComboBoxOption("16:9+-letterbox", "16:9+-letterbox"),
            new ComboBoxOption("14:9-window", "14:9 window"),
            new ComboBoxOption("16:9", "16:9")
        };
        cmbWSS.setModel(new DefaultComboBoxModel<>(wssOptions));
        cmbWSS.setSelectedIndex(-1);
    }
    
    private ArrayList<String> checkWSS() {
        // Populate WSS parameters if enabled
        var al = new ArrayList<String>();
        if (chkWSS.isSelected()) {
            var m = (ComboBoxOption) cmbWSS.getSelectedItem();
            al.add("--wss");
            al.add(m.value());
        }
        return al;
    }
    
    private void addARCorrectionOptions() {
        ComboBoxOption[] arModes;
        if (!captainJack) {
            arModes = new ComboBoxOption[] {
                new ComboBoxOption("", "Stretched"),
                new ComboBoxOption("fit", "Fit"),
                new ComboBoxOption("fill", "Fill"),
                new ComboBoxOption("none", "None")
            };
        } else {
            arModes = new ComboBoxOption[] {
                new ComboBoxOption("", "Stretched"),
                new ComboBoxOption("--letterbox", "Letterboxed"),
                new ComboBoxOption("--pillarbox", "Cropped")
            };
        }
        cmbAspectRatio.setModel(new DefaultComboBoxModel<>(arModes));
        cmbAspectRatio.setSelectedIndex(0);
    }
    
    private ArrayList<String> checkARCorrectionOptions() {
        var al = new ArrayList<String>();
        if (chkAspectRatio.isSelected()) {
            var m = (ComboBoxOption) cmbAspectRatio.getSelectedItem();
            if (m.value().isEmpty()) return al;
            if (!captainJack) al.add("--fit");
            al.add(m.value());
        }
        return al;
    }
    
    private void addLogoOptions() {
        // Extract the list of logos from the INI file
        var keys = modesIni.getKeys("logos");
        if (keys.length == 0) {
            // If nothing was found, disable the logo options and stop
            if (chkLogo.isSelected()) chkLogo.doClick();
            chkLogo.setEnabled(false);
            return;
        }
        var logoOptions = new ComboBoxOption[keys.length];
        for (int i = 0; i < keys.length; i++) {
            logoOptions[i] = new ComboBoxOption(keys[i], modesIni.get("logos", keys[i]));
        }
        cmbLogo.setModel(new DefaultComboBoxModel<>(logoOptions));
        cmbLogo.setSelectedIndex(0);
        if (!chkLogo.isSelected()) cmbLogo.setSelectedIndex(-1);
    }
    
    private ArrayList<String> checkLogo() {
        var al = new ArrayList<String>();
        // Populate logo parameters if enabled
        if (chkLogo.isSelected()) {
            var m = (ComboBoxOption) cmbLogo.getSelectedItem();
            al.add("--logo");
            al.add(m.value());
        }
        return al;
    }
    
    private TestSignalOption parseTestCard(String command, String value) {
        // Parses the INI value returned from a [testcards] section
        return new TestSignalOption(command, value.trim(), "", false, "");
    }
    
    private TestSignalOption parseTestSignal(String command, String value) {
        // Parses the INI value returned from a [testsignals_*_*] section
        String[] parts = value.split("\\s*,\\s*", -1);
        String name = parts.length > 0 ? parts[0].trim() : "";
        String file = parts.length > 1 ? parts[1].trim() : "";
        boolean text = parts.length > 2 && "1".equals(parts[2].trim());
        String rate = parts.length > 3 ? parts[3].trim() : "";
        return new TestSignalOption(command, name, file, text, rate);
    }
    
    private void disableTestCardComboBox() {
        cmbTest.setModel(new DefaultComboBoxModel<>());
        cmbTest.setEnabled(false);
        cmbTest.setSelectedIndex(-1);
    }
    
    private void addTestCardOptions() {
        var m = (ModeInfo) cmbMode.getSelectedItem();
        String tcSection; // The INI section name
        String[] tcKeys;  // The INI setting names
        if (supportsPhilipsTestSignal) {
            // PT8631 emulation
            var c = m.colourMode();
            if (c == ColourMode.PAL || c == ColourMode.NTSC || c == ColourMode.SECAM) {
                // Get the keys from a testsignals section (e.g. [testsignals_625_pal])
                // This will return an empty string array if the section does not exist
                tcSection = 
                        "testsignals" + "_" +
                        Integer.toString(m.lines()) + "_" +
                        (c.toString().toLowerCase(Locale.ENGLISH));
                tcKeys = modesIni.getKeys(tcSection);
            } else {
                // Unsupported mode, disable the test card combobox
                disableTestCardComboBox();
                return;
            }
        } else {
            // Get the test card list
            tcSection = "testcards";
            tcKeys = modesIni.getKeys(tcSection);
        }
        if (tcKeys.length == 0) {
            // Nothing was found, disable the test card combobox
            disableTestCardComboBox();
        } else {           
            var options = new ArrayList<TestSignalOption>();
            for (String key : tcKeys) {
                String value = modesIni.get(tcSection, key, "");
                if (value.isBlank()) continue;
                TestSignalOption opt;
                if (tcSection.equals("testcards")) {
                    opt = parseTestCard(key, value);
                } else {
                    opt = parseTestSignal(key, value);
                }
                options.add(opt);
            }
            // Remove any items where the pattern file is missing
            if (supportsPhilipsTestSignal) {
                String dir = PREFS.get("testdir", hackTVDirectory);
                if (!Files.exists(Paths.get(dir, "pm8546g.bin"))) {
                    // Remove everything, this file is a prerequisite
                    options.clear();
                } else {
                    options.removeIf(o ->
                        o.patternFilename() != null &&
                        !o.patternFilename().isBlank() &&
                        !Files.exists(Paths.get(dir, o.patternFilename()))
                    );    
                }
            }
            // Cache the options to a hashmap we can check later
            testCommandToIndex.clear();
            for (int i = 0; i < options.size(); i++) {
                testCommandToIndex.put(options.get(i).command().toLowerCase(Locale.ENGLISH), i);
            }
            // Apply the model to the combobox
            var model = new DefaultComboBoxModel<TestSignalOption>();
            model.addAll(options);
            cmbTest.setModel(model);
            // Hide the first entry if the radio button is not selected
            if (!radTest.isSelected()) cmbTest.setSelectedIndex(-1);
        }
    }
    
    private ArrayList<String> checkTestCard() {
        var al = new ArrayList<String>();
        if (cmbTest.isEnabled()) {
            var ts = (TestSignalOption) cmbTest.getSelectedItem();
            if (captainJack) {
                al.add("test:" + ts.command());
            }
            else if (supportsPhilipsTestSignal) {
                if (ts.command().equals("colourbars")) {
                    // Use internal hacktv bars rather than the Philips one
                    al.add("test:colourbars");
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
                    if ((isWindows) && (p.matches(".*\\s.*"))) {
                        al.add('\u0022' + p + '\u0022');
                    }
                    else {
                        al.add(p);
                    }
                    // Add test signal parameters
                    al.add("--testsignal");
                    al.add(ts.command());
                    // Check if the selected pattern supports text insertion
                    if (ts.textInsertSupported()) {
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
            if (!playlistModel.contains("test:colourbars")) al.add("test:colourbars");
        }
        return al;
    }
    
    private boolean isPhilipsTestSignal() {
        if (!supportsPhilipsTestSignal) return false;
        if (cmbTest.getSelectedIndex() == -1) return false;
        var ts = (TestSignalOption) cmbTest.getSelectedItem();
        if (ts.patternFilename().isEmpty()) return false;
        String d = PREFS.get("testdir", hackTVDirectory);
        if (!Files.exists(Paths.get(d, "pm8546g.bin"))) return false;
        return Files.exists(Paths.get(d, ts.patternFilename()));
    }
    
    private String getTCSampleRate() {
        // Philips patterns use a fixed sample rate, usually 13.5 or 20 MHz.
        // Default sample rate (if not defined) is 13.5 MHz
        String sr = "13.5";
        // Check if the sample rate has been overriden for the selected pattern
        var ts = (TestSignalOption) cmbTest.getSelectedItem();
        if (Shared.isNumeric(ts.sampleRate())) {
            return ts.sampleRate();
        }
        else {
            return sr;
        }
    }
    
    private void addOutputDevices() {
        var outputDevices = new ComboBoxOption[] {
            new ComboBoxOption("hackrf", "HackRF"),
            new ComboBoxOption("soapysdr", "SoapySDR"),
            new ComboBoxOption("fl2k", "FL2000"),
            new ComboBoxOption("file", "File")
        };
        cmbOutputDevice.setModel(new DefaultComboBoxModel<>(outputDevices));
        cmbOutputDevice.setSelectedIndex(0);
    }
    
    private void addFl2kAudioOptions() {
        var audio = new ComboBoxOption[] {
            new ComboBoxOption("", "None"),
            new ComboBoxOption("mono", "Mono"),
            new ComboBoxOption("stereo", "Stereo"),
            new ComboBoxOption("spdif", "S/PDIF")
        };
        cmbFl2kAudio.setModel(new DefaultComboBoxModel<>(audio));
        cmbFl2kAudio.setSelectedIndex(0);
    }
    
    private void enableRFOptions() {
        txtGain.setText("0");
        txtGain.setEnabled(true);
        txtGain.setEditable(true);
        lblGain.setEnabled(true);
        lblBand.setEnabled(true);
        cmbBand.setEnabled(true);
        txtFrequency.setEnabled(true);
        txtFrequency.setEditable(false);
        chkLockFrequency.setEnabled(true);
        lblChannel.setEnabled(true);
        lblFrequency.setEnabled(true);
        rfPanel.setEnabled(true);
    }
    
    private void disableRFOptions() {
        txtGain.setText("");
        txtGain.setEnabled(false);
        txtGain.setEditable(false);
        lblGain.setEnabled(false);
        lblBand.setEnabled(false);
        cmbBand.setEnabled(false);
        cmbBand.setSelectedIndex(-1);
        cmbChannel.setEnabled(false);
        cmbChannel.setSelectedIndex(-1);
        lblChannel.setEnabled(false);
        lblFrequency.setEnabled(false);
        txtFrequency.setText("");
        txtFrequency.setEnabled(false);
        txtFrequency.setEditable(false);
        chkLockFrequency.setSelected(false);
        chkLockFrequency.setEnabled(false);
        lblRegion.setEnabled(false);
        cmbRegion.setEnabled(false);
        cmbRegion.removeAllItems();
        if (chkAmp.isSelected()) chkAmp.doClick();
        chkAmp.setEnabled(false);
        lblAntennaName.setEnabled(false);
        txtAntennaName.setText("");
        txtAntennaName.setEnabled(false);
        txtAntennaName.setEditable(false);
        rfPanel.setEnabled(false);
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
        if (chkAspectRatio.isSelected()) chkAspectRatio.doClick();
        chkAspectRatio.setEnabled(false);
        if (chkTeletextSubtitles.isSelected()) chkTeletextSubtitles.doClick();
        chkTeletextSubtitles.setEnabled(false);
        var card = (CardLayout) sourceCardPanel.getLayout();
        card.show(sourceCardPanel, "textbox");
    }
    
    private void checkMode() {
        if (cmbMode.getSelectedIndex() == -1) return;
        var mode = (ModeInfo) cmbMode.getSelectedItem();
        var od = (ComboBoxOption) cmbOutputDevice.getSelectedItem();
        boolean noRf = od != null && (od.value().equals("fl2k") || od.value().equals("file"));
        if (mode == null) return;
        cmbBand.removeAllItems();
        int up = mode.getUhfPlans().length;
        int vp = mode.getVhfPlans().length;
        int sp = mode.getSatellitePlans().length;
        if (up > 0) cmbBand.addItem(UHF_BAND);
        if (vp > 0) cmbBand.addItem(VHF_BAND);
        if (sp > 0) cmbBand.addItem(SATELLITE_BAND);
        var mod = mode.modulation();
        if (mod != UNMODULATED) cmbBand.addItem(CUSTOM_FREQUENCY);
        int c = cmbBand.getItemCount();
        if (noRf) {
            disableRFOptions();
        } else if (chkLockFrequency.isSelected()) {
            cmbBand.setEnabled(false);
            cmbBand.setSelectedItem(CUSTOM_FREQUENCY);
        } else if (c > 1) {
            cmbBand.setEnabled(true);
            cmbBand.setSelectedIndex(0);
            lblRegion.setEnabled(true);
            cmbRegion.setEnabled(true);
            cmbChannel.setEnabled(true);
        } else if (c == 1 && mod != UNMODULATED) {
            disableRFOptions();
        } else {
            cmbBand.setEnabled(false);
            cmbBand.removeAllItems();
            lblRegion.setEnabled(false);
            cmbRegion.setEnabled(false);
            cmbRegion.removeAllItems();
            cmbChannel.setEnabled(false);
            cmbChannel.removeAllItems();
        }
        // Populate labels
        String na = "n/a"; // This string is used if the underlying data is null
        String linesValue = mode.lines().toString();
        Double fieldRateValue = mode.fieldRate();
        String fieldRate;
        if (fieldRateValue == null) {
            fieldRate = na;
        } else {
            fieldRate = String.valueOf(fieldRateValue / 2 + "/" + fieldRateValue);
        }
        String videoModValue = null;
        if (videoModValue == null) videoModValue = na;
        switch(mode.modulation()) {
            case VSB -> videoModValue = "AM-VSB";
            case FM -> videoModValue = "FM";
            case UNMODULATED -> videoModValue = "Unmodulated";
        }
        String audioModValue = null;
        var am = mode.audioModulation();
        if (am != null) {
            switch (am) {
                case AM_AUDIO -> audioModValue = "AM";
                case FM_AUDIO -> audioModValue = "FM";
                case DIGITAL_AUDIO -> audioModValue = "Digital";
                case NO_AUDIO -> audioModValue = "No audio";
                default -> {
                }
            }
        }
        if (audioModValue == null) audioModValue = na;
        String audioCarrier;
        Long audioCarrierFrequency = mode.audioCarrierFrequency();
        if (audioCarrierFrequency == null) {
            audioCarrier = na;
        } else {
            audioCarrier = String.format("%.2f MHz", audioCarrierFrequency / 1000000.0);
        }
        lblLinesValue.setText(linesValue);
        lblFieldRateValue.setText(fieldRate);
        lblVideoModValue.setText(videoModValue);
        lblAudioModValue.setText(audioModValue);
        lblAudioSpacingValue.setText(audioCarrier);
        // Enable SECAM field ID by default, as requested
        if (mode.colourMode() == ColourMode.SECAM) {
            chkSecamId.setEnabled(true);
            chkSecamId.doClick();
        } else {
            if (chkSecamId.isSelected()) chkSecamId.doClick();
            chkSecamId.setEnabled(false);
        }
        // Save the line count from the previously selected mode
        int oldLines = mode.lines();
        boolean baseband = false;
        switch (mode.modulation()) {
            case VSB -> {
                if (chkFmFilter.isEnabled()) {
                    chkFmFilter.setSelected(false);
                    chkFmFilter.setEnabled(false);
                }
                chkVsbFilter.setEnabled(true);
                if (!chkSwapIQ.isEnabled()) chkSwapIQ.setEnabled(true);
                if (!chkAmp.isEnabled() && !noRf) chkAmp.setEnabled(true);
                disableFMDeviation();
            }
            case FM -> {
                if (chkVsbFilter.isEnabled()) {
                    chkVsbFilter.setSelected(false);
                    chkVsbFilter.setEnabled(false);
                }
                chkFmFilter.setEnabled(true);
                if (!chkSwapIQ.isEnabled()) chkSwapIQ.setEnabled(true);
                if (!chkAmp.isEnabled() && !noRf) chkAmp.setEnabled(true);
                enableFMDeviation();
            }
            case UNMODULATED -> {
                if (!checkBasebandSupport()) return;
                baseband = true;
            }
            default -> {
                messageBox("No modulation specified, defaulting to VSB.", JOptionPane.INFORMATION_MESSAGE);
                if (!chkFmFilter.isEnabled()) chkFmFilter.setEnabled(true);
                disableFMDeviation();
            }
        }
        if (mode.sampleRate() != null) {
            if (isVisible() && (od != null && od.value().equals("hackrf")) &&
                    PREFS.getInt("hackdac", 0) == 1 && baseband) {
                // HackDAC works at 13.5 MHz only 
                defaultSampleRate = "13.5";
                txtSampleRate.setEnabled(false);
            } else {
                defaultSampleRate = Shared.longToDecimal(mode.sampleRate());
                if (!txtSampleRate.isEnabled()) txtSampleRate.setEnabled(true);
            }
        } else {
            messageBox("No sample rate specified, defaulting to 16 MHz.", JOptionPane.INFORMATION_MESSAGE);
            defaultSampleRate = "16";
            if (!txtSampleRate.isEnabled()) txtSampleRate.setEnabled(true);
        }
        if ( mode.colourMode() != ColourMode.NONE) {
            chkColour.setEnabled(true);
            chkColour.setSelected(true);
        } else {
            if (chkColour.isSelected()) chkColour.setSelected(false);
            chkColour.setEnabled(false);
        }
        if (mode.audio()) {
            enableAudioOption();
        }  else {
            disableAudioOption();
        }
        if (mode.nicam()) {
            enableNICAM();
        } else {
            disableNICAM();
        }
        if (mode.a2()) {
            enableA2Stereo();
        } else {
            disableA2Stereo();
        }
        if (mode.teletext()) {
            enableTeletext();
        } else {
            disableTeletext();
        }
        setWSS(mode.wss());
        if (mode.vits()) {
            enableVITS();
        } else {
            disableVITS();
        }
        setDefaultAudioMode(mode);
        if ( (mode.colourMode() != ColourMode.MAC) && ((mode.lines() == 625) || (mode.lines() == 525)) ) {
            chkVITC.setEnabled(true);
        } else {
            if (chkVITC.isSelected()) chkVITC.doClick();
            chkVITC.setEnabled(false);
        }
        Shared.toggleCheckBox(chkACP, mode.acp());
        if (mode.scrambling()) {
            enableScrambling();
            if (mode.colourMode() == ColourMode.PAL || mode.colourMode() == ColourMode.SECAM) {
                add625ScramblingTypes();
            }
            else if (mode.colourMode() == ColourMode.MAC) {
                addMACScramblingTypes();
            }
        } else {
            disableScrambling();
        }
        if (mode.colourMode() == ColourMode.MAC) {
            bgAudio.clearSelection();
            radNoAudio.setEnabled(false);
            radMono.setEnabled(false);
            audioPanel.setEnabled(false);
        } else {
            radNoAudio.setEnabled(true);
            audioPanel.setEnabled(true);
        }
        if (mode.lines() == 625) {
            chkSiS.setEnabled(true);
        } else {
            if (chkSiS.isSelected()) chkSiS.doClick();
            chkSiS.setEnabled(false);
        }
        // Enable S-Video option for baseband modes for FL2K and file output
        if ( (baseband) && ( (cmbOutputDevice.getSelectedIndex() == 2) ||
                (cmbOutputDevice.getSelectedIndex() == 3) ) &&
                ( (mode.colourMode() == ColourMode.PAL) ||
                    (mode.colourMode() == ColourMode.NTSC) ||
                    (mode.colourMode() == ColourMode.SECAM) ) ) {
            chkSVideo.setEnabled(true);
        } else {
            if (chkSVideo.isSelected()) chkSVideo.doClick();
            chkSVideo.setEnabled(false);
        }
        if (mode.colourMode() != ColourMode.MAC && (mode.lines() == 625 || mode.lines() == 525)) {
            chkCC608.setEnabled(true);
        } else {
            if (chkCC608.isSelected()) chkCC608.doClick();
            chkCC608.setEnabled(false);
        }
        // If the colour system (PAL/NTSC/SECAM) or line count varies from the previous mode...
        if ( (mode.colourMode() != prevColour) || (oldLines != mode.lines())) {
            // ...refresh the available test cards
            addTestCardOptions();
            if (cmbTest.isEnabled() && cmbTest.getItemCount() > 0) cmbTest.setSelectedIndex(0);
        }
        // Save the current colour system to prevColour so we can recall this later
        prevColour = mode.colourMode();
    }
    
    private void setDefaultAudioMode(ModeInfo m) {
        // If the mode supports both NICAM and A2 stereo, choose the default, according to defaultmode
        // If the mode only supports one or the other, and defaultmode is defined, go with that
        // If the mode only supports one or the other, and defaultmode is not defined, use the supported stereo option
        if (m == null) return;
        var defaultMode = m.defaultAudioMode();
        boolean nicam = m.nicam();
        boolean a2 = m.a2();
        if (defaultMode == null) {
            // No defaultmode defined
            if (nicam && a2) {
                // Choose mono, don't play favourites
                radMono.setSelected(true);
            } else if (nicam) {
                radNICAM.setSelected(true);
            } else if (a2) {
                radA2Stereo.setSelected(true);
            }
            return;
        }
        switch (defaultMode) {
            case MONO -> {
                if (!radMono.isEnabled()) radMono.setSelected(true);
            }
            case NICAM -> {
                if (radNICAM.isEnabled()) radNICAM.setSelected(true);
            }
            case A2 -> {
                if (radA2Stereo.isEnabled()) radA2Stereo.setSelected(true);
            }
        }
    }
    
    private void enableAudioOption() {
        radMono.setEnabled(true);
        if (!radMono.isSelected()) radMono.doClick();
    }
    
    private void disableAudioOption() {
        radNoAudio.setSelected(true);
        radMono.setSelected(false);
        radMono.setEnabled(false);
    }
    
    private boolean checkBasebandSupport() {
        // Check if the selected output device supports baseband modes or not.
        var outputDevice = (ComboBoxOption) cmbOutputDevice.getSelectedItem();
        if ( (outputDevice.value().equals("hackrf") && (PREFS.getInt("hackdac", 0) == 1)) ||
                outputDevice.value().equals("fl2k") ||
                outputDevice.value().equals("file") ) {
            disableRFOptions();
            if (chkFmFilter.isSelected()) chkFmFilter.doClick();
            chkFmFilter.setEnabled(false);
            if (chkSwapIQ.isSelected()) chkSwapIQ.doClick();
            chkSwapIQ.setEnabled(false);
            return true;
        } else {
            String err = "This mode is not supported by the selected output device.";
            if (cmbOutputDevice.getSelectedIndex() == 0) {
                err += "\nIf you have a HackDAC board, enable HackDAC support on the Output tab.";
            }
            messageBox(err, JOptionPane.WARNING_MESSAGE);
            if (cmbMode.getSelectedIndex() != previousIndex) {
                cmbMode.setSelectedIndex(previousIndex);
                checkMode();
                return false;
            } else {
                // Fallback for when the previousIndex value matches the current mode
                // This would cause an error message loop otherwise
                for (int i = 0; i < modes.size(); i++) {
                    if (modes.get(i).modulation() == VSB ||
                            modes.get(i).modulation() == FM) {
                        var co = new ColourOption(modes.get(i).colourMode(), modes.get(i).colourMode().toString());
                        lstColour.setSelectedValue(co, true);
                        cmbMode.setSelectedItem(modes.get(i).modeId());
                        return true;
                    }
                }
                // No VSB or FM mode found. This is fatal, we can't continue.
                throw new IllegalStateException("Fatal error: unable to find a suitable mode to revert to.");
            }
        }
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
            chkTeletextSubtitles.setEnabled(true);
        }
    }  
    
    private void disableTeletext() {
        if (chkTeletext.isSelected()) {
            chkTeletext.doClick();
        } 
        chkTeletext.setEnabled(false);
        teletextPanel.setEnabled(false);
        if (chkTeletextSubtitles.isSelected()) chkTeletextSubtitles.doClick();
        chkTeletextSubtitles.setEnabled(false);
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
                messageBox("Please specify a directory that contains teletext files, or a teletext archive file.", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            else if ( (txtTeletextSource.getText().toLowerCase(Locale.ENGLISH).endsWith(".t42")) 
                    && (isWindows) && (txtTeletextSource.getText().matches(".*\\s.*")) ) {
                al.add("raw:" + '\u0022' + txtTeletextSource.getText() + '\u0022');
            }
            else if (txtTeletextSource.getText().toLowerCase(Locale.ENGLISH).endsWith(".t42")) {
                al.add("raw:" + txtTeletextSource.getText());
            }
            else if ( (isWindows) && (txtTeletextSource.getText().matches(".*\\s.*")) ) {
                al.add('\u0022' + txtTeletextSource.getText() + '\u0022');
            }
            else {
                al.add(txtTeletextSource.getText());
            }
            if ( (chkTeletextSubtitles.isSelected()) && (!txtTeletextSource.getText().isBlank()) ) {
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
                if ( (Shared.wildcardFind(txtTeletextSource.getText(), "p8", ".tti") > 0) || 
                        (Shared.wildcardFind(txtTeletextSource.getText(), "p8", ".ttix") > 0) ) {
                    if (!silent) messageBox(p888warn, JOptionPane.WARNING_MESSAGE);
                    return al;
                }
            }
        }
        return al;
    }
    
    private void enableNICAM() {
        if (radMono.isSelected()) {
            radNICAM.setEnabled(true);
        }
    }
       
    private void disableNICAM() {
        radNICAM.setEnabled(false);
        radNICAM.setSelected(false);
    }
    
    private void enableA2Stereo() {
        if (radMono.isEnabled()) {
            radA2Stereo.setEnabled(true);
        }
    }
       
    private void disableA2Stereo() {
        radA2Stereo.setEnabled(false);
        radA2Stereo.setSelected(false);
    }    
    
    private void enableFMDeviation() {
        chkFMDev.setEnabled(true);
        // The --filter parameter enables VSB filtering on AM, or CCIR-405 FM 
        // pre-emphasis filtering on FM, so change the Filter checkbox
        // description to suit  
        chkFmFilter.setEnabled(true);
        if (chkVsbFilter.isSelected()) chkVsbFilter.setEnabled(false);
        chkVsbFilter.setEnabled(false);
    }
    
    private void disableFMDeviation() {
        if (chkFMDev.isSelected()) chkFMDev.doClick();
        chkFMDev.setEnabled(false);
        txtFMDev.setText("");
        txtFMDev.setEnabled(false);
        txtFMDev.setEditable(false);
        if (chkFmFilter.isSelected()) {
            chkFmFilter.setSelected(false);
            txtSampleRate.setText(defaultSampleRate);
        }
        chkVsbFilter.setEnabled(true);
    }
        
    private void youtubedl(String input) {
        // yt-dlp frontend. Pass the download URL as a string.
        // youtube-dl is no longer supported
        if (JOptionPane.showConfirmDialog(null, """
                                                We will now attempt to use yt-dlp to stream the requested video.
                                                Do you wish to continue?""",
                Shared.APP_NAME,
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION) {
            final String ytp;
            String url;
            if (isWindows) {
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
        if (!playlistModel.isEmpty()) return "";
        if ( (radLocalSource.isSelected()) || (isPhilipsTestSignal())) {
            if (cmbM3USource.isVisible()) {
                return ((ComboBoxOption) cmbM3USource.getSelectedItem()).value();
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
                    messageBox("Please specify an input file to broadcast or choose the test card option.", JOptionPane.WARNING_MESSAGE);
                    return null;
                } else if (((TestSignalOption) cmbTest.getSelectedItem()).command().equals("colourbars")) {
                    // Return an empty string on standard hacktv bars, this is set elsewhere
                    return "";
                } else {
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
        if (Shared.isNumeric( txtSampleRate.getText())) {
            Double SR = Double.valueOf(txtSampleRate.getText());
            return (int) (SR * 1000000);
        }
        else {
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
                messageBox("Please specify a valid offset in MHz.", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        return al;
    }
   
    private ArrayList<String> checkFMDeviation() {
        var al = new ArrayList<String>();
        if (chkFMDev.isSelected()) {
            if (Shared.isNumeric(txtFMDev.getText())) {
                al.add("--deviation");
                Double d = Double.valueOf(txtFMDev.getText());
                int i = (int) (d * 1000000);
                al.add(Integer.toString(i));
            }
            else {
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
        var mode = (ModeInfo) cmbMode.getSelectedItem();
        var band = cmbBand.getSelectedItem();
        if (mode.modulation() != FM) return inputFreq;
        // This is the value we'll return if an error is found
        long errValue = Long.MIN_VALUE + 256;
        int lnbType = PREFS.getInt("rxdevice", 0);
        long f;
        switch (lnbType) {
            case 1 -> // Reception from a Ku band LNB
                // Divide Ku frequency by the chosen harmonic
                f = inputFreq / getHarmonic();
            case 2 -> {
                // Reception from a standard Ku band LNB using a BSB receiver
                // Recalculate the transmission frequency based on the IF
                long bsbLO = 10_769_180_000L; // Standard LO of BSB Squarials/LNBs
                long vlo = (long) (PREFS.getDouble("localoscillator", DEFAULT_LO) * 1_000_000_000);
                f = (inputFreq - bsbLO + vlo) / getHarmonic();
            }
            case 3 -> {
                /* Saorsat Ka band LNB mode
                These LNBs aren't fully supported on the receivers that we're
                targeting, you can't enter a 21.2 GHz LO. So we do some trickery
                to calculate the first harmonic. Negate the Ku-band frequency
                and add it to the Ka LO and the Ku LO.
                As the Ka LO is higher than the input frequency, the resulting
                IF is inverted. You should use the "Invert video" option to
                cancel this out.
                
                An example of this LNB can be found at:
                https://www.inverto.tv/lnb/130/twin-ka-circular-dual-polarity-lnb23mm-197-202ghz-lo212o-ghz
                */
                long kaLO = 21_200_000_000L;
                f = (-inputFreq + kaLO + getLO()) / getHarmonic();
            }
            default -> {
                // Direct reception from the HackRF, no LNB
                f = (inputFreq - getLO()) / getHarmonic();
                // Is this the first harmonic?
                boolean firstHarmonic = getHarmonic() == 1;
                // Is "apply LO to custom frequencies" enabled?
                boolean applyLO = PREFS.getInt("applyloforcustomfreq", 0) == 1;
                // Is the frequency not between 950 and 2150 MHz
                boolean outOfRange = f < 950_000_000L || f > 2_150_000_000L;
                if (!silent && firstHarmonic && (!band.equals(CUSTOM_FREQUENCY) || applyLO) && outOfRange) {
                    int q = JOptionPane.showConfirmDialog(null,
                            """
                            This frequency may be outside of your receiver's tuning range.
                            Would you like to continue anyway?""",
                            Shared.APP_NAME,
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );
                    if (q == JOptionPane.NO_OPTION) return errValue;
                }
            }
        }
        if (f > 7_250_000_000L || f < 1_000_000) {
            System.err.println("Frequency of first harmonic (" + f + ") is invalid.");
            if (!silent) messageBox("""
                                    The current configuration is not supported by the HackRF device.
                                    Please try a different frequency.""",
                    JOptionPane.WARNING_MESSAGE
            );
            return errValue;
        }
        return f;
    }
    
    private long getLO() {
        // Returns the local oscillator frequency to be used in calculating
        // the IF or harmonic frequency. Only run on satellite modes.
        var mode = (ModeInfo) cmbMode.getSelectedItem();
        var band = cmbBand.getSelectedItem();
        if (mode.modulation() != FM) return 0;
        // If "Apply these settings to custom frequencies" is disabled, and
        // the Custom radio button is selected, return zero.
        if ( PREFS.getInt("applyloforcustomfreq", 0) == 0 &&
                band.equals(CUSTOM_FREQUENCY) ) return 0;
        // Check first if there's a hardcoded LO in the band plan.
        // This will override any user-defined LO.
        if (cmbRegion.getItemCount() > 0) {
            var bp = (BandPlan) cmbRegion.getSelectedItem();
            Long lo = bpIni.getLong(bp.id(), "lo");
            if (lo != null) return lo;
        }
        // Import from preferences
        Double plo = PREFS.getDouble("localoscillator", DEFAULT_LO);
        // Convert imported LO from GHz to Hz.
        return (long) (plo * 1000000000);
    }
    
    private int getHarmonic() {
        final int defaultValue = 1;
        // Only run on satellite modes
        var band = cmbBand.getSelectedItem();
        if (band != null && band.equals(SATELLITE_BAND)) return defaultValue;
        // Get harmonic setting
        int h = PREFS.getInt("harmonic", defaultValue);
        if (h >= 1 && h <= 4) return h;
        return defaultValue;
    }
    
    private boolean checkCustomFrequency(){
        var band = (String) cmbBand.getSelectedItem();
        var mode = (ModeInfo) cmbMode.getSelectedItem();
        if (band != null && band.equals(CUSTOM_FREQUENCY)) {
            boolean sat = mode.getSatellitePlans() != null;
            boolean s = false;
            if (sat && PREFS.get("applyloforcustomfreq", "0").equals("1")) {
                s = true;
            }
            BigDecimal CustomFreq;
            var Multiplier = new BigDecimal(1000000);
            String InvalidInput = "Please specify a frequency between 1 MHz and 7250 MHz.";
            String SatHint = """
                             
                             If you're trying to use a frequency for a satellite receiver, enable the 
                             "Apply these settings for custom frequencies" option in "Satellite receiver
                             settings" on the GUI Settings tab.""";
            if (Shared.isNumeric(txtFrequency.getText().trim())){
                CustomFreq = new BigDecimal(txtFrequency.getText().trim());
                if ( (!s) && ( (CustomFreq.longValue() < 1) || (CustomFreq.longValue() > 7250) ) ) {
                    if (sat) {
                        messageBox(InvalidInput + SatHint, JOptionPane.WARNING_MESSAGE);
                    }
                    else {
                        messageBox(InvalidInput, JOptionPane.WARNING_MESSAGE);
                    }
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
                return false;  
            }
        }
        return true;
    }
    
    private ArrayList<String> checkMacChId() {
        var al = new ArrayList<String>();
        if (macSettings == null) return al;
        String chid = macSettings.channelID();
        if (chid != null && !chid.isBlank()) {
            if (chid.matches("^[0-9a-fA-F]+$")) {
                al.add("--chid");
                al.add("0x" + chid);
            }
            else {
                messageBox("Please specify a valid hexadecimal channel ID.", JOptionPane.WARNING_MESSAGE);
                return null;
            }            
        }
        return al;
    }
    
    private ArrayList<String> checkGamma() {
        var al = new ArrayList<String>();
        if (chkGamma.isSelected()) {
            if (Shared.isNumeric(txtGamma.getText())) {
                al.add("--gamma");
                al.add(txtGamma.getText());
            }
            else {    
                messageBox("Gamma should be a decimal value.", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        return al;
    }
    
    private ArrayList<String> checkOutputLevel() {
        var al = new ArrayList<String>();
        if (chkOutputLevel.isSelected()) {
            if (Shared.isNumeric(txtOutputLevel.getText())) {
                al.add("--level");
                al.add(txtOutputLevel.getText());
            }
            else {
                messageBox("Output level should be a decimal value.", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        return al;
    }
    
    private ArrayList<String> checkPosition() {
        var al = new ArrayList<String>();
        if (chkPosition.isSelected()) {
            if (!Shared.isNumeric(txtPosition.getText())) {
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
        if (Shared.isNumeric(txtGain.getText())) {
            int g = Integer.parseInt(txtGain.getText());
            if ( (g >= 0) && (g <= 47) ) {
                al.add("-g");
                al.add(txtGain.getText());
            }
            else {
                messageBox(InvalidGain, JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        else {
            messageBox(InvalidGain, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return al;
    }
    
    private void checkTestCardStatus() {
        if ( (!cmbTest.isEnabled())
                && (!htvLoadInProgress)
                && cmbTest.getItemCount() > 1 ) {
            // Enable cmbTest (test card dropdown)
            cmbTest.setEnabled(true);
            cmbTest.setSelectedIndex(0);
        }
        else if (htvLoadInProgress) {
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
        var m = (ComboBoxOption) cmbOutputDevice.getSelectedItem();
        switch (m.value()) {
            case "file" -> {
                // If File is selected, check if the path is blank
                if (txtOutputDevice.getText().isBlank()) {
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
                } else {
                    al.add("-o");
                    if ( (isWindows) && txtOutputDevice.getText().contains(" ") ) {
                        al.add('\u0022' + txtOutputDevice.getText() + '\u0022');
                    } else {
                        al.add(txtOutputDevice.getText());
                    }
                    if (cmbFileType.getSelectedIndex() != 3) {
                        al.add ("-t");
                        al.add(cmbFileType.getItemAt(cmbFileType.getSelectedIndex()));
                    }
                }
            }
            case "fl2k" -> {
                // fl2k
                al.add("-o");
                if (!txtOutputDevice.getText().isBlank()) {
                    al.add("fl2k:" + txtOutputDevice.getText());
                } else {
                    al.add("fl2k");
                }
                // fl2k audio
                var fa = (ComboBoxOption) cmbFl2kAudio.getSelectedItem();
                if (!fa.value().isEmpty()) {
                    al.add("--fl2k-audio");
                    al.add(fa.value());
                }
            }
            case "soapysdr" -> {
                // SoapySDR
                al.add("-o");
                if (!txtOutputDevice.getText().isBlank()) {
                    al.add("soapysdr:" + txtOutputDevice.getText());
                } else {
                    al.add("soapysdr");
                }
                if (!txtAntennaName.getText().isBlank()) {
                    al.add("--antenna");
                    al.add(txtAntennaName.getText());
                }
            }
            case "hackrf" -> {
                // HackRF
                if (!txtOutputDevice.getText().isBlank()) {
                    al.add("-o");
                    al.add("hackrf:" + txtOutputDevice.getText());
                }
            }
            default -> {
                // This should never run
            }
        }
        return al;
    }
    
    private ArrayList<String> checkVolume() {
        var al = new ArrayList<String>();
        // Only check volume if the option is enabled
        if (chkVolume.isSelected()) {
            if (Shared.isNumeric(txtVolume.getText())) {
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
        if (macSettings.audioMode()) al.add("--mac-audio-mono");
        if (macSettings.audioQuality()) al.add("--mac-audio-medium-quality");
        if (macSettings.audioCompression()) al.add("--mac-audio-linear");
        if (macSettings.audioProtection()) al.add("--mac-audio-l2-protection");
        return al;
    }
    
    private String getYtDlpPath() {
        // This method attempts to find yt-dlp on *nix by checking some common
        // locations, as well as by retrieving paths that were defined in
        // terminal configuration files in the user's home directory.
        if (isWindows) return ""; // Not required on Windows
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
    
    private boolean applyTemplate(String mode, ColourMode colour, String regionLookup, Channel channel) {
        if (mode == null || colour == null || regionLookup == null || channel == null) return false;
        Stream<ModeInfo> stream = modes.stream().filter(m -> m.modeId().equals(mode));
        var streamQuery = stream.findFirst();
        var s = streamQuery == null ? null : streamQuery.get();
        if (s != null) {
            // Select mode
            lstColour.setSelectedValue(new ColourOption(colour, colour.toString()), true);
            cmbMode.setSelectedItem(s);
        } else {
            messageBox("Unable to find the '" + mode + "' mode, which is required for this template.", JOptionPane.ERROR_MESSAGE);
            resetAllControls();
            return false;
        }
        // Find the band plan
        var m = (ModeInfo) cmbMode.getSelectedItem();
        var band = m.getSatellitePlan(regionLookup);
        cmbRegion.setSelectedItem(band);
        var r = (BandPlan) cmbRegion.getSelectedItem();
        int ind = r.channels().indexOf(channel);
        if (ind != -1) {
            // Set correct channel
            cmbChannel.setSelectedIndex(ind);
            var probe = (Channel) cmbChannel.getSelectedItem();
            if (!probe.equals(channel)) {
                // Use a custom frequency instead
                cmbBand.setSelectedItem("Custom");
                var df2 = new DecimalFormat("0.00");
                txtFrequency.setText(df2.format((double) channel.frequency() / 1000000));
            }
        } else {
            messageBox("Unable to find the channel '" + channel.name() + "' for this template.", JOptionPane.ERROR_MESSAGE);
            resetAllControls();
            return false;
        }
        // Enable pre-emphasis filter and enable FM deviation option
        chkFmFilter.doClick();
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
        return true;
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
        var mode = (ModeInfo) cmbMode.getSelectedItem();
        // Video mode
        allArgs.add("-m");
        allArgs.add(mode.modeId());
        // Only add frequency for HackRF (not in baseband mode) or SoapySDR
        var od = (ComboBoxOption) cmbOutputDevice.getSelectedItem();
        boolean bb = mode.modulation() == UNMODULATED;
        if ((od.value().equals("hackrf") && (!bb)) || od.value().equals("soapysdr")) {
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
        if (chkTeletextSubtitles.isSelected()) {
            allArgs.add("--tx-subtitles");
            if (!txtTeletextSubtitleIndex.getText().isEmpty()) {
                allArgs.add(txtTeletextSubtitleIndex.getText());
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
            // Default is 9, so we don't need to send an argument for it
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
        if (mode.colourMode() == ColourMode.MAC) {
            if (checkMacChId() == null) return;
            allArgs.addAll(checkMacOptions());
            allArgs.addAll(checkMacChId());
        }
        if (radNoAudio.isSelected() && radMono.isEnabled()) {
            allArgs.add("--noaudio");
        }
        else if ( (radNICAM.isEnabled())
                && (!radNICAM.isSelected()) 
                && (!radA2Stereo.isSelected()) ) {
            allArgs.add("--nonicam");
        }
        else if (radA2Stereo.isSelected()) {
            allArgs.add("--a2stereo");
        }
        if (chkACP.isSelected()) allArgs.add("--acp");
        if (chkRepeat.isSelected()) allArgs.add("--repeat");
        if (chkInterlace.isSelected()) allArgs.add("--interlace");
        allArgs.addAll(checkScrambling());
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
        if (chkVsbFilter.isSelected() || chkFmFilter.isSelected()) allArgs.add("--filter");
        //if (chkVerbose.isSelected()) allArgs.add("--verbose");
        if (chkVITS.isSelected()) allArgs.add("--vits");
        if (chkVITC.isSelected()) allArgs.add("--vitc");
        if (chkColour.isEnabled() && !chkColour.isSelected()) allArgs.add("--nocolour");
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
            if (playlistModel.isEmpty() && radTest.isSelected()) {
                if (checkTestCard() != null) {
                    allArgs.addAll(checkTestCard());
                }
                else {
                    return;
                }
            }
            if (!playlistModel.isEmpty()) {
                if (chkRandom.isSelected()) {
                    // Set the start point as the first item
                    if (startPoint != -1) {
                        if ( (isWindows) && (playlistModel.get(startPoint).contains(" "))) {
                            allArgs.add('\u0022' + playlistModel.get(startPoint) + '\u0022');
                        }
                        else {
                            allArgs.add(playlistModel.get(startPoint));
                        }
                    }
                    RND.ints(0, playlistModel.size())
                        .distinct()
                        .limit(playlistModel.size())
                        .forEach(
                            r -> {
                                // Add the rest. except for the start point or test cards
                                if ( (!playlistModel.get(r).startsWith("test:")) && (r != startPoint) ) {
                                    if ( (isWindows) && (playlistModel.get(r).contains(" "))) {
                                        allArgs.add('\u0022' + playlistModel.get(r) + '\u0022');
                                    }
                                    else {
                                       allArgs.add(playlistModel.get(r));
                                    }
                                }
                            }
                        );
                }
                else {
                    // Move through playlistModel, starting at the value defined by startPoint.
                    // When we reach the end of the array, start again at zero until we
                    // reach playlistModel.size() minus one.
                    int i = startPoint;
                    int j = 0;
                    if (i == -1) i++;
                    while (j < playlistModel.size()) {
                        if ( (i == playlistModel.size()) && (startPoint != 0) ) {
                            i = 0;
                        }
                        if ( (playlistModel.get(i).startsWith("test:")) ||
                            (playlistModel.get(i).startsWith("http")) ) {
                            allArgs.add(playlistModel.get(i));
                        }
                        else {
                            if ( (isWindows) && playlistModel.get(i).contains(" ") ) {
                                allArgs.add('\u0022' + playlistModel.get(i) + '\u0022');
                            }
                            else {
                                allArgs.add(playlistModel.get(i));
                            }
                        }
                        i++;
                        j++;
                    }
                }  
            }
            else if ( (isWindows) && InputSource.contains(" ")) {
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
                    hacktvProcess = pb.start();
                    // Capture the output of hacktv
                    try (var br = new BufferedReader(new InputStreamReader(hacktvProcess.getInputStream(), StandardCharsets.UTF_8))) {
                        // Create a 4096 byte buffer to improve performance.
                        char[] buffer = new char[4096];
                        int count;
                        while ((count = br.read(buffer)) != -1) {
                            // Publish the buffer
                            publish(new String(buffer, 0, count));
                        }
                        publish("hacktv stopped");
                    }
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
                // If an invalid parameter is passed to hacktv, it usually
                // responds with its usage message.
                // Here, we check if the first line of the usage has been
                // returned. If so, we assume that one of the parameters we fed 
                // is not supported.
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
                for (String chunk : chunks) {
                    // Iterate through the buffer for CR handling
                    for (int i = 0; i < chunk.length(); i++) {
                        String c = String.valueOf(chunk.charAt(i));
                        if (c.equals("\r")) {
                            cr = true;
                        } else if (cr && !c.equals("\n")) {
                            String s = txtConsoleOutput.getText();
                            txtConsoleOutput.replaceRange(
                                    c,
                                    s.lastIndexOf("\n") + 1,
                                    s.length());
                            cr = false;
                        } else {
                            txtConsoleOutput.append(c);
                            cr = false;
                        }
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
        if (isWindows) {
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
                    Process y = p.get(0);
                    // Get the hacktv process
                    hacktvProcess = p.get(1);
                    // Capture the output of hacktv
                    try (var br = new BufferedReader(new InputStreamReader(hacktvProcess.getInputStream(), StandardCharsets.UTF_8))) {
                        char[] buffer = new char[4096];
                        int count;
                        while ((count = br.read(buffer)) != -1) {
                            publish(new String(buffer, 0, count));
                        }
                        publish("hacktv stopped");
                    }
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
                for (String chunk : chunks) {
                    for (int i = 0; i < chunk.length(); i++) {
                        String c = String.valueOf(chunk.charAt(i));
                        if (c.equals("\r")) {
                            cr = true;
                        } else if (cr && !c.equals("\n")) {
                            String s = txtConsoleOutput.getText();
                            txtConsoleOutput.replaceRange(
                                    c,
                                    s.lastIndexOf("\n") + 1,
                                    s.length());
                            cr = false;
                        } else {
                            txtConsoleOutput.append(c);
                            cr = false;
                        }
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
                // If an invalid parameter is passed to hacktv, it usually
                // responds with its usage message.
                // Here, we check if the first line of the usage has been
                // returned. If so, we assume that one of the parameters we fed 
                // is not supported.
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
    
    private void stopTV() {
        /** To stop hacktv gracefully, it needs to be sent one of the signals
         *  defined in hacktv.c: SIGINT, SIGILL, SIGFPE, SIGSEGV, SIGTERM, SIGABRT.
         * 
         *  If the JRE supports normal termination, just destroy the process handle.
         *  This will send a SIGTERM (15).
         * 
         *  Under Windows it's not so easy. We need to use the
         *  GenerateConsoleCtrlEvent API, so we have implemented this
         *  using a JNI module.
         */
        if (hacktvProcess == null || !hacktvProcess.isAlive()) return;
        String err = "";
        if (hacktvProcess.supportsNormalTermination()) {
            hacktvProcess.toHandle().destroy(); // Process.destory() also closes the I/O streams
        } else if (isWindows && ConsoleCtrlJNI.isInitialised()) {
            try {
                ConsoleCtrlJNI.sendCtrlC(hacktvProcess.pid());
            } catch (UnsatisfiedLinkError e) {
                System.err.println(e);
                err = "An error occurred when attempting to stop hacktv:\n" + e.getMessage() + "\n";
            }
        }
        // Start a watchdog timer in a new thread to ensure that hacktv has stopped
        // If not, then use destroyForcibly()
        final String stopErr = err;
        Thread.ofVirtual().start(() -> {
            try {
                if (!hacktvProcess.waitFor(5, TimeUnit.SECONDS)) {
                    hacktvProcess.destroyForcibly();
                    SwingUtilities.invokeLater(() -> {
                        String message = stopErr;
                        if (message.isEmpty()) {
                            message = "Unable to stop hacktv gracefully.\n";
                        }
                        messageBox(message +
                                """
                                The process has been forcibly terminated.
                                This may require a reset of your SDR device.""",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
        
    private void preRunTasks() {
        btnRun.setText("Stop hacktv");
        chkSyntaxOnly.setEnabled(false);
        btnHackTVPath.setEnabled(false);
        if (isWindows) btnDownloadHackTV.setEnabled(false);
        running = true;
    }
    
    private void postRunTasks() {
        btnRun.setText("Run hacktv");
        if (!btnRun.isEnabled()) {
            btnRun.setEnabled(true);
            btnRun.requestFocusInWindow();
        }
        chkSyntaxOnly.setEnabled(true);
        btnHackTVPath.setEnabled(true);
        if (isWindows) btnDownloadHackTV.setEnabled(true);
        running = false;
    }
    
    private void cleanupBeforeExit() {
        // Check if a teletext download is in progress
        // If so, then abort
        if (downloadInProgress) downloadCancelled = true;
        // Check if hacktv is running, if so then exit it
        if (running) {
            stopTV();
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

    private void btnHideConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHideConsoleActionPerformed
        boolean visible = !consolePanel.isVisible();
        consolePanel.setVisible(visible);
        btnHideConsole.setText(visible ? "Hide console" : "Show console");
        pack();
    }//GEN-LAST:event_btnHideConsoleActionPerformed

    private void lstColourValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstColourValueChanged
        // Prevent the event from firing on mouse down.
        if (evt.getValueIsAdjusting()) return;
        cmbMode.removeAllItems();
        var listValue = lstColour.getSelectedValue();
        if (listValue == null) return;
        btnMacOptions.setEnabled(listValue.colourMode() == ColourMode.MAC);
        var filtered = modes.stream()
                .filter(m -> m.colourMode() == listValue.colourMode())
                .toList();
        for (var mode : filtered) {
            cmbMode.addItem(mode);
        }
    }//GEN-LAST:event_lstColourValueChanged

    private void btnTestSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestSettingsActionPerformed
        // Show the setting dialogue box
        var td = new TestSettingsDialogue(this, true, hackTVDirectory);
        td.setVisible(true);
        if (td.settingsChanged()) {
            addTestCardOptions();
            if (radTest.isSelected()) radTest.doClick();
        }
    }//GEN-LAST:event_btnTestSettingsActionPerformed

    private void btnSourceBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSourceBrowseActionPerformed
        var sourceFileChooser = new SystemFileChooser();
        sourceFileChooser.setMultiSelectionEnabled(true);
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
                        playlistModel.addElement(fn.toString());
                    }
                }
            } else {
                var file = new File (Shared.stripQuotes(f[0].toString()));
                if ( (file.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(".m3u"))
                      || (file.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(".m3u8")) ) {
                    // If the source is an M3U file, pass it to the M3U handler
                    txtSource.setText(file.getAbsolutePath());
                    m3uHandler(file.getAbsolutePath());
                }
                else if (file.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(".htv")) {
                    // Don't try to process a file with a .HTV extension
                    messageBox("Configuration files should be opened from the File menu.", JOptionPane.WARNING_MESSAGE);    
                } else {
                    txtSource.setVisible(true);
                    cmbM3USource.setVisible(false);
                    cmbM3USource.setEnabled(false);
                    txtSource.setText(file.getAbsolutePath());
                }                
            }
        }
    }//GEN-LAST:event_btnSourceBrowseActionPerformed

    private void menuNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNewActionPerformed
        resetAllControls();
    }//GEN-LAST:event_menuNewActionPerformed

    private void menuOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenActionPerformed
        var configFileChooser = new SystemFileChooser();
        configFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        configFileChooser.addChoosableFileFilter(
                new SystemFileChooser.FileNameExtensionFilter("hacktv configuration file (*.htv)", "htv")
        );
        int result = configFileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = configFileChooser.getSelectedFile();
            selectedFile = new File(Shared.stripQuotes(selectedFile.toString()));
            checkSelectedFile(selectedFile);
        }
    }//GEN-LAST:event_menuOpenActionPerformed

    private void menuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveActionPerformed
        if (menuSave.getText().contains("...")) {
            saveFilePrompt();
        }
        else {
            saveConfigFile(selectedFile);
        }
    }//GEN-LAST:event_menuSaveActionPerformed

    private void menuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveAsActionPerformed
        saveFilePrompt();
    }//GEN-LAST:event_menuSaveAsActionPerformed

    private void menuMRUFile1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile1ActionPerformed
        selectedFile = new File(menuMRUFile1.getText());
        checkSelectedFile(selectedFile);
    }//GEN-LAST:event_menuMRUFile1ActionPerformed

    private void menuMRUFile2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile2ActionPerformed
        selectedFile = new File(menuMRUFile2.getText());
        checkSelectedFile(selectedFile);
    }//GEN-LAST:event_menuMRUFile2ActionPerformed

    private void menuMRUFile3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile3ActionPerformed
        selectedFile = new File(menuMRUFile3.getText());
        checkSelectedFile(selectedFile);
    }//GEN-LAST:event_menuMRUFile3ActionPerformed

    private void menuMRUFile4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMRUFile4ActionPerformed
        selectedFile = new File(menuMRUFile4.getText());
        checkSelectedFile(selectedFile);
    }//GEN-LAST:event_menuMRUFile4ActionPerformed

    private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
        dispose();
    }//GEN-LAST:event_menuExitActionPerformed

    private void menuAstraTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAstraTemplateActionPerformed
        if (JOptionPane.showConfirmDialog(null,
                """
                This will load template values for an Astra satellite receiver.
                All current settings will be cleared. Do you wish to continue?""",
                Shared.APP_NAME,
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION) {
        // Reset all controls
        resetAllControls();
        
        // Specify mode and channel parameters here
        String mode = "pal-fm";
        ColourMode colour = ColourMode.PAL;
        String regionLookup = "astra";
        String txp = "35";
        long freq = 10993750000L; // Transponder 35
        String chid = null;
        Channel channel = new Channel(txp, freq, chid);

        if (!applyTemplate(mode, colour, regionLookup, channel)) return;
        
        var df5 = new DecimalFormat("0.00000");
        String s = df5.format((double) freq / 1000000000);
        messageBox("Template values have been loaded. Tune your receiver to "
            + s + " GHz and run hacktv.", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_menuAstraTemplateActionPerformed

    private void menuBSBTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuBSBTemplateActionPerformed
        if (JOptionPane.showConfirmDialog(null,
                """
                This will load template values for a BSB satellite receiver.
                All current settings will be cleared. Do you wish to continue?""",
            Shared.APP_NAME,
            JOptionPane.YES_NO_OPTION
        ) == JOptionPane.NO_OPTION) return;
        // Reset all controls
        resetAllControls();
        
        // Specify mode and channel parameters here
        String mode = "dmac-fm";
        ColourMode colour = ColourMode.MAC;
        String regionLookup = "bsb";
        String txp = "3 (Galaxy)";
        long freq = 11861740000L;
        String chid = "70B2";
        var channel = new Channel(txp, freq, chid);
        
        if (!applyTemplate(mode, colour, regionLookup, channel)) return;
        
        messageBox("Template values have been loaded. Tune your receiver to the Galaxy "
            + "channel, or change this in the channel dropdown box on the Output tab.", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_menuBSBTemplateActionPerformed

    private void menuWikiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuWikiActionPerformed
        String u = "https://github.com/steeviebops/hacktv-gui/wiki";
        try {
            Shared.launchBrowser(u);
        }
        catch (IOException | UnsupportedOperationException e) {
            messageBox("Unable to launch default browser.", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_menuWikiActionPerformed

    private void menuGithubRepoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuGithubRepoActionPerformed
        String u = "https://github.com/steeviebops/hacktv-gui/";
        try {
            Shared.launchBrowser(u);
        }
        catch (IOException | UnsupportedOperationException e) {
            messageBox("Unable to launch default browser.", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_menuGithubRepoActionPerformed

    private void menuUpdateCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuUpdateCheckActionPerformed
        checkForUpdates(false);
    }//GEN-LAST:event_menuUpdateCheckActionPerformed

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
            Shared.APP_NAME +
            "\nBuild date: " + v +
            "\nUsing " + modesFileLocation + " modes file, version " + modesFileVersion +
            "\nUsing " + bpFileLocation + " band plan file, version " + bpFileVersion +
            "\nUsing Java Runtime Environment version " + jv +
            "\n\nCopyright" + y + " Stephen McGarry.\n" +
            "Provided under the terms of the GNU General Public Licence (GPL) v2 or later.\n" +
            "FlatLaf is provided under the terms of the Apache 2.0 Licence.\n\n" +
            "https://github.com/steeviebops/hacktv-gui\n\n",
            "About " + Shared.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_menuAboutActionPerformed

    private void menuDownloadUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuDownloadUpdateActionPerformed
        try {
            Shared.launchBrowser("https://github.com/steeviebops/hacktv-gui/releases/latest");
        } catch (IOException | UnsupportedOperationException e) {
            messageBox("Unable to launch default browser.", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_menuDownloadUpdateActionPerformed

    private void btnScramblingOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScramblingOptionsActionPerformed
        var m = (ModeInfo) cmbMode.getSelectedItem();
        var c1 = (ComboBoxOption) cmbScrambling1.getSelectedItem();
        var c2 = (ComboBoxOption) cmbScrambling2.getSelectedItem();
        if (c1 == null || c2 == null) return;
        ScramblingInfo ca;
        if (m.colourMode() != MAC) {
            ca = getScramblingInfo(m, c1.value());
        } else {
            ca = getScramblingInfo(m, c2.value());
        }
        var sd = new ScramblingSettingsDialogue(this, true, scramblingSettings);
        sd.postInit(ca, c2.value());
        sd.setLocationRelativeTo(this);
        sd.setVisible(true);
        var settings = sd.getSettings();
        if (settings != null) scramblingSettings = settings;
    }//GEN-LAST:event_btnScramblingOptionsActionPerformed

    private void cmbModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbModeActionPerformed
        if (cmbMode.getSelectedIndex() == -1) return;
        checkMode();
        previousIndex = cmbMode.getSelectedIndex();
        // Set sample rate
        txtSampleRate.setText(defaultSampleRate);
        // If test card is selected, see if the selected mode is supported
        if (radTest.isSelected()) checkTestCardStatus();
    }//GEN-LAST:event_cmbModeActionPerformed

    private void cmbRegionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbRegionActionPerformed
        String cf = null;
        if (chkLockFrequency.isSelected()) cf = txtFrequency.getText();
        if (cmbRegion.getSelectedIndex() == -1) return;
        var mode = (ModeInfo) cmbMode.getSelectedItem();
        var band = cmbBand.getSelectedItem().toString();
        var r = (BandPlan) cmbRegion.getSelectedItem();
        if (mode == null || band == null || r == null) return;
        cmbChannel.removeAllItems();
        BandPlan bp = null;
        switch (band) {
            case UHF_BAND:
                bp = mode.getUhfPlan(r.id());
                break;
            case VHF_BAND:
                bp = mode.getVhfPlan(r.id());
                break;
            case SATELLITE_BAND:
                bp = mode.getSatellitePlan(r.id());
                break;
            case CUSTOM_FREQUENCY:
            default:
                break;
        }
        if (bp != null) {
            var c = bp.channels();
            for (var entry : c) {
                cmbChannel.addItem(new Channel(entry.name(), entry.frequency(), entry.macChannelId()));
            }
        }
        lblChannel.setEnabled(cmbChannel.getItemCount() > 0);
        cmbChannel.setEnabled(cmbChannel.getItemCount() > 0);
        if (cf != null) {
            cmbBand.setSelectedItem(CUSTOM_FREQUENCY);
            txtFrequency.setText(cf);
        }
    }//GEN-LAST:event_cmbRegionActionPerformed

    private void cmbBandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbBandActionPerformed
        if (cmbBand.getSelectedIndex() == -1) return;
        var mode = (ModeInfo) cmbMode.getSelectedItem();
        var band = cmbBand.getSelectedItem().toString();
        cmbRegion.removeAllItems();
        BandPlan[] bp;
        switch(band) {
            case UHF_BAND:
                bp = mode.getUhfPlans();
                break;
            case VHF_BAND:
                bp = mode.getVhfPlans();
                break;
            case SATELLITE_BAND:
                bp = mode.getSatellitePlans();
                break;
            case CUSTOM_FREQUENCY:
            default:
                bp = new BandPlan[0];
                cmbRegion.removeAllItems();
                lblChannel.setEnabled(false);
                cmbChannel.setEnabled(false);
                cmbChannel.removeAllItems();
                break;
        }
        for (BandPlan b : bp) {
            cmbRegion.addItem(b);
        }
        lblRegion.setEnabled(cmbRegion.getItemCount() > 0);
        cmbRegion.setEnabled(cmbRegion.getItemCount() > 0);
        txtFrequency.setEditable(band.equals(CUSTOM_FREQUENCY));
    }//GEN-LAST:event_cmbBandActionPerformed

    private void cmbChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbChannelActionPerformed
        if (chkLockFrequency.isSelected()) return;
        if ( cmbChannel.getSelectedIndex() == -1) return;
        var ch = (Channel) cmbChannel.getSelectedItem();
        frequency = ch.frequency();
        // Convert the imported value so we can display it in MHz on-screen
        var df = new DecimalFormat("0.00");
        double input;
        if (PREFS.getInt("showrealfrequency", 0) == 1) {
            // Calculate TX frequency
            input = calculateFrequency(frequency, true);
        } else {
            // Use the frequency defined in the band plan
            input = frequency;
        }
        txtFrequency.setText((df.format(input / 1000000)));
        // Retrieve MAC channel ID
        if (((ModeInfo) cmbMode.getSelectedItem()).colourMode() == ColourMode.MAC) {
            var chid = ch.macChannelId();
            var settings = new MacSettings(chid, false, false, false, false);
            if (macSettings == null) { 
                macSettings = settings;
            } else {
                macSettings = new MacSettings(
                        chid,
                        macSettings.audioMode(),
                        macSettings.audioQuality(),
                        macSettings.audioCompression(),
                        macSettings.audioProtection()
                );
            }
        }
    }//GEN-LAST:event_cmbChannelActionPerformed

    private void chkSecamIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSecamIdActionPerformed
        cmbSecamIdLines.setEnabled(chkSecamId.isSelected());
        // Set ID 9 as default        
        cmbSecamIdLines.setSelectedIndex(chkSecamId.isSelected() ? 8 : -1);
    }//GEN-LAST:event_chkSecamIdActionPerformed

    private void cmbModeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmbModeMouseEntered
        var tt = (ModeInfo) cmbMode.getSelectedItem();
        if (tt.description() == null) {
            // Set tooltip to null to prevent it from showing a previously cached one
            cmbMode.setToolTipText(tt.description());
        } else {
            // Render the tooltip in HTML. This allows us to use <br> as a line
            // separator in the INI file.
            cmbMode.setToolTipText("<html>" + tt.description() + "</html>");
        }
    }//GEN-LAST:event_cmbModeMouseEntered

    private void btnMacOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMacOptionsActionPerformed
        var m = new MacSettingsDialogue(this, true, macSettings);
        m.postInit();
        m.setLocationRelativeTo(this);
        m.setVisible(true);
        var settings = m.getSettings();
        if (settings != null) macSettings = settings;
    }//GEN-LAST:event_btnMacOptionsActionPerformed

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
        chkAspectRatio.setEnabled(true);
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
                chkTeletextSubtitles.setEnabled(true);
            }
        }
    }//GEN-LAST:event_radLocalSourceActionPerformed

    private void chkAmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAmpActionPerformed
        if (chkAmp.isSelected()) {
            if ( (!htvLoadInProgress) && (PREFS.getInt("SuppressWarnings", 0) != 1))
                messageBox("""
                           Care is advised when using this option.
                           Incorrect use may permanently damage the amplifier.""",
                    JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_chkAmpActionPerformed

    private void txtFrequencyKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFrequencyKeyTyped
        if(txtFrequency.getText().length() > 9) {
            evt.consume();
        }
    }//GEN-LAST:event_txtFrequencyKeyTyped

    private void btnHackTVPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHackTVPathActionPerformed
        var hacktvFileChooser = new SystemFileChooser();
        if (isWindows) {
            hacktvFileChooser.addChoosableFileFilter(
                new SystemFileChooser.FileNameExtensionFilter("Applications (*.exe)", "exe")
            );
        }
        // Retrieve the last used directory from the prefs store if it exists
        hacktvFileChooser.setCurrentDirectory(
            new File(PREFS.get("lasthtvdir", System.getProperty("user.home")))
        );
        int returnVal = hacktvFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // Save the chosen directory to prefs
            PREFS.put("lasthtvdir", hacktvFileChooser.getCurrentDirectory().toString());
            File file = hacktvFileChooser.getSelectedFile();
            hackTVPath = Shared.stripQuotes(file.toString());
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

    private void btnClearMRUListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearMRUListActionPerformed
        if (JOptionPane.showConfirmDialog(null,
                "This will clear the list of most recently used "
                + "files from the File menu. Do you wish to continue?", 
                Shared.APP_NAME, JOptionPane.YES_NO_OPTION
        )  == JOptionPane.YES_OPTION) {
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
                Shared.APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            Shared.resetPreferences();
            dispose();
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
        var mode = (ModeInfo) cmbMode.getSelectedItem();
        if (!txtOutputDevice.getText().isBlank()) txtOutputDevice.setText("");
        var c = mode.colourMode();
        var od = (ComboBoxOption) cmbOutputDevice.getSelectedItem();
        boolean bb = mode.modulation() == UNMODULATED;
        switch(od.value()) {
            case "hackrf" -> {
                lblFl2kAudio.setEnabled(false);
                cmbFl2kAudio.setEnabled(false);
                cmbFl2kAudio.setSelectedIndex(-1);
                chkHackDAC.setEnabled(true);
                lblOutputDevice2.setText("Serial number (optional)");
                if (!cmbBand.isEnabled()) {
                    // If a baseband mode is selected and HackDAC is not enabled,
                    // reset the mode to something else
                    if ( (bb) &&
                            (PREFS.getInt("hackdac", 0) == 0) ) {
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
            }
            case "soapysdr" -> {
                lblFl2kAudio.setEnabled(false);
                cmbFl2kAudio.setEnabled(false);
                cmbFl2kAudio.setSelectedIndex(-1);
                chkHackDAC.setEnabled(false);
                lblOutputDevice2.setText("Device options");
                if (!cmbBand.isEnabled()) {
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
            }
            case "fl2k" -> {
                lblFl2kAudio.setEnabled(true);
                cmbFl2kAudio.setSelectedIndex(0);
                cmbFl2kAudio.setEnabled(true);
                chkHackDAC.setEnabled(false);
                lblOutputDevice2.setText("Device number (optional)");
                // fl2k is baseband only for now so disable all RF options
                disableRFOptions();
                // Enable S-Video option if a baseband mode is selected
                if ( bb && (c == ColourMode.PAL || c == ColourMode.NTSC ||
                        c == ColourMode.SECAM)) chkSVideo.setEnabled(true);
            }
            case "file" -> {
                // Output to file
                lblFl2kAudio.setEnabled(false);
                cmbFl2kAudio.setEnabled(false);
                cmbFl2kAudio.setSelectedIndex(-1);
                chkHackDAC.setEnabled(false);
                lblOutputDevice2.setText("Destination file");
                disableRFOptions();
                // Enable S-Video option if a baseband mode is selected
                if ( bb && (c == ColourMode.PAL || c == ColourMode.NTSC ||
                        c == ColourMode.SECAM)) chkSVideo.setEnabled(true);
                lblFileType.setEnabled(true);
                cmbFileType.setEnabled(true);
                cmbFileType.setSelectedIndex(3);
                // Opens the save file dialogue, but only if selected by the user
                if (!htvLoadInProgress) {
                    var outputFileChooser = new SystemFileChooser();
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
            }
            default -> System.err.println("Output device error");
        }
    }//GEN-LAST:event_cmbOutputDeviceActionPerformed

    private void chkVolumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkVolumeActionPerformed
        if (chkVolume.isSelected()) {
            txtVolume.setEnabled(true);
            txtVolume.setEditable(true);
        } else {
            txtVolume.setEnabled(false);
            txtVolume.setText("");
            txtVolume.setEditable(false);
        }
    }//GEN-LAST:event_chkVolumeActionPerformed

    private void chkTeletextSubtitlesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTeletextSubtitlesActionPerformed
        lblTeletextSubtitleIndex.setEnabled(chkTeletextSubtitles.isSelected());
        txtTeletextSubtitleIndex.setEnabled(chkTeletextSubtitles.isSelected()); 
        txtTeletextSubtitleIndex.setEditable(chkTeletextSubtitles.isSelected());
        if (!chkTeletextSubtitles.isSelected()) txtTeletextSubtitleIndex.setText("");
    }//GEN-LAST:event_chkTeletextSubtitlesActionPerformed

    private void chkLocalModesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLocalModesActionPerformed
        if (chkLocalModes.isSelected()) {
            PREFS.putInt("uselocalmodesfile", 1);
        } else {
            PREFS.putInt("uselocalmodesfile", 0);
        }
        // Reopen modes file with new settings
        selectModesFile();
    }//GEN-LAST:event_chkLocalModesActionPerformed

    private void chkFMDevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkFMDevActionPerformed
        if (chkFMDev.isSelected()) {
            txtFMDev.setEnabled(true);
            txtFMDev.setEditable(true);
        } else {
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
            startPoint = -1;
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
        else if ( (lstPlaylist.getSelectedIndex() == 0) && (playlistModel.size() == 1) ) {
            btnPlaylistUp.setEnabled(false);
            btnPlaylistDown.setEnabled(false);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(false);
            btnPlaylistStart.setText(playFirst);
            if (chkRandom.isSelected()) chkRandom.doClick();
            chkRandom.setEnabled(false);
        }
        // Is the selected item an intermediate item? (not the first or last)
        else if ( (lstPlaylist.getSelectedIndex() != 0) && (lstPlaylist.getSelectedIndex() != playlistModel.size() - 1) ) {
            btnPlaylistUp.setEnabled(true);
            btnPlaylistDown.setEnabled(true);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(true);
            if (lstPlaylist.getSelectedIndex() == startPoint) {
                btnPlaylistStart.setText(reset);
            } else {
                btnPlaylistStart.setText(playFirst);
            }
            chkRandom.setEnabled(true);
        }
        // Is the first item in the playlist selected?
        else if ( (lstPlaylist.getSelectedIndex() == 0) && (playlistModel.size() > 1) ) {
            btnPlaylistUp.setEnabled(false);
            btnPlaylistDown.setEnabled(true);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(true);
            if (lstPlaylist.getSelectedIndex() == startPoint) {
                btnPlaylistStart.setText(reset);
            } else {
                btnPlaylistStart.setText(playFirst);
            }
            chkRandom.setEnabled(true);
        }
        // Is the last item in the playlist selected?
        else if (lstPlaylist.getSelectedIndex() == playlistModel.size() - 1) {
            btnPlaylistUp.setEnabled(true);
            btnPlaylistDown.setEnabled(false);
            btnRemove.setEnabled(true);
            btnPlaylistStart.setEnabled(true);
            if (lstPlaylist.getSelectedIndex() == startPoint) {
                btnPlaylistStart.setText(reset);
            } else {
                btnPlaylistStart.setText(playFirst);
            }
            chkRandom.setEnabled(true);
        }
    }//GEN-LAST:event_lstPlaylistValueChanged

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (cmbM3USource.isVisible()) {
            // Add the URL from the selected M3U item to the playlist
            playlistModel.addElement(((ComboBoxOption) (cmbM3USource.getSelectedItem())).value());
        }
        // Don't add YouTube or other yt-dlp compatible URLs to the playlist
        else if ( (txtSource.getText().contains("://youtube.com/")) ||
                  (txtSource.getText().contains("://www.youtube.com/")) ||
                  (txtSource.getText().contains("://youtu.be/")) ||
                  (txtSource.getText().startsWith("ytdl:")) ) {
            messageBox("""
                       Unable to add this URL to the playlist.
                       The yt-dlp handler is only supported for single URLs at present.""",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        else if ((isPhilipsTestSignal()) && (txtSource.getText().isBlank())) {
            // Don't add Philips test cards
            messageBox("""
                       Adding a Philips test pattern to the playlist is not supported. Click "Run hacktv" to use it without the playlist.
                       However, you can add audio files to the playlist, which will be played over the test pattern.""",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        else if ( (txtSource.isEnabled()) && (!txtSource.getText().isBlank()) ) {
            // Add whatever is in txtSource to playlistModel
            playlistModel.addElement(txtSource.getText());
        }
        else if (radTest.isSelected()) {
            for (int i = 0; i < playlistModel.size(); i++) {
                if (playlistModel.get(i).startsWith("test:")) {
                    messageBox("""
                               Only one test card can be added to the playlist.
                               It should also be placed as the last item in the playlist.""",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
            }
            if (cmbTest.isEnabled()) {
                // Add the selected test card
                var ts = (TestSignalOption) cmbTest.getSelectedItem();
                playlistModel.addElement("test:" + ts.command());
            } else {
                // Add the test card
                playlistModel.addElement("test:colourbars");
            }
        } else {
            btnSourceBrowse.doClick();
            if (!txtSource.getText().isBlank()) btnAdd.doClick();
            return;
        }
        txtSource.setText("");
        lstPlaylist.setSelectedIndex(playlistModel.size() -1);
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int[] ia = lstPlaylist.getSelectedIndices();
        // If only one item was selected, put it back in the source box
        if (ia.length == 1) {
            String item = playlistModel.get(ia[0]);

            if (radLocalSource.isSelected()) {
                txtSource.setText(item);
            } else if (radTest.isSelected() && cmbTest.isEnabled()) {
                if (item != null && item.startsWith("test:")) {
                    String cmd = item.substring(5).trim(); // after "test:"

                    ComboBoxModel<TestSignalOption> model = cmbTest.getModel();
                    for (int i = 0; i < model.getSize(); i++) {
                        TestSignalOption opt = model.getElementAt(i);
                        if (opt != null && opt.command() != null && opt.command().equalsIgnoreCase(cmd)) {
                            cmbTest.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        }
        // Process the selection array in reverse order and remove the items from the arraylist
        for (int j = ia.length -1; j >= 0; j--) {
            // Remove the requested item from the arraylist
            playlistModel.remove(ia[j]);
            // If the item removed was the start point, or if only one item
            // is left, reset startPoint to default
            if ((ia[j] == startPoint) || (playlistModel.size() < 2)) {
                startPoint = -1;
            }                
            // If the item removed was before the start point, reduce startPoint
            // by one so the selected item remains selected
            else if (ia[j] < startPoint) {
                startPoint = startPoint - 1;
            }
        }
        // If only one item was selected...
        if (ia.length == 1) {
            // If the last item in the list was selected, select whatever
            // was the second from last (and is now last).
            if (playlistModel.size() == ia[0]) {
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
        } else {
            btnAdd.requestFocusInWindow();
        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnPlaylistStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlaylistStartActionPerformed
        // Don't set a test card as the start point of the playlist.
        // It never ends, so the playlist becomes pointless.
        int s = lstPlaylist.getSelectedIndex();
        if (playlistModel.get(s).startsWith("test:")) {
            messageBox("Test cards cannot be set as the start point of a playlist.", JOptionPane.WARNING_MESSAGE);
        } else if (s == startPoint) {
            // Reset the start point
            startPoint = -1;
            lstPlaylist.repaint();
        } else {
            // Set the start point
            startPoint = s;
            lstPlaylist.repaint();
        }
        // Reselect the item that was selected before the playlist was updated
        lstPlaylist.setSelectedIndex(s);
        btnPlaylistStart.requestFocusInWindow();
    }//GEN-LAST:event_btnPlaylistStartActionPerformed

    private void btnPlaylistUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlaylistUpActionPerformed
        int index = lstPlaylist.getSelectedIndex();
        if (index > 0) {
            String item = playlistModel.getElementAt(index);
            playlistModel.remove(index);
            playlistModel.add(index - 1, item);
            lstPlaylist.setSelectedIndex(index - 1);
            lstPlaylist.ensureIndexIsVisible(index - 1);
        }
    }//GEN-LAST:event_btnPlaylistUpActionPerformed

    private void btnPlaylistDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlaylistDownActionPerformed
        int index = lstPlaylist.getSelectedIndex();
        if (index != -1 && index < playlistModel.size() - 1) {
            String item = playlistModel.getElementAt(index);
            playlistModel.remove(index);
            playlistModel.add(index + 1, item);
            lstPlaylist.setSelectedIndex(index + 1);
            lstPlaylist.ensureIndexIsVisible(index + 1);
        }
    }//GEN-LAST:event_btnPlaylistDownActionPerformed

    private void cmbTestMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbTestMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbTestMouseWheelMoved

    private void cmbModeMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbModeMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbModeMouseWheelMoved

    private void cmbOutputDeviceMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbOutputDeviceMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbOutputDeviceMouseWheelMoved

    private void cmbBandMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbBandMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbBandMouseWheelMoved

    private void cmbRegionMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbRegionMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbRegionMouseWheelMoved

    private void cmbChannelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbChannelMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbChannelMouseWheelMoved

    private void cmbFl2kAudioMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbFl2kAudioMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbFl2kAudioMouseWheelMoved

    private void cmbFileTypeMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbFileTypeMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbFileTypeMouseWheelMoved

    private void cmbAspectRatioMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbAspectRatioMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbAspectRatioMouseWheelMoved

    private void cmbLogoMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbLogoMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbLogoMouseWheelMoved

    private void cmbWSSMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbWSSMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbWSSMouseWheelMoved

    private void cmbSecamIdLinesMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbSecamIdLinesMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbSecamIdLinesMouseWheelMoved

    private void cmbScrambling1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbScrambling1MouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbScrambling1MouseWheelMoved

    private void cmbScrambling2MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbScrambling2MouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbScrambling2MouseWheelMoved

    private void cmbScrambling3MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbScrambling3MouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbScrambling3MouseWheelMoved

    private void cmbNMSCeefaxRegionMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbNMSCeefaxRegionMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbNMSCeefaxRegionMouseWheelMoved

    private void cmbLookAndFeelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbLookAndFeelMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbLookAndFeelMouseWheelMoved

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

    private void cmbLookAndFeelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbLookAndFeelActionPerformed
        if (isVisible()) {
            SwingUtilities.invokeLater(() -> {
                changeLaf();
            });
        }
    }//GEN-LAST:event_cmbLookAndFeelActionPerformed

    private void chkGammaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGammaActionPerformed
        txtGamma.setEnabled(chkGamma.isSelected());
        txtGamma.setEditable(chkGamma.isSelected());
        if (!chkGamma.isSelected()) txtGamma.setText("");
    }//GEN-LAST:event_chkGammaActionPerformed

    private void txtGammaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtGammaKeyTyped
        if (evt.getKeyChar() == '\u002e') {
            if (txtGamma.getText().contains(".")) {
                evt.consume();
            }
        }
        else if (!Shared.isNumeric(String.valueOf(evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtGammaKeyTyped

    private void chkOutputLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkOutputLevelActionPerformed
        txtOutputLevel.setEnabled(chkOutputLevel.isSelected());
        txtOutputLevel.setEditable(chkOutputLevel.isSelected());
        if (!chkOutputLevel.isSelected()) txtOutputLevel.setText("");
    }//GEN-LAST:event_chkOutputLevelActionPerformed

    private void txtOutputLevelKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtOutputLevelKeyTyped
        if (evt.getKeyChar() == '\u002e') {
            if (txtOutputLevel.getText().contains(".")) {
                evt.consume();
            }
        }
        else if (!Shared.isNumeric(String.valueOf(evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtOutputLevelKeyTyped

    private void txtVolumeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtVolumeKeyTyped
        if (evt.getKeyChar() == '\u002e') {
            if (txtVolume.getText().contains(".")) {
                evt.consume();
            }
        }
        else if (!Shared.isNumeric(String.valueOf(evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtVolumeKeyTyped

    private void chkWSSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkWSSActionPerformed
        cmbWSS.setEnabled(chkWSS.isSelected());
        cmbWSS.setSelectedIndex(chkWSS.isSelected() ? 0 : -1);
    }//GEN-LAST:event_chkWSSActionPerformed

    private void chkPixelRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPixelRateActionPerformed
        txtPixelRate.setEnabled(chkPixelRate.isSelected());
        txtPixelRate.setEditable(chkPixelRate.isSelected());
        txtPixelRate.setText(chkPixelRate.isSelected() ? txtSampleRate.getText() : "");
    }//GEN-LAST:event_chkPixelRateActionPerformed

    private void chkAspectRatioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAspectRatioActionPerformed
        cmbAspectRatio.setEnabled(chkAspectRatio.isSelected());
        cmbAspectRatio.setSelectedIndex(0);
    }//GEN-LAST:event_chkAspectRatioActionPerformed

    private void chkSubtitlesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSubtitlesActionPerformed
        txtSubtitleIndex.setEditable(chkSubtitles.isSelected());
        txtSubtitleIndex.setEnabled(chkSubtitles.isSelected());
        if (!chkSubtitles.isSelected()) txtSubtitleIndex.setText("");
    }//GEN-LAST:event_chkSubtitlesActionPerformed

    private void chkLogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLogoActionPerformed
        cmbLogo.setEnabled(chkLogo.isSelected());
        cmbLogo.setSelectedIndex(chkLogo.isSelected() ? 0 : -1);
    }//GEN-LAST:event_chkLogoActionPerformed

    private void chkPositionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPositionActionPerformed
        txtPosition.setEditable(chkPosition.isSelected());
        txtPosition.setEnabled(chkPosition.isSelected());
        if (!chkPosition.isSelected()) txtPosition.setText("");
    }//GEN-LAST:event_chkPositionActionPerformed

    private void chkTeletextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTeletextActionPerformed
        boolean b = chkTeletext.isSelected();
        btnTeletextBrowse.setEnabled(b);
        txtTeletextSource.setEnabled(b);
        txtTeletextSource.setEditable(b);
        teletextDownloadPanel.setEnabled(b);
        teletextDownloadPanel.setEnabled(b);
        lblTeletextDownloadHeader.setEnabled(b);
        lblTeletextDescription.setEnabled(b);
        cmbTeletextDownload.setSelectedIndex(0);
        cmbTeletextDownload.setEnabled(b);
        if (!b) txtTeletextSource.setText("");
    }//GEN-LAST:event_chkTeletextActionPerformed

    private void cmbLookAndFeelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmbLookAndFeelMouseEntered
        // Show tooltip as the friendly name may be longer than the combobox
        cmbLookAndFeel.setToolTipText(cmbLookAndFeel.getSelectedItem().toString());
    }//GEN-LAST:event_cmbLookAndFeelMouseEntered

    private void chkHackDACActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkHackDACActionPerformed
        var m = ((ModeInfo) cmbMode.getSelectedItem()).modulation();
        if (chkHackDAC.isSelected()) {
            PREFS.putInt("hackdac", 1);
        } else {
            if (m == UNMODULATED) {
                messageBox("Please switch to a VSB or FM mode before disabling this option.", JOptionPane.WARNING_MESSAGE);
                chkHackDAC.setSelected(true);
                return;
            }
            PREFS.remove("hackdac");
            if (m == UNMODULATED) checkBasebandSupport();
        }
    }//GEN-LAST:event_chkHackDACActionPerformed

    private void chkSVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSVideoActionPerformed
        // Force enable "Colour" option when S-Video selected
        if (chkSVideo.isSelected()) {
            if (!chkColour.isSelected()) chkColour.doClick();
            chkColour.setEnabled(false);
        } else {
            var m = (ModeInfo) cmbMode.getSelectedItem();
            if (m.colourMode() != ColourMode.NONE){
                chkColour.setEnabled(true);
            }
        }
    }//GEN-LAST:event_chkSVideoActionPerformed

    private void btnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunActionPerformed
        if (!running) {
            if (downloadInProgress) {
                downloadCancelled = true;
                btnRun.setEnabled(false);
            } else if ( !chkSyntaxOnly.isSelected() && !Files.exists(Path.of(hackTVPath)) || hackTVPath.isBlank() ) {
                messageBox("Unable to find hacktv. Please go to the GUI settings tab to add its location.", JOptionPane.WARNING_MESSAGE);
            } else {
                populateArguments("");
            }
        } else {
            btnRun.setEnabled(false);
            stopTV();
        }
    }//GEN-LAST:event_btnRunActionPerformed

    private void chkOffsetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkOffsetActionPerformed
        txtOffset.setEditable(chkOffset.isSelected());
        txtOffset.setEnabled(chkOffset.isSelected());
        if (!chkOffset.isEnabled()) txtOffset.setText("");
    }//GEN-LAST:event_chkOffsetActionPerformed

    private void cmbScrambling1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbScrambling1ActionPerformed
        if (cmbScrambling1.getSelectedIndex() == -1) return;
        if (((ModeInfo) cmbMode.getSelectedItem()).colourMode() != ColourMode.MAC) {
            addScramblingKey();
        } else {
            addMACScramblingCA();
        }
    }//GEN-LAST:event_cmbScrambling1ActionPerformed

    private void cmbScrambling2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbScrambling2ActionPerformed
        if (cmbScrambling2.getSelectedIndex() == -1) {
            configureScramblingOptions();
            return;
        }
        if (((ModeInfo) cmbMode.getSelectedItem()).colourMode() == ColourMode.MAC) {
            addMACScramblingKey();
        }
        configureScramblingOptions();
    }//GEN-LAST:event_cmbScrambling2ActionPerformed

    private void btnTeletextBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeletextBrowseActionPerformed
        var teletextFileChooser = new SystemFileChooser();
        teletextFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        teletextFileChooser.addChoosableFileFilter(
                new SystemFileChooser.FileNameExtensionFilter("All teletext files (*.tti, *.t42)", "tti", "t42")
        );
        teletextFileChooser.addChoosableFileFilter(
                new SystemFileChooser.FileNameExtensionFilter("Teletext files (*.tti)", "tti")
        );
        teletextFileChooser.addChoosableFileFilter(
                new SystemFileChooser.FileNameExtensionFilter("Teletext containers (*.t42)", "t42")
        );
        teletextFileChooser.setAcceptAllFileFilterUsed(true);
        // Retrieve the last used directory from the prefs store if it exists
        teletextFileChooser.setCurrentDirectory(
            new File(PREFS.get("lasttxdir", System.getProperty("user.home")))
        );
        int result = teletextFileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            // Save the chosen directory to prefs
            PREFS.put("lasttxdir", teletextFileChooser.getCurrentDirectory().toString());
            File f = teletextFileChooser.getSelectedFile();
            txtTeletextSource.setText(Shared.stripQuotes(f.getAbsolutePath()));
        }
    }//GEN-LAST:event_btnTeletextBrowseActionPerformed

    private void chkUpdateCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkUpdateCheckActionPerformed
        PREFS.putInt("noupdatecheck", chkUpdateCheck.isSelected() ? 0 : 1);
    }//GEN-LAST:event_chkUpdateCheckActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cleanupBeforeExit();
    }//GEN-LAST:event_formWindowClosing

    private void cmbTeletextDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTeletextDownloadActionPerformed
        var selectedItem = (ComboBoxOption) cmbTeletextDownload.getSelectedItem();
        if (selectedItem == null) return;
        String defaultDesc = "No item selected";
        String desc = null;
        switch (selectedItem.value()) {
            case "" -> desc = defaultDesc;
            case "ceefax" -> desc = "Ceefax recreation by NMS";
            case "teefax" -> desc = "Teefax by Peter Kwan";
            case "spark" -> desc = "SPARK by TVARK";
            default -> System.err.println("Unexpected value: " + selectedItem.value());
        }
        if (desc == null) desc = defaultDesc;
        lblTeletextDescription.setText(desc);
        btnTeletextDownload.setEnabled(!desc.equals(defaultDesc));
    }//GEN-LAST:event_cmbTeletextDownloadActionPerformed

    private void cmbTeletextDownloadMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbTeletextDownloadMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), (JComboBox) evt.getComponent());
    }//GEN-LAST:event_cmbTeletextDownloadMouseWheelMoved

    private void btnTeletextDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTeletextDownloadActionPerformed
        if ((btnTeletextDownload.getText()).equals(STOP_DOWNLOAD)) {
            downloadCancelled = true;
            return;
        }
        var selectedDownload = (ComboBoxOption) cmbTeletextDownload.getSelectedItem();
        if (selectedDownload == null) return;
        btnTeletextDownload.setText(STOP_DOWNLOAD);
        cmbTeletextDownload.setEnabled(false);
        chkTeletext.setEnabled(false);
        txtTeletextSource.setEnabled(false);
        txtTeletextSource.setEditable(false);
        btnTeletextBrowse.setEnabled(false);
        btnRun.setEnabled(false);
        downloadCancelled = false;
        // Disable hacktv download button so it doesn't interfere
        if (isWindows) btnDownloadHackTV.setEnabled(false);
        // Set variables accordingly
        String url;
        String query1;
        String query2;
        switch (selectedDownload.value()) {
            case ("ceefax") -> {
                // If only I could bundle a copy of BART...
                var region = (ComboBoxOption) cmbNMSCeefaxRegion.getSelectedItem();
                url = "https://feeds.nmsni.co.uk/svn/ceefax/" + region.value() + "/";
                query1 = "file";
                query2 = "name";
            }
            case ("teefax") -> {
                url = "https://teastop.plus.com/svn/teletext/";
                query1 = null;
                query2 = null;
            }
            case ("spark") ->  {
                url = "https://api.github.com/repos/spark-teletext/spark-teletext/contents/";
                query1 = "download_url";
                query2 = "";
            }
            default -> {
                System.err.println("Unexpected value: " + selectedDownload.value());
                resetTeletextButtons();
                return;
            }
        }
        // Start the download
        downloadTeletext(url, selectedDownload.value(), query1, query2);
    }//GEN-LAST:event_btnTeletextDownloadActionPerformed

    private void btnSatSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSatSettingsActionPerformed
        // Show the setting dialogue box
        var sd = new SatSettingsDialogue(this, true);
        sd.setVisible(true);
        // See if a setting has changed. If so, refresh the channel combobox.
        if ( (sd.settingsChanged()) && (cmbChannel.isEnabled()) ) {
            cmbChannel.setSelectedIndex(cmbChannel.getSelectedIndex());
        }
    }//GEN-LAST:event_btnSatSettingsActionPerformed

    private void chkLockFrequencyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLockFrequencyActionPerformed
        boolean b = chkLockFrequency.isSelected();
        if (b) cmbBand.setSelectedItem(CUSTOM_FREQUENCY);
        cmbBand.setEnabled(!b);
    }//GEN-LAST:event_chkLockFrequencyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advModePanel;
    private javax.swing.JPanel audioPanel;
    private javax.swing.ButtonGroup bgAudio;
    private javax.swing.ButtonGroup bgSource;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClearMRUList;
    private javax.swing.JButton btnDownloadHackTV;
    private javax.swing.JButton btnHackTVPath;
    private javax.swing.JButton btnHideConsole;
    private javax.swing.JButton btnMacOptions;
    private javax.swing.JButton btnPlaylistDown;
    private javax.swing.JButton btnPlaylistStart;
    private javax.swing.JButton btnPlaylistUp;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnResetAllSettings;
    private javax.swing.JButton btnRun;
    private javax.swing.JButton btnSatSettings;
    private javax.swing.JButton btnScramblingOptions;
    private javax.swing.JButton btnSourceBrowse;
    private javax.swing.JButton btnTeletextBrowse;
    private javax.swing.JButton btnTeletextDownload;
    private javax.swing.JButton btnTestSettings;
    private javax.swing.JPanel buildLabelPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel captainJackPanel;
    private javax.swing.JCheckBox chkACP;
    private javax.swing.JCheckBox chkAmp;
    private javax.swing.JCheckBox chkAspectRatio;
    private javax.swing.JCheckBox chkCC608;
    private javax.swing.JCheckBox chkColour;
    private javax.swing.JCheckBox chkDownmix;
    private javax.swing.JCheckBox chkFMDev;
    private javax.swing.JCheckBox chkFmFilter;
    private javax.swing.JCheckBox chkGamma;
    private javax.swing.JCheckBox chkHackDAC;
    private javax.swing.JCheckBox chkInterlace;
    private javax.swing.JCheckBox chkInvertVideo;
    private javax.swing.JCheckBox chkLocalModes;
    private javax.swing.JCheckBox chkLockFrequency;
    private javax.swing.JCheckBox chkLogo;
    private javax.swing.JCheckBox chkOffset;
    private javax.swing.JCheckBox chkOutputLevel;
    private javax.swing.JCheckBox chkPixelRate;
    private javax.swing.JCheckBox chkPosition;
    private javax.swing.JCheckBox chkRandom;
    private javax.swing.JCheckBox chkRepeat;
    private javax.swing.JCheckBox chkSVideo;
    private javax.swing.JCheckBox chkSecamId;
    private javax.swing.JCheckBox chkSiS;
    private javax.swing.JCheckBox chkSubtitles;
    private javax.swing.JCheckBox chkSwapIQ;
    private javax.swing.JCheckBox chkSyntaxOnly;
    private javax.swing.JCheckBox chkTeletext;
    private javax.swing.JCheckBox chkTeletextSubtitles;
    private javax.swing.JCheckBox chkTimestamp;
    private javax.swing.JCheckBox chkUpdateCheck;
    private javax.swing.JCheckBox chkVITC;
    private javax.swing.JCheckBox chkVITS;
    private javax.swing.JCheckBox chkVolume;
    private javax.swing.JCheckBox chkVsbFilter;
    private javax.swing.JCheckBox chkWSS;
    private javax.swing.JComboBox<ComboBoxOption> cmbAspectRatio;
    private javax.swing.JComboBox<String> cmbBand;
    private javax.swing.JComboBox<Channel> cmbChannel;
    private javax.swing.JComboBox<String> cmbFileType;
    private javax.swing.JComboBox<ComboBoxOption> cmbFl2kAudio;
    private javax.swing.JComboBox<ComboBoxOption> cmbLogo;
    private javax.swing.JComboBox<ComboBoxOption> cmbLookAndFeel;
    private javax.swing.JComboBox<ComboBoxOption> cmbM3USource;
    private javax.swing.JComboBox<ModeInfo> cmbMode;
    private javax.swing.JComboBox<ComboBoxOption> cmbNMSCeefaxRegion;
    private javax.swing.JComboBox<ComboBoxOption> cmbOutputDevice;
    private javax.swing.JComboBox<BandPlan> cmbRegion;
    private javax.swing.JComboBox<ComboBoxOption> cmbScrambling1;
    private javax.swing.JComboBox<ComboBoxOption> cmbScrambling2;
    private javax.swing.JComboBox<ComboBoxOption> cmbScrambling3;
    private javax.swing.JComboBox<String> cmbSecamIdLines;
    private javax.swing.JComboBox<ComboBoxOption> cmbTeletextDownload;
    private javax.swing.JComboBox<TestSignalOption> cmbTest;
    private javax.swing.JComboBox<ComboBoxOption> cmbWSS;
    private javax.swing.JPanel comboBoxPanel;
    private javax.swing.JPanel consolePanel;
    private javax.swing.JScrollPane consoleScrollPane;
    private javax.swing.JPanel deviceOptionsPanel;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPanel fl2kOptionsPanel;
    private javax.swing.JPanel generalSettingsPanel;
    private javax.swing.JPanel hacktvPathPanel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel lblAntennaName;
    private javax.swing.JLabel lblAudioModDesc;
    private javax.swing.JLabel lblAudioModValue;
    private javax.swing.JLabel lblAudioSpacingDesc;
    private javax.swing.JLabel lblAudioSpacingValue;
    private javax.swing.JLabel lblBand;
    private javax.swing.JLabel lblChannel;
    private javax.swing.JLabel lblDetectedBuild;
    private javax.swing.JLabel lblFieldRateDesc;
    private javax.swing.JLabel lblFieldRateValue;
    private javax.swing.JLabel lblFileType;
    private javax.swing.JLabel lblFl2kAudio;
    private javax.swing.JLabel lblFork;
    private javax.swing.JLabel lblFrequency;
    private javax.swing.JLabel lblGain;
    private javax.swing.JLabel lblHackTVLocation;
    private javax.swing.JLabel lblLinesDesc;
    private javax.swing.JLabel lblLinesValue;
    private javax.swing.JLabel lblLookAndFeel;
    private javax.swing.JLabel lblNMSCeefaxRegion;
    private javax.swing.JLabel lblOutputDevice;
    private javax.swing.JLabel lblOutputDevice2;
    private javax.swing.JLabel lblRegion;
    private javax.swing.JLabel lblSampleRate;
    private javax.swing.JLabel lblScrambling1;
    private javax.swing.JLabel lblScrambling2;
    private javax.swing.JLabel lblScrambling3;
    private javax.swing.JLabel lblTeletextDescription;
    private javax.swing.JLabel lblTeletextDownloadHeader;
    private javax.swing.JLabel lblTeletextSubtitleIndex;
    private javax.swing.JLabel lblVideoModDesc;
    private javax.swing.JLabel lblVideoModValue;
    private javax.swing.JList<ColourOption> lstColour;
    private javax.swing.JList<String> lstPlaylist;
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
    private javax.swing.JPanel modeContainerPanel;
    private javax.swing.JPanel modePanel;
    private javax.swing.JPanel modeTab;
    private javax.swing.JPanel modeTabContainerPanel;
    private javax.swing.JPanel outputContainerPanel;
    private javax.swing.JPanel outputDevicePanel;
    private javax.swing.JPanel outputTab;
    private javax.swing.JPanel playbackContainerPanel;
    private javax.swing.JPanel playbackOptionsPanel;
    private javax.swing.JPanel playbackTab;
    private javax.swing.JPanel playlistButtonsPanel;
    private javax.swing.JScrollPane playlistScrollPane;
    private javax.swing.JRadioButton radA2Stereo;
    private javax.swing.JRadioButton radLocalSource;
    private javax.swing.JRadioButton radMono;
    private javax.swing.JRadioButton radNICAM;
    private javax.swing.JRadioButton radNoAudio;
    private javax.swing.JRadioButton radTest;
    private javax.swing.JPanel resetSettingsPanel;
    private javax.swing.JPanel rfPanel;
    private javax.swing.JPanel scramblingContainerPanel;
    private javax.swing.JPanel scramblingPanel;
    private javax.swing.JPanel scramblingTab;
    private javax.swing.JPopupMenu.Separator sepAboutSeparator;
    private javax.swing.JPopupMenu.Separator sepExitSeparator;
    private javax.swing.JPopupMenu.Separator sepMruSeparator;
    private javax.swing.JPanel settingsContainerPanel;
    private javax.swing.JPanel settingsTab;
    private javax.swing.JPanel sourceCardPanel;
    private javax.swing.JPanel sourceSelectionPanel;
    private javax.swing.JPanel sourceTab;
    private javax.swing.JPanel sourceTabContainer;
    private javax.swing.JScrollPane spCategory;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JPanel teletextContainerPanel;
    private javax.swing.JPanel teletextDownloadPanel;
    private javax.swing.JPanel teletextPanel;
    private javax.swing.JPanel teletextTab;
    private javax.swing.JMenu templatesMenu;
    private javax.swing.JTextField txtAntennaName;
    private javax.swing.JTextArea txtConsoleOutput;
    private javax.swing.JTextField txtFMDev;
    private javax.swing.JTextField txtFrequency;
    private javax.swing.JTextField txtGain;
    private javax.swing.JTextField txtGamma;
    private javax.swing.JTextField txtHackTVPath;
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
    private javax.swing.JTextField txtTeletextSubtitleIndex;
    private javax.swing.JTextField txtVolume;
    private javax.swing.JMenu updateMenu;
    private javax.swing.JPanel vbiOptionsPanel;
    // End of variables declaration//GEN-END:variables
}
