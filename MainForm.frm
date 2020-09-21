VERSION 5.00
Begin VB.Form MainForm 
   BorderStyle     =   1  'Fixed Single
   Caption         =   "GUI frontend for hacktv"
   ClientHeight    =   7245
   ClientLeft      =   1425
   ClientTop       =   1080
   ClientWidth     =   12015
   BeginProperty Font 
      Name            =   "Tahoma"
      Size            =   8.25
      Charset         =   0
      Weight          =   400
      Underline       =   0   'False
      Italic          =   0   'False
      Strikethrough   =   0   'False
   EndProperty
   Icon            =   "MainForm.frx":0000
   KeyPreview      =   -1  'True
   LinkTopic       =   "Form1"
   MaxButton       =   0   'False
   ScaleHeight     =   7245
   ScaleWidth      =   12015
   Begin hacktv_gui.NetGrab NetGrab2 
      Left            =   6480
      Top             =   6480
      _ExtentX        =   661
      _ExtentY        =   661
   End
   Begin VB.Frame FrmScramblingOptions 
      Caption         =   "Video scrambling options"
      Height          =   1215
      Left            =   120
      TabIndex        =   27
      Top             =   3840
      Width           =   6495
      Begin VB.ComboBox encryption_type 
         Enabled         =   0   'False
         Height          =   315
         Left            =   120
         Style           =   2  'Dropdown List
         TabIndex        =   28
         Top             =   360
         Width           =   1815
      End
      Begin VB.CheckBox ChkEncryptAudio 
         Caption         =   "Scramble audio"
         Enabled         =   0   'False
         Height          =   195
         Left            =   120
         TabIndex        =   32
         Top             =   840
         Width           =   1455
      End
      Begin VB.CheckBox ChkEnableEMM 
         Caption         =   "Activate card"
         Enabled         =   0   'False
         Height          =   195
         Left            =   1800
         TabIndex        =   33
         Top             =   840
         Width           =   1455
      End
      Begin VB.CheckBox ChkDisableEMM 
         Caption         =   "Deactivate card"
         Enabled         =   0   'False
         Height          =   195
         Left            =   3360
         TabIndex        =   34
         Top             =   840
         Width           =   1575
      End
      Begin VB.TextBox CardNumber 
         BackColor       =   &H8000000F&
         Enabled         =   0   'False
         Height          =   285
         Left            =   5040
         MaxLength       =   13
         TabIndex        =   35
         Top             =   840
         Width           =   1335
      End
      Begin VB.ComboBox vc1key 
         Height          =   315
         Left            =   2040
         Style           =   2  'Dropdown List
         TabIndex        =   30
         Top             =   360
         Visible         =   0   'False
         Width           =   2115
      End
      Begin VB.ComboBox vc2key 
         Height          =   315
         Left            =   4260
         Style           =   2  'Dropdown List
         TabIndex        =   31
         Top             =   360
         Visible         =   0   'False
         Width           =   2115
      End
      Begin VB.CheckBox ChkFindKey 
         Caption         =   "Find keys on PPV card"
         Enabled         =   0   'False
         Height          =   195
         Left            =   1800
         TabIndex        =   38
         Top             =   840
         Visible         =   0   'False
         Width           =   2055
      End
      Begin VB.ComboBox encryption_key 
         Enabled         =   0   'False
         Height          =   315
         Left            =   2040
         Style           =   2  'Dropdown List
         TabIndex        =   29
         Top             =   360
         Width           =   4335
      End
      Begin VB.CheckBox ChkShowSerial 
         Caption         =   "Show card serial"
         Enabled         =   0   'False
         Height          =   195
         Left            =   120
         TabIndex        =   37
         Top             =   840
         Visible         =   0   'False
         Width           =   1575
      End
   End
   Begin VB.Frame FrmTeletext 
      Caption         =   "Teletext options"
      Height          =   1215
      Left            =   6720
      TabIndex        =   50
      Top             =   120
      Width           =   5175
      Begin VB.PictureBox Picture4 
         BorderStyle     =   0  'None
         Height          =   855
         Left            =   120
         ScaleHeight     =   855
         ScaleWidth      =   4995
         TabIndex        =   51
         Top             =   240
         Width           =   5000
         Begin VB.CommandButton BtnTeletextBrowseFile 
            Caption         =   "File..."
            Enabled         =   0   'False
            Height          =   375
            Left            =   1680
            TabIndex        =   76
            Top             =   480
            Width           =   1600
         End
         Begin VB.CheckBox ChkTeletext 
            Caption         =   "Teletext"
            Height          =   255
            Left            =   0
            TabIndex        =   52
            Top             =   120
            Width           =   975
         End
         Begin VB.TextBox teletext_source 
            BackColor       =   &H8000000F&
            Enabled         =   0   'False
            Height          =   285
            Left            =   1080
            TabIndex        =   53
            Top             =   120
            Width           =   3855
         End
         Begin VB.CommandButton BtnTeletextBrowse 
            Caption         =   "Directory..."
            Enabled         =   0   'False
            Height          =   375
            Left            =   0
            TabIndex        =   54
            Top             =   480
            Width           =   1600
         End
         Begin VB.CommandButton BtnTeletextDownload 
            Caption         =   "Download..."
            Enabled         =   0   'False
            Height          =   375
            Left            =   3360
            TabIndex        =   55
            Top             =   480
            Width           =   1605
         End
      End
   End
   Begin VB.Frame FrmOther 
      Caption         =   "Additional options"
      Height          =   4575
      Left            =   6720
      TabIndex        =   56
      Top             =   1440
      Width           =   5175
      Begin VB.CheckBox ChkVITS 
         Caption         =   "VITS test signal"
         Enabled         =   0   'False
         Height          =   315
         Left            =   120
         TabIndex        =   59
         Top             =   960
         Width           =   1815
      End
      Begin VB.CheckBox ChkARCorrection 
         Caption         =   "16:9 source on 4:3 display"
         Height          =   315
         Left            =   120
         TabIndex        =   63
         Top             =   2040
         Width           =   2535
      End
      Begin VB.ComboBox ARCorrectionMode 
         Enabled         =   0   'False
         Height          =   315
         Left            =   3600
         Style           =   2  'Dropdown List
         TabIndex        =   64
         Top             =   2040
         Width           =   1455
      End
      Begin VB.CheckBox ChkShowECM 
         Caption         =   "Show ECMs on console"
         Enabled         =   0   'False
         Height          =   315
         Left            =   120
         TabIndex        =   72
         Top             =   3840
         Width           =   2055
      End
      Begin VB.CheckBox ChkVerbose 
         Caption         =   "Enable verbose output"
         Height          =   315
         Left            =   120
         TabIndex        =   73
         Top             =   4200
         Width           =   2415
      End
      Begin VB.CheckBox ChkWSS 
         Caption         =   "Widescreen signalling (WSS) on line 23"
         Height          =   315
         Left            =   120
         TabIndex        =   61
         Top             =   1680
         Width           =   3255
      End
      Begin VB.ComboBox wss_mode 
         Enabled         =   0   'False
         Height          =   315
         Left            =   3600
         Style           =   2  'Dropdown List
         TabIndex        =   62
         Top             =   1680
         Width           =   1455
      End
      Begin VB.CheckBox ChkGamma 
         Caption         =   "Gamma correction"
         Height          =   315
         Left            =   120
         TabIndex        =   65
         Top             =   2400
         Width           =   2175
      End
      Begin VB.TextBox gammavalue 
         BackColor       =   &H8000000F&
         Enabled         =   0   'False
         Height          =   285
         Left            =   4320
         MaxLength       =   4
         TabIndex        =   66
         Top             =   2400
         Width           =   735
      End
      Begin VB.CheckBox ChkOutputLevel 
         Caption         =   "Output level"
         Height          =   315
         Left            =   120
         TabIndex        =   67
         Top             =   2760
         Width           =   1935
      End
      Begin VB.TextBox outputlevelvalue 
         BackColor       =   &H8000000F&
         Enabled         =   0   'False
         Height          =   285
         Left            =   4320
         MaxLength       =   4
         TabIndex        =   68
         Top             =   2760
         Width           =   735
      End
      Begin VB.CheckBox ChkFMDev 
         Caption         =   "FM deviation (MHz)"
         Height          =   315
         Left            =   120
         TabIndex        =   69
         Top             =   3120
         Width           =   1935
      End
      Begin VB.TextBox fm_deviation 
         BackColor       =   &H8000000F&
         Enabled         =   0   'False
         Height          =   285
         Left            =   4320
         MaxLength       =   5
         TabIndex        =   70
         Top             =   3120
         Width           =   735
      End
      Begin VB.CheckBox ChkACP 
         Caption         =   "Macrovision ACP"
         Height          =   315
         Left            =   120
         TabIndex        =   60
         Top             =   1320
         Width           =   1575
      End
      Begin VB.CheckBox ChkVideoFilter 
         Caption         =   "VSB-AM filter"
         Height          =   315
         Left            =   120
         TabIndex        =   71
         Top             =   3480
         Width           =   3135
      End
      Begin VB.CheckBox ChkAudio 
         Caption         =   "Audio enabled"
         Height          =   315
         Left            =   120
         TabIndex        =   57
         Top             =   240
         Width           =   1455
      End
      Begin VB.CheckBox ChkNICAM 
         Caption         =   "NICAM stereo"
         Enabled         =   0   'False
         Height          =   315
         Left            =   120
         TabIndex        =   58
         Top             =   600
         Width           =   1575
      End
   End
   Begin VB.TextBox allargs 
      BackColor       =   &H8000000F&
      Height          =   285
      Left            =   120
      Locked          =   -1  'True
      TabIndex        =   75
      Top             =   6840
      Width           =   11775
   End
   Begin VB.CommandButton BtnRun 
      Caption         =   "Run hacktv..."
      Height          =   615
      Left            =   6720
      TabIndex        =   74
      Top             =   6120
      Width           =   5175
   End
   Begin VB.Frame FrmFrequency 
      Caption         =   "Frequency and TX options"
      Height          =   1575
      Left            =   120
      TabIndex        =   36
      Top             =   5160
      Width           =   6495
      Begin VB.PictureBox Picture3 
         BorderStyle     =   0  'None
         Height          =   255
         Left            =   120
         ScaleHeight     =   255
         ScaleWidth      =   4095
         TabIndex        =   39
         Top             =   360
         Width           =   4095
         Begin VB.OptionButton VHF 
            Caption         =   "VHF"
            Height          =   255
            Left            =   1200
            TabIndex        =   41
            Top             =   0
            Width           =   615
         End
         Begin VB.OptionButton UHF 
            Caption         =   "UHF"
            Height          =   255
            Left            =   0
            TabIndex        =   40
            Top             =   0
            Width           =   735
         End
         Begin VB.OptionButton CustomFreq 
            Caption         =   "Custom"
            Height          =   255
            Left            =   3000
            TabIndex        =   42
            Top             =   0
            Width           =   975
         End
      End
      Begin VB.TextBox txgain 
         Height          =   285
         Left            =   1320
         MaxLength       =   2
         TabIndex        =   48
         Text            =   "0"
         Top             =   1200
         Width           =   1455
      End
      Begin VB.TextBox frequency_mhz 
         Enabled         =   0   'False
         Height          =   285
         Left            =   4680
         MaxLength       =   7
         TabIndex        =   46
         Top             =   720
         Width           =   1695
      End
      Begin VB.CheckBox ChkRFAmp 
         Caption         =   "TX RF amplifier"
         Height          =   255
         Left            =   3120
         TabIndex        =   49
         Top             =   1200
         Width           =   1575
      End
      Begin VB.ComboBox frequency_ch 
         Height          =   315
         Left            =   1320
         Style           =   2  'Dropdown List
         TabIndex        =   44
         Top             =   720
         Width           =   1455
      End
      Begin VB.Label LblGain 
         Caption         =   "TX gain (dB)"
         Height          =   255
         Left            =   120
         TabIndex        =   47
         Top             =   1200
         Width           =   975
      End
      Begin VB.Label LblFrequency 
         Caption         =   "Frequency (MHz)"
         Height          =   255
         Left            =   3120
         TabIndex        =   45
         Top             =   720
         Width           =   1335
      End
      Begin VB.Label LblChannel 
         Caption         =   "Channel"
         Height          =   255
         Left            =   120
         TabIndex        =   43
         Top             =   720
         Width           =   855
      End
   End
   Begin VB.Frame FrmVideoFormat 
      Caption         =   "Video format options"
      Height          =   1215
      Left            =   120
      TabIndex        =   17
      Top             =   2520
      Width           =   6495
      Begin VB.PictureBox Picture2 
         BorderStyle     =   0  'None
         Height          =   255
         Left            =   120
         ScaleHeight     =   255
         ScaleWidth      =   6255
         TabIndex        =   18
         Top             =   360
         Width           =   6255
         Begin VB.OptionButton BW 
            Caption         =   "Black and white"
            Height          =   195
            Left            =   3840
            TabIndex        =   22
            Top             =   0
            Width           =   1455
         End
         Begin VB.OptionButton NTSC 
            Caption         =   "NTSC"
            Height          =   195
            Left            =   1200
            TabIndex        =   20
            Top             =   0
            Width           =   855
         End
         Begin VB.OptionButton PAL 
            Caption         =   "PAL"
            Height          =   195
            Left            =   0
            TabIndex        =   19
            Top             =   0
            Width           =   735
         End
         Begin VB.OptionButton SECAM 
            Caption         =   "SECAM"
            Height          =   195
            Left            =   2520
            TabIndex        =   21
            Top             =   0
            Width           =   855
         End
         Begin VB.OptionButton MAC 
            Caption         =   "MAC"
            Height          =   195
            Left            =   5520
            TabIndex        =   23
            Top             =   0
            Width           =   735
         End
      End
      Begin VB.TextBox SampleRate 
         Height          =   285
         Left            =   5640
         MaxLength       =   6
         TabIndex        =   26
         Top             =   720
         Width           =   735
      End
      Begin VB.ComboBox VideoFormat 
         Height          =   315
         Left            =   120
         Style           =   2  'Dropdown List
         TabIndex        =   24
         Top             =   720
         Width           =   3975
      End
      Begin VB.Label LblSampleRate 
         Caption         =   "Sample rate (MHz)"
         Height          =   285
         Left            =   4200
         TabIndex        =   25
         Top             =   720
         Width           =   1455
      End
   End
   Begin VB.Frame FrmSource 
      Caption         =   "Source options"
      Height          =   2295
      Left            =   120
      TabIndex        =   0
      Top             =   120
      Width           =   6495
      Begin VB.CheckBox ChkInterlace 
         Caption         =   "Interlaced video"
         Height          =   315
         Left            =   120
         TabIndex        =   13
         Top             =   1920
         Width           =   1815
      End
      Begin VB.TextBox TxtSubtitleIndex 
         BackColor       =   &H8000000F&
         Enabled         =   0   'False
         Height          =   285
         Left            =   5640
         MaxLength       =   2
         TabIndex        =   16
         Top             =   1920
         Width           =   735
      End
      Begin VB.CheckBox ChkSubtitles 
         Caption         =   "Subtitles"
         Enabled         =   0   'False
         Height          =   315
         Left            =   2880
         TabIndex        =   14
         Top             =   1920
         Width           =   975
      End
      Begin VB.CheckBox ChkRepeat 
         Caption         =   "Repeat indefinitely"
         Height          =   315
         Left            =   120
         TabIndex        =   7
         Top             =   1200
         Width           =   1695
      End
      Begin VB.CheckBox ChkLogo 
         Caption         =   "Overlay logo"
         Height          =   255
         Left            =   2880
         TabIndex        =   11
         Top             =   1560
         Width           =   1575
      End
      Begin VB.ComboBox LogoFolder 
         Enabled         =   0   'False
         Height          =   315
         Left            =   4560
         Style           =   2  'Dropdown List
         TabIndex        =   12
         Top             =   1560
         Width           =   1815
      End
      Begin VB.PictureBox Picture1 
         BorderStyle     =   0  'None
         Height          =   735
         Left            =   120
         ScaleHeight     =   735
         ScaleWidth      =   6255
         TabIndex        =   1
         Top             =   360
         Width           =   6255
         Begin VB.CommandButton BtnSourceBrowse 
            Caption         =   "Browse..."
            Height          =   375
            Left            =   4800
            TabIndex        =   6
            Top             =   360
            Width           =   1455
         End
         Begin VB.OptionButton LocalSource 
            Caption         =   "Local or internet source"
            Height          =   255
            Left            =   0
            TabIndex        =   2
            Top             =   0
            Width           =   2055
         End
         Begin VB.OptionButton TestCard 
            Caption         =   "Test card"
            Height          =   255
            Left            =   2760
            TabIndex        =   3
            Top             =   0
            Width           =   1575
         End
         Begin VB.TextBox input_source 
            Height          =   315
            Left            =   0
            TabIndex        =   5
            Top             =   360
            Width           =   4695
         End
         Begin VB.ComboBox M3USource 
            Height          =   315
            Left            =   0
            Style           =   2  'Dropdown List
            TabIndex        =   4
            Top             =   360
            Visible         =   0   'False
            Width           =   4695
         End
      End
      Begin VB.CheckBox ChkTimestamp 
         Caption         =   "Overlay timestamp"
         Enabled         =   0   'False
         Height          =   255
         Left            =   120
         TabIndex        =   10
         Top             =   1560
         Width           =   1695
      End
      Begin VB.TextBox positionValue 
         BackColor       =   &H8000000F&
         Enabled         =   0   'False
         Height          =   285
         Left            =   5640
         MaxLength       =   3
         TabIndex        =   9
         Top             =   1200
         Width           =   735
      End
      Begin VB.CheckBox ChkPosition 
         Caption         =   "Start position (minutes)"
         Enabled         =   0   'False
         Height          =   315
         Left            =   2880
         TabIndex        =   8
         Top             =   1200
         Width           =   2055
      End
      Begin VB.Label LblSubtitleIndex 
         Caption         =   "Index (optional)"
         Enabled         =   0   'False
         Height          =   255
         Left            =   4080
         TabIndex        =   15
         Top             =   1920
         Width           =   1455
      End
   End
   Begin VB.Menu FileMenu 
      Caption         =   "&File"
      Begin VB.Menu NewFile 
         Caption         =   "New"
      End
      Begin VB.Menu OpenFile 
         Caption         =   "Open..."
      End
      Begin VB.Menu SaveFile 
         Caption         =   "Save..."
      End
      Begin VB.Menu SaveFileAs 
         Caption         =   "Save As..."
      End
      Begin VB.Menu MRUbar 
         Caption         =   "-"
         Visible         =   0   'False
      End
      Begin VB.Menu file1 
         Caption         =   "MRUFile1"
         Enabled         =   0   'False
         Visible         =   0   'False
      End
      Begin VB.Menu file2 
         Caption         =   "MRUFile2"
         Enabled         =   0   'False
         Visible         =   0   'False
      End
      Begin VB.Menu file3 
         Caption         =   "MRUFile3"
         Enabled         =   0   'False
         Visible         =   0   'False
      End
      Begin VB.Menu file4 
         Caption         =   "MRUFile4"
         Enabled         =   0   'False
         Visible         =   0   'False
      End
      Begin VB.Menu bar3 
         Caption         =   "-"
      End
      Begin VB.Menu exit 
         Caption         =   "E&xit"
      End
   End
   Begin VB.Menu TemplateMenu 
      Caption         =   "&Templates"
      Begin VB.Menu Astra975Template 
         Caption         =   "Astra analogue STB (9.75 GHz)..."
      End
      Begin VB.Menu Astra10Template 
         Caption         =   "Astra analogue STB (10 GHz)..."
      End
      Begin VB.Menu BSBTemplate 
         Caption         =   "BSB D-MAC STB..."
      End
   End
   Begin VB.Menu settings 
      Caption         =   "&Settings"
      Begin VB.Menu PathSettings 
         Caption         =   "&Path settings..."
      End
      Begin VB.Menu bar4 
         Caption         =   "-"
      End
      Begin VB.Menu CloseOnExit 
         Caption         =   "Don't close on exit"
      End
      Begin VB.Menu GenerateOnly 
         Caption         =   "Generate syntax only"
      End
      Begin VB.Menu ChkRestrictCPUIdleStates 
         Caption         =   "Restrict CPU idle states"
      End
      Begin VB.Menu bar5 
         Caption         =   "-"
      End
      Begin VB.Menu ClearMRUList 
         Caption         =   "Clear MRU list..."
         Enabled         =   0   'False
      End
      Begin VB.Menu ResetAllSettings 
         Caption         =   "Reset all settings..."
      End
   End
   Begin VB.Menu HelpMenu 
      Caption         =   "&Help"
      Begin VB.Menu HelpIndex 
         Caption         =   "&Index..."
      End
      Begin VB.Menu UpdateCheck 
         Caption         =   "Check for updates..."
      End
      Begin VB.Menu bar2 
         Caption         =   "-"
      End
      Begin VB.Menu about 
         Caption         =   "&About..."
      End
   End
End
Attribute VB_Name = "MainForm"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

' Declare a variable for NICAM stereo status.
Dim NICAMSupported As Boolean

' Declare a variable to determine the selected fork
Public forktype As String

' Declare Teletext-related variables that are reused across multiple subs
Dim demotext As String
Dim TeefaxPath As String

' Declare variables used for path resolution
Public HackTVEXEName As String
Public HackTVPath As String
Public TerminalPath As String
Public DefaultHackTVPath As String
Public DefaultTerminalPath As String

' Declare variable for file numbers
Dim iFileNo As Integer

' Declare variable for the title bar display
Dim TitleBar As String
Dim TitleBarChanged As Boolean

' Declare a variable to check if a command line file has been processed or not
Dim FileOpened As Boolean

' Declare a variable used to hold the file name of a configuration file
Dim ConfigFileName As String

' Arrays used for M3U files
Dim URL() As String
Dim M3USourceFile As Boolean
Dim AppBusy As Boolean
Dim StopProcessing As Boolean

' Declare boolean used for dual VideoCrypt mode
Dim DualVCMode As Boolean

' Declare a variable for storing the default sample rate for the selected video mode
' This allows us to revert back to the default if the sample rate is changed by filters or scrambling systems
Dim DefaultSampleRate As String

' Declare a variable which we set if the CPU C-state option is set
Dim CStateSettingChanged As Boolean

' Declare all variables used for storing parameters
Dim inputsource As String
Dim freq As String
Dim sys As String
Dim nicamstatus As String
Dim audiostatus As String
Dim acpstatus As String
Dim repeatstatus As String
Dim wssstatus As String
Dim encryptiontype As String
Dim encryptionkey As String
Dim audioencryption As String
Dim TeletextFlag As String
Dim teletextsource As String
Dim rfAmpFlag As String
Dim sr As String
Dim fmdevargument As String
Dim fmdevvalue As String
Dim gammaParam As String
Dim outputlevelparam As String
Dim filterparam As String
Dim positionparam As String
Dim timestampParam As String
Dim logoParam As String
Dim logoPath As String
Dim verboseParam As String
Dim EMMParam As String
Dim TruncatedCardNumber As String
Dim ShowECMParam As String
Dim SubtitlesParam As String
Dim ScalingMode As String
Dim Interlaced As String
Dim ShowCardSerial As String
Dim FindKey As String
Dim VITS As String
Dim chid As String

' Initialise XP-ish window styles
' Also initialise a workaround for a bug which causes a VB6 application to crash on exit if
' a UserControl is loaded when InitCommonControls is specified.
Private Declare Function IsUserAnAdmin Lib "shell32" Alias "#680" () As Integer
Private Declare Function InitCommonControls Lib "comctl32.dll" () As Long

Private Sub Form_Initialize()
' Don't initialise common controls (for visual styles) on Wine as it doesn't need it.
' This also prevents the above mentioned VB6 bug from affecting Wine.
    If IsWine = False Then
' Set a variable so we don't need to run the Wine API check every time
        RunningOnWine = False
        IsUserAnAdmin
        InitCommonControls
    Else
' Set a variable so we don't need to run the Wine API check every time
        RunningOnWine = True
    End If
' Check if a previous instance is still running. If so, then exit.
    If App.PrevInstance = True Then
        MsgBox "Another instance of hacktv-gui is already running.", vbCritical, App.Title
        End
    End If
