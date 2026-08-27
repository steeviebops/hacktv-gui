Unicode true
!include "MUI2.nsh"
!include "FileFunc.nsh"
!include "WinVer.nsh"
!include "Integration.nsh"
!include x64.nsh

Target "amd64-unicode"

Name "hacktv-gui"
InstallDir "$APPDATA\hacktv-gui"
OutFile "hacktv-gui_setup.exe"
RequestExecutionLevel user
SetCompressor /SOLID lzma

!insertmacro MUI_PAGE_WELCOME

!define MUI_PAGE_HEADER_TEXT "hacktv-gui Licence"
!define MUI_PAGE_HEADER_SUBTEXT "Please review the hacktv-gui licence."
!insertmacro MUI_PAGE_LICENSE "licenses\LICENSE.txt"

!define /ReDef MUI_PAGE_HEADER_TEXT "FlatLaf Licence"
!define /ReDef MUI_PAGE_HEADER_SUBTEXT "Please review the FlatLaf licence."
!insertmacro MUI_PAGE_LICENSE "licenses\FlatLaf-LICENSE.txt"

!insertmacro MUI_PAGE_COMPONENTS

var StartMenuFolder
!define MUI_INNERTEXT_STARTMENU_CHECKBOX "Do not create shortcuts (Portable installation)"
!insertmacro MUI_PAGE_STARTMENU $(^Name) $StartMenuFolder

!insertmacro MUI_PAGE_DIRECTORY

!insertmacro MUI_PAGE_INSTFILES

#!define MUI_FINISHPAGE_NOAUTOCLOSE
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Version Information

!ifndef VERSION
    !define VERSION "0.0.0.0"
!endif
VIProductVersion ${VERSION}
VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "hacktv-gui"
VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "GUI wrapper for hacktv"
VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "Copyright (C) 2026 Stephen McGarry (https://github.com/steeviebops)"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "hacktv-gui installer"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" ${VERSION}
VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductVersion" ${VERSION}
VIAddVersionKey /LANG=${LANG_ENGLISH} "InternalName" "setup"
VIAddVersionKey /LANG=${LANG_ENGLISH} "OriginalFilename" "hacktv-gui_setup.exe"

;--------------------------------

!ifndef ARCH
    !error "ARCH must be specified (x64 or arm64)"
!endif

!if "${ARCH}" == "x64"
    !define INSTALL_X64
!else
    !if "${ARCH}" == "arm64"
        !define INSTALL_ARM64
    !else
        !error "Invalid ARCH: ${ARCH}"
    !endif
!endif

