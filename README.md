# My Investment Tracker

Offline Android app to track PPF, SSA, PF, FD, RD, stocks, metals, real estate, and NPS with interest estimates and allocation charts.

## Install from your phone (APK link)

1. Open the latest release: **[Releases](https://github.com/gobisakthi/MyInvestmentTracker/releases/latest)**
2. Download the `MyInvestmentTracker-vX.X.X-debug.apk` file.
3. Open it and install (enable “Install unknown apps” for your browser if prompted).

Direct link pattern (replace tag with the latest version):

`https://github.com/gobisakthi/MyInvestmentTracker/releases/download/v1.2.1/MyInvestmentTracker-v1.2.1-debug.apk`

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
