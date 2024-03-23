package com.example.Wake_My_PCs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.Wake_My_PCs.SSHManager.executeCommand
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.getString
import com.example.Wake_My_PCs.R.*
import kotlinx.coroutines.flow.internal.NoOpContinuation.context
import kotlin.coroutines.jvm.internal.CompletedContinuation.context


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Main()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Main(){
    val host = stringResource(id = string.user)
    val port = stringResource(id = string.user).toInt()
    val user = stringResource(id = string.user)
    val pass = stringResource(id = string.user)
    Column (
        Modifier
            .fillMaxSize()
            .padding(90.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Button(contentPadding = PaddingValues(30.dp),
            onClick = {
                executeCommand(host, user, pass, port,
                    "sudo etherwake -i eth0 9C:6B:00:30:15:52")
            }) {
            Text(text = "Green PC")
        }
        Button(contentPadding = PaddingValues(30.dp),
            onClick = {
                executeCommand(host, user, pass, port,
                    "sudo etherwake -i eth0 04:D9:F5:7C:FB:33")
            },
            modifier = Modifier.padding(30.dp)
        ) {
            Text(text = "Soul PC")
        }
    }
}

/*
fun check(){
    nc -z hostname 22 > /dev/null
    echo $?
}