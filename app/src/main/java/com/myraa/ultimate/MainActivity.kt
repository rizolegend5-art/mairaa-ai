package com.myraa.ultimate

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity: ComponentActivity(){
 private val perms=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ }
 override fun onCreate(s:Bundle?){super.onCreate(s);enableEdgeToEdge();perms.launch(arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.POST_NOTIFICATIONS,Manifest.permission.SEND_SMS,Manifest.permission.READ_CONTACTS));setContent{MyraaApp(viewModel())}}
}
