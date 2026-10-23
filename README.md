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
`build\libs\nexora-client-1.3.3.jar`. Install Fabric Loader 0.16.10 (or newer)
for Minecraft 1.21.1, then place the JAR and Fabric API 0.116.6+1.21.1 in that
Minecraft installation's `mods` folder.

## Menu behavior

The startup screen shows a smooth-filtered Nexora logo and a "Press to Enter"
prompt. Enter or click to open the main menu; returning to the title screen
after leaving a world shows the intro again. The logo follows an upward curved
animation as the menu appears. A bottom-left label shows the Nexora Client
version and Minecraft version in the same font as the menu. Singleplayer,
Multiplayer, and Settings open Minecraft's original screens; Client Settings
opens Nexora's settings page, and Quit Game closes the client. The main menu
uses a minimal dark layout with a centered transparent Nexora logo and centered
text-first buttons with subtle animated hover underlines. It has no character
preview. The account selector sits in the upper-right corner without a skin
preview and no account selector.

Menu labels use the
Onest Bold font from [combatant-client](https://github.com/Parcus123/combatant-client),
under the SIL Open Font License 1.1; its license is included with the font
asset.
