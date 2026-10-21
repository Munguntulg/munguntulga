#!/bin/sh
# Generate assets/voiceover.mp3 with a running VoiceStudio backend
# (desktop app, Docker or server) through its OpenAI-compatible speech API.
#
#   VOICESTUDIO_URL   backend address (default http://127.0.0.1:3900)
#   VOICESTUDIO_VOICE voice profile id/name or OpenAI alias (default "default")
#   OMNIVOICE_API_KEY bearer key, only needed for a remote backend
set -eu

cd "$(dirname "$0")/.."

url="${VOICESTUDIO_URL:-http://127.0.0.1:3900}"
voice="${VOICESTUDIO_VOICE:-default}"
text="All tasks completed. Nice work! And now, your app can read it aloud."

auth=""
if [ -n "${OMNIVOICE_API_KEY:-}" ]; then
  auth="Authorization: Bearer ${OMNIVOICE_API_KEY}"
fi

mkdir -p assets
curl -fsS "$url/v1/audio/speech" \
  -H "Content-Type: application/json" \
  ${auth:+-H "$auth"} \
  -d "{\"model\":\"omnivoice\",\"input\":\"$text\",\"voice\":\"$voice\",\"language\":\"en\",\"response_format\":\"mp3\"}" \
  -o assets/voiceover.mp3

echo "Wrote promo/assets/voiceover.mp3 from $url"
