# My Investment Tracker

Offline Android app to track PPF, SSA, PF, FD, RD, stocks, metals, real estate, and NPS with interest estimates and allocation charts.

## Install from your phone (APK link)

1. Open the latest release: **[Releases](https://github.com/gobisakthi/MyInvestmentTracker/releases/latest)**
2. Download the `MyInvestmentTracker-vX.X.X-debug.apk` file.
3. Open it and install (enable “Install unknown apps” for your browser if prompted).

Direct link (latest, all phones):

`https://github.com/gobisakthi/MyInvestmentTracker/releases/download/v1.3.1/MyInvestmentTracker-v1.3.1-debug.apk`

**Smaller file** if Chrome sticks at 100% (most Android phones, arm64):

`https://github.com/gobisakthi/MyInvestmentTracker/releases/download/v1.3.1/MyInvestmentTracker-v1.3.1-arm64-debug.apk`

If download shows 100% forever: wait 2 minutes, then open **Files → Downloads** and tap the APK, or use **Samsung Internet / Firefox**, or the arm64 link above.

## Develop

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug testDebugUnitTest
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## New release (maintainers)

```bash
git tag v1.0.0   # bump to match app versionName
git push origin v1.0.0
```

GitHub Actions builds the APK and attaches it to the release automatically.
