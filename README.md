# hacktv-gui - a GUI frontend application for hacktv

hacktv-gui is, as the name suggests, a GUI frontend application for hacktv. - https://github.com/fsphil/hacktv/ or https://github.com/captainjack64/hacktv/. It allows you to run hacktv if you have no command line experience. You can also save your settings to a file and reload them later.

This repo is a full rewrite for Java 11. The interface has been redesigned and incorporates the output of hacktv in its own console window. All features from the Visual Basic 6.0 version are supported, with the exception of the C-state option (which was Windows-specific). Configuration files from version 3.0 or later of the VB6 version are fully supported.

The application has been tested on Windows 10, Ubuntu 20.04 and Fedora Workstation 32. It has also had some basic testing on MacOS Catalina.

### Known issues on Windows
- In most cases, the console window will only display hacktv's output when it is closed. This appears to be related to Microsoft's behaviour of buffering stdOut and stdErr streams. You can work around this by editing the hacktv source to add the line *setvbuf(stderr, NULL, _IONBF, 0);* as the first line inside the *vid_info* function in the video.c file.
- Windows doesn't have a native way of programmatically sending SIGINT signals to gracefully close hacktv. Therefore, a helper application (windows-kill) is required when running hacktv-gui on Windows. You can download it from https://github.com/alirdn/windows-kill/releases/. Save windows-kill.exe in the same directory as the JAR file.

### Licence information
This project was originally licensed under GPL v2 until 26th April 2021. This was due to the use of GPL v2 code to capture the console output and display it on-screen. I have since replaced this code with my own work, so I have now relicensed the project to GPL v2 or later. Feel free to reuse any of this code in a GPL v3 project.

### Credits
A big thank you to:

- fsphil - for creating hacktv and making this possible in the first place.
- Captain Jack - for expanding on fsphil's work, particularly with conditional access.
- My wife Amanda - for putting up with me while working on this project!
