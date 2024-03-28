package com.example.Wake_My_PCs

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Wake_My_PCs.SSHManager.executeCommand
import com.example.Wake_My_PCs.SuspendFunctions.getArpTable
import com.example.Wake_My_PCs.SuspendFunctions.getCurrentIPv4
import com.example.Wake_My_PCs.SuspendFunctions.getStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.NumberFormatException

//private const val TAG = "MyActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = getSharedPreferences("Settings", MODE_PRIVATE)
        val pcs = getSharedPreferences("PCs", MODE_PRIVATE)

        settings.edit().clear().commit()
        pcs.edit().clear().commit()
        pcs.edit().apply { putString("Soul PC", "04:D9:F5:7C:FB:33");putString("Green PC", "9C:6B:00:30:15:52");commit()}

        var start = 0

        if (settings.all.isEmpty()) start = 2

        setContent {
            Main(settings, pcs, start)
        }
    }
}

@SuppressLint("ApplySharedPref")
@Composable
fun Settings(settings: SharedPreferences, onUpdate: () -> Unit) {
    val editor = settings.edit()
    val initPort = if (settings.getInt("port", -1).toString()=="-1")""
    else settings.getInt("port", -1).toString()
    val host = remember {
        mutableStateOf(settings.getString("host",""))
    }
    val port = remember {
        mutableStateOf(initPort)
    }
    val user = remember {
        mutableStateOf(settings.getString("user",""))
    }
    val pass = remember {
        mutableStateOf(settings.getString("pass",""))
    }
    var visible by rememberSaveable {
        mutableStateOf(false)
    }


    Column(
        Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            text = "Linux Wake-on-Lan Server Host Info"
        )

            host.value?.let {
                TextField(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    value = it,
                    onValueChange = { data -> host.value = data },
                    singleLine = true,
                    placeholder = { Text("Host IP") })
            }



            TextField(
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                value = port.value,
                onValueChange = { data -> port.value = data},
                singleLine = true,
                placeholder = { Text("Port\t(Default 22)") })



            user.value?.let {
                TextField(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    value = it,
                    onValueChange = { data -> user.value = data },
                    singleLine = true,
                    placeholder = { Text("Username") })
            }




            pass.value?.let {
                TextField(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    value = it,
                    onValueChange = { data -> pass.value = data },
                    singleLine = true,
                    placeholder = { Text("Password") },
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (visible)
                            painterResource(id = R.drawable.visable)
                        else painterResource(id = R.drawable.visibility_off)

                        IconButton(onClick = {visible = !visible}){
                            Icon(image, "visibility")
                        }
                    }
                )

        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    editor.apply {
                        putString("host", host.value)
                        putInt("port", port.value.toInt())
                        putString("user", user.value)
                        putString("pass", pass.value)
                        commit()
                    }
                }
            ) {
                Text(text = "Save")
            }

            Spacer(modifier = Modifier.width(20.dp))

            Button(
                onClick = onUpdate
            ) {
                Text(text = "Continue")
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }


}

@Composable
fun Main(settings: SharedPreferences, pcs: SharedPreferences, start: Int) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val connected = rememberSaveable { mutableIntStateOf(start) }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            var temp: Int
            while (true) {
                if (connected.intValue != 2) {
                    val answer = async {
                        executeCommand(
                            settings.getString("host",""),
                            settings.getString("user",""),
                            settings.getString("pass",""),
                            settings.getInt("port",22),
                            "echo hello"
                        )
                    }
                    val ret = answer.await()
                    temp = if (ret.contains("Error")) -1
                    else 1
                    withContext(Dispatchers.Main) {
                        connected.intValue = temp
                    }
                    delay(15000L)
                }
            }
        }
    }

    Column(
        Modifier
            .size(width = screenWidth, height = screenHeight),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when (connected.intValue) {
            -1 -> Error(settings, onUpdate = { connected.intValue = 2 })
            0 -> Connecting(settings)
            1 -> HomeScreen(pcs, settings, onUpdate = { connected.intValue = 2 })
            2 -> Settings(settings, onUpdate = { connected.intValue = 0 })
        }
    }
}

@Composable
fun Connecting(settings: SharedPreferences) {
    Column(
        Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            text = "Connecting To Host\n${settings.getString("host","")}"
        )
        CircularProgressIndicator(
            modifier = Modifier
                .width(180.dp)
                .offset(y = (-80).dp)
        )
        Text(
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            text = "Please Wait..."
        )
    }
}

@Composable
fun Error(settings: SharedPreferences, onUpdate: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 120.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            text = "Connection Error"
        )
        Spacer(modifier = Modifier.height(40.dp))
        Image(
            modifier = Modifier
                .size(150.dp),
            painter = painterResource(id = R.drawable.error),
            contentDescription = "error"
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            text = "Couldn't Connect To\n${settings.getString("host","")}"
        )
        Spacer(modifier = Modifier.height(180.dp))
        Text(
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            text = "Try A Different Host?"
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onUpdate
        ) {
            Text(text = "Settings")
        }
    }
}

@Composable
fun HomeScreen(pcs: SharedPreferences, settings: SharedPreferences, onUpdate: () -> Unit) {
    val context = LocalContext.current


    Row(
        Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(color = Color.LightGray),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .heightIn()
                .offset(x = 5.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            text = "Current WoL Host:"
        )
        settings.getString("host","")?.let {
            Text(
                modifier = Modifier
                    .heightIn()
                    .offset(x = (-30).dp, y = 2.dp),
                color = Color(0xFF009900),
                text = it
            )
        }
        Button(
            modifier = Modifier
                .width(40.dp)
                .padding(0.dp)
                .offset(x = (-5).dp),
            onClick = onUpdate
        ) {
            Image(
                modifier = Modifier
                    .requiredSize(20.dp)
                    .offset(x = (-4).dp),
                painter = painterResource(id = R.drawable.baseline_settings_24),
                contentDescription = "Settings"
            )
        }
    }

    Column(
        Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        pcs.all.forEach() {
            var arp = ArrayList<ArrayList<String>>()
            val sts = remember {
                mutableIntStateOf(R.drawable.wait)
            }
            val name = it.key
            val mac = it.value.toString()
            val update = remember {
                mutableStateOf(false)
            }
            var ip: String

            LaunchedEffect(Unit) {
                CoroutineScope(Dispatchers.Default).launch {
                    arp = getArpTable(context)
                    ip = getCurrentIPv4(mac, arp)
                    while (true) {
                        if (ip != "" && !update.value) sts.intValue = getStatus(ip, context)
                        if (ip == "" && arp.size != 0 && !update.value) sts.intValue =
                            R.drawable.off
                        delay(7000L)
                    }
                }
            }

            Row(
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
                        if (sts.intValue == R.drawable.off) {
                            sts.intValue = R.drawable.wait
                            update.value = true
                            arp.clear()
                            CoroutineScope(Dispatchers.IO).launch {
                                executeCommand(
                                    settings.getString("host",""),
                                    settings.getString("user",""),
                                    settings.getString("pass",""),
                                    settings.getInt("port",22),
                                    "sudo etherwake -i eth0 $mac"
                                )
                                delay(45000L)
                                withContext(Dispatchers.Main) {
                                    arp = getArpTable(context)
                                    ip = getCurrentIPv4(mac, arp)
                                    update.value = false
                                }
                            }
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
                    contentDescription = "status"
                )
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