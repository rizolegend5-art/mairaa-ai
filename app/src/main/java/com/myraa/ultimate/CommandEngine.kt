package com.myraa.ultimate

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import java.util.Locale

class CommandEngine(private val context: Context) {

    fun execute(raw: String): String {
        val t = raw.lowercase(Locale.getDefault())

        if ("battery" in t || "charge" in t) {
            val b = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return "Aapki battery ${b.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)} percent hai."
        }

        if ("settings" in t || "setting" in t) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return "Settings open kar rahi hoon."
        }

        alarmCommand(raw, t)?.let { return it }
        whatsappCommand(raw, t)?.let { return it }
        smsCommand(raw, t)?.let { return it }

        if (listOf("open", "kholo", "khol do", "khol", "chalao").any { t.contains(it) }) {
            val name = raw.replace(Regex("(?i)myraa[, ]*"), "")
                .replace(Regex("(?i)open|kholo|khol do|khol|chalao"), "").trim()
            if (name.isBlank()) return "Kaunsi app open karun?"
            val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val apps = context.packageManager.queryIntentActivities(i, 0)
                .map { it.loadLabel(context.packageManager).toString() to it.activityInfo.packageName }
            val match = apps.firstOrNull {
                it.first.equals(name, true) || it.first.contains(name, true) || name.contains(it.first, true)
            }
            if (match == null) return "Mujhe $name naam ki installed app nahi mili."
            val launch = context.packageManager.getLaunchIntentForPackage(match.second)
                ?: return "${match.first} open nahi ho saki."
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
            return "${match.first} open kar rahi hoon."
        }

        return "Main battery, settings, app kholna, SMS bhejna, WhatsApp message aur alarm laga sakti hoon."
    }

    // ---------- Alarm / Reminder ----------
    private fun alarmCommand(raw: String, t: String): String? {
        val isAlarm = listOf("alarm", "yaad dila", "reminder").any { t.contains(it) }
        if (!isAlarm) return null

        val time = parseTime(t)
            ?: return "Alarm ke liye time samajh nahi aaya, jaise 'alarm 7 baje' ya 'shaam 5:30 baje alarm'."
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
            "Theek hai, alarm ${h12}:${minute.toString().padStart(2, '0')} ${if (hour < 12) "subah" else "shaam"} ke liye laga diya."
        } catch (e: Exception) {
            "Alarm app nahi mila, khud se laga lijiye."
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
            ?: return "WhatsApp message ke liye bolein: 'WhatsApp bhejo [naam] ko [message]'."
        val name = m.groupValues[1].trim()
        val message = m.groupValues[2].trim()
        val number = findContactNumber(name) ?: return "$name naam ka contact nahi mila."
        return try {
            val cleanNumber = number.filter { it.isDigit() || it == '+' }
            val uri = Uri.parse("https://wa.me/$cleanNumber?text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "$name ke liye WhatsApp khol diya hai, message likha hua hai, ab aap khud send dabaiye."
        } catch (e: Exception) {
            "WhatsApp install nahi hai ya open nahi ho saka."
        }
    }

    // ---------- SMS ----------
    private fun smsCommand(raw: String, t: String): String? {
        if ("whatsapp" in t) return null
        if (!(t.contains("sms") || (t.contains("message") && t.contains("bhejo")))) return null
        val m = Regex("(?i)(?:sms|message)\\s+bhejo\\s+(.+?)\\s+ko\\s+(.+)").find(raw)
            ?: return "SMS ke liye bolein: 'SMS bhejo [naam] ko [message]'."
        val name = m.groupValues[1].trim()
        val message = m.groupValues[2].trim()
        val number = findContactNumber(name) ?: return "$name naam ka contact nahi mila."

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return "SMS bhejne ke liye permission nahi di gayi hai."
        }
        return try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                context.getSystemService(SmsManager::class.java)
            else
                @Suppress("DEPRECATION") SmsManager.getDefault()
            smsManager.sendTextMessage(number, null, message, null, null)
            "$name ko SMS bhej diya."
        } catch (e: Exception) {
            "SMS bhejne mein masla hua."
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
