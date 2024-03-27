package com.example.Wake_My_PCs

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Wake_My_PCs.SSHManager.executeCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Main()
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun Main(){
    val host = stringResource(id = R.string.host)
    val port = stringResource(id = R.string.port).toInt()
    val user = stringResource(id = R.string.user)
    val pass = stringResource(id = R.string.pass)
    val connected = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit){
        GlobalScope.launch(Dispatchers.IO) {
            var temp: Int
            while(true){
                val answer = async {
                    executeCommand(host, user, pass, port,
                        "echo hello")
                    }
                temp = if(answer.await().contains("Error")) -1
                else 1
                withContext(Dispatchers.Main){
                    connected.intValue = temp
                }
                delay(5000L)
            }
        }
    }

    when (connected.intValue){
        -1 -> Text(text = "Couldn't connect to $host")
        0 -> Text(text = "Please Wait")
        1 -> HomeScreen()
    }
}


@Preview(showBackground = true)
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun HomeScreen(){
    val configuration = LocalConfiguration.current
    val host = stringResource(id = R.string.host)
    val port = stringResource(id = R.string.port).toInt()
    val user = stringResource(id = R.string.user)
    val pass = stringResource(id = R.string.pass)
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val pcs = stringArrayResource(id = R.array.PCs)
    var arp: ArrayList<ArrayList<String>>
    val context = LocalContext.current

    CoroutineScope(Dispatchers.IO).launch { arp = getArpTable(context) }

    Column (
        Modifier
            .size(width = screenWidth, height = screenHeight),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row (
            Modifier
                .height(50.dp)
                .fillMaxWidth()
                .background(color = Color.LightGray),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                modifier = Modifier
                    .heightIn()
                    .offset(x = 5.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                text = "Current WoL Host:")
            Text(
                modifier = Modifier
                    .heightIn()
                    .offset(x = (-30).dp, y = 2.dp),
                    color = Color(0xFF009900),
                text = host)
            Button(
                modifier = Modifier
                    .width(40.dp)
                    .padding(0.dp)
                    .offset(x = (-5).dp),
                onClick = {}) {
                Image(
                    modifier = Modifier
                        .requiredSize(20.dp)
                        .offset(x = (-4).dp),
                    painter = painterResource(id = R.drawable.baseline_settings_24),
                    contentDescription = "Settings")
            }
        }

        Column(
            Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            pcs.forEach {
                val sts = remember { mutableIntStateOf(R.drawable.wait) }
                val name = it.split(" = ")[0]
                val mac = it.split(" = ")[1]
                var update = false
                var ip = ""

                CoroutineScope(Dispatchers.Default).launch {
                    while (arp.size == 0) delay(5000L)
                    run loop@ {
                        arp.forEach {
                            if(mac.uppercase()==it[1]){
                                withContext(Dispatchers.Main){ip = it[1]}
                                return@loop
                            }
                        }
                    }
                    while (true){
                        if(ip!="" && !update) sts.intValue = getStatus(ip,context)
                        delay(5000L)
                    }
                }

                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {

                    Button(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .width(150.dp),
                        onClick = {
                            sts.intValue = R.drawable.wait
                            update = true
                            CoroutineScope(Dispatchers.IO).launch {
                                executeCommand(
                                    host, user, pass, port,
                                "sudo etherwake -i eth0 $mac")
                                delay(30000L)
                                withContext(Dispatchers.Main){update=false}
                            }


                        }
                    ) {
                        Text(
                            text = name,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }

                    Image(
                        painter = painterResource(id = sts.intValue),
                        contentDescription = "status")
                }
            }
            Button(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(150.dp),
                onClick = { /*TODO*/ }) {
                Text(text = "Add PC")
                
            }
        }
    }
}

suspend fun ping(ip:String, context: Context): Int{
    val host = context.resources.getString(R.string.host)
    val port = context.resources.getString(R.string.port).toInt()
    val user = context.resources.getString(R.string.user)
    val pass = context.resources.getString(R.string.pass)
    val ret: Int

    val answer = CoroutineScope(Dispatchers.IO).async {
        executeCommand(host,user,pass,port,
            "ping -c 1 $ip"
        )
    }
    val res = answer.await().split("\n")[4]
    ret = res.split(", ")[1].split(" ")[0].toInt()
    return ret
}

suspend fun getStatus(ip: String, context: Context): Int {
    var ret = 0
    val res = CoroutineScope(Dispatchers.IO).async { ping(ip, context) }
    when (res.await()) {
        1 -> ret = R.drawable.on
        0 -> ret = R.drawable.off
    }
    return ret
}

suspend fun getArpTable(context: Context): ArrayList<ArrayList<String>>{
    val host = context.resources.getString(R.string.host)
    val port = context.resources.getString(R.string.port).toInt()
    val user = context.resources.getString(R.string.user)
    val pass = context.resources.getString(R.string.pass)
    val arp = ArrayList<ArrayList<String>>()

    val answer = CoroutineScope(Dispatchers.IO).async { executeCommand(
        host, user, pass, port,
        "sudo arp-scan -l")
    }

    val res = answer.await()
    run loop@{
        res.split("\n").drop(2).forEach {
            if (it == "") return@loop
            val innerArray = arrayListOf<String>()
            it.split("\t").forEach { subit: String ->
                if (subit != "") innerArray += subit.uppercase()
            }
            arp.add(innerArray)
        }
    }

    return arp
}