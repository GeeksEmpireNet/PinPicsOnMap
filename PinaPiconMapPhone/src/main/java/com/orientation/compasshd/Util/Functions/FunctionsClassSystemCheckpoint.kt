/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 3:13 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.chat.vicinity.Util.FunctionsClass

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.orientation.compasshd.Util.Functions.PublicVariable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.ipify.Ipify
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL


class FunctionsClassSystemCheckpoint (var context: Context) {

    fun networkConnection(): Boolean {
        var networkAvailable = false

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork

        if (connectivityManager.getNetworkCapabilities(activeNetwork) != null) {
            return false
        } else {
            return connectivityManager.getNetworkCapabilities(activeNetwork).hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || connectivityManager.getNetworkCapabilities(activeNetwork).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || connectivityManager.getNetworkCapabilities(activeNetwork).hasTransport(NetworkCapabilities.TRANSPORT_VPN) }
    }

    fun GetNetworkAddressIP() = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {

        if (networkConnection()) {
            var ipAddress: String? = null

            //"https://checkip.amazonaws.com"
            //"https://geeksempire.net/NetworkInformation/GetNetworkIP.html"
            val lookupIP: String = "https://checkip.amazonaws.com"
            val URLlookupIP = URL(lookupIP)
            var bufferedReader: BufferedReader? = null
            try {
                bufferedReader = BufferedReader(InputStreamReader(URLlookupIP.openStream()))
                ipAddress = bufferedReader!!.readLine()
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            if (ipAddress!!.isEmpty()) {
                ipAddress = Ipify.getPublicIp()
            }

            PublicVariable.PUBLIC_INTERNET_IP = ipAddress.toString()
        }
    }
}