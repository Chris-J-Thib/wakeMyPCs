package com.wakeMyPCs

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Wake_My_PCs.R
import com.wakeMyPCs.SSHManager.executeCommand
import com.wakeMyPCs.SuspendFunctions.getArpTable
import com.wakeMyPCs.SuspendFunctions.getCurrentIPv4
import com.wakeMyPCs.SuspendFunctions.getStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = getSharedPreferences("settings", MODE_PRIVATE)
        val pcs = getSharedPreferences("pcs", MODE_PRIVATE)

        var start = 0

        if (settings.all.isEmpty()) start = 2

        setContent {
            Main(settings, pcs, start)
        }
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
                            settings.getString("host", ""),
                            settings.getString("user", ""),
                            settings.getString("pass", ""),
                            settings.getInt("port", 22),
                            "sudo echo 'hello'"
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

@SuppressLint("ApplySharedPref")
@Composable
fun Settings(settings: SharedPreferences, onUpdate: () -> Unit) {
    val editor = settings.edit()
    val initPort = if (settings.getInt("port", -1).toString() == "-1") ""
    else settings.getInt("port", -1).toString()
    val host = remember {
        mutableStateOf(settings.getString("host", ""))
    }
    val port = remember {
        mutableStateOf(initPort)
    }
    val user = remember {
        mutableStateOf(settings.getString("user", ""))
    }
    val pass = remember {
        mutableStateOf(settings.getString("pass", ""))
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
            text = stringResource(R.string.linux_wake_on_lan_server_host_info)
        )

        host.value?.let {
            TextField(
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                value = it,
                onValueChange = { data -> host.value = data },
                singleLine = true,
                placeholder = { Text("xxx.xxx.xxx.xxx") },
                label = { Text(stringResource(R.string.host_ip)) })
        }

        TextField(
            modifier = Modifier
                .width(200.dp)
                .height(50.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            value = port.value,
            onValueChange = { data -> port.value = data },
            singleLine = true,
            label = { Text(stringResource(R.string.port)) },
            placeholder = { Text(stringResource(R.string.default_22)) })

        user.value?.let {
            TextField(
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                value = it,
                onValueChange = { data -> user.value = data },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.pi)) },
                label = { Text(stringResource(R.string.username)) })
        }

        pass.value?.let {
            TextField(
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                value = it,
                onValueChange = { data -> pass.value = data },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.password)) },
                label = { Text(stringResource(id = R.string.password)) },
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (visible)
                        painterResource(id = R.drawable.visable)
                    else painterResource(id = R.drawable.visibility_off)

                    IconButton(onClick = { visible = !visible }) {
                        Icon(image, stringResource(R.string.visibility))
                    }
                }
            )

        }

        Spacer(modifier = Modifier.height(10.dp))


        Button(
            onClick = {
                val portVal = if (port.value == "") 22 else port.value.toInt()
                editor.apply {
                    putString("host", host.value)
                    putInt("port", portVal)
                    putString("user", user.value)
                    putString("pass", pass.value)
                    commit()
                }
                onUpdate()
            }
        ) {
            Text(stringResource(R.string.save_update))
        }


        Spacer(modifier = Modifier.height(60.dp))
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
            text = stringResource(R.string.connecting_to_host) +settings.getString("host", "")
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
            text = stringResource(R.string.please_wait)
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
            text = stringResource(R.string.connection_error)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Image(
            modifier = Modifier
                .size(150.dp),
            painter = painterResource(id = R.drawable.error),
            contentDescription = stringResource(R.string.error)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            text = stringResource(R.string.couldn_t_connect_to)+ settings.getString("host","")
        )
        Spacer(modifier = Modifier.height(180.dp))
        Text(
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            text = stringResource(R.string.try_a_different_host)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onUpdate
        ) {
            Text(stringResource(id = R.string.settings))
        }
    }
}

