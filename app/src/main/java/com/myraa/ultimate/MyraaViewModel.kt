package com.myraa.ultimate

import android.app.Application
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class ChatMessage(val text:String,val user:Boolean)
class MyraaViewModel(app:Application):AndroidViewModel(app),TextToSpeech.OnInitListener{
 private val app=app;private val engine=CommandEngine(app);private val tts=TextToSpeech(app,this)
 private val _messages=MutableStateFlow(listOf(ChatMessage("Assalam-o-alaikum! Main MYRAA hoon. Voice Mode on karo aur \"Hey MYRAA\" bol kar shuru karo. Jaise: 'Hey MYRAA, PLAYit kholo', 'Hey MYRAA, SMS bhejo Ali ko main aa raha hoon', 'Hey MYRAA, WhatsApp bhejo Sara ko kal milte hain', 'Hey MYRAA, alarm 7 baje lagao'.",false)));val messages=_messages.asStateFlow()
 private val _status=MutableStateFlow("READY");val status=_status.asStateFlow()
 override fun onInit(s:Int){if(s==TextToSpeech.SUCCESS)tts.language=Locale("ur","PK")}
 fun send(t:String){if(t.isBlank())return;_messages.value+=ChatMessage(t,true);_status.value="THINKING";val a=engine.execute(t);_messages.value+=ChatMessage(a,false);tts.speak(a,TextToSpeech.QUEUE_FLUSH,null,"r");_status.value="READY"}
 fun toggle(on:Boolean){if(on){ContextCompat.startForegroundService(app,Intent(app,BackgroundVoiceService::class.java));_status.value="VOICE MODE ON"}else{app.stopService(Intent(app,BackgroundVoiceService::class.java));_status.value="READY"}}
 override fun onCleared(){tts.shutdown();super.onCleared()}
}