' Realign text labels to make things look better.
    LblSubtitleIndex.Top = TxtSubtitleIndex.Top + ((TxtSubtitleIndex.Height - LblSubtitleIndex.Height) + 24)
    LblSampleRate.Top = SampleRate.Top + ((SampleRate.Height - LblSampleRate.Height) + 24)
    LblFrequency.Top = frequency_mhz.Top + (frequency_mhz.Height - LblFrequency.Height)
    ChkLogo.Top = LogoFolder.Top + ((LogoFolder.Height - ChkLogo.Height) / 2)
    ChkTimestamp.Top = LogoFolder.Top + ((LogoFolder.Height - ChkLogo.Height) / 2)
    ChkPosition.Top = positionValue.Top + ((positionValue.Height - ChkPosition.Height) / 2)
    ChkRepeat.Top = positionValue.Top + ((positionValue.Height - ChkPosition.Height) / 2)
End Sub

Private Sub Form_Load()
    If RunningOnWine = False Then
' Check status of CPU idle state setting
    If GetSetting(App.EXEName, "Settings", "RestrictCPUIdleStates") = "1" Then ChkRestrictCPUIdleStates.Checked = True
' Set default Windows paths here
        DefaultHackTVPath = App.Path
        HackTVEXEName = "hacktv.exe"
    ElseIf RunningOnWine = True Then
' Hide CPU idle state option
        ChkRestrictCPUIdleStates.Enabled = False
        ChkRestrictCPUIdleStates.Visible = False
' Set default Wine paths here
        DefaultHackTVPath = "Z:\usr\local\bin"
        DefaultTerminalPath = "Z:\bin"
        HackTVEXEName = "hacktv"
    End If
' End set paths
' Retrieve registry value which contains the hacktv path
    HackTVPath = GetSetting(App.Title, "Settings", "HackTVPath", "")
' If the value doesn't exist, set the location to the default directory
    If HackTVPath = "" Then HackTVPath = DefaultHackTVPath
' Specifies the help file to be invoked when the F1 key is pressed
    App.HelpFile = App.Path & Chr(92) & "hacktv-gui.chm"
' Try to detect what fork is selected
    Call detectfork
' While not essential in VB6 (it is recommended in VB.NET), we don't select anything in Design mode.
' So this loads default values (local source file, PAL video, UHF channels and audio enabled).
    ChkAudio.Value = vbChecked
    UHF.Value = True
    PAL.Value = True
    LocalSource.Value = True
' Populate MRU list from registry
    Call CheckMRUList
' If a command line parameter has been specified, call the Open function to read it
    If Not Command = "" Then Call OpenFile_Click
End Sub

Public Sub detectfork()
    ' Dim position As Long
    Dim fData As String
    Dim filesize As Long
    iFileNo = FreeFile
' If hacktv cannot be found at the specified path, call the fsphil sub so we don't crash
    If DoesFileExist(HackTVPath & Chr(92) & HackTVEXEName) = False Then
        Call fsphil
        Exit Sub
    End If
' Check if the file is locked
    If IsFileInUse(HackTVPath & Chr(92) & HackTVEXEName) = True Then
        MsgBox "hacktv cannot be opened. It may be locked by another process or you may not have permission to access it.", vbCritical, App.Title
        Call fsphil
        Exit Sub
    End If
' Check the file size of the hacktv/hacktv.exe file in the specified path
' If larger than 24MB, call the fsphil sub and don't go any further as we risk buffer overflows
' Plus, it's a sign that the wrong file was specified because hacktv.exe is normally about 19MB
    filesize = FileLen(HackTVPath & Chr(92) & HackTVEXEName)
    If filesize > 25165824 Then
        MsgBox "The specified hacktv file is too large. Please ensure that you have selected the correct location.", vbCritical, App.Title
        Call fsphil
        Exit Sub
    End If
' Open hacktv.exe, set its contents as a string called fdata and then close it
' This can be quite memory intensive (roughly 40MB of RAM) so blank the string once we get a result
    Open HackTVPath & Chr(92) & HackTVEXEName For Binary As #iFileNo
    fData = Space$(LOF(1))
    Get #iFileNo, 1, fData
    Close #iFileNo
' Find something that exists in Captain Jack's fork and not present in fsphil's
' In this case, we'll use the enableemm switch
    If InStr(fData, "enableemm") > 0 Then
' Blank the string to save memory, then call the captainjack sub
        fData = ""
        Call captainjack
    Else
' Blank the string to save memory, then call the fsphil sub
        fData = ""
        Call fsphil
    End If
End Sub

Private Sub fsphil()
' Disable features unsupported in fsphil's build
    ChkTimestamp.Enabled = False
    ChkTimestamp.Value = vbUnchecked
    ChkLogo.Value = vbUnchecked
    ChkLogo.Enabled = False
    ChkSubtitles.Value = vbUnchecked
    ChkSubtitles.Enabled = False
    ChkARCorrection.Value = vbUnchecked
    ChkARCorrection.Enabled = False
    ARCorrectionMode.ListIndex = -1
    forktype = ""
    Call DisablePosition
    If PAL.Value = True Then Call AddPALEncryptionTypes
    If SECAM.Value = True Then Call AddPALEncryptionTypes
    If MAC.Value = True Then Call AddMACEncryptionTypes
End Sub

Private Sub captainjack()
' Enable features supported in Captain Jack's build
    If TestCard.Value = False Then
        ChkPosition.Enabled = True
        ChkTimestamp.Enabled = True
        Call EnablePosition
        ChkLogo.Enabled = True
        ChkSubtitles.Enabled = True
        ChkARCorrection.Enabled = True
        If ARCorrectionMode.ListCount = 0 Then Call AddAspectRatioScalingOptions
        ARCorrectionMode.ListIndex = 0
    End If
' Set the forktype variable so we know what features to enable or disable later
    forktype = "CJ"
    If PAL.Value = True Then Call AddPALEncryptionTypes
    If SECAM.Value = True Then Call AddPALEncryptionTypes
    If MAC.Value = True Then Call AddMACEncryptionTypes
End Sub

Private Sub ResetAllSettings_Click()
' Deletes all registry settings and exits the application
' Use this to either uninstall the application (so we leave the system as we found it), or for troubleshooting
    Dim ResetSettingsPrompt As VbMsgBoxResult
    ResetSettingsPrompt = MsgBox("This option will remove all of this application's settings from the registry " _
    & "and exit." & vbCrLf & "Do you wish to continue?", vbQuestion + vbYesNo, App.Title)
    If ResetSettingsPrompt = vbYes Then
    ' Delete reg key
        On Error Resume Next
        DeleteSetting (App.Title)
        On Error GoTo 0
    ' Exit application
        Unload Me
    End If
End Sub

Private Sub CheckMRUList()
    Dim ConfigFile1 As String
    Dim ConfigFile2 As String
    Dim ConfigFile3 As String
    Dim ConfigFile4 As String
    file1.Visible = False
    file2.Visible = False
    file3.Visible = False
    file4.Visible = False
    MRUbar.Visible = False
    ClearMRUList.Enabled = False
    ConfigFile1 = GetSetting(App.Title, "Settings", "File1")
    ConfigFile2 = GetSetting(App.Title, "Settings", "File2")
    ConfigFile3 = GetSetting(App.Title, "Settings", "File3")
    ConfigFile4 = GetSetting(App.Title, "Settings", "File4")
    If Not ConfigFile1 = "" Then
        MRUbar.Visible = True
        file1.Enabled = True
        file1.Caption = ConfigFile1
        file1.Visible = True
        ClearMRUList.Enabled = True
    End If
    If Not ConfigFile2 = "" Then
        MRUbar.Visible = True
        file2.Enabled = True
        file2.Caption = ConfigFile2
        file2.Visible = True
        ClearMRUList.Enabled = True
    End If
    If Not ConfigFile3 = "" Then
        MRUbar.Visible = True
        file3.Enabled = True
        file3.Caption = ConfigFile3
        file3.Visible = True
        ClearMRUList.Enabled = True
    End If
    If Not ConfigFile4 = "" Then
        MRUbar.Visible = True
        file4.Enabled = True
        file4.Caption = ConfigFile4
        file4.Visible = True
        ClearMRUList.Enabled = True
    End If
    If ConfigFile1 = "" And ConfigFile2 = "" And ConfigFile3 = "" And ConfigFile4 = "" Then
        file1.Visible = False
        file2.Visible = False
        file3.Visible = False
        file4.Visible = False
        MRUbar.Visible = False
        ClearMRUList.Enabled = False
    End If
End Sub

Private Sub UpdateMRUList(FilePath As String)
' This sub adds the specified file path to the #1 position in the MRU list, and shifts everything else down
    Dim ConfigFile1 As String
    Dim ConfigFile2 As String
    Dim ConfigFile3 As String
    Dim ConfigFile4 As String
    ConfigFile1 = GetSetting(App.Title, "Settings", "File1")
    ConfigFile2 = GetSetting(App.Title, "Settings", "File2")
    ConfigFile3 = GetSetting(App.Title, "Settings", "File3")
    ConfigFile4 = GetSetting(App.Title, "Settings", "File4")
    If FilePath = ConfigFile2 Then
        SaveSetting App.Title, "Settings", "File2", ConfigFile1
        SaveSetting App.Title, "Settings", "File1", FilePath
        Call CheckMRUList
    ElseIf FilePath = ConfigFile3 Then
        SaveSetting App.Title, "Settings", "File3", ConfigFile2
        SaveSetting App.Title, "Settings", "File2", ConfigFile1
        SaveSetting App.Title, "Settings", "File1", FilePath
        Call CheckMRUList
    ElseIf FilePath = ConfigFile4 Then
        SaveSetting App.Title, "Settings", "File4", ConfigFile3
        SaveSetting App.Title, "Settings", "File3", ConfigFile2
        SaveSetting App.Title, "Settings", "File2", ConfigFile1
        SaveSetting App.Title, "Settings", "File1", FilePath
        Call CheckMRUList
    ElseIf FilePath = ConfigFile1 Then
        ' No need to do anything
    Else
        SaveSetting App.Title, "Settings", "File4", ConfigFile3
        SaveSetting App.Title, "Settings", "File3", ConfigFile2
        SaveSetting App.Title, "Settings", "File2", ConfigFile1
        SaveSetting App.Title, "Settings", "File1", FilePath
        Call CheckMRUList
    End If
End Sub

Private Sub ClearMRUList_Click()
    Dim ClearMRUPrompt As VbMsgBoxResult
    ClearMRUPrompt = MsgBox("This will clear the list of most recently used files from the File menu. Do you wish to continue?", _
    vbQuestion + vbYesNo, App.Title)
    If ClearMRUPrompt = vbYes Then
        If Not GetSetting(App.Title, "Settings", "File4") = "" Then DeleteSetting App.Title, "Settings", "File4"
        If Not GetSetting(App.Title, "Settings", "File3") = "" Then DeleteSetting App.Title, "Settings", "File3"
        If Not GetSetting(App.Title, "Settings", "File2") = "" Then DeleteSetting App.Title, "Settings", "File2"
        If Not GetSetting(App.Title, "Settings", "File1") = "" Then DeleteSetting App.Title, "Settings", "File1"
        Call CheckMRUList
        ClearMRUList.Enabled = False
    End If
End Sub

Private Sub GenerateOnly_Click()
    If GenerateOnly.Checked = False Then
        GenerateOnly.Checked = True
        BtnRun.Caption = "Generate syntax"
        ' Disable the "close on exit" option when "generate only" is enabled
        CloseOnExit.Checked = False
        CloseOnExit.Enabled = False
    Else
        GenerateOnly.Checked = False
        BtnRun.Caption = "Run hacktv..."
        ' Re-enable the "close on exit" option
        CloseOnExit.Enabled = True
    End If
End Sub

Private Sub HelpMenu_Click()
' If the help file above does not exist in the application directory, grey out the index menu option
    If DoesFileExist(App.HelpFile) = False Then
        HelpIndex.Enabled = False
    Else
        HelpIndex.Enabled = True
    End If
End Sub

Private Sub HelpIndex_Click()
    ShellExecute Me.hWnd, vbNullString, "hh.exe", App.HelpFile, vbNullString, SW_SHOWNORMAL
End Sub

Private Sub exit_Click()
    Unload Me
End Sub

Private Sub about_Click()
    Dim lblTitle As String
    Dim lblDescription As String
    lblTitle = "About " & App.Title
    lblDescription = "A GUI frontend for hacktv" & vbCrLf & "Version " & App.Major & "." & App.Minor _
    & vbCrLf & "Created 2019-2020 by Stephen McGarry" & vbCrLf & vbCrLf _
    & "This application has no affiliation with hacktv's developers and is provided for your convenience."
    MsgBox lblDescription, vbInformation, lblTitle
End Sub

Private Sub ChkVITS_Click()
    If ChkVITS.Value = vbChecked Then
        VITS = "--vits"
    Else
        VITS = ""
    End If
End Sub

Private Sub Astra975Template_Click()
    Call AstraTemplate(9.75)
End Sub

Private Sub Astra10Template_Click()
    Call AstraTemplate(10)
End Sub

Private Sub AstraTemplate(LocalOscillator As Variant)
    Dim AstraTemplatePrompt As VbMsgBoxResult
    AstraTemplatePrompt = MsgBox("This will load template values for an Astra satellite receiver configured for a " & LocalOscillator & " GHz LO LNB." & vbCrLf & "All current settings will be cleared. Do you wish to continue?", vbQuestion + vbYesNo, App.Title)
    If AstraTemplatePrompt = vbNo Then Exit Sub
' Clear all current settings
    Call NewFile_Click
' Select PAL-FM mode
    PAL.Value = True
    VideoFormat.ListIndex = 2
' Enable pre-emphasis filter and set FM deviation to 16 MHz
    ChkVideoFilter.Value = vbChecked
    ChkFMDev.Value = vbChecked
    fm_deviation.Text = 10
' Set IF to Sky News
    If LocalOscillator = 9.75 Then frequency_mhz.Text = 1627
    If LocalOscillator = 10 Then frequency_mhz.Text = 1377
    MsgBox "Template values have been loaded. Tune your receiver to Sky News (11.377 GHz) and run hacktv.", vbInformation, App.Title
End Sub

Private Sub BSBTemplate_Click()
    Dim BSBTemplatePrompt As VbMsgBoxResult
    BSBTemplatePrompt = MsgBox("This will load template values for a BSB satellite receiver." & vbCrLf & "All current settings will be cleared. Do you wish to continue?", vbQuestion + vbYesNo, App.Title)
    If BSBTemplatePrompt = vbNo Then Exit Sub
' Clear all current settings
    Call NewFile_Click
' Select D-MAC FM mode
    MAC.Value = True
    VideoFormat.ListIndex = 3
' Enable pre-emphasis filter and set FM deviation to 16 MHz
    ChkVideoFilter.Value = vbChecked
    ChkFMDev.Value = vbChecked
    fm_deviation.Text = 16
' Set to Galaxy channel
    frequency_ch.ListIndex = 1
    MsgBox "Template values have been loaded. Tune your receiver to the Galaxy channel, or change this in the channel dropdown box.", vbInformation, App.Title
End Sub

Public Sub RestrictCPUIdleStates(Enable As Boolean)
' This sub enables or disables CPU C-states from C2 onwards. Some systems are very aggressive with CPU power
' management and this can cause timing issues with hacktv, resulting in choppy video at high sample rates.
    If Enable = True Then
' Run powercfg.exe, set the max C-state to 1, then reapply the Balanced power profile with the new setting.
        ShellExecute Me.hWnd, vbNullString, Environ$("WINDIR") & "\system32\powercfg.exe", "/setacvalueindex 381b4222-f694-41f0-9685-ff5bb260df2e 54533251-82be-4824-96c1-47b60b740d00 9943e905-9a30-4ec1-9b99-44dd3b76f7a2 1", vbNullString, SW_SHOWMINIMIZED
        ShellExecute Me.hWnd, vbNullString, Environ$("WINDIR") & "\system32\powercfg.exe", "/setactive 381b4222-f694-41f0-9685-ff5bb260df2e", vbNullString, SW_SHOWMINIMIZED
        CStateSettingChanged = True
    ElseIf Enable = False Then
' Run powercfg.exe, set the max C-state to 0 (any), then reapply the Balanced power profile with the new setting.
        ShellExecute Me.hWnd, vbNullString, Environ$("WINDIR") & "\system32\powercfg.exe", "/setacvalueindex 381b4222-f694-41f0-9685-ff5bb260df2e 54533251-82be-4824-96c1-47b60b740d00 9943e905-9a30-4ec1-9b99-44dd3b76f7a2 0", vbNullString, SW_SHOWMINIMIZED
        ShellExecute Me.hWnd, vbNullString, Environ$("WINDIR") & "\system32\powercfg.exe", "/setactive 381b4222-f694-41f0-9685-ff5bb260df2e", vbNullString, SW_SHOWMINIMIZED
        CStateSettingChanged = False
    End If
End Sub

Private Sub ChkSubtitles_Click()
    If ChkSubtitles.Value = vbChecked Then
        LblSubtitleIndex.Enabled = True
        TxtSubtitleIndex.Enabled = True
        TxtSubtitleIndex.BackColor = vbWindowBackground
        SubtitlesParam = "--subtitles"
    Else
        LblSubtitleIndex.Enabled = False
        TxtSubtitleIndex.Text = ""
        TxtSubtitleIndex.Enabled = False
        TxtSubtitleIndex.BackColor = vbButtonFace
        SubtitlesParam = ""
    End If
End Sub

Private Sub ChkARCorrection_Click()
    If ChkARCorrection.Value = vbChecked Then
        ARCorrectionMode.Enabled = True
    Else
        ARCorrectionMode.Enabled = False
        ARCorrectionMode.ListIndex = 0
    End If
End Sub

Private Sub AddAspectRatioScalingOptions()
' Populate the ARCorrectionMode combobox with the arguments for scaling 16:9 content to fit a 4:3 display
' The index integer value is read later and translated to the correct command line parameters
    With ARCorrectionMode
        ' Default behaviour, stretches the video vertically to cover the entire screen
        .AddItem "Stretched"
        .ItemData(.NewIndex) = "2101"
        ' Letterboxes the video by adding black bars to the top and bottom
        .AddItem "Letterboxed"
        .ItemData(.NewIndex) = "2102"
        ' Crops the video by zooming it to fit the 4:3 display and losing the sides
        .AddItem "Cropped"
        .ItemData(.NewIndex) = "2103"
    End With
End Sub

Private Sub AddWSSModes()
' Populate the wss_mode combobox with the arguments for widescreen signalling (WSS)
' The index integer value is read later and translated to the correct command line parameters
    With wss_mode
        .Clear
        .AddItem "auto"
        .ItemData(.NewIndex) = "501"
        .AddItem "4:3"
        .ItemData(.NewIndex) = "502"
        .AddItem "16:9"
        .ItemData(.NewIndex) = "503"
        .AddItem "14:9 letterbox"
        .ItemData(.NewIndex) = "504"
        .AddItem "16:9 letterbox"
        .ItemData(.NewIndex) = "505"
    End With
End Sub

Private Sub frequency_ch_Click()
' Call a sub to populate the frequency_mhz textbox with the value from the frequency_ch combobox
    Call SetFrequency
End Sub

Private Sub Encryption_Key_Click()
    Call CheckEncryptionKey
End Sub

Private Sub ChkRestrictCPUIdleStates_Click()
    If ChkRestrictCPUIdleStates.Checked = True Then
        SaveSetting App.EXEName, "Settings", "RestrictCPUIdleStates", CInt(ChkRestrictCPUIdleStates.Checked) + 1
        ChkRestrictCPUIdleStates.Checked = False
    Else
        SaveSetting App.EXEName, "Settings", "RestrictCPUIdleStates", CInt(ChkRestrictCPUIdleStates.Checked) + 1
        ChkRestrictCPUIdleStates.Checked = True
    End If
End Sub

Private Sub UpdateCheck_Click()
' Set variables
Dim DownloadURL As String
    DownloadURL = "https://raw.githubusercontent.com/steeviebops/hacktv-gui/master/hacktv-gui.vbp"
Dim FilePath As String
    FilePath = Environ$("temp") & "\update.vbp"
Dim strText As String
Dim MajorVersion() As String
Dim MinorVersion() As String
Dim LatestVersion As String
Dim UpdatePrompt As VbMsgBoxResult
UpdatePrompt = MsgBox("This will check for a new version of the application. We do this by downloading " _
& "part of the source from Github. No personal information is transmitted." & vbCrLf _
& "Do you wish to continue?", vbYesNo + vbQuestion, App.Title)
If UpdatePrompt = vbNo Then Exit Sub
    NetGrab2.DownloadStart DownloadURL, vbAsyncReadSynchronousDownload
' Check if file exists
    If DoesFileExist(FilePath) = False Then
        MsgBox "Unable to check for updates at this time. Please ensure that you are connected to the internet " _
        & "and try again.", vbCritical, App.Title
        Exit Sub
    End If
