<div align="center">

<img src="logo.png" width="376" height="128" alt="Winlator Logo" />

# 🎮 Winlator — RedMagic Edition

### Run Windows games on your Android phone — tuned for Snapdragon 8+ Gen 1

<a href="https://readme-typing-svg.demolab.com">
  <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=600&size=22&duration=2800&pause=900&color=00B0FF&center=true&vCenter=true&width=720&lines=Wine+%2B+Box64+for+ARM64+Android;Adreno+730+%E2%80%A2+Turnip+%E2%80%A2+DXVK+2.6.1;120+Hz+%E2%80%A2+Sustained+Performance+%E2%80%A2+Material+You;Forked+%26+tuned+by+Shorif+Uddin+Piash" alt="Typing SVG" />
</a>

<br/>

[![Android CI](https://github.com/piashmsuf-eng/winlator/actions/workflows/android.yml/badge.svg)](https://github.com/piashmsuf-eng/winlator/actions/workflows/android.yml)
[![Latest Release](https://img.shields.io/github/v/release/piashmsuf-eng/winlator?display_name=tag&label=Release&color=00b0ff)](https://github.com/piashmsuf-eng/winlator/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/piashmsuf-eng/winlator/total?color=ff7043)](https://github.com/piashmsuf-eng/winlator/releases)
[![License](https://img.shields.io/badge/license-GPL--3.0-blueviolet)](LICENSE)
[![Min SDK](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](#)
[![ABI](https://img.shields.io/badge/ABI-arm64--v8a-1976d2)](#)

[![Box64](https://img.shields.io/badge/Box64-0.4.0-3949ab)](https://github.com/ptitSeb/box64)
[![Wine](https://img.shields.io/badge/Wine-9.21--staging--tkg-7e57c2)](https://www.winehq.org/)
[![DXVK](https://img.shields.io/badge/DXVK-2.6.1-e91e63)](https://github.com/doitsujin/dxvk)
[![VKD3D](https://img.shields.io/badge/VKD3D-2.14.1-009688)](https://gitlab.winehq.org/wine/vkd3d)
[![Mesa Turnip](https://img.shields.io/badge/Mesa%20Turnip-26.1.0-ff5722)](https://www.mesa3d.org/)

<br/>

[**📥 Download APK**](https://github.com/piashmsuf-eng/winlator/releases/latest) &nbsp;•&nbsp;
[**🐛 Issues**](https://github.com/piashmsuf-eng/winlator/issues) &nbsp;•&nbsp;
[**🚀 Pull Requests**](https://github.com/piashmsuf-eng/winlator/pulls) &nbsp;•&nbsp;
[**🧠 Upstream**](https://github.com/brunodev85/winlator)

</div>

---

## ✨ What's new in this fork

> Maintained by **Shorif Uddin Piash** ([@piashmsuf-eng](https://github.com/piashmsuf-eng))
> Forked from [brunodev85/winlator 11.0](https://github.com/brunodev85/winlator). Targeted at **Nubia RedMagic 7s Pro** (Snapdragon 8+ Gen 1, Adreno 730, 120 Hz AMOLED) but works on any Android 8+ arm64 device.

<table>
<tr>
<td width="50%" valign="top">

### 🔥 Performance
- **Box64 preset** `RedMagic / SD 8+ Gen 1` — Cortex-X2 + A710 + A510 dynarec tune (BLEEDING_EDGE, BIGBLOCK_DETECT, NATIVEFLAGS, WEAKBARRIER=2)
- **120 Hz refresh-rate request** on the X server activity (auto-detects the panel's highest mode)
- **Sustained Performance Mode** — pins SoC clocks to a thermally stable level for long sessions
- **DXVK 2.6.1** bundled as default (was 2.4.1) — shader-cache + d3d11 perf fixes

</td>
<td width="50%" valign="top">

### 🎨 UI / UX
- **Material You / Dynamic Colors** — wallpaper-derived theme on Android 12+
- **Search bar** on Containers and Shortcuts home screens
- **Auto-update checker** — daily poll of GitHub Releases, opt-in dialog
- **Crash dialog with Copy-to-Clipboard** — no more silent crashes
- **Log viewer filter** — case-insensitive substring filter above the in-app log

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 🎯 Per-shortcut tuning
- **DXVK HUD toggle** (`DXVK_HUD=fps,frametimes,gpuload,memory,version`)
- **Max FPS cap** field (`DXVK_FRAME_RATE` + `VKD3D_FRAME_RATE`) — thermally pin a 90 Hz cap on a 120 Hz panel
- **Box64 preset override** — pick a different preset per game
- **Force Fullscreen**, **Screen Size** override, **Controls Profile**, **Audio Driver**

</td>
<td width="50%" valign="top">

### 🛠️ Build & Dev
- **GitHub Actions CI** — debug APK built on every push, uploaded as artefact
- **De-submoduled** — `app/`, `vortek/`, `gladio/` inlined for single-repo PRs
- **JDK 17 / NDK 24.0.8215888 / CMake 3.22.1 / AGP 7.2.2**
- **arm64-v8a only** — slim APK, no x86/armv7 bloat

</td>
</tr>
</table>

---

## 📦 Installation

```text
1. Go to  ┃ https://github.com/piashmsuf-eng/winlator/releases/latest
2. Tap on ┃ winlator-redmagic.apk
3. Enable ┃ Settings → Apps → "Install unknown apps" for your file manager / browser
4. Tap    ┃ the .apk → Install
5. Launch ┃ wait ~1-2 min for rootfs install on first run
```

> The signed Play Store build is **not** available; this is a debug APK signed with the Android debug keystore. Side-load only.

---

## 🚀 Quick-start: First container

```text
┌─[ Step 1 ]──── Add a container
│  Containers → ➕  → Detail screen
└─

┌─[ Step 2 ]──── RedMagic-tuned settings
│  Graphics Driver  →  Turnip + DXVK
│  DX Wrapper       →  DXVK
│  Box64 Preset     →  RedMagic / SD 8+ Gen 1
│  Screen Size      →  1920x1080  (or 1280x720 for heavy games)
│  Audio Driver     →  PulseAudio
└─

┌─[ Step 3 ]──── Save & Run
│  ▶ Run icon launches Wine desktop
│  Drop your setup.exe in /sdcard/Downloads, install via Wine File Manager
│  Long-press the .exe → "Add Shortcut to Frontend"
└─

┌─[ Step 4 ]──── Per-shortcut tuning  (NEW in this fork)
│  Long-press the shortcut → Settings → Advanced
│  ☑ DXVK HUD   →  FPS overlay
│  Max FPS  90  →  thermal save on 120 Hz panel
│  Box64 Preset →  override per game if needed
└─
```

Full step-by-step Bangla tutorial: see <kbd>Wiki</kbd> → "Container Setup".

---

## 🧪 Building from source

```bash
git clone https://github.com/piashmsuf-eng/winlator.git
cd winlator/app
./gradlew assembleDebug
# Output: app/app/build/outputs/apk/debug/app-debug.apk
```

**Requirements** &nbsp;|&nbsp; JDK 17 &nbsp;•&nbsp; Android SDK `platforms;android-34` &nbsp;•&nbsp; NDK `24.0.8215888` &nbsp;•&nbsp; CMake `3.22.1` &nbsp;•&nbsp; AGP `7.2.2`

CI is set up at [`.github/workflows/android.yml`](.github/workflows/android.yml) — it builds a debug APK on every push and uploads it as a workflow artefact.

---

## 📺 Demos (upstream)

[![Play on Youtube](https://img.youtube.com/vi/ETYDgKz4jBQ/3.jpg)](https://www.youtube.com/watch?v=ETYDgKz4jBQ)
[![Play on Youtube](https://img.youtube.com/vi/9E4wnKf2OsI/2.jpg)](https://www.youtube.com/watch?v=9E4wnKf2OsI)
[![Play on Youtube](https://img.youtube.com/vi/czEn4uT3Ja8/2.jpg)](https://www.youtube.com/watch?v=czEn4uT3Ja8)
[![Play on Youtube](https://img.youtube.com/vi/eD36nxfT_Z0/2.jpg)](https://www.youtube.com/watch?v=eD36nxfT_Z0)

---

## 💡 Tips

- **Performance issues?** Container Settings → Advanced → Box64 Preset → `Performance` or `RedMagic / SD 8+ Gen 1`
- **.NET Framework apps?** Start Menu → System Tools → Installers → `Wine Mono`
- **Old game won't launch?** Container Settings → Environment Variables → `MESA_EXTENSION_MAX_YEAR=2003`
- **Run from shortcut, not container** — per-game settings stick that way
- **Low-res game with black bars?** Shortcut Settings → ☑ Force Fullscreen
- **Unity engine crash?** Box64 Preset → `Stability` and add exec arg `-force-gfx-direct`

---

## 🙏 Credits

| Component | Project |
|---|---|
| GLIBC Patches | [Termux Pacman](https://github.com/termux-pacman/glibc-packages) |
| Wine | [winehq.org](https://www.winehq.org/) |
| Box86 / Box64 | [@ptitSeb](https://github.com/ptitSeb) |
| Mesa (Turnip / Zink / VirGL) | [mesa3d.org](https://www.mesa3d.org) |
| DXVK | [doitsujin/dxvk](https://github.com/doitsujin/dxvk) |
| VKD3D | [winehq.org/vkd3d](https://gitlab.winehq.org/wine/vkd3d) |
| CNC DDraw | [FunkyFr3sh/cnc-ddraw](https://github.com/FunkyFr3sh/cnc-ddraw) |
| Upstream Winlator | [@brunodev85](https://github.com/brunodev85/winlator) |
| **RedMagic / SD 8+ Gen 1 fork** | **Shorif Uddin Piash ([@piashmsuf-eng](https://github.com/piashmsuf-eng))** |

Special thanks to all the developers involved in these projects, and to everyone who believes in this project. ❤️

<div align="center">

---

<sub>📱 Built for the RedMagic 7s Pro &nbsp;•&nbsp; Powered by Wine, Box64 and Vulkan &nbsp;•&nbsp; License: GPL-3.0</sub>

</div>
