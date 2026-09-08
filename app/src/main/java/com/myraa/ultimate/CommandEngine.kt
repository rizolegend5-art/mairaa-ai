package com.myraa.ultimate

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.SmsManager
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import java.util.Locale

class CommandEngine(private val context: Context) {

    fun execute(raw: String): String {
        val t = raw.lowercase(Locale.getDefault())

        if ("battery" in t || "charge" in t) {
            val b = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return "Boss, power core ${b.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)} percent par hai."
        }

        if ("rizo" in t) {
            return "Rizo mera developer hain, Boss. RIZO X NB TECH ke naam se woh Android apps, Telegram bots aur WhatsApp bots banate hain — bina Android Studio ke, seedha cloud build systems se. Mujhe — CAPTAIN — bhi unhone hi banaya hai. Woh kai Telegram channels bhi khud manage karte hain aur logon ko apne tareeke sikhate hain."
        }

        if ("settings" in t || "setting" in t) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return "Roger Boss, control panel khol rahi hoon."
        }

        flashlightCommand(t)?.let { return it }
        volumeCommand(t)?.let { return it }
        brightnessCommand(t)?.let { return it }
        screenTimeoutCommand(t)?.let { return it }
        mediaCommand(t)?.let { return it }
        locationCommand(t)?.let { return it }
        connectivityCommand(t)?.let { return it }
        dndCommand(t)?.let { return it }
        callCommand(raw, t)?.let { return it }
        alarmCommand(raw, t)?.let { return it }
        whatsappCommand(raw, t)?.let { return it }
        smsCommand(raw, t)?.let { return it }
        videoCommand(raw, t)?.let { return it }
        noteCommand(raw, t)?.let { return it }

        if (listOf("open", "kholo", "khol do", "khol", "chalao").any { t.contains(it) }) {
            val name = raw.replace(Regex("(?i)myraa[, ]*"), "")
                .replace(Regex("(?i)open|kholo|khol do|khol|chalao"), "").trim()
            if (name.isBlank()) return "Boss, kaunsa module load karun?"
            val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val apps = context.packageManager.queryIntentActivities(i, 0)
                .map { it.loadLabel(context.packageManager).toString() to it.activityInfo.packageName }
            val match = apps.firstOrNull {
                it.first.equals(name, true) || it.first.contains(name, true) || name.contains(it.first, true)
            }
            if (match == null) return "Boss, $name naam ka module scan mein nahi mila."
            val launch = context.packageManager.getLaunchIntentForPackage(match.second)
                ?: return "${match.first} launch nahi ho saka, Boss."
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
            return "Roger Boss, ${match.first} online kar rahi hoon."
        }

