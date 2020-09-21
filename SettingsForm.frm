VERSION 5.00
Begin VB.Form SettingsForm 
   BorderStyle     =   1  'Fixed Single
   Caption         =   "Path settings"
   ClientHeight    =   3045
   ClientLeft      =   45
   ClientTop       =   435
   ClientWidth     =   5655
   BeginProperty Font 
      Name            =   "Tahoma"
      Size            =   8.25
      Charset         =   0
      Weight          =   400
      Underline       =   0   'False
      Italic          =   0   'False
      Strikethrough   =   0   'False
   EndProperty
   LinkTopic       =   "Form1"
   MaxButton       =   0   'False
   MinButton       =   0   'False
   ScaleHeight     =   3045
   ScaleWidth      =   5655
   StartUpPosition =   3  'Windows Default
   Begin VB.Frame Frame1 
      Caption         =   "Fork (auto-detected)"
      Height          =   615
      Left            =   120
      TabIndex        =   0
      Top             =   120
      Width           =   5415
      Begin VB.Label DetectedFork 
         Caption         =   "DetectedFork"
         Height          =   195
         Left            =   120
         TabIndex        =   12
         Top             =   240
         Width           =   2535
      End
   End
   Begin VB.CommandButton SettingsReset 
      Caption         =   "Reset path"
      Height          =   375
      Left            =   3840
      TabIndex        =   9
      Top             =   2520
      Width           =   1575
   End
   Begin VB.CommandButton SettingsOK 
      Caption         =   "OK"
      Height          =   375
      Left            =   240
      TabIndex        =   7
      Top             =   2520
      Width           =   1575
   End
   Begin VB.CommandButton SettingsCancel 
      Caption         =   "Cancel"
      Height          =   375
      Left            =   2040
      TabIndex        =   8
      Top             =   2520
      Width           =   1575
   End
   Begin VB.Frame TerminalFrame 
      Caption         =   "Path to gnome-terminal (Wine only)"
      Enabled         =   0   'False
      Height          =   735
      Left            =   120
      TabIndex        =   4
      Top             =   1680
      Visible         =   0   'False
      Width           =   5415
      Begin VB.PictureBox Picture4 
         BorderStyle     =   0  'None
         Height          =   375
         Left            =   3720
         ScaleHeight     =   375
         ScaleWidth      =   1575
         TabIndex        =   11
         Top             =   240
         Width           =   1575
         Begin VB.CommandButton TerminalBrowse 
            Caption         =   "Browse..."
            Height          =   375
            Left            =   0
            TabIndex        =   6
            Top             =   0
            Width           =   1575
         End
      End
      Begin VB.TextBox TerminalText 
         Enabled         =   0   'False
         Height          =   315
         Left            =   120
         TabIndex        =   5
         Top             =   240
         Width           =   3495
      End
   End
   Begin VB.Frame HackTVPathFrame 
      Caption         =   "Path to hacktv"
      Height          =   735
      Left            =   120
      TabIndex        =   1
      Top             =   840
      Width           =   5415
      Begin VB.PictureBox Picture1 
         BorderStyle     =   0  'None
         Height          =   375
         Left            =   3720
         ScaleHeight     =   375
         ScaleWidth      =   1575
         TabIndex        =   10
         Top             =   240
         Width           =   1575
         Begin VB.CommandButton HackTVBrowse 
            Caption         =   "Browse..."
            Height          =   375
            Left            =   0
            TabIndex        =   3
            Top             =   0
            Width           =   1575
         End
      End
      Begin VB.TextBox HackTVPathText 
         Height          =   315
         Left            =   120
         TabIndex        =   2
         Top             =   240
         Width           =   3495
      End
   End
   Begin VB.Label FillerLabel 
      Caption         =   "Specify the location of hacktv.exe here."
      Height          =   255
      Left            =   1200
      TabIndex        =   13
      Top             =   1920
      Visible         =   0   'False
      Width           =   3135
   End
End
Attribute VB_Name = "SettingsForm"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Dim regvalue As String
Dim TempHackTVPath As String
Dim PathChanged As Boolean
Dim BadPath As Boolean

Private Sub Form_Load()
' Set the form's icon to the same icon used in the main form
    Me.Icon = MainForm.Icon
    PathChanged = False
    MainForm.HackTVPath = GetSetting(App.EXEName, "Settings", "HackTVPath", "")
    If MainForm.HackTVPath = "" Then
        HackTVPathText.Text = MainForm.DefaultHackTVPath
    Else
        HackTVPathText.Text = MainForm.HackTVPath
    End If
' If running on Wine, show the hidden gnome-terminal path setting
    If RunningOnWine = True Then
        Call EnableTerminalPath
    End If
' Enables a hidden descriptor label if running on Windows, to fill up the space vacated by the gnome-terminal path
    If RunningOnWine = False Then
        FillerLabel.Visible = True
    End If
    If MainForm.forktype = "CJ" Then
        DetectedFork.Caption = "Captain Jack"
    Else
        DetectedFork.Caption = "fsphil"
    End If
    If DoesFileExist(HackTVPathText.Text & Chr(92) & MainForm.HackTVEXEName) = False Then DetectedFork.Caption = "hacktv not found"
End Sub