Section "!Required files" MAIN
    SectionIn RO
    SetOutPath $INSTDIR
    File "hacktv-gui.exe"

    CreateDirectory "$INSTDIR\bin"
    SetOutPath "$INSTDIR\bin"

    # hacktv-gui.jar
    File "hacktv-gui.jar"

    # FlatLaf
    CreateDirectory "$INSTDIR\bin\lib"
    SetOutPath "$INSTDIR\bin\lib"
    File "lib\flatlaf-3.7.2.jar"
    File "lib\flatlaf-intellij-themes-3.7.2.jar"

    # JRE
    CreateDirectory "$INSTDIR\jre"
    SetOutPath "$INSTDIR\jre"
    File /r "jre\"

    # Licences
    CreateDirectory "$INSTDIR\bin\licenses"
    SetOutPath "$INSTDIR\bin\licenses"
    File "licenses\LICENSE.txt"
    File "licenses\FlatLaf-LICENSE.txt"

    # Create Start menu shortcuts if enabled
    !insertmacro MUI_STARTMENU_WRITE_BEGIN $(^Name)
        CreateDirectory "$SMPrograms\$StartMenuFolder"
        CreateShortcut /NoWorkingDir "$SMPrograms\$StartMenuFolder\$(^Name).lnk" "$InstDir\hacktv-gui.exe"
        CreateShortcut /NoWorkingDir "$SMPrograms\$StartMenuFolder\$(^Name) (Console mode).lnk" "$InstDir\hacktv-gui.exe" "/console"
        ${If} $(^Name) != $StartMenuFolder
            # Write the name of the selected Start Menu folder to the registry so we can remove it during uninstall
            WriteRegStr HKCU "Software\$(^Name)\Setup" "CustomStartDir" $StartMenuFolder
        ${EndIf}
        # Uninstaller data
        WriteUninstaller "$INSTDIR\uninstall.exe"
        !define UNINSTALL_PATH "Software\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
        WriteRegStr HKCU "${UNINSTALL_PATH}" "DisplayName" $(^Name)
        WriteRegStr HKCU "${UNINSTALL_PATH}" "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
        WriteRegStr HKCU "${UNINSTALL_PATH}" "QuietUninstallString" "$\"$INSTDIR\uninstall.exe$\" /S"
        WriteRegStr HKCU "${UNINSTALL_PATH}" "DisplayIcon" "$\"$INSTDIR\hacktv-gui.exe$\""
        WriteRegStr HKCU "${UNINSTALL_PATH}" "Publisher" "Stephen McGarry"
        WriteRegStr HKCU "${UNINSTALL_PATH}" "UrlUpdateInfo" "https://github.com/steeviebops/hacktv-gui"
        # File associations
        !define ASSOC_EXT ".htv"
        !define ASSOC_PROGID "hacktv-gui"
        !define ASSOC_VERB "open"
        !define ASSOC_APPEXE "hacktv-gui.exe"
        !define ASSOC_DESC "hacktv-gui configuration file"
        # Register file type
        WriteRegStr HKCU "Software\Classes\${ASSOC_PROGID}\DefaultIcon" "" "$InstDir\${ASSOC_APPEXE},0"
        WriteRegStr HKCU "Software\Classes\${ASSOC_PROGID}" "" "${ASSOC_DESC}"
        WriteRegStr HKCU "Software\Classes\${ASSOC_PROGID}\shell\${ASSOC_VERB}\command" "" '"$InstDir\${ASSOC_APPEXE}" "%1"'
        WriteRegStr HKCU "Software\Classes\${ASSOC_EXT}" "" "${ASSOC_PROGID}"

        ${NotifyShell_AssocChanged}

    !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

Section "hacktv" HACKTV
SectionEnd

Section ""
    SetOutPath "$INSTDIR\bin"
    ${If} ${SectionIsSelected} ${HACKTV}
        DetailPrint "Downloading hacktv..."
        NScurl::http GET "https://download.bops.ie/hacktv/fsphil.zip" "$INSTDIR\bin\fsphil.zip" /INSIST /CANCEL /RESUME /END
        Pop $6
        ${If} $6 == "OK"
            ExecWait '"$INSTDIR\hacktv-gui.exe" /copydllandhacktv' $1
            ${If} $1 != 0
                MessageBox MB_OK|MB_ICONEXCLAMATION \
                    "An error occurred while attempting to install native components and/or hacktv."
            ${EndIf}
        ${Else}
            MessageBox MB_OK|MB_ICONEXCLAMATION \
                "hacktv could not be downloaded.$\n$\nYou can download it later from within hacktv-gui."
            ExecWait '"$INSTDIR\hacktv-gui.exe" /copydll' $1
            ${If} $1 != 0
                MessageBox MB_OK|MB_ICONEXCLAMATION \
                    "An error occurred while attempting to install native components."
            ${EndIf}
        ${EndIf}
    ${Else}
        DetailPrint "Installing native components..."
        ExecWait '"$INSTDIR\hacktv-gui.exe" /copydll' $2
        ${If} $2 != 0
            MessageBox MB_OK|MB_ICONEXCLAMATION \
                "An error occurred while attempting to install native components."
        ${EndIf}
    ${EndIf}
SectionEnd

Section /o "yt-dlp" YT_DLP
    SetOutPath "$INSTDIR\bin"
    DetailPrint "Downloading https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
    NScurl::http GET "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe" "$INSTDIR\bin\yt-dlp.exe" /INSIST /CANCEL /RESUME /END
    Pop $7
    ${If} $7 != "OK"
        MessageBox MB_OK|MB_ICONEXCLAMATION \
            "An error occurred while attempting to download yt-dlp."
    ${EndIf}
SectionEnd

