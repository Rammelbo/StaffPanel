# StaffPanel — Fabric / Minecraft 26.2

This is a **source project plus an automated build workflow**, not a compiled/tested mod JAR.

## Features in this starter
- Press F6 to open the panel
- Enter a player name, reason, and optional duration
- Buttons for warn, kick, ban, pardon/unban, and timeout

## Important limitations
- The mod has not been compiled or tested in a running Minecraft client yet.
- The first version does not automatically populate an online-player list; enter the username manually.
- `/warn` and `/timeout` are not universal vanilla commands. Commands must exist on the server and your account must have permission.
- Use only where you are authorized to moderate.

## Build automatically
The `.github/workflows/build.yml` workflow builds the project with Java 25 and Gradle 9.5.1 and publishes the resulting JAR as a downloadable GitHub Actions artifact.

1. Create a GitHub repository and upload the contents of this folder (not the ZIP file itself).
2. Open the repository's **Actions** tab and run **Build StaffPanel**, or push the files to the `main` branch.
3. If the build succeeds, open that workflow run and download the artifact named `staffpanel-jar`.
4. Extract the artifact and put the JAR in your modpack's `mods` folder. The pack must use Fabric and Minecraft 26.2.

I could not produce the compiled JAR directly in this environment: Gradle is absent and network access to the required build repositories is blocked here. The workflow is provided as the next best route, but it still needs a successful build before the JAR should be considered usable.
