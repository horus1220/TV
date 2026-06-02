# AGENTS.md

## Project Overview

Android media streaming application based on CatVod, supporting both **TV (leanback)** and **mobile** form factors. Written in Java, not Kotlin.

## Build & Run

```bash
# Build TV version (arm64)
./gradlew assembleLeanbackArm64_v8aRelease

# Build Mobile version
./gradlew assembleMobileArm64_v8aRelease

# Build both flavors
./gradlew assembleRelease
```

APKs output to `app/build/outputs/apk/` with naming: `{mode}-{abi}.apk`

## Project Structure

```
TV/
├── app/            Main application module
│   ├── src/main/     Shared code (both flavors)
│   ├── src/leanback/ TV-specific UI
│   └── src/mobile/   Phone-specific UI
├── catvod/         Spider abstraction layer + OkHttp networking
├── quickjs/        QuickJS JavaScript engine
├── chaquo/         Chaquopy Python engine
```

**Package**: `com.fongmi.android.tv`  
**applicationId**: `com.shan.android.tv`  
**minSdk**: 24 (Android 7.0)  
**Java**: 21

## Flavor Dimensions

- **mode**: `leanback` (TV), `mobile` (phone)
- **abi**: `arm64-v8a`, `armeabi_v7a`

## Key Technologies

- **Player**: ExoPlayer (Media3) + FFmpeg soft decode
- **Networking**: OkHttp 5.3.2
- **Database**: Room
- **Events**: EventBus (annotation processor)
- **Spider engines**: Java JAR, JavaScript (QuickJS), Python (Chaquopy)
- **DLNA**: JUPnP 3.0.4

## Documentation

- `docs/CONFIG.md` - Vod/Live configuration fields
- `docs/SPIDER.md` - Spider API specification
- `docs/LOCAL.md` - Local HTTP API endpoints
- `docs/LIVE.md` - Live source format

## Notes

- No CI/CD workflows configured (only `.github/FUNDING.yml`)
- Local config signing via `local.properties` (not in repo)
- Room schema exported to `app/schemas/`
