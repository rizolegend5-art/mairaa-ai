package com.myraa.ultimate

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val dark=Color(0xFF050913);private val cyan=Color(0xFF37D8FF);private val violet=Color(0xFF8B5CFF)
@Composable fun MyraaApp(vm:MyraaViewModel){val msgs by vm.messages.collectAsStateWithLifecycle();val status by vm.status.collectAsStateWithLifecycle();var text by remember{mutableStateOf("")};var on by remember{mutableStateOf(false)}
 MaterialTheme(colorScheme=darkColorScheme(primary=cyan,secondary=violet,background=dark)){Scaffold(containerColor=dark){pad->Column(Modifier.fillMaxSize().padding(pad).background(Brush.verticalGradient(listOf(dark,Color(0xFF0A1730),dark))).padding(16.dp)){
  Text("MYRAA",color=cyan,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text("ULTIMATE VOICE ASSISTANT • $status",color=Color(0xFF9FB0C6));Orb(status)
  Button(onClick={on=!on;vm.toggle(on)},modifier=Modifier.fillMaxWidth()){Text(if(on)"VOICE MODE ON • TAP TO STOP" else "START BACKGROUND VOICE MODE")};Spacer(Modifier.height(8.dp));Text("Command example: “MYRAA PLAYit kholo”",color=Color(0xFF9FB0C6),style=MaterialTheme.typography.bodySmall)
  LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(vertical=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){items(msgs){m->Row(Modifier.fillMaxWidth(),horizontalArrangement=if(m.user)Arrangement.End else Arrangement.Start){Surface(shape=RoundedCornerShape(18.dp),color=if(m.user)Color(0xFF17375A)else Color(0xFF101827),modifier=Modifier.widthIn(max=340.dp)){Text(m.text,Modifier.padding(14.dp),color=Color.White)}}}}
  Row(verticalAlignment=Alignment.CenterVertically){OutlinedTextField(text,{text=it},Modifier.weight(1f),label={Text("MYRAA se baat karein")},singleLine=true);Spacer(Modifier.width(8.dp));Button(onClick={vm.send(text);text=""}){Text("SEND")}}
 }}}}
@Composable private fun Orb(status:String){val inf=rememberInfiniteTransition(label="orb");val p by inf.animateFloat(.94f,1.06f,infiniteRepeatable(tween(900),RepeatMode.Reverse),label="p");Box(Modifier.fillMaxWidth().height(185.dp),contentAlignment=Alignment.Center){Surface(Modifier.size(142.dp).scale(p),CircleShape,color=Color.Transparent){Box(Modifier.background(Brush.radialGradient(listOf(cyan,violet,dark))),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text("MYRAA",color=Color.White,fontWeight=FontWeight.ExtraBold);Text(status,color=Color.White)}}}}}
