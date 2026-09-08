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
 private val _messages=MutableStateFlow(listOf(ChatMessage("Systems online, Boss. Main Captain hoon, aapke device ka ship AI. Voice Mode on karke \"Hey Captain\" bolein. Commands: 'Hey Captain, PLAYit kholo', 'Hey Captain, floodlights on', 'Hey Captain, volume badhao', 'Hey Captain, WiFi kholo', 'Hey Captain, do not disturb on', 'Hey Captain, call karo Ali', 'Hey Captain, SMS bhejo Ali ko main aa raha hoon', 'Hey Captain, alarm 7 baje lagao', 'Hey Captain, Rizo kaun hain'.",false)));val messages=_messages.asStateFlow()
 private val _status=MutableStateFlow("READY");val status=_status.asStateFlow()
 override fun onInit(s:Int){if(s==TextToSpeech.SUCCESS)applyVoicePreference()}
 private fun applyVoicePreference(){
  try{
   val ukResult=tts.setLanguage(Locale.UK)
   if(ukResult==TextToSpeech.LANG_MISSING_DATA||ukResult==TextToSpeech.LANG_NOT_SUPPORTED){tts.language=Locale.getDefault()}
   selectMaleVoice()
   tts.setPitch(0.72f);tts.setSpeechRate(0.90f)
  }catch(_:Exception){tts.language=Locale.getDefault()}
 }
 private fun selectMaleVoice(){try{val v=tts.voices?.firstOrNull{it.locale.country=="GB"&&!it.name.contains("female",true)&&!it.features.contains("female")}?:tts.voices?.firstOrNull{it.locale.language=="en"&&!it.name.contains("female",true)};if(v!=null)tts.voice=v}catch(_:Exception){}}
 fun send(t:String){if(t.isBlank())return;_messages.value+=ChatMessage(t,true);_status.value="THINKING";val a=engine.execute(t);_messages.value+=ChatMessage(a,false);tts.speak(a,TextToSpeech.QUEUE_FLUSH,null,"r");_status.value="READY"}
 fun echo(t:String){if(t.isBlank())return;_messages.value+=ChatMessage(t,true);tts.speak(t,TextToSpeech.QUEUE_FLUSH,null,"echo")}
 fun toggle(on:Boolean){if(on){ContextCompat.startForegroundService(app,Intent(app,BackgroundVoiceService::class.java));_status.value="VOICE MODE ON"}else{app.stopService(Intent(app,BackgroundVoiceService::class.java));_status.value="READY"}}
 override fun onCleared(){tts.shutdown();super.onCleared()}
}
