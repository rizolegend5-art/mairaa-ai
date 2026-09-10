# MYRAA Ultimate BY  RIZO— Background Voice Edition

Features: futuristic UI, voice input/replies, installed-app opening by name, battery status, wake-word activation ("Hey MYRAA"), voice-based SMS, WhatsApp message drafting, alarm/reminder setting, and a user-started visible foreground Voice Mode.

Android requires microphone permission and a visible foreground-service notification for background microphone use. Some Android versions restrict launching activities from the background, so app opening while MYRAA is not foreground can vary by device/system policy.

## New commands
- **Wake word**: say "Hey MYRAA" (or "MYRAA") before any command in background Voice Mode — random background speech no longer triggers actions.
- **SMS**: "SMS bhejo [naam] ko [message]" — looks up the contact and sends directly (needs SEND_SMS + READ_CONTACTS permission).
- **WhatsApp**: "WhatsApp bhejo [naam] ko [message]" — opens WhatsApp with the chat and message pre-filled; you tap Send yourself (Android does not allow apps to auto-send WhatsApp messages).
- **Alarm/Reminder**: "alarm 7 baje lagao" / "shaam 5:30 baje yaad dilana doctor ka appointment" — sets a native alarm via the clock app.
- **Flashlight**: "flashlight on" / "flash band karo"
- **Volume**: "volume badhao", "volume kam karo", "volume mute", "volume full"
- **Brightness**: "brightness badhao" / "brightness kam karo" (needs a one-time "Modify system settings" grant, MYRAA opens the screen automatically the first time)
- **WiFi / Bluetooth**: "WiFi kholo", "Bluetooth kholo" — opens the quick panel (Android blocks apps from silently toggling these since Android 10)
- **Do Not Disturb**: "do not disturb on" / "silent mode off" (needs a one-time Notification Access grant, MYRAA opens the screen automatically the first time)
- **Call**: "call karo [naam]" — looks up the contact and places the call directly (needs CALL_PHONE permission)

MYRAA now addresses the user as "Captain" with a spaceship-AI personality throughout.

## AI Live Chat (Gemini)CAPTAIN now also supports a real, natural conversation mode powered by Google's Gemini Live API — instead of fixed phrases, you can talk to it like a person and it understands, replies with real generated speech, and still executes all the same device commands underneath via the `device_command` tool.

**Setup required before this works:**
1. Get a free Gemini API key from Google AI Studio (aistudio.google.com → "Create API key").
2. In your GitHub repo: Settings → Secrets and variables → Actions → New repository secret → name it `GEMINI_API_KEY` → paste the key value. Never commit the key directly into any file.
3. Rebuild via Actions — the key is injected at build time only, it never appears in source code or the repo history.

**Notes / limitations:**
- This mode uses your phone's mobile data/WiFi and consumes Gemini API quota (free tier has limits) — it is an on-demand "tap to start" conversation, not part of the always-on background wake-word listener, to avoid unexpected data/battery/quota usage.
- Model and protocol details for the Gemini Live API can change on Google's side; if the AI Live Chat button gets stuck on "CONNECTING" or shows an ERROR, screenshot it and it can be adjusted.
- The offline "Hey Captain" wake-word mode still works with zero API key needed, for all the same device commands.

## YouTube Auto-Play (optional upgrade)
By default, "video chalao" opens YouTube search results and you tap the top one. Add a free YouTube Data API v3 key to make CAPTAIN find and **auto-play the exact video directly**, no tap needed.

**Setup:**
1. Go to console.cloud.google.com → create/select a project → "APIs & Services" → "Library" → search "YouTube Data API v3" → Enable.
2. "APIs & Services" → "Credentials" → "Create Credentials" → "API key". Copy it.
3. In your GitHub repo: Settings → Secrets and variables → Actions → New repository secret → name it `YOUTUBE_API_KEY` → paste the key.
4. Rebuild via Actions.

Without this key, video commands still work fine via the search-results fallback — this is purely an optional upgrade.


Build with Android Studio or a compatible cloud Android workspace.
