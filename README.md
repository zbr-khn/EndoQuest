# 🦷 EndoQuest: The Root Canal Run

An Interactive 3D Educational Endodontic Adventure for Dental Professionals, Students, and Mobile Gamers.

[![Download APK](https://img.shields.io/badge/Download-Android%20APK-brightgreen?style=for-the-badge&logo=android)](https://github.com/zbr-khn/EndoQuest/releases/latest/download/EndoQuest.apk)
[![Vercel Deployment](https://img.shields.io/badge/Deploy-Vercel-black?style=flat&logo=vercel)](https://vercel.com)
[![Platform](https://img.shields.io/badge/Platform-Android%20Native%20%7C%20iOS%20(PWA)%20%7C%20Web-00E5FF)](https://github.com/zbr-khn/EndoQuest)

---

## 📥 Download the Android Game (APK)

You can download and play the latest native Android APK directly:

👉 **[Download EndoQuest.apk (Latest Build)](https://github.com/zbr-khn/EndoQuest/releases/latest/download/EndoQuest.apk)**

### How to Install:
1. Tap the download link above on your Android device (or download to PC and transfer via USB).
2. Tap the downloaded `EndoQuest.apk` file.
3. If prompted by Android, enable **"Install from unknown sources"** for your browser or file manager.
4. Tap **Install** and enjoy playing!

---

## 🎮 The 4-Round Clinical Gameplay Experience

EndoQuest bridges evidence-based clinical dentistry with vibrant arcade mobile gaming:

### 🏃‍♂️ Round 1: 3D Dental Highway & Highway Patrol
- **3D Perspective Endless Runner**: Sprint as Dr. Alex down the 3-lane dental highway, collecting $25 gold coins.
- **Custom Subway Surfers-Style Obstacles**:
  - 🦷 **Decayed Tooth Arch**: Large decayed tooth with tall roots — **Swipe DOWN to Slide Under**. Includes guide coins passing directly through the root tunnel.
  - 🕳️ **Broken Gum & Socket**: Compact inflamed gum with deep empty socket — **Swipe UP to Jump Over**. Features a 5-coin parabolic jump arc floating in mid-air.
  - ⚙️ **Giant Rotating Dental Bur**: Electric spinning dental handpiece blocking the lane — **Swipe LEFT/RIGHT to Dodge**. Features a guiding coin curve into open adjacent lanes.
- **Controlled Spawner & Cadence**: Guarantees all three obstacle mechanics appear in every run with steady reaction windows and continuous coin runs.
- **Dental Highway Patrol Inspections (Strict 15s Cadence)**: Officer Floss halts the runner at 15s, 30s, and 45s intervals for high-yield clinical MCQs (+500 reward on pass; wrong answers trigger Round Over).

### 🧭 Round 2: Dental Clinic Maze
- Top-down clinical navigation challenge through clinic corridors to retrieve essential endodontic instruments (Rubber dam, K-files, Apex locator) before time runs out.

### 🧟 Round 3: Zombie Tooth Survival Chase
- High-intensity pursuit mode where necrotic pulp micro-organisms and virulent zombie teeth chase the player through contaminated dentinal tubules.

### 🏥 Round 4: Radiant Smile Clinic RCT Simulation
- **Stage 1: Pulp Chamber Debridement**: NaOCl vs EDTA tissue dissolution and smear layer removal.
- **Stage 2: Apical Danger Zone**: Working length determination, apex locator zero-point calibration, and chemical safety to prevent apical extrusion.
- **Stage 3: Inter-Appointment Vault**: Calcium hydroxide dressing mechanism, alkaline pH antimicrobial action, and final obturation.
- **Clinical Competency Certificate**: Final 4th-Year BDS performance assessment report and graduation badge.

---

## 🌐 Deploying to Vercel (Web / PWA)

This repository includes a standalone web application and PWA inside the [`web/`](./web) directory.

### Quick Steps to Deploy on Vercel:
1. Go to [vercel.com](https://vercel.com) and click **"Add New Project"**.
2. Select and import your **`EndoQuest`** GitHub repository.
3. In the project configuration:
   - **Root Directory**: Click *Edit* and select **`web`**.
   - **Framework Preset**: Leave as *Other* (Standard HTML/JS).
4. Click **Deploy**.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose, Material 3, AndroidX Canvas 2D/3D perspective rendering
- **Architecture**: MVVM with StateFlow / Compose State
- **Audio Engine**: Android SoundPool with synthesized clinical sound effects
- **CI/CD**: GitHub Actions workflow automatically compiles and publishes the latest `EndoQuest.apk` to GitHub Releases on every push.