Private Sub EnableTerminalPath()
' Disable advanced options frame and enable terminal path frame
    SettingsReset.Caption = "Reset paths"
    TerminalFrame.Visible = True
    TerminalFrame.Enabled = True
    TerminalText.Enabled = True
    TerminalText.BackColor = vbWindowBackground
    MainForm.TerminalPath = GetSetting(App.EXEName, "Settings", "TerminalPath", "")
    If MainForm.TerminalPath = "" Then
        TerminalText.Text = MainForm.DefaultTerminalPath
    Else
        TerminalText.Text = MainForm.TerminalPath
    End If
End Sub

Private Sub HackTVBrowse_Click()
' Spawn an open folder dialog box
    Dim sDir As String
    sDir = BrowseForFolder(Me, "Please specify a directory that contains " & MainForm.HackTVEXEName, HackTVPathText.Text)
    If (sDir <> "") Then
        HackTVPathText.Text = sDir
        ' Set a temporary variable containing the previous path. If Cancel is clicked, we'll restore it later
        TempHackTVPath = MainForm.HackTVPath
        MainForm.HackTVPath = sDir
    End If
    If DoesFileExist(HackTVPathText.Text & Chr(92) & MainForm.HackTVEXEName) = True Then Call MainForm.detectfork
        If MainForm.forktype = "CJ" Then
        DetectedFork.Caption = "Captain Jack"
    Else
        DetectedFork.Caption = "fsphil"
    End If
    If DoesFileExist(HackTVPathText.Text & Chr(92) & MainForm.HackTVEXEName) = False Then DetectedFork.Caption = "hacktv not found"
    PathChanged = True
    BadPath = False
End Sub

Private Sub TerminalBrowse_Click()
' Spawn an open folder dialog box
    Dim sDir As String
    sDir = BrowseForFolder(Me, "Please specify a directory that contains gnome-terminal", TerminalText.Text)
    If (sDir <> "") Then
        TerminalText.Text = sDir
    End If
End Sub

Private Sub SettingsOK_Click()
    If BadPath = True Then
        MsgBox "hacktv was not found at the specified path.", vbCritical, App.Title
        Exit Sub
    End If
    If RunningOnWine = True Then
        Call ApplyWINESettings
    Else
        Call ApplyWindowsSettings
    End If
    If RunningOnWine = True Then
        SaveSetting App.EXEName, "Settings", "TerminalPath", TerminalText.Text
    End If
' Unload form
    Unload Me
End Sub

Private Sub ApplyWindowsSettings()
' If the hacktv path field is left blank, set it to the default value
    If HackTVPathText.Text = "" Then HackTVPathText.Text = MainForm.DefaultHackTVPath
    If Not HackTVPathText.Text = MainForm.DefaultHackTVPath Then
        SaveSetting App.EXEName, "Settings", "HackTVPath", HackTVPathText.Text
    Else
        regvalue = GetSetting(App.EXEName, "Settings", "HackTVPath", "")
        If Not regvalue = "" Then DeleteSetting App.EXEName, "Settings", "HackTVPath"
    End If
    MainForm.HackTVPath = HackTVPathText.Text
    regvalue = ""
    If PathChanged = True Then Call MainForm.detectfork
End Sub

Private Sub ApplyWINESettings()
' If the hacktv path field is left blank, set it to the default value
    If HackTVPathText.Text = "" Then HackTVPathText.Text = MainForm.DefaultHackTVPath
' If the hacktv path is not the default, save the custom path to the registry
    If Not HackTVPathText.Text = MainForm.DefaultHackTVPath Then
        SaveSetting App.EXEName, "Settings", "HackTVPath", HackTVPathText.Text
    Else
' If the hacktv path is set to default, delete the custom path from the registry if it exists
        regvalue = GetSetting(App.EXEName, "Settings", "HackTVPath", "")
        If Not regvalue = "" Then DeleteSetting App.EXEName, "Settings", "HackTVPath"
    End If
    regvalue = ""
    MainForm.HackTVPath = HackTVPathText.Text
' If the gnome-terminal path field is left blank, set it to the default value
    If TerminalText.Text = "" Then TerminalText.Text = MainForm.DefaultTerminalPath
' If the gnome-terminal path is not the default, save the custom path to the registry
    If Not TerminalText.Text = MainForm.DefaultTerminalPath Then
        SaveSetting App.EXEName, "Settings", "TerminalPath", TerminalText.Text
    Else
' If the gnome-terminal path is not the default, delete the custom path from the registry if it exists
        regvalue = GetSetting(App.EXEName, "Settings", "TerminalPath", "")
        If Not regvalue = "" Then DeleteSetting App.EXEName, "Settings", "TerminalPath"
    End If
    regvalue = ""
    MainForm.TerminalPath = TerminalText.Text
' Restart the fork detection process
    If PathChanged = True Then Call MainForm.detectfork
End Sub

Private Sub SettingsCancel_Click()
    If Not TempHackTVPath = "" Then
    MainForm.HackTVPath = TempHackTVPath
    TempHackTVPath = ""
    ' Restart the fork detection process
    Call MainForm.detectfork
    End If
    Unload Me
End Sub

Private Sub SettingsReset_Click()
' Load default paths
    If RunningOnWine = False Then
        HackTVPathText.Text = MainForm.DefaultHackTVPath
    ElseIf RunningOnWine = True Then
        HackTVPathText.Text = MainForm.DefaultHackTVPath
        TerminalText.Text = MainForm.DefaultTerminalPath
    End If
End Sub
