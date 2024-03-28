package com.example.Wake_My_PCs

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object SuspendFunctions {
    private suspend fun ping(ip:String, context: Context): Int{
        val host = context.resources.getString(R.string.host)
        val port = context.resources.getString(R.string.port).toInt()
        val user = context.resources.getString(R.string.user)
        val pass = context.resources.getString(R.string.pass)
        val ret: Int

        val answer = CoroutineScope(Dispatchers.IO).async {
            SSHManager.executeCommand(
                host, user, pass, port,
                "ping -c 1 $ip"
            )
        }
        val res = answer.await()
        if (res.contains("Error")) return -1
        else
            ret = res.split("\n").dropLast(2).last().split(", ")[1].split(" ")[0].toInt()
        return ret
    }

    suspend fun getStatus(ip: String, context: Context): Int {
        var ret = 0
        val res = CoroutineScope(Dispatchers.IO).async { ping(ip, context) }
        when (res.await()) {
            1 -> ret = R.drawable.on
            0 -> ret = R.drawable.off
            -1-> ret = R.drawable.wait
        }
        return ret
    }

    suspend fun getArpTable(context: Context): ArrayList<ArrayList<String>> {
        val host = context.resources.getString(R.string.host)
        val port = context.resources.getString(R.string.port).toInt()
        val user = context.resources.getString(R.string.user)
        val pass = context.resources.getString(R.string.pass)
        val arp = ArrayList<ArrayList<String>>()

        val answer = CoroutineScope(Dispatchers.IO).async {
            SSHManager.executeCommand(
                host, user, pass, port,
                "sudo arp-scan -l"
            )
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

    suspend fun getCurrentIPv4(mac: String, arp: ArrayList<ArrayList<String>>): String{
        var ip = ""
        while (arp.size == 0) delay(15000L)
        run loop@{
            arp.forEach {
                if (mac.uppercase() == it[1]) {
                    withContext(Dispatchers.Main) { ip = it[0] }
                    return@loop
                }
            }
        }
        return ip
    }
}