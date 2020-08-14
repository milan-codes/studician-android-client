# Studician - Android client
**_Productivity and time management tool for students🚀_**
![logo](https://imgur.com/B5IyNV5)

## For students, by students.🎓
Studician is a free, open-source android app developed to make students life easier.\
Students (and teachers too!) can easily digitalize their timetable. Users can add their classes, tasks and exams to the app. Furthermore, MyStudiez supports Google Sign-in, so users can synchronize their data across their devices.

## Tech used⚙
The entire application is written in Kotlin. Documentation can be found [here](https://kotlinlang.org/docs/reference/basic-syntax.html).\
Using [Firebase Realtime Database](https://firebase.google.com/docs/database) makes it possible for users to synchronize their data across their devices.\
Minimum API-level is set to 23, so contributors need to have either an emulator or a physical device running Android 6 or higher.

## Contribution🏗
If you want to contribute to this project, please read the [Contribution guidline](/CONTRIBUTING.md)!

## Installation💿
I recommend using **[Android Studio](https://developer.android.com/studio)** for development.\
Fork the repository and open it in your preferred code editor!

### Supported devices
Contributors need to have a physical android device (In this case: Turn on `Developer options` and `USB debugging`) or emulator running Android 6 or higher.

### Firebase
In order to run this application you must have a Firebase project set up.\
If you are logged in to your Google account, you can easily add a new project in [Firebase Console](https://firebase.google.com/).
Once you've done that, you have to download **`google-services.json`** (In Firebase Console: Project Settings -> General -> Download latest config file) and put it under `MyStudiez\app\` (project-level dictionary), so the path to the file would look like this: `MyStudiez\app\google-services.json`.
After that, you're ready to develop!
