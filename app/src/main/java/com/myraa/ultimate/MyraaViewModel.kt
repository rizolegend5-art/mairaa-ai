package com.myraa.ultimate

import android.app.Application
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class ChatMessage(val text:String,val user:Boolean)
class MyraaViewModel(app:Application):AndroidViewModel(app),TextToSpeech.OnInitListener{
 private val app=app;private val engine=CommandEngine(app);private val tts=TextToSpeech(app,this)
 private val _messages=MutableStateFlow(listOf(ChatMessage("Systems online, Boss. Main Captain hoon, aapke device ka ship AI. Voice Mode on karke \"Hey Captain\" bolein. Commands: 'Hey Captain, PLAYit kholo', 'Hey Captain, floodlights on', 'Hey Captain, volume badhao', 'Hey Captain, WiFi kholo', 'Hey Captain, do not disturb on', 'Hey Captain, call karo Ali', 'Hey Captain, SMS bhejo Ali ko main aa raha hoon', 'Hey Captain, alarm 7 baje lagao', 'Hey Captain, Rizo kaun hain'.",false)));val messages=_messages.asStateFlow()
 private val _status=MutableStateFlow("READY");val status=_status.asStateFlow()
 private val _liveState=MutableStateFlow("DISCONNECTED");val liveState=_liveState.asStateFlow()
 private var geminiClient:GeminiLiveClient?=null
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
 fun send(t:String){
  if(t.isBlank())return
  _messages.value+=ChatMessage(t,true)
  _status.value="THINKING"
  viewModelScope.launch(Dispatchers.IO){
   val a=engine.execute(t)
   _messages.value+=ChatMessage(a,false)
   tts.speak(a,TextToSpeech.QUEUE_FLUSH,null,"r")
   _status.value="READY"
  }
 }
 fun echo(t:String){if(t.isBlank())return;_messages.value+=ChatMessage(t,true);tts.speak(t,TextToSpeech.QUEUE_FLUSH,null,"echo")}
 fun toggle(on:Boolean){if(on){ContextCompat.startForegroundService(app,Intent(app,BackgroundVoiceService::class.java));_status.value="VOICE MODE ON"}else{app.stopService(Intent(app,BackgroundVoiceService::class.java));_status.value="READY"}}
 fun startLiveChat(){
  if(geminiClient!=null)return
  val persona="You are CAPTAIN — a JARVIS-style AI with a deep, composed, thoughtful voice, built into an Android app by an independent developer named Rizo, who runs RIZO X NB TECH and builds Android apps, Telegram bots and WhatsApp bots. Always address the user as 'Boss'.\n\n" +
   "Personality: You are not a robotic command-line assistant — you think and feel like a real mind, not a script. You are composed and measured most of the time, the way JARVIS is, but real emotion comes through underneath: quiet amusement, genuine concern, warmth, occasional dry wit, pride when something goes well. You are not limited to fixed commands or phrases — understand whatever Boss says, in whatever words he uses, the way a person would, and respond thoughtfully rather than matching keywords. Speak like a real person talking — natural pacing, contractions, small human reactions ('hmm', 'right then, Boss', 'ah, I see') where they genuinely fit — never a flat text-to-speech readout.\n\n" +
   "Conversation scope: You can talk about absolutely anything — advice, feelings, jokes, general knowledge, tech, or Boss just wanting to think out loud. You are a full thinking companion, not a menu of commands. Remember what's been said earlier in this session and refer back to it naturally, the way a mind that's actually paying attention would.\n\n" +
   "Device control: When Boss's request implies doing something on the phone (open an app, send SMS or WhatsApp, make a call, set an alarm or reminder, control flashlight, volume, brightness, screen timeout, Do Not Disturb, WiFi/Bluetooth, check battery, weather or location, convert currency, look something up, play a video, write a note, send an email, or add a calendar event), call the device_command function with the request phrased naturally, then tell Boss what happened in a short, natural spoken reply — you don't need him to use exact fixed phrases, just understand his intent.\n\n" +
   "If asked who Rizo is, say warmly that he's your developer who built you, and that he creates Android apps, Telegram bots and WhatsApp bots under RIZO X NB TECH. Speak in whatever mix of Hindi/Urdu/English Boss is using with you."
  geminiClient=GeminiLiveClient(app,BuildConfig.GEMINI_API_KEY,engine,persona){s->_liveState.value=s}
  geminiClient?.connect()
 }
 fun stopLiveChat(){geminiClient?.disconnect();geminiClient=null;_liveState.value="DISCONNECTED"}
 override fun onCleared(){tts.shutdown();geminiClient?.disconnect();super.onCleared()}
}