        return "Boss, main flashlight, volume, brightness, screen timeout, music control, location, video search, note dictation, WiFi/Bluetooth, Do Not Disturb, call, SMS, WhatsApp, alarm aur app launch — sab control kar sakti hoon."
    }

    // ---------- Flashlight ----------
    private fun flashlightCommand(t: String): String? {
        val mentioned = listOf("flash", "torch", "flashlight").any { t.contains(it) }
        if (!mentioned) return null
        val turnOff = listOf("band", "off", "bujha").any { t.contains(it) }
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull {
                cameraManager.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return "Boss, is device mein floodlight hardware detect nahi hua."
            cameraManager.setTorchMode(cameraId, !turnOff)
            if (turnOff) "Boss, floodlights band kar diye." else "Roger Boss, floodlights on."
        } catch (e: Exception) {
            "Boss, floodlight control fail ho gaya."
        }
    }

    // ---------- Volume ----------
    private fun volumeCommand(t: String): String? {
        if (!(t.contains("volume") || t.contains("awaaz"))) return null
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when {
            t.contains("mute") -> {
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
                "Boss, audio channels mute kar diye."
            }
            t.contains("kam") || t.contains("down") || t.contains("halki") -> {
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                "Boss, volume kam kar diya."
            }
            t.contains("full") || t.contains("max") -> {
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                am.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_SHOW_UI)
                "Boss, audio full power par hai."
            }
            t.contains("zyada") || t.contains("tez") || t.contains("up") || t.contains("badhao") -> {
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                "Boss, volume badha diya."
            }
            else -> "Boss, bolein: volume badhao, volume kam karo, volume mute ya volume full."
        }
    }

    // ---------- Brightness ----------
    private fun brightnessCommand(t: String): String? {
        if (!(t.contains("brightness") || t.contains("roshni"))) return null
        if (!Settings.System.canWrite(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return "Boss, brightness control ke liye ek baar 'Modify system settings' allow karna hoga — screen khol di hai."
        }
        return try {
            val resolver = context.contentResolver
            val current = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 125)
            val newValue = when {
                t.contains("full") || t.contains("max") -> 255
                t.contains("kam se kam") || t.contains("min") -> 10
                t.contains("kam") || t.contains("down") -> (current - 60).coerceAtLeast(10)
                t.contains("zyada") || t.contains("tez") || t.contains("up") || t.contains("badhao") -> (current + 60).coerceAtMost(255)
                else -> current
            }
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, newValue)
            "Boss, display brightness adjust kar di."
        } catch (e: Exception) {
            "Boss, brightness control fail ho gaya."
        }
    }

    // ---------- WiFi / Bluetooth ----------
    private fun connectivityCommand(t: String): String? {
        if (t.contains("wifi")) {
            return try {
                context.startActivity(Intent(Settings.Panel.ACTION_WIFI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                "Boss, WiFi control panel khol diya — Android policy directly switch nahi karne deti, ek tap mein on/off kar dijiye."
            } catch (e: Exception) {
                "Boss, WiFi panel open nahi ho saka."
            }
        }
        if (t.contains("bluetooth")) {
            return try {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                "Boss, Bluetooth settings khol di."
            } catch (e: Exception) {
                "Boss, Bluetooth settings open nahi ho saki."
            }
        }
        return null
    }

    // ---------- Do Not Disturb ----------
    private fun dndCommand(t: String): String? {
        val mentioned = t.contains("do not disturb") || t.contains("dnd") || t.contains("silent mode")
        if (!mentioned) return null
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return "Boss, silent-running mode ke liye ek baar permission dena hoga — screen khol di hai, CAPTAIN ko allow kar dijiye."
        }
        val turnOff = listOf("band", "off", "hatao").any { t.contains(it) }
        nm.setInterruptionFilter(
            if (turnOff) NotificationManager.INTERRUPTION_FILTER_ALL
            else NotificationManager.INTERRUPTION_FILTER_PRIORITY
        )
        return if (turnOff) "Boss, Do Not Disturb band kar diya." else "Silent-running mode engaged, Boss."
    }

    // ---------- Call ----------
    private fun callCommand(raw: String, t: String): String? {
        val mentioned = t.contains("call karo") || t.contains("call laga") || t.contains("phone laga") || (t.contains("call") && t.contains(" ko"))
        if (!mentioned) return null
        val m = Regex("(?i)(?:call|phone)\\s+(?:laga(?:o|iye)?\\s+)?(.+?)(?:\\s+ko)?(?:\\s+call\\s+karo)?$").find(raw)
            ?: return "Boss, kise call karun?"
        val name = m.groupValues[1].replace(Regex("(?i)ko|karo|laga"), "").trim()
        if (name.isBlank()) return "Boss, kise call karun?"
        val number = findContactNumber(name) ?: return "Boss, $name naam ka contact log mein nahi mila."
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return "Boss, call lagane ke liye phone permission nahi di gayi."
        }
        return try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            "Roger Boss, $name ko connect kar rahi hoon."
        } catch (e: Exception) {
            "Boss, call channel open nahi ho saka."
        }
    }

    // ---------- Media Control ----------
    private fun mediaCommand(t: String): String? {
        val mentioned = listOf("music", "gaana", "gana", "song", "play kar", "pause kar", "next kar", "skip kar").any { t.contains(it) }
        if (!mentioned) return null
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val keyCode = when {
            t.contains("pause") || t.contains("rok") || t.contains("stop") -> KeyEvent.KEYCODE_MEDIA_PAUSE
            t.contains("next") || t.contains("agla") || t.contains("skip") -> KeyEvent.KEYCODE_MEDIA_NEXT
            t.contains("previous") || t.contains("pichla") || t.contains("peeche") -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            else -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        }
        return try {
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            "Roger Boss, media control signal bhej diya."
        } catch (e: Exception) {
            "Boss, media control fail ho gaya — pehle koi music app khol lijiye."
        }
    }

    // ---------- Location ----------
    private fun locationCommand(t: String): String? {
        val mentioned = listOf("location", "kahan hoon", "kaha hoon", "kaha hun", "mera location").any { t.contains(it) }
        if (!mentioned) return null
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return "Boss, location dekhne ke liye permission nahi di gayi."
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var best: android.location.Location? = null
            for (p in lm.getProviders(true)) {
                val l = lm.getLastKnownLocation(p) ?: continue
                if (best == null || l.accuracy < best!!.accuracy) best = l
            }
            if (best == null) "Boss, abhi GPS lock nahi mila, thodi der baad try karein."
            else "Boss, coordinates hain latitude %.4f, longitude %.4f.".format(best!!.latitude, best!!.longitude)
        } catch (e: Exception) {
            "Boss, location retrieve nahi ho saki."
        }
    }

    // ---------- Screen Timeout ----------
    private fun screenTimeoutCommand(t: String): String? {
        if (!t.contains("screen timeout") && !t.contains("screen off time")) return null
        if (!Settings.System.canWrite(context)) {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return "Boss, iske liye ek baar 'Modify system settings' allow karna hoga — screen khol di hai."
        }
        val millis = when {
            t.contains("30 second") -> 30000
            t.contains("2 minute") -> 120000
            t.contains("5 minute") -> 300000
            t.contains("10 minute") -> 600000
            t.contains("kabhi nahi") || t.contains("never") -> Int.MAX_VALUE
            else -> 60000
        }
        return try {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, millis)
            "Boss, screen timeout adjust kar diya."
        } catch (e: Exception) {
            "Boss, screen timeout set nahi ho saka."
        }
    }

    // ---------- Video Search & Play ----------
    private fun videoCommand(raw: String, t: String): String? {
        val triggers = listOf("video chalao", "video play", "youtube par", "video dikhao", "gaana chalao youtube")
        if (!triggers.any { t.contains(it) }) return null
        val query = raw.replace(Regex("(?i)video chalao|video play karo|video play|youtube par chalao|youtube par|video dikhao|gaana chalao"), "").trim()
        if (query.isBlank()) return "Boss, kaunsa video chalaun?"
        return try {
            val uri = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            "Roger Boss, '$query' YouTube par search kar diya — top result tap kar dijiye, play ho jayega."
        } catch (e: Exception) {
            "Boss, video search open nahi ho saka."
        }
    }

    // ---------- Note / Dictation ----------
    private fun noteCommand(raw: String, t: String): String? {
        val triggers = listOf("note likho", "likh do", "type karo", "note banao")
        if (!triggers.any { t.contains(it) }) return null
        val content = raw.replace(Regex("(?i)note likho|likh do|type karo|note banao"), "").trim()
        if (content.isBlank()) return "Boss, kya likhun?"
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Captain note", content))
            "Boss, likh diya aur clipboard mein copy bhi kar diya — jahan chahe paste kar dijiye."
        } catch (e: Exception) {
            "Boss, note save nahi ho saka."
        }
    }

    // ---------- Alarm / Reminder ----------
    private fun alarmCommand(raw: String, t: String): String? {
        val isAlarm = listOf("alarm", "yaad dila", "reminder").any { t.contains(it) }
        if (!isAlarm) return null

        val time = parseTime(t)
            ?: return "Boss, time samajh nahi aaya, jaise 'alarm 7 baje' ya 'shaam 5:30 baje alarm'."
        val (hour, minute) = time

        val label = Regex("(?i)(?:yaad dila(?:o|na)?|reminder)\\s+(?:ke liye\\s+)?(.+?)(?:\\s+\\d.*)?$")
            .find(raw)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }

        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                if (label != null) putExtra(AlarmClock.EXTRA_MESSAGE, label)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val h12 = if (hour % 12 == 0) 12 else hour % 12
            "Roger Boss, alarm ${h12}:${minute.toString().padStart(2, '0')} ${if (hour < 12) "subah" else "shaam"} ke liye set kar diya."
        } catch (e: Exception) {
            "Boss, alarm module nahi mila, khud se laga lijiye."
        }
    }

    private fun parseTime(t: String): Pair<Int, Int>? {
        val m = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*baje").find(t) ?: return null
        var hour = m.groupValues[1].toIntOrNull() ?: return null
        val minute = m.groupValues[2].toIntOrNull() ?: 0
        val isPm = listOf("shaam", "raat", "dopahar").any { t.contains(it) }
        val isAm = listOf("subah", "savere").any { t.contains(it) }
        if (isPm && hour < 12) hour += 12
        if (isAm && hour == 12) hour = 0
        return hour.coerceIn(0, 23) to minute.coerceIn(0, 59)
    }

    // ---------- WhatsApp ----------
    private fun whatsappCommand(raw: String, t: String): String? {
        if ("whatsapp" !in t) return null
        val m = Regex("(?i)whatsapp\\s+(?:bhejo|send|par)\\s+(.+?)\\s+ko\\s+(.+)").find(raw)
            ?: return "Boss, bolein: 'WhatsApp bhejo [naam] ko [message]'."
        val name = m.groupValues[1].trim()
        val message = m.groupValues[2].trim()
        val number = findContactNumber(name) ?: return "Boss, $name naam ka contact nahi mila."
        return try {
            val cleanNumber = number.filter { it.isDigit() || it == '+' }
            val uri = Uri.parse("https://wa.me/$cleanNumber?text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Boss, $name ke liye WhatsApp channel khol diya, message ready hai — Send aap khud dabaiye."
        } catch (e: Exception) {
            "Boss, WhatsApp install nahi hai ya open nahi ho saka."
        }
    }

    // ---------- SMS ----------
    private fun smsCommand(raw: String, t: String): String? {
        if ("whatsapp" in t) return null
        if (!(t.contains("sms") || (t.contains("message") && t.contains("bhejo")))) return null
        val m = Regex("(?i)(?:sms|message)\\s+bhejo\\s+(.+?)\\s+ko\\s+(.+)").find(raw)
            ?: return "Boss, bolein: 'SMS bhejo [naam] ko [message]'."
        val name = m.groupValues[1].trim()
        val message = m.groupValues[2].trim()
        val number = findContactNumber(name) ?: return "Boss, $name naam ka contact nahi mila."

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return "Boss, SMS bhejne ke liye permission nahi di gayi hai."
        }
        return try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                context.getSystemService(SmsManager::class.java)
            else
                @Suppress("DEPRECATION") SmsManager.getDefault()
            smsManager.sendTextMessage(number, null, message, null, null)
            "Roger Boss, $name ko signal bhej diya."
        } catch (e: Exception) {
            "Boss, SMS bhejne mein masla hua."
        }
    }

    private fun findContactNumber(name: String): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
            null, null, null
        )
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val cName = it.getString(nameIdx) ?: continue
                if (cName.contains(name, true) || name.contains(cName, true)) {
                    return it.getString(numIdx)
                }
            }
        }
        return null
    }
}
