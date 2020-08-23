Attribute VB_Name = "mINIfunctions"
' Procedures to read and write INI files
' Based on http://www.vbforums.com/showthread.php?277554-VB-INI-Handling
    Private Declare Function GetPrivateProfileString Lib "kernel32" Alias "GetPrivateProfileStringA" (ByVal lpApplicationName As String, ByVal lpKeyName As Any, ByVal lpDefault As String, ByVal lpReturnedString As String, ByVal nSize As Long, ByVal lpFileName As String) As Long
    Private Declare Function WritePrivateProfileString Lib "kernel32" Alias "WritePrivateProfileStringA" (ByVal lpApplicationName As String, ByVal lpKeyName As Any, ByVal lpString As Any, ByVal lpFileName As String) As Long
     
    ' Read INI file
    ' This function has been modified to return an optional default value rather than returning nothing
    ' Ideal for cases where a returned null value is unacceptable (e.g. integer or Boolean values)
    Public Function ReadIniValue(FileName As String, Section As String, Key As String, Optional DefaultVal As String) As String
    If DefaultVal = vbNullString Then DefaultVal = ""
    Dim RetVal As String * 255, v As Long
    v = GetPrivateProfileString(Section, Key, "", RetVal, 255, FileName)
    If Not Left(RetVal, v) = "" Then
        ReadIniValue = Left(RetVal, v)
    Else
        If Not DefaultVal = "" Then ReadIniValue = DefaultVal
    End If
    End Function
   
    ' Write INI file
    Public Sub WriteIniValue(FileName As String, Section As String, Key As String, Value As String)
    If Value = vbNullString Then Value = ""
    WritePrivateProfileString Section, Key, Value, FileName
    End Sub
