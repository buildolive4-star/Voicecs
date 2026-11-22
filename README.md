# SocialVerse (Android User Panel)

Social entertainment, live voice chat rooms, virtual gifts, VIP/SVIP memberships, Family clans, messaging, private voice calls, and mini-games built with Kotlin, Jetpack Compose, Material 3, Firebase Auth, and Cloud Firestore.

---

## 1. Download Built APK from GitHub Actions (No PC Setup Needed)

1. Push this project to GitHub (via AI Studio **Push to GitHub** or `git push`).
2. Open your repository on GitHub and click the **Actions** tab.
3. Select the latest **Build SocialVerse Android APK** workflow run (or click **Run workflow** to trigger manually).
4. Once the build completes, scroll down to **Artifacts** and download **`SocialVerse-Debug-APK`** (contains `app-debug.apk` signed with the project's registered debug SHA-1 so Google Sign-In works immediately).

---

## 2. Clone & Build Locally After Downloading from GitHub

Because `debug.keystore` and `.env` are gitignored, restore them once after cloning:

### Linux / macOS
```bash
base64 -d debug.keystore.base64 > debug.keystore
cp .env.example .env
gradle :app:assembleDebug
```

### Windows (PowerShell)
```powershell
[IO.File]::WriteAllBytes("debug.keystore", [Convert]::FromBase64String((Get-Content -Raw debug.keystore.base64)))
Copy-Item .env.example .env
gradle :app:assembleDebug
```

> **Note:** Use **Gradle 9.3.1** (defined in `gradle/wrapper/gradle-wrapper.properties`) and **JDK 17+**. In Android Studio, open the root folder directly and let Gradle sync; make sure `debug.keystore` is restored in the root folder first so `debugConfig` signing matches the SHA-1 in `app/google-services.json`.

---

## 3. Optional: Signed Release Builds on GitHub

To enable signed Release APK & AAB generation on `workflow_dispatch` or version tags (`v*`), add these repository secrets in **Settings → Secrets and variables → Actions**:
- `KEYSTORE_BASE64` — Base64-encoded `.jks` keystore (`keyAlias`: `upload`)
- `STORE_PASSWORD` — Keystore password
- `KEY_PASSWORD` — Key password
