<p align="center">
  <img src="Homescreen.png" alt="Booknest home screen" width="280" />
</p>

# 📚 Booknest

> Community book-box app for Android — find little free libraries around you, share books with neighbours, and keep track of what you've read.

Built with **Jetpack Compose**, **Firebase** and **Google Maps**.

---

## ✨ Features

- 🗺️ **Map of nearby libraries** — discover little free libraries around you on a Google Map.
- ➕ **Add a library** — mark a new book box with a photo, name and size.
- 📖 **Books in a library** — browse books available in each library.
- 📷 **Add book by ISBN scan** — scan a barcode with your camera (ML Kit) or search by title / author via the Google Books API.
- 💬 **Reviews & likes** — leave a comment on a book and like others'.
- 👤 **Profile** — saved books, books you've added, libraries you've added.
- 🕑 **History** — libraries you've visited and books you've contributed.
- 🔐 **Authentication** — email / password or Google Sign-In (Firebase Auth).
- 🌐 **Languages** — English and Czech.
- 📴 **Offline-first** — local Room cache with background sync when you're back online.

---

## 🧱 Tech stack

| Layer            | Libraries |
| ---------------- | --------- |
| UI               | Jetpack Compose, Material 3, Navigation Compose, Coil |
| Architecture     | MVVM, Hilt (DI), Kotlin Coroutines & Flow |
| Local storage    | Room, DataStore Preferences |
| Networking       | Retrofit, Moshi, OkHttp, Kotlinx Serialization |
| Backend          | Firebase Auth, Firestore, Storage |
| Maps & location  | Google Maps Compose, Play Services Location |
| Camera / scanner | CameraX, ML Kit Barcode Scanning, ML Kit Text Recognition |
| Testing          | JUnit, Espresso, Compose UI Test, Hilt Testing |

**Min SDK:** 28 · **Target / Compile SDK:** 36 · **Java / Kotlin target:** 11

---

## 🗂️ Project structure

```
app/src/main/java/git/pef/mendelu/cz/booknest/
├── communication/      # Retrofit API + remote repositories (Google Books)
├── database/           # Room DB, DAOs, entities, local repositories
├── datastore/          # DataStore preferences wrapper
├── di/                 # Hilt modules (API, DB, DataStore, Retrofit, Repos)
├── navigation/         # NavGraph + navigation router
├── sync/               # NetworkMonitor, OfflineSyncManager, LocalImageStore
└── ui/
    ├── activities/     # Splash, AppIntro, Main, Login, Register
    ├── auth/           # Firebase auth VM + screens
    ├── components/     # Shared composables (BaseScreen, …)
    └── screens/
        ├── MapScreen/         # Map, bottom sheet, library books, ISBN scanner
        ├── HistoryScreen/     # Visited libraries + books added tabs
        ├── ProfileScreen/     # Saved books, added books, added libraries
        └── SettingsScreen/    # Language, change profile info
```

---

## 🚀 Getting started

### 1. Prerequisites

- **Android Studio** (latest stable)
- **JDK 11**
- A Firebase project (Auth + Firestore + Storage enabled)
- A Google Maps API key

### 2. Clone

```bash
git clone https://github.com/Fietstopa/booknest-app.git
cd booknest-app
```

### 3. Configure `local.properties`

Android Studio generates this file automatically. Add the Google Books server URL:

```properties
server="https://www.googleapis.com/books/v1/"
```

### 4. Firebase

Drop your own `google-services.json` into `app/`.

### 5. Google Maps key

Replace the value of `google_maps_key` in `app/src/main/res/values/strings.xml` with your own key (and restrict it by Android package name + SHA-1 in the Google Cloud Console).

### 6. Run

```bash
./gradlew installDebug
```

…or just hit ▶️ in Android Studio.

---

## 🧪 Tests

```bash
./gradlew test                  # unit tests
./gradlew connectedAndroidTest  # instrumented tests (device/emulator required)
```

Instrumented tests use a **custom Hilt test runner** (`HiltTestRunner`) so DI works end-to-end in UI tests.

---

## 🔐 Permissions

| Permission | Why |
| ---------- | --- |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Show your position on the map & find nearby libraries |
| `CAMERA` | Scan book ISBN barcodes |

---

## 📝 License

This is a student / hobby project — see repository for details.
