# MYRAA Ultimate — Background Voice Edition

Features: futuristic UI, voice input/replies, installed-app opening by name, battery status, wake-word activation ("Hey MYRAA"), voice-based SMS, WhatsApp message drafting, alarm/reminder setting, and a user-started visible foreground Voice Mode.

Android requires microphone permission and a visible foreground-service notification for background microphone use. Some Android versions restrict launching activities from the background, so app opening while MYRAA is not foreground can vary by device/system policy.

## New commands
- **Wake word**: say "Hey MYRAA" (or "MYRAA") before any command in background Voice Mode — random background speech no longer triggers actions.
- **SMS**: "SMS bhejo [naam] ko [message]" — looks up the contact and sends directly (needs SEND_SMS + READ_CONTACTS permission).
- **WhatsApp**: "WhatsApp bhejo [naam] ko [message]" — opens WhatsApp with the chat and message pre-filled; you tap Send yourself (Android does not allow apps to auto-send WhatsApp messages).
- **Alarm/Reminder**: "alarm 7 baje lagao" / "shaam 5:30 baje yaad dilana doctor ka appointment" — sets a native alarm via the clock app.

## Notes / limitations
- WhatsApp sending is intentionally manual-tap-to-send; there is no public Android API to auto-send on your behalf.
- Contact name matching is a simple substring match — exact contact names work best.
- Alarm time parsing expects Hindi/Urdu-style phrasing with "baje" (e.g. "7 baje", "shaam 5:30 baje").

Build with Android Studio or a compatible cloud Android workspace.
