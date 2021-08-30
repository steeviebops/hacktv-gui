# hacktv-gui - a GUI frontend application for hacktv

hacktv-gui is, as the name suggests, a GUI frontend application for hacktv. - https://github.com/fsphil/hacktv/ or https://github.com/captainjack64/hacktv/. It allows you to run hacktv if you have no command line experience. You can also save your settings to a file and reload them later.

This repo is a full rewrite for Java 11. The interface has been redesigned and incorporates the output of hacktv in its own console window. All features from the Visual Basic 6.0 version are supported, with the exception of the C-state option (which was Windows-specific). Configuration files from version 3.0 or later of the VB6 version are fully supported.

The application has been tested on Windows 10, Ubuntu 20.04, 21.04 and Fedora Workstation 32. It has also had some basic testing on MacOS Catalina.

### Pre-requisites
- A Java 11 or later runtime environment is required. Java 8 will not work. <a href="https://adoptopenjdk.net">AdoptOpenJDK</a> or <a href="https://www.microsoft.com/openjdk">Microsoft's build of OpenJDK</a> have been tested.
- Windows doesn't have a native way of programmatically sending SIGINT signals to gracefully close hacktv. Therefore, a helper application (windows-kill) is required when running hacktv-gui on Windows. If windows-kill cannot be found on startup, you'll be prompted to download it. This can also be done from the GUI settings tab. Alternatively, you can download it from https://github.com/ElyDotDev/windows-kill/releases/ and save it in the same directory as the JAR file.

### Licence information
This project was originally licensed under GPL v2 until 26th April 2021. This was due to the use of GPL v2 code to capture the console output and display it on-screen. I have since replaced this code with my own work, so I have now relicensed the project to GPL v2 or later. Feel free to reuse any of this code in a GPL v3 project.

### Credits
A big thank you to:

- fsphil - for creating hacktv and making this possible in the first place.
- Captain Jack - for expanding on fsphil's work, particularly with conditional access.
- My wife Amanda - for putting up with me while working on this project!
