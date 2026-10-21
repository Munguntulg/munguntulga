# munguntulga

Android app (Kotlin, Jetpack Compose) in `app/`, plus a HyperFrames promo video in `promo/`.

- Build and unit tests: `./gradlew assembleDebug testDebugUnitTest`
- "Read aloud" talks to a VoiceStudio backend through `app/src/main/java/com/example/task/voice/`. `VoiceStudioClient` is plain JVM code; keep Android imports out of it so its tests run on the host.
- Promo video: `cd promo && npm run check` after every composition edit, `npm run render` to make the MP4. Follow `promo/CLAUDE.md`.

## Agent skills

Engineering skills from mattpocock/skills live in `.claude/skills/` (MIT, see `.claude/skills/LICENSE-mattpocock-skills`). Start with `/ask-matt` if unsure which one fits.

### Issue tracker

Issues live in this repo's GitHub Issues. See `docs/agents/issue-tracker.md`.

### Triage labels

Default vocabulary (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `GLOSSARY.md` and `docs/adr/` at the repo root, created lazily. See `docs/agents/domain.md`.
