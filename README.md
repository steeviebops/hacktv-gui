# hacktv-gui - a GUI frontend application for hacktv

hacktv-gui is, as the name suggests, a GUI frontend application for hacktv, which is available at https://github.com/fsphil/hacktv/ or https://github.com/captainjack64/hacktv/. It allows you to run hacktv without the use of a command line. In addition, it includes playlist support, a handler for online teletext services, and support for any streaming video site which is compatible with yt-dlp. You can also save your settings to a file and reload them later.

The application has been tested on:

- Windows 7 SP1, 10 and 11
- Ubuntu 20.04, 21.04 and 22.04
- Kubuntu 22.04
- Fedora Workstation 32
- macOS Catalina and Ventura.

### Pre-requisites
- A Java 11 or later runtime environment is required. Java 8 will not work. <a href="https://adoptopenjdk.net">AdoptOpenJDK</a> or <a href="https://www.microsoft.com/openjdk">Microsoft's build of OpenJDK</a> have been tested.
- To use yt-dlp features, a copy of <a href="https://github.com/yt-dlp/yt-dlp/releases/">yt-dlp</a> is required. This can be either in your system path, or placed in the same directory as the JAR file.

### Licence information
This project was originally licensed under GPL v2 until 26th April 2021. This was due to the use of GPL v2 code to capture the console output and display it on-screen. I have since replaced this code with my own work, so I have now relicensed the project to GPL v2 or later. Feel free to reuse any of this code in a GPL v3 project.

### Credits
A big thank you to:

- fsphil - for creating hacktv and making this possible in the first place.
- Captain Jack - for expanding on fsphil's work, particularly with conditional access.
- My wife Amanda - for putting up with me while working on this project!
