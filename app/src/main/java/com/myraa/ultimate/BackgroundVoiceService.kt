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
   val words=lower.split(Regex("\\s+"))
   val wakeIndex=words.indexOfFirst{w->listOf("captain","capten","kaptan","kaptaan","kaption","capitan","kaptin").any{target->levenshtein(w,target)<=1}}
   if(wakeIndex>=0){
    val command=words.drop(wakeIndex+1).joinToString(" ").trim()
    val toRun=if(command.isBlank())heard else command
    tts.speak(engine.execute(toRun),TextToSpeech.QUEUE_FLUSH,null,"m")
   }
  };restart()}
  override fun onPartialResults(p:Bundle?){};override fun onEvent(t:Int,p:Bundle?){}
 });listen()}
 private fun listen(){try{sr.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)})}catch(_:Exception){}}
 private fun levenshtein(a:String,b:String):Int{
  val dp=Array(a.length+1){IntArray(b.length+1)}
  for(i in 0..a.length)dp[i][0]=i
  for(j in 0..b.length)dp[0][j]=j
  for(i in 1..a.length)for(j in 1..b.length){
   val cost=if(a[i-1]==b[j-1])0 else 1
   dp[i][j]=minOf(dp[i-1][j]+1,dp[i][j-1]+1,dp[i-1][j-1]+cost)
  }
  return dp[a.length][b.length]
 }
 private fun restart(){android.os.Handler(mainLooper).postDelayed({if(active)listen()},700)}
 override fun onInit(s:Int){if(s==TextToSpeech.SUCCESS){applyVoicePreference();tts.speak("At your service, Boss. Captain systems online.",TextToSpeech.QUEUE_FLUSH,null,"ready")}}
 private fun applyVoicePreference(){
  try{
   val ukResult=tts.setLanguage(Locale.UK)
   if(ukResult==TextToSpeech.LANG_MISSING_DATA||ukResult==TextToSpeech.LANG_NOT_SUPPORTED){tts.language=Locale.getDefault()}
   selectMaleVoice()
   tts.setPitch(0.72f);tts.setSpeechRate(0.90f)
  }catch(_:Exception){tts.language=Locale.getDefault()}
 }
 private fun selectMaleVoice(){try{val v=tts.voices?.firstOrNull{it.locale.country=="GB"&&!it.name.contains("female",true)&&!it.features.contains("female")}?:tts.voices?.firstOrNull{it.locale.language=="en"&&!it.name.contains("female",true)};if(v!=null)tts.voice=v}catch(_:Exception){}}
 override fun onStartCommand(i:Intent?,f:Int,id:Int)=START_NOT_STICKY
 override fun onBind(i:Intent?):IBinder?=null
 override fun onDestroy(){active=false;sr.destroy();tts.stop();tts.shutdown();super.onDestroy()}
 private fun channel(){(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(NotificationChannel("myraa_voice","CAPTAIN Voice Mode",NotificationManager.IMPORTANCE_LOW))}
 private fun notice():Notification=NotificationCompat.Builder(this,"myraa_voice").setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentTitle("CAPTAIN Voice Mode").setContentText("Voice listening is active").setOngoing(true).build()
}
