# Translation App

A multi-language translation Android application developed in **Kotlin** using **Google's ML Kit**. The app provides real-time speech and text translation using on-device machine learning models, offering a fast and user-friendly translation experience without the need for a constant internet connection.

## Overview

The Translation App is designed to make communication across languages seamless. It can listen to spoken input, automatically detect the language, translate it into a selected target language, and speak the translation using text-to-speech. Additionally, it leverages the device's camera to detect text in real time and translate it using ML Kit's on-device text recognition and translation APIs.

All translation and recognition tasks are performed using on-device models, which can be downloaded and managed at the user's discretion for offline use.

## Features

- 🎤 **Speech-to-Text Translation**: Listens to spoken input, detects the source language, and translates it to the selected language.  
- 🔊 **Text-to-Speech**: Speaks the translated output using the device’s TTS engine.  
- 📷 **Live Camera Translation**: Uses the device’s camera and ML Kit to detect and translate text from the environment.  
- 📡 **Offline Support**: Language models can be downloaded or removed by the user to enable offline translation.  
- ⚙️ **Smart Language Detection**: Automatically identifies the language being spoken or scanned.

## Tech Stack

- **Language:** Kotlin  
- **Machine Learning:** Google ML Kit (Translate, Language ID, Text Recognition)  
- **Speech APIs:** Android SpeechRecognizer, TextToSpeech  
- **UI:** Android Jetpack, Material Components  
- **Camera:** CameraX (or legacy Camera API if applicable)  

## Screenshots


## Setup & Installation

> ⚠️ This project requires Android Studio and an Android device or emulator with Google Play services enabled.

1. **Clone the Repository**
   ```bash
   git clone https://github.com/AthlonsDev/Translation_App.git
   cd Translation_App

Permissions
The app requires the following permissions:

Microphone access (for speech input)

Camera access (for text recognition)

Internet (for model downloads)

Notes
Language models are downloaded on demand. Users can manage them via app settings or system storage.

All processing is done locally, ensuring fast performance and enhanced privacy.

License
This project is licensed under the MIT License.
