package com.myraa.ultimate

import android.app.*
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.speech.*
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import java.util.Locale

class BackgroundVoiceService:Service(),TextToSpeech.OnInitListener{
 private lateinit var sr:SpeechRecognizer; private lateinit var tts:TextToSpeech; private lateinit var engine:CommandEngine; private var active=true
 override fun onCreate(){super.onCreate();engine=CommandEngine(applicationContext);tts=TextToSpeech(this,this);channel();startForeground(101,notice());sr=SpeechRecognizer.createSpeechRecognizer(this);sr.setRecognitionListener(object:RecognitionListener{
  override fun onReadyForSpeech(p:Bundle?){};override fun onBeginningOfSpeech(){};override fun onRmsChanged(v:Float){};override fun onBufferReceived(b:ByteArray?){};override fun onEndOfSpeech(){}
  override fun onError(e:Int){restart()};override fun onResults(r:Bundle?){r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let{heard->
   val lower=heard.lowercase(Locale.getDefault())
   val wakeWords=listOf("hey myraa","myraa","hey myra","myra","hey mira","mira")
   if(wakeWords.any{lower.contains(it)}){
    val command=heard.replace(Regex("(?i)hey\\s+myraa|myraa|hey\\s+myra|myra|hey\\s+mira|mira"),"").trim()
    val toRun=if(command.isBlank())heard else command
    tts.speak(engine.execute(toRun),TextToSpeech.QUEUE_FLUSH,null,"m")
   }
  };restart()}
  override fun onPartialResults(p:Bundle?){};override fun onEvent(t:Int,p:Bundle?){}
 });listen()}
 private fun listen(){try{sr.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ur-PK")})}catch(_:Exception){}}
 private fun restart(){android.os.Handler(mainLooper).postDelayed({if(active)listen()},700)}
 override fun onInit(s:Int){if(s==TextToSpeech.SUCCESS){tts.language=Locale("ur","PK");tts.speak("MYRAA voice mode active hai.",TextToSpeech.QUEUE_FLUSH,null,"ready")}}
 override fun onStartCommand(i:Intent?,f:Int,id:Int)=START_NOT_STICKY
 override fun onBind(i:Intent?):IBinder?=null
 override fun onDestroy(){active=false;sr.destroy();tts.stop();tts.shutdown();super.onDestroy()}
 private fun channel(){(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(NotificationChannel("myraa_voice","MYRAA Voice Mode",NotificationManager.IMPORTANCE_LOW))}
 private fun notice():Notification=NotificationCompat.Builder(this,"myraa_voice").setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentTitle("MYRAA Voice Mode").setContentText("Voice listening is active").setOngoing(true).build()
}
