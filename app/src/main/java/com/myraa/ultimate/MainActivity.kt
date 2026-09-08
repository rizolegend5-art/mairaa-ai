package com.myraa.ultimate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity: ComponentActivity(){
 private val perms=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ }
 private lateinit var vm: MyraaViewModel
 private val repeatLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
  if(result.resultCode==Activity.RESULT_OK){
   val heard=result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
   if(!heard.isNullOrBlank())vm.echo(heard)
  }
 }
 override fun onCreate(s:Bundle?){super.onCreate(s);enableEdgeToEdge();perms.launch(arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.POST_NOTIFICATIONS,Manifest.permission.SEND_SMS,Manifest.permission.READ_CONTACTS,Manifest.permission.CALL_PHONE,Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
  setContent{
   vm=viewModel()
   MyraaApp(vm, onRepeatClick={ launchRepeat() })
  }
 }
 private fun launchRepeat(){
  val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{
   putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
   putExtra(RecognizerIntent.EXTRA_PROMPT,"Boliye, Captain repeat karega...")
  }
  try{repeatLauncher.launch(intent)}catch(_:Exception){}
 }
}