Section ""
    # Calculate on-disk size, from https://nsis-dev.github.io/NSIS-Forums/html/t-267188.html
    ${GetSize} "$INSTDIR" "/S=0K" $0 $1 $2
    IntFmt $0 "0x%08X" $0
    WriteRegDWORD HKCU "${UNINSTALL_PATH}" "EstimatedSize" "$0"
SectionEnd

Section Uninstall
    MessageBox MB_YESNO|MB_ICONQUESTION|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" /SD IDYES IDYES true IDNO false
    true:
        # Remove launcher
        ${If} ${FileExists} `$INSTDIR\hacktv-gui.exe`
            Delete "$INSTDIR\hacktv-gui.exe"
        ${EndIf}
        # Remove bin directory
        ${If} ${FileExists} `$INSTDIR\bin\*.*`
            RMDir /r $INSTDIR\bin
        ${EndIf}
        # Remove JRE
        ${If} ${FileExists} `$INSTDIR\jre\*.*`
            RMDir /r $INSTDIR\jre
        ${EndIf}
        # Remove Start menu shortcuts
        var /global startDir
        ReadRegStr $0 HKCU "Software\$(^Name)\Setup" "CustomStartDir"
        ${If} $0 == ""
            StrCpy $startDir $(^Name)
        ${Else}
            StrCpy $startDir $0
        ${EndIf}
        ${If} ${FileExists} `$SMPrograms\$startDir\*.*`
            Delete "$SMPrograms\$startDir\$(^Name).lnk"
            Delete "$SMPrograms\$startDir\$(^Name) (Console mode).lnk"
            RMDir "$SMPrograms\$startDir"
        ${EndIf}
        # Remove reg keys
        DeleteRegKey HKCU "Software\$(^Name)"
        DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
        # Unregister file type
        DeleteRegKey HKCU "Software\Classes\.htv"
        DeleteRegKey HKCU "Software\Classes\$(^Name)"
        ${NotifyShell_AssocChanged}
        # Remove uninstaller
        ${If} ${FileExists} `$INSTDIR\uninstall.exe`
            Delete "$INSTDIR\Uninstall.exe"
        ${EndIf}
        # Remove install dir
        ${If} ${FileExists} `$INSTDIR`
            RMDir $INSTDIR
        ${EndIf}
        Goto next
    false:
        Quit
    next:
SectionEnd

Function .onInit
    # CPU architecture check
    !ifdef INSTALL_X64
        ${IfNot} ${RunningX64}
            MessageBox MB_OK|MB_ICONSTOP \
                "This installer is for x64 builds of Windows only."
            Quit
        ${EndIf}
    !endif

    !ifdef INSTALL_ARM64
        ${IfNot} ${IsNativeARM64}
            MessageBox MB_OK|MB_ICONSTOP \
                "This installer is for ARM64 builds of Windows only."
            Quit
        ${EndIf}
    !endif

    # Windows version check
    ${WinVerGetBuild} $R2
    ${IfNot} ${AtLeastWin10}
        MessageBox MB_OK|MB_ICONSTOP "This application requires Windows 10 or later."
        Quit
    ${EndIf}

    # Set estimated disk space requirements for each section
    SectionSetSize ${MAIN} 204800
    SectionSetSize ${HACKTV} 24576
    SectionSetSize ${YT_DLP} 20480
FunctionEnd

# Set section descriptions
LangString DESC_MAIN ${LANG_ENGLISH} "Installs hacktv-gui and supporting files."
LangString DESC_HACKTV ${LANG_ENGLISH} "Installs the latest build of hacktv. The fork can be changed in hacktv-gui after installation is complete."
LangString DESC_YT_DLP ${LANG_ENGLISH} "Installs yt-dlp, a YouTube downloader. Used for streaming videos from online video sites."
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${MAIN} $(DESC_MAIN)
    !insertmacro MUI_DESCRIPTION_TEXT ${HACKTV} $(DESC_HACKTV)
    !insertmacro MUI_DESCRIPTION_TEXT ${YT_DLP} $(DESC_YT_DLP)
!insertmacro MUI_FUNCTION_DESCRIPTION_END
