# PDFLNX

PDFLNX is an Android application designed for high-performance PDF processing and Optical Character Recognition (OCR). It extracts and parses text from scanned or digital PDF documents using Tesseract OCR, featuring background processing and a modern responsive interface.

## Features

- **Multi-language OCR Support**:
  - Bengali (`ben`)
  - Arabic (`ara`)
  - English (`eng`)
  - Urdu (`urd`)
- **Background Processing**:
  - `PdfProcessService` handles long-running PDF extraction and OCR tasks in the background without UI interruption.
- **Modern UI**:
  - Clean and responsive HTML5 & Tailwind CSS interface embedded in Android WebView.
- **Tesseract Engine**:
  - Built-in traineddata support for fast, on-device OCR without external API dependencies.

## Architecture

- **Language**: Kotlin & Java
- **Framework**: Android SDK (Gradle-based build system)
- **OCR Engine**: Tesseract Android Tools (`tess-two` / Tesseract OCR)
- **UI**: Embedded WebView with Tailwind CSS

## Project Structure

```
PDFLNX/
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   ├── index.html        # Web UI
│   │   │   ├── tailwind.js       # Tailwind CSS bundle
│   │   │   └── tessdata/         # OCR Trained Data models
│   │   ├── kotlin/com/pdflnx/ahmad/
│   │   │   ├── MainActivity.kt
│   │   │   ├── AppEngine.kt
│   │   │   └── PdfProcessService.kt
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/ahmadhibban/PDFLNX.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build the APK.

## 👤 Author

**Ahmad Hibban**
- GitHub: [@ahmadhibban](https://github.com/ahmadhibban)

## 📄 License

This project is open-source under the Apache License 2.0. Copyright © Ahmad Hibban.