' Open the file we downloaded and load it to a string
    Open FilePath For Input As #iFileNo
    strText = Input(LOF(1), #iFileNo)
    Close #iFileNo
' Delete the downloaded file as we no longer need it
    Kill FilePath
' Check the string for the MajorVer and MinorVer lines, then load their values to arrays
    Between strText, "MajorVer=", vbLf, MajorVersion
    Between strText, "MinorVer=", vbLf, MinorVersion
    LatestVersion = MajorVersion(0) & Chr(46) & MinorVersion(0)
' If version in VBP file is newer than app version then notify
    If LatestVersion > App.Major & "." & App.Minor Then
        MsgBox "Current version: " & App.Major & Chr(46) & App.Minor & vbCrLf _
        & "Version available on Github: " & LatestVersion & vbCrLf & vbCrLf _
        & "A new version is now available. Please visit the Github page to download.", vbInformation, App.Title
    ElseIf LatestVersion <= App.Major & "." & App.Minor Then
        MsgBox "Current version: " & App.Major & Chr(46) & App.Minor & vbCrLf _
        & "Version available on Github: " & LatestVersion & vbCrLf & vbCrLf _
        & "No updates are available at this time.", vbInformation, App.Title
    End If
End Sub

Private Sub NetGrab2_DownloadComplete(ByVal nBytes As Long)
Dim FilePath As String
    FilePath = Environ$("temp") & "\update.vbp"
    Call NetGrab2.SaveAs(FilePath)
End Sub

Private Sub vc1key_Click()
    If vc2key.ListIndex = "-1" Then
        vc2key.ListIndex = "0"
    Else
        Call CheckDualEncryptionKey
    End If
End Sub

Private Sub vc2key_Click()
    Call CheckDualEncryptionKey
End Sub

Private Sub ChkOutputLevel_Click()
    If ChkOutputLevel.Value = vbChecked Then
        outputlevelvalue.Enabled = True
        outputlevelvalue.BackColor = vbWindowBackground
        outputlevelparam = "--level"
    Else
        outputlevelvalue.Enabled = False
        outputlevelvalue.BackColor = vbButtonFace
        outputlevelparam = ""
        outputlevelvalue.Text = ""
    End If
End Sub

Private Sub ChkGamma_Click()
    If ChkGamma.Value = vbChecked Then
        gammavalue.Enabled = True
        gammavalue.BackColor = vbWindowBackground
        gammaParam = "--gamma"
    Else
        gammavalue.Enabled = False
        gammavalue.BackColor = vbButtonFace
        gammavalue.Text = ""
        gammaParam = ""
    End If
End Sub

Private Sub ChkShowSerial_Click()
    If ChkShowSerial.Value = vbChecked Then
        ShowCardSerial = "--showserial"
    Else
        ShowCardSerial = ""
    End If
End Sub

Private Sub ChkFindKey_Click()
    If ChkFindKey.Value = vbChecked Then
        FindKey = "--findkey"
    Else
        FindKey = ""
    End If
End Sub

Private Sub ChkInterlace_Click()
    If ChkInterlace.Value = vbChecked Then
        Interlaced = "--interlace"
    Else
        Interlaced = ""
    End If
End Sub

Private Sub EnablePosition()
    ChkPosition.Enabled = True
End Sub

Private Sub DisablePosition()
    ChkPosition.Enabled = False
    ChkPosition.Value = vbUnchecked
    positionValue.Enabled = False
    positionValue.BackColor = vbButtonFace
    positionValue.Text = ""
End Sub

Private Sub PathSettings_Click()
    SettingsForm.Show vbModal
End Sub

Private Sub ChkPosition_Click()
    If ChkPosition.Value = vbChecked Then
        positionValue.Enabled = True
        positionValue.BackColor = vbWindowBackground
        positionparam = "--position"
    Else
        positionValue.Enabled = False
        positionValue.BackColor = vbButtonFace
        positionparam = ""
        positionValue.Text = ""
    End If
End Sub

Private Sub ChkVerbose_Click()
    If ChkVerbose.Value = vbChecked Then
        verboseParam = "-v"
    Else
        verboseParam = ""
    End If
End Sub

Private Sub ChkShowECM_Click()
    If ChkShowECM.Value = vbChecked Then
        ShowECMParam = "--showecm"
    Else
        ShowECMParam = ""
    End If
End Sub

Private Sub CloseOnExit_Click()
' If "don't close on exit" is enabled or disabled, set the closeonexit variable accordingly
    If CloseOnExit.Checked = False Then
        CloseOnExit.Checked = True
    Else
        CloseOnExit.Checked = False
    End If
End Sub

Private Sub BtnTeletextDownload_Click()
    TeletextDownload.Show vbModal
End Sub

Private Sub ChkTimestamp_Click()
    If ChkTimestamp.Value = vbChecked Then
        timestampParam = "--timestamp"
    Else
        timestampParam = ""
    End If
End Sub

' The following subs ensure that only numeric (or decimal) values can be entered in the specified textboxes
Private Sub TxtSubtitleIndex_keypress(KeyAscii As Integer)
    Call CheckInput(KeyAscii)
End Sub

Private Sub positionValue_keypress(KeyAscii As Integer)
    Call CheckInput(KeyAscii)
End Sub

Private Sub CardNumber_KeyPress(KeyAscii As Integer)
    Call CheckInput(KeyAscii)
End Sub

Private Sub BtnTeletextBrowseFile_Click()
' Opens a file open dialog box to specify the location of teletext files
    Dim sFile As String
        If (VBGetOpenFileName(sFile, , True, False, , True, "All teletext files (*.tti, *.t42)|*.tti;*.t42|Teletext page (*.tti)|*.tti|Teletext container (*.t42)|*.t42|All Files (*.*)|*.*", 0, , "Choose File", , Me.hWnd)) Then
            teletext_source.Text = sFile
        End If
    Exit Sub
End Sub

Private Sub txgain_KeyPress(KeyAscii As Integer)
    Call CheckInput(KeyAscii)
End Sub

Private Sub frequency_mhz_KeyPress(KeyAscii As Integer)
    Call CheckInputDecimal(KeyAscii, frequency_mhz.Text)
End Sub

Private Sub SampleRate_KeyPress(KeyAscii As Integer)
    Call CheckInputDecimal(KeyAscii, SampleRate.Text)
End Sub

Private Sub gammavalue_KeyPress(KeyAscii As Integer)
    Call CheckInputDecimal(KeyAscii, gammavalue.Text)
End Sub

Private Sub outputlevelvalue_KeyPress(KeyAscii As Integer)
    Call CheckInputDecimal(KeyAscii, outputlevelvalue.Text)
End Sub

Private Sub fm_deviation_KeyPress(KeyAscii As Integer)
    Call CheckInputDecimal(KeyAscii, fm_deviation.Text)
End Sub

Private Sub CheckInput(KeyAscii As Integer)
' Ensure that only numeric characters can be entered into the textbox
    If Not IsNumeric(Chr(KeyAscii)) And Not KeyAscii = 8 Then
        KeyAscii = 0
    End If
End Sub

Private Sub CheckInputDecimal(KeyAscii As Integer, TextBoxToCheck As String)
' Ensure that only numeric characters and one decimal point can be entered into the textbox
    If Not IsNumeric(Chr(KeyAscii)) And Not KeyAscii = 8 Then
        If KeyAscii = 46 Then
            If Not InStr(TextBoxToCheck, Chr(46)) = 0 Then KeyAscii = 0
        Else
            KeyAscii = 0
        End If
    End If
End Sub

Private Sub NewFile_Click()
' Adapted from http://www.vbforums.com/showthread.php?333003-VB6-Clear-Controls-on-a-Form
' Reset all controls and reinitialise
    On Error GoTo Err
    Dim Control As Object
    
    Call DisableEncryption
    
    M3USource.Visible = False
    input_source.Visible = True
    M3USourceFile = False
    
    For Each Control In Me.Controls
        Control.Text = ""
        Control.Value = ""
    Next
    
    CloseOnExit.Checked = False
    Call AddWSSModes
    If forktype = "" Then Call fsphil
    If forktype = "CJ" Then Call captainjack
    ChkAudio.Value = vbChecked
    UHF.Value = True
    PAL.Value = True
    LocalSource.Value = True
    txgain.Text = 0
    If Not TitleBar = "" Then Caption = TitleBar
    SaveFile.Caption = "Save..."
    ConfigFileName = ""
    If GenerateOnly.Checked = True Then Call GenerateOnly_Click
    Exit Sub
    
Err:
    If Err.Number = 438 Then
        Resume Next
    ElseIf Err.Number = 13 Then
        Control.Value = False
        Resume Next
    ElseIf Err.Number = 383 Then
        Control.Clear
        Resume Next
    Else
        Resume Next
    End If
End Sub

Private Sub SaveFile_Click()
    If ConfigFileName = "" Then
        ' Spawn an open file dialog box to browse for a settings file
        If Not (VBGetSaveFileName(ConfigFileName, , True, "hacktv-gui configuration file (*.htv)|*.htv|All Files (*.*)|*.*", 1, , "Save File", "htv", Me.hWnd)) Then Exit Sub
        ' Pass the selected file to the save sub
        Call SaveConfigFile(ConfigFileName)
    Else
        ' Pass the existing file name to the save sub
        Call SaveConfigFile(ConfigFileName)
    End If
End Sub

Private Sub SaveFileAs_Click()
    ' Spawn an open file dialog box to browse for a settings file
    If Not (VBGetSaveFileName(ConfigFileName, , True, "hacktv-gui configuration file (*.htv)|*.htv|All Files (*.*)|*.*", 1, , "Save File", "htv", Me.hWnd)) Then Exit Sub
    Call SaveConfigFile(ConfigFileName)
End Sub

Private Sub SaveConfigFile(ByVal FileName As String)
    ' Write file structure
    Open FileName For Output As #iFileNo
    Print #iFileNo, "[hacktv]" & vbCrLf & vbCrLf & "[hacktv-gui3]"
    Close #iFileNo
    ' Save current fork if applicable
    If forktype = "CJ" Then WriteIniValue FileName, "hacktv-gui3", "fork", "CaptainJack"
    ' Input source or test card
    If input_source.Enabled = False Then
        WriteIniValue FileName, "hacktv", "input", "test:colourbars"
    ElseIf LCase(Right$(input_source.Text, 4)) = ".m3u" Then
        Dim m3uarrayindex As Integer
        m3uarrayindex = M3USource.ItemData(M3USource.ListIndex)
        WriteIniValue FileName, "hacktv", "input", URL(m3uarrayindex)
        WriteIniValue FileName, "hacktv-gui3", "m3usource", input_source.Text
        WriteIniValue FileName, "hacktv-gui3", "m3uindex", M3USource.ListIndex
    Else
        WriteIniValue FileName, "hacktv", "input", input_source.Text
    End If
    ' Video format
    WriteIniValue FileName, "hacktv", "mode", sys
    ' Frequency
    WriteIniValue FileName, "hacktv-gui3", "channel", frequency_ch.Text
    WriteIniValue FileName, "hacktv", "frequency", frequency_mhz.Text * 1000000
    ' Sample rate
    WriteIniValue FileName, "hacktv", "samplerate", SampleRate.Text * 1000000
    ' Gain
    WriteIniValue FileName, "hacktv", "gain", txgain.Text
    ' RF Amp
    If ChkRFAmp.Value = vbChecked Then WriteIniValue FileName, "hacktv", "amp", ChkRFAmp.Value
    ' Output level
    If ChkOutputLevel.Value = vbChecked Then WriteIniValue FileName, "hacktv", "level", outputlevelvalue.Text
    ' FM deviation
    If ChkFMDev.Value = vbChecked Then WriteIniValue FileName, "hacktv", "deviation", fm_deviation.Text * 1000000
    ' Gamma
    If ChkGamma.Value = vbChecked Then WriteIniValue FileName, "hacktv", "gamma", gammavalue.Text
    ' Repeat
    If ChkRepeat.Value = vbChecked Then WriteIniValue FileName, "hacktv", "repeat", ChkRepeat.Value
    ' Position
    If ChkPosition.Value = vbChecked Then WriteIniValue FileName, "hacktv", "position", positionValue.Text
    ' Verbose
    If ChkVerbose.Value = vbChecked Then WriteIniValue FileName, "hacktv", "verbose", ChkVerbose.Value
    ' Logo
    If ChkLogo.Value = vbChecked Then WriteIniValue FileName, "hacktv", "logo", LogoFolder.Text
    ' Timestamp
    If ChkTimestamp.Value = vbChecked Then WriteIniValue FileName, "hacktv", "timestamp", ChkTimestamp.Value
    ' Interlace
    If ChkInterlace.Value = vbChecked Then WriteIniValue FileName, "hacktv", "interlace", ChkInterlace.Value
    ' Teletext
    If LCase(Right$(teletext_source.Text, 4)) = ".t42" Then
        WriteIniValue FileName, "hacktv", "teletext", "raw:" & teletext_source.Text
    ElseIf Not teletext_source.Text = "" Then
        WriteIniValue FileName, "hacktv", "teletext", teletext_source.Text
    End If
    ' WSS
    ' We increase the value by one, because zero is interpreted as "option disabled" while 1 is
    ' interpreted as "auto". We will subtract this again when opening.
    If ChkWSS.Value = vbChecked Then WriteIniValue FileName, "hacktv", "wss", wss_mode.ListIndex + 1
    ' AR Correction
    If ChkARCorrection.Value = vbChecked Then WriteIniValue FileName, "hacktv", "arcorrection", ARCorrectionMode.ListIndex
    ' Encryption
    If DualVCMode = False Then
        If encryptiontype = "--single-cut" Or encryptiontype = "--double-cut" Then
            WriteIniValue FileName, "hacktv", "scramblingtype", Mid$(encryptiontype, 3)
            WriteIniValue FileName, "hacktv", "scramblingkey", Mid$(encryptionkey, 3)
        ElseIf Not encryption_type.ListIndex = 0 Then
            WriteIniValue FileName, "hacktv", "scramblingtype", Mid$(encryptiontype, 3)
            WriteIniValue FileName, "hacktv", "scramblingkey", encryptionkey
        End If
    ElseIf DualVCMode = True Then
        WriteIniValue FileName, "hacktv", "scramblingtype", "videocrypt1+2"
        WriteIniValue FileName, "hacktv", "scramblingkey", Mid$(encryptiontype, 14)
        WriteIniValue FileName, "hacktv", "scramblingkey2", Mid$(encryptionkey, 15)
    End If
    ' EMM
    If ChkEnableEMM.Value = vbChecked Then
        WriteIniValue FileName, "hacktv", "emm", "1"
        If Len(CardNumber.Text) = 9 Then
            WriteIniValue FileName, "hacktv", "cardnumber", Left$(CardNumber.Text, Len(CardNumber.Text) - 1)
        ElseIf Len(CardNumber.Text) = 13 Then
            WriteIniValue FileName, "hacktv", "cardnumber", Mid$(CardNumber.Text, 5, 8)
            WriteIniValue FileName, "hacktv-gui3", "13digitprefix", Left(CardNumber.Text, 4)
        End If
    ElseIf ChkDisableEMM.Value = vbChecked Then
        WriteIniValue FileName, "hacktv", "emm", "2"
        If Len(CardNumber.Text) = 9 Then
            WriteIniValue FileName, "hacktv", "cardnumber", Left$(CardNumber.Text, 8)
        ElseIf Len(CardNumber.Text) = 13 Then
            WriteIniValue FileName, "hacktv", "cardnumber", Mid$(CardNumber.Text, 5, 8)
            WriteIniValue FileName, "hacktv-gui3", "13digitprefix", Left(CardNumber.Text, 4)
        End If
    End If
    ' Show card serial
    If ChkShowSerial.Value = vbChecked Then WriteIniValue FileName, "hacktv", "showserial", ChkShowSerial.Value
    ' Brute force PPV key
    If ChkFindKey.Value = vbChecked Then WriteIniValue FileName, "hacktv", "findkey", ChkFindKey.Value
    ' Encrypt audio
    If ChkEncryptAudio.Value = vbChecked Then WriteIniValue FileName, "hacktv", "scramble-audio", ChkEncryptAudio.Value
    ' ACP
    If ChkACP.Value = vbChecked Then WriteIniValue FileName, "hacktv", "acp", ChkACP.Value
    ' Filter
    If ChkVideoFilter.Value = vbChecked Then WriteIniValue FileName, "hacktv", "filter", ChkVideoFilter.Value
    ' Audio
    If ChkAudio.Value = vbChecked Then WriteIniValue FileName, "hacktv", "audio", ChkAudio.Value
    ' NICAM
    If ChkNICAM.Value = vbChecked Then WriteIniValue FileName, "hacktv", "nicam", ChkNICAM.Value
    ' Show ECM
    If ChkShowECM.Value = vbChecked Then WriteIniValue FileName, "hacktv", "showecm", ChkShowECM.Value
    ' Subtitles
    If ChkSubtitles.Value = vbChecked Then WriteIniValue FileName, "hacktv", "subtitles", ChkSubtitles.Value
    If ChkSubtitles.Value = vbChecked Then WriteIniValue FileName, "hacktv", "subtitleindex", TxtSubtitleIndex.Text
    ' VITS
    If ChkVITS.Value = vbChecked Then WriteIniValue FileName, "hacktv", "vits", ChkVITS.Value
    ' Display the opened filename in the title bar
    If TitleBarChanged = False Then TitleBar = Caption
    TitleBarChanged = True
    Caption = TitleBar & " - " & Mid(FileName, InStrRev(FileName, "\") + 1, Len(FileName))
    SaveFile.Caption = "Save"
    ' Update MRU list
    Call UpdateMRUList(FileName)
End Sub

Private Sub OpenFile_Click()
    ' If a file was specified as a command line parameter, set it as the file to open
    If Command = "" Or FileOpened = True Then
        ' Spawn an open file dialog box to browse for a settings file
        If Not (VBGetOpenFileName(ConfigFileName, , True, False, , True, "hacktv configuration file (*.htv)|*.htv|All Files (*.*)|*.*", 1, , "Choose File", , Me.hWnd)) Then Exit Sub
    Else
        ConfigFileName = Command
        ' Convert Unix-style input path to Windows-style so we can open it
        If RunningOnWine = True Then
            If InStr(ConfigFileName, "/") > 0 Then
                ConfigFileName = Replace$(ConfigFileName, "/", "\")
                ConfigFileName = "Z:" & ConfigFileName
            End If
        End If
    End If
    Call CheckSelectedFile(ConfigFileName)
End Sub

Private Sub file1_Click()
    Call CheckSelectedFile(file1.Caption)
End Sub

Private Sub file2_Click()
    Call CheckSelectedFile(file2.Caption)
End Sub

Private Sub file3_Click()
    Call CheckSelectedFile(file3.Caption)
End Sub

Private Sub file4_Click()
    Call CheckSelectedFile(file4.Caption)
End Sub

Private Sub CheckSelectedFile(ConfigFileName As String)
    Dim fData As String
    iFileNo = FreeFile
    ' If the filename contains quotation marks, remove them as the runtime will crash otherwise
        If InStr(ConfigFileName, Chr(34)) > 0 Then ConfigFileName = Replace$(ConfigFileName, Chr(34), "")
            ' If the file doesn't exist, check the current working directory for it
            If DoesFileExist(ConfigFileName) = False Then
                If DoesFileExist(CurDir & Chr(92) & ConfigFileName) = False Then
                    ' If still not found then generate an error and stop
                    MsgBox "Unable to find the file specified.", vbCritical, App.Title
                    Call NewFile_Click
                    FileOpened = True
                    Exit Sub
                Else
                    ' Change the ConfigFileName variable to include the working directory
                    ConfigFileName = CurDir & Chr(92) & ConfigFileName
                End If
            End If
    ' If the specified file name does not have a .htv extension, assume that the file is a source file
    If Not Command = "" And FileOpened = False Then
        If Not Right(LCase(ConfigFileName), 4) = ".htv" Then
            If DoesFileExist(ConfigFileName) = True Then
                If Not Mid$(ConfigFileName, 2, 1) = ":" Then
                    input_source.Text = CurDir & Chr(92) & ConfigFileName
                Else
                    input_source.Text = ConfigFileName
                End If
                FileOpened = True
                If Right(LCase(ConfigFileName), 4) = ".m3u" Then Call M3UHandler(ConfigFileName)
                Exit Sub
            End If
        End If
    End If
    ' Open the file to check for the [hacktv] section. This ensures that it's a valid file.
    ' We limit it to 50 lines to avoid performance or buffer overrun issues
    Dim linecounter As Integer
    linecounter = 0
    On Error GoTo FileAccessError
    Open ConfigFileName For Input As #iFileNo
    Do While Not EOF(1) And linecounter <= 50
        Line Input #iFileNo, fData
        linecounter = linecounter + 1
        ' Stop processing if or when we find what we need
        If fData = "[" & App.Title & "]" Or fData = "[hacktv]" Then Exit Do
        Loop
    Close #iFileNo
    ' Fix for preference files with Unix-style line breaks (LF only)
    If Mid$(fData, 9, 1) = vbLf Then fData = Left$(fData, 8)
    ' If the correct header is not found, generate an error and stop processing
    If Not fData = "[" & App.Title & "]" And Not fData = "[hacktv]" Then
        MsgBox "Invalid configuration file.", vbCritical, App.Title
        FileOpened = True
        Call NewFile_Click
        Exit Sub
    End If
    On Error Resume Next
    ' If the path is not fully qualified, add the current working directory path to it
    If Not Mid(ConfigFileName, 2, 1) = ":" Then ConfigFileName = CurDir & Chr(92) & ConfigFileName
    ' Check the file format
    If fData = "[hacktv]" Then
        Call OpenConfigFile(ConfigFileName)
    ElseIf fData = "[hacktv-gui]" Then
    ' Inform that this file is in the old format
        MsgBox "This file was created in an older version of the application." & vbCrLf _
               & "The save file format has been changed in version 3.0. Older files will continue to load for the time being." & vbCrLf & vbCrLf _
               & "You can convert the file to the new format by simply saving it again." _
                , vbInformation, App.Title
        Call LegacyOpenConfigFile(ConfigFileName)
    Else
    End If
    If Not ConfigFileName = "" Then
        ' Display the opened filename in the title bar
        ' Back up the original caption once
        If TitleBarChanged = False Then TitleBar = Caption
        TitleBarChanged = True
        ' Set the caption to the backed up name and the filename
        If fData = "[hacktv]" Then
            Caption = TitleBar & " - " & Mid(ConfigFileName, InStrRev(ConfigFileName, "\") + 1, Len(ConfigFileName))
        ' Add "(compatibility mode)" to the title bar if the save file is in the old format
        ElseIf fData = "[hacktv-gui]" Then
            Caption = TitleBar & " - " & Mid(ConfigFileName, InStrRev(ConfigFileName, "\") + 1, Len(ConfigFileName)) & " (compatibility mode)"
        End If
        SaveFile.Caption = "Save"
        ' Set the FileOpened value to True to allow us to open a second file after accepting command line parameters
        FileOpened = True
        ' Update MRU list
        Call UpdateMRUList(ConfigFileName)
    End If
    Exit Sub
FileAccessError:
    MsgBox "Unable to access the requested file. It may be locked by another process or you may not have permission to access it.", vbCritical, App.Title
End Sub

Private Sub OpenConfigFile(ConfigFileName As String)
' Sub to handle the new 3.x file format
    Dim WrongFork As String
    Dim NoFrequencyOrChannel As String
    Dim intItem As Integer
    Dim ImportedOutputDevice As String
    Dim ImportedVideoFormat As String
    Dim ImportedScramblingSystem As String
    Dim ImportedKey As String
    Dim ImportedVC2Key As String
    Dim ImportedM3USource As String
    Dim ImportedChannel As String
    Dim ImportedFrequency As Long
    Dim ImportedLevel As String
    Dim ImportedDeviation As String
    Dim ImportedGamma As String
    Dim ImportedPosition As String
    Dim ImportedLogo As String
    Dim ImportedTeletext As String
    Dim ImportedWSS As Integer
    Dim ImportedAR As Integer
    Dim ImportedEMM As Integer
    Dim ImportedCardNumber As String
    WrongFork = "This file was created with a different fork of hacktv. We will attempt to process the file but some options may not be available."
    NoFrequencyOrChannel = "No frequency or valid channel number was found in the configuration file. Load aborted."
    ' Check the fork value specified in the file. If it doesn't match, give a warning
    If forktype = "" Then
        If Not ReadIniValue(ConfigFileName, "hacktv-gui3", "fork") = "" Then
            MsgBox WrongFork, vbExclamation, App.Title
        End If
    ElseIf forktype = "CJ" Then
        If Not LCase(ReadIniValue(ConfigFileName, "hacktv-gui3", "fork")) = "captainjack" Then
            MsgBox WrongFork, vbExclamation, App.Title
        End If
    End If
    ' Output device. We don't support this as of yet but it is here for future reference
    ImportedOutputDevice = LCase(ReadIniValue(ConfigFileName, "hacktv", "output"))
    If Not ImportedOutputDevice = "" And Not ImportedOutputDevice = "hackrf" Then
        MsgBox "This application only supports output to HackRF devices at present. Support for other SDRs " _
        & "may be added in a future release.", vbExclamation, App.Title
    End If
    ' Input source or test card
    If LCase(ReadIniValue(ConfigFileName, "hacktv", "input")) = "test:colourbars" Then
        TestCard.Value = True
    ' Is the input source an M3U file?
    ElseIf LCase$(Right$(ReadIniValue(ConfigFileName, "hacktv-gui3", "m3usource"), 4)) = ".m3u" Then
        ImportedM3USource = ReadIniValue(ConfigFileName, "hacktv-gui3", "m3usource")
        If M3UHandler(ImportedM3USource) = True Then
            M3USourceFile = True
            M3USource.ListIndex = ReadIniValue(ConfigFileName, "hacktv-gui3", "m3uindex")
        End If
    Else
        M3USource.Visible = False
        input_source.Visible = True
        M3USourceFile = False
        LocalSource.Value = True
        input_source.Text = ReadIniValue(ConfigFileName, "hacktv", "input")
    End If
    ' Video format
    ImportedVideoFormat = LCase(ReadIniValue(ConfigFileName, "hacktv", "mode"))
    If ImportedVideoFormat = "i" Then
        PAL.Value = True
        VideoFormat.ListIndex = 0
    ElseIf ImportedVideoFormat = "b" Or ImportedVideoFormat = "g" Then
        PAL.Value = True
        VideoFormat.ListIndex = 1
    ElseIf ImportedVideoFormat = "pal-fm" Then
        PAL.Value = True
        VideoFormat.ListIndex = 2
    ElseIf ImportedVideoFormat = "pal-m" Then
        PAL.Value = True
        VideoFormat.ListIndex = 3
    ElseIf ImportedVideoFormat = "m" Then
        NTSC.Value = True
        VideoFormat.ListIndex = 0
     ElseIf ImportedVideoFormat = "ntsc-fm" Then
        NTSC.Value = True
        VideoFormat.ListIndex = 1
    ElseIf ImportedVideoFormat = "ntsc-bs" Then
        NTSC.Value = True
        VideoFormat.ListIndex = 2
    ElseIf ImportedVideoFormat = "apollo-fsc-fm" Then
        NTSC.Value = True
        VideoFormat.ListIndex = 3
    ElseIf ImportedVideoFormat = "m-cbs405" Then
        NTSC.Value = True
        VideoFormat.ListIndex = 4
    ElseIf ImportedVideoFormat = "l" Then
        SECAM.Value = True
        VideoFormat.ListIndex = 0
    ElseIf ImportedVideoFormat = "d" Or ImportedVideoFormat = "k" Then
        SECAM.Value = True
        VideoFormat.ListIndex = 1
    ElseIf ImportedVideoFormat = "secam-fm" Then
        SECAM.Value = True
        VideoFormat.ListIndex = 2
    ElseIf ImportedVideoFormat = "a" Then
        BW.Value = True
        VideoFormat.ListIndex = 0
    ElseIf ImportedVideoFormat = "e" Then
        BW.Value = True
        VideoFormat.ListIndex = 1
    ElseIf ImportedVideoFormat = "240-am" Then
        BW.Value = True
        VideoFormat.ListIndex = 2
    ElseIf ImportedVideoFormat = "30-am" Then
        BW.Value = True
        VideoFormat.ListIndex = 3
    ElseIf ImportedVideoFormat = "apollo-fm" Then
        BW.Value = True
        VideoFormat.ListIndex = 4
    ElseIf ImportedVideoFormat = "d2mac-am" Then
        MAC.Value = True
        VideoFormat.ListIndex = 0
    ElseIf ImportedVideoFormat = "d2mac-fm" Then
        MAC.Value = True
        VideoFormat.ListIndex = 1
    ElseIf ImportedVideoFormat = "dmac-am" Then
        MAC.Value = True
        VideoFormat.ListIndex = 2
    ElseIf ImportedVideoFormat = "dmac-fm" Then
        MAC.Value = True
        VideoFormat.ListIndex = 3
    Else
        Call InvalidConfigFileValue("video format", ImportedVideoFormat, vbCritical)
        Call NewFile_Click
    End If
    ' Sample rate (default to 16 MHz if not specified)
    SampleRate.Text = ReadIniValue(ConfigFileName, "hacktv", "samplerate", 16000000) / 1000000
    ' Frequency
        ' Read values from config file to variables
        ' As we're dealing with a long integer for the frequency value, return a value of -250 if the value is blank
        ' Allowing it to return blank will cause a crash
    ImportedChannel = ReadIniValue(ConfigFileName, "hacktv-gui3", "channel")
    ImportedFrequency = ReadIniValue(ConfigFileName, "hacktv", "frequency", -250)
        ' Check if we're dealing with a channel number or custom frequency. If neither, then stop processing
    If ImportedChannel = "" And ImportedFrequency = "-250" Then
        MsgBox NoFrequencyOrChannel, vbCritical, App.Title
        Call NewFile_Click
        Exit Sub
    ElseIf ImportedChannel = "" Then
        CustomFreq.Value = True
        frequency_mhz.Text = ImportedFrequency / 1000000
    Else
        ' Try to find the channel name by trying UHF first
        UHF.Value = True
        For intItem = 0 To frequency_ch.ListCount - 1
            If frequency_ch.List(intItem) = ImportedChannel Then
                frequency_ch.Text = ImportedChannel
            End If
        Next
        If Not UCase(frequency_ch.Text) = UCase(ImportedChannel) Then
        ' If not found, try VHF
            VHF.Value = True
            For intItem = 0 To frequency_ch.ListCount - 1
                If frequency_ch.List(intItem) = ImportedChannel Then
                    frequency_ch.Text = ImportedChannel
                End If
            Next
        ' If still not found, generate an error and use the frequency instead of the channel
            If Not UCase(frequency_ch.Text) = UCase(ImportedChannel) Then
                If Not ImportedFrequency = "-250" Then
                    CustomFreq.Value = True
                    frequency_mhz.Text = ImportedFrequency / 1000000
                    Call InvalidConfigFileValue("channel", ImportedChannel)
                Else
        ' If not found, and the frequency is also blank, abort
                    MsgBox NoFrequencyOrChannel, vbCritical, App.Title
                    Call NewFile_Click
                    Exit Sub
                End If
            End If
        End If
    End If
    ' Gain
    txgain.Text = ReadIniValue(ConfigFileName, "hacktv", "gain", 0)
    ' Amp
    ChkRFAmp.Value = ReadIniValue(ConfigFileName, "hacktv", "amp", 0)
    ' Output level
    ImportedLevel = ReadIniValue(ConfigFileName, "hacktv", "level")
    If Not ImportedLevel = "" Then
        ChkOutputLevel.Value = vbChecked
        outputlevelvalue.Text = ImportedLevel
    End If
    ' FM deviation
    If ChkFMDev.Enabled = True Then
        ImportedDeviation = ReadIniValue(ConfigFileName, "hacktv", "deviation")
        If Not ImportedDeviation = "" Then
            ChkFMDev.Value = vbChecked
            fm_deviation.Text = ReadIniValue(ConfigFileName, "hacktv", "deviation") / 1000000
        End If
    End If
    ' Gamma
    ImportedGamma = ReadIniValue(ConfigFileName, "hacktv", "gamma")
    If Not ImportedGamma = "" Then
        ChkGamma.Value = vbChecked
        gammavalue.Text = ReadIniValue(ConfigFileName, "hacktv", "gamma")
    End If
    ' Repeat
    If ChkRepeat.Enabled = True Then ChkRepeat.Value = ReadIniValue(ConfigFileName, "hacktv", "repeat", 0)
    ' Position
    If ChkPosition.Enabled = True Then
        ImportedPosition = ReadIniValue(ConfigFileName, "hacktv", "position")
        If Not ImportedPosition = "" Then
            ChkPosition.Value = vbChecked
            positionValue.Text = ReadIniValue(ConfigFileName, "hacktv", "position")
        End If
    End If
    ' Verbose mode
    ChkVerbose.Value = ReadIniValue(ConfigFileName, "hacktv", "verbose", 0)
    ' Logo
    If ChkLogo.Enabled = True Then ImportedLogo = ReadIniValue(ConfigFileName, "hacktv", "logo")
    ' If a logo is specified, check if it exists first
    If Not ImportedLogo = "" Then
        If DoesFileExist(HackTVPath & "\resources\logos\" & ImportedLogo) Then
            ChkLogo.Value = vbChecked
            LogoFolder.Text = ImportedLogo
        Else
            MsgBox "The file " & ImportedLogo & " could not be found. Logo option disabled.", vbExclamation, App.Title
        End If
    End If
    ' Timestamp
    If ChkTimestamp.Enabled = True Then ChkTimestamp.Value = ReadIniValue(ConfigFileName, "hacktv", "timestamp", 0)
    ' Interlace
    If ChkInterlace.Enabled = True Then ChkInterlace.Value = ReadIniValue(ConfigFileName, "hacktv", "interlace", 0)
    ' Teletext
    ImportedTeletext = ReadIniValue(ConfigFileName, "hacktv", "teletext")
    If Not ImportedTeletext = "" Then
        ChkTeletext.Value = vbChecked
        If LCase$(Left$(ImportedTeletext, 4)) = "raw:" Then
            teletext_source.Text = Mid$(ImportedTeletext, 5)
        Else
            teletext_source.Text = ImportedTeletext
        End If
    End If
    ' WSS
    ImportedWSS = ReadIniValue(ConfigFileName, "hacktv", "wss", 0)
    If Not ImportedWSS = 0 Then
        ChkWSS.Value = vbChecked
        ' Since we increased the value by one when saving, decrease by one when loading
        wss_mode.ListIndex = ImportedWSS - 1
    End If
    ' Aspect ratio correction for 16:9 content on 4:3 display
    ' If the arcorrection value is not defined, return -1 and leave the option unchecked
    ' Otherwise, check the option and process it as normal
    If ChkARCorrection.Enabled = True Then
        ImportedAR = ReadIniValue(ConfigFileName, "hacktv", "arcorrection", -1)
        If Not ImportedAR = -1 Then
            ChkARCorrection.Value = vbChecked
            ARCorrectionMode.ListIndex = ReadIniValue(ConfigFileName, "hacktv", "arcorrection")
        End If
    End If
    ' Encryption system
    ImportedScramblingSystem = LCase$(ReadIniValue(ConfigFileName, "hacktv", "scramblingtype"))
    If Not ImportedScramblingSystem = "" Then ImportedKey = LCase$(ReadIniValue(ConfigFileName, "hacktv", "scramblingkey"))
    If ImportedScramblingSystem = "" Then
        encryption_type.ListIndex = 0
    ElseIf ImportedScramblingSystem = "videocrypt" Then
        encryption_type.ListIndex = 1
    ElseIf ImportedScramblingSystem = "videocrypt2" Then
        encryption_type.ListIndex = 2
    ElseIf ImportedScramblingSystem = "videocrypt1+2" Then
        encryption_type.ListIndex = 3
    ElseIf ImportedScramblingSystem = "videocrypts" Then
        encryption_type.ListIndex = 4
    ElseIf ImportedScramblingSystem = "syster" Then
        encryption_type.ListIndex = 5
    ElseIf ImportedScramblingSystem = "d11" And forktype = "CJ" Then
        encryption_type.ListIndex = 6
    ElseIf ImportedScramblingSystem = "smartcrypt" And forktype = "CJ" Then
        encryption_type.ListIndex = 7
    ElseIf ImportedScramblingSystem = "single-cut" Then
        encryption_type.ListIndex = 1
    ElseIf ImportedScramblingSystem = "double-cut" Then
        encryption_type.ListIndex = 2
    Else
        Call InvalidConfigFileValue("scrambling system", ImportedScramblingSystem)
    End If
    ' Encryption key/viewing card type
    ' Nothing
    If ImportedKey = "" And ImportedScramblingSystem = "" Then
        ' Do nothing
    ' VideoCrypt 1/2/S (including VC1 side of dual VC1/2 mode)
    ElseIf ImportedKey = "free" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 0
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 0
        ' VC2 conditional mode only supported on Captain Jack fork
    ElseIf ImportedScramblingSystem = "videocrypt2" And ImportedKey = "conditional" And forktype = "CJ" Then
        encryption_key.ListIndex = 1
    ElseIf ImportedScramblingSystem = "videocrypt" And ImportedKey = "conditional" _
    Or ImportedScramblingSystem = "videocrypts" Then
        encryption_key.ListIndex = 1
    ElseIf ImportedKey = "sky12" And forktype = "CJ" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 1
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 1
    ElseIf ImportedKey = "sky11" And forktype = "CJ" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 2
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 2
    ElseIf ImportedKey = "sky10" And forktype = "CJ" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 3
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 3
    ElseIf ImportedKey = "sky10ppv" And forktype = "CJ" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 4
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 4
    ElseIf ImportedKey = "sky09" And forktype = "CJ" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 5
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 5
    ElseIf ImportedKey = "sky07" And forktype = "CJ" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 6
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 6
    ElseIf ImportedKey = "sky03" And forktype = "CJ" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 7
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 7
    ElseIf ImportedKey = "tac" And forktype = "CJ" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 8
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 8
    ElseIf ImportedKey = "tac2" And forktype = "CJ" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 9
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 9
    ElseIf ImportedKey = "xtea" And forktype = "CJ" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 10
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 10
    ElseIf ImportedKey = "ppv" And forktype = "CJ" Then
        If Not ImportedScramblingSystem = "videocrypt1+2" Then encryption_key.ListIndex = 11
        If ImportedScramblingSystem = "videocrypt1+2" Then vc1key.ListIndex = 11
    ' Syster/D11/Smartcrypt
    ElseIf ImportedKey = "" And ImportedScramblingSystem = "syster" Then
        encryption_key.ListIndex = 0
    ElseIf ImportedKey = "" And ImportedScramblingSystem = "d11" And forktype = "CJ" Then
        encryption_key.ListIndex = 0
    ElseIf ImportedKey = "" And ImportedScramblingSystem = "smartcrypt" And forktype = "CJ" Then
        encryption_key.ListIndex = 0
    ElseIf ImportedKey = "premiere-fa" And forktype = "CJ" Then
        encryption_key.ListIndex = 0
    ElseIf ImportedKey = "premiere-ca" And forktype = "CJ" Then
        encryption_key.ListIndex = 1
    ElseIf ImportedKey = "cfrfa" And forktype = "CJ" Then
        encryption_key.ListIndex = 2
    ElseIf ImportedKey = "cfrca" And forktype = "CJ" Then
        encryption_key.ListIndex = 3
    ElseIf ImportedKey = "cplfa" And forktype = "CJ" Then
        encryption_key.ListIndex = 4
    ElseIf ImportedKey = "cesfa" And forktype = "CJ" Then
        encryption_key.ListIndex = 5
    ElseIf ImportedKey = "ntvfa" And forktype = "CJ" Then
        encryption_key.ListIndex = 6
    ' Eurocrypt
    ElseIf ImportedKey = "" And ImportedScramblingSystem = "single-cut" Then
        encryption_key.ListIndex = 0
    ElseIf ImportedKey = "" And ImportedScramblingSystem = "double-cut" Then
        encryption_key.ListIndex = 0
    ElseIf ImportedKey = "eurocrypt filmnet" Then
        encryption_key.ListIndex = 1
    ElseIf ImportedKey = "eurocrypt tv1000" Then
        encryption_key.ListIndex = 2
    ElseIf ImportedKey = "eurocrypt ctv" Then
        encryption_key.ListIndex = 3
    ElseIf ImportedKey = "eurocrypt ctvs" Then
        encryption_key.ListIndex = 4
    ElseIf ImportedKey = "eurocrypt tvplus" Then
        encryption_key.ListIndex = 5
    ElseIf ImportedKey = "eurocrypt tvs" Then
        encryption_key.ListIndex = 6
    ElseIf ImportedKey = "eurocrypt rdv" Then
        encryption_key.ListIndex = 7
    ElseIf ImportedKey = "eurocrypt nrk" Then
        encryption_key.ListIndex = 8
    Else
        If Not ImportedScramblingSystem = "videocrypt1+2" Then Call InvalidConfigFileValue("scrambling key", ImportedKey)
        If ImportedScramblingSystem = "videocrypt1+2" Then Call InvalidConfigFileValue("VideoCrypt I scrambling key", ImportedKey)
    End If
        ' VC2 side of dual VC1/2 mode
    If ImportedScramblingSystem = "videocrypt1+2" Then
        ImportedVC2Key = ReadIniValue(ConfigFileName, "hacktv", "scramblingkey2")
        If ImportedVC2Key = "free" Then
            vc2key.ListIndex = 0
        ElseIf ImportedVC2Key = "conditional" And forktype = "CJ" Then
            vc2key.ListIndex = 1
        Else
            Call InvalidConfigFileValue("VideoCrypt II scrambling key", ImportedVC2Key)
        End If
    End If
    ' EMM
    If ChkEnableEMM.Enabled = True And ChkDisableEMM.Enabled = True Then
        ImportedEMM = ReadIniValue(ConfigFileName, "hacktv", "emm", 0)
        If ImportedEMM = 0 Then
            ChkEnableEMM.Value = vbUnchecked
            ChkDisableEMM.Value = vbUnchecked
        ElseIf ImportedEMM = 1 Then
            ChkEnableEMM.Value = vbChecked
            ChkDisableEMM.Value = vbUnchecked
            ImportedCardNumber = ReadIniValue(ConfigFileName, "hacktv", "cardnumber")
            ImportedCardNumber = ImportedCardNumber & LuhnCheck(ImportedCardNumber)
            CardNumber.Text = ReadIniValue(ConfigFileName, "hacktv-gui3", "13digitprefix") & ImportedCardNumber
        ElseIf ImportedEMM = 2 Then
            ChkEnableEMM.Value = vbUnchecked
            ChkDisableEMM.Value = vbChecked
            ImportedCardNumber = ReadIniValue(ConfigFileName, "hacktv", "cardnumber")
            ImportedCardNumber = ImportedCardNumber & LuhnCheck(ImportedCardNumber)
            CardNumber.Text = ReadIniValue(ConfigFileName, "hacktv-gui3", "13digitprefix") & ImportedCardNumber
        End If
    End If
    ' Show card serial
    If ChkShowSerial.Enabled = True Then ChkShowSerial.Value = ReadIniValue(ConfigFileName, "hacktv", "showserial", 0)
    ' Brute force PPV key
    If ChkFindKey.Enabled = True Then ChkFindKey.Value = ReadIniValue(ConfigFileName, "hacktv", "findkey", 0)
    ' Encrypt audio
    If ChkEncryptAudio.Enabled = True Then ChkEncryptAudio.Value = ReadIniValue(ConfigFileName, "hacktv", "scramble-audio", 0)
    ' ACP
    If ChkACP.Enabled = True Then ChkACP.Value = ReadIniValue(ConfigFileName, "hacktv", "acp", 0)
    ' Filter
    ChkVideoFilter.Value = ReadIniValue(ConfigFileName, "hacktv", "filter", 0)
    ' Audio
    If ChkAudio.Enabled = True Then ChkAudio.Value = ReadIniValue(ConfigFileName, "hacktv", "audio", 0)
    ' NICAM
    If ChkNICAM.Enabled = True Then ChkNICAM.Value = ReadIniValue(ConfigFileName, "hacktv", "nicam", 0)
    ' ECM
    If ChkShowECM.Enabled = True Then ChkShowECM.Value = ReadIniValue(ConfigFileName, "hacktv", "showecm", 0)
    ' Subtitles
    ChkSubtitles.Value = ReadIniValue(ConfigFileName, "hacktv", "subtitles", 0)
    If ChkSubtitles.Value = vbChecked Then TxtSubtitleIndex.Text = ReadIniValue(ConfigFileName, "hacktv", "subtitleindex")
    ' VITS
    If ChkVITS.Enabled = True Then ChkVITS.Value = ReadIniValue(ConfigFileName, "hacktv", "vits", 0)
End Sub

Private Sub InvalidConfigFileValue(SettingName As String, Value As String, Optional ErrorType As VbMsgBoxStyle)
' This sub is used to generate an error when an invalid value is found in a config file
' Saves us writing out the same error message multiple times
' To use it, just feed two values or variables into it and they will be added to the message below
    If ErrorType = 0 Then ErrorType = vbExclamation
    ' If an incorrect encryption system/key was specified, disable encryption
    If SettingName = "scrambling system" Or SettingName = "scrambling key" Or _
    SettingName = "VideoCrypt I scrambling key" Or SettingName = "VideoCrypt II scrambling key" _
    Then encryption_type.ListIndex = 0
    MsgBox "The " & SettingName & Chr(32) & Chr(34) & Value & Chr(34) & " specified in the configuration file could not be found." & vbCrLf _
    & "The file may have been created in a newer version or the value is invalid.", ErrorType, App.Title
End Sub

Private Sub LegacyOpenConfigFile(ConfigFileName As String)
' Pre-version 3.x file open code, may be removed in a future release
    Dim WrongFork As String
    WrongFork = "Unable to load this file because it was created with a different hacktv fork."
    ' Check the fork value specified in the INI file
    ' We don't want to load a file created for a different fork as it won't behave as expected
    If forktype = "" Then
        If Not ReadIniValue(ConfigFileName, App.Title, "fork") = "fsphil" Then
        MsgBox WrongFork, vbCritical, App.Title
        ' Call the new file sub so we can load default values
        Call NewFile_Click
        Exit Sub
        End If
    End If
    If forktype = "CJ" Then
        If Not ReadIniValue(ConfigFileName, App.Title, "fork") = "CaptainJack" Then
        MsgBox WrongFork, vbCritical, App.Title
        ' Call the new file sub so we can load default values
        Call NewFile_Click
        Exit Sub
        End If
    End If
    ' Input source or test card
    If ReadIniValue(ConfigFileName, "Settings", "source") = "test" Then
        TestCard.Value = True
    Else
        LocalSource.Value = True
        input_source.Text = ReadIniValue(ConfigFileName, "Settings", "source")
    End If
    ' Is the input source an M3U file?
    If LCase(Right$(input_source.Text, 4)) = ".m3u" Then
        If M3UHandler(input_source.Text) = True Then
            M3USourceFile = True
            M3USource.ListIndex = ReadIniValue(ConfigFileName, "Settings", "M3Uindex")
        End If
    Else
        M3USource.Visible = False
        input_source.Visible = True
        M3USourceFile = False
    End If
    ' Video format
    If ReadIniValue(ConfigFileName, "Settings", "videoformat") = "0" Then PAL.Value = True
    If ReadIniValue(ConfigFileName, "Settings", "videoformat") = "1" Then NTSC.Value = True
    If ReadIniValue(ConfigFileName, "Settings", "videoformat") = "2" Then SECAM.Value = True
    If ReadIniValue(ConfigFileName, "Settings", "videoformat") = "3" Then BW.Value = True
    If ReadIniValue(ConfigFileName, "Settings", "videoformat") = "4" Then MAC.Value = True
    ' Video mode
    VideoFormat.ListIndex = ReadIniValue(ConfigFileName, "Settings", "mode")
    ' Sample rate
    SampleRate.Text = ReadIniValue(ConfigFileName, "Settings", "samplerate")
    ' Frequency
    If ReadIniValue(ConfigFileName, "Settings", "band") = "0" Then
        UHF.Value = True
        frequency_ch.ListIndex = ReadIniValue(ConfigFileName, "Settings", "channel")
    End If
    If ReadIniValue(ConfigFileName, "Settings", "band") = "1" Then
        VHF.Value = True
        frequency_ch.ListIndex = ReadIniValue(ConfigFileName, "Settings", "channel")
    End If
    If ReadIniValue(ConfigFileName, "Settings", "band") = "2" Then
        CustomFreq.Value = True
        frequency_mhz.Text = ReadIniValue(ConfigFileName, "Settings", "frequency")
    End If
    ' Gain
    txgain.Text = ReadIniValue(ConfigFileName, "Settings", "gain")
    ' Amp
    ChkRFAmp.Value = ReadIniValue(ConfigFileName, "Settings", "amp")
    ' Output level
    ChkOutputLevel.Value = ReadIniValue(ConfigFileName, "Settings", "level")
    If ChkOutputLevel.Value = vbChecked Then outputlevelvalue.Text = ReadIniValue(ConfigFileName, "Settings", "levelValue")
    ' FM deviation
    ChkFMDev.Value = ReadIniValue(ConfigFileName, "Settings", "deviation")
    If ChkFMDev.Value = vbChecked Then fm_deviation.Text = ReadIniValue(ConfigFileName, "Settings", "deviationValue")
    ' Gamma
    ChkGamma.Value = ReadIniValue(ConfigFileName, "Settings", "gamma")
    If ChkGamma.Value = vbChecked Then gammavalue.Text = ReadIniValue(ConfigFileName, "Settings", "gammaValue")
    ' Repeat
    ChkRepeat.Value = ReadIniValue(ConfigFileName, "Settings", "repeat")
    ' Position
    ChkPosition.Value = ReadIniValue(ConfigFileName, "Settings", "position")
    If ChkPosition.Value = vbChecked Then positionValue.Text = ReadIniValue(ConfigFileName, "Settings", "positionValue")
    ' Verbose mode
    ChkVerbose.Value = ReadIniValue(ConfigFileName, "Settings", "verbose")
    ' Logo
    ChkLogo.Value = ReadIniValue(ConfigFileName, "Settings", "logo")
    If ChkLogo.Value = vbChecked Then LogoFolder.ListIndex = ReadIniValue(ConfigFileName, "Settings", "logoValue")
    ' Timestamp
    ChkTimestamp.Value = ReadIniValue(ConfigFileName, "Settings", "timestamp")
    ' Teletext
    ChkTeletext.Value = ReadIniValue(ConfigFileName, "Settings", "teletext")
    If ChkTeletext.Value = vbChecked Then teletext_source.Text = ReadIniValue(ConfigFileName, "Settings", "teletextpath")
    ' WSS
    ChkWSS.Value = ReadIniValue(ConfigFileName, "Settings", "wss")
    If ChkWSS.Value = vbChecked Then wss_mode.ListIndex = ReadIniValue(ConfigFileName, "Settings", "wssaspect")
    ' Encryption
    encryption_type.ListIndex = ReadIniValue(ConfigFileName, "Settings", "encryptiontype")
    If Not encryption_type.ListIndex = 0 Then encryption_key.ListIndex = ReadIniValue(ConfigFileName, "Settings", "encryptionkey")
    If DualVCMode = True Then
        vc1key.ListIndex = ReadIniValue(ConfigFileName, "Settings", "vc1key")
        vc2key.ListIndex = ReadIniValue(ConfigFileName, "Settings", "vc2key")
    End If
    ' EMM
    If ReadIniValue(ConfigFileName, "Settings", "emm") = "0" Then
        ChkEnableEMM.Value = vbUnchecked
        ChkDisableEMM.Value = vbUnchecked
    ElseIf ReadIniValue(ConfigFileName, "Settings", "emm") = "1" Then
        ChkEnableEMM.Value = vbChecked
        ChkDisableEMM.Value = vbUnchecked
        CardNumber.Text = ReadIniValue(ConfigFileName, "Settings", "cardnumber")
    ElseIf ReadIniValue(ConfigFileName, "Settings", "emm") = "2" Then
        ChkEnableEMM.Value = vbUnchecked
        ChkDisableEMM.Value = vbChecked
        CardNumber.Text = ReadIniValue(ConfigFileName, "Settings", "cardnumber")
    End If
    ' Encrypt audio
    ChkEncryptAudio.Value = ReadIniValue(ConfigFileName, "Settings", "systeraudio")
    ' ACP
    ChkACP.Value = ReadIniValue(ConfigFileName, "Settings", "acp")
    ' Filter
    ChkVideoFilter.Value = ReadIniValue(ConfigFileName, "Settings", "filter")
    ' Audio
    ChkAudio.Value = ReadIniValue(ConfigFileName, "Settings", "audio")
    ' NICAM
    ChkNICAM.Value = ReadIniValue(ConfigFileName, "Settings", "nicam")
    ' ECM
    ChkShowECM.Value = ReadIniValue(ConfigFileName, "Settings", "showecm")
    ' Subtitles
    ChkSubtitles.Value = ReadIniValue(ConfigFileName, "Settings", "subtitles")
    If ChkSubtitles.Value = vbChecked Then TxtSubtitleIndex.Text = ReadIniValue(ConfigFileName, "Settings", "subtitleindex")
End Sub

Private Sub BtnSourceBrowse_Click()
    Dim sFile As String
' Spawn an open file dialog box to browse for a source video file
    If (VBGetOpenFileName(sFile, , True, False, , True, "All Files (*.*)|*.*", 1, , "Choose File", , Me.hWnd)) Then
        input_source.Text = sFile
        ' Check if the selected file has a .M3U extension (we handle these in a different way)
        If LCase(Right$(sFile, 4)) = ".m3u" Then
            Call M3UHandler(sFile)
            M3USourceFile = True
        ' Check if the selected file has a .HTV extension (these should be opened from the File menu)
        ElseIf LCase(Right$(sFile, 4)) = ".htv" Then
            MsgBox "Configuration files should be opened from the File menu.", vbExclamation, App.Title
            input_source.Text = ""
            M3USource.Visible = False
            input_source.Visible = True
            M3USourceFile = False
        Else
            M3USource.Visible = False
            input_source.Visible = True
            M3USourceFile = False
        End If
    End If
End Sub

Private Function M3UHandler(FileName As String) As Boolean
    If StopProcessing = True Then StopProcessing = False
    Dim TitleBar As String
    TitleBar = Caption
    AppBusy = True
    MousePointer = 13
    FrmSource.Enabled = False
    BtnRun.Enabled = False
    TitleBar = Caption
    Caption = TitleBar & " - Loading playlist file, please wait..."
    ' Try to parse the M3U file in Extended M3U format.
    If LoadM3U(FileName) = False Then
        ' If this fails then do no special handling.
        input_source.Visible = True
        M3USource.Visible = False
        MousePointer = 0
        FrmSource.Enabled = True
        BtnRun.Enabled = True
        Caption = TitleBar
        AppBusy = False
        M3UHandler = False
    Else
        ' Hide the input_source textbox and replace it with the M3USource combobox.
        input_source.Visible = False
        M3USource.Visible = True
        MousePointer = 0
        FrmSource.Enabled = True
        BtnRun.Enabled = True
        Caption = TitleBar
        AppBusy = False
        M3UHandler = True
    End If
End Function

' M3U file parser
' Based on https://www.developerfusion.com/code/3843/load-m3u-playlist/
' We can't get this working in a module due to VB6 disallowing public arrays

Public Function LoadM3U(ByVal strFileName As String) As Boolean
   'Error handler
   On Error GoTo ErrHap
   
       'Declare variables
       Dim lngFileNo As Long
       Dim strTemp As String
       Dim i As Long
       Dim strLines() As String
       Dim lngLines As Long
       Dim strM3ULoc As String
      
       'Check if file exists
       If Dir(strFileName) <> "" Then
           
           'Get M3U location
           strM3ULoc = Left$(strFileName, InStrRev(strFileName, "\"))
           
           'Get new file number
           lngFileNo = FreeFile
           
           'Open the file
           Open strFileName For Input As lngFileNo
               
               'Get the file
               strTemp = Input(LOF(lngFileNo), #lngFileNo)
               
           Close lngFileNo
           
               ' VB6 doesn't support UTF-8 files so strip out the Unicode BOM and treat the file like ASCII
               ' Double-byte (UTF-16 or UCS-2) files aren't supported at all; they just fail
               If Left$(strTemp, 3) = Chr(239) & Chr(187) & Chr(191) Then
                    strTemp = Mid(strTemp, 4, Len(strTemp))
               End If
               
               ' If the file is in Unix format (LF only), convert to Windows format (CR + LF) so we can parse it
               If InStr(strTemp, vbCr) = 0 Then
                strTemp = Replace$(strTemp, vbLf, vbCrLf)
               Else
               ' I've encountered some M3U files that have a LF on line 1 only but are CR + LF elsewhere
               ' This will throw us off so let's change the first line
                If Not InStr(Mid$(strTemp, 8, 1), vbCr) > 0 Then
                 strTemp = Replace$(strTemp, vbLf, vbCrLf, , 1)
                End If
               End If
           
           'Split the file into its lines
           strLines = Split(strTemp, vbCrLf)
           
           'Check that this file has enough lines
           If UBound(strLines) > 2 Then
           
               'Check that it's an M3U file
               If InStr(strLines(0), "#EXTM3U") > 0 Then
                
                   'Get number of lines
                   lngLines = UBound(strLines)
                   
                   'Attention! If you have any errors over the next 3 lines then you need to make sure
                   'that you have declared the array variables without specifying their size,
                   'because here we're changing their sizes to match.  - Thanks
                   ReDim strFilePaths(0 To (lngLines / 2)) As String
                   ReDim strNames(0 To (lngLines / 2)) As String
                   ReDim URL(0 To lngLines / 2) As String
                    
                    Dim filecounter As Long
                    filecounter = 0
                    M3USource.Clear
                   'Loop through each line
                   For i = 1 To lngLines
                   
                       'Check what kind of data we've got
                       If Left$(strLines(i), 7) = "#EXTINF" Then
                           If StopProcessing = True Then Exit For
                           DoEvents
                           'Get channel name
                           strNames((i - 1) / 2) = Right$(strLines(i), Len(strLines(i)) - InStr(1, strLines(i), ","))
                           With M3USource
                           .AddItem strNames((i - 1) / 2)
                           .ItemData(.NewIndex) = filecounter
                           .ListIndex = 0
                           End With
                       Else
                            'Populate URL array with the URL of the stream
                            URL(filecounter) = strLines(i)
                            filecounter = filecounter + 1
                       End If
                       
                   Next i
                   
                   'Set return value to true
                   LoadM3U = True
               
               End If
               
           Else
               'Return error
               LoadM3U = False
           End If
           
       Else
           'Return error
           LoadM3U = False
       End If
       
Exit Function
ErrHap:
   'Display Error
   Call MsgBox(Err.Description & " Number: " & Err.Number, vbExclamation, App.Title)
   
   'Set return value
   LoadM3U = False
   
End Function

Private Sub ChkRepeat_Click()
    If ChkRepeat.Value = vbChecked Then
        repeatstatus = "--repeat"
    Else
        repeatstatus = ""
    End If
End Sub

Private Sub ChkNICAM_Click()
' Passes an option to disable NICAM digital stereo, but only if the video format supports it
' We don't need to pass it if NICAM isn't supported anyway, so we check if the checkbox is greyed out/disabled
    If ChkNICAM.Value = vbUnchecked Then
        If ChkNICAM.Enabled = True Then
            nicamstatus = "--nonicam"
        End If
    Else
        nicamstatus = ""
    End If
End Sub

Private Sub VHF_Click()
' Populates the frequency_ch combobox with VHF channel frequencies for the selected video format
' Also disables the frequency_mhz textbox
    frequency_mhz.Enabled = False
    frequency_ch.Enabled = True
    If sys = "i" Then Call AddSysIVHFChannels
    If sys = "g" Then Call AddSystemBChannels
    If sys = "m" Then Call AddNTSCVHFChannels
    If sys = "l" Then Call AddFrenchVHFChannels
    If sys = "d" Then Call AddSystemDRussiaChannels
    If sys = "a" Then Call AddSystemAChannels
    If sys = "e" Then Call AddSystemEChannels
    If sys = "pal-m" Then Call AddNTSCVHFChannels
End Sub

Private Sub UHF_Click()
' Populates the frequency_ch combobox with UHF channel frequencies for the selected video format
' Also disables the frequency_mhz textbox
    frequency_mhz.Enabled = False
    frequency_ch.Enabled = True
    If sys = "i" Then Call AddEuropeUHFChannels
    If sys = "g" Then Call AddEuropeUHFChannels
    If sys = "m" Then Call AddNTSCUHFChannels
    If sys = "l" Then Call AddEuropeUHFChannels
    If sys = "d" Then Call AddEuropeUHFChannels
    If sys = "pal-m" Then Call AddNTSCUHFChannels
    If sys = "dmac-fm" Then Call AddBSBChannels
End Sub

Private Sub PAL_Click()
' Configures features supported (or not) by PAL video formats and populates the VideoFormat combobox
    Call AddPALEncryptionTypes
    If ChkAudio.Enabled = False Then Call EnableAudioOption
    With VideoFormat
        .Clear
        .AddItem "PAL-I (625 lines, 25 fps, 6.0 MHz FM audio)"
        .ItemData(.NewIndex) = "101"
        .AddItem "PAL-B/G (625 lines, 25 fps, 5.5 MHz FM audio)"
        .ItemData(.NewIndex) = "102"
        .AddItem "PAL-FM (625 lines, 25 fps, 6.5 MHz FM audio)"
        .ItemData(.NewIndex) = "103"
        .AddItem "PAL-M (525 lines, 30 fps, 4.5 MHz FM audio)"
        .ItemData(.NewIndex) = "104"
        .ListIndex = 0
    End With
End Sub

Private Sub NTSC_Click()
' Configures features supported (or not) by NTSC video formats and populates the VideoFormat combobox
    If ChkAudio.Enabled = False Then Call EnableAudioOption
    With VideoFormat
        .Clear
        .AddItem "NTSC-M (525 lines, 29.97 fps, 4.5 MHz FM audio)"
        .ItemData(.NewIndex) = "201"
        .AddItem "NTSC-FM (525 lines, 29.97 fps, 6.5 MHz FM audio)"
        .ItemData(.NewIndex) = "203"
        .AddItem "NTSC-FM BS (525 lines, 29.97 fps, BS digital audio)"
        .ItemData(.NewIndex) = "204"
        .AddItem "Apollo Field Sequential Color (525 lines, 29.97 fps)"
        .ItemData(.NewIndex) = "202"
        .AddItem "CBS Field Sequential Color (405 lines, 72 fps)"
        .ItemData(.NewIndex) = "205"
        .ListIndex = 0
    End With
End Sub

Private Sub SECAM_Click()
' Configures features supported (or not) by SECAM video formats and populates the VideoFormat combobox
    If ChkAudio.Enabled = False Then Call EnableAudioOption
    Call AddPALEncryptionTypes
    With VideoFormat
        .Clear
        .AddItem "SECAM-L (625 lines, 25 fps, 6.5 MHz AM audio)"
        .ItemData(.NewIndex) = "301"
        .AddItem "SECAM-D/K (625 lines, 25 fps, 6.5 MHz FM audio)"
        .ItemData(.NewIndex) = "302"
        .AddItem "SECAM-FM (625 lines, 25 fps, 6.5 MHz FM audio)"
        .ItemData(.NewIndex) = "303"
        .ListIndex = 0
    End With
End Sub

Private Sub BW_Click()
' Configures features supported (or not) by legacy black and white video formats and populates the VideoFormat combobox
    ChkACP.Enabled = False
    ChkACP.Value = vbUnchecked
    Call DisableNICAM
    NICAMSupported = False
    If ChkAudio.Enabled = False Then Call EnableAudioOption
    Call DisableTeletext
    Call DisableTeletextButton
    Call DisableWSSButton
    Call DisableWSS
    Call DisableEncryption
    Call DisableVITS
    With VideoFormat
        .Clear
        .AddItem "CCIR System A (405 lines, 25 fps)" ', -3.5 MHz AM audio)" - too long to fit!
        .ItemData(.NewIndex) = "405"
        .AddItem "CCIR System E (819 lines, 25 fps)" ', +/- 11.0 MHz AM audio)" - too long to fit!
        .ItemData(.NewIndex) = "819"
        .AddItem "240 lines (mechanical), 25 fps"
        .ItemData(.NewIndex) = "403"
        .AddItem "30 lines (mechanical), 12.5 fps"
        .ItemData(.NewIndex) = "404"
        .AddItem "Apollo (320 lines, 10 fps, FM)"
        .ItemData(.NewIndex) = "401"
        .ListIndex = 0
    End With
End Sub

Private Sub MAC_Click()
' Configures features supported (or not) by MAC video formats and populates the VideoFormat combobox
    Call DisableNICAM
    NICAMSupported = False
    Call EnableTeletextButton
    ChkAudio.Enabled = False
    ChkAudio.Value = vbChecked
    If ChkWSS.Enabled = False Then
        Call EnableWSSButton
        Call AddWSSModes
    End If
    Call EnableEncryption
    Call AddMACEncryptionTypes
    If DualVCMode = True Then Call DisableDualVCMode
    ChkACP.Enabled = True
    Call DisableVITS
    With VideoFormat
        .Clear
        .AddItem "D2-MAC (625 lines, 25 fps, AM, digital audio)"
        .ItemData(.NewIndex) = "801"
        .AddItem "D2-MAC (625 lines, 25 fps, FM, digital audio)"
        .ItemData(.NewIndex) = "802"
        .AddItem "D-MAC (625 lines, 25 fps, AM, digital audio)"
        .ItemData(.NewIndex) = "803"
        .AddItem "D-MAC (625 lines, 25 fps, FM, digital audio)"
        .ItemData(.NewIndex) = "804"
        .ListIndex = 0
    End With
End Sub

Private Sub EnableAudioOption()
    ChkAudio.Enabled = True
    ChkAudio.Value = vbChecked
End Sub

Private Sub EnableVITS()
    ChkVITS.Enabled = True
End Sub

Private Sub DisableVITS()
    ChkVITS.Value = vbUnchecked
    ChkVITS.Enabled = False
End Sub

Private Sub Common625Features()
' Configure features common to 625-line modes
    If Not encryption_key.Enabled = True Then ChkACP.Enabled = True
    Call EnableNICAM
    NICAMSupported = True
    Call EnableTeletextButton
    If ChkWSS.Enabled = False Then
        Call EnableWSSButton
        Call AddWSSModes
    End If
    Call EnableEncryption
    Call EnableVITS
End Sub

Private Sub Common525Features()
' Configure features common to 525-line modes
    If Not encryption_key.Enabled = True Then ChkACP.Enabled = True
    Call DisableNICAM
    NICAMSupported = False
    Call DisableTeletext
    Call DisableTeletextButton
    Call DisableWSSButton
    Call DisableWSS
    Call DisableEncryption
    Call EnableVITS
End Sub

Private Sub CustomFreq_Click()
' Disables the frequency_ch combobox and enables frequency_mhz to allow manual input
    frequency_ch.Enabled = False
    frequency_ch.ListIndex = -1
    frequency_mhz.Enabled = True
End Sub

Private Sub BtnTeletextBrowse_Click()
' Opens a folder browser dialog box to specify the location of teletext files
    Dim sDir As String
    sDir = BrowseForFolder(Me, "Please specify a directory that contains teletext files.", teletext_source.Text)
            teletext_source.Text = sDir
    Exit Sub
End Sub

Private Sub encryption_type_Click()
    Call CheckEncryptionType
End Sub

Private Sub LocalSource_Click()
    input_source.Enabled = True
    input_source.Text = ""
    input_source.BackColor = vbWindowBackground
    BtnSourceBrowse.Enabled = True
    inputsource = ""
    ChkRepeat.Enabled = True
    ChkRepeat.Value = vbUnchecked
    ChkInterlace.Enabled = True
    ChkInterlace.Value = vbUnchecked
    If forktype = "CJ" Then
        Call EnablePosition
        ChkTimestamp.Enabled = True
        ChkLogo.Enabled = True
        ChkSubtitles.Enabled = True
        ChkARCorrection.Enabled = True
        ARCorrectionMode.ListIndex = 0
    End If
End Sub

Private Sub TestCard_Click()
' Disables all options in the source frame as they are not supported or required
    If M3USourceFile = True Then
        M3USource.Visible = False
        input_source.Visible = True
        M3USourceFile = False
    End If
    input_source.Enabled = False
    input_source.BackColor = vbButtonFace
    BtnSourceBrowse.Enabled = False
    ChkRepeat.Enabled = False
    ChkRepeat.Value = vbUnchecked
    ChkInterlace.Enabled = False
    ChkInterlace.Value = vbUnchecked
    If forktype = "CJ" Then
        Call DisablePosition
        ChkTimestamp.Value = vbUnchecked
        ChkTimestamp.Enabled = False
        ChkLogo.Value = vbUnchecked
        ChkLogo.Enabled = False
        ChkSubtitles.Value = vbUnchecked
        ChkSubtitles.Enabled = False
        ChkARCorrection.Value = vbUnchecked
        ChkARCorrection.Enabled = False
        ARCorrectionMode.ListIndex = -1
    End If
    inputsource = "test:colourbars"
    input_source.Text = ""
End Sub

Private Sub ChkTeletext_Click()
    If ChkTeletext.Value = vbChecked Then
        teletext_source.BackColor = vbWindowBackground
        Call EnableTeletext
    Else
        teletext_source.BackColor = vbButtonFace
        Call DisableTeletext
    End If
End Sub

Private Sub ChkRFAmp_Click()
    If ChkRFAmp.Value = vbChecked Then
        Call EnableRFAmp
    Else
        Call DisableRFAmp
    End If
End Sub

Private Sub VideoFormat_Click()
    Call CheckVideoFormat
    If ChkVideoFilter.Value = vbUnchecked Then
       ' Set the sample rate to the default for the selected mode
       SampleRate.Text = DefaultSampleRate
    End If
End Sub

Private Sub ChkVideoFilter_Click()
    If ChkVideoFilter.Value = vbChecked Then
        filterparam = "--filter"
        ' Set sample rate to recommended values if an FM mode is selected
        If ChkFMDev.Enabled = True Then
            If NTSC.Value = True Then
                SampleRate.Text = 18
            ElseIf encryptiontype = "--videocrypt" Or encryptiontype = "--videocrypt2" Then
                SampleRate.Text = 14
            Else
                SampleRate.Text = 20.25
            End If
        End If
    Else
        filterparam = ""
        ' Set sample rate to back to default if an FM mode is selected
        If ChkFMDev.Enabled = True Then
            If encryptiontype = "--videocrypt" Or encryptiontype = "--videocrypt2" Then
                SampleRate.Text = 14
            Else
                SampleRate.Text = DefaultSampleRate
            End If
        End If
    End If
End Sub

Private Sub ChkWSS_Click()
    If ChkWSS.Value = vbChecked Then
        Call EnableWSS
        Call CheckWSS
    End If
    If Not ChkWSS.Value = vbChecked Then
        Call DisableWSS
        wssstatus = ""
    End If
End Sub

Private Sub EnableWSSButton()
    ChkWSS.Enabled = True
    Call AddWSSModes
End Sub

Private Sub EnableWSS()
    wss_mode.Enabled = True
    Call AddWSSModes
    wss_mode.ListIndex = 0
End Sub

Private Sub DisableWSS()
    wss_mode.Enabled = False
    wss_mode.ListIndex = -1
End Sub

Private Sub DisableWSSButton()
    ChkWSS.Enabled = False
    ChkWSS.Value = 0
End Sub

Private Sub EnableTeletext()
    ChkTeletext.Enabled = True
    teletext_source.Enabled = True
    BtnTeletextBrowse.Enabled = True
    BtnTeletextDownload.Enabled = True
    BtnTeletextBrowseFile.Enabled = True
    TeletextFlag = "--teletext"
End Sub

Private Sub EnableTeletextButton()
    ChkTeletext.Enabled = True
End Sub

Private Sub DisableTeletext()
    teletext_source.Enabled = False
    BtnTeletextBrowse.Enabled = False
    BtnTeletextDownload.Enabled = False
    BtnTeletextBrowseFile.Enabled = False
    teletext_source.Text = ""
    TeletextFlag = ""
    teletextsource = ""
End Sub

Private Sub DisableTeletextButton()
    ChkTeletext.Enabled = False
    ChkTeletext.Value = 0
End Sub

Private Sub EnableEncryption()
    encryption_type.Enabled = True
End Sub

Private Sub DisableEncryption()
    encryption_type.Enabled = False
    encryption_type.ListIndex = "0"
End Sub

Private Sub EnableNICAM()
    If ChkAudio.Value = vbChecked Then
        ChkNICAM.Enabled = True
        ChkNICAM.Value = vbChecked
    End If
End Sub

Private Sub DisableNICAM()
    If ChkNICAM.Value = vbUnchecked Then
        nicamstatus = ""
        ChkNICAM.Enabled = False
        ChkNICAM.Value = vbUnchecked
    Else
        ChkNICAM.Enabled = False
        ChkNICAM.Value = vbUnchecked
    End If
End Sub

Private Sub ChkFMDev_Click()
    If ChkFMDev.Value = vbChecked Then
        fmdevargument = "--deviation"
        fm_deviation.Enabled = True
        fm_deviation.BackColor = vbWindowBackground
    Else
        fmdevargument = ""
        fm_deviation.Enabled = False
        fm_deviation.BackColor = vbButtonFace
        fm_deviation.Text = ""
        fmdevvalue = ""
    End If
End Sub

Private Sub ChkAudio_Click()
' Since the audio and NICAM settings are linked, make sure that NICAM isn't enabled when audio is disabled
    If ChkAudio.Value = vbChecked Then
        audiostatus = ""
        If NICAMSupported = True Then Call EnableNICAM
' If Syster, D11 or EuroCrypt are enabled, enable the encrypt audio checkbox
        If encryptiontype = "--syster" Or encryptiontype = "--d11" Or encryptiontype = "--smartcrypt" _
        Or encryptiontype = "--single-cut" Or encryptiontype = "--double-cut" _
        Then ChkEncryptAudio.Enabled = True
    Else
        Call DisableNICAM
        audiostatus = "--noaudio"
' If Syster, D11 or EuroCrypt are not enabled, disable and grey out the encrypt audio checkbox
        If encryptiontype = "--syster" Or encryptiontype = "--d11" Or encryptiontype = "--smartcrypt" _
        Or encryptiontype = "--single-cut" Or encryptiontype = "--double-cut" Then
            ChkEncryptAudio.Enabled = False
            ChkEncryptAudio.Value = vbUnchecked
        End If
    End If
End Sub

Private Sub ChkEnableEMM_Click()
    If ChkEnableEMM.Value = vbChecked Then
        ChkDisableEMM.Value = vbUnchecked
        CardNumber.Enabled = True
        CardNumber.BackColor = vbWindowBackground
        EMMParam = "--enableemm"
' This prevents the CardNumber textbox from being blanked when toggling between the activate and deactivate checkboxes
    ElseIf Not ChkDisableEMM.Value = vbChecked Then
        CardNumber.Text = ""
        CardNumber.Enabled = False
        CardNumber.BackColor = vbButtonFace
        EMMParam = ""
    End If
End Sub

Private Sub ChkDisableEMM_Click()
    If ChkDisableEMM.Value = vbChecked Then
        ChkEnableEMM.Value = vbUnchecked
        CardNumber.Enabled = True
        CardNumber.BackColor = vbWindowBackground
        EMMParam = "--disableemm"
' This prevents the CardNumber textbox from being blanked when toggling between the activate and deactivate checkboxes
    ElseIf Not ChkEnableEMM.Value = vbChecked Then
        CardNumber.Text = ""
        CardNumber.Enabled = False
        CardNumber.BackColor = vbButtonFace
        EMMParam = ""
    End If
End Sub

Private Sub ChkEncryptAudio_Click()
If ChkEncryptAudio.Value = vbChecked Then
    If encryptiontype = "--syster" Or encryptiontype = "--d11" Or encryptiontype = "--smartcrypt" Then
        audioencryption = "--systeraudio"
    ElseIf encryptiontype = "--single-cut" Or encryptiontype = "--double-cut" Then
        audioencryption = "--scramble-audio"
    End If
Else
    audioencryption = ""
End If
End Sub

Private Sub AddEuropeUHFChannels()
' Populate frequency_ch with standard Western European UHF channels (E21 to E69, 471.25 to 855.25 MHz)
' We add the frequency (in Hz) as the item data and pass it raw to hacktv
    UHF.Value = True
    UHF.Enabled = True
    With frequency_ch
        .Clear
        .AddItem "E21"
        .ItemData(.NewIndex) = "471250000"
        .AddItem "E22"
        .ItemData(.NewIndex) = "479250000"
        .AddItem "E23"
        .ItemData(.NewIndex) = "487250000"
        .AddItem "E24"
        .ItemData(.NewIndex) = "495250000"
        .AddItem "E25"
        .ItemData(.NewIndex) = "503250000"
        .AddItem "E26"
        .ItemData(.NewIndex) = "511250000"
        .AddItem "E27"
        .ItemData(.NewIndex) = "519250000"
        .AddItem "E28"
        .ItemData(.NewIndex) = "527250000"
        .AddItem "E29"
        .ItemData(.NewIndex) = "535250000"
        .AddItem "E30"
        .ItemData(.NewIndex) = "543250000"
        .AddItem "E31"
        .ItemData(.NewIndex) = "551250000"
        .AddItem "E32"
        .ItemData(.NewIndex) = "559250000"
        .AddItem "E33"
        .ItemData(.NewIndex) = "567250000"
        .AddItem "E34"
        .ItemData(.NewIndex) = "575250000"
        .AddItem "E35"
        .ItemData(.NewIndex) = "583250000"
        .AddItem "E36"
        .ItemData(.NewIndex) = "591250000"
        .AddItem "E37"
        .ItemData(.NewIndex) = "599250000"
        .AddItem "E38"
        .ItemData(.NewIndex) = "607250000"
        .AddItem "E39"
        .ItemData(.NewIndex) = "615250000"
        .AddItem "E40"
        .ItemData(.NewIndex) = "623250000"
        .AddItem "E41"
        .ItemData(.NewIndex) = "631250000"
        .AddItem "E42"
        .ItemData(.NewIndex) = "639250000"
        .AddItem "E43"
        .ItemData(.NewIndex) = "647250000"
        .AddItem "E44"
        .ItemData(.NewIndex) = "655250000"
        .AddItem "E45"
        .ItemData(.NewIndex) = "663250000"
        .AddItem "E46"
        .ItemData(.NewIndex) = "671250000"
        .AddItem "E47"
        .ItemData(.NewIndex) = "679250000"
        .AddItem "E48"
        .ItemData(.NewIndex) = "687250000"
        ' E49 to E60 were deallocated in 2020, pending allocation to rural 5G mobile services.
        .AddItem "E49"
        .ItemData(.NewIndex) = "695250000"
        .AddItem "E50"
        .ItemData(.NewIndex) = "703250000"
        .AddItem "E51"
        .ItemData(.NewIndex) = "711250000"
        .AddItem "E52"
        .ItemData(.NewIndex) = "719250000"
        .AddItem "E53"
        .ItemData(.NewIndex) = "727250000"
        .AddItem "E54"
        .ItemData(.NewIndex) = "735250000"
        .AddItem "E55"
        .ItemData(.NewIndex) = "743250000"
        .AddItem "E56"
        .ItemData(.NewIndex) = "751250000"
        .AddItem "E57"
        .ItemData(.NewIndex) = "759250000"
        .AddItem "E58"
        .ItemData(.NewIndex) = "767250000"
        .AddItem "E59"
        .ItemData(.NewIndex) = "775250000"
        .AddItem "E60"
        .ItemData(.NewIndex) = "783250000"
        ' E61 to E69 were deallocated after analogue switch-off in 2012. Now used for LTE/4G mobile.
        .AddItem "E61"
        .ItemData(.NewIndex) = "791250000"
        .AddItem "E62"
        .ItemData(.NewIndex) = "799250000"
        .AddItem "E63"
        .ItemData(.NewIndex) = "807250000"
        .AddItem "E64"
        .ItemData(.NewIndex) = "815250000"
        .AddItem "E65"
        .ItemData(.NewIndex) = "823250000"
        .AddItem "E66"
        .ItemData(.NewIndex) = "831250000"
        .AddItem "E67"
        .ItemData(.NewIndex) = "839250000"
        .AddItem "E68"
        .ItemData(.NewIndex) = "847250000"
        .AddItem "E69"
        .ItemData(.NewIndex) = "855250000"
        .ListIndex = 0
    End With
End Sub

Private Sub AddSysIVHFChannels()
' Populate frequency_ch with standard Irish PAL-I 8 MHz VHF channels (A to J, 45.75 to 223.25 MHz)
' We add the frequency (in Hz) as the item data and pass it raw to hacktv
    With frequency_ch
        .Clear
        ' Channel A was never used but all TVs supported it so we'll keep it
        .AddItem "A"
        .ItemData(.NewIndex) = "45750000"
        .AddItem "B"
        .ItemData(.NewIndex) = "53750000"
        .AddItem "C"
        .ItemData(.NewIndex) = "61750000"
        .AddItem "D"
        .ItemData(.NewIndex) = "175250000"
        .AddItem "E"
        .ItemData(.NewIndex) = "183250000"
        .AddItem "F"
        .ItemData(.NewIndex) = "191250000"
        .AddItem "G"
        .ItemData(.NewIndex) = "199250000"
        .AddItem "H"
        .ItemData(.NewIndex) = "207250000"
        .AddItem "I"
        .ItemData(.NewIndex) = "215250000"
        ' Channel J was rarely used
        ' Used by Kippure for a short time and the Moville and Letterkenny transposers until the late 90s.
        .AddItem "J"
        .ItemData(.NewIndex) = "223250000"
        .ListIndex = 0
    End With
End Sub

Private Sub AddSystemBChannels()
' Populate frequency_ch with standard Western European PAL-B 7 MHz VHF channels (E2 to E12, 48.25 to 224.25 MHz)
' We add the frequency (in Hz) as the item data and pass it raw to hacktv
    With frequency_ch
        .Clear
        .AddItem "E2"
        .ItemData(.NewIndex) = "48250000"
        .AddItem "E3"
        .ItemData(.NewIndex) = "55250000"
        .AddItem "E4"
        .ItemData(.NewIndex) = "62250000"
        .AddItem "E5"
        .ItemData(.NewIndex) = "175250000"
        .AddItem "E6"
        .ItemData(.NewIndex) = "182250000"
        .AddItem "E7"
        .ItemData(.NewIndex) = "189250000"
        .AddItem "E8"
        .ItemData(.NewIndex) = "196250000"
        .AddItem "E9"
        .ItemData(.NewIndex) = "203250000"
        .AddItem "E10"
        .ItemData(.NewIndex) = "210250000"
        .AddItem "E11"
        .ItemData(.NewIndex) = "217250000"
        .AddItem "E12"
        .ItemData(.NewIndex) = "224250000"
        .ListIndex = 0
    End With
End Sub

Private Sub AddFrenchVHFChannels()
' Populate frequency_ch with standard French SECAM-L VHF channels (1 to 10, 47.75 to 216.00 MHz)
' We add the frequency (in Hz) as the item data and pass it raw to hacktv
    With frequency_ch
        .Clear
        ' L1 was never used but some TVs support it so we'll keep it
        .AddItem "L1"
        .ItemData(.NewIndex) = "47750000"
        .AddItem "L2"
        .ItemData(.NewIndex) = "55750000"
        .AddItem "L3"
        .ItemData(.NewIndex) = "60500000"
        .AddItem "L4"
        .ItemData(.NewIndex) = "63750000"
        .AddItem "L5"
        .ItemData(.NewIndex) = "176000000"
        .AddItem "L6"
        .ItemData(.NewIndex) = "184000000"
        .AddItem "L7"
        .ItemData(.NewIndex) = "192000000"
        .AddItem "L8"
        .ItemData(.NewIndex) = "200000000"
        .AddItem "L9"
        .ItemData(.NewIndex) = "208000000"
        .AddItem "L10"
        .ItemData(.NewIndex) = "216000000"
        .ListIndex = 0
    End With
End Sub

Private Sub AddSystemAChannels()
' Populate frequency_ch with standard British System A VHF channels (B1 to B13, 45.00 to 214.75 MHz)
' We add the frequency (in Hz) as the item data and pass it raw to hacktv
    With frequency_ch
        .Clear
        .AddItem "B1"
        .ItemData(.NewIndex) = "45000000"
        .AddItem "B2"
        .ItemData(.NewIndex) = "51750000"
        .AddItem "B3"
        .ItemData(.NewIndex) = "56750000"
        .AddItem "B4"
        .ItemData(.NewIndex) = "61750000"
        .AddItem "B5"
        .ItemData(.NewIndex) = "66750000"
        .AddItem "B6"
        .ItemData(.NewIndex) = "179750000"
        .AddItem "B7"
        .ItemData(.NewIndex) = "184750000"
        .AddItem "B8"
        .ItemData(.NewIndex) = "189750000"
        .AddItem "B9"
        .ItemData(.NewIndex) = "194750000"
        .AddItem "B10"
        .ItemData(.NewIndex) = "199750000"
        .AddItem "B11"
        .ItemData(.NewIndex) = "204750000"
        .AddItem "B12"
        .ItemData(.NewIndex) = "209750000"
        .AddItem "B13"
        .ItemData(.NewIndex) = "214750000"
        ' B14 was allocated but never used and most TVs didn't support it
        '.AddItem "B14"
        '.ItemData(.NewIndex) = "219750000"
        .ListIndex = 0
    End With
End Sub

Private Sub AddSystemEChannels()
' Populate frequency_ch with standard French System E VHF channels (F2 to F12, 41.25 to 224.25 MHz)
' We add the frequency (in Hz) as the item data and pass it raw to hacktv
    With frequency_ch
        VHF.Enabled = True
        VHF.Value = True
        UHF.Enabled = False
        .Clear
        ' Channel F1 was not used on 819 lines, so we'll leave it out
        '.AddItem "F1"
        '.ItemData(.NewIndex) = "41250000"
        .AddItem "F2"
        .ItemData(.NewIndex) = "48250000"
        ' Channel F3 was not part of the official standard but it existed in some form
        '.AddItem "F3"
        '.ItemData(.NewIndex) = "55250000"
        .AddItem "F4"
        .ItemData(.NewIndex) = "62250000"
        .AddItem "F5"
        .ItemData(.NewIndex) = "175250000"
        .AddItem "F6"
        .ItemData(.NewIndex) = "182250000"
        .AddItem "F7"
        .ItemData(.NewIndex) = "189250000"
        .AddItem "F8"
        .ItemData(.NewIndex) = "196250000"
        .AddItem "F9"
        .ItemData(.NewIndex) = "203250000"
        .AddItem "F10"
        .ItemData(.NewIndex) = "210250000"
        .AddItem "F11"
        .ItemData(.NewIndex) = "217250000"
        .AddItem "F12"
        .ItemData(.NewIndex) = "224250000"
        .ListIndex = 0
    End With
End Sub

Private Sub AddNTSCVHFChannels()
' Populate frequency_ch with standard American NTSC VHF channels (A2 to A13, 55.25 to 211.25 MHz)
' Also used in Brazil for PAL-M
' We add the frequency (in Hz) as the item data and pass it raw to hacktv
    With frequency_ch
        VHF.Enabled = True
        VHF.Value = True
        .Clear
        .AddItem "A2"
        .ItemData(.NewIndex) = "55250000"
        .AddItem "A3"
        .ItemData(.NewIndex) = "61250000"
        .AddItem "A4"
        .ItemData(.NewIndex) = "67250000"
        .AddItem "A5"
        .ItemData(.NewIndex) = "77250000"
        .AddItem "A6"
        .ItemData(.NewIndex) = "83250000"
        .AddItem "A7"
        .ItemData(.NewIndex) = "175250000"
        .AddItem "A8"
        .ItemData(.NewIndex) = "181250000"
        .AddItem "A9"
        .ItemData(.NewIndex) = "187250000"
        .AddItem "A10"
        .ItemData(.NewIndex) = "193250000"
        .AddItem "A11"
        .ItemData(.NewIndex) = "199250000"
        .AddItem "A12"
        .ItemData(.NewIndex) = "205250000"
        .AddItem "A13"
        .ItemData(.NewIndex) = "211250000"
        .ListIndex = 0
    End With
End Sub

Private Sub AddNTSCUHFChannels()
' Populate frequency_ch with standard American NTSC UHF channels (A14 to A83, 471.25 to 885.25 MHz)
' Also used in Brazil for PAL-M
' We add the frequency (in Hz) as the item data and pass it raw to hacktv
    With frequency_ch
        UHF.Value = True
        Call EnableUHF
        Call EnableVHF
        .Clear
        .AddItem "A14"
        .ItemData(.NewIndex) = "471250000"
        .AddItem "A15"
        .ItemData(.NewIndex) = "477250000"
        .AddItem "A16"
        .ItemData(.NewIndex) = "483250000"
        .AddItem "A17"
        .ItemData(.NewIndex) = "489250000"
        .AddItem "A18"
        .ItemData(.NewIndex) = "495250000"
        .AddItem "A19"
        .ItemData(.NewIndex) = "501250000"
        .AddItem "A20"
        .ItemData(.NewIndex) = "507250000"
        .AddItem "A21"
        .ItemData(.NewIndex) = "513250000"
        .AddItem "A22"
        .ItemData(.NewIndex) = "519250000"
        .AddItem "A23"
        .ItemData(.NewIndex) = "525250000"
        .AddItem "A24"
        .ItemData(.NewIndex) = "531250000"
        .AddItem "A25"
        .ItemData(.NewIndex) = "537250000"
        .AddItem "A26"
        .ItemData(.NewIndex) = "543250000"
        .AddItem "A27"
        .ItemData(.NewIndex) = "549250000"
        .AddItem "A28"
        .ItemData(.NewIndex) = "555250000"
        .AddItem "A29"
        .ItemData(.NewIndex) = "561250000"
        .AddItem "A30"
        .ItemData(.NewIndex) = "567250000"
        .AddItem "A31"
        .ItemData(.NewIndex) = "573250000"
        .AddItem "A32"
        .ItemData(.NewIndex) = "579250000"
        .AddItem "A33"
        .ItemData(.NewIndex) = "585250000"
        .AddItem "A34"
        .ItemData(.NewIndex) = "591250000"
        .AddItem "A35"
        .ItemData(.NewIndex) = "597250000"
        .AddItem "A36"
        .ItemData(.NewIndex) = "603250000"
        .AddItem "A37"
        .ItemData(.NewIndex) = "609250000"
        .AddItem "A38"
        .ItemData(.NewIndex) = "615250000"
        .AddItem "A39"
        .ItemData(.NewIndex) = "621250000"
        .AddItem "A40"
        .ItemData(.NewIndex) = "627250000"
        .AddItem "A41"
        .ItemData(.NewIndex) = "633250000"
        .AddItem "A42"
        .ItemData(.NewIndex) = "639250000"
        .AddItem "A43"
        .ItemData(.NewIndex) = "645250000"
        .AddItem "A44"
        .ItemData(.NewIndex) = "651250000"
        .AddItem "A45"
        .ItemData(.NewIndex) = "657250000"
        .AddItem "A46"
        .ItemData(.NewIndex) = "663250000"
        .AddItem "A47"
        .ItemData(.NewIndex) = "669250000"
        .AddItem "A48"
        .ItemData(.NewIndex) = "675250000"
        .AddItem "A49"
        .ItemData(.NewIndex) = "681250000"
        .AddItem "A50"
        .ItemData(.NewIndex) = "687250000"
        .AddItem "A51"
        .ItemData(.NewIndex) = "693250000"
        .AddItem "A52"
        .ItemData(.NewIndex) = "699250000"
        .AddItem "A53"
        .ItemData(.NewIndex) = "705250000"
        .AddItem "A54"
        .ItemData(.NewIndex) = "711250000"
        .AddItem "A55"
        .ItemData(.NewIndex) = "717250000"
        .AddItem "A56"
        .ItemData(.NewIndex) = "723250000"
        .AddItem "A57"
        .ItemData(.NewIndex) = "729250000"
        .AddItem "A58"
        .ItemData(.NewIndex) = "735250000"
        .AddItem "A59"
        .ItemData(.NewIndex) = "741250000"
        .AddItem "A60"
        .ItemData(.NewIndex) = "747250000"
        .AddItem "A61"
        .ItemData(.NewIndex) = "753250000"
        .AddItem "A62"
        .ItemData(.NewIndex) = "759250000"
        .AddItem "A63"
        .ItemData(.NewIndex) = "765250000"
        .AddItem "A64"
        .ItemData(.NewIndex) = "771250000"
        .AddItem "A65"
        .ItemData(.NewIndex) = "777250000"
        .AddItem "A66"
        .ItemData(.NewIndex) = "783250000"
        .AddItem "A67"
        .ItemData(.NewIndex) = "789250000"
        .AddItem "A68"
        .ItemData(.NewIndex) = "795250000"
        .AddItem "A69"
        .ItemData(.NewIndex) = "801250000"
        ' A70 to A83 were deallocated by the FCC in 1983 to be used by AMPS analog cellular services
        .AddItem "A70"
        .ItemData(.NewIndex) = "807250000"
        .AddItem "A71"
        .ItemData(.NewIndex) = "813250000"
        .AddItem "A72"
        .ItemData(.NewIndex) = "819250000"
        .AddItem "A73"
        .ItemData(.NewIndex) = "825250000"
        .AddItem "A74"
        .ItemData(.NewIndex) = "831250000"
        .AddItem "A75"
        .ItemData(.NewIndex) = "837250000"
        .AddItem "A76"
        .ItemData(.NewIndex) = "843250000"
        .AddItem "A77"
        .ItemData(.NewIndex) = "849250000"
        .AddItem "A78"
        .ItemData(.NewIndex) = "855250000"
        .AddItem "A79"
        .ItemData(.NewIndex) = "861250000"
        .AddItem "A80"
        .ItemData(.NewIndex) = "867250000"
        .AddItem "A81"
        .ItemData(.NewIndex) = "873250000"
        .AddItem "A82"
        .ItemData(.NewIndex) = "879250000"
        .AddItem "A83"
        .ItemData(.NewIndex) = "885250000"
        .ListIndex = 0
    End With
End Sub

Private Sub AddSystemDRussiaChannels()
' Populate frequency_ch with standard Russian SECAM-D VHF channels (1 to 12, 49.75 to 223.25 MHz)
' Note that channels 4 and 5 overlap with the standard VHF-FM band
' We add the frequency (in Hz) as the item data and pass it raw to hacktv
    With frequency_ch
        Call EnableVHF
        .Clear
        .AddItem "R1"
        .ItemData(.NewIndex) = "49750000"
        .AddItem "R2"
        .ItemData(.NewIndex) = "59250000"
        .AddItem "R3"
        .ItemData(.NewIndex) = "77250000"
        .AddItem "R4"
        .ItemData(.NewIndex) = "85250000"
        .AddItem "R5"
        .ItemData(.NewIndex) = "93250000"
        .AddItem "R6"
        .ItemData(.NewIndex) = "175250000"
        .AddItem "R7"
        .ItemData(.NewIndex) = "183250000"
        .AddItem "R8"
        .ItemData(.NewIndex) = "191250000"
        .AddItem "R9"
        .ItemData(.NewIndex) = "199250000"
        .AddItem "R10"
        .ItemData(.NewIndex) = "207250000"
        .AddItem "R11"
        .ItemData(.NewIndex) = "215250000"
        .AddItem "R12"
        .ItemData(.NewIndex) = "223250000"
        .ListIndex = 0
    End With
End Sub

Private Sub AddBSBChannels()
' Populate frequency_ch with intermediate frequencies (IFs) for BSB satellite receivers
' Based on information provided by fsphil at https://www.sanslogic.co.uk/dmac/bsb.html
' Tested and confirmed working on an ITT/Nokia BSB receiver
' We add the frequency (in Hz) as the item data and pass it raw to hacktv
    With frequency_ch
        .Clear
        ' Now/Sky News
        ' 11.78502 GHz, IF 1015.84 MHz
        .AddItem "4 (Now)"
        .ItemData(.NewIndex) = "1015840000"
        ' Galaxy/Sky One
        ' 11.86174 GHz, IF 1092.56 MHz
        .AddItem "8 (Galaxy)"
        .ItemData(.NewIndex) = "1092560000"
        ' The Sports Channel/Sky Sports
        ' 11.93846 GHz, IF 1169.28 MHz
        .AddItem "12 (Sports Ch)"
        .ItemData(.NewIndex) = "1169280000"
        ' The Power Station/Sky Movies Plus
        ' 12.01518 GHz, IF 1246.00 MHz
        .AddItem "16 (Power St)"
        .ItemData(.NewIndex) = "1246000000"
        ' The Movie Channel
        ' 12.09190 GHz, IF 1322.72 MHz
        .AddItem "20 (Movie Ch)"
        .ItemData(.NewIndex) = "1322720000"
        ' Irish DBS channels are listed below
        ' These were never used but are available for use on BSB receivers
        ' 11.74666 GHz, IF 977.48 MHz
        .AddItem "2 (Irish DBS)"
        .ItemData(.NewIndex) = "977480000"
        ' 11.82338 GHz, IF 1054.2 MHz
        .AddItem "6 (Irish DBS)"
        .ItemData(.NewIndex) = "1054200000"
        ' 11.90010 GHz, IF 1130.92 MHz
        .AddItem "10 (Irish DBS)"
        .ItemData(.NewIndex) = "1130920000"
        ' 11.97682 GHz, IF 1207.64 MHz
        .AddItem "14 (Irish DBS)"
        .ItemData(.NewIndex) = "1207640000"
        ' 12.05354 GHz, IF 1284.36 MHz
        .AddItem "18 (Irish DBS)"
        .ItemData(.NewIndex) = "1284360000"
        .ListIndex = 0
    End With
End Sub

Private Sub SetFrequency()
' This sub is only needed when the frequency_ch combobox is enabled
    If frequency_ch.Enabled = True Then
' Set a variable for the selected channel number's frequency in Hz
        freq = frequency_ch.ItemData(frequency_ch.ListIndex)
' As the frequency passed to hacktv is expected to be in Hz, we do a little calculation to make it
' clearer for the user. Divide by 1,000,000 to show the value in MHz and populate frequency_mhz.
        Dim Hz As Single
        Hz = Val(freq) / 1000000
        frequency_mhz.Text = Format$(Hz)
    End If
End Sub

Private Sub ChkACP_Click()
    If ChkACP.Value = vbUnchecked Then
        acpstatus = ""
    Else
        acpstatus = "--acp"
    End If
End Sub

Private Sub chkLogo_Click()
    If ChkLogo.Value = vbChecked Then
        LogoFolder.Enabled = True
' Set variables
        Dim strFile As String
        Dim strPath As String
        Dim missinglogos As String
        strPath = HackTVPath & "\resources\logos\*.png"
        If RunningOnWine = False Then
            missinglogos = "No PNG files were found in Resources\Logos, or the directory was not found."
        Else
            missinglogos = "No PNG files were found in resources/logos, or the directory was not found."
        End If
' Populate the LogoFolder combobox with PNG files from the strPath variable specified above
' This path is hard-coded into hacktv and cannot be changed without a re-compile
        strFile = Dir(strPath)
        LogoFolder.Clear
        Do While strFile <> ""
            If Not (strFile = "." Or strFile = "..") Then
                LogoFolder.AddItem strFile
            End If
            strFile = Dir()
        Loop
        If LogoFolder.ListCount = "0" Then
            MsgBox missinglogos, vbExclamation, App.Title
            ChkLogo.Value = vbUnchecked
        Else
            logoParam = "--logo"
            LogoFolder.ListIndex = 0
        End If
' If the chkLogo checkbox is not selected, disable the LogoFolder combobox and its variables
    Else
        LogoFolder.Enabled = False
        LogoFolder.ListIndex = -1
        logoParam = ""
        logoPath = ""
    End If
End Sub

Private Sub LogoFolder_Click()
' Set the value of the logoPath variable to the contents of the LogoFolder combobox
    logoPath = LogoFolder
End Sub

Private Sub wss_mode_Click()
    Call CheckWSS
End Sub

Private Sub CheckWSS()
' Read the index value assigned to the wss_mode combobox
' Use this value to set the correct command line parameters
    If ChkWSS.Value = vbUnchecked Then
        wssstatus = ""
    Else
        wssstatus = wss_mode.ItemData(wss_mode.ListIndex)
        If wssstatus = "501" Then wssstatus = "--wss auto"
        If wssstatus = "502" Then wssstatus = "--wss 4:3"
        If wssstatus = "503" Then wssstatus = "--wss 16:9"
        If wssstatus = "504" Then wssstatus = "--wss 14:9-letterbox"
        If wssstatus = "505" Then wssstatus = "--wss 16:9-letterbox"
    End If
End Sub

Private Sub CheckVideoFormat()
' Read the index value assigned to the VideoFormat combobox
' Use this value to set the correct command line parameters and enable/disable
' additional options for the selected format
    sys = VideoFormat.ItemData(VideoFormat.ListIndex)
    If sys = "101" Then
        Call Common625Features
        Call EnableVHF
        Call EnableUHF
        Call AddEuropeUHFChannels
        Call EnableNICAM
        NICAMSupported = True
        Call DisableFMDeviation
        DefaultSampleRate = 16
        sys = "i"
        Exit Sub
    End If
    If sys = "102" Then
        Call Common625Features
        Call EnableVHF
        Call EnableUHF
        Call AddEuropeUHFChannels
        Call EnableNICAM
        NICAMSupported = True
        Call DisableFMDeviation
        DefaultSampleRate = 16
        sys = "g"
        Exit Sub
    End If
    If sys = "103" Then
        Call Common625Features
        Call DisableVHF
        Call DisableUHF
        Call DisableNICAM
        NICAMSupported = False
        Call EnableFMDeviation
        DefaultSampleRate = 16
        CustomFreq.Value = True
        sys = "pal-fm"
        Exit Sub
    End If
    If sys = "104" Then
        Call Common525Features
        Call EnableVHF
        Call EnableUHF
        Call AddNTSCUHFChannels
        Call DisableNICAM
        NICAMSupported = False
        Call DisableFMDeviation
        DefaultSampleRate = 13.5
        sys = "pal-m"
        Exit Sub
    End If
    If sys = "201" Then
        Call Common525Features
        Call EnableVHF
        Call EnableUHF
        Call AddNTSCUHFChannels
        Call DisableFMDeviation
        DefaultSampleRate = 13.5
        sys = "m"
        Exit Sub
    End If
    If sys = "301" Then
        Call Common625Features
        Call EnableVHF
        Call EnableUHF
        Call AddEuropeUHFChannels
        Call DisableFMDeviation
        DefaultSampleRate = 16
        sys = "l"
        Exit Sub
    End If
    If sys = "405" Then
        Call DisableUHF
        Call EnableVHF
        VHF.Value = True
        Call AddSystemAChannels
        Call DisableFMDeviation
        DefaultSampleRate = 6.48
        sys = "a"
        Exit Sub
    End If
    If sys = "819" Then
        Call DisableUHF
        Call EnableVHF
        Call AddSystemEChannels
        Call DisableFMDeviation
        DefaultSampleRate = 20.475
        sys = "e"
        Exit Sub
    End If
    If sys = "801" Then
        Call DisableUHF
        Call DisableVHF
        Call DisableFMDeviation
        DefaultSampleRate = 20.25
        CustomFreq.Value = True
        sys = "d2mac-am"
        Exit Sub
    End If
    If sys = "802" Then
        Call DisableUHF
        Call DisableVHF
        Call EnableFMDeviation
        DefaultSampleRate = 20.25
        CustomFreq.Value = True
        sys = "d2mac-fm"
        Exit Sub
    End If
    If sys = "803" Then
        Call DisableUHF
        Call DisableVHF
        Call DisableFMDeviation
        DefaultSampleRate = 20.25
        CustomFreq.Value = True
        sys = "dmac-am"
        Exit Sub
    End If
    If sys = "804" Then
        Call EnableUHF
        Call DisableVHF
        Call EnableFMDeviation
        DefaultSampleRate = 20.25
        UHF.Value = True
        Call AddBSBChannels
        sys = "dmac-fm"
        Exit Sub
    End If
    If sys = "403" Then
        Call DisableUHF
        Call DisableVHF
        Call DisableFMDeviation
        DefaultSampleRate = 1.992
        CustomFreq.Enabled = True
        CustomFreq.Value = True
        sys = "240-am"
        Exit Sub
    End If
    If sys = "404" Then
        Call DisableUHF
        Call DisableVHF
        Call DisableFMDeviation
        DefaultSampleRate = 0.1005
        CustomFreq.Enabled = True
        CustomFreq.Value = True
        sys = "30-am"
        Exit Sub
    End If
    If sys = "401" Then
        Call DisableUHF
        Call DisableVHF
        Call EnableFMDeviation
        DefaultSampleRate = 2.048
        CustomFreq.Enabled = True
        CustomFreq.Value = True
        sys = "apollo-fm"
        Exit Sub
    End If
    If sys = "202" Then
        Call Common525Features
        Call DisableUHF
        Call DisableVHF
        Call EnableFMDeviation
        DefaultSampleRate = 13.5
        CustomFreq.Enabled = True
        CustomFreq.Value = True
        sys = "apollo-fsc-fm"
        Exit Sub
    End If
    If sys = "302" Then
        Call Common625Features
        Call EnableVHF
        Call EnableUHF
        Call DisableNICAM
        Call AddEuropeUHFChannels
        Call DisableFMDeviation
        DefaultSampleRate = 16
        sys = "d"
        Exit Sub
    End If
    If sys = "203" Then
        Call Common525Features
        Call DisableVHF
        Call DisableUHF
        Call DisableNICAM
        NICAMSupported = False
        Call EnableFMDeviation
        DefaultSampleRate = 13.5
        CustomFreq.Value = True
        sys = "ntsc-fm"
        Exit Sub
    End If
    If sys = "303" Then
        Call Common625Features
        Call DisableVHF
        Call DisableUHF
        Call DisableNICAM
        NICAMSupported = False
        Call EnableFMDeviation
        DefaultSampleRate = 16
        CustomFreq.Value = True
        sys = "secam-fm"
        Exit Sub
    End If
    If sys = "204" Then
        Call Common525Features
        Call DisableVHF
        Call DisableUHF
        Call DisableNICAM
        NICAMSupported = False
        Call EnableFMDeviation
        DefaultSampleRate = 13.5
        CustomFreq.Value = True
        sys = "ntsc-bs"
        Exit Sub
    End If
    If sys = "205" Then
        Call DisableUHF
        Call DisableVHF
        Call DisableFMDeviation
        DefaultSampleRate = 18.954
        CustomFreq.Enabled = True
        CustomFreq.Value = True
        sys = "m-cbs405"
        Exit Sub
    End If
End Sub

Private Sub AddPALEncryptionTypes()
' Populates the encryption_type combobox with the supported encryption types for the selected build
' The index integer value is read later and translated to the correct command line parameters
    With encryption_type
        .Clear
        .AddItem "No scrambling"
        .ItemData(.NewIndex) = "6000"
        .AddItem "VideoCrypt-I"
        .ItemData(.NewIndex) = "6001"
        .AddItem "VideoCrypt-II"
        .ItemData(.NewIndex) = "6002"
        .AddItem "VideoCrypt-I & II"
        .ItemData(.NewIndex) = "6006"
        .AddItem "VideoCrypt-S"
        .ItemData(.NewIndex) = "6003"
        .AddItem "Nagravision Syster"
        .ItemData(.NewIndex) = "6004"
        If forktype = "CJ" Then
            .AddItem "Discret 11"
            .ItemData(.NewIndex) = "6005"
            .AddItem "Smartcrypt"
            .ItemData(.NewIndex) = "6009"
        End If
        .ListIndex = "0"
    End With
End Sub

Private Sub AddMACEncryptionTypes()
' Populates the encryption_type combobox with the supported encryption types for the selected build
' The index integer value is read later and translated to the correct command line parameters
    With encryption_type
        .Clear
        .AddItem "No scrambling"
        .ItemData(.NewIndex) = "6000"
        .AddItem "Single cut"
        .ItemData(.NewIndex) = "6007"
        .AddItem "Double cut"
        .ItemData(.NewIndex) = "6008"
        .ListIndex = "0"
    End With
End Sub

Private Sub CheckEncryptionType()
    encryptiontype = encryption_type.ItemData(encryption_type.ListIndex)
    If encryptiontype = "6000" Then
        Call DisableDualVCMode
        Call DisableAudioEncryption
        SampleRate.Text = DefaultSampleRate
        ChkFindKey.Enabled = False
        ChkFindKey.Value = vbUnchecked
        ChkShowSerial.Enabled = False
        ChkShowSerial.Value = vbUnchecked
        ChkShowSerial.Visible = False
        ChkEncryptAudio.Visible = True
        encryptiontype = ""
    ElseIf encryptiontype = "6006" Then
    ' Hide the encryption_key box and show the vc1key and vc2key boxes
        With encryption_key
            .Enabled = False
            .Visible = False
            .Clear
        End With
        vc1key.Enabled = True
        vc1key.Visible = True
        vc2key.Enabled = True
        vc2key.Visible = True
        DualVCMode = True
        Call AddBothVCModes
    Else
        If DualVCMode = True Then Call DisableDualVCMode
        encryption_key.Enabled = True
        If encryptiontype = "6001" And forktype = "CJ" Then
            Call AddVC1Modes
            ChkEncryptAudio.Value = vbUnchecked
            ChkEncryptAudio.Visible = False
            ChkShowSerial.Visible = True
        ElseIf encryptiontype = "6001" Then
            Call AddVC1Modes
        ElseIf forktype = "CJ" Then
            ChkShowSerial.Visible = False
            ChkEncryptAudio.Visible = True
        End If
        If encryptiontype = "6002" Then Call AddVC2Modes
        If encryptiontype = "6003" Then Call AddVCSModes
        If encryptiontype = "6004" Then Call AddSysterModes
        If encryptiontype = "6005" Then Call AddSysterModes
        If encryptiontype = "6007" Then Call AddMACModes
        If encryptiontype = "6008" Then Call AddMACModes
        If encryptiontype = "6009" Then Call AddSysterModes
        DualVCMode = False
    End If
End Sub

Private Sub AddVC1Modes()
    encryptiontype = "--videocrypt"
    SampleRate.Text = 14
    If forktype = "CJ" Then
        ChkShowSerial.Enabled = True
        ChkFindKey.Enabled = True
    End If
    With encryption_key
        .Clear
        .AddItem "Free access/soft scrambled (no card required)"
        .ItemData(.NewIndex) = "6101"
        If forktype = "CJ" Then
            .AddItem "Conditional access (Sky 12 card)"
            .ItemData(.NewIndex) = "6110"
            .AddItem "Conditional access (Sky 11 card)"
            .ItemData(.NewIndex) = "6105"
            .AddItem "Conditional access (Sky 10 card)"
            .ItemData(.NewIndex) = "6108"
            .AddItem "Pay-per-view mode (Sky 10 card)"
            .ItemData(.NewIndex) = "6109"
            .AddItem "Conditional access (Sky 09 card)"
            .ItemData(.NewIndex) = "6103"
            .AddItem "Conditional access (Sky 07 or 06 card)"
            .ItemData(.NewIndex) = "6104"
            .AddItem "Conditional access (Sky 03 or 04 card)"
            .ItemData(.NewIndex) = "6113"
            .AddItem "Conditional access (Old Adult Channel card)"
            .ItemData(.NewIndex) = "6106"
            .AddItem "Conditional access (Newer Adult Channel card)"
            .ItemData(.NewIndex) = "6111"
            .AddItem "Conditional access (xtea mode)"
            .ItemData(.NewIndex) = "6107"
            .AddItem "Pay-per-view mode (phone cards)"
            .ItemData(.NewIndex) = "6112"
        Else
            .AddItem "Conditional access (Sky 11 card)"
            .ItemData(.NewIndex) = "6102"
        End If
        .ListIndex = "0"
    End With
End Sub

Private Sub DisableDualVCMode()
' Disables and hides the VC1 and VC2 mode comboboxes and re-enables and shows the single mode combobox
    DualVCMode = False
    encryption_key.Visible = True
    encryption_key.Enabled = False
    encryption_key.ListIndex = "-1"
    vc1key.Enabled = False
    vc1key.Visible = False
    vc2key.Enabled = False
    vc2key.Visible = False
    encryptionkey = ""
End Sub

Private Sub AddVC2Modes()
    encryptiontype = "--videocrypt2"
    SampleRate.Text = 14
    ChkShowSerial.Enabled = False
    ChkShowSerial.Value = vbUnchecked
    With encryption_key
        .Clear
        .AddItem "Free access/soft scrambled (no card required)"
        .ItemData(.NewIndex) = "6201"
        If forktype = "CJ" Then
            .AddItem "Conditional access (Multichoice Europe card)"
            .ItemData(.NewIndex) = "6202"
        End If
        .ListIndex = 0
    End With
End Sub

Private Sub AddBothVCModes()
' Adding two encryption types simultaneously is a little awkward with the structure that we have
' We won't define the encryptiontype variable yet but will do so when we check the encryption key
    SampleRate.Text = 14
    ChkShowSerial.Enabled = False
    ChkShowSerial.Value = vbUnchecked
    With vc1key
        .Clear
        .AddItem "Free access"
        .ItemData(.NewIndex) = "6101"
        If forktype = "CJ" Then
            .Enabled = True
            .AddItem "Sky 12 card"
            .ItemData(.NewIndex) = "6110"
            .AddItem "Sky 11 card"
            .ItemData(.NewIndex) = "6105"
            .AddItem "Sky 10 card"
            .ItemData(.NewIndex) = "6108"
            .AddItem "Sky 10 PPV mode"
            .ItemData(.NewIndex) = "6109"
            .AddItem "Sky 09 card"
            .ItemData(.NewIndex) = "6103"
            .AddItem "Sky 07 or 06 card"
            .ItemData(.NewIndex) = "6104"
            .AddItem "Sky 03 card"
            .ItemData(.NewIndex) = "6113"
            .AddItem "Old Adult Channel card"
            .ItemData(.NewIndex) = "6106"
            .AddItem "Newer Adult Channel card"
            .ItemData(.NewIndex) = "6111"
            .AddItem "xtea mode"
            .ItemData(.NewIndex) = "6107"
            .AddItem "PPV mode"
            .ItemData(.NewIndex) = "6112"
        Else
            ' fsphil's version only supports Dual VC1/VC2 in free access mode
            ' So we so grey out the comboboxes as there's only one option anyway
            .Enabled = False
        End If
    End With
    With vc2key
        .Clear
        .AddItem "Free access"
        .ItemData(.NewIndex) = "6201"
        If forktype = "CJ" Then
            .Enabled = True
            .AddItem "Multichoice Europe card"
            .ItemData(.NewIndex) = "6202"
        Else
            .Enabled = False
        End If
    End With
    vc1key.ListIndex = "0"
End Sub

Private Sub AddVCSModes()
    SampleRate.Text = 14
    ChkShowSerial.Enabled = False
    ChkShowSerial.Value = vbUnchecked
    encryptiontype = "--videocrypts"
    With encryption_key
        .Clear
        .AddItem "Free access/soft scrambled (no card required)"
        .ItemData(.NewIndex) = "6401"
        .ListIndex = 0
        .AddItem "Conditional access (BBC Select card)"
        .ItemData(.NewIndex) = "6402"
        .ListIndex = 0
    End With
End Sub

Private Sub AddSysterModes()
' Syster, Discret and Smartcrypt all share the same access keys
' Therefore, we detect which system was selected
    ChkShowSerial.Enabled = False
    ChkShowSerial.Value = vbUnchecked
    If encryptiontype = "6004" Then encryptiontype = "--syster"
    If encryptiontype = "6005" Then encryptiontype = "--d11"
    If encryptiontype = "6009" Then encryptiontype = "--smartcrypt"
    If forktype = "CJ" Then
        With encryption_key
            .Clear
            .AddItem "Free access (Premiere Germany)"
            .ItemData(.NewIndex) = "6502"
            .AddItem "Conditional access (Premiere Germany)"
            .ItemData(.NewIndex) = "6503"
            .AddItem "Free access (Canal+ France)"
            .ItemData(.NewIndex) = "6505"
            .AddItem "Conditional access (Canal+ France)"
            .ItemData(.NewIndex) = "6506"
            .AddItem "Free access (Canal+ Poland)"
            .ItemData(.NewIndex) = "6504"
            .AddItem "Free access (Canal+ Spain)"
            .ItemData(.NewIndex) = "6507"
            .AddItem "Free access (HTB+ Russia)"
            .ItemData(.NewIndex) = "6508"
        End With
    Else
        With encryption_key
            .Clear
            .AddItem "Free access"
            .ItemData(.NewIndex) = "6501"
        End With
    End If
    encryption_key.ListIndex = "0"
End Sub

Private Sub AddMACModes()
    ChkShowSerial.Enabled = False
    ChkShowSerial.Value = vbUnchecked
    If encryptiontype = "6007" Then encryptiontype = "--single-cut"
    If encryptiontype = "6008" Then encryptiontype = "--double-cut"
    With encryption_key
        .Clear
        .AddItem "No conditional access (free)"
        .ItemData(.NewIndex) = "6701"
        .AddItem "EuroCrypt M (FilmNet card)"
        .ItemData(.NewIndex) = "6702"
        .AddItem "EuroCrypt M (TV1000 card)"
        .ItemData(.NewIndex) = "6703"
        .AddItem "EuroCrypt M (CTV card)"
        .ItemData(.NewIndex) = "6704"
        .AddItem "EuroCrypt M (TV Plus card)"
        .ItemData(.NewIndex) = "6705"
        .AddItem "EuroCrypt S2 (TVS Denmark card)"
        .ItemData(.NewIndex) = "6706"
        .AddItem "EuroCrypt S2 (RDV card)"
        .ItemData(.NewIndex) = "6707"
        .AddItem "EuroCrypt S2 (NRK card)"
        .ItemData(.NewIndex) = "6708"
        .AddItem "EuroCrypt S2 (CTV card)"
        .ItemData(.NewIndex) = "6709"
        .ListIndex = 0
    End With
End Sub

Private Sub CheckEncryptionKey()
    If encryption_key.Enabled = True Then
' Read the index value assigned to the encryptionkey combobox
' Use this value to set the correct command line parameters
        encryptionkey = encryption_key.ItemData(encryption_key.ListIndex)
' VideoCrypt I
        If encryptionkey = "6101" Then encryptionkey = "free"
        If encryptionkey = "6102" Then encryptionkey = "conditional"
        If encryptionkey = "6103" Then encryptionkey = "sky09"
        If encryptionkey = "6104" Then encryptionkey = "sky07"
        If encryptionkey = "6105" Then encryptionkey = "sky11"
        If encryptionkey = "6106" Then encryptionkey = "tac1"
        If encryptionkey = "6107" Then encryptionkey = "xtea"
        If encryptionkey = "6108" Then encryptionkey = "sky10"
        If encryptionkey = "6109" Then encryptionkey = "sky10ppv"
        If encryptionkey = "6110" Then encryptionkey = "sky12"
        If encryptionkey = "6111" Then encryptionkey = "tac2"
        If encryptionkey = "6112" Then encryptionkey = "ppv"
        If encryptionkey = "6113" Then encryptionkey = "sky03"
' VideoCrypt II
        If encryptionkey = "6201" Then encryptionkey = "free"
        If encryptionkey = "6202" Then encryptionkey = "conditional"
' VideoCrypt S
        If encryptionkey = "6401" Then encryptionkey = "free"
        If encryptionkey = "6402" Then encryptionkey = "conditional"
' Syster/D11/Smartcrypt
        If encryptionkey = "6501" Then encryptionkey = ""
        If encryptionkey = "6502" Then encryptionkey = "premiere-fa"
        If encryptionkey = "6503" Then encryptionkey = "premiere-ca"
        If encryptionkey = "6504" Then encryptionkey = "cplfa"
        If encryptionkey = "6505" Then encryptionkey = "cfrfa"
        If encryptionkey = "6506" Then encryptionkey = "cfrca"
        If encryptionkey = "6507" Then encryptionkey = "cesfa"
        If encryptionkey = "6508" Then encryptionkey = "ntvfa"
' EuroCrypt
        If encryptionkey = "6701" Then encryptionkey = ""
        If encryptionkey = "6702" Then encryptionkey = "--eurocrypt filmnet"
        If encryptionkey = "6703" Then encryptionkey = "--eurocrypt tv1000"
        If encryptionkey = "6704" Then encryptionkey = "--eurocrypt ctv"
        If encryptionkey = "6705" Then encryptionkey = "--eurocrypt tvplus"
        If encryptionkey = "6706" Then encryptionkey = "--eurocrypt tvs"
        If encryptionkey = "6707" Then encryptionkey = "--eurocrypt rdv"
        If encryptionkey = "6708" Then encryptionkey = "--eurocrypt nrk"
        If encryptionkey = "6709" Then encryptionkey = "--eurocrypt ctvs"
        If encryptionkey = "ppv" Then
            ChkEncryptAudio.Visible = False
            ChkEnableEMM.Visible = False
            ChkDisableEMM.Visible = False
            CardNumber.Visible = False
            ChkFindKey.Visible = True
            ChkFindKey.Enabled = True
        Else
            ChkFindKey.Value = vbUnchecked
            ChkFindKey.Enabled = False
            ChkFindKey.Visible = False
            ChkEnableEMM.Visible = True
            ChkDisableEMM.Visible = True
            CardNumber.Visible = True
        End If
' ACP is not supported when encryption is enabled, so disable the option
        ChkACP.Enabled = False
        ChkACP.Value = vbUnchecked
' Enable EMM options if sky07, sky09 or VC2 conditional is selected
        If encryptionkey = "sky07" Or encryptionkey = "sky09" Then
            Call EnableEMMOptions
        ElseIf encryptiontype = "--videocrypt2" And encryptionkey = "conditional" Then
            Call EnableEMMOptions
        Else
            Call DisableEMMOptions
        End If
' Enable audio encryption for Syster, Discret or MAC modes, disable for anything else
        If encryptiontype = "--syster" Or encryptiontype = "--d11" Or encryptiontype = "--smartcrypt" _
        Or encryptiontype = "--single-cut" Or encryptiontype = "--double-cut" Then
            Call EnableAudioEncryption
        Else
            Call DisableAudioEncryption
        End If
' Enable ShowECM option if Captain Jack fork enabled
        If forktype = "CJ" Then
            Call EnableShowECM
        Else
            Call DisableShowECM
        End If
    Else
' Blank the encryptiontype and encryptionkey variables to disable encryption
        encryptiontype = ""
        encryptionkey = ""
' Re-enable ACP option
        ChkACP.Enabled = True
' Disable EMM options
        Call DisableEMMOptions
' Disable audio encryption
        Call DisableAudioEncryption
' Disable Show ECM checkbox
        Call DisableShowECM
    End If
End Sub

Private Sub CheckDualEncryptionKey()
' Special sub for dual VC1/VC2 mode
' This needs special handling because hacktv expects --videocrypt <mode> --videocrypt2 <mode>
' As a workaround, we set the encryption type to the VC1 mode and the encryption key to the VC2 mode
    If DualVCMode = True Then
' VideoCrypt I
        encryptiontype = vc1key.ItemData(vc1key.ListIndex)
        If encryptiontype = "6101" Then encryptiontype = "--videocrypt free"
        If encryptiontype = "6102" Then encryptiontype = "--videocrypt conditional"
        If encryptiontype = "6103" Then encryptiontype = "--videocrypt sky09"
        If encryptiontype = "6104" Then encryptiontype = "--videocrypt sky07"
        If encryptiontype = "6105" Then encryptiontype = "--videocrypt sky11"
        If encryptiontype = "6106" Then encryptiontype = "--videocrypt tac1"
        If encryptiontype = "6107" Then encryptiontype = "--videocrypt xtea"
        If encryptiontype = "6108" Then encryptiontype = "--videocrypt sky10"
        If encryptiontype = "6109" Then encryptiontype = "--videocrypt sky10ppv"
        If encryptiontype = "6110" Then encryptiontype = "--videocrypt sky12"
        If encryptiontype = "6111" Then encryptiontype = "--videocrypt tac2"
        If encryptiontype = "6112" Then encryptiontype = "--videocrypt ppv"
        If encryptiontype = "6113" Then encryptiontype = "--videocrypt sky03"
' VideoCrypt II
        encryptionkey = vc2key.ItemData(vc2key.ListIndex)
        If encryptionkey = "6201" Then encryptionkey = "--videocrypt2 free"
        If encryptionkey = "6202" Then encryptionkey = "--videocrypt2 conditional"
' ACP is not supported when encryption is enabled, so disable the option
        ChkACP.Enabled = False
        ChkACP.Value = vbUnchecked
' Enable ShowECM option if Captain Jack fork enabled
        If forktype = "CJ" Then
            Call EnableShowECM
        Else
            Call DisableShowECM
        End If
' Disable audio encryption
        If ChkEncryptAudio.Enabled = True Then Call DisableAudioEncryption
' Disable EMM support
        If ChkEnableEMM.Enabled = True Then Call DisableEMMOptions
    ElseIf DualVCMode = False Then
        Call CheckEncryptionKey
        Exit Sub
    End If
End Sub

Private Sub EnableEMMOptions()
    ChkEnableEMM.Enabled = True
    ChkDisableEMM.Enabled = True
End Sub

Private Sub DisableEMMOptions()
    ChkEnableEMM.Enabled = False
    ChkDisableEMM.Enabled = False
    ChkEnableEMM.Value = vbUnchecked
    ChkDisableEMM.Value = vbUnchecked
End Sub

Private Sub EnableShowECM()
    ChkShowECM.Enabled = True
End Sub

Private Sub DisableShowECM()
    ChkShowECM.Value = vbUnchecked
    ChkShowECM.Enabled = False
End Sub

Private Sub EnableAudioEncryption()
    If ChkAudio.Value = vbChecked Then ChkEncryptAudio.Enabled = True
End Sub

Private Sub DisableAudioEncryption()
    ChkEncryptAudio.Value = vbUnchecked
    ChkEncryptAudio.Enabled = False
End Sub

Private Sub DisableUHF()
    UHF.Enabled = False
End Sub

Private Sub DisableVHF()
    VHF.Enabled = False
End Sub

Private Sub EnableUHF()
    UHF.Enabled = True
End Sub

Private Sub EnableVHF()
    VHF.Enabled = True
End Sub

Private Sub EnableRFAmp()
    rfAmpFlag = "-a"
End Sub

Private Sub DisableRFAmp()
    rfAmpFlag = ""
End Sub

Private Sub EnableFMDeviation()
' Enable the FM deviation checkbox
    ChkFMDev.Enabled = True
' The --filter parameter enables VSB filtering on AM, or CCIR-405 FM pre-emphasis
' filtering on FM, so change the Filter checkbox description to suit
    ChkVideoFilter.Caption = "FM video pre-emphasis filter"
End Sub

Private Sub DisableFMDeviation()
' Disable the FM deviation checkbox
    ChkFMDev.Enabled = False
    ChkFMDev.Value = vbUnchecked
    fm_deviation.Text = ""
    fm_deviation.Enabled = False
    fm_deviation.BackColor = vbButtonFace
    If ChkVideoFilter.Value = vbChecked Then
        ChkVideoFilter.Value = vbUnchecked
        SampleRate.Text = DefaultSampleRate
    End If
' Revert Filter checkbox name to VSB-AM
    ChkVideoFilter.Caption = "VSB-AM filter"
End Sub

Private Function CheckInputSource() As Boolean
    CheckInputSource = True
    If input_source.Enabled = True Then
' Check if input source is an M3U file
        If M3USource.Visible = True Then
            Dim arrayindex As Integer
            arrayindex = M3USource.ItemData(M3USource.ListIndex)
            inputsource = URL(arrayindex)
' Check if input source is selected but no file specified
        ElseIf input_source.Text = "" Then
            CheckInputSource = False
            MsgBox "Please specify an input file to broadcast or choose the test card option.", vbExclamation, App.Title
' Check for YouTube URLs
        ElseIf InStr(input_source.Text, "://youtube.com/") > 0 Or InStr(input_source.Text, "://www.youtube.com/") > 0 Or InStr(input_source.Text, "://youtu.be/") > 0 Then
            MsgBox "YouTube URLs are not supported. You should first download the video using youtube-dl or an equivalent tool.", vbExclamation, App.Title
            CheckInputSource = False
        Else
' Don't add quotes to internet URLs, but do for local sources
            If InStr(input_source.Text, Chr(58) & Chr(47) & Chr(47)) = 0 Then
' Add quotes to the input source if it doesn't already contain them
                If InStr(input_source.Text, Chr(34)) = 0 Then
                    inputsource = Chr(34) & input_source.Text & Chr(34)
                End If
            Else
                inputsource = input_source.Text
            End If
        End If
    End If
End Function

Private Sub CheckTeletextSource()
    Dim strtti As String
' Checks if a source file or folder has been specified.
' If not (and the teletext box is checked), then the demo page is saved to the temp folder and set as the source
' The demo page is saved as a resource at TELETEXT\1
    If teletext_source.Enabled = True Then
        If teletext_source = "" Then
            strtti = StrConv(LoadResData(1, "TELETEXT"), vbUnicode)
            demotext = Environ$("temp") & Chr(92) & "demo.tti"
            Dim iFileNo As Integer
            iFileNo = FreeFile
' Open the file for writing
            Open demotext For Output As #iFileNo
' Write the resource data to the file
            Print #iFileNo, strtti
' Close the file for writing
            Close #iFileNo
' Set the teletext_source textbox and teletextsource variable (in quotation marks) to the demo page path
            teletext_source.Text = demotext
            teletextsource = Chr(34) & demotext & Chr(34)
        Else
' Add quotes to the teletext path if it doesn't already contain them
            If InStr(teletext_source.Text, Chr(34)) = 0 Then
                teletextsource = Chr(34) & teletext_source.Text & Chr(34)
            End If
' Is the file a .t42 teletext archive? If so, add a "raw:" prefix to it.
' But don't apply the prefix for directories.
            If LCase(Right$(teletext_source.Text, 4)) = ".t42" Then
                On Error Resume Next
                If Not (GetAttr(teletext_source.Text) And vbDirectory) <> 0 Then teletextsource = "raw:" & teletextsource
            End If
        End If
    End If
End Sub

Private Function CheckTXGain() As Boolean
    CheckTXGain = True
    If Not IsNumeric(txgain.Text) Then CheckTXGain = False
    If Not CheckTXGain = False Then If txgain.Text = "" Then CheckTXGain = False
    If Not CheckTXGain = False Then If txgain.Text > 47 Then CheckTXGain = False
    If CheckTXGain = False Then
        MsgBox "Gain should be between 0 and 47 dB.", vbExclamation, App.Title
    End If
End Function

Private Function CheckOutputLevel() As Boolean
    CheckOutputLevel = True
    If ChkOutputLevel.Value = vbChecked Then
        If Not IsNumeric(outputlevelvalue.Text) Then CheckOutputLevel = False
        ' Only process the next two lines if the previous check passed. Otherwise we could crash.
        If CheckOutputLevel = True Then If outputlevelvalue.Text > 1 Then CheckOutputLevel = False
        If CheckOutputLevel = True Then If outputlevelvalue.Text < 0 Then CheckOutputLevel = False
        ' If any of the above lines returned false, generate an error
        If CheckOutputLevel = False Then
            MsgBox "Output level should be between 0.0 and 1.0.", vbExclamation, App.Title
        Else
        ' If the first character is a decimal point, add a leading zero
            If Left$(outputlevelvalue.Text, 1) = Chr(46) Then outputlevelvalue.Text = "0" & outputlevelvalue.Text
        End If
    End If
End Function

Private Function CheckGamma() As Boolean
    CheckGamma = True
    If ChkGamma.Value = vbChecked Then
        If Not IsNumeric(gammavalue.Text) Then CheckGamma = False
        ' Only process the next two lines if the previous check passed. Otherwise we could crash.
        If CheckGamma = True Then If gammavalue.Text > 1 Then CheckGamma = False
        If CheckGamma = True Then If gammavalue.Text < 0 Then CheckGamma = False
        ' If any of the above lines returned false, generate an error
        If CheckGamma = False Then
            MsgBox "Gamma should be between 0.0 and 1.0.", vbExclamation, App.Title
        Else
        ' If the first character is a decimal point, add a leading zero
            If Left$(gammavalue.Text, 1) = Chr(46) Then gammavalue.Text = "0" & gammavalue.Text
        End If
    End If
End Function

Private Function CheckCustomFrequency() As Boolean
    CheckCustomFrequency = True
' This sub is only required for custom frequencies, so we skip it if the custom radio button is not selected
    If CustomFreq.Value = True Then
' Fail if the frequency_mhz textbox is blank or not numeric
        If Not IsNumeric(frequency_mhz.Text) Then
            CheckCustomFrequency = False
        Else
' Fail if the frequency_mhz textbox is not between 1 and 7250
            If frequency_mhz.Text < 1 Then CheckCustomFrequency = False
            If frequency_mhz.Text > 7250 Then CheckCustomFrequency = False
        End If
        If CheckCustomFrequency = False Then
            MsgBox "Please specify a frequency between 1 MHz and 7250 MHz.", vbExclamation, App.Title
        Else
' As the frequency passed to hacktv is expected to be in Hz, we need to convert MHz to Hz
' Multiply the contents of the frequency_mhz textbox by 1,000,000 and populate the freq variable with the result
            freq = frequency_mhz.Text * 1000000
        End If
    End If
End Function

Private Function CheckSampleRate() As Boolean
    CheckSampleRate = True
' Clear the sr variable
    sr = ""
' If the SampleRate textbox is blank or not numeric, then generate an error and fail
    If Not IsNumeric(SampleRate.Text) Then
        MsgBox "Please specify a valid sample rate in MHz.", vbExclamation, App.Title
        CheckSampleRate = False
    Else
' As with the frequency, hacktv expects to receive the sample rate in Hz
' Multiply the contents of the SampleRate textbox by 1,000,000 and populate the sr variable with the result
        sr = SampleRate.Text * 1000000
    End If
End Function

Private Function CheckFMDeviation() As Boolean
    CheckFMDeviation = True
    If ChkFMDev.Value = vbChecked Then
        If Not IsNumeric(fm_deviation) Then
            MsgBox "Please specify a valid deviation in MHz.", vbExclamation, App.Title
            CheckFMDeviation = False
        Else
' As with the frequency and sample rate, hacktv expects FM deviation in Hz
' Multiply the contents of the fm_deviation textbox by 1,000,000 and populate the fmdevvalue variable with the result
            fmdevvalue = fm_deviation.Text * 1000000
        End If
    End If
End Function

Private Function CheckCardNumber() As Boolean
    Dim EnteredCheckDigit As Integer
    Dim CalculatedCheckDigit As Integer
    Dim IncorrectInput As String
    Dim LuhnCheckFailed As String
    IncorrectInput = "Card number should be exactly 9 or 13 digits." & vbCrLf & "See the help file for more information."
    LuhnCheckFailed = "The specified card number appears to be incorrect (Luhn check failed)."
' Issue 07 viewing cards use either a 13-digit or 9-digit card number. All subsequent issues use the 9-digit format.
' Here, we will check the supplied card number and extract the eight digits that we need.
' For the 9-digit format, we truncate the last digit. For the 13-digit format, we need digits 5-12.
    If CardNumber.Enabled = True Then
' If the data in the CardNumber textbox is not numeric, generate an error and exit
        If Not IsNumeric(CardNumber.Text) Then
            MsgBox IncorrectInput, vbExclamation, App.Title
            TruncatedCardNumber = ""
            CheckCardNumber = False
            Exit Function
        End If
        If Len(CardNumber.Text) = 9 Then
' Get the last digit (the check digit) from the supplied card number
            EnteredCheckDigit = Right$(CardNumber.Text, 1)
' Truncate the check digit from the supplied card number
            TruncatedCardNumber = Left$(CardNumber.Text, Len(CardNumber.Text) - 1)
' Calculate what the check digit should be, using the Luhn algorithm
            CalculatedCheckDigit = LuhnCheck(TruncatedCardNumber)
' Check if both digits match
            If Not EnteredCheckDigit = CalculatedCheckDigit Then
                MsgBox LuhnCheckFailed, vbExclamation, App.Title
                CheckCardNumber = False
                Exit Function
            Else
' No need to do anything else here, set to True and exit
                CheckCardNumber = True
                Exit Function
            End If
        End If
        If Len(CardNumber.Text) = 13 Then
' 13-digit card numbers have the issue number as the first two digits
' Here, we check for a 07 card
            If Not Left$(CardNumber.Text, 2) = "07" Then
            MsgBox "Incorrect card number specified, must start with 07.", vbExclamation, App.Title
            CheckCardNumber = False
            Exit Function
        End If
' Get the last digit (the check digit) from the supplied card number
            EnteredCheckDigit = Right$(CardNumber.Text, 1)
' Strip out the first four digits and the check digit
            TruncatedCardNumber = Mid$(CardNumber.Text, 5, 8)
' Calculate what the check digit should be, using the Luhn algorithm
            CalculatedCheckDigit = LuhnCheck(TruncatedCardNumber)
' Check if both digits match
            If Not EnteredCheckDigit = CalculatedCheckDigit Then
                MsgBox LuhnCheckFailed, vbExclamation, App.Title
                CheckCardNumber = False
                Exit Function
            Else
' No need to do anything else here, set to True and exit
                CheckCardNumber = True
                Exit Function
            End If
        Else
' If the entered card serial is not 9 or 13 digits, generate an error message and exit
            MsgBox IncorrectInput, vbExclamation, App.Title
            TruncatedCardNumber = ""
            CheckCardNumber = False
        End If
    Else
' If the CardNumber textbox is disabled, return true and exit
        TruncatedCardNumber = ""
        CheckCardNumber = True
    End If
End Function

Private Sub CheckAspectRatioScaling()
    Dim ARIndex As Integer
    If ChkARCorrection.Value = vbChecked Then
        ARIndex = ARCorrectionMode.ItemData(ARCorrectionMode.ListIndex)
        If ARIndex = 2101 Then ScalingMode = ""
        If ARIndex = 2102 Then ScalingMode = "--letterbox"
        If ARIndex = 2103 Then ScalingMode = "--pillarbox"
    Else
        ScalingMode = ""
    End If
End Sub

Private Sub CheckMACChannelID()
' Handling for BSB channels
' If one of the BSB IFs is selected in D-MAC FM mode, send the appropriate channel ID to the receiver so it
' will display the correct channel name.
' In version 3.1, we hacked this together using five separate compiled EXEs, but hacktv now supports it
' natively using the --chid parameter.
' First, we'll remove the beta reg value from version 3.1 if it exists because we no longer use it
    If Not GetSetting(App.EXEName, "Settings", "SeparateEXEsForBSB") = "" Then DeleteSetting App.EXEName, "Settings", "SeparateEXEsForBSB"
' Second, check if D-MAC FM mode is enabled, and if one of the BSB channels has been selected
    If sys = "dmac-fm" Then
        If frequency_ch.Text = "4 (Now)" Then
            chid = "--chid 0x00B1"
        ElseIf frequency_ch.Text = "8 (Galaxy)" Then
            chid = "--chid 0x00B2"
        ElseIf frequency_ch.Text = "12 (Sports Ch)" Then
            chid = "--chid 0x00B3"
        ElseIf frequency_ch.Text = "16 (Power St)" Then
            chid = "--chid 0x00B4"
        ElseIf frequency_ch.Text = "20 (Movie Ch)" Then
            chid = "--chid 0x00B5"
        Else
            chid = ""
        End If
    Else
        chid = ""
    End If
End Sub

Private Sub BtnRun_Click()
    If GenerateOnly.Checked = False Then
' If running on Wine, don't check for the presence of hacktv.exe as we don't need it.
' Instead, call a different sub to check if it is installed in the Unix file system.
        If RunningOnWine = True Then
            If CheckWinePreReqs = False Then
                Exit Sub
            Else
                Call RunTV
                Exit Sub
            End If
        End If
' Check to see if hacktv.exe exists. If not, then stop processing and return an error.
' If hacktv.exe does exist, then move on to the next step
        If DoesFileExist(HackTVPath & Chr(92) & "hacktv.exe") = True Then
            Call RunTV
        Else
            MsgBox "Unable to find hacktv.exe. Please check the Settings menu to verify its location.", vbCritical, App.Title
        End If
    Else
' Bypass file check as we're not running hacktv
        Call RunTV
    End If
End Sub

Private Function CheckWinePreReqs() As Boolean
    CheckWinePreReqs = True
    Dim missinghacktv As Integer
    Dim missinggnome As Integer
' Retrieve registry value which contains the gnome-terminal path
    TerminalPath = GetSetting(App.Title, "Settings", "TerminalPath", "")
' If the value doesn't exist, set the default location to this directory
    If TerminalPath = "" Then
        TerminalPath = DefaultTerminalPath
    End If
' Check for the presence of hacktv
    If DoesFileExist(HackTVPath & Chr(92) & "hacktv") = False Then missinghacktv = 1
' Check for the presence of gnome-terminal
    If DoesFileExist(TerminalPath & Chr(92) & "gnome-terminal") = False Then missinggnome = 1
    If missinghacktv = 1 Then
        MsgBox "Unable to find hacktv. Please check the Settings menu to verify its location.", vbCritical, App.Title
        CheckWinePreReqs = False
        Exit Function
    End If
    If CloseOnExit.Checked = False Then
        If missinggnome = 1 Then
            MsgBox "Unable to find gnome-terminal. Please check the Settings menu to verify its location.", vbCritical, App.Title
            CheckWinePreReqs = False
            Exit Function
        End If
    End If
End Function

Private Sub RunTV()
' Declare variables
    Dim frequencyargument As String
    Dim videomodeargument As String
    Dim txgainargument As String
    Dim samplerateargument As String
    Dim SpacesRemoved As String
    Dim runpath As String
    Dim args As String
 
' Call each function/sub and check for errors. If no error, then move on.
    If CheckInputSource = False Then Exit Sub
    Call CheckTeletextSource
    If CheckTXGain = False Then Exit Sub
    If CheckCustomFrequency = False Then Exit Sub
    If CheckSampleRate = False Then Exit Sub
    If CheckFMDeviation = False Then Exit Sub
    If CheckGamma = False Then Exit Sub
    If CheckOutputLevel = False Then Exit Sub
    If CheckCardNumber = False Then Exit Sub
    Call CheckAspectRatioScaling
    Call CheckMACChannelID
        
' Combine all parameters into one text field
' This doesn't include the filename as we will be trimming later
    allargs.Text = "-m" & Chr(32) & sys & Chr(32) & "-f" & Chr(32) & freq & Chr(32) & "-s" & Chr(32) _
    & sr & Chr(32) & "-g" & Chr(32) & txgain.Text & Chr(32) & chid & Chr(32) & SubtitlesParam & Chr(32) _
    & TxtSubtitleIndex.Text & Chr(32) & nicamstatus & Chr(32) & audiostatus & Chr(32) & acpstatus _
    & Chr(32) & repeatstatus & Chr(32) & wssstatus & Chr(32) & encryptiontype & Chr(32) & encryptionkey _
    & Chr(32) & audioencryption & Chr(32) & TeletextFlag & Chr(32) & teletextsource & Chr(32) & rfAmpFlag _
    & Chr(32) & fmdevargument & Chr(32) & fmdevvalue & Chr(32) & gammaParam & Chr(32) & gammavalue.Text _
    & Chr(32) & outputlevelparam & Chr(32) & outputlevelvalue.Text & Chr(32) & filterparam & Chr(32) _
    & positionparam & Chr(32) & positionValue.Text & Chr(32) & timestampParam & Chr(32) _
    & logoParam & Chr(32) & logoPath & Chr(32) & verboseParam & Chr(32) & EMMParam & Chr(32) _
    & TruncatedCardNumber & Chr(32) & ShowECMParam & Chr(32) & ScalingMode & Chr(32) & Interlaced _
    & Chr(32) & ShowCardSerial & Chr(32) & FindKey & Chr(32) & VITS
' Tidy up parameters by removing additional spaces if required
    Do While InStr(1, allargs.Text, "  ")
        allargs.Text = Replace$(allargs.Text, "  ", " ")
    Loop
    SpacesRemoved = Trim(allargs.Text)
' Add the source path to the arguments field
    allargs.Text = SpacesRemoved & Chr(32) & inputsource
' If running on Wine, call its sub
    If RunningOnWine = True Then
        Call TranslatePathsForWine
    Else
' If the Generate only option is enabled, stop here
        If GenerateOnly.Checked = True Then Exit Sub
' Check if the option to restrict CPU C-states is enabled
' If it is, then restrict the C-state to C1 before we start
        If ChkRestrictCPUIdleStates.Checked = True Then Call RestrictCPUIdleStates(True)
' If running on Windows, check if closeonexit is enabled
' If so, change the parameters to run cmd.exe /k first. If disabled, then run hacktv.exe directly.
        If CloseOnExit.Checked = False Then
            runpath = HackTVPath & Chr(92) & HackTVEXEName
            args = allargs.Text
        Else
            runpath = "cmd.exe"
            args = " /k call " & Chr(34) & HackTVPath & Chr(92) & HackTVEXEName & Chr(34) & Chr(32) & allargs.Text
        End If
        If IsFileInUse(HackTVPath & Chr(92) & HackTVEXEName) = True Then
            MsgBox HackTVEXEName & " cannot be opened. It may be locked by another process or you may not have permission to access it.", vbCritical, App.Title
        Exit Sub
        End If
' Use ShellExecute to run the parameters shown above. We're done.
        ShellExecute Me.hWnd, vbNullString, runpath, args, HackTVPath, SW_SHOWNORMAL
    End If
End Sub

Private Sub TranslatePathsForWine()
    Dim nodriveletter As String
    Dim wrongdrive As Integer
    wrongdrive = 0
    Dim ascii As Integer
    Dim gnomeparams As String
    Dim wineparams As String
' Convert TerminalPath variable to use Unix-style naming
    TerminalPath = Replace$(TerminalPath, "Z:\", "/")
    TerminalPath = Replace$(TerminalPath, "\", "/")
    If CloseOnExit.Checked = False Then
        gnomeparams = "/c start /d " & Chr(34) & App.Path & Chr(34) & " /unix " & TerminalPath & "/gnome-terminal -- "
    Else
        gnomeparams = "/c start /d " & Chr(34) & App.Path & Chr(34) & " /unix "
    End If
    wineparams = HackTVPath & "/hacktv "
' Convert wineparams variable to use Unix-style naming
    wineparams = Replace$(wineparams, "Z:\", "/")
    wineparams = Replace$(wineparams, "\", "/")
    
    If InStr(1, allargs.Text, "A:\") > 0 Then wrongdrive = 1
    If InStr(1, allargs.Text, "B:\") > 0 Then wrongdrive = 1
    For ascii = 68 To 89 ' Drives D to Y
        If InStr(1, allargs.Text, (Chr(ascii)) & ":\") > 0 Then wrongdrive = 1
        If wrongdrive = 1 Then Exit For
    Next
      
    If wrongdrive = 1 Then
        MsgBox "Input is only accepted from the virtual C: or Z: drives."
    Else
' Convert allargs.Text contents to use Unix-style naming
        nodriveletter = Replace$(allargs.Text, "Z:\", "/")
        nodriveletter = Replace$(nodriveletter, "C:\", Mid$(Environ$("WINECONFIGDIR"), 7) & "/drive_c/")
        allargs.Text = Replace$(nodriveletter, "\", "/")
' If the generate only option is enabled, stop here
    If GenerateOnly.Checked = True Then Exit Sub
' Check if the file is locked
        If IsFileInUse(HackTVPath & Chr(92) & "hacktv") = True Then
            MsgBox "hacktv cannot be opened. It may be locked by another process or you may not have permission to access it.", vbCritical, App.Title
            Exit Sub
        End If
        ShellExecute Me.hWnd, vbNullString, "cmd.exe", gnomeparams & wineparams & allargs.Text, HackTVPath, SW_SHOWNORMAL
    End If
End Sub

Private Sub Form_QueryUnload(Cancel As Integer, UnloadMode As Integer)
    If AppBusy = True Then
        Dim AbortM3UOpen As VbMsgBoxResult
        AbortM3UOpen = MsgBox("A playlist file is currently being processed. Do you wish to abort?", vbExclamation + vbYesNo, App.Title)
        If AbortM3UOpen = vbNo Then
            Cancel = True
        Else
            StopProcessing = True
            Cancel = True
        End If
    End If
End Sub

Private Sub Form_Unload(Cancel As Integer)
    Call CleanupBeforeExit
End Sub

Private Sub CleanupBeforeExit()
' If the option to restrict CPU C-states is enabled, revert it to defaults
    If CStateSettingChanged = True Then Call RestrictCPUIdleStates(False)
' Clean up temp files if we made any
    Dim TeefaxHTML As String
    Dim SparkHTML As String
    Dim SparkPath As String
    TeefaxHTML = Environ$("temp") & Chr(92) & "teefax.html"
    SparkHTML = Environ$("temp") & Chr(92) & "spark.html"
    demotext = Environ$("temp") & Chr(92) & "demo.tti"
    TeefaxPath = Environ$("temp") & Chr(92) & "teefax"
    SparkPath = Environ$("temp") & Chr(92) & "spark"

    If DoesFileExist(SparkHTML) = True Then Kill (SparkHTML)
    If DoesFileExist(TeefaxHTML) = True Then Kill (TeefaxHTML)
    If FolderExists(TeefaxPath) = True Then
        If PathIsDirectoryEmpty(TeefaxPath) = "1" Then
            RmDir TeefaxPath
        Else
            Kill (TeefaxPath & Chr(92) & "*.*")
            RmDir TeefaxPath
        End If
    End If
    If FolderExists(SparkPath) = True Then
        If PathIsDirectoryEmpty(SparkPath) = "1" Then
            RmDir SparkPath
        Else
            Kill (SparkPath & Chr(92) & "*.*")
            RmDir SparkPath
        End If
    End If
End Sub


