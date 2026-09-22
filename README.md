# 🦷 EndoQuest: The Root Canal Run

An Interactive 3D Educational Endodontic Adventure for Dental Professionals and Students.

[![Vercel Deployment](https://img.shields.io/badge/Deploy-Vercel-black?style=flat&logo=vercel)](https://vercel.com)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20(PWA)%20%7C%20Web-00E5FF)](https://github.com/zbr-khn/EndoQuest)

---

## 📖 Overview

**EndoQuest** bridges clinical dentistry and arcade gaming:
1. **Cinematic Opening Story (8 Scenes)**: Follow Alex experiencing acute pulpitis pain, consulting Dr. Smile, and learning about the $5,000 Root Canal Treatment (RCT).
2. **3D Perspective Tooth Runner**: Sprint down the 3-lane dental highway, collecting $25 gold coins, dodging dental instrument trays and lamps.
3. **Dental Highway Patrol (4th-Year BDS Question Bank)**: Encounter Officer Floss at $500 milestones and answer curriculum-based Endodontics MCQs with dynamic option shuffling and retry logic.
4. **Celebration & Clinic Transition**: Reach the $5,000 RCT goal and celebrate with Dr. Smile before entering Radiant Smile Clinic.
5. **3-Stage Clinical Endodontic Simulation**:
   - **Stage 1**: The Pulp Chamber (Tissue & Smear Layer — EDTA vs NaOCl debridement slider).
   - **Stage 2**: The Apical Danger Zone (Chemical safety, Chlorhexidine/PCA prevention, and electronic apex locator stop).
   - **Stage 3**: The Inter-Appointment Vault (Calcium hydroxide alkaline pH mechanism and intracanal dressing).
6. **Clinical Results**: Receive a 4th-Year BDS Assessment Grade and Competency Certificate.

---

## 🌐 Deploying to Vercel (Web / PWA)

This repository includes a standalone, zero-dependency web application and PWA inside the [`web/`](./web) directory.

### Quick Steps to Deploy on Vercel:
1. Go to [vercel.com](https://vercel.com) and click **"Add New Project"**.
2. Select and import your **`EndoQuest`** GitHub repository.
3. In the project configuration:
   - **Root Directory**: Click *Edit* and select **`web`**.
   - **Framework Preset**: Leave as *Other* (Standard HTML/JS).
4. Click **Deploy**.
5. Within 30 seconds, your game will be live with a free SSL custom domain!

### Installing on iPhone & Android (PWA):
- **iOS (Safari)**: Open your Vercel URL in Safari $\rightarrow$ Tap the **Share** button $\rightarrow$ Select **"Add to Home Screen"**.
- **Android (Chrome)**: Tap the menu $\rightarrow$ Select **"Install App"** or **"Add to Home screen"**.

---

## 📱 Android Native App

The native Android app is built using **Kotlin**, **Jetpack Compose**, **Material 3**, **Canvas Graphics**, and **Coroutines**.

### Build & Run:
- Open the root folder in **Android Studio**.
- Sync Gradle and run on an Android Device or Emulator (API 26+).
