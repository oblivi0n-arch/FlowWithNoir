# Flow with Noir

A breathing and journaling app for Android. Two colours, no accounts, no network.

> **The app interface is in Polish.** The code, commit history and this document are in English.

## What it does

**Breathing sessions.** Nine techniques across four categories — box breathing, 4-7-8, coherent breathing, physiological sigh and others. The circle on screen is driven by the actual pattern rather than a generic pulse: it grows through the inhale, holds through the hold, and collapses across the exhale. The physiological sigh has its own phase, where a second short inhale pushes the circle past the progress ring. A ring around it tracks how much of the session is left, and the system bars hide while a session runs.

**Journal.** Entries carry a title, a mood, context tags and a note. Written after a session or on their own. Search is permanent, filters cover moods and tags, and a thirty-day strip shows which days have entries alongside a count per mood. Entries can be edited and deleted.

**Daily reminder.** One notification at a chosen hour. If a session was already completed that day, it stays quiet — a reminder to meditate sent to someone who just meditated only teaches people to ignore notifications.

**Onboarding.** Four steps on first launch: a breathing circle, an optional nickname, the time of day you want to practise, and the reminder built from that answer.

## Privacy

The app declares no internet permission. It cannot send anything anywhere, by construction rather than by promise. Entries live in a local Room database and preferences in DataStore, both on the device only. There is no account, no sync and no analytics.

Settings hold a full reset that clears entries, preferences and the scheduled reminder, guarded by typing a confirmation word.

## Built with

Kotlin, Jetpack Compose, Material 3, Navigation Compose, Room, DataStore Preferences and `AlarmManager`. `minSdk 26`, `targetSdk 37`.

## Building

Clone the repository and open it in Android Studio. Debug builds need no extra setup — the signing config is skipped when no keystore is configured, so a fresh clone compiles and runs as is.

### Release builds

Create a keystore, then a `keystore.properties` file in the project root:

```properties
storeFile=flowwithnoir.jks
storePassword=<password>
keyAlias=flowwithnoir
keyPassword=<password>
```

```bash
./gradlew assembleRelease
```

`keystore.properties`, `*.jks` and `*.keystore` are gitignored and must stay out of version control.

## Third-party licences

The dependencies below are used under the Apache License 2.0, which is compatible with GPL-3.0 in this direction. Their copyright and licence notices are preserved.

| Component | Licence |
| --- | --- |
| AndroidX, Jetpack Compose, Room, DataStore | Apache 2.0 |
| Material Symbols icons (`material-icons-extended`) | Apache 2.0 |
| Kotlin standard library and coroutines | Apache 2.0 |

The launcher icon is original work and falls under this project's licence.

## Licence

Released under the **GNU General Public License v3.0**. The full text is in [LICENSE](LICENSE).

In short: you may use, study, modify and share this code, including commercially. If you distribute a modified version, it has to stay open under the same licence. Nobody can take this, close the source and ship it as a proprietary product.

Built by [@oblivi0n-arch](https://github.com/oblivi0n-arch).
