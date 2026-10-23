# Nexora Client

Nexora Client is a client-side Fabric mod that replaces Minecraft Java Edition
1.21.1's title screen with a dark neon menu. It does not replace Minecraft or
TLauncher and does not add a separate launcher.

## Build

Requirements: JDK 21.

From the repository root, run:

```powershell
.\gradlew.bat build
```

The distributable mod is created at
`build\libs\nexora-client-1.0.1.jar`. Install Fabric Loader 0.16.10 (or newer)
for Minecraft 1.21.1, then place the JAR and Fabric API 0.116.6+1.21.1 in that
Minecraft installation's `mods` folder.

## Menu behavior

Singleplayer, Multiplayer, and Settings open Minecraft's original screens.
The menu uses the supplied Nexora logo, animated hover underlines, and a
character preview with a walking entrance and cursor-following head. The
account panel displays the currently authenticated Minecraft session and its
available skin. Authentication and account selection remain with the
launcher: the mod does not invent accounts, change usernames to impersonate a
login, or save credentials.