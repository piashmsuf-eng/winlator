<p align="center">
	<img src="logo.png" width="376" height="128" alt="Winlator Logo" />
</p>

# Winlator

Winlator is an Android application that lets you to run Windows (x86_64) applications with Wine and Box86/Box64.

# Installation

1. Download and install the APK (Winlator_11.0.apk) from [GitHub Releases](https://github.com/brunodev85/winlator/releases)
2. Launch the app and wait for the installation process to finish

----

[![Play on Youtube](https://img.youtube.com/vi/ETYDgKz4jBQ/3.jpg)](https://www.youtube.com/watch?v=ETYDgKz4jBQ)
[![Play on Youtube](https://img.youtube.com/vi/9E4wnKf2OsI/2.jpg)](https://www.youtube.com/watch?v=9E4wnKf2OsI)
[![Play on Youtube](https://img.youtube.com/vi/czEn4uT3Ja8/2.jpg)](https://www.youtube.com/watch?v=czEn4uT3Ja8)
[![Play on Youtube](https://img.youtube.com/vi/eD36nxfT_Z0/2.jpg)](https://www.youtube.com/watch?v=eD36nxfT_Z0)

----

# Building from source

This fork inlines the previously-separate `app`, `vortek` and `gladio` submodules so the
whole project builds from a single repo.

Requirements:

- JDK 17
- Android SDK with `platforms;android-34` and `build-tools;34.0.0`
- Android NDK `24.0.8215888`
- CMake `3.22.1`

```bash
git clone https://github.com/piashmsuf-eng/winlator.git
cd winlator/app
./gradlew assembleDebug
# -> app/app/build/outputs/apk/debug/app-debug.apk
```

The repo also ships a [GitHub Actions workflow](.github/workflows/android.yml) that
produces a debug APK on every push and uploads it as a build artefact.

# RedMagic / Snapdragon 8+ Gen 1 tuning (this fork)

This fork adds the following extras over upstream Winlator 11.0:

- **Box64 preset `RedMagic / SD 8+ Gen 1`** — tuned for Cortex-X2 + A710 + A510
  with Adreno 730. Available under *Container Settings → Advanced → Box64 Preset*.
- **High Refresh Rate** toggle — requests the highest supported refresh rate (90/120 Hz on
  RedMagic 7s Pro AMOLED) for the X server activity.
- **Sustained Performance Mode** toggle — uses Android's
  `Window#setSustainedPerformanceMode(true)` so the SoC clocks to a level the device can
  hold for the whole gaming session, reducing thermal-throttle drops on long sessions.
- **Crash dialog with Copy-to-Clipboard** — replaces silent crashes with a stack-trace
  dialog so you can file useful bug reports.

Both new toggles default to **on** and live under *Settings → Advanced*. The Box64 preset
must be selected manually (per container or globally).

# Useful Tips

- If you are experiencing performance issues, try changing the Box64 preset to `Performance` (or `RedMagic / SD 8+ Gen 1` on supported devices) in Container Settings -> Advanced Tab.
- For applications that use .NET Framework, try installing `Wine Mono` found in Start Menu -> System Tools -> Installers.
- If some older games don't open, try adding the environment variable `MESA_EXTENSION_MAX_YEAR=2003` in Container Settings -> Environment Variables.
- Try running the games using the shortcut on the Winlator home screen, there you can define individual settings for each game.
- To display low resolution games correctly, try to enabling the `Force Fullscreen` option in the shortcut settings.
- To improve stability in games that uses Unity Engine, try changing the Box64 preset to `Stability` or in the shortcut settings add the exec argument `-force-gfx-direct`.

# Credits and Third-party apps

- GLIBC Patches by [Termux Pacman](https://github.com/termux-pacman/glibc-packages)
- Wine ([winehq.org](https://www.winehq.org/))
- Box86/Box64 by [ptitseb](https://github.com/ptitSeb)
- Mesa (Turnip/Zink/VirGL) ([mesa3d.org](https://www.mesa3d.org))
- DXVK ([github.com/doitsujin/dxvk](https://github.com/doitsujin/dxvk))
- VKD3D ([gitlab.winehq.org/wine/vkd3d](https://gitlab.winehq.org/wine/vkd3d))
- CNC DDraw ([github.com/FunkyFr3sh/cnc-ddraw](https://github.com/FunkyFr3sh/cnc-ddraw))

Special thanks to all the developers involved in these projects.<br>
Thank you to all the people who believe in this project.
