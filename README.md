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
`build\libs\nexora-client-1.0.6.jar`. Install Fabric Loader 0.16.10 (or newer)
for Minecraft 1.21.1, then place the JAR and Fabric API 0.116.6+1.21.1 in that
Minecraft installation's `mods` folder.

## Menu behavior

The startup screen shows the Nexora logo and a "Press to Enter" prompt. Enter
or click to open the main menu; the logo follows an upward curved animation as
the menu appears. Singleplayer, Multiplayer, and Settings open Minecraft's
original screens; Client Settings opens Nexora's settings page, and Quit Game
closes the client. The menu uses the transparent Nexora logo, compact centered
button hit areas with animated hover underlines, and a three-dimensional player
model with a walking entrance and correctly oriented, cursor-following head.
The preview uses the active Minecraft skin, including slim-arm models, or
Steve as a fallback. Menu labels use the
Onest Bold font from [combatant-client](https://github.com/Parcus123/combatant-client),
under the SIL Open Font License 1.1; its license is included with the font
asset.

The account panel displays the currently authenticated Minecraft session and
its available skin. Authentication and account selection remain with the
launcher: the mod does not invent accounts, change usernames to impersonate a
login, or save credentials.