@SuppressLint("ApplySharedPref")
@Composable
fun HomeScreen(pcs: SharedPreferences, settings: SharedPreferences, onUpdate: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val bannerHeight = 50.dp
    val count = remember {
        mutableIntStateOf(pcs.all.size)
    }
    val showAddPC = remember {
        mutableStateOf(false)
    }
    if (showAddPC.value) AddPC(pcs,
        onAdd = { showAddPC.value = false; count.intValue = pcs.all.size },
        onCancel = { showAddPC.value = false })
    Row(
        Modifier
            .height(bannerHeight)
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
            text = stringResource(R.string.current_wol_host)
        )
        settings.getString("host", "")?.let {
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
                contentDescription = stringResource(R.string.settings)
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .size(screenWidth, screenHeight - bannerHeight),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (count.intValue >= 0) {
            pcs.all.forEach { pc ->
                item(key = pc.value) {
                    val sts = remember { mutableIntStateOf(R.drawable.wait) }
                    val update = remember { mutableStateOf(false) }
                    val ip = remember { mutableStateOf("")}
                    var arp = ArrayList<ArrayList<String>>()
                    val name = pc.key
                    val mac = pc.value.toString()

                    LaunchedEffect(Unit) {
                        CoroutineScope(Dispatchers.Default).launch {
                            arp = getArpTable(settings)
                            ip.value = getCurrentIPv4(mac, arp)
                            while (true) {
                                if (ip.value != "" && !update.value) sts.intValue =
                                    getStatus(ip.value, settings)
                                if (ip.value == "" && arp.size != 0 && !update.value) sts.intValue =
                                    R.drawable.off
                                delay(5000L)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                            settings.getString("host", ""),
                                            settings.getString("user", ""),
                                            settings.getString("pass", ""),
                                            settings.getInt("port", 22),
                                            "sudo etherwake -i eth0 $mac"
                                        )
                                        delay(30000L)
                                        withContext(Dispatchers.Main) {
                                            arp = getArpTable(settings)
                                            ip.value = getCurrentIPv4(mac, arp)
                                            sts.intValue = getStatus(ip.value, settings)
                                            update.value = false
                                        }
                                    }
                                }
                            }
                        ) {
                            Text(text = name, overflow = TextOverflow.Ellipsis, maxLines = 1)
                        }

                        Image(
                            painter = painterResource(id = sts.intValue),
                            contentDescription = stringResource(R.string.status)
                        )

                        Button(
                            modifier = Modifier
                                .width(40.dp),
                            colors = ButtonDefaults.buttonColors(Color(0xffAA0000)),
                            onClick = {
                                pcs.edit().remove(name).commit()
                                count.intValue = pcs.all.size
                            }
                        ) {
                            Image(
                                modifier = Modifier
                                    .requiredSize(20.dp)
                                    .offset(x = (-4).dp),
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = stringResource(R.string.delete)
                            )
                        }

                    }
                }
            }
        }

        item {
            Button(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(150.dp)
                    .offset(x = (-100).dp),
                onClick = { showAddPC.value = true }
            ) {
                Text(stringResource(R.string.add_pc))
            }
        }
    }
}


@SuppressLint("ApplySharedPref")
@Composable
fun AddPC(pcs: SharedPreferences, onAdd: () -> Unit, onCancel: () -> Unit) {
    val newName = remember {
        mutableStateOf("")
    }
    val newMac = remember {
        mutableStateOf("")
    }
    AlertDialog(
        onDismissRequest = {
            onCancel()
        },
        title = { Text(stringResource(R.string.add_pc)) },
        confirmButton = {
            Button(onClick = {
                // Add a new item to pcs
                if (newName.value.isNotBlank() && newMac.value.isNotBlank()) {
                    pcs.edit().apply {
                        putString(newName.value, newMac.value)
                        commit()
                    }
                }
                onAdd()
            }) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            Button(onClick = {
                onCancel()
            }) {
                Text(stringResource(R.string.cancel))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                TextField(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    value = newName.value,
                    onValueChange = { newName.value = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.my_pc)) },
                    label = { Text(stringResource(R.string.pc_name)) })

                Spacer(modifier = Modifier.height(40.dp))

                TextField(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    value = newMac.value,
                    onValueChange = { newMac.value = it },
                    singleLine = true,
                    placeholder = { Text("FF:FF:FF:FF:FF:FF") },
                    label = { Text(stringResource(R.string.mac_address)) })
            }
        }
    )
}