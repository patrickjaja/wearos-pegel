# GitHub Workflows

## Release Workflow

The `release.yml` workflow automatically builds and publishes APK releases.

### How to Create a Release

1. **Update version in `app/build.gradle.kts`:**
   ```kotlin
   versionCode = 2
   versionName = "1.1.0"
   ```

2. **Update `CHANGELOG.md`** with release notes following [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) format

3. **Commit changes:**
   ```bash
   git add .
   git commit -m "Release v1.1.0"
   git push
   ```

4. **Create and push a version tag:**
   ```bash
   git tag v1.1.0
   git push origin v1.1.0
   ```

5. **Automated workflow** will:
   - Build release APK
   - Extract changelog notes for this version
   - Create GitHub Release with notes
   - Attach APK file to release
   - Upload build artifacts (retained for 90 days)

### What Gets Built

- **Release APK**: `pegel-{version}.apk` (unsigned by default)
- **Artifact**: Uploaded to GitHub Actions for download

### APK Signing (Optional)

To sign the APK automatically:

1. Add your keystore file to repository secrets
2. Configure signing in workflow (set `if: true` in sign step)
3. Add signing credentials to secrets:
   - `KEYSTORE_FILE` (base64 encoded)
   - `KEYSTORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`

### Release Notes

The workflow automatically extracts release notes from `CHANGELOG.md` for the tagged version and includes them in the GitHub Release.

### Requirements

- Java 17 (Temurin distribution)
- Gradle wrapper included in repository
- Write permissions for GitHub Actions (already configured)
