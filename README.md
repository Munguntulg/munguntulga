# munguntulga

A small Jetpack Compose app ("All task completed / Nice work!") extended with three open-source projects:

| Project | License | What it adds here |
|---|---|---|
| [VoiceStudio](https://github.com/debpalash/VoiceStudio) | AGPL-3.0 | **Read aloud** button in the app, narrated by your local VoiceStudio (cloned/designed voices, 646 languages incl. Mongolian). Called over HTTP only; no VoiceStudio code is copied in. |
| [HyperFrames](https://github.com/heygen-com/hyperframes) | Apache-2.0 | `promo/`: an HTML-to-MP4 promo video of the app, narrated by VoiceStudio. |
| [mattpocock/skills](https://github.com/mattpocock/skills) | MIT | `.claude/skills/`: 27 engineering skills for Claude Code (`/grill-with-docs`, `/tdd`, `/diagnosing-bugs`, `/improve-codebase-architecture`, `/to-spec`, `/implement`, ...). |

## 1. Read aloud (VoiceStudio)

1. Install and open VoiceStudio on your computer (`curl -fsSL https://voicestudio.sh/install | sh`, or the Releases page). It serves on port 3900.
2. Run the app on the emulator. It reaches the host at `http://10.0.2.2:3900` by default.
3. Tap **Read aloud**. If VoiceStudio is offline the app falls back to the phone's built-in voice and says so.

Optional settings in `~/.gradle/gradle.properties` (keep keys out of git):

```properties
voicestudio.url=http://10.0.2.2:3900
voicestudio.voice=default        # a voice profile id or name from VoiceStudio
voicestudio.apiKey=              # only for a remote/protected backend
```

A physical phone needs VoiceStudio's network sharing turned on and either `https://` or your computer's LAN address added to `app/src/main/res/xml/network_security_config.xml`.

## 2. Promo video (HyperFrames)

Needs Node.js 22+ and FFmpeg.

```sh
cd promo
VOICESTUDIO_URL=http://127.0.0.1:3900 ./scripts/voiceover.sh   # replace the silent placeholder narration
npm run check      # lint + runtime + layout + contrast
npm run dev        # live preview in the browser
npm run render     # writes renders/*.mp4
```

## 3. Agent skills (Claude Code)

The Matt Pocock skills load automatically from `.claude/skills/` when you open this repo in Claude Code. Repo conventions they read are in `CLAUDE.md` and `docs/agents/`. The `triage` skill expects the five labels in `docs/agents/triage-labels.md`; create them once with `gh label create needs-triage` (and so on) if you plan to use it.

To also get the HyperFrames skills and VoiceStudio's MCP tools in Claude Code:

```sh
claude plugin marketplace add heygen-com/hyperframes
claude plugin install hyperframes@hyperframes
claude mcp add --transport http voicestudio http://127.0.0.1:3900/mcp/   # while VoiceStudio is running
npx skills add debpalash/VoiceStudio                                     # optional VoiceStudio skill
```

## Монгол товч

- **Read aloud** товч: апп таны компьютер дээрх VoiceStudio-оор (монгол хэл дэмждэг) текстийг уншина; VoiceStudio асаагүй бол утасны дуу хоолойгоор уншина.
- `promo/`: HyperFrames ашиглан аппын танилцуулга видео (MP4) хийнэ.
- `.claude/skills/`: Matt Pocock-ийн инженерийн skill-үүд Claude Code-д автоматаар ачаалагдана.
