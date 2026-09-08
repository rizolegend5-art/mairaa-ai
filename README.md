# MYRAA Ultimate — Background Voice Edition

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

## Notes / limitations
- WhatsApp sending is intentionally manual-tap-to-send; there is no public Android API to auto-send on your behalf.
- WiFi/Bluetooth cannot be silently toggled on Android 10+ for privacy/security reasons — MYRAA opens the control panel instead, one tap away.
- Contact name matching is a simple substring match — exact contact names work best.
- Alarm time parsing expects Hindi/Urdu-style phrasing with "baje" (e.g. "7 baje", "shaam 5:30 baje").
- True OS-level control (root-style automation of any app, auto-clicking buttons in other apps) is intentionally not implemented — Android's security model does not allow third-party apps this level of access without root, and MYRAA does not attempt to bypass it.

Build with Android Studio or a compatible cloud Android workspace.
