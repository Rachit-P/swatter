# Swatter

Swatter helps people who aren't comfortable with phones get rid of the apps that flood an Android phone with pop-up ads and slow it down. Think of a phone that someone else (often kids) has filled with games and junk apps.

## How it works

1. **One-time setup, by a helper** (a relative, employer or phone shop): install Swatter on the owner's phone, allow *Usage access*, and turn on Swatter's accessibility shortcut ("hold both volume keys").
2. **When pop-ups take over**, the owner holds both volume buttons for 3 seconds. Swatter's clean-up screen opens.
3. The screen lists the downloaded apps, **newest first**, with simple warnings:
   - Shows pop-ups over other apps (it has "Display over other apps" permission)
   - Was open in the last 15 minutes (e.g. when the ads appeared)
   - Uses a lot of space
   - Hidden from home screen (installed, but no icon)

   Apps with two or more warnings are shown in red. Each has a big **Remove** button, which opens Android's own uninstall confirmation.

The screen is available in English and Hindi.

## What Android doesn't allow (by design)

- A normal app can't see other apps' memory use or which apps are running, so Swatter uses the signals above instead.
- Uninstalling always needs the owner's tap on Android's confirmation dialog.
- The volume-button shortcut needs Android 11 or newer. On older phones, open Swatter from its icon.

## Project layout

- `core/`: plain Kotlin logic that ranks apps and assigns warnings (unit tested on the JVM).
- `app/`: the Android app (Jetpack Compose).
- `.github/workflows/ci.yml`: builds, runs unit tests, and runs the instrumented tests on Android emulators (API 30 and 34) on every push.

## Building

```
./gradlew :core:test assembleDebug
./gradlew connectedDebugAndroidTest   # with an emulator or device attached
```
